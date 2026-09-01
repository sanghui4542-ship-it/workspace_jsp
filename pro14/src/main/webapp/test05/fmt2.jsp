<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%-- JSTL 중에서 core 태그들을 사용하기 위해 외부 주소로 요청 --%>    
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%-- JSTL 중에서 fomatting 태그들을 사용하기 위해 외부 주소로 요청 --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>    
    
<% request.setCharacterEncoding("UTF-8"); %>    
 
 <%-- 
	 날짜 포맷 및 타임존
	 
	 <fmt:formatDate /> 태그    (날짜 포맷)
	 
	 	- 이태그는 날짜와 시간 포맷을 지정하는 태그입니다.
	 	
	 	- 문법
	 		 <fmt:formatDate   
	 		 	  
	 		 	  value="변환하여출력할날짜"
	 		 	  
	 		 	  type="변환하여 출력할 양식 세가지중 하나   (출력양식 종류 : 날짜 date,  시간  time,  날짜 및 시간모두  both  )"
	 		 	  
	 		 	  var="변환하여 출력할 날짜 또는 시간을 저장할 변수"
	 		 	
	 		 	  dataStyle="날짜 스타일 종류 지정  (default, short,  medium, long, full 중 하나 )"
	 		 	  
	 		 	  timeStyle="시간 스타일 종류 지정 (default, short,  medium, long, full 중 하나 ) "
	 		 	
	 		 	  pattern = "출력할 날짜 및 시간의 양식을 패턴으로 직접 지정합니다."
	 		 	
	 		 	  scope = "변환한 날짜가 저장된 var의 변수를 저장할 내장객체 영역중 하나"

	 		 />
	 	
	 
	  타임존
	 	<fmt:timeZone> </fmt:timeZone> 태그
	 	
	 	- 출력할 시간의 세계 수도 지역에 맞게 시간대를 설정할수 있는 태그 
	 	
	 	- 위 <fmt:formatDate>태그를 <fmt:timeZone>여는 부분과 닫는 부분 사이에 작성하면 ,
	 	  설정한 시간대에 따라 다른 시간을 출력할수 있습니다.
	 	  
	 	  <fmt:timeZone  value="America/Chicago" >
	 	  	
	 	  		<fmt:formatDate value="날짜및시간" ....  />
	 	  
	 	  </fmt:timeZone>


--%>     
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<%--
		  java.util패키지에서 제공해주는 Date클래스의 기본생성자를 호출해서 객체를 생성해서 변수에 저장합니다.
		    참고. Date클래스의 기본생성자로 객체를 생성하면? 오늘 날짜와 시간값을 가지는 Date객체가 만들어집니다.
	 --%>
	 <c:set  var="today"  value="<%=new java.util.Date()%>"  />
	 
	 <%--Tue Feb 10 11:31:03 KST 2026 --%>
	 <c:out value="${today}" /> <br>
	 
	 <h4>날짜 포맷(변환)</h4>
	 
	 <%-- 날짜만 포맷(변환) 하기 위해서는  type="date"로 설정하고, 날짜스타일은 dateStyle속성에 각각 지정합니다.  --%>
	 
 <%--full : 2026년 2월 10일 화요일 --%>
	 full : <fmt:formatDate  value="${today}" type="date" dateStyle="full" /> <br>

<%-- long : 2026년 2월 10일 --%>
	 long : <fmt:formatDate  value="${today}" type="date" dateStyle="long" /> <br>

<%-- default : 2026. 2. 10. --%>
	 default : <fmt:formatDate  value="${today}" type="date" dateStyle="default" /> <br>
	
<%-- short : 26. 2. 10. --%>
	 short : <fmt:formatDate  value="${today}" type="date" dateStyle="short" /> <br>
	 
	 <hr>

	 <h4>시간만 포맷(변환)</h4>
	 
	 <%-- 시간만 포맷(변환) 하기 위해서는 type="time"로 설정하고, 시간스타일은 tiemStyle속성에 값을 각각 지정합니다. --%>
	 
<%-- full : 오전 11시 41분 40초 대한민국 표준시 --%>
	 full : <fmt:formatDate  value="${today}" type="time" timeStyle="full" /> <br>

<%-- long : 오전 11시 42분 35초 KST --%>
	 long : <fmt:formatDate  value="${today}" type="time" timeStyle="long" /> <br>

<%-- default : 오전 11:43:31 --%>
	 default : <fmt:formatDate  value="${today}" type="time" timeStyle="default" /> <br>

<%-- short : 오전 11:44 --%>
	 short : <fmt:formatDate  value="${today}" type="time" timeStyle="short" /> <br>
	 
	 <hr>
	 
	 <h4>날짜/시간 모두 포멧(변환)</h4>
	 <%-- 참고. 날짜와 시간을 포맷해서 동시에 얻어 출력하기 위해서는 type="both"로 설정합니다. --%>
	
	 <%--2026년 2월 10일 화요일 오전 11시 47분 3초 대한민국 표준시 --%>
	 <fmt:formatDate value="${today}" type="both" dateStyle="full" timeStyle="full"/> <br>

	 <%-- dateStyle속성과  timeStyle속성을 설정하는 대신
	 	  pattern속성의 값을 설정 해서  개발자가 직접 날짜와 시간 스타일의 포맷 형식을 설정해서 모두 변경한 날짜와 시간을 받아볼수 있다.
	  --%>											  <%-- 2026/02/10 11:51:59 --%>
	 <fmt:formatDate value="${today}" type="both" pattern="yyyy/MM/dd hh:mm:ss"   var="result" /> <br>

 	 <%-- 2026/02/10 11:51:59 --%>
	 ${result} <br>

	 <hr>
	 
	 <h4>타임존(세계 수도 도시 시간대로 다다르게 포멧 해서 표시) 설정</h4>
	 
	 <%-- 시간대를 세계협정시(전세계에서 정한 표준 시간) = GMT, 대한민국 시간보다 9시간 빠름 --%>

	 세계 협정 시간대 : 
	 <fmt:timeZone value="GMT">
	 
	 	<fmt:formatDate value="${today}" type="both" dateStyle="full" timeStyle="full" />
	 
	 </fmt:timeZone>

	 <br>
	 
	 시카고 지역 시간대 : 
	 <fmt:timeZone value="America/Chicago">
	 
	 	<fmt:formatDate value="${today}" type="both" dateStyle="full" timeStyle="full" />
	 
	 </fmt:timeZone>

	 <br>	 
	 
	
	유럽 런던 현재 시간대 : 
	 <fmt:timeZone value="Europe/London">
	 
	 	<fmt:formatDate value="${today}" type="both" dateStyle="full" timeStyle="full" />
	 
	 </fmt:timeZone>	

	<br>	
	
	<%--
			지역기반 ID
			
			 대륙/도시
			 
			 Asia/Seoul         (한국)
			 Asia/Tokyo         (일본)
			 America/New_York  (미국 뉴욕)
			 Europe/London     (영국 런던)
			 Australia/Sydeny  (호주 시드니)
			 .....
			 .....

	 --%>

</body>
</html>






















