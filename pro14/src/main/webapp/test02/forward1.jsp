<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
	/* 1. 요청한 문자 한글처리 */
	request.setCharacterEncoding("UTF-8");	
	
	/* 2. request 내장객체에  개발자가 수동으로 직접 바인딩 
	   바인딩 할 데이터 -> ("id", "hong"),  ("pwd", "1234") */
	   request.setAttribute("id", "hong");  request.setAttribute("pwd", "1234");
	
	/* 3. session 내장객체에 개발자가 수동으로 직접 바인딩
	   바인딩 할 데이터  -> ("name", "홍길동") */
	   session.setAttribute("name", "홍길동");
	   
	/* 4. application 내장객체에 개발자가 수동으로 직접 바인딩 
	   바인딩 할 데이터  -> ("email", "hong@test.com") */   
	   application.setAttribute("email", "hong@test.com");
	   
	/* 5. member1.jsp 페이지로 디스패처방식으로 포워딩시 request, response 내장객체 공유 */
	
	//   request.getRequestDispatcher("member1.jsp").forward(request, response);
	
%>
	   <jsp:forward  page="member1.jsp"/>









