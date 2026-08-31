<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
//흐름  :  memberForm.html 입력한 가입정보들   저장-> 
//                   MemberVO객체의 변수에 저장  ->  
//                   MembeVO객체의 변수 정보를 EL, 표현식을 사용해서 각각 얻어 출력
	

	//순서1. 입력하여 요청한 데이터들 한글처리
	request.setCharacterEncoding("UTF-8");
%>
	<%--순서2. 순서3. memberForm.html에 입력한 요청한 데이터들을 모두 request 내장객체에서 얻어 
	                MemberVO 객체 생성 후 각 변수에 저장 (액션태그 이용) --%>
	<jsp:useBean  id="vo" class="sec01.ex01.MemberVO" /> <%--  <- MemberVO객체 생성후 참조변수 vo에 저장  --%>
	<jsp:setProperty  name="vo" property="*"/>           <%--   <- 생성한 MemberVO객체의 모든 setter메소드 호출해 
	                                                               입력한 정보들 모든 변수에 저장 --%>	                                                               
 <table width="100%" align="center">
 	<tr align="center" bgcolor="pink">
 		<td width="7%">아이디</td>
 		<td width="7%">비밀번호</td>
 		<td width="7%">이름</td>
 		<td width="7%">이메일</td>
 	</tr>	  
<%-- EL 태그 ${ } 로  MemberVO객체의 각변수에 저장된 입력한 요청 데이터들을 얻어 출력 --%>	
<%-- 작성 방법 => ${MemberVO객체.인스턴스변수명}   --%> 	          
 	<tr align="center">
 		<td>${vo.id}</td>
 		<td>${vo.pwd}</td>
 		<td>${vo.name}</td>
 		<td>${vo.email}</td>
 	</tr>
<%--
	표현식 태그와 자바 코드를 이용해 MemberVO객체의 각변수에 저장된 입력한 요청데이터들을 얻어 출력 
	작성 문법 => <%= MemberVO객체.getter메소드호출  %>
 --%>	 	
 	<tr align="center">
 		<td><%=vo.getId()%></td>
 		<td><%=vo.getPwd()%></td>
 		<td><%=vo.getName()%></td>
 		<td><%=vo.getEmail()%></td>
 	</tr>
 	
	<tr height="1" bgcolor="pink">
		<td colspan="5"></td>
	</tr>	

 </table>	                                                   
	                               
	              
	              
	              
	              
	              
	              
	              
	              
	              
	              
	                               
	                               
	                               
	                               
	                               
	                                                               