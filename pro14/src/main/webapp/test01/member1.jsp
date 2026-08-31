<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<% 
  /* 순서1. 요청한 데이터 한글문자 인코딩 방식 UTF-8로 설정 */
  request.setCharacterEncoding("UTF-8");

  /*순서2. 입력해서 요청한 데이터들 얻기(가입을 위해 입력한 값들을 request 내장객체 메모리영역에서 꺼내오기)
	입력한 아이디 얻기
	입력한 비밀번호 얻기 
	입력한 이름 얻기
	입력한 이메일 얻기 */
  String id = request.getParameter("id");
  String pwd = request.getParameter("pwd");
  String name = request.getParameter("name");
  String email = request.getParameter("email");
%>
 <table width="100%" align="center">
 	<tr align="center" bgcolor="pink">
 		<td width="7%">아이디</td>
 		<td width="7%">비밀번호</td>
 		<td width="7%">이름</td>
 		<td width="7%">이메일</td>
 	</tr>
 	<%-- 순서3. 자바코드를 작성하는데... 위  순서2.에서 작성한 request.getParameter메소드 작성해서 얻었던 요청한 데이터들을 표현식태그로 출력 --%>
 	<tr align="center">
 		<td><%=id%></td>
 		<td><%=pwd%></td>
 		<td><%=name%></td>
 		<td><%=email%></td>
 	</tr>
 	<%--
 		순서3. EL 태그 영역 ${    } 이 제공 해주는 param 내장객체 명을 사용해 request 내장객체 메모리 영역에 접근하여 
 		      request.getParameter () 메소드 호출 구문을 작성하지 않고!!!! 바로 요청한 데이터들을 request 내장객체 메모리에서 얻어올수 있다.
 		      
 		      작성순서1. ${ param } 의미 -> 요청한 데이터들이 저장된? request 내장객체 메모리 영역에 접근 한다는 의미
 		      
 		      작성순서2. ${ param.<input>태그의 name_속성값 } 의미 -> request 내장객체 메모리영역에 param으로 접근해서 요청한 데이터 얻기 
 	 --%>
 	 <tr align="center">
 	 	<td>${param.id}</td> <!-- 입력한 아이디 얻을때  : 자바코드 작성은?   request.getParameter("id"); -->	
 	 	<td>${param.pwd}</td> <!-- 입력한 비밀번호 얻을때 : 자바코드 작성은 ? request.getParameter("pwd"); -->	
 	 	<td>${param.name}</td> <!--  입력한 이름을 얻을때 : 자바코드 작성은? request.getParameter("name"); -->
 	 	<td>${param.email}</td> <!--  입력한 이메일을 얻을때: 자바코드 작성은? request.getParameter("email"); -->	
 	 </tr>
 	 
 	 <tr height="1" bgcolor="pink">
 	 	<td colspan="5"></td>
 	 </tr>
 </table>	



















