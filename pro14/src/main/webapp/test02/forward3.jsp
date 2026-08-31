

<%@page import="sec01.ex01.MemberVO"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
//forward3.jsp : 클라이언트가 브라우저 주소창에 URL로 요청하는 첫 서버페이지

	//1. 요청한 한글문자 인코딩 UTF-8설정
	request.setCharacterEncoding("UTF-8");
	
	//2. ArrayList 배열 생성
	List membersList = new ArrayList();
	
	//3. ArrayList 배열에 각 칸에 순서대로  MemberVO클래스의 객체 2개 생성 후 추가 
	membersList.add(new MemberVO("lee","1234","이순신","lee@test.com"));
	membersList.add(new MemberVO("son","1234","손흥민","son@test.com"));
	
	//4. forward3.jsp를 처음 요청한 클라이언트의 정보가 저장된 request 내장객체에 ArrayList배열 바인딩
	request.setAttribute("list", membersList);
	//					  key  ,   value
	
	//5. member3.jsp 두번째 서버페이지로 디스패처방식으로 포워딩(재요청)시 request 공유 
%>
	<jsp:forward  page="member3.jsp"/>








