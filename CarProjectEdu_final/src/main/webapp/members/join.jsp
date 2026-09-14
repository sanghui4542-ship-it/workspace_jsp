<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%--
 ============================================================================
  members/join.jsp  -  회원가입 화면

  CarMain.jsp 의 가운데 영역에 끼워져(include) 보이는 화면 조각이다.
    요청 흐름 : Top.jsp 회원가입 버튼 -> /member/join.me -> 사장(MemberController) -> CarMain.jsp -> 이 화면

  이 화면과 연결된 파일
    js/join.js : 입력칸 검사, 아이디 중복 확인(fetch), 가입 버튼 check() 함수
    다음 우편번호 API : 주소 찾기 팝업 (아래 sample4_execDaumPostcode 함수)

  입력칸 속성 규칙
    id   속성 : join.js 가 입력칸을 찾을 때 사용한다      예) document.getElementById("name")
    name 속성 : 서버가 값을 꺼낼 때 사용한다              예) request.getParameter("name")
 ============================================================================
--%>
<%
	// 요청 글자 방식을 UTF-8 로 정한다
	request.setCharacterEncoding("UTF-8");

	// 프로젝트 경로 얻기  예) "/CarProject"  -> form 의 action 주소를 만들 때 사용
	String contextPath = request.getContextPath();
%>

<%-- 회원가입하기 버튼을 누르면 입력한 값들을 POST 방식으로 사장(MemberController)의 /member/joinPro.me 에 전송한다 --%>
<form action="<%=contextPath%>/member/joinPro.me" method="post">

	<div style="height: 100px;"></div>
	<div class="container">

		<%-- ===== 이용약관 : 제목 버튼을 누르면 내용이 펼쳐진다 (js/app.js 의 접기/펼치기 기능) ===== --%>
		<div class="row justify-content-lefts">
			<h2>이용약관</h2>
		</div>
		<div class="accordion" id="accordionExample">
			<div class="card">
				<div class="card-header" id="headingOne">
					<h2 class="mb-0">
						<button class="btn btn-link btn-block text-left" type="button"
							data-toggle="collapse" data-target="#collapseOne"
							aria-expanded="true" aria-controls="collapseOne">
							약관동의 내용1
						</button>
					</h2>
				</div>

				<div id="collapseOne" class="collapse" aria-labelledby="headingOne"
					data-parent="#accordionExample">
					<div class="card-body">Anim pariatur cliche reprehenderit,
						enim eiusmod high life accusamus terry richardson ad squid. 3 wolf
						moon officia aute, non cupidatat skateboard dolor brunch. Food
						truck quinoa nesciunt laborum eiusmod. Brunch 3 wolf moon tempor,
						sunt aliqua put a bird on it squid single-origin coffee nulla
						assumenda shoreditch et. Nihil anim keffiyeh helvetica, craft beer
						labore wes anderson cred nesciunt sapiente ea proident. Ad vegan
						excepteur butcher vice lomo. Leggings occaecat craft beer
						farm-to-table, raw denim aesthetic synth nesciunt you probably
						haven't heard of them accusamus labore sustainable VHS.</div>
				</div>
			</div>
			<div class="card">
				<div class="card-header" id="headingTwo">
					<h2 class="mb-0">
						<button class="btn btn-link btn-block text-left collapsed"
							type="button" data-toggle="collapse" data-target="#collapseTwo"
							aria-expanded="false" aria-controls="collapseTwo">
							약관동의 내용2
						</button>
					</h2>
				</div>
				<div id="collapseTwo" class="collapse" aria-labelledby="headingTwo"
					data-parent="#accordionExample">
					<div class="card-body">Anim pariatur cliche reprehenderit,
						enim eiusmod high life accusamus terry richardson ad squid. 3 wolf
						moon officia aute, non cupidatat skateboard dolor brunch. Food
						truck quinoa nesciunt laborum eiusmod. Brunch 3 wolf moon tempor,
						sunt aliqua put a bird on it squid single-origin coffee nulla
						assumenda shoreditch et. Nihil anim keffiyeh helvetica, craft beer
						labore wes anderson cred nesciunt sapiente ea proident. Ad vegan
						excepteur butcher vice lomo. Leggings occaecat craft beer
						farm-to-table, raw denim aesthetic synth nesciunt you probably
						haven't heard of them accusamus labore sustainable VHS.</div>
				</div>
			</div>
			<div class="card">
				<div class="card-header" id="headingThree">
					<h2 class="mb-0">
						<button class="btn btn-link btn-block text-left collapsed"
							type="button" data-toggle="collapse" data-target="#collapseThree"
							aria-expanded="false" aria-controls="collapseThree">
							약관동의 내용3
						</button>
					</h2>
				</div>
				<div id="collapseThree" class="collapse"
					aria-labelledby="headingThree" data-parent="#accordionExample">
					<div class="card-body">Anim pariatur cliche reprehenderit,
						enim eiusmod high life accusamus terry richardson ad squid. 3 wolf
						moon officia aute, non cupidatat skateboard dolor brunch. Food
						truck quinoa nesciunt laborum eiusmod. Brunch 3 wolf moon tempor,
						sunt aliqua put a bird on it squid single-origin coffee nulla
						assumenda shoreditch et. Nihil anim keffiyeh helvetica, craft beer
						labore wes anderson cred nesciunt sapiente ea proident. Ad vegan
						excepteur butcher vice lomo. Leggings occaecat craft beer
						farm-to-table, raw denim aesthetic synth nesciunt you probably
						haven't heard of them accusamus labore sustainable VHS.</div>
				</div>
			</div>
		</div>

		<%-- ===== 약관 동의 체크박스 : join.js 의 checkAgree() 가 검사하고, 결과 문구를 agreeInput 에 표시한다 ===== --%>
		<div class="row justify-content-center text-center">
			<div class="col-4">
				<input type="checkbox" name="agree" id="agree"> 위의 약관의 내용에 동의합니다.
				<p id="agreeInput"></p>
			</div>

		</div>
		<div class="row" style="height: 25px;"></div>
		<div class="row justify-content-left">
			<h1>회원가입 </h1>
		</div>

			<%-- ===== 회원 정보 입력칸 : 각 입력칸 아래 <p id="xxxInput"> 에 join.js 가 빨강/파랑 안내 문구를 표시한다 ===== --%>
			<div class="form-group">
				<div class="row">
					<div class="col-6">
						<label>아이디</label>
						<input type="text"
							   id="id"
							   name="id"
							   class="form-control"
							   placeholder="가입할 아이디를 적어주세요.">
					    <p id="idInput"></p>
					</div>
					<div class="col-6">
						<label>비밀번호</label>
						<input type="password"
							   id="pass"
							   name="pass"
							   class="form-control"
							   placeholder="가입할 비밀번호를 적어주세요.">
						<p id="passInput"></p>
					</div>
				</div>
			</div>
			<div class="form-group">
				<div class="row">
					<div class="col-6">
						<label>이름</label>
						<input type="text"
							   id="name"
							   name="name"
							   class="form-control"
							   placeholder="가입할 이름을 적어주세요.">
						<p id="nameInput"></p>
					</div>

					<div class="col-6">
						<label>나이</label>
						<input type="text"
							   id="age"
							   name="age"
							   class="form-control"
							   placeholder="나이를 적어주세요.">
						<p id="ageInput"></p>
					</div>
				</div>
			</div>
			<div class="form-group">
				<div class="row">
					<div class="col-6">
						<%-- ===== 주소 5칸 : 서버(MemberService)가 address1 ~ address5 를 하나로 이어 붙여 DB 의 address 열에 저장한다 ===== --%>
						<label>주소</label>
						<p id="addressInput"></p>
						<input type="text" id="sample4_postcode" name="address1" class="form-control" placeholder="우편번호">
						<%-- 우편번호 찾기 버튼 : 아래 sample4_execDaumPostcode() 함수가 주소 검색 팝업을 띄운다 --%>
						<input type="button" onclick="sample4_execDaumPostcode()" value="우편번호 찾기" class="form-control"><br>

						<input type="text" id="sample4_roadAddress" name="address2" placeholder="도로명주소" class="form-control">
						<input type="text" id="sample4_jibunAddress" placeholder="지번주소" name="address3" class="form-control">

						<%-- 예상 주소 안내 자리 (처음에는 숨겨져 있다가 필요할 때만 보인다) --%>
						<span id="guide" style="color:#999;display:none"></span>

						<input type="text" id="sample4_detailAddress" placeholder="상세주소" name="address4" class="form-control">
						<input type="text" id="sample4_extraAddress" placeholder="참고항목"  name="address5" class="form-control">

					</div>

					<div class="col-6">
						<%-- ===== 성별 : 같은 name="gender" 라디오 버튼은 둘 중 하나만 선택된다 ===== --%>
						<label>성별</label>
						<p id="genderInput"></p>
						<hr>
						남성
					    <input type="radio"
							   class="gender"
							   name="gender"
							   value="man"
							   >
						&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
					     여성
					   <input type="radio"
							   class="gender"
							   name="gender"
							   value="woman"
							   >
					</div>
				</div>
			</div>
			<div class="form-group">
				<div class="row">
					<div class="col-4">
						<label>Email</label>
						<input type="email"
							   id="email"
							   name="email"
							   class="form-control"
							   placeholder="이메일을 적어주세요.">
						<p id="emailInput"></p>
					</div>

					<div class="col-4">
						<label>연락처</label>
						<input type="tel"
							   id="tel"
							   name="tel"
							   class="form-control"
							   placeholder="연락처를 '-'없이 적어주세요.">
					   	<p id="telInput"></p>
					</div>
					<div class="col-4">
						<label>핸드폰번호</label>
						<input type="text"
							   id="hp"
							   name="hp"
							   class="form-control"
							   placeholder="핸드폰번호를 '-'없이 적어주세요.">
						<p id="hpInput"></p>
					</div>
				</div>
			</div>

			<%-- ===== 회원가입하기 버튼 : join.js 의 check() 가 모든 입력칸을 검사한 뒤 통과하면 form 을 전송한다
			           return false; : a 태그의 원래 동작(주소 # 로 이동)을 막는다 ===== --%>
			<div class="row">
				<div class="col">
					<a href="#" onclick="check(); return false;"
					   type="button"
						class="btn btn-primary btn-block">회원가입하기</a>
				</div>
			</div>
			<br /> <br /> <br />

	</div>
</form>

<%-- ===== 다음(카카오) 우편번호 서비스 스크립트 불러오기 ===== --%>
<script src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
<script>
	// 우편번호 찾기 버튼을 누르면 실행되는 함수 : 주소 검색 팝업을 띄우고, 고른 주소를 주소 입력칸들에 채운다
    function sample4_execDaumPostcode() {

    	// 다음(카카오) 주소 검색 팝업 객체를 만든다
        new daum.Postcode({

        	// 고객이 팝업에서 주소를 고르면 실행되는 함수 (data 에 고른 주소 정보가 들어 있다)
            oncomplete: function(data) {

            	// 고른 도로명 주소
                var roadAddr = data.roadAddress;

            	// 참고항목(동 이름, 아파트 이름)을 모을 변수
                var extraRoadAddr = '';

                // 법정동 이름이 "동/로/가" 로 끝나면 참고항목에 추가한다  예) "역삼동"
                if(data.bname !== '' && /[동|로|가]$/g.test(data.bname)){
                    extraRoadAddr += data.bname;
                }

                // 아파트(공동주택) 이름이 있으면 참고항목에 추가한다  예) "역삼동, 래미안아파트"
                if(data.buildingName !== '' && data.apartment === 'Y'){
                   extraRoadAddr += (extraRoadAddr !== '' ? ', ' + data.buildingName : data.buildingName);
                }

                // 참고항목이 있으면 괄호로 감싼다  예) " (역삼동, 래미안아파트)"
                if(extraRoadAddr !== ''){
                    extraRoadAddr = ' (' + extraRoadAddr + ')';
                }

                // 우편번호, 도로명 주소, 지번 주소 입력칸을 채운다
                document.getElementById('sample4_postcode').value = data.zonecode;
                document.getElementById("sample4_roadAddress").value = roadAddr;
                document.getElementById("sample4_jibunAddress").value = data.jibunAddress;

                // 도로명 주소가 있으면 참고항목 입력칸을 채우고, 없으면 비운다
                if(roadAddr !== ''){
                    document.getElementById("sample4_extraAddress").value = extraRoadAddr;
                } else {
                    document.getElementById("sample4_extraAddress").value = '';
                }

                // 예상 주소 안내 자리 <span id="guide"> 찾기
                var guideTextBox = document.getElementById("guide");

                // 고객이 "선택 안함"을 눌러 주소가 자동으로 잡힌 경우 예상 주소를 안내한다
                if(data.autoRoadAddress) {
                    var expRoadAddr = data.autoRoadAddress + extraRoadAddr;
                    guideTextBox.innerHTML = '(예상 도로명 주소 : ' + expRoadAddr + ')';
                    guideTextBox.style.display = 'block';

                } else if(data.autoJibunAddress) {
                    var expJibunAddr = data.autoJibunAddress;
                    guideTextBox.innerHTML = '(예상 지번 주소 : ' + expJibunAddr + ')';
                    guideTextBox.style.display = 'block';

                // 안내할 예상 주소가 없으면 안내 자리를 비우고 숨긴다
                } else {
                    guideTextBox.innerHTML = '';
                    guideTextBox.style.display = 'none';
                }
            }

        // 만든 주소 검색 팝업을 화면에 띄운다
        }).open();
    }
</script>

<%-- ===== join.js 가 서버 주소를 만들 때 쓸 프로젝트 경로를 전달한다  예) "/CarProject" ===== --%>
<script>
	window.CONTEXT_PATH = "<%=request.getContextPath()%>";
</script>

<%-- ===== 회원가입 입력 검사 스크립트 불러오기
           ?v=8 : 브라우저가 예전 join.js 를 기억(캐시)해 두었어도 새 파일을 받게 하는 번호. 파일을 고치면 숫자를 올린다 ===== --%>
<script src="<%=request.getContextPath()%>/js/join.js?v=8"></script>
