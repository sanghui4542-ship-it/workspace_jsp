<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<header>
	<div id="login">
<%--
   [로그인 상태에 따라 헤더 메뉴를 바꾸는 분기]
   session 내장객체에서 "userId" 를 꺼내 본다.
     값이 없으면(null)  -> 아직 로그인 전  -> login | join 링크 표시
     값이 있으면        -> 로그인 상태     -> 아이디님 | logout 링크 표시
   이 파일은 include 되므로, 모든 하위 페이지의 헤더가 한 번에 바뀐다.
--%>
<%
	String userId = (String) session.getAttribute("userId");
	/* 세션 객체에서 "userId" 이름표의 값을 꺼낸다.
	   getAttribute() 의 반환은 Object 타입이라 (String) 으로 되돌린다.
	   로그인한 적이 없으면 null 이 반환된다. */

	if (userId == null) {
	/* [로그인 전] 원래의 login / join 링크를 그대로 보여준다. */
%>
		<a href="../member/login.jsp">login</a> | <a href="../member/join.jsp">join</a>
<%
	} else {
	/* [로그인 상태] 아이디 환영 문구와 logout 링크를 보여준다. */
%>
		<b><%= userId %></b>님 환영합니다 | <a href="../member/logout.do">logout</a>
<%--     <%= %> 는 표현식 태그 : 변수 값을 그 자리에 바로 출력한다. --%>
<%
	}//if-else
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
