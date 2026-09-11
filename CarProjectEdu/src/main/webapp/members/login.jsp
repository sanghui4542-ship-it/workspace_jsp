<%-- 이 페이지의 글자 인코딩을 UTF-8 로 정한다. 빠뜨리면 한글이 깨진다 --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<style>
/* =============================================
   [로그인 페이지] 반응형 스타일
   ============================================= */
/* 전체 페이지 가운데 정렬 */
.login-page {
    /* 요소를 어떤 방식으로 배치할지 flex */
    display: flex;
    /* 세로 방향 정렬 center */
    align-items: center;
    /* 가로 방향 정렬 center */
    justify-content: center;
    /* 세로로 이보다 작아지지 않게 400px */
    min-height: 400px;
    /* 안쪽 여백 30px 15px */
    padding: 30px 15px;
    /* 크기 계산에 여백 포함 여부 border-box */
    box-sizing: border-box;
}
/* 로그인 카드 */
.login-card {
    /* 가로 크기 100% */
    width: 100%;
    /* 가로로 이보다 커지지 않게 400px */
    max-width: 400px;     /* 최대 너비 (수정: 여기서 너비 조정) */
    padding: 30px 25px;
    /* 배경 색 #fff */
    background-color: #fff;
    /* 테두리 1px solid #ddd */
    border: 1px solid #ddd;
    /* 모서리 둥글기 10px */
    border-radius: 10px;
    /* 그림자 0 4px 12px rgba(0,0,0,0.1) */
    box-shadow: 0 4px 12px rgba(0,0,0,0.1); /* 그림자 효과 */
    box-sizing: border-box;
}
/* 로그인 제목 */
.login-card h2 {
    /* 글자 정렬 center */
    text-align: center;
    /* 아래 바깥 여백 25px */
    margin-bottom: 25px;
    /* 글자 크기 22px */
    font-size: 22px;
    /* 글자 색 #333 */
    color: #333;
    /* 글자 굵기 bold */
    font-weight: bold;
}
/* 스크린리더용 숨김 클래스 (접근성) */
.sr-only {
    /* 위치 기준 absolute */
    position: absolute;
    /* 가로 크기 1px */
    width: 1px;
    /* 세로 크기 1px */
    height: 1px;
    /* 안쪽 여백 0 */
    padding: 0;
    /* 바깥 여백 -1px */
    margin: -1px;
    /* 넘칠 때 처리 hidden */
    overflow: hidden;
    /* 세부 모양 clip — rect(0,0,0,0) */
    clip: rect(0,0,0,0);
    /* 공백·줄바꿈 처리 방식 nowrap */
    white-space: nowrap;
    /* 테두리 0 */
    border: 0;
}
/* 입력 필드 공통 스타일 */
.login-card input[type="text"],
.login-card input[type="password"] {
    /* 가로 크기 100% */
    width: 100%;
    /* 안쪽 여백 10px 14px */
    padding: 10px 14px;
    /* 아래 바깥 여백 15px */
    margin-bottom: 15px;
    /* 테두리 1px solid #ccc */
    border: 1px solid #ccc;
    /* 모서리 둥글기 6px */
    border-radius: 6px;
    /* 글자 크기 14px */
    font-size: 14px;
    /* 크기 계산에 여백 포함 여부 border-box */
    box-sizing: border-box;
    /* 값이 바뀔 때 부드럽게 border-color 0.2s */
    transition: border-color 0.2s; /* 포커스 애니메이션 */
}
/* 포커스 시 테두리 파랑 */
.login-card input[type="text"]:focus,
.login-card input[type="password"]:focus {
    /* 바깥 윤곽선 none */
    outline: none;
    /* 테두리 색 #0055cc */
    border-color: #0055cc;
    /* 그림자 0 0 0 2px rgba(0,85,204,0.15) */
    box-shadow: 0 0 0 2px rgba(0,85,204,0.15);
}
/* 로그인 버튼 */
.btn-login {
    /* 가로 크기 100% */
    width: 100%;
    /* 안쪽 여백 12px */
    padding: 12px;
    /* 배경 색 #cc0000 */
    background-color: #cc0000;   /* 빨간 버튼 (수정: 여기서 색상 변경) */
    color: white;
    /* 테두리 none */
    border: none;
    /* 모서리 둥글기 8px */
    border-radius: 8px;
    /* 글자 크기 16px */
    font-size: 16px;
    /* 글자 굵기 bold */
    font-weight: bold;
    /* 마우스 커서 모양 pointer */
    cursor: pointer;
    /* 값이 바뀔 때 부드럽게 background-color 0.2s */
    transition: background-color 0.2s;
}
/* class="btn-login" 이 붙은 요소의 모양을 정한다 */
.btn-login:hover {
    /* 배경 색 #aa0000 */
    background-color: #aa0000;
}
/* =============================================
   [반응형] 모바일 (480px 이하)
   ============================================= */
@media (max-width: 480px) {
    /* class="login-card" 이 붙은 요소의 모양을 정한다 */
    .login-card {
        /* 안쪽 여백 20px 15px */
        padding: 20px 15px;
    }
    /* class="login-card" 이 붙은 요소의 모양을 정한다 */
    .login-card h2 {
        /* 글자 크기 18px */
        font-size: 18px;
    }
}
/* 모바일 반응형 끝 */
/* =============================================
   [카카오 로그인 버튼]
   색상은 카카오 디자인 가이드 고정값이라 토큰을 쓰지 않는다.
     배경 #FEE500 (카카오 노랑) / 글자·심볼 #191919
   ============================================= */
.sns-divider {
    /* 요소를 어떤 방식으로 배치할지 flex */
    display: flex;
    /* 세로 방향 정렬 center */
    align-items: center;
    /* 요소 사이 간격 10px */
    gap: 10px;
    /* 바깥 여백 18px 0 12px */
    margin: 18px 0 12px;
    /* 글자 색 #999 */
    color: #999;
    /* 글자 크기 12px */
    font-size: 12px;
}
.sns-divider::before,
.sns-divider::after {
    /* 가상 요소에 넣을 내용 "" */
    content: "";
    /* 늘어나는 비율 1 */
    flex: 1;
    /* 위 테두리 1px solid #ddd */
    border-top: 1px solid #ddd;
}
/* class="btn-kakao" 이 붙은 요소의 모양을 정한다 */
.btn-kakao {
    /* 요소를 어떤 방식으로 배치할지 flex */
    display: flex;
    /* 세로 방향 정렬 center */
    align-items: center;
    /* 가로 방향 정렬 center */
    justify-content: center;
    /* 요소 사이 간격 8px */
    gap: 8px;
    /* 가로 크기 100% */
    width: 100%;
    /* 세로로 이보다 작아지지 않게 48px */
    min-height: 48px;              /* 터치 타깃 44px 이상 */
    padding: 12px;
    /* 배경 색 #FEE500 */
    background-color: #FEE500;     /* 카카오 공식 노랑 */
    color: #191919;                /* 카카오 공식 글자색 */
    border-radius: 8px;
    /* 글자 크기 16px */
    font-size: 16px;
    /* 글자 굵기 bold */
    font-weight: bold;
    /* 밑줄 같은 글자 장식 none */
    text-decoration: none;
    /* 값이 바뀔 때 부드럽게 filter 0.2s */
    transition: filter 0.2s;
}
.btn-kakao:hover {
    /* 세부 모양 filter — brightness(0.95);      /* 살짝 어둡게 (색상값을 따 */
    filter: brightness(0.95);      /* 살짝 어둡게 (색상값을 따로 두지 않아도 된다) */
    text-decoration: none;
    /* 글자 색 #191919 */
    color: #191919;
}
.kakao-symbol {
    /* 가로 크기 20px */
    width: 20px;
    /* 세로 크기 20px */
    height: 20px;
}
</style>
    <!-- ==========================================
         로그인 화면
         ========================================== -->
    <div class="login-page">
        <%-- login-card 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
        <div class="login-card">
            <%--MemberController서블릿에.. 로그인 처리 요청시! 입력한 id와 패스워드 전달 --%>
            <%--
             [보안 수정] method="post" 를 추가했다.
               기존에는 method 속성이 없어 브라우저 기본값인 GET으로 전송됐다.
               그러면 로그인 정보가 주소창에 그대로 노출된다.
                   /member/loginPro.me?id=admin&pass=1234
               이 주소는 브라우저 방문기록, 서버 접속로그(access log),
               그리고 다른 사이트로 이동할 때 Referer 헤더에까지 남는다.
               비밀번호처럼 민감한 값은 반드시 POST 본문으로 보내야 한다.
            --%>
            <form class="form-signin" method="post"
                  action="<%=request.getContextPath()%>/member/loginPro.me" id="join">
                <%--
                 ================================================================
                   [보안] CSRF 토큰
                   ${_csrf} 는 세션에 저장된 무작위 문자열이다.
                   CsrfFilter 가 화면을 보여줄 때(GET) 만들어 세션에 넣어두고,
                   POST 요청이 오면 이 값이 함께 왔는지 확인한다.
                   [왜 필요한가]
                     브라우저는 우리 사이트로 가는 요청에 쿠키(JSESSIONID)를 자동으로 붙인다.
                     그래서 공격자가 만든 페이지에 아래 한 줄만 심어두면,
                         <form action="http://우리사이트/CarProject/Board/deleteBoard.do" method="post">
                         <script>document.forms[0].submit()</script>
                     그 페이지를 열어본 로그인 사용자의 권한으로 글이 삭제된다.
                     사용자는 클릭조차 하지 않았다.
                   [토큰이 어떻게 막는가]
                     공격자의 페이지는 우리 사이트의 세션 값을 읽을 수 없다.
                     (브라우저의 동일 출처 정책 때문에 다른 사이트의 HTML 을 읽지 못한다)
                     따라서 올바른 토큰을 넣은 요청을 만들 수 없다.
                   [토큰은 비밀번호가 아니다]
                     "이 요청이 우리 화면에서 출발했는가"만 확인하는 값이다.
                     그래서 화면 소스에 그대로 보여도 문제가 되지 않는다.
                   자세한 설명 : util/CsrfToken.java , filter/CsrfFilter.java
                 ================================================================
                --%>
                <input type="hidden" name="_csrf" value="${_csrf}">
                <%-- 제목 --%>
                <h2 class="form-signin-heading">로그인 화면</h2>
                <!-- 아이디 입력 (스크린리더용 레이블) -->
                <label class="sr-only">아이디</label>
                <%-- 아이디 입력칸. 서버에서 request.getParameter("id") 로 받는다 --%>
                <input type="text" id="id" name="id"
                       placeholder="아이디" required autofocus>
                <!-- 비밀번호 입력 -->
                <label for="inputPassword" class="sr-only">비밀번호</label>
                <%-- 비밀번호 입력칸. 서버에서 request.getParameter("pass") 로 받는다 --%>
                <input type="password" id="pass" name="pass"
                       class="form-control" placeholder="패스워드" required>
                <!-- 로그인 버튼 -->
                <button class="btn-login" type="submit">로그인</button>
            </form>
            <%--
             ================================================================
               카카오 로그인
               폼(아이디/비밀번호)과는 완전히 다른 길이다.
               이 링크는 사용자를 "카카오의 로그인 화면"으로 보내고,
               카카오가 인증을 마치면 /member/kakaoCallback.me 로 돌려보낸다.
               우리 서버는 카카오 비밀번호를 아예 만지지 않는다.
               (전체 흐름 : util/KakaoAuth.java 상단 주석)
               GET 링크라서 CSRF 토큰이 필요 없다.
               (대신 kakaoLogin.me 가 state 값으로 콜백을 검증한다)
             ================================================================
            --%>
            <div class="sns-divider"><span>또는</span></div>
            <%-- 다른 화면으로 넘어가는 링크 --%>
            <a class="btn-kakao" href="<%=request.getContextPath()%>/member/kakaoLogin.me">
                <%-- 카카오 심볼 (말풍선) : 이미지 파일 없이 SVG 로 그린다 --%>
                <svg class="kakao-symbol" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                    <path fill="currentColor"
                          d="M12 3C6.48 3 2 6.54 2 10.9c0 2.8 1.86 5.26 4.66 6.66-.15.52-.97 3.36-1 3.58 0 0-.02.17.09.24.11.07.24.02.24.02.32-.05 3.65-2.39 4.23-2.79.58.08 1.17.13 1.78.13 5.52 0 10-3.54 10-7.84C22 6.54 17.52 3 12 3z"/>
                </svg>
                <%-- 화면에 그대로 보이는 글자: "카카오로 시작하기" --%>
                카카오로 시작하기
            </a>
        </div>
    </div>