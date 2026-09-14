<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%--
 ============================================================================
  Top.jsp  -  상단 헤더 + 메뉴바 화면 조각

  CarMain.jsp 가 <jsp:include page="Top.jsp"/> 로 맨 위에 끼워 넣는다.
  CSS(app.css)와 공용 스크립트(app.js)는 여기서 불러오지 않는다.
  문서의 주인인 CarMain.jsp 의 <head> 에서 딱 1번만 불러온다.
  (이 파일에 <link>, <script src> 를 넣으면 <body> 안에서 불러오게 되어
   스타일 적용 순서를 예측할 수 없고 화면이 한 번 깜빡인다)

  화면 구성
    header.site-header : 로고 + 로그인/회원가입 (또는 아이디/정보수정/로그아웃) + 검색창
    nav.navbar-custom   : 예약하기, 예약확인, 자유게시판, AI 추천, 공지사항 5개 메뉴
                          모바일에서는 햄버거 버튼(.navbar-toggler)을 누르면 펼쳐진다
                          (열고 닫는 동작은 js/app.js 의 CarApp.initCollapse 가 처리한다)
 ============================================================================
--%>

<style>
/* ==========================================================================
   Top.jsp 전용 스타일
   선택자(.이름) 는 아래 HTML 의 class="이름" 인 태그를 꾸민다
   ========================================================================== */

/* [전체 폰트 기준] html 태그의 기본 글자 크기를 62.5% 로 낮춘다
   보통 브라우저 기본 글자 크기는 16px 이므로 62.5% = 10px 이 된다
   그러면 1rem = 10px 이 되어, 1.6rem = 16px 처럼 rem 값을 암산하기 쉬워진다 */
html {
    font-size: 62.5%;
}

/* [헤더 전체] <header class="site-header"> : 로고와 로그인 영역을 담는 가로 줄 */
header.site-header {
    display: flex;                           /* 로고와 로그인 영역을 가로 한 줄로 놓는다 */
    align-items: center;                     /* 둘의 세로 위치를 가운데로 맞춘다 */
    justify-content: space-between;          /* 로고는 왼쪽 끝, 로그인 영역은 오른쪽 끝으로 벌려 놓는다 */
    flex-wrap: wrap;                         /* 화면이 좁으면 다음 줄로 넘긴다 */
    padding: 8px 20px;                       /* 안쪽 여백 : 위아래 8px, 좌우 20px */
    background: #fff;                        /* 배경색 : 흰색 */
    border-bottom: 1px solid #eee;           /* 아래쪽 테두리만 1px 아주 연한 회색 (메뉴바와 구분선 역할) */
    gap: 8px;                                /* 로고와 로그인 영역 사이 최소 간격 8px */
}

/* [로그인 영역] <div id="login"> : 버튼들과 검색창을 담는 가로 줄 */
#login {
    display: flex;                           /* 버튼, 검색창을 가로로 늘어놓는다 */
    align-items: center;                     /* 세로 위치를 가운데로 맞춘다 */
    flex-wrap: wrap;                         /* 공간이 부족하면 다음 줄로 넘긴다 */
    gap: 6px;                                /* 항목 사이 간격 6px */
    font-family: Arial, Helvetica, sans-serif; /* 글꼴 : Arial, 없으면 Helvetica, 그것도 없으면 시스템 고딕체 */
    font-size: 1.3rem;                       /* 글자 크기 : 1.3rem = 13px */
}

/* [로그인 아이디] <span class="login-user-id"> : 로그인 후 보이는 내 아이디 */
#login .login-user-id {
    font-weight: bold;                       /* 글자 굵기 : 굵게 */
    color: #cc0000;                          /* 글자색 : 빨간색 */
    font-size: 1.3rem;                       /* 글자 크기 13px */
    padding: 0 4px;                          /* 안쪽 여백 : 위아래 0, 좌우 4px */
}

/* [버튼 공통 모양] 로그인, 회원가입, 정보수정, 로그아웃 버튼이 모두 이 모양을 쓴다 (알약 모양) */
#login .top-btn {
    display: inline-flex;                    /* 글자를 가운데에 놓을 수 있게 flex 로 만들되, 옆 요소와 한 줄에 놓이게 한다 */
    align-items: center;                     /* 글자를 세로 가운데에 놓는다 */
    justify-content: center;                 /* 글자를 가로 가운데에 놓는다 */
    padding: 5px 14px;                       /* 안쪽 여백 : 위아래 5px, 좌우 14px */
    border-radius: 20px;                     /* 모서리 둥글기 20px : 높이의 절반 이상이라 완전한 알약 모양이 된다 */
    border: none;                            /* 테두리 없음 */
    font-size: 1.25rem;                      /* 글자 크기 12.5px */
    font-weight: 600;                        /* 글자 굵기 : 조금 굵게 */
    cursor: pointer;                         /* 마우스를 올리면 손가락 모양 */
    transition: all 0.2s ease;               /* 모양이 바뀔 때 0.2초 동안 부드럽게 바뀐다 (ease : 처음·끝은 느리게, 중간은 빠르게) */
    white-space: nowrap;                     /* 버튼 글자가 길어도 줄바꿈하지 않는다 */
    text-decoration: none;                   /* 링크처럼 쓰일 때 기본 밑줄을 없앤다 */
}

/* [터치 화면, 좁은 화면(767px 이하)일 때만 적용]
   손가락으로 누르기 쉽도록 버튼 크기를 최소 44x44px 로 키운다
   (애플·구글 디자인 가이드가 공통으로 권장하는 최소 터치 크기)
     hover: none        : 마우스를 올릴 수 없는 기기
     pointer: coarse    : 손가락처럼 끝이 뭉툭한 입력 도구
     max-width: 767px   : 화면 너비가 767px 이하 (위 두 조건을 못 가리는 브라우저 대비) */
@media (hover: none), (pointer: coarse), (max-width: 767px) {
    #login .top-btn {
        min-height: 44px;                    /* 최소 높이 44px */
        min-width: 44px;                     /* 최소 너비 44px */
        padding: 5px 16px;                   /* 안쪽 여백을 좌우 16px 로 조금 더 넓힌다 */
    }
}

/* [주요 버튼] <button class="top-btn top-btn-primary"> : 로그인, 회원가입, 정보수정 (빨간 배경) */
#login .top-btn-primary {
    background: #cc0000;                     /* 배경색 : 빨간색 */
    color: #fff;                             /* 글자색 : 흰색 */
}

/* [주요 버튼에 마우스를 올렸을 때] 더 어둡게 + 살짝 떠오르는 효과 */
#login .top-btn-primary:hover {
    background: #aa0000;                     /* 배경색 : 어두운 빨간색 */
    color: #fff;                             /* 글자색 유지 : 흰색 */
    transform: translateY(-1px);             /* 위로 1px 이동 (살짝 떠오르는 느낌) */
    box-shadow: 0 3px 8px rgba(204,0,0,0.3); /* 그림자 : 아래로 3px, 퍼짐 8px, 빨강 30% 투명도 */
}

/* [보조 버튼] <button class="top-btn top-btn-outline"> : 로그아웃 (테두리만 있는 버튼) */
#login .top-btn-outline {
    background: transparent;                 /* 배경색 : 투명 (헤더의 흰 배경이 그대로 보인다) */
    color: #555;                             /* 글자색 : 중간 회색 */
    border: 1.5px solid #bbb;                /* 테두리 : 1.5px 실선, 회색 */
}

/* [보조 버튼에 마우스를 올렸을 때] 연한 회색 배경 */
#login .top-btn-outline:hover {
    background: #f5f5f5;                     /* 배경색 : 아주 연한 회색 */
    color: #333;                             /* 글자색 : 진한 회색 */
    border-color: #888;                      /* 테두리 색 : 더 진한 회색 */
}

/* [구분선] <span class="top-divider"> : 버튼 사이의 짧은 세로 선 */
#login .top-divider {
    width: 1px;                              /* 너비 1px (얇은 세로선) */
    height: 18px;                            /* 높이 18px */
    background: #ddd;                        /* 배경색 : 연한 회색 (선처럼 보인다) */
    margin: 0 2px;                           /* 좌우 여백 2px 씩 */
}

/* [검색 폼] <form class="top-search-form"> : 입력칸과 버튼을 붙여서 하나처럼 보이게 한다 */
#login .top-search-form {
    display: flex;                           /* 입력칸과 버튼을 가로로 붙인다 */
    align-items: center;                     /* 세로 위치를 가운데로 맞춘다 */
    gap: 0;                                  /* 간격 0 : 입력칸과 버튼이 완전히 붙는다 */
}

/* [검색 입력칸] 왼쪽은 둥글고 오른쪽은 각지게 만들어 버튼과 이어지게 한다 */
#login .top-search-input {
    padding: 5px 12px;                       /* 안쪽 여백 : 위아래 5px, 좌우 12px */
    border: 1.5px solid #ccc;                /* 테두리 : 1.5px 실선, 회색 */
    border-right: none;                      /* 오른쪽 테두리만 없앤다 (버튼과 만나는 쪽) */
    border-radius: 20px 0 0 20px;            /* 모서리 둥글기 : 왼쪽위 20px, 오른쪽위 0, 오른쪽아래 0, 왼쪽아래 20px */
    font-size: 1.25rem;                      /* 글자 크기 12.5px */
    outline: none;                           /* 클릭 시 브라우저 기본 윤곽선을 없앤다 (아래 :focus 로 대신 표시) */
    width: 140px;                            /* 너비 140px */
    transition: border-color 0.2s;           /* 테두리 색이 바뀔 때 0.2초 동안 부드럽게 */
}

/* [검색 입력칸 선택 중] 테두리를 빨갛게 */
#login .top-search-input:focus {
    border-color: #cc0000;                   /* 테두리 색 : 빨간색 */
}

/* [검색 버튼] 돋보기 아이콘 버튼, 입력칸 오른쪽에 붙는다 */
#login .top-search-btn {
    padding: 5px 14px;                       /* 안쪽 여백 : 위아래 5px, 좌우 14px */
    background: #cc0000;                     /* 배경색 : 빨간색 */
    color: #fff;                             /* 글자색 : 흰색 */
    border: none;                            /* 테두리 없음 */
    border-radius: 0 20px 20px 0;            /* 모서리 둥글기 : 왼쪽은 각지고 오른쪽만 둥글게 (입력칸과 반대로) */
    font-size: 1.25rem;                      /* 글자 크기 12.5px */
    cursor: pointer;                         /* 마우스를 올리면 손가락 모양 */
    transition: background 0.2s;             /* 배경색이 바뀔 때 0.2초 동안 부드럽게 */
}

/* [검색 버튼에 마우스를 올렸을 때] 더 어둡게 */
#login .top-search-btn:hover {
    background: #aa0000;                     /* 배경색 : 어두운 빨간색 */
}

/* [터치 화면, 좁은 화면(767px 이하)] 검색 버튼과 입력칸도 44px 를 보장한다 (위 top-btn 과 같은 이유) */
@media (hover: none), (pointer: coarse), (max-width: 767px) {
    #login .top-search-btn {
        min-height: 44px;                    /* 최소 높이 44px */
        min-width: 44px;                     /* 최소 너비 44px */
    }
    #login .top-search-input {
        min-height: 44px;                    /* 입력칸도 버튼과 높이를 맞춘다 */
    }
}

/* [로그인 영역의 링크] 검색창 외에 <a> 태그가 쓰일 경우를 대비한 공통 스타일 */
#login a {
    text-decoration: none;                   /* 링크의 기본 밑줄을 없앤다 */
    color: #333;                             /* 글자색 : 진한 회색 */
}
#login a:hover {
    color: #cc0000;                          /* 마우스를 올리면 빨간색으로 */
}

/* [로고 영역] <div id="logo"> : 이미지 대신 CSS 로 만든 아이콘 박스 + 글자 로고 */
#logo {
    margin: 0;                               /* 바깥 여백 없음 */
    flex-shrink: 0;                          /* 화면이 좁아져도 로고가 찌그러들지 않는다 */
}

/* [로고 링크] 아이콘 박스와 글자를 가로로 붙인다 */
#logo a {
    text-decoration: none;                   /* 링크의 기본 밑줄을 없앤다 */
    display: flex;                           /* 아이콘과 글자를 가로 한 줄로 놓는다 */
    align-items: center;                     /* 세로 위치를 가운데로 맞춘다 */
    gap: 10px;                               /* 아이콘과 글자 사이 간격 10px */
}

/* [로고 아이콘 박스] <div class="logo-icon">SM</div> : 이미지(RENT.jpg) 대신 만든 빨간 정사각형 */
#logo .logo-icon {
    width: 44px; height: 44px;               /* 크기 : 가로 44px, 세로 44px */
    background-color: #cc0000;               /* 배경색 : 빨간색 */
    border-radius: 8px;                      /* 모서리 둥글기 8px */
    display: flex; align-items: center; justify-content: center; /* 안의 글자(SM)를 박스 정가운데에 놓는다 */
    color: white;                            /* 글자색 : 흰색 */
    font-weight: 900;                        /* 글자 굵기 : 가장 굵게 */
    font-size: 1.1rem;                       /* 글자 크기 11px */
    letter-spacing: -1px;                    /* 글자 사이 간격을 1px 좁힌다 */
    box-shadow: 2px 2px 6px rgba(0,0,0,0.2); /* 그림자 : 오른쪽 아래로 2px씩, 퍼짐 6px, 검정 20% 투명도 */
    flex-shrink: 0;                          /* 화면이 좁아져도 아이콘이 찌그러들지 않는다 */
}

/* [로고 글자 영역] 회사명과 부제목을 위아래로 쌓는다 */
#logo .logo-text-wrap {
    display: flex;                           /* 회사명과 부제목을 세로로 놓기 위해 flex 로 만든다 */
    flex-direction: column;                  /* 배치 방향 : 세로(위에서 아래) */
}

/* [회사명] <span class="logo-text">SM렌탈</span> */
#logo .logo-text {
    font-size: 1.5rem;                       /* 글자 크기 15px */
    font-weight: 900;                        /* 글자 굵기 : 가장 굵게 */
    color: #cc0000;                          /* 글자색 : 빨간색 */
    letter-spacing: 2px;                     /* 글자 사이 간격 2px 넓힌다 */
    line-height: 1.1;                        /* 줄 높이 : 글자 크기의 1.1배 (부제목과 가깝게 붙인다) */
}

/* [영문 부제목] <span class="logo-sub">Car Rental Service</span> */
#logo .logo-sub {
    font-size: 0.6rem;                       /* 글자 크기 6px (작게) */
    color: #999;                             /* 글자색 : 회색 */
    letter-spacing: 1px;                     /* 글자 사이 간격 1px */
    text-transform: uppercase;               /* 모든 글자를 대문자로 바꿔서 보여준다 */
}

/* [메뉴바 전체] <nav class="navbar-custom"> : 빨간 배경의 가로 메뉴 줄 */
.navbar-custom {
    background-color: #cc0000;               /* 배경색 : 빨간색 */
    padding: 0;                              /* 안쪽 여백 없음 (메뉴 항목 각각이 자기 여백을 갖는다) */
    width: 100%;                             /* 너비 : 화면 전체를 가로로 채운다 */
    clear: both;                             /* 이전 요소의 float 영향을 받지 않고 새 줄에서 시작한다 */
}

/* [메뉴 글자] <a class="nav-link"> : 예약하기, 예약확인 등 메뉴 1개 */
.navbar-custom .nav-link {
    color: #ffffff !important;               /* 글자색 : 흰색 (!important : 다른 공용 스타일보다 우선 적용) */
    font-size: 1.6rem;                       /* 글자 크기 16px */
    font-weight: bold;                       /* 글자 굵기 : 굵게 */
    text-align: center;                      /* 글자를 가운데 정렬 */
    padding: 12px 10px;                      /* 안쪽 여백 : 위아래 12px, 좌우 10px (이 여백까지 클릭 영역이 된다) */
    display: block;                          /* 한 줄 전체를 차지하게 해서 여백까지 전부 클릭되게 한다 */
}

/* [메뉴에 마우스를 올렸을 때] 반투명 흰 배경 + 노란 글자 */
.navbar-custom .nav-link:hover {
    background-color: rgba(255, 255, 255, 0.2); /* 배경색 : 흰색 20% 투명도 */
    color: #ffff00 !important;               /* 글자색 : 노란색 */
}

/* [햄버거 버튼 테두리] 모바일에서만 보이는 메뉴 버튼의 테두리 색 */
.navbar-custom .navbar-toggler {
    border-color: rgba(255, 255, 255, 0.5);  /* 테두리 색 : 흰색 50% 투명도 */
    margin: 5px;                             /* 바깥 여백 5px */
}

/* [햄버거 아이콘] 이미지 파일 없이, 3줄짜리 선 모양을 SVG 글자로 직접 그려 배경으로 넣는다 */
.navbar-custom .navbar-toggler-icon {
    background-image: url("data:image/svg+xml;charset=utf8,%3Csvg viewBox='0 0 30 30' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath stroke='rgba(255,255,255,1)' stroke-width='2' stroke-linecap='round' stroke-miterlimit='10' d='M4 7h22M4 15h22M4 23h22'/%3E%3C/svg%3E");
}

/* [좁은 화면(768px 이하)일 때만 적용] 헤더를 세로로 쌓고, 로고·메뉴 크기를 줄인다 */
@media (max-width: 768px) {

    /* 헤더 : 로고와 로그인 영역을 가로 대신 세로로 쌓는다 */
    header.site-header {
        padding: 8px 10px;                   /* 안쪽 여백을 좌우 10px 로 줄인다 */
        flex-direction: column;              /* 배치 방향을 세로로 바꾼다 */
        align-items: flex-start;             /* 세로로 쌓인 것들을 왼쪽으로 정렬한다 */
    }

    #logo {
        margin: 0;                           /* 바깥 여백 없음 (좁은 화면에서 여백 낭비를 줄인다) */
    }

    /* 로고 아이콘을 조금 작게 */
    #logo .logo-icon {
        width: 36px; height: 36px;           /* 크기를 44px 에서 36px 로 줄인다 */
        font-size: 0.9rem;                   /* 글자 크기도 9px 로 줄인다 */
    }

    /* [터치 타깃] 아이콘은 36px 로 줄이되, 실제로 누르는 링크 영역은 44px 를 유지한다
       "보이는 크기"와 "누르는 크기"를 다르게 주는 방법이다 */
    #logo a {
        min-height: 44px;                    /* 최소 높이 44px (아이콘보다 크게 잡아 터치 영역을 넓힌다) */
    }

    #logo .logo-text {
        font-size: 1.1rem;                   /* 회사명 글자 크기를 15px 에서 11px 로 줄인다 */
    }

    /* 로그인 영역 : 줄바꿈을 허용하고 화면 너비를 꽉 채운다 */
    #login {
        font-size: 1.2rem;                   /* 글자 크기를 13px 에서 12px 로 줄인다 */
        width: 100%;                         /* 너비 100% */
        justify-content: flex-start;         /* 항목들을 왼쪽부터 채운다 */
    }

    #login .top-search-input {
        width: 110px;                        /* 검색 입력칸 너비를 140px 에서 110px 로 줄인다 */
    }

    /* 메뉴 글자와 여백을 줄인다 */
    .navbar-custom .nav-link {
        font-size: 1.4rem;                   /* 글자 크기를 16px 에서 14px 로 줄인다 */
        padding: 8px 5px;                    /* 안쪽 여백을 줄인다 */
    }
}
</style>

<%
    // 고객이 검색창 등에 입력한 한글이 깨지지 않도록 인코딩을 UTF-8 로 정한다
    request.setCharacterEncoding("utf-8");

    // 프로젝트 경로 얻기  예) "/CarProject"  -> 아래 모든 링크 주소를 만들 때 사용
    String contextPath = request.getContextPath();
%>

<%-- ===== 헤더 : 로고 + 로그인 영역 ===== --%>
<header class="site-header clearfix">

    <%-- 로고 : 이미지 대신 CSS 로 만든 빨간 박스(SM) + 회사명 --%>
    <div id="logo">
        <a href="<%=contextPath %>/Car/Main">
            <div class="logo-icon">SM</div>
            <div class="logo-text-wrap">
                <span class="logo-text">SM렌탈</span>
                <span class="logo-sub">Car Rental Service</span>
            </div>
        </a>
    </div>

    <%
        // 세션에서 로그인한 아이디를 꺼낸다. 로그인 안 했으면 null
        String id = (String)session.getAttribute("id");

        // 카카오 로그인 회원은 아이디가 "kakao_4392817465" 처럼 기계적인 값이라
        // 화면에는 로그인할 때 세션에 함께 저장해 둔 닉네임(loginName)을 대신 보여준다 (없으면 아이디 그대로)
        String loginName = (String)session.getAttribute("loginName");
        String displayName = (loginName != null && !loginName.trim().isEmpty()) ? loginName : id;

        // 로그인 안 했으면 로그인/회원가입 버튼을, 했으면 아이디/정보수정/로그아웃 버튼을 보여준다
        if(id == null){
    %>
            <%-- 비로그인 상태 --%>
            <div id="login">

                <button type="button" class="top-btn top-btn-primary"
                        onclick="location.href='<%=contextPath%>/member/login.me'">
                    로그인
                </button>

                <button type="button" class="top-btn top-btn-primary"
                        onclick="location.href='<%=contextPath%>/member/join.me?center=members/join.jsp'">
                    회원가입
                </button>

                <span class="top-divider"></span>

                <%-- 차량 검색 : 사장(CarController)의 /Car/NaverSearchAPI.do 가 네이버 검색 API 로 결과를 조회한다 --%>
                <form class="top-search-form" action="<%=contextPath%>/Car/NaverSearchAPI.do">
                    <input class="top-search-input" type="search"
                           id="keyword" name="keyword"
                           placeholder="차량 검색" aria-label="Search">
                    <input type="hidden" id="startNum" name="startNum" value="1">
                    <button class="top-search-btn" type="submit">&#128269;</button>
                </form>

            </div>
    <%
        }else{
    %>
            <%-- 로그인 상태 --%>
            <div id="login">

                <%-- 아이디는 회원가입 때 고객이 입력한 값이므로 그대로 믿지 않고 HtmlUtil.escape 로 안전하게 바꿔 출력한다
                     (화면 맨 위, 모든 페이지에 보이는 자리라서 여기를 막아 두는 것이 특히 중요하다) --%>
                <span class="login-user-id">&#128100; <%=util.HtmlUtil.escape(displayName)%></span>

                <span class="top-divider"></span>

                <button type="button" class="top-btn top-btn-primary"
                        onclick="location.href='<%=contextPath%>/member/memberUpdate.me'">
                    정보수정
                </button>

                <button type="button" class="top-btn top-btn-outline"
                        onclick="location.href='<%=contextPath%>/member/logout.me'">
                    로그아웃
                </button>

                <span class="top-divider"></span>

                <form class="top-search-form" action="<%=contextPath%>/Car/NaverSearchAPI.do">
                    <input class="top-search-input" type="search"
                           id="keyword" name="keyword"
                           placeholder="차량 검색" aria-label="Search">
                    <input type="hidden" id="startNum" name="startNum" value="1">
                    <button class="top-search-btn" type="submit">&#128269;</button>
                </form>

            </div>
    <%
        }
    %>

</header>


<%-- ===== 메뉴바 : 모바일에서는 햄버거 버튼을 누르면 펼쳐진다 (js/app.js 의 CarApp.initCollapse 가 처리) ===== --%>
<nav class="navbar navbar-expand-md navbar-custom">

    <button class="navbar-toggler" type="button"
            data-toggle="collapse"
            data-target="#mainNavMenu"
            aria-controls="mainNavMenu"
            aria-expanded="false"
            aria-label="메뉴 열기/닫기">
        <span class="navbar-toggler-icon"></span>
    </button>

    <div class="collapse navbar-collapse" id="mainNavMenu">
        <ul class="navbar-nav w-100">

            <li class="nav-item flex-fill text-center">
                <a class="nav-link" href="<%=contextPath %>/Car/bb?center=CarReservation.jsp">예약하기</a>
            </li>

            <li class="nav-item flex-fill text-center">
                <a class="nav-link" href="<%=contextPath %>/Car/cc?center=CarReserveConfirm.jsp">예약확인</a>
            </li>

            <li class="nav-item flex-fill text-center">
                <a class="nav-link" href="<%=contextPath %>/Board/list.bo">자유게시판</a>
            </li>

            <li class="nav-item flex-fill text-center">
                <a class="nav-link" href="<%=contextPath %>/Car/ai?center=AIService.jsp">AI 추천</a>
            </li>

            <li class="nav-item flex-fill text-center">
                <a class="nav-link" href="<%=contextPath %>/FileBoard/list.bo">공지사항</a>
            </li>

        </ul>
    </div>

</nav>
