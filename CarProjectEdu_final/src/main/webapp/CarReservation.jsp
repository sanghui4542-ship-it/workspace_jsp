<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%
	String contextPath = request.getContextPath();
%>
<%--
 ================================================================================
   CarReservation.jsp  -  예약하기 진입 화면

   [6단계 재작성]
     기존에는 "차량 종류 보기" 제목과 등급 select + 버튼 2개만 있었다.
     예약을 시작하는 첫 화면인데 어떤 차가 얼마인지 알 수 없어
     결국 [전체검색] 을 눌러 목록으로 나가야 했다.

     지금은 등급을 카드로 보여주고, 각 카드에 인원과 가격대를 적어
     한 번의 클릭으로 원하는 등급 목록으로 바로 갈 수 있게 했다.
     (메인 화면의 등급 바로가기와 같은 컴포넌트를 사용해 일관성을 맞췄다)
 ================================================================================
--%>

<div class="container">

	<div class="section-head">
		<span class="section-eyebrow">RESERVATION</span>
		<h2 class="section-heading">예약하기</h2>
		<p class="section-desc">필요한 차량 등급을 고르면 해당 차량 목록으로 이동합니다</p>
	</div>

	<div class="pick-grid mb-8">

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

	<div class="text-center">
		<a class="btn btn-primary btn-lg" href="<%=contextPath%>/Car/CarList.do">
			전체 차량 보기
		</a>
	</div>

	<%-- 이용 절차를 다시 보여줘 "예약이 복잡하지 않다"를 알린다 --%>
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