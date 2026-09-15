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

  ----------------------------------------------------------------------------
  [이번 변경] 자바스크립트를 전부 jQuery 문법으로 바꿨다.

    (기존)  document.getElementById("pass").value
            CarApp.postForm(...)
    (지금)  $("#pass").val()
            $.ajax({ ... })

    ★ jQuery 란?
      "복잡한 자바스크립트를 짧게 쓰게 해 주는 도구상자" 다.
      2006년에 나와 10년 넘게 웹의 표준처럼 쓰였고, 지금도 기존 프로젝트와
      각종 플러그인(달력, 슬라이드 등)에서 아주 많이 만나게 된다.
      → 비전공자 국비 과정에서는 "읽을 줄 아는 것"이 특히 중요하다.

    ★ 핵심 문법 딱 하나만 기억하면 된다
          $("선택자").기능()
        $  = jQuery 를 부르는 기호 (jQuery 라고 풀어 써도 똑같다)
        "" = CSS 선택자.  #아이디  .클래스  태그이름
        예) $("#pass")  = id 가 pass 인 요소를 잡아라

    ★ 솔직한 주의사항 (수업에서 꼭 짚을 것)
      이 프로젝트는 예전에 jQuery 를 '일부러 제거'했던 이력이 있다. 이유는 :
        1) CDN(외부 서버)에서 불러오면 인터넷이 없는 강의실에서 기능이 전부 멈춘다
           → 아래처럼 프로젝트 안(js 폴더)에 파일을 두고 불러오면 해결된다
        2) Top.jsp 가 jquery.slim 을 불러오는데, slim 버전에는 $.ajax 가 없다
           → 여기서 정식 버전을 또 불러오면 jQuery 가 두 번 로드되어
             먼저 등록한 이벤트가 사라지는 사고가 날 수 있다
           ★ 반드시 Top.jsp 의 jquery.slim 로드 줄을 지우거나 정식 버전으로 바꿔야 한다.
 ============================================================================
--%>
<%
	request.setCharacterEncoding("UTF-8");
	String contextPath = request.getContextPath();


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

	if(vo == null){
%>
		<script>
			alert("존재하지 않는 게시글입니다.");   // 없는 글이라고 알린다
			location.href = "<%=contextPath%>/Board/list.bo";   // 글 목록으로 되돌려 보낸다
		</script>
<%
		return;
	}


	String name    = HtmlUtil.escape(vo.getB_name());    //조회한 글을 작성한 사람
	String email   = HtmlUtil.escape(vo.getB_email());   //조회한 글을 작성한 사람의 이메일
	String title   = HtmlUtil.escape(vo.getB_title());   //조회한 글제목
	String content = HtmlUtil.escape(vo.getB_content()); //조회한 글 내용

	String b_idx = (String)request.getAttribute("b_idx");
	String nowPage = (String)request.getAttribute("nowPage");
	String nowBlock = (String)request.getAttribute("nowBlock");
%>

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
					<input class="form-control" type="password" name="pass" id="pass" placeholder="글 작성 시 입력한 비밀번호">

					<%-- 비밀번호 확인 결과와 수정/삭제 결과 메시지가 표시되는 자리 --%>
					<p id="pwInput" class="form-hint"></p>
				</dd>
			</div>

		</dl>

		<div class="flex flex-wrap gap-2 justify-between mt-6">

			<div class="flex flex-wrap gap-2">
				<button type="button" id="update" class="btn btn-primary" style="visibility:hidden;">
					수정
				</button>

				<%-- [변경] onclick 속성을 없앴다.
				     글번호는 아래 숨은 칸(#b_idx)에서 읽으면 되므로 굳이 HTML 에 박을 필요가 없다.
				     동작 연결은 아래 jQuery 의 $("#delete").on("click", ...) 가 담당한다.
				     ★ HTML(구조)과 JS(동작)를 분리하는 것이 유지보수에 좋다. --%>
				<button type="button" id="delete" class="btn btn-danger" style="visibility:hidden;">
					삭제
				</button>

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
	   그런데 아래 자바스크립트가 이 값을 읽는다.
	       - 글 수정      (updateBoard.do 의 idx)
	       - 글 삭제      (deleteBoard.do 의 b_idx)
	       - 비밀번호 확인 (password.do 의 b_idx)
	       - 댓글 전체     (commentList.do / commentAdd.do 의 b_idx)
	   폼만 지우고 이 칸을 남기지 않으면 위 기능이 전부 조용히 멈춘다.
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
	   jQuery 불러오기

	   ★ 반드시 '프로젝트 안의 파일' 을 쓴다 (CDN 금지)
	     src/main/webapp/js/ 폴더에 jquery-3.7.1.min.js 를 넣어 두고 아래처럼 부른다.
	     이렇게 하면 인터넷이 없는 강의실에서도 100% 동작한다.

	   [파일 구하는 법]
	     https://code.jquery.com/jquery-3.7.1.min.js 에 접속 →
	     Ctrl+S 로 저장 → 프로젝트의 js 폴더에 넣기 → 이클립스에서 새로고침(F5)

	   ★★ 매우 중요한 확인사항 ★★
	     Top.jsp 나 CarMain.jsp 에서 이미 jQuery(특히 slim 버전)를 부르고 있다면
	     지금 여기서 또 부르는 순간 jQuery 가 두 번 로드된다.
	     나중에 로드된 것이 앞의 것을 덮어써서, 먼저 등록해 둔 클릭 이벤트가
	     통째로 사라지는 사고가 난다.
	     → Top.jsp 의 jquery.slim 줄을 지우고, 정식 버전 한 곳에서만 부르게 할 것.
	       (slim 버전에는 $.ajax 가 아예 없어서 이 화면이 동작하지 않는다)
	 ================================================================================
	--%>
	
	<script src="https://code.jquery.com/jquery-latest.min.js"></script>

	<script>
	/* ████████████████████████████████████████████████████████████████████████████
	   board/read.jsp 의 자바스크립트 — jQuery 판 (비전공자용 완전 해설)

	   [ jQuery 기본 문법 5줄 요약 ]
	     $("#id")          : id 로 요소 잡기        (document.getElementById 와 같다)
	     $(".클래스")       : 클래스로 요소 잡기
	     .val()            : 입력값 읽기 / .val("값") 이면 쓰기
	     .text("글자")      : 글자 넣기 (태그 무시 = 안전)
	     .html("<b>글</b>") : HTML 넣기 (태그 살림 = 위험. 고정 문구에만)

	   [ jQuery 의 가장 큰 특징 : 체이닝(chaining) ]
	       $("#pwInput").text("성공").css("color", "green");
	     기능을 점(.)으로 계속 이어 붙일 수 있다.
	     각 기능이 '잡아 둔 요소'를 다시 돌려주기 때문이다.
	     ★ 순수 자바스크립트로는 el.textContent=...; el.style.color=...; 두 줄이 필요하다.

	   [ jQuery 의 두 번째 특징 : 없어도 안 죽는다 ]
	       $("#없는아이디").val()   → 오류 없이 undefined
	     순수 JS 는 null.value 로 스크립트 전체가 죽는다.
	     ★ 편하지만 위험하기도 하다. 오타를 내도 조용히 아무 일도 안 일어나
	       "왜 안 되지?" 하고 한참 헤매게 된다. 이게 jQuery 의 양날의 검이다.
	   ████████████████████████████████████████████████████████████████████████████ */


		/* ═══════════════════════════════════════════════════════════════════════
		   0. 전역 변수
		   ═══════════════════════════════════════════════════════════════════════ */

		var ctx = "<%=contextPath%>";
		/* ↑ ★ JSP 표현식이다. 자바스크립트가 자바 변수를 읽는 게 아니라,
		     서버가 HTML 을 만들 때 실제 값을 '글자로 박아서' 내려보낸다.
		     브라우저가 받는 최종 결과 :  var ctx = "/CarProject2";

		   [왜 필요한가] 주소를 "/Board/list.bo" 로 적으면 톰캣 루트 기준이 되어 404 가 난다.
		     실제 주소는 "/CarProject2/Board/list.bo" 이고, 프로젝트 이름은 배포 환경마다
		     달라질 수 있으므로 절대 직접 적으면 안 된다. */

		var EDIT_FIELDS = "#email, #title, #content";
		/* ↑ ★ jQuery 판의 묘미.
		     순수 JS 에서는 ["email","title","content"] 배열을 만들고 for 문으로 돌려야 했다.
		     jQuery 는 선택자에 쉼표를 쓰면 "여러 개를 한 번에" 잡는다.
		         $(EDIT_FIELDS).prop("disabled", true)   ← 이 한 줄로 3개가 전부 잠긴다
		     ★ 수정 칸이 하나 늘어도 이 문자열에 ", #새아이디" 만 추가하면 끝이다. */


		/* ═══════════════════════════════════════════════════════════════════════
		   1. 공용 도우미 (jQuery 판)
		   ═══════════════════════════════════════════════════════════════════════ */

		// ───── 안내 문구 띄우기 (글자 + 색을 한 번에) ─────
		function showMsg(text, color){
			$("#pwInput").text(text).css("color", color);
			/*            ▲          ▲       ▲
			              │          │       └ css("속성", "값") = 스타일을 직접 바꾼다
			              │          └ ★ text() 를 쓰는 이유 : 태그를 해석하지 않고 '글자로만' 넣는다.
			              │             = XSS 가 원천적으로 불가능하다.
			              │             (html() 은 태그를 살리므로 사용자 입력에 쓰면 위험하다)
			              └ ★ 체이닝. text() 가 다시 $("#pwInput") 을 돌려주므로 .css 를 이어 붙일 수 있다.

			   [개선 여지] 색을 직접 지정하는 대신 CSS 클래스를 붙였다 떼는 편이 더 좋다.
			       $("#pwInput").text(text).removeClass("ok err").addClass(isOk ? "ok" : "err");
			     그러면 디자인을 바꿀 때 JS 를 건드릴 필요가 없다. */
		}

		// ───── 안내 문구 띄우기 (태그를 살려야 할 때) ─────
		function showMsgHtml(html, color){
			$("#pwInput").html(html).css("color", color);
			/* ↑ ★ html() 은 위험한 기능이다. 반드시 지킬 것 :
			     "우리가 코드에 직접 적은 고정 문구" 에만 쓴다.
			     사용자 입력이나 서버에서 온 값을 여기에 그대로 넣으면 XSS 가 뚫린다.
			     이 파일에서는 <strong>수정성공</strong> 같은 우리 문구에만 쓴다. */
		}


		/* ═══════════════════════════════════════════════════════════════════════
		   2. ★ 서버와 통신하기 ($.ajax)
		   ═══════════════════════════════════════════════════════════════════════

		   [ AJAX 가 뭔가요? — 비전공자용 ]
		     "화면을 새로고침하지 않고 서버와 데이터만 주고받는" 기술.
		     음식 배달과 같다. 주문하고(요청) → 기다리는 동안 다른 일을 하고 → 도착하면(응답) 받는다.

		   [ jQuery 의 $.ajax 가 편한 이유 — fetch 와 비교 ]
		       fetch  : 404, 500 같은 실패 응답도 '성공'으로 처리한다. 직접 res.ok 를 검사해야 함
		       $.ajax : 404, 500 이면 알아서 error 쪽으로 보내준다  ★ 실수할 여지가 적다
		       fetch  : 값을 URLSearchParams 로 직접 조립해야 함
		       $.ajax : data 에 객체만 주면 알아서 "a=1&b=2" 로 만들고 한글도 인코딩해 준다
		     ★ 이것이 jQuery 가 오래 사랑받은 이유다. 귀찮은 뒤처리를 다 해 준다.

		   [ Promise 와 .done/.fail ]
		     $.ajax 는 결과를 바로 주지 않고 '약속' 을 돌려준다.
		       .done(함수)  → 성공하면 실행
		       .fail(함수)  → 실패하면 실행
		     ★ .then / .catch 와 사실상 같다. jQuery 전통 이름이 done/fail 이다.
		   ═══════════════════════════════════════════════════════════════════════ */

		/* ───── CSRF 토큰 찾기 ─────

		   [CSRF 공격이란? — 쉽게]
		     내가 우리 사이트에 로그인한 상태에서 공격자가 만든 엉뚱한 페이지를 연다.
		     그 페이지에 "내 글 삭제 요청"이 숨어 있으면, 브라우저는 내 로그인 쿠키를
		     자동으로 붙여 보내버린다. → 나는 아무것도 안 눌렀는데 내 글이 지워진다.

		   [막는 법] 서버가 발급한 '일회용 암호(토큰)'를 요청에 함께 보내게 한다.
		     공격자는 이 값을 알 수 없으므로 위조가 불가능해진다.

		   기존 CarApp.postForm 이 이걸 자동으로 붙여줬다.
		   $.ajax 로 바꾸면서 그 기능이 사라지므로 여기서 직접 찾아 붙인다.
		   ★ 페이지가 열릴 때 한 번만 찾아 두고 계속 재사용한다. */
		var CSRF = (function(){

			// (1순위) <meta name="csrf-token" content="값">
			var $meta = $('meta[name="csrf-token"]');
			if($meta.length && $meta.attr("content")){
			/*        ▲
			          └ ★ jQuery 는 못 찾아도 null 이 아니라 '빈 목록'을 돌려준다.
			             그래서 존재 확인은 .length 로 한다. (0이면 없는 것)
			             ★ if($meta) 라고 쓰면 빈 목록도 참이라서 항상 통과해 버린다. 흔한 함정! */
				return {
					name  : $meta.attr("data-param") || "csrfToken",   // attr() = HTML 속성 읽기
					value : $meta.attr("content")
				};
			}

			// (2순위) <input type="hidden" name="csrfToken" value="값">
			var $hidden = $('input[name="csrfToken"], input[name="_csrf"], input[name="csrf_token"]');
			// ↑ 쉼표로 여러 후보를 한 번에 찾는다. 프로젝트마다 토큰 이름이 달라서 넉넉히 적었다.
			if($hidden.length && $hidden.val()){
				return { name : $hidden.attr("name"), value : $hidden.val() };
			}

			return null;   // 못 찾으면 null (토큰을 안 쓰는 설정일 수도 있으므로 오류를 내지 않는다)
		})();
		/* ↑ ★ IIFE (즉시 실행 함수). 함수를 만들자마자 실행해 '결과값'만 변수에 담는 문법.
		     안에서 쓴 $meta, $hidden 같은 임시 변수가 바깥으로 새어나가지 않는다.
		   ★ 변수 이름 앞의 $ 는 문법이 아니라 '관례'다.
		     "이 변수에는 jQuery 로 잡은 요소가 들어 있다" 는 표시.
		     $meta 와 meta 를 구분해 두면 나중에 코드를 읽을 때 훨씬 편하다. */

		/**
		 * 서버에 POST 로 값을 보내고 응답을 받는다.
		 *   url  : 보낼 주소   예) ctx + "/Board/password.do"
		 *   data : 보낼 값들   예) { b_idx : "1", pass : "1234" }
		 *   반환 : jQuery 약속 객체 → .done(function(응답){ ... }) 로 받는다
		 */
		function postForm(url, data){

			// 보안 토큰이 있으면 보낼 값에 추가한다
			if(CSRF){
				data[CSRF.name] = CSRF.value;
				// ↑ 객체에 새 항목을 넣는 문법 : 객체["이름"] = 값
			}

			return $.ajax({
			/*     ▲
			       └ ★ return 을 꼭 붙여야 한다.
			          빼면 부르는 쪽에서 .done() 을 쓸 수 없어
			          "Cannot read property 'done' of undefined" 오류가 난다. */

				url      : url,
				type     : "POST",
				/* ↑ POST = 값을 '몸통'에 숨겨 보낸다 / GET = 주소창에 그대로 노출된다
				   ★ 비밀번호를 GET 으로 보내면 주소창·브라우저 기록·서버 로그에
				     평문으로 남는다. 절대 안 된다. */

				data     : data,
				/* ↑ ★ 여기가 $.ajax 의 최고 장점이다.
				     객체 { b_idx:"1", pass:"1234" } 를 주기만 하면
				     jQuery 가 알아서 "b_idx=1&pass=1234" 로 조립하고
				     한글·특수문자(&, = 등)도 안전하게 인코딩해 준다.
				   ★ 직접 문자열을 이어 붙이면 값에 & 가 들어갈 때 값이 쪼개져 사고가 난다. */

				dataType : "text",
				/* ↑ 응답을 '글자' 로 받겠다는 뜻.

				   [왜 json 이 아니라 text 인가?]
				     이 프로젝트는 응답이 두 종류로 섞여 있다.
				       글 기능   : "삭제성공" 같은 그냥 문자열
				       댓글 기능 : {"ok":true,...} 같은 JSON
				     dataType 을 "json" 으로 두면 앞쪽(문자열) 응답에서 파싱 오류가 난다.
				     그래서 전부 text 로 받고, 댓글 쪽에서만 JSON.parse 를 직접 호출한다. */

				timeout  : 15000
				/* ↑ 15초 안에 응답이 없으면 포기하고 실패 처리한다.
				   ★ 이게 없으면 서버가 멈췄을 때 사용자는 영원히 로딩만 보게 된다.
				     "언제 포기할지" 를 정해 두는 것은 통신 코드의 기본이다. */

			}).then(function(text){
				return (text == null) ? "" : String(text).trim();
				/* ↑ 앞뒤 공백·줄바꿈을 제거한다.
				   ★ 왜 필요한가 : JSP·서블릿 응답은 앞뒤에 빈 줄이 섞이는 일이 아주 잦다.
				     ("\n삭제성공\n" 이 오는 식)
				     trim 을 안 하면 data === "삭제성공" 비교가 조용히 false 가 되어
				     "서버는 성공이라 했는데 화면은 실패" 라는 유령 버그가 생긴다. */
			});
		}

		/* [참고] jQuery 에는 $.post(url, data, 성공함수) 라는 더 짧은 문법도 있다.
		     하지만 dataType·timeout 같은 세부 설정을 넣기 어려워 여기서는 $.ajax 를 썼다.
		     ★ 짧은 문법은 배우기 쉽고, 긴 문법은 통제하기 쉽다. 상황에 따라 고른다. */


		/* ═══════════════════════════════════════════════════════════════════════
		   3. $(document).ready — 모든 동작을 여기 안에서 연결한다
		   ═══════════════════════════════════════════════════════════════════════

		   [ 이게 뭔가요? ]
		     "HTML 을 다 읽고 나면 이 안의 코드를 실행해줘" 라고 예약하는 것.

		   [ 왜 필요한가 ]
		     $("#pass").on("click", ...) 을 실행하는 시점에 #pass 요소가 아직
		     만들어지지 않았다면, jQuery 는 '빈 목록'을 잡고 아무 일도 하지 않는다.
		     ★ 오류도 안 나서 "왜 버튼이 안 먹지?" 하고 한참 헤매게 된다.
		       jQuery 초보자가 가장 많이 겪는 문제 1위다.

		   [ 짧은 표기 ]
		     $(document).ready(function(){ ... })   ← 정식
		     $(function(){ ... })                   ← 똑같은 뜻의 줄임 (실무에서 더 많이 쓴다)
		     여기서는 배우는 입장을 고려해 정식 표기를 썼다.
		   ═══════════════════════════════════════════════════════════════════════ */
		$(document).ready(function(){

			/* ───────────────────────────────────────────────────────────
			   3-1. 본문 높이를 내용에 맞추기
			   ─────────────────────────────────────────────────────────── */
			fitContentHeight();                                  // 화면이 열릴 때 한 번
			$("#content").on("input", fitContentHeight);         // 수정 중 줄이 늘어날 때마다
			/*                ▲
			                  └ input 이벤트 = 글자를 하나 칠 때마다 발생.
			                     ★ change 와 다르다. change 는 '입력을 마치고 빠져나갈 때' 한 번만 발생. */


			/* ───────────────────────────────────────────────────────────
			   3-2. 글 삭제
			   ─────────────────────────────────────────────────────────── */
			$("#delete").on("click", function(){
			/*              ▲       ▲
			                │       └ 클릭되면 실행할 함수 (이름 없는 '익명 함수')
			                └ on("이벤트", 함수) = "이 사건이 일어나면 저 함수를 실행해줘"

			   ★ HTML 에 onclick="..." 을 쓰지 않고 이 방식을 쓰는 이유
			     ① HTML(구조)과 JS(동작)가 섞이지 않아 관리가 쉽다
			     ② 같은 요소에 여러 동작을 등록할 수 있다 (onclick 은 하나만 가능)
			     ③ 나중에 .off() 로 뗄 수도 있다 */

				//[확인]을 누르지 않으면 아무 일도 하지 않는다
				if(!window.confirm("정말로 글을 삭제하시겠어요?")){
					return;
				}
				/* ↑ confirm = [확인]/[취소] 창을 띄우고 true/false 를 돌려준다.
				     ! 를 붙였으므로 "확인을 누르지 않았으면" 여기서 끝낸다.
				   ★ 되돌릴 수 없는 동작 앞에는 반드시 확인 절차를 둔다. */

				/* [보안] 글 비밀번호를 함께 보낸다.
				   기존에는 글번호만 보냈고 서버도 검증하지 않아
				   /Board/deleteBoard.do?b_idx=1 주소만 알면 남의 글이 지워졌다.
				   ★ '화면에서 버튼을 숨겼으니 안전하다' 는 착각이 만든 사고다. */
				postForm(ctx + "/Board/deleteBoard.do", {
					b_idx : $("#b_idx").val(),   // 어느 글을 지울지
					pass  : $("#pass").val()
				})
				.done(function(data){
				/*     ▲
				       └ 서버가 보낸 응답 '글자'. 이 프로젝트는 "삭제성공" 같은
				          짧은 한글 문자열을 약속으로 쓰고 있다.
				       ★ 솔직한 지적 : 이 '문자열 약속' 방식은 오타 한 글자에 조용히 실패하고,
				          서버 문구를 바꾸면 화면이 같이 깨진다.
				          댓글 기능처럼 JSON { ok:true } 로 주고받는 편이 훨씬 안전하다. */

					if(data === "삭제성공"){

						alert("삭제성공");
						$(EDIT_FIELDS).prop("disabled", true);
						/* ↑ prop("disabled", true) = 입력칸을 잠근다(회색).
						   ★ attr() 이 아니라 prop() 을 쓴다.
						     attr 은 'HTML 에 적힌 글자', prop 은 '지금 실제 상태' 를 다룬다.
						     disabled, checked, selected 는 반드시 prop 을 써야 정상 동작한다.
						     (jQuery 1.6 이전 코드에는 attr 이 많은데, 지금은 틀린 방식이다) */

						//2초 뒤 [목록] 버튼을 눌러 글목록으로 이동시킨다
						setTimeout(function(){
							$("#list").trigger("click");
							/* ↑ ★ trigger("click") = 코드로 버튼을 '대신 눌러' 준다.
							     그 버튼의 onclick 에 목록 주소가 이미 들어 있으므로
							     주소를 여기에 또 적을 필요가 없다.
							     → 주소가 바뀌어도 버튼 한 곳만 고치면 된다. */
						}, 2000);
						// ↑ 2000밀리초 = 2초. 성공 메시지를 읽을 시간을 주는 배려다.

					}else if(data === "비밀번호틀림"){
						showMsg("글 비밀번호가 일치하지 않습니다.", "red");

					}else{
						showMsg("삭제실패!", "red");
						$(EDIT_FIELDS).prop("disabled", false);   // 다시 시도할 수 있게 열어 준다
					}
				})
				.fail(function(xhr, status){
				/*             ▲     ▲
				               │     └ 실패 종류 : "timeout"(시간초과) / "error"(서버오류) / "abort"(취소)
				               └ 응답 전체가 담긴 객체. xhr.status 로 404, 500 같은 코드를 알 수 있다.

				   ★ 여기로 오는 경우 : 인터넷 끊김 / 서버 500 / 404 / 15초 초과
				     fetch 와 달리 $.ajax 는 404·500 을 알아서 여기로 보내준다. */
					alert("삭제 요청 실패 : " + (status === "timeout" ? "응답 시간 초과" : "HTTP " + xhr.status));
					/* ★ .fail 을 빼면 오류가 콘솔에만 조용히 찍히고, 사용자는
					     '눌렀는데 아무 일도 안 일어나는' 최악의 상황을 겪는다.
					     모든 통신에는 반드시 실패 처리를 붙인다. */
				});
			});


			/* ───────────────────────────────────────────────────────────
			   3-3. 글 수정
			   ─────────────────────────────────────────────────────────── */
			$("#update").on("click", function(){

				postForm(ctx + "/Board/updateBoard.do", {
					email   : $("#email").val(),
					title   : $("#title").val(),
					content : $("#content").val(),
					idx     : $("#b_idx").val(),
					/*        ▲
					          └ ★ 주의 : 파라미터 이름이 'idx' 다. 다른 곳은 전부 'b_idx' 인데 여기만 다르다.
					             서버(BoardController)가 그렇게 받고 있기 때문인데,
					             이런 불일치는 나중에 반드시 헷갈림을 만든다. (통일하는 게 좋다) */
					pass    : $("#pass").val()   // [보안] 서버가 수정 시점에 비밀번호를 다시 검증한다
				})
				.done(function(data){

					if(data === "수정성공"){
						showMsgHtml("<strong>수정성공</strong>", "green");
						// ↑ <strong> 태그를 살리려고 html 판을 썼다. 우리가 적은 고정 문구라 안전하다.
						$(EDIT_FIELDS).prop("disabled", true);   // 같은 내용을 또 보내지 않게 잠근다

					}else if(data === "비밀번호틀림"){
						showMsgHtml("<strong>글 비밀번호가 일치하지 않습니다.</strong>", "red");

					}else{
						showMsgHtml("<strong>수정 실패</strong>", "red");
						$(EDIT_FIELDS).prop("disabled", false);
					}
				})
				.fail(function(xhr, status){
					alert("수정 요청 실패 : " + (status === "timeout" ? "응답 시간 초과" : "HTTP " + xhr.status));
				});
			});


			/* ───────────────────────────────────────────────────────────
			   3-4. 글 비밀번호 확인

			   [중요] 이것은 '편의 기능' 이지 보안 장치가 아니다.
			     버튼을 숨겨도 주소로 직접 요청할 수 있기 때문에,
			     서버(BoardService)가 수정/삭제 시점에 비밀번호를 다시 검증한다.
			     ★ 화면에서 하는 검사 = 안내 / 진짜 보안 = 서버.
			   ─────────────────────────────────────────────────────────── */
			$("#pass").on("focusout", function(){
			/*              ▲
			                └ focusout = 입력칸에서 커서가 빠져나갈 때

			   ★ 왜 'input'(칠 때마다)이 아니라 focusout 인가?
			     input 으로 하면 "1","12","123","1234" 네 번 모두 서버를 부른다.
			     불필요한 요청 4배 + 빨간 경고 문구가 계속 깜빡인다.
			     → '다 입력하고 나갔을 때' 한 번만 확인하는 게 맞다.

			   ※ blur 와 거의 같지만 focusout 은 부모 요소로도 전달(버블링)된다는 차이가 있다. */

				var pass = $("#pass").val();

				//비밀번호를 입력하지 않았으면 서버를 부르지 않는다
				if(pass === ""){ return; }
				/* ↑ ★ 이 한 줄이 없으면, 칸을 클릭만 하고 지나가도
				     "비밀번호가 다릅니다" 가 떠서 사용자가 당황한다. */

				postForm(ctx + "/Board/password.do", {
					b_idx : $("#b_idx").val(),
					pass  : pass
				})
				.done(function(data){

					//서버는 "비밀번호 맞음" 또는 "비밀번호 틀림" 을 보낸다
					var ok = (data === "비밀번호 맞음");
					/* ↑ 결과를 true/false 하나로 정리해 둔다. 아래에서 세 번 쓰이므로
					     문자열 비교를 세 번 반복하는 것보다 의미가 분명하고 오타 위험도 없다.
					   ★ 주의 : "비밀번호 맞음" 에 띄어쓰기가 있다.
					     서버 문구와 한 글자라도 다르면 조용히 실패한다. (문자열 약속의 약점) */

					showMsg(ok ? "글의 비밀번호가 일치합니다." : "글의 비밀번호가 다릅니다",
							ok ? "green" : "red");
					// ↑ 삼항 연산자(조건 ? 참 : 거짓) 두 개로 문구와 색을 한 번에 결정한다
					
				  //$("#eamil, #title, #content").prop("disabled", !ok);
					$(EDIT_FIELDS).prop("disabled", !ok);
					/* ↑ ★ ! 는 '반대' 라는 뜻. 의미가 뒤집히는 자리라 실수가 잦다.
					     맞으면(ok=true)  → disabled=false → 입력칸이 열린다
					     틀리면(ok=false) → disabled=true  → 입력칸이 잠긴다 */

					$("#update, #delete").css("visibility", ok ? "visible" : "hidden");
					/* ↑ ★ display:none 이 아니라 visibility 를 쓰는 이유
					     display:none  → 요소가 '자리까지' 사라져 옆 버튼들이 확 움직인다
					     visibility    → 자리는 그대로 두고 투명하게만 만든다
					   비밀번호를 칠 때마다 버튼이 나타났다 사라지며 화면이 덜컹거리면
					   사용자는 불안해한다. 기능은 같아도 '느낌'이 다르다. 이게 UX 다. */
				})
				.fail(function(xhr, status){
					alert("비밀번호 확인 실패 : " + (status === "timeout" ? "응답 시간 초과" : "HTTP " + xhr.status));
				});
			});


			/* ███████████████████████████████████████████████████████████████████
			   3-5. 댓글 이벤트 연결 — ★ jQuery 의 진짜 강력한 기능 '이벤트 위임'
			   ███████████████████████████████████████████████████████████████████

			   [ 문제 상황 ]
			     댓글의 [답글][수정][삭제][추천] 버튼들은 자바스크립트가 '나중에' 만든다.
			     그런데 이벤트는 '지금 존재하는 요소' 에만 걸 수 있다.
			     → 화면이 열릴 때 $(".comment-btn").on("click", ...) 을 해봐야
			       그 시점에는 버튼이 하나도 없어서 아무 일도 일어나지 않는다.

			   [ 기존 해법 ] HTML 을 조립할 때 onclick="showReply(3)" 을 문자열로 박아 넣었다.
			     동작은 하지만, HTML 과 동작이 뒤섞이고 함수를 전역으로 열어둬야 한다.

			   [ jQuery 해법 : 이벤트 위임 ]
			       $("#commentList").on("click", ".comment-btn", 함수)
			                          ▲            ▲
			                          │            └ 실제로 눌린 대상 (나중에 생겨도 된다!)
			                          └ 이벤트를 감시하는 부모 (지금 존재하고 계속 살아 있다)

			     원리 : 클릭은 자식에서 부모로 '거품처럼 올라간다(버블링)'.
			            부모가 그 신호를 받아서 "방금 눌린 게 .comment-btn 인가?" 를 확인한다.
			            → 버튼이 나중에 만들어져도 문제없이 동작한다.

			   ★ 장점이 하나 더 있다.
			     댓글이 100개면 버튼이 400개다. 각각에 이벤트를 걸면 400개를 등록해야 한다.
			     위임을 쓰면 '단 1개' 만 등록하면 된다. 메모리와 속도에서 비교가 안 된다.
			   ███████████████████████████████████████████████████████████████████ */

			// ----- 추천 -----
			$("#commentList").on("click", ".js-like", function(){
				toggleLike($(this).data("idx"));
				/*         ▲      ▲       ▲
				           │      │       └ data("idx") = HTML 의 data-idx 속성값을 읽는다
				           │      │          <button data-idx="7"> 이면 7 을 돌려준다
				           │      │          ★ jQuery 는 숫자처럼 보이면 알아서 숫자로 바꿔 준다
				           │      └ $(this) = "방금 눌린 그 요소" 를 jQuery 로 감싼 것
				           │         ★ this 는 순수 DOM 요소라 .data() 를 못 쓴다. 반드시 $() 로 감싼다.
				           └ 이벤트 위임 덕분에 나중에 만들어진 버튼도 여기에 걸린다 */
			});

			// ----- 답글 입력칸 열기 -----
			$("#commentList").on("click", ".js-reply", function(){
				showReply($(this).data("idx"));
			});

			// ----- 수정 입력칸 열기 -----
			$("#commentList").on("click", ".js-edit", function(){
				showEdit($(this).data("idx"));
			});

			// ----- 댓글 삭제 -----
			$("#commentList").on("click", ".js-remove", function(){
				removeComment($(this).data("idx"));
			});

			// ----- 답글/수정 입력칸 안의 버튼들 (이것도 나중에 만들어진다) -----
			$("#commentList").on("click", ".js-cancel", function(){
				closeSlot($(this).data("idx"));
			});
			$("#commentList").on("click", ".js-reply-submit", function(){
				submitReply($(this).data("idx"));
			});
			$("#commentList").on("click", ".js-edit-submit", function(){
				submitEdit($(this).data("idx"));
			});
			/* ★ 클래스 이름 앞에 js- 를 붙인 이유
			     comment-btn 같은 클래스는 'CSS 디자인용' 이다.
			     디자이너가 클래스를 바꾸면 자바스크립트가 조용히 깨진다.
			     → 'JS 가 잡는 용도' 의 클래스는 js- 로 따로 만든다. 실무에서 널리 쓰는 규칙이다. */


			/* ───────────────────────────────────────────────────────────
			   3-6. 댓글 쓰기 버튼 / 글자수 표시
			   ─────────────────────────────────────────────────────────── */
			$("#commentSubmit").on("click", function(){
				addComment(null, $("#commentInput").val(), null);
				// ↑ 부모 없음(null) = 최상위 댓글, 슬롯 없음(null) = 상단 입력칸 사용
			});

			$("#commentInput").on("input", function(){
				$("#commentLen").text($(this).val().length);
				/* ↑ 글자를 칠 때마다 "37 / 1000" 의 앞 숫자를 갱신한다.
				   ★ maxlength=1000 으로 막아뒀어도 이 표시가 있어야
				     사용자가 '왜 더 안 써지지?' 하고 당황하지 않는다.
				     제한을 걸었으면 그 상태를 반드시 보여줘야 한다. */
			});


			/* ───────────────────────────────────────────────────────────
			   3-7. 화면이 열리면 댓글을 불러온다
			   ─────────────────────────────────────────────────────────── */
			loadComments();
			/* ★ ready 블록 안에 있으므로 HTML 과 jQuery 가 모두 준비된 뒤에 실행된다.
			     순수 JS 판에서 document.readyState 를 검사하던 부분이
			     jQuery 에서는 이 블록 하나로 전부 해결된다. */

		});   // ←←← $(document).ready 끝


		/* ████████████████████████████████████████████████████████████████████████
		   4. 기능 함수들

		   ★ 왜 ready 블록 '바깥' 에 두었나?
		     ready 안에 넣어도 동작하지만, 바깥에 두면
		       ① 코드가 "연결(ready 안) / 기능(바깥)" 으로 깔끔하게 나뉘어 읽기 쉽다
		       ② 브라우저 콘솔(F12)에서 loadComments() 를 직접 호출해 시험해 볼 수 있다
		     ★ ②번은 디버깅할 때 정말 유용하다. 꼭 써 보게 할 것.
		   ████████████████████████████████████████████████████████████████████████ */

		/* ───────── 본문 높이를 내용에 맞추기 ─────────
		   본문은 <textarea> 다. 읽기 전용이지만 비밀번호가 맞으면 여기서 바로 수정하므로
		   구조를 바꿀 수 없다. CSS 최소 높이가 160px 이라 긴 글은 잘려 보인다. */
		function fitContentHeight(){

			var $ta = $("#content");
			if($ta.length === 0){ return; }   // 그 칸이 없는 화면이면 아무것도 하지 않는다
			/* ↑ ★ jQuery 는 못 찾아도 오류가 안 나므로 .length 로 직접 확인해야 한다. */

			$ta.css("height", "auto");
			/* ↑ ★ 이 줄이 왜 필요한지가 핵심이다.
			     scrollHeight 는 "현재 높이 안에서 내용이 차지하는 높이" 를 알려준다.
			     이미 500px 로 늘려놨다면 scrollHeight 도 500 근처가 나온다.
			     → 글을 지워서 내용이 줄어도 높이가 절대 안 줄어드는 버그가 생긴다.
			     그래서 먼저 auto 로 되돌려 '진짜 필요한 높이'를 재측정하는 것이다. */

			$ta.css("height", ($ta[0].scrollHeight + 4) + "px");
			/*                  ▲
			                    └ ★ $ta[0] = jQuery 로 감싼 것을 '순수 DOM 요소'로 다시 꺼내는 문법.
			                       scrollHeight 는 jQuery 기능이 아니라 브라우저 기본 속성이라
			                       이렇게 꺼내서 써야 한다.
			                       ★ jQuery 를 쓰다가도 필요하면 언제든 순수 JS 로 내려갈 수 있다.

			     + 4  = 테두리 여유. 없으면 마지막 줄이 살짝 잘려 스크롤바가 생긴다.
			     +"px"= CSS 는 단위가 필요하다. (숫자 + 문자열 = 문자열이 된다) */
		}

		/* ───────── [보안 핵심] XSS 차단 함수 ─────────

		   댓글은 '다른 사람이 쓴 글' 이다. = 세상에서 가장 믿을 수 없는 값.
		   아래 renderComments 는 html() 로 화면을 그리므로,
		   댓글 내용에 <script> 가 들어 있으면 그대로 실행된다.
		   → 반드시 이 함수를 통과시킨 뒤에 넣어야 한다. */
		function cEsc(text){
			return String(text == null ? "" : text)
			/*     ▲            ▲
			       │            └ ★ == 를 일부러 쓴 드문 경우.
			       │               == null 은 null 과 undefined 둘 다를 한 번에 잡아준다.
			       └ String(...) 으로 감싸는 이유 : 숫자가 와도 .replace 를 쓸 수 있게.
			          (숫자에는 replace 함수가 없어 감싸지 않으면 오류가 난다) */
				.replace(/&/g, "&amp;")
				/* ↑ ★★ 순서가 절대적으로 중요하다. & 를 반드시 '가장 먼저' 바꾼다 ★★
				     < 를 먼저 바꾸면 "<" → "&lt;" 가 되고,
				     그 다음 & 를 바꾸면 "&lt;" → "&amp;lt;" 가 되어
				     화면에 &lt; 라는 글자가 그대로 보이는 이상한 결과가 나온다.

				   /&/g 의 g = global, "하나만 말고 전부 다 바꿔라".
				     g 를 빼면 첫 번째만 바뀌어 두 번째부터는 공격이 통한다. */
				.replace(/</g, "&lt;")      // < 를 막으면 태그를 열 수 없다 = 공격의 핵심 차단
				.replace(/>/g, "&gt;")
				.replace(/"/g, "&quot;")    // 속성값을 끊고 나오는 것을 막는다
				.replace(/'/g, "&#39;");
		}

		// ───── 줄바꿈만 <br> 로 되살리기 ─────
		function cText(text){
			return cEsc(text).replace(/\r\n|\r|\n/g, "<br>");
			/*     ▲                  ▲
			       │                  └ 윈도우(\r\n) / 옛 맥(\r) / 리눅스(\n) 세 종류를 모두 처리.
			       │                     ★ \r\n 을 가장 앞에 둬야 한다. 뒤에 두면 \r 이 먼저 잡혀
			       │                       줄바꿈이 두 번 들어간다.
			       └ ★★ 반드시 이스케이프를 '먼저' 한다.
			          순서를 바꾸면 우리가 넣은 <br> 까지 &lt;br&gt; 로 바뀌어 무용지물이 된다. */
		}
	  	
		/* ───────── 댓글 목록 불러오기 ───────── */
		function loadComments(){

			postForm(ctx + "/Board/commentList.do", { b_idx : $("#b_idx").val() })
				.done(function(text){

					var data = JSON.parse(text);
					/* ↑ 서버가 보낸 '글자'를 자바스크립트 '객체'로 바꾼다.
					     '{"ok":true,"count":3}'  →  data.ok, data.count 로 꺼내 쓸 수 있는 형태

					   ★ JSON.parse 는 형식이 조금이라도 깨지면 예외를 던진다.
					     그러면 이 .done 이 중단되고 아래 .fail 로 넘어간다. */

					if(!data.ok){
						$("#commentList").html('<p class="comment-empty">' + cEsc(data.message) + '</p>');
						/*                                                  ▲
						                                                    └ ★ 서버가 보낸 메시지도 이스케이프한다.
						                                                       "서버가 보냈으니 안전하겠지"는 위험한 가정이다.
						                                                       그 안에 사용자 입력이 섞여 있을 수 있다. */
						return;
					}

					renderComments(data.comments, data.loggedIn);
					$("#commentCount").text(data.count);
					// ↑ 숫자를 넣을 때도 text(). 습관을 들이면 XSS 사고가 원천 차단된다.
				})
				.fail(function(){
					$("#commentList").html('<p class="comment-empty">댓글을 불러오지 못했습니다.</p>');
					/* ↑ ★ 오류가 나도 '댓글을 불러오는 중입니다...' 가 영원히 남아 있으면
					     사용자는 계속 기다린다. 실패했다는 사실을 반드시 화면에 알려야 한다. */
				});
		}

		/* ───────── 받은 댓글 목록으로 화면 그리기 ─────────

		   [왜 서버가 HTML 을 안 만들고 자바스크립트가 그리나]
		     서버가 HTML 을 통째로 주면 편하지만, 그러면 댓글 하나 달 때마다
		     화면 전체를 다시 받아야 한다. 데이터(JSON)만 받아 여기서 그리면
		     필요한 부분만 바뀌어 읽던 위치가 유지된다. */
		function renderComments(list, loggedIn){

			var $box = $("#commentList");

			if(!list || list.length === 0){
				$box.html('<p class="comment-empty">첫 댓글을 남겨보세요.</p>');
				/* ↑ ★ '빈 상태(empty state)' 처리.
				     아무것도 없을 때 진짜 아무것도 안 보여주면 사용자는 '고장났나?' 한다.
				     행동을 유도하는 문구를 넣는 것이 좋다. */
				return;
			}

			var html = "";
			/* ↑ ★ 중요한 성능 기법 : HTML 을 '글자로 전부 조립한 뒤 마지막에 한 번만' 넣는다.
			     반복문 안에서 $box.append(...) 를 하면 댓글 50개에 화면을 50번 다시 그린다.
			     눈에 띄게 느려진다. 모아서 한 번에 넣으면 1번만 그린다. */

			for(var i = 0; i < list.length; i++){

				var c = list[i];
				// ★ 서버가 이미 '보여줄 순서'대로 정렬해 보냈다.
				//   대댓글 계층 정렬은 SQL 로 하는 것이 훨씬 정확하고 빠르다.

				var cIdx  = Number(c.cIdx) || 0;
				var depth = Number(c.depth) || 0;
				var likes = Number(c.likeCount) || 0;
				/* ↑ ★ Number(...) || 0 은 아주 유용한 관용구다.
				     Number("5")→5 / Number("abc")→NaN(거짓)→0 / Number(null)→0

				   [왜 숫자로 강제하나]
				     이 값들은 아래에서 HTML 안에 그대로 박힌다.
				     문자열이 들어와 따옴표나 태그가 섞이면 HTML 이 깨지거나 공격에 이용될 수 있다.
				     숫자로 바꿔버리면 그런 위험이 사라진다. */

				html += '<div class="comment-item" data-depth="' + depth + '" id="c-' + cIdx + '">';
				/* ↑ data-depth = HTML 표준 '사용자 정의 속성'. data- 로 시작하면 뭐든 만들 수 있다.
				     CSS 가 이 값을 읽어 들여쓰기 폭을 정한다.
				   ★ style="margin-left:40px" 처럼 직접 넣지 않는 이유 :
				     디자인을 바꾸려면 자바스크립트를 고쳐야 하기 때문이다.
				     값(depth)만 넘기고 표현은 CSS 에 맡긴다.

				   id="c-123" : 나중에 이 댓글만 콕 집어 찾기 위한 이름표.
				     ★ 숫자로 시작하는 id 는 CSS 선택자에서 문제가 되므로 "c-" 를 앞에 붙였다. */

				if(depth > 0){
					html += '<span class="comment-arrow" aria-hidden="true">&#8627;</span>';
					/* ↑ &#8627; = 꺾인 화살표(↳) 문자 코드. 답글임을 시각적으로 알린다.
					     aria-hidden="true" = 스크린리더에게 "이건 장식이니 읽지 마라".
					   ★ 화면을 못 보는 사용자에게 "아래로 꺾인 화살표"를 읽어줘봐야 방해만 된다.
					     의미 없는 장식에 이 속성을 붙이는 것이 접근성의 기본이다. */
				}

				html += '<div class="comment-body">';

				// ─── 삭제된 댓글 : 뼈대만 남긴다 ───
				if(c.deleted){
					html += '<p class="comment-deleted">삭제된 댓글입니다.</p>';
					html += '</div></div>';
					continue;
					/* ↑ continue = 남은 부분은 건너뛰고 다음 댓글로.

					   [왜 지운 댓글의 자리를 남기나]
					     그 댓글에 달린 '답글' 들이 있기 때문이다.
					     부모를 완전히 없애면 답글들이 대화 맥락을 잃고 붕 뜬다.
					     그래서 내용만 지우고 자리는 남긴다. (실제 서비스들이 쓰는 방식) */
				}

				// ─── 머리글 : 이름 / 날짜 / (수정됨) ───
				html += '<div class="comment-meta">'
					 +    '<b class="comment-name">' + cEsc(c.name) + '</b>'
					 +    '<span class="comment-date">' + cEsc(c.date) + '</span>'
					 +    (c.edited ? '<span class="comment-edited">(수정됨)</span>' : '')
					 /*     ▲
					        └ ★ 삼항 연산자를 문자열 조립 안에서 쓰는 기법.
					           수정된 댓글에만 (수정됨)을 붙이고, 아니면 빈 문자열을 붙인다.
					           ★ (수정됨) 표시는 '신뢰'의 문제다. 누가 댓글을 몰래 바꿔치기
					             할 수 없게 하는 최소한의 장치. */
					 + '</div>';

				// ─── 내용 ───
				html += '<p class="comment-content" id="cc-' + cIdx + '">' + cText(c.content) + '</p>';
				/*                                                          ▲
				                                                            └ ★ cEsc 가 아니라 cText.
				                                                               댓글은 줄바꿈을 살려야 읽기 좋다.
				                                                               (이스케이프 먼저, 줄바꿈 나중) */

				// ─── 버튼줄 ───
				html += '<div class="comment-actions">';

				if(!c.mine){
					// 남의 댓글 → 추천 버튼 (내가 이미 눌렀으면 is-liked 로 색이 채워진다)
					html += '<button type="button" class="comment-like js-like' + (c.likedByMe ? ' is-liked' : '') + '"'
						 +  ' data-idx="' + cIdx + '">'
						 /*   ▲
						      └ ★ 여기가 jQuery 판의 가장 큰 변화다.
						         (기존) onclick="toggleLike(3)"     ← HTML 에 동작을 박아 넣음
						         (지금) class="js-like" data-idx="3" ← '표시'만 남기고 동작은 위임이 처리

						         왜 좋아지나
						           ① HTML(구조)과 JS(동작)가 분리된다
						           ② 함수를 전역으로 열어둘 필요가 줄어든다
						           ③ 버튼이 400개여도 이벤트는 1개만 등록된다 */
						 +  '&#128077; <span id="cl-' + cIdx + '">' + likes + '</span></button>';
						 //   ▲ &#128077; = 👍 이모지의 문자 코드.
						 //      이모지를 직접 넣으면 파일 인코딩 문제로 깨질 수 있어 코드로 적었다.
				}else{
					// 내 댓글 → 자기 추천 방지. 버튼 없이 숫자만 보여준다.
					html += '<span class="comment-like is-static">&#128077; '
						 +  '<span id="cl-' + cIdx + '">' + likes + '</span></span>';
					/* ★ 버튼을 숨기는 것은 '안내'일 뿐이다.
					   진짜 자기추천 차단은 서버가 해야 한다. (여기서도 같은 원칙) */
				}

				if(loggedIn){
					html += '<button type="button" class="comment-btn js-reply" data-idx="' + cIdx + '">답글</button>';
				}
				if(c.mine){
					html += '<button type="button" class="comment-btn js-edit" data-idx="' + cIdx + '">수정</button>'
						 +  '<button type="button" class="comment-btn comment-btn-danger js-remove" data-idx="' + cIdx + '">삭제</button>';
					/* ↑ ★ c.mine 은 '서버가' 판단해서 보내준 값이다.
					     화면이 세션을 보고 스스로 판단하면 조작이 가능하다.
					     "이 댓글이 내 것인가" 같은 판단은 반드시 서버 몫이다. */
				}

				html += '</div>';                                                // comment-actions 닫기
				html += '<div class="comment-slot" id="cs-' + cIdx + '"></div>';  // 답글/수정 입력칸이 열릴 빈 자리
				html += '</div></div>';                                          // comment-body, comment-item 닫기
				/* ★ 여는 태그와 닫는 태그 개수를 반드시 맞춰야 한다.
				   하나라도 빠지면 레이아웃이 통째로 무너진다.
				   그래서 닫는 곳마다 무엇을 닫는지 주석을 달아 두는 것이 좋다. */
			}

			$box.html(html);   // ★ 마지막에 딱 한 번. (위에서 설명한 성능 이유)
		}

		/* ───────── 댓글 등록 (최상위 댓글과 답글이 같은 함수를 쓴다) ───────── */
		function addComment(parentIdx, content, slotId){
		/*                  ▲          ▲        ▲
		                    │          │        └ 답글이면 입력칸이 열린 자리의 id. 최상위면 null
		                    │          └ 입력한 내용
		                    └ 답글이면 부모 댓글 번호, 최상위면 null

		   ★ 좋은 설계 : 최상위 댓글과 답글은 'parent 가 있느냐'만 다르다.
		     함수를 두 개 만들지 않고 하나로 처리하면 고칠 때도 한 곳만 고치면 된다. */

			if(!content || content.trim() === ""){
				alert("내용을 입력해주세요.");
				return;
			}
			/* ↑ trim() 으로 공백을 지운 뒤 비교하는 이유 :
			     스페이스만 잔뜩 친 댓글도 걸러내기 위해서다.
			   ★ 여기서 막아도 서버는 반드시 다시 검사해야 한다. (화면 검사는 편의) */

			var data = { b_idx : $("#b_idx").val(), content : content };
			if(parentIdx){ data.parent_idx = parentIdx; }
			// ↑ 답글일 때만 parent_idx 항목을 추가한다. (최상위면 아예 안 보내는 편이 깔끔하다)

			postForm(ctx + "/Board/commentAdd.do", data)
				.done(function(text){

					var res = JSON.parse(text);
					if(!res.ok){ alert(res.message); return; }
					/* ↑ ★ JSON 방식의 장점이 여기서 드러난다.
					     서버가 "권한이 없습니다", "1000자를 넘었습니다" 등 구체적인 이유를
					     message 에 담아 보낼 수 있다.
					     문자열 방식이었다면 실패 종류마다 새 약속 문자열을 만들어야 한다. */

					// 입력칸 정리
					if(slotId){
						$("#" + slotId).empty();
						/* ↑ empty() = 그 요소의 '안쪽 내용만' 전부 지운다.
						   ★ remove() 와 헷갈리면 안 된다. remove() 는 요소 자신까지 사라진다.
						     여기서 remove 를 쓰면 답글 입력칸이 열릴 '자리'가 통째로 없어져
						     다음부터 답글을 달 수 없게 된다. */
					}else{
						$("#commentInput").val("");     // 입력칸을 비운다
						$("#commentLen").text("0");     // 글자수 표시도 0 으로
						/* ★ 이걸 깜빡하면 "0 / 1000" 이 아니라 이전 숫자가 그대로 남는다.
						   작지만 이런 게 '완성도' 다. */
					}

					loadComments();
					/* ↑ 목록을 다시 불러온다.
					   ★ 왜 방금 쓴 댓글만 화면에 끼워넣지 않고 전체를 다시 받나?
					     그 사이 다른 사람이 쓴 댓글도 함께 보이고,
					     계층 정렬 순서도 서버가 다시 계산해 주기 때문이다.
					     "화면이 직접 계산하지 말고 서버의 진실을 다시 받아온다"가 더 안전하다. */
				})
				.fail(function(){ alert("등록 실패 : 잠시 후 다시 시도해주세요."); });
		}

		/* ───────── 답글 입력칸 열기 ───────── */
		function showReply(cIdx){

			var $slot = $("#cs-" + cIdx);   // 그 댓글 아래 빈 자리

			// 이미 열려 있으면 닫는다 (같은 버튼으로 토글)
			if($slot.html() !== ""){ $slot.empty(); return; }
			/* ↑ ★ '토글' 구현.
			     같은 버튼을 다시 눌렀을 때 아무 반응이 없으면 사용자는 답답해한다.
			     '열려 있는가'를 따로 변수로 기억하지 않고 내용이 비었는지로 판단한다 — 간단하고 확실. */

			$slot.html(
				'<div class="comment-form comment-form-sm">'
			  +   '<textarea class="form-control" rows="2" maxlength="1000" id="rp-' + cIdx + '"'
			  +             ' placeholder="답글을 입력하세요"></textarea>'
			  +   '<div class="comment-form-foot">'
			  +     '<button type="button" class="btn btn-ghost btn-sm js-cancel" data-idx="' + cIdx + '">취소</button>'
			  +     '<button type="button" class="btn btn-primary btn-sm js-reply-submit" data-idx="' + cIdx + '">답글 등록</button>'
			  +   '</div>'
			  + '</div>'
			);
			/* ↑ id="rp-123" 처럼 댓글 번호를 붙여 이름을 만든다.
			     ★ 답글 입력칸이 동시에 여러 개 열려도 서로 안 섞이게 하는 핵심 장치다.
			     모두 id="reply" 로 했다면 브라우저가 첫 번째 것만 찾아 엉뚱한 곳에 글이 써진다.

			   ★ 여기 버튼들도 js-cancel / js-reply-submit 클래스만 붙였다.
			     동작은 위에서 등록해 둔 이벤트 위임이 알아서 처리한다.
			     "나중에 만들어지는 요소"의 전형적인 사례다. */

			$("#rp-" + cIdx).focus();
			/* ↑ 커서를 자동으로 놓아 준다.
			   ★ 이게 없으면 사용자가 입력칸을 한 번 더 클릭해야 한다.
			     클릭 한 번을 줄이는 것이 UX 다. */
		}

		function submitReply(cIdx){
			addComment(cIdx, $("#rp-" + cIdx).val(), "cs-" + cIdx);
			// ↑ 부모 번호(cIdx)와 함께 넘기면 addComment 가 답글로 처리한다
		}

		function closeSlot(cIdx){
			$("#cs-" + cIdx).empty();
			// ↑ 자리를 비우면 그 안의 입력칸·버튼이 통째로 사라진다. 따로 지울 필요가 없다.
		}

		/* ───────── 수정 입력칸 열기 ───────── */
		function showEdit(cIdx){

			var $slot = $("#cs-" + cIdx);
			if($slot.html() !== ""){ $slot.empty(); return; }   // 토글

			/* ★★ 여기가 이 스크립트에서 가장 까다로운 부분이다 ★★

			   문제 : 화면에 보이는 댓글은 이미 '가공된 상태' 다.
			     원본  : 안녕\n하세요 & 반가워요
			     화면  : 안녕<br>하세요 &amp; 반가워요
			   이걸 그대로 수정칸에 넣으면 사용자에게
			     "안녕<br>하세요 &amp; 반가워요"
			   라는 괴상한 글자가 보인다. → 원본으로 되돌려야 한다.

			   ★ jQuery 는 이걸 아주 우아하게 해결한다. */

			// [1단계] <br> 을 다시 줄바꿈 문자로
			var current = $("#cc-" + cIdx).html().replace(/<br\s*\/?>/gi, "\n");
			/*            ▲                       ▲      ▲
			              │                       │      └ gi : g=전부, i=대소문자 무시 (<BR> 도 처리)
			              │                       └ \s*\/? = 공백이나 / 가 있어도 된다는 뜻.
			              │                          브라우저마다 <br>, <br/>, <br /> 로 다르게 저장하기 때문.
			              └ html() 로 읽으면 태그가 포함된 상태로 가져온다 (text() 는 태그가 빠진다) */

			// [2단계] &amp; 같은 엔티티를 원래 글자로 되돌리기
			current = $("<textarea/>").html(current).text();
			/* ↑ ★★ 순수 JS 판보다 훨씬 짧아진 부분이다. 원리를 이해하면 재미있다.

			     $("<textarea/>")  : 화면에 붙이지 않은 '가짜' textarea 를 메모리에만 만든다
			     .html(current)    : 거기에 HTML 을 넣으면 브라우저가 엔티티를 원래 글자로 해석한다
			     .text()           : 해석된 '순수 글자' 를 꺼낸다

			   즉 브라우저의 해석 능력을 빌려 쓰는 것이다.
			   직접 &amp;→& 로 바꾸는 코드를 짜면 엔티티 종류가 수백 개라 다 처리할 수 없다.

			   ★ 화면에 붙이지 않았기 때문에 안에 <script> 가 있어도 실행되지 않는다. */

			$slot.html(
				'<div class="comment-form comment-form-sm">'
			  +   '<textarea class="form-control" rows="3" maxlength="1000" id="ed-' + cIdx + '"></textarea>'
			  +   '<div class="comment-form-foot">'
			  +     '<button type="button" class="btn btn-ghost btn-sm js-cancel" data-idx="' + cIdx + '">취소</button>'
			  +     '<button type="button" class="btn btn-primary btn-sm js-edit-submit" data-idx="' + cIdx + '">수정 완료</button>'
			  +   '</div>'
			  + '</div>'
			);

			$("#ed-" + cIdx).val(current).focus();
			/* ↑ ★ 체이닝으로 '값 넣기'와 '커서 놓기'를 한 줄에 처리했다. jQuery 다운 코드다.

			   ★ 여기서 val() 로 넣는 것이 핵심이다.
			     위 HTML 조립 때 textarea 안에 내용을 끼워넣지 않고 빈 칸으로 만든 뒤,
			     만들어진 다음에 val() 로 넣었다.
			     HTML 문자열에 직접 끼워넣으면 내용에 </textarea> 가 있을 때
			     태그가 깨지면서 XSS 로 이어질 수 있기 때문이다. */
		}

		function submitEdit(cIdx){

			var content = $("#ed-" + cIdx).val();

			if(!content || content.trim() === ""){
				alert("내용을 입력해주세요.");
				return;
			}

			postForm(ctx + "/Board/commentEdit.do", { c_idx : cIdx, content : content })
				.done(function(text){
					var res = JSON.parse(text);
					if(!res.ok){ alert(res.message); return; }
					loadComments();   // 목록을 다시 받아 수정 결과와 (수정됨) 표시를 반영
				})
				.fail(function(){ alert("수정 실패 : 잠시 후 다시 시도해주세요."); });
		}

		/* ───────── 댓글 삭제 ───────── */
		function removeComment(cIdx){

			if(!window.confirm("이 댓글을 삭제하시겠어요?")){ return; }

			postForm(ctx + "/Board/commentDelete.do", { c_idx : cIdx })
				.done(function(text){
					var res = JSON.parse(text);
					if(!res.ok){ alert(res.message); return; }
					loadComments();
					/* ★ 여기서 목록을 다시 받는 이유가 특히 중요하다.
					   답글이 달린 댓글은 '삭제된 댓글입니다'로 남고,
					   답글이 없으면 완전히 사라진다.
					   그 판단은 서버가 하므로 화면이 마음대로 지우면 안 된다. */
				})
				.fail(function(){ alert("삭제 실패 : 잠시 후 다시 시도해주세요."); });
		}

		/* ───────── 추천 (누르면 추천, 다시 누르면 취소) ───────── */
		function toggleLike(cIdx){

			postForm(ctx + "/Board/commentLike.do", { c_idx : cIdx })
				.done(function(text){

					var res = JSON.parse(text);
					if(!res.ok){ alert(res.message); return; }

					/* ★★ 여기만 loadComments() 를 부르지 않는다. 그 이유가 중요하다 ★★

					   추천은 아주 가볍게, 자주 누르는 동작이다.
					   누를 때마다 목록 전체를 다시 그리면
					     - 화면이 깜빡이고
					     - 열어둔 답글 입력칸이 닫히고
					     - 읽던 스크롤 위치가 튄다
					   → 바뀐 것은 '숫자 하나와 색 하나' 뿐이므로 그것만 고친다.

					   ★ 원칙 : 바뀐 범위가 작으면 작게 고친다. 전체 다시 그리기는 최후의 수단. */

					$("#cl-" + cIdx).text(res.likeCount);
					/* ↑ ★ 화면의 숫자를 +1 하지 않고, '서버가 알려준 숫자'를 그대로 쓴다.
					     동시에 다른 사람도 눌렀다면 화면 계산은 틀리기 때문이다.
					     숫자의 진실은 언제나 서버(DB)에 있다. */

					$("#c-" + cIdx).find(".comment-like").toggleClass("is-liked", !!res.liked);
					/*                 ▲                  ▲                      ▲
					                   │                  │                      └ !! 는 "확실히 true/false 로 바꿔라".
					                   │                  │                         서버가 1/0 이나 "true" 를 보내도 안전하다.
					                   │                  └ ★ toggleClass(클래스, 조건)
					                   │                     조건이 true 면 붙이고, false 면 뗀다.
					                   │                     순수 JS 판에서는 if/else 로 add/remove 를 나눠 썼는데
					                   │                     jQuery 는 한 줄로 끝난다.
					                   └ find() = "그 안에서 찾아라". 그 댓글 안의 추천 버튼만 정확히 잡는다.

					   ★ 전체 화면에서 .comment-like 를 찾으면 다른 댓글의 버튼이 잡힌다.
					     그래서 '그 댓글 안에서'로 범위를 좁히는 것이다.

					   ★ 색을 css() 로 직접 바꾸지 않고 클래스를 붙이는 이유 :
					     '어떤 색인지'는 CSS 가 정한다. 자바스크립트는 '상태'만 알려준다.
					     나중에 디자인을 바꿔도 이 코드는 건드릴 필요가 없다. */
				})
				.fail(function(){ alert("추천 실패 : 잠시 후 다시 시도해주세요."); });
		}


		/* ████████████████████████████████████████████████████████████████████████
		   [ jQuery 전환 대응표 — 수업 자료용 ]

		     순수 자바스크립트                              jQuery
		     ─────────────────────────────────────────────────────────────────
		     document.getElementById("id")                 $("#id")
		     document.querySelector(".cls")                $(".cls")
		     el.value                                      .val()
		     el.value = "x"                                .val("x")
		     el.textContent = "x"                          .text("x")
		     el.innerHTML = "<b>x</b>"                     .html("<b>x</b>")
		     el.innerHTML = ""                             .empty()
		     el.disabled = true                            .prop("disabled", true)
		     el.style.color = "red"                        .css("color", "red")
		     el.classList.add("a") / .remove("a")          .addClass("a") / .removeClass("a")
		     (if/else 로 add·remove)                        .toggleClass("a", 조건)
		     el.getAttribute("data-idx")                   .data("idx")
		     el.addEventListener("click", fn)              .on("click", fn)
		     el.click()                                    .trigger("click")
		     (동적 요소는 onclick 속성 필요)                 .on("click", ".자식", fn)  ★ 이벤트 위임
		     document.readyState 검사                       $(document).ready(fn)
		     fetch(...).then().catch()                     $.ajax({...}).done().fail()
		     (res.ok 직접 검사 필요)                         404·500 을 알아서 .fail 로 보냄
		     URLSearchParams 로 직접 조립                   data 에 객체만 주면 끝
		     $ta.scrollHeight                              $ta[0].scrollHeight  ★ [0] 으로 DOM 꺼내기

		   [ 학생이 반드시 가져가야 할 7가지 ]
		     ① 화면 검사는 '안내', 진짜 보안은 '서버'. prop("disabled") 는 F12 로 뚫린다.
		     ② 남이 쓴 글은 화면에 넣기 전에 반드시 cEsc. 그리고 & 를 가장 먼저 바꾼다.
		     ③ 글자만 넣을 땐 text(), 태그를 살릴 때만 html(). html() 에는 고정 문구만.
		     ④ 나중에 만들어지는 요소에는 반드시 '이벤트 위임' 을 쓴다.
		     ⑤ 모든 $.ajax 에는 .fail 을 붙인다. 없으면 '눌렀는데 아무 일도 안 일어난다'.
		     ⑥ 숫자의 진실은 서버에 있다. 화면에서 +1 하지 말고 서버가 준 값을 쓸 것.
		     ⑦ jQuery 는 없는 요소를 잡아도 오류를 안 낸다. 그래서 오타가 '조용히' 숨는다.
		        안 될 때는 $("#아이디").length 를 콘솔에 찍어 0 인지부터 확인할 것.

		   [ 반드시 확인할 것 ]
		     · js 폴더에 jquery-3.7.1.min.js 파일이 실제로 있는가
		     · Top.jsp 에서 jquery.slim 을 또 부르고 있지 않은가 (있으면 반드시 제거)
		     · js/app.js 의 CSRF 토큰 파라미터 이름이 위 CSRF 탐색 목록과 맞는가
		   ████████████████████████████████████████████████████████████████████████ */
	</script>
