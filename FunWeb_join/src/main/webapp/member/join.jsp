<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%--
================================================================
 [실습 5] join.jsp 를 회원가입이 되도록 고치기

 완성 목표 3가지
   1. 폼의 목적지를 컨트롤러(join.do)로 연결
   2. Submit 버튼을 실제 전송되는 버튼으로 변경
   3. 가입 실패 메시지(joinMsg)를 경고창으로 표시

 ** 주석 아래 빈 줄에 코드를 직접 작성한다. **
================================================================
--%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<link href="../css/default.css" rel="stylesheet" type="text/css">
<link href="../css/subpage.css" rel="stylesheet" type="text/css">

<%-- [실습 5-3] 가입 실패 안내 --%>

<%-- request 에서 "joinMsg" 를 꺼내 String 변수 joinMsg 에 저장 --%>
<% String joinMsg = (String)request.getAttribute("joinMsg"); %>

<%-- joinMsg 가 null 이 아니면 아래 script 를 출력하는 if 문 시작 --%>
<% if(joinMsg != null){ %>	
	
	<%-- alert 로 joinMsg 값을 표현식(<%= %>)으로 출력하는 script 태그 작성 --%>
	<script>
		alert("<%=joinMsg%>");
	</script>
	
<% } %>
<%-- if 문 닫기 --%>


</head>
<body>
	<div id="wrap">
		<!-- 헤더들어가는 곳 -->
		<%@ include file="../inc/top.jsp" %>
		<!-- 헤더들어가는 곳 -->

		<!-- 본문들어가는 곳 -->
		<!-- 본문메인이미지 -->
		<div id="sub_img_member"></div>
		<!-- 본문메인이미지 -->
		<!-- 왼쪽메뉴 -->
		<nav id="sub_menu">
			<ul>
				<li><a href="#">Join us</a></li>
				<li><a href="#">Privacy policy</a></li>
			</ul>
		</nav>
		<!-- 왼쪽메뉴 -->
		<!-- 본문내용 -->
		<article>
			<h1>Join Us</h1>

			<!-- [실습 5-1] form 태그의 action 을 "join.do" 로, method 를 "post" 로 작성 -->
			<form action="join.do" id="join" method="post">

				<fieldset>
					<legend>가입 기본정보 입력</legend>
					
					<label>아이디</label> <input type="text" name="id" class="id" onkeyup="mySend();"> 
					<span id="result"></span><br>
						
					<label>비밀빈호</label> <input type="password" name="pass"><br>
					<label>비밀번호 확인</label> <input type="password" name="pass2"><br>
					<label>이름</label> <input type="text" name="name"><br>
					<label>이메일</label> <input type="email" name="email"><br>
					<label>이메일 확인</label> <input type="email" name="email2"><br>
				</fieldset>
				<fieldset>
					<legend>상세정보 입력</legend>
					<label>주소</label> <input type="text" name="address"><br>
					<label>전화번호</label> <input type="text" name="phone"><br>
					<label>HP</label> <input type="text" name="mobile"><br>
				</fieldset>
				<div class="clear"></div>
				<div id="buttons">
					<input type="submit" value="회원가입" class="submit">
					<input type="reset" value="가입취소" class="cancel">
				</div>
			</form>
		</article>
		<!-- 본문내용 -->
		<!-- 본문들어가는 곳 -->

		<div class="clear"></div>
		<!-- 푸터들어가는 곳 -->
		<%@ include file="../inc/bottom.jsp" %>
		<!-- 푸터들어가는 곳 -->
	</div>
	
	<%--JQuery 문법 사용하기 위한 요청 주소 설정 --%>
	<script src="http://code.jquery.com/jquery-latest.min.js"></script>
	
	<script type="text/javascript">
	
		//아이디 입력 <input>에  아이디를 입력하면 아이디 중복체크 요청을 Ajax 비동기 통신으로  서블릿에 요청합니다.
		function mySend(){
			
			let id = $("input[name='id']").val();   //회원가입시 입력한 아이디 얻기 (아이디 중복 검사를 위해)
			
			//아이디를 입력하지 않고 아이디 중복 검사 요청한다면?
			if(id == ""){
				
				//<span id="result"></span> 요소를 선택해서  "아이디 입력 필수" 메세지를 콘텐츠영역에 넣어 보여주자
				$("#result").text("아이디 입력 필수");
				
				//<input type="text" name="id" class="id" onkeyup="mySend();"> 아이디 입력요소 선택후 강제 포커스 설정
				$("input[name='id']").focus();
				
				return; //  Ajax 요청이 들어가면 안되니 mySend() 함수 즉시 종료 
			}
			
			//아이디 입력 하고 아이디 중복 검사 요청을 하는 곳
			
			//아이디를 입력 하고  아이디 중복 체크 요청 했다면(<input>에  키를 눌러따가 뗀  keyup이벤트가 발생했을떄)?
			//서버페이지(서블릿)에 아이디 중복 체크 요청을 하여  DB의 테이블에 입력한 아이디가 존재하는지 하지 않는지 유무를 응답 받습니다.
			$.ajax({
					  //http://localhost:8181/FunWeb_join/idCheck.do	
					 url : "<%=request.getContextPath()%>/idCheck.do",
					 type : "post",
					 data : {userid:id}, //입력한 아이디 요청하는 값으로 사용  
					 dataType : "text",  //서버페이지(서블릿)으로 응답받을 예상 데이터 유형 
					 
					 success : function(response){ //요청 통신에 성공했을때  응답 데이터를 매개변수 response 로 전달 받음
												   //"아이디 중복" 또는  "사용가능한 아이디"  둘중 하나의 응답 데이터를 받습니다.
					 		
							//<span id="result"></span> 요소를 선택해서  응답 메세지 보여주자
							$("#result").html("<font style='color:red;'>" +  response + "</font>");
					 		
					 },
					 
					 error : function(){ //요청 통신에 실패 했을때 실패 메세지를 클라이언트의 브라우저로 보여주자
						 alert("통신 에러가 발생했습니다.");
					 }
			});
		}//------------------------------------------ function mySend() 끝 
	
	</script>
	
	
	
</body>
</html>










