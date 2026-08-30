<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
	/*
	request1.jsp 서버페이지 
	
	순서1. 클라이언트가 웹브라우저 주소창에 http://localhost:8181/pro12/test01/request1.jsp 요청 주소 입력후 
	      톰캣 서버에 request1.jsp 서버페이지를 요청하면
	      톰캣 서버는 요청한 주소 하나당 하나의 request내장객체 메모리를 생성해서 제공해 줍니다.
	
	 순서2. 제공받은 request내장객체 메모리에 우리 개발자가 request1.jsp서버페이지를 구현할때  데이터를 바인딩 합니다.
	*/	
	request.setAttribute("name", "이순신");
	request.setAttribute("address", "서울시 강남구");
	
    /*
	 순서3. 다른 서버페이지(.jsp 또는 서블릿)를 다시 요청하면 
	      요청주소(URL) 하나당 하나의 request 내장객체 메모리를 톰캣 서버가 새롭게 생성해서 제공하기 떄문에
	      request1.jsp에서 사용한 request내장객체 메모리를 request2.jsp다른 서버페이지에서 공유해서 사용하기 위해서는
	      RequestDispatcher객체의 forwar()메소드 이용방법으로  포워딩(재요청) 해야 합니다.
	요약 : request1.jsp에서 request2.jsp를 포워딩(재요청)합니다.
    */
    
    //RequestDispatcher 방법의 포워딩(재요청) : 포워딩 시 request, response 내장객체 메모리를 request2.jsp로  공유 
     	request.getRequestDispatcher("request2.jsp").forward(request, response);
    
	//Redirect 방법의 포워딩(재요청) -  위 requst내장객체 메모리를 다른 request2.jsp에 공유 할수는 없지만 페이지 전환(이동)시 사용하자.
	//=> response.sendRedirect("request2.jsp");	
    
    
%>




