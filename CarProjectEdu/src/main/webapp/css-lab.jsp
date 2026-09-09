<%-- 이 페이지의 글자 인코딩을 UTF-8 로 정한다. 빠뜨리면 한글이 깨진다 --%>
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
	// step — 숫자 0 을 담는다
	int step = 0;
	// 화면(form·링크)에서 보낸 step 값을 꺼낸다
	try { step = Integer.parseInt(request.getParameter("step")); } catch (Exception e) {}
	// 조건을 확인해 맞을 때만 아래를 실행한다
	if (step < 0) step = 0;
	// 조건을 확인해 맞을 때만 아래를 실행한다
	if (step > 4) step = 4;
	// stepFiles — 계산한 값을 담는다
	String[] stepFiles = { "step1-layout.css", "step2-color.css", "step3-card.css", "step4-detail.css" };
%>
<%-- 이 문서가 HTML5 라는 선언. 항상 첫 줄에 온다 --%>
<!DOCTYPE html>
<%-- 문서 전체 시작 --%>
<html>
<%-- 화면에 안 보이는 설정 구역 --%>
<head>
<%-- 글자 인코딩 설정. 한글이 깨지지 않게 한다 --%>
<meta charset="UTF-8">
<%-- 휴대폰에서 화면 크기에 맞게 보이도록 하는 설정. 없으면 모바일에서 글씨가 아주 작아진다 --%>
<meta name="viewport" content="width=device-width, initial-scale=1">
<%-- 브라우저 탭에 표시되는 제목 --%>
<title>CSS 실습장 — <%= step %>단계</title>
<%-- 자바 코드를 쓰는 구간의 시작 --%>
<% for (int i = 0; i < step; i++) { %>
<%-- CSS 파일을 불러와 화면에 모양을 입힌다 --%>
<link rel="stylesheet" href="<%=contextPath%>/css/steps/<%= stepFiles[i] %>">
<%-- 자바 코드를 쓰는 구간의 시작 --%>
<% } %>
</head>
<%-- 실제로 보이는 내용 구역 --%>
<body>
	<header>
		<%-- 다른 화면으로 넘어가는 링크 --%>
		<a class="logo" href="#">SM렌탈</a>
		<%-- 메뉴 구역 --%>
		<nav>
			<%-- 점 목록 --%>
			<ul>
				<%-- 목록의 한 줄 --%>
				<li><a href="#">차량 목록</a></li>
				<%-- 목록의 한 줄 --%>
				<li><a href="#">예약하기</a></li>
				<%-- 목록의 한 줄 --%>
				<li><a href="#">예약확인</a></li>
				<%-- 목록의 한 줄 --%>
				<li><a href="#">게시판</a></li>
				<%-- 목록의 한 줄 --%>
				<li><a href="#">로그인</a></li>
			</ul>
		</nav>
	</header>
	<main>
		<%-- 제목 --%>
		<h2>이번 주 인기 차량</h2>
		<%-- car-grid 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
		<div class="car-grid">
			<%-- car-card 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
			<div class="car-card">
				<%-- 이미지를 화면에 보여준다 --%>
				<img src="<%=contextPath%>/img/morning.jpg" alt="모닝">
				<%-- card-body 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
				<div class="card-body">
					<%-- 작은 제목 --%>
					<h3>모닝</h3>
					<%-- 문단 글 --%>
					<p class="price">35,000원 / 1일</p>
					<%-- 다른 화면으로 넘어가는 링크 --%>
					<a class="btn" href="#">예약하기</a>
				</div>
			</div>
			<%-- car-card 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
			<div class="car-card">
				<%-- 이미지를 화면에 보여준다 --%>
				<img src="<%=contextPath%>/img/ray.jpg" alt="레이">
				<%-- card-body 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
				<div class="card-body">
					<%-- 작은 제목 --%>
					<h3>레이</h3>
					<%-- 문단 글 --%>
					<p class="price">38,000원 / 1일</p>
					<%-- 다른 화면으로 넘어가는 링크 --%>
					<a class="btn" href="#">예약하기</a>
				</div>
			</div>
			<%-- car-card 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
			<div class="car-card">
				<%-- 이미지를 화면에 보여준다 --%>
				<img src="<%=contextPath%>/img/spark.jpg" alt="스파크">
				<%-- card-body 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
				<div class="card-body">
					<%-- 작은 제목 --%>
					<h3>스파크</h3>
					<%-- 문단 글 --%>
					<p class="price">36,000원 / 1일</p>
					<%-- 다른 화면으로 넘어가는 링크 --%>
					<a class="btn" href="#">예약하기</a>
				</div>
			</div>
		</div>
	</main>
	<%-- 바닥 구역 --%>
	<footer>
		(주)SM렌탈 | 대표 신상국 | 서울시 강남구 역삼동 | 02-3456-6789
	</footer>
<!-- 강의 진행용: 현재 단계와 다음 단계 이동 (실습장에만 있는 도구) -->
<div style="position:fixed; right:12px; bottom:12px; background:#222; color:#fff;
            padding:8px 14px; border-radius:8px; font-size:13px; opacity:0.85;">
	<%-- 자바 값을 화면에 바로 찍는다 --%>
	<%= step %>단계
	<%-- 자바 코드를 쓰는 구간의 시작 --%>
	<% if (step > 0) { %><a style="color:#8cf" href="?step=<%= step-1 %>">◀ 이전</a><% } %>
	<%-- 자바 코드를 쓰는 구간의 시작 --%>
	<% if (step < 4) { %><a style="color:#8cf" href="?step=<%= step+1 %>">다음 ▶</a><% } %>
</div>
</body>
</html>

