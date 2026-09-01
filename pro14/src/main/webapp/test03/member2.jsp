<%@page import="sec01.ex01.MemberVO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%--  JSTL의  core 라이브러리 태그들을 사용하기 위해 외부 사이트에서 불러오는 taglib 디렉티브 태그 한줄 작성 --%>    
<%@ taglib  uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>    
    
<%
	//순서1. member2.jsp 요청한 한 글문자 인코딩 방식 UTF-8 설정
	request.setCharacterEncoding("UTF-8");
%>       

<%-- 순서2.  HashMap , ArrayList 배열 생성 (액션태그 사용) --%>    
<jsp:useBean  id="membersMap"  class="java.util.HashMap"    scope="page"  />
<jsp:useBean  id="membersList" class="java.util.ArrayList"  scope="page"  />     
<%
	//순서3. ArrayList 배열에 MemberVO객체 2쌍을 생성해서 추가  
 	membersList.add(new MemberVO("ki", "4321", "기성용", "ki@test.com"));
	membersList.add(new MemberVO("son","1234", "손흥민", "son@test.com"));

 	//순서4. HashMap 배열에  key,value를 한쌍의 형태로 묶어서 박지성 정보를 바인딩(저장) 하자.
	membersMap.put("id", "park2");  //key, value
	membersMap.put("pwd", "4321");  //key, value
	membersMap.put("name", "박지성"); 
	membersMap.put("email", "park2@test.com");
	
	//순서5. HashMap 배열에 key,value를 한쌍의 형태로 묶어서  바로 위~~~ ArrayList 배열 자체를 바인딩 하자.
	membersMap.put("List", membersList);	
%>    
    
<%-- 순서6. c:set 태그를 사용해 HashMap에 바인딩된 ArrayList배열을 꺼내서 저장할 membersList변수를 만들고 ArrayList배열을 저장시키자 

		   참고. 아래 membersList 변수명으로 저장된 ArrayList배열 메모리 자체를 EL태그 태부에 작성해 사용할수 있다.
--%>   
<c:set  var="memberslist" value="${pageScope.membersMap.List}"/>    
    
	<table width="100%" align="center">
		<tr align="center" bgcolor="pink">
			<td width="7%">아이디</td>
			<td width="7%">비밀번호</td>
			<td width="7%">이름</td>
			<td width="7%">이메일</td>			
		</tr>
<%-- HashMap 배열에 저장된 박지성 대한 문자열 정보들을 EL태그로 얻어 출력 
	 작성 방법 :  먼저 page 내장객체에 접근하기 위해  EL태그 문법에서 제공하는  paceScope. 을 사용하고
	 		    그리고 HashMap 배열을 꺼내오기 위해  pageScope.membersMap 을 작성하고
	 		    마지막으로 박지성에 대한 문자열 값을 얻어오기 위해  pageScope.membersMap.키  작성해서 최종 얻어 각각 출력 --%>
		<tr align="center">
			<td>${pageScope.membersMap.id}</td>   <%-- "park2" --%>
			<td>${pageScope.membersMap.pwd}</td>  <%-- "4321" --%>
			<td>${          membersMap.name}</td> <%-- "박지성" --%>
			<td>${          membersMap.email}</td> <%-- "park2@test.com" --%>
		</tr>
<%-- membersMap.key -> membersMap.List 를 작성하면 key와 저장된 ArrayList배열을 HashMap에서 꺼내 옵니다.

	 그런데 우리는 위  c:set 태그로  memberslist변수를 선언하고  HashMap배열에서 꺼내온 ArrayList배열 자체 주소를 저장 해 놓았습니다.
	 
	 그러므로 아래 처럼 memberlist[index]를 작성 하면  ArrayList배열 내부의 index위치 칸에 저장된 MemberVO객체를 꺼내 올수 있습니다.
 --%>	
 	
<%-- 위 만들어 져 있는 ArrayList배열의 0 index 위치칸에 저장된 첫번째 MemberVO객체를 얻고, 
     얻은 MemberVO객체의 각 변수의 값을 얻어 EL태그로 출력 --%>
		<tr align="center">
			<td>${memberslist[0].id}</td> <%-- "ki" --%>	
			<td>${memberslist[0].pwd}</td><%--"4321" --%>	
			<td>${memberslist[0].name}</td><%--"기성용" --%>	
			<td>${memberslist[0].email}</td><%--"ki@test.com" --%>		
		</tr>
		
<%-- 위 만들어 져 있는 ArrayList배열의 1 index 위치칸에 저장된 두번째 MemberVO객체를 얻고, 
     얻은 MemberVO객체의 각 변수의 값을 얻어 EL태그로 출력 --%>
		<tr align="center">
			<td>${memberslist[1].id}</td> <%-- "son" --%>	
			<td>${memberslist[1].pwd}</td><%--"1234" --%>	
			<td>${memberslist[1].name}</td><%--"손흥민" --%>	
			<td>${memberslist[1].email}</td><%--"son@test.com" --%>		
		</tr>		
	
	</table>


















