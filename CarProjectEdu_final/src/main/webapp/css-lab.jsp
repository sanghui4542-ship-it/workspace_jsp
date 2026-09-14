<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	/* ============================================================
	   css-lab.jsp — CSS 단계별 강의용 실습장
	   주소 뒤의 ?step=숫자 만큼 css/steps/ 파일을 차례로 끼운다.

	     css-lab.jsp?step=0  : CSS 없음 (HTML 뼈대 그대로 — 못생긴 게 정상)
	     css-lab.jsp?step=1  : + step1-layout.css  (배치)
	     css-lab.jsp?step=2  : + step2-color.css   (색·글꼴)
	     css-lab.jsp?step=3  : + step3-card.css    (카드·버튼)
	     css-lab.jsp?step=4  : + step4-detail.css  (여백·그림자·hover·반응형)

	   강의 방법: 학생은 step1 부터 파일을 직접 만들어 저장하고
	   새로고침해서 "방금 쓴 CSS가 화면을 어떻게 바꾸는지" 눈으로 확인한다.
	   ============================================================ */
	String contextPath = request.getContextPath();
	int step = 0;
	try { step = Integer.parseInt(request.getParameter("step")); } catch (Exception e) {}
	if (step < 0) step = 0;
	if (step > 4) step = 4;
	String[] stepFiles = { "step1-layout.css", "step2-color.css", "step3-card.css", "step4-detail.css" };
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>CSS 실습장 — <%= step %>단계</title>
<% for (int i = 0; i < step; i++) { %>
<link rel="stylesheet" href="<%=contextPath%>/css/steps/<%= stepFiles[i] %>">
<% } %>
</head>
<body>

<header>
	<a class="logo" href="#">SM렌탈</a>
	<nav>
		<ul>
			<li><a href="#">차량 목록</a></li>
			<li><a href="#">예약하기</a></li>
			<li><a href="#">예약확인</a></li>
			<li><a href="#">게시판</a></li>
			<li><a href="#">로그인</a></li>
		</ul>
	</nav>
</header>

<main>
	<h2>이번 주 인기 차량</h2>
	<div class="car-grid">
		<div class="car-card">
			<img src="<%=contextPath%>/img/morning.jpg" alt="모닝">
			<div class="card-body">
				<h3>모닝</h3>
				<p class="price">35,000원 / 1일</p>
				<a class="btn" href="#">예약하기</a>
			</div>
		</div>
		<div class="car-card">
			<img src="<%=contextPath%>/img/ray.jpg" alt="레이">
			<div class="card-body">
				<h3>레이</h3>
				<p class="price">38,000원 / 1일</p>
				<a class="btn" href="#">예약하기</a>
			</div>
		</div>
		<div class="car-card">
			<img src="<%=contextPath%>/img/spark.jpg" alt="스파크">
			<div class="card-body">
				<h3>스파크</h3>
				<p class="price">36,000원 / 1일</p>
				<a class="btn" href="#">예약하기</a>
			</div>
		</div>
	</div>
</main>

<footer>
	(주)SM렌탈 | 대표 신상국 | 서울시 강남구 역삼동 | 02-3456-6789
</footer>

<!-- 강의 진행용: 현재 단계와 다음 단계 이동 (실습장에만 있는 도구) -->
<div style="position:fixed; right:12px; bottom:12px; background:#222; color:#fff;
            padding:8px 14px; border-radius:8px; font-size:13px; opacity:0.85;">
	<%= step %>단계
	<% if (step > 0) { %><a style="color:#8cf" href="?step=<%= step-1 %>">◀ 이전</a><% } %>
	<% if (step < 4) { %><a style="color:#8cf" href="?step=<%= step+1 %>">다음 ▶</a><% } %>
</div>

</body>
</html>
