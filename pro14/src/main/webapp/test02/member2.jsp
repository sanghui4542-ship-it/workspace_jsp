<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
/* member2.jsp :  forward2.jsp로 부터 포워딩(재요청) 당한  request를 공유 받아 사용하는 페이지  */

	/*1. 요청한 한글문자 인코딩 방식 UTF-8 설정*/
	request.setCharacterEncoding("UTF-8");
%>
<table width="100%" align="center">
	<tr align="center" bgcolor="pink">
		<td width="7%">아이디</td>
		<td width="7%">비밀번호</td>
		<td width="7%">이름</td>
		<td width="7%">이메일</td>
	</tr>
<%-- 2. 대스패처 방식으로 포워딩(재요청) 당한  member2.jsp에서는 request 객체를 공유 받아서 바인딩한 자원을 EL로 얻어 출력할수 있다. --%>		

<%-- 참고. 공유 request 내장객체에 접근하기 위해 EL에서 제공하는 requestScope 내장객체로 접근해서 사용한다. 

								 key        value
		  request.setAttribute("member", memberVO); <---- forward2.jsp 에서 바인딩 했던 MemberVO객체 하나 	
		  
		  작성방법
		  		  ${requestScope.key.값이_저장된_변수명}		  
--%>	
	<tr align="center">
		<td width="7%">${requestScope.member.id}</td>
		<td width="7%">${requestScope.member.pwd}</td>
		<td width="7%">${             member.name}</td>
		<td width="7%">${             member.email}</td>			
	</tr>
	
	
	
</table>	
	
	
	
	
	
	
	
	
	