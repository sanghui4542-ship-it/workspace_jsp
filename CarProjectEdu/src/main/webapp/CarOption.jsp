<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%
	request.setCharacterEncoding("UTF-8");
	String contextPath = request.getContextPath();

	/*
	 [AI 예약 비서 연동] 앞 화면(CarInfo.jsp)이 hidden 으로 넘겨준
	 시작일(prefillBegin)과 일수(prefillDays)가 있으면 입력칸을 미리 채운다.

	 [보안] 이 값들의 출발점은 주소창 파라미터이므로 여기서도 형식 검사를 한다.
	        (value 속성에 검사 없이 넣으면 따옴표로 속성을 끊는 XSS 통로가 된다)
	*/
	String preBegin = request.getParameter("prefillBegin");
	String preDays  = request.getParameter("prefillDays");

	if (preBegin == null || !preBegin.matches("\\d{4}-\\d{2}-\\d{2}")) {
		preBegin = "";
	}
	int preDaysNum = 0;   //0 = 미리 채울 값 없음
	if (preDays != null && preDays.matches("\\d{1,2}")) {
		preDaysNum = Integer.parseInt(preDays);
		if (preDaysNum < 1 || preDaysNum > 5) {
			preDaysNum = 0;   //이 화면의 선택지는 1~5일이다. 벗어나면 채우지 않는다
		}
	}
	pageContext.setAttribute("preDaysNum", preDaysNum);
%>
<%--
 ================================================================================
   CarOption.jsp  -  추가 옵션 선택 화면 (예약 흐름 2단계)

   [6단계 전면 재작성]

   ------------------------------------------------------------------------------
   ★ 가장 중요한 수정 : 화면에 적힌 요금이 실제 결제금액과 달랐다

     (기존 코드)
         <option value="1">적용(1일 1만원)</option>   <- 무선 WiFi
         <option value="1">적용(무료)</option>         <- 네비게이션

     (서버의 실제 요금 : CarService 상수)
         자차보험 10,000 / WiFi 5,000 / 네비게이션 3,000 / 베이비시트 10,000

     즉
       - WiFi 는 5,000원인데 화면에는 10,000원으로 안내 (2배)
       - 네비게이션은 3,000원인데 화면에는 "무료" 로 안내

     "무료"라고 안내하고 요금을 청구하면 실서비스에서는 분쟁 사유가 된다.

     원인은 같은 숫자를 화면과 서버에 각각 적어둔 것이다. 한쪽만 고치면 조용히 어긋난다.
     이제 화면은 CarService 상수를 그대로 받아서 표시한다(단일 기준).

   ------------------------------------------------------------------------------
   ★ 두 번째 수정 : 총액을 미리 알 수 없었다

     기존에는 옵션을 다 고르고 [예약정보계산] 을 눌러 다음 화면으로 넘어가야
     비로소 총액을 볼 수 있었다. 금액이 마음에 안 들면 되돌아와야 했다.

     지금은 옵션을 바꿀 때마다 화면 아래 총액이 즉시 바뀐다.
     (계산식은 서버와 동일 : (차량요금 + 옵션합계) x 대여일수 x 대여수량)

     [주의] 이 화면의 계산은 "미리 보여주기" 용도다.
            실제 결제금액은 서버가 DB의 차량 요금을 다시 조회해 계산한다.
            화면 계산만 믿으면 hidden 값을 조작해 금액을 바꿀 수 있기 때문이다.

   ------------------------------------------------------------------------------
   ★ 그 외 개선
     - 이모지(🚗) 플레이스홀더 -> 실제 차량 사진
     - 표(table) 레이아웃 -> label + form-control (모바일에서 1열로 쌓임)
     - 대여일에 min(오늘) 지정 : 지난 날짜로 예약하는 것을 막는다
     - 다음 화면에서도 사진을 쓸 수 있도록 carimg / carname 을 함께 전달
 ================================================================================
--%>

<%-- 서버가 내려준 옵션 단가 (없을 때를 대비한 기본값 포함) --%>
<c:set var="pIns"  value="${empty priceInsurance ? 10000 : priceInsurance}"/>
<c:set var="pWifi" value="${empty priceWifi      ?  5000 : priceWifi}"/>
<c:set var="pNavi" value="${empty priceNavi      ?  3000 : priceNavi}"/>
<c:set var="pBaby" value="${empty priceBabyseat  ? 10000 : priceBabyseat}"/>

<div class="container">

	<div class="section-head">
		<span class="section-eyebrow">STEP 2 / 3</span>
		<h2 class="section-heading">옵션 선택</h2>
		<p class="section-desc">필요한 옵션만 고르세요. 총액이 아래에서 바로 계산됩니다</p>
	</div>

	<form action="<%=contextPath%>/Car/CarOptionResult.do" method="post" id="optionForm">

		<%-- [보안] CSRF 토큰 : 이 화면에서 출발한 요청임을 증명한다 (설명은 members/login.jsp) --%>
		<input type="hidden" name="_csrf" value="${_csrf}">

		<%-- 앞 화면에서 넘어온 예약 정보 (다음 화면으로도 이어서 전달한다) --%>
		<input type="hidden" name="carno"    value="${param.carno}">
		<input type="hidden" name="carqty"   value="${param.carqty}">
		<input type="hidden" name="carprice" value="${param.carprice}">
		<%-- [추가] 다음 화면에서도 차량 사진과 이름을 보여주기 위해 함께 전달 --%>
		<input type="hidden" name="carimg"   value="${param.carimg}">
		<input type="hidden" name="carname"  value="${param.carname}">

		<div class="grid-2">

			<%-- ================= 왼쪽 : 선택한 차량 ================= --%>
			<div>
				<div class="card">
					<div class="car-photo">
						<c:choose>
							<c:when test="${not empty param.carimg}">
								<img src="<%=contextPath%>/img/${param.carimg}"
									 alt="${param.carname} 차량 사진">
							</c:when>
							<c:otherwise>
								<%-- 사진 정보가 없을 때만 대체 표시 --%>
								<div class="flex items-center justify-center" style="height:100%;">
									<span class="text-muted">사진 준비중</span>
								</div>
							</c:otherwise>
						</c:choose>
					</div>

					<div class="car-body">
						<div class="car-name">
							<c:out value="${empty param.carname ? '선택한 차량' : param.carname}"/>
						</div>
						<div class="car-price-row">
							<div class="car-price">
								<fmt:formatNumber value="${param.carprice}" pattern="#,###"/>원
								<span class="unit">/ 1일 · 1대</span>
							</div>
							<span class="badge">${param.carqty}대 예약</span>
						</div>
					</div>
				</div>
			</div>

			<%-- ================= 오른쪽 : 옵션 입력 ================= --%>
			<div>

				<div class="form-group">
					<label class="form-label" for="carbegindate">대여 시작일 <span class="required">*</span></label>
					<%-- min 은 아래 스크립트가 "오늘" 로 설정한다 (지난 날짜 선택 차단)
					     [AI 예약 비서 연동] 앞 화면에서 넘어온 시작일이 있으면 미리 채운다 --%>
					<input class="form-control" type="date" id="carbegindate" name="carbegindate"
						   value="<%=preBegin%>" required>
				</div>

				<div class="grid-2">
					<div class="form-group">
						<label class="form-label" for="carreserveday">대여 기간</label>
						<select class="form-control" id="carreserveday" name="carreserveday">
							<%-- [AI 예약 비서 연동] 넘어온 일수(preDaysNum)가 있으면 미리 선택한다 --%>
							<c:forEach var="d" begin="1" end="5">
								<option value="${d}" <c:if test="${d == preDaysNum}">selected</c:if>>${d}일</option>
							</c:forEach>
						</select>
					</div>

					<div class="form-group">
						<label class="form-label">대여 수량</label>
						<%-- 수량은 앞 화면에서 이미 골랐다. 바꾸려면 되돌아가도록 안내한다. --%>
						<input class="form-control" type="text" value="${param.carqty}대" disabled>
					</div>
				</div>

				<%--
				 옵션 선택
				   각 select 에 data-price 로 1일 요금을 담아두고
				   스크립트가 그 값을 읽어 총액을 계산한다.
				   화면에 표시하는 금액과 계산에 쓰는 금액이 같은 값이라 어긋날 수 없다.
				--%>
				<div class="form-group">
					<label class="form-label">추가 옵션</label>

					<div class="price-list">

						<div class="price-item">
							<span class="name">&#128737; 자차보험</span>
							<select class="form-control option-select" name="carins"
									data-price="${pIns}" style="max-width:150px;">
								<option value="0">미적용</option>
								<option value="1">적용 +<fmt:formatNumber value="${pIns}" pattern="#,###"/>원</option>
							</select>
						</div>

						<div class="price-item">
							<span class="name">&#128246; 무선 WiFi</span>
							<select class="form-control option-select" name="carwifi"
									data-price="${pWifi}" style="max-width:150px;">
								<option value="0">미적용</option>
								<option value="1">적용 +<fmt:formatNumber value="${pWifi}" pattern="#,###"/>원</option>
							</select>
						</div>

						<div class="price-item">
							<span class="name">&#128506; 네비게이션</span>
							<select class="form-control option-select" name="carnave"
									data-price="${pNavi}" style="max-width:150px;">
								<option value="0">미적용</option>
								<option value="1">적용 +<fmt:formatNumber value="${pNavi}" pattern="#,###"/>원</option>
							</select>
						</div>

						<div class="price-item">
							<span class="name">&#128118; 베이비시트</span>
							<select class="form-control option-select" name="carbabyseat"
									data-price="${pBaby}" style="max-width:150px;">
								<option value="0">미적용</option>
								<option value="1">적용 +<fmt:formatNumber value="${pBaby}" pattern="#,###"/>원</option>
							</select>
						</div>

					</div>
					<p class="form-hint">모든 옵션 요금은 1일 · 1대 기준입니다</p>
				</div>

			</div>
		</div>


		<%-- =====================================================================
		     예상 결제금액 (옵션을 바꿀 때마다 즉시 갱신)
		     ===================================================================== --%>
		<div class="section-soft mt-6">

			<div class="detail-list">
				<div class="detail-row">
					<dt>차량 요금</dt>
					<dd id="sumBase">-</dd>
				</div>
				<div class="detail-row">
					<dt>옵션 요금</dt>
					<dd id="sumOption">-</dd>
				</div>
			</div>

			<div class="action-bar mt-4">
				<div class="action-total">
					<div class="label">예상 결제금액</div>
					<div class="price" id="sumTotal">-</div>
				</div>
				<button type="submit" class="btn btn-primary btn-lg">예약 정보 확인</button>
			</div>

			<p class="form-hint mt-2">
				&#8226; 최종 결제금액은 다음 화면에서 서버가 다시 계산해 확정합니다.
			</p>
		</div>

		<div class="flex flex-wrap gap-2 mt-4">
			<a class="btn btn-ghost" href="<%=contextPath%>/Car/CarInfo.do?carno=${param.carno}">
				&lsaquo; 차량 다시 선택
			</a>
			<a class="btn btn-ghost" href="<%=contextPath%>/Car/CarList.do">차량 목록</a>
		</div>

	</form>
</div>

<script>
/* ============================================================================
   예상 결제금액 실시간 계산
   계산식은 서버(CarService)와 동일하다.
       총액 = (차량 1일요금 + 옵션 1일 합계) x 대여일수 x 대여수량
   [중요] 이 계산은 사용자에게 "미리 보여주기" 위한 것이다.
          실제 결제금액은 서버가 DB의 차량 요금을 다시 조회해 계산한다.
          화면 값(hidden)은 조작할 수 있으므로 절대 기준이 될 수 없다.
   ============================================================================ */
(function () {
	//앞 화면에서 넘어온 값 (숫자로 변환. 값이 없으면 0)
	var carPrice = parseInt("${empty param.carprice ? 0 : param.carprice}", 10) || 0;
	// carQty — 조건에 따라 둘 중 하나를 담는다
	var carQty   = parseInt("${empty param.carqty   ? 1 : param.carqty}",   10) || 1;   // 화면에서 고른 대수. 값이 없으면 1대로 본다.  parseInt(글자, 10) 은 10진수로 바꾸라는 뜻이다
	// 화면에서 id 가 "carreserveday" 인 요소를 찾는다
	var dayEl     = document.getElementById("carreserveday");   // 대여 일수 선택칸을 찾는다
	// 화면에서 ".option-select" 에 해당하는 요소들을 전부 찾는다
	var optionEls = document.querySelectorAll(".option-select");   // 옵션 선택칸 4개를 모두 찾는다
	// 화면에서 id 가 "carbegindate" 인 요소를 찾는다
	var dateEl    = document.getElementById("carbegindate");   // 대여 시작일 입력칸을 찾는다
	/* 지난 날짜로 예약하지 못하게 오늘을 최소값으로 지정한다.
	   (서버도 ParamUtil.getRequiredDate 로 날짜 형식을 다시 검사한다) */
	if (dateEl) {
		// Date 그릇을 새로 하나 만들어 today 라는 이름으로 잡아 둔다
		var today = new Date();
		// m — String( ) 의 결과를 담는다
		var m = String(today.getMonth() + 1).padStart(2, "0");   // 월을 두 자리로 맞춘다 (8 -> "08"). padStart 가 앞을 0으로 채운다
		// d — String( ) 의 결과를 담는다
		var d = String(today.getDate()).padStart(2, "0");   // 일도 두 자리로 맞춘다
		// min 에 getFullYear( ) 의 결과를 담는다
		dateEl.min = today.getFullYear() + "-" + m + "-" + d;   // 달력에서 오늘보다 앞선 날짜를 못 고르게 막는다
	}
	/** 숫자에 천단위 쉼표를 붙인다 (45000 -> 45,000) */
	function won(n) {
		return n.toLocaleString("ko-KR") + "원";   // toLocaleString("ko-KR") 이 천 단위 쉼표를 넣어 준다 (45000 -> 45,000)
	}
	/** 선택된 옵션들의 1일 요금 합계 */
	function optionPerDay() {
		// sum — 계산한 값을 담는다
		var sum = 0;   // 합계를 담을 변수. 0부터 더해 나간다
		// optionEls 의 forEach( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
		optionEls.forEach(function (sel) {   // 옵션 선택칸을 하나씩 확인한다
			//"1"(적용)을 고른 경우에만 data-price 를 더한다
			if (sel.value === "1") {
				sum += parseInt(sel.getAttribute("data-price"), 10) || 0;
			}
		});
		return sum;   // 고른 옵션들의 1일 합계를 돌려준다
	}
	/** 화면의 금액 표시를 다시 계산해서 갱신 */
	function refresh() {
		// days — 조건에 따라 둘 중 하나를 담는다
		var days = parseInt(dayEl ? dayEl.value : 1, 10) || 1;   // 고른 대여 일수. 값이 없거나 숫자가 아니면 1일로 본다
		// base — 계산한 값을 담는다
		var base   = carPrice * carQty * days;          //차량 요금
		// option — optionPerDay( ) 의 결과를 담는다
		var option = optionPerDay() * carQty * days;    //옵션 요금
		// 화면에서 id 가 "sumBase" 인 요소를 찾는다
		document.getElementById("sumBase").textContent =   // 차량 요금 계산식을 그대로 화면에 보여 준다 (왜 이 금액인지 알 수 있게)
			// won( ) 를 실행한다 (직접 만든 도우미)
			won(carPrice) + " x " + carQty + "대 x " + days + "일 = " + won(base);
		// 화면에서 id 가 "sumOption" 인 요소를 찾는다
		document.getElementById("sumOption").textContent =   // 옵션 요금도 계산식과 함께 보여 준다
			(optionPerDay() === 0)
				? "선택한 옵션 없음"
				: won(optionPerDay()) + " x " + carQty + "대 x " + days + "일 = " + won(option);
		// 화면에서 id 가 "sumTotal" 인 요소를 찾는다
		document.getElementById("sumTotal").textContent = won(base + option);   // 차량 요금과 옵션 요금을 더해 최종 금액을 표시한다
	}
	//옵션이나 기간을 바꿀 때마다 다시 계산
	if (dayEl) { dayEl.addEventListener("change", refresh); }
	// "change" 사건이 일어나면 실행할 동작을 걸어 둔다
	optionEls.forEach(function (sel) { sel.addEventListener("change", refresh); });
	//첫 진입 시에도 한 번 계산해 둔다
	refresh();
})();   // 이 괄호가 위에서 시작한 함수를 "바로 실행" 시킨다
</script>