<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
//forward4.jsp : 클라이언트가 최초로 브라우저 주소창에  URL을 입력하여 요청한 서버페이지

	/*
	예제 내용
	request, session, application 내장 객체에서는 데이터를 바인딩해서 다른 JSP 포워딩시  전달합니다.
	그런데 각 내장 객체에 바인딩하는 키 이름이 같은 경우 JSP에서는 각 내장 객체에 바인딩된  값을 EL태그로 얻어 출력 하는데
	각 내장 객체의 접근 하는 우선순위에 따라 순서대로 각 내장객체의 값을 키로 얻어 출력합니다. 
	이번에는 각 내장 객체에 같은 키 이름으로 바인딩할 때의 EL태그로 얻어 출력 우선순위를 알아보겠습니다.
	*/
	
	request.setCharacterEncoding("UTF-8");   //요청한 한글 문자 인코딩 방식 UTF-8설정
	
	//request 내장객체에 개발자가 수동으로 직접 바인딩
	request.setAttribute("id", "hong");
	request.setAttribute("pwd", "1234");
	request.setAttribute("address", "서울시 강남구");
	
	//session 내장객체에 개발자가 수동으로 직접 작성해서 바인딩
	session.setAttribute("name", "홍길동");
	
	//application 내장객체에 개발자가 수동으로 직접 작성해서 바인딩
	application.setAttribute("email", "hong@test.com");
	
	//member4.jsp 로 디스패처방식으로 포워딩시 request 공유
	request.getRequestDispatcher("member4.jsp").forward(request, response);

%>





