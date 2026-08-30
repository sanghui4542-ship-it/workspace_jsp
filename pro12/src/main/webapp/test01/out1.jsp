<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
 	<%-- out1.jsp --%>
 	
 	<form action="out2.jsp" method="post">
 		이름 : <input name="name"> <br>
 		나이 : <input name="age">  <br>
 		<input type="submit" value="out2.jsp서버페이지에 요청 전송">
 	</form>
 	

</body>
</html>