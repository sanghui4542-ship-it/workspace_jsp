<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%-- JSTL 전체 라이브러리에 속한 core, fmt, functions 라이브러리 태그들을 사용하기 위해 요청 주소 작성 --%>    
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"       prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"        prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions"  prefix="fn" %>

<% request.setCharacterEncoding("UTF-8"); %>             
    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
	<style type="text/css">
		/*
			 class="highlight" <span>태그를 선택해서 
			 - 텍스트 색상을 빨강으로 설정   -> color:red;
			 - 글자 굵기를 굵게 표시       -> font-weight:bold;
			 
			 예 : <span class="highlight">JSP</span> 의  JSP키워드 강조 표시용 스타일 
		*/
		.highlight {
			color:red; 
			font-weight: bold;
		}		
	</style>
</head>
<body>
	<h2>JSP기반 텍스트 분석기</h2>

	<%--
			클라이언트가 텍스트를 입력하여 fnTest2.jsp 서버페이지를 요청하는 폼 디자인 
			
			요청 URL : http://localhost:8181/pro14/test05/fnTest2.jsp
	 --%>
	 <form    method="post">
	 
	 	<label>분석할 문장을 입력하세요:</label>
	 		
	 	<%-- 클라이언트가 입력한 요청 데이터를 톰캣서버가 실행하는 fnTest2.jsp로 request 내장객체에 담아 전송
	 		 처음 <input>에 입력한 값을 유지하면서 브라우저로 보여지게 하려면? value속성에 request 내장객체에서 얻은 값을 다시 설정!
	 	  --%>
	 	<input type="text" name="inputText" value="${param.inputText}" />
	 
	 	<button type="submit">분석요청하기</button>
	 
	 </form>
	 
	 <%-- 클라이언트가 입력한 텍스트가 있을 경우 실행! --%>
	 <c:if test="${not empty param.inputText}">
	 			
	 	  <%-- 클라이언트가 입력한 텍스트를 얻어 text 변수를 만들어 저장 --%>       
	 	  <c:set  var="text" value="${param.inputText}" />    
	 	  
	 	  <%-- 검색할 키워드 목록을 콤마(,)로 구분된 전체 문자열을 만들어 keywords변수에 저장  --%>  
	 	  <c:set var="keywords" value="JSP,Java,Spring" />
	 
	 	  <h3>입력된 문장 : ${text}</h3>
	 	  
	 	  <ul>
	 	  	  <%-- 1. 입력한 전체 문자열의 총 문자 갯수 얻어 출력 --%>
	 	  	  <li>
	 	  	  	 <b>입력받은 전체 문자열의 총 문자 갯수 출력</b>
	 	  	  		
	 	  	  	  ${ fn:length(text)  } 문자  <%-- fn:length() 함수로 문자열의 총 문자 갯수를 얻자 --%>
	 	  	  </li>
	 	  	  
	 	  	  <%-- 2. 입력한 전체 문자열에 대문자들을 소문자들로 변경후 얻어 출력 --%>
	 	  	  <li>
	 	  	  	 <b>입력받은 전체 문자열의 대문자들을 소문자로 변경후 얻어 출력</b>
	 	  	  	
	 	  	  	 ${ fn:toLowerCase(text) }   <%-- fn:toLowerCase() 함수로 모두 소문자로 변경한 문자열을 얻자  --%>	 	  	  	
	 	  	  </li>
	 	  	  
	 	  	  <%--3. 공백 문자를 밑줄_  문자로 변경 후 얻어 출력 --%>
	 	  	  <li>
	 	  	  	<b>입력받은 전체 문자열에 공백 문자가 있으면 밑줄_ 기호로 변경 한 전체 문자열을 얻어 출력</b>
	 	  	  	
	 	  	  	 ${ fn:replace(text,' ','_') } 
	 	  	  </li>
	 	  	  
			<%-- 4. 특정 키워드가 전체문자열에 포함되어 있는지 여부 확인해서 출력 
					
				 	<c:set var="keywords" value="JSP,Java,Spring" />
				 	
				 								["JSP,"Java","Spring"]
			--%>	 	  	  
	 	  	  <li>
	 	  	  		<b>키워드 포함 여부 :</b><br>
	 	  	  		
	 	  	  		<c:forEach var="keyword"   items="${ fn:split(keywords, ',')  }"  >
	 	  	  		  					  <%-- items=["JSP","Java","Spring"] --%> 
	 	  	  		  					  
	 	  	  		  					 <%-- 
 		 							  		fn:contains() 함수를 사용하여 전체문자열에서 키워드 포함여부 확인할수 있다.
 		 							  		
 		 							  		입력한 텍스트 전체 문자열에서 
 		 							        "JSP" 또는 
 		 							        "Java" 또는 
 		 							        "Spring"가 포함된 문자열이냐?    --%>	  
	 	  	  		  					  
	 	  	  		  		"${keyword}"  : ${ fn:contains(text, keyword) ? "포함됨" :  "없음"  } <br>			  
  	  		  							
	 	  	  		</c:forEach>
	 	  	  </li>
	 	  	  
	 	  	  <%-- 6. 문자열 뒤집기 --%>
	 	  	  <li>
	 	  	  		<b>문자열 뒤집기:</b>
	 	  	  
	 	  	  		<%-- reversedText 변수 선언 , 초기 저장하는 값으로 ""(빈문자열)로 저장
	 	  	  			 -> 뒤집어 진 문자열을 누적해서 저장할 공간
	 	  	  		 --%>
	 	  	  		 <c:set var="reversedText" value="" />
	 	  	  
			    <%-- 
			        c:forEach 반복문
			
			        begin="0"
			        → 첫 번째 문자 index 번호부터 시작
			
			        end="${fn:length(text)-1}"
			        → 입력된 문자열의 전체 길이 -1 만큼 반복
			        → 이유 : index는 0부터 시작하기 때문
			
			        예:
			        text="JSP"
			        length=3
			        index = 0,1,2 까지 반복
			    --%>			 	  	  
	 	  	  		 <c:forEach var="i" begin="0"   end="${fn:length(text)-1}"     step="1">
	 				     <%--
							 문자열 뒤집기 해심 로직
							 
							 	fn:substring(text, i, i+1)
							 	-> text 우리가 입력해 요청한 문자열 "JSP"일경우  i index위치번째 문자 1개를 잘라내어옴 
							 	예: text = "JSP"
							 	    i=0 -> "J"
							 	    i=1 -> "S"
							 	    i=2 -> "P"
							 	    
							 	${fn:substring(text, i, i+1)}  ${reversedText}    
							 	->                     새 문자 + 기존 문자열 앞에 붙이기 
							 	
					 기존 방식 : 
							    reversedText = reversedText + 새 문자 
							    (뒤에 새문자를 붙이면 순서는 유지됨) 							 	
					 현재 방식 : 
							 	reversedText = 새 문자 + reversedText
							 	(앞에 새문자를 붙이면 순서가 뒤집힘)    
	 	  	  		 	  --%>
	 	  	  		 	<c:set var="reversedText" value="${fn:substring(text, i, i+1)}${reversedText}" />
	 	  	  		 		 	
	 	  	  		 </c:forEach>
	 	  	  		 
	 	  	  		 <%-- 최종적으로 완성된 뒤집힌 문자열 출력 --%>
	 	  	  		 ${reversedText}
	 	  	  
	 	  	  </li>
	 	  	  
	 	  	  
	 	  	  
	 	  	 
	 	  </ul>
	 </c:if>
	 
	 
	 



</body>
</html>


















