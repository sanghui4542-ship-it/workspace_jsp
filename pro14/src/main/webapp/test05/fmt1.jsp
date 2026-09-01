<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
    
<%-- JSTL 전체 라이브러리에 속한 core, fmt 라이브러리 태그들을 사용하기 위해 요청 주소 작성 --%>    
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>

<% request.setCharacterEncoding("UTF-8"); %>      
        
<%--

	 국제화(Formatting)태그
	 - 이태그들은  국제화 태그로, 국가별로 다양한 언어, 날짜, 시간, 숫자형식을 설정할때 사용하는 태그입니다.
	 - 종류
	 	분류			태그명				기능
	 	숫자포맷		formatNumber		숫자 포맷을 설정하는태그
	 			    parseNumber			문자열을 숫자 포맷으로 변환하는 태그
	 	
	 	날자포맷		formatDate			날짜나 시간의 포맷을 설정하는 태그
	 				parseDate			문자열을 날짜 포맷으로 변환하는 태그
	 				
	 	타임존설정		setTimeZone			시간대 설정정보를 변수에 저장하는 태그 
	 				timeZone			시간대를 설정하는 태그
	 				
	 	로케일설정		setlocale			통화 기호나 시간대를 설정한 지역에 맞게 표시 하는 태그 
	 				requestEncoding		요청 매개변수의 문자셋을 설정합니다.
	
	
	
	
	주제 : 숫자 포맷팅 및 파싱
	
		<fmt:formatNumber> 사용 형식
		
			<fmt:formatNumber  
				 
				 value="출력할 숫자"  
				 
				 type="출력 양식 percent(퍼센트), currency(통화), number(일반 숫자,기본값) 등을 설정합니다. "     
				 
				 var="출력할 숫자를 변수에 저장합니다. 해당속성 사용시 즉시 출력되지 않고 원하는 위치에 출력할수 있게 변수 선언"
				 
				 groupingUsed="세자리 마다 콤마를 출력할지 여부를 설정합니다. 기본값은 true입니다."
				 
				 pattern="출력할 숫자의 양식을 패턴으로 지정합니다."
				 
				 scope="var로 선언한 변수를 저장할 내장객체 종류중 하나 지정합니다."
				 
				 />
	
	
		 <fmt:parseNumber /> 태그 사용형식
	 						
	 			<fmt:parseNumber   
	 				 
	 				 var="변환된 숫자를 저장할변수선언"
	 				 
	 				 value="변환할(파싱할) 문자열을 설정합니다."
	 				 
	 				 type="문자열의 타입을 설정합니다. 기본값은 number(숫자)입니다."
	 				 
	 				 integerOnly="정수 부분만 표시할지 여부를 결정합니다. 기본값은 false입니다."
	 				 
	 				 pattern="문자열의 양식을 패턴으로 지정합니다."
	 				 
	 				 scope="var로 선언된 변수를 저장할 내장객체 영역중 하나를 설정합니다."
	 		      />								   
 --%>        
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<h4>숫자 포맷 설정</h4>
	
	<c:set var="number1" value="12345" />
	
	<%--formatNumber 태그의 groupingUsed="true"로 설정해 큰수는 세자리마다 콤마 표시로 변환 해서 반환받아 출력 12,345 --%>
	콤마 O : <fmt:formatNumber  type="number"  value="${number1}"  groupingUsed="true"  /> <br>
	
	<%--반면 groupingUsed="false"로 설정하면 세자리마다 콤마로 구분하지 않은 숫자로 변환해서 반환받아 출력 12345 --%>
	콤마 X : <fmt:formatNumber  type="number"  value="${number1}"  groupingUsed="false"  /> <br>
	
	<%-- type 속성의  값을 다양하게 설정하면 통화 기호를 달거나 백분율 %로 숫자 포맷 형식을 변경해서 출력합니다. ₩12,345
		 만약 변환된 ₩12,345 를 저장하려면?  var="변수명" 을 작성해 변수에 저장해서 사용합니다.	
	 --%>
	<fmt:formatNumber   type="currency"  value="${number1}"  var="printNum1" />
	
			<%--₩12,345 --%>
	통화기호 : ${printNum1} <br>
	
		
	<fmt:formatNumber  value="0.03"  type="percent" var="printNum2"  />
		
			<%--   3% --%>
	퍼센트  :  ${printNum2} <br>
	
	<hr><hr>
	
	
	
	<h4>문자열을 숫자로 변경(포멧팅)</h4>
	
	<c:set  var="number2" value="${'6,789.01'}" />
	
	<fmt:parseNumber  value="${number2}" pattern="00,000.00" var="printNum3" />
	
<%-- 소수점 까지 : 6789.01 --%>
	 소수점 까지 : ${printNum3} <br>
	 
	 <%-- integerOnly="true" 로 설정해 '6,789.01' 문자열을  정수부 6789만 추출해서 반환받아 printNum4변수에 저장 할수 있음 --%>
	<fmt:parseNumber   value="${number2}" integerOnly="true" var="printNum4"  />
	
<%-- 정수 부분만 : 6789 --%>	
	 정수 부분만 : ${printNum4} <br>
	 
	 


</body>
</html>












