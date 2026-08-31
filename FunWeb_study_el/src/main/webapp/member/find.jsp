<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%--
   ================================================================
   find.jsp : 아이디 찾기 / 비밀번호 찾기 공용 화면   [MVC : View]

   [화면 하나로 두 기능을 처리하는 방법]
   컨트롤러가 request 에 담아 준 "mode" 값으로 화면을 나눈다.
     mode 가 "id"  -> 아이디 찾기 화면 (이름만 입력)
     mode 가 "pwd" -> 비밀번호 찾기 화면 (아이디 + 이름 입력)

   ** 주소창에 직접 입력해 열면 mode 가 없어 화면이 어긋난다.
      반드시 컨트롤러를 거쳐야 한다.
        member/findIdForm.do   또는   member/findPwdForm.do  **
   ================================================================
--%>
<%
	// 컨트롤러가 담아 준 화면 구분 값을 꺼낸다
	String mode = (String) request.getAttribute("mode");

	// 값이 없으면(직접 열었을 때) 아이디 찾기 화면을 기본으로 보여준다
	if (mode == null) {
		mode = "id";
	}

	// 찾기 결과 문구를 꺼낸다. 처음 화면에서는 아직 없으므로 null 이다
	String resultMsg = (String) request.getAttribute("resultMsg");

	// 화면에 표시할 제목과 폼 목적지를 mode 에 따라 정한다
	String title  = mode.equals("id") ? "아이디 찾기" : "비밀번호 찾기";
	String action = mode.equals("id") ? "findId.do"  : "findPwd.do";

	// 화면에 보여줄 안내 문구도 mode 에 따라 정한다
	String guide  = mode.equals("id")
	              ? "가입할 때 입력한 이름과 이메일을 입력해 주세요."
	              : "아이디와 가입할 때 입력한 이메일을 입력해 주세요.";
	/* 삼항 연산자 : 조건 ? 참일때값 : 거짓일때값
	   if-else 를 한 줄로 줄여 쓴 것이다. */
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title><%= title %></title>
<link href="../css/default.css" rel="stylesheet" type="text/css">
<link href="../css/subpage.css" rel="stylesheet" type="text/css">
<script type="text/javascript">
	// [입력 검사] 전송 버튼을 눌렀을 때 실행되는 함수
	function checkFind() {

<%
	// 비밀번호 찾기 화면일 때만 "아이디" 검사 코드를 넣는다
	if (mode.equals("pwd")) {
%>
		if (document.frmFind.id.value == "") {
			alert("아이디를 입력해 주세요.");
			return false;   // false 를 반환하면 전송이 취소된다
		}
<%
	}//if
%>
		// 이름은 두 기능 모두 반드시 입력해야 한다
		if (document.frmFind.name.value == "") {
			alert("이름을 입력해 주세요.");
			return false;
		}

		// 이메일은 두 기능 모두 반드시 입력해야 한다
		if (document.frmFind.email.value == "") {
			alert("가입할 때 입력한 이메일을 입력해 주세요.");
			return false;
		}

		// 이메일 형식인지 간단히 확인한다 (골뱅이와 점이 있는지)
		var email = document.frmFind.email.value;
		if (email.indexOf("@") < 0 || email.indexOf(".") < 0) {
			alert("이메일 형식이 올바르지 않습니다.");
			return false;
		}
		/* indexOf : 찾는 글자의 위치를 알려준다.
		   없으면 -1 을 돌려주므로 0보다 작으면 없는 것이다. */

		// 모든 검사를 통과하면 전송을 진행시킨다
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
				<li><a href="findIdForm.do">아이디 찾기</a></li>
				<li><a href="findPwdForm.do">비밀번호 찾기</a></li>
				<li><a href="login.jsp">로그인</a></li>
			</ul>
		</nav>

		<article>
			<h1><%= title %></h1>

<%
	// 찾기 결과가 있을 때만 결과 상자를 보여준다
	if (resultMsg != null) {
%>
			<h3 style="border:1px solid #ccc; padding:12px; background:#f7f7f7;">
				<%= resultMsg %>
			</h3>
			<%-- 컨트롤러가 담아 준 결과 문구를 그대로 출력한다.
			     찾았으면 아이디나 비밀번호가, 못 찾았으면 안내 문구가 나온다. --%>
<%
	}//if
%>

			<h3><%= guide %></h3>

			<%-- 폼의 목적지는 mode 에 따라 달라진다 (위에서 정한 action 변수) --%>
			<form action="<%= action %>" method="post" name="frmFind" id="join"
			      onsubmit="return checkFind();">

				<fieldset>
					<legend>본인 확인</legend>

<%
	// [비밀번호 찾기] 일 때만 아이디 칸을 추가로 보여준다
	if (mode.equals("pwd")) {
%>
					<label>User ID</label>
					<input type="text" name="id">
					<br>
<%
	}//if
%>
					<label>Name</label>
					<input type="text" name="name">
					<br>
					<%-- 이름은 두 기능 공통 입력 항목이다.
					     아이디 찾기 : 이름 + 이메일   (2개)
					     비밀번호 찾기 : 아이디 + 이름 + 이메일 (3개) --%>
					<label>E-Mail</label>
					<input type="email" name="email">
					<br>
					<%-- 이메일은 두 기능 공통으로 입력받는 본인 확인 항목이다.
					     type="email" 로 지정하면 브라우저가 형식을 한 번 걸러 준다. --%>
				</fieldset>

				<div class="clear"></div>
				<div id="buttons">
					<input type="submit" value="찾기" class="submit">
					<input type="button" value="로그인 화면으로" class="cancel"
					       onclick="location.href='login.jsp';">
				</div>
			</form>

			<%--
			   [학습 참고]
			   t_member 에 email 열을 추가하여 본인 확인을 강화했다.
			     아이디 찾기   : 이름 + 이메일 이 모두 맞아야 알려준다.
			     비밀번호 찾기 : 아이디 + 이메일 이 모두 맞아야 알려준다.
			   -> 이름만 알던 사람은 더 이상 조회할 수 없다.

			   [실무는 여기서 한 걸음 더 나아간다]
			     1. 결과를 화면에 보여주지 않고 입력한 이메일로 발송한다.
			        -> 이메일함을 열 수 있는 본인만 확인할 수 있다.
			     2. 비밀번호는 알려주지 않고 새로 정하게 한다.
			        -> 비밀번호는 알아볼 수 없는 형태로 저장되어
			           꺼내서 보여주는 것 자체가 불가능하기 때문이다.
			--%>
		</article>
		<!-- 본문들어가는 곳 -->

		<div class="clear"></div>
		<%@ include file="../inc/bottom.jsp" %>
	</div>
</body>
</html>
