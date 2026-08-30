<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         
         isErrorPage="true"
         %>
		  <%--
			다른 jsp(add.jsp)에서 예외 발생시
			예외를 처리하는 예외처리 서버페이지로 톰캣에게 알려주는 isErrorPage속성으로 true값 설정
		 --%>    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
		<!-- 예외 처리 코드 아래와 같이 작성 해 놓고 브라우저 화면에 500 에러 대신 보여준다. -->
		<h3>
			 자연수 숫자만 입력 가능합니다. 다시 입력해 주세요. 아래 링크 클릭하세요.
			 <a href="add.html">다시 입력하러가기</a>	 
		</h3>

		<%-- add.jsp페이지에서 발생한 예외 메세지를 exception 내장객체에서 얻어 출력 --%>
		
		================ exception 내장객체의 toString() 메소드 호출한 예외 메세지 출력 내용 ======== <br>
		<h1><%= exception.toString() %></h1>

		================ exception 내장객체의 getMessage() 메소드 호출한 예외 메세지 출력 내용 ====== <br>
		<h1><%= exception.getMessage()  %></h1>

		<%--
			printStackTrace() 는 브라우저 화면이 아니라 "톰캣 서버 내부에"에 출력하는 메소드다.
			브라우저에는 아무것도 안 나오고, 이클립스 Console 창에 찍힌다.
			그래서 <h1> 태그로 감싸도 화면에는 빈 제목만 보인다.
		--%>
		================== exception 내장객체의 printStackTrace() 메소드를 호출하여 발생한 예외 메세지 출력 내용 ===== <br>
		<%   exception.printStackTrace();     %>
		


</body>
</html>


















