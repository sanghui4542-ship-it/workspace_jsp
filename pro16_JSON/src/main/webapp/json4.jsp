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
				
				//JSONObject {} 를 문자열 '{}' 형태로 만들어 변수에 저장
				//회원 두명의 정보 만들기
				let jsonStr = '{"members" : [{"name":"박지성", "age":25, "gender":"남자","nickname":"날센돌이"},'
										  +	'{"name":"손흥민", "age":30, "gender":"남자","nickname":"탱크"} ] }';				
						
				
				//jsonStr변수에 저장된 '{...}' 문자열을 ===변경===> {....} 형태로 변경해서 저장
				let jsonInfo = JSON.parse(jsonStr);
				
				let output = "회원정보들 <br>===============<br>"
				for(let index in jsonInfo.members){
			 			
					 output += "이름:  " + jsonInfo.members[index].name + "<br>";
					 output += "나이:  " + jsonInfo.members[index].age + "<br>";
					 output += "성별:  " + jsonInfo.members[index].gender + "<br>";
					 output += "별명:  " + jsonInfo.members[index].nickname + "<br><br>";
			 		 
		 		}
						 				
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



