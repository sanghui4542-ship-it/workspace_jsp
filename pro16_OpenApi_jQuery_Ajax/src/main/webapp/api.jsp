<%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.io.BufferedReader"%>
<%@ page import="java.io.InputStreamReader"%>
<%@ page import="java.net.HttpURLConnection"%>
<%@ page import="java.net.URL"%>
<%@ page import="java.net.URLEncoder"%>
<%--
================================================================================
 파일명 : api.jsp
 역할   : 브라우저 대신 외부 API 를 호출해 주는 프록시(중계) 서버 페이지
================================================================================

 참고. 프록시가 필요한 이유 3가지

   1) CORS 차단 우회
      브라우저는 다른 도메인의 서버를 직접 부르는 것을 기본적으로 막습니다.
      상대 서버가 허용 표시(access-control-allow-origin)를 보내 줘야만 통과됩니다.
      허용하지 않는 API 는 브라우저에서 절대 직접 호출할 수 없습니다.
      서버끼리의 통신에는 이 제한이 없으므로 프록시를 쓰면 해결됩니다.

   2) API 키 숨기기
      키가 필요한 API 라면 키를 이 파일 안에 넣어 두면 됩니다.
      브라우저는 키를 전혀 볼 수 없습니다.

   3) 응답 가공
      외부 API 가 준 값에서 필요한 것만 골라 내보낼 수 있습니다.
      불필요한 데이터를 걸러 통신량을 줄입니다.

 참고. 이 파일의 동작 순서

   [브라우저]  api.jsp?type=coin&markets=KRW-BTC
       |
       V
   [우리 톰캣서버]  api.jsp
       |
       | type 값을 보고 어느 API 를 부를지 결정
       V
   [외부 API 서버]  업비트 또는 환율 서버
       |
       | JSON 응답
       V
   [우리 톰캣서버]  받은 JSON 을 그대로 전달
       |
       V
   [브라우저]  화면 갱신

 참고. 호출 방법

   api.jsp?type=coin&markets=KRW-BTC,KRW-ETH   -> 업비트 시세
   api.jsp?type=rate                            -> 환율
--%>
<%
	//===============================================================
	//1. 요청 데이터의 한글 인코딩 설정 (getParameter 보다 먼저)
	//===============================================================
	request.setCharacterEncoding("UTF-8");

	//===============================================================
	//2. 브라우저가 보낸 값 꺼내기
	//===============================================================
	//   type    : 어느 API 를 부를지 구분하는 값
	//   markets : 코인 종목 코드 (type 이 coin 일 때만 사용)
	String type    = request.getParameter("type");
	String markets = request.getParameter("markets");

	//   값이 없을 때를 대비한 기본값 처리
	//   null 검사를 하지 않으면 아래에서 NullPointerException 이 발생합니다.
	if (type == null || type.trim().equals("")) {
		type = "rate";
	}
	if (markets == null || markets.trim().equals("")) {
		markets = "KRW-BTC";
	}

	//===============================================================
	//3. type 값에 따라 호출할 주소를 결정
	//===============================================================
	//   URLEncoder.encode(값, "UTF-8")
	//     -> 공백이나 한글, 특수문자가 있어도 안전한 글자로 바꿔 줍니다.
	//     -> 자바스크립트의 encodeURIComponent 와 같은 역할입니다.
	//
	//   참고. "coin".equals(type) 순서로 쓴 이유
	//        type 이 null 이어도 안전하게 false 를 돌려주기 때문입니다.
	//        type.equals("coin") 은 null 일 때 오류가 납니다.
	String apiUrl;

	if ("coin".equals(type)) {
		apiUrl = "https://api.upbit.com/v1/ticker?markets="
		       + URLEncoder.encode(markets, "UTF-8");
	} else {
		apiUrl = "https://open.er-api.com/v6/latest/USD";
	}

	//===============================================================
	//4. 외부 API 호출
	//===============================================================
	//   StringBuilder 는 글자를 계속 이어붙일 때 쓰는 클래스입니다.
	//   String 을 + 로 이어붙이면 반복할 때마다 새 객체가 생겨 느립니다.
	StringBuilder sb = new StringBuilder();

	HttpURLConnection conn = null;

	try {
		//4.1 주소 객체를 만들고 연결 통로를 엽니다.
		URL url = new URL(apiUrl);
		conn = (HttpURLConnection) url.openConnection();

		//4.2 요청 방식과 시간 제한을 설정합니다.
		//
		//    setConnectTimeout : 연결이 맺어질 때까지 기다릴 최대 시간 (밀리초)
		//    setReadTimeout    : 응답을 읽을 때까지 기다릴 최대 시간 (밀리초)
		//
		//    이 설정이 없으면 상대 서버가 멈췄을 때 우리 서버까지 함께 멈춥니다.
		//    외부 API 를 호출할 때 반드시 지정해야 하는 항목입니다.
		conn.setRequestMethod("GET");
		conn.setConnectTimeout(5000);
		conn.setReadTimeout(5000);

		//4.3 일부 서버는 브라우저가 아닌 요청을 거절합니다.
		//    요청자 정보를 함께 보내면 대부분 정상 처리됩니다.
		conn.setRequestProperty("Accept", "application/json");
		conn.setRequestProperty("User-Agent", "Mozilla/5.0");

		//4.4 상태코드 얻기
		int status = conn.getResponseCode();

		//4.5 상태에 따라 읽을 통로를 다르게 선택합니다.
		//
		//    정상(200번대)  -> conn.getInputStream()
		//    오류           -> conn.getErrorStream()
		//
		//    오류일 때 getInputStream() 을 쓰면 예외가 발생하므로 구분해야 합니다.
		//    오류 응답에도 원인이 적혀 있으므로 그대로 읽어 전달합니다.
		BufferedReader br;
		if (status >= 200 && status < 300) {
			br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		} else {
			br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
		}

		//4.6 응답을 끝까지 한 줄씩 읽어 이어붙입니다.
		//    readLine() 은 더 읽을 줄이 없으면 null 을 돌려줍니다.
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}

		br.close();

	} catch (Exception e) {
		//통신 자체가 실패한 경우입니다. (인터넷 끊김, 주소 오류, 시간 초과 등)
		//톰캣 콘솔에 원인을 출력하고, 브라우저에는 JSON 형태로 알려 줍니다.
		e.printStackTrace();
		sb.setLength(0);
		sb.append("{\"error\":true, \"message\":\"외부 API 호출에 실패했습니다\"}");

	} finally {
		//성공하든 실패하든 연결을 반드시 끊습니다.
		//끊지 않으면 연결이 계속 쌓여 서버가 느려집니다.
		if (conn != null) {
			conn.disconnect();
		}
	}

	//===============================================================
	//5. 받은 JSON 을 그대로 브라우저에 전달
	//===============================================================
	//   여기서 값을 가공하지 않고 그대로 넘기므로
	//   화면 코드는 직접 호출할 때와 거의 같습니다.
	out.print(sb.toString());
%>
