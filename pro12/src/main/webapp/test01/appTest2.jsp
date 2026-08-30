<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
	/*
	================================================================
	appTest2.jsp 역할 요약 (각 내장객체 메모리에 저장된 값을 꺼내서 사용하는 서버페이지)
	---------------------------------------------------------------
	1. appTest1.jsp 요청 후 , 사용자가 <a>링크를 클릭하여 이 appTest2.jsp페이지를 요청함
	2. 이미 톰캣서버 메모리에 올라가있는 유지되는
	   - session 내장객체에 보관된 데이터
	   - application 내장객체에 보관된 데이터 를 각각 꺼내서 사용함
	3. 꺼낸 데이터를 요청한 클라이언트의 브라우저 화면으로 전달해 응답(출력)함   
	*/
	//============================
	// session 내장객체에 보관된 데이터 얻기
	//=============================
	//appTest1.jsp에서 아래 코드로 보관(바인딩) 했었음 
	//session.setAttribute("name", "이순신");
	
	//session 내장객체 메모리에서 "name"이라는 key와 함께 묶어서 바인딩 했던 "이순신" value를 얻자
	String name = (String)session.getAttribute("name");
	//-> 결과 : "이순신"

	//====================================
	// application 내장 객체에 보관된 데이터 얻기 
	//===================================
	//appTest1.jsp에서 아래 코드로 보관(바인딩,저장) 했었음
	//application.setAttribute("address", "서울시 성동구");
	
	//application 내장객체 메모리에서 "address"이라는 key와 함께 묶어서 바인딩 했었던 "서울시 성동구"를 value로 얻자
	Object obj = application.getAttribute("address");
//  Object obj = "서울시 성동구";

	//다운캐스팅 해서 "서울시 성동구" 저장
	String address = (String)obj;
    //-> 결과 : "서울시 성동구"
%>
<!--  JSP 기술중에서 표현식 태그를 사용하여 내장객체에서 꺼낸 값을 브라우저로 화면으로 출력(응답) -->

<h1>이름은 <%=name%></h1>
<!--  session 내장객체 메모리에서 가져온 "이순신" 출력 -->
 <!-- 같은 브라우저창이니 session 에서 공유받아 출력할수 있음       -->

<h1>주소는 <%=address%></h1> 
<!--  application 내장객체 메모리에서 가져온 "서울시 성동구" 출력 -->
<!--  톰캣서버가 중지되기 전까지 모든 종류의 브라우저에서 공유받아 출력할 수 있음  -->






