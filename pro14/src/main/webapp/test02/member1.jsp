<%@page import="java.util.ArrayList"%>
<%@page import="java.util.HashMap"%>
<%@page import="sec01.ex01.MemberVO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%
	/* 순서1. 요청 한 한글 문자 인코딩 방식 UTF-8 설정*/
	request.setCharacterEncoding("UTF-8");
%>

	<table width="100%" align="center">
		<tr align="center" bgcolor="pink">
			<td width="7%">아이디</td>
			<td width="7%">비밀번호</td>
			<td width="7%">이름</td>
			<td width="7%">이메일</td>
		</tr>
<%-- 순서2. 표현식 태그와 자바코드를 사용해서 각 내장객체 메모리 영역에 바인딩 했었던 value를 얻어 출력 --%>		
		<tr align="center">
			<td width="7%"><%= (String)request.getAttribute("id") %></td>
			<td width="7%"><%= (String)request.getAttribute("pwd") %></td>
			<td width="7%"><%= (String)session.getAttribute("name") %></td>
			<td width="7%"><%= (String)application.getAttribute("email") %></td>			
		</tr>
<%-- 순서3. 표현식 태그와 자바코드 없이 ~~ 
		   EL태그와 EL태그에서 제공해주는 각 내장객체메모리에  접근하는 단어들을 이용해 
		   각 내장객체 메모리 영역에 바인딩 했었던 value를 얻어 출력 
 --%>		
		<tr align="center">
<%-- 		<td width="7%">${requestScope.꺼내올 값과 함께 묶어 바인딩한 키}</td> --%>
			<td width="7%">${requestScope.id}</td>
			<td width="7%">${requestScope.pwd}</td>
			
<%-- 		<td width="7%">${sessionScope.꺼내올 값과 함께 묶어 바인딩한 키}</td> --%>			
			<td width="7%">${sessionScope.name}</td>
						
<%-- 		<td width="7%">${applicationScope.키}</td>			 --%>
			<td width="7%">${applicationScope.email}</td>			
		</tr> 

		
	</table>











