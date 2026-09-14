<%@page import="Vo.MemberVO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%
	// 요청 글자 방식을 UTF-8 로 정한다
	request.setCharacterEncoding("UTF-8");

	// 프로젝트 경로 얻기  예) "/CarProject"  -> 아래 폼과 목록 링크의 주소를 만들 때 사용
	String contextPath = request.getContextPath();

	/*
	 부모글 번호(b_idx) 받기

	   답글은 반드시 "어떤 글에 대한 답변인지" 알아야 한다. 그래서 사장
	   (BoardController)이 원글의 글번호를 request.setAttribute("b_idx", ...)
	   로 담아 보내 준 것을 여기서 꺼낸다. 이 값은 아래 폼의 hidden 입력칸에
	   담겨 답글 등록 요청(/Board/replyPro.do)과 함께 서버로 다시 전달된다.
	*/
	String b_idx = (String)request.getAttribute("b_idx");

	// 사장(BoardController)이 조회해 넘겨준 로그인한 회원(답글 작성자)의 정보
	MemberVO vo = (MemberVO)request.getAttribute("vo");

	// 세션에 저장된 로그인 아이디 확인 (로그인 성공 시 session.setAttribute("id", 아이디) 되어 있다)
	String id = (String)session.getAttribute("id");

	if(id == null){ // 로그인하지 않은 상태라면
%>
	<script>
		alert("로그인을 하셔야 작성하실수 있습니다.");   // 로그인해야 답글을 쓸 수 있다고 알린다
		history.back(); // 이전 페이지로 이동
	</script>
<%
		/* [버그 수정] return; 이 없어서 경고창만 띄우고 아래 폼이 계속 만들어졌다.
		   board/read.jsp 에서 고쳤던 것과 똑같은 문제다.
		   경고창은 자바스크립트를 끄면 뜨지도 않으므로
		   "응답 생성을 여기서 멈추는 것" 이 진짜 차단이다. */
		return;
	}

	/* [버그 수정] 작성자 이름·이메일을 담을 변수가 아예 없었다.

	     아래 화면에서 name / email 을 쓰고 있는데 그 변수를 어디에서도
	     선언하지 않았다. JSP 는 컴파일 검사를 받지 않으므로 서버는 조용히
	     떴고, "답변달기" 를 누른 순간에만 500 에러가 났다.
	         JSP 파일 [/board/reply.jsp]의 [107]행에서 오류가 발생했습니다
	         JSP 파일 [/board/reply.jsp]의 [113]행에서 오류가 발생했습니다
	     화면을 다시 만들 때 변수 선언을 함께 옮기지 않아 생긴 누락이었다.

	   vo(회원정보)는 컨트롤러가 담아준다. 다만 로그인 아이디가 member 테이블에
	   없으면 null 일 수 있으므로(아이디만 세션에 있는 예외 상황) 빈 문자열로 방어한다.
	*/
	String name  = (vo == null || vo.getName()  == null) ? "" : vo.getName();
	String email = (vo == null || vo.getEmail() == null) ? "" : vo.getEmail();
%>

<%--
 ============================================================================
  board/reply.jsp  -  자유게시판 답글 작성 화면 (로그인한 회원만 볼 수 있다)

  CarMain.jsp 의 가운데 영역에 끼워져(include) 보이는 화면 조각이다.
    요청 흐름 : read.jsp 답변 버튼 -> /Board/reply.do -> 사장(BoardController)
               -> 원글 번호(b_idx)와 로그인한 회원 정보(vo)를 request 에 담아 -> 이 화면

  이 폼은 write.jsp 와 달리 CarApp.postForm(AJAX) 대신 일반 form 전송을
  그대로 쓴다. onsubmit="return check();" 가 먼저 실행되어 빈 칸이 있으면
  전송을 막고, 통과하면 브라우저가 /Board/replyPro.do 로 직접 이동(POST)한다.
 ============================================================================
--%>
	<form action="<%=contextPath%>/Board/replyPro.do"
	      method="post"
	      onsubmit="return check();">

		<%-- 부모글 번호 : 답글이 어떤 글에 속하는지 DB 에 저장하기 위해 hidden 으로 함께 전송 --%>
		<input type="hidden"
		       name="super_b_idx"
		       value="<%=b_idx%>">

		<%-- 작성자 아이디 : 로그인한 사용자의 아이디를 DB 에 저장하기 위해 hidden 으로 함께 전송 --%>
		<input type="hidden"
		       name="id"
		       value="<%=id%>">

	<div class="board-container">

		<h2 class="section-title">답변글 작성</h2>

		<%-- 작성자 / 이메일 : 로그인 정보라 수정할 수 없다 (readonly) --%>
		<div class="grid-2">

			<div class="form-group">
				<label class="form-label" for="writer">작성자</label>
				<input class="form-control" type="text" name="writer" id="writer"
					   value="<%=util.HtmlUtil.escape(name)%>" readonly>
			</div>

			<div class="form-group">
				<label class="form-label" for="email">이메일</label>
				<input class="form-control" type="email" name="email" id="email"
					   value="<%=util.HtmlUtil.escape(email)%>" readonly>
			</div>

		</div>

		<div class="form-group">
			<label class="form-label" for="title">제목 <span class="required">*</span></label>
			<input class="form-control" type="text" name="title" id="title"
				   maxlength="200" placeholder="답변 제목을 입력하세요">
		</div>

		<div class="form-group">
			<label class="form-label" for="content">내용 <span class="required">*</span></label>
			<textarea class="form-control" name="content" id="content"
					  placeholder="답변 내용을 입력하세요"></textarea>
		</div>

		<div class="form-group">
			<label class="form-label" for="pass">비밀번호 <span class="required">*</span></label>
			<input class="form-control" type="password" name="pass" id="pass"
				   placeholder="답변글 수정·삭제에 사용됩니다">
			<%-- 검증 실패 메시지가 표시되는 자리 --%>
			<p id="pwInput" class="form-hint"></p>
		</div>

		<div class="flex flex-wrap gap-2 justify-between mt-6">
			<button type="submit" id="reply" class="btn btn-primary">등록</button>
			<button type="button" class="btn btn-ghost"
					onclick="location.href='<%=contextPath%>/Board/list.bo?nowBlock=0&nowPage=0'">
				목록
			</button>
		</div>

	</div>
	<%-- board-container 끝 --%>
	</form>

	<script>
		// 폼이 전송되기 직전(onsubmit)에 실행되는 검증 함수
		// false 를 돌려주면 전송이 취소되고, true 를 돌려주면 그대로 전송된다
		function check(){

			var writer = document.querySelector("input[name='writer']").value;   // 입력한 작성자 이름
			var email = document.querySelector("input[name='email']").value;   // 입력한 이메일
			var title = document.querySelector("input[name='title']").value;   // 입력한 답글 제목
			var content = document.querySelector("textarea[name='content']").value;   // 입력한 답글 내용
			var pass = document.querySelector("input[name='pass']").value;   // 입력한 글 비밀번호

			// 다섯 칸 중 하나라도 비어 있으면 안내하고 전송을 막는다
			if(writer == "" || email == "" ||
			   title == "" || content == "" || pass == ""){

				CarApp.setText("pwInput", "모두 작성해 주세요.", "red");   //js/app.js 의 공용 도우미

				return false; // 전송 차단
			}

			return true; // 전송 허용
		}
	</script>
