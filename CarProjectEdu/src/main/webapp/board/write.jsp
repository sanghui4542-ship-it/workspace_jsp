<%@page import="Vo.MemberVO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
	request.setCharacterEncoding("UTF-8");
	String contextPath = request.getContextPath();

	/*
	 nowPage, nowBlock 을 받는 이유

	   list.jsp 에서 페이지 번호(예: 2페이지, nowPage=1)를 보다가 글쓰기
	   버튼을 눌러 이 화면으로 넘어왔다. 글을 다 쓰고 [목록] 버튼을 누르면
	   방금 보고 있던 그 페이지로 돌아가야 자연스럽다. 그래서 그 번호를
	   여기서 받아 두었다가, 아래 [목록] 버튼의 이동 주소에 그대로 붙여 준다.
	*/
	String nowPage = (String)request.getAttribute("nowPage");
	String nowBlock = (String)request.getAttribute("nowBlock");

	//사장(BoardController)이 조회해 넘겨준 글 작성자(로그인한 나)의 정보
	MemberVO membervo = (MemberVO)request.getAttribute("membervo");
	String email = membervo.getEmail();
	String name = membervo.getName();
	String id = membervo.getId();
%>

<%--
 ============================================================================
  board/write.jsp  -  자유게시판 글쓰기 화면 (로그인한 회원만 볼 수 있다)

  CarMain.jsp 의 가운데 영역에 끼워져(include) 보이는 화면 조각이다.
    요청 흐름 : list.jsp 새 글쓰기 버튼 -> /Board/write.bo -> 사장(BoardController)
               -> 로그인한 회원 정보(membervo)를 request 에 담아 -> 이 화면	
 ============================================================================
--%>
<div class="board-container">

	<h2 class="section-title">자유게시판</h2>

	<form id="writeForm" onsubmit="return false;">

		<%-- 작성자 / 아이디 : 로그인 정보라 수정할 수 없다 (readonly) --%>
		<div class="grid-2">

			<div class="form-group">
				<label class="form-label" for="writer">작성자</label>
				<input class="form-control" type="text" id="writer" name="writer"
					   value="<%=util.HtmlUtil.escape(name)%>" readonly>
			</div>

			<div class="form-group">
				<label class="form-label" for="writer_id">아이디</label>
				<input class="form-control" type="text" id="writer_id" name="writer_id"
					   value="<%=util.HtmlUtil.escape(id)%>" readonly>
			</div>

		</div>

		<div class="form-group">
			<label class="form-label" for="email">메일주소</label>
			<input class="form-control" type="email" id="email" name="email"
				   value="<%=util.HtmlUtil.escape(email)%>" readonly>
		</div>

		<div class="form-group">
			<label class="form-label" for="title">제목 <span class="required">*</span></label>
			<input class="form-control" type="text" id="title" name="title"
				   maxlength="200" placeholder="제목을 입력하세요">
		</div>

		<div class="form-group">
			<label class="form-label" for="content">내용</label>
			<%-- rows/cols 대신 CSS 로 높이를 정한다 (cols 는 글자수 기준이라 반응형에 맞지 않는다) --%>
			<textarea class="form-control" id="content" name="content"
					  placeholder="내용을 입력하세요"></textarea>
		</div>

		<div class="form-group">
			<label class="form-label" for="pass">비밀번호 <span class="required">*</span></label>
			<input class="form-control" type="password" id="pass" name="pass"
				   placeholder="글 수정·삭제에 사용됩니다">
			<p class="form-hint">&#8226; 나중에 이 글을 수정하거나 삭제할 때 필요합니다. 꼭 기억해 주세요.</p>
		</div>

	</form>

	<%-- 버튼 : 좁은 화면에서는 자동으로 줄바꿈된다 --%>
	<div class="flex flex-wrap gap-2 justify-between mt-6">
		<button type="button" id="registration1" class="btn btn-primary">등록</button>
		<button type="button" id="list" class="btn btn-ghost">목록</button>
	</div>

	<%-- 등록 결과 메시지가 표시되는 자리 (자바스크립트가 채운다) --%>
	<p id="resultInsert" class="mt-4 fw-bold"></p>

</div>
<%-- board-container 끝 --%>

<script type="text/javascript">

	// JSP 가 서버에서 만든 프로젝트 경로를 자바스크립트 변수로 넘겨받는다  예) "/CarProject"
	var ctx = "<%=contextPath%>";

	/* ----- [목록] 버튼 : 보고 있던 페이지로 돌아간다 ----- */
	document.getElementById("list").addEventListener("click", function(event){

		event.preventDefault();   // 버튼의 기본 동작을 막는다

		// nowPage, nowBlock 은 이 화면에 처음 들어올 때 list.jsp 에서 넘겨받은 값이다 (위 스크립틀릿 참고)
		location.href = ctx + "/Board/list.bo?nowPage=<%=nowPage%>&nowBlock=<%=nowBlock%>";   // 보던 페이지 번호를 그대로 달고 목록으로 돌아간다
	});

	/* ----- [등록] 버튼 : 입력값을 검사한 뒤 서버에 새 글 등록을 요청한다 ----- */
	document.getElementById("registration1").addEventListener("click", function(event){

		event.preventDefault();   // 버튼의 기본 동작(폼 전송)을 막는다. 아래에서 우리가 직접 보낼 것이다

		//폼 안의 입력값을 name 속성으로 찾아 가져온다
		var form = document.querySelector("form");

		var writer  = form.querySelector("input[name=writer]").value;     //작성자 명
		var id      = form.querySelector("input[name=writer_id]").value;  //작성자 아이디
		var email   = form.querySelector("input[name=email]").value;      //작성자 이메일
		var title   = form.querySelector("input[name=title]").value;      //글 제목
		var content = form.querySelector("textarea[name=content]").value; //글 내용
		var pass    = form.querySelector("input[name=pass]").value;       //글 비밀번호

		//화면에서도 최소 검증 (서버에서도 다시 검증한다 - BoardService.serviceInsertBoard 참고)
		if(title.trim() === ""){
			CarApp.setText("resultInsert", "글 제목을 입력해주세요.", "red");
			return;   // 여기서 끝낸다 (서버로 보내지 않는다)
		}
		if(pass.trim() === ""){   // 글 비밀번호가 비어 있으면
			CarApp.setText("resultInsert", "글 비밀번호를 입력해주세요.", "red");   // 빨간 글씨로 안내한다
			return;   // 여기서 끝낸다
		}

		/* 서버로 전송
		   파라미터 이름이 w / i / e / t / c / p 로 짧은 이유는 BoardController 가
		   그 이름으로 request.getParameter() 를 읽도록 정해져 있기 때문이다. */
		CarApp.postForm(ctx + "/Board/writePro.bo", {
			w : writer,   // w = writer(작성자 명)
			i : id,       // i = id(작성자 아이디)
			e : email,    // e = email(이메일)
			t : title,    // t = title(제목)
			c : content,  // c = content(내용)
			p : pass      // p = pass(비밀번호)
		}).then(function(responseData){

			//서버는 "1"(성공) 또는 "0"(실패) 을 보낸다
			if(responseData === "1"){

				CarApp.setText("resultInsert", "글작성 완료!", "green");

				if(window.confirm("추가한 글을 조회해서 보기 위해 목록페이지로 이동하시겠습니까?")){   // 확인창에서 "확인" 을 누르면
					location.href = ctx + "/Board/list.bo";   // 글 목록으로 이동한다
				}

			}else{
				CarApp.setText("resultInsert", "글작성 실패", "red");   // 서버가 실패를 알린 경우 빨간 글씨로 안내한다
			}

		}).catch(function(err){   // 통신 자체가 실패한 경우
			CarApp.setText("resultInsert", "글작성 실패 : " + err.message, "red");   // 실패 이유를 함께 보여 준다
		});
	});
</script>
