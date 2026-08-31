<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%
	/*
           순서1. 입력한 가입 정보 중 한글문자 하나라도 존재하면  request 내장객체 메모리 영역에서 
                 입력해서 요청한 한글문자가 깨진 상태로 꺼내와 지므로 
                 컴퓨터가 한글 문자를 처리할수 있는 방식(인코딩 방식)의 값을 UTF-8(전세계 문자처리방식의값)으로 설정
	*/
	request.setCharacterEncoding("UTF-8");
%> 
 <table width="100%" align="center">
 	<tr align="center" bgcolor="pink">
 		<td width="7%">아이디</td>
 		<td width="7%">비밀번호</td>
 		<td width="7%">이름</td>
 		<td width="7%">이메일</td>
 		<td width="7%">주소</td>
 	</tr>
<%-- 순서2. memberForm.html 내부의 <input>에 입력해서 요청한 데이터들을 공유 받은 request 내장객체 메모리 영역에서 얻을때
           param 내장객체명으로 request에 접근하여 얻는다. --%>
     <tr align="center">
     	<td>${param.id}</td>
        <td>${param.pwd}</td>
        <td>${param.name}</td>
        <td>${param.email}</td>      
<%--
	순서3. forward.jsp에서 request.setAttribute("address","서울시 강남구"); 수동으로 개발자가 직접 바인딩 해 놓은
	      request 내장객체 메모리 영역에 접근 할때... EL에서 제공해주는 requestScope내장객체 명으로 접근하여 얻는다. --%>        
        <td>${requestScope.address}</td>       
<%--
	    위 ${requestScope.address} 코드를 자바언어로 작성할떄는? 아래와 같이 작성 해야 한다.
			 
		 <td>
		 	<%
		 		Object obj = request.getAttribute("address");
		 		String data = (String)obj;
		 		out.print(data);
		 	%> 
		 </td>	
 --%>	           
     </tr>            
     
      <tr height="1" bgcolor="pink">
 	 	<td colspan="5"></td>
 	 </tr>     	

 </table>	  
 
 
 
 
 
 
 
 
 