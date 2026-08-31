<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%--
   ================================================================
   withdraw.jsp : 회원탈퇴 확인 화면   [MVC : View]

   탈퇴는 되돌릴 수 없으므로 두 가지 안전장치를 둔다.
     1. 비밀번호를 다시 입력받아 본인인지 확인한다
     2. 전송 직전에 한 번 더 물어본다 (확인 창)
   ================================================================
--%>
<%
	// 탈퇴 실패 시 컨트롤러가 담아 준 안내 문구를 꺼낸다 (없으면 null)
	String withdrawMsg = (String) request.getAttribute("withdrawMsg");

	// 문구가 있을 때만 경고창을 띄운다
	if (withdrawMsg != null) {
%>
	<script> alert("<%= withdrawMsg %>"); </script>
<%
	}//if
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>회원탈퇴</title>
<link href="../css/default.css" rel="stylesheet" type="text/css">
<link href="../css/subpage.css" rel="stylesheet" type="text/css">
<script type="text/javascript">
	// [최종 확인] 전송 버튼을 눌렀을 때 실행되는 함수
	function checkWithdraw() {

		// 비밀번호를 입력했는지 확인한다
		if (document.frmWithdraw.pwd.value == "") {
			alert("비밀번호를 입력해 주세요.");
			return false;
		}

		// confirm : [확인]과 [취소]가 있는 창을 띄운다.
		// [확인]을 누르면 true, [취소]를 누르면 false 가 된다.
		return confirm("정말 탈퇴하시겠습니까?\n탈퇴하면 회원 정보가 삭제되며 되돌릴 수 없습니다.");
	}
</script>
</head>
<body>
	<div id="wrap">
		<!-- 헤더들어가는 곳 -->
		<%@ include file="../inc/top.jsp" %>
		<!-- 헤더들어가는 곳 -->

		<!-- 본문들어가는 곳 -->
		<div id="sub_img_member"></div>

		<nav id="sub_menu">
			<ul>
				<li><a href="modifyForm.do">회원정보 수정</a></li>
				<li><a href="withdrawForm.do">회원탈퇴</a></li>
			</ul>
		</nav>

		<article>
			<h1>회원탈퇴</h1>

			<h3>
				탈퇴하시면 회원 정보가 모두 삭제되며 되돌릴 수 없습니다.<br>
				계속하시려면 비밀번호를 입력해 주세요.
			</h3>

			<%-- 폼의 목적지는 컨트롤러의 탈퇴 처리 기능이다 --%>
			<form action="withdraw.do" method="post" name="frmWithdraw" id="join"
			      onsubmit="return checkWithdraw();">

				<fieldset>
					<legend>본인 확인</legend>

					<label>Password</label>
					<input type="password" name="pwd">
					<%-- 탈퇴할 아이디는 폼으로 보내지 않는다.
					     컨트롤러가 세션에서 가져오므로 본인 것만 삭제된다. --%>
					<br>
				</fieldset>

				<div class="clear"></div>
				<div id="buttons">
					<input type="submit" value="탈퇴하기" class="submit">
					<input type="button" value="취소" class="cancel"
					       onclick="location.href='../index.jsp';">
				</div>
			</form>
		</article>
		<!-- 본문들어가는 곳 -->

		<div class="clear"></div>
		<%@ include file="../inc/bottom.jsp" %>
	</div>
</body>
</html>
