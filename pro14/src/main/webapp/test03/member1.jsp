<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%--  JSTL의  core 라이브러리 태그들을 사용하기 위해 외부 사이트에서 불러오는 taglib 디렉티브 태그 한줄 작성 --%>    
<%@ taglib  uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>    
    
<%
	//순서1. member1.jsp 요청한 한 글문자 인코딩 방식 UTF-8 설정
	request.setCharacterEncoding("UTF-8");
%>    
<%--
		JSTL의 core 라이브러리에 속한 태그 중에서  c:set 태그 ?
		- 변수 선언 하는 태그 
		- c:set 태그 작성 방법
			
			<c:set  var="선언할_변수명_작성"  
			        value="변수에_저장할_값"  
			        scope="선언한_변수를_바인딩할_내장객체 종류중 하나"/>
 --%>
<%-- id변수 선언 후 "hong" 저장 하고,  id 변수를 page 내장객체 영역에 바인딩 --%>
<c:set  var="id" value="hong"  scope="page"/>
<c:set  var="pwd" value="1234" scope="page"/>
<c:set  var="name" value="${'홍길동'}"  scope="page"/>
<c:set  var="age"  value="${22}"  scope="page"/>
<c:set  var="height" value="${177}" scope="page"/>

	<table width="100%" align="center">
		<tr align="center" bgcolor="pink">
			<td width="7%">아이디</td>
			<td width="7%">비밀번호</td>
			<td width="7%">이름</td>
			<td width="7%">나이</td>
			<td width="7%">키</td>
		</tr>
	<%-- EL ${} 태그를 작성해  page내장객체 영역에 바인딩된 변수의 값을 얻어 출력 --%>
		<tr align="center">
			<td>${pageScope.id}</td>
			<td>${pwd}</td>
			<td>${name}</td>
			<td>${age}</td>
			<td>${height}</td>			
		</tr>
	</table>











