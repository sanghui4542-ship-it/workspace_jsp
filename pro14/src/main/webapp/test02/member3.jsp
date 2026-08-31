<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
//member3.jsp 역할 : forward3.jsp로 부터 포워딩(재요청) 당해 request를 공유 받아 사용하는 두번쨰 서버페이지

	/*재요청한 한글 문자 인코딩 UTF-8설정 */
	request.setCharacterEncoding("UTF-8");
%>
<table width="100%" align="center">
	<tr align="center" bgcolor="pink">
		<td width="7%">아이디</td>
		<td width="7%">비밀번호</td>
		<td width="7%">이름</td>
		<td width="7%">이메일</td>
	</tr>
<%-- 순서1. EL ${} 태그에서 request에 바인딩된 ArrayList배열 얻고, 
	 순서2. ArrayList배열의 0 index위치 칸에 저장된 첫번째 MemberVO객체주소 하나 얻습니다. 
	 순서3. 첫번째 MemberVO객체의 각 인스턴스 변수에 저장된 회원정보를 얻어 EL로 출력 
	 
	 	 request        -> ArrayList 얻기          -> MemberVO객체 얻기           -> MemberVO객체의 각 인스턴스변수 값 얻어   EL로 출력! 
	 	 requestScope      requestScope.list         requestScope.list[0]           ${requestScope.list[0].id}
	 --%>
	<tr align="center">
		<td width="7%">${requestScope.list[0].id}</td>
		<td width="7%">${             list[0].pwd}</td>
		<td width="7%">${             list[0].name}</td>
		<td width="7%">${requestScope.list[0].email}</td>   <%-- getEmail() 메소드 호출한 email 인스턴스변수값 반환받아 출력 --%>
	</tr>	 
	 	
<%-- 순서1. EL ${} 태그에서 request에 바인딩된 ArrayList배열 얻고, 
	 순서2. ArrayList배열의 1 index위치 칸에 저장된 첫번째 MemberVO객체주소 하나 얻습니다. 
	 순서3. 첫번째 MemberVO객체의 각 인스턴스 변수에 저장된 회원정보를 얻어 EL로 출력 
	 
	 	 request        -> ArrayList 얻기          -> MemberVO객체 얻기           -> MemberVO객체의 각 인스턴스변수 값 얻어   EL로 출력! 
	 	 requestScope      requestScope.list         requestScope.list[1]           ${requestScope.list[1].id}
	 --%>
	<tr align="center">
		<td width="7%">${requestScope.list[1].id}</td>
		<td width="7%">${             list[1].pwd}</td>
		<td width="7%">${             list[1].name}</td>
		<td width="7%">${requestScope.list[1].email}</td>   <%-- getEmail() 메소드 호출한 email 인스턴스변수값 반환받아 출력 --%>
	</tr>	 	
	
</table>	








