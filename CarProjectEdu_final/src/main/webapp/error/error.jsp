<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%--
 ================================================================================
   error/error.jsp  -  잘못된 요청(400) / 권한 없음(403) 안내 화면

   [왜 만들었나]
     기존에는 입력값이 잘못되거나 권한이 없으면
     톰캣 기본 에러 페이지에 자바 예외 스택트레이스가 그대로 노출됐다.
     스택트레이스에는 클래스명, 파일 경로, 라이브러리 버전이 담겨 있어
     공격자에게 정보를 주는 통로가 된다.

   [주의]
     예외 메시지(errorMessage)는 우리가 직접 만든 안내문만 전달한다.
     예외의 원본 메시지(SQL 오류문 등)를 그대로 보여주면 안 된다.
 ================================================================================
--%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>요청을 처리할 수 없습니다</title>
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/app.css?v=7">
<style>
/* app.css 가 아직 없는 단계에서도 최소한의 모습이 유지되도록 하는 임시 스타일 */
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

	<p class="error-code">
		<c:out value="${empty errorStatus ? '400' : errorStatus}"/>
	</p>

	<h1 class="error-title">요청을 처리할 수 없습니다</h1>

	<p class="error-desc">
		<c:out value="${empty errorMessage ? '입력한 내용을 다시 확인해주세요.' : errorMessage}"/>
	</p>

	<div class="error-actions">
		<a class="error-btn error-btn-primary" href="<%=request.getContextPath()%>/Car/Main">메인으로</a>
		<a class="error-btn error-btn-ghost" href="javascript:history.back();">이전 화면으로</a>
	</div>

</div>

</body>
</html>
