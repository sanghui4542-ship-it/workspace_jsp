<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%-- JSTL의 core 라이브러리 태그들을 사용하기 위해 외부 사이트에서 불러오는 taglib 디렉티브 태그 한줄 작성 --%>    
<%@ taglib  uri="http://java.sun.com/jsp/jstl/core"  prefix="c" %>   

<% request.setCharacterEncoding("UTF-8");  %>       
    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
   	<table width="100%" align="center">
		<tr align="center" bgcolor="pink">
			<td width="7%">아이디</td>
			<td width="7%">비밀번호</td>
			<td width="7%">이름</td>
			<td width="7%">이메일</td>
		</tr>
<c:choose>
	<%-- 조건 : 아이디를 입력하지 않고 member9.jsp를 요청 했다면? --%>
	<c:when test="${empty param.id}">
		<tr align="center">
			<td colspan="4">아이디를 입력하세요!</td>
		</tr>
	</c:when>
	<%-- 조건2 : 아이디를 입력하고 member9.jsp를 요청 했다면? --%>
	<c:otherwise>
		<tr align="center" bgcolor="pink">
			<td width="7%"><c:out value="${param.id}"/></td>
			<td width="7%"><c:out value="${param.pwd}"/> </td>
			<td width="7%"><c:out value="${param.name}"/></td>
			<td width="7%"><c:out value="${param.email}"/></td>
		</tr>		
	</c:otherwise>
</c:choose>		
	</table>


</body>
</html>









