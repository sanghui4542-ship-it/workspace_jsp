<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
	/*
	result2.jsp 
	- login2.jsp 화면에서 아이디, 비밀번호를 입력후 로그인 요청하면 요청 받아 처리하는 서버페이지 
	
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
%>		
		<%-- 아이디를 입력하지 않고 result2.jsp로 로그인 요청을 하므로  
		     다시 아이디를 입력하고 요청하는 login2.jsp 화면을 브라우저로 보여주기 위해  forward 액션태그를 사용해 login2.jsp 포워딩시 
		     request 내장객체 메모리에  msg 전역변수에 저장된 값을 포함시켜 포워딩 하자.    
		
			 포워딩 시  request 내장객체 메모리에  요청하는 데이터를 포함(바인딩) 시키는 방법은 param 액션태그에 설정 하면 된다.
		--%>
		<jsp:forward page="login2.jsp">			
			<jsp:param name="msg" value="<%=msg%>"/>		
		</jsp:forward>			
<%	
	}
	//조건 : 아이디를 입력하고 로그인 요철 했다면?
%>
	<h1>환영합니다. 로그인 되셨습니다. <%=userID%>님!!</h1>
<%!
	/*
	login2.jsp화면에서 아이디를 입력하지 않고 로그인 버튼을 누르면 
	재요청(포워딩)에 의해 login2.jsp화면에 보여질 경고메세지를 전역변수를 만들어 저장
	*/
	String msg = "아이디를 입력하지 않았습니다. 아이디를 입력해주세요";

%>








