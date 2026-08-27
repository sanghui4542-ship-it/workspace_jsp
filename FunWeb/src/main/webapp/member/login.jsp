<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<link href="../css/default.css" rel="stylesheet" type="text/css">
<link href="../css/subpage.css" rel="stylesheet" type="text/css">
<!--[if lt IE 9]>
<script src="http://ie7-js.googlecode.com/svn/version/2.1(beta4)/IE9.js" type="text/javascript"></script>
<script src="http://ie7-js.googlecode.com/svn/version/2.1(beta4)/ie7-squish.js" type="text/javascript"></script>
<script src="http://html5shim.googlecode.com/svn/trunk/html5.js" type="text/javascript"></script>
<![endif]-->
<!--[if IE 6]>
 <script src="../script/DD_belatedPNG_0.0.8a.js"></script>
 <script>
   /* EXAMPLE */
   DD_belatedPNG.fix('#wrap');
   DD_belatedPNG.fix('#main_img');   

 </script>
 <![endif]-->
 <%--
 	[로그인 실패 안내]
 	MemberController 서블리이 디스패처방식으로 포워딩할때 공유 받은 request 내장객체에 담아 둔 
 	"loginMsg" 키에 대응하는 ,실패 메세지 값이 있으면  경고창으로 브라우저에 보여준다.
  --%>
<%
	String loginMsg	 = (String)request.getAttribute("loginMsg");
	
	if(loginMsg != null){ //로그인 실패로 인한 request에 바인딩한 경고메세지가 값으로 저장되어 있다면?
%>		
		<script>  alert("<%=loginMsg%>"); </script>				
<%			
	}
%>  
</head>
<body>
	<div id="wrap">
		<!-- 헤더들어가는 곳 -->
		<%@ include file="../inc/top.jsp" %>
		<!-- 헤더들어가는 곳 -->

		<!-- 본문들어가는 곳 -->
		<!-- 본문메인이미지 -->
		<div id="sub_img_member"></div>
		<!-- 본문메인이미지 -->
		<!-- 왼쪽메뉴 -->
		<nav id="sub_menu">
			<ul>
				<li><a href="#">Join us</a></li>
				<li><a href="#">Privacy policy</a></li>
			</ul>
		</nav>
		<!-- 왼쪽메뉴 -->
		<!-- 본문내용 -->
		<article>
			<h1>Login</h1>
			
			<form action="login.do" id="join" method="post">
				<%-- action : 전송 목적지 = 컨트롤러의 로그인 기능 (login.do)
			         method="post" : 비밀번호가 주소창에 노출되지 않게 본문으로 전송 --%>
			     
				<fieldset>
					<legend>Login Info</legend>
					<label>User ID</label> <input type="text" name="id"><br>
					<label>Password</label> <input type="password" name="pass"><br>
				</fieldset>
				<div class="clear"></div>
				<div id="buttons">
					<input type="submit" value="로그인" class="submit"> 
					<input type="reset" value="취소" class="cancel">
				</div>
			</form>
		</article>
		<!-- 본문내용 -->
		<!-- 본문들어가는 곳 -->

		<div class="clear"></div>
		<!-- 푸터들어가는 곳 -->
		<%@ include file="../inc/bottom.jsp" %>
		<!-- 푸터들어가는 곳 -->
	</div>
</body>
</html>






