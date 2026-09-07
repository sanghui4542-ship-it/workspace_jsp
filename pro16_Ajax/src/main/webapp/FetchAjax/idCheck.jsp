<%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%--
================================================================================
 파일명 : FetchAjax/idCheck.jsp
 역할   : 2.html 요청 화면이 보낸 아이디가 이미 사용중인지 검사해서 JSON 으로 응답
================================================================================

 참고. 실제 프로젝트에서의 모습

   지금은 수업용이라 아이디 3개를 코드에 직접 적어 두었지만
   실제 프로젝트에서는 아래처럼 DAO 를 호출해 DB 를 조회합니다.

     MemberDAO dao = new MemberDAO();
     boolean exist = dao.isExistId(id);

   나머지 구조는 실무와 동일합니다.

 참고. 응답 결과의 모습

   admin 을 입력한 경우 :  {"id":"admin", "exist":true}
   abcd  를 입력한 경우 :  {"id":"abcd", "exist":false}
--%>

<%
	//1. 요청한 데이터의 한글 문자 인코딩 설정
	request.setCharacterEncoding("UTF-8");

	//2. 요청한 아이디 얻기
	//	 2.html 이  idCheck.jsp?id=admin 형태로 보낼것이다.
	String id = request.getParameter("id");
	
	//3. 이미 사용중인 아이디 인지 검사
	boolean exist = "admin".equals(id) || "test".equals(id) || "hong".equals(id);	
	
	//4. 톰캣 서버가 일하는 시간을 눈으로 확인하기 위해 1초 지연 
	Thread.sleep(1000);
	
	//5. 아이디 중복인지 아닌지 응답
%>
{"id":"<%=id%>", "exist":<%=exist%>}














