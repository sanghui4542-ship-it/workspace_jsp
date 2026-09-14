/* ============================================================================
   app.js  -  모든 화면에서 함께 쓰는 공용 자바스크립트 (외부 라이브러리 없음)

   CarMain.jsp 의 <head> 에서 딱 1번 불러온다 (defer : HTML 을 다 읽은 뒤 실행)
   전역 이름은 CarApp 하나만 만든다. 기능은 모두 CarApp.기능이름() 으로 쓴다.

   기능 목록
     CarApp.id("아이디")                   : id 로 태그 1개 찾기
     CarApp.val("아이디")                  : 입력칸의 값 얻기 (없으면 "")
     CarApp.setText("아이디", 글자, 색)     : 태그에 글자 넣기 + 글자 색 바꾸기
     CarApp.setHtml("아이디", HTML, 색)     : 태그에 HTML 넣기 + 글자 색 바꾸기
     CarApp.setDisabled([아이디들], true)  : 여러 태그를 한 번에 누를 수 없게/있게
     CarApp.setVisible([아이디들], true)   : 여러 태그를 한 번에 보이게/숨기게
     CarApp.postForm(주소, {이름:값})       : 서버에 POST 요청을 보내고 응답 글자 받기 (fetch 사용)

   화면이 열리면 자동으로 실행되는 기능
     접기/펼치기 : data-toggle="collapse" 버튼을 화면 전체에서 찾아 연결한다
       예1) Top.jsp 의 모바일 메뉴 버튼 - data-target="#mainNavMenu"
       예2) join.jsp 의 약관 내용 버튼   - data-target="#collapseOne"
   ============================================================================ */

// 함수를 만들자마자 바로 실행한다 (안에서 만든 변수가 다른 js 파일의 변수와 섞이지 않게 감싼다)
(function (window, document) {

    // 엄격 모드 : 선언하지 않은 변수 사용 같은 실수를 브라우저가 오류로 알려 준다
    "use strict";

    // 공용 기능을 모아 둘 객체 (맨 아래에서 window.CarApp 으로 내보낸다)
    var CarApp = {};


    /* ------------------------------------------------------------------------
       1. 태그 찾기, 값 읽기, 글자 넣기
       ------------------------------------------------------------------------ */

    // id 로 태그 1개를 찾아 반환한다. 없으면 null
    CarApp.id = function (elementId) {
        return document.getElementById(elementId);
    };

    // CSS 선택자로 태그 1개를 찾아 반환한다 (root 를 주면 그 안에서만 찾는다)
    CarApp.one = function (selector, root) {
        return (root || document).querySelector(selector);
    };

    // CSS 선택자로 태그 여러 개를 찾아 배열로 반환한다 (배열이어야 forEach 를 쓸 수 있다)
    CarApp.all = function (selector, root) {
        return Array.prototype.slice.call((root || document).querySelectorAll(selector));
    };

    // 입력칸의 값을 반환한다. 입력칸이 없으면 오류 대신 빈 글자("")를 반환
    CarApp.val = function (elementId) {
        var el = CarApp.id(elementId);
        return el ? el.value : "";
    };

    // 태그에 글자를 넣고, 색을 주면 글자 색도 바꾼다 (textContent : 태그를 실행하지 않고 글자로만 넣는다)
    CarApp.setText = function (elementId, text, color) {

        // 태그를 찾고, 없으면 아무것도 하지 않는다
        var el = CarApp.id(elementId);
        if (!el) { return; }

        // 글자를 넣고, 색이 있으면 글자 색을 바꾼다
        el.textContent = text;
        if (color) { el.style.color = color; }
    };

    // 태그에 HTML 을 넣고, 색을 주면 글자 색도 바꾼다
    // 주의 : innerHTML 은 태그를 실제로 실행하므로 고객이 입력한 값을 넣으면 안 된다 (서버가 만든 고정 문구에만 사용)
    CarApp.setHtml = function (elementId, html, color) {

        // 태그를 찾고, 없으면 아무것도 하지 않는다
        var el = CarApp.id(elementId);
        if (!el) { return; }

        // HTML 을 넣고, 색이 있으면 글자 색을 바꾼다
        el.innerHTML = html;
        if (color) { el.style.color = color; }
    };

    // 여러 태그를 한 번에 누를 수 없게(true) 또는 누를 수 있게(false) 바꾼다
    // 예) CarApp.setDisabled(["btnSave", "btnDelete"], true);
    CarApp.setDisabled = function (ids, disabled) {
        ids.forEach(function (elementId) {
            var el = CarApp.id(elementId);
            if (el) { el.disabled = disabled; }
        });
    };

    // 여러 태그를 한 번에 보이게(true) 또는 숨기게(false) 바꾼다
    // visibility 로 숨기므로 자리는 그대로 남아 화면 배치가 흔들리지 않는다
    CarApp.setVisible = function (ids, visible) {
        ids.forEach(function (elementId) {
            var el = CarApp.id(elementId);
            if (el) { el.style.visibility = visible ? "visible" : "hidden"; }
        });
    };


    /* ------------------------------------------------------------------------
       2. 서버 통신  -  CarApp.postForm(주소, 보낼 값)

       사용 예)
         CarApp.postForm(contextPath + "/member/memberDelete.me", {})
             .then(function (result) { ... 서버가 보낸 글자(result) 사용 ... })
             .catch(function (err)   { ... err.message 로 실패 이유 안내 ... });

       보낸 값은 "이름=값&이름=값" 형식이라 서블릿에서 request.getParameter("이름") 으로 꺼낸다
       ------------------------------------------------------------------------ */
    CarApp.postForm = function (url, data) {

        // 서버로 보낼 값을 담을 상자 (한글도 알아서 주소용 글자로 바꿔 준다)
        var body = new URLSearchParams();

        // 넘겨받은 {이름:값} 들을 하나씩 상자에 담는다 (값이 undefined, null 이면 빈 글자로)
        Object.keys(data || {}).forEach(function (key) {
            var value = data[key];
            body.append(key, (value === undefined || value === null) ? "" : value);
        });

        // fetch 로 서버에 POST 요청을 보낸다
        return fetch(url, {
            method: "POST",
            headers: {
                // 요청 본문이 "이름=값&이름=값" 형식이라고 알린다
                "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
                // 비동기 요청이라고 알린다 -> 오류가 나면 서버(BaseController)가 에러 화면 대신 짧은 글자로 답한다
                "X-Requested-With": "XMLHttpRequest"
            },
            body: body.toString(),
            // 로그인 정보(세션 쿠키)를 함께 보낸다
            credentials: "same-origin"

        // 서버 응답을 받으면
        }).then(function (response) {

            // 403 (로그인 필요, 권한 없음) : 서버가 보낸 안내 문구를 실패 이유로 넘긴다  예) "로그인이 필요한 기능입니다"
            if (response.status === 403) {
                return response.text().then(function (message) {
                    return Promise.reject(new Error(message.trim()));
                });
            }

            // 그 밖에 정상(200번대)이 아닌 경우 : 상태 번호를 담아 실패로 넘긴다
            if (!response.ok) {
                return Promise.reject(new Error("서버 오류 (" + response.status + ")"));
            }

            // 정상이면 응답 본문을 글자로 읽는다
            return response.text();

        // 읽은 글자의 앞뒤 공백을 지워서 넘긴다 (비교하기 쉽게)
        }).then(function (text) {
            return text.trim();
        });
    };


    /* ------------------------------------------------------------------------
       3. 접기/펼치기  -  data-toggle="collapse" 버튼을 누르면 data-target 의 영역을 열고 닫는다

       HTML 예)
         <button data-toggle="collapse" data-target="#collapseOne">약관동의 내용1</button>
         <div id="collapseOne" class="collapse"> 약관 내용 </div>

       .collapse 는 숨김, .collapse.show 는 보임 (css/app.css 규칙)
       ------------------------------------------------------------------------ */
    CarApp.initCollapse = function () {

        // data-toggle="collapse" 버튼을 모두 찾아 하나씩 클릭 이벤트를 연결한다
        CarApp.all('[data-toggle="collapse"]').forEach(function (trigger) {
            trigger.addEventListener("click", function (event) {

                // 버튼(또는 링크)의 원래 동작(주소 이동 등)을 막는다
                event.preventDefault();

                // 열고 닫을 영역의 선택자를 꺼낸다 (data-target, 없으면 href)  예) "#collapseOne"
                var selector = trigger.getAttribute("data-target") || trigger.getAttribute("href");
                if (!selector) { return; }

                // 그 영역을 찾는다. 없으면 끝낸다
                var target = CarApp.one(selector);
                if (!target) { return; }

                // show class 를 붙였다 뗐다 한다 (toggle : 없으면 붙이고 true, 있으면 떼고 false)
                var isOpen = target.classList.toggle("show");

                // 화면 읽기 프로그램에 열림(true)/닫힘(false) 상태를 알린다
                trigger.setAttribute("aria-expanded", isOpen ? "true" : "false");
            });
        });
    };


    /* ------------------------------------------------------------------------
       4. 화면(HTML)을 다 읽으면 접기/펼치기 기능을 켠다
       ------------------------------------------------------------------------ */
    document.addEventListener("DOMContentLoaded", function () {
        CarApp.initCollapse();
    });

    // 다른 화면의 스크립트가 CarApp.기능() 으로 쓸 수 있도록 전역(window)에 내보낸다
    window.CarApp = CarApp;

})(window, document);
