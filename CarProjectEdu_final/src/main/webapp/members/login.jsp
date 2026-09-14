<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%--
 ============================================================================
  members/login.jsp  -  로그인 화면

  CarMain.jsp 의 가운데 영역에 끼워져(include) 보이는 화면 조각이다.
    요청 흐름 : Top.jsp 로그인 버튼 -> /member/login.me -> 사장(MemberController) -> CarMain.jsp -> 이 화면

  로그인 방법 2가지
    1. 아이디/비밀번호 : form 이 POST 로 /member/loginPro.me 에 전송
    2. 카카오 로그인   : 링크가 /member/kakaoLogin.me 로 이동 -> 카카오 로그인 화면 -> /member/kakaoCallback.me
 ============================================================================
--%>

<style>
/* ==========================================================================
   로그인 화면 전용 스타일
   선택자(.이름) 는 아래 HTML 의 class="이름" 인 태그를 꾸민다
   ========================================================================== */

/* [전체 영역] <div class="login-page"> : 로그인 카드를 화면 가운데에 놓는 바깥 틀 */
.login-page {
    display: flex;                           /* 안의 카드를 flex 방식으로 배치한다 */
    align-items: center;                     /* 카드를 세로 방향 가운데에 놓는다 */
    justify-content: center;                 /* 카드를 가로 방향 가운데에 놓는다 */
    min-height: 400px;                       /* 최소 높이 400px : 내용이 적어도 이만큼은 자리를 차지한다 */
    padding: 30px 15px;                      /* 안쪽 여백 : 위아래 30px, 좌우 15px */
    box-sizing: border-box;                  /* 높이/너비 안에 여백까지 포함해서 계산한다 */
}

/* [로그인 카드] <div class="login-card"> : 흰 바탕의 둥근 사각형 */
.login-card {
    width: 100%;                             /* 너비 : 가능한 만큼 꽉 채운다 */
    max-width: 400px;                        /* 최대 너비 400px : 넓은 화면에서도 이보다 넓어지지 않는다 */
    padding: 30px 25px;                      /* 안쪽 여백 : 위아래 30px, 좌우 25px */
    background-color: #fff;                  /* 배경색 : 흰색 */
    border: 1px solid #ddd;                  /* 테두리 : 1px 실선, 연한 회색 */
    border-radius: 10px;                     /* 모서리 둥글기 10px */
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);  /* 그림자 : 아래로 4px, 퍼짐 12px, 검정 10% 투명도 -> 카드가 떠 보인다 */
    box-sizing: border-box;                  /* 너비 100% 안에 여백과 테두리까지 포함한다 */
}

/* [카드 제목] <h2>로그인 화면</h2> */
.login-card h2 {
    text-align: center;                      /* 글자를 가운데 정렬 */
    margin-bottom: 25px;                     /* 아래 바깥 여백 25px : 입력칸과 떨어뜨린다 */
    font-size: 22px;                         /* 글자 크기 22px */
    color: #333;                             /* 글자색 : 진한 회색 */
    font-weight: bold;                       /* 글자 굵기 : 굵게 */
}

/* [화면에서 숨기는 글자] <label class="sr-only"> : 눈에는 안 보이지만 화면 읽기 프로그램(시각장애인용)은 읽는다 */
.sr-only {
    position: absolute;                      /* 원래 자리에서 빼내서 다른 요소의 배치에 영향을 주지 않게 한다 */
    width: 1px;                              /* 너비 1px */
    height: 1px;                             /* 높이 1px */
    padding: 0;                              /* 안쪽 여백 없음 */
    margin: -1px;                            /* 바깥 여백 -1px : 1px 크기마저 화면 밖으로 밀어낸다 */
    overflow: hidden;                        /* 1px 밖으로 넘치는 글자는 숨긴다 */
    clip: rect(0,0,0,0);                     /* 보이는 영역을 0 크기로 잘라 완전히 안 보이게 한다 */
    white-space: nowrap;                     /* 글자를 줄바꿈하지 않는다 */
    border: 0;                               /* 테두리 없음 */
}

/* [입력칸] 카드 안의 아이디(text), 비밀번호(password) 입력칸 공통 모양 */
.login-card input[type="text"],
.login-card input[type="password"] {
    width: 100%;                             /* 너비 : 카드 너비를 꽉 채운다 */
    padding: 10px 14px;                      /* 안쪽 여백 : 위아래 10px, 좌우 14px */
    margin-bottom: 15px;                     /* 아래 바깥 여백 15px : 입력칸끼리 떨어뜨린다 */
    border: 1px solid #ccc;                  /* 테두리 : 1px 실선, 회색 */
    border-radius: 6px;                      /* 모서리 둥글기 6px */
    font-size: 14px;                         /* 글자 크기 14px */
    box-sizing: border-box;                  /* 너비 100% 안에 여백과 테두리까지 포함한다 */
    transition: border-color 0.2s;           /* 테두리 색이 바뀔 때 0.2초 동안 부드럽게 바뀐다 */
}

/* [입력칸 선택 중] 커서가 들어가 있는 입력칸 : 파란 테두리 */
.login-card input[type="text"]:focus,
.login-card input[type="password"]:focus {
    outline: none;                           /* 브라우저가 그리는 기본 윤곽선을 없앤다 (아래 파란 테두리로 대신 표시) */
    border-color: #0055cc;                   /* 테두리 색 : 파란색 */
    box-shadow: 0 0 0 2px rgba(0,85,204,0.15); /* 테두리 바깥에 2px 두께의 옅은 파란 띠를 두른다 */
}

/* [로그인 버튼] <button class="btn-login"> : 빨간 바탕 흰 글자 */
.btn-login {
    width: 100%;                             /* 너비 : 카드 너비를 꽉 채운다 */
    padding: 12px;                           /* 안쪽 여백 : 사방 12px */
    background-color: #cc0000;               /* 배경색 : 빨간색 */
    color: white;                            /* 글자색 : 흰색 */
    border: none;                            /* 테두리 없음 */
    border-radius: 8px;                      /* 모서리 둥글기 8px */
    font-size: 16px;                         /* 글자 크기 16px */
    font-weight: bold;                       /* 글자 굵기 : 굵게 */
    cursor: pointer;                         /* 마우스를 올리면 손가락 모양 */
    transition: background-color 0.2s;       /* 배경색이 바뀔 때 0.2초 동안 부드럽게 바뀐다 */
}

/* [로그인 버튼에 마우스를 올렸을 때] 조금 더 어두운 빨간색 */
.btn-login:hover {
    background-color: #aa0000;               /* 배경색 : 어두운 빨간색 */
}

/* [좁은 화면(480px 이하, 휴대폰)일 때만 적용] 카드 여백과 제목 크기를 줄인다 */
@media (max-width: 480px) {

    .login-card {
        padding: 20px 15px;                  /* 안쪽 여백을 위아래 20px, 좌우 15px 로 줄인다 */
    }

    .login-card h2 {
        font-size: 18px;                     /* 제목 글자 크기를 18px 로 줄인다 */
    }
}

/* [구분선] <div class="sns-divider"><span>또는</span></div> : ───── 또는 ───── 모양 */
.sns-divider {
    display: flex;                           /* 왼쪽 선, 글자, 오른쪽 선을 가로 한 줄로 놓는다 */
    align-items: center;                     /* 선과 글자의 세로 위치를 가운데로 맞춘다 */
    gap: 10px;                               /* 선과 글자 사이 간격 10px */
    margin: 18px 0 12px;                     /* 바깥 여백 : 위 18px, 좌우 0, 아래 12px */
    color: #999;                             /* 글자색 : 회색 */
    font-size: 12px;                         /* 글자 크기 12px */
}

/* [구분선의 양쪽 선] ::before(글자 앞), ::after(글자 뒤)에 내용 없는 가짜 요소를 만들어 선으로 그린다 */
.sns-divider::before,
.sns-divider::after {
    content: "";                             /* 가짜 요소를 만들려면 content 가 반드시 있어야 한다 (내용은 비움) */
    flex: 1;                                 /* 글자가 쓰고 남은 가로 공간을 양쪽 선이 똑같이 나눠 가진다 */
    border-top: 1px solid #ddd;              /* 위쪽 테두리 1px 연한 회색 = 가로 선 */
}

/* [카카오 로그인 버튼] <a class="btn-kakao"> : 카카오 공식 노란색 버튼 (색은 카카오 디자인 가이드 고정값) */
.btn-kakao {
    display: flex;                           /* 말풍선 아이콘과 글자를 가로 한 줄로 놓는다 */
    align-items: center;                     /* 아이콘과 글자의 세로 위치를 가운데로 맞춘다 */
    justify-content: center;                 /* 아이콘과 글자를 버튼 가로 가운데에 놓는다 */
    gap: 8px;                                /* 아이콘과 글자 사이 간격 8px */
    width: 100%;                             /* 너비 : 카드 너비를 꽉 채운다 */
    min-height: 48px;                        /* 최소 높이 48px : 손가락으로 누르기 쉬운 크기 */
    padding: 12px;                           /* 안쪽 여백 : 사방 12px */
    background-color: #FEE500;               /* 배경색 : 카카오 공식 노란색 */
    color: #191919;                          /* 글자색 : 카카오 공식 검정색 */
    border-radius: 8px;                      /* 모서리 둥글기 8px */
    font-size: 16px;                         /* 글자 크기 16px */
    font-weight: bold;                       /* 글자 굵기 : 굵게 */
    text-decoration: none;                   /* 링크(a 태그)의 기본 밑줄을 없앤다 */
    transition: filter 0.2s;                 /* 밝기가 바뀔 때 0.2초 동안 부드럽게 바뀐다 */
}

/* [카카오 버튼에 마우스를 올렸을 때] 조금 어둡게 */
.btn-kakao:hover {
    filter: brightness(0.95);                /* 밝기를 95% 로 낮춘다 (색상값을 따로 정하지 않아도 어두워진다) */
    text-decoration: none;                   /* 마우스를 올려도 밑줄이 생기지 않게 한다 */
    color: #191919;                          /* 글자색 유지 : 카카오 공식 검정색 */
}

/* [카카오 말풍선 아이콘] <svg class="kakao-symbol"> */
.kakao-symbol {
    width: 20px;                             /* 아이콘 너비 20px */
    height: 20px;                            /* 아이콘 높이 20px */
}
</style>

    <div class="login-page">
        <div class="login-card">

            <%-- ===== 아이디/비밀번호 로그인
                       method="post" : 비밀번호가 주소창에 보이지 않도록 요청 본문에 담아 보낸다
                       전송 주소 : 사장(MemberController)의 /member/loginPro.me ===== --%>
            <form class="form-signin" method="post"
                  action="<%=request.getContextPath()%>/member/loginPro.me" id="join">

                <h2 class="form-signin-heading">로그인 화면</h2>

                <%-- 입력칸의 name 속성(id, pass)이 서버에서 값을 꺼낼 때 쓰는 이름이다  -> request.getParameter("id") --%>
                <label class="sr-only">아이디</label>
                <input type="text" id="id" name="id"
                       placeholder="아이디" required autofocus>

                <label for="pass" class="sr-only">비밀번호</label>
                <input type="password" id="pass" name="pass"
                       class="form-control" placeholder="패스워드" required>

                <button class="btn-login" type="submit">로그인</button>

            </form>

            <%-- ===== 카카오 로그인 : 우리 서버는 카카오 비밀번호를 받지 않는다
                       이 링크 -> /member/kakaoLogin.me -> 카카오 로그인 화면 -> /member/kakaoCallback.me ===== --%>
            <div class="sns-divider"><span>또는</span></div>

            <a class="btn-kakao" href="<%=request.getContextPath()%>/member/kakaoLogin.me">
                <%-- 카카오 말풍선 아이콘 : 이미지 파일 없이 SVG 도형으로 그린다 --%>
                <svg class="kakao-symbol" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                    <path fill="currentColor"
                          d="M12 3C6.48 3 2 6.54 2 10.9c0 2.8 1.86 5.26 4.66 6.66-.15.52-.97 3.36-1 3.58 0 0-.02.17.09.24.11.07.24.02.24.02.32-.05 3.65-2.39 4.23-2.79.58.08 1.17.13 1.78.13 5.52 0 10-3.54 10-7.84C22 6.54 17.52 3 12 3z"/>
                </svg>
                카카오로 시작하기
            </a>

        </div>
    </div>
