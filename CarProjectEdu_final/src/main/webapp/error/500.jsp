<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>
<%--
 ================================================================================
   error/500.jsp  -  서버 내부 오류 안내 화면

   [보안 원칙]
     사용자에게는 "무엇이 잘못됐는지"를 자세히 알려주지 않는다.
     예외 메시지, SQL 문장, 파일 경로, 클래스명은 공격자에게 지도를 주는 정보다.
     상세 내용은 서버 콘솔/로그에만 남긴다.

     isErrorPage 속성을 켜지 않았으므로 이 화면에서는 exception 객체를 쓸 수 없다.
     (일부러 접근하지 못하게 둔 것이다)
 ================================================================================
--%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>일시적인 오류가 발생했습니다</title>
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
.error-help { margin-top: 24px; font-size: 0.85rem; color: #999; }
@media (min-width: 480px) { .error-actions { flex-direction: row; justify-content: center; } }
</style>
</head>
<body>

<div class="error-page">

	<p class="error-code">500</p>

	<h1 class="error-title">일시적인 오류가 발생했습니다</h1>

	<p class="error-desc">
		잠시 후 다시 시도해주세요.<br>
		문제가 계속되면 아래 번호로 문의해주세요.
	</p>

	<div class="error-actions">
		<a class="error-btn error-btn-primary" href="<%=request.getContextPath()%>/Car/Main">메인으로</a>
		<a class="error-btn error-btn-ghost" href="javascript:location.reload();">다시 시도</a>
	</div>

	<p class="error-help">(주)SM렌탈 고객센터 02-3456-6574</p>

</div>

</body>
</html>
