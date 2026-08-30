<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
	/*
	result.jsp 
	- login.jsp 화면에서 아이디, 비밀번호를 입력후 로그인 요청하면 요청 받아 처리하는 서버페이지 
	
	순서1. request내장객체 메모리영역에 요청한 데이터 중에서 
		   한글 문자가 하나라도 저장되어 있을수 있으므로  request객체에서 꺼내와 저장할때
	       한글 문자가 깨져서 꺼내와 지므로  문자 인코딩방식을 UTF-8로 설정.
		   요약 : 요청한 한글 문자 한글처리
	*/
	request.setCharacterEncoding("UTF-8");
	
	/* 순서2. 요청한 데이터(입력한 아이디, 비밀번호 중에서 아이디만 얻자)를  request 내장객체 메모리에서 얻기  */
	String userID = request.getParameter("userID"); //입력한 아이디 얻기 
	
	/* 순서3. 요청한 데이터(입력한 아이디)를 request내장객체 영역에서 꺼나와 얻을수 있는지 없는지 검사 */
	
	//조건 : 아이디를 입력안하고 로그인 요청 했다면?
	if(userID == null  || userID.length() == 0){
		
		//1. 자바코드를 작성하여 RequestDispatcher 방법으로  login.jsp 로 포워딩(재요청) 하여
		//	 현재 result.jsp에서 사용하고 있는 request, response 내장객체를 공유 합니다.
		/*
		RequestDispatcher dispatcher = request.getRequestDispatcher("login.jsp");
		dispatcher.forward(request, response);
		*/
		
		//2. <jsp:forward>액션 태그를 작성하여  위 1. 자바코드를 대체 할수 있게 코드 작성
%>		
		<%-- 작성 방법 :  <jsp:forward  page="재요청할 URL"/> --%>
		<jsp:forward page="login.jsp"/>
			
<%	
	}
	//조건 : 아이디를 입력하고 로그인 요철 했다면?
%>
	<h1>환영합니다. 로그인 되셨습니다. <%=userID%>님!!</h1>










