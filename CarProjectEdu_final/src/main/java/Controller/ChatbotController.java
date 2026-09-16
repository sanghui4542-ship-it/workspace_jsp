package Controller;   // [패키지] 이 파일이 들어 있는 폴더 이름. src/main/java/Controller 폴더 안에 있어야 한다. 폴더명과 이 줄이 다르면 컴파일 에러가 난다.

/* ============================================================================
   [import 구역]
   자바는 "내가 이 파일에서 어떤 도구를 쓸 것인지" 를 맨 위에 미리 선언한다.
   비유하자면 요리 시작 전에 칼, 도마, 냄비를 조리대에 꺼내 두는 것이다.
   여기 적지 않은 도구는 아래 코드에서 이름만 써도 자바가 알아듣지 못한다.
   ============================================================================ */

import java.io.BufferedReader;        // 글자를 "줄 단위"로 읽는 도구. (AI 서버가 보낸 답변을 한 줄씩 읽을 때 사용)
import java.io.IOException;           // 파일/네트워크 읽고쓰기가 실패했을 때 자바가 던지는 '사고 보고서'. 반드시 처리하거나 위로 넘겨야 한다.
import java.io.InputStream;           // 바깥에서 우리 프로그램으로 데이터(바이트)가 들어오는 '수도관'
import java.io.InputStreamReader;     // 바이트 수도관을 '글자' 수도관으로 바꿔 주는 어댑터 (여기서 UTF-8 지정 → 한글 안 깨짐)
import java.io.OutputStream;          // 우리 프로그램에서 바깥으로 데이터를 내보내는 '수도관'
import java.io.PrintWriter;           // 브라우저 화면(응답)에 글자를 직접 찍어 보내는 붓. AJAX 응답을 쓸 때 사용
import java.net.HttpURLConnection;    // 자바가 "브라우저처럼" 다른 서버에 직접 요청을 보낼 때 쓰는 도구
import java.net.URL;                  // 인터넷 주소 한 개를 담는 상자 (예: https://openrouter.ai/...)

import javax.servlet.ServletConfig;                 // 서블릿이 시작될 때 넘어오는 '설정표'
import javax.servlet.ServletException;              // 서블릿이 처리 중 실패했을 때의 사고 보고서
import javax.servlet.annotation.WebServlet;         // "이 서블릿은 어떤 주소의 요청을 받는다" 를 적는 표시(어노테이션)
import javax.servlet.http.HttpServletRequest;       // 브라우저가 보낸 것(주소·입력값·세션)이 전부 담겨 오는 상자
import javax.servlet.http.HttpServletResponse;      // 브라우저에게 돌려줄 것을 담는 상자

import org.json.simple.JSONArray;                   // [ 값, 값, 값 ] 형태의 JSON 목록을 만드는 도구
import org.json.simple.JSONObject;                  // { "이름": "값" } 형태의 JSON 을 만드는 도구
import org.json.simple.parser.JSONParser;           // JSON '글자'를 자바가 다룰 수 있는 '객체'로 바꿔 주는 번역기

import Service.CarService;              // 업무 규칙(요금 계산 등)을 담당하는 우리 프로젝트 클래스
import Vo.CarListVo;                    // 차량 한 대의 정보를 담아 나르는 '상자' 클래스 (VO = Value Object)
import util.ReserveTextParser;          // "내일부터 3일간 4명" 같은 문장을 규칙으로 해석하는 우리 도우미 클래스

/*
 ============================================================
 ChatbotController.java - AI 렌트카 상담 챗봇 서블릿
 ============================================================

 [ 서블릿이 뭔가요? (비전공자용) ]
   서블릿(Servlet) = '웹 요청을 받아 처리하는 자바 프로그램'.
   식당으로 비유하면
     - 손님(브라우저)이 주문서를 낸다            → request
     - 종업원(서블릿)이 주문을 받아 주방에 넘긴다 → 이 파일
     - 음식이 나오면 손님에게 갖다 준다           → response

 [ 이 파일의 역할 — 딱 3가지 ]
   1) /Chatbot/send.do      : 일반 상담 챗봇   (사용자 질문 → AI 답변)
   2) /Chatbot/ai.do        : 특수 서비스      (차량추천 / 비용계산 / Git도우미 / 여행플래너)
   3) /Chatbot/reserveAI.do : 말로 예약하기    (문장 → 조건 추출 → DB에서 실제 차량 추천)

 [ 사용하는 AI 서비스 ]
   - OpenRouter (https://openrouter.ai)
   - 여러 AI 모델을 '한 창구'에서 쓸 수 있게 해 주는 중개 서비스
   - 무료 모델을 쓰면 비용 0원

 ============================================================
 [비전공자를 위한 OpenRouter API 키 발급 가이드]
 ============================================================

 API 키란?
   → AI 서비스를 쓰기 위한 '출입증' 이자 '비밀번호'.
     이 키가 없으면 AI 서버가 "너 누구냐" 하며 문을 안 열어 준다.

 [발급 순서]
   1단계: https://openrouter.ai 접속 → 우측 상단 "Sign In" → 구글 계정 로그인
   2단계: 왼쪽 메뉴 "Keys" (또는 https://openrouter.ai/keys) → "Create Key" 클릭
   3단계: sk-or-v1-... 로 시작하는 문자열 복사 (Ctrl+C)
          ※ 이 키는 절대 남에게 보여 주면 안 된다. 카드 번호와 같다.
   4단계: 아래 API_KEY 상수 자리에 붙여넣기 → 저장(Ctrl+S)
   5단계: Eclipse 에서 Tomcat 서버 Stop → Start (반드시 재시작해야 반영됨)

 ============================================================
*/

@WebServlet("/Chatbot/*")   // [주소 등록] /Chatbot 으로 시작하는 모든 요청을 이 클래스가 받는다. * 는 '뒤에 뭐가 붙든' 이라는 뜻.
public class ChatbotController extends BaseController {   // extends = 상속. BaseController(우리가 만든 공통 부모)의 기능을 물려받는다.

	// [직렬화 번호] 서블릿은 규칙상 이 번호를 갖는 게 좋다. 없으면 노란 경고줄이 뜬다. 기능과는 무관하니 그냥 1L 로 둔다.
	private static final long serialVersionUID = 1L;

	// [필드 = 이 클래스가 계속 들고 다니는 값] --------------------------------

	// AI 에게 맨 먼저 읽히는 '역할 설명서'. 차량 26대 정보가 통째로 들어간다. (init 에서 1번만 만든다)
	private String systemPrompt;

	// OpenRouter 출입증(API 키)을 담아 둘 변수
	private String apiKey;

	// AI 서버의 주소. static final = "절대 안 바뀌는 값" 이라는 표시(상수).
	private static final String API_URL = 
	//"https://openrouter.ai/api/v1/chat/completions";
	
	// 사용할 무료 모델 이름. openrouter/free 는 "지금 쓸 수 있는 무료 모델 아무거나 알아서 골라줘" 라는 뜻.
	private static final String DEFAULT_MODEL = "openrouter/free";

	/* ★★★ 반드시 본인 키로 교체하세요 ★★★
	   [보안 경고] 원본 파일에는 실제 키가 그대로 적혀 있었다.
	   소스에 키를 박아 두면 GitHub 에 올리는 순간 전 세계에 공개된다.
	   → 기존 키는 OpenRouter 사이트에서 '삭제(Revoke)' 하고 새 키를 발급받아 쓰는 것을 권한다.
	   → 더 안전한 방법은 web.xml 이나 환경변수에서 읽어오는 것이다. (아래 init 주석 참고) */
	private static final String API_KEY =  "여기에 발급 받은 본인 KEY 넣으세요";


	// 실제로 사용할 모델명을 담는 변수. 상수(DEFAULT_MODEL)를 기본값으로 복사해 둔다.
	// [왜 상수를 그대로 안 쓰고 변수에 담나?] 무료 모델 목록은 자주 바뀐다.
	// 변수로 두면 나중에 설정파일에서 읽어와 '재컴파일 없이' 모델을 바꿀 수 있다.
	private String modelName = DEFAULT_MODEL;

	// 차량 정보를 DB 에서 꺼내오는 담당자.
	// transient = "서블릿을 파일로 저장할 때 이건 빼라" 는 표시. (경고 방지용)
	private transient CarService carService;

	//===================================================================
	// init() — 서버가 켜질 때 '딱 한 번' 실행되는 준비 메소드
	//   [왜 한 번만 하나?]
	//     차량 26대 정보를 매 요청마다 DB 에서 읽으면 느리다.
	//     어차피 잘 안 바뀌는 정보이므로 켜질 때 한 번 읽어 두고 계속 재사용한다.
	//===================================================================
	@Override   // @Override = "부모가 가진 메소드를 내가 다시 만든다" 는 표시. 오타를 내면 컴파일 에러로 잡아 준다.
	public void init(ServletConfig config) throws ServletException {
		super.init(config);   // 부모(HttpServlet)의 준비 작업을 먼저 시킨다. 이걸 빼먹으면 서블릿이 정상 동작하지 않는다.

		apiKey = API_KEY;     // 위에 적어 둔 키를 실제 사용할 변수에 옮긴다.
		// [더 나은 방법] config.getInitParameter("openrouter.key") 로 web.xml 에서 읽어오면
		//                소스코드에 키가 남지 않는다. 실무에서는 이 방식을 쓴다.

		modelName = DEFAULT_MODEL;   // 사용할 모델명 지정

		/* ----- DB 에서 전체 차량 목록 조회 -----
		   [계층 구조 이야기]
		     Controller(주문받기) → Service(업무규칙) → DAO(DB작업)
		   컨트롤러가 DAO 를 직접 부르면 계층이 무너진다.
		   그래서 반드시 Service 를 거쳐서 부른다. */
		carService = new CarService();                                    // 차량 담당자 생성 (다른 메소드에서도 써야 해서 필드에 보관)
		java.util.List<CarListVo> carList = carService.getAllCars();      // 차량 전체를 한 번에 읽어 온다. List = 순서가 있는 목록 상자.

		// ----- 읽어 온 차량 정보를 AI 가 읽을 '한 덩어리 글'로 정리 -----
		StringBuilder carInfo = new StringBuilder();
		// [StringBuilder 를 쓰는 이유] String 을 + 로 계속 이어 붙이면 붙일 때마다 새 문자열이 만들어져 느리다.
		//                              StringBuilder 는 하나의 메모장에 계속 이어 쓰는 방식이라 훨씬 빠르다.

		for (CarListVo car : carList) {   // for-each 문 : carList 안의 차를 하나씩 꺼내 car 라는 이름으로 사용한다.
			carInfo.append("- ").append(car.getCarname())                                     // 차량명
				  .append(" | 제조사: ").append(car.getCarcompany())                          // 제조사
				  .append(" | 일일렌탈료: ").append(String.format("%,d", car.getCarprice())).append("원")   // %,d = 1000단위 콤마 (50000 → 50,000)
				  .append(" | 탑승인원: ").append(car.getCarusepeople()).append("명")
				  .append(" | 등급: ").append(car.getCarcategory())
				  .append(" | 설명: ").append(car.getCarinfo())
				  .append("\n");                                                              // \n = 줄바꿈
		}

		// ----- AI 에게 줄 '역할 설명서(시스템 프롬프트)' 완성 -----
		// [시스템 프롬프트란?] AI 와 대화하기 전에 미리 읽히는 지침서.
		//                     "너는 누구고, 무엇을 알고 있고, 어떻게 답해야 하는지" 를 정해 준다.
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
			+ carInfo.toString() + "\n"                                   // 위에서 만든 차량 목록을 통째로 끼워 넣는다
			/*
			 [중요한 설계 이야기 — 왜 가격을 직접 안 쓰고 CarService 상수를 쓰나]
			   예전에는 여기에 "WiFi 5,000원" 이라고 직접 적어 두었고,
			   실제 결제 계산식은 CarController 안에 따로 "10,000원" 으로 있었다.
			   → 챗봇은 5천원이라 안내하고 결제는 1만원을 청구하는 사고가 났다.
			   지금은 '요금의 기준점'이 CarService 한 곳뿐이라 어긋날 수가 없다.
			   이것을 SSOT(Single Source of Truth, 단일 진실 공급원) 라고 부른다.
			*/
			+ "=== 추가 옵션 가격 (1일 기준) ===\n"
			+ String.format("- 자차보험: %,d원/일%n", CarService.PRICE_INSURANCE)     // %n = 줄바꿈 (운영체제에 맞는 줄바꿈 문자)
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

		// [로그] System.out.println 은 Eclipse 아래쪽 Console 창에 글자를 찍는 명령이다.
		//        화면에는 안 보이고 개발자만 본다. 문제가 생겼을 때 여기부터 확인한다.
		System.out.println("[ChatbotController] 시스템 프롬프트 생성 완료! 차량 " + carList.size() + "대 정보 로드됨");   // 0대로 찍히면 DB 연결을 의심할 것
		System.out.println("[ChatbotController] 사용 모델: " + modelName);
		System.out.println("[ChatbotController] API 키 설정됨: " + (apiKey != null && !apiKey.isEmpty()));   // ★키 값 자체는 절대 찍지 않는다. 있다/없다만 확인.
	}

	//===================================================================
	// doHandle() — 클라이언트(브라우저 등)에서 요청이 들어올 때마다 호출되는 메소드입니다.
	//   예를 들어 /send.do, /ai.do, /reserveAI.do 등 특정 주소(URL)로 요청이 오면
	//   그 주소에 맞는 작업을 골라서 실행합니다.
	//   쉽게 말하면 "요청의 역할 분배/분기점" 역할입니다.
	//===================================================================
	@SuppressWarnings("unchecked")
	// [이 표시는 뭔가요?]
	//  - 경고 메시지를 안 보이게 끄는 표시입니다.
	//  - json-simple 라이브러리의 JSONObject를 쓰면, put() 등을 쓸 때 컴파일러가
	//    "이거 안전한 타입이 아닐 수도 있음!" 이라는 노란 경고를 띄웁니다.
	//  - 하지만 자바에서 실제로 문제는 없으므로, 경고만 잠깐 꺼두겠습니다.
	protected void doHandle(HttpServletRequest request, HttpServletResponse response) 
							throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");                          
		// [이 설정은 뭔가요?]
		//  - 클라이언트(주로 브라우저)가 보낸 한글 데이터를 올바르게 읽으려면 인코딩을 UTF-8로 맞춰야 합니다.
		//  - 잘못하면 한글이 '???' 식으로 깨져보일 수 있습니다.

		response.setContentType("application/json;charset=UTF-8");      
		// [이 설정은 뭔가요?]
		//  - 서버에서 돌려주는 데이터가 "HTML"이 아니라 "JSON"이라는 것을 브라우저에 알려줍니다.
		//  - 그리고 문자 인코딩도 UTF-8로 명확하게 통보합니다.

		response.setCharacterEncoding("UTF-8");
		// [이 설정은 뭔가요?]
		//  - 톰캣 서버에서 보내는 응답(메시지)도 UTF-8로 보낸다는 뜻입니다.

		// ======================[CORS와 Preflight 요청에 대해]===========================
		// 비전공자 분들을 위해 자세히 설명 드립니다.
		//
		// 1. 웹사이트에서 서버와 데이터를 주고받을 때, 보안 때문에 "이 서버가 내 요청을 정말 받아줄까?"
		//    를 미리 브라우저가 점검하는 경우가 생깁니다.
		//
		// 2. 예를들어,
		//    - 다른 도메인(예: a.com → b.com)끼리 통신하거나
		//    - 특이한 헤더를 쓰거나,
		//    - POST나 PUT 등 민감한 동작을 할 때,
		//    브라우저는 "OPTIONS"라는 특별한 HTTP 방식으로
		//    "이런 요청 보내도 진짜 괜찮니?" 하고 톰캣 서버에 미리 물어봅니다.
		//
		// 3. 이걸 "프리플라이트(Preflight)" 또는 "사전점검" 요청이라고 부릅니다.
		//
		// 4. 서버 입장에선 이 요청에는 "응, 해도 돼!"(200번 코드)를 주면 되고
		//    실질적인 작업은 할 필요가 없습니다.
		//    만약 이 응답을 안 주면, 브라우저가 진짜 요청을 아예 보내지 않습니다.
		//
		// 5. 즉, 아래 코드는 "프리플라이트(OPTIONS 방식) 요청이 오면, OK 만 보내고 끝내라"는 뜻입니다.
		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			// [equalsIgnoreCase는?] 
			//  - 대소문자 구분 없이 문자열이 같은지 비교합니다. (예: OPTIONS, options, OpTiOns 전부 OK)
			response.setStatus(200); // HTTP 상태코드 200 = OK (정상 처리됨을 뜻함)
			return;                  // 이 함수(doHandle) 즉시 종료. 뒤쪽 실제 데이터 처리는 건너뜀
		}

		// ------------- 클라이언트가 요청한 주소의 뒷부분을 가져옴 -------------
		// 예: /Chatbot/send.do로 요청이 오면, 여기엔 "/send.do" 만 들어옵니다.
		String action = request.getPathInfo();   
		System.out.println("[ChatbotController] 요청 주소: " + action);

		// ────────────── 첫번째 분기: 일반 상담 챗봇 ──────────────
		// "/send.do" 주소로 요청이 왔을 때, 일반 챗봇 상담을 처리합니다.
		if ("/send.do".equals(action)) {
			// [참고: 문자열 비교는 ==가 아니라 .equals()를 써야 합니다!]
			//  - ==는 "메모리에서 똑같은(동일한) 객체인지"만 보고,
			//  - .equals()는 "내용이 똑같은지" 체크합니다.

			String userMessage = request.getParameter("message");   
			// [설명] 사용자가 챗봇 입력창에 입력한 내용(메시지)를 가져옵니다.

			String historyJson = request.getParameter("history");   
			// [설명] 지금까지 나눈 대화이력(예: 이전 채팅내용). AI가 문맥을 이해할 수 있도록 보냅니다.

			if (userMessage == null || userMessage.trim().isEmpty()) {
				// [설명] 아무 내용도 입력하지 않았거나(빈 값), 공백만 입력하면(스페이스바 등) 걸러냅니다.
				PrintWriter out = response.getWriter();       
				// [PrintWriter는?] 
				//  - 응답에(내보내는 데이터에) 실제 글자를 쓸 때 사용하는 '붓' 입니다.

				out.write("{\"reply\":\"메시지를 입력해주세요.\"}");        
				// [이 코드는?]
				//  - JSON 형식으로 {"reply":"메시지를 입력해주세요."}를 반환.
				//  - 나중에 화면에서는 "reply" 키 값을 꺼내 사용자에게 보여줍니다.

				return; // 메시지가 없으니, 더 처리할 필요 없이 함수 종료.
			}

			// 실제로 AI에게 질문을 보내 답변을 받아오는 부분입니다.
			String reply = callOpenRouterAPI(userMessage, historyJson);     

			PrintWriter out = response.getWriter();

			// [jsonResponse 용도]
			//  - AI로부터 받은 답변을 JSON 형식으로 감쌉니다. (예: {"reply":"AI답변..."} )
			JSONObject jsonResponse = new JSONObject();         
			jsonResponse.put("reply", reply);                   
			// [reply는?]
			//  - 말 그대로 "답변". 키 값이 reply니, 화면 JS에서 쉽게 꺼내 쓸 수 있음
			out.write(jsonResponse.toJSONString());             
			// [toJSONString()은?]
			//  - JSON 객체를 문자(텍스트)로 바꿔서 보냄

		// ────────────── 두번째 분기: AI 특수 서비스(추천/비용/Git/여행) ──────────────
		// "/ai.do" 로 요청이 왔을 때, 선택한 서비스별로 AI를 호출합니다.
		} else if ("/ai.do".equals(action)) {

			String userMessage = request.getParameter("message");
			// [설명] 사용자가 서비스에 대해 입력한 메시지(예: "경주 여행 추천해줘" 등)
			
			String serviceType = request.getParameter("type");
			// [설명] 어떤 특수 서비스를 요청하는 건지 구분합니다. 
			//   예: "recommend"(추천), "cost"(비용), "git"(Git도우미), "travel"(여행추천) 등

			if (userMessage == null || userMessage.trim().isEmpty()) {
				PrintWriter out = response.getWriter();
				out.write("{\"reply\":\"요청 내용을 입력해주세요.\"}");
				return;
			}

			// [aiPrompt 생성 과정]
			//   1. 서비스 종류(serviceType)를 입력값으로 받음 (예: "recommend", "cost", "git", "travel" 등)
			//   2. buildAIServicePrompt 메서드를 호출하여, 서비스에 해당하는 프롬프트(= AI에게 '이런 식으로 답하라'는 설명서)를 생성
			//      - 내부적으로 서비스별로 요구하는 답변 형태, 문장 스타일, 포함해야 할 정보 등 안내문이 포함됨
			//   3. 반환된 aiPrompt는 AI API 호출 시 prompt로 전달됨
			String aiPrompt = buildAIServicePrompt(serviceType);               

			// [AI 답변(reply) 생성 과정]
			//   1. callOpenRouterAPIWithPrompt 메서드를 호출
			//   2. userMessage(사용자 입력), aiPrompt(서비스 프롬프트), serviceType(서비스 구분)을 인자로 전달
			//   3. 메서드는 AI API(OpenRouter 등)에 실제로 요청을 보냄 
			//      - prompt에는 aiPrompt와 userMessage를 조합해 입력
			//      - serviceType별로 세부 옵션이나 조정이 필요할 경우 내부에서 처리
			//   4. API로부터 받아온 AI 답변(문자열)이 reply 변수에 저장
			String reply = callOpenRouterAPIWithPrompt(userMessage, aiPrompt, serviceType);

			// [AI 답변을 JSON으로 포장 후 응답]
			//   1. response 객체에서 PrintWriter를 얻음 (브라우저에 글자 출력용)
			//   2. JSONObject 생성 후 "reply" 키에 AI 답변(reply)을 저장
			//   3. jsonResponse를 문자열(텍스트)로 변환한 후 out.write로 응답 본문에 기록
			PrintWriter out = response.getWriter();
			JSONObject jsonResponse = new JSONObject();
			jsonResponse.put("reply", reply);
			out.write(jsonResponse.toJSONString());

		// ────────────── 세번째 분기: 자연어 예약 비서 기능 ──────────────
		// "/reserveAI.do" 요청이면, 말을 텍스트로 예약하는 보조기능입니다.
		// 실제 처리가 복잡하여 따로 분리된 메소드(handleReserveAI)에서 실행합니다.
		} else if ("/reserveAI.do".equals(action)) {
			handleReserveAI(request, response);  // 아래 별도 함수 참고!
		}
	}

	//===========================================================================
	// handleReserveAI() — 자연어 예약 비서 (/Chatbot/reserveAI.do)
	//
	//   [기존 'AI 추천' 탭과 무엇이 다른가 — 이 파일에서 가장 중요한 설계]
	//     기존 : 사용자 문장 → AI 가 "말로만" 추천.
	//            AI 는 DB 를 볼 수 없으니 '없는 차'를 추천하거나 '엉뚱한 금액'을 말해도 막을 수 없다.
	//     지금 : AI 의 역할을 "문장에서 조건만 뽑기" 로 확 좁혔다.
	//            차량 선택과 금액 계산은 우리 서버가 DB 데이터로 직접 한다.
	//
	//   [처리 순서]
	//     1. 문장을 '규칙(정규식)'으로 먼저 해석한다
	//     2. 규칙이 못 잡은 애매한 부분만 AI 에게 JSON 으로 뽑아 달라고 한다
	//     3. AI 가 준 값을 서버가 검증·보정한다 (지난 날짜, 100명 같은 이상값 차단)
	//     4. 그 조건으로 DB 의 실제 차량을 고르고 금액을 계산한다
	//     5. 추천 차량 + 예약 이어가기 정보를 JSON 으로 응답한다
	//
	//   ★ 핵심 원칙 : "AI 의 출력은 믿을 수 없는 입력이다."
	//     금액·차량처럼 틀리면 안 되는 값은 반드시 서버가 원본(DB)에서 다시 만든다.
	//===========================================================================
	@SuppressWarnings("unchecked")
	private void handleReserveAI(HttpServletRequest request, HttpServletResponse response) throws IOException {

		PrintWriter out = response.getWriter();
		String userMessage = request.getParameter("message");   // 예: "다음 주 금요일부터 2박 3일, 5명이 탈 차"

		if (userMessage == null || userMessage.trim().isEmpty()) {
			out.write("{\"reply\":\"원하시는 일정을 문장으로 알려주세요. 예) 다음 주 금요일부터 2박 3일, 5명이 탈 차\"}");
			// ↑ 단순히 "잘못됐다"가 아니라 '어떻게 말하면 되는지 예시'까지 준다. 좋은 에러 메시지의 조건이다.
			return;
		}

		// [입력값 길이 제한 - 방어 코드]
		// 사용자가 의도적으로 또는 실수로 아주 긴 문장(예: 10만 글자)을 입력할 경우,
		// 아래와 같은 문제가 발생할 수 있다:
		//   1. AI API(오픈라우터 등) 사용 시 글자 수가 많을수록 비용이 증가한다. (과금 폭탄 위험)
		//   2. 너무 긴 문장은 실제로 질문/예약 의도가 아닐 확률이 높다. (일반적인 예약 문의는 200자를 넘지 않음)
		//   3. 서버 또는 AI 응답 속도가 느려질 수 있다. (불필요한 리소스 소모)
		// 이에 따라 입력받은 메시지가 200자를 초과할 경우, 처음 200자까지만 사용하도록 자른다.
		// substring(0, 200)은 문자열의 0번째(첫 글자)부터 199번째까지 총 200글자만 남기는 역할을 한다.
		// 만약 200자 이내라면 아무 변화 없이 그대로 사용한다.
		if (userMessage.length() > 200) {
			userMessage = userMessage.substring(0, 200);
			// 예시: "이용자 입력이 너무 길 경우(예: 240자) → 앞에서부터 200자만 남기고 나머지는 버림"
		}

		// 오늘 날짜 정보를 가져오는 코드입니다.
		// 예약 요청에서 '내일', '모레', '다음 주 금요일'처럼 상대적인 날짜 표현이 자주 등장할 수 있으므로
		// 이를 정확한 날짜(예: 2024-06-14)로 변환하려면 기준이 되는 현재 날짜가 필요합니다.
		// LocalDate.now()는 시스템이 인식하는 '오늘'의 년-월-일 값을 반환합니다. 
		// 이 값을 기반으로 사용자가 입력한 각종 자연어 날짜 표현을 실제 날짜로 계산할 수 있습니다.
		// 예를 들어, 사용자가 "내일부터 3일간"이라고 입력하면,
		// today를 기준으로 1일을 더한 날짜가 예약 시작일이 됩니다.
		java.time.LocalDate today = java.time.LocalDate.now();


		// ================================
		// 1단계. 규칙 해석기로 먼저 해석 시도
		// ================================
		// 사용자의 입력 문장을 해석하여 예약에 필요한 정보(날짜, 일수, 인원, 차량 등급, 예산 등)를 자동 추출한다.
		// - 이 과정은 ReserveTextParser.parse(...) 코드를 통해 실행된다.
		// - 예: "내일부터 3일간 4명" 입력 시,
		//       - userMessage : 사용자가 입력한 자연어 메시지
		//       - today : 기준이 되는 "오늘 날짜"
		//       결과적으로 시작일, 몇일, 인원 등 값을 객체에 담아 반환한다.
		// - 내부적으로는 정규표현식 등을 사용하여 문장 내에서 의미 있는 정보를 뽑아냄
		ReserveTextParser.Result cond = ReserveTextParser.parse(userMessage, today); // 사용자 입력과 오늘 날짜를 전달하면 예약 조건 객체가 반환된다

		String source = "규칙"; // 어떤 방식(규칙/AI)으로 추출했는지 구분용

		// ================================
		// 2단계. 규칙 기반 해석(ReserveTextParser)에 자신 없으면 AI 모델을 사용하여 부족한 정보 보완
		// ================================
		// [의미]
		// - ReserveTextParser.parse()의 결과 cond.isConfident()가 false라면,
		//   (즉, 규칙 해석기로 날짜·일수·인원 등 예약 필수 정보를 전부 혹은 일부 파악하지 못했을 때)
		// - 이 상황에서 AI(예: GPT 등)를 추가로 활용하여 자연어에서 정보를 추출함으로써 누락된 필드를 채움
		if (!cond.isConfident()) { 
			
			// (1) AI에게 동일한 사용자 메시지를 전달하여, 부족하거나 누락된 예약 정보를 추출하도록 요청
			//     - callOpenRouterAPIWithPrompt는 OpenRouter(외부 AI 서비스)에 필요한 프롬프트와 종류("reserve")를 함께 넘김
			//     - buildReserveExtractPrompt()는 AI가 예약에 필요한 필드(시작일, 일수, 인원 등)를 뽑게 만드는 예시·규칙·설명 등을 포함한 프롬프트 문자열 생성
			String extracted = callOpenRouterAPIWithPrompt(userMessage, buildReserveExtractPrompt(), "reserve"); 
			// 예시: {"begindate":"2024-06-18","days":3,"people":4,"category":"중형","maxprice":150000}

			// (2) AI의 응답(일반적으로 JSON 형태 문자열)을 ReserveTextParser.Result 형식의 객체로 변환
			//     - parseAiJson은 AI가 돌려준 JSON 문자열에서 각종 값을 추출하고, Result 객체의 각 필드에 할당
			ReserveTextParser.Result aiCond = parseAiJson(extracted); 
			// 예시: aiCond.begindate=2024-06-18, aiCond.days=3, aiCond.people=4, aiCond.category="중형", aiCond.maxprice=150000

			if (aiCond != null) {
				// (3) 규칙 기반 해석기(cond)로 이미 추출(파악)된 정보는 그대로 두고,
				//    해당 필드가 비어 있는 경우(즉, 해석에 실패하여 값이 없음: null, 0, 음수 등)만 AI 결과(aiCond)로 보충합니다.
				//    예시값은 주석으로 남기지 않습니다. 각 조건마다 '어떤 상황'에서 AI 값을 반영하는지 최대한 명확히 설명함.
				
				// [시작일] - 규칙 해석 결과 cond.begindate가 null(=시작일을 못 뽑음)이면 AI 결과의 시작일로 채움
				if (cond.begindate == null)
					cond.begindate = aiCond.begindate;
				
				// [대여 일수] - 규칙 해석 결과 cond.days가 0 이하(정상적 일수가 아님)이면 AI 결과의 일수로 대체
				if (cond.days <= 0)
					cond.days = aiCond.days;

				// [인원] - 규칙 해석 결과 cond.people이 0 이하(인원 정보 해석 실패)면 AI 정보로 보충
				if (cond.people <= 0)
					cond.people = aiCond.people;

				// [차량 등급] - 규칙 해석 결과 cond.category가 null(등급 미해석)이라면 AI가 추출한 등급으로 보충
				if (cond.category == null)
					cond.category = aiCond.category;

				// [예산] - 규칙 해석 결과 cond.maxprice가 0 이하(예산 미추출)라면 AI 결과의 예산으로 보충
				if (cond.maxprice <= 0)
					cond.maxprice = aiCond.maxprice;

				// (4) AI 보완이 일어난 경우, source 변수 값을 "규칙+AI"로 갱신해 어떤 방식으로 정보가 해석·추출됐는지 기록합니다.
				source = "규칙+AI";
			} else {
				// (5) AI 응답 파싱이 실패했거나, AI가 아무 정보도 추출하지 못했을 때
				//     - 시스템 에러 없이 기존 규칙 기반 해석 결과만 가지고 계속 진행 (예외 아닌 정상 흐름)
				System.out.println("[ChatbotController] AI 보강 실패 - 규칙 결과만 사용");
			}
		}

		// ============================================================
		// (1) [규칙 기반 해석 + AI 추출 결과]
		//     - 지금까지 규칙 기반 분석(cond) 및 AI 보조 추출(aiCond)로
		//       예약에 필요한 정보(시작일, 일수, 인원, 등급, 예산 등) 중 
		//       단 하나도 확보하지 못했다면, 사용자의 입력값이 불충분하다고 판단합니다.
		//     - hasAny() 메서드는 cond 객체(조건 추출 결과)에 
		//       '해석된 예약 정보가 하나라도 있는지'를 체크합니다.
		//       (예: 날짜만 알아내고 나머지는 몰라도 true, 전부 null/0이면 false)
		// ============================================================
		if (!cond.hasAny()) { // 해석된 정보가 1개도 없으면 (여기서 hasAny()는 조건 5개 모두 체크)
			
			// (2) [실패 응답 JSON 만들기]
			//     - JSONObject 클래스는 { "키": "값" } 형식의 JSON 객체를 쉽게 만들 수 있도록 지원합니다.
			//     - new JSONObject()로 빈 JSON 객체를 먼저 생성합니다.
			JSONObject fail = new JSONObject();
			
			// (3) [사용자에게 보내줄 메시지 작성]
			//     - 클라이언트(웹/앱)의 챗봇 말풍선에 표시될 안내문을 "reply"라는 키로 추가합니다.
			//     - 안내문은 날짜와 인원을 구체적으로 입력해 달라는 요청이며,
			//       아래 \n(개행) 뒤에는 구체적인 예시도 제시하여 사용자의 오해 가능성을 줄입니다.
			fail.put("reply", "일정을 이해하지 못했습니다. 날짜와 인원을 함께 알려주세요.\n예) 8월 15일부터 3일간, 어른 4명");
			
			// (4) [JSON을 문자열로 변환하여 응답으로 전송]
			//     - fail.toJSONString() 메서드는 JSONObject 를 
			//       실제 네트워크로 전송 가능한 JSON 문자열(예: {"reply":"..."})로 변환합니다.
			//     - out.write(...)는 이 응답 메시지를 클라이언트(브라우저/앱)로 전송
			out.write(fail.toJSONString());

			// (5) [로직 조기종료]
			//     - 안내메시지를 이미 보냈으므로, 아래 예약 처리(차량 추천 등)는 더 진행하지 않고 여기서 즉시 종료(return)
			return; 
		}

		// (개발자 디버깅용 로그) 어떤 방식+결과로 해석됐는지 콘솔에 출력
		System.out.println("[ChatbotController] 예약 조건 추출(" + source + ") : " + cond);

		// ================================
		// [ 1단계: 규칙/AI 해석값을 실제 처리에 사용할 변수에 복사 (실제 값으로 할당) ]
		// ================================
		// - cond (ReservationCondition 등) 객체에서 추출된 예약 정보(시작일, 기간, 인원, 예산, 차량등급)를
		//   실질적인 예약/추천 처리에서 사용할 개별 변수(begin, days, people, maxPrice, category)로 복사합니다.
		// - 이 작업을 하는 이유는, 이후 로직에서 원본(cond) 객체를 직접 변경하지 않고,
		//   변수 자체를 수정해가며 보정 처리(이상치/기본값 등)를 독립적으로 하기 위함입니다.
		// - 또한 각 변수에 값을 어떻게 대입하는지 노출하여 추적 쉽도록 합니다.

		java.time.LocalDate begin = cond.begindate;  // [대여 시작일] cond에서 추출 → begin에 복사. ※ 날짜는 LocalDate 타입 사용 (년월일만 사용, 시분초 제외)
		int days   = cond.days;                      // [대여 일수]   cond의 days 값을 days 변수에 복사
		int people = cond.people;                    // [탑승 인원]   cond의 people 값을 people 변수에 복사
		// [예산 처리]
		// - 예산(maxprice)는 0 이상만 인정
		// - 만약 음수/미입력(-1 등)인 경우 0으로 강제 보정하여 이후 로직 혼란 방지
		int maxPrice = Math.max(cond.maxprice, 0);   // cond의 maxprice가 0보다 작으면 0, 크면 본값 → maxPrice 변수에 최종 할당
		String category = cond.category;             // [차량 등급] cond에서 category 추출, category 변수에 복사

		// ================================
		// [ 2단계: 값 누락/이상치에 대한 1차 보정 (기본값 지정) ]
		// ================================
		// - 사용자가 날짜, 기간, 인원, 예산 등 일부를 생략/이상 입력한 경우
		//   예약 처리 중단이 아닌 "합리적 기본값"으로 채워줍니다.
		// - 이 처리는 사용자 실수 대부분을 자동으로 보완함
		// - (예: 시작일=없음 → 내일, 기간=0 → 1일, 인원=0 → 1명)
		// - 단, 이런 보정이 있다는 사실을 아래 안내/로그에도 남깁니다.
		
		if (begin == null)  // 대여 시작일이 아예 추출되지 않은 경우(null)
			begin = today.plusDays(1);     // 오늘+1 = 내일로 자동 지정 (예약은 당일 시작이 어려움)
		if (days <= 0)      // 기간이 0 이하(미입력/음수 등)라면
			days = 1;                        // 1일로 자동 지정 (최소 1일 기준)
		if (people <= 0)    // 인원이 0 이하(미입력/음수 등)라면
			people = 1;                      // 1명으로 자동 지정 (최소 1명 기준)

		// ================================
		// 2-1. 범위를 벗어난 값에 대한 서버 보정 로직 (데이터 유효성 자동 확보)
		// ================================
		// - 이 부분은 사용자가 실제 예약 시 비현실적이거나 불가능한 값을 입력했을 때
		//   서버가 '사실상 가능한 값'으로 자동으로 바꾸고, 사용자에게 안내 메시지를 notes에 적립하는 역할이다.
		// - 이렇게 서버에서 값을 보정함으로써, 오류나 혼선을 줄이고 반복 문의를 방지할 수 있다.
		// - 코드에 각 주석마다 "왜" 그런 값을 지정하는지, "어떻게" 만들어지는지 구체적으로 해설함

		// 안내 메시지 적립용 목록을 준비한다
		//   - 조정된 사항이 있다면 여기에 메시지가 추가된다 
		//   - 마지막 응답에 이 값(리스트)을 함께 담아, 사용자가 알 수 있게 된다
		java.util.List<String> notes = new java.util.ArrayList<String>();

		// (1) [시작일 범위 자동 보정]
		//   - begin(대여시작일)이 오늘보다 이전이면(이미 지난날짜) 예약 불가이므로 내일로 재설정
		//     (예약 시스템 실무상 당일/지난 날짜는 불가 처리)
		//   - 1년보다 먼 미래일 때 역시 예약 비허용, 내일로 재설정
		//   - 자동 조정이 발생하면 안내 메시지도 함께 적립된다
		if (begin.isBefore(today)) {
			// ① 만약 시작일이 오늘보다 이전이라면 (이미 지난 날짜)
			begin = today.plusDays(1); // 내일로 강제 설정
			// 안내문 메모: why / how
			// - "지난 날짜는 예약할 수 없다"는 구체적 제한을 안내
			notes.add("지난 날짜는 예약할 수 없어 내일로 맞췄습니다.");
		} else if (begin.isAfter(today.plusYears(1))) {
			// ② 시작일이 오늘로부터 1년 이후라면
			begin = today.plusDays(1); // 내일로 강제 설정
			// 현실적으로 예약할 수 있는 최대치(1년) 초과에 대한 안내 제공
			notes.add("1년 이후는 예약할 수 없어 내일로 맞췄습니다.");
		}

		// (2) [대여 일수 제한 및 보정]
		//   - 일수가 30일을 초과하면 최댓값(30일)로 자동 보정
		//   - 그 외 1 미만(0, 음수 등)은 1일로(이 경우 별도 안내는 하지 않음)
		if (days > 30) {
			// ① days가 30일 초과일 경우
			days = 30; // 최대 30일로 제한
			// "30일까지"라는 명확한 서비스 정책 안내
			notes.add("최대 30일까지 예약할 수 있어 30일로 맞췄습니다.");
		} else if (days < 1) {
			// ② days가 1 미만(0, 음수 등)일 경우 
			days = 1;  // 최소 1일로 보정 (이때는 안내 없음, 너무 일반적이기 때문)
		}

		// (3) [탑승 인원 제한 및 보정]
		//   - 한 번에 예약 가능한 최대 인원은 24명까지로 제한
		//   - 초과 입력 시 24명으로 보정하고 별도 안내 추가(특별상담 전화번호 안내까지)
		//   - 인원이 1 미만이면 1로 보정(별도 안내는 하지 않음)
		if (people > 24) {
			// ① 입력 인원이 24명 초과일 경우
			people = 24; // 24명으로 제한
			// 안내문에 정책+추가 문의 경로를 명확히 안내
			notes.add("한 번에 최대 24명까지 조회할 수 있습니다. 더 필요하면 02-3456-6574 로 문의해주세요.");
		} else if (people < 1) {
			people = 1; // 인원이 1 미만이면 최소 1명으로 보정 (안내 없음)
		}

		// ----- 3단계. DB의 실제 차량(DB 내 존재하는 모든 차량 데이터)에서 추천 후보 차량을 고르는 코드 -----
		
		// carService.getAllCars() 호출:
		//   - DB에서 등록된 모든 차량(CarListVo 객체)의 리스트를 가져온다.
		//   - 리스트에는 각 차량의 상세정보(차종, 등급, 가격, 정원 등)가 담겨 있다.
		java.util.List<CarListVo> all = carService.getAllCars();

		/*
		 [Object[] 배열 설명]
		 - candidates 리스트에는 조건을 충족하는 추천 후보들을 담는다.
		 - 각각의 후보는 Object[] 배열 하나로 표현:
		     [0] : car – 차량 객체(CarListVo)
		     [1] : qty – 해당 차량으로 필요한 대수(몇 대가 필요?)
		     [2] : total – 해당 차량 × 필요대수 × 대여일수로 계산한 총 대여비(기본 요금)
		 - 실전에서는 별도의 DTO/VO 클래스를 쓰는 게 더 좋지만, 여기서는 설명 목적상 Object[] 사용.
		 */
		java.util.List<Object[]> candidates = new java.util.ArrayList<Object[]>();   // 조건을 만족한 후보 차량 리스트

		// 모든 차량 데이터(all 리스트) 반복: 한 대 한 대 조건에 맞나 검사
		for (CarListVo car : all) {

			// (1) 등급 필터 : category(예: "중형", "SUV" 등)를 사용자가 지정한 경우만 체크
			//     - 등급이 지정됐고, 현재 차량의 등급과 다르면 skip(continue) → 다음 차량으로 넘어감
			if (category != null && !category.equals(car.getCarcategory())) continue;

			// (2) 가격(예산) 필터 : 사용자가 최대 예산(maxPrice) 입력한 경우만 체크
			//     - 차량 1일 가격이 예산보다 비싸면 해당 차량은 추천 후보에서 제외 (continue)
			if (maxPrice > 0 && car.getCarprice() > maxPrice) continue;

			// (3) 차량 정원 데이터 유효성 검사 :
			//     - car.getCarusepeople()이 0 이하(이상 데이터/DB 오류 등)는 수용 불가 → 건너뜀
			//     - 0으로 나누기 방지 (예외/오류 방지)
			if (car.getCarusepeople() <= 0) continue;

			/*
			 [필요 대수 산출 공식 – 올림 나눗셈]
			 - 예: 5명 / 4인승 차 → 실수로는 1.25대지만 차는 2대 필요
			       (자바에서 올림 나눗셈은 (분자+분모-1)/분모 꼼수로 구현)
			 - (people + car.getCarusepeople() - 1) / car.getCarusepeople()
			       예: (5+4-1)/4 = 8/4 = 2
			 */
			int qty = (people + car.getCarusepeople() - 1) / car.getCarusepeople();

			// (4) 필요 대수가 5대를 초과하면 제외 (프론트에서 최대 5대까지만 처리 가능→실무 UI/정책 고려)
			if (qty > 5) continue;

			/*
			 [총 금액 계산]
			 - 차량 1대 가격 × 필요 대수(qty) × 대여일수(days)
			 - 옵션/보험 등은 제외한 '순수 기본 총액'으로 산출
			 */
			int total = car.getCarprice() * qty * days;

			// Object[] 배열에 [차량, 대수, 총액]을 묶어서 candidates 리스트에 추가
			candidates.add(new Object[]{ car, qty, total });
		}

		/*
			[조건 완화 로직]
			- 차량 추천 후보(candidates)가 비어있는 경우, 즉 조건이 너무 까다로워(등급, 예산 등) 하나도 추천 불가라면 조건을 완화해서 한 번 더 차량을 검색합니다.
			- 사용자가 '조건이 너무 빡빡해 추천이 0대'라고 하기보다는, 
			  '요구하신 조건은 아니지만, 이런 차량이라도 있습니다'라고 제안하는 것이 사용자 경험상 낫기 때문입니다.
			 
			- 아래 과정에서 어떻게 조건을 완화하는지, 어떤 방식으로 candidates를 다시 채우는지 단계별로 설명 주석을 추가합니다.
		*/
		boolean relaxed = false; // [relaxed] 조건을 완화 검색을 했는지 판별하는 용도 (추후 안내 문구 출력에 사용)
		
		// [조건 완화 진입 조건 설명]
		// - candidates가 비어 있어야 하고(즉, 한 대도 추천 못한 경우)
		// - 사용자가 등급(category) 혹은 예산(maxPrice) 조건을 명시적으로 입력한 경우임을 체크
		//   (이 조건들이 없으면 처음부터 완화된 것과 다름없으니 재탐색할 필요 없음)
		if (candidates.isEmpty() && (category != null || maxPrice > 0)) {
			relaxed = true;   // [표시] 조건을 풀었으니 나중에 사용자가 이를 인지할 수 있도록 flag를 true로
			
			// [전체 차량 목록 재탐색]
			// - 이제는 등급(category)과 예산(maxPrice) 필터를 모두 적용하지 않고, 단지 차량 인원과 한도(최대 5대 필요 제한)만 두고 탐색
			// - 기존 candidates를 채울 때 등급·예산·정원 등 추가 조건을 걸었지만, 여기서는 정원 데이터(0 초과)와 대수 제한만 남깁니다.
			for (CarListVo car : all) {
				// [정원 필터]
				// - 차량의 탑승 가능 인원이 0 이하인 경우 이상 데이터(잘못된 값)이므로 제외
				if (car.getCarusepeople() <= 0) continue;
				
				// [필요 대수 계산]
				// - 최소 몇 대가 필요한지도 재계산
				int qty = (people + car.getCarusepeople() - 1) / car.getCarusepeople();
				
				// [대수 제한]
				// - 추천 후 UI 및 정책상 5대를 넘기는 경우 제외
				if (qty > 5) continue;
				
				// [후보 등록]
				// - Object[] 구조에 맞게 차량, 필요 대수, 총 요금(1일요금×대수×기간) 순서로 배열 생성 후 candidates에 추가
				candidates.add(new Object[]{ car, qty, car.getCarprice() * qty * days });
			}
		}

		// [1] 차량 추천 후보(candidates)가 비어 있는지 확인합니다.
		//     - 후보 리스트가 비어 있다는 것은, 
		//        (a) 처음 조건으로 찾지 못함 → 
		//        (b) 조건을 완화(등급·예산 무시)해서도 한 대도 추천 불가인 경우입니다.
		if (candidates.isEmpty()) {   // candidates 리스트의 요소 개수가 0개인가?

			// [2] 안내 메시지를 JSON 형태로 만들어서 사용자에게 알려줍니다.
			//     - JSONObject none = new JSONObject(); 
			//       => 빈 JSON 객체를 생성합니다.
			JSONObject none = new JSONObject();

			//     - none.put("reply", "..."); 
			//       => reply(답변)라는 키에 안내 메세지를 저장합니다.
			//       안내 문구: "조건에 맞는 차량을 찾지 못했습니다. 인원을 나눠 여러 대로 예약하시려면 전화(02-3456-6574)로 문의해주세요."
			none.put("reply", "조건에 맞는 차량을 찾지 못했습니다. 인원을 나눠 여러 대로 예약하시려면 전화(02-3456-6574)로 문의해주세요.");

			// [3] out.write(none.toJSONString()); 
			//     => JSON 객체를 문자열로 변환 후, HTTP 응답으로 보냅니다.
			//        사용자의 화면(프론트엔드)로 전달됩니다.
			out.write(none.toJSONString());

			// [4] return; 
			//     => 이후 코드를 실행시키지 않고 메서드를 바로 종료합니다.
			return;
		}

		// [추천 차량 정렬 단계] 
		// 아래 코드는 '추천 후보(candidates) 리스트'를 정렬하는 과정입니다.
		// 
		// --- 작동 원리 설명 ---
		//  1. Java의 리스트(list)는 정렬할 때 .sort(비교자)를 사용할 수 있습니다.
		//     여기서 '비교자'는 두 데이터를 비교해서 순서를 결정해 주는 함수(람다식)입니다.
		//  2. (a, b) -> { ... } 형태의 람다식은
		//     - 리스트 안의 두 요소(a, b)를 받아서
		//     - a가 b보다 앞(=-1), 같음(=0), 뒤(=1) 중 어디에 위치하는 게 맞는지
		//       '정렬 기준'에 따라 값을 반환합니다.
		//     - 양수(>)이면 b가 앞으로, 음수(<)이면 a가 앞으로, 0이면 변화 없음.
		//  3. 각 후보는 Object[] 배열로 저장되어 있는데:
		//        [0]: 차량 정보 CarListVo
		//        [1]: 이 차량이 충족시키기 위해 필요한 최소 대수(qty, Integer)
		//        [2]: 전체 예상 요금(total, Integer)
		//  4. 따라서, 
		//      int qa = (Integer) a[1];   → 첫 번째 후보에서 '필요 대수'를 꺼냄
		//      int qb = (Integer) b[1];   → 두 번째 후보에서 '필요 대수'를 꺼냄
		//  5. 먼저, 필요한 차량 대수(작을수록 좋음)를 비교합니다.  
		//      - 즉, qa와 qb가 다를 때는 더 작은 쪽(a 또는 b)이 앞으로 옴.
		//  6. 필요한 차량 대수가 같다면, 두 번째 기준: 총액(둘 중 싼 가격)을 비교합니다.
		//      - 즉, a와 b의 [2] 값(총 요금) 중 더 작은 금액이 앞으로 옴.
		// 
		// --- 코드 구현 ---
		candidates.sort((a, b) -> {
			// ① 각 후보에서 필요한 차량 대수(qty)를 추출
			int qa = (Integer) a[1];   // 원소 a에서 qty 가져오기
			int qb = (Integer) b[1];   // 원소 b에서 qty 가져오기

			// ② 필요한 차량 대수가 다를 경우: 적은 대수가 앞으로 오도록 정렬
			if (qa != qb) 
				return qa - qb;        // ex) qa=1, qb=3 → -2(qa가 먼저)

			// ③ 필요한 차량 대수가 같으면: 총 금액(total)이 적은 순서로 정렬
			return (Integer) a[2] - (Integer) b[2]; // ex) 더 싼 금액이 우선
		});

		// ----- 4단계. 응답 JSON 만들기 -----
		//   이 구역은 최종적으로 사용자(프론트엔드)에게 보여줄 안내 메시지와 추천 차량 리스트를 JSON 형식으로 조립한다.
		//   ★ 주의: 안내 문장은 AI가 아니라 서버(백엔드)가 직접 생성한다.
		//           서버에서 직접 계산하기 때문에 금액에 오류가 발생하지 않는다.
		
		// (1) 날짜 표시 형식 지정 (예: "7월 18일")
		java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("M월 d일");
		
		// (2) 안내 문장 생성
		StringBuilder reply = new StringBuilder(); // 안내 문장을 순차적으로 이어 붙이기 위한 StringBuilder 생성
		
		// (2-1) 예약 시작일 ~ 대여일수 ~ 인원수로 안내문 기본정보 조립
		//      예) 7월 18일부터 3일간, 4명 기준으로 골랐습니다.
		reply.append(begin.format(fmt)).append("부터 ").append(days).append("일간, ")
			 .append(people).append("명 기준으로 골랐습니다.");
		
		// (2-2) relaxed가 true면 "등급/예산에 맞는 차량이 없어 전체 차량에서 골랐다"는 알림 추가
		//       즉, 고객이 원한 조건(등급/예산)을 모두 충족하는 차가 없을 때 이 안내가 붙음
		if (relaxed) {
			reply.append("\n(말씀하신 등급/예산에 맞는 차가 없어 전체 차량에서 골랐습니다)");
		}
		
		// (2-3) 옵션 요금이 별도임도 안내 (왜냐하면 결제 금액과 혼동될 수 있으므로)
		reply.append("\n금액은 옵션 제외 기본 요금입니다.");
		
		// (2-4) 위에서 계산하면서 발생한 모든 참고사항(notes)도 빠짐없이 한 줄씩 안내
		//       예) ⚠ 선택하신 날짜엔 대형 차량이 매진입니다.
		for (String note : notes) {
			reply.append("\n⚠ ").append(note);
		}
		
		// (3) 추천 차량 목록을 위한 JSON 배열 생성
		JSONArray cars = new JSONArray();
		
		// (3-1) 최대 3대까지 추천 (후보가 더 적으면 있는 만큼만)
		int limit = Math.min(3, candidates.size());
		
		// (3-2) 후보 리스트(정렬된 상태)에서 최대 3개의 차량 정보 추출해 JSON 객체로 변환 후 배열에 추가
		for (int i = 0; i < limit; i++) {
			// candidates의 각 요소는 Object[]이고 구조는 [0]:CarListVo, [1]:필요 대수(qty, Integer), [2]:총금액(total, Integer)
			CarListVo car = (CarListVo) candidates.get(i)[0]; // Object를 CarListVo로 형변환 하여 사용
			int qty   = (Integer) candidates.get(i)[1];
			int total = (Integer) candidates.get(i)[2];
			
			// 각 차량의 주요 정보와 서버 계산 결과(필요 대수, 총액)를 JSON 객체로 조립
			JSONObject item = new JSONObject();
			item.put("carno",    car.getCarno());        // 차량번호: 예약 연동용
			item.put("carname",  car.getCarname());      // 차량명 표시용
			item.put("carimg",   car.getCarimg());       // 차량 이미지 파일명
			item.put("carprice", car.getCarprice());     // 1일 대여 요금(원)
			item.put("seats",    car.getCarusepeople()); // 탑승 가능 인원
			item.put("qty",      qty);                   // 해당 인원/날짜 조건에 필요한 대수
			item.put("total",    total);                 // 계산된 총금액 (옵션 제외)
			cars.add(item); // 후보 리스트에 추가
		}
		
		// (4) 최종 JSON 응답 객체(result)에 안내문, 차량리스트, 예약조건 정보 삽입
		JSONObject result = new JSONObject();
		result.put("reply", reply.toString());           // 안내 문장 (프론트엔드에서 그대로 안내할 문자열)
		result.put("cars", cars);                        // 추천 차량 리스트(JSON 배열)
		result.put("begindate", begin.toString());       // 예약 시작일(YYYY-MM-DD), 화면에 기본값으로 채움
		result.put("days", days);                        // 예약 일수, 화면에 기본값으로 채움
		
		// (5) 최종 JSON 문자열로 변환하여 클라이언트(프론트엔드)로 전송
		out.write(result.toJSONString());
	}

	/**
	 * buildReserveExtractPrompt()
	 *  — AI에게 "렌트카 예약 문장에서 조건만 뽑아라" 라고 강하게 지시하는 프롬프트(지시문)를 만들어서 반환하는 함수입니다.
	 *  
	 *  ★ 설계 의도: AI가 쓸데없이 친근한 인사("네, 알겠습니다~!") 같은 말을 덧붙이면
	 *    JSON만 파싱해야 하는 메인 코드가 에러가 나므로, 
	 *    오직 JSON 하나만 예쁘게 뽑게 지시합니다.
	 *  
	 *  — 코드가 만들어지는 과정 및 각 부분의 역할을 상세히 설명함
	 */
	private String buildReserveExtractPrompt() {

		// [1] 오늘 날짜(LocalDate)를 가져옵니다. (예: 2024-06-15)
		java.time.LocalDate today = java.time.LocalDate.now();

		// [2] 한글 요일 이름 배열을 만듭니다. 
		//     0:월, 1:화, 2:수, 3:목, 4:금, 5:토, 6:일
		String[] week = {"월","화","수","목","금","토","일"};   

		// [3] 오늘 날짜와 요일 정보를 조합해서 자연어 스타일로 만듭니다.
		//     - today.getDayOfWeek().getValue()는 월=1 ~ 일=7이므로
		//       요일 이름 배열 인덱스에 맞추려면 -1 해주어야 합니다.
		//     - todayText 예시: "2024-06-15 (토요일)"
		String todayText = today + " (" + week[today.getDayOfWeek().getValue() - 1] + "요일)";

		// [4] 아래 String은 프롬프트 전체 문장입니다.
		//     - "\n"은 줄바꿈을 의미
		//     - + 연산을 사용해 여러 줄에 걸쳐 구체적으로 프롬프트를 만듬
		//   주요 지침 요약:
		//     1. 너는 "렌트카 예약 조건 추출" 전용 분석기(=역할 고정)
		//     2. AI가 오늘 날짜를 스스로 모르므로 '오늘' 값을 직접 알려줌 (날짜 계산에 필요)
		//     3. 반드시 아래 JSON 형식 한 줄만 출력하라고 명령 (설명, 인사, 코드블록 기호 금지)
		//     4. 입력이 예약과 무관하면 { "error": "not_reservation" }만 딱 내놓으라고 강하게 제한
		//     5. 각 필드가 없을 때 기본 처리값(예: 언급 없으면 내일, 1명 등) 명시
		//     6. 카테고리는 한글을 영어형 코드("Small", "Mid", "Big")로 변환해서 내보내라고 직접 지시
		return 
			"너는 렌트카 예약 문장에서 조건만 뽑는 분석기다.\n"
			+ "오늘은 " + todayText + " 이다. '내일', '다음 주 금요일' 같은 상대 날짜는 오늘 기준으로 계산하라.\n\n"
			// ↑ [코드 설명] AI가 날짜 계산을 정확히 하게 하려고 todayText 값을 명확히 삽입합니다.
			+ "아래 형식의 JSON 한 개만 출력하라. 설명, 인사, 코드블록 기호를 절대 붙이지 마라.\n"
			+ "{\"begindate\":\"YYYY-MM-DD\",\"days\":숫자,\"people\":숫자,\"category\":null,\"maxprice\":null}\n\n"
			// ↑ [코드 설명] 백엔드에서 파싱 가능한 구조와 타입을 명확히 예시로 제시합니다.
			+ "규칙:\n"
			+ "- begindate : 대여 시작일. 날짜 언급이 없으면 내일 날짜.\n"
			+ "- days : 대여 일수. '2박 3일'은 3, '당일'은 1. 언급 없으면 1.\n"
			+ "- people : 탑승 인원. '가족 4명'은 4. 언급 없으면 1.\n"
			+ "- category : '소형/경차'->\"Small\", '중형/SUV'->\"Mid\", '대형/승합'->\"Big\". 언급 없으면 null.\n"
			+ "- maxprice : '하루 5만원 이하'처럼 1일 예산 상한이 있으면 숫자(원). 없으면 null.\n"
			+ "- 렌트카 예약과 무관한 문장이면 {\"error\":\"not_reservation\"} 만 출력하라.";
		// ↑ [코드 설명] 각 조건이 구체적으로 무엇을 의미하는지, 그리고 AI가 반드시 따를 지침 및 예외 상황을 명확히 적시
	}

	/**
	 * parseAiJson()
	 *  — AI가 반환한 JSON 형태의 문자열을 우리가 사용하는 규칙 해석기 결과(Result 객체)에 맞게 변환하는 함수.
	 *  — 만약 JSON 형식이 맞지 않거나, 필수 정보가 없다면 null을 반환한다.
	 *
	 *  [보안] AI의 응답은 완전히 신뢰할 수 없는 사용자 입력 취급!
	 *         - category(차량 유형)는 'Small', 'Mid', 'Big' 세 가지 값만 허용(화이트리스트 검증).
	 *         - 숫자(date, days, people, maxprice)는 변환 실패 시 무효처리(버림).
	 *         - 이런 식으로 반드시 안전망을 두는 것이 중요하다.
	 *  [코드 생성 설계(구조)]
	 *    1. AI가 돌려준 응답 문자에서 JSON 덩어리만 'cutJson' 함수로 잘라냄
	 *    2. JSONParser로 파싱(문자 → JSONObject로 구조화)
	 *    3. "error" 키가 있으면 대상 문장이 예약이 아님을 의미 — 무조건 null 반환 후 종료
	 *    4. ReserveTextParser.Result 빈 객체 생성(결과를 담을 공간)
	 *    5. 각 JSON 키를 파고드는 5단계 타입체크 및 변환 수행
	 *       - begindate: "YYYY-MM-DD"냐 정규식 확인 → java.time.LocalDate로 변환 시도(실패 시 무시)
	 *       - days, people, maxprice: toInt 함수 통해 숫자 변환(실패 시 -1)
	 *       - category: null 체크 후 허용값(Small/Mid/Big)만 통과
	 *    6. 변환된 내용을 r 객체에 담음(값 누락도 허용)
	 *    7. r에 한 개라도 값이 들어있으면 r 반환, 하나도 없으면 null 반환
	 *    8. 변환/파싱 과정 중 예외 발생하면 catch로 이동하여 오류 로그 후 null 반환
	 */
	private ReserveTextParser.Result parseAiJson(String aiText) {

		try {   
			// [1] 입력: AI가 준 문자열(aiText)을 받아옴
			// [2] 전처리: cutJson() 함수로 앞뒤 잡담, 코드블럭 등을 제거, 순수 JSON 텍스트만 남김
			JSONObject json = (JSONObject) new JSONParser().parse(cutJson(aiText));

			// [3] 예약문장 아님을 사전 필터: "error" 필드 존재 시(RAW 응답: {"error": "not_reservation"})
			if (json.get("error") != null) {   
				return null;
			}

			// [4] 결과 데이터를 담을 빈 객체 생성 (이후 각 값을 집어넣음)
			ReserveTextParser.Result r = new ReserveTextParser.Result();   

			// [5-1] 대여 시작일: "YYYY-MM-DD" 형식 여부 체크(정규표현식) 후 변환
			Object begin = json.get("begindate");
			if (begin != null && String.valueOf(begin).matches("\\d{4}-\\d{2}-\\d{2}")) {
				try {
					// 날짜 형식만 맞고 실제로 존재하지 않는 날짜(예: 2026-02-30)는 예외 발생 시 무시
					r.begindate = java.time.LocalDate.parse(String.valueOf(begin));
				} catch (Exception ignore) {
					// 비어두기
				}
			}

			// [5-2] 대여일수/인원수/가격: toInt()로 안전변환, 실패 시 사전에 정한 기본값(-1)
			r.days     = toInt(json.get("days"), -1);      
			r.people   = toInt(json.get("people"), -1);
			r.maxprice = toInt(json.get("maxprice"), -1);

			// [5-3] 카테고리(차량 타입): 허용된 3개 값(화이트리스트)만 인정, 외에는 모두 null
			Object cat = json.get("category");
			if (cat != null) {
				String c = cat.toString();
				// AI가 "중형" 같은 한글이나 엉뚱한 값을 줘도 위 세 가지 이외는 모두 무시
				if ("Small".equals(c) || "Mid".equals(c) || "Big".equals(c)) {
					r.category = c;
				}
			}

			// [6] 5가지 값 중 단 하나라도 값이 들어있을 때만 반환, 모두 비었으면 null 반환
			// hasAny() → 다 비었으면 false, 뭔가 채워졌으면 true
			return r.hasAny() ? r : null;   

		} catch (Exception e) {   
			// [예외 처리] 파싱/변환 중 오류 발생 시 경고 로깅 후 null 반환
			System.out.println("[ChatbotController] AI 조건 JSON 해석 실패 : " + e.getMessage());
			return null;   
		}
	}

	/**
	 * cutJson()
	 *  — AI 응답에서 실제 JSON 부분(중괄호 { ... })만 잘라냅니다.
	 *  — 예를 들어 AI가 "네! ```json { ... } ```"처럼 앞 뒤로 의미 없는 멘트, 코드블럭 등을 붙여도 견디도록 만듦.
	 * 
	 * [작동 과정 자세히 설명]
	 *   1. 입력 값이 null인지 확인한다.
	 *      - 만약 null이면, 의미 있는 JSON이 있을 수 없으므로 빈 문자열 ""을 바로 반환한다.
	 *   2. 여는 중괄호 '{'가 처음 나오는 인덱스를 찾는다.
	 *      - text.indexOf('{')를 사용.
	 *      - 이 위치(s)는 JSON의 시작점이 될 수 있다.
	 *   3. 닫는 중괄호 '}'가 마지막으로 나오는 인덱스를 찾는다.
	 *      - text.lastIndexOf('}')를 사용.
	 *      - 이 위치(e)는 JSON의 마지막 부분이 될 수 있다.
	 *   4. 여는 중괄호가 없거나(s < 0), 닫는 중괄호가 여는 괄호보다 앞에 나오면(e <= s),
	 *      - 정상적인 JSON 포맷일 가능성이 극히 낮으므로,
	 *      - 원본 text를 그대로 반환한다. (이후 파싱 시도에서 실패하도록 함)
	 *   5. 그 외의 경우, 여는 중괄호 ~ 닫는 중괄호(+1)까지를 잘라 반환한다.
	 *      - text.substring(s, e + 1)을 사용하여 substring(시작, 끝)포맷으로 오려냄
	 *      - substring의 두 번째 인자는 '미포함'이므로, 반드시 e+1로 해야 마지막 중괄호까지 포함됨
	 * 
	 * [방어 코드 의의]
	 *   - AI가 말을 하다가 앞/뒤에 설명을 붙이거나, 코드블럭마크 등을 넣더라도
	 *     실제 JSON 부분만 정확히 잘라낼 수 있도록 설계됨.
	 *   - 만약 중괄호가 아예 없다면 원본을 반환하여 파싱 에러로 이어지게 하고, 
	 *     중괄호 쌍이 비정상이어도 안전하게 실패하도록 함.
	 */
	private String cutJson(String text) {
		// (1) 입력이 null이면 JSON 추출 자체가 불가능하니 빈 문자열 반환
		if (text == null) {
			return "";
		}
		// (2) 여는 중괄호 위치(처음 나오는 위치) 탐색: JSON 본문의 시작점이 될 후보
		int s = text.indexOf('{');
		// (3) 닫는 중괄호 위치(마지막 나오는 위치) 탐색: JSON 본문의 끝점이 될 후보
		int e = text.lastIndexOf('}');
		// (4) 중괄호가 없거나, 짝이 잘못(순서가 반대 등)된 경우: 원본 반환 (뒤에서 파싱 에러 처리)
		if (s < 0 || e <= s) {
			return text;
		}
		// (5) 정상적으로 여는/닫는 중괄호를 찾은 경우,
		//     substring(s, e+1)로 시작~끝(끝포함)까지 잘라서 반환
		return text.substring(s, e + 1);
	}

	/**
	 * toInt()
	 *  — JSON 객체에서 숫자 값(Object)을 받아서 Java의 int 타입으로 변환하는 메소드입니다.
	 *  — 특히 json-simple 라이브러리로 JSON을 파싱할 때, 숫자가 대부분 Long 타입(예: {"age":5} → 5L)으로 반환되기 때문에
	 *    실제로 자바에서 많이 쓰는 int 타입으로 맞춰줘야 쓸 때 편리합니다.
	 *  — 또한, 값이 null이거나(숫자 없음), 숫자로 변환할 수 없는 이상한 값(예: "apple" 같은 글자)이 들어오면
	 *    미리 정해 둔 기본값(defaultValue)을 돌려주며, 프로그램이 중단되지 않도록 방어합니다.
	 *
	 * [코드가 만들어지는 과정]
	 *   (1) 먼저, value가 Number(숫자: Integer, Long, Double 등) 타입인지 확인합니다.
	 *       - 예: 5, 15L, 3.14 등이 모두 Number의 하위 클래스이므로 true가 됩니다.
	 *       - 만일 맞다면, Number 타입으로 변환(Number로 캐스팅)하여 long 값으로 읽습니다.
	 *         그 다음 (int)로 강제 변환(casting)해서 int 값으로 돌려줍니다.
	 *         ※ JSON에서 온 5L(Long) → (int) 5 로 안전하게 내려줍니다.
	 *   (2) value가 Number가 아니라면(보통은 String이거나, 엉뚱한 타입일 때),
	 *       - value를 문자열로 바꿉니다 (String.valueOf(value)).
	 *       - 혹시 앞뒤에 공백이 있으면(.trim()) 제거하고,
	 *       - Integer.parseInt()로 숫자로 변환합니다.
	 *         예: "7" → 7, " 42 " → 42
	 *   (3) 만약 (2) 과정에서 문자열이 숫자로 변환 불가(예: "apple")거나, value가 null이라서 예외가 발생하면,
	 *       - catch 블록이 동작하여, 사용자가 지정해둔 기본값(defaultValue)을 반환합니다.
	 *       - 즉, 예외가 발생해도 절대 프로그램 전체가 멈추지 않고 안전하게 처리됩니다.
	 */
	private int toInt(Object value, int defaultValue) {
		// (1) 값이 숫자(Number)이면 바로 int로 변환해서 반환
		if (value instanceof Number) {                  
			// Number 계열(예: Integer, Long, Double 등)로 캐스팅 후, 
			// longValue()로 안전하게 추출하고, 다시 (int)로 강제변환(cast)
			return (int) ((Number) value).longValue();
		}
		try {
			// (2) 숫자가 아니면 String 변환 → 공백 제거 → int로 파싱
			//    예: "6" → 6, " 77 " → 77
			return Integer.parseInt(String.valueOf(value).trim());   
		} catch (Exception e) {
			// (3) 숫자로 못바꾸면(예: null, "one" 등) 예외 발생 → 기본값 반환
			return defaultValue;                                     
		}
	}

	//===================================================================
	// callOpenRouterAPI() — AI 에게 질문을 보내고 답변을 받는 메소드 (일반 상담용)
	//   네트워크는 가끔 이유 없이 실패한다. 그래서 실패 시 1회 자동 재시도한다.
	//===================================================================
	/* ═══════════════════════════════════════════════════════════════════════════
	   callOpenRouterAPI() — AI 에게 질문을 보내고 답변을 받아오는 '총괄 매니저' 메소드
	   ═══════════════════════════════════════════════════════════════════════════

	   [ 이 메소드가 하는 일 — 딱 4단계 ]
	     1단계. 출입증(API 키)이 있는지 미리 확인한다
	     2단계. AI 에게 보낼 편지(JSON)를 만든다
	     3단계. 최대 2번까지 전송을 시도한다  ← 이 메소드의 핵심
	     4단계. 끝내 실패하면 사용자에게 '이해 가능한 말'로 알린다

	   [ 역할 분담이 어떻게 되어 있나 ]
	     callOpenRouterAPI()        ← 지금 이 메소드. '몇 번 시도할지' 를 결정하는 관리자
	       └ buildOpenRouterRequest()  : 보낼 내용을 만드는 담당
	       └ doApiCall()               : 실제로 인터넷에 나가는 담당 (실무자)

	     ★ 왜 굳이 나눴나?
	       "전송하는 일" 과 "실패하면 다시 시도하는 일" 은 성격이 다른 일이다.
	       한 메소드에 다 넣으면 100줄이 넘어가고, 나중에 재시도 횟수만 바꾸고 싶어도
	       통신 코드까지 같이 들여다봐야 한다. 이렇게 나누면 각자 한 가지 일만 한다.
	       (이것을 '단일 책임 원칙' 이라고 부른다)

	   [ 재시도를 왜 하나 — 비전공자를 위한 설명 ]
	     인터넷은 생각보다 자주 '이유 없이' 실패한다.
	     와이파이가 0.5초 끊기거나, AI 서버가 잠깐 바빴거나, 중간 통신사 장비가 헛기침을 한다.
	     이런 건 '다시 걸면 대부분 된다'. 전화가 안 걸릴 때 한 번 더 걸어보는 것과 같다.
	     사용자에게 "실패했습니다" 라고 말하기 전에, 우리가 조용히 한 번 더 시도해 주는 것이다.
	   ═══════════════════════════════════════════════════════════════════════════ */
	private String callOpenRouterAPI(String userMessage, String historyJson) {
	/*  ▲        ▲                    ▲                    ▲
	    │        │                    │                    └ 지금까지의 대화 기록 (AI 가 앞 대화를 기억하게 하려고)
	    │        │                    └ 사용자가 방금 입력한 메시지
	    │        └ 돌려주는 값의 종류 : String(글자). 즉 "AI 답변 글자를 돌려준다"
	    └ private = 이 클래스 안에서만 부를 수 있다. 바깥에서 함부로 못 부르게 잠근 것.
	       (public 으로 열어 두면 다른 곳에서 마음대로 AI 를 호출해 요금이 샐 수 있다) */

		/* ───────── 1단계. 출입증(API 키) 사전 확인 ─────────

		   [왜 미리 확인하나?]
		     키가 없는데 통신을 시도하면
		       ① 30초 기다렸다가 ② 401 에러를 받고 ③ 또 재시도하며 2초 더 기다린다.
		     결국 30초 넘게 기다린 끝에 "실패"를 보게 된다. 확인은 0.001초면 끝난다.
		     ★ 확실히 실패할 일은 시작하기 전에 막는다. 이런 걸 '빠른 실패(fail fast)' 라고 한다. */
		if (apiKey == null || "YOUR_API_KEY_HERE".equals(apiKey)) {
		/*  ▲          ▲       ▲
		    │          │       └ ★★ "글자".equals(변수) 순서에 주목! ★★
		    │          │          apiKey.equals("YOUR...") 로 쓰면 apiKey 가 null 일 때
		    │          │          NullPointerException 이 터져 프로그램이 죽는다.
		    │          │          "글자" 를 앞에 두면 절대 null 이 아니므로 안전하다.
		    │          │          (실무에서 쓰는 방어 기법. 'Yoda 조건문' 이라고도 부른다)
		    │          └ || = OR(또는). 둘 중 하나만 참이어도 실행된다.
		    │               자바는 앞이 참이면 뒤를 아예 확인하지 않는다(단축 평가).
		    │               그래서 null 검사를 반드시 '앞'에 둬야 한다.
		    └ 검사 대상 : 키가 아예 없거나(null), 예시 문구 그대로면(=아직 설정 안 함) */

			// [좋은 에러 메시지의 조건] "안 됩니다" 로 끝내지 않고 '어떻게 고치는지'까지 알려 준다.
			//   이 메시지를 보는 사람은 개발자(=상국쌤 또는 학생)이므로 해결 절차를 그대로 적는다.
			return "AI 챗봇 API 키가 설정되지 않았습니다.\n\n"   // \n = 줄바꿈 문자
				+ "[설정 방법]\n"
				+ "1. https://openrouter.ai 접속\n"
				+ "2. Google 계정으로 로그인\n"
				+ "3. Keys 메뉴 -> Create Key\n"
				+ "4. 키 입력 후 서버 재시작";
			// return 을 만나는 순간 이 메소드는 즉시 끝난다. 아래 코드는 한 줄도 실행되지 않는다.
		}

		/* ───────── 2단계. AI 에게 보낼 편지(JSON) 만들기 ───────── */

		JSONObject requestBody = buildOpenRouterRequest(userMessage, historyJson);
		// ↑ 다른 메소드에게 "편지 좀 써 줘" 하고 시킨다.
		//   결과물 = { "model":"...", "messages":[...], "temperature":0.7, ... } 형태의 JSON '객체'

		String jsonStr = requestBody.toJSONString();
		// ↑ JSON '객체'(자바가 다루는 형태) → JSON '글자'(인터넷으로 보낼 수 있는 형태) 로 변환.
		//   비유 : 머릿속 생각(객체)을 종이에 적은 편지(글자)로 옮기는 것.
		//   네트워크로는 '글자/바이트' 만 보낼 수 있기 때문에 이 변환이 반드시 필요하다.

		// ★ 여기서 딱 한 번만 변환한다는 점이 중요하다.
		//   재시도할 때마다 다시 만들면 같은 일을 두 번 하는 낭비다. 만들어 둔 글자를 재사용한다.

		System.out.println("[ChatbotController] 요청 JSON 길이: " + jsonStr.length());
		/*  ▲                                                        ▲
		    │                                                        └ length() = 글자 수
		    └ Eclipse 아래쪽 Console 창에만 찍히는 개발자용 기록. 사용자 화면에는 안 보인다.

		   [왜 '길이'를 찍나?]
		     AI 모델은 한 번에 받을 수 있는 글자 수에 한도가 있다(입력 토큰 한도).
		     대화가 길어지거나 차량 목록이 늘어나면 이 숫자가 커진다.
		     원인 모를 실패가 날 때 이 숫자가 갑자기 커져 있으면 '한도 초과'를 의심하면 된다.
		   ★ 절대 내용(jsonStr) 자체를 통째로 찍지 않는다. 사용자 대화 내용이 로그에 남으면
		     개인정보 문제가 된다. '길이' 같은 숫자만 남기는 것이 안전한 로깅이다. */

		/* ───────── 3단계. 최대 2번까지 전송 시도 (이 메소드의 핵심) ───────── */

		int maxRetries = 2;
		/* ★ 이름이 조금 헷갈린다. retry(재시도)라는 이름이지만 실제로는 '총 시도 횟수' 다.
		     값이 2 = 첫 시도 1번 + 재시도 1번 = 합계 2번.
		     (더 정확한 이름은 maxAttempts 였을 것이다. 나중에 바꾼다면 이 이름을 추천)

		   [왜 하필 2번인가? — 무한 재시도는 절대 금물]
		     사용자는 화면 앞에서 로딩 동그라미를 보며 기다리는 중이다.
		     최악의 경우 이미 이만큼 기다렸다 :
		        1차 읽기 대기 60초 + 쉬는 시간 2초 + 2차 읽기 대기 60초 = 약 122초
		     3번으로 늘리면 3분을 넘긴다. 그 전에 사용자는 창을 닫는다.
		     "될 만한 건 건지되, 사용자를 붙잡아 두지는 않는다" 의 타협점이 2번이다. */

		Exception lastException = null;
		/* ↑ '마지막 실패 원인'을 담아 둘 변수.

		   [왜 따로 보관하나?]
		     for 반복문 안에서 catch 로 잡은 e 는 그 블록을 벗어나면 사라진다.
		     그런데 우리는 '2번 다 실패한 뒤'에 그 원인을 봐야 한다.
		       - 시간 초과였다면  → "잠시 후 다시" (기다리면 될 수도 있다)
		       - 네트워크였다면   → "전화 상담" 안내 (기다려도 안 될 수 있다)
		     원인에 따라 안내 문구가 달라져야 하므로 바깥 변수에 챙겨 둔다.
		   null 로 시작하는 이유 : 아직 아무 실패도 없었다는 뜻. */

		for (int attempt = 1; attempt <= maxRetries; attempt++) {
		/*  ▲        ▲              ▲                   ▲
		    │        │              │                   └ 한 바퀴 돌 때마다 1씩 증가
		    │        │              └ 2 이하인 동안 반복 → attempt 는 1, 2 두 번 실행
		    │        └ 1부터 시작. 0부터 시작하면 로그에 "시도 0/2" 로 찍혀 사람이 헷갈린다.
		    └ 반복문 안에서만 쓰는 임시 변수 */

			try {
			/*  ↑ try = "여기서 사고가 날 수 있다" 고 표시하는 구역.
			      사고(예외)가 나면 그 즉시 남은 줄을 건너뛰고 아래 catch 로 점프한다.
			      비유 : 안전그물. 떨어져도 바닥에 부딪히지 않고 그물(catch)에 걸린다. */

				String result = doApiCall(jsonStr);   // ★ 실제로 인터넷 너머 AI 서버와 통신하는 지점
				return result;
				/* ↑ 성공! return 을 만나면 for 반복문도 메소드도 즉시 끝난다.
				     → 1차에 성공하면 2차는 아예 실행되지 않는다. (당연하지만 중요한 동작) */

			} catch (java.net.SocketTimeoutException e) {
			/*  ▲
			    └ [사고 유형 ①] 시간 초과 — "전화는 걸렸는데 상대가 안 받는다"
			       doApiCall 에 setReadTimeout(60000) 이 걸려 있다.
			       60초 안에 AI 가 답을 안 주면 자바가 이 예외를 던진다.

			   ★★ catch 는 '적어 놓은 순서대로' 검사한다. 이 순서가 매우 중요하다! ★★
			     SocketTimeoutException 은 IOException 의 '자식'이다.
			     (가족 관계 : IOException  ←부모
			                   └ SocketTimeoutException  ←자식)
			     만약 IOException 을 위에 적으면, 부모가 자식까지 전부 잡아 버려서
			     아래 SocketTimeoutException 블록은 영원히 실행되지 않는다.
			     ★ 규칙 : 자식(구체적인 것)을 위에, 부모(포괄적인 것)를 아래에. */

				lastException = e;   // 원인을 바깥 변수에 챙겨 둔다 (2번 다 실패했을 때 쓰려고)

				System.out.println("[ChatbotController] 타임아웃 (시도 " + attempt + "/" + maxRetries + "): " + e.getMessage());
				/* ↑ "시도 1/2" 처럼 몇 번째였는지 함께 남긴다.
				     1차에서만 나고 2차에 성공했다면 그건 정상 동작이다.
				     2차까지 찍혔다면 진짜 문제다. 이 구분이 로그에 남아야 원인을 찾을 수 있다.
				   e.getMessage() = 자바가 알려 주는 실패 이유 한 줄 */

				if (attempt < maxRetries) {
				/*  ↑ ★ 이 if 가 없으면?
				      마지막(2차) 시도가 실패한 뒤에도 2초를 더 자고 나서 끝난다.
				      어차피 다시 시도하지도 않을 건데 사용자만 2초 더 기다리는 순수한 낭비다.
				      "다음 시도가 남아 있을 때만 쉰다" 는 뜻. */

					// 2초 쉬었다가 재시도. 바로 다시 걸면 아직 문제가 안 풀려서 또 실패할 확률이 높다.
					// (실무에서는 1초→2초→4초 식으로 점점 늘리는 '지수 백오프' 를 쓰기도 한다)
					try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
					/*  ▲                        ▲                                 ▲
					    │                        │                                 └ 인터럽트 상태를 되살려 준다.
					    │                        │                                    이걸 빼면 "누가 이 작업을 중단시켰다"는
					    │                        │                                    신호가 사라져 톰캣이 서버를 제대로 종료하지
					    │                        │                                    못할 수 있다. 관용적으로 항상 이렇게 쓴다.
					    │                        └ 자는 동안 누가 깨우면 던져지는 예외. sleep 은 이 처리를 강제한다.
					    └ Thread.sleep(2000) = 2000밀리초(=2초) 동안 멈춘다 */
				}

			} catch (java.io.IOException e) {
			/*  ▲
			    └ [사고 유형 ②] 입출력 오류 — "전화 자체가 안 걸린다"
			       와이파이 끊김, DNS 조회 실패, 서버가 연결을 거부 등.
			       doApiCall 안에서 404(모델 일시 불가) / 500번대(서버 오류)도
			       일부러 IOException 으로 바꿔 던지고 있다.
			       → 그래야 '재시도해 볼 가치가 있는 실패' 로 여기에 걸리기 때문이다.
			       ★ 예외의 '종류'로 재시도 여부를 구분하는 설계다. 눈여겨볼 만하다. */

				lastException = e;
				System.out.println("[ChatbotController] 네트워크 오류 (시도 " + attempt + "/" + maxRetries + "): " + e.getMessage());
				if (attempt < maxRetries) {
					try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
				}

			} catch (Exception e) {
			/*  ▲
			    └ [사고 유형 ③] 그 밖의 모든 오류 — 마지막 안전망
			       Exception 은 거의 모든 예외의 조상이다. 위에서 못 잡은 건 전부 여기로 온다.
			       예: 응답 JSON 형식이 깨져서 생긴 파싱 오류, 코드 버그로 인한 NullPointerException 등

			    ★ 여기서는 재시도하지 않고 '즉시 포기' 한다. 왜?
			      이런 오류는 우리 쪽 코드나 응답 형식의 문제다.
			      똑같이 다시 보내면 똑같이 실패한다.
			      될 리 없는 걸 알면서 사용자를 2초 더 기다리게 할 이유가 없다.

			    ★ 이 한 줄의 교훈 :
			      "재시도는 '일시적인 실패'에만 의미가 있다.
			       '구조적인 실패'에는 재시도가 시간 낭비일 뿐이다." */

				System.out.println("[ChatbotController] API 호출 오류: " + e.getMessage());

				e.printStackTrace();
				/* ↑ 예외가 '어느 파일 몇 번째 줄'에서 시작됐는지 전체 경로를 콘솔에 쏟아낸다.
				     예상 못 한 오류는 위치를 모르면 고칠 수가 없으므로 여기서만 사용한다.
				     (위의 타임아웃·네트워크 오류는 원인이 뻔하므로 한 줄 로그로 충분하다) */

				// [사용자에게 보여줄 말] 기술 용어는 한 글자도 쓰지 않는다.
				//   "NullPointerException" 을 보여 주면 사용자는 겁만 먹고 할 수 있는 게 없다.
				//   대신 '지금 당장 할 수 있는 행동'(전화 걸기)을 준다.
				return "죄송합니다. 상담 중 오류가 발생했습니다.\n전화 상담: 02-3456-6574";
			}
		}
		// ↑ for 반복문 끝.

		/* ───────── 4단계. 여기까지 왔다 = 2번 모두 실패했다는 뜻 ─────────

		   [잠깐, 왜 여기 도달할 수 있지?]
		     - 성공했다면 → try 안의 return 으로 이미 나갔다
		     - 사고유형 ③ 이었다면 → catch 안의 return 으로 이미 나갔다
		     ★ 즉 이 자리에 도달하는 경우는 오직 하나 :
		       '타임아웃 또는 네트워크 오류로 2번 다 실패' 한 경우뿐이다.
		       그래서 lastException 은 반드시 값이 들어 있다(null 일 수 없다). */

		System.out.println("[ChatbotController] 모든 재시도 실패: " + lastException.getMessage());

		if (lastException instanceof java.net.SocketTimeoutException) {
		/*  ▲               ▲
		    │               └ instanceof = "이 값이 저 종류가 맞니?" 를 물어보는 연산자.
		    │                  '이 사고가 시간 초과 종류였나?' 를 확인하는 것.
		    └ 위에서 챙겨 둔 마지막 실패 원인

		   ★ 여기가 이 메소드에서 가장 섬세한 부분이다.
		     같은 '실패'라도 사용자가 취할 행동이 다르기 때문에 안내를 나눈다. */

			// [시간 초과] AI 서버는 살아 있는데 답이 느렸을 뿐이다 → 기다렸다 다시 하면 될 가능성이 높다.
			//   그래서 전화번호를 굳이 안내하지 않는다. 괜한 전화를 유발할 필요가 없다.
			return "AI 응답 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.";
		}

		// [네트워크 실패] 연결 자체가 안 됐다 → 다시 눌러도 안 될 수 있다.
		//   그래서 '확실히 되는 대안'인 전화 상담을 함께 안내한다.
		//   ★ 사용자를 막다른 길에 세우지 않는다. 항상 다음 행동을 하나 쥐여 준다.
		return "네트워크 연결에 실패했습니다. 잠시 후 다시 시도해주세요.\n전화 상담: 02-3456-6574";
	}

	/* ═══════════════════════════════════════════════════════════════════════════
	   [ 이 메소드 한 줄 요약 ]
	     "두 번까지 조용히 다시 걸어 보고, 그래도 안 되면 원인에 맞는 말로 알려 준다."

	   [ 학생에게 강조할 포인트 3가지 ]
	     ① 예외는 '종류별로' 나눠 잡는다 (자식 먼저, 부모 나중)
	     ② 재시도는 '일시적 실패'에만 한다 — 구조적 실패에 재시도하면 시간만 버린다
	     ③ 로그는 개발자용(자세히), 화면 메시지는 사용자용(다음 행동을 주기)
	   ═══════════════════════════════════════════════════════════════════════════ */

	
	

	//===================================================================
	// doApiCall() — 실제로 인터넷 너머 AI 서버와 통신하는 부분
	//   이 파일에서 유일하게 '바깥 세상'과 연결되는 지점이다.
	//===================================================================
/* ═══════════════════════════════════════════════════════════════════════════
	   doApiCall() — 실제로 인터넷 너머 AI 서버와 '통신'하는 메소드
	   ═══════════════════════════════════════════════════════════════════════════

	   [ 이 파일에서 가장 특별한 메소드다 ]
	     다른 메소드들은 전부 우리 서버 안(메모리)에서만 논다.
	     이 메소드 하나만 '바깥 세상(인터넷)'으로 나간다.
	     → 그래서 유일하게 '내 마음대로 안 되는' 메소드다.
	       와이파이, 상대 서버 상태, 통신사 장비 등 내가 통제할 수 없는 것에 의존한다.
	       ★ 통제 못 하는 구간은 반드시 ①시간 제한 ②실패 처리 ③로그 3종 세트를 갖춰야 한다.

	   [ 통신의 전체 흐름 — 택배 보내기에 비유 ]
	     ① 주소 준비        : URL 객체 만들기        (송장에 주소 쓰기)
	     ② 봉투 설정        : setRequestProperty     (봉투에 '내용물: 서류', '보내는 사람' 표시)
	     ③ 내용물 넣고 발송  : getOutputStream + write (물건 넣고 택배기사에게 넘김) ← 여기서 실제 전송
	     ④ 결과 확인        : getResponseCode        (배송 완료? 반송?)
	     ⑤ 답장 읽기        : getInputStream         (답장 뜯어보기)
	     ⑥ 연결 종료        : disconnect             (전화 끊기)

	   [ throws Exception 이 붙은 이유 ]
	     이 메소드는 실패를 '자기가 처리하지 않는다'. 위로 그대로 던진다.
	     왜? 재시도할지 말지는 이 메소드가 결정할 일이 아니기 때문이다.
	     그 판단은 부르는 쪽(callOpenRouterAPI)의 몫이다.
	     ★ "실패를 어디서 처리할지" 를 정하는 것도 설계다. 아무데서나 잡으면 안 된다.
	   ═══════════════════════════════════════════════════════════════════════════ */
	private String doApiCall(String jsonStr) throws Exception {

		/* ───────── ① 주소 객체 만들기 ───────── */
		URL url = java.net.URI.create(API_URL).toURL();
		/* ↑ 예전에는 new URL("https://...") 로 바로 만들었다.
		     그런데 그 방식은 최신 자바에서 '더 이상 권장하지 않음(deprecated)' 으로 표시되어
		     취소선이 그어진다. URI 를 한 번 거쳐 만드는 것이 현재 방식이다.
		   [deprecated 란?] "아직 동작은 하지만 앞으로 없앨 예정이니 쓰지 마라" 는 자바의 예고장. */

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		/* ↑ ★ 헷갈리기 쉬운 부분 : 이름이 openConnection 이지만 '아직 연결되지 않았다'.
		     연결 준비물(설정 상자)만 받아온 것이다.
		     실제 연결은 아래 getOutputStream() 또는 getResponseCode() 를 부르는 순간 일어난다.
		   (HttpURLConnection) 는 형변환. openConnection() 은 더 포괄적인 타입을 주므로
		     HTTP 전용 기능(setRequestMethod 등)을 쓰려면 이렇게 바꿔 줘야 한다. */

		/* ───────── ② 봉투 설정 (요청 헤더) ─────────
		   헤더 = 본문과 별개로 붙이는 '꼬리표'.
		   "내용물이 뭔지 / 나는 누군지 / 어디서 왔는지" 를 상대 서버에게 알려 준다. */

		con.setRequestMethod("POST");
		/* ↑ POST = 데이터를 '보내는' 방식 / GET = 데이터를 '가져오는' 방식
		   [왜 POST 인가?]
		     GET 은 보낼 내용이 주소창에 그대로 붙는다 (?message=안녕...).
		     - 주소 길이 제한에 걸린다 (차량 목록까지 붙으면 수천 글자)
		     - 대화 내용이 서버 접속기록에 그대로 남는다 (개인정보 문제)
		     POST 는 내용을 '본문'에 숨겨 보내므로 이런 문제가 없다. */

		con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		// ↑ "내가 보내는 건 JSON 이고, 한글은 UTF-8 로 인코딩했다" 는 선언.
		//   이걸 빼면 상대 서버가 내용을 엉뚱하게 해석해 400 에러를 준다.

		con.setRequestProperty("Authorization", "Bearer " + apiKey);
		/* ↑ ★★ 가장 중요한 한 줄. 우리 출입증(API 키)을 제시하는 부분.

		   [ "Bearer " 가 뭔가요? ]
		     "이 토큰을 '지참한(bear)' 사람에게 권한을 준다" 는 국제 표준 표기법이다.
		     Bearer 와 키 사이의 '공백 한 칸'까지 규격이다. 빠지면 401(인증실패)이 난다.
		     초보자가 가장 많이 틀리는 지점이다.

		   [ 왜 이 한 줄이 위험한가 ]
		     이 키는 '돈이 나가는 신용카드'와 같다.
		     그래서 절대 ①소스코드에 박아두거나 ②콘솔에 찍거나 ③화면에 내려보내면 안 된다. */

		con.setRequestProperty("HTTP-Referer", "http://localhost:8090/CarProject2");
		// ↑ "어느 사이트에서 부른 요청인지" 알리는 값. OpenRouter 통계/순위에 쓰인다.
		//   필수는 아니다. 실제 서비스로 배포하면 진짜 도메인으로 바꿔 주면 된다.

		con.setRequestProperty("X-Title", "SM Rental Chatbot");
		// ↑ 우리 앱 이름. OpenRouter 대시보드에 이 이름으로 사용량이 표시된다.
		//   X- 로 시작하는 헤더 = 표준이 아닌 '그 서비스만의 커스텀 헤더' 라는 관례.

		con.setDoOutput(true);
		/* ↑ "나 본문을 보낼 거야" 라고 미리 선언하는 스위치.
		     이걸 true 로 안 하면 아래 getOutputStream() 에서 예외가 터진다.
		     (자바가 기본적으로는 '받기만 할 것'이라고 가정하기 때문) */

		con.setConnectTimeout(30000);
		/* ↑ [연결 제한시간] 30초 안에 '전화가 안 걸리면' 포기한다. 단위는 밀리초(1000=1초).
		     ★ 이 줄이 없으면 어떻게 되나?
		       상대 서버가 응답이 없을 때 자바는 '무한정' 기다린다.
		       그 사이 톰캣의 처리 담당(스레드)이 하나 묶인다.
		       이런 요청이 200개 쌓이면 서버 전체가 멈춘다. → 서비스 전체 장애로 번진다.
		     ★ 외부 호출에 타임아웃을 안 거는 건 실무에서 '사고 1순위' 다. */

		con.setReadTimeout(60000);
		/* ↑ [읽기 제한시간] 연결은 됐는데 '답이 안 올 때' 60초까지 기다린다.
		     연결(30초)보다 길게 준 이유 : AI 는 생각하는 데 시간이 걸린다.
		     긴 답변은 20~40초도 걸리므로 60초는 넉넉하게 잡은 값이다.

		   ★ 연결 타임아웃 ≠ 읽기 타임아웃 (초보자가 자주 혼동)
		     연결 = "전화가 걸리는가"  /  읽기 = "상대가 말을 시작하는가" */

		/* ───────── ③ 요청 본문 전송 ───────── */

		OutputStream os = con.getOutputStream();   // ★ 이 줄을 부르는 순간 실제 TCP 연결이 열린다
		os.write(jsonStr.getBytes("UTF-8"));
		/* ↑ 글자(String) → 바이트(byte[]) 로 변환해서 내보낸다.
		     [왜 변환하나?] 인터넷은 '글자'를 모른다. 0과 1(바이트)만 오간다.
		     getBytes("UTF-8") = "한글 포함해서 UTF-8 규칙으로 바이트로 바꿔줘"
		     ★ 여기서 UTF-8 을 빼면 서버 OS 기본 인코딩이 쓰여 한글이 깨진다. */

		os.flush();   // 버퍼(임시 보관함)에 남아 있는 걸 마저 밀어낸다. "다 보냈는지 확인 사살"
		os.close();   // 통로를 닫는다. 안 닫으면 자원이 계속 잡혀 있다(자원 누수).

		/* ───────── ④ 응답 코드 확인 ─────────

		   [ HTTP 응답 코드 — 이것만 외우면 된다 ]
		     2xx (200) : 성공
		     4xx       : '요청한 쪽'의 잘못   ← 401 인증실패 / 404 없음 / 429 너무 많이 요청
		     5xx       : '응답하는 서버'의 잘못 ← 우리가 고칠 수 없다. 기다리는 수밖에.
		   ★ 4로 시작하면 내 탓, 5로 시작하면 남 탓. 이 구분이 대응 방법을 가른다. */

		int responseCode = con.getResponseCode();
		System.out.println("[ChatbotController] 응답 코드: " + responseCode);
		// ↑ 챗봇이 이상할 때 가장 먼저 확인할 로그. 이 숫자 하나로 원인이 거의 좁혀진다.

		String responseStr;

		if (responseCode == HttpURLConnection.HTTP_OK) {   // HTTP_OK 는 200 을 뜻하는 상수(이름표)
			// ★ 200 이라는 숫자를 직접 쓰지 않고 상수를 쓰면, 코드를 읽는 사람이 의미를 바로 안다.
			responseStr = readBody(con.getInputStream());   // 정상 통로에서 본문 읽기

		} else {
			/* ───────── 실패 처리 구역 ─────────

			   ★★ 초보자가 가장 많이 막히는 지점 ★★
			     실패했을 때 con.getInputStream() 을 부르면 '예외'가 터진다.
			     실패 응답의 본문은 반드시 getErrorStream() 으로 읽어야 한다.
			     → 이걸 몰라서 "서버가 이유를 안 알려준다" 고 착각하는 경우가 매우 많다.
			       실제로는 이유를 보냈는데 우리가 안 뜯어본 것이다. */

			String errorBody = "";   // 빈 문자열로 시작. null 로 두면 뒤에서 NPE 위험이 생긴다.

			try {
				InputStream errorStream = con.getErrorStream();
				if (errorStream != null) {       // 오류 본문이 아예 없는 경우도 있다
					errorBody = readBody(errorStream);
				}
			} catch (Exception ex) {
				errorBody = "에러 읽기 실패";
				/* ↑ ★ 중요한 설계 판단 :
				     '오류를 읽는 도중에 또 오류' 가 난 경우다.
				     여기서 예외를 다시 던지면, 원래 알고 싶었던 진짜 실패 원인이
				     새 예외에 가려져 영영 안 보이게 된다.
				     그래서 여기서만큼은 조용히 삼키고 넘어간다.
				     ("불 끄러 온 소방차에 불나면 안 된다") */
			}

			System.out.println("[ChatbotController] API 오류 코드: " + responseCode);
			System.out.println("[ChatbotController] API 오류 내용: " + errorBody);
			// ↑ 이 두 줄이 없으면 "그냥 안 돼요" 로 끝난다. 원인 추적의 생명줄이다.

			con.disconnect();   // 실패했어도 연결은 반드시 끊는다 (자원 반납)

			/* ───────── 코드별 맞춤 대응 ─────────
			   ★ 이 아래가 이 메소드의 진짜 핵심이다.
			     같은 '실패'라도 사용자가 할 수 있는 행동이 전부 다르다.
			     429 → 기다리기 / 401 → 관리자 문의 / 404·500 → 자동 재시도
			     이걸 뭉뚱그려 "오류가 발생했습니다" 로 처리하면 아무도 문제를 못 푼다. */

			if (responseCode == 429) {
				/*
				 ════════════════════════════════════════════════════════════════════
				   [429 를 두 종류로 나눠 안내하도록 고친 부분 — 실전에서 배운 것]

				   429 = "요청이 너무 많다". 그런데 여기엔 성질이 완전히 다른 둘이 섞여 있다.
				     (가) 짧은 시간에 몰아서 불렀다  → 30초 뒤면 정말 된다
				     (나) 무료 '하루' 한도를 다 썼다 → 30초 뒤에도 안 된다 (내일 리셋)

				   [고치기 전] 둘 다 "잠시 후(30초) 다시 시도해주세요."
				     → (나)인 사용자는 30초마다 계속 눌러 본다. 될 리가 없는데.
				       이건 친절한 안내가 아니라 '거짓말'에 가깝다.

				   [실제로 겪은 일]
				     무료 하루 한도 50회를 테스트로 소진했는데 "30초 뒤에 다시"가 떠서
				     코드가 고장 난 줄 알고 한참을 뒤졌다.
				     응답 본문을 직접 열어보고서야 일일 한도임을 알았다.
				   ★ 교훈 : 에러 메시지가 틀리면, 디버깅 시간을 몇 배로 잡아먹는다.
				 ════════════════════════════════════════════════════════════════════
				*/
				boolean daily = errorBody != null
						&& (errorBody.contains("free-models-per-day")    // contains = 이 글자가 안에 들어있나?
						 || errorBody.contains("free_tier_daily")        // 서비스가 문구를 바꿀 수 있어
						 || errorBody.contains("per-day"));              // 여러 후보를 || 로 넉넉히 잡는다

				if (daily) {
					// [일일 한도] '내일 오라'고 정확히 말해 준다. + 관리자가 즉시 풀 방법도 함께.
					return "오늘 무료 AI 사용량을 모두 썼습니다.\n"
					     + "내일 다시 이용해주세요. (관리자: OpenRouter 크레딧을 충전하거나 다른 모델로 바꾸면 바로 풀립니다)";
				}
				// [순간 과부하] 이건 진짜로 기다리면 된다
				return "요청이 너무 많습니다. 잠시 후(30초) 다시 시도해주세요.";

			} else if (responseCode == 401) {
				/* [401 = 인증 실패] 키가 틀렸거나 만료됐거나 "Bearer " 표기를 틀렸다.
				   ★ 재시도하지 않는다. 키가 틀린 건 100번 다시 보내도 100번 틀린다.
				   ★ 사용자에게 설정 방법을 알려줘도 소용없다(사용자는 키에 접근 못 함).
				     그래서 '관리자 문의' 로 안내한다. 대상에 맞는 안내가 중요하다. */
				return "API 키가 유효하지 않습니다. 관리자에게 문의해주세요.";

			} else if (responseCode == 404) {
				/* [404 = 그 모델을 지금 못 쓴다]
				   무료 모델은 인기가 많아 일시적으로 내려가는 일이 흔하다.

				   ★★ 여기가 이 파일에서 가장 영리한 한 줄이다 ★★
				     return 하지 않고 '예외를 던진다(throw)'.
				     왜? return 하면 그 문구가 그대로 사용자 화면에 뜨고 끝이다.
				     예외로 던지면 부르는 쪽(callOpenRouterAPI)의 catch(IOException) 에 걸려
				     '자동으로 한 번 더 시도' 된다. 그 사이 모델이 돌아왔을 수도 있다.
				   → 즉 '예외의 종류'를 재시도 여부를 전달하는 신호로 쓰는 설계다. */
				throw new java.io.IOException("모델 일시 불가 (404): " + errorBody);

			} else if (responseCode >= 500) {
				/* [500번대 = 상대 서버가 아픈 것] 우리 코드는 잘못이 없다.
				   대개 잠깐이면 낫는다 → 404 와 같은 이유로 예외를 던져 재시도 대상으로 만든다. */
				throw new java.io.IOException("서버 오류 (코드: " + responseCode + "): " + errorBody);
			}

			// [그 밖의 코드] 400(요청 형식 오류) 등. 예상 못 한 경우이므로 코드와 내용을 그대로 노출한다.
			//   ★ 개발/수업 단계에서는 이렇게 다 보여주는 게 낫다.
			//     실제 서비스라면 errorBody 를 사용자에게 보여주지 않는 게 안전하다(내부 정보 노출).
			return "AI 서비스 오류(코드:" + responseCode + "): " + errorBody;
		}

		/* ───────── ⑥ 정상 종료 ───────── */
		con.disconnect();                              // 연결 끊기 (자원 반납)
		return parseOpenRouterResponse(responseStr);   // 응답 JSON 더미에서 'AI 가 한 말'만 뽑아 반환
	}


	/* ═══════════════════════════════════════════════════════════════════════════
	   buildOpenRouterRequest() — AI 에게 보낼 '편지(요청 JSON)' 를 조립하는 메소드
	   ═══════════════════════════════════════════════════════════════════════════

	   [ 완성되는 모양 — 이것만 이해하면 AI API 는 다 비슷하다 ]
	     {
	       "model"   : "openrouter/free",
	       "messages": [
	          { "role":"system",    "content":"너는 렌트카 상담사다... (차량 26대 목록)" },
	          { "role":"user",      "content":"경차 추천해줘" },        ← 이전 대화
	          { "role":"assistant", "content":"모닝을 추천드려요" },     ← 이전 대화
	          { "role":"user",      "content":"그거 얼마야?" }           ← 이번 질문
	       ],
	       "temperature": 0.7,
	       "max_tokens" : 1024
	     }

	   [ ★ 가장 중요한 개념 : AI 는 '기억'이 없다 ]
	     사람과 대화할 땐 상대가 앞 얘기를 기억한다. AI 는 아니다.
	     매번 요청은 '완전히 처음 만나는 사람'에게 보내는 것과 같다.
	     "그거 얼마야?" 만 보내면 AI 는 '그거'가 뭔지 알 방법이 전혀 없다.
	     → 그래서 이전 대화를 매번 통째로 다시 보내는 것이다.
	       챗봇이 대화를 기억하는 것처럼 보이는 건, 우리가 매번 다시 알려주기 때문이다.

	   [ role 3종류 ]
	     system    : "너는 이런 역할이야" — 맨 앞에 딱 1개. AI 의 인격/지식 설정
	     user      : 사람이 한 말
	     assistant : AI 가 한 말
	   ═══════════════════════════════════════════════════════════════════════════ */
	@SuppressWarnings("unchecked")
	private JSONObject buildOpenRouterRequest(String userMessage, String historyJson) {

		JSONObject requestBody = new JSONObject();   // 편지 전체를 담을 바깥 상자
		requestBody.put("model", modelName);         // 어떤 AI 모델에게 보낼지 지정

		JSONArray messages = new JSONArray();
		/* ↑ 대화 메시지들을 담을 '배열'.
		   ★ Object(상자)가 아니라 Array(배열)인 이유 = '순서'가 의미를 갖기 때문이다.
		     순서가 뒤바뀌면 대화가 뒤죽박죽이 되어 AI 가 엉뚱하게 답한다. */

		/* ───────── 1) 시스템 메시지 : AI 의 역할 지정 ───────── */
		JSONObject systemMsg = new JSONObject();
		systemMsg.put("role", "system");
		systemMsg.put("content", systemPrompt);   // init() 에서 서버 켤 때 만들어 둔 안내문 (차량 26대 포함)
		messages.add(systemMsg);
		/* ↑ ★ 반드시 맨 앞에 넣는다.
		     system 은 "이 대화 전체에 적용되는 규칙" 이다.
		     뒤쪽에 넣으면 AI 가 앞의 대화를 이미 다 읽은 뒤에 규칙을 받게 되어 효과가 약해진다.

		   ★ 매번 이 긴 안내문을 보내는 게 낭비 아닌가?
		     낭비 맞다. 하지만 AI 에게 기억이 없으므로 대안이 없다.
		     그래서 systemPrompt 를 init 에서 '한 번만 만들어 두고' 계속 재사용한다.
		     (만드는 비용은 아끼고, 보내는 비용은 어쩔 수 없이 낸다) */

		/* ───────── 2) 이전 대화 기록 추가 (최근 6개만) ─────────

		   [왜 6개만 보내나? — 3가지 이유]
		     ① 토큰 한도 : 무료 모델은 한 번에 받을 수 있는 글자 수가 작다. 넘으면 에러.
		     ② 속도      : 보내는 글자가 많을수록 응답이 느려진다.
		     ③ 비용      : 유료 모델이라면 입력 글자 수만큼 돈이 나간다.
		   ★ 6개면 대략 최근 3번의 주고받기다. "그거", "아까 그 차" 를 알아듣기에 충분하다. */

		if (historyJson != null && !historyJson.trim().isEmpty()) {
			try {
				JSONParser parser = new JSONParser();
				JSONArray history = (JSONArray) parser.parse(historyJson);   // 브라우저가 보낸 기록 글자 → JSON 배열

				int startIdx = Math.max(0, history.size() - 6);
				/* ↑ ★ Math.max 를 쓴 이유를 꼭 이해할 것.
				     기록이 10개면 : 10-6 = 4 → 4번부터 (최근 6개)
				     기록이 3개면  : 3-6 = -3 → 그대로 쓰면 배열 -3번을 읽어 프로그램이 죽는다!
				                      Math.max(0, -3) = 0 → 0번부터 (있는 3개 전부)
				   ★ 이런 '경계값(0개, 1개, 딱 6개)' 처리가 초보와 실무를 가르는 지점이다. */

				for (int i = startIdx; i < history.size(); i++) {
					JSONObject msg = (JSONObject) history.get(i);
					String role = (String) msg.get("role");   // 누가 한 말인가 ("user" 또는 "model")
					String text = (String) msg.get("text");   // 무슨 말을 했나

					String openaiRole = "model".equals(role) ? "assistant" : role;
					/* ↑ [역할 이름 번역기]
					   이 프로젝트는 원래 Google Gemini 를 쓰다가 OpenRouter 로 갈아탔다.
					     Gemini 용어   : user / model
					     OpenAI 용어   : user / assistant
					   화면 JS 는 아직 예전 이름("model")으로 보낸다.
					   → 화면 JS 를 전부 고치는 대신, 서버에서 한 줄로 번역해 준다.
					   ★ 이런 걸 '어댑터' 라고 한다. 바꾸기 쉬운 쪽만 고치는 실전 기술이다.

					   삼항 연산자 읽는 법 : 조건 ? 참일때 : 거짓일때 */

					JSONObject chatMsg = new JSONObject();
					chatMsg.put("role", openaiRole);
					chatMsg.put("content", text);
					messages.add(chatMsg);
				}
			} catch (Exception e) {
				/* ★ 여기서 예외를 '삼키는' 것은 의도적인 설계 판단이다.

				   [판단 근거]
				     대화 기록은 '있으면 좋은 것'이지 '없으면 안 되는 것'이 아니다.
				     기록이 깨졌다고 챗봇 전체를 실패시키면,
				     사용자는 기억력만 조금 잃어도 될 것을 서비스 전체를 못 쓰게 된다.
				   → 기록 없이라도 질문에는 답하게 한다. (= 우아한 성능 저하)

				   ★ 단, 반드시 로그는 남긴다. 조용히 삼키기만 하면 원인을 영원히 못 찾는다. */
				System.out.println("[ChatbotController] 대화 기록 파싱 오류: " + e.getMessage());
			}
		}

		/* ───────── 3) 이번에 사용자가 한 말 (반드시 맨 마지막) ───────── */
		JSONObject userMsg = new JSONObject();
		userMsg.put("role", "user");
		userMsg.put("content", userMessage);
		messages.add(userMsg);
		// ★ 맨 마지막이어야 AI 가 "이게 지금 답해야 할 질문" 으로 인식한다.

		requestBody.put("messages", messages);   // 완성된 메시지 목록을 편지에 담는다

		/* ───────── 4) 생성 옵션 ───────── */

		requestBody.put("temperature", 0.7);
		/* ↑ [온도] AI 답변의 '자유도'. 0.0 ~ 1.0 (일부 모델은 2.0까지)
		     0.0 에 가까움 : 매번 거의 같은 답. 딱딱하고 정확함. → 번역·요약·데이터 추출에 적합
		     1.0 에 가까움 : 매번 다른 답. 창의적이지만 엉뚱해질 수도. → 아이디어·글쓰기에 적합
		     0.7 = 상담 챗봇에 적당한 중간값. 자연스럽되 너무 헛소리는 안 한다.
		   ★ 참고 : 이 파일의 예약 조건 추출(reserve)은 정확성이 생명이므로
		     원래는 0.1~0.2 로 낮추는 것이 더 좋다. (개선 여지) */

		requestBody.put("max_tokens", 1024);
		/* ↑ [답변 길이 상한] 토큰(token) ≒ 단어 조각. 한글은 대략 1글자 ≒ 1~2토큰.
		     1024토큰 ≒ 한글 500~800자 정도.
		   [너무 작게 잡으면] 답변이 문장 중간에서 뚝 끊긴다.
		   [너무 크게 잡으면] 응답이 느려지고 (유료라면) 요금이 늘어난다.
		   ★ 이건 '최대치'일 뿐이다. AI 가 짧게 답하면 그만큼만 쓴다. 미리 결제되는 게 아니다. */

		return requestBody;
	}


	/* ═══════════════════════════════════════════════════════════════════════════
	   parseOpenRouterResponse() — 응답 JSON 더미에서 'AI 가 한 말'만 뽑아내는 메소드
	   ═══════════════════════════════════════════════════════════════════════════

	   [ AI 서버가 보내오는 실제 응답 모양 ]
	     {
	       "id": "gen-abc123",
	       "model": "...",
	       "choices": [                       ← 답변 '후보 목록' (배열)
	          {
	            "index": 0,
	            "message": {                  ← 실제 메시지 상자
	               "role": "assistant",
	               "content": "★여기가 우리가 원하는 답변★"
	            },
	            "finish_reason": "stop"
	          }
	       ],
	       "usage": { "total_tokens": 350 }   ← 사용량 정보
	     }

	   ★ 우리가 원하는 건 저 깊숙한 곳의 content 한 줄뿐이다.
	     choices(배열) → [0]번째 → message(상자) → content
	     이렇게 3단계를 파고 들어가야 한다. 이걸 '파싱(parsing)' 이라고 한다.

	   [ 왜 choices 가 '배열'인가? ]
	     원래 API 는 "답변 3개 만들어줘" 도 가능하다(n=3 옵션).
	     우리는 1개만 요청하므로 항상 0번째 하나만 쓴다.
	     그래도 형식은 배열이므로 [0] 을 꺼내는 과정이 필요하다.
	   ═══════════════════════════════════════════════════════════════════════════ */
	private String parseOpenRouterResponse(String responseStr) {
		try {
			System.out.println("[ChatbotController] 원본 응답: " + responseStr);
			/* ↑ ★ 파싱이 실패했을 때 가장 먼저 봐야 할 로그.
			     "AI 가 답을 안 준 것"과 "답은 줬는데 우리가 못 꺼낸 것"은 완전히 다른 문제다.
			     이 로그가 있어야 둘을 구분할 수 있다.
			   ※ 실제 서비스라면 대화 내용이 로그에 남으므로 이 줄은 빼거나 줄여야 한다. */

			JSONParser parser = new JSONParser();
			JSONObject response = (JSONObject) parser.parse(responseStr);   // 글자 → 자바 객체

			JSONArray choices = (JSONArray) response.get("choices");

			if (choices != null && choices.size() > 0) {
			/*  ▲            ▲
			    │            └ ★ 둘 다 확인해야 한다.
			    │               null 검사만 하면 : 배열은 있는데 비어 있을 때 [0] 읽다가 죽는다
			    │               size 검사만 하면 : null.size() 를 부르다가 죽는다
			    └ ★ 그리고 순서가 중요하다. null 검사가 반드시 앞. (&& 단축 평가) */

				JSONObject firstChoice = (JSONObject) choices.get(0);          // 첫 번째 답변 후보
				JSONObject message = (JSONObject) firstChoice.get("message");  // 그 안의 message 상자

				if (message != null) {

					Object contentObj = message.get("content");
					String content = (contentObj != null) ? contentObj.toString() : null;
					/* ↑ ★ 왜 바로 (String) 으로 형변환하지 않았나?
					     JSON 의 값은 글자일 수도, 숫자일 수도, null 일 수도 있다.
					     (String) 으로 강제 변환했다가 다른 타입이면 ClassCastException 으로 죽는다.
					     Object 로 받아서 toString() 하면 어떤 타입이든 안전하게 글자가 된다.
					   ★ '믿을 수 없는 외부 데이터'를 다룰 때의 기본자세다. */

					if (content != null && !content.trim().isEmpty()) {
						return content;   // ★ 정상 경로. 99%는 여기서 끝난다.
					}

					/* ───── 예외 상황 : content 가 비어 있는 경우 ─────
					   일부 '생각하는(thinking/reasoning) 모델'은
					   생각 과정을 reasoning 에 담고 content 는 비워 보내는 버릇이 있다.
					   그대로 두면 사용자에게 '빈 말풍선'이 뜬다.
					   → 완벽하진 않아도 reasoning 이라도 보여주는 게 낫다. */
					Object reasoningObj = message.get("reasoning");
					if (reasoningObj != null) {
						String reasoning = reasoningObj.toString();
						if (!reasoning.trim().isEmpty()) {
							return reasoning;
						}
					}
					/* ★ 이런 코드를 '땜질' 이라고 무시하면 안 된다.
					     외부 서비스는 우리 사정을 봐주지 않는다.
					     '예상과 다른 응답'에 대비하는 것도 실력이다. */
				}
			}

			/* ───── AI 서버가 error 로 이유를 알려 준 경우 ─────
			   응답 코드는 200인데 본문에 error 가 들어 있는 경우가 실제로 있다.
			   (중개 서비스라서 '전달은 성공, 내용은 실패' 인 상황) */
			JSONObject error = (JSONObject) response.get("error");
			if (error != null) {
				String errMsg = (String) error.get("message");
				System.out.println("[ChatbotController] API 에러 메시지: " + errMsg);
				return "AI 응답 오류: " + errMsg;   // 서버가 준 이유를 그대로 전달
			}

			/* ───── 우리가 모르는 새로운 응답 구조 ─────
			   AI 서비스는 형식을 예고 없이 바꾸기도 한다.
			   통째로 로그에 남겨 두면 나중에 그 구조를 보고 코드를 고칠 수 있다.
			   ★ "원인을 알 수 없는 실패" 를 "기록이 남은 실패" 로 바꾸는 한 줄이다. */
			System.out.println("[ChatbotController] 파싱 실패 - 응답 구조: " + response.toJSONString());
			return "죄송합니다. 응답을 처리하지 못했습니다. 다시 질문해주세요.";

		} catch (Exception e) {
			// [여기 오는 경우] 응답이 아예 JSON 이 아닐 때. 예: HTML 에러페이지, 빈 문자열
			System.out.println("[ChatbotController] 응답 파싱 오류: " + e.getMessage());
			System.out.println("[ChatbotController] 원본 응답: " + responseStr);   // 원본이 있어야 원인을 찾는다
			return "죄송합니다. 응답을 처리하지 못했습니다. 다시 질문해주세요.";
		}
	}


	/* ═══════════════════════════════════════════════════════════════════════════
	   readBody() — 통로(InputStream)로 들어오는 내용을 전부 읽어 하나의 문자열로 만든다
	   ═══════════════════════════════════════════════════════════════════════════

	   [ 왜 이런 번거로운 과정이 필요한가 ]
	     네트워크로 오는 데이터는 '한 덩어리'로 도착하지 않는다.
	     물이 수도관을 타고 조금씩 흐르듯 나눠서 들어온다.
	     → 그래서 '다 들어올 때까지 반복해서 읽어 모으는' 과정이 필요하다.

	   [ 3단 변환 구조 — 이 모양은 자바에서 계속 만난다 ]
	     InputStream        : 바이트가 흐르는 관       (숫자 0101...)
	        ↓ InputStreamReader 로 감싸면
	     글자 단위로 읽힘                              ('안', '녕')
	        ↓ BufferedReader 로 한 번 더 감싸면
	     줄 단위로 빠르게 읽힘                          ("안녕하세요")
	   ★ 이렇게 겹겹이 감싸 기능을 더하는 방식을 '데코레이터 패턴' 이라고 한다.
	     자바 입출력(IO)은 전부 이 구조다. 한 번 이해하면 파일·네트워크가 다 똑같아 보인다.
	   ═══════════════════════════════════════════════════════════════════════════ */
	private String readBody(InputStream body) {

		InputStreamReader streamReader = new InputStreamReader(body, java.nio.charset.StandardCharsets.UTF_8);
		/* ↑ ★★ 여기서 UTF-8 을 지정하지 않으면 한글이 깨진다 ★★
		     지정하지 않으면 '서버 컴퓨터의 기본 인코딩'이 쓰인다.
		     내 PC(Windows/MS949)에서는 잘 되다가 서버(Linux/UTF-8)에 올리면 깨지는
		     전형적인 "내 컴퓨터에선 되는데요?" 사고가 바로 이것이다.
		   StandardCharsets.UTF_8 을 쓰면 "UTF-8" 오타 위험도 없다(컴파일러가 잡아줌). */

		try (BufferedReader lineReader = new BufferedReader(streamReader)) {
		/*  ▲
		    └ ★ try-with-resources 문법 (자바 7 이상)
		       괄호 안에서 만든 자원은 블록이 끝나면 '자동으로' close() 된다.
		       - 정상 종료해도 닫힌다
		       - 예외가 터져도 닫힌다
		       - return 으로 나가도 닫힌다

		       [옛날 방식과 비교]
		         예전엔 finally { if(r != null) try{ r.close(); }catch(...){} } 를
		         직접 써야 했다. 길고, 빠뜨리기 쉽고, 빠뜨리면 자원 누수가 쌓여
		         나중에 서버가 파일/소켓을 더 못 여는 사고가 난다.
		       ★ '닫기를 잊어버릴 수 없게 만드는' 문법. 실무 필수. */

			StringBuilder responseBody = new StringBuilder();
			// ★ String 을 + 로 이어붙이면 응답 1000줄 = 문자열 1000개 생성 = 매우 느리다.
			//   StringBuilder 는 메모장 하나에 계속 이어 쓰는 방식이라 훨씬 빠르다.

			String line;
			while ((line = lineReader.readLine()) != null) {
			/*  ▲     ▲
			    │     └ ★ 대입과 비교를 한 줄에 쓴 관용 표현. 읽는 순서 :
			    │        ① readLine() 으로 한 줄 읽어서  ② line 에 넣고  ③ 그게 null 이 아니면 반복
			    │        readLine() 은 더 읽을 게 없으면 null 을 돌려준다 = 끝났다는 신호
			    └ while = "조건이 참인 동안 계속 반복" */

				responseBody.append(line);
				/* ↑ ★ 주의 : 줄바꿈(\n)을 붙이지 않고 그냥 이어 붙이고 있다.
				     JSON 은 줄바꿈이 있든 없든 해석에 문제가 없으므로 이게 맞다.
				     하지만 '줄바꿈이 의미 있는 텍스트'를 읽을 때 이 코드를 복사하면
				     전부 한 줄로 붙어버린다. 용도를 알고 써야 한다. */
			}
			return responseBody.toString();

		} catch (IOException e) {
			throw new RuntimeException("API 응답 읽기 실패: " + e.getMessage(), e);
			/* ↑ ★ 여기서 예외를 '삼키지 않고 다시 던지는' 이유 :
			     응답을 못 읽었다 = 결과가 아예 없다.
			     그런데 여기서 조용히 "" (빈 문자열)을 돌려주면
			     부르는 쪽은 "AI 가 빈 답을 줬나 보다" 하고 엉뚱한 곳을 의심하게 된다.
			   ★ 두 번째 인자 e 를 꼭 넘기는 것에 주목.
			     이걸 빼면 '원래 어디서 터졌는지'(원인 추적 정보)가 통째로 사라진다.
			     초보자가 가장 많이 하는 실수 중 하나다.

			   [RuntimeException 을 쓴 이유]
			     IOException 은 '반드시 처리해야 하는 예외'라서 이 메소드를 부르는 모든 곳에
			     try-catch 를 강제한다. RuntimeException 으로 바꾸면 강제하지 않는다.
			     여기서는 doApiCall 이 throws Exception 으로 어차피 다 받아주므로 이 편이 간결하다. */
		}
	}


	/* ═══════════════════════════════════════════════════════════════════════════
	   buildAIServicePrompt() — 서비스 종류별로 다른 'AI 역할 설명서' 를 만든다
	   ═══════════════════════════════════════════════════════════════════════════

	   [ 왜 서비스마다 프롬프트를 다르게 주나 ]
	     같은 AI 라도 "너는 차량 추천 전문가야" 라고 하면 추천을 잘하고,
	     "너는 Git 강사야" 라고 하면 Git 을 잘 설명한다.
	     ★ AI 를 '한 명의 만능 직원'이 아니라 '역할을 갈아끼우는 배우' 로 쓰는 것이다.
	       같은 모델, 같은 API, 다른 지시문 → 전혀 다른 4개의 서비스가 된다.
	       이게 AI 를 제품에 붙일 때 가장 가성비 좋은 기법이다.

	   [ 프롬프트를 잘 쓰는 4가지 원칙 — 아래 코드에 다 들어있다 ]
	     ① 역할 지정   : "당신은 ~ 전문가입니다"
	     ② 범위 제한   : "반드시 아래 목록에서만 추천하세요"   ← 거짓말(환각) 방지
	     ③ 형식 지정   : "답변 형식: **1. 차량명** - 이유"     ← 화면에 일정하게 나오게
	     ④ 금지 사항   : "관련 없는 질문은 정중히 거절"         ← 엉뚱한 용도로 새는 것 방지

	   [ type 종류 ]
	     "recommend"(차량추천) / "cost"(비용계산) / "git"(Git도우미) / "travel"(여행플래너)
	   ═══════════════════════════════════════════════════════════════════════════ */
	private String buildAIServicePrompt(String serviceType) {

		StringBuilder prompt = new StringBuilder();   // 프롬포트 전체 문자열을 이어 붙여 저장할 용도의 객체 메모리 생성

		/* ───────────── 차량 추천 전문가 ───────────── */
		if ("recommend".equals(serviceType)) {
			prompt.append("당신은 (주)SM렌탈의 AI 차량 추천 전문가입니다.\n");        // ① 역할 지정
			prompt.append("고객의 인원수, 예산, 용도를 분석하여 가장 적합한 차량을 추천해주세요.\n");
			prompt.append("반드시 아래 보유 차량 목록에서만 추천하세요.\n");
			/* ↑ ★★ 이 한 줄이 이 프롬프트에서 가장 중요하다 ★★
			     이 문장이 없으면 AI 는 우리에게 없는 차(예: 테슬라 모델Y)를 자신만만하게 추천한다.
			     AI 가 그럴듯한 거짓말을 하는 현상을 '환각(hallucination)' 이라고 한다.
			     AI 는 '모른다'고 말하는 것보다 '그럴듯하게 지어내는' 쪽으로 학습되어 있다.
			   → 범위를 좁혀 주는 것이 환각을 막는 가장 효과적인 방법이다. */
			prompt.append("추천 차량은 최대 3대까지, 각각 추천 이유를 설명해주세요.\n");   // 개수 제한 = 답변이 늘어지는 것 방지
			prompt.append("이모지를 적절히 사용해 친근감을 주세요.\n\n");

			prompt.append("답변 형식:\n");                                          // ③ 형식 지정
			prompt.append("**1. 차량명** - 추천 이유\n");                            // ** ** = 마크다운 굵게. 화면 JS 가 변환해 준다.
			prompt.append("- 일일 렌탈료: OO원\n");
			prompt.append("- 탑승 인원: O명\n");
			prompt.append("- 등급: OO\n\n");
			/* ↑ ★ 형식을 지정하지 않으면 매번 다른 모양으로 답해서 화면이 들쭉날쭉해진다.
			     "예시를 보여주는" 방식이 "설명하는" 방식보다 훨씬 잘 먹힌다.
			     (프롬프트 기법 중 '원샷 프롬프팅' 이라고 부른다) */

		/* ───────────── 비용 계산 전문가 ───────────── */
		} else if ("cost".equals(serviceType)) {
			prompt.append("당신은 (주)SM렌탈의 AI 비용 계산 전문가입니다.\n");
			prompt.append("고객이 선택한 차량, 대여기간, 옵션을 바탕으로 총 비용을 계산해주세요.\n");
			prompt.append("비용 계산 공식: 총 비용 = (차량 일일렌탈료 + 선택 옵션 합계) x 대여일수\n\n");
			/* ↑ ★ 공식을 명시적으로 알려 준다.
			     AI 는 계산에 약하다. 특히 여러 단계 곱셈·덧셈에서 자주 틀린다.
			     공식을 주면 '어떤 순서로 계산할지'가 정해져 정확도가 눈에 띄게 올라간다.
			   ※ 그래도 100%는 아니다. 그래서 실제 결제 금액은 절대 AI 에게 맡기지 않고
			     서버가 직접 계산한다. (handleReserveAI 참고) */

			prompt.append("옵션 가격 (1일 기준):\n");
			prompt.append(String.format("- 자차보험: %,d원/일%n", CarService.PRICE_INSURANCE));
			prompt.append(String.format("- WiFi: %,d원/일%n", CarService.PRICE_WIFI));
			prompt.append(String.format("- 네비게이션: %,d원/일%n", CarService.PRICE_NAVI));
			prompt.append(String.format("- 베이비시트: %,d원/일%n%n", CarService.PRICE_BABYSEAT));
			/* ↑ ★ 숫자를 직접 안 쓰고 CarService 상수를 쓰는 이유 (매우 중요)
			     예전에 여기에 "5,000원" 이라 적어두고 결제 코드에는 "10,000원" 이 있어서
			     챗봇 안내와 실제 청구액이 달라지는 사고가 났다.
			     지금은 요금의 '기준점'이 CarService 한 곳뿐이라 어긋날 수가 없다.
			     → SSOT(Single Source of Truth, 단일 진실 공급원) 원칙.
			   ★ "같은 값이 두 군데 적혀 있으면 언젠가 반드시 달라진다" 는 게 실무 법칙이다. */

			prompt.append("비용을 항목별로 정리해주시고, 절약 팁도 알려주세요.\n");   // 계산만이 아니라 '가치'를 더한다
			prompt.append("이모지를 적절히 사용해 친근감을 주세요.\n\n");

		/* ───────────── Git 도우미 (학습용 대형 프롬프트) ───────────── */
		} else if ("git".equals(serviceType)) {
			/* ★ 이 분기만 유난히 길다. 왜?
			     AI 가 '일반적인 Git 지식'은 알아도 '우리 수업 상황'은 모르기 때문이다.
			       - 학생이 비전공자라는 것
			       - Eclipse Dynamic Web Project 를 쓴다는 것
			       - 팀프로젝트 중이라는 것
			     이런 맥락을 넣어주면 답변 품질이 완전히 달라진다.
			   ★ 프롬프트 = "AI 에게 주는 신입사원 교육자료" 라고 생각하면 이해가 빠르다. */

			prompt.append("[절대규칙] 반드시 한국어로만 답변하세요. 영어 답변 금지. 모든 설명, 예시, 비유 전부 한국어로 작성합니다.\n\n");
			/* ↑ ★ 왜 이걸 맨 앞에, 그것도 [절대규칙] 이라고 강하게 썼나?
			     무료 모델은 영어권 데이터로 더 많이 학습되어 종종 영어로 답한다.
			     특히 Git 은 영어 자료가 압도적이라 영어 답변 확률이 더 높다.
			   ★ AI 는 '맨 앞'과 '맨 뒤'에 있는 지시를 가장 잘 따른다(중간은 흘린다).
			     그래서 꼭 지켜야 할 규칙은 앞이나 뒤에 배치한다. */

			prompt.append("당신은 10년차 Git & GitHub 전문 강사이자 최고의 코딩 튜터입니다.\n");
			prompt.append("비전공자 초보자가 팀프로젝트에서 Git/GitHub를 완벽하게 사용할 수 있도록 도와주세요.\n");
			prompt.append("학생이 완전 처음이라고 가정하고, 컴퓨터 비유(폴더, 파일, USB 등)로 쉽게 설명하세요.\n\n");
			// ↑ '누구에게' 말하는지를 알려주면 AI 가 어휘 수준을 알아서 맞춘다. 매우 효과가 크다.

			prompt.append("=== 당신의 강점 ===\n");   // ★ 잘하는 것을 규정해 주면 그 방향으로 답한다
			prompt.append("- 어려운 개념을 일상생활 비유로 쉽게 설명 (예: commit=세이브, branch=평행우주)\n");
			prompt.append("- 명령어마다 '왜 이걸 해야 하는지' 이유를 설명\n");
			prompt.append("- 실수했을 때 복구 방법을 항상 함께 안내\n");
			prompt.append("- 실제 팀프로젝트 상황 예시로 설명\n\n");

			prompt.append("=== Git 핵심 지식 (답변에 활용) ===\n\n");
			/* ↑ 아래부터는 '지식 주입' 구간.
			   ★ AI 가 모르는 내용이라서 넣는 게 아니다. Git 은 AI 도 잘 안다.
			     '우리가 원하는 방식의 설명'을 고정시키려고 넣는 것이다.
			     그래야 학생이 언제 물어봐도 같은 비유, 같은 용어로 답이 나온다.
			     → 수업 교재와 챗봇 설명이 어긋나지 않게 하는 장치다. */

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

			prompt.append("[ 매일 쓰는 기본 흐름 ]\n");   // ★ 학생이 가장 많이 묻는 것을 미리 넣어둔다
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

			prompt.append("[ 충돌 해결 방법 ]\n");   // ★ 팀프로젝트에서 학생들이 가장 당황하는 지점
			prompt.append("충돌 발생 시 파일에 아래 표시가 생김:\n");
			prompt.append("<<<<<<< HEAD\n");
			prompt.append("내가 수정한 내용\n");
			prompt.append("=======\n");
			prompt.append("상대방이 수정한 내용\n");
			prompt.append(">>>>>>> branch명\n");
			prompt.append("해결: <<<, ===, >>> 표시를 지우고 원하는 코드만 남긴 후 저장 → add → commit\n\n");

			prompt.append("[ 자주 발생하는 에러와 해결법 ]\n");
			/* ↑ ★ 이 블록이 이 프롬프트의 핵심 가치다.
			     학생은 에러 메시지를 그대로 복사해서 물어본다.
			     미리 '원인+해결'을 심어두면 AI 가 헤매지 않고 바로 정답을 준다.
			     실제 수업에서 반복해서 나온 에러 5개를 추린 것 = 현장 데이터의 힘. */
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
			/* ↑ ★ 우리 수업 환경(Eclipse)에 특화된 정보.
			     일반 AI 는 이걸 모르니 엉뚱한 .gitignore 를 알려준다.
			     이런 '우리만의 맥락'이 프롬프트의 진짜 가치다. */
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
			/* ↑ ★ 맨 마지막에 규칙을 한 번 더 정리한다.
			     AI 는 '가장 마지막에 읽은 지시'를 특히 잘 따른다.
			     그래서 1번 항목(한국어)이 맨 앞과 맨 뒤에 두 번 등장한다. 의도적인 반복이다. */
			prompt.append("1. [필수] 모든 답변은 반드시 한국어로 작성\n");
			prompt.append("2. 명령어는 반드시 `백틱`으로 감싸기\n");           // 화면에서 코드박스로 예쁘게 표시됨
			prompt.append("3. 단계별로 번호를 매겨 설명 (1, 2, 3...)\n");
			prompt.append("4. 각 명령어마다 '이 명령어는 ~하는 역할입니다' 식으로 의미 설명\n");
			prompt.append("5. 비유를 최소 1개 이상 사용해서 이해를 도움\n");
			prompt.append("6. 실수/에러 가능성이 있으면 '주의사항'도 함께 안내\n");
			prompt.append("7. 이모지를 적절히 사용해 친근감 주기\n");
			prompt.append("8. Git과 관련 없는 질문에는 정중히 거절하고 Git 도움으로 안내\n");   // ④ 금지 사항

			return prompt.toString();
			/* ↑ ★★ 여기서만 중간에 return 한다. 반드시 이해하고 넘어갈 것 ★★

			   다른 분기들은 아래로 흘러가서 '차량 목록'을 뒤에 붙인다.
			   그런데 Git 질문에 차량 26대 목록이 왜 필요한가? 전혀 필요 없다.

			   억지로 붙이면 :
			     - 보내는 글자 수(토큰)만 늘어 응답이 느려지고
			     - 무료 모델의 입력 한도를 잡아먹고
			     - AI 가 "Git 설명하다가 갑자기 차를 추천"하는 사고까지 난다
			   → 그래서 여기서 바로 끊는다. '안 붙이는 것'도 설계다. */

		/* ───────────── 여행 플래너 ───────────── */
		} else if ("travel".equals(serviceType)) {
			prompt.append("당신은 (주)SM렌탈의 AI 여행 플래너입니다.\n");
			prompt.append("고객의 여행지, 일수, 인원, 스타일을 바탕으로 여행 일정을 만들어주세요.\n");
			prompt.append("일정에 맞는 추천 차량도 함께 안내해주세요.\n");
			/* ↑ ★ 영리한 기획 포인트 :
			     여행 일정을 짜주면서 자연스럽게 우리 차를 추천한다.
			     "광고 같지 않은 광고". 사용자에게 진짜 가치를 주면서 매출로 연결한다. */
			prompt.append("반드시 아래 보유 차량 목록에서만 추천하세요.\n");   // 여기도 환각 방지
			prompt.append("이모지를 적절히 사용해 친근감을 주세요.\n\n");
			prompt.append("답변 형식: 일차별로 오전/오후 일정을 나누어 작성하세요.\n");
			prompt.append("마지막에 추천 차량과 예상 렌탈 비용을 안내해주세요.\n\n");
		}

		/* ───────── [공통] 차량 목록 이어 붙이기 ─────────
		   recommend / cost / travel 세 경우는 모두 차량 정보가 필요하다. (git 은 위에서 이미 빠져나감)

		   ★ 여기에 차량 목록을 다시 쓰지 않고, init() 에서 만든 systemPrompt 에서 '잘라 쓴다'.
		     왜? 차량 목록을 여러 곳에 적어두면 나중에 한 곳만 고치고 다른 곳을 잊게 된다.
		     원본은 하나만 두고 필요한 곳에서 꺼내 쓰는 것이 유지보수의 기본이다. */

		int startIdx = systemPrompt.indexOf("=== 보유 차량 목록 ===");
		/* ↑ indexOf = 그 글자가 '시작되는 위치'를 돌려준다. 못 찾으면 -1.
		   ★ 이런 방식을 '문자열 위치 기반 추출' 이라고 하는데, 솔직히 약한 코드다.
		     나중에 누가 systemPrompt 의 "=== 보유 차량 목록 ===" 문구를 바꾸면
		     여기가 조용히 -1 이 되어 차량 목록이 안 붙는다. 에러도 안 나서 알아채기 어렵다.
		   [더 나은 방법] 차량 목록만 별도 필드(carInfoText)에 따로 보관해 두고 그걸 쓰는 것.
		     → 개선 과제로 학생에게 내주기 좋은 지점이다. */

		if (startIdx >= 0) {   // 찾았을 때만 붙인다 (-1 이면 substring 에서 예외가 터진다)
			prompt.append(systemPrompt.substring(startIdx));   // 그 위치부터 '끝까지' 잘라서 붙임
		}

		return prompt.toString();
	}


	/* ═══════════════════════════════════════════════════════════════════════════
	   callOpenRouterAPIWithPrompt() — 맞춤 지시문으로 '한 번만' AI 를 호출한다
	   ═══════════════════════════════════════════════════════════════════════════

	   [ callOpenRouterAPI() 와 무엇이 다른가 — 딱 3가지 ]
	     ①  지시문(system) : 저쪽은 고정된 systemPrompt / 이쪽은 매번 다른 맞춤 지시문
	     ②  대화 기록      : 저쪽은 최근 6개를 보냄     / 이쪽은 아예 안 보냄 (단발성)
	     ③  답변 길이      : 저쪽은 1024 고정           / 이쪽은 git 이면 3072, 아니면 2048

	   [ 왜 대화 기록을 안 보내나? ]
	     차량 추천·비용 계산·Git 질문은 '한 번 묻고 한 번 답하면 끝'인 성격이다.
	     앞 대화를 참조할 일이 없다.
	     → 안 보내면 : 빨라지고, 토큰을 아끼고, 앞 대화에 오염될 위험도 없다.
	     ★ '안 하는 것'이 이득인 경우를 구분하는 것도 설계 능력이다.

	   [ 솔직한 지적 — 학생에게 좋은 토론거리 ]
	     이 메소드의 재시도 부분은 callOpenRouterAPI() 와 코드가 거의 똑같다.
	     이런 걸 '중복 코드' 라고 하고, 재시도 정책을 바꾸려면 두 곳을 다 고쳐야 해서
	     한 곳만 고치는 실수가 생긴다.
	     → 개선 방향 : 재시도 로직만 뽑아 callWithRetry(jsonStr) 같은 공용 메소드로 만들기.
	   ═══════════════════════════════════════════════════════════════════════════ */
	@SuppressWarnings("unchecked")
	private String callOpenRouterAPIWithPrompt(String userMessage, String customSystemPrompt, String serviceType) {
	/*                                          ▲                  ▲                       ▲
	                                            │                  │                       └ 서비스 종류. 답변 길이를 정하는 데만 쓴다.
	                                            │                  └ buildAIServicePrompt() 가 만들어 준 맞춤 지시문
	                                            └ 사용자가 입력한 요청 내용 */

		// [사전 확인] 키가 없으면 통신할 이유가 없다. (fail fast)
		if (apiKey == null || "YOUR_API_KEY_HERE".equals(apiKey)) {
			return "AI API 키가 설정되지 않았습니다. 관리자에게 문의해주세요.";
		}

		/* ───────── 요청 JSON 조립 (대화 기록이 없어 아주 단순하다) ───────── */

		JSONObject requestBody = new JSONObject();
		requestBody.put("model", modelName);

		JSONArray messages = new JSONArray();

		// 1) 맞춤 지시문 (system) — 이번 서비스 전용 역할 설명서
		JSONObject systemMsg = new JSONObject();
		systemMsg.put("role", "system");
		systemMsg.put("content", customSystemPrompt);
		messages.add(systemMsg);

		// 2) 사용자 요청 (user) — 딱 이 하나뿐. 그래서 '단발성' 이다.
		JSONObject userMsg = new JSONObject();
		userMsg.put("role", "user");
		userMsg.put("content", userMessage);
		messages.add(userMsg);

		requestBody.put("messages", messages);
		requestBody.put("temperature", 0.7);   // 여기도 0.7. 추천·설명에는 약간의 자유도가 있는 편이 자연스럽다.

		requestBody.put("max_tokens", "git".equals(serviceType) ? 3072 : 2048);
		/* ↑ ★ 서비스 성격에 따라 답변 길이 상한을 다르게 준다.
		     git   : 단계별 설명 + 명령어 + 주의사항 → 길어야 한다 → 3072
		     나머지 : 차량 3대 추천, 비용 항목 정리  → 2048 이면 충분

		   [너무 작으면?] 문장 중간에서 뚝 끊긴 답변이 나온다. 학생은 "고장났어요" 라고 한다.
		   [너무 크면?]  느려지고 (유료라면) 비용이 올라간다.
		   ★ 일반 상담(1024)보다 전부 크게 잡은 이유 = 이쪽은 '설명형' 답변이기 때문. */

		String jsonStr = requestBody.toJSONString();
		System.out.println("[ChatbotController] AI서비스 요청 JSON 길이: " + jsonStr.length());
		// ↑ 로그 앞에 'AI서비스' 를 붙여 일반 상담 로그와 구분되게 했다. 작지만 중요한 배려다.

		/* ───────── 재시도 (최대 2번) ───────── */

		int maxRetries = 2;

		for (int attempt = 1; attempt <= maxRetries; attempt++) {
			try {
				return doApiCall(jsonStr);   // 성공하면 즉시 반환 → 반복 종료

			} catch (java.net.SocketTimeoutException e) {   // ① 시간 초과 (자식 먼저!)
				System.out.println("[ChatbotController] AI서비스 타임아웃 (시도 " + attempt + "/" + maxRetries + ")");
				if (attempt < maxRetries) {   // 다음 시도가 남아 있을 때만 쉰다
					try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
				}

			} catch (java.io.IOException e) {               // ② 네트워크 오류 (부모 나중!)
				System.out.println("[ChatbotController] AI서비스 네트워크 오류 (시도 " + attempt + "/" + maxRetries + ")");
				if (attempt < maxRetries) {
					try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
				}

			} catch (Exception e) {                          // ③ 그 밖의 오류 → 재시도 무의미, 즉시 포기
				System.out.println("[ChatbotController] AI서비스 오류: " + e.getMessage());
				return "AI 서비스 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
			}
		}

		return "AI 응답 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.";
		/* ↑ 2번 다 실패한 경우.
		   ★ callOpenRouterAPI() 와 달리 여기서는 lastException 을 보관하지 않아
		     타임아웃인지 네트워크 오류인지 구분 없이 한 문구로 안내한다.
		     → 일관성을 위해 저쪽처럼 원인별로 나누는 것이 더 낫다. (개선 여지) */
	}


	/* ═══════════════════════════════════════════════════════════════════════════
	   process() — 모든 요청이 들어오는 '정문'
	   ═══════════════════════════════════════════════════════════════════════════

	   [ 실행 순서 (전체 그림) ]
	     브라우저 요청
	        ↓
	     BaseController(부모)의 doGet / doPost 가 받음
	        ↓
	     BaseController 가 공통 처리(예외 처리 등)를 하면서
	        ↓
	     process(...) 를 호출   ← 지금 이 메소드 (자식이 구현)
	        ↓
	     doHandle(...) 로 넘김 → 주소별로 갈라짐
	   ═══════════════════════════════════════════════════════════════════════════ */
	/*
	  [ 왜 doGet / doPost 를 직접 만들지 않는가? — 이 프로젝트의 중요한 설계 ]

	    일반적인 서블릿은 doGet/doPost 를 직접 만든다.
	    그런데 컨트롤러가 10개면 같은 try-catch 를 10번 복사하게 된다.
	    하나라도 빠뜨리면 그 화면에서만 톰캣 에러페이지가 노출된다.

	    → 공통 처리를 BaseController 한 곳에 모으고,
	      각 컨트롤러는 '자기 일'인 process() 만 채우게 한 것이다.
	      (디자인 패턴 이름 : 템플릿 메소드 패턴)

	  [ 챗봇에서 이게 특히 중요한 이유 ]
	    챗봇은 AJAX(화면 새로고침 없이 데이터만 주고받는 방식) 요청이다.
	    여기서 에러가 나 톰캣 기본 HTML 에러페이지가 돌아오면
	    JS 는 그걸 그대로 말풍선에 그려버린다.
	    → 사용자 눈에 <html><body>HTTP Status 500... 이 통째로 찍힌다. 최악의 경험이다.

	    BaseController 는 X-Requested-With 헤더로 AJAX 인지 판단해서
	    HTML 대신 '메시지 문자열'만 돌려주도록 처리해 준다.

	  [ @Override 의 역할 ]
	    "부모가 정해 둔 메소드를 내가 구현한다" 는 표시.
	    메소드 이름이나 매개변수를 하나라도 틀리면 컴파일 에러로 즉시 잡아 준다.
	    이게 없으면 '부모 메소드를 덮어쓴 줄 알았는데 새 메소드를 만든' 상태가 되어
	    아무 일도 안 일어나는, 원인 찾기 가장 어려운 버그가 된다.
	 */
	@Override
	protected void process(String action, HttpServletRequest request, HttpServletResponse response) throws Exception {
	/*                     ▲
	                       └ 부모가 주소를 파싱해서 넘겨주는 값.
	                          ★ 이 메소드에서는 사용하지 않는다. (doHandle 이 자기가 다시 구한다)
	                            부모 규격상 받아야 해서 자리만 차지하고 있는 매개변수다. */

		doHandle(request, response);   // 실제 처리는 doHandle 이 담당. 이 메소드는 '전달'만 한다.
	}

}