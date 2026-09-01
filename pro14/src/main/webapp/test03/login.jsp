<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%--  JSTL의  core 라이브러리 태그들을 사용하기 위해 외부 사이트에서 불러오는 taglib 디렉티브 태그 한줄 작성 --%>    
<%@ taglib  uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>        
    
<%-- 
	c:set 태그를 사용하여 변수를 선언하고   클라이언트가 login.jsp를 요청한 전체 URL 중에서 컨텍스트 주소를 얻어 저장
	
	방법 :  pageContext 내장객체의 request 변수를 호출하면  HttpServletRequest 객체 주소를 얻을수 있다.
		   그런 다음  HttpServletRequest 객체의 contextPath변수를 호출하면
		   클라이언트가 login.jsp를 최초로 요청한 전체 URL 중에서  컨텍스트 주소("/pro14") 를 얻어 올수 있다.

		URL :  http://localhost:8181/pro14/test03/login.jsp   
		컨텍스트주소 : /pro14	
--%>
<c:set  var="contextPath"  value="${pageContext.request.contextPath}" />

<a href="${contextPath}/test03/memberForm.html">회원등록하러가기</a>
<%--             /pro14/test03/memberForm.html --%>

<hr>

<%
	/* 자바코드를 작성해서 String contextPath2 변수를 하나 선언하고,
	   클라이언트가 login.jsp를 요청한  전체 URL 중에서 컨텍스트 주소("/pro14")를 반환받아 얻을 수도 있다.*/
	   String contextPath2 = request.getContextPath();
			 //  "/pro14"
%>
<a href="<%=contextPath2%>/test03/memberForm.html">회원등록하러가기</a>
<%--                /pro14/test03/memberForm.html --%>





