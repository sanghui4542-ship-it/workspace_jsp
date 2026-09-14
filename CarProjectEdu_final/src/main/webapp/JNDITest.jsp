<%@page import="javax.sql.DataSource"%>
<%@page import="javax.naming.InitialContext"%>
<%@page import="javax.naming.Context"%>
<%@page import="java.sql.Connection"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<h1>DB연동 테스트</h1>
	<%
		Connection conn = null;
		try{
			Context init = new InitialContext();
			
			//커넥션풀 얻기 
			DataSource ds = (DataSource)init.lookup("java:comp/env/jdbc/jspbeginner");
			
			//커넥션풀에서 커넥션 객체 빌려 오기
			conn = ds.getConnection();
			
			if(conn != null) out.print("DB와의 연결 성공");
			
		}catch(Exception err){
			err.printStackTrace();
		}
	%>
	
	


</body>




</html>