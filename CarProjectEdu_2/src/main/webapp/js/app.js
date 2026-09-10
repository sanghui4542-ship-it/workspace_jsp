/* ==============================================================================
   app.js  -  CarProject 공용 자바스크립트 (순수 바닐라 / 외부 라이브러리 없음)
   ------------------------------------------------------------------------------
   [이 파일을 만든 이유]
     기존 상태
       - jQuery 를 CDN 에서 불러 쓰고 있었다
           Top.jsp        : jquery.slim (AJAX 미지원 버전) + bootstrap.bundle
           board/read.jsp : jquery 3.7.1 을 또 로드  ->  같은 화면에서 jQuery 2중 로드
       - 그래서 사내망이나 오프라인 강의실에서는 화면과 기능이 모두 깨졌다
       - jquery.slim 은 $.ajax 가 없는데 게시판이 $.ajax 를 쓰고 있었다
     바뀐 방향
       - 외부 요청 0건. 필요한 기능만 직접 만들어 쓴다
       - jQuery 를 흉내내지 않는다. 이 프로젝트가 실제로 쓰는 기능만 담는다
   ------------------------------------------------------------------------------
   [jQuery -> 바닐라 대응표]  (수업 자료용)
     jQuery                                  순수 자바스크립트
     ---------------------------------------------------------------------------
     $("#id")                                document.getElementById("id")
     $("#id").val()                          el.value
     $("#id").val("x")                       el.value = "x"
     $("#id").text("x")                      el.textContent = "x"
     $("#id").html("<b>x</b>")               el.innerHTML = "<b>x</b>"
     $("#id").css("color","red")             el.style.color = "red"
     $("#id").on("click", fn)                el.addEventListener("click", fn)
     $("#id").focus()                        el.focus()
     $("#id").submit()                       form.submit()
     $.ajax({url, type, data, success})      fetch(url, {...}).then(...)
     핵심 : jQuery 가 없어도 브라우저가 이미 같은 기능을 표준으로 제공한다.
            2006년에는 브라우저마다 동작이 달라 jQuery 가 필요했지만
            지금은 표준 API 만으로 충분하다.
   ============================================================================== */
(function (window, document) {
    "use strict";   // "엄격 모드". 오타로 만든 변수 같은 흔한 실수를 브라우저가 오류로 잡아 준다
    /* 전역에 하나의 이름만 노출한다 (전역 변수를 여러 개 만들면 서로 충돌한다) */
    var CarApp = {};
    /* ==========================================================================
       1. DOM 선택 도우미
       ========================================================================== */
    /** id 로 요소 하나 찾기.  $("#pass") 대신 CarApp.id("pass") */
    CarApp.id = function (elementId) {
        return document.getElementById(elementId);   // id 로 요소 하나를 찾아 돌려준다. 못 찾으면 null 이다
    };
    /** CSS 선택자로 요소 하나 찾기 */
    CarApp.one = function (selector, root) {
        // return( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
        return (root || document).querySelector(selector);   // CSS 선택자로 첫 번째 요소를 찾는다. root 를 주면 그 안에서만 찾는다
    };
    /** CSS 선택자로 요소 여러 개 찾기 (배열로 반환해 forEach 를 쓸 수 있게) */
    CarApp.all = function (selector, root) {
        return Array.prototype.slice.call((root || document).querySelectorAll(selector));   // 찾은 결과는 배열이 아니라 NodeList 라서 forEach 를 못 쓴다. slice 로 진짜 배열로 바꾼다
    };
    /** 요소의 값 얻기 (없으면 빈 문자열 - null 참조 오류 방지) */
    CarApp.val = function (elementId) {
        // el — id( ) 의 결과를 담는다
        var el = CarApp.id(elementId);   // 먼저 요소를 찾는다
        return el ? el.value : "";   // 요소가 있으면 값을, 없으면 빈 문자열을 돌려준다 (null 참조 오류를 막는다)
    };
    /** 요소에 텍스트 넣기 + 색 지정 (게시판 안내 메시지에 자주 쓰인다) */
    CarApp.setText = function (elementId, text, color) {
        // el — id( ) 의 결과를 담는다
        var el = CarApp.id(elementId);   // 먼저 요소를 찾는다
        // 조건을 확인해 맞을 때만 아래를 실행한다
        if (!el) { return; }   // 요소가 없으면 아무것도 하지 않고 끝낸다
        // 요소 안의 글자를 바꾼다
        el.textContent = text;   // 글자를 넣는다. textContent 는 태그를 글자로만 취급해 XSS 가 생기지 않는다
        // 조건을 확인해 맞을 때만 아래를 실행한다
        if (color) { el.style.color = color; }   // 색을 지정했으면 글자색도 바꾼다
    };
    /** 요소에 HTML 넣기 + 색 지정
        [주의] 사용자 입력을 그대로 넣으면 XSS 가 된다.
               서버가 만든 고정 문구에만 사용한다. */
    CarApp.setHtml = function (elementId, html, color) {
        // el — id( ) 의 결과를 담는다
        var el = CarApp.id(elementId);   // 먼저 요소를 찾는다
        // 조건을 확인해 맞을 때만 아래를 실행한다
        if (!el) { return; }   // 요소가 없으면 아무것도 하지 않고 끝낸다
        // 요소 안의 내용을 통째로 갈아끼운다
        el.innerHTML = html;   // HTML 을 넣는다. 태그가 실제로 해석되므로 사용자 입력에는 절대 쓰면 안 된다
        // 조건을 확인해 맞을 때만 아래를 실행한다
        if (color) { el.style.color = color; }   // 색을 지정했으면 글자색도 바꾼다
    };
    /** 여러 요소의 disabled 상태를 한 번에 바꾼다 */
    CarApp.setDisabled = function (ids, disabled) {
        // ids 의 forEach( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
        ids.forEach(function (elementId) {   // 넘겨받은 id 들을 하나씩 처리한다
            // el — id( ) 의 결과를 담는다
            var el = CarApp.id(elementId);   // 그 id 의 요소를 찾는다
            // 조건을 확인해 맞을 때만 아래를 실행한다
            if (el) { el.disabled = disabled; }   // 요소가 있으면 눌림 가능/불가 상태를 바꾼다
        });
    };
    /** 요소 보이기/숨기기 (visibility 사용 - 자리를 유지해야 레이아웃이 안 흔들린다) */
    CarApp.setVisible = function (ids, visible) {
        // ids 의 forEach( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
        ids.forEach(function (elementId) {   // 넘겨받은 id 들을 하나씩 처리한다
            // el — id( ) 의 결과를 담는다
            var el = CarApp.id(elementId);   // 그 id 의 요소를 찾는다
            // 조건을 확인해 맞을 때만 아래를 실행한다
            if (el) { el.style.visibility = visible ? "visible" : "hidden"; }   // visible 이면 보이게, 아니면 숨긴다. display 가 아니라 visibility 라서 자리는 그대로 남는다
        });
    };
    /* ==========================================================================
       2. 서버 통신 ($.ajax 대체)
       ========================================================================== */
    /**
     * 폼 데이터를 POST 로 보내고 응답 텍스트를 받는다.
     *
     *   (기존 jQuery)
     *     $.ajax({
     *         url: "...", type: "post", dataType: "text",
     *         data: { b_idx: 1, pass: "1234" },
     *         success: function(data){ ... },
     *         error: function(){ ... }
     *     });
     *
     *   (지금)
     *     CarApp.postForm("...", { b_idx: 1, pass: "1234" })
     *           .then(function(text){ ... })
     *           .catch(function(err){ ... });
     *
     * [왜 URLSearchParams 를 쓰는가]
     *   서버(서블릿)는 request.getParameter() 로 값을 읽는다.
     *   그 방식은 "application/x-www-form-urlencoded" 형식을 기대하므로
     *   JSON 으로 보내면 파라미터를 읽지 못한다.
     *   URLSearchParams 가 한글 인코딩까지 알아서 처리해준다.
     *
     * @param {string} url  요청 주소
     * @param {object} data 보낼 값들 (키-값 객체)
     * @returns {Promise<string>} 서버 응답 텍스트
     */
    CarApp.postForm = function (url, data) {
        // URLSearchParams 그릇을 새로 하나 만들어 body 라는 이름으로 잡아 둔다
        var body = new URLSearchParams();   // 보낼 값들을 담을 상자를 만든다. 한글 인코딩을 알아서 처리해 준다
        // Object 의 keys( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
        Object.keys(data || {}).forEach(function (key) {   // 넘겨받은 값들의 이름을 하나씩 훑는다.  data || {} 는 값이 없을 때 빈 객체로 대신하는 방어다
            // value — 계산한 값을 담는다
            var value = data[key];   // 그 이름에 해당하는 값을 꺼낸다
            //undefined / null 은 "undefined" 라는 글자로 전송되지 않도록 빈 값으로 바꾼다
            body.append(key, (value === undefined || value === null) ? "" : value);
        });
        //CSRF 토큰이 화면에 있으면 자동으로 함께 보낸다 (모든 AJAX 호출에 일일이 넣지 않도록)
        var token = CarApp.csrfToken();
        // 조건을 확인해 맞을 때만 아래를 실행한다
        if (token && !body.has("_csrf")) {   // 토큰이 있고 아직 안 담았으면
            // 문자열 body 뒤에 이어 붙인다
            body.append("_csrf", token);   // 함께 보낼 값에 토큰을 추가한다
        }
        return fetch(url, {   // fetch = 브라우저가 서버에 요청을 보내는 표준 기능 ($.ajax 를 대신한다)
            method: "POST",   // POST 방식으로 보낸다
            headers: {
                "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",   // 본문이 "이름=값&이름=값" 형식이라고 알린다. 서블릿의 getParameter 가 이 형식을 읽는다
                /* 서버(AuthFilter)가 "화면 요청"과 "비동기 요청"을 구분하는 데 사용한다.
                   비동기 요청에 로그인 화면 HTML 을 돌려주면 화면에 깨진 글자가 찍히므로,
                   서버는 이 헤더를 보고 401 상태코드로만 답한다. */
                "X-Requested-With": "XMLHttpRequest"
            },
            body: body.toString(),
            /* 세션 쿠키를 함께 보낸다 (같은 사이트 요청이므로 기본값이지만 명시한다) */
            credentials: "same-origin"
        }).then(function (response) {
            // 조건을 확인해 맞을 때만 아래를 실행한다
            if (response.status === 401) {   // 401 = 로그인이 필요하다는 뜻
                //AuthFilter 가 로그인 필요를 알린 경우
                return Promise.reject(new Error("로그인이 필요합니다"));
            }
            // 조건을 확인해 맞을 때만 아래를 실행한다
            if (response.status === 403) {   // 403 = 보안 토큰이 맞지 않는다는 뜻
                /* CsrfFilter 가 토큰 불일치로 막은 경우.
                   화면을 오래 열어두어 세션이 만료되면 여기로 온다.
                   "서버 오류 403" 이라고만 알려주면 사용자가 무엇을 해야 할지 모르므로
                   해야 할 행동(새로 고침)을 그대로 말해준다. */
                return Promise.reject(
                    new Error("보안 토큰이 만료되었습니다. 화면을 새로 고친 뒤 다시 시도해주세요."));
            }
            // 조건을 확인해 맞을 때만 아래를 실행한다
            if (!response.ok) {   // 그 밖에 200번대가 아닌 모든 경우
                return Promise.reject(new Error("서버 오류 (" + response.status + ")"));   // 무슨 코드로 실패했는지 담아 실패로 처리한다
            }
            return response.text();   // 여기까지 왔으면 정상이다. 응답 본문을 글자로 읽는다
        }).then(function (text) {   // 읽은 글자를 한 번 더 다듬는다
            //서버가 앞뒤 공백을 붙여 보내는 경우가 있어 비교하기 쉽게 정리해서 넘긴다
            return text.trim();
        });
    };
    /** 화면에 심어진 CSRF 토큰 값을 찾아 반환 (없으면 null) */
    CarApp.csrfToken = function () {
        // 화면에서 "meta[name=" 에 해당하는 요소를 찾는다
        var meta = document.querySelector('meta[name="_csrf"]');   // 화면 위쪽 meta 태그에서 토큰을 찾는다
        // 조건을 확인해 맞을 때만 아래를 실행한다
        if (meta && meta.content) { return meta.content; }   // 찾았으면 그 값을 돌려준다
        // 화면에서 "input[name=" 에 해당하는 요소를 찾는다
        var input = document.querySelector('input[name="_csrf"]');   // meta 에 없으면 폼 안의 숨은 input 에서 찾는다
        // 조건을 확인해 맞을 때만 아래를 실행한다
        if (input && input.value) { return input.value; }   // 찾았으면 그 값을 돌려준다
        return null;   // 둘 다 없으면 토큰이 없다는 뜻으로 null 을 돌려준다
    };
    /* ==========================================================================
       3. 접기/펼치기 (Bootstrap collapse 대체)
       ========================================================================== */
    /*
       Top.jsp 와 join.jsp 의 내비게이션이 아래 방식을 쓰고 있었다.
           <button data-toggle="collapse" data-target="#menu">메뉴</button>
           <div id="menu" class="collapse"> ... </div>
       이 동작은 Bootstrap 의 JS 플러그인이 처리했다.
       실제로 하는 일은 "대상 요소에 show 클래스를 붙이거나 떼는 것"뿐이므로
       아래 몇 줄로 대체할 수 있다. (Bootstrap CSS 의 .collapse / .show 규칙을 그대로 활용)
    */
    CarApp.initCollapse = function () {
        // CarApp 의 all( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
        CarApp.all('[data-toggle="collapse"]').forEach(function (trigger) {   // data-toggle="collapse" 가 붙은 버튼을 전부 찾아 하나씩 처리한다
            // "click" 사건이 일어나면 실행할 동작을 걸어 둔다
            trigger.addEventListener("click", function (event) {   // 그 버튼을 클릭했을 때 실행할 동작을 등록한다
                // event 의 preventDefault( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
                event.preventDefault();   // a 태그의 기본 동작(주소 이동)을 막는다. 우리는 펼치기만 하면 된다
                // selector — getAttribute( ) 의 결과를 담는다
                var selector = trigger.getAttribute("data-target") || trigger.getAttribute("href");   // 어느 요소를 펼칠지 적혀 있는 값을 읽는다 (data-target 이 없으면 href 를 본다)
                // 조건을 확인해 맞을 때만 아래를 실행한다
                if (!selector) { return; }   // 대상이 안 적혀 있으면 아무것도 하지 않는다
                // target — one( ) 의 결과를 담는다
                var target = CarApp.one(selector);   // 그 선택자로 실제 요소를 찾는다
                // 조건을 확인해 맞을 때만 아래를 실행한다
                if (!target) { return; }   // 요소가 없으면 아무것도 하지 않는다
                // 요소에 class 를 붙였다 뗐다 한다 (모양 전환용)
                var isOpen = target.classList.toggle("show");   // show 클래스를 붙였다 뗐다 한다. 붙었으면 true 가 돌아온다
                //[접근성] 스크린리더에 열림/닫힘 상태를 알려준다
                trigger.setAttribute("aria-expanded", isOpen ? "true" : "false");
            });
        });
    };
    /* ==========================================================================
       4. 모바일 서랍 메뉴 (app.css 의 .main-nav 와 함께 동작)
       ========================================================================== */
    CarApp.initDrawer = function () {
        // toggle — one( ) 의 결과를 담는다
        var toggle  = CarApp.one(".nav-toggle");   // 햄버거 버튼
        // nav — one( ) 의 결과를 담는다
        var nav     = CarApp.one(".main-nav");   // 펼쳐질 메뉴
        // overlay — one( ) 의 결과를 담는다
        var overlay = CarApp.one(".nav-overlay");   // 메뉴 뒤를 덮는 반투명 막
        // 조건을 확인해 맞을 때만 아래를 실행한다
        if (!toggle || !nav) { return; }   // 버튼이나 메뉴가 없는 화면이면 아무것도 하지 않는다
        // open( ) — 아래 일을 하는 함수를 만든다
        function open()  {   // 메뉴를 여는 동작
            // 요소에 class 를 붙인다 (모양 전환용)
            nav.classList.add("is-open");   // 메뉴에 is-open 클래스를 붙인다 (CSS 가 이 클래스를 보고 밀어 넣는다)
            // 조건을 확인해 맞을 때만 아래를 실행한다
            if (overlay) { overlay.classList.add("is-open"); }   // 덮는 막이 있으면 함께 보이게 한다
            // toggle 의 setAttribute( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
            toggle.setAttribute("aria-expanded", "true");   // 스크린리더에 "열렸다" 고 알린다
        }
        // close( ) — 아래 일을 하는 함수를 만든다
        function close() {   // 메뉴를 닫는 동작
            // 요소에 class 를 뗀다 (모양 전환용)
            nav.classList.remove("is-open");   // 메뉴에서 is-open 클래스를 뗀다
            // 조건을 확인해 맞을 때만 아래를 실행한다
            if (overlay) { overlay.classList.remove("is-open"); }   // 덮는 막도 함께 숨긴다
            // toggle 의 setAttribute( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
            toggle.setAttribute("aria-expanded", "false");   // 스크린리더에 "닫혔다" 고 알린다
        }
        // "click" 사건이 일어나면 실행할 동작을 걸어 둔다
        toggle.addEventListener("click", function () {   // 햄버거 버튼을 클릭했을 때 실행할 동작을 등록한다
            // 조건을 확인해 맞을 때만 아래를 실행한다
            if (nav.classList.contains("is-open")) { close(); } else { open(); }   // 지금 열려 있으면 닫고, 닫혀 있으면 연다 (토글)
        });
        // 조건을 확인해 맞을 때만 아래를 실행한다
        if (overlay) { overlay.addEventListener("click", close); }   // 덮는 막을 클릭해도 닫히게 한다
        //ESC 키로 닫기 (키보드 사용자를 위한 기본 예의)
        document.addEventListener("keydown", function (e) {
            // 조건을 확인해 맞을 때만 아래를 실행한다
            if (e.key === "Escape") { close(); }   // 누른 키가 ESC 면 메뉴를 닫는다
        });
    };
    /* ==========================================================================
       5. 확인 후 이동 (삭제 버튼 등에 사용)
       ========================================================================== */
    CarApp.confirmGo = function (message, url) {
        // 조건을 확인해 맞을 때만 아래를 실행한다
        if (window.confirm(message)) {   // 확인창을 띄운다. "확인" 을 누르면 true 가 돌아온다
            // 브라우저 주소를 바꿔 그 화면으로 이동한다
            location.href = url;   // 지정한 주소로 이동한다
        }
        return false;   // false 를 돌려주면 a 태그의 기본 이동이 취소된다
    };
    /* ==========================================================================
       6. 시작
       ========================================================================== */
    /*
       DOMContentLoaded : HTML 을 다 읽은 시점에 실행한다.
         이 시점이면 화면의 요소들이 모두 존재하므로 안전하게 이벤트를 붙일 수 있다.
         (jQuery 의 $(document).ready() 와 같은 역할)
    */
    document.addEventListener("DOMContentLoaded", function () {
        // CarApp 의 initCollapse( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
        CarApp.initCollapse();   // 접기/펼치기 버튼들에 동작을 붙인다
        // CarApp 의 initDrawer( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
        CarApp.initDrawer();   // 모바일 서랍 메뉴에 동작을 붙인다
    });
    //다른 파일(JSP 안의 script)에서 쓸 수 있도록 전역에 등록
    window.CarApp = CarApp;
})(window, document);   // 이 괄호가 위에서 시작한 함수를 "바로 실행" 시킨다. 안의 변수들이 밖으로 새지 않게 감싼 것이다