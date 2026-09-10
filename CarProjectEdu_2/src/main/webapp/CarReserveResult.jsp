<%-- 이 페이지의 글자 인코딩을 UTF-8 로 정한다. 빠뜨리면 한글이 깨진다 --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%-- JSTL 의 core라이브러이 태그 사용을 위해 요청 구문 --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c"  %>

<%-- 금액에 천단위 쉼표를 넣기 위해 fmt 라이브러리를 추가로 불러온다 --%> 
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%-- 컨텍스트 주소 /CarProjectEdu 얻어 변수에 저장 --%>
<c:set  var="contextPath" value="${pageContext.request.contextPath}"   />

<%--
 ================================================================================
   CarReserveResult.jsp  -  예약 조회 결과 (예약 확인 / 변경 / 취소의 시작점)
 ================================================================================
--%>
<div class="container">
	<div class="section-head">
		<span class="section-eyebrow">MY RESERVATION</span>
		<h2 class="section-heading">예약 내역</h2>
		<p class="section-desc">대여 시작일이 지나지 않은 예약만 표시됩니다</p>
	</div>
	<c:if test="${not empty requestScope.flashMessage}">
		<div class="alert alert-success text-center">
			<c:out value="${requestScope.flashMessage}"/>
		</div>
	</c:if>
	<c:choose>
		<%-- ================= 예약이 없을 때 ================= --%>
		<c:when test="${empty requestScope.v}">			
			<div class="alert alert-info text-center">
				조회된 예약이 없습니다.<br>	
				<span class="fs-sm text-muted">
					연락처와 비밀번호가 예약 당시 입력한 값과 같은지 확인해 주세요.
				</span>
			</div>
			<div class="text-center mt-4">
				<a class="btn btn-primary" href="${contextPath}/Car/CarList.do">차량 보고 예약하기</a>
			</div>
		</c:when>
		<%-- ================= 예약 목록 ================= --%>
		<c:otherwise>
			<p class="text-muted fs-sm mb-4">총 ${requestScope.v.size()}건</p>
			
			<div class="car-grid">
			
				<%-- 예약 목록을 하나씩 꺼내며 아래 내용을 반복해 그린다 --%>
				<c:forEach var="carConfirmVo" items="${requestScope.v}">
						
					<%-- 옵션 1일 요금 합계를 계산한다 (총액 표시에 사용)
					     단가 : 보험 10,000 / WiFi 5,000 / 네비 3,000 / 시트 10,000 --%>
					<c:set var="optPerDay" value="0"/>
			
					<c:if test="${carConfirmVo.carins      == 1}"><c:set var="optPerDay" value="${optPerDay + 10000}"/></c:if>
					<c:if test="${carConfirmVo.carwifi     == 1}"><c:set var="optPerDay" value="${optPerDay + 5000}"/></c:if>
					<c:if test="${carConfirmVo.carnave     == 1}"><c:set var="optPerDay" value="${optPerDay + 3000}"/></c:if>
					<c:if test="${carConfirmVo.carbabyseat == 1}"><c:set var="optPerDay" value="${optPerDay + 10000}"/></c:if>
					
					<%-- 총액 = (차량 1일요금 + 옵션 1일합계) x 수량 x 일수 --%>
					<c:set var="totalPrice"
						   value="${(carConfirmVo.carprice + optPerDay) * carConfirmVo.carqty * carConfirmVo.carreserveday}"/>
						
					<div class="card">					
						<div class="car-photo">			
							<c:choose>		
								<c:when test="${not empty carConfirmVo.carimg}">
					
									<img src="${contextPath}/img/${carConfirmVo.carimg}"
										 alt="${carConfirmVo.carname} 차량 사진" loading="lazy">
								</c:when>
								<%-- 위 조건이 전부 아닐 때 그릴 내용 --%>
								<c:otherwise>									
									<div class="flex items-center justify-center" style="height:100%;">								
										<span class="text-muted">사진 없음</span>
									</div>
								</c:otherwise>
							</c:choose>
							
							<%-- Controller 가 보낸 carConfirmVo 에서 orderid 값을 꺼내 화면에 찍는다 --%>
							<span class="car-badge">예약번호 ${carConfirmVo.orderid}</span>
						</div>
						
						<div class="car-body">
						
							<%-- Controller 가 보낸 carConfirmVo 에서 차량이름 값을 꺼내 화면에 찍는다 --%>
							<div class="car-name">${carConfirmVo.carname}</div>
								
							<%-- 예약 핵심 정보 : 언제부터 / 며칠 / 몇 대 --%>
							<dl class="detail-list" style="border-top:0;">
								
								<div class="detail-row">	
									<dt>대여 시작일</dt>
									<dd>${carConfirmVo.carbegindate}</dd>
								</div>								
								<div class="detail-row">			
									<dt>대여 기간</dt>
									<dd>${carConfirmVo.carreserveday}일 · ${carConfirmVo.carqty}대</dd>
								</div>
								
								<div class="detail-row">			
									<dt>선택 옵션</dt>	
									<dd>
										<%-- 선택한 옵션만 보여준다 (미적용 항목을 나열하지 않는다) --%>
										<c:set var="hasOpt" value="false"/>
										
										<c:if test="${carConfirmVo.carins == 1}">
											<span class="badge badge-brand">자차보험</span>
											<c:set var="hasOpt" value="true"/>
										</c:if>
										<c:if test="${carConfirmVo.carwifi == 1}">
											<span class="badge badge-brand">WiFi</span>
											<c:set var="hasOpt" value="true"/>
										</c:if>
										<c:if test="${carConfirmVo.carnave == 1}">
											<span class="badge badge-brand">네비게이션</span>
											<c:set var="hasOpt" value="true"/>
										</c:if>
										<c:if test="${carConfirmVo.carbabyseat == 1}">
											<span class="badge badge-brand">베이비시트</span>
											<c:set var="hasOpt" value="true"/>
										</c:if>
										<c:if test="${not hasOpt}">
											<span class="text-muted fs-sm">없음</span>
										</c:if>
									</dd>
								</div>
							</dl>						
							<div class="car-price-row">
								<div class="car-price">
									<fmt:formatNumber value="${totalPrice}" pattern="#,###"/>원
									<span class="unit">/ 총액</span>
								</div>
							</div>
							<%--
							 예약 수정 / 취소 버튼
							 [보안] 링크(GET) 대신 폼(POST) 을 사용한다.
							        비밀번호를 주소창에 남기지 않기 위한 것이다.
							--%>
							<div class="flex gap-2 mt-4">
														
								<%-- 예약 수정 : 수정 화면(중앙화면 VIEW) CarController로 요청 --%>
								<form action="${contextPath}/Car/update.do" method="post" style="flex:1;">
							
									<%-- [보안] CSRF 토큰 (설명은 members/login.jsp) --%>
									<input type="hidden" name="_csrf"       value="${_csrf}">								
									<input type="hidden" name="orderid"     value="${carConfirmVo.orderid}">								
									<input type="hidden" name="carimg"      value="${carConfirmVo.carimg}">								
									<input type="hidden" name="carname"     value="${carConfirmVo.carname}">
									<input type="hidden" name="memberphone" value="${requestScope.memberphone}">
								
									<button type="submit" class="btn btn-secondary btn-block btn-sm">예약 수정</button>
								</form>
								<%-- 예약 취소 :  예약 취소를 위해  비밀번호 재확인을 위해 입력하는 (중앙화면 VIEW) CarController로 요청 --%>
								<form action="${contextPath}/Car/delete.do" method="post" style="flex:1;">
								
									<%-- [보안] CSRF 토큰 (설명은 members/login.jsp) --%>
									<input type="hidden" name="_csrf"       value="${_csrf}">								
									<input type="hidden" name="orderid"     value="${carConfirmVo.orderid}">							
									<input type="hidden" name="carname"     value="${carConfirmVo.carname}">							
									<input type="hidden" name="memberphone" value="${requestScope.memberphone}">							
									<input type="hidden" name="center"      value="Delete.jsp">							
									<button type="submit" class="btn btn-ghost btn-block btn-sm">예약 취소</button>
								</form>
							</div>
						</div>
					</div>
				</c:forEach>
			</div>
			<div class="text-center mt-8">
				<a class="btn btn-outline" href="${contextPath}/Car/CarList.do">차량 더 예약하기</a>
			</div>
		</c:otherwise>
	</c:choose>
</div>