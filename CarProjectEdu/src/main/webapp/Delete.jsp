<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
	request.setCharacterEncoding("UTF-8");
	String contextPath = request.getContextPath();
%>
<%--
 ================================================================================
   Delete.jsp  -  예약 취소 (비밀번호 재확인)

       - 어떤 예약을 취소하는지 예약번호와 차량명을 먼저 보여준다
       - "되돌릴 수 없다"는 경고를 명확히 표시
       - 실수로 누르는 것을 막기 위해 확인 창을 한 번 띄운다
 ================================================================================
--%>

<div class="container">

	<div class="section-head">
		<span class="section-eyebrow">CANCEL</span>
		<h2 class="section-heading">예약 취소</h2>
	</div>

	<%-- 입력 폼은 좁은 폭으로 가운데 정렬 --%>
	<div style="max-width:480px; margin:0 auto;">

		<%-- 취소는 되돌릴 수 없으므로 경고를 먼저 보여준다 --%>
		<div class="alert alert-error">
			<strong>취소한 예약은 되돌릴 수 없습니다.</strong><br>
			<span class="fs-sm">다시 예약하려면 처음부터 진행해야 합니다.</span>
		</div>

		<%-- 어떤 예약을 취소하는지 확인시켜 준다 --%>
		<dl class="detail-list mb-6">
			<div class="detail-row">
				<dt>예약번호</dt>
				<dd><c:out value="${param.orderid}"/></dd>
			</div>
			<c:if test="${not empty param.carname}">
				<div class="detail-row">
					<dt>차량</dt>
					<dd><c:out value="${param.carname}"/></dd>
				</div>
			</c:if>
			<div class="detail-row">
				<dt>연락처</dt>
				<dd><c:out value="${param.memberphone}"/></dd>
			</div>
		</dl>

		<form action="<%=contextPath%>/Car/deletePro.do" method="post"
			  onsubmit="return confirm('예약을 취소하시겠습니까?\n취소한 예약은 되돌릴 수 없습니다.');">

			<%-- [보안] CSRF 토큰 : 이 화면에서 출발한 요청임을 증명한다 (설명은 members/login.jsp) --%>
			<input type="hidden" name="_csrf" value="${_csrf}">

			<%-- 취소에 필요한 값 (주소창에 남지 않도록 hidden 으로 전달) --%>
			<input type="hidden" name="orderid"     value="${param.orderid}">
			<input type="hidden" name="memberphone" value="${param.memberphone}">

			<div class="form-group">
				<label class="form-label" for="memberpass">예약 비밀번호 <span class="required">*</span></label>
				<input class="form-control" type="password" id="memberpass" name="memberpass"
					   placeholder="예약 시 입력한 비밀번호" required autofocus>
				<p class="form-hint">&#8226; 본인 확인을 위해 필요합니다. 서버에서 다시 검증합니다.</p>
			</div>

			<button type="submit" class="btn btn-danger btn-block btn-lg mt-4">예약 취소하기</button>

		</form>

		<div class="text-center mt-4">
			<a class="btn btn-ghost" href="<%=contextPath%>/Car/cc?center=CarReserveConfirm.jsp">
				취소하지 않고 돌아가기
			</a>
		</div>

	</div>
</div>