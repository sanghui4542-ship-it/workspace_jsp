<%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%--
================================================================================
 파일명 : FetchAjax/slow.jsp
 역할   : 일부러 느리게 응답하는 실습용 서버 페이지 (Promise.all 비교용)
================================================================================

 참고. 이 페이지가 필요한 이유

   순차 실행과 동시 실행의 속도 차이를 눈으로 확인하려면
   서버가 일부러 시간을 끌어 줘야 합니다.

   요청할 때 name 과 sec 값을 함께 보내면
   그 초만큼 늦게 응답합니다.

     slow.jsp?name=회원정보&sec=1   ->  1초 뒤에 응답
     slow.jsp?name=게시글&sec=2     ->  2초 뒤에 응답
--%>
<%
	//1. 요청 데이터의 한글 인코딩 설정 (name 에 한글이 오므로 반드시 필요)
	request.setCharacterEncoding("UTF-8");

	//2. 어떤 요청 데이터인지 구분할 이름 얻기
	String name = request.getParameter("name"); //회원정보   게시글  등 
	
	//3. 몇 초를 기다릴지 얻기
	String secStr = request.getParameter("sec"); // 1     2   등
	int sec =   (secStr == null) ? 1  : Integer.parseInt(secStr);
	
	//4. 지정한 초만큼 톰캣서버 일부러 대기 
	Thread.sleep(sec * 1000L);
	
	//5. JSON 으로 응답 
	//   name 은 글자이므로 큰따옴표로, sec 은 숫자이므로 따옴표 없이 씁니다.
%>
{"name":"<%=name%>",  "sec":<%=sec%>}














