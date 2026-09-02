<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<% request.setCharacterEncoding("UTF-8"); %>  
  
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>first.jsp 입니다.</title>
</head>
<body>

	<%--
		 first.jsp페이지에서는 다운로드 시킬 파일이름을 hidden 태그에 설정해 
		 result.jsp페이지로 전송요청 합니다.
	 --%>
	<form action="result.jsp" method="post">
		
		<input type="hidden" name="param1" value="eclipse.exe">
		<input type="hidden" name="param2" value="lombok.jar">
	
		<input type="submit" value="다운로드할 파일명 전달">
	
	</form>



</body>
</html>




