<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<%
		/*
		설명
			최초 클라이언트가 웹브라우저 주소창에 http://localhost:8181/pro14/login.jsp (URL)입력하여
			톰캣 서버에 URL을 전달해 login.jsp 페이지를 요청합니다.
			
			요청 받은 login.jsp에서는 요청 하나당 톰캣에 의해 생성되는 request 내장객체 메모리를 얻어 사용합니다.
			이  request내장객체 메모리 영역의 header 영역에  요청했던 URL전체 주소가 보관되어 있습니다.
			
			최초 클라이언트가 요청한 URL중에서  /pro14 <-컨텍스트 주소를 얻어 사용할수 있습니다.
		*/		
		//순서1. 요청한 한글문자 데이터 한글처리
		request.setCharacterEncoding("UTF-8");
	%>
	<%-- 순서2. 요청받은 login.jsp 화면을 꾸미기 위해 개발자가 로그인 폼 디자인 합니다. --%>
	<form action="result.jsp" method="post">
		아이디: <input type="text" name="userID"><br>
		비밀번호: <input type="password" name="userPW"><br> 
		<input type="submit" value="로그인"> <input type="reset" value="취소">
	</form>

	<%-- <a>회원가입하러가기</a> 태그를 만들때  href속성에 작성하는 URL 작성방법1. --%>
	<a href="http://localhost:8181/pro14/test01/memberForm.html">회원가입하러가기</a>

	<%-- <a>회원가입하러가기</a> 태그를 만들때 href속성에 URL 작성방법2. --%>
	<%
	/*
	설명
		최초 클라이언트가 웹브라우저 주소창에 http://localhost:8181/pro14/login.jsp (URL)입력하여
		톰캣 서버에 URL을 전달해 login.jsp 페이지를 요청합니다.
		
		요청 받은 login.jsp에서는 요청 하나당 톰캣에 의해 생성되는 request 내장객체 메모리를 얻어 사용합니다.
		이  request내장객체 메모리 영역의 header 영역에  요청했던 URL전체 주소가 보관되어 있습니다.
		
		최초 클라이언트가 요청한 URL중에서  /pro14 <-컨텍스트 주소를 얻어 사용할수 있습니다.
	*/		  
		//자바코드를 작성하여 클라이언트가 요청한 전체 URL 중에서 컨텍스트 주소 /pro14 를 얻어 저장 
		String contextPath = request.getContextPath();
	%>
	<a href="<%=contextPath%>/test01/memberForm.html">회원가입하러가기</a>
	
	<br><br>
	
	<%--
		 <a>회원가입하러가기</a> 태그를 만들때  href속성 URL 작성방법3
	
			EL태그에서 제공하는 pageContext 내장객체로 얻는 방법
			
				pageContext 내장객체 내부에 만들어져 있는  request속성(변수)을 호출하면
				HttpServletRequest 내장객체 메모리 주소 번지를 얻고
				얻은 HttpServletRequest 내장객체 메모리 내부에 만들어져 있는 contextPath속성(변수)를 호출하면
				contextPath속성(변수)에 저장된 "/pro14" 컨텍스트 주소를 얻을수 있다.
				
		 --%>
		<a href="${pageContext.request.contextPath}/test01/memberForm.html">회원가입하러가기</a>
						       <!-- <a href="/pro14/test01/memberForm.html">회원가입하러가기</a> -->
	
	<%--
			EL 표현언어에서 제공해주는 pageContext내장객체 
			
			- pageContext내장객체는 javax.servlet.jsp.PageContext클래스를 상속해
			  톰캣 컨테이너가 JSP파일 실행시 자동으로 생성해서 제공해 주는 내장객체 입니다.
			  
			사용 예
				<a>태그를 이용해 다른 서블릿이나 JSP를 요청하는 방법은 2가지 입니다
					방법1. 컨텍스트 주소(pro14)를 직접 <a>의 href속성에 작성 해서 사용 하는 방법
					
						  <a href="/pro14/test01/memberForm.html"> 회원가입</a>  
	
					방법2. 자바의 request객체의 getContextPath메소드를 호출하여 
						  클라이언트가 요청한 전체 URL중에서 컨텍스트 주소 /pro14 만 얻어 
						  <a>태그의 href속성에 작성해서 사용 하는 방법
						  
						  <a href="<%=request.getContextPath()%>/test01/memberForm.html">회원가입</a>
	
					문제점
						 방법1은  컨텍스트 주소이름이 바뀌면 일일이 찾아서 수정해야한다는 단점이 있고
						 방법2는   자바코드가 HTML코드와 사용되므로 작업이 복잡해 진다는 단점 이 있다
						 
					해결법
						  EL문법에서 제공해주는 pageContext객체의 변수를 이용하면
						  컨텍스트 주소를 반환 받을수 있다.
						  
	 --%>
</body>
</html>













