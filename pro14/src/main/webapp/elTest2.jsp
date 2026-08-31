<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" isELIgnored="false"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<h2>여러가지 산술연산자 EL태그 내부에 작성해 보자</h2>
	<%--
		  EL태그 (표현언어) ---->  ${   }
	
		  EL태그 내부에는 여러가지 식을 작성해서  식의 결과가 브라우저로 출력 할수 있다!
		  ---> ${ 식 }
	 --%>
	 
	 ${10 + 10} <br>
	 
	 ${20 - 10} <br>
	 
	 ${10 * 10} <br>

	 <%--  / 나눗셈 연산자 기호 대신 EL 태그 내부에 div 나눗셈 연산자 사용 가능 --%>
	 ${10 / 10} <br>
	 ${10 div 10} <br>
	 
	 
	 <%-- % 나머지 연산자 기호 대힌 mod 나머지 연산자 EL태그 내부에 작성가능 --%>
	 ${100 % 9} <br>
	 ${100 mod 9} 
	 


</body>
</html>










