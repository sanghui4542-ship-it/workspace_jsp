<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%
	//순서1. 클라이언트가 브라우저 주소창에 login2.jsp를 요청할 URL을 입력하여 Tomcat에게 요청한다.
	//      URL : http://localhost:8181/pro13/login2.jsp

	//순서2. 요청받은 Tomcat은 새로운 request내장객체 메모리를 생성해 login2.jso로 전달합니다.
	//		만약 요청 URL에 포함된 요청 한글문자가 있으면 깨져서 꺼내와 지므로 한글처리 
	request.setCharacterEncoding("UTF-8");
%>   
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<%-- 주제 : <jsp:forward /> 액션태그를 사용한 디스패처방법 포워딩 --%>
	
<%
/*
상황1. 최초로 클라이언트가 login2.jsp를 요청하면 request객체 메모리는 톰캣서버에 의해 하나 생성된다.
      request 객체 영역에는 아무런 값도 저장되지  않았기 떄문에 (null 비어있는 값이 저장되어 있기 때문에)
      개발자가 조건식으로 판단해서 경고 메세지("아이디를 입력하지 않았습니다. 아이디를 입력해주세요") 를 보여주지 말자.

상황2. login2.jsp화면에서 아이디를 입력하지 않고! 로그인 버튼을 눌러 result2.jsp로 로그인 요청해서 갔다가~ 다시 login2.jsp를 재요청해서 오는 상황
	  이  상황에서는 <jsp:param name="msg" value="아이디를 입력하지 않았습니다. 아이디를 입력해주세요"/>액션태그에 설정한 메세지를 
	  request 객체 영역에서 꺼내어 얻어 사용
*/	
	String msgService = request.getParameter("msg");
 //       msgService = "아이디를 입력하지 않았습니다. 아이디를 입력해주세요" <-------------------상황2
 //       msgService = null <----------------상황1	
 
 	if(msgService != null){ //아이디 입력하지 않고 result2.jsp로 로그인 요청 했다면? <---------상황2
 		
 			//로그인 요청하는 사용자에게   "아이디 입력하지 않았습니다. 아이디를 입력해주세요"  브라우저로 출력해 보여 준다.
 			out.print("<h1>" + msgService + "</h1>");
 	}
%>
	<form action="result2.jsp" method="post">
		아이디 : <input name="userID"><br>
		비밀번호 : <input type="password" name="userPw"><br>
		
		<%-- 로그인 요청 버튼으로 만들수 있는 3가지 경우 --%>
		<input type="submit" value="로그인">		
		<!-- <button>로그인</button> -->
		<!-- <input type="image" src="버튼에 보일 이미지파일 경로"> -->
	
		<input type="reset" value="다시입력">
	</form>
	

</body>
</html>









