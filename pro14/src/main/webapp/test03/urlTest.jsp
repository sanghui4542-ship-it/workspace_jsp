<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%-- JSTL의 core 라이브러리 태그들을 사용하기 위해 외부 사이트에서 불러오는 taglib 디렉티브 태그 한줄 작성 --%>    
<%@ taglib  uri="http://java.sun.com/jsp/jstl/core"  prefix="c" %>   

<% request.setCharacterEncoding("UTF-8");  %>   
    
<%-- 클라이언트가 최초로 urlTest.jsp를 브라우저 주소창으로 요청했을때 전체 URL중에서 컨텍스트 주소 "/pro14" 만 얻어 변수에 저장 --%>    
<c:set var="contextPath" value="${pageContext.request.contextPath}"  />    
    
<%-- 현재 페이지에 작성한 <a>태그를 나중에 클릭해서 요청할 전체주소(URL)만들때 사용되는 c:url 태그 작성하자. --%>    

<%-- 만들 url :  url1 =  "/test01/member1.jsp?id=hong&pwd=1234&name=홍길동&email=hong@test.com" --%> 

<c:url var="url1" value="/test01/member1.jsp"> 
	<c:param name="id" value="hong"  />
	<c:param name="pwd" value="1234" />
	<c:param name="name" value="홍길동"/>
	<c:param name="email" value="hong@test.com"/>
</c:url>  

<a href="${url1}">회원정보 출력 요청</a>

<hr>

<a href="${contextPath}/test01/member1.jsp?id=hong&pwd=1234&name=홍길동&email=hong@test.com">회원정보 출력 요청</a>










