
<%@page import="sec02.ex01.MemberVO"%>
<%@page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<% 
	//1. 재요청(포워딩) 당해서 공유 받은 request객체 메모리의 한글데이터 인코딩 방식 UTF-8설정
	request.setCharacterEncoding("UTF-8");

	//2. member_action_java.jsp에서 request.setAttribute("list", membersList)로 바인딩한 조회 값 꺼내기
	//   -> getAttribute()의 리턴 타입은 Object이므로 ArrayList<MemberVO>로 강제 형변환 필수
	ArrayList<MemberVO>  list = (ArrayList<MemberVO>)request.getAttribute("list");
	
	//[MemberVO,  MemberVO,  MemberVO .......]  <===== ArrayList 배열  : 조회된 레코드(MemberVO)들이 저장되어 있음 
			  
%> 
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<%--3. 조회된 모든 회원 레코드 브라우저로 응답
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
<%
//[MemberVO,  MemberVO,  MemberVO .......]  <===== ArrayList 배열  : 조회된 레코드(MemberVO)들이 저장되어 있음 

		//4. list가 null이거나 저장된 MemberVO객체가 하나도 없으면? (c:when test="${empty list}")
		//   -> null 검사를 먼저 해야 NullPointerException이 발생하지 않음	
		if(list == null  || list.size() == 0){
%>
		<tr align="center">
			<td colspan="5">등록된 회원이 없습니다.</td>
		</tr>

<%      }else{//5. ArrayList배열 안에 조회된 MemberVO객체들이 하나라도 저장되어 있으면? (c:otherwise)
		
//          [   MemberVO,  MemberVO,  MemberVO .......  ]  <===== ArrayList 배열  : 조회된 레코드(MemberVO)들이 저장되어 있음 
		
			//6. 저장된 MemberVO객체 개수만큼 반복 (c:forEach var="membervo" items="${list}")
			for(int i=0;  i<list.size();  i++){
				
					//7. i index번째 방에 저장된 MemberVO객체 주소값을 꺼내서 membervo 참조변수에 저장
					MemberVO membervo = list.get(i);
%>			
					<tr align="center">
							<%--8. ${membervo.id} -> membervo.getId() 메소드 호출 결과를 출력 --%>
							<td width="7%"><%=membervo.getId()%></td>
							<td width="7%"><%=membervo.getPwd()%></td>
							<td width="7%"><%=membervo.getName()%></td>	
							<td width="7%"><%=membervo.getEmail()%></td>
							<td width="7%"><%=membervo.getJoinDate()%></td>
					</tr>							
<%					
			}
		}
%>	

	 	<tr height="2" bgcolor="#99ccff">
	 		<td colspan="5"></td>
	 	</tr>
	</table>
	
	

</body>
</html>















