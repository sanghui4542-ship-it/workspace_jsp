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
 ================================================================================ 
--%>
<div class="container">
	<%-- =====================================================================
	     화면 제목 + 현재 조회 결과 개수
	     ===================================================================== --%>
	<div class="section-head">
		<span class="section-eyebrow">CAR LIST</span>	
		<h2 class="section-heading">차량 목록</h2>
		<p class="section-desc">
		<%--
		CarContrller 에서 request에 바인딩 했었던 모습 
	
			request.setAttribute("v", list);
		 --%>	
			<c:choose>	       
				<c:when test="${empty requestScope.v}">
					조회된 차량이 없습니다
				</c:when>
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
	     CarController 가 request 에 담아준 ArrayList 배열(CarListVO객체들)을 반복 출력한다.
	     
		 CarContrller 에서 request에 바인딩 했었던 모습 
	
			request.setAttribute("v", list);
		
	     ===================================================================== --%>
	<c:choose>
		<%-- 조회 결과가 없을 때 : 빈 화면을 그냥 두지 않고 다음 행동을 안내한다 --%>
		<c:when test="${empty requestScope.v}">	
			<div class="alert alert-info text-center">	
				조건에 맞는 차량이 없습니다.
				<a href="${contextPath}/Car/CarList.do">전체 차량 보기</a>
			</div>
		</c:when>
		
		<%-- 조회 결과가 있을 때 --%>
		<c:otherwise>
			<div class="car-grid">	
			
				<%-- 조회 결과가 있을 때  request에 바인딩한 ArrayList배열 안의 CarListVO객체들을(조회한 행 정보들을) 반복해서 보여주자. --%>
				<c:forEach var="vo" items="${requestScope.v}">
				
					<a class="car-card" href="${contextPath}/Car/CarInfo.do?carno=${vo.carno}">
						<div class="car-photo">
							
							<%--실제 차량 사진--%>
							<img src="${contextPath}/img/${vo.carimg}"
								 alt="${vo.carname} 차량 사진" loading="lazy">
								 
							<%-- 등급(유형) 배지 : 색으로 소형/중형/대형을 한눈에 구분 --%>
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
									<%-- 천단위 쉼표 : 45000 -> 45,000 (금액은 쉼표가 있어야 빨리 읽힌다) --%>
									<fmt:formatNumber value="${vo.carprice}" pattern="#,###"/>원
									<span class="unit">/ 1일</span>
								</div>
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





