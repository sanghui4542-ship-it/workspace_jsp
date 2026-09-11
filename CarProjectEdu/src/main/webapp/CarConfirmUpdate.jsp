<%@page import="Vo.CarConfirmVo"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%
	request.setCharacterEncoding("UTF-8");
	String contextPath = request.getContextPath();
	//	   "/CarProject"
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page import="Service.CarService" %>

			<%--
				CarController에서 request 내장객체에 바인딩 했었던  예약 조회 정보  모습 
			
				 2.4.    2.2.에서 조회된 예약한 정보(CarConfirmVo)를 중앙 VIEW에 보여주기 위해 request에 바인딩
						
					request.setAttribute("vo", carConfirmVo);
			 --%>

<%
	//CarController 가 request 에 담아준 조회된 예약 정보 1건
	CarConfirmVo vo = (CarConfirmVo)request.getAttribute("vo");

	int    carqty        = vo.getCarqty();          //대여 수량
	int    orderid       = vo.getOrderid();         //예약 아이디
	String carimg        = vo.getCarimg();          //예약한 차량 이미지명
	String carbegindate  = vo.getCarbegindate();    //대여 시작 날짜
	int    carreserveday = vo.getCarreserveday();   //대여 기간
	int    carins        = vo.getCarins();          //보험 적용 여부
	int    carwifi       = vo.getCarwifi();         //wifi 적용 여부
	int    carnave       = vo.getCarnave();         //네비 적용 여부
	int    carbabyseat   = vo.getCarbabyseat();     //베이비시트 적용 여부
	String memberphone   = vo.getMemberphone();     //예약당시 입력한 휴대폰 번호

	//차량명은 앞 화면(CarReserveResult)에서 POST 로 넘어온다
	String carname = request.getParameter("carname");
%>
<%--
 ================================================================================
   CarConfirmUpdate.jsp  -  예약 정보 수정 화면
 ================================================================================
--%>

<div class="container">

	<div class="section-head">
		<span class="section-eyebrow">MODIFY</span>
		<h2 class="section-heading">예약 정보 수정</h2>
		<p class="section-desc">변경할 내용을 고친 뒤 예약 비밀번호로 본인 확인을 해주세요</p>
	</div>

	<%--  변경할 내용을 고친 뒤 본인을 확인 하기 위하 예약 비밀번호 입력후 변경한 예약 내역 CarController로  수정(변경) 요청 합니다. --%>
	<form action="<%=contextPath%>/Car/updatePro.do" method="post">

		<%-- [보안] CSRF 토큰 : 이 화면에서 출발한 요청임을 증명한다 (설명은 members/login.jsp) --%>
		<input type="hidden" name="_csrf" value="${_csrf}">

		<%-- 수정에 필요한 값들 (주소창에 남지 않도록 hidden 으로 보낸다) --%>
		<input type="hidden" name="orderid"     value="<%=orderid%>" />
		<input type="hidden" name="carimg"      value="<%=carimg%>" />
		<input type="hidden" name="carname"     value="<%=carname == null ? "" : carname%>" />
		
		<%-- [보안] 기존에는 action 주소에 ?memberphone=... 으로 붙어 주소창에 노출됐다 --%>
		<input type="hidden" name="memberphone" value="<%=memberphone%>" />

		<div class="grid-2">

			<%-- ============ 왼쪽 : 어떤 예약을 수정하는지 ============ --%>
			<div>
				<div class="card">
					<div class="car-photo">
						<% if(carimg != null && !carimg.trim().isEmpty()) { %>
							<img src="<%=contextPath%>/img/<%=carimg%>" alt="예약 차량 사진">
						<% } else { %>
							<div class="flex items-center justify-center" style="height:100%;">
								<span class="text-muted">사진 없음</span>
							</div>
						<% } %>
						<span class="car-badge">예약번호 <%=orderid%></span>
					</div>
					<div class="car-body">
						<div class="car-name">
							<%= (carname == null || carname.trim().isEmpty()) ? "예약 차량" : util.HtmlUtil.escape(carname) %>
						</div>
						<div class="car-meta">
							<span>연락처 <%=util.HtmlUtil.escape(memberphone)%></span>
						</div>
					</div>
				</div>
			</div>

			<%-- ============ 오른쪽 : 수정 입력 ============ --%>
			<div>

				<div class="form-group">
					<label class="form-label" for="carbegindate">대여 시작일</label>
					<input class="form-control" type="date" id="carbegindate" name="carbegindate"
						   value="<%=carbegindate%>" required>
				</div>

				<div class="grid-2">
					<div class="form-group">
						<label class="form-label" for="carreserveday">대여 기간</label>
						<select class="form-control" id="carreserveday" name="carreserveday">
							<% for(int d=1; d<=5; d++){ %>
								<option value="<%=d%>" <%= (carreserveday==d) ? "selected" : "" %>><%=d%>일</option>
							<% } %>
						</select>
					</div>
					<div class="form-group">
						<label class="form-label" for="carqty">대여 수량</label>
						<select class="form-control" id="carqty" name="carqty">
							<% for(int q=1; q<=5; q++){ %>
								<option value="<%=q%>" <%= (carqty==q) ? "selected" : "" %>><%=q%>대</option>
							<% } %>
						</select>
					</div>
				</div>

				<%--
				 옵션 선택
				   요금은 CarService 상수를 직접 읽어 표시한다.
				   화면에 숫자를 적어두면 서버 요금이 바뀔 때 조용히 어긋난다.
				--%>
				<div class="form-group">
					<label class="form-label">추가 옵션</label>
					<div class="price-list">

						<div class="price-item">
							<span class="name">&#128737; 자차보험</span>
							<select class="form-control" name="carins" style="max-width:150px;">
								<option value="0" <%= (carins==0) ? "selected" : "" %>>미적용</option>
								<option value="1" <%= (carins==1) ? "selected" : "" %>>적용 +<fmt:formatNumber value="<%=CarService.PRICE_INSURANCE%>" pattern="#,###"/>원</option>
							</select>
						</div>

						<div class="price-item">
							<span class="name">&#128246; 무선 WiFi</span>
							<select class="form-control" name="carwifi" style="max-width:150px;">
								<option value="0" <%= (carwifi==0) ? "selected" : "" %>>미적용</option>
								<option value="1" <%= (carwifi==1) ? "selected" : "" %>>적용 +<fmt:formatNumber value="<%=CarService.PRICE_WIFI%>" pattern="#,###"/>원</option>
							</select>
						</div>

						<div class="price-item">
							<span class="name">&#128506; 네비게이션</span>
							<select class="form-control" name="carnave" style="max-width:150px;">
								<option value="0" <%= (carnave==0) ? "selected" : "" %>>미적용</option>
								<option value="1" <%= (carnave==1) ? "selected" : "" %>>적용 +<fmt:formatNumber value="<%=CarService.PRICE_NAVI%>" pattern="#,###"/>원</option>
							</select>
						</div>

						<div class="price-item">
							<span class="name">&#128118; 베이비시트</span>
							<select class="form-control" name="carbabyseat" style="max-width:150px;">
								<option value="0" <%= (carbabyseat==0) ? "selected" : "" %>>미적용</option>
								<option value="1" <%= (carbabyseat==1) ? "selected" : "" %>>적용 +<fmt:formatNumber value="<%=CarService.PRICE_BABYSEAT%>" pattern="#,###"/>원</option>
							</select>
						</div>

					</div>
					<p class="form-hint">모든 옵션 요금은 1일 · 1대 기준입니다</p>
				</div>

				<div class="form-group">
					<label class="form-label" for="memberpass">예약 비밀번호 <span class="required">*</span></label>
					<input class="form-control" type="password" id="memberpass" name="memberpass"placeholder="예약 시 입력한 비밀번호" required>
					<p class="form-hint">&#8226; 본인 확인을 위해 필요합니다. 서버에서 다시 검증합니다.</p>
				</div>

			</div>
		</div>
		<div class="flex flex-wrap gap-2 mt-6">
			<button type="submit" class="btn btn-primary btn-lg">수정 내용 저장</button>
			<a class="btn btn-ghost" href="<%=contextPath%>/Car/cc?center=CarReserveConfirm.jsp">
				&lsaquo; 예약 조회로
			</a>
		</div>

	</form>
</div>



