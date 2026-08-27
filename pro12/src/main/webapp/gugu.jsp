<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<title>Insert title here</title>
	</head>
	<body>
		<% //순서1. 요청한 데이터 한글 문자 인코딩 UTF-8  HttpServletRequest에 설정 
		   request.setCharacterEncoding("UTF-8");
		
		   //순서2. 요청한 데이터 얻기 
		   //설명 : gugu.html 화면에서 입력한 단수 얻기 
		   int dan = Integer.parseInt(request.getParameter("dan"));
		   
		   
		   //순서3. 브라우저로 응답할 구구단 모습을 만들어서 브라우저로 응답(출력)
		   /*
			2  X  1  = 2
			2  X  2   = 4
			...
			2  X  9   = 18
			*/				
		%>
		<table border="1" width="800">
		
			<tr bgcolor="yellow" align="center">
				<td colspan="2"><%=dan%>단 출력</td>
			</tr>
		<% 
		  for(int i=1;  i<10;  i++){ // 각 구구단의 곱하는 수만큼 9번 반복해서 구구단 정보 행<tr><td></td></tr> 단위로 출력
		%>	  
			<tr align="center">
				<td width="400"> <%=dan%> X <%=i%> </td>
				<td width="400"> <%=dan * i%> </td>
			</tr>
		<%	
		  }
		%>
		</table>
	
	
	</body>
</html>









