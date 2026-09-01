<%@page import="java.util.ArrayList"%>
<%@page import="sec01.ex01.MemberVO"%>
<%@page import="java.util.List"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
    
<%-- JSTL의 core 라이브러리 태그들을 사용하기 위해 외부 사이트에서 불러오는 taglib 디렉티브 태그 한줄 작성 --%>    
<%@ taglib  uri="http://java.sun.com/jsp/jstl/core"  prefix="c" %>    
    
<%
	//순서1. 요청한 한글문자 인코딩 방식 UTF-8 값 설정
	request.setCharacterEncoding("UTF-8");

	//순서2. ArrayList 배열 생성
	List<MemberVO>  membersList = new ArrayList<>();

	//순서3. MemberVO 객체 3개 생성
	MemberVO vo1 = new MemberVO("son","1234","손흥민","son@test.com");
	MemberVO vo2 = new MemberVO("ki","4321","기성용","ki@test.com");
	MemberVO vo3 = new MemberVO("park","1212","박지성","park@test.com");
	
	//순서4. 순서2.에서 생성한  ArrayList 배열의 각 칸에 순서3. 에서 생성한 MemberVO객체 3개 추가 
	membersList.add(vo1);    membersList.add(vo2);   membersList.add(vo3);
%>       
   	<table width="100%" align="center">
		<tr align="center" bgcolor="pink">
			<td width="7%">아이디</td>
			<td width="7%">비밀번호</td>
			<td width="7%">이름</td>
			<td width="7%">이메일</td>
		</tr>
<%-- 1. JSTL c:set 태그를 이용해 list 변수를 하나 만들고, 위 ArrayList 배열 주소번지 저장 --%>		
<c:set  var="list"   value="<%=membersList%>" scope="page"  />

<%--1.1. JSTL c:forEach 태그(향상 for 형태)를 이용해 list 변수에 저장된 ArrayList배열의 MemberVO객체들을 3번반복해서 얻어 정보 출력 --%>	
<c:forEach var="membervo" items="${list}"  >
		<tr align="center" bgcolor="yellow">
			<td width="7%">${membervo.id}</td>
			<td width="7%">${membervo.pwd}</td>
			<td width="7%">${membervo.name}</td>
			<td width="7%">${membervo.email}</td>
		</tr>		
</c:forEach>

<%--1.2. JSTL c:forEach 태그(일반 for 형태)를 이용해 list 변수에 저장된 ArrayList배열의 MemberVO객체들을 3번반복해서 얻어 정보 출력 --%>
<c:forEach var="i" begin="0" end="${list.size()-1}" step="1">
		<tr align="center" bgcolor="aqua">
			<td width="7%">${list[i].id}</td>
			<td width="7%">${list[i].pwd}</td>
			<td width="7%">${list[i].name}</td>
			<td width="7%">${list[i].email}</td>
		</tr>	
</c:forEach>

<%
	//3. 자바코드인 향상 for로 ArrayList배열을 반복 순회하면서 MemberVO객체 3개의 정보를 얻어 출력  
	for(MemberVO vos  : membersList){
%>		
		<tr align="center">
			<td width="7%"><%=vos.getId()%></td>
			<td width="7%"><%=vos.getPwd()%></td>
			<td width="7%"><%=vos.getName()%></td>
			<td width="7%"><%=vos.getEmail()%></td>
		</tr>	
<%		
	}
%>
    </table>
    
    
    
    
    
    
    
    
    
    
    
    
    
    
