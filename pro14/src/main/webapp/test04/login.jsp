<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%
//순서1. 클라이언트가 브라우저 주소창에  login.jsp 를 요청할 URL을 입력하여 요청한다.
//      http://localhost:8181/pro14/login.jsp

//순서2. 요청 받은 login.jsp 에서  얻은 request내장객체 메모리에 만약 요청한 데이터들 중에서 한글 문자가 존재 할수도 있으니!!!!!!!!! 
//      먼저 request내장객체 메모리에 저장된 요청한 한글 데이터를 UTF-8 방식으로 처리 하여 설정 한다. 
	request.setCharacterEncoding("UTF-8");  
%>       
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
<%-- 순서3. 로그인 요청 할수 있는 디자인 코드를 작성 한다. --%>
<!-- <form action="result.jsp" method="post"> -->
	 <form action="result2.jsp" method="post">
	 
		아이디 : <input type="text" name="userID"><br>
		비밀번호 : <input type="password" name="userPW"><br>
		
		<%--로그인 요청 버튼으로 만들수 있는 3가지 경우--%>
		<input type="submit" value="로그인">
		<!-- <button>로그인</button> -->
		<!-- <input type="image" src="버튼으로 보일 이미지 경로"> -->
		
		<%-- 초기화 버튼  --%>
		<input type="reset" value="다시입력">	
	</form>

    
</body>
</html>













