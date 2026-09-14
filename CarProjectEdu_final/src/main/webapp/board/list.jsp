<%@page import="Vo.BoardVo"%>
<%@ page language="java" contentType="text/html; charset=utf-8"  pageEncoding="utf-8"%>
<%
    // 요청 글자 방식을 UTF-8 로 정한다
    request.setCharacterEncoding("UTF-8");

    // 프로젝트 경로 얻기  예) "/CarProject"  -> 아래 모든 링크 주소를 만들 때 사용
    String contextPath = request.getContextPath();
%>

<%--
 ============================================================================
  board/list.jsp  -  자유게시판 글 목록 화면

  CarMain.jsp 의 가운데 영역에 끼워져(include) 보이는 화면 조각이다.
    요청 흐름 : Top.jsp 자유게시판 메뉴 -> /Board/list.bo -> 사장(BoardController)
               -> 조회한 페이지 정보(page)와 글 목록(list)을 request 에 담아 -> 이 화면

  이 화면의 표 스타일(.board-container, .table 등)은 css/app.css 의
  "6-11. 게시판 목록 공용" 절에 있다. 자료실(fileboardlist.jsp)도 같은 구조와
  같은 CSS 클래스를 함께 쓰므로, 스타일을 이 파일 안에 두지 않고 공용 CSS 로 옮겨
  두 화면이 항상 같은 모양을 유지하고 한 번만 고치면 되게 했다.
 ============================================================================
--%>

<script type="text/javascript">
    // 글 제목 <a> 링크를 클릭하면 실행되는 함수. 아래 숨겨진 form(frmRead)을 통해
    // 클릭한 글의 번호를 사장(BoardController)의 /Board/read.bo 로 전송한다
    function fnRead(val){

        // 화면 맨 아래의 <form name="frmRead"> 안 <input type="hidden" name="b_idx"> 에 글번호를 채운다
        document.frmRead.b_idx.value = val;

        // 그 폼이 전송될 주소를 정한다
        document.frmRead.action = "<%=contextPath%>/Board/read.bo";

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
            return false;   // false 를 돌려주면 폼 전송이 취소된다 (onsubmit="fnSearch(); return false;" 와 함께 동작한다)

        // 검색어가 있으면 검색 폼을 사장(BoardController)의 /Board/searchlist.bo 로 전송한다
        }else{
            document.frmSearch.submit();
        }
    }
</script>


<%
    /* ================================================================================
       [페이징 계산은 이 화면이 하지 않는다]

       예전에는 사장(Controller)이 게시판의 "전체 글 목록"을 그대로 넘겨주면, 이
       화면의 스크립틀릿이 총 건수·전체 페이지 수·블록 번호 같은 값을 전부 직접
       계산하고, for 문으로 전체 목록 중 5건만 잘라서 출력했다. 이 방식은 1페이지
       (5건)를 보여주기 위해서도 매번 전체 글을 다 읽어야 했고, 계산 코드가
       자료실(fileboardlist.jsp)에도 똑같이 복사되어 있어 한쪽만 고치면 두 화면의
       동작이 달라지는 문제가 있었다.

       지금은 부장(BoardService)이 DB 에서 이 페이지에 필요한 5건만 조회하고,
       페이지 번호 계산까지 마친 결과를 PageResult 객체(아래의 pageInfo)에 담아
       넘겨준다. 이 화면은 계산은 하지 않고 "받은 값을 그대로 출력"만 한다.
       자세한 계산 방식은 Vo/PageResult.java, Service/BoardService.serviceBoardPage()
       를 참고.
       ================================================================================ */

    // 현재 페이지의 글 목록 (딱 5건만 들어 있다)
    // request.getAttribute() 는 Object 를 돌려주므로 형변환 검사를 할 수 없다(문법상 어쩔 수 없는 경고를 끈다)
    @SuppressWarnings("unchecked")
    java.util.List<BoardVo> list = (java.util.List<BoardVo>)request.getAttribute("list");
    if(list == null){
        list = new java.util.ArrayList<BoardVo>();
    }

    /* 페이지 정보 (전체 건수, 전체 페이지 수, 현재 블록 등 계산이 끝난 값)

       [주의] 변수 이름을 page 로 지으면 안 된다.

         JSP 는 화면이 서블릿으로 변환될 때 아래 9개 변수를 자동으로 만들어 준다.
             request, response, session, out, application,
             config, pageContext, exception, page
         이것을 "내장(암시적) 객체" 라고 한다.
         그중 page 는 자바의 this 처럼 "현재 페이지 객체 자신"을 가리킨다.

         그래서 스크립틀릿에서 page 라는 변수를 선언하면
             Duplicate local variable page
         컴파일 오류가 나면서 화면 전체가 500 에러가 된다.

         request.getAttribute("page") 의 "page" 는 서버(Controller)가 값을 담을 때
         쓴 이름표라서 그대로 두어도 문제가 없다. 충돌하는 것은 자바 변수 이름뿐이다. */
    //request.getAttribute() 는 Object 를 돌려주므로 형변환 검사를 할 수 없다(문법상 어쩔 수 없는 경고를 끈다)
    @SuppressWarnings("unchecked")
    Vo.PageResult<BoardVo> pageInfo = (Vo.PageResult<BoardVo>)request.getAttribute("page");

    /* 검색 조건 유지
         검색 후 2페이지를 누르면 검색어가 사라지면 안 된다.
         그래서 페이지 링크에 검색 조건을 함께 붙인다. */
    String key  = (String)request.getAttribute("key");
    String word = (String)request.getAttribute("word");

    String searchQuery = "";
    boolean searching = (word != null && !word.trim().isEmpty());

    if(searching){
        searchQuery = "&key="  + java.net.URLEncoder.encode(key == null ? "" : key, "UTF-8")
                    + "&word=" + java.net.URLEncoder.encode(word, "UTF-8");
    }

    //검색 중이면 페이지 이동도 검색 주소로 보낸다
    String listUrl = searching ? (contextPath + "/Board/searchlist.bo")
                               : (contextPath + "/Board/list.bo");
%>

<%-- ===== 게시판 전체 컨테이너 ===== --%>
<div class="board-container">

    <%-- 게시판 제목 --%>
    <div class="board-header">
        &nbsp;&nbsp;&nbsp;
        <span class="board-title-text">자유게시판</span>
    </div>

    <%-- 상단 구분선 --%>
    <div class="board-divider">
        <hr/>
    </div>

    <%-- 게시판 안내 배너 --%>
    <div class="board-banner">
        <span class="board-banner-text">자유롭게 소통하는 SM렌탈 커뮤니티 게시판입니다.</span>
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
                </tr>
            </thead>

            <tbody>
<%
    //조회된 글이 없으면 안내 문구 한 줄만 출력
    if(list.isEmpty()){
%>
                <tr>
                    <td colspan="5" class="empty">
                        <%= searching ? "검색 결과가 없습니다." : "등록된 글이 없습니다." %>
                    </td>
                </tr>
<%
    }else{
        /* 받은 목록을 그대로 출력한다.
           이미 그 페이지의 5건만 담겨 있으므로 index 계산이 필요 없다. */
        for(BoardVo vo : list){
%>
                <tr>

                    <%-- 글 번호 --%>
                    <td data-label="번호"><%=vo.getB_idx()%></td>

                    <%-- 글 제목 : 모바일에서는 카드의 머리글이 되므로 cell-title 을 붙인다 --%>
                    <td class="cell-title">
                        <%
                            //답변글이면 들여쓰기 표시 (깊이에 비례해 여백을 준다)
                            if(vo.getB_level() > 0){
                                int indent = vo.getB_level() * 12;
                        %>
                            <span style="display:inline-block; width:<%=indent%>px;"></span>
                            <span class="reply-indent">&#8627;</span>
                        <%
                            }
                        %>
                        <%--
                          [보안] 글 제목을 이스케이프해서 출력한다.
                          제목에 script 태그를 저장하면 목록을 열어본 모든 사람의
                          브라우저에서 그 코드가 실행된다(저장형 XSS).

                          [제목이 빈 글도 다룰 수 있게 한 부분]
                          서버가 지금은 빈 제목을 막지만, 검증을 추가하기 전에
                          이미 저장된 글이 남아 있을 수 있다. 그런 글은 <a> 안에
                          글자가 하나도 없어 링크가 화면에 보이지도, 눌리지도
                          않아 목록에는 한 줄이 보이는데 열지도 지우지도 못하는
                          상태가 된다. 그래서 제목이 비어 있으면 대체 문구를 보여준다.
                        --%>
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
        <form action="<%=contextPath%>/Board/searchlist.bo"
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

        <%-- 새 글쓰기 버튼 : 로그인했을 때만 보여준다 --%>
        <div class="write-btn-area">
            <%
                String id = (String)session.getAttribute("id");

                if(id != null){
            %>
                <%-- 글쓰기를 마치고 돌아올 페이지 번호를 함께 넘긴다 (pageInfo 에서 꺼낸다) --%>
                <button type="button"
                        id="newContent"
                        class="btn-write"
                        onclick="location.href='<%=contextPath%>/Board/write.bo?nowPage=<%=(pageInfo == null ? 0 : pageInfo.getNowPage())%>&nowBlock=<%=(pageInfo == null ? 0 : pageInfo.getNowBlock())%>'">
                    ✏ 새 글쓰기
                </button>
            <%}%>
        </div>

    </div>
    <%-- board-tools 끝 --%>


    <%-- ===== 페이지 번호 표시 영역 (1 2 3 ...) =====
               계산은 모두 PageResult(pageInfo) 가 끝낸 값을 그대로 꺼내 쓴다
               (blockStartPage/blockEndPage = 이 블록에 보여줄 페이지 범위,
                hasPrevBlock/hasNextBlock = 이전·다음 버튼을 보여줄지 여부) --%>
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
    <input type="hidden"  name="nowPage"  value="<%=(pageInfo == null ? 0 : pageInfo.getNowPage())%>" />
    <input type="hidden"  name="nowBlock" value="<%=(pageInfo == null ? 0 : pageInfo.getNowBlock())%>"/>
</form>
