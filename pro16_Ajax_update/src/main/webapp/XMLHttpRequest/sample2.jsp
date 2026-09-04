<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
	//sample2.jsp 페이지
	//- 5.html로 부터 비동기 통신으로 요청 받은 서버페이지 
	//- 5.html로 부터 요청받은 URL ->  sample2.jsp?userid=홍길동&passwd=json
	
	//1. 요청한 한글 문자 request내장객체 메모리에 인코딩 방식 UTF-8설정
	request.setCharacterEncoding("UTF-8");
	
	//2. 요청한 파라미터(데이터)들 얻기
	String userid = request.getParameter("userid"); //"홍길동"
	String passwd = request.getParameter("passwd"); //"json"

	//3. 응답할 데이터를 직접  JSONObject 형태의 문자열로 만들어서 변수에 저장 
	String jsonData = "{'userid':'"+ userid +"', 'passwd':'" + passwd + "'}";
		
//	String jsonData = "{\"userid\":\"" + userid + "\", \"passwd\":\"" + passwd + "\"}";

	//Java에서 문자열 내에 큰따옴표(")를 그대로 사용할 수 없기 때문에, 
	//큰따옴표를 문자열 안에 포함하려면 이스케이프 문자 \를 붙여줘야 합니다.
	
	//JSON 문자열에서 큰따옴표를 추가하려면 \"로 작성하여 Java 컴파일러가 큰따옴표를 문자열의 일부로 인식하게 해야 합니다.
%>
	<%-- 4. 표현식 <%= %> 이용하여  응답 --%>
	<%=jsonData%>












