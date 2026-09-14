<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%-- JSTL 태그 사용을 위해 불러오는 코드 --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%-- [6단계 추가] fmt : 금액에 천단위 쉼표를 넣는 fmt:formatNumber 를 쓰기 위해 필요하다.
     (선언하지 않으면 화면이 500 에러가 난다) --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%
    /* Java 코드: 한글 인코딩 설정 */
    request.setCharacterEncoding("UTF-8");
    /* Java 코드: 컨텍스트 경로 얻기 */
    String contextPath = request.getContextPath();
%>


<%--
 ================================================================================
   CarOrder.jsp (비회원 예약)  -  예약 최종 확인 및 요청 (예약 흐름 3단계)

   [6단계 전면 재작성]

   (기존 화면의 문제)

     문제1. 무엇을 예약하는지 확인할 수 없었다.
            hidden 필드로 차번호/기간/옵션을 넘기기만 하고, 화면에는 금액 3줄만 보여줬다.
                예약할 차량 기본 총 금액 : 135000원
                예약할 차량 옵션 총 금액 : 30000원
                총 비용 : 165000원
            어떤 차를, 언제부터, 며칠, 몇 대, 어떤 옵션으로 빌리는지가 화면에 없었다.
            결제 직전 화면인데 정작 확인할 내용이 없었던 것이다.

     문제2. 금액에 천단위 쉼표가 없어 165000 을 한눈에 읽기 어려웠다.

     문제3. 예약 확인용 비밀번호가 왜 필요한지 설명이 없었다.
            이 비밀번호는 나중에 예약을 조회·변경·취소할 때 쓰는 값인데,
            안내가 없으면 사용자는 대충 입력하고 잊어버린다.

   (지금)
     - 차량 사진 + 예약 내역(차량/시작일/기간/수량/선택옵션)을 표로 확인
     - 금액을 천단위 쉼표로, 총액은 크게 강조
     - 비밀번호가 어디에 쓰이는지 안내 문구 추가
     - 모바일에서는 총액과 결제 버튼이 하단에 고정된다(app.css 의 .action-bar)
 ================================================================================
--%>

<div class="container">

	<div class="section-head">
		<span class="section-eyebrow">STEP 3 / 3</span>
		<h2 class="section-heading">예약 확인</h2>
		<p class="section-desc">아래 내용을 확인한 뒤 예약을 완료해 주세요</p>
	</div>

	<form action="<%=contextPath%>/Car/CarOrder.do" method="post">


		<%-- 앞 화면에서 계산된 예약 정보를 서버로 전달
		     (금액은 보내지 않는다. 서버가 DB 요금으로 다시 계산해 확정한다) --%>
		<input type="hidden" name="carno"          value="${requestScope.vo.carno}" />
		<input type="hidden" name="carqty"         value="${requestScope.vo.carqty}" />
		<input type="hidden" name="carreserveday"  value="${requestScope.vo.carreserveday}" />
		<input type="hidden" name="carbegindate"   value="${requestScope.vo.carbegindate}" />
		<input type="hidden" name="carins"         value="${requestScope.vo.carins}" />
		<input type="hidden" name="carwifi"        value="${requestScope.vo.carwifi}" />
		<input type="hidden" name="carnave"        value="${requestScope.vo.carnave}" />
		<input type="hidden" name="carbabyseat"    value="${requestScope.vo.carbabyseat}" />

		<div class="grid-2">

			<%-- ================= 왼쪽 : 예약 내역 ================= --%>
			<div>
				<h3 class="fw-bold mb-4">예약 내역</h3>

				<dl class="detail-list">

					<div class="detail-row">
						<dt>차량</dt>
						<dd>
							<c:out value="${empty param.carname ? '선택한 차량' : param.carname}"/>
						</dd>
					</div>

					<div class="detail-row">
						<dt>대여 시작일</dt>
						<dd>${requestScope.vo.carbegindate}</dd>
					</div>

					<div class="detail-row">
						<dt>대여 기간</dt>
						<dd>${requestScope.vo.carreserveday}일</dd>
					</div>

					<div class="detail-row">
						<dt>대여 수량</dt>
						<dd>${requestScope.vo.carqty}대</dd>
					</div>

					<div class="detail-row">
						<dt>선택 옵션</dt>
						<dd>
							<%-- 선택한 옵션만 배지로 보여준다. 하나도 없으면 그 사실을 알려준다. --%>
							<c:set var="hasOption" value="false"/>

							<c:if test="${requestScope.vo.carins == 1}">
								<span class="badge badge-brand">자차보험</span>
								<c:set var="hasOption" value="true"/>
							</c:if>
							<c:if test="${requestScope.vo.carwifi == 1}">
								<span class="badge badge-brand">무선 WiFi</span>
								<c:set var="hasOption" value="true"/>
							</c:if>
							<c:if test="${requestScope.vo.carnave == 1}">
								<span class="badge badge-brand">네비게이션</span>
								<c:set var="hasOption" value="true"/>
							</c:if>
							<c:if test="${requestScope.vo.carbabyseat == 1}">
								<span class="badge badge-brand">베이비시트</span>
								<c:set var="hasOption" value="true"/>
							</c:if>

							<c:if test="${not hasOption}">
								<span class="text-muted fs-sm">선택한 옵션 없음</span>
							</c:if>
						</dd>
					</div>

				</dl>
			</div>

			<%-- ================= 오른쪽 : 금액 + 예약자 정보 ================= --%>
			<div>
				<h3 class="fw-bold mb-4">결제 금액</h3>

				<dl class="detail-list mb-6">
					<div class="detail-row">
						<dt>차량 요금</dt>
						<dd><fmt:formatNumber value="${requestScope.totalreserve}" pattern="#,###"/>원</dd>
					</div>
					<div class="detail-row">
						<dt>옵션 요금</dt>
						<dd><fmt:formatNumber value="${requestScope.totaloption}" pattern="#,###"/>원</dd>
					</div>
				</dl>

				<h3 class="fw-bold mb-4">예약 확인용 정보</h3>

				<div class="form-group">
					<label class="form-label" for="memberphone">연락처 <span class="required">*</span></label>
					<input class="form-control" type="tel" id="memberphone" name="memberphone"
						   placeholder="010-1234-5678" required>
				</div>

				<div class="form-group">
					<label class="form-label" for="memberpass">예약 비밀번호 <span class="required">*</span></label>
					<input class="form-control" type="password" id="memberpass" name="memberpass"
						   placeholder="예약 조회·취소에 사용됩니다" required>
					<%-- 이 비밀번호의 용도를 반드시 알려준다. 모르면 나중에 예약을 못 찾는다. --%>
					<p class="form-hint">
						&#8226; 이 연락처와 비밀번호로 <strong>예약 조회 · 변경 · 취소</strong>를 합니다.<br>
						&#8226; 꼭 기억해 주세요. 분실 시 고객센터(02-3456-6574)로 문의해야 합니다.
					</p>
				</div>
			</div>

		</div>

		<%-- 총액 + 결제 버튼 : 모바일에서는 화면 하단에 고정된다 --%>
		<div class="action-bar mt-6">
			<div class="action-total">
				<div class="label">총 결제금액 (${requestScope.vo.carreserveday}일 · ${requestScope.vo.carqty}대)</div>
				<div class="price">
					<fmt:formatNumber value="${totalreserve + totaloption}" pattern="#,###"/>원
				</div>
			</div>
			<button type="submit" class="btn btn-primary btn-lg">예약 완료하기</button>
		</div>

		<div class="flex flex-wrap gap-2 mt-4">
			<a class="btn btn-ghost" href="<%=contextPath%>/Car/CarList.do">&lsaquo; 차량 다시 선택</a>
		</div>

	</form>
</div>