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
</head>
<body>
		<c:set  var="title1" value="hello world!"/>
							<%--    0123456789    index --%>
		
		<c:set  var="title2"  value="쇼핑몰 중심 JSP입니다!"/>	 

		<c:set  var="str1"  value="중심"/>

		<h2>여러가지 문자열 관련 처리 함수들</h2>
		
<%-- contains함수 호출시       첫번째 매개변수로 전체 문자열을 전달하고,
				            두번째 매개변수로 찾을 문자열을 전달하면
				            전체 문자열에서 찾을 문자열이 있으면 true반환하고  없으면 false를 반환
 --%>
	fn:contains(title1, str1)  ->  ${ fn:contains(title1, str1)  } <br>  <%-- false 출력 --%>
	fn:contains(title2, str1) ->   ${ fn:contains(title2, str1)  } <br> <%-- true 출력--%>
	
<%-- indexOf함수 호출시  첫번째 매개변수자리에 찾을 문자열이 포함된 전체 문자열을 전달하고,
					  두번쨰 매개변수 자리에 찾을 문자열을 찾기 위해 전달하면!!!
					  만약 두번쨰 매개변수로 전달한 찾을 문자열을 찾으면?  그 문자열의 첫문자가 저장된 index위치 번호를 반환 합니다.
					  만약 찾지 못하면? -1을 반환 합니다.
 --%>
	fn:indexOf(title2, str1)  -> ${   fn:indexOf(title2, str1)  } <br> <%-- index->  4 --%>	
	
 <%-- replace 함수 호출시  첫번째 전달값으로 전체 문자열을 전달하고,
					    두번쨰 값으로 첫번째로 전달한 전체 문자열중에서 바뀌게될 문자를 전달합니다.
					    세번째 값으로 수정할 문자을 전달 하게 되면
					    바뀌게 될문자들이 모두~ 수정할 문자로 변경된  전체문자열을 반환해 줍니다. 
 					
 					   "hello world!" -> "hello/world!"
 --%>	
	fn:replace(title1, " ", "/") -> ${ fn:replace(title1, " ", "/") } <br> <%--'hello/world!'--%>	

	
<%-- fn :trim 함수 호출시 매개변수로 문자열을 전달하면 양쪽에 빈 공백이 있으면? 양쪽 공백만 제거 후 제거된 문자열을 반환해 줌  --%>
	fn:trim('    안녕  하   세  요     ') ->  ${   fn:trim('    안녕  하   세  요     ')  } <br> <%-- '안녕 하  세  요' --%>	
	
	
 <%-- fn:substring함수 호출시  첫번쨰 전달 값으로 자를 전체 문자열 hello world!을 전달하고,
  						     두번째 전달값으로 자를 문자의 시작 index위치를 전달하고
  						     세번째 전달값으로 자를 문자의 그다음 index위치를 전달하면
  						     두번째로 전달한 자를 문자가 저장된 index위치의 문자부터
  						     세번쨰 전달한 index이전위치의 문자까지를 잘라서 반환해줍니다.
  						  
  						    여기서는 3 index위치의 l부터  5 index ""빈공백 까지의 문자를 문자열로 잘라
  						 	 "lo " 를 반환합니다.  그러므로 문자의 갯수는 총 3입니다.				  
    --%>
	fn:substring(title1, 3, 6) -> ${ fn:substring(title1, 3, 6) } <br>  <%--  "lo "  --%>
	<%--
										"hello world!"
										 01234567
	 --%>	
	 
    <%-- fn:length함수 호출시  문자열을 매개변수로 전달하면  전달한 전체 문자열의 총 문자갯수를 세어 반환 합니다.--%>
	fn:length(title1) -> ${ fn:length(title1) } <br> <%-- 12 --%>
	
    <%-- fn:toUpperCase함수 호출시 문자열을 매개변수로 전달하면 모든 소문자들을 대문자로 변경후 변경된 전체 대문자의 문자열을 반환합니다. --%>
	fn:toUpperCase(title1) -> ${ fn:toUpperCase(title1) } <br> <%-- HELLO WORLD! --%>

    <%-- fn:toLowerCase함수 호출시 문자열을 매개변수로 전달하면 모든 대문자들이 있으면? 모두 소문자로 변경한 전체 소문자의 문자열을 반환합니다. --%>
	fn:toLowerCase(title1) -> ${ fn:toLowerCase(title1) } <br> <%-- hello world! --%> 
	 
	 
	 
	 
</body>
</html>








