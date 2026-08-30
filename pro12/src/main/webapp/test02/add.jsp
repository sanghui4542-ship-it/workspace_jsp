
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         
         errorPage="addException.jsp"
         %>
         <%-- errorPage 속성 = add.jsp에서 예외 발생시 예외를 처리할 jsp 페이지의 주소경로를 설정하는 속성 --%>
                
<%
	/*
		add.jsp -  1 부터 클라이언트가 입력한 자연수 까지의 모든 합을 구해서 요청한 브라우저로 응답하는 서버페이지

		순서1. add.html 화면에서 입력한 자연수(요청데이터)를 request 내장객체 메모리에서 꺼내오기(얻기) 
	*/
	int num = Integer.parseInt( request.getParameter("num")  );
//-------> 바로 위 코드에서 클라이언트가 자연수가 아닌 한글 또는 영문 문자열을 입력해서 요청해 온경우는
//		   Integer.parseInt메소드 코드실행에 의한 숫자로 변경 하지 못한다!!는 의미의 NumberFormatException 예외 발생 할수 있음

	//  순서2. 1부터 클라이언트가 입력한 자연수 값까지 누적된 합!!!!!!을 저장할 변수 선언
	int sum = 0;

	//  순서3. 1부터 클라이언트가 입력한 자연수 값까지 sum변수에 누적 해서 합 구하기
	for(int i=1;    i<=num;     i++){
		
		sum += i;
	}
	// 순서4. 요청한 클라이언트의 브라우저로 누적값을 응답(출력)
	// out.print("1부터 " + num + "까지의 합은" + sum + "입니다.");
%>
<h1>1부터 <%=num%>까지의 합은 <%=sum%>입니다.</h1>









