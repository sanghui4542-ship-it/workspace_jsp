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
<%--
	 <c:redirect>태그
	 - 이태그는 response내장객체의 sendRedirect()메소드를 호출한 
	   포워딩을 통한 페이지 이동을 처리하는 태그입니다.
	      
	 - 문법
	 	<c:redirect url="재요청할 주소 경로"    />
	 
	 	또는
	 	
	 	<c:redirect url="재요청할 서버페이지 주소">
	 	
	 		<c:param name="전달할값을식별할속성명" value="전달할값" >
	 		<c:param name="전달할값을식별할속성명" value="전달할값" >
	 		<c:param name="전달할값을식별할속성명" value="전달할값" >
	 		<c:param name="전달할값을식별할속성명" value="전달할값" >
	 	
	 	</c:redirect>
	 
	 - 포워딩(재요청)시  다른 서버페이지로  전달할 값이 있다면  
	   <c:param>태그를 내부에 사용하면 됩니다. 
 --%> 

	<%-- 1. requestVar 변수를 만들고 "홍길동" 저장! , 그리고  request 내장객체 영역에 requestVar변수-"홍길동" 한쌍으로 바인딩 --%>
	<c:set  var="requestVar"  value="홍길동"   scope="request" />
	<%--
		설명
			   위 c:set태그로 선언한 requestVar변수는 request메모리 영역에 바인딩(저장)하게 되는데
			   
			   클라이언트가 http://locahost:8181/pro14/test03/redirectTest.jsp 주소를 입력해서
			   
			   최초로 요청했을떄  톰캣이 이 URL을 받아 새롭게 만든 request객체 메모리에 저장하게 됩니다.
			   
			  그러므로 아래의 c:reirect 태그를 이용해 /test03/OtherPage.jsp를 리다이렉트방법으로 포워딩하면
			 
			   톰캣서버는 또~ 새로운 요청 URL에 관한 새로운 request객체 메모리를 생성하게 되어 재요청이 이루어 집니다.
		 --%>	
	<hr>
	<%--
		2. OtherPage.jsp를 재요청(포워딩)- 리다이렉트 방법으로 하므로 새로운 요청을 발생시키므로  위 request객체 영역에 저장된 requestVar변수값은 유지 되지 않습니다.
		   만약 유지 하고 싶다면? session내장객체 메모리 영역에 requestVar변수를 바인딩(저장) 해야 합니다.
	 --%>	
	<c:redirect url="/test03/OtherPage.jsp">
		<c:param name="user_param1" value="출판사" />
		<c:param name="user_param2" value="한빛출판사"/>
	</c:redirect>
	
	<%--  위 c:redrect태그를 작성하면 
	
            아래 자바코드 처럼 URL이 만들어 지는 것이다. 
            
      response.sendRedirect("/test03/OhterPage.jsp?user_param1=출판사&user_param2=한빛출판사");  --%>	


</body>
</html>








