<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>업로드할 파일을 첨부하는 fileUpload.jsp 화면</title>
<style type="text/css">
	body  { font-family:'Malgun Gothic'; padding:24px; background:#f7f9fc; }
	h2    { color:#1d5fa8; }
	.box  { background:#fff; border:1px solid #cbd5e2; border-radius:8px; padding:20px; max-width:560px; }
	.row  { margin-bottom:12px; }
	label { display:inline-block; width:110px; font-weight:bold; }
	.btn  { background:#1d5fa8; color:#fff; border:none; padding:9px 18px;
	        border-radius:6px; cursor:pointer; font-size:14px; }
	.link { display:inline-block; margin-top:14px; color:#1d5fa8; font-weight:bold; }
	.tip  { color:#6b7c93; font-size:13px; margin-top:10px; }
</style>
</head>
<body>

	<h2>파일 업로드</h2>
	
	<div class="box">
	<%--
		참고. 파일 업로드용 form태그에서 반드시 지켜야 할 3가지

			1. method="post"
			   -> GET방식은 주소창에 데이터를 실어 보내므로 길이 제한이 있고
			      파일의 실제 내용인 이진 데이터를 담아 보낼 수 없습니다.

			2. enctype="multipart/form-data"
			   -> 이 속성이 없으면 파일의 실제 내용은 오지 않고 파일명 문자열만 전송됩니다.
			   -> 파일 업로드가 안 되는 가장 흔한 원인이 이 속성 누락 입니다.

			3. action="upload.do"
			   -> JSP 파일명이 아니라 컨트롤러(서블릿)의 주소를 지정합니다.
	--%>			
		<form action="upload.do" method="post" enctype="multipart/form-data">		
			<%--
				name 속성값은 FileUploadServlet에서 아래처럼 꺼내 쓰는 이름 입니다.
				multipartRequest.getOriginalFileName("file1")
			--%>		
			<div class="row">
				<label>첨부 파일1</label>
				<input type="file" name="file1">
			</div>			
			<div class="row">
				<label>첨부 파일2</label>
				<input type="file" name="file2">
			</div>		
			<div class="row">
				<label>첨부 파일3</label>
				<input type="file" name="file3">
			</div>	
			
			<button type="submit" class="btn">업로드요청</button>				
		</form>
		
		<p class="tip"> 한 번에 최대 10MB까지 첨부해서 업로드 할수 있습니다.
						같은 이름의 파일이 이미 업로드되어 있으면? 파일명 뒤에 숫자가 붙어 다른파일명으로 업로드됩니다.</p>
						
		<a  class="link"  href="list.do">업로드한 파일 목록 보기</a>				
	
	</div>


</body>
</html>










