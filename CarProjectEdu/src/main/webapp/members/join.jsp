<%@page import="java.sql.Date"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
 <%
	request.setCharacterEncoding("UTF-8");
	String contextPath = request.getContextPath();
%>
     <%-- join.jsp (MVC 중에서 V)  회원가입 요청 디자인 화면 --%>

	 <%-- 가입할 정보를 입력하고 MemberController로 회원 가입 요청 합니다. --%>
<form id="joinPro" action="<%=contextPath%>/member/joinPro.me" method="post">

	<%-- [보안] CSRF 토큰 : 이 화면에서 출발한 요청임을 증명한다 (설명은 members/login.jsp) --%>
	<input type="hidden" name="_csrf" value="${_csrf}">

	<div style="height: 100px;"></div>
	<div class="container">
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
						<%-- name속성값 address1 부터 ~~ address5 까지 입력되어 있는 주소를 모두 합쳐서 DB에 address열에  INSERT 하자. --%>
						<label>주소</label>
						<p id="addressInput"></p> 
						<input type="text" id="sample4_postcode" name="address1" class="form-control" placeholder="우편번호">
						<input type="button" onclick="sample4_execDaumPostcode()" value="우편번호 찾기" class="form-control"><br>
						
						<input type="text" id="sample4_roadAddress" name="address2" placeholder="도로명주소" class="form-control">
						<input type="text" id="sample4_jibunAddress" placeholder="지번주소" name="address3" class="form-control">
						
						<span id="guide" style="color:#999;display:none"></span>
						
						<input type="text" id="sample4_detailAddress" placeholder="상세주소" name="address4" class="form-control">
						<input type="text" id="sample4_extraAddress" placeholder="참고항목"  name="address5" class="form-control">
						
					</div>
					
					<div class="col-6">
						<label>성별</label>
						<p id="genderInput"></p> 
						<hr> 
						남성
					    <input type="radio" 
							   class="gender" 
							   name="gender"
							   value="남"
							   > 
						&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	   
					     여성
					   <input type="radio" 
							   class="gender" 
							   name="gender"
							   value="여"
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

<script src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
<script>
    //본 예제에서는 도로명 주소 표기 방식에 대한 법령에 따라, 내려오는 데이터를 조합하여 올바른 주소를 구성하는 방법을 설명합니다.
    function sample4_execDaumPostcode() {
        new daum.Postcode({   // 다음(카카오)이 제공하는 주소 검색 창을 만든다
            oncomplete: function(data) {   // 주소를 고르면 실행되는 함수. data 안에 고른 주소 정보가 들어 있다
                // 팝업에서 검색결과 항목을 클릭했을때 실행할 코드를 작성하는 부분.

                // 도로명 주소의 노출 규칙에 따라 주소를 표시한다.
                // 내려오는 변수가 값이 없는 경우엔 공백('')값을 가지므로, 이를 참고하여 분기 한다.
                var roadAddr = data.roadAddress; // 도로명 주소 변수
                var extraRoadAddr = ''; // 참고 항목 변수

                // 법정동명이 있을 경우 추가한다. (법정리는 제외)
                // 법정동의 경우 마지막 문자가 "동/로/가"로 끝난다.
                if(data.bname !== '' && /[동|로|가]$/g.test(data.bname)){
                    extraRoadAddr += data.bname;
                }
                // 건물명이 있고, 공동주택일 경우 추가한다.
                if(data.buildingName !== '' && data.apartment === 'Y'){
                   extraRoadAddr += (extraRoadAddr !== '' ? ', ' + data.buildingName : data.buildingName);
                }
                // 표시할 참고항목이 있을 경우, 괄호까지 추가한 최종 문자열을 만든다.
                if(extraRoadAddr !== ''){
                    extraRoadAddr = ' (' + extraRoadAddr + ')';
                }

                // 우편번호와 주소 정보를 해당 필드에 넣는다.
                document.getElementById('sample4_postcode').value = data.zonecode;
                document.getElementById("sample4_roadAddress").value = roadAddr;   // 도로명 주소 칸을 채운다
                document.getElementById("sample4_jibunAddress").value = data.jibunAddress;   // 지번 주소 칸도 채운다
                
                // 참고항목 문자열이 있을 경우 해당 필드에 넣는다.
                if(roadAddr !== ''){
                    document.getElementById("sample4_extraAddress").value = extraRoadAddr;
                } else {
                    document.getElementById("sample4_extraAddress").value = '';   // 참고 항목이 없으면 그 칸을 비운다
                }

                var guideTextBox = document.getElementById("guide");   // 예상 주소를 안내할 자리를 찾는다
                // 사용자가 '선택 안함'을 클릭한 경우, 예상 주소라는 표시를 해준다.
                if(data.autoRoadAddress) {
                    var expRoadAddr = data.autoRoadAddress + extraRoadAddr;
                    guideTextBox.innerHTML = '(예상 도로명 주소 : ' + expRoadAddr + ')';   // 예상 도로명 주소를 안내한다
                    guideTextBox.style.display = 'block';   // 안내 자리를 보이게 한다

                } else if(data.autoJibunAddress) {   // 도로명은 없고 지번만 자동으로 잡힌 경우
                    var expJibunAddr = data.autoJibunAddress;   // 자동으로 잡힌 지번 주소
                    guideTextBox.innerHTML = '(예상 지번 주소 : ' + expJibunAddr + ')';   // 예상 지번 주소를 안내한다
                    guideTextBox.style.display = 'block';   // 안내 자리를 보이게 한다
                } else {
                    guideTextBox.innerHTML = '';   // 안내할 내용이 없으면 비운다
                    guideTextBox.style.display = 'none';   // 안내 자리를 숨긴다
                }
            }
        }).open();   // 주소 검색 창을 실제로 띄운다
    }
</script>	
<%-- 회원가입 유효성 검사 체크 --%>
<%-- join.js 가 서버 주소를 만들 때 사용할 컨텍스트 경로를 전달한다.
     (기존 join.js 에는 http://localhost:8090/CarProject 가 그대로 박혀 있어
      포트나 프로젝트명이 다르면 아이디 중복확인이 실패했다) --%>
<script>
	window.CONTEXT_PATH = "<%=request.getContextPath()%>";   // JSP 가 서버에서 만든 프로젝트 경로를 자바스크립트가 쓸 수 있게 넘겨준다 (join.js 가 이 값을 읽는다)
</script>
<%-- ?v=7 : 캐시 무효화. 파일을 크게 고치면 숫자를 올린다 (CarMain.jsp 의 설명 참고) --%>
<script src="<%=request.getContextPath()%>/js/join.js?v=7"></script>
	
	
		

















