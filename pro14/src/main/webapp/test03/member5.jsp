<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%--  JSTL의  core 라이브러리 태그들을 사용하기 위해 외부 사이트에서 불러오는 taglib 디렉티브 태그 한줄 작성 --%>    
<%@ taglib  uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>    
    
<%
	//순서1. member5.jsp 요청한 한 글문자 인코딩 방식 UTF-8 설정
	request.setCharacterEncoding("UTF-8");
%>    

<%-- id변수 선언 후 "hong" 저장 하고,  id 변수를 page 내장객체 영역에 바인딩 --%>
<c:set  var="id" value="hong"  scope="page"/>
<c:set  var="pwd" value="1234" scope="page"/>
<c:set  var="name" value="${'홍길동'}"  scope="page"/> 
<c:set  var="age"  value="${22}"  scope="request"/>
<c:set  var="height" value="${177}" scope="page"/>    
       
 	<table width="100%" align="center">
		<tr align="center" bgcolor="pink">
			<td width="7%">아이디</td>
			<td width="7%">비밀번호</td>
			<td width="7%">이름</td>
			<td width="7%">나이</td>
			<td width="7%">키</td>
		</tr>
<c:choose>

<%-- page 내장객체 영역에 name변수가 저장되어 있지 않느냐? --%>
<%--<c:when test="${pageScope.name == null}"> --%>
	<c:when test="${empty pageScope.name}">
		<tr align="center">
			<td colspan="5">이름이 저장되어 있지 않습니다.</td>
		</tr>	
	</c:when>	
	<c:when test="${empty pageScope.pwd}">
		<tr align="center">
			<td colspan="5">비밀번호가 저장되어 있지 않습니다.</td>
		</tr>		
	</c:when>	
	<c:when test="${empty age}">
		<tr align="center">
			<td colspan="5">나이가 저장되어 있지 않습니다.</td>
		</tr>		
	</c:when>
	<c:when test="${empty height}">
		<tr align="center">
			<td colspan="5">키가 저장되어 있지 않습니다.</td>
		</tr>		
	</c:when>		
	<%-- page 내장객체 영역에 name, pwd, age, height 변수와 값을 한쌍의 형태로 묶어 저장(바인딩) 되어 있으면? --%>	
	<c:otherwise>
		<tr align="center">
			<td>${pageScope.id}</td>
			<td>${pwd}</td>
			<td>${name}</td>
			<td>${age}</td>
			<td>${height}</td>
		</tr>
	</c:otherwise>	
</c:choose>				
	</table>	   
    
    
<%--
	  <c:choose>,  <c:when>,  <c:otherwise> 태그
	  
	  - <c:choose> 태그는 다중 조건식을 통해 판단해야 할때 사용되는 태그 입니다.
	     하위 태그로 <c:when>태그, <c:otherwise>태그를 함께 사용합니다.
	     
	  - 작성문법
	  			<c:choose>
	  				<c:when test="${조건식1}"> 조건식1의 결과가 참이면 실행될 코드 </c:when>      if(조건식1) { .....
	  				<c:when test="${조건식2}"> 조건식2의 결과가 참이면 실행될 코드 </c:when>      } else if(조건식2) { ......
					<c:otherwise>모든 조건식의 결과가 거짓이면 실행될 코드</c:otherwise>          } else { ...... }
				</c:choose>

				설명 :  얼핏 보면 자바의 switch  case  default 비슷하지만
					   각각의 <c:when>에서 조건식을 비교한다는 점은    if   else if   else 와 같은 구조 입니다.
 --%>    
    
    
    
    
    
    
    
    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>

</body>
</html>