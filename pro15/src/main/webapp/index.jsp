<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
	/*
	참고.
		시작 페이지로 접속하면 곧바로 목록 컨트롤러인 list.do 로 이동시킵니다.
	
		여기서 직접 목록을 그리지 않는 이유
		 -> MVC 패턴에서는 모든 요청이 반드시 컨트롤러(FileListServlet.java)를 먼저 거쳐야 하고
		    JSP는 화면을 그리는 일만 담당해야 하기 때문입니다.
	
		sendRedirect  : 브라우저에게 다른 주소로 다시 재요청(포워딩)하라고 지시하는 메소드
		                주소창이 실제로 list.do 로 바뀝니다.
	*/	
	response.sendRedirect("list.do");

%>