<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%
	/*
		이 result3.jsp 파일의 역할
		
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
	<%
		//아이디를 입력하지 않고 로그인 버튼을 눌러 로그인 요청을  result2.jsp로 하였을 경우
		if(user_id == null  ||  user_id.length() == 0){
	%>		
			아이디를 입력하고 오세요.<br>
			<a href="/pro12/login.html">로그인 요청 화면으로 다시 이동</a>
	<%		
		}else{ //아이디를 입력하고 로그인 버튼을 눌러 로그인 요청을 result2.jsp로 하였을 경우 
						 
			if(user_id.equals("admin")){ //입력한 아이디가 관리자 아이디("admin") 과 같은 경우
	%>		
				<h1>관리자 admin 아이디로 로그인 된 화면 입니다.</h1>
				<form>
					<button type="button">회원정보조회</button> 
					<button type="button">회원정보추가</button>
					<input type="button" value="회원정보수정">
					<input type="button" value="회원정보삭제">
				</form>			
	<%		
			}else{ //입력한 아이디가 일반 사용자 아이디("admin"이 아닌 다른 아이디)과 같은 경우 
	%>			
				<h1>환영합니다. <%=user_id%>님!!</h1>
	<%			
			} //- 안쪽 else
			
		} //- 바깥쪽 else
	%>

</body>
</html>








