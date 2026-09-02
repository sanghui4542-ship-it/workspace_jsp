<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
 <%-- JSTL 중에서 core 태그들을 사용하기 위해 외부 주소로 요청 --%>    
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>  

 <%-- JSTL 중에서 fomatting 태그들을 사용하기 위해 외부 주소로 요청 --%>    
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>  

<% 
	//1. 재요청(포워딩) 당해서 공유 받은 request객체 메모리의 한글데이터 인코딩 방식 UTF-8설정
	request.setCharacterEncoding("UTF-8");
%>
  
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<%--2. 조회된 모든 회원 레코드 브라우저로 응답
		  -> t_member테이블에서 조회된 모든 회원 레코드의 정보를 표의 목록형태로 출력!
	 --%>
	<table width="100%" align="cener">
		<tr align="center" bgcolor="#99ccff">
			<td width="7%">아 이 디</td>
			<td width="7%">비밀번호</td>
			<td width="7%">이     름</td>
			<td width="7%">이 메 일</td>
			<td width="7%">가 입 일</td>
		</tr>
<c:choose>
	<%-- member_action.jsp에서 포워딩을 통해 공유받은 request내장객체 메모리 영역 에 바인딩된 ArrayList배열 안에 조회된 MemberVO객체들이 저장되어 있지 않으면? --%>
	<c:when test="${empty requestScope.list}">
		<tr align="center">
			<td colspan="5">등록된 회원이 없습니다.</td>
		</tr>
	</c:when>			
	<%-- member_action.jsp에서 포워딩을 통해 공유받은 request내장객체 메모리 영역 에 바인딩된 ArrayList배열 안에 조회된 MemberVO객체들이 하나라도 저장되어 있으면? --%>
	<c:otherwise>		
		<c:forEach var="membervo"  items="${requestScope.list}">  <%--  request.setAttribute("list", membersList); --%>
													              <%--  [MemberVO, MemberVO, MemberVO, MemberVO, MemberVO..... ] --%>
			<tr align="center">
				<td width="7%">${membervo.id}</td>
				<td width="7%">${membervo.pwd}</td>
				<td width="7%">${membervo.name}</td>
				<td width="7%">${membervo.email}</td>
				<td width="7%">${membervo.joinDate}</td>
			</tr>	
		</c:forEach>
	</c:otherwise>
</c:choose>			
		 	<tr height="2" bgcolor="#99ccff">
		 		<td colspan="5"></td>
		 	</tr>
	</table>
	
	

</body>
</html>















