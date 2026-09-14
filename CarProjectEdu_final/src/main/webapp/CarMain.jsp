<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%-- JSTL 태그를 쓰기 위해 불러온다 (c:if, c:forEach, fmt:formatNumber 등) --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<%-- 반응형 필수 메타 태그 : 모바일에서 화면 너비를 기기 너비 그대로 쓰게 한다 --%>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>SM렌탈 - 자동차 렌탈 서비스</title>

<%--
 ============================================================================
  CarMain.jsp  -  이 프로젝트의 단 하나뿐인 HTML 문서

  <html><head><body> 는 이 파일에만 있다. Top.jsp, Center.jsp, Bottom.jsp,
  그리고 메뉴를 눌러 바뀌는 가운데 화면들은 모두 "조각(fragment)"이다.
  즉 태그 내용만 출력하고, 각자 <html><head><body>를 만들지 않는다.
  CSS(app.css)와 공용 스크립트(app.js)도 여기 <head> 에서 한 번만 불러온다.

  화면 구조
    <head>            : app.css, app.js 를 딱 1번 불러온다
    <body>
      .main-wrapper
        Top.jsp        (상단 헤더 + 메뉴)
        .main-content  (${center} 값에 따라 바뀌는 가운데 화면)
        Bottom.jsp     (하단 푸터)
      챗봇 위젯         (오른쪽 아래 플로팅 버튼 + 대화창, 모든 화면에 공통으로 떠 있다)

  [Bootstrap 을 쓰지 않는 이유]
    Top.jsp 와 join.jsp 가 쓰는 Bootstrap 4 클래스(navbar-expand-md, form-control 등)
    25종만 app.css 안에 직접 구현해 두었다. 103KB 짜리 프레임워크를 통째로 받는 대신
    실제로 쓰는 규칙만 약 1KB 로 넣었고, 화면 모양은 그대로다.
 ============================================================================
--%>

<%-- ?v=13 : 파일을 크게 고칠 때마다 숫자를 올린다. 그래야 브라우저가 캐시된 옛 파일 대신 새 파일을 받는다 --%>
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/app.css?v=13">

<%-- defer : HTML 을 다 읽은 뒤에 실행한다. head 에서 불러도 첫 화면 그리기를 막지 않는다 --%>
<script src="<%=request.getContextPath()%>/js/app.js?v=13" defer></script>

<style>
/* ==========================================================================
   문서 전체 레이아웃
   ========================================================================== */

/* [모든 태그] 안쪽 여백·테두리를 너비 계산에 포함시키는 규칙을 문서 전체에 적용한다
   (이렇게 해야 width:100% 인 요소에 padding 을 줘도 부모 밖으로 삐져나오지 않는다) */
* {
    box-sizing: border-box;
}

/* [전체 감싸는 틀] <div class="main-wrapper"> : Top, 가운데 화면, Bottom 을 세로로 담는다 */
.main-wrapper {
    max-width: 1200px;                       /* 최대 너비 1200px : 큰 화면에서도 이보다 넓어지지 않는다 */
    width: 100%;                             /* 너비 100% : 화면이 좁으면 화면 전체를 쓴다 */
    margin: 0 auto;                          /* 위아래 여백 0, 좌우 auto : 가운데 정렬 */
}

/* [가운데 화면 영역] <div class="main-content"> : 메뉴를 누를 때마다 안의 내용만 바뀐다 */
.main-content {
    width: 100%;                             /* 너비 100% : main-wrapper 를 꽉 채운다 */
    min-height: 500px;                       /* 최소 높이 500px : 내용이 짧아도 푸터가 위로 붙어 올라오지 않는다 */
}
</style>

</head>
<body>

<%--
 가운데에 보여줄 화면 주소 정하기
   사장(Controller)이 request 에 저장한 center 값을 꺼내 util/CenterView 의 허용 목록으로 검사한다
   허용 목록에 있으면 그 화면을, 없거나 목록 밖이면 기본 화면 Center.jsp 를 아래 <jsp:include page="${center}"/> 에 끼워 넣는다
   (검사하지 않으면 ?center=WEB-INF/web.xml 처럼 서버 설정 파일이 화면에 보인다)
--%>
<%
	pageContext.setAttribute("center", util.CenterView.resolve((String) request.getAttribute("center")));
%>

<div class="main-wrapper">

    <div>
        <jsp:include page="Top.jsp"/>
    </div>

    <div class="main-content">
        <jsp:include page="${center}"/>
    </div>

    <div>
        <jsp:include page="Bottom.jsp"/>
    </div>

</div>

<%-- ============================================================
     AI 챗봇 (Google Gemini) - 오른쪽 아래 플로팅 버튼 + 대화창
     모든 화면에 공통으로 떠 있으므로 Top/Bottom 처럼 조각으로 나누지 않고
     이 문서의 주인인 CarMain.jsp 에 직접 둔다
     ============================================================ --%>

<%
    // 챗봇 스크립트가 fetch 요청 주소를 만들 때 쓸 프로젝트 경로
    String chatbotCtx = request.getContextPath();
%>

<%-- 오른쪽 아래 동그란 버튼. 누르면 대화창이 열리고 닫힌다 (toggleChatbot, 아래 script) --%>
<div id="chatbot-btn" onclick="toggleChatbot()" title="AI 상담 챗봇">
    <span id="chatbot-btn-icon">&#128172;</span>
    <span id="chatbot-btn-close" style="display:none;">&#10005;</span>
</div>

<%-- 대화창 : 처음에는 숨겨져 있다가(css: display:none) 버튼을 누르면 나타난다 --%>
<div id="chatbot-window">

    <div id="chatbot-header">
        <div id="chatbot-header-info">
            <div id="chatbot-avatar">&#129302;</div>
            <div>
                <div id="chatbot-title">SM렌탈 AI 상담</div>
                <div id="chatbot-status">&#9679; 온라인</div>
            </div>
        </div>
        <button id="chatbot-close-btn" onclick="toggleChatbot()">&#10005;</button>
    </div>

    <%-- 말풍선들이 쌓이는 영역. 아래 script 의 appendMessage() 가 여기에 말풍선을 하나씩 추가한다 --%>
    <div id="chatbot-messages">

        <div class="chat-msg bot-msg">
            <div class="chat-bubble bot-bubble">
                안녕하세요! &#128075;<br>
                <b>SM렌탈 AI 상담사</b>입니다.<br><br>
                렌트카 관련 궁금한 점을 편하게 물어보세요!
            </div>
        </div>

        <%-- 자주 묻는 질문 버튼 4개 : 누르면 그 문장을 그대로 입력한 것처럼 전송한다 --%>
        <div id="quick-questions">
            <button class="quick-btn" onclick="sendQuickMsg('보유 차량 종류를 알려주세요')">&#128663; 차량 종류</button>
            <button class="quick-btn" onclick="sendQuickMsg('렌트 가격이 궁금합니다')">&#128176; 가격 안내</button>
            <%-- 예약 조회는 AI 를 거치지 않고 바로 조회 폼을 띄운다 (showOrderLookup, 아래 script) --%>
            <button class="quick-btn" onclick="showOrderLookup()">&#128203; 내 예약 조회</button>
            <button class="quick-btn" onclick="sendQuickMsg('추가 옵션에는 어떤 것이 있나요?')">&#9881; 추가 옵션</button>
        </div>
    </div>

    <div id="chatbot-input-area">
        <input type="text" id="chatbot-input" placeholder="메시지를 입력하세요..." onkeypress="if(event.key==='Enter') sendMessage();" />
        <button id="chatbot-send-btn" onclick="sendMessage()">&#10148;</button>
    </div>
</div>

<style>
/* ==========================================================================
   챗봇 전용 스타일
   선택자(#이름) 는 위 HTML 의 id="이름" 인 태그를, (.이름) 은 class="이름" 인 태그를 꾸민다
   ========================================================================== */

/* [플로팅 버튼] <div id="chatbot-btn"> : 화면 오른쪽 아래에 항상 떠 있는 동그란 버튼 */
#chatbot-btn {
    position: fixed;                         /* 위치 고정 : 스크롤해도 화면의 같은 자리에 계속 떠 있다 */
    bottom: 30px;                            /* 화면 아래에서 30px 띄운다 */
    right: 30px;                             /* 화면 오른쪽에서 30px 띄운다 */
    width: 60px; height: 60px;               /* 크기 : 가로 60px, 세로 60px */
    background: linear-gradient(135deg, #cc0000, #ff3333); /* 배경 : 135도 대각선으로 진한 빨강 -> 밝은 빨강 그라데이션 */
    border-radius: 50%;                      /* 모서리 둥글기 50% : 정사각형이 완전한 원이 된다 */
    display: flex;                           /* 안의 아이콘을 가운데에 놓기 위해 flex 로 만든다 */
    align-items: center; justify-content: center; /* 아이콘을 버튼 정가운데에 놓는다 */
    cursor: pointer;                         /* 마우스를 올리면 손가락 모양 */
    z-index: 99999;                          /* 쌓임 순서를 아주 높게 : 다른 어떤 요소보다도 위에 보이게 한다 */
    box-shadow: 0 4px 15px rgba(204, 0, 0, 0.4); /* 그림자 : 아래로 4px, 퍼짐 15px, 빨강 40% 투명도 (붕 떠 보이는 효과) */
    transition: transform 0.3s, box-shadow 0.3s; /* 크기와 그림자가 바뀔 때 0.3초 동안 부드럽게 바뀐다 */
}

/* [플로팅 버튼에 마우스를 올렸을 때] 살짝 커지고 그림자가 진해진다 */
#chatbot-btn:hover {
    transform: scale(1.1);                   /* 크기를 1.1배로 키운다 */
    box-shadow: 0 6px 20px rgba(204, 0, 0, 0.5); /* 그림자를 더 크고 진하게 */
}

/* [버튼 안 아이콘] 말풍선 아이콘과 X 아이콘 공통 스타일 (둘 중 하나만 보이도록 script 가 display 를 바꾼다) */
#chatbot-btn-icon, #chatbot-btn-close {
    font-size: 28px;                         /* 아이콘 크기 28px */
    color: #fff;                             /* 색 : 흰색 */
    line-height: 1;                          /* 줄 높이를 글자 크기와 같게 : 세로 가운데 정렬이 흐트러지지 않게 한다 */
}

/* [대화창] <div id="chatbot-window"> : 플로팅 버튼 위에 뜨는 채팅 상자 */
#chatbot-window {
    position: fixed;                         /* 위치 고정 : 스크롤해도 같은 자리 */
    bottom: 100px;                           /* 화면 아래에서 100px (플로팅 버튼보다 위쪽) */
    right: 30px;                             /* 화면 오른쪽에서 30px */
    width: 380px; height: 520px;             /* 크기 : 가로 380px, 세로 520px */
    background: #fff;                        /* 배경색 : 흰색 */
    border-radius: 16px;                     /* 모서리 둥글기 16px */
    box-shadow: 0 8px 40px rgba(0, 0, 0, 0.18); /* 그림자 : 아래로 8px, 퍼짐 40px, 검정 18% 투명도 (은은하게 떠 보인다) */
    z-index: 99998;                          /* 쌓임 순서 : 플로팅 버튼(99999) 바로 아래 */
    display: none;                           /* 처음엔 숨김. script 의 toggleChatbot() 이 flex 로 바꿔 보이게 한다 */
    flex-direction: column;                  /* 보일 때는 헤더-메시지-입력창을 세로로 쌓는다 */
    overflow: hidden;                        /* 자식 요소가 둥근 모서리 밖으로 삐져나오지 않게 자른다 */
    animation: chatbotSlideUp 0.3s ease-out; /* 나타날 때 아래에서 위로 살짝 떠오르는 애니메이션 0.3초 */
}

/* [나타나는 애니메이션] 투명하고 20px 아래에 있던 상태 -> 완전히 보이고 제자리인 상태 */
@keyframes chatbotSlideUp {
    from { opacity: 0; transform: translateY(20px); }  /* 시작 : 투명, 아래로 20px */
    to   { opacity: 1; transform: translateY(0); }     /* 끝   : 불투명, 제자리 */
}

/* [대화창 머리] <div id="chatbot-header"> : "SM렌탈 AI 상담" 이 적힌 빨간 띠 */
#chatbot-header {
    background: linear-gradient(135deg, #cc0000, #aa0000); /* 배경 : 대각선 빨강 그라데이션 */
    padding: 14px 18px;                      /* 안쪽 여백 : 위아래 14px, 좌우 18px */
    display: flex;                           /* 아바타·이름을 왼쪽에, 닫기 버튼을 오른쪽에 놓기 위해 flex */
    align-items: center;                     /* 세로 가운데 정렬 */
    justify-content: space-between;          /* 왼쪽 묶음과 닫기 버튼을 양 끝으로 벌린다 */
}

/* [머리 안 아바타+이름 묶음] */
#chatbot-header-info {
    display: flex;                           /* 아바타와 이름을 가로로 놓는다 */
    align-items: center;                     /* 세로 가운데 정렬 */
    gap: 10px;                               /* 아바타와 이름 사이 간격 10px */
}

/* [아바타] &#129302; (로봇 이모지) 를 담은 반투명 동그라미 */
#chatbot-avatar {
    width: 38px; height: 38px;               /* 크기 38x38px */
    background: rgba(255,255,255,0.2);       /* 배경 : 흰색 20% 투명도 (빨간 배경 위에 옅게 보인다) */
    border-radius: 50%;                      /* 완전한 원 */
    display: flex; align-items: center; justify-content: center; /* 이모지를 정가운데에 놓는다 */
    font-size: 22px;                         /* 이모지 크기 22px */
}

/* [상담 제목 글자] "SM렌탈 AI 상담" */
#chatbot-title {
    color: #fff;                             /* 흰색 */
    font-size: 15px;
    font-weight: 700;                        /* 굵게 */
}

/* [상태 글자] "● 온라인" (기본값. 바로 아래 규칙이 실제 보이는 색을 다시 정한다) */
#chatbot-status {
    color: rgba(255,255,255,0.8);
    font-size: 11px;
}

/* [상태 글자 - 실제 적용] 위 규칙보다 더 구체적인 선택자라서 이 색이 최종 적용된다 (초록색 "온라인" 표시) */
#chatbot-status > span, #chatbot-status {
    color: #7fff7f;                          /* 연한 초록색 */
    font-size: 11px;
}

/* [닫기 버튼] 대화창 오른쪽 위 X 버튼 */
#chatbot-close-btn {
    background: none;                        /* 배경 없음 (투명) */
    border: none;                            /* 테두리 없음 */
    color: #fff;                             /* 글자색 흰색 */
    font-size: 18px;
    cursor: pointer;                         /* 손가락 모양 */
    padding: 4px 8px;                        /* 안쪽 여백 : 위아래 4px, 좌우 8px (클릭 영역 확보) */
    border-radius: 50%;                      /* 마우스를 올렸을 때 배경이 원 모양으로 보이도록 미리 둥글게 */
    transition: background 0.2s;             /* 배경이 바뀔 때 0.2초 부드럽게 */
}
#chatbot-close-btn:hover {
    background: rgba(255,255,255,0.15);      /* 마우스를 올리면 옅은 흰색 원 배경이 생긴다 */
}

/* [메시지 영역] <div id="chatbot-messages"> : 말풍선들이 위에서 아래로 쌓이는 스크롤 영역 */
#chatbot-messages {
    flex: 1;                                 /* 머리·입력창이 차지하고 남은 세로 공간을 모두 차지한다 */
    overflow-y: auto;                        /* 내용이 넘치면 세로 스크롤바가 생긴다 */
    padding: 16px;                           /* 안쪽 여백 16px */
    background: #f9f9f9;                     /* 배경색 : 아주 연한 회색 (흰 말풍선과 구분되게) */
    display: flex;                           /* 말풍선들을 세로로 쌓기 위해 flex */
    flex-direction: column;                  /* 배치 방향 : 세로 (위에서 아래) */
    gap: 12px;                               /* 말풍선 사이 간격 12px */
}

/* [말풍선 줄] <div class="chat-msg"> : 말풍선 하나를 감싸는 줄 (최대 폭 제한용) */
.chat-msg {
    display: flex;                           /* 안의 말풍선을 배치하기 위해 flex */
    max-width: 85%;                          /* 최대 너비 85% : 대화창 폭을 다 채우지 않는다 (말풍선다운 느낌) */
}
.bot-msg  { align-self: flex-start; }        /* 봇 메시지는 왼쪽 정렬 */
.user-msg { align-self: flex-end; }          /* 내 메시지는 오른쪽 정렬 */

/* [말풍선 몸체] <div class="chat-bubble"> */
.chat-bubble {
    padding: 10px 14px;                      /* 안쪽 여백 : 위아래 10px, 좌우 14px */
    border-radius: 14px;                     /* 모서리 둥글기 14px (동글동글한 말풍선 모양) */
    font-size: 13px;
    line-height: 1.5;                        /* 줄 간격 : 글자 크기의 1.5배 (여러 줄일 때 읽기 편하게) */
    word-break: break-word;                  /* 긴 영어 단어도 말풍선 폭에 맞춰 강제로 줄바꿈한다 */
    white-space: pre-wrap;                   /* 글 속의 줄바꿈(\n)을 그대로 보여준다 (붙여 쓰지 않는다) */
}

/* [봇 말풍선] 흰 배경 + 왼쪽 위 모서리만 각지게 (말풍선이 왼쪽에서 나온 느낌) */
.bot-bubble {
    background: #fff;
    color: #333;
    border: 1px solid #e8e8e8;               /* 연한 회색 테두리 */
    border-top-left-radius: 4px;             /* 왼쪽 위 모서리만 살짝만 둥글게 (꼬리처럼 보이는 효과) */
}

/* [내 말풍선] 빨간 배경 + 오른쪽 위 모서리만 각지게 (말풍선이 오른쪽에서 나온 느낌) */
.user-bubble {
    background: #cc0000;
    color: #fff;
    border-top-right-radius: 4px;
}

/* [폭 넓은 말풍선] 예약 조회 입력 폼, 예약 카드처럼 안의 내용이 넓어야 하는 경우
   일반 말풍선(.chat-msg)은 max-width:85% 로 내용 크기에 맞춰 줄어드는데,
   입력칸이나 카드가 들어가면 폭이 좁아져 글자와 사진이 겹쳐 보이는 문제가 있었다.
   이 클래스가 붙은 말풍선만 폭을 강제로 넓게 확보한다 */
.chat-msg-wide { width: 85%; }
.chat-msg-wide .chat-bubble { width: 100%; }

/* [챗봇 안 입력 폼] <div class="chat-form"> : 예약 조회·취소 때 나오는 입력칸 묶음 */
.chat-form {
    display: flex;
    flex-direction: column;                  /* 입력칸과 버튼을 세로로 쌓는다 */
    gap: 6px;                                /* 항목 사이 간격 6px */
    width: 100%;
}

/* [챗봇 안 입력칸] 이 프로젝트의 터치 타깃 기준(44px)을 좁은 대화창 안에서도 지킨다 */
.chat-input-sm {
    width: 100%;
    min-height: 44px;                        /* 최소 높이 44px (손가락으로 누르기 쉬운 크기) */
    padding: 10px;
    border: 1px solid #ddd;
    border-radius: 6px;
    font-size: 13px;
    font-family: inherit;                    /* 부모 요소의 글꼴을 그대로 물려받는다 */
    box-sizing: border-box;                  /* 너비 100% 안에 여백과 테두리까지 포함한다 */
}
.chat-input-sm:focus {
    outline: none;                           /* 브라우저 기본 파란 윤곽선을 없앤다 */
    border-color: #cc0000;                   /* 대신 빨간 테두리로 선택 중임을 표시 */
}

/* [챗봇 안 버튼] 기본(빨강) 모양 */
.chat-form-btn {
    min-height: 44px;
    padding: 10px 12px;
    background: #cc0000;
    color: #fff;
    border: none;
    border-radius: 6px;
    font-size: 13px;
    font-weight: bold;
    font-family: inherit;
    cursor: pointer;
}
.chat-form-btn:hover { background: #aa0000; }

/* [위험 버튼] 예약 취소 확정처럼 되돌릴 수 없는 동작 - 검정으로 눈에 띄게 */
.chat-form-btn-danger { background: #333; }
.chat-form-btn-danger:hover { background: #000; }

/* [보조 버튼] "그만두기" 처럼 덜 중요한 동작 - 연한 회색 */
.chat-form-btn-ghost {
    background: #f5f5f5;
    color: #555;
    font-weight: normal;                     /* 기본 버튼과 달리 굵지 않게 (덜 강조) */
}
.chat-form-btn-ghost:hover { background: #e8e8e8; }

/* [작은 안내 글자] "비밀번호는 대화 내용으로 저장되지 않습니다" 같은 부가 설명 */
.chat-form-note {
    margin: 2px 0 0;                         /* 위쪽 여백만 2px (버튼과 살짝 띄운다) */
    font-size: 11px;
    color: #999;
    line-height: 1.4;
}

/* [예약 카드] <div class="chat-order"> : 조회된 예약 1건을 보여주는 카드 */
.chat-order { width: 100%; }

/* [카드 머리] 차 사진 + 이름 + 예약번호 줄 */
.chat-order-head {
    display: flex;
    align-items: center;
    gap: 8px;
    padding-bottom: 8px;
    margin-bottom: 8px;
    border-bottom: 1px solid #eee;           /* 아래쪽 얇은 선으로 나머지 정보와 구분한다 */
}

/* [카드 안 차 사진] 작은 썸네일 */
.chat-order-img {
    width: 56px; height: 38px;               /* 크기 56x38px (가로로 긴 썸네일) */
    object-fit: cover;                       /* 사진 비율이 달라도 이 크기를 꽉 채우도록 잘라서 보여준다 */
    border-radius: 4px;
    background: #f5f5f5;                     /* 사진이 없거나 로딩 전일 때 보이는 배경색 */
    flex: 0 0 auto;                          /* 다른 글자가 길어져도 사진 크기는 그대로 유지 */
}

.chat-order-name  { font-weight: bold; font-size: 13px; }  /* 차량 이름 */
.chat-order-no    { font-size: 11px; color: #999; }        /* 예약번호 (작고 연하게) */
.chat-order-row   { font-size: 12px; color: #666; line-height: 1.7; } /* 시작일·기간·옵션 각 줄 */

/* [총 금액] 오른쪽 정렬 + 빨간 강조 */
.chat-order-total {
    margin-top: 6px;
    font-size: 14px;
    font-weight: bold;
    color: #cc0000;
    text-align: right;
}

/* [예약 취소 버튼] 카드 맨 아래 흰 바탕 빨간 테두리 버튼 */
.chat-cancel-btn {
    width: 100%;
    min-height: 44px;
    margin-top: 8px;
    padding: 10px;
    background: #fff;
    color: #cc0000;
    border: 1px solid #cc0000;
    border-radius: 6px;
    font-size: 12px;
    font-weight: bold;
    font-family: inherit;
    cursor: pointer;
}
.chat-cancel-btn:hover { background: #cc0000; color: #fff; } /* 마우스를 올리면 색이 반전된다 */

/* [자주 묻는 질문 버튼 묶음] <div id="quick-questions"> */
#quick-questions {
    display: flex;
    flex-wrap: wrap;                         /* 버튼이 많으면 다음 줄로 넘긴다 */
    gap: 6px;
    padding: 4px 0;
}

/* [자주 묻는 질문 버튼 1개] 알약 모양의 흰 버튼 */
.quick-btn {
    padding: 6px 12px;
    background: #fff;
    border: 1px solid #ddd;
    border-radius: 16px;                     /* 높이의 절반 이상이라 알약 모양이 된다 */
    font-size: 12px;
    color: #555;
    cursor: pointer;
    transition: all 0.2s;
    white-space: nowrap;                     /* 버튼 글자가 줄바꿈되지 않게 한다 */
}
.quick-btn:hover {
    background: #cc0000;
    color: #fff;
    border-color: #cc0000;
}

/* [입력 중 표시] <div class="typing-indicator"> : 점 3개가 통통 튀는 말풍선 */
.typing-indicator {
    display: flex;
    gap: 4px;                                /* 점 사이 간격 4px */
    padding: 10px 14px;
    background: #fff;
    border: 1px solid #e8e8e8;
    border-radius: 14px;
    border-top-left-radius: 4px;             /* 봇 말풍선과 같은 모양 (왼쪽에서 나온 느낌) */
    align-self: flex-start;                  /* 봇 메시지처럼 왼쪽 정렬 */
}

/* [점 1개] */
.typing-dot {
    width: 8px; height: 8px;
    background: #ccc;
    border-radius: 50%;                      /* 동그란 점 */
    animation: typingBounce 1.2s infinite;   /* 1.2초 주기로 위아래로 튀는 애니메이션을 무한 반복 */
}
.typing-dot:nth-child(2) { animation-delay: 0.2s; }  /* 2번째 점은 0.2초 늦게 시작 */
.typing-dot:nth-child(3) { animation-delay: 0.4s; }  /* 3번째 점은 0.4초 늦게 시작 (순서대로 튀는 효과) */

/* [튀는 동작] 대부분 제자리, 중간(30%)에만 위로 6px 튀어오른다 */
@keyframes typingBounce {
    0%, 60%, 100% { transform: translateY(0); }
    30% { transform: translateY(-6px); }
}

/* [입력창 영역] <div id="chatbot-input-area"> : 대화창 맨 아래 입력칸 + 보내기 버튼 */
#chatbot-input-area {
    display: flex;
    padding: 12px;
    background: #fff;
    border-top: 1px solid #eee;              /* 위쪽에 얇은 선으로 메시지 영역과 구분한다 */
    gap: 8px;
}

/* [메시지 입력칸] */
#chatbot-input {
    flex: 1;                                 /* 보내기 버튼이 차지하고 남은 가로 공간을 모두 차지한다 */
    padding: 10px 14px;
    border: 1px solid #ddd;
    border-radius: 20px;                     /* 알약 모양 */
    font-size: 13px;
    outline: none;
    transition: border-color 0.2s;
}
#chatbot-input:focus {
    border-color: #cc0000;                   /* 선택 중이면 빨간 테두리 */
}

/* [보내기 버튼] 종이비행기 모양 아이콘이 든 빨간 동그라미 */
#chatbot-send-btn {
    width: 40px; height: 40px;
    background: #cc0000;
    color: #fff;
    border: none;
    border-radius: 50%;                      /* 완전한 원 */
    font-size: 18px;
    cursor: pointer;
    display: flex;
    align-items: center; justify-content: center; /* 아이콘을 정가운데에 놓는다 */
    transition: background 0.2s;
}
#chatbot-send-btn:hover {
    background: #aa0000;
}

/* [좁은 화면(480px 이하, 휴대폰)일 때만 적용] 대화창이 화면 거의 전체를 채우게 한다 */
@media (max-width: 480px) {
    #chatbot-window {
        width: calc(100vw - 20px);           /* 너비 : 화면 너비에서 좌우 여백 20px 을 뺀 만큼 (거의 꽉 채움) */
        height: calc(100vh - 120px);         /* 높이 : 화면 높이에서 위아래 여백 120px 을 뺀 만큼 */
        right: 10px;                         /* 오른쪽 여백을 10px 로 줄인다 */
        bottom: 80px;                        /* 아래 여백을 80px 로 줄인다 */
        border-radius: 12px;                 /* 모서리 둥글기를 조금 줄인다 */
    }
    #chatbot-btn {
        right: 16px; bottom: 16px;           /* 플로팅 버튼 위치도 화면 가장자리에 더 가깝게 */
        width: 54px; height: 54px;           /* 버튼 크기를 60px 에서 54px 로 살짝 줄인다 */
    }
}
</style>

<script>
/* ============================================================================
   AI 챗봇 스크립트

   상태 변수
     chatHistory   : 지금까지 주고받은 대화 (최근 20개만 서버로 보낸다)
     isChatbotOpen : 대화창이 열려 있는지
     isSending     : 메시지를 보내는 중인지 (버튼 연타로 중복 전송되는 것을 막는다)

   함수 목록
     toggleChatbot()                    대화창 열기/닫기
     sendMessage(), sendQuickMsg(글)     메시지 보내기 (AI 응답)
     appendMessage(글, 보낸사람)          말풍선 하나를 대화 영역에 추가
     chatEscape(글)                     AI 응답을 화면에 넣기 전 안전한 글자로 바꾸기
     showOrderLookup(), doOrderLookup() 예약 조회 폼 띄우기 / 조회 실행
     renderOrderCards(목록)              조회된 예약들을 카드로 그리기
     askCancelOrder(번호), doCancelOrder(번호), closeCancelForm()
                                         예약 취소 폼 띄우기 / 취소 실행 / 취소 중단
     looksLikeOrderIntent(글)           입력한 문장이 "예약 조회/취소" 의도인지 판단
     showTypingIndicator(), removeTypingIndicator()
                                         "입력 중..." 표시 보이기/지우기
   ============================================================================ */

var chatbotContextPath = "<%=chatbotCtx%>";
var chatHistory = [];
var isChatbotOpen = false;
var isSending = false;

/* ----- 대화창 열기/닫기 ----- */
function toggleChatbot() {
    var win = document.getElementById("chatbot-window");
    var iconChat = document.getElementById("chatbot-btn-icon");
    var iconClose = document.getElementById("chatbot-btn-close");

    if (isChatbotOpen) {
        win.style.display = "none";
        iconChat.style.display = "inline";
        iconClose.style.display = "none";
    } else {
        win.style.display = "flex";                          // flex 라야 안의 세로 배치(header-messages-input)가 유지된다
        iconChat.style.display = "none";
        iconClose.style.display = "inline";
        document.getElementById("chatbot-input").focus();     // 열자마자 바로 입력할 수 있게 커서를 놓는다
    }
    isChatbotOpen = !isChatbotOpen;
}

/* ----- 메시지 보내기 ----- */
function sendMessage() {
    if (isSending) return;                                    // 이미 보내는 중이면 무시 (중복 전송 방지)

    var input = document.getElementById("chatbot-input");
    var msg = input.value.trim();
    if (!msg) return;                                         // 빈 내용은 보내지 않는다

    var quickQ = document.getElementById("quick-questions");
    if (quickQ) quickQ.style.display = "none";                // 대화가 시작됐으니 추천 질문 버튼은 숨긴다

    appendMessage(msg, "user");
    input.value = "";

    // "예약 조회/취소" 의도로 보이면 AI 대신 조회 폼을 띄운다
    // (AI 는 DB 를 볼 수 없어 "홈페이지에서 확인하세요" 같은 답만 하게 되므로, 규칙으로 되는 일에 AI 를 쓰지 않는다)
    if (looksLikeOrderIntent(msg)) {
        showOrderLookup();
        return;
    }

    chatHistory.push({ role: "user", text: msg });

    showTypingIndicator();
    isSending = true;

    var formData = new URLSearchParams();
    formData.append("message", msg);
    var recentHistory = chatHistory.slice(-20);               // 최근 20개만 보낸다 (토큰 절약)
    var historyToSend = recentHistory.slice(0, -1);           // 방금 넣은 user 메시지는 서버가 자동으로 붙이므로 제외
    formData.append("history", JSON.stringify(historyToSend));

    fetch(chatbotContextPath + "/Chatbot/send.do", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
            "X-Requested-With": "XMLHttpRequest"              // 서버가 비동기 요청으로 인식해 짧은 글자로만 답하게 한다
        },
        body: formData.toString()
    })
    .then(function(response) { return response.json(); })
    .then(function(data) {
        removeTypingIndicator();
        var reply = data.reply || "응답을 받지 못했습니다.";
        appendMessage(reply, "bot");
        chatHistory.push({ role: "model", text: reply });     // 다음 질문 때 문맥으로 함께 보내기 위해 기록에 남긴다
        isSending = false;
    })
    .catch(function(error) {
        removeTypingIndicator();
        appendMessage("네트워크 오류가 발생했습니다. 다시 시도해주세요.", "bot");
        isSending = false;
        console.error("Chatbot error:", error);
    });
}

/* ----- 자주 묻는 질문 버튼 클릭 : 그 문장을 그대로 입력한 것처럼 전송한다 ----- */
function sendQuickMsg(msg) {
    document.getElementById("chatbot-input").value = msg;
    sendMessage();
}

/*
 [보안] chatEscape : AI 응답을 화면(innerHTML)에 넣기 전에 반드시 거치는 안전장치

   AI 응답과 DB 값은 "외부에서 들어온, 믿을 수 없는 글자"다.
   이스케이프 없이 innerHTML 에 그대로 넣으면, 사용자가 <script> 같은 태그를
   포함한 질문을 던졌을 때 AI 가 그 글자를 그대로 되풀이하는 순간 스크립트가 실행될 수 있다.
   그래서 < > & " ' 다섯 글자를 먼저 안전한 형태로 바꾼 뒤에만 화면에 넣는다.
*/
function chatEscape(text) {
    return String(text)                                       // 숫자나 null 이 들어와도 오류 없이 문자열로 바꾼다
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

/* ----- 말풍선 1개를 대화 영역에 추가 ----- */
function appendMessage(text, sender) {
    var messagesDiv = document.getElementById("chatbot-messages");
    var msgDiv = document.createElement("div");
    msgDiv.className = "chat-msg " + (sender === "bot" ? "bot-msg" : "user-msg");

    var bubble = document.createElement("div");
    bubble.className = "chat-bubble " + (sender === "bot" ? "bot-bubble" : "user-bubble");

    if (sender === "bot") {
        // 순서가 중요하다 : 먼저 전부 안전하게 바꾼(chatEscape) 뒤에만 **굵게**와 줄바꿈 서식을 되살린다
        var safe = chatEscape(text);
        safe = safe.replace(/\*\*(.+?)\*\*/g, "<b>$1</b>");   // **글자** -> <b>글자</b>
        safe = safe.replace(/\n/g, "<br>");                   // 줄바꿈 -> <br>
        bubble.innerHTML = safe;
    } else {
        bubble.textContent = text;                            // 사용자 메시지는 태그 해석 없이 글자 그대로 (가장 안전)
    }

    msgDiv.appendChild(bubble);
    messagesDiv.appendChild(msgDiv);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;          // 새 말풍선이 보이도록 맨 아래로 스크롤
}

/* ============================================================================
   예약 조회 · 취소

   [설계에서 가장 중요한 결정] 비밀번호를 대화 메시지로 입력받지 않는다
     "연락처와 비밀번호를 알려주세요" -> 사용자가 대화창에 직접 타이핑하는 방식은
     비밀번호가 화면에 그대로 보이고, chatHistory 에 남아 AI 서버로까지 전송된다.
     그래서 대화 흐름 안에 <input type="password"> 가 들어간 작은 폼을 띄우고,
     그 값은 대화 메시지가 되지 않고 /Car/orderListJson.do 로만 직접 전송한다.

   [AI 를 부르지 않는 이유] "내 예약 조회"는 단순 DB 조회라서 AI 가 할 일이 없다.
   ============================================================================ */

/* 대화창 안에 조회 폼(연락처+비밀번호)을 띄운다 */
function showOrderLookup() {

    var quickQ = document.getElementById("quick-questions");
    if (quickQ) quickQ.style.display = "none";

    appendMessage("예약을 조회할게요. 예약 당시 입력한 연락처와 비밀번호를 넣어주세요.", "bot");

    var messagesDiv = document.getElementById("chatbot-messages");

    var wrap = document.createElement("div");
    wrap.className = "chat-msg bot-msg chat-msg-wide";
    wrap.id = "order-lookup-form";                             // 조회 후 이 폼만 지우기 위한 id
    wrap.innerHTML =
        '<div class="chat-bubble bot-bubble chat-form">' +
            '<input type="tel" id="lookup-phone" class="chat-input-sm" placeholder="010-1234-5678" autocomplete="tel">' +
            '<input type="password" id="lookup-pass" class="chat-input-sm" placeholder="예약 비밀번호" autocomplete="current-password">' +
            '<button type="button" class="chat-form-btn" onclick="doOrderLookup()">조회하기</button>' +
            '<p class="chat-form-note">비밀번호는 대화 내용으로 저장되지 않습니다.</p>' +
        '</div>';

    messagesDiv.appendChild(wrap);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;

    document.getElementById("lookup-phone").focus();
    document.getElementById("lookup-pass").addEventListener("keypress", function (e) {
        if (e.key === "Enter") { doOrderLookup(); }            // Enter 로도 조회할 수 있게 한다
    });
}

/* 조회 실행 : 연락처+비밀번호를 서버로 보내 예약 목록을 받는다 */
function doOrderLookup() {

    var phone = document.getElementById("lookup-phone").value.trim();
    var pass  = document.getElementById("lookup-pass").value;  // 비밀번호는 공백도 뜻이 있어 trim 하지 않는다

    if (!phone || !pass) {
        appendMessage("연락처와 비밀번호를 모두 입력해주세요.", "bot");
        return;
    }

    var form = document.getElementById("order-lookup-form");
    if (form) form.remove();                                   // 입력 폼을 지운다 (비밀번호가 화면에 남지 않게)

    appendMessage(phone + " 로 조회할게요", "user");            // 연락처만 대화에 남긴다. 비밀번호는 chatHistory 에도 넣지 않는다
    showTypingIndicator();

    var body = new URLSearchParams();
    body.append("memberphone", phone);
    body.append("memberpass", pass);

    fetch(chatbotContextPath + "/Car/orderListJson.do", {
        method: "POST",                                        // POST : 비밀번호가 주소창에 남지 않게
        headers: {
            "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
            "X-Requested-With": "XMLHttpRequest"
        },
        body: body.toString(),
        credentials: "same-origin"
    })
    .then(function (res) {
        if (res.status === 403) { throw new Error("보안 토큰이 만료되었습니다. 화면을 새로 고쳐주세요."); }
        if (!res.ok) { throw new Error("서버 오류 (" + res.status + ")"); }
        return res.json();
    })
    .then(function (data) {
        removeTypingIndicator();
        appendMessage(data.message || "", "bot");
        if (data.orders && data.orders.length > 0) {
            renderOrderCards(data.orders);
        }
    })
    .catch(function (err) {
        removeTypingIndicator();
        appendMessage(err.message || "조회 중 오류가 발생했습니다.", "bot");
    });
}

/* 조회된 예약들을 말풍선 카드로 하나씩 그린다 */
function renderOrderCards(orders) {

    var messagesDiv = document.getElementById("chatbot-messages");

    for (var i = 0; i < orders.length; i++) {
        var o = orders[i];

        // 숫자는 Number() 로 바꾸고, 실패(NaN)하면 0 을 대신 쓴다. 글자는 chatEscape() 로 안전하게 바꾼다
        var orderid = Number(o.orderid) || 0;
        var total   = Number(o.total) || 0;
        var days    = Number(o.days) || 0;
        var qty     = Number(o.qty) || 0;

        var div = document.createElement("div");
        div.className = "chat-msg bot-msg chat-msg-wide";
        div.id = "order-card-" + orderid;                      // 취소 후 이 카드만 지우기 위한 id
        div.innerHTML =
            '<div class="chat-bubble bot-bubble chat-order">' +
                '<div class="chat-order-head">' +
                    '<img class="chat-order-img" alt="" src="' +
                        chatbotContextPath + '/img/' + encodeURIComponent(o.carimg || '') + '">' +
                    '<div>' +
                        '<div class="chat-order-name">' + chatEscape(o.carname) + '</div>' +
                        '<div class="chat-order-no">예약번호 ' + orderid + '</div>' +
                    '</div>' +
                '</div>' +
                '<div class="chat-order-row">시작일 <b>' + chatEscape(o.begindate) + '</b></div>' +
                '<div class="chat-order-row">기간 <b>' + days + '일 · ' + qty + '대</b></div>' +
                '<div class="chat-order-row">옵션 ' + chatEscape(o.options) + '</div>' +
                '<div class="chat-order-total">' + total.toLocaleString() + '원</div>' +
                '<button type="button" class="chat-cancel-btn" onclick="askCancelOrder(' + orderid + ')">' +
                    '예약 취소하기</button>' +
            '</div>';

        messagesDiv.appendChild(div);
    }
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
}

/* 예약 취소 1단계 : 본인 확인을 위해 비밀번호를 한 번 더 받는 폼을 띄운다 */
function askCancelOrder(orderid) {

    var messagesDiv = document.getElementById("chatbot-messages");

    var old = document.getElementById("cancel-form");
    if (old) old.remove();                                     // 이미 열려 있던 취소 폼이 있으면 지운다 (중복 방지)

    appendMessage("예약번호 " + orderid + " 을 취소합니다. 확인을 위해 비밀번호를 한 번 더 입력해주세요.", "bot");

    var wrap = document.createElement("div");
    wrap.className = "chat-msg bot-msg chat-msg-wide";
    wrap.id = "cancel-form";
    wrap.innerHTML =
        '<div class="chat-bubble bot-bubble chat-form">' +
            '<input type="password" id="cancel-pass" class="chat-input-sm" placeholder="예약 비밀번호">' +
            '<button type="button" class="chat-form-btn chat-form-btn-danger" ' +
                    'onclick="doCancelOrder(' + orderid + ')">취소 확정</button>' +
            '<button type="button" class="chat-form-btn chat-form-btn-ghost" ' +
                    'onclick="closeCancelForm()">그만두기</button>' +
            '<p class="chat-form-note">취소한 예약은 되돌릴 수 없습니다.</p>' +
        '</div>';

    messagesDiv.appendChild(wrap);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
    document.getElementById("cancel-pass").focus();
}

/* 예약 취소를 중단할 때 (그만두기 버튼) */
function closeCancelForm() {
    var f = document.getElementById("cancel-form");
    if (f) f.remove();
    appendMessage("취소를 중단했습니다.", "bot");
}

/* 예약 취소 실행 */
function doCancelOrder(orderid) {

    var passEl = document.getElementById("cancel-pass");
    var pass = passEl ? passEl.value : "";

    if (!pass) {
        appendMessage("비밀번호를 입력해주세요.", "bot");
        return;
    }

    var f = document.getElementById("cancel-form");
    if (f) f.remove();
    showTypingIndicator();

    var body = new URLSearchParams();
    body.append("orderid", orderid);
    body.append("memberpass", pass);

    fetch(chatbotContextPath + "/Car/orderCancelJson.do", {
        method: "POST",                                        // 되돌릴 수 없는 동작이므로 POST
        headers: {
            "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
            "X-Requested-With": "XMLHttpRequest"
        },
        body: body.toString(),
        credentials: "same-origin"
    })
    .then(function (res) {
        if (res.status === 403) { throw new Error("보안 토큰이 만료되었습니다. 화면을 새로 고쳐주세요."); }
        if (!res.ok) { throw new Error("서버 오류 (" + res.status + ")"); }
        return res.json();
    })
    .then(function (data) {
        removeTypingIndicator();
        appendMessage(data.message || "", "bot");
        if (data.ok) {
            var card = document.getElementById("order-card-" + orderid);
            if (card) card.remove();                            // 취소 성공한 예약 카드는 화면에서 지워 상태를 맞춘다
        }
    })
    .catch(function (err) {
        removeTypingIndicator();
        appendMessage(err.message || "취소 중 오류가 발생했습니다.", "bot");
    });
}

/* 입력한 문장이 "예약 조회/취소" 의도로 보이는지 판단 (AI 에게 보내기 전에 먼저 거른다) */
function looksLikeOrderIntent(msg) {
    var m = msg.replace(/\s/g, "");                             // 공백 제거 : "예약 조회" 와 "예약조회" 를 같게 본다
    var hasOrder  = m.indexOf("예약") >= 0;
    var hasLookup = m.indexOf("조회") >= 0 || m.indexOf("확인") >= 0
                 || m.indexOf("취소") >= 0 || m.indexOf("변경") >= 0
                 || m.indexOf("내역") >= 0;
    return hasOrder && hasLookup;                                // "예약" + (조회/확인/취소/변경/내역) 이 함께 있어야 조회 의도로 본다
}

/* "입력 중..." 점 3개 표시를 추가/제거 */
function showTypingIndicator() {
    var messagesDiv = document.getElementById("chatbot-messages");
    var typingDiv = document.createElement("div");
    typingDiv.id = "typing-indicator";
    typingDiv.className = "typing-indicator";
    typingDiv.innerHTML = '<div class="typing-dot"></div><div class="typing-dot"></div><div class="typing-dot"></div>';
    messagesDiv.appendChild(typingDiv);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
}

function removeTypingIndicator() {
    var el = document.getElementById("typing-indicator");
    if (el) el.remove();
}
</script>

</body>
</html>
