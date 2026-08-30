<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<%-- 주제 : <jsp:forward /> 액션태그를 사용한 디스패처방법 포워딩 --%>
	
	<%
		//순서1. 클라이언트가 브라우저 주소창에 login.jsp를 요청할 URL을 입력하여 Tomcat에게 요청한다.
		//      URL : http://localhost:8181/pro13/login.jsp
	
		//순서2. 요청받은 Tomcat은 새로운 request내장객체 메모리를 생성해 login.jso로 전달합니다.
		//		만약 요청 URL에 포함된 요청 한글문자가 있으면 깨져서 꺼내와 지므로 한글처리 
		request.setCharacterEncoding("UTF-8");
		
		//순서3. 로그인 요청할수 있는 디자인 코드를 작성한다.
	%>
	<h1>아이디를 입력하지 않았습니다. 아이디를 입력해 주세요.</h1>
	
	<form action="result.jsp" method="post">
		아이디 : <input name="userID"><br>
		비밀번호 : <input type="password" name="userPw"><br>
		
		<%-- 로그인 요청 버튼으로 만들수 있는 3가지 경우 --%>
		<input type="submit" value="로그인">		
		<!-- <button>로그인</button> -->
		<!-- <input type="image" src="버튼에 보일 이미지파일 경로"> -->
	
		<input type="reset" value="다시입력">
	</form>
	

</body>
</html>









