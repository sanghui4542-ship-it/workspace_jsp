<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%
	/*
		이 result.jsp 파일의 역할
		
		-  login.html 화면에서 아이디와 비밀번호를 입력하고 로그인 요청을 post 요청방식으로 전송 요청 하면
		   그 요청한 아이디, 비밀번호를 TOMCAT 서버에서 받아서 화면에 다시 보여주는 역할.
	*/
	//순서1. 요청한 데이터들 (입력한 아이디, 비밀번호) 중에 한글 문자 인코딩 방식 UTF-8 로 HttpServletRquest에 설정
	request.setCharacterEncoding("UTF-8");

	//순서2. 요청한 데이터들을 얻어 변수에 저장	
	String user_id = request.getParameter("user_id"); //1. 입력한 아이디 얻어 저장
	//예) "admin"

	String user_pw = request.getParameter("user_pw"); //2. 입력한 비밀번호 얻어 저장
	//예) "1234"
	
	//순서3. 요청한 클라이언트의 브라우저로 응답할 데이터를 생성해서 응답(출력)
%>    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<!-- 사용자가 입력한 아이디, 비밀번호를 확인시켜 주기 위해 응답할 데이터로 사용하자. -->
	<h1>입력한 아이디 : <%=user_id%> </h1>
	<h1>입력한 비밀번호 : <%=user_pw%></h1>

</body>
</html>








