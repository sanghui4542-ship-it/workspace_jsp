<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
	/*
	===============================================
	appTest1.jsp 역할 요약 (값을 저장(바인딩) 하는 서버페이지)
	-----------------------------------------------
	1. 클라이언트(웹브라우저)가  이 appTest1.jsp를 요청함
	2. 톰캣 서버는 이 appTest1.jsp 의 코드를 실행하면서 
	   - session 내장객체메모리 
	   - application 내장객체 메모리에 각각  값을 저장(바인딩)함
	3. 다음 서버페이지(appTest2.jsp)에서 각 내장객체 메모리에 바인딩한 값을 꺼내어 사용하도록 준비함.   
	================================================
	*/
	//[순서1] 사용자가 웹브라우저 주소창에 아래 주소를 입력해서 appTest1.jsp를 요청합니다.
	//		 http://localhost:8181/pro12/test01/appTest1.jsp
	//
	// -> 이 순간, 요청 주소가 Tomcat 서버로 전달됨
	
	//[순서2] Tomcat 서버는 appTest1.jsp의 코드를 실행하기 전에 
	//       session, request, application 같은 "내장 객체"를 자동으로 생성 해 줌
	//
	//우리는 이 중에서 session과 application 메모리를 사용함
	
	//=========================================
	// session 내장객체 설명
	//========================================
	//- 요청한 사용자(브라우저) 1명당  1개씩 생성되는 톰캣 서버에 올라가 유지 되는 메모리 공간
	//- 로그인 정보, 사용자 정보 저장에 주로 사용
	//- 브라우저 를 닫거나 세션 유효시간이 만료되면 톰캣 서버에 올라가 있다가 자동 삭제됨
	
	//session 내장객체 메모리에 "name"이라는 이름(key)와 value를 한쌍으로 묶어 저장(바인딩)
	session.setAttribute("name", "이순신");

	//======================================
	//application (ServletContext) 내장객체 설명
	//=====================================
	//- pro12 웹 프로젝트(웹 애플리케이션) 전체에 포함된 모든 서블릿 또는 jsp페이지에서 공유하는 톰캣 서버에 올라가는 메모리 공간
	//- 모든 사용자, 모든 JSP/Servlet이 접근 가능
	//- TomCat 서버가 종료되면 함께 삭제 됨
	
	//application 내장객체 메모리에 "address"이라는 이름(key)으로 value를 한쌍으로 묶어 저장(바인딩)
	application.setAttribute("address", "서울시 성동구");
%>    
<!-- Tomcat 서버에서 session / application 내장 객체 메모리에 값이 바인딩 되었음을 클라이언트의 브라우저화면에 안내 -->  
<h1> session 과  application 내장객체 메모리영역에 각각 key-value 형태로 묶어 저장(바인딩) 완료</h1>
   
<a href="appTest2.jsp">두 번째 서버페이지(appTest2.jsp) 이동</a> 
    
    
    
    
    
    
    
    
    
    
    
    
    
    
