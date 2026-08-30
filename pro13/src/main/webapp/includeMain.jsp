<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%-- 현재 페이지 : includeMain.jsp   --%>    
<%
	/*
	주제 : 두 외부 파일(OuterPage1.jsp, OuterPage2.jsp)을  
	     include 디렉티브 태그와  include 액션태그로 
	     각각 태그 작성 하면  어떤 차이가 있는지 알아보겠습니다.
	*/	

	//1. 포함시킬 외부 파일들의 경로를 변수에 저장합니다.
	String outerPath1 = "./inc/OuterPage1.jsp";  
	String outerPath2 = "./inc/OuterPage2.jsp";
	
	//2. page 내장객체 영역과  request 내장객체 영역에 각각 key-value 한쌍의 형태로 묶어 저장(바인딩)
	//바인딩 이유 : 여기서 각 내장객체 영역에 바인딩한 데이터를 두 외부 파일에서 읽어 올수 있는지 확인하기 위한용도로 바인딩.
	pageContext.setAttribute("pAttr", "동명왕");
	request.setAttribute("rAttr", "온조왕");
	
	//참고.  page 내장객체 영역 - 하나의 jsp페이지내부에서 공유할 데이터가 있으면 바인딩 하고 그 jsp페이지 내부에서만 공유
	//		request 내장객체 영역 - 다른 jsp페이지 요청하면 공유할 데이터가 계속 공유
%>   
<h1> include 디렉티브 태그와  include 액션태그 동작 방식 비교 </h1>

<%-- 1. include 디렉티브 태그 사용 --%>
	 <h3>include 디렉티브 태그를 사용하여 외부 페이지 OuterPage1.jsp 파일에 작성한 코드를 현재 위치 행 줄에 포함시키기</h3>
	 <%@ include file="./inc/OuterPage1.jsp"%>
	
	 <%--표현식 태그 영역을 사용해서 외부 페이지 경로 작성 불가능하니 컴파일 에러 발생 --%>
	 <%-- <%@ include file="<%= 외부페이지경로 %>" %> --%>

	 <%--OuterPage1.jsp 외부파일에 작성한 newVar1변수 값을 현재 페이지에서 얻어 출력 가능? 가능 
	 
	 	이유 : include 디렉티브 태그를 위에 작성 해서 사용했기떄문에 includeMain.jsp를 실행하기 전에 
		      외부파일 OuterPage1.jsp의 코드를 포함시켜 한번만 컴파일을 진행 하므로 
		     includeMain.jsp와 OuterPage1.jsp 페이지의 코드는 결국 하나의 페이지의 코드로 합체 되어 
		     includeMain.jsp 하나의 파일만????? includeMain_jsp.java로 변환 되고 컴파일되어 
		     includeMain_jsp.class 실행파일이 만들어져 실행 됩니다. 
	 --%>
	 <p>외부 파일 OuterPage1.jsp에 작성한 변수값 : <%=newVar1%>  </p>
<%-- --------------------------------------------------------------------------------------------------- --%>

<%-- 2. include 액션 태그 사용 --%>
		
<h3>include 액션태그를 사용하여 외부페이지 OuterPage2.jsp에 작성 해 놓은 코드의 실행 결과를 웹브라우저 화면에 보여줄때 포함시켜 보이게 하기</h3>

	<%-- include 액션태그를 사용하여 외부 페이지의 코드 실행결과를 웹브라우저에 보내어 포함시켜 보이게 하기  --%>
	<jsp:include  page="./inc/OuterPage2.jsp" />
   
   	<%-- 표현식 태그 영역을 사용해서 외부 페이지 경로   "./inc/OuterPage2.jsp"   설정 가능! --%>
    <jsp:include page="<%=outerPath2%>" />
   
   	 <%-- 에러 발생 이유 :
   	  include 액션태그 사용 방식에는 
      각각 includeMain.jsp, OuterPage2.jsp 파일이 컴파일 되어 includeMain_jsp.class, OuterPage2_jsp.class 만들어져서 실행되며
      외부파일 코드를 실행할때 재요청을 통해 외부파일에서 실행되어 브라우저로 전송된 결과값만 가져와 브라우저에 보여주기 떄문.                                 
	 --%>
    <p>외부 파일 OuterPage2.jsp에 작성한 변수값 : <%-- <%=newVar2%> --%>   </p>
   
   
   
   
   
 
   