<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
	//1. 요청한 한글 문자 request내장객체 메모리에 인코딩 방식 UTF-8설정
	request.setCharacterEncoding("UTF-8");
	
	//2. 요청한 파라미터(데이터)들 얻기
	String userid = request.getParameter("userid"); //"홍길동"
	String passwd = request.getParameter("passwd"); //"post"
	
	//3. 응답 메세지 생성해서 응답
	out.println(userid + "\t" + passwd);
%>













