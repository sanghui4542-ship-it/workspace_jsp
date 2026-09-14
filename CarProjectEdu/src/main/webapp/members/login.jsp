<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<style>
/* =============================================
   [로그인 페이지] 반응형 스타일
   ============================================= */

/* 전체 페이지 가운데 정렬 */
.login-page {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 400px;
    padding: 30px 15px;
    box-sizing: border-box;
}

/* 로그인 카드 */
.login-card {
    width: 100%;
    max-width: 400px;     /* 최대 너비 (수정: 여기서 너비 조정) */
    padding: 30px 25px;
    background-color: #fff;
    border: 1px solid #ddd;
    border-radius: 10px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.1); /* 그림자 효과 */
    box-sizing: border-box;
}

/* 로그인 제목 */
.login-card h2 {
    text-align: center;
    margin-bottom: 25px;
    font-size: 22px;
    color: #333;
    font-weight: bold;
}

/* 스크린리더용 숨김 클래스 (접근성) */
.sr-only {
    position: absolute;
    width: 1px;
    height: 1px;
    padding: 0;
    margin: -1px;
    overflow: hidden;
    clip: rect(0,0,0,0);
    white-space: nowrap;
    border: 0;
}

/* 입력 필드 공통 스타일 */
.login-card input[type="text"],
.login-card input[type="password"] {
    width: 100%;
    padding: 10px 14px;
    background-color: #fff;
    margin-bottom: 15px;
    border: 1px solid #ccc;
    border-radius: 6px;
    font-size: 14px;
    color : #333;
    box-sizing: border-box;
    transition: border-color 0.2s; /* 포커스 애니메이션 */
}

/* 포커스 시 테두리 파랑 */
.login-card input[type="text"]:focus,
.login-card input[type="password"]:focus {
    outline: none;
    border-color: #0055cc;
    box-shadow: 0 0 0 2px rgba(0,85,204,0.15);
}

/* 로그인 버튼 */
.btn-login {
    width: 100%;
    padding: 12px;
    background-color: #cc0000;   /* 빨간 버튼 (수정: 여기서 색상 변경) */
    color: white;
    border: none;
    border-radius: 8px;
    font-size: 16px;
    font-weight: bold;
    cursor: pointer;
    transition: background-color 0.2s;
}

.btn-login:hover {
    background-color: #aa0000;
}

/* =============================================
   [반응형] 모바일 (480px 이하)
   ============================================= */
@media (max-width: 480px) {

    .login-card {
        padding: 20px 15px;
    }

    .login-card h2 {
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
    display: flex;
    align-items: center;
    gap: 10px;
    margin: 18px 0 12px;
    color: #999;
    font-size: 12px;
}
.sns-divider::before,
.sns-divider::after {
    content: "";
    flex: 1;
    border-top: 1px solid #ddd;
}

.btn-kakao {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    width: 100%;
    min-height: 48px;              /* 터치 타깃 44px 이상 */
    padding: 12px;
    background-color: #FEE500;     /* 카카오 공식 노랑 */
    color: #191919;                /* 카카오 공식 글자색 */
    border-radius: 8px;
    font-size: 16px;
    font-weight: bold;
    text-decoration: none;
    transition: filter 0.2s;
}
.btn-kakao:hover {
    filter: brightness(0.95);      /* 살짝 어둡게 (색상값을 따로 두지 않아도 된다) */
    text-decoration: none;
    color: #191919;
}
.kakao-symbol {
    width: 20px;
    height: 20px;
}
</style>


    <!-- ==========================================
         로그인 화면
         ========================================== -->
    <div class="login-page">
        <div class="login-card">

            <%--MemberController서블릿에.. 로그인 처리 요청시! 입력한 id와 패스워드 전달 --%>

            <form class="form-signin" method="post"
                  action="<%=request.getContextPath()%>/member/loginPro.me" id="join">

                <input type="hidden" name="_csrf" value="${_csrf}">

                <h2 class="form-signin-heading">로그인 화면</h2>

                <!-- 아이디 입력 (스크린리더용 레이블) -->
                <label class="sr-only">아이디</label>
                <input type="text" id="id" name="id" placeholder="아이디" required autofocus>

                <!-- 비밀번호 입력 -->
                <label for="inputPassword" class="sr-only">비밀번호</label>
                <input type="password" id="pass" name="pass" class="form-control" placeholder="패스워드" required>

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

            <a class="btn-kakao" href="<%=request.getContextPath()%>/member/kakaoLogin.me">
                <%-- 카카오 심볼 (말풍선) : 이미지 파일 없이 SVG 로 그린다 --%>
                <svg class="kakao-symbol" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                    <path fill="currentColor"
                          d="M12 3C6.48 3 2 6.54 2 10.9c0 2.8 1.86 5.26 4.66 6.66-.15.52-.97 3.36-1 3.58 0 0-.02.17.09.24.11.07.24.02.24.02.32-.05 3.65-2.39 4.23-2.79.58.08 1.17.13 1.78.13 5.52 0 10-3.54 10-7.84C22 6.54 17.52 3 12 3z"/>
                </svg>
                카카오로 시작하기
            </a>

        </div>
    </div>

