<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<link href="css/default.css" rel="stylesheet" type="text/css">
<link href="css/front.css" rel="stylesheet" type="text/css">

<!--[if lt IE 9]>
<script src="http://ie7-js.googlecode.com/svn/version/2.1(beta4)/IE9.js" type="text/javascript"></script>
<script src="http://ie7-js.googlecode.com/svn/version/2.1(beta4)/ie7-squish.js" type="text/javascript"></script>
<script src="http://html5shim.googlecode.com/svn/trunk/html5.js" type="text/javascript"></script>
<![endif]-->

<!--[if IE 6]>
 <script src="script/DD_belatedPNG_0.0.8a.js"></script>
 <script>
   /* EXAMPLE */
   DD_belatedPNG.fix('#wrap');
   DD_belatedPNG.fix('#main_img');   

 </script>
 <![endif]-->


</head>
<body>
	<div id="wrap">
		<!-- 헤더파일들어가는 곳 -->
		<header>
			<div id="login">
			
<%
	//의미1. index.jsp(메인화면)을 요청 했을때 session 내장객체 메모리에 바인딩한 값을 얻는데.. 없으면?(null이면?) 미로그인된 화면 보여주기
	//의미2. index.jsp(메인화면)을 요청 했을때 session 내장객체 메모리에 바인딩한 값을 얻는데...있으면?(로그인 요청한 세션아이디이면?) 로그인된 화면 보여주기		
	String userId = (String)session.getAttribute("userId");

	if(userId == null){ //아직 로그인 된 상태가 아니면?
%>			
				<a href="member/login.jsp">login</a> | <a href="member/join.jsp">join</a>
<%	
	}else { //지금현재 로그인 처리하여 session내장객체 메모리에 로그인요청한 아이디가 존재 한다면?
%>		
				<b><%=userId%></b>님 환영합니다 | <a href="member/logout.do">logout</a>
<%	
	}
%>
					
			</div>
			<div class="clear"></div>
			<!-- 로고들어가는 곳 -->
			<div id="logo">
				<img src="images/logo.gif" width="265" height="62" alt="Fun Web">
			</div>
			<!-- 로고들어가는 곳 -->
			<nav id="top_menu">
				<ul>
					<li><a href="index.jsp">HOME</a></li>
					<li><a href="company/welcome.jsp">COMPANY</a></li>
					<li><a href="#">SOLUTIONS</a></li>
					<li><a href="center/notice.jsp">CUSTOMER CENTER</a></li>
					<li><a href="#">CONTACT US</a></li>
				</ul>
			</nav>
		</header>
		<!-- 헤더파일들어가는 곳 -->
		<!-- 메인이미지 들어가는곳 -->
		<div class="clear"></div>
		<div id="main_img">
			<img src="images/main_img.jpg" width="971" height="282">
		</div>
		<!-- 메인이미지 들어가는곳 -->
		<!-- 메인 콘텐츠 들어가는 곳 -->
		<article id="front">
			<div id="solution">
				<div id="hosting">
					<h3>Web Hosting Solution</h3>
					<p>Lorem impsun Lorem impsunLorem impsunLorem impsunLorem
						impsunLorem impsunLorem impsunLorem impsunLorem impsunLorem
						impsun....</p>
				</div>
				<div id="security">
					<h3>Web Security Solution</h3>
					<p>Lorem impsun Lorem impsunLorem impsunLorem impsunLorem
						impsunLorem impsunLorem impsunLorem impsunLorem impsunLorem
						impsun....</p>
				</div>
				<div id="payment">
					<h3>Web Payment Solution</h3>
					<p>Lorem impsun Lorem impsunLorem impsunLorem impsunLorem
						impsunLorem impsunLorem impsunLorem impsunLorem impsunLorem
						impsun....</p>
				</div>
			</div>
			<div class="clear"></div>
			<div id="sec_news">
				<h3>
					<span class="orange">Security</span> News
				</h3>
				<dl>
					<dt>Vivamus id ligula....</dt>
					<dd>Proin quis ante Proin quis anteProin quis anteProin quis
						anteProin quis anteProin quis ante......</dd>
				</dl>
				<dl>
					<dt>Vivamus id ligula....</dt>
					<dd>Proin quis ante Proin quis anteProin quis anteProin quis
						anteProin quis anteProin quis ante......</dd>
				</dl>
			</div>
			<div id="news_notice">
				<h3 class="brown">News &amp; Notice</h3>
				<table>
					<tr>
						<td class="contxt"><a href="#">Vivans....</a></td>
						<td>2012.11.02</td>
					</tr>
					<tr>
						<td class="contxt"><a href="#">Vivans....</a></td>
						<td>2012.11.02</td>
					</tr>
					<tr>
						<td class="contxt"><a href="#">Vivans....</a></td>
						<td>2012.11.02</td>
					</tr>
					<tr>
						<td class="contxt"><a href="#">Vivans....</a></td>
						<td>2012.11.02</td>
					</tr>
					<tr>
						<td class="contxt"><a href="#">Vivans....</a></td>
						<td>2012.11.02</td>
					</tr>
				</table>
			</div>
		</article>
		<!-- 메인 콘텐츠 들어가는 곳 -->
		<div class="clear"></div>
		<!-- 푸터 들어가는 곳 -->
		<footer>
			<hr>
			<div id="copy">
				All contents Copyright 2011 FunWeb 2011 FunWeb Inc. all rights
				reserved<br> Contact mail:funweb@funwebbiz.com Tel +82 64 123
				4315 Fax +82 64 123 4321
			</div>
			<div id="social">
				<img src="images/facebook.gif" width="33" height="33" alt="Facebook">
				<img src="images/twitter.gif" width="34" height="34" alt="Twitter">
			</div>
		</footer>
		<!-- 푸터 들어가는 곳 -->
	</div>
</body>
</html>