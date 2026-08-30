<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	  include1.jsp의 상단 영역 코드 입니다.
	  
<%-- <jsp:include  page="재요청 해서 실행할 코드가 작성된 서버페이지 주소 경로"> ....... </jsp:include> --%>	  
	
	 <jsp:include page="duke_image.jsp">	 	
	 	 <jsp:param name="name"    value="duke" />
	 	 <jsp:param name="imgName" value="duke.png" />
	 </jsp:include>
<%--  
	include 액션태그 코드를 톰캣서버가 만나면
	page 속성에 작성한 서버페이지를 재요청해서 실행한 코드 내용을 웹브라워로 전달해 포함 시켜 보여줍니다
	재요청시 request 객체 메모리를 duke_image.jsp에 공유할수 있기떄문에
	request 객체 메모리에 바인딩후 재요청해서 공유합니다.
	
	include 액션태그에 의해 duke_image.jsp 재요청시~ request객체 메모리에
	요청하는 값을 추가하여 바인딩할때 사용되는 태그가 jsp:param 액션 태그 입니다.
--%>
	  include1.jsp의 하단 영역 코드 입니다.

</body>
</html>

