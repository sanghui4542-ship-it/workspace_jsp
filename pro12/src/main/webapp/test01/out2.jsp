<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<%
		//1. 요청한 한글 문자 데이터 인코딩 방식 UTF-8 설정
		request.setCharacterEncoding("UTF-8");
	
		//2. 요청한 데이터들 request 내장객체 메모리 영역에서 얻기
		String name = request.getParameter("name");
		String age  = request.getParameter("age");
		
		//3. 조건에 따라 웹브라우저에 응답할 디자인 코드를 생성 후 응답(출력)
		//조건1. 이름을 입력하고 요청했다면?
		if(name != null || name.length() != 0){
	%>
			<h1><%=name%>, <%=age%></h1>   
	<%		
		}else{//조건2. 이름을 입력하지 않고 out2.jsp를 요청 했다면?
	%>		
			<h1>이름을 입력하고 오세요</h1>
	<%			
		}
	%>

	<%-- ----------------------------------------------------------------------------- --%>	
	<%
	//out 내장 객체( 요청한 웹브라우저와 연결된 출력스트림 JspWriter )
	
		//3. 조건에 따라 웹브라우저에 응답할 디자인 코드를 생성 후 응답(출력)
		//조건1. 이름을 입력하고 요청했다면?
		if(name != null || name.length() != 0){
			
			out.println("<h1>" + name + "," + age + "</h1>");
			
		}else{//조건2. 이름을 입력하지 않고 out2.jsp를 요청 했다면?
			
			out.println("<h1>이름을 입력하고 오세요</h1>");
		}
	
	%>

</body>
</html>








