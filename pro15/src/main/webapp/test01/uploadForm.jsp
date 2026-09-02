<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
    
<%-- JSTL 전체 라이브러리에 속한 core 라이브러리 태그 사용을 위해 외부 사이트에 태그 요청 --%>   
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c"  %> 

<% request.setCharacterEncoding("UTF-8"); %>
    
<%-- 톰캣 서버가 프로젝트 까지 찾아갈수 있는 컨텍스트 주소 경로 "/pro15" 얻어 변수에 저장 --%>    
<c:set  var="contextPath" value="${pageContext.request.contextPath}" />    
    <%--      "/pro15"   --%>
        
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<%--
		 서블릿(서버페이지)에 파일업로드를 요청해 파일업로드 하기위해 
		 action속성에 서블릿을 요청할 매핑주소 /upload.do 를 작성하고,
		 파일 업로드 요청시 ~~ 반드시 <form>태그에는 enctype속성의 값을 multipart/form-data 로 설정해서 업로드요청해야합니다.
	 --%>
	<form action="${contextPath}/upload.do" method="post"  enctype="multipart/form-data">
	
		첨부파일1 : <input type="file" name="file1"> <br>  <%-- DiskFileItem 객체 정보 --%>
		첨부파일2 : <input type="file" name="file2"> <br>  <%-- DiskFileItem 객체 정보 --%>
	
		파라미터1 : <input type="text" name="param1"> <br> <%-- DiskFileItem 객체 정보 --%>
		파라미터2 : <input type="text" name="param2"> <br> <%-- DiskFileItem 객체 정보 --%>
		파라미터3 : <input type="text" name="param3"> <br> <%-- DiskFileItem 객체 정보 --%>
		
		<input type="submit" value="업로드">
	
	</form>



</body>
</html>








