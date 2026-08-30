<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
	/*
	include1.jsp페이지에서 작성한 <jsp:include>액션태그에 의해 재요청 당한 duke_image.jsp파일입니다.
	
	   재요청(포워딩) 하여 공유 받은 request내장객체 메모리에 접근해서  재요청할때 요청할 값을 꺼내서 사용
	
		  <jsp:include page="duke_image.jsp" >		  
				<jsp:param name="name"   value="duke"/>
				<jsp:param name="imgName" value="duke.png"/>	
		  </jsp:include>
	*/
	//순서1. <jsp:include>액션태그로 재요청한 duke_image.jsp현재 페이지에서 요청한 데이터들 request 영역에서 얻기 전체 한글처리
	request.setCharacterEncoding("UTF-8");
	
	//순서2. <jsp:include>액션태그로 재요청한 duke_image.jsp현재 페이지에서 요청한 데이터들을 request 영역에서 얻기
	String name = request.getParameter("name");
	String imgName = request.getParameter("imgName");
	
	//순서3. 웹브라우로 출력
%>
	<br><br>
	<h1>이름은 <%=name%>입니다.</h1>
	<br><br>
	<img src="./image/<%=imgName%>" />












