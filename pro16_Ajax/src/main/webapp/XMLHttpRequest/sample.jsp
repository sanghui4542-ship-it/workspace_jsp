<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
	//sample.jsp 페이지
	//- 4.html로 부터 비동기 통신으로 요청 받은 서버페이지 
	//- 4.html로 부터 요청받은 URL ->  sample.jsp?userid=홍길동&passwd=test
	
	//1. 요청한 한글 문자 request내장객체 메모리에 인코딩 방식 UTF-8설정
	request.setCharacterEncoding("UTF-8");
	
	//2. 요청한 파라미터(데이터)들 얻기
	String userid = request.getParameter("userid"); //"홍길동"
	String passwd = request.getParameter("passwd"); //"test"

	//3. 비동기 통신(Ajax 통신)으로 요청한 4.html 화면에  응답할데이터를 만들어 보냅니다.
	out.print(userid + "\t" + passwd);
/*	
	결과는 [파일수신]버튼을 클릭하면 4.html은 요청한 파라미터를 가지고 Ajax통신을 이용하여 톰캣서버가 실행하는 sample.jsp를 요청한다.
	확장자가 .jsp인 파일을 요청 했기 때문에 톰캣서버에는 동적인 데이터를 클라이언트로 반환하게 된다.
*/

%>






