<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<h2>여러가지 논리 연산자를  EL 표현언어의 태그 내부에 작성</h2>
	<%--
		EL 표현 언어의 태그 내부에 논리 연산자 작성 방법
		
		1.  && 논리 연산자는  and 논리연산자로 사용할수 있다.
		2.  || 논리 연산자는  or 논리 연산자로 사용할수 있다.
		3.  !  논리 연산자는  not 논리 연산자로 사용할수 있다.
	 --%>
	 
	 <%-- &&, and 연산자들은 연산 양쪽 데이터가 true일때만 true를 반환해서 EL태그로 출력합니다. --%>
	 ${ (10 == 10)  &&   (20 == 20) } <br>
	 <%--   true    &&       true  --%>
	 <%--          true            --%>
	 
	 ${ (10 eq 10)  and  (20 eq 20) } <br><br>
	 <%--   true    and      true  --%>
	 <%--          true            --%>
	 
	 <%-- ||, or 연산자들은 양쪽의 연산식의 결과값 중 하나라도 true결과가 나오면 true를 반환하여 EL태그로 출력합니다. --%>
	 ${ (10 == 10)  ||  (20 != 30)  } <br>
	 <%--   true    ||      true   --%>
	 <%--          true            --%>
	 
	 ${ (10 eq 10)  or  (20 ne 30)  } <br><br>
	 <%--   true    or      true   --%>
	 <%--          true            --%>
	 
	 <%-- !, not 연산자들은 논리 부정 연산자로  반대의 결과를 계산해서 반환한 true 또는 false를  EL태그로 출력합니다. --%>
	 ${   !(20  != 10)   } <br>
	 <%-- !(    true ) --%>
	 <%--     false    --%>
	 
	 ${  not(20 ne 10)   } <br>
	 <%--not(  true  ) --%>
	 <%--     false    --%>
	 
	 
	 
	 
</body>
</html>







