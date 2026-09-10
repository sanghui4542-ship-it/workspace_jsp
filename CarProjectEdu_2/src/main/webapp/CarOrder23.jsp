<%-- 이 페이지의 글자 인코딩을 UTF-8 로 정한다. 빠뜨리면 한글이 깨진다 --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="Vo.CarOrderVO"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%
    request.setCharacterEncoding("UTF-8"); // [인코딩] 클라이언트에서 전송된 데이터의 문자 인코딩을 UTF-8로 설정하여 한글 데이터가 깨지지 않게 함

    String contextPath = request.getContextPath(); // [경로설정] 현재 웹 애플리케이션의 컨텍스트 경로(/myapp 등)를 가져와서, 이후 form action 등에서 절대경로로 사용

    Object voObj = request.getAttribute("vo"); // [예약정보] request 영역에 저장된 예약정보 객체(CarOrderVO 형태 예상)를 voObj에 할당

    int totalreserve = 0; // [금액초기화] 차량 기본 대여요금을 저장할 변수 선언 및 기본값 0으로 초기화
    int totaloption = 0;  // [금액초기화] 선택 옵션에 대한 추가요금을 저장할 변수 선언 및 기본값 0으로 초기화

    if (request.getAttribute("totalreserve") != null) { // [요금가져오기] request 영역에서 차량 요금(totalreserve)이 전달되어 있을 경우
        totalreserve = ((Number)request.getAttribute("totalreserve")).intValue(); // Object 타입을 Number로 캐스팅한 뒤 intValue()로 int 변환 후 할당
    }
    if (request.getAttribute("totaloption") != null) { // [옵션요금가져오기] request 영역에서 옵션 요금(totaloption)이 전달되어 있을 경우
        totaloption = ((Number)request.getAttribute("totaloption")).intValue(); // Object 타입을 Number로 캐스팅해서 int로 변환 후 할당
    }

    String paramCarname = request.getParameter("carname"); // [차량명] HTTP 파라미터에서 "carname" 값을 가져옴 (차량 명칭)
    boolean carnameEmpty = (paramCarname == null || paramCarname.trim().isEmpty()); // [차량명 체크] carname 파라미터가 null이거나 공백문자만 있는지 검사(차량 미선택 상태 확인)

    String paramCarimg = request.getParameter("carimg"); // [차량사진] 앞 화면에서 넘긴 파일명
    boolean carimgOk = paramCarimg != null && paramCarimg.matches("[A-Za-z0-9._-]+"); // 경로 조작 방지: 파일명만 허용

    CarOrderVO vo = null; // [예약정보 객체] CarOrderVO형 참조 변수 선언 및 null로 초기화
    if (voObj instanceof CarOrderVO) { // [vo 할당] 컨트롤러가 넣은 Vo.CarOrderVO이면
        vo = (CarOrderVO)voObj; // Object 타입에서 CarOrderVO 타입으로 캐스팅 후 vo에 할당(실예약정보 접근용)
    }

    Object csrfAttr = request.getAttribute("_csrf"); // [CSRF 토큰] request 영역에서 "_csrf" 속성(토큰값) 추출
    String csrfToken = csrfAttr == null ? "" : csrfAttr.toString(); // [CSRF 토큰 가공] 토큰값이 null이면 빈 문자열, 아니면 문자열로 변환해 csrfToken에 할당

    pageContext.setAttribute("totalreserve", Integer.valueOf(totalreserve));
    pageContext.setAttribute("totaloption", Integer.valueOf(totaloption));
    pageContext.setAttribute("grandTotal", Integer.valueOf(totalreserve + totaloption));
%>
<%--
 ================================================================================
   CarOrder.jsp (비회원 예약)  -  예약 최종 확인 및 요청

     - 차량 사진 + 예약 내역(차량/시작일/기간/수량/선택옵션)을 표로 확인
     - 금액을 천단위 쉼표로, 총액은 크게 강조
     - 비밀번호가 어디에 쓰이는지 안내 문구 추가
     - 모바일에서는 총액과 결제 버튼이 하단에 고정된다(app.css 의 .action-bar)
 ================================================================================
--%>
<div class="container">
	<div class="section-head">
		<span class="section-eyebrow">STEP 3 / 3</span>
		<h2 class="section-heading">예약 확인</h2>
		<p class="section-desc">내역과 금액을 확인한 뒤, 연락처와 비밀번호를 입력해 예약을 완료해 주세요</p>
	</div>

	<ol class="checkout-steps" aria-label="예약 단계">
		<li class="checkout-step is-done"><span class="num">1</span> 차량 선택</li>
		<li class="checkout-step is-done"><span class="num">2</span> 옵션 선택</li>
		<li class="checkout-step is-current" aria-current="step"><span class="num">3</span> 예약 확인</li>
	</ol>

	<%-- 최종 렌트 금액 확인 하고 CarController 사장에게  예약 요청 하는 <form> --%>
	<form action="<%=contextPath%>/Car/CarOrder.do" method="post"><%-- [action-경로] contextPath를 이용한 form 제출(POST) 목적지 url --%>

		<%-- [보안] CSRF 토큰 : 이 화면에서 출발한 요청임을 증명한다 --%>
		<input type="hidden" name="_csrf" value="<%=csrfToken %>"><%-- [CSRF] 위에서 구한 CSRF 토큰값을 숨은 필드로 전송 --%>
		<input type="hidden" name="carno"          value="<%= vo != null ? vo.getCarno() : "" %>" /><%-- [차량번호] vo에 값 있으면 전달 --%>
		<input type="hidden" name="carqty"         value="<%= vo != null ? vo.getCarqty() : "" %>" /><%-- [대여수량] vo에 값 있으면 전달 --%>
		<input type="hidden" name="carreserveday"  value="<%= vo != null ? vo.getCarreserveday() : "" %>" /><%-- [대여기간(일)] vo에 값 있으면 전달 --%>
		<input type="hidden" name="carbegindate"   value="<%= vo != null ? vo.getCarbegindate() : "" %>" /><%-- [대여시작일] vo에 값 있으면 전달 --%>
		<input type="hidden" name="carins"         value="<%= vo != null ? vo.getCarins() : "" %>" /><%-- [자차보험 선택] vo에 값 있으면 전달 --%>
		<input type="hidden" name="carwifi"        value="<%= vo != null ? vo.getCarwifi() : "" %>" /><%-- [와이파이선택] vo에 값 있으면 전달 --%>
		<input type="hidden" name="carnave"        value="<%= vo != null ? vo.getCarnave() : "" %>" /><%-- [네비게이션 선택] vo에 값 있으면 전달 --%>
		<input type="hidden" name="carbabyseat"    value="<%= vo != null ? vo.getCarbabyseat() : "" %>" /><%-- [베이비시트 선택] vo에 값 있으면 전달 --%>

		<div class="checkout-grid">
			<%-- ================= 왼쪽 : 예약 내역 ================= --%>
			<div>
				<div class="card">
					<div class="car-photo">
						<% if (carimgOk) { %>
							<img src="<%=contextPath%>/img/<%= paramCarimg %>" alt="선택한 차량 사진">
						<% } else { %>
							<div class="flex items-center justify-center" style="height:100%;">
								<span class="text-muted">사진 준비중</span>
							</div>
						<% } %>
					</div>
					<div class="car-body">
						<div class="car-name">
							<% if (carnameEmpty) { %>
								선택한 차량
							<% } else { %>
								<c:out value="${param.carname}"/>
							<% } %>
						</div>
						<p class="car-meta">예약 전에 기간·수량·옵션을 한 번 더 확인해 주세요</p>
					</div>
					<div class="checkout-facts">
						<div class="checkout-fact">
							<span class="k">대여 시작일</span>
							<div class="v"><%= vo != null && vo.getCarbegindate() != null ? vo.getCarbegindate() : "-" %></div>
						</div>
						<div class="checkout-fact">
							<span class="k">대여 기간</span>
							<div class="v"><%= vo != null ? vo.getCarreserveday() : "-" %>일</div>
						</div>
						<div class="checkout-fact">
							<span class="k">대여 수량</span>
							<div class="v"><%= vo != null ? vo.getCarqty() : "-" %>대</div>
						</div>
						<div class="checkout-fact">
							<span class="k">예약 단계</span>
							<div class="v">최종 확인</div>
						</div>
					</div>
					<div class="checkout-options">
						<%
						    boolean hasOption = false; // [옵션선택] 총 네가지 옵션 중 아무거나 하나라도 선택됐는지 확인용 플래그
						    if (vo != null) { // [옵션탐색] vo 객체가 있는 경우에만 옵션별 체크 수행
						        if (vo.getCarins() == 1) { // [자차보험] 1이면 선택됨
						            hasOption = true;
						 %>
						            <span class="badge badge-brand">자차보험</span>
						        <%
						        }
						        if (vo.getCarwifi() == 1) { // [와이파이] 1이면 선택됨
						            hasOption = true;
						 %>
						            <span class="badge badge-brand">무선 WiFi</span>
						        <%
						        }
						        if (vo.getCarnave() == 1) { // [네비게이션] 1이면 선택됨
						            hasOption = true;
						 %>
						            <span class="badge badge-brand">네비게이션</span>
						        <%
						        }
						        if (vo.getCarbabyseat() == 1) { // [베이비시트] 1이면 선택됨
						            hasOption = true;
						 %>
						            <span class="badge badge-brand">베이비시트</span>
						        <%
						        }
						    }
						    if (!hasOption) { // [옵션 미선택] 모든 옵션이 선택되지 않은 경우 안내문 출력
						 %>
								<span class="text-muted fs-sm">선택한 옵션 없음</span>
						<% } %>
					</div>
				</div>
			</div>

			<%-- ================= 오른쪽 : 금액 + 예약자 정보 ================= --%>
			<div>
				<div class="checkout-receipt">
					<div class="head">
						<div class="label">총 결제금액</div>
						<div class="amount"><fmt:formatNumber value="${grandTotal}" pattern="#,###"/>원</div>
						<div class="meta"><%= vo != null ? vo.getCarreserveday() : "" %>일 · <%= vo != null ? vo.getCarqty() : "" %>대 기준</div>
					</div>
					<div class="checkout-payline">
						<span class="k">차량 요금</span>
						<span class="v"><fmt:formatNumber value="${totalreserve}" pattern="#,###"/>원</span>
					</div>
					<div class="checkout-payline">
						<span class="k">옵션 요금</span>
						<span class="v"><fmt:formatNumber value="${totaloption}" pattern="#,###"/>원</span>
					</div>
					<div class="checkout-payline is-total">
						<span class="k">합계</span>
						<span class="v"><fmt:formatNumber value="${grandTotal}" pattern="#,###"/>원</span>
					</div>
				</div>

				<div class="checkout-form-card">
					<h3>예약 확인용 정보</h3>
					<p class="form-hint mb-4">비회원 예약입니다. 조회·변경·취소에 사용됩니다.</p>
					<div class="form-group">
						<label class="form-label" for="memberphone">연락처 <span class="required">*</span></label>
						<input class="form-control" type="tel" id="memberphone" name="memberphone" placeholder="010-1234-5678" autocomplete="tel" required>
					</div>
					<div class="form-group">
						<label class="form-label" for="memberpass">예약 비밀번호 <span class="required">*</span></label>
						<input class="form-control" type="password" id="memberpass" name="memberpass" placeholder="예약 조회·취소에 사용됩니다" autocomplete="new-password" required>
					</div>
					<div class="checkout-note">
						&#8226; 이 연락처와 비밀번호로 <strong>예약 조회 · 변경 · 취소</strong>를 합니다.<br>
						&#8226; 꼭 기억해 주세요. 분실 시 고객센터(02-3456-6574)로 문의해야 합니다.
					</div>
				</div>
			</div>
		</div>

		<div class="action-bar mt-6">
			<div class="action-total">
				<div class="label">
					총 결제금액 (<%= vo != null ? vo.getCarreserveday() : "" %>일 · <%= vo != null ? vo.getCarqty() : "" %>대)
					<%-- [액션바] vo가 null이 아닐 때만 대여일수, 대여수량을 해당 값으로, 아니면 빈값 처리 --%>
				</div>
				<div class="price">
					<fmt:formatNumber value="${grandTotal}" pattern="#,###"/>원 <%-- [합계] 차량요금 + 옵션요금 합산 결과를 3자리 콤마로 포맷, 최종 결제금액 표시 --%>
				</div>
			</div>
			<button type="submit" class="btn btn-primary btn-lg">예약 완료하기</button>
		</div>
		<div class="flex flex-wrap gap-2 mt-4">
			<a class="btn btn-ghost" href="<%=contextPath%>/Car/CarList.do">&lsaquo; 차량 다시 선택</a><%-- [목록링크] contextPath를 활용한 차량 목록 재선택 이동 --%>
		</div>
	</form>
</div>
