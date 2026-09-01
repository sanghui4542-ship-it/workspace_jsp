


<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%--
	  <c:forEach> 태그 
	  - 반복을 위해 사용되는 태그
	  - 자바 언어의 문법 for문과 같이 두가지 형태의 기능을 제공하는 태그 
	       첫번째 형태 :    일반 for(초기식;  조건식;  증감식){...}
	       두번쨰 형태  :  향상된 for(변수선언   :  배열){.....}

	  - 첫번째 형태 
	  	
	  		<c:forEach var="현재 반복중인 값을 저장할 변수이름" 
	  		           begin="반복 시작조건값 설정(초기값설정)"
	  		           end="반복 끝 조건값 설정(최대값설정)"
	  		           step="반복 할때 증가될 값 설정(기본값:1)  
	  		           varStatus="반복상태정보를 저장하는 변수이름">
	  		
	  			반복 실행할 코드 작성 
	  			
	  		</c:forEach>
	  		
	  - 두번째 작성 형태
	  		
	  		<c:forEach  var="순회할배열의 0 index ~ 끝 index 위치칸에 데이터를 반복해서 얻어 저장할 변수명"
	  					items="순회할배열">
	  			
	  			배열에서 차례로 얻은 데이터를 사용해서 반복할 코드 작성 
	  		
	  		</c:forEach>		
 --%>       
    
<%--  JSTL의  core 라이브러리 태그들을 사용하기 위해 외부 사이트에서 불러오는 taglib 디렉티브 태그 한줄 작성 --%>    
<%@ taglib  uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>    
    
<%
	//순서1. member6.jsp 요청한 한 글문자 인코딩 방식 UTF-8 설정
	request.setCharacterEncoding("UTF-8");

	//순서2. 자바 언어의 코드로  ArrayList 배열 생성후  문자열들 저장
	List  dataList = new ArrayList();
		  dataList.add("hello");
		  dataList.add("world!");
		  dataList.add("안녕하세요!");
%>     
<%-- JSTL 의  c:forEach 태그에서  EL ${}을 사용할수 있도록   
	 list 변수를 하나 만들고  위 dataList참조변수에 저장된 ArrayList배열의 주소번지를 저장 합니다.--%>
<c:set var="list" value="<%=dataList%>" />	 
	 
<%-- 순서3.  첫번째 작성형태의 일반 for로 작성하는  <c:forEach> 태그를 사용해
		    변수 i를 선언 후 초기값으로 1로 저장 후  10이 될때까지  1씩 증가시키면서  10번반복해서 i변수에 저장되는 값을 출력
		    그리고 현재 몇번 반복하고 있는지 상태 정보들이 저장되는 객체를 varStatus속성에 loop이름으로 설정해 정보들을 반복해서 출력할수 있다.
	 
	 		for(int i=1;   i<11;   i++){
	 		
	 			out.print("i변수에 저장된 값은" + <%=i%> + "입니다.<br>");
	 		}
 --%>
 <c:forEach var="i"  begin="1"  end="10"  step="1" varStatus="loop">
 
 	현재 ${loop.count}반복한 상태의  i변수에 저장된 값은 ${i}입니다. <br>
 
 </c:forEach>
<%--
	varStatus="loop"에서 loop는 c:forEach 태그의 반복 상태를 저장하는 변수입니다. 
	varStatus 속성을 사용하면 반복에 대한 다양한 정보를 얻을 수 있는데, 
	그 중 loop는 이 정보를 담고 있는 객체가 됩니다. 이 loop 객체를 통해 반복의 상태 정보를 참조할 수 있습니다.

	loop 객체가 제공하는 주요 속성들은 다음과 같습니다:	
		loop.index: 0부터 시작하는 현재 반복의 인덱스.
		loop.count: 1부터 시작하는 현재 반복 횟수.
		loop.first: 첫 번째 반복인지 여부 (Boolean).
		loop.last: 마지막 반복인지 여부 (Boolean).
		loop.even: 현재 반복이 짝수인지 여부.
		loop.odd: 현재 반복이 홀수인지 여부.
		
	위코드를 예를 들면, ${loop.count}는 현재 몇 번째 반복인지 나타내고, 
	               ${i}는 var="i"에 정의된 반복 변수 값을 참조합니다.
--%> 
 
 <hr>
<%--
		 일반 for문으로 초기식; 에 작성하는 초기변수를 i로 선언하고 1로 초기화
		 					            초기변수i의 값이 10이 될떄까지만 반복 합니다.
		 					            증감식에서 2씩 증가시키면서 
		 					      
		 					      반복실행될 코드는?
		 					      	5단을 반복해서 출력
		 					      	
		 					      	5  *  1 = 5  <br> 
		 					      	5  *  3 = 15 <br> 
		 					      	5  *  5 = 25 <br> 
		 					      	5  *  7 = 35 <br> 
		 					      	5  *  9 = 45 <br> 	
--%>
 <c:forEach var="i"  begin="1"  end="10"  step="2" varStatus="loop">
 
 	현재 ${loop.count}번 반복한 상태의 실행코드 ->  5  *  ${i} =  ${5  * i} <br>
 
 </c:forEach>
 
 <hr>
 <%
 	for(int i=1;  i<=10;  i+=2){
 %>				
 		5  *  <%=i%> =  <%= 5  * i%> <br>
 <%	
 	}
 %>
 
 <hr>
 	<%-- ArrayList배열에 저장된 문자열객체의 갯수만큼 반복해서  data변수에 저장하고 반복해서 출력! 
 	
	     							ArrayList배열
	     				   ["hello","world","안녕하세요!!"]
	     				      0        1        2         index
	    자바의 향상된 for
	     				      
	     for (변수선언   :  배열){
	     	  변수선언에 저장된 배열의 값 반복해서 실행할 코드
	     }				      
	     							ArrayList배열
	     				 items=["hello","world","안녕하세요!!"]
     --%>
 <c:forEach  var="data"  items="${list}" varStatus="loop">
 
 	 ${loop.count} 번째 반복하고 있는 상태에서 얻은 문자열 은?  ${data} 문자열 이다~~~~<br>
 
 </c:forEach>
 
 <hr>
 
 <c:set var="fruits" value="사과 파인애플 바나나 망고 귤" />
 
 <c:forTokens var="value"  items="${fruits}" delims=" ">
 
 	${value} <br>
 	
 </c:forTokens>
 
  
	 
    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>

</body>
</html>








