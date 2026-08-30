<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
		/*
		request2.jsp 역할 설명
		-----------------------
		- request1.jsp에서 request내장객체 메모리에 바인딩 해둔 데이터를
		  RequestDispatcher의 forwar() 방식으로 포워딩 으로 그대로 공유 받아 사용하는 페이지 입니다.
		- 즉, request1.jsp -> request2.jsp 이 두페이지는 "하나의 요청(request)" 흐름 안에 존재합니다.
		
		순서1. request1.jsp 에서 request내장객체에 아래에 코드처럼 작성해서 바인딩 했었던
		      "이순신" 그리고 "서울시 강남구" 데이터를 얻어와보자.
		      
		      request.setAttribute("name", "이순신");
			  request.setAttribute("address", "서울시 강남구"); 
		            
		      만약 얻어와 지면? request객체 메모리는 다른 서버페이지(request2.jsp)를 재요청(포워딩)하면 유지되는 메모리 임을 확인 할수 있다.
		*/
		String name = (String)request.getAttribute("name");
		//    "이순신"      
		String address = (String)request.getAttribute("address");
		//    "서울시 강남구"
		
//     순서2. 공유받은 request 내장객체 메모리에서 얻은 데이터들을 브라우저 화면으로 응답(출력)
%>
	   <h1>request1.jsp 에서 RequestDispatcher 방법으로 포워딩해서 제공받은 request 내장객체 메모리 내부의 바인딩 정보</h1>
       <h1>현재 응답한 서버페이지(request2.jsp)</h1>
       <h1>이름 : <%=name%>입니다.</h1>
	   <h1>주소 : <%=address%>입니다.</h1>




