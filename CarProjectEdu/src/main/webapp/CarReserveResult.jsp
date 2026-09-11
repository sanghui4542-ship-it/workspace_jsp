<%-- 이 페이지의 글자 인코딩을 UTF-8 로 정한다. 빠뜨리면 한글이 깨진다 --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%-- JSTL 의 core라이브러이 태그 사용을 위해 요청 구문 --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c"  %>
<%-- 컨텍스트 주소 /CarProject 얻어 변수에 저장 --%>
<c:set  var="contextPath" value="${pageContext.request.contextPath}"   />
<%-- 금액에 천단위 쉼표를 넣기 위해 fmt 라이브러리를 추가로 불러온다 --%> 
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%--
 ================================================================================
   CarReserveResult.jsp  -  예약 조회 결과 (예약 확인 / 변경 / 취소의 시작점)
   [6단계 전면 재작성]
   ------------------------------------------------------------------------------
   ★ 가장 중요한 수정 : 비밀번호가 주소(URL)에 노출되고 있었다
     (기존 코드)
         <a href="...?orderid=3&carimg=k5.jpg&memberpass=${memberpass}&memberphone=...">
             예약수정
         </a>
     예약 수정 / 취소 링크에 예약 비밀번호를 그대로 붙여 보냈다.
     주소창에 들어간 값은 아래에 모두 남는다.
         - 브라우저 방문 기록
         - 서버 접속 로그 (access log)
         - 다른 사이트로 이동할 때 Referer 헤더
     -> 링크(GET)를 폼(POST)으로 바꿨다.
        POST 는 값을 요청 본문에 담아 보내므로 주소창에 남지 않는다.
   ------------------------------------------------------------------------------
   ★ 두 번째 수정 : 11칸 표를 모바일에서 볼 수 없었다
     열이 11개(썸네일/차량명/대여일/기간/가격/보험/WiFi/네비/시트/수정/취소)였다.
     스마트폰에서는 가로로 한참 밀어야 했고, 어떤 칸이 무엇인지 알기 어려웠다.
     -> 예약 1건 = 카드 1개 로 바꿨다.
        차량 사진과 함께 "언제부터 며칠, 얼마" 를 위에서 아래로 읽을 수 있다.
   ------------------------------------------------------------------------------
   ★ 그 외
     - 이모지 썸네일 -> 실제 차량 사진
     - 45000원 -> 45,000원 (천단위 쉼표)
     - 옵션은 "선택한 것만" 배지로 표시 (미적용 4개를 나열하지 않는다)
     - 총 결제금액을 계산해 함께 보여준다
 ================================================================================
--%>
<div class="container">
	<%-- section-head 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
	<div class="section-head">
		<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
		<span class="section-eyebrow">MY RESERVATION</span>
		<%-- 제목 --%>
		<h2 class="section-heading">예약 내역</h2>
		<%-- 문단 글 --%>
		<p class="section-desc">대여 시작일이 지나지 않은 예약만 표시됩니다</p>
	</div>
	<%-- 예약 취소 등 처리 결과 안내 (서버가 flashMessage 를 담아준 경우) --%>
	<c:if test="${not empty requestScope.flashMessage}">
		<%-- alert alert-success text-center 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
		<div class="alert alert-success text-center">
			<%-- 값을 안전하게 출력한다. 태그 문자를 글자로 바꿔줘 스크립트 공격을 막는다 --%>
			<c:out value="${requestScope.flashMessage}"/>
		</div>
	</c:if>
	<%-- 여러 갈래 중 하나만 그린다. 아래 when·otherwise 로 갈래를 적는다 --%>
	<c:choose>
		<%-- ================= 예약이 없을 때 ================= --%>
		<c:when test="${empty requestScope.v}">
			<%-- alert alert-info text-center 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
			<div class="alert alert-info text-center">
				조회된 예약이 없습니다.<br>
				<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
				<span class="fs-sm text-muted">
					<%-- 화면에 그대로 보이는 글자: "연락처와 비밀번호가 예약 당시 입력한 값과 …" --%>
					연락처와 비밀번호가 예약 당시 입력한 값과 같은지 확인해 주세요.
				</span>
			</div>
			<%-- text-center mt-4 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
			<div class="text-center mt-4">
				<%-- 다른 화면으로 넘어가는 링크 --%>
				<a class="btn btn-primary" href="${contextPath}/Car/CarList.do">차량 보고 예약하기</a>
			</div>
		</c:when>
		<%-- ================= 예약 목록 ================= --%>
		<c:otherwise>
			<%-- 문단 글 --%>
			<p class="text-muted fs-sm mb-4">총 ${requestScope.v.size()}건</p>
			<%-- car-grid 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
			<div class="car-grid">
				<%-- 목록을 하나씩 꺼내며 아래 내용을 반복해 그린다 --%>
				<c:forEach var="carConfirmVo" items="${requestScope.v}">
					<%-- 옵션 1일 요금 합계를 계산한다 (총액 표시에 사용)
					     단가 : 보험 10,000 / WiFi 5,000 / 네비 3,000 / 시트 10,000 --%>
					<c:set var="optPerDay" value="0"/>
					<%-- 조건 ${carConfirmVo.carins      == 1} 이 맞을 때만 아래를 화면에 그린다 --%>
					<c:if test="${carConfirmVo.carins      == 1}"><c:set var="optPerDay" value="${optPerDay + 10000}"/></c:if>
					<%-- 조건 ${carConfirmVo.carwifi     == 1} 이 맞을 때만 아래를 화면에 그린다 --%>
					<c:if test="${carConfirmVo.carwifi     == 1}"><c:set var="optPerDay" value="${optPerDay + 5000}"/></c:if>
					<%-- 조건 ${carConfirmVo.carnave     == 1} 이 맞을 때만 아래를 화면에 그린다 --%>
					<c:if test="${carConfirmVo.carnave     == 1}"><c:set var="optPerDay" value="${optPerDay + 3000}"/></c:if>
					<%-- 조건 ${carConfirmVo.carbabyseat == 1} 이 맞을 때만 아래를 화면에 그린다 --%>
					<c:if test="${carConfirmVo.carbabyseat == 1}"><c:set var="optPerDay" value="${optPerDay + 10000}"/></c:if>
					<%-- 총액 = (차량 1일요금 + 옵션 1일합계) x 수량 x 일수 --%>
					<c:set var="totalPrice"
						   value="${(carConfirmVo.carprice + optPerDay) * carConfirmVo.carqty * carConfirmVo.carreserveday}"/>
					<%-- card 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
					<div class="card">
						<%-- 차량 사진 (실제 이미지) --%>
						<div class="car-photo">
							<%-- 여러 갈래 중 하나만 그린다. 아래 when·otherwise 로 갈래를 적는다 --%>
							<c:choose>
								<%-- ${not empty carConfirmVo.carimg} 일 때 그릴 내용 --%>
								<c:when test="${not empty carConfirmVo.carimg}">
									<%-- Controller 가 보낸 carConfirmVo 에서 carimg 값을 꺼내 화면에 찍는다 --%>
									<img src="${contextPath}/img/${carConfirmVo.carimg}"
										 alt="${carConfirmVo.carname} 차량 사진" loading="lazy">
								</c:when>
								<%-- 위 조건이 전부 아닐 때 그릴 내용 --%>
								<c:otherwise>
									<%-- flex items-center justify-center 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
									<div class="flex items-center justify-center" style="height:100%;">
										<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
										<span class="text-muted">사진 없음</span>
									</div>
								</c:otherwise>
							</c:choose>
							<%-- Controller 가 보낸 carConfirmVo 에서 orderid 값을 꺼내 화면에 찍는다 --%>
							<span class="car-badge">예약번호 ${carConfirmVo.orderid}</span>
						</div>
						<%-- car-body 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
						<div class="car-body">
							<%-- Controller 가 보낸 carConfirmVo 에서 차량이름 값을 꺼내 화면에 찍는다 --%>
							<div class="car-name">${carConfirmVo.carname}</div>
							<%-- 예약 핵심 정보 : 언제부터 / 며칠 / 몇 대 --%>
							<dl class="detail-list" style="border-top:0;">
								<%-- detail-row 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
								<div class="detail-row">
									<%-- 항목의 이름 --%>
									<dt>대여 시작일</dt>
									<%-- Controller 가 보낸 carConfirmVo 에서 carbegindate 값을 꺼내 화면에 찍는다 --%>
									<dd>${carConfirmVo.carbegindate}</dd>
								</div>
								<%-- detail-row 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
								<div class="detail-row">
									<%-- 항목의 이름 --%>
									<dt>대여 기간</dt>
									<%-- Controller 가 보낸 carConfirmVo 에서 carreserveday 값을 꺼내 화면에 찍는다 --%>
									<dd>${carConfirmVo.carreserveday}일 · ${carConfirmVo.carqty}대</dd>
								</div>
								<%-- detail-row 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
								<div class="detail-row">
									<%-- 항목의 이름 --%>
									<dt>선택 옵션</dt>
									<%-- 항목의 내용 --%>
									<dd>
										<%-- 선택한 옵션만 보여준다 (미적용 항목을 나열하지 않는다) --%>
										<c:set var="hasOpt" value="false"/>
										<%-- 조건 ${carConfirmVo.carins == 1} 이 맞을 때만 아래를 화면에 그린다 --%>
										<c:if test="${carConfirmVo.carins == 1}">
											<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
											<span class="badge badge-brand">자차보험</span><c:set var="hasOpt" value="true"/>
										</c:if>
										<%-- 조건 ${carConfirmVo.carwifi == 1} 이 맞을 때만 아래를 화면에 그린다 --%>
										<c:if test="${carConfirmVo.carwifi == 1}">
											<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
											<span class="badge badge-brand">WiFi</span><c:set var="hasOpt" value="true"/>
										</c:if>
										<%-- 조건 ${carConfirmVo.carnave == 1} 이 맞을 때만 아래를 화면에 그린다 --%>
										<c:if test="${carConfirmVo.carnave == 1}">
											<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
											<span class="badge badge-brand">네비게이션</span><c:set var="hasOpt" value="true"/>
										</c:if>
										<%-- 조건 ${carConfirmVo.carbabyseat == 1} 이 맞을 때만 아래를 화면에 그린다 --%>
										<c:if test="${carConfirmVo.carbabyseat == 1}">
											<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
											<span class="badge badge-brand">베이비시트</span><c:set var="hasOpt" value="true"/>
										</c:if>
										<%-- 조건 ${not hasOpt} 이 맞을 때만 아래를 화면에 그린다 --%>
										<c:if test="${not hasOpt}">
											<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
											<span class="text-muted fs-sm">없음</span>
										</c:if>
									</dd>
								</div>
							</dl>
							<%-- car-price-row 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
							<div class="car-price-row">
								<%-- car-price 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
								<div class="car-price">
									<%-- Controller 가 "totalPrice" 이름으로 실어 보낸 값을 화면에 찍는다 --%>
									<fmt:formatNumber value="${totalPrice}" pattern="#,###"/>원
									<%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
									<span class="unit">/ 총액</span>
								</div>
							</div>
							<%--
							 예약 수정 / 취소 버튼
							 [보안] 링크(GET) 대신 폼(POST) 을 사용한다.
							        비밀번호를 주소창에 남기지 않기 위한 것이다.
							--%>
							<div class="flex gap-2 mt-4">
								<%--
								 예약 수정 : 수정 화면으로 이동
								 [보안] 비밀번호를 hidden 으로 넘기지 않는다.
								   (제거된 코드)
								       <input type="hidden" name="memberpass" value="${requestScope.memberpass}">
								   사용자가 방금 입력한 비밀번호를 화면 HTML 안에 심어 다음 화면으로
								   넘기고 있었다. POST 라서 주소창에는 안 남지만,
								   "페이지 소스 보기" 를 하면 평문 비밀번호가 그대로 보인다.
								   수정 화면(CarConfirmUpdate.jsp)에서 사용자가 비밀번호를 직접 다시
								   입력하므로 넘길 필요가 없다. 오히려 다시 입력받는 것이
								   "본인 확인" 이라는 목적에 맞다.
								--%>
								<form action="${contextPath}/Car/update.do" method="post" style="flex:1;">
									<%-- [보안] CSRF 토큰 (설명은 members/login.jsp) --%>
									<input type="hidden" name="_csrf"       value="${_csrf}">
									<%-- 화면에는 안 보이지만 서버로 함께 보낼 orderid 값 --%>
									<input type="hidden" name="orderid"     value="${carConfirmVo.orderid}">
									<%-- 화면에는 안 보이지만 서버로 함께 보낼 carimg 값 --%>
									<input type="hidden" name="carimg"      value="${carConfirmVo.carimg}">
									<%-- 화면에는 안 보이지만 서버로 함께 보낼 차량이름 값 --%>
									<input type="hidden" name="carname"     value="${carConfirmVo.carname}">
									<%-- 화면에는 안 보이지만 서버로 함께 보낼 memberphone 값 --%>
									<input type="hidden" name="memberphone" value="${requestScope.memberphone}">
									<%-- 누르면 동작하는 버튼 --%>
									<button type="submit" class="btn btn-secondary btn-block btn-sm">예약 수정</button>
								</form>
								<%-- 예약 취소 : 비밀번호 재확인 화면으로 이동 --%>
								<form action="${contextPath}/Car/delete.do" method="post" style="flex:1;">
									<%-- [보안] CSRF 토큰 (설명은 members/login.jsp) --%>
									<input type="hidden" name="_csrf"       value="${_csrf}">
									<%-- 화면에는 안 보이지만 서버로 함께 보낼 orderid 값 --%>
									<input type="hidden" name="orderid"     value="${carConfirmVo.orderid}">
									<%-- 화면에는 안 보이지만 서버로 함께 보낼 차량이름 값 --%>
									<input type="hidden" name="carname"     value="${carConfirmVo.carname}">
									<%-- 화면에는 안 보이지만 서버로 함께 보낼 memberphone 값 --%>
									<input type="hidden" name="memberphone" value="${requestScope.memberphone}">
									<%-- 화면에는 안 보이지만 서버로 함께 보낼 center 값 --%>
									<input type="hidden" name="center"      value="Delete.jsp">
									<%-- 누르면 동작하는 버튼 --%>
									<button type="submit" class="btn btn-ghost btn-block btn-sm">예약 취소</button>
								</form>
							</div>
						</div>
					</div>
				</c:forEach>
			</div>
			<%-- text-center mt-8 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
			<div class="text-center mt-8">
				<%-- 다른 화면으로 넘어가는 링크 --%>
				<a class="btn btn-outline" href="${contextPath}/Car/CarList.do">차량 더 예약하기</a>
			</div>
		</c:otherwise>
	</c:choose>
</div>