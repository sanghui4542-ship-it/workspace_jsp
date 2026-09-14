<%@page import="Vo.FileBoardVo"%>
<%@ page language="java" contentType="text/html; charset=utf-8"  pageEncoding="utf-8"%>
<%
	request.setCharacterEncoding("UTF-8");
	String contextPath = request.getContextPath();
%>

<%--
 ============================================================================
  board/fileboardlist.jsp  -  자료실(파일게시판) 글 목록 화면

  CarMain.jsp 의 가운데 영역에 끼워져(include) 보이는 화면 조각이다.
    요청 흐름 : Top.jsp 공지사항 메뉴 -> /FileBoard/list.bo -> 사장(FileBoardController)
               -> 조회한 페이지 정보(page)와 글 목록(list)을 request 에 담아 -> 이 화면

  구조와 CSS 클래스(.board-container, .table 등)는 자유게시판의 board/list.jsp
  와 완전히 같다 (공용 CSS 는 css/app.css 의 "6-11. 게시판 목록 공용" 절 참고).
  한쪽 게시판의 화면 구조를 고치면 다른 쪽도 같은 방법으로 맞춰 고친다.
--%>

<%
	//현재 페이지의 글 목록 (딱 5건만 들어 있다)
	//request.getAttribute() 는 Object 를 돌려주므로 형변환 검사를 할 수 없다(문법상 어쩔 수 없는 경고를 끈다)
	@SuppressWarnings("unchecked")
	java.util.List<FileBoardVo> list = (java.util.List<FileBoardVo>)request.getAttribute("list");
	if(list == null){
		list = new java.util.ArrayList<FileBoardVo>();
	}

	/* 페이지 정보 (전체 건수, 전체 페이지 수, 현재 블록 등 계산이 끝난 값)
	   [주의] 변수 이름을 page 로 지으면 안 된다 (자세한 이유는 board/list.jsp 의 같은 위치 주석 참고) */
	//request.getAttribute() 는 Object 를 돌려주므로 형변환 검사를 할 수 없다(문법상 어쩔 수 없는 경고를 끈다)
	@SuppressWarnings("unchecked")
	Vo.PageResult<FileBoardVo> pageInfo = (Vo.PageResult<FileBoardVo>)request.getAttribute("page");

	//조회된 글이 하나도 없으면 0, 있으면 pageInfo 가 계산해 둔 값을 그대로 쓴다
	int nowPage  = (pageInfo != null) ? pageInfo.getNowPage()  : 0;
	int nowBlock = (pageInfo != null) ? pageInfo.getNowBlock() : 0;

	/* 검색 조건 유지
	     검색 후 2페이지를 누르면 검색어가 사라지면 안 된다.
	     그래서 페이지 링크에 검색 조건을 함께 붙인다. (list.jsp 와 같은 방식) */
	String key  = (String)request.getAttribute("key");
	String word = (String)request.getAttribute("word");

	String searchQuery = "";
	boolean searching = (word != null && !word.trim().isEmpty());

	if(searching){
		searchQuery = "&key="  + java.net.URLEncoder.encode(key == null ? "" : key, "UTF-8")
		            + "&word=" + java.net.URLEncoder.encode(word, "UTF-8");
	}

	//검색 중이면 페이지 이동도 검색 주소로 보낸다
	String listUrl = searching ? (contextPath + "/FileBoard/searchlist.bo")
	                           : (contextPath + "/FileBoard/list.bo");
%>

<%-- ===== 게시판 전체 컨테이너 ===== --%>
<div class="board-container">

    <%-- 게시판 제목 --%>
    <div class="board-header">
        &nbsp;&nbsp;&nbsp;
        <span class="board-title-text">파일 게시판</span>
    </div>

    <%-- 상단 구분선 --%>
    <div class="board-divider">
        <hr/>
    </div>

    <%-- 게시판 안내 배너 --%>
    <div class="board-banner">
        <span class="board-banner-text">&#128193; 자료를 올리고 내려받는 SM렌탈 파일 게시판입니다.</span>
    </div>

    <%-- ===== 게시글 목록 : class="table table-cards" 를 주면 768px 미만에서
               자동으로 카드 모양으로 바뀐다 (자세한 원리는 css/app.css 의
               "4-6-1. 반응형 데이터 표" 절, data-label 속성 참고) ===== --%>
    <div class="table-wrap">
        <table class="table table-cards">

            <thead>
                <tr>
                    <th>번호</th>
                    <th>제목</th>
                    <th>이름</th>
                    <th>날짜</th>
                    <th>조회수</th>
                    <th>다운로드</th>
                </tr>
            </thead>

            <tbody>
<%
	//조회된 글이 없으면 안내 문구 한 줄만 출력
	if(list.isEmpty()){
%>
                <tr>
                    <td colspan="6" class="empty">
                        <%= searching ? "검색 결과가 없습니다." : "등록된 글이 없습니다." %>
                    </td>
                </tr>
<%
	}else{
		/* 받은 목록을 그대로 출력한다.
		   이미 그 페이지의 5건만 담겨 있으므로 index 계산이 필요 없다. (board/list.jsp 와 같은 방식) */
		for(FileBoardVo vo : list){
%>
                <tr>

                    <%-- 글 번호 --%>
                    <td data-label="번호"><%=vo.getB_idx()%></td>

                    <%-- 글 제목 : 모바일에서는 카드의 머리글이 되므로 cell-title 을 붙인다 --%>
                    <td class="cell-title">
                        <%
                            //답변글이면 들여쓰기 표시 (깊이에 비례해 여백을 준다)
                            if(vo.getB_level() > 0){
                                int indent = vo.getB_level() * 10;
                        %>
                            <span style="display:inline-block; width:<%=indent%>px;"></span>
                            <span class="reply-indent">&#8627;</span>
                        <%
                            }
                        %>
                        <%-- [보안] 글 제목을 이스케이프해서 출력한다 (자세한 이유는 board/list.jsp 참고).
                             [제목이 빈 글도 열 수 있게] 자유게시판 목록과 같은 처리다. --%>
                        <%
                            String safeTitle = util.HtmlUtil.escape(vo.getB_title());
                            if (safeTitle == null || safeTitle.trim().isEmpty()) {
                                safeTitle = "(제목 없음)";
                            }
                        %>
                        <a href="javascript:fnRead('<%=vo.getB_idx()%>')"
                           class="title-link"><%=safeTitle%></a>
                    </td>

                    <%-- 작성자 이름 (이스케이프) --%>
                    <td data-label="이름"><%=util.HtmlUtil.escape(vo.getB_name())%></td>

                    <%-- 작성 날짜 --%>
                    <td data-label="날짜"><%=vo.getB_date()%></td>

                    <%-- 조회수 --%>
                    <td data-label="조회수"><%=vo.getB_cnt()%></td>

                    <%-- 첨부파일 다운로드 횟수 --%>
                    <td data-label="다운로드"><%=vo.getDowncount()%></td>

                </tr>
<%
		}//for
	}//else
%>
            </tbody>

        </table>
    </div>
    <%-- table-wrap 끝 --%>


    <%-- ===== 검색 폼 + 글쓰기 버튼 영역 ===== --%>
    <div class="board-tools">

        <%-- 검색 폼 : onsubmit 이 fnSearch() 를 먼저 실행해 검색어를 확인한다 --%>
        <form action="<%=contextPath%>/FileBoard/searchlist.bo"
              method="post"
              name="frmSearch" onsubmit="fnSearch(); return false;"
              class="search-form">

            <%-- 검색 기준 선택 (제목+내용 or 작성자) --%>
            <select name="key">
                <option value="titleContent">제목 + 내용</option>
                <option value="name">작성자</option>
            </select>

            <%-- 검색어 입력 --%>
            <input type="text" name="word" id="word" placeholder="검색어 입력"/>

            <%-- 검색 버튼 --%>
            <input type="submit" value="검색"/>

        </form>

        <%-- 새 글쓰기 버튼 : 로그인한 사람에게만 보인다 --%>
        <div class="write-btn-area">
            <%
                String id = (String)session.getAttribute("id");

                if(id != null){
            %>
                <button type="button"
                        id="newContent"
                        class="btn-write"
                        onclick="location.href='<%=contextPath%>/FileBoard/write.bo?nowPage=<%=nowPage%>&nowBlock=<%=nowBlock%>'">
                    &#9998; 새 글쓰기
                </button>
            <%}%>
        </div>

    </div>
    <%-- board-tools 끝 --%>


    <%-- ===== 페이지 번호 표시 영역 (1 2 3 ...) =====
               계산은 모두 PageResult(pageInfo) 가 끝낸 값을 그대로 꺼내 쓴다
               (자세한 원리는 board/list.jsp 의 같은 위치 주석 참고) --%>
    <div class="page-control">
        Go To Page

        <%
            //조회된 글이 하나도 없으면 페이지 번호를 표시하지 않는다
            if(pageInfo != null && !pageInfo.isEmpty()){
        %>
                <%-- [1] 이전 블록으로 이동 --%>
                <% if(pageInfo.isHasPrevBlock()){ %>
                    <a href="<%=listUrl%>?nowPage=<%=pageInfo.getPrevBlockPage()%><%=searchQuery%>"
                       class="block-link">&#9664; 이전 <%=pageInfo.getPagePerBlock()%>개</a>
                    &nbsp;&nbsp;
                <% } %>

                <%-- [2] 현재 블록의 페이지 번호들
                         페이지 번호는 내부적으로 0부터 세지만 화면에는 1부터 보여준다 --%>
                <% for(int p = pageInfo.getBlockStartPage(); p <= pageInfo.getBlockEndPage(); p++){ %>

                    <% if(p == pageInfo.getNowPage()){ %>
                        <%-- 지금 보고 있는 페이지는 링크 대신 강조 표시 --%>
                        <strong class="page-current"><%=p + 1%></strong>
                    <% }else{ %>
                        <a href="<%=listUrl%>?nowPage=<%=p%><%=searchQuery%>"><%=p + 1%></a>
                    <% } %>
                    &nbsp;&nbsp;&nbsp;

                <% } %>

                <%-- [3] 다음 블록으로 이동 --%>
                <% if(pageInfo.isHasNextBlock()){ %>
                    <a href="<%=listUrl%>?nowPage=<%=pageInfo.getNextBlockPage()%><%=searchQuery%>"
                       class="block-link">&#9654; 다음 <%=pageInfo.getPagePerBlock()%>개</a>
                <% } %>

                <%-- [4] 전체 건수 안내 --%>
                <div class="page-summary">
                    전체 <%=pageInfo.getTotalRecord()%>건 &nbsp;|&nbsp;
                    <%=pageInfo.getNowPage() + 1%> / <%=pageInfo.getTotalPage()%> 페이지
                </div>
        <%
            }//if
        %>

    </div>
    <%-- page-control 끝 --%>

</div>
<%-- board-container 끝 --%>


<%-- ===== 글 제목 링크 클릭 시 사용하는 숨은 폼
           fnRead() 함수가 이 폼에 글번호를 채우고 전송한다 ===== --%>
<form action="" name="frmRead">
	<input type="hidden"  name="b_idx" value=""/> <%-- 조회할 글번호가 fnRead()에서 채워진다 --%>
	<%-- 글 상세를 본 뒤 [목록] 버튼으로 돌아올 때 같은 페이지로 오도록 페이지 번호를 함께 전달 --%>
	<input type="hidden"  name="nowPage" value="<%=nowPage%>" />
	<input type="hidden"  name="nowBlock" value="<%=nowBlock%>"/>
</form>

<script type="text/javascript">
	// 글 제목 <a> 링크를 클릭하면 실행되는 함수. 위 숨겨진 form(frmRead)을 통해
	// 클릭한 글의 번호를 사장(FileBoardController)의 /FileBoard/read.bo 로 전송한다
	function fnRead(val){

		// 화면 위 <form name="frmRead"> 안 <input type="hidden" name="b_idx"> 에 글번호를 채운다
		document.frmRead.b_idx.value = val;

		// 그 폼이 전송될 주소를 정한다
		document.frmRead.action = "<%=contextPath%>/FileBoard/read.bo";

		// 폼을 실제로 전송한다 (화면에는 보이지 않는 폼이라 사용자는 페이지 이동만 보게 된다)
		document.frmRead.submit();
	}

	// 검색 폼의 onsubmit 에서 호출되는 함수. 검색어가 비어 있으면 전송을 막고,
	// 입력되어 있으면 검색 폼(frmSearch)을 실제로 전송한다
	function fnSearch(){

		// 검색어 입력칸의 값을 얻는다
		let word = document.getElementById("word").value;

		// 검색어가 없으면 안내하고 전송을 막는다
		if(word == null || word == ""){
			window.alert("검색어를 입력하세요");
			document.getElementById("word").focus();   // 검색어 칸에 커서를 놓아 준다 (바로 칠 수 있게)
			return false;   // false 를 돌려주면 폼 전송이 취소된다

		// 검색어가 있으면 검색 폼을 사장(FileBoardController)의 /FileBoard/searchlist.bo 로 전송한다
		}else{
			document.frmSearch.submit();
		}
	}
</script>
