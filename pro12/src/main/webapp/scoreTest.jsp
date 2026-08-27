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
		//순서1. 요청한 데이터 한글문자 인코딩 UTF-8설정
		request.setCharacterEncoding("UTF-8");
		
		//순서2. 요청한 데이터(입력한 시험점수)를 HttpServletRequest 객체 메모리 내부에서 얻어 int score변수에 저장
		int score =  Integer.parseInt(request.getParameter("score"));   
		//  59
	%>

	<%-- 순서3. 요청한 데이터(입력한 시험점수)를 이용해 조건문 if  else if  else에 따라 응답할 데이터 생성후 브라우저로 응답! --%>
	
	<h1>입력한 시험점수 : <%=score%> </h1>

	<%  if(score >= 90){ //조건1. 입력한 시험점수가 90점 이상이면? %>
			
			<h1>A학점 입니다.</h1>	 
						
	<%	}else if(score >= 80  &&  score < 90){ //조건2. 입력한 시험점수가 80 ~ 90 사이라면?(80은 포함 90은 미포함) %>
			
			<h1>B학점 입니다.</h1>
			
	<%	}else if(score >= 70 && score < 80){//조건3. 입력한 시험점수가 70 ~ 80 사이라면?(70은 포함 80은 미포함) %>
			
			<h1>C학점 입니다.</h1>
		
	<%  }else if(score >= 60 && score < 70){ //조건4. 입력한 시험점수가 60 ~ 70 사이라면?(60은 포함 70은 미포함) %>
		
			<h1>D학점 입니다.</h1>
			
	<%	}else{ //조건5. 그외 점수를 입력 받았다면? (조건1.조건2.조건3.조건4 의 조건식이 모두 거짓이라면?) %>
			
			<h1>F학점 입니다.</h1>			
	<%	}  %>

	<br> <a href="./scoreTest.html">다른 시험점수 다시 입력하러 가기</a>

</body>
</html>










