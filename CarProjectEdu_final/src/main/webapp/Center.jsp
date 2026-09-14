<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%
	String contextPath = request.getContextPath();
%>
<%--
 ============================================================================
  Center.jsp  -  메인 화면 (CarMain.jsp 가 처음 방문했을 때 가운데에 보여주는 화면)

  화면 구성 (위에서 아래로)
    1. 히어로       : 서비스 소개 + 예약하기 버튼
    2. 등급 바로가기 : 소형/중형/대형 중 먼저 고르게 한다
    3. 인기 차량     : CarController 의 /Main 이 조회한 carList 중 6대만 사진과 함께 보여준다
    4. 이용 절차     : 예약 4단계 안내
    5. 요금 안내     : 옵션 4개의 가격 (Service/CarService.java 의 PRICE_* 상수와 같은 값)
    6. 고객센터      : 전화번호 + AI 상담 버튼

  이 화면의 스타일은 이 파일 안에 없고 css/app.css 의 공용 클래스(.hero, .section, .car-card 등)를 쓴다
 ============================================================================
--%>

<div class="container">

	<%-- ===== 1. 히어로 ===== --%>
	<section class="hero">
		<div class="hero-inner">

			<span class="hero-eyebrow">SM RENTAL SERVICE</span>

			<h1 class="hero-title">
				필요한 순간에,<br>
				<span class="accent">바로 타는 렌터카</span>
			</h1>

			<p class="hero-desc">
				경차부터 12인승 승합차까지 26종 보유<br>
				회원가입 없이도 예약할 수 있습니다
			</p>

			<div class="hero-actions">
				<a class="btn btn-hero" href="<%=contextPath%>/Car/CarList.do">
					차량 보고 예약하기
				</a>
				<a class="btn btn-hero-ghost" href="<%=contextPath%>/Car/cc?center=CarReserveConfirm.jsp">
					내 예약 확인
				</a>
			</div>

			<%-- 보유 차종 수는 DB 조회 결과(carList)에서 가져온다. carList 가 없으면 "-" 로 표시한다 --%>
			<div class="hero-stats">
				<div class="hero-stat">
					<span class="num">${empty carList ? '-' : carList.size()}종</span>
					<span class="label">보유 차종</span>
				</div>
				<div class="hero-stat">
					<span class="num">4종</span>
					<span class="label">추가 옵션</span>
				</div>
				<div class="hero-stat">
					<span class="num">연중무휴</span>
					<span class="label">예약 접수</span>
				</div>
			</div>

		</div>
	</section>


	<%-- ===== 2. 등급 바로가기 : /Car/carcategory.do?carcategory=Small/Mid/Big 로 이동 ===== --%>
	<section class="section">

		<div class="section-head">
			<span class="section-eyebrow">STEP 1</span>
			<h2 class="section-heading">어떤 차가 필요하세요?</h2>
			<p class="section-desc">인원과 용도에 맞는 등급을 먼저 골라보세요</p>
		</div>

		<div class="pick-grid">

			<a class="pick-card" href="<%=contextPath%>/Car/carcategory.do?carcategory=Small">
				<span class="pick-icon">&#128663;</span>
				<div class="pick-name">소형 · 준중형</div>
				<div class="pick-meta">4~5인 · 시내 주행 / 출퇴근</div>
				<div class="pick-price">1일 30,000원부터</div>
			</a>

			<a class="pick-card" href="<%=contextPath%>/Car/carcategory.do?carcategory=Mid">
				<span class="pick-icon">&#128665;</span>
				<div class="pick-name">중형 · SUV</div>
				<div class="pick-meta">5~7인 · 가족 여행 / 출장</div>
				<div class="pick-price">1일 65,000원부터</div>
			</a>

			<a class="pick-card" href="<%=contextPath%>/Car/carcategory.do?carcategory=Big">
				<span class="pick-icon">&#128656;</span>
				<div class="pick-name">대형 · 승합</div>
				<div class="pick-meta">7~12인 · 단체 이동 / 의전</div>
				<div class="pick-price">1일 110,000원부터</div>
			</a>

		</div>
	</section>


	<%-- ===== 3. 인기 차량 : carList 앞에서 6대만 사진·가격과 함께 보여준다 ===== --%>
	<c:if test="${not empty carList}">
	<section class="section">

		<div class="section-head">
			<span class="section-eyebrow">POPULAR</span>
			<h2 class="section-heading">인기 차량</h2>
			<p class="section-desc">사진을 누르면 상세 정보와 예약으로 이어집니다</p>
		</div>

		<div class="car-grid">

			<%-- varStatus(st) 로 반복 횟수를 세어 6대(index 0~5)까지만 출력한다 --%>
			<c:forEach var="vo" items="${carList}" varStatus="st">
				<c:if test="${st.index < 6}">

					<a class="car-card" href="<%=contextPath%>/Car/CarInfo.do?carno=${vo.carno}">

						<div class="car-photo">
							<%-- img 폴더의 실제 차량 사진 (파일명은 DB carlist.carimg 열에 저장되어 있다)
							     loading="lazy" : 화면에 보일 때 불러와 첫 화면 로딩을 빠르게 한다 --%>
							<img src="<%=contextPath%>/img/${vo.carimg}"
								 alt="${vo.carname} 차량 사진" loading="lazy">

							<%-- 등급 배지 : 색으로 소형/중형/대형을 구분한다 --%>
							<c:choose>
								<c:when test="${vo.carcategory eq 'Small'}">
									<span class="car-badge car-badge-small">소형</span>
								</c:when>
								<c:when test="${vo.carcategory eq 'Mid'}">
									<span class="car-badge car-badge-mid">중형</span>
								</c:when>
								<c:otherwise>
									<span class="car-badge car-badge-big">대형</span>
								</c:otherwise>
							</c:choose>
						</div>

						<div class="car-body">

							<div class="car-name">${vo.carname}</div>

							<div class="car-meta">
								<span>${vo.carcompany}</span>
								<span>${vo.carusepeople}인승</span>
							</div>

							<p class="car-desc">${vo.carinfo}</p>

							<div class="car-price-row">
								<div class="car-price">
									<%-- 천단위 쉼표 넣기 : 45000 -> 45,000 --%>
									<fmt:formatNumber value="${vo.carprice}" pattern="#,###"/>원
									<span class="unit">/ 1일</span>
								</div>
								<span class="car-cta">예약하기 &rsaquo;</span>
							</div>

						</div>
					</a>

				</c:if>
			</c:forEach>

		</div>

		<div class="text-center mt-6">
			<a class="btn btn-outline" href="<%=contextPath%>/Car/CarList.do">
				전체 차량 보기 (${carList.size()}종)
			</a>
		</div>

	</section>
	</c:if>


	<%-- ===== 4. 이용 절차 (번호는 css/app.css 의 .step 이 자동으로 붙인다) ===== --%>
	<section class="section section-soft">

		<div class="section-head">
			<span class="section-eyebrow">HOW TO USE</span>
			<h2 class="section-heading">예약은 4단계면 끝납니다</h2>
			<p class="section-desc">회원가입 없이도 예약할 수 있습니다</p>
		</div>

		<div class="steps">

			<div class="step">
				<div class="step-title">차량 선택</div>
				<p class="step-desc">등급과 인원에 맞는 차량을 고릅니다.</p>
			</div>

			<div class="step">
				<div class="step-title">날짜 · 수량 입력</div>
				<p class="step-desc">대여 시작일과 기간, 필요한 대수를 정합니다.</p>
			</div>

			<div class="step">
				<div class="step-title">옵션 선택</div>
				<p class="step-desc">보험, 내비게이션 등 필요한 옵션만 추가합니다.</p>
			</div>

			<div class="step">
				<div class="step-title">예약 완료</div>
				<p class="step-desc">연락처와 비밀번호로 언제든 예약을 확인·변경할 수 있습니다.</p>
			</div>

		</div>
	</section>


	<%-- ===== 5. 요금 안내 : 아래 4개 금액은 Service/CarService.java 의 PRICE_* 상수와 같은 값이다 ===== --%>
	<section class="section">

		<div class="section-head">
			<span class="section-eyebrow">PRICE</span>
			<h2 class="section-heading">추가 옵션 요금</h2>
			<p class="section-desc">모든 옵션은 1일 기준이며, 필요한 것만 선택할 수 있습니다</p>
		</div>

		<div class="price-list">

			<div class="price-item">
				<span class="name">&#128737; 자차보험</span>
				<span class="amount">10,000원</span>
			</div>

			<div class="price-item">
				<span class="name">&#128246; 무선 WiFi</span>
				<span class="amount">5,000원</span>
			</div>

			<div class="price-item">
				<span class="name">&#128506; 내비게이션</span>
				<span class="amount">3,000원</span>
			</div>

			<div class="price-item">
				<span class="name">&#128118; 베이비시트</span>
				<span class="amount">10,000원</span>
			</div>

		</div>

		<p class="form-hint mt-4 text-center">
			총 결제금액 = (차량 1일 요금 + 선택한 옵션 합계) &times; 대여일수 &times; 대여수량
		</p>
	</section>


	<%-- ===== 6. 고객센터 ===== --%>
	<section class="section">
		<div class="cta-band">
			<div>
				<div class="fs-sm" style="opacity:.75;">전화 상담 · 평일 09:00 ~ 18:00</div>
				<div class="tel">02-3456-6574</div>
			</div>
			<a class="btn btn-hero" href="<%=contextPath%>/Car/ai?center=AIService.jsp">
				AI 상담받기
			</a>
		</div>
	</section>

</div>
