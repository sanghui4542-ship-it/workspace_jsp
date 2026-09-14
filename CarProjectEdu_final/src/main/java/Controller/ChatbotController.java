package Controller;   // 이 파일이 속한 패키지(=폴더) 이름. 실제 폴더 구조와 반드시 같아야 한다

import java.io.BufferedReader;   // 글자를 줄 단위로 읽는 도구 (외부 API 응답을 읽을 때 사용)
import java.io.IOException;   // 파일/네트워크 입출력이 실패했을 때의 예외
import java.io.InputStream;   // 바이트를 읽어 들이는 통로
import java.io.InputStreamReader;   // 바이트 통로를 글자 통로로 바꿔 주는 도구
import java.io.OutputStream;   // 바이트를 내보내는 통로
import java.io.PrintWriter;   // 응답 화면에 글자를 직접 찍어 보낼 때 쓰는 도구 (AJAX 응답에 사용)
import java.net.HttpURLConnection;   // 자바가 직접 다른 서버에 요청을 보낼 때 쓰는 도구
import java.net.URL;   // 인터넷 주소 하나를 나타내는 객체

import javax.servlet.ServletConfig;   // 서블릿의 설정값을 읽는 객체
import javax.servlet.ServletException;   // 서블릿이 처리 중 실패했을 때의 예외
import javax.servlet.annotation.WebServlet;   // 이 서블릿이 어떤 주소의 요청을 받을지 정하는 표시 (아래 클래스 위에 붙어 있다)
import javax.servlet.http.HttpServletRequest;   // 브라우저가 보낸 요청(주소·파라미터·세션)이 담긴 객체
import javax.servlet.http.HttpServletResponse;   // 브라우저에게 돌려줄 응답을 담는 객체

import org.json.simple.JSONArray;   // [ ... ] 형태의 JSON 목록을 만드는 도구
import org.json.simple.JSONObject;   // { "key": "value" } 형태의 JSON 을 만드는 도구
import org.json.simple.parser.JSONParser;   // JSON 글자를 자바 객체로 바꿔 주는 도구

import Service.CarService;   // 업무 규칙을 담당하는 Service 클래스
import Vo.CarListVo;   // 값을 담아 나르는 상자(VO) 클래스
import util.ReserveTextParser;   // 여러 곳에서 함께 쓰는 도우미 클래스

/*
 ============================================================
 ChatbotController.java - AI 렌트카 상담 챗봇 서블릿
 ============================================================
 
 [역할]
 - 사용자가 챗봇에 메시지를 보내면 이 서블릿이 받아서
 - OpenRouter를 통해 무료 AI 모델에게 질문을 전달하고
 - AI의 답변을 다시 사용자에게 돌려보내는 역할
 
 [사용하는 AI 서비스]
 - OpenRouter (https://openrouter.ai)
 - 무료 모델: openrouter/free (사용 가능한 무료 모델 자동 선택 라우터)
 - Gemini 직접 사용 대비 훨씬 높은 한도 제공
 
 ============================================================
 [비전공자를 위한 OpenRouter API 키 발급 가이드]
 ============================================================
 
 OpenRouter란?
   -> 여러 AI 모델을 하나의 창구에서 사용할 수 있는 서비스입니다.
   -> 무료 모델도 제공하여 비용 없이 AI를 사용할 수 있습니다.
 
 API 키란?
   -> AI 서비스를 사용하기 위한 "비밀번호" 같은 것입니다.
   -> 이 키가 있어야 AI에게 질문을 보낼 수 있습니다.
 
 [발급 순서]
 
   1단계: OpenRouter 사이트 접속
      -> 웹브라우저에서 https://openrouter.ai 입력 후 접속
      -> 오른쪽 상단 "Sign In" 클릭
      -> Google 계정(Gmail)으로 로그인
 
   2단계: API 키 만들기
      -> 로그인 후 왼쪽 메뉴에서 "Keys" 클릭
      -> 또는 직접 주소 입력: https://openrouter.ai/keys
      -> "Create Key" 버튼 클릭
      -> 이름은 아무거나 입력 (예: "렌트카챗봇") 후 "Create" 클릭
 
   3단계: API 키 복사
      -> 화면에 나타난 키(sk-or-로 시작하는 문자열)를 복사 (Ctrl+C)
      -> 예시: sk-or-v1-abc123def456...
      -> 이 키는 절대 다른 사람에게 공유하지 마세요!
 
   4단계: 프로젝트에 API 키 적용
      -> Eclipse에서 src/main/webapp/WEB-INF/web.xml 파일 열기
      -> YOUR_API_KEY_HERE 부분을 복사한 API 키로 교체
      -> 예시: <param-value>sk-or-v1-abc123def456...</param-value>
      -> 파일 저장 (Ctrl+S)
 
   5단계: 서버 재시작
      -> Eclipse에서 Tomcat 서버 Stop 후 Start
      -> 웹브라우저에서 새로고침 후 챗봇 테스트!
 
 [무료 한도]
   -> 무료 모델 사용시 비용 없음
   -> Gemini 직접 사용 대비 훨씬 관대한 한도
   -> 비용: 무료 모델 사용시 완전 무료!
 
 ============================================================
*/

@WebServlet("/Chatbot/*")
public class ChatbotController extends BaseController {

	//직렬화 버전 번호 (HttpServlet이 Serializable을 구현하므로 경고 방지용으로 선언)
	private static final long serialVersionUID = 1L;

	// AI에게 전달할 시스템 프롬프트 (렌트카 정보가 담긴 안내문)
	private String systemPrompt;
	
	// OpenRouter API 키
	private String apiKey;
	
	// OpenRouter API 엔드포인트 URL
	private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

	// 사용할 무료 AI 모델 기본값 - openrouter/free는 사용 가능한 무료 모델 중 자동 선택하는 라우터
	private static final String DEFAULT_MODEL = "openrouter/free";

	/** openrouter.ai 에서 발급받은 본인 키로 바꾸세요 */
	private static final String API_KEY = "여기에_본인_OpenRouter_키";

	// 실제 사용할 모델명 (설정값 openrouter.model 로 바꿀 수 있다)
	// [변경 이유] 무료 모델은 제공 목록이 자주 바뀐다.
	//            상수로 박아두면 모델이 바뀔 때마다 소스를 고치고 재컴파일해야 한다.
	private String modelName = DEFAULT_MODEL;

	// 자연어 예약 비서가 실제 차량을 조회할 때 사용 (init 에서 생성)
	// (transient : 서블릿 직렬화 대상에서 제외)
	private transient CarService carService;
	
	//===================================
	// init() - 서블릿이 처음 시작될 때 1번만 실행
	// 역할: DB에서 차량 정보를 조회해서 AI에게 줄 안내문을 미리 만들어둠
	//===================================
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);   // 부모(HttpServlet)의 init 을 먼저 실행시킨다. 서블릿이 정상적으로 준비되게 하는 형식적인 줄이다
		
				apiKey = API_KEY;   //openrouter.ai 에서 발급받은 본인 키

		// 사용할 모델명도 설정에서 읽는다 (무료 모델이 바뀌어도 재컴파일 불필요)
		modelName = DEFAULT_MODEL;
		
		/*
		 2. DB에서 전체 차량 목록 조회

		    [3단계 변경] CarDAO 직접 호출 -> CarService 경유
		      컨트롤러가 DAO를 직접 부르지 않도록 계층을 통일했다.
		      Controller -> Service -> DAO
		*/
		carService = new CarService();   //자연어 예약 비서도 함께 쓰므로 필드에 보관한다
		java.util.List<CarListVo> carList = carService.getAllCars();   // 차량 26대를 전부 조회한다. 서버가 켜질 때 한 번만 읽어 두고 계속 재사용한다
		
		// 3. 차량 정보를 문자열로 정리
		StringBuilder carInfo = new StringBuilder();
		for (CarListVo car : carList) {   // 차량을 하나씩 훑으며 AI 에게 줄 안내문에 이어 붙인다
			carInfo.append("- ").append(car.getCarname())   // 차량 하나를 "- 이름 | 제조사 | 요금 | 인원 | 등급 | 설명" 한 줄로 만든다
				  .append(" | 제조사: ").append(car.getCarcompany())
				  .append(" | 일일렌탈료: ").append(String.format("%,d", car.getCarprice())).append("원")
				  .append(" | 탑승인원: ").append(car.getCarusepeople()).append("명")
				  .append(" | 등급: ").append(car.getCarcategory())
				  .append(" | 설명: ").append(car.getCarinfo())
				  .append("\n");
		}
		
		// 4. AI에게 전달할 시스템 프롬프트 만들기
		systemPrompt = "당신은 (주)SM렌탈의 친절한 AI 렌트카 상담사입니다.\n"
			+ "고객의 질문에 친절하고 정확하게 답변해주세요.\n"
			+ "답변은 간결하게 해주시되, 필요하면 자세히 설명해주세요.\n"
			+ "이모지를 적절히 사용해 친근감을 주세요.\n\n"
			+ "=== 회사 정보 ===\n"
			+ "회사명: (주)SM렌탈\n"
			+ "주소: 서울시 강남구 역삼동 역삼빌딩 2층 21호\n"
			+ "전화번호: 02-3456-6574\n"
			+ "팩스: 01-3254-9874\n\n"
			+ "=== 보유 차량 목록 ===\n"
			+ carInfo.toString() + "\n"
			/*
			 [변경] 옵션 가격을 CarService 의 상수에서 가져온다.

			   기존에는 이 프롬프트에 가격이 직접 적혀 있었고,
			   실제 결제 계산식은 CarController 안에 따로 있었다.
			   두 곳의 숫자가 달라서 "챗봇 안내 금액"과 "실제 결제 금액"이 어긋났다.
			   (WiFi를 5,000원으로 안내하고 10,000원을 청구하고 있었다)

			   이제 요금의 기준점이 CarService 한 곳뿐이므로 어긋날 수 없다.
			*/
			+ "=== 추가 옵션 가격 (1일 기준) ===\n"
			+ String.format("- 자차보험: %,d원/일%n", CarService.PRICE_INSURANCE)
			+ String.format("- WiFi: %,d원/일%n", CarService.PRICE_WIFI)
			+ String.format("- 네비게이션: %,d원/일%n", CarService.PRICE_NAVI)
			+ String.format("- 베이비시트: %,d원/일%n%n", CarService.PRICE_BABYSEAT)
			+ "=== 예약 절차 ===\n"
			+ "1. 홈페이지에서 원하는 차량 선택\n"
			+ "2. 대여일자 및 반납일자 선택\n"
			+ "3. 추가 옵션 선택 (보험, WiFi 등)\n"
			+ "4. 예약 확인 및 결제\n\n"
			+ "=== 가격 계산 공식 ===\n"
			+ "총 렌탈 비용 = (차량 일일렌탈료 + 선택한 옵션 합계) x 대여일수\n\n"
			+ "=== 주의사항 ===\n"
			+ "- 렌트카 관련 질문에만 답변하세요.\n"
			+ "- 렌트카와 관련 없는 질문에는 정중히 거절하고 렌트카 상담으로 안내하세요.\n"
			+ "- 예약이나 결제는 직접 홈페이지에서 진행하도록 안내하세요.\n";
		
		System.out.println("[ChatbotController] 시스템 프롬프트 생성 완료! 차량 " + carList.size() + "대 정보 로드됨");   // 몇 대의 정보를 실었는지 콘솔에 남긴다. 0대면 DB 연결을 의심해야 한다
		System.out.println("[ChatbotController] 사용 모델: " + modelName);   // 어떤 AI 모델을 쓰는지 콘솔에 남긴다
		System.out.println("[ChatbotController] API 키 설정됨: " + (apiKey != null && !apiKey.isEmpty()));   // 키가 채워져 있는지만 확인해 남긴다. 키 값 자체는 절대 찍지 않는다
	}
	
	//===================================
	// doHandle() - 모든 요청을 처리하는 메인 메소드
	//===================================
	@SuppressWarnings("unchecked") //json-simple의 JSONObject가 제네릭 없는 HashMap이라 put() 사용 시 발생하는 경고 억제
	protected void doHandle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");   // 브라우저가 보낸 한글이 깨지지 않게 UTF-8 로 읽는다
		response.setContentType("application/json;charset=UTF-8");   // 응답이 JSON 이라고 알린다. 챗봇은 화면이 아니라 데이터를 주고받는다
		response.setCharacterEncoding("UTF-8");   // 응답 글자도 UTF-8 로 내보낸다

		/*
		 ============================================================================
		   [보안 수정] CORS 전체 허용(*)을 제거했다.

		   (기존 코드)
		       response.setHeader("Access-Control-Allow-Origin", "*");

		   무엇이 문제였나
		     이 엔드포인트는 서버에 저장된 OpenRouter API 키로 AI를 호출한다.
		     그런데 * 로 모든 출처를 허용하면, 전혀 다른 사이트의 자바스크립트가
		     이 주소를 그대로 호출해 우리 키로 AI를 쓸 수 있다.

		         fetch('http://우리서버/CarProject/Chatbot/send.do', {...})

		     즉 우리 서버가 "누구나 쓸 수 있는 무료 AI 대행 창구"가 된다.
		     무료 사용 한도가 남의 트래픽으로 소진되고, 유료 키라면 요금이 청구된다.

		   (지금)
		     헤더를 지정하지 않으면 브라우저의 기본 정책(같은 출처만 허용)이 적용된다.
		     우리 사이트 화면에서 호출하는 것은 같은 출처이므로 그대로 동작한다.

		   [참고] 정말 외부 도메인에서 호출해야 한다면 * 대신 그 도메인만 적어야 한다.
		          response.setHeader("Access-Control-Allow-Origin", "https://허용할도메인");
		 ============================================================================
		*/

		// OPTIONS 프리플라이트 요청 처리
		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			response.setStatus(200);
			return;   // 본 요청이 아니므로 여기서 끝낸다
		}

		String action = request.getPathInfo();   // 2단계 주소를 얻는다 (/send.do, /ai.do, /reserveAI.do 중 하나)
		System.out.println("[ChatbotController] 요청 주소: " + action);   // 어떤 주소가 들어왔는지 콘솔에 남긴다
		
		if ("/send.do".equals(action)) {   // 일반 상담 챗봇 요청이면
			// 사용자가 보낸 메시지와 대화 기록 받기
			String userMessage = request.getParameter("message");
			String historyJson = request.getParameter("history");   // 지금까지의 대화 기록 (AI 가 앞 대화를 기억하게 하려고 함께 보낸다)

			if (userMessage == null || userMessage.trim().isEmpty()) {   // 메시지가 비어 있으면 AI 를 부를 이유가 없다
				PrintWriter out = response.getWriter();   // 응답에 글자를 쓸 수 있는 붓을 얻는다
				out.write("{\"reply\":\"메시지를 입력해주세요.\"}");   // 안내 문구를 JSON 으로 바로 돌려준다 (AI 호출 없이)
				return;   // 여기서 끝낸다
			}

			// OpenRouter API 호출
			String reply = callOpenRouterAPI(userMessage, historyJson);

			// JSON 응답 생성
			PrintWriter out = response.getWriter();
			JSONObject jsonResponse = new JSONObject();   // 화면에 돌려줄 JSON 객체를 만든다
			jsonResponse.put("reply", reply);   // AI 답변을 reply 라는 이름표로 담는다. 화면이 이 값을 말풍선에 그린다
			out.write(jsonResponse.toJSONString());   // JSON 을 글자로 바꿔 응답에 쓴다

		} else if ("/ai.do".equals(action)) {   // AI 서비스(차량추천·비용계산·여행플래너) 요청이면
			// AI 추천 서비스 전용 엔드포인트 (차량추천, 비용계산, 여행플래너)
			String userMessage = request.getParameter("message");
			String serviceType = request.getParameter("type"); // "recommend", "cost", "travel"

			if (userMessage == null || userMessage.trim().isEmpty()) {   // 요청 내용이 비어 있으면 AI 를 부를 이유가 없다
				PrintWriter out = response.getWriter();   // 응답에 글자를 쓸 수 있는 붓을 얻는다
				out.write("{\"reply\":\"요청 내용을 입력해주세요.\"}");   // 안내 문구를 JSON 으로 바로 돌려준다
				return;   // 여기서 끝낸다
			}

			// 서비스 유형별 맞춤 시스템 프롬프트 생성 후 AI 호출
			String aiPrompt = buildAIServicePrompt(serviceType);
			String reply = callOpenRouterAPIWithPrompt(userMessage, aiPrompt, serviceType);   // 서비스 종류에 맞는 지시문을 붙여 AI 를 부른다

			PrintWriter out = response.getWriter();   // 응답에 글자를 쓸 수 있는 붓을 얻는다
			JSONObject jsonResponse = new JSONObject();   // 화면에 돌려줄 JSON 객체를 만든다
			jsonResponse.put("reply", reply);   // AI 답변을 reply 라는 이름표로 담는다
			out.write(jsonResponse.toJSONString());   // JSON 을 글자로 바꿔 응답에 쓴다

		} else if ("/reserveAI.do".equals(action)) {   // "말로 예약하기" 요청이면
			// 자연어 예약 비서 : "다음 주 금요일부터 2박 3일, 5명, 제주" -> 실제 차량 추천
			handleReserveAI(request, response);
		}
	}

	//===========================================================================
	// 자연어 예약 비서 (/Chatbot/reserveAI.do)
	//
	//   [기존 AI 추천 탭과 무엇이 다른가]
	//     기존 : 사용자 문장 -> AI 가 "말로만" 추천 (DB를 보지 않는다.
	//            없는 차를 추천하거나 다른 금액을 말해도 막을 방법이 없다)
	//     지금 : AI 의 역할을 "문장에서 조건 뽑기"로 좁혔다.
	//            차량 선택과 금액 계산은 우리 서버가 DB 데이터로 직접 한다.
	//
	//   [처리 순서]
	//     1. 사용자 문장 -> AI 에게 "JSON 만" 뽑아 달라고 요청
	//            {"begindate":"2026-08-07","days":3,"people":5,...}
	//     2. AI 가 준 JSON 을 서버가 검증·보정한다 (지난 날짜, 이상한 숫자 차단)
	//     3. 그 조건으로 DB 의 실제 차량을 골라 금액을 계산한다
	//     4. 추천 차량 + "예약 이어가기" 정보(JSON)로 응답한다
	//        -> 화면의 버튼이 기존 예약 흐름(CarInfo -> CarOption)으로 값을 미리 채워 보낸다
	//
	//   왜 이렇게 나누는가 : AI 의 출력은 "믿을 수 없는 입력"이다.
	//   금액·차량처럼 틀리면 안 되는 값은 반드시 서버가 원본(DB)에서 다시 만든다.
	//===========================================================================
	@SuppressWarnings("unchecked")
	private void handleReserveAI(HttpServletRequest request, HttpServletResponse response) throws IOException {

		PrintWriter out = response.getWriter();   // 응답에 글자를 쓸 수 있는 붓을 얻는다
		String userMessage = request.getParameter("message");   // 사용자가 말한 예약 문장 (예: "내일부터 3일간 4명")

		if (userMessage == null || userMessage.trim().isEmpty()) {   // 문장이 비어 있으면 해석할 것이 없다
			out.write("{\"reply\":\"원하시는 일정을 문장으로 알려주세요. 예) 다음 주 금요일부터 2박 3일, 5명이 탈 차\"}");   // 어떻게 말하면 되는지 예시까지 함께 알려 준다
			return;   // 여기서 끝낸다
		}
		//프롬프트 폭주 방지 : 문장은 200자면 충분하다
		if (userMessage.length() > 200) {
			userMessage = userMessage.substring(0, 200);
		}

		java.time.LocalDate today = java.time.LocalDate.now();   // 오늘 날짜를 얻는다. "내일", "다음 주 금요일" 을 계산하는 기준점이다

		/*
		 ----- 1. 규칙 해석기로 먼저 시도한다 (AI 보다 먼저) -----

		   "내일부터 3일간 4명" 처럼 흔한 문장은 정규식으로 100% 정확히 풀린다.
		   이런 문장까지 AI 에게 보내면
		     - 호출 요금(또는 무료 한도)이 소진되고
		     - 응답이 수 초 느려지고
		     - 외부 서비스가 죽으면 우리 기능도 함께 죽는다.

		   그래서 규칙으로 되는 것은 규칙으로 처리하고,
		   규칙이 못 잡은 애매한 문장만 AI 에게 넘긴다.
		   (자세한 설명은 util/ReserveTextParser.java 상단 주석)
		*/
		ReserveTextParser.Result cond = ReserveTextParser.parse(userMessage, today);
		String source = "규칙";   // 무엇으로 해석했는지 표시. 나중에 콘솔에 남겨 확인한다

		//----- 2. 규칙이 확신하지 못하면 AI 에게 보강을 요청한다 -----
		if (!cond.isConfident()) {

			String extracted = callOpenRouterAPIWithPrompt(userMessage, buildReserveExtractPrompt(), "reserve");
			ReserveTextParser.Result aiCond = parseAiJson(extracted);   // AI 가 돌려준 JSON 글자를 우리 결과 상자로 바꾼다

			if (aiCond != null) {   // AI 해석에 성공했으면
				//규칙이 이미 잡은 값은 그대로 두고, 비어 있는 칸만 AI 값으로 채운다
				//(규칙 결과가 AI 보다 신뢰도가 높다. AI 는 빈칸 채우기 담당)
				if (cond.begindate == null)  cond.begindate = aiCond.begindate;
				if (cond.days     <= 0)      cond.days      = aiCond.days;
				if (cond.people   <= 0)      cond.people    = aiCond.people;   // 인원을 못 잡았으면 AI 값으로 채운다
				if (cond.category == null)   cond.category  = aiCond.category;   // 등급을 못 잡았으면 AI 값으로 채운다
				if (cond.maxprice <= 0)      cond.maxprice  = aiCond.maxprice;   // 예산을 못 잡았으면 AI 값으로 채운다
				source = "규칙+AI";   // 규칙과 AI 를 함께 썼다고 표시한다
			} else {
				System.out.println("[ChatbotController] AI 보강 실패 - 규칙 결과만 사용");   // AI 까지 실패해도 멈추지 않는다. 규칙이 잡은 만큼으로 진행한다
			}
		}

		//규칙도 AI 도 아무것도 못 잡았으면 다시 묻는다
		if (!cond.hasAny()) {
			JSONObject fail = new JSONObject();
			fail.put("reply", "일정을 이해하지 못했습니다. 날짜와 인원을 함께 알려주세요.\n"   // 무엇을 알려줘야 하는지까지 함께 안내한다
					+ "예) 8월 15일부터 3일간, 어른 4명");
			out.write(fail.toJSONString());   // JSON 을 글자로 바꿔 응답에 쓴다
			return;   // 여기서 끝낸다
		}

		System.out.println("[ChatbotController] 예약 조건 추출(" + source + ") : " + cond);   // 무엇을 어떻게 해석했는지 콘솔에 남긴다. 오해석을 잡을 때 여기부터 본다

		java.time.LocalDate begin = cond.begindate;   // 해석된 대여 시작일
		int days     = cond.days;   // 해석된 대여 일수
		int people   = cond.people;   // 해석된 탑승 인원
		int maxPrice = Math.max(cond.maxprice, 0);   //-1(없음) -> 0(제한 없음)
		String category = cond.category;   // 해석된 차량 등급 (안 말했으면 null)

		//----- 2-0. 빠진 값의 기본값 -----
		//   날짜를 말하지 않았으면 "내일", 일수를 말하지 않았으면 1일,
		//   인원을 말하지 않았으면 1명으로 본다. (없다고 실패시키지 않는다)
		if (begin == null)  begin = today.plusDays(1);
		if (days   <= 0)    days = 1;
		if (people <= 0)    people = 1;   // 인원을 말하지 않았으면 1명으로 본다

		/*
		 ----- 2-1. 서버측 보정 -----

		   규칙 해석기든 AI 든, 결과는 "화면에서 들어온 값"과 같은 취급을 받는다.
		   범위를 벗어난 값은 여기서 바로잡는다.

		   [중요] 값을 조용히 바꾸지 않고 무엇을 바꿨는지 사용자에게 알린다.
		          "50명"이라고 입력했는데 결과가 24명 기준으로 나오면
		          사용자는 시스템이 고장난 줄 안다. 바꿨다면 이유를 말해줘야 한다.
		*/
		java.util.List<String> notes = new java.util.ArrayList<String>();

		if (begin.isBefore(today)) {   // 지난 날짜를 말한 경우
			begin = today.plusDays(1);   // 내일로 맞춘다
			notes.add("지난 날짜는 예약할 수 없어 내일로 맞췄습니다.");   // 무엇을 바꿨는지 반드시 알려 준다 (조용히 바꾸면 고장난 줄 안다)
		} else if (begin.isAfter(today.plusYears(1))) {   // 1년보다 먼 날짜를 말한 경우
			begin = today.plusDays(1);   // 내일로 맞춘다
			notes.add("1년 이후는 예약할 수 없어 내일로 맞췄습니다.");   // 무엇을 바꿨는지 알려 준다
		}

		if (days > 30) {   // 30일을 넘겨 말한 경우
			days = 30;   // 상한인 30일로 맞춘다
			notes.add("최대 30일까지 예약할 수 있어 30일로 맞췄습니다.");   // 무엇을 바꿨는지 알려 준다
		} else if (days < 1) {   // 0일 이하로 해석된 경우
			days = 1;   // 최소 1일로 맞춘다 (이건 굳이 안내하지 않아도 어색하지 않다)
		}

		if (people > 24) {   // 24명을 넘겨 말한 경우
			people = 24;   // 상한인 24명으로 맞춘다
			notes.add("한 번에 최대 24명까지 조회할 수 있습니다. 더 필요하면 02-3456-6574 로 문의해주세요.");   // 안내와 함께 전화 문의를 권한다
		} else if (people < 1) {   // 0명 이하로 해석된 경우
			people = 1;   // 0명 이하로 해석된 경우 최소 1명으로 맞춘다
		}

		//----- 3. DB 의 실제 차량에서 후보를 고른다 -----
		java.util.List<CarListVo> all = carService.getAllCars();

		//3-1. 인원이 한 대에 다 타는 차 우선. 없으면(13명 이상 등) 여러 대로 나눈다.
		java.util.List<Object[]> candidates = new java.util.ArrayList<Object[]>();  //[차량, 대수, 총액]

		for (CarListVo car : all) {   // 보유 차량을 하나씩 확인하며 조건에 맞는지 본다

			if (category != null && !category.equals(car.getCarcategory())) continue;   // 등급을 말했는데 그 등급이 아니면 건너뛴다
			if (maxPrice > 0 && car.getCarprice() > maxPrice) continue;   // 예산을 말했는데 그보다 비싸면 건너뛴다
			if (car.getCarusepeople() <= 0) continue;   // 탑승 인원이 0인 이상한 데이터는 건너뛴다 (0으로 나누면 오류가 난다)

			//몇 대가 필요한가 (올림 나눗셈)
			int qty = (people + car.getCarusepeople() - 1) / car.getCarusepeople();
			if (qty > 5) continue;                        //예약 화면의 수량 상한(5대)을 넘기면 제외

			int total = car.getCarprice() * qty * days;   //옵션 제외 기본 요금
			candidates.add(new Object[]{ car, qty, total });   // 차량·필요 대수·총액을 한 묶음으로 후보에 담는다
		}

		//조건이 너무 좁아 후보가 없으면 등급/예산 조건을 풀고 한 번 더
		boolean relaxed = false;
		if (candidates.isEmpty() && (category != null || maxPrice > 0)) {   // 조건이 너무 좁아 후보가 하나도 없고, 등급이나 예산 조건이 걸려 있었다면
			relaxed = true;   // 조건을 푼 채로 다시 골랐다고 표시해 둔다 (나중에 사용자에게 알려 준다)
			for (CarListVo car : all) {   // 이번에는 등급·예산 조건 없이 전체 차량을 훑는다
				if (car.getCarusepeople() <= 0) continue;   // 탑승 인원이 0인 이상한 데이터는 건너뛴다
				int qty = (people + car.getCarusepeople() - 1) / car.getCarusepeople();   // 몇 대가 필요한지 올림 나눗셈으로 구한다
				if (qty > 5) continue;   // 5대를 넘으면 예약 화면에서 처리할 수 없으므로 제외한다
				candidates.add(new Object[]{ car, qty, car.getCarprice() * qty * days });   // 차량·대수·총액을 후보에 담는다
			}
		}

		if (candidates.isEmpty()) {   // 조건을 풀고도 후보가 없으면
			JSONObject none = new JSONObject();   // 안내를 담을 JSON 객체를 만든다
			none.put("reply", "조건에 맞는 차량을 찾지 못했습니다. 인원을 나눠 여러 대로 예약하시려면 전화(02-3456-6574)로 문의해주세요.");   // 전화 문의를 권하는 안내를 담는다
			out.write(none.toJSONString());   // JSON 을 글자로 바꿔 응답에 쓴다
			return;   // 여기서 끝낸다
		}

		//3-2. "한 대로 되는 차" 를 앞으로, 그 다음 총액이 싼 순서로
		candidates.sort((a, b) -> {
			int qa = (Integer) a[1], qb = (Integer) b[1];   // 두 후보의 필요 대수를 꺼낸다
			if (qa != qb) return qa - qb;   // 대수가 다르면 적은 쪽을 앞으로 (한 대로 되는 차가 낫다)
			return (Integer) a[2] - (Integer) b[2];   // 대수가 같으면 총액이 싼 쪽을 앞으로
		});

		//----- 4. 응답 JSON 조립 (안내 문장은 AI 가 아니라 서버가 만든다 - 금액이 틀릴 수 없다) -----
		java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("M월 d일");

		StringBuilder reply = new StringBuilder();   // 사용자에게 보여줄 안내 문장을 조립할 준비
		reply.append(begin.format(fmt)).append("부터 ").append(days).append("일간, ")   // "8월 7일부터 3일간, ..." 형태로 앞부분을 만든다
			 .append(people).append("명 기준으로 골랐습니다.");
		if (relaxed) {   // 등급·예산 조건을 풀고 고른 경우라면
			reply.append("\n(말씀하신 등급/예산에 맞는 차가 없어 전체 차량에서 골랐습니다)");   // 왜 말한 조건과 다른 차가 나왔는지 설명해 준다
		}
		reply.append("\n금액은 옵션 제외 기본 요금입니다.");   // 옵션이 빠진 금액임을 분명히 밝힌다 (나중에 금액이 달라 놀라지 않게)

		//보정한 내용이 있으면 함께 알려준다 (조용히 바꾸지 않는다)
		for (String note : notes) {
			reply.append("\n⚠ ").append(note);   //⚠ = 경고 기호
		}

		JSONArray cars = new JSONArray();   // 추천 차량 목록을 담을 JSON 배열을 만든다
		int limit = Math.min(3, candidates.size());   // 최대 3대까지만 보여준다. 후보가 3대보다 적으면 있는 만큼만
		for (int i = 0; i < limit; i++) {   // 앞에서부터(=가장 알맞은 차부터) 차례로 담는다
			CarListVo car = (CarListVo) candidates.get(i)[0];   // 후보 묶음에서 차량 정보를 꺼낸다
			int qty   = (Integer) candidates.get(i)[1];   // 필요 대수를 꺼낸다
			int total = (Integer) candidates.get(i)[2];   // 총액을 꺼낸다

			JSONObject item = new JSONObject();   // 차량 하나를 담을 JSON 객체를 만든다
			item.put("carno",    car.getCarno());   // 차량번호 ("예약 이어가기" 버튼이 이 번호로 예약 화면을 연다)
			item.put("carname",  car.getCarname());   // 차량명
			item.put("carimg",   car.getCarimg());   // 차량 사진 파일명
			item.put("carprice", car.getCarprice());   // 1일 요금
			item.put("seats",    car.getCarusepeople());   // 탑승 가능 인원
			item.put("qty",      qty);   // 필요 대수
			item.put("total",    total);   // 옵션 제외 총액
			cars.add(item);   // 완성된 차량 하나를 목록에 담는다
		}

		JSONObject result = new JSONObject();   // 화면에 돌려줄 최종 JSON 객체를 만든다
		result.put("reply", reply.toString());   // 위에서 만든 안내 문장
		result.put("cars", cars);   // 추천 차량 목록
		result.put("begindate", begin.toString());   //YYYY-MM-DD (예약 화면 미리 채움용)
		result.put("days", days);   // 대여 일수 (예약 화면에 미리 채워 넣는 데 쓴다)
		out.write(result.toJSONString());   // JSON 을 글자로 바꿔 응답에 쓴다
	}

	/** 조건 추출 전용 시스템 프롬프트 - "JSON 외에는 아무것도 쓰지 마라"가 핵심이다 */
	private String buildReserveExtractPrompt() {

		java.time.LocalDate today = java.time.LocalDate.now();   // 오늘 날짜를 얻는다. AI 에게 "오늘이 며칠인지" 를 알려줘야 "내일" 을 계산할 수 있다
		String[] week = {"월","화","수","목","금","토","일"};   // 요일 이름 배열. 아래에서 숫자 요일을 한글로 바꾸는 데 쓴다
		String todayText = today + " (" + week[today.getDayOfWeek().getValue() - 1] + "요일)";   // "2026-08-21 (목요일)" 형태로 만든다. getValue() 는 월=1 이라 -1 을 해서 배열 자리를 맞춘다

		return "너는 렌트카 예약 문장에서 조건만 뽑는 분석기다.\n"   // AI 에게 줄 지시문. "조건만 JSON 으로 뽑아라" 라고 역할을 좁게 정해 준다
			+ "오늘은 " + todayText + " 이다. '내일', '다음 주 금요일' 같은 상대 날짜는 오늘 기준으로 계산하라.\n\n"
			+ "아래 형식의 JSON 한 개만 출력하라. 설명, 인사, 코드블록 기호를 절대 붙이지 마라.\n"
			+ "{\"begindate\":\"YYYY-MM-DD\",\"days\":숫자,\"people\":숫자,\"category\":null,\"maxprice\":null}\n\n"
			+ "규칙:\n"
			+ "- begindate : 대여 시작일. 날짜 언급이 없으면 내일 날짜.\n"
			+ "- days : 대여 일수. '2박 3일'은 3, '당일'은 1. 언급 없으면 1.\n"
			+ "- people : 탑승 인원. '가족 4명'은 4. 언급 없으면 1.\n"
			+ "- category : '소형/경차'->\"Small\", '중형/SUV'->\"Mid\", '대형/승합'->\"Big\". 언급 없으면 null.\n"
			+ "- maxprice : '하루 5만원 이하' 처럼 1일 예산 상한이 있으면 숫자(원). 없으면 null.\n"
			+ "- 렌트카 예약과 무관한 문장이면 {\"error\":\"not_reservation\"} 만 출력하라.";
	}

	/**
	 * AI 가 돌려준 JSON 문자열을 규칙 해석기와 같은 형태(Result)로 바꾼다.
	 * 형식이 어긋나면 null 을 돌려준다 (호출한 쪽이 규칙 결과만 쓰도록).
	 *
	 * [보안] AI 응답은 "외부에서 들어온 믿을 수 없는 값"이다.
	 *        category 는 허용된 세 글자만 통과시키고, 숫자는 형 변환에 실패하면 버린다.
	 */
	private ReserveTextParser.Result parseAiJson(String aiText) {

		try {
			JSONObject json = (JSONObject) new JSONParser().parse(cutJson(aiText));   // AI 답변에서 JSON 부분만 잘라 자바 객체로 바꾼다

			//AI 가 "예약 문장이 아니다" 라고 판단한 경우
			if (json.get("error") != null) {
				return null;
			}

			ReserveTextParser.Result r = new ReserveTextParser.Result();   // 해석 결과를 담을 빈 상자를 만든다

			Object begin = json.get("begindate");   // 시작일 값을 꺼낸다
			if (begin != null && String.valueOf(begin).matches("\\d{4}-\\d{2}-\\d{2}")) {   // yyyy-MM-dd 형태일 때만 인정한다. AI 가 엉뚱한 형식을 줄 수 있기 때문이다
				try {
					r.begindate = java.time.LocalDate.parse(String.valueOf(begin));   // 글자를 날짜로 바꿔 담는다
				} catch (Exception ignore) {
					//형식은 맞지만 2026-02-30 처럼 없는 날짜인 경우 - 그냥 비워 둔다
				}
			}

			r.days     = toInt(json.get("days"), -1);   // 대여 일수. 숫자가 아니면 -1(없음)로 둔다
			r.people   = toInt(json.get("people"), -1);   // 탑승 인원. 숫자가 아니면 -1(없음)로 둔다
			r.maxprice = toInt(json.get("maxprice"), -1);   // 예산 상한. 숫자가 아니면 -1(없음)로 둔다

			Object cat = json.get("category");   // 차량 등급 값을 꺼낸다
			if (cat != null) {   // 등급을 말했으면
				String c = cat.toString();   // 글자로 바꾼다
				if ("Small".equals(c) || "Mid".equals(c) || "Big".equals(c)) {   // 우리 DB 에 실제로 있는 세 값일 때만 인정한다 (AI 가 "중형" 같은 한글을 줄 수 있다)
					r.category = c;   // 검사를 통과한 등급만 담는다
				}
			}

			return r.hasAny() ? r : null;   // 하나라도 건진 것이 있으면 결과를, 아무것도 없으면 null 을 돌려준다

		} catch (Exception e) {
			System.out.println("[ChatbotController] AI 조건 JSON 해석 실패 : " + e.getMessage());   // AI 가 JSON 이 아닌 답을 준 경우다. 원인만 콘솔에 남긴다
			return null;   // null 을 돌려주면 부르는 쪽이 "규칙 결과만 쓰자" 로 넘어간다
		}
	}

	/** AI 응답에서 JSON 부분만 잘라낸다 (코드블록 기호나 잡담이 붙어 와도 견딘다) */
	private String cutJson(String text) {
		if (text == null) {   // 답변 자체가 없으면
			return "";   // 빈 문자열을 돌려준다
		}
		int s = text.indexOf('{');   // 여는 중괄호가 처음 나오는 위치를 찾는다
		int e = text.lastIndexOf('}');   // 닫는 중괄호가 마지막으로 나오는 위치를 찾는다
		if (s < 0 || e <= s) {   // 중괄호를 못 찾았거나 순서가 이상하면
			return text;   // 자르지 않고 원문을 그대로 돌려준다
		}
		return text.substring(s, e + 1);   // 여는 괄호부터 닫는 괄호까지만 잘라 낸다. AI 가 앞뒤에 붙인 설명 문장을 버리는 것이다
	}

	/** JSON 숫자를 int 로 (json-simple 은 숫자를 Long 으로 준다). null/이상값은 기본값 */
	private int toInt(Object value, int defaultValue) {
		if (value instanceof Number) {   // 값이 숫자 종류이면
			return (int) ((Number) value).longValue();   // 정수로 바꿔 돌려준다
		}
		try {
			return Integer.parseInt(String.valueOf(value).trim());   // 글자로 온 숫자를 정수로 바꿔 본다
		} catch (Exception e) {
			return defaultValue;   // 숫자로 바꿀 수 없으면 미리 정해 둔 기본값을 돌려준다
		}
	}
	
	//===================================
	// callOpenRouterAPI() - OpenRouter AI에게 질문을 보내고 답변을 받는 메소드
	// 일시적 오류 시 1회 자동 재시도
	//===================================
	private String callOpenRouterAPI(String userMessage, String historyJson) {

		// API 키 확인
		if (apiKey == null || "YOUR_API_KEY_HERE".equals(apiKey)) {
			return "AI 챗봇 API 키가 설정되지 않았습니다.\n\n"
				+ "[설정 방법]\n"
				+ "1. https://openrouter.ai 접속\n"
				+ "2. Google 계정으로 로그인\n"
				+ "3. Keys 메뉴 -> Create Key\n"
				+ "4. web.xml에 키 입력 후 서버 재시작";
		}

		// 요청 JSON 만들기 (OpenAI 호환 형식)
		JSONObject requestBody = buildOpenRouterRequest(userMessage, historyJson);
		String jsonStr = requestBody.toJSONString();   // 만든 JSON 객체를 실제로 보낼 글자로 바꾼다
		System.out.println("[ChatbotController] 요청 JSON 길이: " + jsonStr.length());   // 얼마나 긴 요청을 보내는지 콘솔에 남긴다. 너무 길면 모델 한도를 넘는다

		// 최대 2회 시도 (첫 시도 + 1회 재시도)
		int maxRetries = 2;
		Exception lastException = null;   // 마지막에 잡힌 예외를 담아 둘 변수. 모두 실패했을 때 원인을 알려 주려는 것이다

		for (int attempt = 1; attempt <= maxRetries; attempt++) {   // 1회차, 2회차 순서로 시도한다
			try {
				String result = doApiCall(jsonStr);   // 실제로 AI 서버에 요청을 보낸다
				return result;   // 성공했으면 그 답을 바로 돌려주고 반복을 끝낸다
			} catch (java.net.SocketTimeoutException e) {
				lastException = e;   // 원인을 보관해 둔다
				System.out.println("[ChatbotController] 타임아웃 (시도 " + attempt + "/" + maxRetries + "): " + e.getMessage());   // 몇 번째 시도에서 시간이 초과됐는지 콘솔에 남긴다
				if (attempt < maxRetries) {   // 아직 재시도가 남아 있으면
					try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }   // 2초 쉬었다가 다시 시도한다. 바로 다시 걸면 또 실패할 확률이 높다
				}
			} catch (java.io.IOException e) {
				lastException = e;   // 원인을 보관해 둔다
				System.out.println("[ChatbotController] 네트워크 오류 (시도 " + attempt + "/" + maxRetries + "): " + e.getMessage());   // 몇 번째 시도에서 네트워크가 끊겼는지 콘솔에 남긴다
				if (attempt < maxRetries) {   // 아직 재시도가 남아 있으면
					try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }   // 2초 쉬었다가 다시 시도한다
				}
			} catch (Exception e) {
				// 재시도 불가능한 오류 (파싱 오류 등)는 즉시 반환
				System.out.println("[ChatbotController] API 호출 오류: " + e.getMessage());
				e.printStackTrace();   // 예상 못 한 오류는 콘솔에 자세히 남긴다
				return "죄송합니다. 상담 중 오류가 발생했습니다.\n전화 상담: 02-3456-6574";   // 사용자에게는 전화 상담 번호와 함께 무난한 안내를 준다
			}
		}

		// 모든 재시도 실패
		System.out.println("[ChatbotController] 모든 재시도 실패: " + lastException.getMessage());
		if (lastException instanceof java.net.SocketTimeoutException) {   // 마지막 실패 원인이 시간 초과였다면
			return "AI 응답 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.";   // "조금 뒤에 다시" 라고 안내한다 (기다리면 될 가능성이 있다)
		}
		return "네트워크 연결에 실패했습니다. 잠시 후 다시 시도해주세요.\n전화 상담: 02-3456-6574";   // 네트워크 문제였다면 전화 상담을 함께 안내한다
	}

	//===================================
	// doApiCall() - 실제 HTTP API 호출 (재시도 로직에서 사용)
	//===================================
	private String doApiCall(String jsonStr) throws Exception {

		//URI를 거쳐 URL 생성 (new URL(String) 생성자는 최신 JDK에서 deprecated)
		URL url = java.net.URI.create(API_URL).toURL();
		HttpURLConnection con = (HttpURLConnection) url.openConnection();   // 실제로 연결을 여는 객체를 얻는다
		con.setRequestMethod("POST");   // 요청 방식을 POST 로 지정한다
		con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");   // 보내는 본문이 JSON 이고 한글은 UTF-8 이라고 알린다
		con.setRequestProperty("Authorization", "Bearer " + apiKey);   // 우리 API 키를 신분증처럼 붙인다. "Bearer " 접두어까지 정확해야 한다
		con.setRequestProperty("HTTP-Referer", "http://localhost:8090/CarProject2");   // 어느 사이트에서 부르는지 알리는 값 (OpenRouter 통계용)
		con.setRequestProperty("X-Title", "SM Rental Chatbot");   // 우리 앱 이름을 알리는 값 (OpenRouter 대시보드에 표시된다)
		con.setDoOutput(true);   // "본문을 보낼 것" 이라고 미리 선언해야 출력 통로를 열 수 있다
		con.setConnectTimeout(30000);   // 30초 안에 연결이 안 되면 포기한다 (무한 대기 방지)
		con.setReadTimeout(60000);   // AI 는 생각하는 데 오래 걸리므로 읽기는 60초까지 기다린다

		// 요청 본문 전송
		OutputStream os = con.getOutputStream();
		os.write(jsonStr.getBytes("UTF-8"));   // 요청 글자를 UTF-8 바이트로 바꿔 내보낸다. 이 순간 실제 통신이 시작된다
		os.flush();   // 남아 있는 것을 마저 밀어낸다
		os.close();   // 통로를 닫는다

		// 응답 코드 확인
		int responseCode = con.getResponseCode();
		System.out.println("[ChatbotController] 응답 코드: " + responseCode);   // 응답 코드를 콘솔에 남긴다. 200이 아니면 아래에서 종류별로 안내가 달라진다

		String responseStr;   // 응답 본문을 담을 변수
		if (responseCode == HttpURLConnection.HTTP_OK) {   // 200(정상)이면
			responseStr = readBody(con.getInputStream());   // 정상 응답 통로에서 본문을 읽는다
		} else {
			String errorBody = "";   // 오류 본문을 담을 변수. 비어 있을 수도 있어 빈 문자열로 시작한다
			try {
				InputStream errorStream = con.getErrorStream();   // 실패했을 때는 오류 전용 통로에서 읽어야 원인을 볼 수 있다
				if (errorStream != null) {   // 오류 통로가 있으면
					errorBody = readBody(errorStream);   // 오류 본문을 읽는다
				}
			} catch (Exception ex) {
				errorBody = "에러 읽기 실패";   // 오류를 읽는 것마저 실패한 경우다. 여기서 또 예외를 던지면 원인이 가려진다
			}
			System.out.println("[ChatbotController] API 오류 코드: " + responseCode);   // 어떤 코드로 실패했는지 콘솔에 남긴다
			System.out.println("[ChatbotController] API 오류 내용: " + errorBody);   // AI 서버가 알려 준 실패 이유를 콘솔에 남긴다
			con.disconnect();   // 연결을 끊는다 (자원 반납)

			if (responseCode == 429) {   // 429 = "요청이 너무 많다". 아래에서 두 종류로 나눠 안내한다
				/*
				 ============================================================================
				   [429 를 두 종류로 나눠 안내하도록 고친 부분 - 10단계 작업]

				   [Before] 무조건 "잠시 후(30초) 다시 시도해주세요."

				     429 에는 성질이 다른 두 가지가 섞여 있다.

				       (가) 짧은 시간에 너무 많이 불렀다  -> 정말 30초 뒤에 된다
				       (나) 무료 하루 한도를 다 썼다      -> 30초 뒤에도 안 된다 (다음날 리셋)

				     둘을 같은 문구로 안내하면 (나) 인 사용자는 "30초 뒤"를 믿고
				     계속 다시 눌러본다. 될 리가 없는데 계속 기다리게 만드는 안내다.

				   [After] OpenRouter 응답의 limit_source 를 보고 구분한다.
				     일일 한도면 "오늘 사용량을 다 썼다"고 알려주고,
				     아니면 기존처럼 잠시 뒤 재시도를 안내한다.

				   [실제로 겪은 것]
				     무료 티어 하루 한도는 50회다. 검사를 반복해 돌리다 소진했는데
				     "30초 뒤에 다시"라고 나와서 코드가 고장난 것으로 오해했다.
				     응답 본문을 직접 열어보고서야 일일 한도임을 알았다.
				 ============================================================================
				*/
				boolean daily = errorBody != null
						&& (errorBody.contains("free-models-per-day")
						 || errorBody.contains("free_tier_daily")
						 || errorBody.contains("per-day"));

				if (daily) {   // 하루 한도를 다 쓴 경우라면
					return "오늘 무료 AI 사용량을 모두 썼습니다.\n"   // 내일 다시 오라고 정확히 알려 준다 (30초 뒤에는 안 되기 때문이다)
					     + "내일 다시 이용해주세요. (관리자: OpenRouter 크레딧을 충전하거나 다른 모델로 바꾸면 바로 풀립니다)";
				}
				return "요청이 너무 많습니다. 잠시 후(30초) 다시 시도해주세요.";   // 잠깐 몰린 경우라면 30초 뒤 재시도를 안내한다

			} else if (responseCode == 401) {   // 401 = 키가 잘못됐다
				return "API 키가 유효하지 않습니다. 관리자에게 문의해주세요.";   // 사용자가 고칠 수 없는 문제이므로 관리자 문의를 안내한다
			} else if (responseCode == 404) {   // 404 = 그 모델을 지금 쓸 수 없다
				// 무료 모델 일시적 404 → 재시도 대상
				throw new java.io.IOException("모델 일시 불가 (404): " + errorBody);
			} else if (responseCode >= 500) {   // 500대 = AI 서버 쪽 문제다
				// 서버 오류는 IOException으로 변환하여 재시도 대상으로 만듦
				throw new java.io.IOException("서버 오류 (코드: " + responseCode + "): " + errorBody);
			}
			return "AI 서비스 오류(코드:" + responseCode + "): " + errorBody;   // 위 어느 경우도 아니면 코드와 내용을 함께 알려 준다
		}

		con.disconnect();   // 연결을 끊는다 (자원 반납)

		// 응답에서 텍스트 추출
		return parseOpenRouterResponse(responseStr);
	}
	
	//===================================
	// buildOpenRouterRequest() - OpenRouter API 요청 JSON 만들기
	// OpenAI 호환 형식: messages 배열에 role/content 사용
	//===================================
	@SuppressWarnings("unchecked") //json-simple의 JSONObject/JSONArray가 제네릭 없는 컬렉션이라 발생하는 경고 억제
	private JSONObject buildOpenRouterRequest(String userMessage, String historyJson) {
		
		JSONObject requestBody = new JSONObject();   // AI 서버에 보낼 요청 전체를 담을 JSON 객체를 만든다
		requestBody.put("model", modelName);   // 어떤 모델을 쓸지 지정한다
		
		JSONArray messages = new JSONArray();   // 대화 메시지들을 순서대로 담을 배열을 만든다
		
		// 1. 시스템 메시지 (AI의 역할 지정)
		JSONObject systemMsg = new JSONObject();
		systemMsg.put("role", "system");   // system = "너는 이런 역할이다" 를 알려주는 자리
		systemMsg.put("content", systemPrompt);   // 서버가 켜질 때 만들어 둔 안내문(차량 26대 정보 포함)을 넣는다
		messages.add(systemMsg);   // 첫 메시지로 담는다. 순서가 중요하다 — system 이 가장 앞이어야 한다
		
		// 2. 이전 대화 기록 추가 (최근 6개만 사용 - 토큰 한도 초과 방지)
		if (historyJson != null && !historyJson.trim().isEmpty()) {
			try {
				JSONParser parser = new JSONParser();
				JSONArray history = (JSONArray) parser.parse(historyJson);   // 브라우저가 보낸 대화 기록 글자를 JSON 배열로 바꾼다

				// 무료 모델 토큰 한도 초과 방지: 최근 6개 메시지만 포함
				int startIdx = Math.max(0, history.size() - 6);

				for (int i = startIdx; i < history.size(); i++) {   // 최근 6개만 앞에서부터 차례로 담는다
					JSONObject msg = (JSONObject) history.get(i);   // 메시지 하나를 꺼낸다
					String role = (String) msg.get("role");   // 누가 한 말인지 (user 또는 model)
					String text = (String) msg.get("text");   // 무슨 말을 했는지

					// Gemini의 "model" 역할을 OpenAI의 "assistant"로 변환
					String openaiRole = "model".equals(role) ? "assistant" : role;

					JSONObject chatMsg = new JSONObject();   // 메시지 하나를 담을 JSON 객체를 만든다
					chatMsg.put("role", openaiRole);   // 변환한 역할 이름을 담는다
					chatMsg.put("content", text);   // 말한 내용을 담는다
					messages.add(chatMsg);   // 메시지 목록에 담는다
				}
			} catch (Exception e) {
				System.out.println("[ChatbotController] 대화 기록 파싱 오류: " + e.getMessage());   // 기록이 깨져도 챗봇은 동작해야 한다. 원인만 콘솔에 남기고 기록 없이 진행한다
			}
		}
		
		// 3. 현재 사용자 메시지 추가
		JSONObject userMsg = new JSONObject();
		userMsg.put("role", "user");   // user = 사용자가 한 말이라는 표시
		userMsg.put("content", userMessage);   // 이번에 입력한 메시지
		messages.add(userMsg);   // 메시지 목록의 맨 마지막에 담는다
		
		requestBody.put("messages", messages);   // 완성된 메시지 목록을 요청에 담는다
		
		// 4. 생성 설정
		requestBody.put("temperature", 0.7);
		requestBody.put("max_tokens", 1024);   // 답변 길이 상한. 너무 길면 요금과 시간이 늘어난다
		
		return requestBody;   // 완성된 요청 객체를 돌려준다
	}
	
	//===================================
	// parseOpenRouterResponse() - 응답 JSON에서 AI 텍스트 추출
	// OpenAI 호환 형식: choices[0].message.content
	//===================================
	private String parseOpenRouterResponse(String responseStr) {
		try {
			System.out.println("[ChatbotController] 원본 응답: " + responseStr);   // AI 가 뭐라고 답했는지 통째로 콘솔에 남긴다. 파싱이 틀렸을 때 여기부터 본다

			JSONParser parser = new JSONParser();   // JSON 글자를 자바 객체로 바꿀 도구를 만든다
			JSONObject response = (JSONObject) parser.parse(responseStr);   // 응답 전체를 JSON 객체로 바꾼다

			// choices 배열에서 첫 번째 응답 추출
			JSONArray choices = (JSONArray) response.get("choices");
			if (choices != null && choices.size() > 0) {   // 답변이 하나라도 있으면
				JSONObject firstChoice = (JSONObject) choices.get(0);   // 첫 번째 답변을 꺼낸다
				JSONObject message = (JSONObject) firstChoice.get("message");   // 그 안의 message 부분을 꺼낸다
				if (message != null) {   // message 가 있으면
					// content 필드 추출 (일부 모델은 content가 null이고 reasoning 필드를 사용)
					Object contentObj = message.get("content");
					String content = (contentObj != null) ? contentObj.toString() : null;   // 값이 있으면 글자로 바꾸고, 없으면 null 로 둔다
					if (content != null && !content.trim().isEmpty()) {   // 내용이 비어 있지 않으면
						return content;   // 그 내용을 답변으로 돌려준다
					}
					// content가 비어있으면 reasoning 필드 확인 (thinking 모델용)
					Object reasoningObj = message.get("reasoning");
					if (reasoningObj != null) {   // reasoning 값이 있으면
						String reasoning = reasoningObj.toString();   // 글자로 바꾼다
						if (!reasoning.trim().isEmpty()) {   // 비어 있지 않으면
							return reasoning;   // 그것을 답변으로 대신 쓴다
						}
					}
				}
			}

			// error 필드 확인
			JSONObject error = (JSONObject) response.get("error");
			if (error != null) {   // error 부분이 있으면 = AI 서버가 오류를 알려 준 것이다
				String errMsg = (String) error.get("message");   // 오류 메시지를 꺼낸다
				System.out.println("[ChatbotController] API 에러 메시지: " + errMsg);   // 원인을 콘솔에 남긴다
				return "AI 응답 오류: " + errMsg;   // 사용자에게도 그 이유를 그대로 전한다
			}

			System.out.println("[ChatbotController] 파싱 실패 - 응답 구조: " + response.toJSONString());   // 우리가 모르는 응답 구조다. 통째로 콘솔에 남겨 나중에 확인한다
			return "죄송합니다. 응답을 처리하지 못했습니다. 다시 질문해주세요.";   // 사용자에게는 무난한 안내를 준다
			
		} catch (Exception e) {
			System.out.println("[ChatbotController] 응답 파싱 오류: " + e.getMessage());   // 파싱에 실패한 원인을 콘솔에 남긴다
			System.out.println("[ChatbotController] 원본 응답: " + responseStr);   // 원본 응답도 함께 남겨야 원인을 찾을 수 있다
			return "죄송합니다. 응답을 처리하지 못했습니다. 다시 질문해주세요.";   // 사용자에게는 무난한 안내를 준다
		}
	}
	
	//===================================
	// readBody() - 응답 내용을 읽어서 문자열로 반환하는 메소드
	//===================================
	private String readBody(InputStream body) {
		InputStreamReader streamReader = new InputStreamReader(body, java.nio.charset.StandardCharsets.UTF_8);   // 바이트 통로를 글자 통로로 바꾼다. UTF-8 을 지정해야 한글이 안 깨진다
		
		try (BufferedReader lineReader = new BufferedReader(streamReader)) {   // 줄 단위로 읽는 도구로 한 번 더 감싼다. 다 쓰면 자동으로 닫힌다
			StringBuilder responseBody = new StringBuilder();   // 읽은 줄을 이어 붙일 준비
			String line;   // 읽은 한 줄을 담을 변수
			while ((line = lineReader.readLine()) != null) {   // 더 읽을 것이 없으면 null 이 나온다. 그때까지 반복한다
				responseBody.append(line);   // 읽은 줄을 뒤에 이어 붙인다
			}
			return responseBody.toString();   // 완성된 응답 글자를 돌려준다
		} catch (IOException e) {
			throw new RuntimeException("API 응답 읽기 실패: " + e.getMessage(), e);   // 읽는 도중 끊긴 경우다. 원인을 담아 위로 던진다
		}
	}
	
	//===================================
	// buildAIServicePrompt() - AI 서비스 유형별 맞춤 시스템 프롬프트 생성
	// type: "recommend"(차량추천), "cost"(비용계산), "travel"(여행플래너)
	//===================================
	private String buildAIServicePrompt(String serviceType) {
		StringBuilder prompt = new StringBuilder();   // AI 에게 줄 지시문을 조립할 준비

		if ("recommend".equals(serviceType)) {   // 차량 추천 서비스라면
			prompt.append("당신은 (주)SM렌탈의 AI 차량 추천 전문가입니다.\n");   // 아래 여러 줄을 이어 붙여 "차량 추천 전문가" 역할을 지시한다
			prompt.append("고객의 인원수, 예산, 용도를 분석하여 가장 적합한 차량을 추천해주세요.\n");
			prompt.append("반드시 아래 보유 차량 목록에서만 추천하세요.\n");
			prompt.append("추천 차량은 최대 3대까지, 각각 추천 이유를 설명해주세요.\n");
			prompt.append("이모지를 적절히 사용해 친근감을 주세요.\n\n");
			prompt.append("답변 형식:\n");   // AI 가 답할 형식까지 정해 준다. 형식을 정해야 화면에 일정하게 보인다
			prompt.append("**1. 차량명** - 추천 이유\n");
			prompt.append("- 일일 렌탈료: OO원\n");
			prompt.append("- 탑승 인원: O명\n");
			prompt.append("- 등급: OO\n\n");
		} else if ("cost".equals(serviceType)) {   // 비용 계산 서비스라면
			prompt.append("당신은 (주)SM렌탈의 AI 비용 계산 전문가입니다.\n");   // 아래 여러 줄을 이어 붙여 "비용 계산 전문가" 역할을 지시한다
			prompt.append("고객이 선택한 차량, 대여기간, 옵션을 바탕으로 총 비용을 계산해주세요.\n");
			prompt.append("비용 계산 공식: 총 비용 = (차량 일일렌탈료 + 선택 옵션 합계) x 대여일수\n\n");
			//[변경] 여기도 CarService 상수를 사용해 결제 금액과 어긋나지 않게 한다
			prompt.append("옵션 가격 (1일 기준):\n");
			prompt.append(String.format("- 자차보험: %,d원/일%n", CarService.PRICE_INSURANCE));   // 옵션 요금은 CarService 의 상수를 그대로 넣는다. 요금이 바뀌면 AI 안내도 함께 바뀐다
			prompt.append(String.format("- WiFi: %,d원/일%n", CarService.PRICE_WIFI));   // WiFi 요금도 상수에서 읽어 넣는다
			prompt.append(String.format("- 네비게이션: %,d원/일%n", CarService.PRICE_NAVI));   // 네비게이션 요금도 상수에서 읽어 넣는다
			prompt.append(String.format("- 베이비시트: %,d원/일%n%n", CarService.PRICE_BABYSEAT));   // 베이비시트 요금도 상수에서 읽어 넣는다
			prompt.append("비용을 항목별로 정리해주시고, 절약 팁도 알려주세요.\n");   // 계산 결과만이 아니라 절약 팁까지 요청한다
			prompt.append("이모지를 적절히 사용해 친근감을 주세요.\n\n");
		} else if ("git".equals(serviceType)) {   // Git 도우미 서비스라면
			prompt.append("[절대규칙] 반드시 한국어로만 답변하세요. 영어 답변 금지. 모든 설명, 예시, 비유 전부 한국어로 작성합니다.\n\n");   // 아래 100여 줄을 이어 붙여 "Git 강사" 역할과 지식을 통째로 지시한다

			prompt.append("당신은 10년차 Git & GitHub 전문 강사이자 최고의 코딩 튜터입니다.\n");
			prompt.append("비전공자 초보자가 팀프로젝트에서 Git/GitHub를 완벽하게 사용할 수 있도록 도와주세요.\n");
			prompt.append("학생이 완전 처음이라고 가정하고, 컴퓨터 비유(폴더, 파일, USB 등)로 쉽게 설명하세요.\n\n");

			prompt.append("=== 당신의 강점 ===\n");
			prompt.append("- 어려운 개념을 일상생활 비유로 쉽게 설명 (예: commit=세이브, branch=평행우주)\n");
			prompt.append("- 명령어마다 '왜 이걸 해야 하는지' 이유를 설명\n");
			prompt.append("- 실수했을 때 복구 방법을 항상 함께 안내\n");
			prompt.append("- 실제 팀프로젝트 상황 예시로 설명\n\n");

			prompt.append("=== Git 핵심 지식 (답변에 활용) ===\n\n");

			prompt.append("[ 기본 개념 비유 ]\n");
			prompt.append("- 작업 디렉토리(Working Directory) = 내 책상 위 (지금 작업 중인 파일들)\n");
			prompt.append("- 스테이징(Staging Area) = 택배 상자 (보낼 물건을 담는 곳, git add)\n");
			prompt.append("- 로컬 저장소(Local Repository) = 내 집 창고 (git commit으로 보관)\n");
			prompt.append("- 원격 저장소(Remote/GitHub) = 공용 창고 (git push로 올림, git pull로 가져옴)\n");
			prompt.append("- 브랜치(Branch) = 평행우주 (독립적으로 작업 후 나중에 합침)\n");
			prompt.append("- 머지(Merge) = 평행우주 합치기\n");
			prompt.append("- 충돌(Conflict) = 같은 줄을 두 사람이 동시에 수정해서 Git이 혼란\n");
			prompt.append("- HEAD = 지금 내가 서 있는 위치 표시\n\n");

			prompt.append("[ 필수 초기 설정 (최초 1회) ]\n");
			prompt.append("git config --global user.name \"본인이름\"\n");
			prompt.append("git config --global user.email \"GitHub가입이메일\"\n");
			prompt.append("→ Git이 '누가 작업했는지' 기록하기 위해 반드시 필요\n\n");

			prompt.append("[ 매일 쓰는 기본 흐름 ]\n");
			prompt.append("1. git pull origin main     ← 출근! 최신 코드 가져오기\n");
			prompt.append("2. (코딩 작업...)            ← 열심히 개발\n");
			prompt.append("3. git add .                ← 변경파일 택배상자에 담기\n");
			prompt.append("4. git commit -m \"메시지\"    ← 상자 포장 + 라벨 붙이기\n");
			prompt.append("5. git push origin main     ← 공용 창고에 보내기\n\n");

			prompt.append("[ 브랜치 협업 흐름 (추천) ]\n");
			prompt.append("1. git checkout -b feature/기능명  ← 새 브랜치(평행우주) 생성+이동\n");
			prompt.append("2. (코딩 작업...)                   ← 이 브랜치에서 자유롭게 작업\n");
			prompt.append("3. git add . → git commit -m \"...\" ← 저장\n");
			prompt.append("4. git push origin feature/기능명   ← 내 브랜치를 GitHub에 올림\n");
			prompt.append("5. GitHub에서 Pull Request 생성     ← 팀원에게 '합쳐주세요' 요청\n");
			prompt.append("6. 팀원 코드리뷰 후 Merge           ← main에 합침\n\n");

			prompt.append("[ 충돌 해결 방법 ]\n");
			prompt.append("충돌 발생 시 파일에 아래 표시가 생김:\n");
			prompt.append("<<<<<<< HEAD\n");
			prompt.append("내가 수정한 내용\n");
			prompt.append("=======\n");
			prompt.append("상대방이 수정한 내용\n");
			prompt.append(">>>>>>> branch명\n");
			prompt.append("해결: <<<, ===, >>> 표시를 지우고 원하는 코드만 남긴 후 저장 → add → commit\n\n");

			prompt.append("[ 자주 발생하는 에러와 해결법 ]\n");
			prompt.append("에러1: 'rejected - non-fast-forward'\n");
			prompt.append("  원인: 다른 팀원이 먼저 push해서 내 코드가 뒤처짐\n");
			prompt.append("  해결: git pull origin main → 충돌 있으면 해결 → 다시 push\n\n");
			prompt.append("에러2: 'fatal: not a git repository'\n");
			prompt.append("  원인: Git 저장소가 아닌 폴더에서 명령어 실행\n");
			prompt.append("  해결: cd 명령어로 프로젝트 폴더로 이동하거나, git init 실행\n\n");
			prompt.append("에러3: 'CONFLICT (content): Merge conflict'\n");
			prompt.append("  원인: 같은 파일 같은 줄을 동시에 수정\n");
			prompt.append("  해결: 파일 열어서 <<<< ==== >>>> 표시 정리 → add → commit\n\n");
			prompt.append("에러4: 'Permission denied (publickey)'\n");
			prompt.append("  원인: GitHub 인증 실패\n");
			prompt.append("  해결: GitHub 토큰 확인 또는 SSH 키 재설정\n\n");
			prompt.append("에러5: 'Your local changes would be overwritten'\n");
			prompt.append("  원인: 로컬 변경사항이 있는데 pull 시도\n");
			prompt.append("  해결: git stash → git pull → git stash pop\n\n");

			prompt.append("[ Eclipse Dynamic Web Project .gitignore 추천 ]\n");
			prompt.append("/build/\n");
			prompt.append("/.settings/\n");
			prompt.append("/.classpath\n");
			prompt.append("/.project\n");
			prompt.append("/target/\n\n");

			prompt.append("[ 유용한 명령어 모음 ]\n");
			prompt.append("git status       ← 현재 상태 확인 (가장 많이 씀!)\n");
			prompt.append("git log --oneline ← 커밋 기록 한줄씩 보기\n");
			prompt.append("git diff         ← 뭐가 바뀌었는지 확인\n");
			prompt.append("git branch       ← 브랜치 목록 보기\n");
			prompt.append("git branch -d 이름 ← 브랜치 삭제\n");
			prompt.append("git checkout main ← main 브랜치로 이동\n");
			prompt.append("git merge 브랜치명 ← 브랜치 합치기\n");
			prompt.append("git stash        ← 작업 임시 저장 (급할 때)\n");
			prompt.append("git stash pop    ← 임시 저장 꺼내기\n");
			prompt.append("git reset HEAD 파일명 ← add 취소\n");
			prompt.append("git clone URL    ← 원격 저장소 복제\n");
			prompt.append("git remote -v    ← 연결된 원격 저장소 확인\n\n");

			prompt.append("=== 답변 형식 규칙 ===\n");
			prompt.append("1. [필수] 모든 답변은 반드시 한국어로 작성\n");
			prompt.append("2. 명령어는 반드시 `백틱`으로 감싸기\n");
			prompt.append("3. 단계별로 번호를 매겨 설명 (1, 2, 3...)\n");
			prompt.append("4. 각 명령어마다 '이 명령어는 ~하는 역할입니다' 식으로 의미 설명\n");
			prompt.append("5. 비유를 최소 1개 이상 사용해서 이해를 도움\n");
			prompt.append("6. 실수/에러 가능성이 있으면 '주의사항'도 함께 안내\n");
			prompt.append("7. 이모지를 적절히 사용해 친근감 주기\n");
			prompt.append("8. Git과 관련 없는 질문에는 정중히 거절하고 Git 도움으로 안내\n");
			return prompt.toString();   // 완성된 지시문을 돌려준다 (아래 차량 목록은 붙이지 않는다. Git 질문에는 필요 없다)

		} else if ("travel".equals(serviceType)) {   // 여행 플래너 서비스라면
			prompt.append("당신은 (주)SM렌탈의 AI 여행 플래너입니다.\n");   // 아래 여러 줄을 이어 붙여 "여행 플래너" 역할을 지시한다
			prompt.append("고객의 여행지, 일수, 인원, 스타일을 바탕으로 여행 일정을 만들어주세요.\n");
			prompt.append("일정에 맞는 추천 차량도 함께 안내해주세요.\n");
			prompt.append("반드시 아래 보유 차량 목록에서만 추천하세요.\n");
			prompt.append("이모지를 적절히 사용해 친근감을 주세요.\n\n");
			prompt.append("답변 형식: 일차별로 오전/오후 일정을 나누어 작성하세요.\n");
			prompt.append("마지막에 추천 차량과 예상 렌탈 비용을 안내해주세요.\n\n");
		}

		// 기존 systemPrompt에서 차량 목록 부분 재사용
		int startIdx = systemPrompt.indexOf("=== 보유 차량 목록 ===");
		if (startIdx >= 0) {   // 차량 목록 부분을 찾았으면
			prompt.append(systemPrompt.substring(startIdx));   // 그 부분부터 끝까지를 잘라 지시문 뒤에 붙인다 (차량 정보를 다시 쓰지 않으려는 것이다)
		}

		return prompt.toString();   // 완성된 지시문을 돌려준다
	}

	//===================================
	// callOpenRouterAPIWithPrompt() - 커스텀 시스템 프롬프트로 단발성 AI 호출
	// 대화 기록 없이 한 번의 질문-응답 (AI 서비스용)
	//===================================
	@SuppressWarnings("unchecked") //json-simple의 JSONObject/JSONArray가 제네릭 없는 컬렉션이라 발생하는 경고 억제
	private String callOpenRouterAPIWithPrompt(String userMessage, String customSystemPrompt, String serviceType) {
		if (apiKey == null || "YOUR_API_KEY_HERE".equals(apiKey)) {   // 키가 없거나 예시 값 그대로면
			return "AI API 키가 설정되지 않았습니다. 관리자에게 문의해주세요.";   // AI 를 부를 수 없으므로 안내만 돌려준다
		}

		// 요청 JSON 생성 (대화 기록 없음, 시스템 프롬프트 + 사용자 메시지만)
		JSONObject requestBody = new JSONObject();
		requestBody.put("model", modelName);   // 어떤 모델을 쓸지 지정한다

		JSONArray messages = new JSONArray();   // 대화 메시지들을 담을 배열을 만든다

		JSONObject systemMsg = new JSONObject();   // 역할 지시문을 담을 JSON 객체를 만든다
		systemMsg.put("role", "system");   // system = "너는 이런 역할이다" 를 알려주는 자리
		systemMsg.put("content", customSystemPrompt);   // 서비스 종류에 맞게 만든 지시문을 넣는다
		messages.add(systemMsg);   // 첫 메시지로 담는다

		JSONObject userMsg = new JSONObject();   // 사용자 메시지를 담을 JSON 객체를 만든다
		userMsg.put("role", "user");   // user = 사용자가 한 말이라는 표시
		userMsg.put("content", userMessage);   // 이번에 입력한 내용
		messages.add(userMsg);   // 메시지 목록에 담는다

		requestBody.put("messages", messages);   // 완성된 메시지 목록을 요청에 담는다
		requestBody.put("temperature", 0.7);   // 0에 가까울수록 딱딱하고 일정한 답, 1에 가까울수록 자유로운 답이 나온다
		// git 타입은 상세 설명이 필요하므로 토큰 늘림
		requestBody.put("max_tokens", "git".equals(serviceType) ? 3072 : 2048);

		String jsonStr = requestBody.toJSONString();   // 만든 JSON 객체를 실제로 보낼 글자로 바꾼다
		System.out.println("[ChatbotController] AI서비스 요청 JSON 길이: " + jsonStr.length());   // 얼마나 긴 요청을 보내는지 콘솔에 남긴다

		// 기존 재시도 로직 재사용
		int maxRetries = 2;

		for (int attempt = 1; attempt <= maxRetries; attempt++) {   // 1회차, 2회차 순서로 시도한다
			try {
				return doApiCall(jsonStr);   // 성공하면 그 답을 바로 돌려준다
			} catch (java.net.SocketTimeoutException e) {
				System.out.println("[ChatbotController] AI서비스 타임아웃 (시도 " + attempt + "/" + maxRetries + ")");   // 몇 번째 시도에서 시간이 초과됐는지 콘솔에 남긴다
				if (attempt < maxRetries) {   // 아직 재시도가 남아 있으면
					try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }   // 2초 쉬었다가 다시 시도한다
				}
			} catch (java.io.IOException e) {
				System.out.println("[ChatbotController] AI서비스 네트워크 오류 (시도 " + attempt + "/" + maxRetries + ")");   // 몇 번째 시도에서 네트워크가 끊겼는지 콘솔에 남긴다
				if (attempt < maxRetries) {   // 아직 재시도가 남아 있으면
					try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }   // 2초 쉬었다가 다시 시도한다
				}
			} catch (Exception e) {
				System.out.println("[ChatbotController] AI서비스 오류: " + e.getMessage());   // 예상 못 한 오류의 원인을 콘솔에 남긴다
				return "AI 서비스 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";   // 사용자에게는 무난한 안내를 준다
			}
		}

		return "AI 응답 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.";   // 두 번 다 실패했으면 시간 초과 안내를 준다
	}

	//===========================================================
	// 요청 진입점 : BaseController 가 GET/POST 를 한곳으로 모아준다
	//===========================================================
	/*
	  [왜 doGet/doPost 를 직접 만들지 않는가]
	    공통 예외 처리를 BaseController 한곳에 모으기 위해서다.
	    챗봇은 AJAX 요청이라 HTML 에러페이지를 주면 화면에 태그가 그대로 찍힌다.
	    BaseController 는 X-Requested-With 헤더를 보고 AJAX 인 경우
	    메시지 문자열만 응답하도록 처리해준다.
	 */
	@Override
	protected void process(String action, HttpServletRequest request, HttpServletResponse response) throws Exception {
		doHandle(request, response);   // 실제 처리는 위에 있는 doHandle 이 한다
	}

}
