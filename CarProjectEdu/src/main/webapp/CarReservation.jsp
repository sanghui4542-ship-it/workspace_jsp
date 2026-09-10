<%-- 이 페이지의 글자 인코딩을 UTF-8 로 정한다. 빠뜨리면 한글이 깨진다 --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
	// contextPath — getContextPath( ) 의 결과를 담는다
	String contextPath = request.getContextPath();
%>
<%--
 ================================================================================
   CarReservation.jsp  -  예약하기 진입 화면
 ================================================================================
--%>
<div class="container">
	<div class="section-head">
		<span class="section-eyebrow">RESERVATION</span>
		<h2 class="section-heading">예약하기</h2>
		<p class="section-desc">필요한 차량 등급을 고르면 해당 차량 목록으로 이동합니다</p>
	</div>
	<div class="pick-grid mb-8">
	
		<%-- 소형차 유형만 CarController로 carlist 테이블에서 조회 요청 --%>
		<a class="pick-card" href="<%=contextPath%>/Car/carcategory.do?carcategory=Small">
			<span class="pick-icon">&#128663;</span>
			<div class="pick-name">소형 · 준중형</div>
			<div class="pick-meta">4~5인 · 시내 주행 / 출퇴근</div>
			<div class="pick-price">1일 30,000원부터</div>
		</a>
		
		<%-- 중형차 유형만 CarController로 carlist 테이블에서 조회 요청 --%>
		<a class="pick-card" href="<%=contextPath%>/Car/carcategory.do?carcategory=Mid">
			<span class="pick-icon">&#128665;</span>
			<div class="pick-name">중형 · SUV</div>
			<div class="pick-meta">5~7인 · 가족 여행 / 출장</div>
			<div class="pick-price">1일 65,000원부터</div>
		</a>
		
		<%-- 대형차 유형만 CarController로 carlist 테이블에서 조회 요청 --%>
		<a class="pick-card" href="<%=contextPath%>/Car/carcategory.do?carcategory=Big">
			<span class="pick-icon">&#128656;</span>
			<div class="pick-name">대형 · 승합</div>
			<div class="pick-meta">7~12인 · 단체 이동 / 의전</div>
			<div class="pick-price">1일 110,000원부터</div>
		</a>
	</div>
	<div class="text-center">
		<%-- 전체차량 유형 모두 CarController로 carlist 테이블에서 조회 요청 --%>
		<a class="btn btn-primary btn-lg" href="<%=contextPath%>/Car/CarList.do">
			전체 차량 보기
		</a>
	</div>
	<div class="section-soft mt-12">
		<div class="section-head">
			<h3 class="section-heading" style="font-size:var(--fs-xl);">예약은 4단계면 끝납니다</h3>
		</div>
		<div class="steps">
			<div class="step">
				<div class="step-title">차량 선택</div>
				<p class="step-desc">등급과 인원에 맞는 차량을 고릅니다.</p>
			</div>
			<div class="step">
				<div class="step-title">날짜 · 수량 입력</div>
				<p class="step-desc">대여 시작일과 기간, 대수를 정합니다.</p>
			</div>
			<div class="step">
				<div class="step-title">옵션 선택</div>
				<p class="step-desc">필요한 옵션만 추가합니다. 총액이 바로 계산됩니다.</p>
			</div>
			<div class="step">
				<div class="step-title">예약 완료</div>
				<p class="step-desc">연락처와 비밀번호로 언제든 확인·변경할 수 있습니다.</p>
			</div>
		</div>
	</div>
</div>