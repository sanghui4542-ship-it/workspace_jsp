

<%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%--
================================================================================
 파일명 : JQueryAjax/calc2.jsp
 역할   : 값 2개를 받아 계산한 뒤 "JSON 형태" 로 응답하는 서버 페이지
================================================================================

 참고. contentType 을 application/json 으로 지정한 이유

   text/html 로 지정하면 브라우저는 HTML 문서로 인식합니다.
   JSON 데이터임을 정확히 알려 줘야
   jQuery 의 dataType:"json" 과 fetch 의 res.json() 이 안정적으로 동작합니다.

 참고. calc.jsp 와 calc2.jsp 의 차이

   calc.jsp   응답 :  30
                      -> 값이 하나뿐이라 "무엇의 결과인지" 알 수 없습니다.

   calc2.jsp  응답 :  { "v1":10, "v2":20, "result":30 }
                      -> 이름표가 붙어 있어 여러 값을 한 번에 보낼 수 있습니다.
                      -> 실무에서는 거의 항상 이 JSON 방식을 사용합니다.
--%>

<%
	//1. 요청 데이터의 한글 문자 인코딩 방식을 UTF-8 로 설정
	request.setCharacterEncoding("UTF-8");

	//2. 2.html에서 보낸 값 2개를 request 에서 꺼내 숫자로 변환해서 저장
	// http://localhost:8181/pro16_Ajax7/JQueryAjax/calc2.jsp?v1=10&v2=20
	int v1 = Integer.parseInt(request.getParameter("v1"));
	int v2 = Integer.parseInt(request.getParameter("v2"));
	
	//3.  JSON 형태의 글자를 직접 작성해서 응답합니다.
	//요청한 2.html 내부에 작성한  success : function(response){} 의 response 매개변수로 {.......} 전체 전달 됨 
%>
{
	"v1": <%=v1%>,
	"v2": <%=v2%>, 
	"result" : <%=v1+v2%>
}












