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
		/* 웹브라우저가  json1.jsp 안에 작성된 모든 HTML태그들을 모두 읽어서 보관 했을때 
		   function(){} 콜백함수가 실행되게 작성 */
		$(function(){
			
			/* id=checkJson 작성된 <a>태그 하나를 선택해서 click()메소드를 작성해서 click하는 동작 등록후
			   사이트 이용자가 <a>를 한번 클릭하는 동작을 하면 function(){} 콜백함수가 실행되어 처리하게 작성 */
			$("#checkJson").click(function(){
				
				/* JSONObject -> 중괄호 {} 둘러싸서 표현하는  .json파일에 저장되는 객체 데이터.
							  -> 중괄호 {} 안에 실제 데이터는  key:value를 한쌍으로 나열 합니다.
				   참고.
				   		 "name"     					-> key명 또는 배열명으로 불린다.
				   	     ["홍길동", "이순신", "임꺽정"]		-> value 이자 배열로 불린다.
				*/
				//1. JSONObject 형식의 문자열을 만들어서 jsonStr변수에 저장
				let jsonStr = '{ "name" : ["홍길동", "이순신", "임꺽정"] }';				
				/*
			 	참고.  JSON.parse() 메소드 
			 		  - parse()메소드는  문자열을 JSONObject로 변환하여 반환 해줍니다.
			 		    즉!  '{"name":["홍길동", "이순신", "임꺽정"]}' 문자열?
			 		         {"name":["홍길동", "이순신", "임꺽정"]} <- JSONObject로 변환 해서 반환 해줍니다.
			     */
				//2.  1.에서 작성한 jsonStr변수에 저장된 JSONObject형식의 문자열을 실제 JSONObject로 변환해서 저장
				let  jsonObject = JSON.parse(jsonStr);
			 		         
			 	//   {"name":["홍길동", "이순신", "임꺽정"]} 
			 	//      key :       배열 값
			 	
			 	//3. 위 jsonObject 변수에 저장된 실제 JSONObject객체{}에서 "name" key로  [...] value로 얻어
			 	//	 브라우저로 보여줄 데이터를 만들자.
			 	let output = "회원이름<br>";
			 		output += "================<br>";
			 		
			 		//		변수	 in ["홍길동", "이순신", "임꺽정"]
			 		//                 0        1       2     index
			 		for(let  i   in  jsonObject.name){
			 			
			 			output += jsonObject.name[i]; //첫번째 반복실행시 "홍길동" 누적
			 										  //두번째 반복실행시 "이순신" 누적
			 										  //세번째 반복실행시 "임꺽정" 누적					
			 		}
			     	//id=output 작성된 <div id="output"></div>한쌍을 선택해서 
			     	//<div id="output"></div>의 콘텐츠영역 사이에  output변수에 누적된 전체 문자열을 보여주자.
				    $("#output").html(output);	     
			});
			
		});   
	</script>
</head>
<body>
	<a id="checkJson" style="cursor: pointer;">출력</a> <br><br>

	<div id="output"></div>

</body>
</html>





