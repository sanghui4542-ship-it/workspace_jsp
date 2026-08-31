<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
//member4.jsp : foward4.jsp으로 부터 포워딩 당해 request내장객체를 공유 받아 사용하는 서버퍼에지 

    //순서1. 요청 한 한글 문자 인코딩 방식 UTF-8 설정
	request.setCharacterEncoding("UTF-8");

	//순서2. forward4.jsp에서 request내장객체 영역에 address 키와 함께 "서울시 강남구"값을 함께 묶어서 한쌍의 형태로 바인딩 했었습니다.
	//		그런데~~  현재 포워딩 당한 member4.jsp에서도 ~  session내장객체 영역에  address키와 함께 "수원시 팔달구"값을 함꼐 묶어서 한쌍의 형태로 바인딩 할것입니다.
	//		이렇게 같은 address키 이름으로 바인딩한 값을 아래의 EL태그로 얻어 출력할때  XXXScope.은 생략 할수 있는데...
	//      어떤 내장객체 메모리에 접근 해서 address키에 관한 값을 얻어 출력하는지 살펴보자.
	//요약 : session내장객체 메모리에 한번더~~ address 키와 함께 "수원시 팔달구"값을 한쌍의 형태로 묶어서 바인딩
	session.setAttribute("address", "수원시 팔달구");
%>
<table width="100%" align="center">
	<tr align="center" bgcolor="pink">
		<td width="7%">아이디</td>
		<td width="7%">비밀번호</td>
		<td width="7%">이름</td>
		<td width="7%">이메일</td>
	</tr>
<%-- 순서3. foward4.jsp에서 포워딩 해서 공유한 각 내장객체 메모리 영역에 접근해서 바인딩한 값들을 얻어 EL태그로 출력(xxxxScope.  생략 해서 작성해 보자.) --%>		
		<tr align="center">
			<td width="7%">${id}</td>    <%-- ${requestScope.id} 작성 전체 구문에서  requestScope. 생략 가능!  --%>
			<td width="7%">${pwd}</td>   <%-- ${requestScope.pwd} 작성 전체 구문에서 requestScope. 생략 가능! --%>
			<td width="7%">${name}</td>  <%-- ${sessionScope.name} 작성 전체 구문에서  sessionScope. 생략 가능!  --%>
			<td width="7%">${address}</td>
			<%-- ${requestScope.address} 또는 ${sessionScope.address} 작성 전체 구문중 하나를 작성 해야 하지만  모두 xxxxxScope. 생략가능!   --%>
		</tr>	
	
	<%--   page -> request ->  session -> application    
		 
		   결론 : 위의 EL태그로 xxxxxScope. 을 생략하여 ~~  address키(속성)의 값을 얻어 출력하면 
		   		 
		   		 같은 address키(속성)로 각각 request와 session내장객체에 바인딩 해 놓았기때문에!!
		   		 
		   		 session보다 request내장객체 메모리 영역에 EL태그로 접근하는 우선순위가 더 높으므로
		   		 
		   		 request내장객체 메모리영역에 바인딩된  "서울시 강남구" 값을 얻어 출력할것이다.
	
	--%>			
</table>	








