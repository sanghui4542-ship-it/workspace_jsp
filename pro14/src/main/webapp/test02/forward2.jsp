<%@page import="sec01.ex01.MemberVO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
//forward2.jsp

	 request.setCharacterEncoding("UTF-8");  //요청 한글 문자 인코딩 UTF-8처리
	
	 //sec01.ex01 패키지에 만들어 놓은 MemberVO클래스의 객체 생성
	 MemberVO memberVO = new MemberVO("lee", "1234", "이순신", "lee@test.com");
	 
	 //위 생성한 MemberVO객체 하나를 request 내장객체에 key,value 형태로 묶어 바인딩
	 request.setAttribute("member", memberVO);
	 					 // key     value 
%> 
<%-- member2.jsp 로  디스패처 방식으로 포워딩(재요청)시 request 공유   --%>
<jsp:forward  page="member2.jsp"/> 