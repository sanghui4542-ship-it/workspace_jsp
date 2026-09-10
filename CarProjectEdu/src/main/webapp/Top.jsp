 <%-- 이 페이지의 글자 인코딩을 UTF-8 로 정한다. 빠뜨리면 한글이 깨진다 --%>
 <%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%--
 [5단계 변경] 여기 있던 CSS <link> 를 제거했다.
   (제거된 코드)
     cdn.jsdelivr.net 의 bootstrap 4.6.2 css 를 link 태그로 불러오던 한 줄
   제거한 이유 2가지
     1) 외부 서버(CDN)에 의존했다
        사내망이나 인터넷이 없는 강의실에서는 이 파일을 못 받아와
        화면 전체 레이아웃이 깨졌다.
        프로젝트 안에 이미 같은 파일(css/bootstrap.min.css)이 있었는데도 쓰지 않고 있었다.
     2) 이 파일은 이제 "조각(fragment)"이다
        Top.jsp 는 CarMain.jsp 안에 include 되므로, 여기에 <link> 를 쓰면
        <body> 안에 스타일시트가 들어간다.
        스타일이 언제 적용되는지 예측할 수 없고, 화면이 한 번 깜빡이는 원인이 된다.
   CSS 는 이제 문서 주인인 CarMain.jsp 의 <head> 에서 한 번만 불러온다.
--%>
<style>
/* =============================================
   [전체 폰트 기본 크기] - 반응형 폰트 설정
   html의 font-size를 62.5%로 설정하면
   1rem = 10px 이 되어 계산하기 쉬워짐
   ============================================= */
html {
    /* 글자 크기 62.5% */
    font-size: 62.5%; /* 기본 폰트 크기 반응형 설정 */
}
/* =============================================
   [상단 헤더 레이아웃] - flexbox 반응형
   ============================================= */
header.site-header {
    /* 요소를 어떤 방식으로 배치할지 flex */
    display: flex;
    /* 세로 방향 정렬 center */
    align-items: center;
    /* 가로 방향 정렬 space-between */
    justify-content: space-between;
    /* 줄바꿈 허용 wrap */
    flex-wrap: wrap;
    /* 안쪽 여백 8px 20px */
    padding: 8px 20px;
    /* 배경 #fff */
    background: #fff;
    /* 아래 테두리 1px solid #eee */
    border-bottom: 1px solid #eee;
    /* 요소 사이 간격 8px */
    gap: 8px;
}
/* =============================================
   [상단 로그인/버튼 영역] - flexbox 정렬
   ============================================= */
#login {
    /* 요소를 어떤 방식으로 배치할지 flex */
    display: flex;
    /* 세로 방향 정렬 center */
    align-items: center;
    /* 줄바꿈 허용 wrap */
    flex-wrap: wrap;
    /* 요소 사이 간격 6px */
    gap: 6px;
    /* 글꼴 Arial, Helvetica, sans-serif */
    font-family: Arial, Helvetica, sans-serif;
    /* 글자 크기 1.3rem */
    font-size: 1.3rem;
}
/* 로그인된 사용자 ID 표시 */
#login .login-user-id {
    /* 글자 굵기 bold */
    font-weight: bold;
    /* 글자 색 #cc0000 */
    color: #cc0000;
    /* 글자 크기 1.3rem */
    font-size: 1.3rem;
    /* 안쪽 여백 0 4px */
    padding: 0 4px;
}
/* 버튼 공통 스타일 - 알약형 */
#login .top-btn {
    /* 요소를 어떤 방식으로 배치할지 inline-flex */
    display: inline-flex;
    /* 세로 방향 정렬 center */
    align-items: center;
    /* 가로 방향 정렬 center */
    justify-content: center;
    /* 안쪽 여백 5px 14px */
    padding: 5px 14px;
    /* 모서리 둥글기 20px */
    border-radius: 20px;
    /* 테두리 none */
    border: none;
    /* 글자 크기 1.25rem */
    font-size: 1.25rem;
    /* 글자 굵기 600 */
    font-weight: 600;
    /* 마우스 커서 모양 pointer */
    cursor: pointer;
    /* 값이 바뀔 때 부드럽게 all 0.2s ease */
    transition: all 0.2s ease;
    /* 공백·줄바꿈 처리 방식 nowrap */
    white-space: nowrap;
    /* 밑줄 같은 글자 장식 none */
    text-decoration: none;
}
/*
  [터치 타깃 규격 - 6단계 반응형 기준]
    손가락으로 누르는 화면에서는 버튼이 최소 44x44px 이어야 한다.
    (애플 휴먼 인터페이스 가이드라인, 구글 머티리얼 디자인이 같은 값을 권한다)
    padding: 5px 14px 만으로는 높이가 30px 밖에 안 된다.
    마우스로는 문제없지만 스마트폰에서는 옆 버튼을 잘못 누르게 된다.
    데스크톱에서 헤더가 불필요하게 두꺼워지지 않도록 세 조건 중 하나만 맞으면 적용한다.
    - hover: none        -> 손가락으로 쓰는 기기 (스마트폰, 태블릿)
    - pointer: coarse    -> 정밀하지 않은 포인터
    - max-width: 767px   -> 좁은 화면 (기기 종류를 못 알아내는 브라우저 대비)
    앞의 두 조건만 쓰면 마우스가 달린 노트북에서 창을 좁혔을 때는 적용되지 않는다.
    폭 조건을 함께 넣으면 어떤 환경에서든 좁은 화면에서 44px 가 보장된다.
*/
@media (hover: none), (pointer: coarse), (max-width: 767px) {
    /* id="login" 인 요소 하나의 모양을 정한다 */
    #login .top-btn {
        /* 세로로 이보다 작아지지 않게 44px */
        min-height: 44px;
        /* 가로 최소 크기 44px */
        min-width: 44px;
        /* 안쪽 여백 5px 16px */
        padding: 5px 16px;
    }
}
/* 주요 버튼 (로그인, 회원가입, 정보수정) */
#login .top-btn-primary {
    /* 배경 #cc0000 */
    background: #cc0000;
    /* 글자 색 #fff */
    color: #fff;
}
/* id="login" 인 요소 하나의 모양을 정한다 */
#login .top-btn-primary:hover {
    /* 배경 #aa0000 */
    background: #aa0000;
    /* 글자 색 #fff */
    color: #fff;
    /* 이동·회전·확대 translateY(-1px) */
    transform: translateY(-1px);
    /* 그림자 0 3px 8px rgba(204,0,0,0.3) */
    box-shadow: 0 3px 8px rgba(204,0,0,0.3);
}
/* 보조 버튼 (로그아웃, 방명록) */
#login .top-btn-outline {
    /* 배경 transparent */
    background: transparent;
    /* 글자 색 #555 */
    color: #555;
    /* 테두리 1.5px solid #bbb */
    border: 1.5px solid #bbb;
}
/* id="login" 인 요소 하나의 모양을 정한다 */
#login .top-btn-outline:hover {
    /* 배경 #f5f5f5 */
    background: #f5f5f5;
    /* 글자 색 #333 */
    color: #333;
    /* 테두리 색 #888 */
    border-color: #888;
}
/* 구분선 */
#login .top-divider {
    /* 가로 크기 1px */
    width: 1px;
    /* 세로 크기 18px */
    height: 18px;
    /* 배경 #ddd */
    background: #ddd;
    /* 바깥 여백 0 2px */
    margin: 0 2px;
}
/* 검색 폼 인라인 스타일 */
#login .top-search-form {
    /* 요소를 어떤 방식으로 배치할지 flex */
    display: flex;
    /* 세로 방향 정렬 center */
    align-items: center;
    /* 요소 사이 간격 0 */
    gap: 0;
}
/* id="login" 인 요소 하나의 모양을 정한다 */
#login .top-search-input {
    /* 안쪽 여백 5px 12px */
    padding: 5px 12px;
    /* 테두리 1.5px solid #ccc */
    border: 1.5px solid #ccc;
    /* 오른쪽 테두리 none */
    border-right: none;
    /* 모서리 둥글기 20px 0 0 20px */
    border-radius: 20px 0 0 20px;
    /* 글자 크기 1.25rem */
    font-size: 1.25rem;
    /* 바깥 윤곽선 none */
    outline: none;
    /* 가로 크기 140px */
    width: 140px;
    /* 값이 바뀔 때 부드럽게 border-color 0.2s */
    transition: border-color 0.2s;
}
/* id="login" 인 요소 하나의 모양을 정한다 */
#login .top-search-input:focus {
    /* 테두리 색 #cc0000 */
    border-color: #cc0000;
}
/* id="login" 인 요소 하나의 모양을 정한다 */
#login .top-search-btn {
    /* 안쪽 여백 5px 14px */
    padding: 5px 14px;
    /* 배경 #cc0000 */
    background: #cc0000;
    /* 글자 색 #fff */
    color: #fff;
    /* 테두리 none */
    border: none;
    /* 모서리 둥글기 0 20px 20px 0 */
    border-radius: 0 20px 20px 0;
    /* 글자 크기 1.25rem */
    font-size: 1.25rem;
    /* 마우스 커서 모양 pointer */
    cursor: pointer;
    /* 값이 바뀔 때 부드럽게 background 0.2s */
    transition: background 0.2s;
}
/* id="login" 인 요소 하나의 모양을 정한다 */
#login .top-search-btn:hover {
    /* 배경 #aa0000 */
    background: #aa0000;
}
/* 검색 버튼도 좁은 화면에서는 44px 를 보장한다 (위 top-btn 과 같은 이유) */
@media (hover: none), (pointer: coarse), (max-width: 767px) {
    /* id="login" 인 요소 하나의 모양을 정한다 */
    #login .top-search-btn {
        /* 세로로 이보다 작아지지 않게 44px */
        min-height: 44px;
        /* 가로 최소 크기 44px */
        min-width: 44px;
    }
    /* 검색 입력창도 함께 키워야 버튼과 높이가 맞는다 */
    #login .top-search-input {
        /* 세로로 이보다 작아지지 않게 44px */
        min-height: 44px;
    }
}
/* 로그인 링크 스타일 (레거시 호환) */
#login a {
    /* 밑줄 같은 글자 장식 none */
    text-decoration: none;
    /* 글자 색 #333 */
    color: #333;
}
/* id="login" 인 요소 하나의 모양을 정한다 */
#login a:hover {
    /* 글자 색 #cc0000 */
    color: #cc0000;
}
/* =============================================
   [로고 영역] - 이미지(RENT.jpg) 제거 → CSS 텍스트 로고로 대체
   - 수정 방법: .logo-text 안의 텍스트를 변경해 회사명 수정
   - 수정 방법: .logo-icon 배경색(background-color) 변경으로 아이콘 색 변경
   ============================================= */
#logo {
    /* 바깥 여백 0 */
    margin: 0;
    /* 세부 모양 flex-shrink — 0 */
    flex-shrink: 0;
}
/* 로고 링크: flex 사용으로 아이콘+텍스트 가로 배치 */
#logo a {
    /* 밑줄 같은 글자 장식 none */
    text-decoration: none;
    /* 요소를 어떤 방식으로 배치할지 flex */
    display: flex;
    /* 세로 방향 정렬 center */
    align-items: center;
    /* 요소 사이 간격 10px */
    gap: 10px;         /* 아이콘과 텍스트 사이 간격 */
}
/* CSS 아이콘 박스 (이미지 RENT.jpg 대체) */
/* 수정 방법: background-color 값으로 아이콘 배경색 변경 */
#logo .logo-icon {
    /* 가로 크기 44px */
    width: 44px;
    /* 세로 크기 44px */
    height: 44px;
    /* 배경 색 #cc0000 */
    background-color: #cc0000; /* 빨간 배경 */
    border-radius: 8px;
    /* 요소를 어떤 방식으로 배치할지 flex */
    display: flex;
    /* 세로 방향 정렬 center */
    align-items: center;
    /* 가로 방향 정렬 center */
    justify-content: center;
    /* 글자 색 white */
    color: white;
    /* 글자 굵기 900 */
    font-weight: 900;
    /* 글자 크기 1.1rem */
    font-size: 1.1rem;
    /* 글자 사이 간격 -1px */
    letter-spacing: -1px;
    /* 그림자 2px 2px 6px rgba(0,0,0,0.2) */
    box-shadow: 2px 2px 6px rgba(0,0,0,0.2); /* 그림자 효과 */
    flex-shrink: 0; /* 크기 고정 */
}
/* 로고 텍스트 영역 */
#logo .logo-text-wrap {
    /* 요소를 어떤 방식으로 배치할지 flex */
    display: flex;
    /* 가로/세로 배치 방향 column */
    flex-direction: column;
}
/* 회사명 (수정 방법: 여기 텍스트를 변경하면 로고 이름 바뀜) */
#logo .logo-text {
    /* 글자 크기 1.5rem */
    font-size: 1.5rem;
    /* 글자 굵기 900 */
    font-weight: 900;
    /* 글자 색 #cc0000 */
    color: #cc0000;    /* 빨간색 (수정 방법: 이 색상값 변경) */
    letter-spacing: 2px;
    /* 줄 간격 1.1 */
    line-height: 1.1;
}
/* 로고 아래 작은 영문 부제목 */
#logo .logo-sub {
    /* 글자 크기 0.6rem */
    font-size: 0.6rem;
    /* 글자 색 #999 */
    color: #999;
    /* 글자 사이 간격 1px */
    letter-spacing: 1px;
    /* 세부 모양 text-transform — uppercase */
    text-transform: uppercase;
}
/* =============================================
   [네비게이션 메뉴바] 반응형 설정
   - 배경색 빨강 (기존 bgcolor="red" 대체)
   - PC: 5개 메뉴 가로 배치
   - 모바일: 접히는 햄버거 메뉴
   ============================================= */
.navbar-custom {
    /* 배경 색 #cc0000 */
    background-color: #cc0000; /* 메뉴 배경색 빨강 (수정 방법: 여기서 색상 변경) */
    padding: 0;
    /* 가로 크기 100% */
    width: 100%;
    /* float 해제 both */
    clear: both;               /* float 해제 */
}
/* 메뉴 링크 기본 스타일 */
.navbar-custom .nav-link {
    /* 글자 색 #ffffff !important */
    color: #ffffff !important; /* 흰색 글자 */
    font-size: 1.6rem;         /* 글자 크기 (수정 방법: 값 변경으로 크기 조정) */
    font-weight: bold;
    /* 글자 정렬 center */
    text-align: center;
    /* 안쪽 여백 12px 10px */
    padding: 12px 10px;        /* 위아래 12px, 좌우 10px 안여백 */
    display: block;
}
/* 마우스 올릴때 스타일 */
.navbar-custom .nav-link:hover {
    /* 배경 색 rgba(255, 255, 255, 0.2) */
    background-color: rgba(255, 255, 255, 0.2); /* 반투명 흰색 배경 */
    color: #ffff00 !important;                  /* 노란색 글자 */
}
/* 햄버거 메뉴 버튼 색상 (모바일에서 보임) */
.navbar-custom .navbar-toggler {
    /* 테두리 색 rgba(255, 255, 255, 0.5) */
    border-color: rgba(255, 255, 255, 0.5);
    /* 바깥 여백 5px */
    margin: 5px;
}
/* 햄버거 아이콘 SVG (이미지 없이 인라인 SVG 데이터로 구현) */
.navbar-custom .navbar-toggler-icon {
    /* 세부 모양 background-image — url("data:image/svg+xml;charset=utf8,%3C */
    background-image: url("data:image/svg+xml;charset=utf8,%3Csvg viewBox='0 0 30 30' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath stroke='rgba(255,255,255,1)' stroke-width='2' stroke-linecap='round' stroke-miterlimit='10' d='M4 7h22M4 15h22M4 23h22'/%3E%3C/svg%3E");
}
/* =============================================
   [반응형] 모바일 (768px 이하)
   ============================================= */
@media (max-width: 768px) {
    /* 헤더: 세로 방향으로 쌓기 */
    header.site-header {
        /* 안쪽 여백 8px 10px */
        padding: 8px 10px;
        /* 가로/세로 배치 방향 column */
        flex-direction: column;
        /* 세로 방향 정렬 flex-start */
        align-items: flex-start;
    }
    /* 로고 크기 줄이기 */
    #logo {
        /* 바깥 여백 0 */
        margin: 0;
    }
    /* id="logo" 인 요소 하나의 모양을 정한다 */
    #logo .logo-icon {
        /* 가로 크기 36px */
        width: 36px;
        /* 세로 크기 36px */
        height: 36px;
        /* 글자 크기 0.9rem */
        font-size: 0.9rem;
    }
    /*
      [터치 타깃] 로고는 홈으로 가는 링크다.
        좁은 화면에서 아이콘을 36px 로 줄여 자리를 아끼되,
        누르는 영역(<a>)만 44px 로 유지한다.
        보이는 크기와 누르는 크기를 따로 두는 것이 실무에서 쓰는 방법이다.
    */
    #logo a {
        /* 세로로 이보다 작아지지 않게 44px */
        min-height: 44px;
    }
    /* id="logo" 인 요소 하나의 모양을 정한다 */
    #logo .logo-text {
        /* 글자 크기 1.1rem */
        font-size: 1.1rem;
    }
    /* 로그인 영역: 줄바꿈 허용, 전체 너비 */
    #login {
        /* 글자 크기 1.2rem */
        font-size: 1.2rem;
        /* 가로 크기 100% */
        width: 100%;
        /* 가로 방향 정렬 flex-start */
        justify-content: flex-start;
    }
    /* id="login" 인 요소 하나의 모양을 정한다 */
    #login .top-search-input {
        /* 가로 크기 110px */
        width: 110px;
    }
    /* 메뉴 글자 크기 조정 */
    .navbar-custom .nav-link {
        /* 글자 크기 1.4rem */
        font-size: 1.4rem;
        /* 안쪽 여백 8px 5px */
        padding: 8px 5px;
    }
}
/* 모바일 반응형 끝 */
</style>
<%
    /* Java 코드: 한글 인코딩 설정 */
    request.setCharacterEncoding("utf-8");

    /* Java 코드: 현재 웹 애플리케이션의 경로 얻기 (예: /CarProjectEdu) */
    String contextPath = request.getContextPath();
%>
<!-- ==========================================
     상단 헤더 영역: 로고 + 로그인/회원가입 버튼
     ========================================== -->
<header class="site-header clearfix">
    <%-- 메인 로고 영역: 이미지(RENT.jpg) → CSS 텍스트 로고로 대체 --%>
    <div id="logo">
        <%-- 다른 화면으로 넘어가는 링크 --%>
        <a href="<%=contextPath %>/Car/Main">
            <%-- CSS 아이콘 박스 (이미지 대체: 수정 방법→ .logo-icon 배경색 변경) --%>
            <div class="logo-icon">SM</div>
            <%-- 텍스트 로고 (수정 방법→ .logo-text 안의 글자 변경) --%>
            <div class="logo-text-wrap">
                <%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
                <span class="logo-text">SM렌탈</span>
                <%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
                <span class="logo-sub">Car Rental Service</span>
            </div>
        </a>
    </div>
    <%--
        Java 코드: 세션에서 로그인한 사용자 아이디 확인
        - 로그인 안 했으면 id = null
        - 로그인 했으면 id = 아이디 문자열
    --%>
    <%
        //Session내장객체 메모리 영역에 session값 얻기
        String id = (String)session.getAttribute("id");
        /*
         [카카오 로그인 지원] 화면에 보여줄 이름을 따로 정한다.
           카카오 로그인 회원의 아이디는 "kakao_4392817465" 처럼 기계적인 값이라
           그대로 보여주면 어색하다. 로그인 시 세션에 함께 저장해 둔
           닉네임(loginName)이 있으면 그것을, 없으면 아이디를 보여준다.
        */
        String loginName = (String)session.getAttribute("loginName");
        // displayName — 조건에 따라 둘 중 하나를 담는다
        String displayName = (loginName != null && !loginName.trim().isEmpty()) ? loginName : id;
        //Session에 값이 저장되어 있지 않으면? (비로그인 상태)
        if(id == null){
    %>
            <%-- 비로그인 상태: 로그인, 회원가입 버튼과 검색 바 표시 --%>
            <div id="login">
                <!-- 로그인 버튼 -->
                <button type="button" class="top-btn top-btn-primary"
                        onclick="location.href='<%=contextPath%>/member/login.me'">
                    <%-- 화면에 그대로 보이는 글자: "로그인" --%>
                    로그인
                </button>
                <!-- 회원가입 버튼 -->
                <button type="button" class="top-btn top-btn-primary"
                        onclick="location.href='<%=contextPath%>/member/join.me?center=members/join.jsp'">
                    <%-- 화면에 그대로 보이는 글자: "회원가입" --%>
                    회원가입
                </button>
                <%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
                <span class="top-divider"></span>

                <span class="top-divider"></span>
                <!-- 인라인 검색 폼 -->
                <form class="top-search-form" action="<%=contextPath%>/Car/NaverSearchAPI.do">
                    <%-- 입력칸 --%>
                    <input class="top-search-input" type="search"
                           id="keyword" name="keyword"
                           placeholder="차량 검색" aria-label="Search">
                    <%-- 화면에는 안 보이지만 서버로 함께 보낼 startNum 값 --%>
                    <input type="hidden" id="startNum" name="startNum" value="1">
                    <%-- 누르면 동작하는 버튼 --%>
                    <button class="top-search-btn" type="submit">&#128269;</button>
                </form>
            </div>
    <%
        // 위 조건들이 전부 아닐 때
        }else{
    %>
            <%-- 로그인 상태: 아이디 + 정보수정, 로그아웃 버튼 표시 --%>
            <div id="login">
                <!-- 로그인된 아이디 표시
                     [보안] 세션 값이라도 이스케이프한다.
                            아이디는 가입 화면에서 사용자가 입력한 값이므로
                            "믿을 수 있는 값"이 아니다. 모든 화면 상단에 출력되는
                            자리라서 여기가 뚫리면 사이트 전체가 뚫린다. -->
                <span class="login-user-id">&#128100; <%=util.HtmlUtil.escape(displayName)%></span>
                <%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
                <span class="top-divider"></span>            
                <!-- 정보수정 버튼 -->
                <button type="button" class="top-btn top-btn-primary"
                        onclick="location.href='<%=contextPath%>/member/memberUpdate.me'">
                    <%-- 화면에 그대로 보이는 글자: "정보수정" --%>
                    정보수정
                </button>
                <!-- 로그아웃 버튼 -->
                <button type="button" class="top-btn top-btn-outline"
                        onclick="location.href='<%=contextPath%>/member/logout.me'">
                    <%-- 화면에 그대로 보이는 글자: "로그아웃" --%>
                    로그아웃
                </button>

                <span class="top-divider"></span>
                <!-- 인라인 검색 폼 -->
                <form class="top-search-form" action="<%=contextPath%>/Car/NaverSearchAPI.do">
                    <%-- 입력칸 --%>
                    <input class="top-search-input" type="search"
                           id="keyword" name="keyword"
                           placeholder="차량 검색" aria-label="Search">
                    <%-- 화면에는 안 보이지만 서버로 함께 보낼 startNum 값 --%>
                    <input type="hidden" id="startNum" name="startNum" value="1">
                    <%-- 누르면 동작하는 버튼 --%>
                    <button class="top-search-btn" type="submit">&#128269;</button>
                </form>
            </div>
    <%
        }
    %>
</header>
<!-- ==========================================
     네비게이션 메뉴바
     - Bootstrap navbar 반응형 적용
     - 모바일에서는 햄버거 버튼으로 접힘
     - 수정 방법: nav-item 안의 텍스트와 링크(href) 변경
     ========================================== -->
<nav class="navbar navbar-expand-md navbar-custom">
    <!-- 모바일에서 보이는 햄버거 버튼 -->
    <button class="navbar-toggler" type="button"
            data-toggle="collapse"
            data-target="#mainNavMenu"
            aria-controls="mainNavMenu"
            aria-expanded="false"
            aria-label="메뉴 열기/닫기">
        <%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
        <span class="navbar-toggler-icon"></span>
    </button>
    <!-- 메뉴 항목들 (모바일에서는 접힘) -->
    <div class="collapse navbar-collapse" id="mainNavMenu">
        <%-- 점 목록 --%>
        <ul class="navbar-nav w-100">
            <!-- 예약하기 메뉴 -->
            <li class="nav-item flex-fill text-center">
            
                <%-- 예약하기 클릭하면 CarController 서블릿으로 예약을 할 수 있는 VIEW(CarReservation.jsp) 중앙화면 요청 --%>
                <a class="nav-link" href="<%=contextPath %>/Car/bb?center=CarReservation.jsp">
                    <%-- 화면에 그대로 보이는 글자: "예약하기" --%>
                    예약하기
                </a>
                <%--예약하기 --%>
            </li>
            <!-- 예약확인 메뉴 -->
            <li class="nav-item flex-fill text-center">           
                <a class="nav-link"
                   href="<%=contextPath %>/Car/cc?center=CarReserveConfirm.jsp">       
                    예약확인
                </a> 
            </li>
            <!-- 자유게시판 메뉴 -->
            <li class="nav-item flex-fill text-center">
                <%-- 다른 화면으로 넘어가는 링크 --%>
                <a class="nav-link"
                   href="<%=contextPath %>/Board/list.bo">
                    <%-- 화면에 그대로 보이는 글자: "자유게시판" --%>
                    자유게시판
                </a>
                <%--자유게시판 --%>
            </li>
            <!-- AI 추천 메뉴 -->
            <li class="nav-item flex-fill text-center">
                <%-- 다른 화면으로 넘어가는 링크 --%>
                <a class="nav-link"
                   href="<%=contextPath %>/Car/ai?center=AIService.jsp">
                    <%-- 화면에 그대로 보이는 글자: "AI 추천" --%>
                    AI 추천
                </a>
                <%--AI 추천 서비스 --%>
            </li>
            <!-- 공지사항 메뉴 -->
            <li class="nav-item flex-fill text-center">
                <%-- 다른 화면으로 넘어가는 링크 --%>
                <a class="nav-link"
                   href="<%=contextPath %>/FileBoard/list.bo">
                    <%-- 화면에 그대로 보이는 글자: "공지사항" --%>
                    공지사항
                </a>
                <%--공지사항게시판 --%>
            </li>
        </ul>
    </div>
</nav>
<%--
 ================================================================================
   [5단계 변경] 여기 있던 자바스크립트 <script> 6개를 모두 제거했다.
   (제거된 코드)
     jquery.slim.min.js          (CDN)
     bootstrap.bundle.min.js     (CDN)
     그리고 주석 처리된 Option 2 : jquery.slim + popper + bootstrap.min.js
   제거한 이유
     1) 외부 서버 의존 : 오프라인 환경에서 메뉴 동작이 멈췄다.
     2) jQuery 를 두 번 불러오고 있었다.
        여기서 jquery.slim(가벼운 버전)을 불러오는데,
        게시판 화면(board/read.jsp 등)은 다시 jquery 3.7.1 정식 버전을 불러왔다.
        같은 페이지에 jQuery 가 두 번 로드되면 나중에 로드된 것이 앞의 것을 덮어써
        먼저 등록한 이벤트가 사라지는 등 원인 찾기 어려운 오류가 생긴다.
        게다가 slim 버전에는 $.ajax 가 없다.
        그런데 이 프로젝트는 $.ajax 를 쓰고 있었으니, 게시판 JSP 가 정식 버전을
        추가로 불러오지 않으면 비동기 기능이 전부 동작하지 않는 상태였다.
     3) Bootstrap JS 로 하던 일은 "메뉴 접기/펼치기" 하나뿐이었다.
            <button data-toggle="collapse" data-target="#menu">
        이 동작은 대상 요소에 show 클래스를 붙이고 떼는 것이 전부라서
        js/app.js 의 initCollapse() 몇 줄로 대체했다.
        (Bootstrap CSS 의 .collapse / .show 규칙은 그대로 활용한다)
   공용 스크립트는 CarMain.jsp 의 <head> 에서 js/app.js 로 한 번만 불러온다.
 ================================================================================
--%>