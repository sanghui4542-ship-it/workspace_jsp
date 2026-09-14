<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%--
 ================================================================================
   error/404.jsp  -  요청한 페이지/자원이 없을 때

   web.xml 의 <error-page> 에 등록되어 있어
     - 존재하지 않는 주소로 접속한 경우
     - 컨트롤러가 NotFoundException 을 던진 경우
   양쪽 모두 이 화면이 보인다.
 ================================================================================
--%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>페이지를 찾을 수 없습니다</title>
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/app.css?v=7">
<style>
.error-page { max-width: 520px; margin: 0 auto; padding: 48px 20px; text-align: center; font-family: "NanumGothic", "맑은 고딕", sans-serif; }
.error-code { font-size: 3rem; font-weight: 700; color: #cc0000; margin: 0 0 8px; }
.error-title { font-size: 1.25rem; margin: 0 0 12px; color: #333; }
.error-desc { font-size: 0.95rem; color: #666; line-height: 1.6; margin: 0 0 28px; word-break: keep-all; }
.error-actions { display: flex; flex-direction: column; gap: 10px; }
.error-btn { display: inline-block; padding: 13px 20px; border-radius: 8px; text-decoration: none; font-weight: 700; font-size: 1rem; min-height: 44px; box-sizing: border-box; }
.error-btn-primary { background: #cc0000; color: #fff; }
.error-btn-ghost { background: #f2f2f2; color: #333; }
@media (min-width: 480px) { .error-actions { flex-direction: row; justify-content: center; } }
</style>
</head>
<body>

<div class="error-page">

	<p class="error-code">404</p>

	<h1 class="error-title">페이지를 찾을 수 없습니다</h1>

	<p class="error-desc">
		<c:out value="${empty errorMessage ? '주소가 바뀌었거나 삭제된 페이지일 수 있습니다.' : errorMessage}"/>
	</p>

	<div class="error-actions">
		<a class="error-btn error-btn-primary" href="<%=request.getContextPath()%>/Car/Main">메인으로</a>
		<a class="error-btn error-btn-ghost" href="<%=request.getContextPath()%>/Car/CarList.do">차량 보기</a>
	</div>

</div>

</body>
</html>
