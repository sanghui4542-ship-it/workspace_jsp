<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
		<h1>홈페이지(include.jsp)의 상단 메뉴 영역</h1> <br>
		
			<%-- 
				include 디렉티브 태그?
			
				- 다른 jsp의 코드내용을 현재 jsp 페이지의 현재위치로 불러와 포함시키는 태그
				
				include 디렉티브 태그 작성문법
				
				<%@ include fule속성="불러와포함시킬jsp경로%>
		
			--%>
		
		
		<!-- duke_image.jsp에 작성된 <img src="...."/> 코드를 불러와 현재 위치에 포함 -->
		<%@ include file="duke_image.jsp"%>
		
		
		<h1>홈페이지(include.jsp)의 하단 정보 영역</h1>
		
</body>
</html>