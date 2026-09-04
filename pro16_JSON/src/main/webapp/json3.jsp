<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>

	<%-- JQuery 사용을 위해 CDN 주소로 사용문법 사이트에 요청 --%>
	<script src="http://code.jquery.com/jquery-latest.min.js"></script>
	
	<script type="text/javascript">
	
		$(function(){
			
			//id="checkJson" 작성된 <a>요소를 선택해서 click 이벤트(동작) 등록후 이벤트 처리 코드 작성
			$("#checkJson").click(function(){
				
				//JSONObject 를  문자열 형태로 만들어 변수에 저장
				let jsonStr = '{"name":"박지성",  "age":25,    "gender":"남자",  "nickname": "날센돌이" }';
				//				 key  : value,   key : value,  key   : value,   key      :  value
				
				//JSONObject 형태의문자열을 ==변경===> JSONObject {} 로 변경해서 다시 변수에 저장
				//변경이유 : key로 value를 추출해서 사용하기 위해
				let jsonObj =  JSON.parse(jsonStr);
				//  {"name":"박지성",  "age":25,    "gender":"남자",  "nickname": "날센돌이" }
				//	   key : value,   key:value,        key:value,        key:value
				
				let output = "회원정보<br>";
					output += "==================<br>";
					output += "이름:  " + jsonObj.name  + "<br>";
					output += "나이:  " + jsonObj.age   + "<br>";
					output += "성별:  " + jsonObj.gender + "<br>";
					output += "별명:  " + jsonObj.nickname + "<br>";
				
				$("#output").html(output);	
				
			});		
			
		});		
	</script>
</head>
<body>
	<a id="checkJson" style="cursor: pointer;">출력</a> <br><br>

	<div id="output">콘텐츠영역</div>

</body>
</html>

<%--
	JSON.parse("JSON문자열") 함수
	
	-   자바스크립트에서 제공하는 내장함수로 
	    서버에서 받은 JSON형식의 문자열을  JavaScript의 JSONObject객체로 변환해서
	    반환 하는 함수 
	  예) '{ "키":"값"}' 문자열을 -> { "키":"값"} 로 변환 해서 반환 
	    
	    
    JSON.stringify(JSONObject)함수 
    
    - 자바스크립트에서 제공하는 내장함수로
      JSONObject객체를  JSON형식의 문자열로 변환해서 반환 하는 함수 
      
		예) { "키":"값"}객체를 -> '{ "키":"값"}'문자열로 변환 해서 반환 


--%>



