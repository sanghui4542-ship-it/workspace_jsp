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
<%
	//===============================================================
	//1. 요청 본문을 한 줄씩 읽어 담을 공간 만들기
	//===============================================================
	//   StringBuilder 는 글자를 계속 이어붙일 때 쓰는 자바 클래스입니다.
	//
	//   참고. String 대신 StringBuilder 를 쓰는 이유
	//        String 은 + 로 이어붙일 때마다 새 객체를 계속 만들어 냅니다.
	//        반복문 안에서 String 을 이어붙이면 성능이 크게 떨어집니다.
	//        StringBuilder 는 하나의 공간에 계속 덧붙이므로 훨씬 빠릅니다.
	StringBuilder sb = new StringBuilder();

	//===============================================================
	//2. 요청 본문을 읽는 통로를 얻어 끝까지 반복해서 읽기
	//===============================================================
	//   request.getReader()  ->  요청 본문을 글자로 읽는 통로를 돌려줍니다.
	//
	//   참고. try(자원) { } 형태를 try-with-resources 라고 부릅니다.
	//        소괄호 안에서 연 자원은 블록이 끝나면 자동으로 닫힙니다.
	//        finally 에 br.close() 를 직접 쓰지 않아도 되므로 안전합니다.
	try (BufferedReader br = request.getReader()) {

		//읽어 온 한 줄을 임시로 담을 변수
		String line;

		//readLine() 은 한 줄을 읽어 돌려주고,
		//더 읽을 줄이 없으면 null 을 돌려줍니다.
		//
		//  (line = br.readLine()) != null
		//    -> 한 줄을 읽어 line 에 넣고, 그 값이 null 이 아니면 반복을 계속합니다.
		//    -> null 이 나오는 순간 반복이 끝납니다.
		while ((line = br.readLine()) != null) {
			sb.append(line);   //읽은 줄을 뒤에 이어붙입니다.
		}
	}

	//===============================================================
	//3. 읽어들인 JSON 문자열 전체
	//===============================================================
	//   sb.toString() 은 StringBuilder 에 담긴 내용을 String 으로 바꿔 줍니다.
	//   예 : {"title":"제목입니다","content":"내용입니다"}
	String jsonBody = sb.toString();

	//===============================================================
	//4. 서버 처리 시간을 눈으로 확인하기 위한 의도적인 지연 (1초)
	//===============================================================
	//   이 1초 동안 버튼이 잠겨 있는지 직접 연타해서 확인해 보세요.
	//   주의. 실제 프로젝트에서는 반드시 지워야 하는 코드입니다.
	Thread.sleep(1000);

	//===============================================================
	//5. 저장 결과를 JSON 으로 응답
	//===============================================================
	//   실제 프로젝트라면 여기서 DAO 를 호출해 DB 에 insert 합니다.
	//
	//     BoardDAO dao = new BoardDAO();
	//     dao.insertBoard(제목, 내용);
	//
	//   참고. received 의 값에 따옴표를 붙이지 않은 이유
	//        jsonBody 자체가 이미 { } 로 된 JSON 덩어리이기 때문입니다.
	//        따옴표로 감싸면 객체가 아니라 문자열 `{"success":true, "received":<%=jsonBody% >}`로 전달되어
	//        화면에서 json.received.title 로 꺼낼 수 없게 됩니다. 
%>
{"success":true, "received":<%=jsonBody%>}








