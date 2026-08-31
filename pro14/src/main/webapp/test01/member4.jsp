<%@page import="sec01.ex01.MemberVO"%>
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
	                                                               
	<jsp:useBean  id="membersList" class="java.util.ArrayList" />                                                               
<%

	//2번쨰 MemberVO클래스의 객체 생성 
	MemberVO  vo2 = new MemberVO("son","1234","손흥민","son@test.com");

	//위 두개의 MemberVO 객체들을  ArrayList 배열에 추가 해서 저장
	membersList.add(vo);     membersList.add(vo2);

	/*
		 ArrayList membersList 배열 모습
		
		[  MemberVO첫번째객체  ,  MemberVO두번째객체   ]  
				0                 1                index		
	*/
%>	                                                           	                                                               	                                                                 
	                                                                                                                                                                    
 <table width="100%" align="center">
 	<tr align="center" bgcolor="pink">
 		<td width="7%">아이디</td>
 		<td width="7%">비밀번호</td>
 		<td width="7%">이름</td>
 		<td width="7%">이메일</td>
 	</tr>	  
<!-- 
	ArrayList membersList 배열 모습
		
		[  MemberVO첫번째객체  ,  MemberVO두번째객체   ]  
				0                 1                index	

	위 ArrayList 배열의 첫번째 칸(0 index위치 칸)에 저장되어 있는 MemberVO객체의 각변수값들을 최종 얻어 EL태그로 출력 
	
		작성방법 ->  ${컬렉션가변배열[index].꺼내온MemberVO객체의변수명 }
 -->
	<tr align="center">
		<td>${membersList[0].id}</td> <%-- <td>${MemberVO첫번쨰객체.id}</td> --%>	
		<td>${membersList[0].pwd}</td>
		<td>${membersList[0].name}</td>
		<td>${membersList[0].email}</td>
	</tr>
	<tr align="center">
		<td>${membersList[1].id}</td> <%-- <td>${MemberVO두번쨰객체.id}</td> --%>	
		<td>${membersList[1].pwd}</td>
		<td>${membersList[1].name}</td>
		<td>${membersList[1].email}</td>
	</tr>


	<tr height="1" bgcolor="pink">
		<td colspan="5"></td>
	</tr>	

 </table>	                                                   
	                               
	              
	              
	              
	              
	              
	              
	              
	              
	              
	              
	                               
	                               
	                               
	                               
	                               
	                                                               