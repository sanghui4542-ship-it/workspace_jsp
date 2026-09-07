<%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.io.BufferedReader"%>
<%--
================================================================================
 파일명 : FetchAjax/save.jsp
 역할   : 3.html 이 POST 방식으로 보낸 JSON 본문을 읽어서 그대로 되돌려 줌
================================================================================

 참고. 위 두 번째 줄 <%@ page import="..." %> 의 의미

   자바에서 다른 패키지의 클래스를 쓰려면 import 가 필요합니다.
   jsp 에서는 이 지시자로 import 를 작성합니다.
   자바 파일의  import java.io.BufferedReader;  와 같은 뜻입니다.

 참고. fetch 로 POST 전송한 JSON 본문을 읽는 방법 (가장 중요한 부분)

   3.html 에서 아래처럼 보냈습니다.
     body : JSON.stringify({ title:"제목", content:"내용" })

   이때 서버에서는 request.getParameter("title") 로 읽을 수 없습니다.
   폼 형식(title=제목&content=내용)이 아니라
   JSON 문자열 덩어리 하나로 전송되었기 때문입니다.

   따라서 request.getReader() 로 본문 전체를 직접 읽어야 합니다.

 참고. 두 방식의 비교

   [폼 형식으로 보낸 경우]
     본문 :  title=제목&content=내용
     읽기 :  request.getParameter("title")          <- 톰캣이 자동으로 분해해 줌

   [JSON 으로 보낸 경우]
     본문 :  {"title":"제목","content":"내용"}
     읽기 :  request.getReader() 로 통째로 읽은 뒤 직접 해석
--%>
