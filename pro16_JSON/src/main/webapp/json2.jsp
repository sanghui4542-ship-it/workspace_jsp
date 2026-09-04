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
				
				//JSONObject 를 문자열형태로 만들어 변수에 저장
				let jsonStr = '{ "age" : [22, 33, 44] }';
				//				  key  :    value
				
				//console.log( typeof  jsonStr ); //string
				
				//위 문자열로 구성된 JSONObject를 실제 { }로 구성되는 JSONObject로 변환해서 다시 변수에 저장
				 let jsonInfo = JSON.parse( jsonStr );
				 				//'{ "age" : [22, 33,  44]  }' 문자열을?
				 				// { "age" : [22, 33,  44]  } 로 구성되는 JSONObject로 변환해서 반환 
				 				//    key  :    value
				 
 				//console.log( typeof jsonInfo );  //object
				 
 				//<div id="output">콘텐츠영역<div>요소의  "콘텐츠영역"에 보여질 데이터를 만들어서 output변수에 저장시키자.
 				let  output = "회원 나이<br>";
 					 output += "=================<br>";
 					 
 					 for(let index  in jsonInfo.age){
 						 			//[22, 33, 44]
 						 			//  0   1   2   index
 						 output += jsonInfo.age[index] + "<br>";
 						 		 
 					 }
 	 				//<div id="output">콘텐츠영역<div>요소의  "콘텐츠영역"에 output변수에 누적된 문자열을 넣어 보여주자.				 				
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



