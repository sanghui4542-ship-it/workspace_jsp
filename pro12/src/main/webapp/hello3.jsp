<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<%--
		1. 선언문 태그 영역
			-   <%!   %>
			-   전역변수 선언 해 놓거나  전역메소드 선언해 놓을 태그 영역
	 --%>
	 <%! 	 	
	 	String name="이순신"; //전역변수 선언
	 	public String getName(){ return this.name;  } //전역메소드 선언	 
	 %>
	<%--
		2. 스크립 틀릿 태그 영역
			-  <%   %>
			-  지역변수 선언 해 놓거나  다른 자바코드를 작성해 놓을 태그 영역 
	 --%>
	 <%
	 	/*
	 	스토리 : 클라이언트가 브라우저 주소창에 요청할 URL http://localhost:8181/pro12/hello3.jsp?age=22 을 작성해서 
	 	       톰캣 서버에 hello3.jsp 페이지를 요청 합니다. 
	 	       톰캣 서버는 요청한 URL주소에 관한  HttpServletRequest 객체 메모리에 요청한 주소  + 요청데이터들을 저장해서 
	 	       hello3.jsp 서버페이지로 전달하게 됩니다.
	 	       우리 개발자가 hello3.jsp 서버페이지 개발 코드를 작성해서 요청데이터들을 HttpServletRequest객체 메모리 내부에서 꺼내와 얻는다.	 	
		*/
 		//요청한 데이터 =========>   ?age=22
 	
 		String requestAge =  request.getParameter("age");  //요청한 데이터 얻기 
 		//		"22"	
	 %>
<%--
	3. 표현식 태그 영역
	 - <%= %>
	 
	 - 표현식 태그 영역 내부에 작성할 코드는?  요청한 브라우저로 응답할 데이터를 작성 하되,  
	   변수명 또는 실제응답할데이터  또는  자바식 을 넣어 응답할수 있다.
	   
	 - PrintWriter 출력스트림과 같은 역할을 하는 JspWrtier의 print 메소드의 역할을 하는 표현식 태그 >>> <%= %> 입니다.
 --%>	
	<h1>안녕하세요 <%=name%>입니다.</h1>
	<h1>나이는 <%=requestAge%>입니다.</h1>
	<h1>키는 <%=180%>cm 입니다.</h1>
	<h1> 요청한 나이 + 10의 계산결과는?  <%=Integer.parseInt(requestAge) + 10%>   </h1>	
	
</body>
</html>










