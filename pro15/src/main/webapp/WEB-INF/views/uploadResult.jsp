<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%-- JSTL 중에서 core태그들을 사용하기 위해 외부 주소로 요청 --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>업로드 결과를 보여주는 uploadResult.jsp 화면</title>
<style type="text/css">
	body  { font-family:'Malgun Gothic'; padding:24px; background:#f7f9fc; }
	.box  { background:#fff; border:1px solid #cbd5e2; border-radius:8px; padding:24px; max-width:520px; }
	.ok   { color:#1e7a46; font-weight:bold; font-size:18px; }
	.fail { color:#c0392b; font-weight:bold; font-size:18px; }
	.btn  { display:inline-block; background:#1d5fa8; color:#fff; padding:9px 18px;
	        border-radius:6px; text-decoration:none; margin-top:16px; margin-right:8px; }
</style>
</head>
<body>
	<div class="box">
		
		<c:choose>
			<%-- FileUploadServlet에서 request.setAttribute("successCount", successCount)로
	             바인딩한 업로드 성공 건수가 1건 이상이면? --%>
			<c:when test="${requestScope.successCount > 0}">			
				<p class="ok">파일 ${successCount}건 업로드에 성공했습니다.</p>			
			</c:when>
					
			<%-- 업로드 성공 건수가 0건이면? (파일을 하나도 첨부 하지 않고 업로드 요청전송한 경우) --%>
			<c:otherwise>
				<p class="fail">업로드된 파일이 없습니다. 파일을 첨부해서 업로드 요청했는지 확인하세요.</p>	
			</c:otherwise>				
		</c:choose>
		
		<%-- 두 a링크 모두 JSP 파일명이 아니라 컨트롤러 역할을 하는 요청할 서블릿 매핑주소를 지정합니다. --%>
		
		<a class="btn" href="list.do">파일 목록 보기</a>
		<%-- GET방식 요청  http://localhost:8181/pro15/list.do   -> 다운로드 링크가 보이는 다운로드 요청 하는  화면 요청 --%>
		
		
		<a class="btn" href="upload.do">계속 업로드</a>
		<%--  GET방식 요청  http://localhost:8181/pro15/upload.do   -> 파일 첨부후 업로드 요청 하는 화면 요청 --%>
		
		
	</div>




</body>
</html>








