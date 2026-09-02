<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%-- JSTL 중에서 core 라이브러리 태그들 사용하기 위해 외부주소 요청 --%>    
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>   
    
<%-- JSTL 중에서 formatting 라이브러리 태그들 사용하기 위해 외부주소 요청 --%>    
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>     
    
<%-- 요청한 데이터 한글처리 --%>    
<% request.setCharacterEncoding("UTF-8"); %>    


<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	 <h4>로케일 설정</h4>
 <%--
	  <fmt:setLocale> 태그 
	  - 국가별 다른 통화 기호나 날짜를 표현할때 사용하는 태그 
 --%>

	<%-- java.util 패키지에서 제공하는 Date 클래스의 기본생성자를 호출해 객체를 생성해서 today변수에 저장합니다.
		 참고. Date 클래스의 기본생성자로 객체를 생성하면?  오늘 날짜와 시간값을 가지는 Date 클래스의 객체가 만들어 집니다.
	 --%>
	<c:set var="today"  value="<%=new java.util.Date()%>"   />

	한글로 설정 : <fmt:setLocale value="ko_kr"/>
	<fmt:formatNumber  value="10000" type="currency" />
	<fmt:formatDate value="${today}"/>
	<br>
	
	일어로 설정 : <fmt:setLocale value="ja_JP"/>
	<fmt:formatNumber  value="10000" type="currency" />
	<fmt:formatDate value="${today}"/>
	<br>	
	
	영어로 설정 : <fmt:setLocale value="en_US"/>
	<fmt:formatNumber  value="10000" type="currency" />
	<fmt:formatDate value="${today}"/>
	<br>	


</body>
</html>


















