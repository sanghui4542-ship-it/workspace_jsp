<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%-- 1. MemberVO객체 생성 후 setName메소드를 호출하여 name 인스턴스변수에 "이순신" 저장 --%>
	
	<%-- jsp:useBean 액션태그를 이용해 MemberVO클래스의 객체(빈) 생성 --%>
	<jsp:useBean id="membervo"  class="sec01.ex01.MemberVO"    scope="page" />

	<%-- jsp:setProperty 액션태그를 이용해   
		 MemberVO 객체의 setName 메소드 호출시 매개변수로 "이순신" 문자열을 전달해 name 인스턴스변수에 저장 --%>
    <jsp:setProperty   name="membervo"  property="name"  value="이순신" />
    
<%-- 2. jsp:useBean 액션태그를 이용해 java.util 패키지에 만들어져 있는 ArrayList 클래스에 대한 배열 객체(빈) 생성 --%>    
	<jsp:useBean id="arrayList"  class="java.util.ArrayList"  scope="page" />

	empty 연산자를 이용하여 EL 태그영역에 출력 <br>
	
	<h2>
		<%--
			 MemberVO 객체 메모리의 모든 인스턴스변수에 값이 저장되어 있지 않느냐? 라고 물어볼때 ${empty 참조변수명} 작성합니다.
		
			 모든 인스턴스변수에 값이 저장되어 있지 않으면? empty연산자는 true를 반환해 주고,  값이 하나라도 저장되어 있으면? false를 반환 해줍니다.
		 --%>
		 ${empty  membervo} <br> <!--  false 출력 -->
		 
		 <%-- MemberVO 객체의 모든 인스턴스변수에 값이 하나라도 저장되어 있느냐? 라고 물어볼때 ${not empty new MemberVO()} 사용하면 됩니다.--%>
		 ${not empty membervo} <br> <!-- true 출력 -->

		<%-- empty 연산자 뒤에 작성한 arrayList 배열에 값이 저장되어 있지 않느냐?(비어 있느냐? 없느냐?)라고 물어 볼때 ${empty 참조변수명} 		     
			결과 -> 위 코드에서 jsp:useBean 액션태그로 ArrayList 배열만 만들어 놓고 값은 저장하기 않았기때문에 true 참의 결과가 출력됨    
		--%>
		 ${empty arrayList} <br> <!-- true 출력 -->
		 
	    <%-- empty 연산자 뒤에 작성한 arraylist 배열에 값이 하나라도 저장되어 있느냐?    
	  	     결과->  값이 하나 이상 저장되어 있지 않기 떄문에 false 를 반환해 EL로 출력해 준다.
	 	 --%>
		 ${not empty arrayList} <br> <!-- false 출력 -->
		
		 <br><br>
		 
	    ${ empty "hello"} <br> <%-- 문자열 객체 메모리 내부에 문자열값 hello이 저장되어 있지 않느냐? false를 반환 해 서 EL태그영역에서 출력합니다. --%>
	    <br>
	    
	    ${ empty null} <br>    <%-- empty 연산자 뒤에 null을 작성하면 무조건! true를 반환해서 EL태그영역에서 출력합니다.  --%>
	    <br>
	    
	    ${ empty ""} <br>      <%-- 문자열 객체 메모리 내부에 문자열값이 저장되어 있지 않느냐? true를 반환 해서 EL태그영역에서 출력합니다. --%>
	    <br>
			 
		 
		 
		 
		 
	</h2>
	
	
	
	
	
	
	
	
	
	
	
	
	
	