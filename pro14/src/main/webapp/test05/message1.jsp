<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%-- JSTL 전체 라이브러리에 속한 core, fmt 라이브러리 태그들을 사용하기 위해 요청 주소 작성 --%>    
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>

<% request.setCharacterEncoding("UTF-8"); %>    
     
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
<%--
실습 내용 
- <fmt:setLocale> 태그를 이용해 jsp 페이지에 표시할 언어를 지정한 후 
  <fmt:bundle> 태그를 이용해 resource 패키지의 .properties 파일의 데이터를 읽어옵니다.
  그리고 <fmt:message> 태그를 이용해  .properties 파일에 작성한 키(mem.name)에 대한 값을 각각 jsp 페이지에 출력합니다.
--%>

<%-- 1. jsp 페이지에 표시할 언어를 지정합니다. --%>
<%-- <fmt:setLocale value="ko_KR"/>   --%> <%-- 언어를 한국어로 지정함. --%>

<fmt:setLocale value="en_US"/> <%-- 언어를 영어로 지정함. --%>

<h1>
	<%--2. <fmt:bundle>태그를 이용해 resource패키지의  .properties 파일의 데이터를 읽어오게 설정--%>
	<fmt:bundle basename="resource.member">
	
		<%--3. <fmt:message>태그를 이용해 resource패키지에 아래에 만들어 놓은 
			   파일이름이 member 로 시작하는 파일의 확장자가 .properties에 작성한 키를 이용해 값을 얻어 
			   이자리에 메세지로 출력--%>
		이름 : <fmt:message  key="mem.name" /> <br>
		주소 : <fmt:message  key="mem.address"/> <br>
		직업 : <fmt:message  key="mem.job"/>
	
	</fmt:bundle>


</h1>




</body>
</html>










