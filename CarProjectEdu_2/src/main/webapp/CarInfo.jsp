<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%
    // 1. 웹 요청의 글자 인코딩을 'UTF-8'(유니코드)로 설정해서 한글이 깨지지 않게 한다.
    request.setCharacterEncoding("UTF-8");

    // 2. 현재 웹 프로젝트의 컨텍스트 경로(프로젝트 별칭, 예: /CarProject)를 변수에 저장한다.
    String contextPath = request.getContextPath();

    /*
      3. "AI 예약 비서 연동" 기능용
      
         - 다른 화면에서 '예약 이어하기'를 누르면CarInfo.do?carno=3&carqty=2&carbegindate=2026-08-07&carreserveday=3
           처럼 주소에 미리 선택한 값들이 딸려 올 수 있다.

         - 이 값들(수량, 시작일, 대여일수)을 준비해서 화면에도 미리 채우고,
           다음 옵션선택 화면으로도 전달해야 한다.

         - 주소의 파라미터는 사용자가 임의로 바꿀 수 있으니 '형식'을 꼭 검사한다.
           (ex. 날짜는 '2023-07-15' 형태, 수량은 1~5 사이 숫자만 허용)
    */
    // 3-1. "차량 수량" 파라미터를 받아온다 (없을 수도 있다)
    String prefillQty   = request.getParameter("carqty");
    // 3-2. "예약 시작일" 파라미터를 받아온다
    String prefillBegin = request.getParameter("carbegindate");
    // 3-3. "대여 기간(일수)" 파라미터를 받아온다
    String prefillDays  = request.getParameter("carreserveday");

    // 4. 미리 선택된 차량 수량(기본값 1)을 저장할 변수
    int qtySelected = 1;

    // 4-1. 수량이 null이 아니고 1~5중 하나면(유효한 숫자) -> 그 값을 수량으로 사용
    if (prefillQty != null && prefillQty.matches("[1-5]")) {
        qtySelected = Integer.parseInt(prefillQty);
    }

    // 4-2. 시작일이 없거나 YYYY-MM-DD형식이 아니면 빈 문자열로(값 없다고 처리)
    if (prefillBegin == null || !prefillBegin.matches("\\d{4}-\\d{2}-\\d{2}")) {
        prefillBegin = "";
    }

    // 4-3. 대여일수가 없거나 1~2자리 정수(10~99까지) 형식이 아니면 빈 문자열
    if (prefillDays == null || !prefillDays.matches("\\d{1,2}")) {
        prefillDays = "";
    }

    // 5. 위에서 계산한 수량을 JSP내부 변수("qtyPre")로 저장해서 아래 JSTL에서 쉽게 꺼내 쓸 수 있게 한다.
    pageContext.setAttribute("qtyPre", qtySelected);
%>

<%--
 ================================================================================
   CarInfo.jsp  -  차량 상세 + 대여 수량 선택 (예약 흐름 1단계)
     - 실제 사진 + 등급 배지
     - 45,000원 / "소형 · 준중형" / 5인승 처럼 사람이 읽는 형태로 표시
     - label + form-control 로 바꿔 모바일에서 위아래로 쌓인다
     - 다음 화면(옵션 선택)에서도 사진을 쓸 수 있도록 carname 을 함께 전달
 ================================================================================
--%>    
<div class="container">

	<div class="section-head">
		<span class="section-eyebrow">STEP 1 / 3</span>
		<h2 class="section-heading">차량 정보</h2>
		<p class="section-desc">대여 수량을 정하고 옵션 선택으로 이동하세요</p>
	</div>

	<%-- 조회된 차량 정보를 보고 대여수량을 선택해 옵션 선택 화면을 요청한다 --%>
	<form action="<%=contextPath%>/Car/CarOption.do" method="post">

		<%-- [보안] CSRF 토큰 : 이 화면에서 출발한 요청임을 증명한다 (설명은 members/login.jsp) --%>
		<input type="hidden" name="_csrf" value="${_csrf}">

		<%-- 옵션 선택 화면으로 예약할 차번호 / 사진 / 요금을 전달 --%>
		<input type="hidden" name="carno"    value="${requestScope.vo.carno}">
		<input type="hidden" name="carimg"   value="${requestScope.vo.carimg}">
		<input type="hidden" name="carprice" value="${requestScope.vo.carprice}">
		
		<%-- [추가] 다음 화면에서 차량명을 보여주기 위해 함께 전달 --%>
		<input type="hidden" name="carname"  value="${requestScope.vo.carname}">

		<%-- [AI 예약 비서 연동] 시작일/일수를 옵션 화면까지 전달한다 (값이 없으면 빈 문자열) --%>
		<input type="hidden" name="prefillBegin" value="<%=prefillBegin%>">
		<input type="hidden" name="prefillDays"  value="<%=prefillDays%>">

		<div class="grid-2">

			<%-- ================= 왼쪽 : 차량 사진 ================= --%>
			<div class="card">
				<div class="car-photo">
					<img src="<%=contextPath%>/img/${requestScope.vo.carimg}"
						 alt="${requestScope.vo.carname} 차량 사진">

					<%-- 등급 배지 : 색으로 소형/중형/대형을 구분 --%>
					<c:choose>
						<c:when test="${requestScope.vo.carcategory eq 'Small'}">
							<span class="car-badge car-badge-small">소형</span>
						</c:when>
						<c:when test="${requestScope.vo.carcategory eq 'Mid'}">
							<span class="car-badge car-badge-mid">중형</span>
						</c:when>
						<c:otherwise>
							<span class="car-badge car-badge-big">대형</span>
						</c:otherwise>
					</c:choose>
				</div>
			</div>

			<%-- ================= 오른쪽 : 상세 정보 + 수량 ================= --%>
			<div>

				<h3 class="car-name" style="font-size:var(--fs-2xl); margin-bottom:var(--sp-3);">
					${requestScope.vo.carname}
				</h3>

				<div class="car-meta mb-4">
					<span>${requestScope.vo.carcompany}</span>
					<span>${requestScope.vo.carusepeople}인승</span>
					<%-- 영문 코드(Small/Mid/Big)를 사용자가 읽는 말로 바꿔 표시한다 --%>
					<span>
						<c:choose>
							<c:when test="${requestScope.vo.carcategory eq 'Small'}">소형 · 준중형</c:when>
							<c:when test="${requestScope.vo.carcategory eq 'Mid'}">중형 · SUV</c:when>
							<c:otherwise>대형 · 승합</c:otherwise>
						</c:choose>
					</span>
				</div>

				<dl class="detail-list mb-4">
					<div class="detail-row">
						<dt>1일 대여료</dt>
						<dd>
							<strong class="text-brand" style="font-size:var(--fs-xl);">
								<fmt:formatNumber value="${requestScope.vo.carprice}" pattern="#,###"/>원
							</strong>
							<span class="text-muted fs-sm">/ 1대</span>
						</dd>
					</div>
					<div class="detail-row">
						<dt>탑승 인원</dt>
						<dd>최대 ${requestScope.vo.carusepeople}명</dd>
					</div>
					<div class="detail-row">
						<dt>제조사</dt>
						<dd>${requestScope.vo.carcompany}</dd>
					</div>
				</dl>

				<div class="form-group">
					<label class="form-label" for="carqty">대여 수량</label>
					<select class="form-control" id="carqty" name="carqty">
						<%-- AI 예약 비서가 계산해 준 대수(qtyPre)가 있으면 미리 선택해 둔다 --%>
						<c:forEach var="q" begin="1" end="5">
							<option value="${q}" <c:if test="${q == qtyPre}">selected</c:if>>${q}대</option>
						</c:forEach>
					</select>
					<p class="form-hint">여러 대가 필요하면 수량을 늘려주세요</p>
				</div>

				<div class="flex flex-wrap gap-2 mt-6">
					<button type="submit" class="btn btn-primary btn-lg">옵션 선택하기 &rsaquo;</button>
					<a class="btn btn-ghost" href="<%=contextPath%>/Car/CarList.do">&lsaquo; 목록으로</a>
				</div>

			</div>
		</div>

	</form>

	<%-- 차량 설명 : DB 의 carinfo 값 --%>
	<c:if test="${not empty requestScope.vo.carinfo}">
		<div class="section-soft mt-8">
			<h3 class="fw-bold mb-2">차량 안내</h3>
			<p class="text-muted">${requestScope.vo.carinfo}</p>
		</div>
	</c:if>

</div>
