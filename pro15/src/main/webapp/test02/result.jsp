<%@page import="java.net.URLEncoder"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%

	//1. first.jsp에서 result.jsp로 전송된 다운로드할 파일명이 한글문자 이면 깨져서 얻어와 지기때문에 한글처리
	request.setCharacterEncoding("UTF-8");

	//2. first.jsp에서 <form>에 의해 result.jsp를 요청한 전체 URL중에서 
	//	 컨텍스트 주소 경로 "/pro15" 얻어 저장
	String contextPath = request.getContextPath();

	//3. first.jsp의 <form>태그 내부에 작성한 <input type="hidden"> 두쌍의 value속성에 설정한 다운로드할 파일명 얻어
	//   한글문자가 포함되어 있으면? 파일명의 한글이 깨져서 다운로드 되는 것을 방지 하기 위해
	//   파일명을 얻어 한글 문자 인코딩 UTF-8 설정해서 파일명 저장
	String file1 = URLEncoder.encode(request.getParameter("param1")); //"eclipse.exe"
	String file2 = URLEncoder.encode(request.getParameter("param2")); //"lombak.jar"
	
	//4. 첫번째 , 두번째 다운로드할 파일을 FileDownload.java 서블릿으로 다운로드 요청할 <a>링크 만들기
%>
	파일 내려받기1 : <a href="<%=contextPath%>/download.do?fileName=<%=file1%>">파일 다운로드 요청1</a>
	<br><br>
	파일 내려받기2 : <a href="<%=contextPath%>/download.do?fileName=<%=file2%>">파일 다운로드 요청2</a>
	<br><br>	











