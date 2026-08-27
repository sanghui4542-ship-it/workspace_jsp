<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%
/*
session1.jsp 페이지

- SessionTest 서블릿 클래스에서 보여준 <a>태그를 클릭하여 요청받는 첫번째 서버페이지 session1.jsp
  요청받은 session1.jsp는 요청한 클라이언트의 브라우저 창의 종류가 같기 때문에 
  session(HttpSession)내장 객체 메모리를 공유받아 사용할수 있다.

  
참고. 확장자가 .jsp인  session1.jsp서버페이지에서 공유 받은 HttpSession객체 메모리를 얻을떄
   SessionTest 서블릿 클래스 내부 코드에서는  request.getSession(); 메소드를 호출하여 얻었지만
   session1.jsp 서버페이지 내부에 코드를 작성할떄는  session참조변수명을 사용하여 HttpSession객체 메모리에 바로 얻어 사용할수 있다.
*/

//1. SessionTest 서블릿으로 부터 공유 받은 session(HttpSession)내장 객체 메모리에 접근해서
//	 바인딩 (session.setAttribute("name","이순신"); ) 했던 "이순신" 값 꺼내와 얻자

//방법 :               session.getAttribute("name") => "이순신"
String name = (String)session.getAttribute("name");

//2. 추가로  session1.jsp 서버페이지 내부에서 SessionTest 서블릿으로 부터 공유 받은 session 내장객체에 바인딩 하자
session.setAttribute("address", "서울시 강남구");

//3. 표현식 태그를 이용해 String name변수에 저장된 "이순신"을 브라우저로 응답(출력)
%>
	이름은 <%=name%> 입니다.  이 이름은 SessionTest 서블릿으로 부터 공유받은 HttpSession에 바인딩 했던 공유 값입니다.<br>
	<a href="session2.jsp">두번째 session2.jsp 요청하여 보여주기 => Httpsession 공유 가능함 </a>
	 






