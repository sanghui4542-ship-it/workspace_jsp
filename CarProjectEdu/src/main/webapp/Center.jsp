<%-- 이 페이지의 글자 인코딩을 UTF-8 로 정한다. 빠뜨리면 한글이 깨진다 --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%-- JSTL 태그를 c: 라는 이름으로 쓰겠다는 선언. 이 줄이 없으면 <c:...> 가 그냥 글자로 나온다 --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%-- JSTL 태그를 fmt: 라는 이름으로 쓰겠다는 선언. 이 줄이 없으면 <fmt:...> 가 그냥 글자로 나온다 --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%
	// contextPath — getContextPath( ) 의 결과를 담는다
	String contextPath = request.getContextPath();
%>
<%--
 ================================================================================
   Center.jsp  -  메인 화면 (첫 방문자가 보는 화면)
   [6단계 전면 재작성]
   (기존)
     그라디언트 배너 1개 + "지금 예약하기" 버튼 1개.  이것이 전부였다.
   무엇이 문제였나
     처음 방문한 사람 입장에서 아래를 알 수 없었다.
       - 어떤 차가 있는지          (사진도 가격도 없음)
       - 얼마인지                  (요금 정보가 어디에도 없음)
       - 어떻게 빌리는지            (절차 설명 없음)
       - 문제가 생기면 어디로 연락하는지
     결국 메뉴를 하나씩 눌러 들어가 봐야 파악이 됐다.
   (지금) 정보를 아래 순서로 쌓았다. 스크롤만 내리면 파악이 끝난다.
       1) 히어로       : 무슨 서비스인가 + 가장 중요한 행동 1개(예약)
       2) 등급 바로가기 : 소형/중형/대형 중 고르게 해서 선택 부담을 줄임
       3) 인기 차량     : 실제 사진 + 가격을 즉시 보여줌
       4) 이용 절차     : 4단계로 "어렵지 않다"를 시각화
       5) 요금 안내     : 추가 옵션 가격을 숨기지 않고 먼저 공개
       6) 고객센터      : 전화번호를 크게
   [실제 차량 사진을 쓰게 된 이유]
     img 폴더에 차량 사진 26장이 이미 들어 있고 DB(carlist.carimg)에도 파일명이 있는데,
     화면은 🚗 이모지 플레이스홀더를 쓰고 있었다.
     있는 자원을 쓰지 않아 서비스가 실제보다 빈약하게 보였다.
   스타일은 css/app.css 의 컴포넌트를 사용한다. (이 파일에는 <style> 을 두지 않는다)
 ================================================================================
--%>
<div class="container">
	<%-- =====================================================================
	     1. 히어로 : 첫 화면에서 "무엇을 하는 곳인지"와 "할 행동 1개"를 전달
	     ===================================================================== --%>
	<section class="hero">
		<%-- hero-inner 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
		<div class="hero-inner">
			<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
			<span class="hero-eyebrow">SM RENTAL SERVICE</span>
			<%-- 가장 큰 제목 --%>
			<h1 class="hero-title">
				필요한 순간에,<br>
				<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
				<span class="accent">바로 타는 렌터카</span>
			</h1>
			<%-- 문단 글 --%>
			<p class="hero-desc">
				경차부터 12인승 승합차까지 26종 보유<br>
				<%-- 화면에 그대로 보이는 글자: "회원가입 없이도 예약할 수 있습니다" --%>
				회원가입 없이도 예약할 수 있습니다
			</p>
			<%-- hero-actions 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
			<div class="hero-actions">
				<%-- 가장 중요한 행동은 버튼 하나로 명확하게 --%>
				<a class="btn btn-hero" href="<%=contextPath%>/Car/CarList.do">
					<%-- 화면에 그대로 보이는 글자: "차량 보고 예약하기" --%>
					차량 보고 예약하기
				</a>
				<%-- 다른 화면으로 넘어가는 링크 --%>
				<a class="btn btn-hero-ghost" href="<%=contextPath%>/Car/cc?center=CarReserveConfirm.jsp">
					<%-- 화면에 그대로 보이는 글자: "내 예약 확인" --%>
					내 예약 확인
				</a>
			</div>
			<%-- 신뢰 지표 : 숫자로 보여주면 설명 문장보다 빠르게 읽힌다 --%>
			<div class="hero-stats">
				<%-- hero-stat 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
				<div class="hero-stat">
					<%-- 보유 차종 수는 DB 조회 결과에서 가져온다 (숫자를 하드코딩하지 않는다).
					     carList 가 없을 때(조회 실패 등)는 이 항목을 아예 숨긴다. --%>
					<span class="num">${empty carList ? '-' : carList.size()}종</span>
					<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
					<span class="label">보유 차종</span>
				</div>
				<%-- hero-stat 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
				<div class="hero-stat">
					<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
					<span class="num">4종</span>
					<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
					<span class="label">추가 옵션</span>
				</div>
				<%-- hero-stat 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
				<div class="hero-stat">
					<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
					<span class="num">연중무휴</span>
					<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
					<span class="label">예약 접수</span>
				</div>
			</div>
		</div>
	</section>
	<%-- =====================================================================
	     2. 등급 바로가기
	         차량이 26종이면 "다 보여주기"보다 "먼저 좁혀주기"가 친절하다.
	     ===================================================================== --%>
	<section class="section">
		<%-- section-head 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
		<div class="section-head">
			<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
			<span class="section-eyebrow">STEP 1</span>
			<%-- 제목 --%>
			<h2 class="section-heading">어떤 차가 필요하세요?</h2>
			<%-- 문단 글 --%>
			<p class="section-desc">인원과 용도에 맞는 등급을 먼저 골라보세요</p>
		</div>
		<%-- pick-grid 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
		<div class="pick-grid">
			<%-- 다른 화면으로 넘어가는 링크 --%>
			<a class="pick-card" href="<%=contextPath%>/Car/carcategory.do?carcategory=Small">
				<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
				<span class="pick-icon">&#128663;</span>
				<%-- pick-name 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
				<div class="pick-name">소형 · 준중형</div>
				<%-- pick-meta 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
				<div class="pick-meta">4~5인 · 시내 주행 / 출퇴근</div>
				<%-- pick-price 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
				<div class="pick-price">1일 30,000원부터</div>
			</a>
			<%-- 다른 화면으로 넘어가는 링크 --%>
			<a class="pick-card" href="<%=contextPath%>/Car/carcategory.do?carcategory=Mid">
				<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
				<span class="pick-icon">&#128665;</span>
				<%-- pick-name 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
				<div class="pick-name">중형 · SUV</div>
				<%-- pick-meta 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
				<div class="pick-meta">5~7인 · 가족 여행 / 출장</div>
				<%-- pick-price 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
				<div class="pick-price">1일 65,000원부터</div>
			</a>
			<%-- 다른 화면으로 넘어가는 링크 --%>
			<a class="pick-card" href="<%=contextPath%>/Car/carcategory.do?carcategory=Big">
				<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
				<span class="pick-icon">&#128656;</span>
				<%-- pick-name 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
				<div class="pick-name">대형 · 승합</div>
				<%-- pick-meta 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
				<div class="pick-meta">7~12인 · 단체 이동 / 의전</div>
				<%-- pick-price 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
				<div class="pick-price">1일 110,000원부터</div>
			</a>
		</div>
	</section>
	<%-- =====================================================================
	     3. 인기 차량
	         CarController 의 /Main 이 조회해 넘겨준 carList 를 사용한다.
	         앞에서 6대만 보여주고, 나머지는 "전체 보기"로 유도한다.
	     ===================================================================== --%>
	<c:if test="${not empty carList}">
	<%-- 내용을 묶는 구역 --%>
	<section class="section">
		<%-- section-head 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
		<div class="section-head">
			<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
			<span class="section-eyebrow">POPULAR</span>
			<%-- 제목 --%>
			<h2 class="section-heading">인기 차량</h2>
			<%-- 문단 글 --%>
			<p class="section-desc">사진을 누르면 상세 정보와 예약으로 이어집니다</p>
		</div>
		<%-- car-grid 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
		<div class="car-grid">
			<%-- varStatus 로 반복 횟수를 세어 6대까지만 출력한다 --%>
			<c:forEach var="vo" items="${carList}" varStatus="st">
				<%-- 조건 ${st.index < 6} 이 맞을 때만 아래를 화면에 그린다 --%>
				<c:if test="${st.index < 6}">
					<%-- 다른 화면으로 넘어가는 링크 --%>
					<a class="car-card" href="<%=contextPath%>/Car/CarInfo.do?carno=${vo.carno}">
						<%-- car-photo 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
						<div class="car-photo">
							<%--
							 실제 차량 사진 (img 폴더의 파일명이 DB carimg 컬럼에 들어 있다)
							   loading="lazy" : 화면에 보일 때 불러와 첫 화면 로딩을 빠르게 한다
							   alt            : 사진을 못 볼 때와 스크린리더를 위한 설명
							--%>
							<img src="<%=contextPath%>/img/${vo.carimg}"
								 alt="${vo.carname} 차량 사진" loading="lazy">
							<%-- 등급 배지 : 색으로 소형/중형/대형을 구분 --%>
							<c:choose>
								<%-- ${vo.carcategory eq  일 때 그릴 내용 --%>
								<c:when test="${vo.carcategory eq 'Small'}">
									<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
									<span class="car-badge car-badge-small">소형</span>
								</c:when>
								<%-- ${vo.carcategory eq  일 때 그릴 내용 --%>
								<c:when test="${vo.carcategory eq 'Mid'}">
									<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
									<span class="car-badge car-badge-mid">중형</span>
								</c:when>
								<%-- 위 조건이 전부 아닐 때 그릴 내용 --%>
								<c:otherwise>
									<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
									<span class="car-badge car-badge-big">대형</span>
								</c:otherwise>
							</c:choose>
						</div>
						<%-- car-body 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
						<div class="car-body">
							<%-- Controller 가 보낸 vo 에서 차량이름 값을 꺼내 화면에 찍는다 --%>
							<div class="car-name">${vo.carname}</div>
							<%-- car-meta 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
							<div class="car-meta">
								<%-- Controller 가 보낸 vo 에서 carcompany 값을 꺼내 화면에 찍는다 --%>
								<span>${vo.carcompany}</span>
								<%-- Controller 가 보낸 vo 에서 carusepeople 값을 꺼내 화면에 찍는다 --%>
								<span>${vo.carusepeople}인승</span>
							</div>
							<%-- Controller 가 보낸 vo 에서 carinfo 값을 꺼내 화면에 찍는다 --%>
							<p class="car-desc">${vo.carinfo}</p>
							<%-- car-price-row 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
							<div class="car-price-row">
								<%-- car-price 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
								<div class="car-price">
									<%-- 숫자에 천단위 쉼표를 넣는다 : 45000 -> 45,000 (읽기 쉬움) --%>
									<fmt:formatNumber value="${vo.carprice}" pattern="#,###"/>원
									<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
									<span class="unit">/ 1일</span>
								</div>
								<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
								<span class="car-cta">예약하기 &rsaquo;</span>
							</div>
						</div>
					</a>
				</c:if>
			</c:forEach>
		</div>
		<%-- text-center mt-6 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
		<div class="text-center mt-6">
			<%-- 다른 화면으로 넘어가는 링크 --%>
			<a class="btn btn-outline" href="<%=contextPath%>/Car/CarList.do">
				전체 차량 보기 (${carList.size()}종)
			</a>
		</div>
	</section>
	</c:if>
	<%-- =====================================================================
	     4. 이용 절차
	         "복잡할 것 같다"는 심리적 장벽을 낮추는 구간.
	         번호는 CSS 카운터가 자동으로 붙인다 (app.css 의 .step)
	     ===================================================================== --%>
	<section class="section section-soft">
		<%-- section-head 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
		<div class="section-head">
			<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
			<span class="section-eyebrow">HOW TO USE</span>
			<%-- 제목 --%>
			<h2 class="section-heading">예약은 4단계면 끝납니다</h2>
			<%-- 문단 글 --%>
			<p class="section-desc">회원가입 없이도 예약할 수 있습니다</p>
		</div>
		<%-- steps 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
		<div class="steps">
			<%-- step 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
			<div class="step">
				<%-- step-title 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
				<div class="step-title">차량 선택</div>
				<%-- 문단 글 --%>
				<p class="step-desc">등급과 인원에 맞는 차량을 고릅니다.</p>
			</div>
			<%-- step 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
			<div class="step">
				<%-- step-title 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
				<div class="step-title">날짜 · 수량 입력</div>
				<%-- 문단 글 --%>
				<p class="step-desc">대여 시작일과 기간, 필요한 대수를 정합니다.</p>
			</div>
			<%-- step 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
			<div class="step">
				<%-- step-title 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
				<div class="step-title">옵션 선택</div>
				<%-- 문단 글 --%>
				<p class="step-desc">보험, 내비게이션 등 필요한 옵션만 추가합니다.</p>
			</div>
			<%-- step 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
			<div class="step">
				<%-- step-title 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
				<div class="step-title">예약 완료</div>
				<%-- 문단 글 --%>
				<p class="step-desc">연락처와 비밀번호로 언제든 예약을 확인·변경할 수 있습니다.</p>
			</div>
		</div>
	</section>
	<%-- =====================================================================
	     5. 요금 안내
	         옵션 가격을 마지막 결제 단계에서 처음 보여주면 신뢰를 잃는다.
	         먼저 공개한다.
	     ===================================================================== --%>
	<section class="section">
		<%-- section-head 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
		<div class="section-head">
			<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
			<span class="section-eyebrow">PRICE</span>
			<%-- 제목 --%>
			<h2 class="section-heading">추가 옵션 요금</h2>
			<%-- 문단 글 --%>
			<p class="section-desc">모든 옵션은 1일 기준이며, 필요한 것만 선택할 수 있습니다</p>
		</div>
		<%-- price-list 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
		<div class="price-list">
			<%-- price-item 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
			<div class="price-item">
				<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
				<span class="name">&#128737; 자차보험</span>
				<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
				<span class="amount">10,000원</span>
			</div>
			<%-- price-item 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
			<div class="price-item">
				<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
				<span class="name">&#128246; 무선 WiFi</span>
				<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
				<span class="amount">5,000원</span>
			</div>
			<%-- price-item 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
			<div class="price-item">
				<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
				<span class="name">&#128506; 내비게이션</span>
				<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
				<span class="amount">3,000원</span>
			</div>
			<%-- price-item 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
			<div class="price-item">
				<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
				<span class="name">&#128118; 베이비시트</span>
				<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
				<span class="amount">10,000원</span>
			</div>
		</div>
		<%-- 문단 글 --%>
		<p class="form-hint mt-4 text-center">
			총 결제금액 = (차량 1일 요금 + 선택한 옵션 합계) &times; 대여일수 &times; 대여수량
		</p>
	</section>
	<%-- =====================================================================
	     6. 고객센터
	     ===================================================================== --%>
	<section class="section">
		<%-- cta-band 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
		<div class="cta-band">
			<%-- 내용을 묶는 상자 --%>
			<div>
				<%-- fs-sm 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
				<div class="fs-sm" style="opacity:.75;">전화 상담 · 평일 09:00 ~ 18:00</div>
				<%-- tel 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
				<div class="tel">02-3456-6574</div>
			</div>
			<%-- 다른 화면으로 넘어가는 링크 --%>
			<a class="btn btn-hero" href="<%=contextPath%>/Car/ai?center=AIService.jsp">
				<%-- 화면에 그대로 보이는 글자: "AI 상담받기" --%>
				AI 상담받기
			</a>
		</div>
	</section>
</div>
<%-- container 끝 --%>