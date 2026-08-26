



<%@ page language="java" 
		 contentType="text/html; charset=UTF-8"
    	 pageEncoding="UTF-8"
    	 session="true"
    	 buffer="8kb"
    	 autoFlush="true"
    	 isThreadSafe="false"
    	 info="현재 JSP페이지는 쇼핑몰 메인기능이 작성되어"
    	 isErrorpage="false"
    	 errorpage=""
    	 
    	 
    	 %>
    	 
 <%@ page import="java.util.ArrayList"%>
 <%@ page import="java.util.Date" %>
 
 
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
		<%
			/* 가변 배열 ArrayList클래스의 객체를 생성한다.
			   위의 page 디렉티브 태그에 import속성에 경로를 설정해야 객체를 생성할 수 있다.*/
			ArrayList list = new ArrayList();
		
			/* 현재 날짜/시간 정보를 담은 Date클래스의 객체를 생성한다.
			   위의 page 디렉티브 태그에 import속성에 설정해야 객체 생성할 수 있다.*/
			Date date = new Date();	
		%>
		<%=date%>
		
		<h2>쇼핑몰 구현 중심 JSP 페이지 입니다!!</h2>

</body>
</html>