<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%-- JSTL 중에서 core태그들을 사용하기 위해 외부 주소로 요청 --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%-- JSTL 중에서 functions태그들을 사용하기 위해 외부 주소로 요청 --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>업로드된 파일 목록을 보여주는 fileList.jsp 화면</title>
<style type="text/css">
	body  { font-family:'Malgun Gothic'; padding:24px; background:#f7f9fc; }
	h2    { color:#1d5fa8; }
	table { border-collapse:collapse; width:100%; background:#fff; margin-top:12px; }
	th,td { border:1px solid #cbd5e2; padding:9px; }
	th    { background:#e8f0fb; color:#1d5fa8; }
	a     { color:#1d5fa8; font-weight:bold; text-decoration:none; }
	.btn  { display:inline-block; background:#1d5fa8; color:#fff; padding:9px 18px;
	        border-radius:6px; text-decoration:none; }
	.none { color:#c0392b; font-weight:bold; }
</style>
</head>
<body>

	<h2>업로드된 파일 목록</h2>

	<a class="btn" href="upload.do">파일 업로드하기</a>
	
	<%-- request 내장객체에 바인딩된  ArrayList배열 모습
		   
		   [ new FileVO(..), new FileVO(..), new FileVO(..) ]  --%>
		   
	<h3>총 ${ fn:length(list) } 건</h3>

	<table>
		<tr>
			<th width="45%">원본 파일명(첨부한 파일명)</th>
			<th width="35%">톰켓 서버에 업로드한 실제 파일명</th>
			<th width="10%">다운로드 횟수</th>
			<th width="10%">다운로드</th>
		</tr>	
<c:choose>
	<%-- FileListServlet에서 포워딩을 통해 공유받은 request 내장객체 메모리 영역에
     바인딩된 ArrayList배열 안에 조회된 FileVO객체들이 저장되어 있지 않으면? --%>
	<c:when test="${empty requestScope.list}">
		<tr>
			<td colspan="4" align="center" class="none">업로드된 파일이 없습니다.</td>
		</tr>
	</c:when>
	
	<%-- FileListServlet에서 포워딩을 통해 공유받은 request내장객체 메모리 영역에
	     바인딩된 ArrayList배열 안에 조회된 FileVO객체들이 하나라도 저장되어 있으면? --%>
	<c:otherwise>
		
		<%--ArrayList배열에 저장된 FileVO객체의 개수만큼 반복하면서 한 개씩 꺼내어 vo라는 이름의 변수에 담아 사용합니다. 
		
		  							                              ArrayList 배열 모습 
		     			     items="${requestScope.list}" -> [ FileVO, FileVO, FileVO ..... ]  --%>
		<c:forEach var="vo"  items="${requestScope.list}">
			<tr>
				<%--
						fn:escapeXml() 로 감싸는 이유
						  - 파일명은 사용자가 지은 글자이므로 태그가 섞여 있을 수 있습니다.
						  - 예 : <script>alert('공격')</script>.txt 라는 이름으로 업로드
						  - 감싸지 않으면 브라우저가 진짜 태그로 해석해 스크립트를 실행합니다.
						    (이 공격을 XSS 라고 부릅니다)
						  - 감싸면 < 를 &lt; 로 바꿔 보내므로 글자로만 보이고 실행되지 않습니다.
			
						  바뀌는 글자 5개 :  <  >  &  "  '
						  주의. 바뀌는 것은 화면에 보내는 글자뿐이며 DB 원본은 그대로입니다.
				 --%>
				<td>${ fn:escapeXml(vo.fileName) }</td>      <%-- 업로드 요청시 첨부한 원본파일명  --%>
				<td>${ fn:escapeXml(vo.fileRealName) }</td>	 <%-- 업로드 한 실제 파일명 --%>
				<td align="center">${vo.downloadCount}</td>	 <%-- 다운로드 횟수 --%>
				<td align="center">
					<%--다운로드 요청 링크    fileRealName=다운로드할_실제파일명   --%>
					<a href="download.do?fileRealName=${ fn:escapeXml(vo.fileRealName) }">다운로드</a>
				</td>		
			</tr>
		</c:forEach>

	</c:otherwise>
</c:choose>	
	
	
	</table>



</body>
</html>











