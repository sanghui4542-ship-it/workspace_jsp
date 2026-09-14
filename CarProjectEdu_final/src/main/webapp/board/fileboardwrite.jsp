
<%@page import="Vo.MemberVO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
	request.setCharacterEncoding("UTF-8");
	String contextPath = request.getContextPath();

	/*
	 nowPage, nowBlock 을 받는 이유 (자세한 설명은 board/write.jsp 와 같다)

	   fileboardlist.jsp 에서 보던 페이지 번호를 여기서 받아 두었다가,
	   아래 [목록] 버튼의 이동 주소에 그대로 붙여 준다. 그래야 글을 쓰고
	   나서 [목록]을 눌렀을 때 방금 보고 있던 페이지로 돌아갈 수 있다.
	*/
	String nowPage = (String)request.getAttribute("nowPage");
	String nowBlock = (String)request.getAttribute("nowBlock");

	//사장(FileBoardController)이 조회해 넘겨준 글 작성자(로그인한 나)의 정보
	MemberVO membervo = (MemberVO)request.getAttribute("membervo");
	String email = membervo.getEmail();
	String name = membervo.getName();
	String id = membervo.getId();
%>

<%--
 ============================================================================
  board/fileboardwrite.jsp  -  자료실(파일게시판) 글쓰기 화면 (로그인한 회원만 볼 수 있다)

  CarMain.jsp 의 가운데 영역에 끼워져(include) 보이는 화면 조각이다.
    요청 흐름 : fileboardlist.jsp 새 글쓰기 버튼 -> /FileBoard/write.bo
               -> 사장(FileBoardController) -> 로그인한 회원 정보(membervo)를
               request 에 담아 -> 이 화면

  [이 화면만 board/write.jsp 와 다른 점 - 파일 첨부]
    enctype="multipart/form-data" 로 파일과 글자를 함께 보낼 수 있게 했고,
    등록도 자유게시판처럼 CarApp.postForm(AJAX)이 아니라 폼을 실제로
    전송(submit)한다. 파일이 큰 경우 업로드 진행 중에는 AJAX보다 브라우저의
    기본 폼 전송(페이지 이동)이 더 안정적이기 때문이다.

  [파일 선택 칸을 여러 개 만들 수 있는 구조]
    [파일 추가] 버튼을 누를 때마다 addFileRow() 가 파일 선택 줄을 하나씩
    새로 만든다. 모든 줄의 <input type="file"> 은 name="fileName" 으로
    같은 이름을 쓰는데, 이렇게 하면 서버(FileBoardService.upload())가
    request.getParameterValues 방식이 아니라 commons-fileupload 의
    FileItem 목록으로 여러 개를 한 번에 받을 수 있다.
    #fileContainer(줄들이 쌓이는 자리)와 #addFileBtn(추가 버튼) 두 id 는
    아래 자바스크립트가 직접 찾아 쓰므로 이름을 바꾸면 안 된다.
 ============================================================================
--%>
<form action="<%=contextPath%>/FileBoard/writePro.bo"
      method="post" enctype="multipart/form-data">

	<div class="board-container">

		<h2 class="section-title">파일 게시판 글쓰기</h2>

		<%-- 작성자 / 아이디 : 로그인 정보라 수정할 수 없다 --%>
		<div class="grid-2">

			<div class="form-group">
				<label class="form-label" for="writer">작성자</label>
				<input class="form-control" type="text" name="writer" id="writer"
					   value="<%=util.HtmlUtil.escape(name)%>" readonly>
			</div>

			<div class="form-group">
				<label class="form-label" for="writer_id">아이디</label>
				<input class="form-control" type="text" name="writer_id" id="writer_id"
					   value="<%=util.HtmlUtil.escape(id)%>" readonly>
			</div>

		</div>

		<div class="form-group">
			<label class="form-label" for="email">메일주소</label>
			<input class="form-control" type="email" name="email" id="email"
				   value="<%=util.HtmlUtil.escape(email)%>" readonly>
		</div>

		<div class="form-group">
			<label class="form-label" for="title">제목 <span class="required">*</span></label>
			<input class="form-control" type="text" name="title" id="title"
				   maxlength="200" placeholder="제목을 입력하세요">
		</div>

		<%-- 첨부파일 : 아래 두 id(fileContainer / addFileBtn)는 자바스크립트가 사용하므로 유지 --%>
		<div class="form-group">
			<label class="form-label">첨부파일</label>

			<div id="fileContainer" class="flex flex-col gap-2"></div>

			<button type="button" id="addFileBtn" class="btn btn-secondary btn-sm mt-2"
					onclick="addFileRow()">
				&#43; 파일 추가
			</button>

			<p class="form-hint">
				&#8226; 파일 1개당 최대 10MB, 전체 30MB까지 올릴 수 있습니다.<br>
				&#8226; 이미지·문서·압축파일만 첨부할 수 있습니다.
			</p>
		</div>

		<div class="form-group">
			<label class="form-label" for="content">내용</label>
			<textarea class="form-control" name="content" id="content"
					  placeholder="내용을 입력하세요"></textarea>
		</div>

		<div class="form-group">
			<label class="form-label" for="pass">비밀번호 <span class="required">*</span></label>
			<input class="form-control" type="password" name="pass" id="pass"
				   placeholder="글 수정·삭제에 사용됩니다">
			<p class="form-hint">&#8226; 나중에 이 글을 수정하거나 삭제할 때 필요합니다. 꼭 기억해 주세요.</p>
		</div>

		<%-- 버튼 : 이 화면은 파일 업로드(multipart)라서 AJAX 가 아니라 폼을 실제로 전송한다 --%>
		<div class="flex flex-wrap gap-2 justify-between mt-6">
			<button type="submit" class="btn btn-primary">글 등록</button>
			<button type="button" id="list" class="btn btn-ghost">목록</button>
		</div>

		<p id="resultInsert" class="mt-4 fw-bold"></p>

	</div>
	<%-- board-container 끝 --%>
</form>

<style>
/* ==========================================================================
   fileboardwrite.jsp 전용 스타일 - 동적으로 추가되는 "파일 선택 줄" 하나의 모양
   이 화면에만 쓰이는 부품이라 공용 CSS(app.css)로 옮기지 않고 여기 둔다
   ========================================================================== */

/* [파일 선택 줄 하나] addFileRow() 가 자바스크립트로 만들어 이 클래스를 붙인다 */
.file-row {
	display: flex;                 /* 파일선택 버튼·파일명·삭제버튼을 가로로 나란히 놓는다 */
	align-items: center;           /* 세 요소의 세로 위치를 가운데로 맞춘다 */
	gap: 8px;                      /* 세 요소 사이 간격 8px */
	background: #fff;              /* 배경색 : 흰색 */
	border: 1px solid #ddd;        /* 테두리 : 1px 실선, 연한 회색 */
	border-radius: 6px;            /* 모서리 둥글기 6px */
	padding: 5px 10px;             /* 안쪽 여백 : 위아래 5px, 좌우 10px */
	max-width: 600px;              /* 최대 너비 600px : 파일명이 길어도 줄이 너무 넓어지지 않게 */
}

/* [진짜 파일 선택 칸] 브라우저 기본 모양("파일 선택" 버튼 + "선택된 파일 없음")은 화면마다 다르게 생겨서 디자인을 맞추기 어렵다.
   그래서 이 input 자체는 숨기고, 아래 .file-label(<label for="...">) 을 대신 눌러서 연다.
   <label for="input의id"> 는 그 input 을 대신 클릭해 주는 표준 HTML 기능이라 자바스크립트 없이도 동작한다 */
.file-row input[type=file] {
	display: none;                 /* 화면에서 완전히 숨긴다 (지운 것은 아니라서 label 로 여전히 열 수 있다) */
}

/* [파일 선택 버튼처럼 보이는 라벨] 위에서 숨긴 input 대신 이 라벨을 눌러 파일 선택창을 연다 */
.file-row .file-label {
	display: inline-block;         /* 안쪽 여백(padding)이 잘 적용되도록 블록 성질을 준다 */
	padding: 4px 12px;             /* 안쪽 여백 : 위아래 4px, 좌우 12px */
	background: #555;              /* 배경색 : 어두운 회색 */
	color: #fff;                   /* 글자색 : 흰색 */
	border-radius: 4px;            /* 모서리 둥글기 4px */
	cursor: pointer;                /* 마우스를 올리면 손가락 모양 (누를 수 있다는 표시) */
	font-size: 12px;                /* 글자 크기 12px */
	white-space: nowrap;            /* "파일 선택" 글자가 줄바꿈되지 않게 한다 */
	flex-shrink: 0;                  /* 옆 파일명이 길어져도 이 버튼 크기는 줄어들지 않는다 */
}
/* [파일 선택 라벨에 마우스를 올렸을 때] 더 어둡게 */
.file-row .file-label:hover { background: #333; }

/* [선택된 파일명 표시] showFileName() 이 이 자리에 파일 이름과 크기를 채워 넣는다 */
.file-row .file-name {
	flex: 1;                        /* 남는 가로 공간을 전부 차지한다 (파일명이 길면 늘어난다) */
	font-size: 12px;
	color: #555;                    /* 아직 선택 전 기본 글자색 (선택하면 자바스크립트가 더 진하게 바꾼다) */
	overflow: hidden;                /* 이 칸보다 긴 파일명은 넘치는 부분을 자른다 */
	text-overflow: ellipsis;         /* 잘린 부분을 "..." 으로 표시한다 */
	white-space: nowrap;             /* 파일명이 길어도 줄바꿈하지 않고 한 줄로 (그래야 ellipsis 가 동작한다) */
}

/* [삭제(✖) 버튼] 이 파일 선택 줄 전체를 없앤다 */
.file-row .remove-btn {
	background: none;               /* 배경 없음 (투명) */
	border: none;                   /* 테두리 없음 */
	color: #aaa;                     /* 글자색 : 연한 회색 (눈에 덜 띄게) */
	font-size: 16px;
	cursor: pointer;
	padding: 0 4px;                  /* 안쪽 여백 : 좌우 4px 만 (클릭 영역을 살짝 넓힌다) */
	line-height: 1;                   /* 줄 높이를 글자 크기와 같게 : 세로 가운데 정렬이 흐트러지지 않게 */
	flex-shrink: 0;                   /* 옆 파일명이 길어져도 이 버튼 크기는 줄어들지 않는다 */
}
/* [삭제 버튼에 마우스를 올렸을 때] 브랜드색(빨강)으로 강조 */
.file-row .remove-btn:hover { color: #cc0000; }

/*
  [터치 타깃] ✖ 는 글자 하나라 실제 높이가 16px 밖에 안 된다.
    파일 목록에서 잘못 누르면 엉뚱한 첨부가 지워지므로 특히 위험하다.
    보이는 ✖ 크기는 그대로 두고, 좁은 화면에서만 누르는 영역을 44x44 로 넓힌다.
*/
@media (hover: none), (pointer: coarse), (max-width: 767px) {
	.file-row .remove-btn {
		display: inline-flex;        /* 안의 ✖ 글자를 정가운데에 놓기 위해 flex 로 바꾼다 */
		align-items: center;
		justify-content: center;
		min-width: 44px;              /* 최소 너비 44px */
		min-height: 44px;             /* 최소 높이 44px */
	}
}

/* [파일 추가 버튼에 마우스를 올렸을 때] .btn-secondary 기본 모양보다 더 뚜렷하게 강조한다
   (!important 를 쓰는 이유 : app.css 의 .btn-secondary:hover 규칙보다 우선 적용되게 하려고) */
#addFileBtn:hover {
	background: #f0f0f0 !important;
	border-color: #888 !important;
	color: #333 !important;
}
</style>

<script type="text/javascript">

	var rowCounter = 0; // 고유 id 생성용 카운터

	/* ------------------------------------------------
	   파일 행(row) 하나를 동적으로 생성해서 컨테이너에 추가
	   ------------------------------------------------ */
	function addFileRow() {
		var container = document.getElementById('fileContainer');   // 파일 선택 줄들이 쌓이는 자리를 찾는다
		rowCounter++;   // 줄 번호를 하나 올린다. 각 줄에 겹치지 않는 id 를 주기 위해서다
		var rowId  = 'fileRow_'  + rowCounter;   // 이 줄의 id (지울 때 이 이름으로 찾는다)
		var inputId = 'fileInput_' + rowCounter;   // 이 줄 안 파일 입력칸의 id

		var row = document.createElement('div');   // 새 줄을 담을 상자를 만든다
		row.className = 'file-row';   // CSS 가 이 클래스를 보고 줄 모양을 잡는다
		row.id = rowId;   // 위에서 만든 id 를 붙인다

		row.innerHTML =   // 파일 선택 버튼·파일명 표시·삭제 버튼을 HTML 로 만들어 넣는다
			'<label class="file-label" for="' + inputId + '">&#128206; 파일 선택</label>' +
			'<input type="file" name="fileName" id="' + inputId + '" ' +
			       'onchange="showFileName(this)">' +
			'<span class="file-name" id="name_' + rowCounter + '">선택된 파일 없음</span>' +
			'<button type="button" class="remove-btn" ' +
			        'onclick="removeFileRow(\'' + rowId + '\')" title="삭제">&#10005;</button>';

		container.appendChild(row);   // 만든 줄을 화면에 붙인다
	}

	/* ------------------------------------------------
	   파일을 선택했을때 파일명 표시
	   ------------------------------------------------ */
	function showFileName(input) {
		// input id에서 번호 추출 ("fileInput_3" → "3")
		var num = input.id.replace('fileInput_', '');
		var nameSpan = document.getElementById('name_' + num);   // 이 줄의 파일명을 표시할 자리를 찾는다
		if (input.files && input.files.length > 0) {   // 파일을 실제로 골랐으면
			var f = input.files[0];   // 고른 파일 중 첫 번째
			var size = f.size < 1024   // 1024바이트(1KB)보다 작으면 바이트로, 아니면 KB 로 보기 좋게 바꾼다
				? f.size + ' B'
				: (f.size < 1024*1024
					? Math.round(f.size/1024) + ' KB'
					: (f.size/(1024*1024)).toFixed(1) + ' MB');
			nameSpan.textContent = f.name + '  (' + size + ')';   // 파일명과 크기를 함께 표시한다
			nameSpan.style.color = '#333';   // 골랐다는 표시로 글자를 진하게 만든다
		} else {
			nameSpan.textContent = '선택된 파일 없음';   // 고른 파일이 없으면 안내 문구를 표시한다
			nameSpan.style.color = '#aaa';   // 아직 안 골랐다는 표시로 연한 회색으로 둔다
		}
	}

	/* ------------------------------------------------
	   파일 행 삭제
	   ------------------------------------------------ */
	function removeFileRow(rowId) {
		var container = document.getElementById('fileContainer');   // 파일 선택 줄들이 쌓이는 자리를 찾는다
		var row = document.getElementById(rowId);   // 지울 줄을 id 로 찾는다
		if (row) {   // 그 줄이 있으면
			container.removeChild(row);   // 화면에서 그 줄을 없앤다
		}
	}

	/* ------------------------------------------------
	   페이지 로드 시 첫 번째 파일 행 자동 추가
	   ------------------------------------------------ */
	window.addEventListener('load', function() {
		addFileRow();   // 화면이 열리면 파일 선택 줄을 하나 미리 만들어 둔다
	});

	/* ------------------------------------------------
	   글 등록 전 유효성 검사 (제목, 패스워드 필수)
	   ------------------------------------------------ */
	document.querySelector('form').addEventListener('submit', function(e) {
		if (!document.querySelector('input[name=title]').value.trim()) {   // 제목이 비어 있으면
			alert('제목을 입력해 주세요.');   // 입력해 달라고 알린다
			e.preventDefault(); return;   // 폼 전송을 막고 여기서 끝낸다
		}
		if (!document.querySelector('input[name=pass]').value.trim()) {   // 비밀번호가 비어 있으면
			alert('패스워드를 입력해 주세요.');   // 입력해 달라고 알린다
			e.preventDefault(); return;   // 폼 전송을 막고 여기서 끝낸다
		}
	});

	//목록 버튼 클릭 이벤트
	document.getElementById("list").addEventListener("click", function(event){
		event.preventDefault();   // a 태그의 기본 이동을 막는다. 아래에서 우리가 직접 보낼 것이다
		location.href="<%=contextPath%>/FileBoard/list.bo?nowPage=<%=nowPage%>&nowBlock=<%=nowBlock%>";   // 보던 페이지 번호를 그대로 달고 목록으로 돌아간다
	});
</script>
