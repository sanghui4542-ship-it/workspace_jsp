<%-- 이 페이지의 글자 인코딩을 UTF-8 로 정한다. 빠뜨리면 한글이 깨진다 --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
 <%-- JSTL 태그들 사용을 위해 불러오는 구문  --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%-- JSTL 태그를 fmt: 라는 이름으로 쓰겠다는 선언. 이 줄이 없으면 <fmt:...> 가 그냥 글자로 나온다 --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<%-- 자바 코드를 쓰는 구간의 시작 --%>
<% request.setCharacterEncoding("UTF-8"); %>

<%-- 화면에서만 쓸 임시 값 contextPath 을 만든다 --%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />


<%--
 ================================================================================
   CarList.jsp  -  차량 목록 화면
   [6단계 전면 재작성]
   (기존 화면의 문제)
     문제1. 실제 차량 사진을 쓰지 않았다.
            img 폴더에 차량 사진 26장이 있고 DB(carlist.carimg)에도 파일명이 있는데,
            화면은 아래처럼 이모지 플레이스홀더를 보여줬다.
                <div class="car-img-placeholder">
                    <span class="car-img-icon">🚗</span>
                    <span class="car-img-name">${vo.carname}</span>
                </div>
            차를 고르는 화면에서 차 사진이 없으면 고를 근거가 없다.
     문제2. 정보가 글자로만 나열됐다.
                차량명 : 아반떼
                한대당 렌트 가격 : 45000
            - "45000" 은 천단위 쉼표가 없어 45,000 인지 450,000 인지 순간 헷갈린다
            - 제조사 / 탑승인원 / 등급 / 설명이 DB 에 있는데 보여주지 않았다
     문제3. 화면 너비를 calc(50% - 10px) 처럼 직접 계산해 나눴다.
            열 개수를 미디어쿼리로 하나하나 지정해야 해서 중간 크기 화면에서 어색했다.
   (지금)
     - 실제 차량 사진 + 등급 배지 + 제조사/인원 + 설명 2줄 + 강조된 가격
     - CSS Grid 의 auto-fill 로 화면 너비에 따라 1~4열이 자동으로 바뀐다
     - 스타일은 css/app.css 의 .car-grid / .car-card 컴포넌트를 사용한다
       (이 파일에서 <style> 을 없애 중복을 제거했다)
 ================================================================================ 
--%>
<div class="container">
	<%-- =====================================================================
	     화면 제목 + 현재 조회 결과 개수
	     ===================================================================== --%>
	<div class="section-head">
		<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
		<span class="section-eyebrow">CAR LIST</span>
		<%-- 제목 --%>
		<h2 class="section-heading">차량 목록</h2>
		<%-- 문단 글 --%>
		<p class="section-desc">
			<%-- 여러 갈래 중 하나만 그린다. 아래 when·otherwise 로 갈래를 적는다 --%>
			<c:choose>
		         <%-- ${empty requestScope.v} 일 때 그릴 내용 --%>
				<c:when test="${empty requestScope.v}">
					<%-- 화면에 그대로 보이는 글자: "조회된 차량이 없습니다" --%>
					조회된 차량이 없습니다
				</c:when>
				<%-- 위 조건이 전부 아닐 때 그릴 내용 --%>
				<c:otherwise>
					총 <strong>${requestScope.v.size()}</strong>대 · 사진을 누르면 상세 정보와 예약으로 이어집니다
				</c:otherwise>
			</c:choose>
		</p>
	</div>
	<%-- =====================================================================
	     등급별 재검색
	     [변경] 기존에는 목록 아래쪽에 있었다.
	            목록을 다 본 뒤에야 "다시 좁힐 수 있다"는 걸 알게 되는 순서였다.
	            검색은 목록 위에 두는 것이 자연스럽다.
	      
	     ===================================================================== --%>
	<form class="flex flex-wrap gap-2 items-center mb-6"
		  action="${contextPath}/Car/carcategory.do" method="get">
		  
		<%-- 입력칸에 붙는 이름표 --%>
		<label class="form-label mb-0" for="carcategory">등급별 검색</label>
		
		<%-- 여러 개 중 하나를 고르는 목록 상자 — 서버로 "carcategory" 이름으로 전송 --%>
		<select class="form-control" id="carcategory" name="carcategory" style="max-width:180px;">	
			<option value="Small">소형 · 준중형</option> <%-- 고르기 목록의 항목 하나 (값 Small) --%>	
			<option value="Mid">중형 · SUV</option> <%-- 고르기 목록의 항목 하나 (값 Mid) --%>
			<option value="Big">대형 · 승합</option> <%-- 고르기 목록의 항목 하나 (값 Big) --%>
		</select>
		
		<%-- 누르면 동작하는 버튼 --%>
		<button type="submit" class="btn btn-secondary">검색</button>
		<%-- 전체 목록으로 되돌아가는 링크 (검색 후 빠져나올 길을 만들어 준다) --%>
		<a class="btn btn-ghost" href="${contextPath}/Car/CarList.do">전체 보기</a>
	</form>
	<%-- =====================================================================
	     차량 카드 그리드
	     CarController 가 request 에 담아준 Vector 배열(v)을 반복 출력한다.
	     ===================================================================== --%>
	<c:choose>
		<%-- 조회 결과가 없을 때 : 빈 화면을 그냥 두지 않고 다음 행동을 안내한다 --%>
		<c:when test="${empty requestScope.v}">	
			<div class="alert alert-info text-center">	
				조건에 맞는 차량이 없습니다.
				<a href="${contextPath}/Car/CarList.do">전체 차량 보기</a>
			</div>
		</c:when>
		<%-- 위 조건이 전부 아닐 때 그릴 내용 --%>
		<c:otherwise>
			<%-- car-grid 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
			<div class="car-grid">	
			
				<c:forEach var="vo" items="${requestScope.v}">
	
					<a class="car-card" href="${contextPath}/Car/CarInfo.do?carno=${vo.carno}">
						<%-- car-photo 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
						<div class="car-photo">
							
							<%--실제 차량 사진--%>
							<img src="${contextPath}/img/${vo.carimg}"
								 alt="${vo.carname} 차량 사진" loading="lazy">
								 
							<%-- 등급 배지 : 색으로 소형/중형/대형을 한눈에 구분 --%>
							<c:choose>		
								<c:when test="${vo.carcategory eq 'Small'}">
									<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
									<span class="car-badge car-badge-small">소형</span>
								</c:when>			
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
							<%-- 차량 설명 : app.css 가 2줄까지만 보여줘 카드 높이를 일정하게 유지한다 --%>
							<p class="car-desc">${vo.carinfo}</p>
							<%-- car-price-row 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
							<div class="car-price-row">
								<%-- car-price 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
								<div class="car-price">
									<%-- 천단위 쉼표 : 45000 -> 45,000 (금액은 쉼표가 있어야 빨리 읽힌다) --%>
									<fmt:formatNumber value="${vo.carprice}" pattern="#,###"/>원
									<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
									<span class="unit">/ 1일</span>
								</div>
								<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
								<span class="car-cta">예약하기 &rsaquo;</span>
							</div>
						</div>
					</a>
				</c:forEach>
			</div>
		</c:otherwise>
	</c:choose>
</div>
<%-- container 끝 --%>