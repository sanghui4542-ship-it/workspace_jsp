<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%--
 ============================================================================
  Bottom.jsp  -  푸터(하단) 화면 조각

  CarMain.jsp 가 <jsp:include page="Bottom.jsp"/> 로 맨 아래에 끼워 넣는다.
  로고, 링크, 회사 정보 모두 이미지 없이 CSS 와 글자로만 만들었다.
 ============================================================================
--%>

<style>
/* ==========================================================================
   Bottom.jsp 전용 스타일
   선택자(.이름) 는 아래 HTML 의 class="이름" 인 태그를 꾸민다
   ========================================================================== */

/* [푸터 전체] <div class="footer-wrapper"> : 화면 맨 아래 남색 띠 */
.footer-wrapper {
    width: 100%;                             /* 너비 : 화면 전체를 가로로 채운다 */
    border-top: 3px solid #cc0000;           /* 위쪽 테두리만 3px 두께의 빨간 실선 (구분선 역할) */
    padding: 20px 10px;                      /* 안쪽 여백 : 위아래 20px, 좌우 10px */
    background-color: #1a1a2e;               /* 배경색 : 짙은 남색 */
    box-sizing: border-box;                  /* 너비 100% 안에 여백과 테두리까지 포함한다 */
}

/* [푸터 안쪽] <div class="footer-inner"> : 로고, 링크, 회사정보 3개를 담는 가로 줄 */
.footer-inner {
    max-width: 1000px;                       /* 최대 너비 1000px : 화면이 넓어져도 이보다 넓어지지 않는다 */
    margin: 0 auto;                          /* 위아래 여백 0, 좌우 auto : 좌우 여백을 똑같이 나눠 가운데 정렬 */
    display: flex;                           /* 로고, 링크, 회사정보를 가로 한 줄로 놓는다 */
    flex-wrap: wrap;                         /* 화면이 좁아지면 다음 줄로 넘긴다 */
    align-items: flex-start;                 /* 세 영역의 윗선을 맞춘다 */
    gap: 20px;                               /* 세 영역 사이 간격 20px */
}

/* [로고 영역] <div class="footer-logo"> : 크기를 내용만큼만 차지하고 늘어나지 않는다 */
.footer-logo {
    flex: 0 0 auto;                          /* 늘어나지 않음(0), 줄어들지 않음(0), 크기는 내용 크기(auto) */
}

/* [로고 링크] <a class="footer-logo-link"> : 아이콘 박스와 글자를 가로로 붙인다 */
.footer-logo-link {
    text-decoration: none;                   /* 링크(a 태그)의 기본 밑줄을 없앤다 */
    display: flex;                           /* 아이콘 박스와 글자를 가로 한 줄로 놓는다 */
    align-items: center;                     /* 아이콘 박스와 글자의 세로 위치를 가운데로 맞춘다 */
    gap: 10px;                               /* 아이콘 박스와 글자 사이 간격 10px */
}

/* [로고 아이콘 박스] <div class="footer-logo-box">SM</div> : 이미지 대신 글자 2개를 넣은 빨간 정사각형 */
.footer-logo-box {
    width: 50px; height: 50px;               /* 크기 : 가로 50px, 세로 50px */
    background-color: #cc0000;               /* 배경색 : 빨간색 */
    border-radius: 8px;                      /* 모서리 둥글기 8px */
    display: flex; align-items: center; justify-content: center; /* 안의 글자(SM)를 박스 정가운데에 놓는다 */
    color: white;                            /* 글자색 : 흰색 */
    font-weight: 900;                        /* 글자 굵기 : 가장 굵게 (100~900 중 최대값) */
    font-size: 1rem;                         /* 글자 크기 : 기본 크기(16px 정도) */
    letter-spacing: -1px;                    /* 글자 사이 간격 : 1px 좁힌다 (두 글자를 더 붙여 보이게) */
}

/* [로고 이름] <span class="footer-logo-name">SM렌탈</span> */
.footer-logo-name {
    font-size: 1.2rem;                       /* 글자 크기 : 기본 글자의 1.2배 */
    font-weight: 900;                        /* 글자 굵기 : 가장 굵게 */
    color: #ffffff;                          /* 글자색 : 흰색 */
    letter-spacing: 2px;                     /* 글자 사이 간격 : 2px 넓힌다 */
}

/* [링크 영역] <div class="footer-links"> : 회사소개, 개인정보처리방침 등의 링크 묶음 */
.footer-links {
    flex: 1;                                 /* 로고가 차지하고 남은 가로 공간을 차지한다 */
    min-width: 200px;                        /* 최소 너비 200px : 화면이 좁아져도 이보다 좁아지지 않는다 (줄바꿈 유도) */
    display: flex;                           /* 링크들을 가로로 늘어놓는다 */
    flex-wrap: wrap;                         /* 공간이 부족하면 다음 줄로 넘긴다 */
    align-items: center;                     /* 링크들의 세로 위치를 가운데로 맞춘다 */
    gap: 8px;                                /* 링크 사이 간격 8px */
}

/* [링크 글자] <a> 태그 하나하나 (회사소개, 개인정보취급방침) */
.footer-links a {
    color: #cccccc;                          /* 글자색 : 연한 회색 */
    text-decoration: none;                   /* 링크의 기본 밑줄을 없앤다 */
    font-size: 13px;                         /* 글자 크기 13px */
    padding: 4px 8px;                        /* 안쪽 여백 : 위아래 4px, 좌우 8px */
    border: 1px solid rgba(255,255,255,0.3); /* 테두리 : 1px 실선, 흰색 30% 투명도 (남색 배경 위에 옅게 보인다) */
    border-radius: 4px;                      /* 모서리 둥글기 4px */
    transition: all 0.2s;                    /* 모양이 바뀔 때 0.2초 동안 부드럽게 바뀐다 */
}

/* [링크에 마우스를 올렸을 때] 더 밝게 */
.footer-links a:hover {
    color: #ffffff;                          /* 글자색 : 흰색 */
    background-color: rgba(255,255,255,0.1); /* 배경색 : 흰색 10% 투명도 */
    border-color: rgba(255,255,255,0.5);     /* 테두리 색 : 흰색 50% 투명도 (더 진하게) */
}

/* [터치 화면, 좁은 화면(767px 이하)일 때만 적용]
   손가락으로 누르기 쉽도록 링크의 높이를 최소 44px 로 키운다
   푸터 링크는 서로 가까이 붙어 있어 특히 필요하다 */
@media (hover: none), (pointer: coarse), (max-width: 767px) {
    .footer-links a {
        display: inline-flex;                /* 글자만큼의 크기를 유지하면서 세로 가운데 정렬을 쓸 수 있게 한다 */
        align-items: center;                 /* 글자를 세로 가운데에 놓는다 */
        min-height: 44px;                    /* 최소 높이 44px */
        padding: 4px 12px;                   /* 안쪽 여백 : 위아래 4px, 좌우 12px (좌우를 더 넓힌다) */
    }
}

/* [부가 안내 글자] <span class="footer-extra"> : "사이버 신문고 | 이용약관 | 인재채용" */
.footer-links .footer-extra {
    font-size: 12px;                         /* 글자 크기 12px */
    color: #aaaaaa;                          /* 글자색 : 연한 회색 */
}

/* [회사 정보 영역] <div class="footer-info"> : 사업자번호, 주소, 전화번호 */
.footer-info {
    flex: 2;                                 /* 로고(0), 링크(1)보다 2배 넓은 비율로 남은 공간을 차지한다 */
    min-width: 250px;                        /* 최소 너비 250px */
    font-size: 12px;                         /* 글자 크기 12px */
    color: #aaaaaa;                          /* 글자색 : 연한 회색 */
    line-height: 1.8em;                      /* 줄 간격 : 글자 크기의 1.8배 (여러 줄일 때 답답하지 않게) */
}

/* [좁은 화면(768px 이하)일 때만 적용] 가로 배치를 세로 배치로 바꾼다 */
@media (max-width: 768px) {

    /* 로고, 링크, 회사정보를 위아래로 쌓고 가운데 정렬한다 */
    .footer-inner {
        flex-direction: column;              /* 배치 방향을 가로(row)에서 세로(column)로 바꾼다 */
        align-items: center;                 /* 세로로 쌓인 항목들을 가로 가운데에 놓는다 */
        text-align: center;                  /* 안의 글자도 가운데 정렬한다 */
    }

    /* 로고를 가운데로 (좌우 여백을 auto 로 주면 가운데 정렬된다) */
    .footer-logo {
        margin: 0 auto;
    }

    /* 링크들을 가운데 정렬 */
    .footer-links {
        justify-content: center;
    }

    /* 회사 정보 글자를 가운데 정렬 */
    .footer-info {
        text-align: center;
    }
}
</style>

<%-- 사장(Controller)이 넘긴 한글 파라미터가 이 화면에서도 깨지지 않도록 인코딩을 맞춰 둔다 --%>
<%
    request.setCharacterEncoding("UTF-8");
%>

<div class="footer-wrapper">
    <div class="footer-inner">

        <%-- 로고 : 이미지 대신 CSS 로 만든 빨간 박스(SM) + 글자(SM렌탈) --%>
        <div class="footer-logo">
            <a href="#" class="footer-logo-link">
                <div class="footer-logo-box">SM</div>
                <span class="footer-logo-name">SM렌탈</span>
            </a>
        </div>

        <%-- 링크 : 실제 페이지가 없어 href="#" 로 자리만 잡아 두었다 --%>
        <div class="footer-links">
            <a href="#">회사소개</a>
            <a href="#">개인정보취급방침</a>
            <span class="footer-extra">
                &nbsp;| 사이버 신문고 &nbsp;| 이용약관 &nbsp;| 인재채용
            </span>
        </div>

        <%-- 회사 정보 : 사업자등록번호, 통신판매업신고번호, 주소, 전화번호 --%>
        <div class="footer-info">
            (주) SM렌탈 &nbsp; 사업자 등록번호 214-98754-9874 &nbsp;
            통신 판매업신고 번호 : 제 2010-충남-05호
            <br>
            서울시 강남구 역삼동 역삼빌딩 2층 21호
            <br><br>
            대표전화 : 02-3456-6574
            <br>
            FAX : 01-3254-9874
        </div>

    </div>
</div>
