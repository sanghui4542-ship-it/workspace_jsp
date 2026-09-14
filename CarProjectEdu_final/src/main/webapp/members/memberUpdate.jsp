<%@page import="Vo.MemberVO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%--
 ============================================================================
  members/memberUpdate.jsp  -  회원정보 수정 화면  (로그인한 회원만 볼 수 있다)

  CarMain.jsp 의 가운데 영역에 끼워져(include) 보이는 화면 조각이다.
    요청 흐름 : Top.jsp 회원정보 수정 버튼 -> /member/memberUpdate.me -> 사장(MemberController)
               -> DB 에서 조회한 내 정보(membervo)를 request 에 담아 -> CarMain.jsp -> 이 화면

  버튼별 동작 (아래 script 영역)
    수정하기 : CarApp.postForm 으로 /member/memberUpdatePro.me 에 POST 요청 (화면 이동 없음)
    목록으로 : 메인 화면으로 이동
    회원탈퇴 : CarApp.postForm 으로 /member/memberDelete.me 에 POST 요청 (화면 이동 없음)
    CarApp 은 CarMain.jsp 가 불러온 js/app.js 의 공용 도우미다
 ============================================================================
--%>
<%
    // 요청 글자 방식을 UTF-8 로 정한다
    request.setCharacterEncoding("UTF-8");

    // 프로젝트 경로 얻기  예) "/CarProject"  -> 아래 script 에서 요청 주소를 만들 때 사용
    String contextPath = request.getContextPath();

    // 사장(MemberController)이 request 에 저장해 둔 내 회원 정보(MemberVO 상자) 꺼내기
    MemberVO membervo = (MemberVO)request.getAttribute("membervo");

    // 상자에서 값을 꺼내 화면에 넣기 좋게 준비한다
    // 값이 null 이면 빈 글자("")로 바꾸고, HtmlUtil.escape 로 < > " 같은 특수문자를 안전한 글자로 바꾼다
    // (이름에 태그를 넣어 두어도 화면에서 스크립트로 실행되지 않게 막는다 = XSS 방지)
    String id      = util.HtmlUtil.escape(membervo.getId()      != null ? membervo.getId()      : "");
    String name    = util.HtmlUtil.escape(membervo.getName()    != null ? membervo.getName()    : "");
    String email   = util.HtmlUtil.escape(membervo.getEmail()   != null ? membervo.getEmail()   : "");
    int    age     = membervo.getAge();
    String gender  = util.HtmlUtil.escape(membervo.getGender()  != null ? membervo.getGender()  : "");
    String address = util.HtmlUtil.escape(membervo.getAddress() != null ? membervo.getAddress() : "");
%>

<style>
/* ==========================================================================
   회원정보 수정 화면 전용 스타일
   선택자(.이름) 는 아래 HTML 의 class="이름" 인 태그를 꾸민다
   ========================================================================== */

/* [전체 감싸는 상자] <div class="update-wrap"> : 화면 가운데에 놓이는 바깥 틀 */
.update-wrap {
    max-width: 680px;                        /* 최대 너비 680px : 화면이 넓어져도 이보다 넓어지지 않는다 */
    margin: 30px auto;                       /* 위아래 바깥 여백 30px, 좌우 auto : 좌우 여백을 똑같이 나눠 가운데 정렬 */
    padding: 0 16px 40px;                    /* 안쪽 여백 : 위 0, 좌우 16px, 아래 40px (좁은 화면에서 글자가 벽에 붙지 않게) */
    font-family: 'Noto Sans KR', sans-serif; /* 글꼴 : Noto Sans KR, 없으면 기본 고딕체(sans-serif) */
}

/* [카드] <div class="update-card"> : 흰 바탕의 둥근 사각형 카드 */
.update-card {
    background: #fff;                        /* 배경색 : 흰색 */
    border: 1px solid #e0e0e0;               /* 테두리 : 두께 1px, 실선(solid), 연한 회색 */
    border-radius: 10px;                     /* 모서리 둥글기 : 반지름 10px */
    overflow: hidden;                        /* 카드 밖으로 넘치는 부분 숨김 : 빨간 헤더의 모서리도 카드처럼 둥글게 잘린다 */
    box-shadow: 0 2px 12px rgba(0,0,0,0.07); /* 그림자 : 가로 0, 아래로 2px, 퍼짐 12px, 검정 7% 투명도 -> 카드가 살짝 떠 보인다 */
}

/* [카드 머리] <div class="update-card-header"> : 빨간 제목 띠 */
.update-card-header {
    background: #cc0000;                     /* 배경색 : 빨간색 */
    padding: 18px 28px;                      /* 안쪽 여백 : 위아래 18px, 좌우 28px */
    display: flex;                           /* 안의 아이콘과 제목을 가로 한 줄로 나란히 놓는다 */
    align-items: center;                     /* 아이콘과 제목의 세로 위치를 가운데로 맞춘다 */
    gap: 10px;                               /* 아이콘과 제목 사이 간격 10px */
}

/* [카드 머리 제목] <h2>회원정보 수정</h2> */
.update-card-header h2 {
    margin: 0;                               /* h2 의 기본 바깥 여백을 없앤다 */
    color: #fff;                             /* 글자색 : 흰색 */
    font-size: 1.15rem;                      /* 글자 크기 : 기본 글자(1rem = 보통 16px)의 1.15배 */
    font-weight: 700;                        /* 글자 굵기 : 굵게 (400 보통, 700 굵게) */
    letter-spacing: -0.3px;                  /* 글자 사이 간격 : 0.3px 좁힌다 */
}

/* [카드 머리 아이콘] <div class="header-icon"> : 사람 모양 아이콘을 담은 동그라미 */
.update-card-header .header-icon {
    width: 28px; height: 28px;               /* 크기 : 가로 28px, 세로 28px */
    background: rgba(255,255,255,0.25);      /* 배경색 : 흰색 25% 투명도 (빨간 바탕 위에 옅게 보인다) */
    border-radius: 50%;                      /* 모서리 둥글기 50% : 정사각형이 동그라미가 된다 */
    display: flex; align-items: center; justify-content: center;  /* 아이콘을 동그라미의 정가운데에 놓는다 */
    font-size: 14px; color: #fff;            /* 아이콘 크기 14px, 색 흰색 */
}

/* [카드 몸통] <div class="update-card-body"> : 입력칸들이 들어가는 영역 */
.update-card-body {
    padding: 28px 32px;                      /* 안쪽 여백 : 위아래 28px, 좌우 32px */
}

/* [아이디 표시 띠] <div class="id-badge"> : 수정할 수 없는 아이디를 보여주는 회색 띠 */
.id-badge {
    display: inline-flex;                    /* 안의 항목들을 가로 한 줄로 놓는다 */
    align-items: center;                     /* 항목들의 세로 위치를 가운데로 맞춘다 */
    gap: 8px;                                /* 항목 사이 간격 8px */
    background: #f5f5f5;                     /* 배경색 : 아주 연한 회색 */
    border: 1px solid #ddd;                  /* 테두리 : 1px 실선, 연한 회색 */
    border-radius: 6px;                      /* 모서리 둥글기 6px */
    padding: 8px 16px;                       /* 안쪽 여백 : 위아래 8px, 좌우 16px */
    margin-bottom: 22px;                     /* 아래 바깥 여백 22px : 다음 입력칸과 떨어뜨린다 */
    width: 100%;                             /* 너비 : 카드 몸통 너비를 꽉 채운다 */
}

/* [아이디 띠 안의 제목] <span class="label">아이디</span> */
.id-badge .label {
    font-size: 12px;                         /* 글자 크기 12px */
    color: #999;                             /* 글자색 : 회색 */
    white-space: nowrap;                     /* 글자가 길어도 줄바꿈하지 않는다 */
}

/* [아이디 띠 안의 값] <span class="value">hong</span> */
.id-badge .value {
    font-size: 14px;                         /* 글자 크기 14px */
    font-weight: 700;                        /* 글자 굵기 : 굵게 */
    color: #333;                             /* 글자색 : 진한 회색 */
}

/* [구분선] <hr class="divider"> : 비밀번호 영역과 기본 정보 영역을 나누는 점선 */
.divider {
    border: none;                            /* hr 의 기본 테두리를 모두 없앤다 */
    border-top: 1px dashed #e0e0e0;          /* 위쪽 테두리만 1px 점선(dashed), 연한 회색으로 그린다 */
    margin: 18px 0;                          /* 위아래 바깥 여백 18px, 좌우 0 */
}

/* [입력 한 줄] <div class="form-row"> : 왼쪽 제목 + 오른쪽 입력칸 */
.form-row {
    display: flex;                           /* 제목과 입력칸을 가로 한 줄로 나란히 놓는다 */
    align-items: flex-start;                 /* 제목과 입력칸의 윗선을 맞춘다 (힌트 글자가 있어도 제목이 위에 붙어 있다) */
    gap: 10px;                               /* 제목과 입력칸 사이 간격 10px */
    margin-bottom: 14px;                     /* 줄과 줄 사이 간격 14px */
}

/* [입력 줄의 제목] <span class="form-label">이름</span> */
.form-row .form-label {
    min-width: 80px;                         /* 최소 너비 80px : 제목 길이가 달라도 입력칸의 시작 위치가 똑같다 */
    font-size: 13px;                         /* 글자 크기 13px */
    font-weight: 600;                        /* 글자 굵기 : 조금 굵게 */
    color: #555;                             /* 글자색 : 중간 회색 */
    padding-top: 9px;                        /* 위쪽 안쪽 여백 9px : 입력칸 글자와 높이를 맞춘다 */
}

/* [입력칸을 감싸는 상자] <div class="form-control-wrap"> */
.form-row .form-control-wrap {
    flex: 1;                                 /* 제목이 쓰고 남은 가로 공간을 모두 차지한다 */
}

/* [입력칸] 글자, 비밀번호, 숫자, 이메일 입력칸 공통 모양 */
.form-row input[type=text],
.form-row input[type=password],
.form-row input[type=number],
.form-row input[type=email] {
    width: 100%;                             /* 너비 : 감싸는 상자를 꽉 채운다 */
    padding: 8px 12px;                       /* 안쪽 여백 : 위아래 8px, 좌우 12px */
    border: 1px solid #ddd;                  /* 테두리 : 1px 실선, 연한 회색 */
    border-radius: 6px;                      /* 모서리 둥글기 6px */
    font-size: 14px;                         /* 글자 크기 14px */
    color: #333;                             /* 글자색 : 진한 회색 */
    outline: none;                           /* 클릭했을 때 브라우저가 그리는 기본 파란 윤곽선을 없앤다 (아래 :focus 로 대신 표시) */
    transition: border-color 0.2s;           /* 테두리 색이 바뀔 때 0.2초 동안 부드럽게 바뀐다 */
    box-sizing: border-box;                  /* 너비 100% 안에 여백과 테두리까지 포함한다 (칸이 상자 밖으로 삐져나오지 않게) */
}

/* [입력칸 선택 중] 커서가 들어가 있는 입력칸 */
.form-row input:focus {
    border-color: #cc0000;                   /* 테두리 색 : 빨간색 */
    box-shadow: 0 0 0 3px rgba(204,0,0,0.08);/* 테두리 바깥에 3px 두께의 옅은 빨간 띠를 두른다 */
}

/* [성별 묶음] <div class="gender-group"> : 남성/여성 라디오 버튼 묶음 */
.gender-group {
    display: flex;                           /* 남성, 여성을 가로 한 줄로 놓는다 */
    gap: 16px;                               /* 남성과 여성 사이 간격 16px */
    padding-top: 8px;                        /* 위쪽 안쪽 여백 8px : 왼쪽 제목과 높이를 맞춘다 */
}

/* [성별 항목] <label><input type="radio"> 남성</label> */
.gender-group label {
    display: flex;                           /* 동그라미 버튼과 글자를 가로로 놓는다 */
    align-items: center;                     /* 버튼과 글자의 세로 위치를 가운데로 맞춘다 */
    gap: 5px;                                /* 버튼과 글자 사이 간격 5px */
    font-size: 14px;                         /* 글자 크기 14px */
    color: #444;                             /* 글자색 : 진한 회색 */
    cursor: pointer;                         /* 마우스를 올리면 손가락 모양 : 글자를 눌러도 선택된다는 표시 */
}

/* [성별 동그라미 버튼] <input type="radio"> */
.gender-group input[type=radio] {
    accent-color: #cc0000;                   /* 선택된 동그라미의 색 : 빨간색 */
    cursor: pointer;                         /* 마우스를 올리면 손가락 모양 */
}

/* [안내 글자] <p class="hint-text"> : 입력칸 아래의 작은 회색 도움말 */
.hint-text {
    font-size: 11px;                         /* 글자 크기 11px */
    color: #999;                             /* 글자색 : 회색 */
    margin-top: 4px;                         /* 위쪽 바깥 여백 4px : 입력칸과 살짝 띄운다 */
}

/* [버튼 영역] <div class="btn-area"> : 왼쪽 수정/목록 버튼, 오른쪽 탈퇴 버튼 */
.btn-area {
    display: flex;                           /* 버튼 묶음들을 가로 한 줄로 놓는다 */
    justify-content: space-between;          /* 첫 묶음은 왼쪽 끝, 마지막 묶음은 오른쪽 끝으로 벌려 놓는다 */
    align-items: center;                     /* 버튼들의 세로 위치를 가운데로 맞춘다 */
    margin-top: 26px;                        /* 위쪽 바깥 여백 26px */
    padding-top: 20px;                       /* 위쪽 안쪽 여백 20px */
    border-top: 1px solid #f0f0f0;           /* 위쪽에만 1px 연한 회색 선을 그어 입력 영역과 나눈다 */
}

/* [수정하기 버튼] <button class="btn-save"> : 빨간 바탕 흰 글자 */
.btn-save {
    padding: 10px 32px;                      /* 안쪽 여백 : 위아래 10px, 좌우 32px */
    background: #cc0000;                     /* 배경색 : 빨간색 */
    color: #fff;                             /* 글자색 : 흰색 */
    border: none;                            /* 테두리 없음 */
    border-radius: 6px;                      /* 모서리 둥글기 6px */
    font-size: 14px;                         /* 글자 크기 14px */
    font-weight: 700;                        /* 글자 굵기 : 굵게 */
    cursor: pointer;                         /* 마우스를 올리면 손가락 모양 */
    transition: background 0.15s;            /* 배경색이 바뀔 때 0.15초 동안 부드럽게 바뀐다 */
}

/* [수정하기 버튼에 마우스를 올렸을 때] 조금 더 어두운 빨간색 */
.btn-save:hover { background: #aa0000; }

/* [목록으로 버튼] <button class="btn-cancel"> : 흰 바탕 회색 테두리 */
.btn-cancel {
    padding: 10px 20px;                      /* 안쪽 여백 : 위아래 10px, 좌우 20px */
    background: #fff;                        /* 배경색 : 흰색 */
    color: #555;                             /* 글자색 : 중간 회색 */
    border: 1px solid #ccc;                  /* 테두리 : 1px 실선, 회색 */
    border-radius: 6px;                      /* 모서리 둥글기 6px */
    font-size: 13px;                         /* 글자 크기 13px */
    cursor: pointer;                         /* 마우스를 올리면 손가락 모양 */
    transition: all 0.15s;                   /* 모양이 바뀔 때 0.15초 동안 부드럽게 바뀐다 */
}

/* [목록으로 버튼에 마우스를 올렸을 때] 아주 연한 회색 바탕 */
.btn-cancel:hover { background: #f5f5f5; }

/* [회원탈퇴 버튼] <button class="btn-withdraw"> : 흰 바탕 빨간 글자 (위험한 동작이라 빨간색으로 경고) */
.btn-withdraw {
    padding: 10px 20px;                      /* 안쪽 여백 : 위아래 10px, 좌우 20px */
    background: #fff;                        /* 배경색 : 흰색 */
    color: #c00;                             /* 글자색 : 빨간색 (#c00 은 #cc0000 을 줄여 쓴 것) */
    border: 1px solid #ffaaaa;               /* 테두리 : 1px 실선, 연한 빨간색 */
    border-radius: 6px;                      /* 모서리 둥글기 6px */
    font-size: 13px;                         /* 글자 크기 13px */
    cursor: pointer;                         /* 마우스를 올리면 손가락 모양 */
    transition: all 0.15s;                   /* 모양이 바뀔 때 0.15초 동안 부드럽게 바뀐다 */
}

/* [회원탈퇴 버튼에 마우스를 올렸을 때] 연한 분홍 바탕 + 진한 빨간 테두리 */
.btn-withdraw:hover { background: #fff5f5; border-color: #cc0000; }

/* [터치 화면, 좁은 화면(767px 이하)일 때만 적용]
   손가락으로 누르기 쉽도록 버튼과 입력칸의 높이를 최소 44px 로 키운다
     hover: none        : 마우스를 올릴 수 없는 기기 (휴대폰, 태블릿)
     pointer: coarse    : 손가락처럼 끝이 뭉툭한 입력 도구
     max-width: 767px   : 화면 너비가 767px 이하 */
@media (hover: none), (pointer: coarse), (max-width: 767px) {

    /* 수정하기, 목록으로, 회원탈퇴 버튼 */
    .btn-save,
    .btn-cancel,
    .btn-withdraw {
        min-height: 44px;                    /* 최소 높이 44px */
    }

    /* 입력칸들 */
    .form-row input[type=text],
    .form-row input[type=password],
    .form-row input[type=number],
    .form-row input[type=email] {
        min-height: 44px;                    /* 최소 높이 44px */
    }
}

/* [결과 안내 상자] <div id="resultMsg"> : 수정 결과를 보여주는 상자 (#이름 은 id="이름" 인 태그를 꾸민다) */
#resultMsg {
    display: none;                           /* 처음에는 숨긴다 (script 의 showMsg() 가 보이게 바꾼다) */
    padding: 10px 16px;                      /* 안쪽 여백 : 위아래 10px, 좌우 16px */
    border-radius: 6px;                      /* 모서리 둥글기 6px */
    font-size: 13px;                         /* 글자 크기 13px */
    margin-top: 14px;                        /* 위쪽 바깥 여백 14px */
    text-align: center;                      /* 글자를 가운데 정렬 */
}

/* [성공 안내] <div id="resultMsg" class="success"> : 초록색 상자 */
#resultMsg.success {
    background: #e8f5e9;                     /* 배경색 : 연한 초록 */
    color: #2e7d32;                          /* 글자색 : 진한 초록 */
    border: 1px solid #a5d6a7;               /* 테두리 : 1px 실선, 중간 초록 */
}

/* [실패 안내] <div id="resultMsg" class="error"> : 빨간색 상자 */
#resultMsg.error {
    background: #ffebee;                     /* 배경색 : 연한 분홍 */
    color: #c62828;                          /* 글자색 : 진한 빨강 */
    border: 1px solid #ffcdd2;               /* 테두리 : 1px 실선, 연한 빨강 */
}
</style>

<div class="update-wrap">
    <div class="update-card">

        <%-- ===== 카드 머리 : 아이콘 + 제목 ===== --%>
        <div class="update-card-header">
            <div class="header-icon">&#128100;</div>
            <h2>회원정보 수정</h2>
        </div>

        <div class="update-card-body">

            <%-- ===== 아이디 : 수정할 수 없으므로 입력칸이 아닌 글자로만 보여준다 ===== --%>
            <div class="id-badge">
                <span class="label">아이디</span>
                <span class="value"><%=id%></span>
                <span style="margin-left:auto; font-size:11px; color:#aaa; background:#e9e9e9; padding:2px 8px; border-radius:10px;">변경 불가</span>
            </div>

            <%-- ===== 비밀번호 : 새 비밀번호를 비워 두면 기존 비밀번호가 그대로 유지된다
                       바꿀 때만 본인 확인용 현재 비밀번호를 함께 입력한다 ===== --%>
            <div class="form-row">
                <span class="form-label">새 비밀번호</span>
                <div class="form-control-wrap">
                    <input type="password" id="pass" autocomplete="new-password"
                           placeholder="새 비밀번호를 입력하세요 (4자 이상)" />
                    <p class="hint-text">&#8226; 변경하지 않으려면 비워두세요 (기존 비밀번호 유지)</p>
                </div>
            </div>

            <div class="form-row">
                <span class="form-label">현재 비밀번호</span>
                <div class="form-control-wrap">
                    <input type="password" id="currentPass" autocomplete="current-password"
                           placeholder="비밀번호를 변경할 때만 입력하세요" />
                    <p class="hint-text">&#8226; 본인 확인을 위해 현재 비밀번호를 함께 입력해야 변경됩니다</p>
                </div>
            </div>

            <hr class="divider">

            <%-- ===== 기본 정보 : value 에 DB 에서 조회한 현재 값을 미리 채워 둔다 ===== --%>
            <div class="form-row">
                <span class="form-label">이름</span>
                <div class="form-control-wrap">
                    <input type="text" id="name" value="<%=name%>" placeholder="이름을 입력하세요" />
                </div>
            </div>

            <div class="form-row">
                <span class="form-label">나이</span>
                <div class="form-control-wrap">
                    <input type="number" id="age" value="<%=age%>" min="1" max="150" placeholder="나이를 입력하세요" />
                </div>
            </div>

            <%-- 성별 : DB 에 저장된 값과 같은 쪽 라디오 버튼에 checked 를 붙여 미리 선택해 둔다 --%>
            <div class="form-row">
                <span class="form-label">성별</span>
                <div class="form-control-wrap">
                    <div class="gender-group">
                        <label>
                            <input type="radio" name="gender" value="남"
                                <%="남".equals(gender) ? "checked" : ""%> /> 남성
                        </label>
                        <label>
                            <input type="radio" name="gender" value="여"
                                <%="여".equals(gender) ? "checked" : ""%> /> 여성
                        </label>
                    </div>
                </div>
            </div>

            <div class="form-row">
                <span class="form-label">주소</span>
                <div class="form-control-wrap">
                    <input type="text" id="address" value="<%=address%>" placeholder="주소를 입력하세요" />
                </div>
            </div>

            <div class="form-row">
                <span class="form-label">이메일</span>
                <div class="form-control-wrap">
                    <input type="email" id="email" value="<%=email%>" placeholder="이메일을 입력하세요" />
                </div>
            </div>

            <%-- ===== 결과 안내 상자 : 아래 script 의 showMsg() 가 초록(성공)/빨강(실패)으로 보여준다 ===== --%>
            <div id="resultMsg"></div>

            <%-- ===== 버튼 영역 ===== --%>
            <div class="btn-area">
                <div style="display:flex; gap:8px;">
                    <button type="button" class="btn-save" id="btnSave">수정하기</button>
                    <button type="button" class="btn-cancel" id="btnCancel">목록으로</button>
                </div>
                <button type="button" class="btn-withdraw" id="btnWithdraw">&#128465; 회원탈퇴</button>
            </div>

        </div>
    </div>
</div>

<script type="text/javascript">

    // 요청 주소를 만들 때 쓸 프로젝트 경로  예) "/CarProject"
    var contextPath = "<%=contextPath%>";

    /* ------------------------------------------------------------------------
       수정하기 버튼 : 입력값을 검사한 뒤 서버에 회원정보 수정을 요청한다
       ------------------------------------------------------------------------ */
    document.getElementById("btnSave").addEventListener("click", function() {

        // 입력칸들의 값 얻기 (CarApp.val("id") = document.getElementById("id").value)
        var name        = CarApp.val("name").trim();
        var ageVal      = CarApp.val("age").trim();
        var address     = CarApp.val("address").trim();
        var email       = CarApp.val("email").trim();
        var newPass     = CarApp.val("pass").trim();
        var currentPass = CarApp.val("currentPass");

        // 성별 라디오 버튼 중 선택된 것을 찾아 값을 얻는다 (선택 안 했으면 빈 글자)
        var genderEl = document.querySelector("input[name=gender]:checked");
        var gender   = genderEl ? genderEl.value : "";

        // 이름이 비어 있으면 안내하고 끝낸다
        if(!name) {
            showMsg("이름을 입력해 주세요.", "error"); return;
        }

        // 나이가 비었거나, 숫자가 아니거나(isNaN), 1보다 작으면 안내하고 끝낸다
        if(!ageVal || isNaN(ageVal) || parseInt(ageVal) < 1) {
            showMsg("나이를 올바르게 입력해 주세요.", "error"); return;
        }

        // 성별을 선택하지 않았으면 안내하고 끝낸다
        if(!gender) {
            showMsg("성별을 선택해 주세요.", "error"); return;
        }

        // 이메일이 비어 있으면 안내하고 끝낸다
        if(!email) {
            showMsg("이메일을 입력해 주세요.", "error"); return;
        }

        // 새 비밀번호를 입력한 경우에만 길이와 현재 비밀번호 입력 여부를 검사한다
        if(newPass !== "") {
            if(newPass.length < 4) {
                showMsg("새 비밀번호는 4자 이상이어야 합니다.", "error"); return;
            }
            if(!currentPass) {
                showMsg("비밀번호를 변경하려면 현재 비밀번호를 입력해 주세요.", "error"); return;
            }
        }

        // 서버로 보낼 값 모으기 (아이디는 보내지 않는다. 서버가 세션의 로그인 아이디를 쓴다)
        var sendData = {
            name:    name,
            age:     ageVal,
            gender:  gender,
            address: address,
            email:   email
        };

        // 새 비밀번호를 입력한 경우에만 새 비밀번호와 현재 비밀번호를 함께 보낸다
        if(newPass !== "") {
            sendData.pass        = newPass;
            sendData.currentPass = currentPass;
        }

        // 사장(MemberController)의 /member/memberUpdatePro.me 에 POST 로 수정 요청을 보낸다
        CarApp.postForm(contextPath + "/member/memberUpdatePro.me", sendData)

            // 서버가 보낸 결과 글자(result)에 따라 안내한다
            .then(function(result) {

                // 수정 성공 : 초록 상자로 안내하고, 입력했던 비밀번호 칸을 비운다
                if(result === "수정성공") {
                    showMsg("\u2714 회원정보가 성공적으로 수정되었습니다.", "success");
                    CarApp.id("pass").value = "";
                    CarApp.id("currentPass").value = "";

                // 현재 비밀번호 틀림 : 빨간 상자로 안내하고, 현재 비밀번호 칸을 비운 뒤 커서를 옮긴다
                } else if(result === "현재비밀번호불일치") {
                    showMsg("현재 비밀번호가 일치하지 않습니다.", "error");
                    CarApp.id("currentPass").value = "";
                    CarApp.id("currentPass").focus();

                // 새 비밀번호가 너무 짧음 : 빨간 상자로 안내
                } else if(result === "비밀번호길이부족") {
                    showMsg("새 비밀번호는 4자 이상이어야 합니다.", "error");

                // 그 밖의 실패 : 빨간 상자로 안내
                } else {
                    showMsg("수정에 실패했습니다. 다시 시도해 주세요.", "error");
                }
            })

            // 요청 자체가 실패한 경우 (서버 오류, 로그인 만료 등) : 실패 이유를 빨간 상자로 안내
            .catch(function(err) {
                showMsg("서버 오류가 발생했습니다. (" + err.message + ")", "error");
            });
    });

    /* ------------------------------------------------------------------------
       목록으로 버튼 : 메인 화면으로 이동한다
       ------------------------------------------------------------------------ */
    document.getElementById("btnCancel").addEventListener("click", function() {
        location.href = contextPath + "/Car/Main";
    });

    /* ------------------------------------------------------------------------
       회원탈퇴 버튼 : 한 번 더 확인한 뒤 서버에 탈퇴를 요청한다
       ------------------------------------------------------------------------ */
    document.getElementById("btnWithdraw").addEventListener("click", function() {

        // 확인창에서 취소를 누르면 아무것도 하지 않고 끝낸다
        if(!confirm("정말 탈퇴하시겠습니까?\n탈퇴 후 복구가 불가능합니다.")) {
            return;
        }

        // 사장(MemberController)의 /member/memberDelete.me 에 POST 로 탈퇴 요청을 보낸다 (누구인지는 서버가 세션으로 안다)
        CarApp.postForm(contextPath + "/member/memberDelete.me", {})

            // 서버가 보낸 결과 글자(result)에 따라 안내한다
            .then(function(result) {

                // 탈퇴 성공 : 안내창을 띄우고 메인 화면으로 이동
                if(result === "삭제성공") {
                    alert("회원 탈퇴가 완료되었습니다.\n이용해 주셔서 감사합니다.");
                    location.href = contextPath + "/Car/Main";

                // 탈퇴 실패 : 빨간 상자로 안내
                } else {
                    showMsg("탈퇴에 실패했습니다. 다시 시도해 주세요.", "error");
                }
            })

            // 요청 자체가 실패한 경우 : 실패 이유를 빨간 상자로 안내
            .catch(function(err) {
                showMsg("서버 오류가 발생했습니다. (" + err.message + ")", "error");
            });
    });

    /* ------------------------------------------------------------------------
       showMsg : 결과 안내 상자(<div id="resultMsg">)에 안내 문구를 보여준다
         msg  : 보여줄 문구
         type : "success"(초록) 또는 "error"(빨강) -> 위 CSS 의 #resultMsg.success / #resultMsg.error
       ------------------------------------------------------------------------ */
    function showMsg(msg, type) {

        // 결과 안내 상자 찾기
        var box = document.getElementById("resultMsg");

        // 이전에 붙어 있던 색 class 를 떼고, 이번 종류(success/error)의 class 를 붙인다
        box.classList.remove("success", "error");
        box.classList.add(type);

        // 안내 문구를 넣고 상자를 보이게 한다
        box.textContent = msg;
        box.style.display = "block";

        // 성공 안내는 3초(3000 밀리초) 뒤에 자동으로 숨긴다
        if(type === "success") {
            setTimeout(function() { box.style.display = "none"; }, 3000);
        }
    }

</script>
