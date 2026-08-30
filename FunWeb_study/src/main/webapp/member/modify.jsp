<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="member.MemberVO"%>
<%--
   ================================================================
   modify.jsp : 회원정보 수정 화면   [MVC : View]

   [주의] 이 파일을 주소창에 직접 입력해 열면 안 된다.
   반드시 컨트롤러를 거쳐야 한다 -> member/modifyForm.do

   이유 : 입력칸에 기존 정보를 미리 채워 넣으려면
          컨트롤러가 DB 에서 조회해 request 에 담아 줘야 하기 때문이다.
          직접 열면 아래 member 가 null 이라 화면이 비어 버린다.
   ================================================================
--%>
<%
	// 컨트롤러가 request 에 담아 준 회원 정보 상자를 꺼낸다
	MemberVO member = (MemberVO) request.getAttribute("member");

	// 수정 실패 시 컨트롤러가 담아 준 안내 문구를 꺼낸다 (없으면 null)
	String modifyMsg = (String) request.getAttribute("modifyMsg");

	// 문구가 있을 때만 경고창을 띄운다
	if (modifyMsg != null) {
%>
	<script> alert("<%= modifyMsg %>"); </script>
<%
	}//if
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>회원정보 수정</title>
<link href="../css/default.css" rel="stylesheet" type="text/css">
<link href="../css/subpage.css" rel="stylesheet" type="text/css">
<script type="text/javascript">
	// [입력 검사] 전송 버튼을 눌렀을 때 실행되는 함수
	function checkModify() {

		// 입력칸의 값을 가져온다
		var pwd  = document.frmModify.pwd.value;
		var pwd2 = document.frmModify.pwd2.value;
		var name = document.frmModify.name.value;

		// 비밀번호를 비워 두면 전송하지 않는다
		if (pwd == "") {
			alert("비밀번호를 입력해 주세요.");
			return false;   // false 를 반환하면 전송이 취소된다
		}

		// 두 번 입력한 비밀번호가 서로 다르면 전송하지 않는다
		if (pwd != pwd2) {
			alert("비밀번호가 서로 일치하지 않습니다.");
			return false;
		}

		// 이름을 비워 두면 전송하지 않는다
		if (name == "") {
			alert("이름을 입력해 주세요.");
			return false;
		}

		// 이메일을 비워 두면 전송하지 않는다
		var email = document.frmModify.email.value;
		if (email == "") {
			alert("이메일을 입력해 주세요.");
			return false;
		}

		// 모든 검사를 통과하면 true 를 반환해 전송을 진행시킨다
		return true;
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
			<h1>회원정보 수정</h1>

			<%-- 폼의 목적지는 컨트롤러의 수정 처리 기능이다 --%>
			<form action="modify.do" method="post" name="frmModify" id="join"
			      onsubmit="return checkModify();">
			<%-- onsubmit : 전송 직전에 위 자바스크립트 함수를 실행한다.
			     false 가 반환되면 전송이 취소된다. --%>

				<fieldset>
					<legend>회원 정보</legend>

					<label>User ID</label>
					<input type="text" value="<%= member.getId() %>" readonly>
					<%-- 아이디는 바꿀 수 없으므로 readonly 로 보여주기만 한다.
					     name 속성이 없으므로 서버로 전송되지도 않는다.
					     -> 컨트롤러는 세션의 아이디를 사용한다 (본인 것만 수정) --%>
					<br>

					<label>Password</label>
					<input type="password" name="pwd" value="<%= member.getPwd() %>">
					<br>

					<label>Retype Password</label>
					<input type="password" name="pwd2" value="<%= member.getPwd() %>">
					<%-- 두 칸에 기존 비밀번호를 미리 채워 두었다.
					     그대로 두면 비밀번호는 바뀌지 않는다. --%>
					<br>

					<label>Name</label>
					<input type="text" name="name" value="<%= member.getName() %>">
					<br>

					<label>E-Mail</label>
					<input type="email" name="email" value="<%= member.getEmail() %>">
					<%-- 이메일도 수정할 수 있게 칸을 추가했다.
					     아이디/비밀번호 찾기의 본인 확인 수단이므로
					     바뀌면 반드시 갱신해 두어야 한다. --%>
					<%-- value 에 기존 이름을 넣어 두어 수정하기 편하게 했다.
					     이 값들은 컨트롤러가 DB 에서 조회해 담아 준 것이다. --%>
					<br>
				</fieldset>

				<div class="clear"></div>
				<div id="buttons">
					<input type="submit" value="수정하기" class="submit">
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
