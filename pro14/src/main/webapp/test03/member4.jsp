<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%--  JSTL의  core 라이브러리 태그들을 사용하기 위해 외부 사이트에서 불러오는 taglib 디렉티브 태그 한줄 작성 --%>    
<%@ taglib  uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>    
    
<%
	//member4.jsp 요청한 한 글문자 인코딩 방식 UTF-8 설정
	request.setCharacterEncoding("UTF-8");
%>     
 <%--
	 <c:if>태그
	 
	 -    자바의 if문과 동일하게 제어 구문을 작성할떄 사용합니다. 
	      하지만  else가 별도로 없기때문에 일련의 조건을 나열하는 형태로 작성하기에는
	      어려움이 있습니다.
	      
	 - 문법
	 	<c:if test="${조건식}"   var="조건식의 결과 저장시 만들 변수명" scope="변수가 저장될 내장객체영역중 하나">
	 	
	 		test속성의 조건식이 참이면 출력될 코드 문장  
	 	
	 	</c:if> 
 --%>	

<c:set var="id"  value="hong" scope="page"/>  <%--  String id = "hong"   pageContext.setAttribute("id","hong"); --%>
<c:set var="pwd" value="1234" scope="page"/>    
<c:set var="name" value="${'홍길동'}" scope="page" />    
<c:set var="age"  value="${22}" scope="page" />
<c:set var="height" value="${177}" scope="page"/>    
   
<%-- 위 page 내장객체 영역에 바인딩된 height 변수의 값이 160보다 크면? --%>   
<c:if test="${pageScope.height > 160}">

	<h1>${pageScope.name}의 키는 160보다 큽니다.</h1>

</c:if>   

<%-- 위 page 내장객체 영역에 바인딩된 age 변수의 값이 22숫자와 같으면?
	 var 속성에 설정한 result 변수에 true 저장 하고
	 scope 속성에 설정한 page 내장객체에 result 변수를 바인딩 --%>
<c:if test="${pageScope.age == 22}"  var="result" scope="page">
	
	<%-- age 변수 값이 22와 같으면? 실행될 코드 작성 --%>
	
	<h1>${pageScope.name}의 나이는 ${pageScope.age}살 입니다.</h1>
	
	${pageScope.result}
	
	<c:if test="${pageScope.result}">
	
		<h1>그래 맞다~~</h1>
	
	</c:if>
	
</c:if>
	 
<%-- 위 page내장객체 영역에 바인딩된 id 변수의 값이 'hong'문자열과 같으냐? 그리고 name 변수의값이 '홍길동' 과 같으면?  --%>
<c:if test="${ (pageScope.id == 'hong') &&  (pageScope.name == '홍길동') }">	
	
	 <h1>아이디는 ${pageScope.id} 이고, 이름은 ${pageScope.name} 입니다 </h1> 
	 
</c:if>	 
	 
	 
<%-- 조건식을 작성하는 자리에  true 라는 참을 의미하는 값을 넣어서 항상 참을 만들수 있습니다. --%>
<c:if test="${true}">
	<h1>항상 참입니다.</h1>
</c:if>

<c:if test="${11 eq 11}">
	<h1>두 11은 같습니다.</h1>
</c:if>

<c:if test="${11 != 31}">
	<h1>두 값은 같지 않습니다.</h1>
</c:if>

    