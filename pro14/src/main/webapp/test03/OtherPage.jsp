<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<h4>/test03/OtherPage.jsp</h4>

<ul>
	<li>
		redirectTest.jsp를 클라이언트가 최초 요청했을때 톰캣에 의해 생성된 request 내장객체 영역에 바인딩된 requestVar변수의 값:
		${requestScope.reqeustVar} <br> <%-- 출력 되지 않을 것임!  null이기 때문에 EL태그에 의해 빈공백으로 변환되어 출력됨 --%>
	</li>
	<li>c:redirect 태그로 재요청시 전달 받은 값 1: ${param.user_param1}</li>
	<li>c:redirect 태그로 재요청시 전달 받은 값 2: ${param.user_param2}</li>

</ul>