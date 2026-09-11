<%-- 이 페이지의 글자 인코딩을 UTF-8 로 정한다. 빠뜨리면 한글이 깨진다 --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%-- JSTL 태그를 c: 라는 이름으로 쓰겠다는 선언. 이 줄이 없으면 <c:...> 가 그냥 글자로 나온다 --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%-- JSTL 태그를 fmt: 라는 이름으로 쓰겠다는 선언. 이 줄이 없으면 <fmt:...> 가 그냥 글자로 나온다 --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%
    /* Java 코드: 한글 인코딩 설정 */
    request.setCharacterEncoding("UTF-8");
    /* Java 코드: 컨텍스트 주소 /CarProject 얻기 */
    String contextPath = request.getContextPath();
%>
<%--
 ================================================================================
   CarReserveConfirm.jsp  -  예약 조회 입력 화면
   [6단계 재작성]
     표(table) 레이아웃 + 고정 너비를 label + form-control 로 바꿨다.
     또한 "무엇을 입력해야 하는지"를 안내 문구로 명확히 했다.
     (예약할 때 입력한 연락처와 비밀번호를 그대로 넣어야 한다)
 ================================================================================
--%>
<div class="container">
	<%-- section-head 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
	<div class="section-head">
		<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
		<span class="section-eyebrow">MY RESERVATION</span>
		<%-- 제목 --%>
		<h2 class="section-heading">예약 확인</h2>
		<%-- 문단 글 --%>
		<p class="section-desc">예약할 때 입력한 연락처와 비밀번호를 넣어주세요</p>
	</div>
	<%-- 입력 폼은 좁은 폭으로 가운데 정렬해 집중도를 높인다 --%>
	<div style="max-width:480px; margin:0 auto;">
		<%-- 입력한 내용을 <%=contextPath%>/Car/CarReserveConfirm.do 주소로 POST 방식으로 보낸다 --%>
		<form action="<%=contextPath%>/Car/CarReserveConfirm.do" method="post">
			<%-- [보안] CSRF 토큰 : 이 화면에서 출발한 요청임을 증명한다 (설명은 members/login.jsp) --%>
			<input type="hidden" name="_csrf" value="${_csrf}">
			<%-- form-group 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
			<div class="form-group">
				<%-- 입력칸에 붙는 이름표 --%>
				<label class="form-label" for="memberphone">연락처 <span class="required">*</span></label>
				<%-- memberphone 입력칸. 서버에서 request.getParameter("memberphone") 로 받는다 --%>
				<input class="form-control" type="tel" id="memberphone" name="memberphone"
					   placeholder="010-1234-5678" required autofocus>
			</div>
			<%-- form-group 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
			<div class="form-group">
				<%-- 입력칸에 붙는 이름표 --%>
				<label class="form-label" for="memberpass">예약 비밀번호 <span class="required">*</span></label>
				<%-- memberpass 입력칸. 서버에서 request.getParameter("memberpass") 로 받는다 --%>
				<input class="form-control" type="password" id="memberpass" name="memberpass"
					   placeholder="예약 시 입력한 비밀번호" required>
			</div>
			<%-- 누르면 동작하는 버튼 --%>
			<button type="submit" class="btn btn-primary btn-block btn-lg mt-4">예약 조회</button>
		</form>
		<%-- alert alert-info mt-6 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
		<div class="alert alert-info mt-6">
			&#8226; 대여 시작일이 지난 예약은 조회되지 않습니다.<br>
			&#8226; 비밀번호를 잊으셨다면 고객센터(02-3456-6574)로 문의해 주세요.
		</div>
	</div>
</div>