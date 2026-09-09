/* ==============================================================================
   join.js  -  회원가입 화면(members/join.jsp) 입력 검증 스크립트
   ------------------------------------------------------------------------------
   [5단계 변경] jQuery -> 순수 자바스크립트로 전면 재작성
     (기존)  353행 / jQuery 호출 89곳 / $.ajax 1곳
             code.jquery.com 에서 jQuery 를 받아야 동작했으므로
             인터넷이 없는 강의실에서는 회원가입 검증이 전부 멈췄다.
     (지금)  브라우저 표준 API 만 사용한다. 외부 라이브러리 0개.
   ------------------------------------------------------------------------------
   [함께 고친 실제 버그 3가지]
     버그1. 아이디 중복확인 주소가 절대경로로 박혀 있었다.
         url: "http://localhost:8090/CarProject/member/joinIdCheck.me"
       포트가 8080이거나 프로젝트명이 다르거나 다른 PC에 배포하면
       중복확인이 조용히 실패했다. -> 상대경로로 변경.
     버그2. 이름 검증 기준이 두 곳에서 서로 달랐다.
       입력 중에는 "길이 2~6자"만 보고, 최종 검증에서는 "한글 2~6자"를 봤다.
       그래서 "abcd"를 입력하면 입력 중에는 "이름입력완료!"라고 안내했다가
       가입 버튼을 누르면 거부되었다.
       -> 최종 검증 기준(한글 2~6자)으로 통일해 안내와 실제 동작을 맞췄다.
   ------------------------------------------------------------------------------
   [주소 검증 관련 참고 - 버그가 아니라 구조 개선]
     기존 최종 검증은 주소를 id 로 찾았다.
         $("#sample4_postcode"), $("#sample4_roadAddress") ...
     이 id 들은 카카오(다음) 우편번호 API 예제에서 온 이름이다.
     실제 입력칸은 id 와 name 을 함께 갖고 있어서
         <input id="sample4_postcode" name="address1">
     기존 코드도 정상 동작했다. (없는 요소를 찾는 것이 아니다)
     다만 서버가 값을 받을 때 쓰는 기준은 name(address1~address5)이므로,
     검증도 name 기준으로 맞췄다.
     이렇게 하면 우편번호 API 를 다른 것으로 바꿔 id 가 달라져도 검증이 깨지지 않는다.
   ------------------------------------------------------------------------------
   [jQuery -> 표준 API 대응]
     $("#id").val()                  ->  el.value
     $("#id").focusout(fn)           ->  el.addEventListener("blur", fn)
     $("#x").text(t).css("color",c)  ->  el.textContent = t; el.style.color = c;
     $("#agree").is(":checked")      ->  el.checked
     $.ajax({...})                   ->  CarApp.postForm(...)   (js/app.js)
   ============================================================================== */
(function () {
    "use strict";   // "엄격 모드". 오타로 만든 변수 같은 흔한 실수를 브라우저가 오류로 잡아 준다
    /* ==========================================================================
       0. 도우미 함수
       ========================================================================== */
    function el(id) {
        return document.getElementById(id);   // id 로 요소 하나를 찾아 돌려준다. 못 찾으면 null 이다
    }
    /**
     * 입력값 얻기. 요소가 없어도 빈 문자열을 돌려준다.
     *
     * [왜 방어하는가]
     *   jQuery 는 없는 요소를 조회해도 오류를 내지 않았다.
     *   반면 document.getElementById("없는id").value 는 즉시 오류가 나
     *   그 뒤의 스크립트 전체가 멈춘다.
     *   화면 구성이 조금 달라져도 검증이 통째로 죽지 않게 한다.
     */
    function val(id) {
        // e — el( ) 의 결과를 담는다
        var e = el(id);   // 먼저 요소를 찾는다
        return e ? e.value : "";   // 요소가 있으면 값을, 없으면 빈 문자열을 돌려준다
    }
    /** name 속성으로 입력값 얻기 (주소 칸처럼 id 가 없는 입력에 사용) */
    function valByName(name) {
        // 화면에서 "[name=" 에 해당하는 요소를 찾는다
        var e = document.querySelector("[name='" + name + "']");   // id 가 아니라 name 속성으로 찾는다 (주소 칸들은 id 가 없고 name 만 있다)
        return e ? e.value : "";   // 요소가 있으면 값을, 없으면 빈 문자열을 돌려준다
    }
    /** 안내 문구 표시 (요소id, 문구, 색) */
    function msg(id, text, color) {
        // e — el( ) 의 결과를 담는다
        var e = el(id);   // 안내 문구를 띄울 자리를 찾는다
        // 조건을 확인해 맞을 때만 아래를 실행한다
        if (!e) { return; }   // 그 자리가 없으면 아무것도 하지 않는다
        // 요소 안의 글자를 바꾼다
        e.textContent = text;   // 안내 문구를 넣는다
        // 요소의 모양(color)을 바꾼다
        e.style.color = color;   // 빨강(오류) 또는 파랑(정상)으로 색을 바꾼다
    }
    /** blur(입력칸을 벗어남) 이벤트 등록. 요소가 없으면 조용히 넘어간다 */
    function onBlur(id, handler) {
        // e — el( ) 의 결과를 담는다
        var e = el(id);   // 그 입력칸을 찾는다
        // 조건을 확인해 맞을 때만 아래를 실행한다
        if (e) { e.addEventListener("blur", handler); }   // blur = 입력칸에서 커서가 빠져나갈 때. 그 순간 검사를 실행하도록 등록한다
    }
    /** 회원가입 폼 찾기 */
    function joinForm() {
        // 화면에서 "form.form" 에 해당하는 요소를 찾는다
        return document.querySelector("form.form") || document.querySelector("form");   // form.form 을 먼저 찾고, 없으면 화면의 첫 번째 form 을 쓴다
    }
    /* ==========================================================================
       1. 검증 규칙 (기존 정규식을 그대로 유지했다)
       ========================================================================== */
    var RULES = {
        id:    /^[A-Za-z0-9_\-]{3,20}$/,                              // 영문/숫자/_/- 3~20자
        pass:  /^[A-Za-z0-9_\-]{4,20}$/,                              // 영문/숫자/_/- 4~20자
        name:  /^[가-힣]{2,6}$/,                                       // 한글 2~6자
        email: /^\w{5,12}@[a-z]{2,10}[\.][a-z]{2,3}[\.]?[a-z]{0,2}$/,  // 예) abcde@naver.com
        tel:   /^0[0-9]{8,10}$/,                                       // 0으로 시작 9~11자리
        hp:    /^01[0179][0-9]{7,8}$/                                  // 010/011/017/019 + 7~8자리
    };
    /** 주소 입력칸 name 목록 */
    var ADDRESS_NAMES = ["address1", "address2", "address3", "address4", "address5"];
    /** 주소 5칸 중 하나라도 비어 있으면 true */
    function isAddressEmpty() {
        return ADDRESS_NAMES.some(function (name) {   // some = 하나라도 조건에 맞으면 true. 주소 5칸을 차례로 확인한다
            return valByName(name).trim() === "";   // 공백을 없앤 뒤 비어 있으면 true
        });
    }
    /** 컨텍스트 경로 (join.jsp 가 window.CONTEXT_PATH 로 알려준다) */
    function contextPath() {
        // 조건을 확인해 맞을 때만 아래를 실행한다
        if (window.CONTEXT_PATH) { return window.CONTEXT_PATH; }   // JSP 가 알려 준 값이 있으면 그것을 쓴다
        //알려주지 않았다면 현재 주소에서 유추한다 (/CarProject/members/... -> /CarProject)
        var first = location.pathname.split("/")[1];
        return first ? ("/" + first) : "";   // 주소의 첫 조각이 프로젝트 이름이다. 없으면 빈 문자열
    }
    /* ==========================================================================
       2. 입력하는 중에 알려주는 검증 (실시간 안내)
       ========================================================================== */
    /* ---- 약관 동의 ---- */
    var agreeEl = el("agree");
    // 조건을 확인해 맞을 때만 아래를 실행한다
    if (agreeEl) {   // 동의 체크박스가 화면에 있으면
        // "click" 사건이 일어나면 실행할 동작을 걸어 둔다
        agreeEl.addEventListener("click", function () {   // 체크박스를 클릭했을 때 실행할 동작을 등록한다
            // 조건을 확인해 맞을 때만 아래를 실행한다
            if (!agreeEl.checked) {   // 체크가 풀린 상태라면
                // msg( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
                msg("agreeInput", "약관에 동의해 주세요!", "red");   // 동의해 달라고 빨간 글씨로 안내한다
            // 위 조건들이 전부 아닐 때
            } else {
                // msg( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
                msg("agreeInput", "약관동의 완료!", "blue");   // 체크했으면 완료를 파란 글씨로 안내한다
            }
        });
    }
    /* ---- 아이디 : 형식 검사 + 서버에 중복 확인 ---- */
    onBlur("id", function () {
        // value — val( ) 의 결과를 담는다
        var value = val("id");   // 입력한 아이디를 읽는다
        // 조건을 확인해 맞을 때만 아래를 실행한다
        if (!RULES.id.test(value)) {   // 정해 둔 형식(영문·숫자·_·- 3~20자)에 맞지 않으면
            // msg( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
            msg("idInput", "한글,특수문자 없이 3~20글자사이로 작성해 주세요!", "red");   // 무엇이 잘못됐는지 빨간 글씨로 안내한다
            return;   // 형식부터 틀렸으니 서버에 물어볼 필요가 없다. 여기서 끝낸다
        }
        //서버에 중복 확인 요청 (상대경로 사용 - 버그1 수정)
        CarApp.postForm(contextPath() + "/member/joinIdCheck.me", { id: value })
            .then(function (data) {
                //서버는 "usable"(사용가능) 또는 "not_usable"(중복) 을 보낸다
                if (data === "usable") {
                    // msg( ) 를 실행한다 (직접 만든 도우미)
                    msg("idInput", "사용할수 있는 ID입니다.", "blue");
                // 위 조건들이 전부 아닐 때
                } else {
                    // msg( ) 를 실행한다 (직접 만든 도우미)
                    msg("idInput", "이미 사용중인 ID입니다.", "red");
                }
            })
            .catch(function () {   // 통신 자체가 실패한 경우 (서버가 꺼졌거나 네트워크 문제)
                // msg( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
                msg("idInput", "아이디 확인 중 오류가 발생했습니다.", "red");   // 사용자에게 오류가 났음을 알린다
            });
    });
    /* ---- 비밀번호 ---- */
    onBlur("pass", function () {
        // 조건을 확인해 맞을 때만 아래를 실행한다
        if (val("pass").length < 4) {   // 비밀번호가 4글자보다 짧으면
            // msg( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
            msg("passInput", "한글,특수문자 없이 4글자 이상으로 작성해 주세요!", "red");   // 빨간 글씨로 안내한다
        // 위 조건들이 전부 아닐 때
        } else {
            // msg( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
            msg("passInput", "올바르게 입력되었습니다.", "blue");   // 길이가 충분하면 파란 글씨로 안내한다
        }
    });
    /* ---- 이름 (버그3 수정 : 최종 검증과 같은 기준인 '한글 2~6자' 적용) ---- */
    onBlur("name", function () {
        // 조건을 확인해 맞을 때만 아래를 실행한다
        if (!RULES.name.test(val("name"))) {   // 이름이 한글 2~6자 형식에 맞지 않으면
            // msg( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
            msg("nameInput", "이름을 한글 2~6자로 작성해 주세요.", "red");   // 빨간 글씨로 안내한다
        // 위 조건들이 전부 아닐 때
        } else {
            // msg( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
            msg("nameInput", "이름입력완료!", "blue");   // 형식에 맞으면 파란 글씨로 안내한다
        }
    });
    /* ---- 나이 ---- */
    onBlur("age", function () {
        // 조건을 확인해 맞을 때만 아래를 실행한다
        if (val("age").trim() === "") {   // 나이 칸이 비어 있으면
            // msg( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
            msg("ageInput", "나이를 입력해주세요.", "red");   // 빨간 글씨로 안내한다
        // 위 조건들이 전부 아닐 때
        } else {
            // msg( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
            msg("ageInput", "나이입력완료!", "blue");   // 입력했으면 파란 글씨로 안내한다
        }
    });
    /* ---- 성별 (여러 개라 클래스로 찾는다) ---- */
    Array.prototype.forEach.call(document.querySelectorAll(".gender"), function (g) {
        // "click" 사건이 일어나면 실행할 동작을 걸어 둔다
        g.addEventListener("click", function () {   // 성별 라디오 버튼을 클릭했을 때 실행할 동작을 등록한다
            // msg( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
            msg("genderInput", "성별체크완료!", "blue");   // 골랐다는 사실만 알려 주면 된다
        });
    });
    /* ---- 이메일 ---- */
    onBlur("email", function () {
        // 조건을 확인해 맞을 때만 아래를 실행한다
        if (!RULES.email.test(val("email"))) {   // 이메일 형식에 맞지 않으면
            // msg( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
            msg("emailInput", "이메일 형식이 올바르지 않습니다.", "red");   // 빨간 글씨로 안내한다
        // 위 조건들이 전부 아닐 때
        } else {
            // msg( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
            msg("emailInput", "올바르게 입력되었습니다.", "blue");   // 형식에 맞으면 파란 글씨로 안내한다
        }
    });
    /* ---- 전화번호 ---- */
    onBlur("tel", function () {
        // 조건을 확인해 맞을 때만 아래를 실행한다
        if (!RULES.tel.test(val("tel"))) {   // 전화번호 형식에 맞지 않으면
            // msg( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
            msg("telInput", "전화번호 형식이 올바르지 않습니다.", "red");   // 빨간 글씨로 안내한다
        // 위 조건들이 전부 아닐 때
        } else {
            // msg( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
            msg("telInput", "올바르게 입력되었습니다.", "blue");   // 형식에 맞으면 파란 글씨로 안내한다
        }
    });
    /* ---- 휴대폰번호 ---- */
    onBlur("hp", function () {
        // 조건을 확인해 맞을 때만 아래를 실행한다
        if (!RULES.hp.test(val("hp"))) {   // 휴대폰번호 형식에 맞지 않으면
            // msg( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
            msg("hpInput", "휴대폰번호 형식이 올바르지 않습니다.", "red");   // 빨간 글씨로 안내한다
        // 위 조건들이 전부 아닐 때
        } else {
            // msg( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
            msg("hpInput", "올바르게 입력되었습니다.", "blue");   // 형식에 맞으면 파란 글씨로 안내한다
        }
    });
    /* ---- 주소 5칸 ---- */
    ADDRESS_NAMES.forEach(function (name) {
        // 화면에서 "[name=" 에 해당하는 요소를 찾는다
        var input = document.querySelector("[name='" + name + "']");   // name 으로 주소 입력칸 하나를 찾는다
        // 조건을 확인해 맞을 때만 아래를 실행한다
        if (!input) { return; }   // 그 칸이 화면에 없으면 아무것도 하지 않는다
        // "blur" 사건이 일어나면 실행할 동작을 걸어 둔다
        input.addEventListener("blur", function () {   // 그 칸에서 커서가 빠져나갈 때 검사한다
            // 조건을 확인해 맞을 때만 아래를 실행한다
            if (isAddressEmpty()) {   // 주소 5칸 중 하나라도 비어 있으면
                // msg( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
                msg("addressInput", "주소를 모두 작성하여주세요.", "red");   // 모두 채워 달라고 빨간 글씨로 안내한다
            // 위 조건들이 전부 아닐 때
            } else {
                // msg( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
                msg("addressInput", "올바르게 입력되었습니다.", "blue");   // 다 채웠으면 파란 글씨로 안내한다
            }
        });
    });
    /* ==========================================================================
       3. 최종 검증
          하나라도 통과하지 못하면 false 를 돌려준다.
          [중요] 화면 검증은 사용자 편의를 위한 것이다.
                 주소로 직접 요청하면 이 검증을 건너뛸 수 있으므로
                 서버(MemberService)도 같은 검증을 다시 한다.
       ========================================================================== */
    function validate() {
        //1. 약관 동의
        var agree = el("agree");
        // 조건을 확인해 맞을 때만 아래를 실행한다
        if (!agree || !agree.checked) {   // 동의 체크박스가 없거나 체크가 안 됐으면
            // msg( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
            msg("agreeInput", "약관에 동의해 주세요!", "red");   // 빨간 글씨로 안내한다
            return false;   // false 를 돌려주면 전송이 중단된다
        }
        //2. 아이디
        if (!RULES.id.test(val("id"))) {
            // msg( ) 를 실행한다 (직접 만든 도우미)
            msg("idInput", "한글,특수문자 없이 3~20글자사이로 작성해 주세요!", "red");
            // 조건을 확인해 맞을 때만 아래를 실행한다
            if (el("id")) { el("id").focus(); }   // 잘못 쓴 칸으로 커서를 옮겨 준다. 사용자가 어디를 고칠지 바로 안다
            return false;   // false 를 돌려주면 전송이 중단된다
        }
        //3. 비밀번호
        if (!RULES.pass.test(val("pass"))) {
            // msg( ) 를 실행한다 (직접 만든 도우미)
            msg("passInput", "한글,특수문자 없이 4글자 이상으로 작성해 주세요!", "red");
            // 조건을 확인해 맞을 때만 아래를 실행한다
            if (el("pass")) { el("pass").focus(); }   // 잘못 쓴 칸으로 커서를 옮겨 준다
            return false;   // false 를 돌려주면 전송이 중단된다
        }
        // msg( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
        msg("passInput", "올바르게 입력되었습니다.", "blue");   // 통과했으면 파란 글씨로 표시해 둔다
        //4. 이름 (한글 2~6자)
        if (!RULES.name.test(val("name"))) {
            // msg( ) 를 실행한다 (직접 만든 도우미)
            msg("nameInput", "이름을 한글로 작성하여주세요.", "red");
            // 조건을 확인해 맞을 때만 아래를 실행한다
            if (el("name")) { el("name").focus(); }   // 잘못 쓴 칸으로 커서를 옮겨 준다
            return false;   // false 를 돌려주면 전송이 중단된다
        }
        // msg( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
        msg("nameInput", "올바르게 입력되었습니다.", "blue");   // 통과했으면 파란 글씨로 표시해 둔다
        //5. 나이
        if (val("age").trim() === "") {
            // msg( ) 를 실행한다 (직접 만든 도우미)
            msg("ageInput", "나이를 입력해주세요.", "red");
            // 조건을 확인해 맞을 때만 아래를 실행한다
            if (el("age")) { el("age").focus(); }   // 잘못 쓴 칸으로 커서를 옮겨 준다
            return false;   // false 를 돌려주면 전송이 중단된다
        }
        // msg( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
        msg("ageInput", "올바르게 입력되었습니다.", "blue");   // 통과했으면 파란 글씨로 표시해 둔다
        //6. 주소 (버그2 수정 : 실제 입력칸을 검사한다)
        if (isAddressEmpty()) {
            // msg( ) 를 실행한다 (직접 만든 도우미)
            msg("addressInput", "주소를 모두 작성하여주세요.", "red");
            return false;   // false 를 돌려주면 전송이 중단된다
        }
        // msg( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
        msg("addressInput", "올바르게 입력되었습니다.", "blue");   // 통과했으면 파란 글씨로 표시해 둔다
        //7. 성별
        if (!document.querySelector(".gender:checked")) {
            // msg( ) 를 실행한다 (직접 만든 도우미)
            msg("genderInput", "성별을 체크 해주세요.", "red");
            return false;   // false 를 돌려주면 전송이 중단된다
        }
        //8. 이메일
        if (!RULES.email.test(val("email"))) {
            // msg( ) 를 실행한다 (직접 만든 도우미)
            msg("emailInput", "이메일 형식이 올바르지 않습니다.", "red");
            // 조건을 확인해 맞을 때만 아래를 실행한다
            if (el("email")) { el("email").focus(); }   // 잘못 쓴 칸으로 커서를 옮겨 준다
            return false;   // false 를 돌려주면 전송이 중단된다
        }
        // msg( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
        msg("emailInput", "올바르게 입력되었습니다.", "blue");   // 통과했으면 파란 글씨로 표시해 둔다
        //9. 전화번호
        if (!RULES.tel.test(val("tel"))) {
            // msg( ) 를 실행한다 (직접 만든 도우미)
            msg("telInput", "전화번호 형식이 올바르지 않습니다.", "red");
            // 조건을 확인해 맞을 때만 아래를 실행한다
            if (el("tel")) { el("tel").focus(); }   // 잘못 쓴 칸으로 커서를 옮겨 준다
            return false;   // false 를 돌려주면 전송이 중단된다
        }
        // msg( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
        msg("telInput", "올바르게 입력되었습니다.", "blue");   // 통과했으면 파란 글씨로 표시해 둔다
        //10. 휴대폰번호
        if (!RULES.hp.test(val("hp"))) {
            // msg( ) 를 실행한다 (직접 만든 도우미)
            msg("hpInput", "휴대폰번호 형식이 올바르지 않습니다.", "red");
            // 조건을 확인해 맞을 때만 아래를 실행한다
            if (el("hp")) { el("hp").focus(); }   // 잘못 쓴 칸으로 커서를 옮겨 준다
            return false;   // false 를 돌려주면 전송이 중단된다
        }
        // msg( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
        msg("hpInput", "올바르게 입력되었습니다.", "blue");   // 통과했으면 파란 글씨로 표시해 둔다
        return true; //모두 통과
    }
    /* ==========================================================================
       4. 전송 처리
          [기존] check() 마지막에서 $("form").submit() 을 직접 호출했다.
          [지금] 두 가지 호출 방식을 모두 지원한다.
            (가) 화면 버튼이 onclick="check()" 로 호출하는 경우
                 -> 검증 통과 시 이 함수가 폼을 전송한다 (기존 동작 유지)
            (나) 폼이 그냥 submit 되는 경우
                 -> submit 이벤트에서 검증하고 실패 시 전송을 막는다
                    (기존에는 이 경로로 검증 없이 전송될 여지가 있었다)
       ========================================================================== */
    /** 화면에서 onclick="check()" 로 호출하는 함수 (기존 이름 유지) */
    window.check = function () {
        // 조건을 확인해 맞을 때만 아래를 실행한다
        if (!validate()) {   // 검증을 통과하지 못했으면
            return false;   // false 를 돌려주어 전송을 막는다
        }
        // form — joinForm( ) 의 결과를 담는다
        var form = joinForm();   // 가입 폼을 찾는다
        // 조건을 확인해 맞을 때만 아래를 실행한다
        if (form) {   // 폼이 있으면
            //DOM 의 submit() 은 submit 이벤트를 발생시키지 않으므로 중복 검증이 일어나지 않는다
            form.submit();
        }
        return true;   // true 를 돌려주어 정상 처리되었음을 알린다
    };
    /** 폼이 직접 전송될 때도 검증을 거치게 한다 */
    document.addEventListener("DOMContentLoaded", function () {
        // form — joinForm( ) 의 결과를 담는다
        var form = joinForm();   // 가입 폼을 찾는다
        // 조건을 확인해 맞을 때만 아래를 실행한다
        if (!form) { return; }   // 이 화면에 폼이 없으면 아무것도 하지 않는다
        // "submit" 사건이 일어나면 실행할 동작을 걸어 둔다
        form.addEventListener("submit", function (event) {   // 폼이 전송되려 할 때 실행할 동작을 등록한다
            // 조건을 확인해 맞을 때만 아래를 실행한다
            if (!validate()) {   // 검증을 통과하지 못했으면
                // event 의 preventDefault( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
                event.preventDefault(); //검증 실패 -> 전송 차단
            }
        });
    });
})();   // 이 괄호가 위에서 시작한 함수를 "바로 실행" 시킨다. 안의 변수들이 밖으로 새지 않게 감싼 것이다