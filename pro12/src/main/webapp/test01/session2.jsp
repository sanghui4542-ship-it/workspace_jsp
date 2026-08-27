<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
	/*
	session2.jsp 설명
	
	session1.jsp에서 <a>태그를 클릭하여 요청받은 session2.jsp입니다.
	당연히 같은 브라우저 종류의 브라우저 창을 이용했기 떄문에 session1.jsp에서 사용했던 HttpSession객체 메모리는
	session2.jsp페이지에서 공유받아 사용할수 있습니다.
	
	SessionTest서블릿에서 HttpSession객체 메모리에 바인딩(session.setAttribute("name", "이순신");) 했었던
	"이순신" 값을 얻어 현재 session2.jsp에서 공유 받아 사용가능한지 ,
	
	그리고 session1.jsp에서 HttpSession객체 메모리에 바인딩(session.setAttribute("address", "서울시 강남구");) 했었던
	"서울시 강남구" 값을 얻어 현재 session2.jsp에서 공유 받 사용가능한지 테스트 해보자.
	
	참고. session(HttpSession객체 메모리)는 요청한 클라이언트의 브라우저창을 닫기 전까지는 
	     계속 톰캣 서버 메모리에 유지되서 다른 서버페이지(session1.jsp와 session2.jsp)에 메모리를  공유 합니다.
	
	1. SessionTest서블릿에서 HttpSession객체 메모리에 바인딩(session.setAttribute("name", "이순신");) 했었던
	"이순신" 값을 얻어 현재 session2.jsp에서 공유 받아 사용가능한지 알아 보기 위해
	HttpSession내장객체 메모리에 접근해서 key가  "name"인   value "이순신" 값을 얻어 변수에 저장 해보자.
	*/
	String name = (String)session.getAttribute("name");
	//    "이순신"
	
	/*
	2.그리고 추가로 session1.jsp에서 HttpSession객체 메모리에 바인딩(session.setAttribute("address", "서울시 강남구");) 했었던
	"서울시 강남구" 값을 얻어 현재 session2.jsp에서 공유 받 사용가능한지 알아보기 위해 
	HttpSession내장객체 메모리에 접근해서 key가  "address"인   value "서울시 강남구" 값을 얻어 변수에 저장 해 보자.
	*/
	String address = (String)session.getAttribute("address");
	//    "서울시 강남구"
	/*
	3.아래에 표현식 <%= % > 태그를 이용해 HttpSession에서 얻은 변수 정보를 브라우저에 출력 하자.
	  만약 제대로 출력 되면? 
	   HttpSession객체 메모리는? 
	  요청한 클라이언트 브라우저 창을 닫기 전까지는 모든 서버페이지에서 바인딩했었던 정보를 얻어 사용가능 함을 이해 할수 있을 것이다.
	*/	
%>
	이름은 <%=name%> 입니다. <br>
	주소는 <%=address%> 입니다. <br>






