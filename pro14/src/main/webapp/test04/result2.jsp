<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%-- JSTL 전체 라이브러리에 포함된 core태그들을 사용하기 위해 외부 사이트에서 불러오자. --%>    
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>    
    
<% request.setCharacterEncoding("UTF-8"); %>      
    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<%-- 조건 :  입력한 아이디를 request 내장객체 영역에서 얻는데.. 아이디가 존재 하지 않으면? 
		 조건요약 : 아이디를 입력하지 않고 result.jsp에 로그인 요청했다면?--%>
	<c:if test="${empty param.userID}">
<%--			<%= request.getParameter("userID") == null %>	 --%>
	
		아이디를 입력하세요. <br>
		<a href="login.jsp">로그인요청 하러가기</a>
	</c:if>

	<%-- 조건요약 : 아이디를 입력하고 result.jsp에 로그인 요청했다면? --%>
	<c:if test="${not empty param.userID}">
	
		<%--조건요약 : 입력한 아이디가 관리자 아이디(admin)를 입력해서 result2.jsp에 로그인 요청 했다면? --%>
		<c:if test="${param.userID eq 'admin'}">		
			<h1>관리자 계정 admin으로 로그인 했습니다.</h1>
	 		<form>
	 			<button>회원정보 수정</button>
	 			<button>회원정보 삭제</button>
	 		</form>		
		</c:if>

	 	<%--요약 : 입력한 아이디가 관리자 아이디(admin)이 아닌 다른 일반 아이디를 입력해서 result2.jsp에 로그인 요청 했다면?  --%>
		<c:if test="${param.userID != 'admin'}">			
			<h1>일반계정 ${param.userID} 님 로그인 했습니다!!</h1>	
		</c:if>
	</c:if>



</body>
</html>







