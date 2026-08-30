<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<%
	 //실습1. 404 예외 발생 할 경우.
	 
 		//정상적인 요청 URL -> http://localhost:8181/pro12/number.jsp?num=10
 	
 		//비상적인 요청 URL -> http://localhost:8181/pro12/number.jsp        : 500  NumberFormatException 숫자로 변경 불가
 		
 		//비정상적인 요청 URL -> http://localhost:8181/pro12/num.jsp          :  404 num.jsp경로 찾을수 없다.예외 발생
 
 	int num = Integer.parseInt(request.getParameter("num")); 	//예외가 예상되는 코드 직접 작성		
	
	out.print(num);
	
	%>
	<h1>쇼핑몰 중심 JSP 입니다!!!!</h1>
</body>
</html>