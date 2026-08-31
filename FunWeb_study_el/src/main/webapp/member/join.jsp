<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%--
================================================================
 [실습 5] join.jsp 를 회원가입이 되도록 고치기

 완성 목표 3가지
   1. 폼의 목적지를 컨트롤러(join.do)로 연결
   2. Submit 버튼을 실제 전송되는 버튼으로 변경
   3. 가입 실패 메시지(joinMsg)를 경고창으로 표시

 ** 주석 아래 빈 줄에 코드를 직접 작성한다. **
================================================================
--%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<link href="../css/default.css" rel="stylesheet" type="text/css">
<link href="../css/subpage.css" rel="stylesheet" type="text/css">

<%-- [실습 5-3] 가입 실패 안내 --%>

<%-- request 에서 "joinMsg" 를 꺼내 String 변수 joinMsg 에 저장 --%>
<% String joinMsg = (String)request.getAttribute("joinMsg"); %>

<%-- joinMsg 가 null 이 아니면 아래 script 를 출력하는 if 문 시작 --%>
<% if(joinMsg != null){ %>	
	
	<%-- alert 로 joinMsg 값을 표현식(<%= %>)으로 출력하는 script 태그 작성 --%>
	<script>
		alert("<%=joinMsg%>");
	</script>
	
<% } %>
<%-- if 문 닫기 --%>


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
			<h1>Join Us</h1>

			<!-- [실습 5-1] form 태그의 action 을 "join.do" 로, method 를 "post" 로 작성 -->
			<form action="join.do" id="join" method="post">

				<fieldset>
					<legend>Basic Info</legend>
					<label>User ID</label> <input type="text" name="id" class="id">
					<input type="button" value="dup. check" class="dup"><br>
					<label>Password</label> <input type="password" name="pass"><br>
					<label>Retype Password</label> <input type="password" name="pass2"><br>
					<label>Name</label> <input type="text" name="name"><br>
					<label>E-Mail</label> <input type="email" name="email"><br>
					<label>Retype E-Mail</label> <input type="email" name="email2"><br>
				</fieldset>

				<fieldset>
					<legend>Optional</legend>
					<label>Address</label> <input type="text" name="address"><br>
					<label>Phone Number</label> <input type="text" name="phone"><br>
					<label>Mobile Phone Number</label> <input type="text" name="mobile"><br>
				</fieldset>
				<div class="clear"></div>
				<div id="buttons">

					<!-- [실습 5-2] type 을 "button" 에서 "submit" 으로 변경 (눌러야 전송됨) -->
					<input type="submit" value="Submit" class="submit">

					<input type="button" value="Cancel" class="cancel">
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
