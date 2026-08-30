<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<h2>OuterPage1.jsp 외부파일1</h2>
	
	<%  String newVar1 = "고구려 세운 동명왕";   %>

	<%-- includeMain.jsp 페이지에서  page 내장객체 영역과 request 내장객체 영역에 각각 바인딩 했던 값 얻어 출력 --%>
	<ul>
		<li>
			page 내장객체 영역에 저장된 데이터를 얻어 출력:
			<%=pageContext.getAttribute("pAttr") %>
		</li>
		<li>
			request 내장객체 영역에 저장된 데이터를 얻어 출력:
			<%=request.getAttribute("rAttr") %>
		</li>
	</ul>


</body>
</html>




