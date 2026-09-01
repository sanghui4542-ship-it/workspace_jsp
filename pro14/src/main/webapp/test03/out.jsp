<%@page import="sec01.ex01.MemberVO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%-- JSTL의 core 라이브러리 태그들을 사용하기 위해 외부 사이트에서 불러오는 taglib 디렉티브 태그 한줄 작성 --%>    
<%@ taglib  uri="http://java.sun.com/jsp/jstl/core"  prefix="c" %>   

<% request.setCharacterEncoding("UTF-8");  %>           
    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<%--
		<c:out> 태그는 out.println() 과  <%= %>표현식 태그 대신 제공하는 JSTL의 태그!
		그리고 EL ${}를 사용하여 계산식도 함께 작성할수 있다.
	 --%>
	 <c:out  value="안녕하세요"  />  <br>
	 <c:out  value="${2*3}"/>     <br>
	 
<%--
	requestScope.membervo.id  은  
	null 을 가져와  예외가 발생하게 되어 출력이 안되지만	
	실제 아래 구문은 예외처리를 자동으로 해주어  빈공백을 출력해 버립니다. 
	빈공백을 default 속성에 적은 기본값으로 출력할수도 있습니다.
 --%>	 
	 <c:out  value="${requestScope.membervo.id}" default="빈공백 대신 출력할 기본값 설정"  />

	<hr>
	<hr>
	
	<%-- <abc> 는 html태그로 인식하여 그대로 화면에 출력되지 않습니다. --%>
	<abc>는 abc입니다.<br>
	
	<%-- <abc>를 그대로 브라우저에 출력되게 하려면?  
		 EL ${}의 문법중에서 &lt; 엔티티로  < 기호를 나타내어 브라우저에 출력되도록 할수 있습니다. 
		 그러나 &lt; 인티티를 작성해야 한다는 점이 매우 불편합니다.
	--%>
	&lt;abc>는 abc입니다.<br>
   
    <%-- 위 코드들의 단점을 보완 하기 위해 JSTL 의  c:out 태그를 이용하여 쉽게 이스케이프 문자들을 브라우저 화면에 출력할수 있습니다. --%>
	<c:out value="<abc>는 abc입니다."  />

</body>





</html>


