<%-- 이 페이지의 글자 인코딩을 UTF-8 로 정한다. 빠뜨리면 한글이 깨진다 --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
/* 이 화면에서만 쓰는 모양(CSS) 시작 */
<style>
/* =============================================
   [푸터 전체 스타일] 반응형 설정
   - 수정 방법: background-color 값으로 배경색 변경
   ============================================= */
.footer-wrapper {
    /* 가로 크기 100% */
    width: 100%;
    /* 위 테두리 3px solid #cc0000 */
    border-top: 3px solid #cc0000; /* 빨간 가로 라인 (기존 <hr color="red"> 대체) */
    padding: 20px 10px;
    /* 배경 색 #1a1a2e */
    background-color: #1a1a2e;  /* 짙은 남색 배경 (이미지 under_logo.gif 대체) */
    box-sizing: border-box;
}
/* 푸터 내부 - 최대 너비 제한 & 가운데 정렬 */
.footer-inner {
    /* 가로로 이보다 커지지 않게 1000px */
    max-width: 1000px;
    /* 바깥 여백 0 auto */
    margin: 0 auto;
    /* 요소를 어떤 방식으로 배치할지 flex */
    display: flex;           /* 가로 배치 (PC) */
    flex-wrap: wrap;         /* 화면이 작아지면 줄바꿈 */
    align-items: flex-start;
    /* 요소 사이 간격 20px */
    gap: 20px;               /* 요소들 사이 간격 */
}
/* =============================================
   [푸터 로고 영역] - 이미지(bo.jpg) 제거 → CSS 텍스트 로고로 대체
   - 수정 방법: .footer-logo-box 배경색 변경, .footer-logo-name 텍스트 변경
   ============================================= */
.footer-logo {
    /* 늘어나는 비율 0 0 auto */
    flex: 0 0 auto;         /* 크기 고정 */
}
/* CSS 로고 링크 스타일 */
.footer-logo-link {
    /* 밑줄 같은 글자 장식 none */
    text-decoration: none;
    /* 요소를 어떤 방식으로 배치할지 flex */
    display: flex;
    /* 세로 방향 정렬 center */
    align-items: center;
    /* 요소 사이 간격 10px */
    gap: 10px;
}
/* CSS 로고 아이콘 박스 (이미지 bo.jpg 대체) */
/* 수정 방법: background-color 값으로 박스 배경색 변경 */
.footer-logo-box {
    /* 가로 크기 50px */
    width: 50px;
    /* 세로 크기 50px */
    height: 50px;
    /* 배경 색 #cc0000 */
    background-color: #cc0000;  /* 빨간 배경 */
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
    /* 글자 크기 1rem */
    font-size: 1rem;
    /* 글자 사이 간격 -1px */
    letter-spacing: -1px;
}
/* 로고 텍스트 */
/* 수정 방법: 이 텍스트를 변경하면 로고 이름 바뀜 */
.footer-logo-name {
    /* 글자 크기 1.2rem */
    font-size: 1.2rem;
    /* 글자 굵기 900 */
    font-weight: 900;
    /* 글자 색 #ffffff */
    color: #ffffff;       /* 흰색 */
    letter-spacing: 2px;
}
/* =============================================
   [푸터 링크 메뉴 영역] - 이미지(sodog.jpg, info.jpg) → 텍스트 링크로 대체
   - 수정 방법: <a> 태그 텍스트와 href 변경으로 링크 수정
   ============================================= */
.footer-links {
    /* 늘어나는 비율 1 */
    flex: 1;                /* 남은 공간 차지 */
    min-width: 200px;
    /* 요소를 어떤 방식으로 배치할지 flex */
    display: flex;
    /* 줄바꿈 허용 wrap */
    flex-wrap: wrap;
    /* 세로 방향 정렬 center */
    align-items: center;
    /* 요소 사이 간격 8px */
    gap: 8px;
}
/* 링크 스타일 (이미지 → 텍스트 링크로 대체) */
.footer-links a {
    /* 글자 색 #cccccc */
    color: #cccccc;         /* 연한 회색 글자 */
    text-decoration: none;
    /* 글자 크기 13px */
    font-size: 13px;
    /* 안쪽 여백 4px 8px */
    padding: 4px 8px;
    /* 테두리 1px solid rgba(255,255,255,0.3) */
    border: 1px solid rgba(255,255,255,0.3); /* 반투명 테두리 */
    border-radius: 4px;
    /* 값이 바뀔 때 부드럽게 all 0.2s */
    transition: all 0.2s;
}
/* 마우스 올릴때 스타일 */
.footer-links a:hover {
    /* 글자 색 #ffffff */
    color: #ffffff;
    /* 배경 색 rgba(255,255,255,0.1) */
    background-color: rgba(255,255,255,0.1); /* 반투명 흰색 배경 */
    border-color: rgba(255,255,255,0.5);
}
/*
  [터치 타깃 규격]
    padding: 4px 8px 만으로는 높이가 31px 이라 손가락으로 누르기 어렵다.
    푸터 링크는 서로 붙어 있어서 잘못 누르기 더 쉽다.
    손가락으로 쓰는 기기와 좁은 화면에서만 44px 를 보장한다 (데스크톱 푸터는 그대로).
*/
@media (hover: none), (pointer: coarse), (max-width: 767px) {
    .footer-links a {
        /* 요소를 어떤 방식으로 배치할지 inline-flex */
        display: inline-flex;
        /* 세로 방향 정렬 center */
        align-items: center;
        /* 세로로 이보다 작아지지 않게 44px */
        min-height: 44px;
        /* 안쪽 여백 4px 12px */
        padding: 4px 12px;
    }
}
/* 구분 텍스트 (사이버 신문고, 이용약관, 인재채용) */
.footer-links .footer-extra {
    /* 글자 크기 12px */
    font-size: 12px;
    /* 글자 색 #aaaaaa */
    color: #aaaaaa;
}
/* =============================================
   [회사 정보 텍스트 영역]
   - 수정 방법: 이 영역 안의 텍스트를 직접 변경하면 됩니다
   ============================================= */
.footer-info {
    /* 늘어나는 비율 2 */
    flex: 2;                /* 더 많은 공간 차지 */
    min-width: 250px;
    /* 글자 크기 12px */
    font-size: 12px;        /* 작은 글자 */
    color: #aaaaaa;         /* 연한 회색 글자 */
    line-height: 1.8em;
}
/* =============================================
   [반응형] 모바일 (768px 이하)
   ============================================= */
@media (max-width: 768px) {
    /* 세로 방향으로 변경 */
    .footer-inner {
        /* 가로/세로 배치 방향 column */
        flex-direction: column;
        /* 세로 방향 정렬 center */
        align-items: center;
        /* 글자 정렬 center */
        text-align: center;
    }
    /* class="footer-logo" 이 붙은 요소의 모양을 정한다 */
    .footer-logo {
        /* 바깥 여백 0 auto */
        margin: 0 auto;
    }
    /* class="footer-links" 이 붙은 요소의 모양을 정한다 */
    .footer-links {
        /* 가로 방향 정렬 center */
        justify-content: center;
    }
    /* class="footer-info" 이 붙은 요소의 모양을 정한다 */
    .footer-info {
        /* 글자 정렬 center */
        text-align: center;
    }
}
/* 모바일 반응형 끝 */
</style>
    <%
        /* Java 코드: 한글 인코딩 설정 */
        request.setCharacterEncoding("UTF-8");
        /* Java 코드: 현재 웹 애플리케이션의 경로 얻기 */
        String contextPath = request.getContextPath();
    %>
    <!-- ==========================================
         푸터 영역 (하단 정보)
         - 빨간 라인, CSS 로고, 링크, 회사 정보
         - 수정 방법: footer-info 안의 텍스트 변경으로 회사 정보 수정
         ========================================== -->
    <div class="footer-wrapper">
        <%-- footer-inner 모양을 입힐 영역. 실제 모양은 CSS 에서 정한다 --%>
        <div class="footer-inner">
            <!-- ======================================
                 하단 CSS 로고 (이미지 bo.jpg 제거 → CSS 텍스트 로고 대체)
                 수정 방법: .footer-logo-box 안 텍스트, .footer-logo-name 텍스트 변경
                 ====================================== -->
            <div class="footer-logo">
                <%-- 다른 화면으로 넘어가는 링크 --%>
                <a href="#" class="footer-logo-link">
                    <%-- CSS 아이콘 박스 (이미지 bo.jpg 대체) --%>
                    <div class="footer-logo-box">SM</div>
                    <%-- 회사명 텍스트 --%>
                    <span class="footer-logo-name">SM렌탈</span>
                </a>
            </div>
            <!-- ======================================
                 링크 메뉴 영역
                 이미지 sodog.jpg → "회사소개" 텍스트 링크로 대체
                 이미지 info.jpg  → "개인정보취급방침" 텍스트 링크로 대체
                 수정 방법: <a> 태그의 href 값과 텍스트 변경
                 ====================================== -->
            <div class="footer-links">
                <!-- 이미지 sodog.jpg → 텍스트 링크로 대체 -->
                <a href="#">회사소개</a>
                <!-- 이미지 info.jpg → 텍스트 링크로 대체 -->
                <a href="#">개인정보취급방침</a>
                <%-- 글자 묶음 — CSS 로 모양을 입히는 용도 --%>
                <span class="footer-extra">
                    &nbsp;| 사이버 신문고 &nbsp;| 이용약관 &nbsp;| 인재채용
                </span>
            </div>
            <!-- 회사 주소 및 연락처 정보 -->
            <div class="footer-info">
                <!-- 회사 기본 정보: 사업자등록번호, 통신판매업신고번호 -->
                (주) SM렌탈 &nbsp; 사업자 등록번호 214-98754-9874 &nbsp;
                <%-- 화면에 그대로 보이는 글자: "통신 판매업신고 번호 : 제 2010-충남-…" --%>
                통신 판매업신고 번호 : 제 2010-충남-05호
                <%-- 줄바꿈 --%>
                <br>
                <!-- 회사 주소 -->
                서울시 강남구 역삼동 역삼빌딩 2층 21호
                <%-- 줄바꿈 --%>
                <br><br>
                <!-- 연락처 -->
                대표전화 : 02-3456-6574
                <%-- 줄바꿈 --%>
                <br>
                <!-- 팩스 번호 -->
                FAX : 01-3254-9874
            </div>
        </div>
    </div>