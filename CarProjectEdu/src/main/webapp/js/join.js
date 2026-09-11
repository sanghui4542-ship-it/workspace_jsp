/* ==============================================================================
   join.js  -  회원가입 화면(members/join.jsp) 입력 검증

   [읽는 방법]  위에서 아래로 한 번만 읽으면 된다.

     0. 안내 문구 함수 (딱 1개)
     1. 약관      -> checkAgree()
     2. 아이디    -> checkId()  + 서버 중복확인 checkIdDuplicate()
     3. 비밀번호  -> checkPass()
     4. 이름      -> checkName()
     5. 나이      -> checkAge()
     6. 주소      -> checkAddress()
     7. 성별      -> checkGender()
     8. 이메일    -> checkEmail()
     9. 전화번호  -> checkTel()
    10. 휴대폰    -> checkHp()
    11. 가입 버튼 -> check()  : 1~10 을 순서대로 부른다

   [모든 check 함수의 약속]
     통과하면  : 파란 글씨 안내 + true  돌려줌
     실패하면  : 빨간 글씨 안내 + false 돌려줌

   [흐름]
     입력칸을 벗어남(blur) ---> checkXxx() ---> 파랑 / 빨강 안내

     가입 버튼 클릭 ---> check()
                          |
                          +-- checkAgree()  false 면 멈춤
                          +-- checkId()     false 면 멈춤 + 커서 이동
                          +-- ...
                          +-- 모두 true ---> 폼 전송

   [참고] 이 파일은 join.jsp 의 </body> 바로 앞에서 불러야 한다.
          입력칸이 먼저 만들어져 있어야 이벤트를 붙일 수 있다.
   ============================================================================== */


/* ==============================================================================
   0. 안내 문구 함수
   ============================================================================== */

/* showMsg 라는 이름의 함수를 만든다.
   괄호 안 3개는 부를 때 넘겨받는 값이다.
     id    : 안내 문구를 넣을 자리의 id        예) "idInput"
     text  : 보여 줄 안내 문구                 예) "사용할 수 있는 ID입니다."
     color : 글자 색                           예) "blue" 또는 "red"
   사용 예) showMsg("idInput", "사용할 수 있는 ID입니다.", "blue"); */
function showMsg(id, text, color) {

    /* 안내 문구 자리 <span id="idInput"></span> 같은 태그를 id 로 찾아 box 에 저장 */
    const box = document.getElementById(id);

    /* 찾은 자리 <span> 안에 안내 문구(text)를 글자로 넣는다
       결과 예) <span id="idInput">사용할 수 있는 ID입니다.</span> */
    box.textContent = text;

    /* 찾은 자리의 글자 색을 바꾼다 (빨강 = 잘못 입력, 파랑 = 올바르게 입력)
       결과 예) <span id="idInput" style="color: blue;"> */
    box.style.color = color;
}


/* ==============================================================================
   1. 약관 동의
   ============================================================================== */

/* checkAgree 라는 이름의 함수를 만든다. 약관 체크박스가 체크됐는지 검사한다
   체크됨 -> true 돌려줌 / 체크 안 됨 -> false 돌려줌 */
function checkAgree() {

    /* 약관 동의 체크박스 <input type="checkbox" id="agree"> 를 찾아 agree 에 저장 */
    const agree = document.getElementById("agree");

    /* agree.checked 는 체크되어 있으면 true, 아니면 false 이다
       체크되어 있다면 아래 { } 안을 실행한다 */
    if (agree.checked) {

        /* 약관 안내 자리 <span id="agreeInput"> 에 파란 글씨로 "약관동의 완료!" 표시 */
        showMsg("agreeInput", "약관동의 완료!", "blue");

        /* 검사 통과라는 뜻으로 true 를 돌려주고 함수를 끝낸다 */
        return true;
    }

    /* 여기까지 왔다면 체크가 안 된 것이다
       약관 안내 자리 <span id="agreeInput"> 에 빨간 글씨로 안내 표시 */
    showMsg("agreeInput", "약관에 동의해 주세요!", "red");

    /* 검사 실패라는 뜻으로 false 를 돌려주고 함수를 끝낸다 */
    return false;
}

/* 약관 체크박스 <input type="checkbox" id="agree"> 를 찾아서
   "클릭(click)할 때마다 checkAgree 함수를 실행하라" 고 등록한다
   주의 : checkAgree() 가 아니라 checkAgree 로 쓴다. ( ) 를 붙이면 지금 바로 실행돼 버린다 */
document.getElementById("agree").addEventListener("click", checkAgree);


/* ==============================================================================
   2. 아이디  (형식 검사 + 서버 중복 확인)
   ============================================================================== */

   
   
/* checkId 라는 이름의 함수를 만든다. 아이디가 규칙(영문/숫자/_/- 3~20자)에 맞는지 검사한다 */
function checkId() {

    /* 아이디 입력 <input type="text" id="id" name="id"> 에 입력한 값 얻어 id 에 저장 */
    const id = document.getElementById("id").value;

    /* /^[A-Za-z0-9_\-]{3,20}$/ 는 정규식(글자 모양 규칙)이다
         ^            : 처음부터
         [A-Za-z0-9_\-] : 영문 대문자, 소문자, 숫자, 밑줄(_), 빼기(-) 중 하나
         {3,20}       : 위 글자가 3개 이상 20개 이하
         $            : 끝까지
       .test(id) 는 id 가 규칙에 맞으면 true, 틀리면 false 를 돌려준다 */
    if (/^[A-Za-z0-9_\-]{3,20}$/.test(id)) {

        /* 형식 통과. 파란 안내는 서버 중복확인 결과를 보고 따로 표시하므로 여기서는 true 만 돌려준다 */
        return true;
    }

    /* 여기까지 왔다면 규칙에 맞지 않은 것이다
       아이디 안내 자리 <span id="idInput"> 에 빨간 글씨로 안내 표시 */
    showMsg("idInput", "영문, 숫자, _, - 로 3~20글자 작성해 주세요!", "red");

    /* 검사 실패 */
    return false;
}

/* checkIdDuplicate 라는 이름의 함수를 만든다. 서버에 "이 아이디 이미 있나요?" 를 물어본다

     브라우저 ---- POST id=hong ----> 서버 joinIdCheck.me
     브라우저 <--- "usable"(사용가능) 또는 "not_usable"(중복) ----

   async : 이 함수 안에서 await(기다리기) 를 쓰겠다는 표시다.
           서버 응답은 시간이 걸리므로 기다렸다가 다음 줄을 실행해야 한다. */
async function checkIdDuplicate() {

    /* 요청을 보낼 서버 주소를 만들어 url 에 저장
       window.CONTEXT_PATH 는 join.jsp 가 알려 준 프로젝트 이름이다  예) "/CarProject"
       결과 예) "/CarProjectEdu/member/joinIdCheck.me" */
    const url = window.CONTEXT_PATH + "/member/joinIdCheck.me";

    /* 서버로 보낼 데이터를 담을 빈 상자를 만들어 params 에 저장
       URLSearchParams 는 "이름=값" 형태(폼 전송 형식)로 데이터를 만들어 준다 */
    const params = new URLSearchParams();

    /* 아이디 입력 <input type="text" id="id" name="id"> 에 입력한 값을 꺼내
       "id" 라는 이름으로 상자에 넣는다  결과 예) id=hong
       서버(서블릿)에서는 request.getParameter("id") 로 이 값을 꺼낸다 */
    params.append("id", document.getElementById("id").value);

    /* try { } : 이 안에서 오류가 날 수 있으니 감시한다. 오류가 나면 아래 catch { } 로 간다 */
    try {

        /* fetch 로 서버에 요청을 보낸다
             url                : 보낼 주소
             method: "POST"     : 전송 방식 (폼의 method="post" 와 같다)
             body: params       : 보낼 데이터 (id=hong)
           await : 서버 응답이 올 때까지 기다렸다가, 온 응답을 response 에 저장 */
        const response = await fetch(url, { method: "POST", body: params });

        /* response.ok 는 서버가 정상 응답(200번대)이면 true 이다
           !response.ok 는 "정상이 아니면" 이라는 뜻 (404 주소 없음, 500 서버 오류 등)
           fetch 는 404/500 이어도 스스로 오류를 내지 않으므로 직접 확인해야 한다 */
        if (!response.ok) {

            /* 오류를 일부러 발생시킨다. 그러면 바로 아래 catch { } 로 넘어간다 */
            throw new Error("서버 오류 " + response.status);
        }

        /* 서버가 보낸 응답 내용을 글자로 읽는다 (await : 다 읽을 때까지 기다린다)
           .trim() 으로 앞뒤 공백·줄바꿈을 지운 뒤 result 에 저장
           결과 예) "usable" 또는 "not_usable" */
        const result = (await response.text()).trim();
								//	"not_usable"
								//	"usable"
									
   		 /* 서버가 "usable"(아이디 사용 가능) 이라고 답했다면 */
        if (result === "usable") {

            /* 아이디 안내 자리 <span id="idInput"> 에 파란 글씨로 안내 표시 */
            showMsg("idInput", "사용할 수 있는 ID입니다.", "blue");

        /* 그 외("not_usable") 라면 이미 누군가 쓰고 있는 아이디다(아이디 중복) */
        } else {

            /* 아이디 안내 자리 <span id="idInput"> 에 빨간 글씨로 안내 표시 */
            showMsg("idInput", "이미 사용중인 ID입니다.", "red");
        }

    /* catch { } : 위 try 안에서 오류가 나면 여기로 온다
       (서버가 꺼졌거나, 주소가 틀렸거나, 인터넷이 끊긴 경우)
       error 에는 무슨 오류인지 정보가 담겨 있다 */
    } catch (error) {

        /* 아이디 안내 자리 <span id="idInput"> 에 빨간 글씨로 오류 안내 표시 */
        showMsg("idInput", "아이디 확인 중 오류가 발생했습니다.", "red");

        /* 개발자용 : 브라우저 F12 -> Console 탭에 오류 내용을 빨간 줄로 출력 */
        console.error(error);
    }
}

/* 아이디 입력 <input type="text" id="id" name="id"> 를 찾아서
   "커서가 칸을 벗어날 때(blur) 아래 function 안을 실행하라" 고 등록한다 */
document.getElementById("id").addEventListener("blur", function () {

    /* 먼저 형식 검사를 한다. 형식이 맞아서 true 가 돌아오면 */
    if (checkId()) {

        /* 서버에 중복 확인을 요청한다 (형식이 틀리면 서버에 물어볼 필요가 없다) */
        checkIdDuplicate();
    }
});


/* ==============================================================================
   3. 비밀번호  (영문/숫자/_/- 4~20자)
   ============================================================================== */

/* checkPass 라는 이름의 함수를 만든다. 비밀번호가 규칙에 맞는지 검사한다 */
function checkPass() {

    /* 비밀번호 입력 <input type="password" id="pass" name="pass"> 에 입력한 값 얻어 pass 에 저장 */
    const pass = document.getElementById("pass").value;

    /* 규칙 : 영문/숫자/_/- 로만 4~20자
       pass 가 규칙에 맞으면 아래 { } 안을 실행한다 */
    if (/^[A-Za-z0-9_\-]{4,20}$/.test(pass)) {

        /* 비밀번호 안내 자리 <span id="passInput"> 에 파란 글씨로 안내 표시 */
        showMsg("passInput", "올바르게 입력되었습니다.", "blue");

        /* 검사 통과 */
        return true;
    }

    /* 규칙에 맞지 않으면 비밀번호 안내 자리 <span id="passInput"> 에 빨간 글씨로 안내 표시 */
    showMsg("passInput", "영문, 숫자, _, - 로 4~20글자 작성해 주세요!", "red");

    /* 검사 실패 */
    return false;
}

/* 비밀번호 입력 <input type="password" id="pass"> 를 찾아서
   커서가 칸을 벗어날 때(blur) checkPass 함수를 실행하도록 등록 */
document.getElementById("pass").addEventListener("blur", checkPass);


/* ==============================================================================
   4. 이름  (한글 2~6자)
   ============================================================================== */

/* checkName 이라는 이름의 함수를 만든다. 이름이 한글 2~6자인지 검사한다 */
function checkName() {

    /* 이름 입력 <input type="text" id="name" name="name" placeholder="가입할 이름을 적어주세요."> 에 입력한 값 얻어 name 에 저장 */
    const name = document.getElementById("name").value;

    /* 규칙 : [가-힣] 은 한글 한 글자, {2,6} 은 2~6글자
       name 이 한글 2~6자이면 아래 { } 안을 실행한다
       예) "홍길동" -> 통과 / "hong" -> 실패 / "홍" -> 실패 */
    if (/^[가-힣]{2,6}$/.test(name)) {

        /* 이름 안내 자리 <span id="nameInput"> 에 파란 글씨로 안내 표시 */
        showMsg("nameInput", "이름입력완료!", "blue");

        /* 검사 통과 */
        return true;
    }

    /* 규칙에 맞지 않으면 이름 안내 자리 <span id="nameInput"> 에 빨간 글씨로 안내 표시 */
    showMsg("nameInput", "이름을 한글 2~6자로 작성해 주세요.", "red");

    /* 검사 실패 */
    return false;
}

/* 이름 입력 <input type="text" id="name"> 을 찾아서
   커서가 칸을 벗어날 때(blur) checkName 함수를 실행하도록 등록 */
document.getElementById("name").addEventListener("blur", checkName);


/* ==============================================================================
   5. 나이  (비어 있지 않으면 통과)
   ============================================================================== */

/* checkAge 라는 이름의 함수를 만든다. 나이를 입력했는지 검사한다 */
function checkAge() {

    /* 나이 입력 <input type="text" id="age" name="age"> 에 입력한 값을 얻고
       .trim() 으로 앞뒤 공백을 지운 뒤 age 에 저장
       예) "  25  " -> "25" / "   " -> "" (빈 글자) */
    const age = document.getElementById("age").value.trim();

    /* !== "" 는 "빈 글자가 아니면" 이라는 뜻. 무언가 입력했다면 아래 { } 안을 실행한다 */
    if (age !== "") {

        /* 나이 안내 자리 <span id="ageInput"> 에 파란 글씨로 안내 표시 */
        showMsg("ageInput", "나이입력완료!", "blue");

        /* 검사 통과 */
        return true;
    }

    /* 비어 있으면 나이 안내 자리 <span id="ageInput"> 에 빨간 글씨로 안내 표시 */
    showMsg("ageInput", "나이를 입력해주세요.", "red");

    /* 검사 실패 */
    return false;
}

/* 나이 입력 <input type="text" id="age"> 를 찾아서
   커서가 칸을 벗어날 때(blur) checkAge 함수를 실행하도록 등록 */
document.getElementById("age").addEventListener("blur", checkAge);


/* ==============================================================================
   6. 주소  (address1 ~ address5 다섯 칸이 모두 채워져야 통과)
   ============================================================================== */

/* 주소 입력칸 5개를 name 속성으로 한 번에 찾아 addressInputs 에 저장
     <input type="text" id="sample4_postcode" name="address1">  우편번호
     <input type="text" ...                   name="address2">  도로명주소
     <input type="text" ...                   name="address3">  지번주소
     <input type="text" ...                   name="address4">  상세주소
     <input type="text" ...                   name="address5">  참고항목
   querySelectorAll 은 조건에 맞는 태그를 "여러 개" 찾아 목록으로 돌려준다
   [name='address1'] 은 "name 속성이 address1 인 태그" 라는 뜻이다
   id 대신 name 으로 찾는 이유 : 서버도 name 으로 값을 받기 때문이다 */
const addressInputs = document.querySelectorAll(
    "[name='address1'], [name='address2'], [name='address3'], [name='address4'], [name='address5']"
);

/* checkAddress 라는 이름의 함수를 만든다. 주소 5칸이 모두 채워졌는지 검사한다 */
function checkAddress() {

    /* for 반복문 : i 를 0 부터 시작해서, 주소칸 개수(5)보다 작은 동안, 1씩 늘리며 반복
       즉 i = 0, 1, 2, 3, 4 로 5번 반복한다 (주소칸 5개를 하나씩 본다) */
    for (let i = 0; i < addressInputs.length; i++) {

        /* addressInputs[i] 는 i 번째 주소칸이다  예) i=0 이면 name="address1" 칸
           그 칸에 입력한 값에서 공백을 지웠더니 빈 글자("")라면 아래 { } 안을 실행한다 */
        if (addressInputs[i].value.trim() === "") {

            /* 주소 안내 자리 <span id="addressInput"> 에 빨간 글씨로 안내 표시 */
            showMsg("addressInput", "주소를 모두 작성하여주세요.", "red");

            /* 한 칸이라도 비었으면 나머지는 볼 필요 없이 바로 실패로 끝낸다 */
            return false;
        }
    }

    /* 반복문을 끝까지 통과했다 = 5칸 모두 채워져 있다
       주소 안내 자리 <span id="addressInput"> 에 파란 글씨로 안내 표시 */
    showMsg("addressInput", "올바르게 입력되었습니다.", "blue");

    /* 검사 통과 */
    return true;
}

/* 주소칸 5개를 하나씩 돌면서 (i = 0, 1, 2, 3, 4) */
for (let i = 0; i < addressInputs.length; i++) {

    /* i 번째 주소칸에 "커서가 칸을 벗어날 때(blur) checkAddress 함수를 실행하라" 고 등록 */
    addressInputs[i].addEventListener("blur", checkAddress);
}


/* ==============================================================================
   7. 성별  (라디오 버튼 중 하나가 체크되어야 통과)
   ============================================================================== */

/* checkGender 라는 이름의 함수를 만든다. 성별을 골랐는지 검사한다 */
function checkGender() {

    /* 성별 라디오 버튼
         <input type="radio" class="gender" name="gender" value="남">
         <input type="radio" class="gender" name="gender" value="여">
       중에서 체크된(:checked) 것을 찾아 checked 에 저장
       .gender 는 "class 가 gender 인 태그" 라는 뜻이다
       아무것도 체크 안 했으면 null(없음) 이 저장된다 */
    const checked = document.querySelector(".gender:checked");

    /* 체크된 버튼을 찾았다면 (null 이 아니라면) 아래 { } 안을 실행한다 */
    if (checked) {

        /* 성별 안내 자리 <span id="genderInput"> 에 파란 글씨로 안내 표시 */
        showMsg("genderInput", "성별체크완료!", "blue");

        /* 검사 통과 */
        return true;
    }

    /* 아무것도 체크 안 했으면 성별 안내 자리 <span id="genderInput"> 에 빨간 글씨로 안내 표시 */
    showMsg("genderInput", "성별을 체크 해주세요.", "red");

    /* 검사 실패 */
    return false;
}

/* 성별 라디오 버튼 <input type="radio" class="gender"> 들을 모두 찾아 genderButtons 에 저장
   (남, 여 2개가 목록으로 저장된다) */
const genderButtons = document.querySelectorAll(".gender");

/* 성별 버튼을 하나씩 돌면서 (i = 0 은 남, i = 1 은 여) */
for (let i = 0; i < genderButtons.length; i++) {

    /* i 번째 성별 버튼에 "클릭(click)할 때 checkGender 함수를 실행하라" 고 등록 */
    genderButtons[i].addEventListener("click", checkGender);
}


/* ==============================================================================
   8. 이메일  (예: abcde@naver.com)
   ============================================================================== */

/* checkEmail 이라는 이름의 함수를 만든다. 이메일 형식이 맞는지 검사한다 */
function checkEmail() {

    /* 이메일 입력 <input type="text" id="email" name="email"> 에 입력한 값 얻어 email 에 저장 */
    const email = document.getElementById("email").value;

    /* 이메일 규칙
         \w{5,12}      : 영문/숫자/_ 5~12자          예) abcde
         @             : 골뱅이
         [a-z]{2,10}   : 소문자 2~10자                예) naver
         [\.]          : 점(.)
         [a-z]{2,3}    : 소문자 2~3자                 예) com
         [\.]?[a-z]{0,2} : ".kr" 처럼 뒤에 더 붙어도 됨 예) co.kr
       email 이 규칙에 맞으면 아래 { } 안을 실행한다 */
    if (/^\w{5,12}@[a-z]{2,10}[\.][a-z]{2,3}[\.]?[a-z]{0,2}$/.test(email)) {

        /* 이메일 안내 자리 <span id="emailInput"> 에 파란 글씨로 안내 표시 */
        showMsg("emailInput", "올바르게 입력되었습니다.", "blue");

        /* 검사 통과 */
        return true;
    }

    /* 규칙에 맞지 않으면 이메일 안내 자리 <span id="emailInput"> 에 빨간 글씨로 안내 표시 */
    showMsg("emailInput", "이메일 형식이 올바르지 않습니다.", "red");

    /* 검사 실패 */
    return false;
}

/* 이메일 입력 <input type="text" id="email"> 을 찾아서
   커서가 칸을 벗어날 때(blur) checkEmail 함수를 실행하도록 등록 */
document.getElementById("email").addEventListener("blur", checkEmail);


/* ==============================================================================
   9. 전화번호  (0 으로 시작, 숫자 9~11자리)
   ============================================================================== */

/* checkTel 이라는 이름의 함수를 만든다. 전화번호 형식이 맞는지 검사한다 */
function checkTel() {

    /* 전화번호 입력 <input type="text" id="tel" name="tel"> 에 입력한 값 얻어 tel 에 저장 */
    const tel = document.getElementById("tel").value;

    /* 규칙 : 0 으로 시작하고 + 숫자([0-9]) 8~10개 = 총 9~11자리 (빼기 - 없이)
       예) "0515551234" -> 통과 / "051-555-1234" -> 실패
       tel 이 규칙에 맞으면 아래 { } 안을 실행한다 */
    if (/^0[0-9]{8,10}$/.test(tel)) {

        /* 전화번호 안내 자리 <span id="telInput"> 에 파란 글씨로 안내 표시 */
        showMsg("telInput", "올바르게 입력되었습니다.", "blue");

        /* 검사 통과 */
        return true;
    }

    /* 규칙에 맞지 않으면 전화번호 안내 자리 <span id="telInput"> 에 빨간 글씨로 안내 표시 */
    showMsg("telInput", "전화번호 형식이 올바르지 않습니다.", "red");

    /* 검사 실패 */
    return false;
}

/* 전화번호 입력 <input type="text" id="tel"> 을 찾아서
   커서가 칸을 벗어날 때(blur) checkTel 함수를 실행하도록 등록 */
document.getElementById("tel").addEventListener("blur", checkTel);


/* ==============================================================================
   10. 휴대폰번호  (010/011/017/019 + 숫자 7~8자리)
   ============================================================================== */

/* checkHp 라는 이름의 함수를 만든다. 휴대폰번호 형식이 맞는지 검사한다 */
function checkHp() {

    /* 휴대폰번호 입력 <input type="text" id="hp" name="hp"> 에 입력한 값 얻어 hp 에 저장 */
    const hp = document.getElementById("hp").value;

    /* 규칙 : 01 로 시작 + [0179] 중 하나(010/011/017/019) + 숫자 7~8개 (빼기 - 없이)
       예) "01012345678" -> 통과 / "010-1234-5678" -> 실패
       hp 가 규칙에 맞으면 아래 { } 안을 실행한다 */
    if (/^01[0179][0-9]{7,8}$/.test(hp)) {

        /* 휴대폰 안내 자리 <span id="hpInput"> 에 파란 글씨로 안내 표시 */
        showMsg("hpInput", "올바르게 입력되었습니다.", "blue");

        /* 검사 통과 */
        return true;
    }

    /* 규칙에 맞지 않으면 휴대폰 안내 자리 <span id="hpInput"> 에 빨간 글씨로 안내 표시 */
    showMsg("hpInput", "휴대폰번호 형식이 올바르지 않습니다.", "red");

    /* 검사 실패 */
    return false;
}

/* 휴대폰번호 입력 <input type="text" id="hp"> 를 찾아서
   커서가 칸을 벗어날 때(blur) checkHp 함수를 실행하도록 등록 */
document.getElementById("hp").addEventListener("blur", checkHp);


/* ==============================================================================
   11. 가입 버튼  -  join.jsp 의 가입 버튼이 부르는 함수
       <input type="button" value="회원가입" onclick="check()">

       위에서 만든 check 함수를 1번부터 순서대로 부른다.
       하나라도 false 면 그 칸으로 커서를 옮기고 멈춘다.
       모두 true 면 폼을 서버로 보낸다.

       [참고] 화면 검증은 개발자도구로 건너뛸 수 있으므로
              서버(MemberService)에서도 같은 검사를 다시 한다.
   ============================================================================== */

/* check 라는 이름의 함수를 만든다. 가입 버튼을 누르면 실행된다 */
function check() {

    /* 1. 약관 검사
       ! 는 "반대로" 라는 뜻이다. checkAgree() 가 false(실패) 면 !false = true 가 되어 { } 안을 실행
       return; 은 함수를 여기서 끝낸다는 뜻 -> 아래 검사도, 전송도 하지 않는다 */
    if (!checkAgree()) { return; }

    /* 2. 아이디 검사. 실패하면
       아이디 입력 <input type="text" id="id"> 를 찾아 .focus() 로 커서를 옮기고 함수를 끝낸다 */
    if (!checkId())    { document.getElementById("id").focus();    return; }

    /* 3. 비밀번호 검사. 실패하면
       비밀번호 입력 <input type="password" id="pass"> 로 커서를 옮기고 함수를 끝낸다 */
    if (!checkPass())  { document.getElementById("pass").focus();  return; }

    /* 4. 이름 검사. 실패하면
       이름 입력 <input type="text" id="name"> 으로 커서를 옮기고 함수를 끝낸다 */
    if (!checkName())  { document.getElementById("name").focus();  return; }

    /* 5. 나이 검사. 실패하면
       나이 입력 <input type="text" id="age"> 로 커서를 옮기고 함수를 끝낸다 */
    if (!checkAge())   { document.getElementById("age").focus();   return; }

    /* 6. 주소 검사. 실패하면 함수를 끝낸다 (칸이 5개라 커서는 옮기지 않는다) */
    if (!checkAddress()) { return; }

    /* 7. 성별 검사. 실패하면 함수를 끝낸다 (라디오 버튼이라 커서는 옮기지 않는다) */
    if (!checkGender())  { return; }

    /* 8. 이메일 검사. 실패하면
       이메일 입력 <input type="text" id="email"> 로 커서를 옮기고 함수를 끝낸다 */
    if (!checkEmail()) { document.getElementById("email").focus(); return; }

    /* 9. 전화번호 검사. 실패하면
       전화번호 입력 <input type="text" id="tel"> 로 커서를 옮기고 함수를 끝낸다 */
    if (!checkTel())   { document.getElementById("tel").focus();   return; }

    /* 10. 휴대폰번호 검사. 실패하면
       휴대폰번호 입력 <input type="text" id="hp"> 로 커서를 옮기고 함수를 끝낸다 */
    if (!checkHp())    { document.getElementById("hp").focus();    return; }

    /* 11. 여기까지 왔다면 1~10 모두 통과한 것이다
       화면의 회원가입 폼 <form action="..." method="post"> 을 찾아 .submit() 으로 서버에 전송한다
       (폼 안의 모든 입력값이 name 이름으로 서버에 전달된다) */
    document.querySelector("#joinPro").submit();
}

/* 요약
     칸마다 checkXxx() 하나 : 규칙 검사 + 파랑/빨강 안내 + true/false
     칸을 벗어나면(blur)     : 그 칸의 checkXxx() 실행
     아이디만 추가로          : fetch 로 서버 중복 확인
     가입 버튼 check()       : checkXxx() 를 1번부터 차례로 -> 모두 통과하면 전송 */
