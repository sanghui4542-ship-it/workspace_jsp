<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<header>
	<div id="login">
<%--
   [로그인 상태에 따라 헤더 메뉴를 바꾸는 분기]
   session 내장객체에서 "userId" 를 꺼내 본다.
     값이 없으면(null)  -> 아직 로그인 전  -> login | join 링크 표시
     값이 있으면        -> 로그인 상태     -> 아이디님 | logout 링크 표시
   이 top.jsp 파일은 include 되므로, 모든 하위 페이지의 헤더가 한 번에 바뀐다.
--%>	
<%
	/* session 내장객체에서 로그인 처리시 바인딩한 아이디를 꺼내옵니다.*/
	String userId = (String)session.getAttribute("userId");
	
	/* session 내장객체에 로그인 처리시 바인딩한 아이디가 없다면? 미로그인된 화면 보여 주기*/
	if(userId == null){
%>
		<a href="../member/login.jsp">login</a> | <a href="../member/join.jsp">join</a>
<%	
	}else{ /* session 내장객체에 로그인 처리시 바인딩한 아이디가 있다면?(데이터베이스에서 아이디 조회되면?) 로그인된 화면 보여 주기*/
%>		
		<b><%=userId%></b>님 환영합니다 | <a href="../member/logout.do">logout</a>
<%	
	}
%>	
	
	</div>
	<div class="clear"></div>
	<!-- 로고들어가는 곳 -->
	<div id="logo">
		<img src="../images/logo.gif" width="265" height="62" alt="Fun Web">
	</div>
	<!-- 로고들어가는 곳 -->
	<nav id="top_menu">
		<ul>
			<li><a href="../index.jsp">HOME</a></li>
			<li><a href="../company/welcome.jsp">COMPANY</a></li>
			<li><a href="#">SOLUTIONS</a></li>
			<li><a href="../center/notice.jsp">CUSTOMER CENTER</a></li>
			<li><a href="#">CONTACT US</a></li>
		</ul>
	</nav>
</header>