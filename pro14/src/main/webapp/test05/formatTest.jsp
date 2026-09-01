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
	<h2>fmt의 formatNumber 태그를 이용한 숫자 포맷팅 예제</h2>

	<c:set var="price" value="100000000" />

	<%-- groupingUsed="true" 설정하면 숫자를 3자리씩 콤마로 표시해 변환합니다. --%>
	
	<fmt:formatNumber  value="${price}"  type="number" var="priceNumber" groupingUsed="true"/>
	<br>
	
					<%-- 100,000,000 --%>
	일반 숫자형태로 표시 : ${priceNumber} <br>
	
	통화 기호 표시 1 : <%-- ₩100,000,000 --%>	
	<fmt:formatNumber  value="${price}" type="currency" groupingUsed="true" />
	<br>
	
	<%--
		currencySymbol속성에 
		type 속성에 지정한 currency ₩ 대신 개발자가 원하는 기호 $표시로 설정 해서 변환 할수 있다.
	 --%>
	통화 기호 표시 2 : <%--$100,000,000 --%>
	<fmt:formatNumber   value="${price}" type="currency" currencySymbol="$" groupingUsed="true"/>
	<br>
	
	퍼센트로 표시 :  <%-- 10,000,000,000% --%>
	<fmt:formatNumber value="${price}" type="percent" groupingUsed="true"/>
	


</body>
</html>











