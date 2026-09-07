<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%--
================================================================================
 파일명 : FetchAjax/calc.jsp
 역할   : 1.html 이 보낸 입력값 2개를 더해서 "숫자 하나만" 응답하는 서버 페이지
================================================================================

 참고. 위 page 지시자 한 줄의 의미

   language="java"                     이 문서 안의 코드는 자바로 작성한다
   contentType="text/html; charset=UTF-8"  브라우저에게 응답 형식과 문자셋을 알림
   pageEncoding="UTF-8"                이 jsp 파일 자체를 저장한 문자셋

   contentType 을 지정하지 않으면 응답한 한글이 브라우저에서 깨집니다.

 참고. 일반 JSP 와 Ajax 용 JSP 의 결정적 차이

   [일반 JSP]  <html> <body> <table> ... 처럼 HTML 문서 전체를 응답합니다.
               브라우저가 그 문서로 화면 전체를 새로 그립니다.

   [Ajax JSP]  HTML 태그가 하나도 없습니다.
               값 하나 또는 JSON 데이터만 응답합니다.
               자바스크립트가 그 값을 받아 화면의 일부분에만 끼워 넣습니다.

 참고. 이 파일이 호출되는 실제 주소

   http://localhost:8181/pro16_Ajax7/FetchAjax/calc.jsp?v1=10&v2=20
                                                       ------------
                                                       쿼리 스트링
--%>
<%
	//1. 요청 데이터의 한글 문자 인코딩 방식을 UTF-8로 설정
	request.setCharacterEncoding("UTF-8");

	//2. 요청화면(1.html)이 보낸 값 2개 꺼내기
	int v1 = Integer.parseInt(request.getParameter("v1"));
	int v2 = Integer.parseInt(request.getParameter("v2"));
	
	//3. + 한 계산 결과만 응답
	//========================================================================
	//   out 은 응답 내용을 브라우저로 내보내는 내장 객체입니다.
	//   out.print(30) 을 하면 브라우저는 딱 "30" 이라는 글자만 받습니다.
	//
	//   이 값이 1.html 의  const data = await res.text();  의 data 로 들어갑니다.
	//
	//   참고. HTML 태그가 전혀 없는 값 하나만 응답합니다.
	//         이것이 비동기 통신(Ajax)의 핵심입니다.
	out.print(v1 + v2);
%>













