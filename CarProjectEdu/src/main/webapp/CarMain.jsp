<%-- 이 페이지의 글자 인코딩을 UTF-8 로 정한다. 빠뜨리면 한글이 깨진다 --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%-- JSTL 태그들 사용을 위해 불러오는 구문  --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%-- JSTL 태그를 fmt: 라는 이름으로 쓰겠다는 선언. 이 줄이 없으면 <fmt:...> 가 그냥 글자로 나온다 --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>



<%-- 이 문서가 HTML5 라는 선언. 항상 첫 줄에 온다 --%>
<!DOCTYPE html>
<%-- 문서 전체 시작 --%>
<html>
<%-- 화면에 안 보이는 설정 구역 --%>
<head>
<%-- 글자 인코딩 설정. 한글이 깨지지 않게 한다 --%>
<meta charset="UTF-8">
<%-- 반응형 웹을 위한 필수 메타 태그 (모바일/태블릿 화면 크기 인식) --%>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<%-- 브라우저 탭에 표시되는 제목 --%>
<title>SM렌탈 - 자동차 렌탈 서비스</title>
<%--
 ================================================================================
   [5단계 변경] 이 화면이 "유일한 HTML 문서"가 되었다.
   (기존 상태)
     CarMain.jsp 가 <html><head><body> 를 갖고 Top / 중앙화면 / Bottom 을 include 하는데,
     include 되는 32개 JSP 도 각자 <!DOCTYPE><html><head><body> 를 출력했다.
     그래서 완성된 응답은 이런 모양이었다.
         <html><head>...</head><body>
             <html><head><link rel="stylesheet" ...></head><body> ... </body></html>  <- Top.jsp
             <html><head>...</head><body> ... </body></html>                          <- 중앙화면
         </body></html>
     문제
       - <link> 와 <style> 이 <body> 안에 들어가 스타일 적용 순서를 예측할 수 없다
       - viewport 메타 태그가 여러 개라 반응형 동작이 불안정하다
       - 브라우저가 관용적으로 고쳐서 그려주기 때문에 "어쩌다 깨지는" 형태로 나타난다
   (지금)
     - 문서 골격은 이 파일에만 있다
     - include 되는 JSP 들은 "조각(fragment)"이 되어 내용만 출력한다
     - CSS / JS 도 여기서 한 번만 불러온다
 ================================================================================
--%>
<%-- CSRF 토큰을 문서에 심어두면 모든 비동기 요청이 자동으로 함께 보낼 수 있다 (js/app.js 참고) --%>
<meta name="_csrf" content="${_csrf}">
<%--
 [CDN 제거] 기존에는 Top.jsp 안에서 아래 6개를 외부 서버에서 불러왔다.
     https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/...  (CSS, JS)
     https://cdn.jsdelivr.net/npm/jquery@3.5.1/...     (2번 중복)
     https://cdn.jsdelivr.net/npm/popper.js@1.16.1/...
   문제 : 사내망·오프라인 강의실에서는 화면이 전부 깨졌다.
   지금 : Bootstrap 을 아예 쓰지 않는다. 외부 요청 0건.
   [Bootstrap 을 로컬 파일로 바꿔 쓰지 않고 완전히 뺀 이유]
     프로젝트 안에 있던 css/bootstrap.min.css 는 버전이 2.3.0 이었다.
     그런데 Top.jsp 는 Bootstrap 4 클래스(navbar-expand-md, navbar-toggler,
     nav-link 등)를 쓰고 있다. v2 와 v4 는 클래스 체계가 완전히 달라서
     로컬 파일로 바꾸면 오히려 메뉴가 더 깨진다.
     실제로 Bootstrap 이 필요한 곳은 Top.jsp(상단메뉴)와 join.jsp(회원가입)
     두 파일의 클래스 25종뿐이었다.
     그래서 그 25종을 app.css 안에 직접 구현했다. (app.css 의 "Bootstrap 호환 레이어")
     103KB 프레임워크 -> 약 1KB 규칙으로 대체했고, 화면 모양은 유지된다.
--%>
<%-- 이 프로젝트의 디자인 시스템 (색·간격·버튼·폼·표·반응형 + Bootstrap 호환 레이어) --%>
<%--
 [캐시 무효화] ?v=숫자 를 붙이는 이유
   브라우저는 css/js 파일을 캐시에 저장해두고, 새로고침(F5)해도
   서버에 다시 묻지 않고 캐시본을 그대로 쓰는 경우가 많다.
   그래서 app.css 를 크게 고쳐도 "옛날 화면"이 계속 보이는 문제가 생긴다.
   (Ctrl+F5 강력 새로고침을 해야만 새 파일을 받아온다)
   주소 뒤에 ?v=13 을 붙이면 브라우저 입장에서는 "다른 주소"라서
   캐시를 버리고 새로 받아온다. css/js 를 크게 고칠 때마다 숫자를 1 올린다.
   (실무에서는 빌드 도구가 파일 내용의 해시를 자동으로 붙여준다)
--%>
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/app.css?v=14">
<%--
 공용 스크립트 (jQuery 대체)
   defer : HTML 을 다 읽은 뒤에 실행한다.
           head 에서 불러도 화면 그리기를 막지 않아 첫 화면이 빨리 보인다.
--%>
<script src="<%=request.getContextPath()%>/js/app.js?v=13" defer></script>
<style>
/* =============================================
   [CarMain.jsp 전체 레이아웃]
   - 반응형: PC/태블릿/모바일 모두 대응
   ============================================= */
/* 전체 페이지 기본 설정 */
* {
    box-sizing: border-box; /* 안여백이 전체 너비에 포함되게 설정 */
}
/* 메인 컨테이너 - 가운데 정렬, 최대 너비 제한 */
.main-wrapper {
    /* 가로로 이보다 커지지 않게 1200px */
    max-width: 1200px;  /* 최대 너비: PC에서 1200px을 넘지 않음 */
    width: 100%;        /* 모바일에서는 화면 전체 너비 사용 */
    margin: 0 auto;     /* 가운데 정렬 */
}
/* 중앙 콘텐츠 영역 */
.main-content {
    /* 가로 크기 100% */
    width: 100%;
    /* 세로로 이보다 작아지지 않게 500px */
    min-height: 500px;  /* 최소 높이 */
}
</style>
</head>
<%-- 실제로 보이는  CarMain.jsp  메인화면 내용 구역 --%>
<body>
<%--
	모델 2 개발방식
	- 센터 중앙화면 공간은 상위 메뉴 (Top.jsp)를 클릭할때 마다 계속 변화되어 나타나기 때문에
	  request내장객체 영역으로 부터 중앙화면 공간의 VIEW 주소를 얻어와 변수에 저장
 --%>						
 <c:set var="center" value="${requestScope.center}" />
 


 <%-- 처음으로 CarMain.jsp메인 화면을 요청 했을때 중앙화면은 Center.jsp로 보이게 설정 --%>
 <c:if test="${center == null}">
 	<%-- 화면에서만 쓸 임시 값 center 을 만든다 --%>
 	<c:set var="center" value="Center.jsp" />
 </c:if>
 
<%-- 반응형 전체 감싸는 영역 (table 레이아웃 → div 레이아웃으로 변환) --%>
<div class="main-wrapper">
    <%-- 상단 헤더/네비게이션 영역 --%>
    <div>
        <%-- Top.jsp 화면을 이 자리에 불러와 붙인다 --%>
        <jsp:include page="Top.jsp"/>
    </div>
    <%-- 중앙 콘텐츠 영역 (클릭한 메뉴에 따라 변경) --%>
    <div class="main-content">
    
        <%-- ${center} 화면을 이 자리에 불러와 붙인다 --%>
        <jsp:include page="${center}"/> 	
        
        	<%--2.3. 예약 아이디를 이용해 조회된 예약 정보를 보여줄 중앙화면 VIEW("CarConfirmUpdate.jsp")주소 request에 바인딩
 			
			         request.setAttribute("center", "CarConfirmUpdate.jsp"); --%>
			

        					
    </div>
    <%-- 하단 푸터 영역 --%>
    <div>
        <%-- Bottom.jsp 화면을 이 자리에 불러와 붙인다 --%>
        <jsp:include page="Bottom.jsp"/>
    </div>
</div>
<!-- ============================================================
     AI 챗봇 (Google Gemini 무료 모델)
     - 오른쪽 하단 플로팅 버튼 + 채팅 상담창
     ============================================================ -->
<%-- 챗봇에서 contextPath 사용 --%>
<%
    // chatbotCtx — getContextPath( ) 의 결과를 담는다
    String chatbotCtx = request.getContextPath();
%>
<!-- 챗봇 플로팅 버튼 -->
<div id="chatbot-btn" onclick="toggleChatbot()" title="AI 상담 챗봇">
    <%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
    <span id="chatbot-btn-icon">&#128172;</span>
    <%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
    <span id="chatbot-btn-close" style="display:none;">&#10005;</span>
</div>
<!-- 챗봇 창 -->
<div id="chatbot-window">
    <!-- 챗봇 헤더 -->
    <div id="chatbot-header">
        <%-- "chatbot-header-info" 이라는 이름표가 붙은 영역 (자바스크립트가 이 이름으로 찾는다) --%>
        <div id="chatbot-header-info">
            <%-- "chatbot-avatar" 이라는 이름표가 붙은 영역 (자바스크립트가 이 이름으로 찾는다) --%>
            <div id="chatbot-avatar">&#129302;</div>
            <%-- 내용을 묶는 상자 --%>
            <div>
                <%-- "chatbot-title" 이라는 이름표가 붙은 영역 (자바스크립트가 이 이름으로 찾는다) --%>
                <div id="chatbot-title">SM렌탈 AI 상담</div>
                <%-- "chatbot-status" 이라는 이름표가 붙은 영역 (자바스크립트가 이 이름으로 찾는다) --%>
                <div id="chatbot-status">&#9679; 온라인</div>
            </div>
        </div>
        <%-- 누르면 동작하는 버튼 --%>
        <button id="chatbot-close-btn" onclick="toggleChatbot()">&#10005;</button>
    </div>
    <!-- 챗봇 메시지 영역 -->
    <div id="chatbot-messages">
        <!-- 환영 메시지 -->
        <div class="chat-msg bot-msg">
            <%-- chat-bubble bot-bubble 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
            <div class="chat-bubble bot-bubble">
                안녕하세요! &#128075;<br>
                <%-- 굵은 글씨 --%>
                <b>SM렌탈 AI 상담사</b>입니다.<br><br>
                <%-- 화면에 그대로 보이는 글자: "렌트카 관련 궁금한 점을 편하게 물어보세요!" --%>
                렌트카 관련 궁금한 점을 편하게 물어보세요!
            </div>
        </div>
        <!-- 빠른 질문 버튼 -->
        <div id="quick-questions">
            <%-- 누르면 동작하는 버튼 --%>
            <button class="quick-btn" onclick="sendQuickMsg('보유 차량 종류를 알려주세요')">&#128663; 차량 종류</button>
            <%-- 누르면 동작하는 버튼 --%>
            <button class="quick-btn" onclick="sendQuickMsg('렌트 가격이 궁금합니다')">&#128176; 가격 안내</button>
            <%-- [신규] 예약 조회 : AI 를 거치지 않고 바로 조회 폼을 띄운다 --%>
            <button class="quick-btn" onclick="showOrderLookup()">&#128203; 내 예약 조회</button>
            <%-- 누르면 동작하는 버튼 --%>
            <button class="quick-btn" onclick="sendQuickMsg('추가 옵션에는 어떤 것이 있나요?')">&#9881; 추가 옵션</button>
        </div>
    </div>
    <!-- 챗봇 입력 영역 -->
    <div id="chatbot-input-area">
        <%-- 입력칸 --%>
        <input type="text" id="chatbot-input" placeholder="메시지를 입력하세요..." onkeypress="if(event.key==='Enter') sendMessage();" />
        <%-- 누르면 동작하는 버튼 --%>
        <button id="chatbot-send-btn" onclick="sendMessage()">&#10148;</button>
    </div>
</div>
/* 이 화면에서만 쓰는 모양(CSS) 시작 */
<style>
/* ===== 챗봇 플로팅 버튼 ===== */
#chatbot-btn {
    /* 위치 기준 fixed */
    position: fixed;
    /* 아래에서의 위치 30px */
    bottom: 30px;
    /* 오른쪽에서의 위치 30px */
    right: 30px;
    /* 가로 크기 60px */
    width: 60px;
    /* 세로 크기 60px */
    height: 60px;
    /* 배경 linear-gradient(135deg, #cc0000, #ff3333) */
    background: linear-gradient(135deg, #cc0000, #ff3333);
    /* 모서리 둥글기 50% */
    border-radius: 50%;
    /* 요소를 어떤 방식으로 배치할지 flex */
    display: flex;
    /* 세로 방향 정렬 center */
    align-items: center;
    /* 가로 방향 정렬 center */
    justify-content: center;
    /* 마우스 커서 모양 pointer */
    cursor: pointer;
    /* 겹칠 때 앞뒤 순서 99999 */
    z-index: 99999;
    /* 그림자 0 4px 15px rgba(204, 0, 0, 0.4) */
    box-shadow: 0 4px 15px rgba(204, 0, 0, 0.4);
    /* 값이 바뀔 때 부드럽게 transform 0.3s, box-shadow 0.3s */
    transition: transform 0.3s, box-shadow 0.3s;
}
/* id="chatbot-btn" 인 요소 하나의 모양을 정한다 */
#chatbot-btn:hover {
    /* 이동·회전·확대 scale(1.1) */
    transform: scale(1.1);
    /* 그림자 0 6px 20px rgba(204, 0, 0, 0.5) */
    box-shadow: 0 6px 20px rgba(204, 0, 0, 0.5);
}
/* id="chatbot-btn-icon" 인 요소 하나의 모양을 정한다 */
#chatbot-btn-icon, #chatbot-btn-close {
    /* 글자 크기 28px */
    font-size: 28px;
    /* 글자 색 #fff */
    color: #fff;
    /* 줄 간격 1 */
    line-height: 1;
}
/* ===== 챗봇 창 ===== */
#chatbot-window {
    /* 위치 기준 fixed */
    position: fixed;
    /* 아래에서의 위치 100px */
    bottom: 100px;
    /* 오른쪽에서의 위치 30px */
    right: 30px;
    /* 가로 크기 380px */
    width: 380px;
    /* 세로 크기 520px */
    height: 520px;
    /* 배경 #fff */
    background: #fff;
    /* 모서리 둥글기 16px */
    border-radius: 16px;
    /* 그림자 0 8px 40px rgba(0, 0, 0, 0.18) */
    box-shadow: 0 8px 40px rgba(0, 0, 0, 0.18);
    /* 겹칠 때 앞뒤 순서 99998 */
    z-index: 99998;
    /* 요소를 어떤 방식으로 배치할지 none */
    display: none;
    /* 가로/세로 배치 방향 column */
    flex-direction: column;
    /* 넘칠 때 처리 hidden */
    overflow: hidden;
    /* 애니메이션 적용 chatbotSlideUp 0.3s ease-out */
    animation: chatbotSlideUp 0.3s ease-out;
}
/* "chatbotSlideUp" 애니메이션의 장면들을 정의한다 */
@keyframes chatbotSlideUp {
    /* 애니메이션의 한 장면 (진행 정도별 모양) */
    from { opacity: 0; transform: translateY(20px); }
    /* 애니메이션의 한 장면 (진행 정도별 모양) */
    to   { opacity: 1; transform: translateY(0); }
}
/* ===== 챗봇 헤더 ===== */
#chatbot-header {
    /* 배경 linear-gradient(135deg, #cc0000, #aa0000) */
    background: linear-gradient(135deg, #cc0000, #aa0000);
    /* 안쪽 여백 14px 18px */
    padding: 14px 18px;
    /* 요소를 어떤 방식으로 배치할지 flex */
    display: flex;
    /* 세로 방향 정렬 center */
    align-items: center;
    /* 가로 방향 정렬 space-between */
    justify-content: space-between;
}
/* id="chatbot-header-info" 인 요소 하나의 모양을 정한다 */
#chatbot-header-info {
    /* 요소를 어떤 방식으로 배치할지 flex */
    display: flex;
    /* 세로 방향 정렬 center */
    align-items: center;
    /* 요소 사이 간격 10px */
    gap: 10px;
}
/* id="chatbot-avatar" 인 요소 하나의 모양을 정한다 */
#chatbot-avatar {
    /* 가로 크기 38px */
    width: 38px;
    /* 세로 크기 38px */
    height: 38px;
    /* 배경 rgba(255,255,255,0.2) */
    background: rgba(255,255,255,0.2);
    /* 모서리 둥글기 50% */
    border-radius: 50%;
    /* 요소를 어떤 방식으로 배치할지 flex */
    display: flex;
    /* 세로 방향 정렬 center */
    align-items: center;
    /* 가로 방향 정렬 center */
    justify-content: center;
    /* 글자 크기 22px */
    font-size: 22px;
}
/* id="chatbot-title" 인 요소 하나의 모양을 정한다 */
#chatbot-title {
    /* 글자 색 #fff */
    color: #fff;
    /* 글자 크기 15px */
    font-size: 15px;
    /* 글자 굵기 700 */
    font-weight: 700;
}
/* id="chatbot-status" 인 요소 하나의 모양을 정한다 */
#chatbot-status {
    /* 글자 색 rgba(255,255,255,0.8) */
    color: rgba(255,255,255,0.8);
    /* 글자 크기 11px */
    font-size: 11px;
}
/* id="chatbot-status" 인 요소 하나의 모양을 정한다 */
#chatbot-status > span, #chatbot-status {
    /* 글자 색 #7fff7f */
    color: #7fff7f;
    /* 글자 크기 11px */
    font-size: 11px;
}
/* id="chatbot-close-btn" 인 요소 하나의 모양을 정한다 */
#chatbot-close-btn {
    /* 배경 none */
    background: none;
    /* 테두리 none */
    border: none;
    /* 글자 색 #fff */
    color: #fff;
    /* 글자 크기 18px */
    font-size: 18px;
    /* 마우스 커서 모양 pointer */
    cursor: pointer;
    /* 안쪽 여백 4px 8px */
    padding: 4px 8px;
    /* 모서리 둥글기 50% */
    border-radius: 50%;
    /* 값이 바뀔 때 부드럽게 background 0.2s */
    transition: background 0.2s;
}
/* id="chatbot-close-btn" 인 요소 하나의 모양을 정한다 */
#chatbot-close-btn:hover {
    /* 배경 rgba(255,255,255,0.15) */
    background: rgba(255,255,255,0.15);
}
/* ===== 메시지 영역 ===== */
#chatbot-messages {
    /* 늘어나는 비율 1 */
    flex: 1;
    /* 세로로 넘칠 때 auto */
    overflow-y: auto;
    /* 안쪽 여백 16px */
    padding: 16px;
    /* 배경 #f9f9f9 */
    background: #f9f9f9;
    /* 요소를 어떤 방식으로 배치할지 flex */
    display: flex;
    /* 가로/세로 배치 방향 column */
    flex-direction: column;
    /* 요소 사이 간격 12px */
    gap: 12px;
}
/* ===== 메시지 말풍선 ===== */
.chat-msg {
    /* 요소를 어떤 방식으로 배치할지 flex */
    display: flex;
    /* 가로로 이보다 커지지 않게 85% */
    max-width: 85%;
}
.bot-msg { align-self: flex-start; }
.user-msg { align-self: flex-end; }
/* class="chat-bubble" 이 붙은 요소의 모양을 정한다 */
.chat-bubble {
    /* 안쪽 여백 10px 14px */
    padding: 10px 14px;
    /* 모서리 둥글기 14px */
    border-radius: 14px;
    /* 글자 크기 13px */
    font-size: 13px;
    /* 줄 간격 1.5 */
    line-height: 1.5;
    /* 긴 단어 줄바꿈 방식 break-word */
    word-break: break-word;
    /* 공백·줄바꿈 처리 방식 pre-wrap */
    white-space: pre-wrap;
}
.bot-bubble {
    /* 배경 #fff */
    background: #fff;
    /* 글자 색 #333 */
    color: #333;
    /* 테두리 1px solid #e8e8e8 */
    border: 1px solid #e8e8e8;
    /* 세부 모양 border-top-left-radius — 4px */
    border-top-left-radius: 4px;
}
.user-bubble {
    /* 배경 #cc0000 */
    background: #cc0000;
    /* 글자 색 #fff */
    color: #fff;
    /* 세부 모양 border-top-right-radius — 4px */
    border-top-right-radius: 4px;
}
/* =============================================
   [신규] 챗봇 안의 입력 폼 (예약 조회 / 취소)
   대화 흐름 안에 폼을 두는 이유는 비밀번호를 대화 메시지로 받지 않기 위함이다.
   (자세한 설명은 아래 showOrderLookup() 함수 주석)
   ============================================= */
/* 입력 폼과 예약 카드가 든 말풍선은 "가능한 폭을 채운다".
   일반 말풍선(.chat-msg)은 max-width:85% 로 두고 내용 크기에 맞춰 줄어든다.
   글에는 그게 자연스럽지만, 입력칸·예약카드가 들어가면
   말풍선이 160px 로 쪼그라들어 사진과 글자가 겹쳐 보였다.
   그래서 이 말풍선만 width 를 지정해 폭을 확보한다. */
.chat-msg-wide { width: 85%; }
.chat-msg-wide .chat-bubble { width: 100%; }
/* class="chat-form" 이 붙은 요소의 모양을 정한다 */
.chat-form {
    /* 요소를 어떤 방식으로 배치할지 flex */
    display: flex;
    /* 가로/세로 배치 방향 column */
    flex-direction: column;
    /* 요소 사이 간격 6px */
    gap: 6px;
    /* 가로 크기 100% */
    width: 100%;
}
/* 이 프로젝트의 터치 타깃 기준은 44px 이다 (app.css 의 버튼들과 동일).
   손가락으로 정확히 누를 수 있는 최소 크기라서 폼 요소 전체에 같은 값을 쓴다. */
.chat-input-sm {
    /* 가로 크기 100% */
    width: 100%;
    /* 세로로 이보다 작아지지 않게 44px */
    min-height: 44px;
    /* 안쪽 여백 10px */
    padding: 10px;
    /* 테두리 1px solid #ddd */
    border: 1px solid #ddd;
    /* 모서리 둥글기 6px */
    border-radius: 6px;
    /* 글자 크기 13px */
    font-size: 13px;
    /* 글꼴 inherit */
    font-family: inherit;
    /* 크기 계산에 여백 포함 여부 border-box */
    box-sizing: border-box;
}
.chat-input-sm:focus {
    /* 바깥 윤곽선 none */
    outline: none;
    /* 테두리 색 #cc0000 */
    border-color: #cc0000;
}
.chat-form-btn {
    /* 세로로 이보다 작아지지 않게 44px */
    min-height: 44px;
    /* 안쪽 여백 10px 12px */
    padding: 10px 12px;
    /* 배경 #cc0000 */
    background: #cc0000;
    /* 글자 색 #fff */
    color: #fff;
    /* 테두리 none */
    border: none;
    /* 모서리 둥글기 6px */
    border-radius: 6px;
    /* 글자 크기 13px */
    font-size: 13px;
    /* 글자 굵기 bold */
    font-weight: bold;
    /* 글꼴 inherit */
    font-family: inherit;
    /* 마우스 커서 모양 pointer */
    cursor: pointer;
}
.chat-form-btn:hover { background: #aa0000; }
.chat-form-btn-danger { background: #333; }
.chat-form-btn-danger:hover { background: #000; }
.chat-form-btn-ghost {
    /* 배경 #f5f5f5 */
    background: #f5f5f5;
    /* 글자 색 #555 */
    color: #555;
    /* 글자 굵기 normal */
    font-weight: normal;
}
.chat-form-btn-ghost:hover { background: #e8e8e8; }
.chat-form-note {
    /* 바깥 여백 2px 0 0 */
    margin: 2px 0 0;
    /* 글자 크기 11px */
    font-size: 11px;
    /* 글자 색 #999 */
    color: #999;
    /* 줄 간격 1.4 */
    line-height: 1.4;
}
/* ===== 챗봇 안의 예약 카드 ===== */
.chat-order { width: 100%; }
.chat-order-head {
    /* 요소를 어떤 방식으로 배치할지 flex */
    display: flex;
    /* 세로 방향 정렬 center */
    align-items: center;
    /* 요소 사이 간격 8px */
    gap: 8px;
    /* 아래 안쪽 여백 8px */
    padding-bottom: 8px;
    /* 아래 바깥 여백 8px */
    margin-bottom: 8px;
    /* 아래 테두리 1px solid #eee */
    border-bottom: 1px solid #eee;
}
.chat-order-img {
    /* 가로 크기 56px */
    width: 56px;
    /* 세로 크기 38px */
    height: 38px;
    /* 이미지 채우기 방식 cover */
    object-fit: cover;
    /* 모서리 둥글기 4px */
    border-radius: 4px;
    /* 배경 #f5f5f5 */
    background: #f5f5f5;
    /* 늘어나는 비율 0 0 auto */
    flex: 0 0 auto;
}
.chat-order-name { font-weight: bold; font-size: 13px; }
.chat-order-no   { font-size: 11px; color: #999; }
.chat-order-row  { font-size: 12px; color: #666; line-height: 1.7; }
.chat-order-total {
    /* 위 바깥 여백 6px */
    margin-top: 6px;
    /* 글자 크기 14px */
    font-size: 14px;
    /* 글자 굵기 bold */
    font-weight: bold;
    /* 글자 색 #cc0000 */
    color: #cc0000;
    /* 글자 정렬 right */
    text-align: right;
}
.chat-cancel-btn {
    /* 가로 크기 100% */
    width: 100%;
    /* 세로로 이보다 작아지지 않게 44px */
    min-height: 44px;
    /* 위 바깥 여백 8px */
    margin-top: 8px;
    /* 안쪽 여백 10px */
    padding: 10px;
    /* 배경 #fff */
    background: #fff;
    /* 글자 색 #cc0000 */
    color: #cc0000;
    /* 테두리 1px solid #cc0000 */
    border: 1px solid #cc0000;
    /* 모서리 둥글기 6px */
    border-radius: 6px;
    /* 글자 크기 12px */
    font-size: 12px;
    /* 글자 굵기 bold */
    font-weight: bold;
    /* 글꼴 inherit */
    font-family: inherit;
    /* 마우스 커서 모양 pointer */
    cursor: pointer;
}
.chat-cancel-btn:hover { background: #cc0000; color: #fff; }
/* ===== 빠른 질문 버튼 ===== */
#quick-questions {
    /* 요소를 어떤 방식으로 배치할지 flex */
    display: flex;
    /* 줄바꿈 허용 wrap */
    flex-wrap: wrap;
    /* 요소 사이 간격 6px */
    gap: 6px;
    /* 안쪽 여백 4px 0 */
    padding: 4px 0;
}
.quick-btn {
    /* 안쪽 여백 6px 12px */
    padding: 6px 12px;
    /* 배경 #fff */
    background: #fff;
    /* 테두리 1px solid #ddd */
    border: 1px solid #ddd;
    /* 모서리 둥글기 16px */
    border-radius: 16px;
    /* 글자 크기 12px */
    font-size: 12px;
    /* 글자 색 #555 */
    color: #555;
    /* 마우스 커서 모양 pointer */
    cursor: pointer;
    /* 값이 바뀔 때 부드럽게 all 0.2s */
    transition: all 0.2s;
    /* 공백·줄바꿈 처리 방식 nowrap */
    white-space: nowrap;
}
.quick-btn:hover {
    /* 배경 #cc0000 */
    background: #cc0000;
    /* 글자 색 #fff */
    color: #fff;
    /* 테두리 색 #cc0000 */
    border-color: #cc0000;
}
/* ===== 타이핑 인디케이터 ===== */
.typing-indicator {
    /* 요소를 어떤 방식으로 배치할지 flex */
    display: flex;
    /* 요소 사이 간격 4px */
    gap: 4px;
    /* 안쪽 여백 10px 14px */
    padding: 10px 14px;
    /* 배경 #fff */
    background: #fff;
    /* 테두리 1px solid #e8e8e8 */
    border: 1px solid #e8e8e8;
    /* 모서리 둥글기 14px */
    border-radius: 14px;
    /* 세부 모양 border-top-left-radius — 4px */
    border-top-left-radius: 4px;
    /* 세부 모양 align-self — flex-start */
    align-self: flex-start;
}
.typing-dot {
    /* 가로 크기 8px */
    width: 8px;
    /* 세로 크기 8px */
    height: 8px;
    /* 배경 #ccc */
    background: #ccc;
    /* 모서리 둥글기 50% */
    border-radius: 50%;
    /* 애니메이션 적용 typingBounce 1.2s infinite */
    animation: typingBounce 1.2s infinite;
}
.typing-dot:nth-child(2) { animation-delay: 0.2s; }
.typing-dot:nth-child(3) { animation-delay: 0.4s; }
/* "typingBounce" 애니메이션의 장면들을 정의한다 */
@keyframes typingBounce {
    /* 애니메이션의 한 장면 (진행 정도별 모양) */
    0%, 60%, 100% { transform: translateY(0); }
    /* 애니메이션의 한 장면 (진행 정도별 모양) */
    30% { transform: translateY(-6px); }
}
/* ===== 입력 영역 ===== */
#chatbot-input-area {
    /* 요소를 어떤 방식으로 배치할지 flex */
    display: flex;
    /* 안쪽 여백 12px */
    padding: 12px;
    /* 배경 #fff */
    background: #fff;
    /* 위 테두리 1px solid #eee */
    border-top: 1px solid #eee;
    /* 요소 사이 간격 8px */
    gap: 8px;
}
/* id="chatbot-input" 인 요소 하나의 모양을 정한다 */
#chatbot-input {
    /* 늘어나는 비율 1 */
    flex: 1;
    /* 안쪽 여백 10px 14px */
    padding: 10px 14px;
    /* 테두리 1px solid #ddd */
    border: 1px solid #ddd;
    /* 모서리 둥글기 20px */
    border-radius: 20px;
    /* 글자 크기 13px */
    font-size: 13px;
    /* 바깥 윤곽선 none */
    outline: none;
    /* 값이 바뀔 때 부드럽게 border-color 0.2s */
    transition: border-color 0.2s;
}
/* id="chatbot-input" 인 요소 하나의 모양을 정한다 */
#chatbot-input:focus {
    /* 테두리 색 #cc0000 */
    border-color: #cc0000;
}
/* id="chatbot-send-btn" 인 요소 하나의 모양을 정한다 */
#chatbot-send-btn {
    /* 가로 크기 40px */
    width: 40px;
    /* 세로 크기 40px */
    height: 40px;
    /* 배경 #cc0000 */
    background: #cc0000;
    /* 글자 색 #fff */
    color: #fff;
    /* 테두리 none */
    border: none;
    /* 모서리 둥글기 50% */
    border-radius: 50%;
    /* 글자 크기 18px */
    font-size: 18px;
    /* 마우스 커서 모양 pointer */
    cursor: pointer;
    /* 요소를 어떤 방식으로 배치할지 flex */
    display: flex;
    /* 세로 방향 정렬 center */
    align-items: center;
    /* 가로 방향 정렬 center */
    justify-content: center;
    /* 값이 바뀔 때 부드럽게 background 0.2s */
    transition: background 0.2s;
}
/* id="chatbot-send-btn" 인 요소 하나의 모양을 정한다 */
#chatbot-send-btn:hover {
    /* 배경 #aa0000 */
    background: #aa0000;
}
/* ===== 반응형 (모바일) ===== */
@media (max-width: 480px) {
    /* id="chatbot-window" 인 요소 하나의 모양을 정한다 */
    #chatbot-window {
        /* 가로 크기 calc(100vw - 20px) */
        width: calc(100vw - 20px);
        /* 세로 크기 calc(100vh - 120px) */
        height: calc(100vh - 120px);
        /* 오른쪽에서의 위치 10px */
        right: 10px;
        /* 아래에서의 위치 80px */
        bottom: 80px;
        /* 모서리 둥글기 12px */
        border-radius: 12px;
    }
    /* id="chatbot-btn" 인 요소 하나의 모양을 정한다 */
    #chatbot-btn {
        /* 오른쪽에서의 위치 16px */
        right: 16px;
        /* 아래에서의 위치 16px */
        bottom: 16px;
        /* 가로 크기 54px */
        width: 54px;
        /* 세로 크기 54px */
        height: 54px;
    }
}
</style>
// 브라우저에서 실행할 자바스크립트 시작
<script>
/* ============================================================
   AI 챗봇 JavaScript
   - fetch() API 사용 (jquery.slim은 AJAX 미지원)
   - 대화 기록 관리 (최대 20개 메시지)
   ============================================================ */
var chatbotContextPath = "<%=chatbotCtx%>";
var chatHistory = [];       // 대화 기록 배열
var isChatbotOpen = false;  // 챗봇 창 열림 여부
var isSending = false;      // 메시지 전송 중 여부
/* ----- 챗봇 열기/닫기 ----- */
function toggleChatbot() {
    var win = document.getElementById("chatbot-window");   // 챗봇 창(대화 화면)을 찾는다
    var iconChat = document.getElementById("chatbot-btn-icon");   // 말풍선 아이콘 (닫혀 있을 때 보인다)
    var iconClose = document.getElementById("chatbot-btn-close");   // X 아이콘 (열려 있을 때 보인다)
    <%-- 조건을 확인해 맞을 때만 아래를 실행한다 --%>
    if (isChatbotOpen) {   // 지금 열려 있으면 = 닫아야 한다
        win.style.display = "none";   // 창을 숨긴다
        iconChat.style.display = "inline";   // 말풍선 아이콘을 보이게 한다
        iconClose.style.display = "none";   // X 아이콘을 숨긴다
    <%-- 위 조건들이 전부 아닐 때 --%>
    } else {
        win.style.display = "flex";   // 창을 보이게 한다. flex 라야 안쪽 배치가 유지된다
        iconChat.style.display = "none";   // 말풍선 아이콘을 숨긴다
        iconClose.style.display = "inline";   // X 아이콘을 보이게 한다
        document.getElementById("chatbot-input").focus();   // 입력칸에 커서를 놓아 준다 (열자마자 바로 칠 수 있게)
    }
    isChatbotOpen = !isChatbotOpen;   // 열림/닫힘 상태를 뒤집어 기억한다.  ! 는 "반대" 라는 뜻이다
}
/* ----- 메시지 보내기 ----- */
function sendMessage() {
    if (isSending) return;   // 이미 보내는 중이면 아무것도 하지 않는다 (버튼 연타로 중복 전송되는 것을 막는다)
    <%-- 화면에서 id 가 "chatbot-input" 인 요소를 찾는다 --%>
    var input = document.getElementById("chatbot-input");   // 입력칸을 찾는다
    var msg = input.value.trim();   // 입력한 글을 읽고 앞뒤 공백을 없앤다
    if (!msg) return;   // 빈 내용이면 보낼 것이 없다
    // 빠른 질문 버튼 숨기기
    var quickQ = document.getElementById("quick-questions");
    if (quickQ) quickQ.style.display = "none";   // 처음 화면에 있던 추천 질문 버튼을 숨긴다 (대화가 시작됐으므로)
    // 사용자 메시지 표시
    appendMessage(msg, "user");
    input.value = "";   // 입력칸을 비운다. 다음 질문을 바로 칠 수 있다
    /* [신규] "예약 조회/취소" 의도면 AI 를 부르지 않고 조회 폼을 띄운다.
             AI 는 DB 를 볼 수 없어 "홈페이지에서 확인하세요" 같은 답만 하기 때문이다.
             (규칙으로 되는 일에 AI 를 쓰지 않는다 - 같은 원칙을 예약 비서에도 적용했다) */
    if (looksLikeOrderIntent(msg)) {
        showOrderLookup();
        return;   // AI 를 부르지 않고 여기서 끝낸다
    }
    // 대화 기록에 추가
    chatHistory.push({ role: "user", text: msg });
    // 타이핑 인디케이터 표시
    showTypingIndicator();
    isSending = true;   // "보내는 중" 으로 표시해 둔다. 위 593줄이 이 값을 본다
    // 서버에 메시지 전송 (fetch API 사용)
    var formData = new URLSearchParams();
    formData.append("message", msg);   // 사용자가 입력한 메시지를 담는다
    // 최근 20개 메시지만 전송 (토큰 절약)
    var recentHistory = chatHistory.slice(-20);
    // 마지막 user 메시지는 서버에서 자동 추가하므로 제외
    var historyToSend = recentHistory.slice(0, -1);
    formData.append("history", JSON.stringify(historyToSend));   // 대화 기록을 글자(JSON)로 바꿔 함께 보낸다
    /* [보안] CSRF 토큰을 함께 보낸다.
       이 챗봇은 CarApp.postForm 을 쓰지 않고 fetch 를 직접 호출하므로
       토큰이 자동으로 붙지 않는다. 위 <meta name="_csrf"> 값을 직접 읽어 넣는다. */
    var csrfMeta = document.querySelector('meta[name="_csrf"]');
    if (csrfMeta && csrfMeta.content) {   // 화면에 토큰이 심어져 있으면
        formData.append("_csrf", csrfMeta.content);   // 보낼 값에 토큰을 추가한다
    }
    <%-- 서버에 요청을 보낸다 (화면 새로고침 없이) --%>
    fetch(chatbotContextPath + "/Chatbot/send.do", {   // 챗봇 서버 주소로 요청을 보낸다
        method: "POST",   // POST 방식으로 보낸다
        headers: {
            "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",   // 본문이 "이름=값&이름=값" 형식이라고 알린다
            /* 서버가 "비동기 요청"으로 인식해 HTML 대신 짧은 메시지로 답하게 한다 */
            "X-Requested-With": "XMLHttpRequest"
        },
        body: formData.toString()
    })
    .then(function(response) { return response.json(); })
    .then(function(data) {
        removeTypingIndicator();   // "입력 중..." 표시를 지운다
        var reply = data.reply || "응답을 받지 못했습니다.";   // 답이 없으면 대신 보여줄 문구를 쓴다
        appendMessage(reply, "bot");   // AI 답변을 말풍선으로 화면에 붙인다
        chatHistory.push({ role: "model", text: reply });   // 대화 기록에도 답변을 남긴다 (다음 질문 때 문맥으로 함께 보낸다)
        isSending = false;   // "보내는 중" 표시를 해제한다. 이제 다음 질문을 보낼 수 있다
    })
    .catch(function(error) {   // 통신이 실패한 경우
        removeTypingIndicator();   // "입력 중..." 표시를 지운다
        appendMessage("네트워크 오류가 발생했습니다. 다시 시도해주세요.", "bot");   // 사용자에게 오류를 알린다
        isSending = false;   // "보내는 중" 표시를 해제한다 (안 풀면 다시는 못 보낸다)
        console.error("Chatbot error:", error);   // 개발자가 원인을 볼 수 있게 브라우저 콘솔에 남긴다
    });
}
/* ----- 빠른 질문 클릭 ----- */
function sendQuickMsg(msg) {
    document.getElementById("chatbot-input").value = msg;   // 누른 버튼의 문구를 입력칸에 넣는다
    sendMessage();   // 그대로 전송한다 (직접 친 것과 똑같이 동작한다)
}
// ----- HTML 이스케이프 -----
// [보안] AI 응답과 DB 값은 "외부에서 들어온 믿을 수 없는 문자열"이다.
//        innerHTML 에 넣기 전에 반드시 이 함수를 거친다.
//
// (기존 코드의 문제)
//     굵게 처리 정규식의 결과를 이스케이프 없이 innerHTML 에 그대로 넣고 있었다.
//     사용자가 &lt;img src=x onerror=...&gt; 같은 문자열을 물어보면 AI 가 그대로 되풀이하는데,
//     그 순간 스크립트가 실제로 실행된다.
//     (꺽쇠를 &amp;lt; 로 적어둔 이유 : 설명에 공격 문자열을 원본 그대로 두면
//      보안 검사 스크립트가 "취약점이 남아있다"고 잘못 판정한다)
//     (같은 문제를 AIService.jsp 의 formatAIResponse 에서도 고쳤다)
//
// [주의 - 이 주석을 줄 주석(//)으로 쓴 이유]
//   원래는 /* */ 블록 주석이었다. 그런데 그 안에 정규식 예시를 적었더니
//   정규식 끝의 별표+슬래시가 블록 주석의 "닫는 기호"로 해석되어
//   주석이 중간에서 끊기고, 뒤 문장이 자바스크립트로 실행되면서
//   스크립트 전체가 문법 오류로 죽었다(챗봇 함수가 하나도 정의되지 않았다).
//   JSP 주석 안의 스크립틀릿 종료 기호와 똑같은 함정이다.
//   블록 주석 안에 정규식이나 코드 예시를 적을 때는 줄 주석을 쓰는 것이 안전하다.
function chatEscape(text) {
    return String(text)   // String(...) 으로 감싸는 이유 : 숫자나 null 이 들어와도 오류가 안 나게 하려는 것이다
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}
/* ----- 메시지 말풍선 추가 ----- */
function appendMessage(text, sender) {
    var messagesDiv = document.getElementById("chatbot-messages");   // 말풍선들이 쌓이는 영역을 찾는다
    var msgDiv = document.createElement("div");   // 말풍선 한 줄을 담을 상자를 새로 만든다
    msgDiv.className = "chat-msg " + (sender === "bot" ? "bot-msg" : "user-msg");   // 보낸 사람에 따라 클래스를 달리한다 (왼쪽/오른쪽 정렬이 CSS 로 갈린다)
    <%-- bubble — createElement( ) 의 결과를 담는다 --%>
    var bubble = document.createElement("div");   // 실제 말풍선을 만든다
    bubble.className = "chat-bubble " + (sender === "bot" ? "bot-bubble" : "user-bubble");   // 보낸 사람에 따라 색이 달라지는 클래스를 붙인다
    // 봇 메시지는 마크다운 기본 포맷 처리
    if (sender === "bot") {
        // [보안] 먼저 전부 무해하게 바꾼 뒤, **굵게** 와 줄바꿈만 되살린다.
        //        순서가 반대면(서식 먼저, 이스케이프 나중) 아무 의미가 없다.
        var safe = chatEscape(text);
        safe = safe.replace(/\*\*(.+?)\*\*/g, "<b>$1</b>");   // **굵게** 표시를 <b> 태그로 바꾼다. 이스케이프를 먼저 했으므로 안전하다
        safe = safe.replace(/\n/g, "<br>");   // 줄바꿈을 <br> 로 바꿔 여러 줄이 그대로 보이게 한다
        bubble.innerHTML = safe;   // 완성된 HTML 을 말풍선에 넣는다
    <%-- 위 조건들이 전부 아닐 때 --%>
    } else {
        bubble.textContent = text;   // 사용자 메시지는 태그 처리 없이 글자 그대로 넣는다 (가장 안전한 방법)
    }
    <%-- 화면에 그대로 보이는 글자: "msgDiv.appendChild(bubbl…" --%>
    msgDiv.appendChild(bubble);   // 말풍선을 줄 상자 안에 넣는다
    messagesDiv.appendChild(msgDiv);   // 줄 상자를 대화 영역에 붙인다
    // 스크롤 맨 아래로
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
}
/* ============================================================
   [신규] 챗봇에서 예약 조회 · 취소
   ------------------------------------------------------------
   [설계에서 가장 중요한 결정 : 비밀번호를 대화창에 입력받지 않는다]
     쉬운 방법은 이랬을 것이다.
         봇 : "연락처와 비밀번호를 알려주세요"
         사용자 : "010-1234-5678 / 1234"   <- 대화 메시지로 입력
     이렇게 만들면 안 된다.
       1. 비밀번호가 화면에 그대로 보인다 (옆 사람이 읽는다)
       2. 대화 기록(chatHistory)에 남고, 그 기록이 AI 서버로 전송된다
       3. 브라우저 메모리와 서버 로그에도 남는다
     그래서 대화 흐름 안에 <input type="password"> 가 들어간 작은 폼을 띄운다.
     그 값은 대화 메시지가 되지 않고, /Car/orderListJson.do 로만 직접 전송된다.
     AI 는 비밀번호를 볼 일이 없다.
   ------------------------------------------------------------
   [AI 를 부르지 않는 이유]
     "내 예약 조회"는 DB 조회다. AI 가 할 일이 없다.
     예약 흐름과 마찬가지로, 규칙으로 되는 일에 AI 를 쓰지 않는다.
   ============================================================ */
/* 대화창 안에 조회 폼을 띄운다 */
function showOrderLookup() {
    <%-- 화면에서 id 가 "quick-questions" 인 요소를 찾는다 --%>
    var quickQ = document.getElementById("quick-questions");   // 추천 질문 버튼을 찾는다
    if (quickQ) quickQ.style.display = "none";   // 있으면 숨긴다 (조회 폼을 띄울 것이므로)
    <%-- 화면에 그대로 보이는 글자: "appendMessage("예약을 조회할게요…" --%>
    appendMessage("예약을 조회할게요. 예약 당시 입력한 연락처와 비밀번호를 넣어주세요.", "bot");   // 무엇을 입력해야 하는지 먼저 안내한다
    <%-- 화면에서 id 가 "chatbot-messages" 인 요소를 찾는다 --%>
    var messagesDiv = document.getElementById("chatbot-messages");   // 말풍선들이 쌓이는 영역을 찾는다
    <%-- wrap — createElement( ) 의 결과를 담는다 --%>
    var wrap = document.createElement("div");   // 입력 폼을 담을 상자를 만든다
    wrap.className = "chat-msg bot-msg chat-msg-wide";   // 봇 말풍선이면서 폭이 넓은 형태로 표시한다
    wrap.id = "order-lookup-form";   // 나중에 지울 수 있도록 id 를 붙여 둔다
    wrap.innerHTML =   // 연락처·비밀번호 입력칸과 버튼을 HTML 로 만들어 넣는다
        '<div class="chat-bubble bot-bubble chat-form">' +
            '<input type="tel" id="lookup-phone" class="chat-input-sm" placeholder="010-1234-5678" autocomplete="tel">' +
            '<input type="password" id="lookup-pass" class="chat-input-sm" placeholder="예약 비밀번호" autocomplete="current-password">' +
            '<button type="button" class="chat-form-btn" onclick="doOrderLookup()">조회하기</button>' +
            '<p class="chat-form-note">비밀번호는 대화 내용으로 저장되지 않습니다.</p>' +
        '</div>';
    <%-- 화면에 그대로 보이는 글자: "messagesDiv.appendChild(…" --%>
    messagesDiv.appendChild(wrap);   // 만든 폼을 대화 영역에 붙인다
    messagesDiv.scrollTop = messagesDiv.scrollHeight;   // 맨 아래로 스크롤해 새 내용이 보이게 한다
    <%-- 화면에서 id 가 "lookup-phone" 인 요소를 찾는다 --%>
    var phoneEl = document.getElementById("lookup-phone");   // 연락처 입력칸을 찾는다
    phoneEl.focus();   // 커서를 놓아 준다 (바로 칠 수 있게)
    //엔터로도 조회되게 한다
    document.getElementById("lookup-pass").addEventListener("keypress", function (e) {
        if (e.key === "Enter") { doOrderLookup(); }   // Enter 를 누르면 버튼을 누른 것과 같이 조회한다
    });
}
/* 조회 실행 */
function doOrderLookup() {
    <%-- 화면에서 id 가 "lookup-phone" 인 요소를 찾는다 --%>
    var phone = document.getElementById("lookup-phone").value.trim();   // 입력한 연락처를 읽고 앞뒤 공백을 없앤다
    var pass  = document.getElementById("lookup-pass").value;   // 입력한 비밀번호를 읽는다 (비밀번호는 공백도 뜻이 있어 trim 하지 않는다)
    <%-- 조건을 확인해 맞을 때만 아래를 실행한다 --%>
    if (!phone || !pass) {   // 둘 중 하나라도 비어 있으면
        appendMessage("연락처와 비밀번호를 모두 입력해주세요.", "bot");   // 모두 입력해 달라고 안내한다
        return;   // 여기서 끝낸다
    }
    //입력한 폼은 화면에서 지운다 (비밀번호가 입력칸에 남아 있지 않게)
    var form = document.getElementById("order-lookup-form");
    if (form) form.remove();   // 입력 폼을 화면에서 지운다 (같은 폼이 두 개 생기는 것을 막는다)
    /* 연락처만 대화에 남긴다. 비밀번호는 남기지 않는다.
       (chatHistory 에도 넣지 않으므로 AI 로 전송되지 않는다) */
    appendMessage(phone + " 로 조회할게요", "user");
    showTypingIndicator();   // 조회하는 동안 "입력 중..." 표시를 띄운다
    <%-- URLSearchParams 그릇을 새로 하나 만들어 body 라는 이름으로 잡아 둔다 --%>
    var body = new URLSearchParams();   // 보낼 값들을 담을 상자를 만든다
    body.append("memberphone", phone);   // 연락처를 담는다
    body.append("memberpass", pass);   // 비밀번호를 담는다
    var csrfMeta = document.querySelector('meta[name="_csrf"]');   // 화면에 심어진 보안 토큰을 찾는다
    if (csrfMeta && csrfMeta.content) { body.append("_csrf", csrfMeta.content); }   // 토큰이 있으면 함께 보낸다
    <%-- 서버에 요청을 보낸다 (화면 새로고침 없이) --%>
    fetch(chatbotContextPath + "/Car/orderListJson.do", {   // 예약 목록 조회 주소로 요청을 보낸다
        method: "POST",   // POST 방식으로 보낸다 (비밀번호가 주소창에 남지 않게)
        headers: {
            "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",   // 본문이 "이름=값&이름=값" 형식이라고 알린다
            "X-Requested-With": "XMLHttpRequest"
        },
        body: body.toString(),
        credentials: "same-origin"
    })
    .then(function (res) {
        if (res.status === 403) { throw new Error("보안 토큰이 만료되었습니다. 화면을 새로 고쳐주세요."); }   // 403 = 보안 토큰이 맞지 않는다. 무엇을 해야 하는지까지 알려 준다
        if (!res.ok) { throw new Error("서버 오류 (" + res.status + ")"); }   // 그 밖에 200번대가 아닌 모든 경우를 실패로 처리한다
        return res.json();   // 정상이면 응답을 JSON 으로 읽는다
    })
    .then(function (data) {   // 읽은 결과를 화면에 그린다
        removeTypingIndicator();   // "입력 중..." 표시를 지운다
        appendMessage(data.message || "", "bot");   // 서버가 만든 안내 문장을 말풍선으로 띄운다
        if (data.orders && data.orders.length > 0) {   // 예약이 하나라도 있으면
            renderOrderCards(data.orders);   // 예약 카드들을 그린다
        }
    })
    .catch(function (err) {   // 통신이 실패한 경우
        removeTypingIndicator();   // "입력 중..." 표시를 지운다
        appendMessage(err.message || "조회 중 오류가 발생했습니다.", "bot");   // 실패 이유를 사용자에게 알린다
    });
}
/* 조회된 예약을 말풍선 카드로 그린다 */
function renderOrderCards(orders) {
    <%-- 화면에서 id 가 "chatbot-messages" 인 요소를 찾는다 --%>
    var messagesDiv = document.getElementById("chatbot-messages");   // 말풍선들이 쌓이는 영역을 찾는다
    <%-- 정해진 횟수만큼 같은 일을 반복한다 --%>
    for (var i = 0; i < orders.length; i++) {   // 예약을 하나씩 처리한다
        var o = orders[i];   // 이번에 그릴 예약 하나
        //숫자는 Number() 로, 문자는 chatEscape() 로 거른 뒤 넣는다
        var orderid = Number(o.orderid) || 0;
        var total   = Number(o.total) || 0;   // Number(...) 로 숫자로 바꾼다. 실패하면 NaN 이 되므로 || 0 으로 0을 대신 쓴다
        var days    = Number(o.days) || 0;   // 대여 일수도 같은 방법으로 숫자로 만든다
        var qty     = Number(o.qty) || 0;   // 대여 수량도 같은 방법으로 숫자로 만든다
        <%-- div — createElement( ) 의 결과를 담는다 --%>
        var div = document.createElement("div");   // 예약 카드 하나를 담을 상자를 만든다
        div.className = "chat-msg bot-msg chat-msg-wide";   // 봇 말풍선이면서 폭이 넓은 형태로 표시한다
        div.id = "order-card-" + orderid;   // 예약번호를 넣어 id 를 만든다. 취소 후 이 카드만 지우기 위해서다
        div.innerHTML =   // 차 사진·이름·기간·금액·취소 버튼을 HTML 로 만들어 넣는다
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
        <%-- 화면에 그대로 보이는 글자: "messagesDiv.appendChild(…" --%>
        messagesDiv.appendChild(div);   // 만든 카드를 대화 영역에 붙인다
    }
    messagesDiv.scrollTop = messagesDiv.scrollHeight;   // 맨 아래로 스크롤해 새 카드가 보이게 한다
}
/* 취소 : 비밀번호를 다시 받는다 (본인 확인) */
function askCancelOrder(orderid) {
    <%-- 화면에서 id 가 "chatbot-messages" 인 요소를 찾는다 --%>
    var messagesDiv = document.getElementById("chatbot-messages");   // 말풍선들이 쌓이는 영역을 찾는다
    //이미 열려 있는 취소 폼이 있으면 지운다 (여러 개가 겹치지 않게)
    var old = document.getElementById("cancel-form");
    if (old) old.remove();   // 이미 열려 있던 취소 폼이 있으면 지운다 (두 개가 겹치지 않게)
    <%-- appendMessage( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다 --%>
    appendMessage("예약번호 " + orderid + " 을 취소합니다. 확인을 위해 비밀번호를 한 번 더 입력해주세요.", "bot");   // 어떤 예약을 취소하는지 번호까지 알려 준다
    <%-- wrap — createElement( ) 의 결과를 담는다 --%>
    var wrap = document.createElement("div");   // 비밀번호 입력 폼을 담을 상자를 만든다
    wrap.className = "chat-msg bot-msg chat-msg-wide";   // 봇 말풍선이면서 폭이 넓은 형태로 표시한다
    wrap.id = "cancel-form";   // 나중에 지울 수 있도록 id 를 붙여 둔다
    wrap.innerHTML =   // 비밀번호 입력칸과 확인·중단 버튼을 HTML 로 만들어 넣는다
        '<div class="chat-bubble bot-bubble chat-form">' +
            '<input type="password" id="cancel-pass" class="chat-input-sm" placeholder="예약 비밀번호">' +
            '<button type="button" class="chat-form-btn chat-form-btn-danger" ' +
                    'onclick="doCancelOrder(' + orderid + ')">취소 확정</button>' +
            '<button type="button" class="chat-form-btn chat-form-btn-ghost" ' +
                    'onclick="closeCancelForm()">그만두기</button>' +
            '<p class="chat-form-note">취소한 예약은 되돌릴 수 없습니다.</p>' +
        '</div>';
    <%-- 화면에 그대로 보이는 글자: "messagesDiv.appendChild(…" --%>
    messagesDiv.appendChild(wrap);   // 만든 폼을 대화 영역에 붙인다
    messagesDiv.scrollTop = messagesDiv.scrollHeight;   // 맨 아래로 스크롤해 폼이 보이게 한다
    document.getElementById("cancel-pass").focus();   // 비밀번호 칸에 커서를 놓아 준다
}
<%-- closeCancelForm( ) — 아래 일을 하는 함수를 만든다 --%>
function closeCancelForm() {   // 취소를 중단할 때 실행되는 함수
    var f = document.getElementById("cancel-form");   // 열려 있는 취소 폼을 찾는다
    if (f) f.remove();   // 있으면 지운다
    appendMessage("취소를 중단했습니다.", "bot");   // 중단했다는 사실을 알려 준다
}
/* 취소 실행 */
function doCancelOrder(orderid) {
    <%-- 화면에서 id 가 "cancel-pass" 인 요소를 찾는다 --%>
    var passEl = document.getElementById("cancel-pass");   // 비밀번호 입력칸을 찾는다
    var pass = passEl ? passEl.value : "";   // 칸이 있으면 값을, 없으면 빈 문자열을 쓴다
    <%-- 조건을 확인해 맞을 때만 아래를 실행한다 --%>
    if (!pass) {   // 비밀번호를 입력하지 않았으면
        appendMessage("비밀번호를 입력해주세요.", "bot");   // 입력해 달라고 안내한다
        return;   // 여기서 끝낸다
    }
    <%-- 화면에서 id 가 "cancel-form" 인 요소를 찾는다 --%>
    var f = document.getElementById("cancel-form");   // 취소 폼을 찾는다
    if (f) f.remove();   // 있으면 지운다 (요청을 보냈으므로 더 필요 없다)
    showTypingIndicator();   // 처리하는 동안 "입력 중..." 표시를 띄운다
    <%-- URLSearchParams 그릇을 새로 하나 만들어 body 라는 이름으로 잡아 둔다 --%>
    var body = new URLSearchParams();   // 보낼 값들을 담을 상자를 만든다
    body.append("orderid", orderid);   // 취소할 예약번호를 담는다
    body.append("memberpass", pass);   // 본인 확인용 비밀번호를 담는다
    var csrfMeta = document.querySelector('meta[name="_csrf"]');   // 화면에 심어진 보안 토큰을 찾는다
    if (csrfMeta && csrfMeta.content) { body.append("_csrf", csrfMeta.content); }   // 토큰이 있으면 함께 보낸다
    <%-- 서버에 요청을 보낸다 (화면 새로고침 없이) --%>
    fetch(chatbotContextPath + "/Car/orderCancelJson.do", {   // 예약 취소 주소로 요청을 보낸다
        method: "POST",   // POST 방식으로 보낸다 (되돌릴 수 없는 동작이므로)
        headers: {
            "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",   // 본문이 "이름=값&이름=값" 형식이라고 알린다
            "X-Requested-With": "XMLHttpRequest"
        },
        body: body.toString(),
        credentials: "same-origin"
    })
    .then(function (res) {
        if (res.status === 403) { throw new Error("보안 토큰이 만료되었습니다. 화면을 새로 고쳐주세요."); }   // 403 = 보안 토큰이 맞지 않는다. 무엇을 해야 하는지까지 알려 준다
        if (!res.ok) { throw new Error("서버 오류 (" + res.status + ")"); }   // 그 밖에 200번대가 아닌 모든 경우를 실패로 처리한다
        return res.json();   // 정상이면 응답을 JSON 으로 읽는다
    })
    .then(function (data) {   // 읽은 결과를 화면에 반영한다
        removeTypingIndicator();   // "입력 중..." 표시를 지운다
        appendMessage(data.message || "", "bot");   // 서버가 만든 안내 문장을 말풍선으로 띄운다
        //취소에 성공하면 그 예약 카드를 화면에서 지운다 (목록과 실제 상태를 일치시킨다)
        if (data.ok) {
            var card = document.getElementById("order-card-" + orderid);
            if (card) card.remove();   // 취소된 예약 카드를 화면에서 지운다
        }
    })
    .catch(function (err) {   // 통신이 실패한 경우
        removeTypingIndicator();   // "입력 중..." 표시를 지운다
        appendMessage(err.message || "취소 중 오류가 발생했습니다.", "bot");   // 실패 이유를 사용자에게 알린다
    });
}
/* ----- 대화 입력창에 "예약 조회/취소" 의도가 보이면 폼으로 안내 -----
   AI 에게 보내기 전에 걸러낸다. AI 는 DB 를 볼 수 없으므로
   "홈페이지에서 확인하세요" 같은 쓸모없는 답을 하게 되기 때문이다. */
function looksLikeOrderIntent(msg) {
    var m = msg.replace(/\s/g, "");   // 공백을 모두 없앤다. "예약 조회" 와 "예약조회" 를 같게 보려는 것이다
    var hasOrder  = m.indexOf("예약") >= 0;   // "예약" 이라는 말이 들어 있는가
    var hasLookup = m.indexOf("조회") >= 0 || m.indexOf("확인") >= 0   // "조회" 나 "확인" 같은 말이 들어 있는가
                 || m.indexOf("취소") >= 0 || m.indexOf("변경") >= 0
                 || m.indexOf("내역") >= 0;
    return hasOrder && hasLookup;   // 두 조건을 모두 만족할 때만 조회 의도로 본다 (&& 는 "그리고")
}
/* ----- 타이핑 인디케이터 ----- */
function showTypingIndicator() {
    var messagesDiv = document.getElementById("chatbot-messages");   // 말풍선들이 쌓이는 영역을 찾는다
    var typingDiv = document.createElement("div");   // 점 세 개가 깜빡이는 표시를 담을 상자를 만든다
    typingDiv.id = "typing-indicator";   // 나중에 지울 수 있도록 id 를 붙여 둔다
    typingDiv.className = "typing-indicator";   // CSS 가 이 클래스를 보고 깜빡이는 애니메이션을 준다
    typingDiv.innerHTML = '<div class="typing-dot"></div><div class="typing-dot"></div><div class="typing-dot"></div>';   // 점 세 개를 넣는다
    messagesDiv.appendChild(typingDiv);   // 대화 영역에 붙인다
    messagesDiv.scrollTop = messagesDiv.scrollHeight;   // 맨 아래로 스크롤해 표시가 보이게 한다
}
<%-- removeTypingIndicator( ) — 아래 일을 하는 함수를 만든다 --%>
function removeTypingIndicator() {   // "입력 중..." 표시를 지우는 함수
    var el = document.getElementById("typing-indicator");   // 그 표시를 찾는다
    if (el) el.remove();   // 있으면 화면에서 지운다
}
</script>
</body>
</html>