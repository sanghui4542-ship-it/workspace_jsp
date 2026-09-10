package Controller;   // 이 파일이 속한 패키지(=폴더) 이름. 실제 폴더 구조와 반드시 같아야 한다

import java.io.BufferedReader;   // 글자를 줄 단위로 읽는 도구 (외부 API 응답을 읽을 때 사용)
import java.io.IOException;   // 파일/네트워크 입출력이 실패했을 때의 예외
import java.io.InputStream;   // 바이트를 읽어 들이는 통로
import java.io.InputStreamReader;   // 바이트 통로를 글자 통로로 바꿔 주는 도구
import java.io.PrintWriter;   // 응답 화면에 글자를 직접 찍어 보낼 때 쓰는 도구 (AJAX 응답에 사용)
import java.io.UnsupportedEncodingException;   // 지원하지 않는 문자 인코딩을 만났을 때의 예외
import java.net.HttpURLConnection;   // 자바가 직접 다른 서버에 요청을 보낼 때 쓰는 도구
import java.net.MalformedURLException;   // 주소 형식이 잘못됐을 때의 예외
import java.net.URL;   // 인터넷 주소 하나를 나타내는 객체
import java.net.URLEncoder;   // 한글·공백을 주소에 넣을 수 있는 형태로 바꿔 준다
import java.util.HashMap;   // Map 을 실제로 만들 때 쓰는 구현체
import java.util.List;   // 순서가 있는 목록 (게시글 5건이 순서대로 들어간다)
import java.util.Map;   // 이름표(키)로 값을 찾는 자료구조. 예) map.get("carno")

import javax.servlet.RequestDispatcher;   // 다른 JSP/서블릿으로 요청을 넘겨주는(forward) 도구
import javax.servlet.ServletConfig;   // 서블릿의 설정값을 읽는 객체
import javax.servlet.ServletException;   // 서블릿이 처리 중 실패했을 때의 예외
import javax.servlet.annotation.WebServlet;   // 이 서블릿이 어떤 주소의 요청을 받을지 정하는 표시 (아래 클래스 위에 붙어 있다)
import javax.servlet.http.HttpServletRequest;   // 브라우저가 보낸 요청(주소·파라미터·세션)이 담긴 객체
import javax.servlet.http.HttpServletResponse;   // 브라우저에게 돌려줄 응답을 담는 객체
import javax.servlet.http.HttpSession;   // 로그인 정보처럼 사용자별로 서버에 보관하는 저장소

import Service.CarService;   // 업무 규칙을 담당하는 Service 클래스
import Vo.CarConfirmVo;   // 값을 담아 나르는 상자(VO) 클래스
import Vo.CarListVo;   // 값을 담아 나르는 상자(VO) 클래스
import Vo.CarOrderVO;   // 값을 담아 나르는 상자(VO) 클래스
import util.ParamUtil;   // 여러 곳에서 함께 쓰는 도우미 클래스


//            /Car/CarInfo.do?carno=${vo.carno}

//            /Car/CarOption.do
@WebServlet("/Car/*")
public class CarController extends BaseController {

	/* developers.naver.com 에서 발급받은 본인 값으로 바꾸세요 */
	private static final String NAVER_CLIENT_ID = "여기에_본인_Client_ID";
	private static final String NAVER_CLIENT_SECRET = "여기에_본인_Client_Secret";   // 네이버 개발자센터에서 받은 본인 값으로 바꿔야 검색이 동작한다. 공개 저장소에 올리면 안 되는 값이다

	//직렬화 버전 번호 (HttpServlet이 Serializable을 구현하므로 경고 방지용으로 선언)
	private static final long serialVersionUID = 1L;

	private transient CarService carService;

	@Override   // 부모(HttpServlet)의 init 을 덮어쓴다는 표시
	public void init(ServletConfig config) throws ServletException {
		carService = new CarService();   // Service 를 하나 만들어 계속 재사용한다
	}

	//json-simple 의 JSONObject/JSONArray 는 제네릭이 없는 옛 컬렉션이라
	//put()/add() 를 쓰면 unchecked 경고가 난다. 라이브러리 특성이므로 경고만 끈다.
	@SuppressWarnings("unchecked")
	protected void doHandle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//재료 준비 
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=UTF-8");   // 응답이 HTML 이고 한글은 UTF-8 이라고 브라우저에 알린다
		response.setCharacterEncoding("UTF-8");   // 응답 글자를 UTF-8 로 내보낸다 (안 하면 한글이 깨진다)
		PrintWriter out = response.getWriter();   // 응답에 글자를 쓸 수 있는 붓을 얻는다
		 		
		//1. 클라이언트가 요청한 전체 URL  중에서 2단계 주소 얻기 
		String action = request.getPathInfo();
		      
		// "/Main"<- CarMain.jsp(VIEW) 메인화면 2단계 요청 주소 얻기
		// "/CarList.do" <- 전체 차량 검색  2단계 요청 주소 얻기 
		// "/carcategory.do" <- 차량 유형별 선택 후 검색 2단계 요청 주소 얻기
		// "/CarInfo.do"     <- 차량 한대 정보 검색 2단계 요청 주소 얻기
		// "/CarOption.do"   <- 차량 렌트 예약을 위해  옵션을 추가로 선택할수 있는 화면 2단계 요청 주소 얻기
		// "/CarOptionResult.do" <- 차량 렌트 예약을 위해 추가한 옵션금액 + 기본 금액 계산 2단계 요청 주소 얻기 	
		
		
		// "/bb" <- 예약하기 메뉴를 클릭 했을때  전체 검색 또는 카테고리별 검색 VIEW 화면 2단계 요청주소 얻기	
		
		// "/CarOrder.do" <-  비회원 결제후 예약 요청 2단계 주소 얻기 
		// "/cc"          <- 예약 확인 하기 위해 예약당시 입력 했던 비회원 핸드폰번호, 비밀번호를 입력하여 예약확인 요청하는 디자인 VIEW 2단계 요청주소얻기
		// "/delete.do"   <- 예약 취소를 위해 비밀번호를 입력해서 예위취소 요청하는 VIEW 중앙 화면 Delete.jsp보여줘~ 2단계 요청 주소 얻기
		// "/deletePro.do" <- 예약 취소 요청하는 2단계 요청 주소 얻기 
											
		System.out.println("요청한 2단계 주소:" + action);
		
		//2.포워딩(재요청)할  경로 주소를 저장할 변수 선언 후 저장 
		String nextPage = null;
		
		if(action.equals("/Main")) { //<- CarMain.jsp 메인화면 2단계 요청주소를 받으면?

			/*
			 [6단계 추가] 메인 화면에 "인기 차량"을 실제 사진으로 보여주기 위해 차량 목록을 조회한다.

			   기존에는 메인 화면(Center.jsp)에 그라디언트 배너와 버튼 하나만 있었다.
			   처음 방문한 사람은 "어떤 차가 있고 얼마인지"를 알 수 없어
			   메뉴를 눌러 들어가 봐야만 파악이 됐다.

			   이제 메인에서 바로 차량 사진과 가격을 보여준다.
			   (img 폴더에 이미 차량 사진 26장이 있는데 화면에서 쓰지 않고 있었다)
			*/
			List<CarListVo> mainCarList = carService.getAllCars();
			request.setAttribute("carList", mainCarList);   // 조회한 차량 목록을 요청에 담는다. Center.jsp 가 ${carList} 로 꺼내 카드를 그린다

			nextPage = "/CarMain.jsp";   // 틀이 되는 CarMain.jsp 로 넘긴다

		}else if(action.equals("/bb")) {//<- 예약하기 메뉴 링크 클릭시 보여질 중앙화면
			//   Car/bb?center=CarReservation.jsp
			
			//2.1. 중앙화면 요청한 파라미터 얻기
			String center = request.getParameter("center");
			
			//2.2. request 내장객체 메모리에 "CarReservation.jsp"중앙화면경로 바인딩
			//[보안] CenterView 허용 목록에 등록된 화면만 통과시킨다.
			//       (기존에는 ?center=WEB-INF/web.xml 로 서버 파일 내용이 화면에 노출됐다)
			request.setAttribute("center", center);
			
			//2.3. 메인 화면 CarMain.jsp를 포워딩 하기 위해 경로 저장
			nextPage = "/CarMain.jsp";
			
		}else if(action.equals("/CarList.do")) {//<- 전체 차량 검색 요청을 받았을때 
			
			//2.1. 전체 차량 검색을 부장(CarService)에게 시킨다
			//참고. 검색된 전체 차량 정보들은? MVC중에서 M인 Model이 된다.
			List<CarListVo> vector = carService.getAllCars();
			
			//2.2. View (CarList.jsp) 중앙화면에  검색된 전체 차량 정보를 보여주기 위해 
			//     request 내장객체에  Vector배열을 바인딩 
			request.setAttribute("v", vector);
			
			//2.2.1 View (CarList.jsp) 중앙화면 주소 또한
			//     request 내장객체에 바인딩 
			request.setAttribute("center", "CarList.jsp");
			
			//2.3. 메인 화면 CarMain.jsp를 포워딩 하기 위해 경로 저장
			nextPage = "/CarMain.jsp";
			
			
		}else if(action.equals("/carcategory.do")) {//소형, 중형, 대형 중 선택한 유형의 차량 검색 요청이 들어 오면
			
			//2.1. 클라이언트가 요청한 소형, 중형, 대형중  선택한 하나의 <option>의 value속성값 얻기 
			/*
			소형을 선택하고 검색요청 버튼을 클릭하면?
					/Car/carcategory.do?carcategory=Small
					 
				중형을 선택하고 검색요청 버튼을 클릭하면?
					/Car/carcategory.do?carcategory=Mid

				대형을 선택하고 검색요청 버튼을 클릭하면?
					/Car/carcategory.do?carcategory=Big
			*/
			String category = request.getParameter("carcategory");
			
			//2.1.1. 클라이언트가 선택한 유형의 차량 조회를 부장(CarService)에게 시킨다
			List<CarListVo> list = carService.getCarsByCategory( category );
			
			//2.2. View (CarList.jsp) 중앙화면에  검색된 전체 차량 정보를 보여주기 위해 
			//     request 내장객체에  ArrayList배열을 바인딩 
			request.setAttribute("v", list);
			
			//2.2.1 View (CarList.jsp) 중앙화면 주소 또한
			//     request 내장객체에 바인딩 
			request.setAttribute("center", "CarList.jsp");
			
			//2.3. 메인 화면 CarMain.jsp를 포워딩 하기 위해 경로 저장
			nextPage = "/CarMain.jsp";
			
			
		}else if(action.equals("/CarInfo.do")) {//렌트 하기 위한 차량을 보여주기 위해 차량 한대 검색요청을 받았을떄..
			
			//2.1. 검색시 사용할 차번호 얻기
			/*
			 [변경] Integer.parseInt(request.getParameter(...)) -> ParamUtil.getRequiredInt(...)

			   기존 코드는 주소창에서 carno 를 지우거나 문자를 넣으면
			   NumberFormatException 이 발생해 500 에러 페이지(스택트레이스)가 노출됐다.
			   ParamUtil 은 잘못된 입력을 400(잘못된 요청)으로 처리한다.
			*/
			int carno = ParamUtil.getRequiredInt(request, "carno");

			//2.1.1. 차량 한 대 정보 조회를 부장(CarService)에게 시킨다
			//       없는 차량번호면 CarService가 404 예외를 던진다 (기존에는 null이 JSP로 넘어가 NPE 500)
			CarListVo vo = carService.getCar(carno);
			
			//2.2. View(CarInfo.jsp) 중앙화면에 검색된 차량 한대 정보를 보여주기 위해
			//     request내장객체 메모리 영역에 CarListVo객체 하나를 바인딩 
			request.setAttribute("vo", vo);
			
			//2.2.1. View(CarInfo.jsp) 중앙화면 주소 또한~ request내장객체 메모리 영역에 바인딩
			request.setAttribute("center", "CarInfo.jsp");
			
			//2.3. 메인화면(CarMain.jsp)를 포워딩하기 위해 경로 저장
			nextPage = "/CarMain.jsp";
		
		}else if(action.equals("/CarOption.do")) {//추가로 옵션을 선택할수 있는 화면 요청을 받았을때

			request.setAttribute("priceInsurance", CarService.PRICE_INSURANCE);// 자차 보험 1일 요금 10000 request 에 바인딩 
			request.setAttribute("priceWifi",      CarService.PRICE_WIFI); // 무선 WIFI 1일 요금 5000 request 에 바인딩 
			request.setAttribute("priceNavi",      CarService.PRICE_NAVI); // 네비게이션 1일 요금 3000 request 에 바인딩 
			request.setAttribute("priceBabyseat",  CarService.PRICE_BABYSEAT);// 베이비시트 1일 요금 10000 request 에 바인딩 

			//View(CarOption.jsp) 추가로 옵션을 선택할수 있는 중앙화면 주소를
			//request 내장객체 메모리 영역에 바인딩
			request.setAttribute("center", "CarOption.jsp");

			//메인화면(CarMain.jsp)를 포워딩하기 위해 경로 저장
			nextPage = "/CarMain.jsp";

			
		}else if(action.equals("/CarOptionResult.do")) {   // 옵션 금액 계산 요청이면
						   // "/CarOptionResult.do" <- 차량 렌트 예약을 위해 추가한 옵션금액 + 기본 금액 계산 요청을 받았을때
			
			//2.1 차량 렌트 예약을 위해 선택했던 요청한 값들을 얻습니다.
			int carno = Integer.parseInt(request.getParameter("carno")); //차번호
			String carbegindate = request.getParameter("carbegindate");  //차 대여일
			int carqty = Integer.parseInt(request.getParameter("carqty")); //렌트할 차 수량
			/* [정리] carprice 파라미터를 더 이상 읽지 않는다.
			   금액은 아래에서 DB 요금으로 다시 계산하므로 화면이 보낸 가격은 쓸 일이 없다.
			   쓰지도 않을 값을 Integer.parseInt 하면, 값이 빠졌을 때 이유 없이 500 에러만 난다. */
			int carreserveday = Integer.parseInt(request.getParameter("carreserveday")); //차 대여 기간

			int carins = Integer.parseInt(request.getParameter("carins")); //보험 적용 여부   적용하면? 1  미적용하면 0
			int carwifi = Integer.parseInt(request.getParameter("carwifi")); //wifi옵션 적용 여부 적용하면? 1 미적용하면 0
			int carnave = Integer.parseInt(request.getParameter("carnave")); //네비게이션 옵션 적용 여부 적용하면? 1 미적용하면 0
			int carbabyseat = Integer.parseInt(request.getParameter("carbabyseat")); //베이비시트 옵션 적용 여부 적용하면? 1 미적용하면 0
			
		//2.2. 전체 렌트 가격을 계산 해서 클라이언트의 브라우저로 보여 줘야 합니다.
		//요약 : 응답할 데이터를 생성
			
			//2.2.1. 차량 기본 금액 계산 = 대여수량 * 차 한대당 렌트 가격 * 대여기간
			/*
			 [보안 수정] 금액을 화면이 보낸 값으로 계산하지 않는다.

			   기존 : carprice 를 hidden 파라미터로 받아 그대로 곱했다.
			          <input type="hidden" name="carprice" value="150000">
			          -> 개발자도구로 value 를 1 로 바꿔 보내면
			             제네시스를 1일 1원에 예약할 수 있었다.

			   지금 : 차량번호로 DB에서 1일 요금을 다시 조회해 계산한다.
			          "돈과 관련된 값은 절대 화면에서 받지 않는다"가 원칙이다.
			*/
			CarListVo priceCar = carService.getCar(carno); //DB에서 1일 요금 재조회

			//계산에 사용할 임시 VO (선택한 수량/기간/옵션을 담는다)
			CarOrderVO calcVo = new CarOrderVO();
			calcVo.setCarqty(carqty);   // 대여 수량
			calcVo.setCarreserveday(carreserveday);   // 대여 일수
			calcVo.setCarins(carins);   // 보험 선택 여부 (1 또는 0)
			calcVo.setCarwifi(carwifi);   // 와이파이 선택 여부
			calcVo.setCarnave(carnave);   // 네비게이션 선택 여부
			calcVo.setCarbabyseat(carbabyseat);   // 베이비시트 선택 여부

			//차량 기본 금액 = 1일 요금 x 대여수량 x 대여기간
			int totalreserve = carService.getBasePrice(priceCar.getCarprice(), calcVo);
			
			//추가한 옵션 금액 계산
			/*
			 [버그 수정] 옵션 금액 계산

			   기존 : (carins + carwifi + carbabyseat) * carreserveday * 10000 * carqty

			     버그1. carnave(네비게이션)가 빠져 있다 -> 선택해도 요금이 0원이었다
			     버그2. 옵션 단가를 모두 10,000원으로 계산했다
			            AI 챗봇 안내는 보험 10,000 / WiFi 5,000 / 네비 3,000 / 시트 10,000 이라
			            안내 금액과 실제 결제 금액이 달랐다 (WiFi는 5,000원 과다 청구)

			   지금 : CarService 의 옵션 단가 상수를 사용한다.
			          챗봇 프롬프트도 같은 상수를 참조하므로 안내와 결제가 항상 일치한다.
			*/
			int totalOption = carService.getOptionPrice(calcVo);
			
			//2.2.3. 응답할 VIEW(CarOrder.jsp) 중앙 화면에 보여주기 위해 CarOrderVO객체를 생성해서 각 인스턴스변수에 저장 시킴
			CarOrderVO carordervo = new CarOrderVO();
					   carordervo.setCarno(carno); //예약할 차번호
					   carordervo.setCarqty(carqty);//예약할 차량 수량
					   carordervo.setCarreserveday(carreserveday);//예약할 차량 대여기간
					   carordervo.setCarbegindate(carbegindate);//예약 날짜
					   carordervo.setCarins(carins);//보험적용 여부값 1 또는 0
					   carordervo.setCarwifi(carwifi);//무선 WIFI 옵션 적용 여부값 1 또는 0
					   carordervo.setCarnave(carnave);//네비게이션 옵션 적용 여부값 1 또는 0
					   carordervo.setCarbabyseat(carbabyseat);//베이비시트 옵션 적용 여부값 1또는 0
					   
		//3. 웹브라우저로 응답할(VIEW)-> CarOrder.jsp 중앙화면에 보여주기 위해
		//   request 내장객체 메모리에 바인딩
			 request.setAttribute("vo", carordervo);//예약시 선택했던 예약 정보 
			 request.setAttribute("totalreserve", totalreserve);//기본 총금액 
			 request.setAttribute("totaloption", totalOption);//추가한 옵션 총금액 
			
		//4. 비회원 또는 회원 으로 예약 해서 다른 VIEW를 보여주기 위해 코드 작성
			 //4.1. HttpSession 객체 메모리 영역 새로 얻기
			 HttpSession session = request.getSession();
			 
			 //4.2. HttpSession 객체 메모리 영역에 로그인 또는 미로그인 한 사람의 정보를 얻기 
			 String id = (String)session.getAttribute("id");

			 //4.3. 로그인이 안된 (미로그인 된) 비회원의 총 결제 금액을 보여주기 위해  VIEW 경로를 CarOrder.jsp로 request에 바인딩
			 if(id == null) {
				 request.setAttribute("center", "CarOrder.jsp");
			 
			 }else {//4.4. 로그인된 회원으로 예약시 총 결제 금액을 보여주기 위해 VIEW 경로를 LoginCarOrder.jsp로 request에 바인딩
				 request.setAttribute("center", "LoginCarOrder.jsp");   // 가운데에 로그인 회원용 예약 화면을 끼우라고 알린다
			 }
		//5.  메인화면(CarMain.jsp)를 포워딩 하기 위해 경로 저장
			 nextPage = "/CarMain.jsp";
			
			 
		}else if(action.equals("/CarOrder.do")) {//결제 후 예약 요청을 받았을때....
			
			//2.1. 렌트 예약을 위해 선택했던 예약  정보 10개 중 8개 모두 얻어 저장
			int carno = Integer.parseInt(request.getParameter("carno")); //차번호
			String carbegindate = request.getParameter("carbegindate");  //차 대여일
			int carqty = Integer.parseInt(request.getParameter("carqty")); //렌트할 차 수량 
			int carreserveday = Integer.parseInt(request.getParameter("carreserveday")); //차 대여 기간

			int carins = Integer.parseInt(request.getParameter("carins")); //보험 적용 여부   적용하면? 1  미적용하면 0
			int carwifi = Integer.parseInt(request.getParameter("carwifi")); //wifi옵션 적용 여부 적용하면? 1 미적용하면 0
			int carnave = Integer.parseInt(request.getParameter("carnave")); //네비게이션 옵션 적용 여부 적용하면? 1 미적용하면 0
			int carbabyseat = Integer.parseInt(request.getParameter("carbabyseat")); //베이비시트 옵션 적용 여부 적용하면? 1 미적용하면 0
			
			//2.2.   2.1.에서 얻은 예약시 선택했던 예약 정보들을 CarOrderVo객체를 생성해서 각 인스턴스변수에 저장
			CarOrderVO carordervo = new CarOrderVO();
					   carordervo.setCarno(carno); //예약할 차번호
					   carordervo.setCarqty(carqty);//예약할 차량 수량
					   carordervo.setCarreserveday(carreserveday);//예약할 차량 대여기간
					   carordervo.setCarbegindate(carbegindate);//예약 날짜
					   carordervo.setCarins(carins);//보험적용 여부값 1 또는 0
					   carordervo.setCarwifi(carwifi);//무선 WIFI 옵션 적용 여부값 1 또는 0
					   carordervo.setCarnave(carnave);//네비게이션 옵션 적용 여부값 1 또는 0
					   carordervo.setCarbabyseat(carbabyseat);//베이비시트 옵션 적용 여부값 1또는 0
			
			//2.3. 로그인  후 예약 요청을 했는지  미로그인 상태에서 비회원 예약 요청 했는지 판단하기 위해 HttpSession 메모리에 저장된 값으로 판단
			HttpSession session = request.getSession();			
			String id = (String)session.getAttribute("id");   // 세션에서 로그인한 아이디를 꺼낸다. null 이면 비회원이다
				
			if(id == null) {//미로그인 상태에서 비회원 예약 요청 했을떄~
				
				//비회원으로 예약시 입력했던 핸드폰번호와 비밀번호 얻기 
				String memberphone = request.getParameter("memberphone");
				String memberpass = request.getParameter("memberpass");   // 예약 확인에 쓸 비밀번호
				
				//CarOrderVO객체에 추가로 2개의 정보 저장 
				carordervo.setMemberphone(memberphone);
				carordervo.setMemberpass(memberpass);   // 예약 확인용 비밀번호를 상자에 담는다
				
			}else {//로그인 후 회원 예약요청 했을떄~

				//LoginCarOrder.jsp에서 전달한 회원 아이디, 예약확인용 전화번호와 비밀번호를 얻기
				String memberid = request.getParameter("memberid");
				String memberphone = request.getParameter("memberphone");   // 예약 확인에 쓸 연락처
				String memberpass = request.getParameter("memberpass");   // 예약 확인에 쓸 비밀번호

				//아이디가 전달되지 않았다면 세션에 저장된 로그인 아이디를 사용
				if(memberid == null || memberid.trim().isEmpty()) {
					memberid = id;
				}

				//CarOrderVO객체에 정보 저장
				carordervo.setId(memberid);
				/*
				 회원 예약도 non_carorder테이블에 memberphone/memberpass로 저장되고
				 예약 확인(CarReserveConfirm.jsp)도 이 두 값으로 조회하므로
				 전화번호를 반드시 함께 저장해야 나중에 예약 조회가 가능하다.
				*/
				carordervo.setMemberphone(memberphone);
				carordervo.setMemberpass(memberpass);   // 예약 확인용 비밀번호를 상자에 담는다

			}//else
			
			//3.1. 예약시 선택했던 정보를  데이터베이스의 non_carorder테이블에 추가 하기 위해  CarDAO객체의 insertCarOrder메소드 호출해서 명령!
			//참고. insertCarOrder메소드 호출시! 매개변수로  CarOderVo객체 전달, 매개변수로 HttpSession객체 전달
			/*
			 [변경] 예약 등록을 부장(CarService)에게 시킨다.

			   기존 : cardao.insertCarOrder(carordervo, session);
			     - 반환형이 void 라서 예약 실패를 알 수 없었다
			       -> INSERT가 실패해도 화면에는 "예약되었습니다"가 떴다
			     - session 을 넘겨받지만 DAO 안에서 쓰지 않았다
			     - 결제 금액을 저장하지 않았다

			   지금 : Service가 차량 요금을 DB에서 조회해 총액을 계산하고 함께 저장하며,
			          성공 여부를 돌려준다.
			*/
			boolean ordered = carService.createOrder(carordervo);

			if(!ordered) {   // 저장에 실패했으면
				out.print("<script>");   // 브라우저가 실행할 스크립트를 만들어 보낸다
				out.print(" window.alert('예약 처리에 실패했습니다. 다시 시도해주세요.');");   // 실패를 알린다
				out.print(" history.back();");   // 이전 화면으로 되돌린다 (입력한 값이 살아 있다)
				out.print("</script>");   // 스크립트를 닫는다
				return;   // 여기서 끝낸다
			}
			
			//4. 예약에 성공 했으면? 클라이언트의 브라우저로 예약 성공! 출력! 
			//   그리고 모든 차량 검색요청을 하기 위해 CarController서블릿으로  재요청(포워딩) -> URL: /Car/CarList.do
			out.print("<script>");
			out.print(" window.alert('예약되었습니다.');");   // 예약이 되었다고 알린다
			out.print(" location.href='" + request.getContextPath()  +  "/Car/CarList.do'");   // 차량 목록 화면으로 이동시킨다
			out.print("</script>");   // 스크립트를 닫는다
			
			return; //doHandle 메소드 종료 
		
		
		}else if(action.equals("/cc")) {// 예약 확인을 요청하는 디자인 중앙 VIEW 요청을 받았을떄..
			//URL ->  /Car/cc?center=CarReserveConfirm.jsp

			//2.1. 요청한 중앙 VIEW 경로 얻기
			String center = request.getParameter("center");
			//     center = "CarReserveConfirm.jsp";

			//2.2. request내장객체 메모리에 요청한 중앙 VIEW "CarReserveConfirm.jsp" 경로 바인딩
			//[보안] CenterView 허용 목록에 등록된 화면만 통과시킨다.
			//       (기존에는 ?center=WEB-INF/web.xml 로 서버 파일 내용이 화면에 노출됐다)
			request.setAttribute("center", center);

			//3. 메인 화면 포워딩 할 주소 저장
			nextPage = "/CarMain.jsp";

		}else if(action.equals("/ai")) {// AI 추천 서비스 중앙 VIEW 요청
			//URL ->  /Car/ai?center=AIService.jsp

			//2.1. 요청한 중앙 VIEW 경로 얻기
			String center = request.getParameter("center");

			//2.2. AI 비용 계산기에서 차량 목록 드롭다운을 보여주기 위해 DB에서 전체 차량 조회
			List<CarListVo> carList = carService.getAllCars();
			request.setAttribute("carList", carList);   // 조회한 차량 목록을 요청에 담는다

			/*
			 [4단계 원칙 적용] 옵션 단가를 화면에 하드코딩하지 않고 서버 상수를 내려준다.

			   기존 AIService.jsp 는 화면 안에 금액을 직접 적어두고 있었다.
			       <input type="checkbox" value="자차보험(10,000원/일)">

			   문제는 이 값이 실제 결제 금액과 따로 관리된다는 점이다.
			   요금을 바꾸면 CarService.PRICE_* 만 고치고 이 화면은 잊어버린다.
			   그러면 AI 는 옛 요금으로 안내하고, 결제는 새 요금으로 이뤄진다.
			   (실제로 예약 화면에서 WiFi 를 2배로 표시하고 네비게이션을 "무료"로
			    안내하면서 3,000원을 받던 버그가 있었다)

			   요금의 출처는 CarService.PRICE_* 한 곳뿐이어야 한다.
			*/
			request.setAttribute("priceInsurance", CarService.PRICE_INSURANCE);
			request.setAttribute("priceWifi",      CarService.PRICE_WIFI);   // WiFi 요금도 담는다
			request.setAttribute("priceNavi",      CarService.PRICE_NAVI);   // 네비게이션 요금도 담는다
			request.setAttribute("priceBabyseat",  CarService.PRICE_BABYSEAT);   // 베이비시트 요금도 담는다

			//2.3. request내장객체 메모리에 중앙 VIEW 경로 바인딩
			//[보안] CenterView 허용 목록에 등록된 화면만 통과시킨다.
			//       (기존에는 ?center=WEB-INF/web.xml 로 서버 파일 내용이 화면에 노출됐다)
			request.setAttribute("center", center);

			//3. 메인 화면 포워딩 할 주소 저장
			nextPage = "/CarMain.jsp";

		}else if(action.equals("/orderListJson.do")) {   // 챗봇이 부르는 "내 예약 목록" 요청이면
			/*
			 ============================================================================
			   [신규] 챗봇 예약 조회 (JSON 응답)

			   화면용 조회(/CarReserveConfirm.do)와 "같은 Service 메소드"를 부른다.
			   다른 것은 응답 형식뿐이다.

			       /CarReserveConfirm.do  -> HTML 화면 (CarReserveResult.jsp)
			       /orderListJson.do      -> JSON     (챗봇이 말풍선으로 그린다)

			   [왜 로직을 복사하지 않는가]
			     비밀번호 검증·해시 이관·조회 조건이 두 벌이 되면
			     한쪽만 고쳐서 "화면에서는 되는데 챗봇에서는 안 되는" 상태가 된다.
			     응답 형식만 바꾸고 업무 규칙은 반드시 한 곳(Service)에 둔다.

			   [보안] 비밀번호는 챗봇 대화 내용(message)으로 받지 않는다.
			          챗봇 화면의 별도 입력칸(type="password")에서 이 주소로 직접 보낸다.
			          그래야 비밀번호가 대화 기록에 남지 않고, AI 에게도 전달되지 않는다.
			 ============================================================================
			*/
			response.setContentType("application/json;charset=UTF-8");

			String phone = request.getParameter("memberphone");   // 예약할 때 넣은 연락처
			String pass  = request.getParameter("memberpass");   // 예약할 때 넣은 비밀번호

			if (phone == null || phone.trim().isEmpty() || pass == null || pass.isEmpty()) {   // 둘 중 하나라도 비어 있으면 조회할 수 없다
				out.write("{\"ok\":false,\"message\":\"연락처와 예약 비밀번호를 모두 입력해주세요.\"}");   // 이유를 JSON 으로 알려 준다
				return;   // 여기서 끝낸다
			}

			List<CarConfirmVo> myOrders = carService.findOrders(phone.trim(), pass);   // 연락처와 비밀번호가 맞는 예약만 받아 온다

			//JSON 조립 (json-simple 로 만들면 따옴표·특수문자 이스케이프를 알아서 해준다)
			org.json.simple.JSONArray arr = new org.json.simple.JSONArray();

			for (CarConfirmVo vo : myOrders) {   // 예약을 하나씩 JSON 으로 옮긴다

				//옵션 1일 합계와 총액을 서버가 계산한다 (화면에서 다시 계산하지 않는다)
				CarOrderVO calc = new CarOrderVO();
				calc.setCarqty(vo.getCarqty());   // 대여 수량
				calc.setCarreserveday(vo.getCarreserveday());   // 대여 일수
				calc.setCarins(vo.getCarins());   // 보험 선택 여부
				calc.setCarwifi(vo.getCarwifi());   // 와이파이 선택 여부
				calc.setCarnave(vo.getCarnave());   // 네비게이션 선택 여부
				calc.setCarbabyseat(vo.getCarbabyseat());   // 베이비시트 선택 여부

				int total = carService.getTotalPrice(vo.getCarprice(), calc);   // 1일 요금과 선택 옵션으로 총액을 다시 계산한다

				//선택한 옵션 이름만 모은다
				StringBuilder opt = new StringBuilder();
				if (vo.getCarins() == 1)      opt.append("자차보험 ");   // 보험을 골랐으면 이름을 붙인다
				if (vo.getCarwifi() == 1)     opt.append("WiFi ");   // 와이파이를 골랐으면 이름을 붙인다
				if (vo.getCarnave() == 1)     opt.append("네비게이션 ");   // 네비게이션을 골랐으면 이름을 붙인다
				if (vo.getCarbabyseat() == 1) opt.append("베이비시트 ");   // 베이비시트를 골랐으면 이름을 붙인다

				org.json.simple.JSONObject item = new org.json.simple.JSONObject();   // 예약 하나를 담을 JSON 객체를 만든다
				item.put("orderid",   vo.getOrderid());   // 예약번호 (취소할 때 쓴다)
				item.put("carname",   vo.getCarname());   // 차량명
				item.put("carimg",    vo.getCarimg());   // 차량 사진 파일명
				item.put("begindate", vo.getCarbegindate());   // 대여 시작일
				item.put("days",      vo.getCarreserveday());   // 대여 일수
				item.put("qty",       vo.getCarqty());   // 대여 수량
				item.put("total",     total);   // 위에서 계산한 총액
				item.put("options",   opt.length() == 0 ? "없음" : opt.toString().trim());   // 고른 옵션이 없으면 "없음" 이라고 적는다
				arr.add(item);   // 완성된 예약 하나를 목록에 담는다
			}

			org.json.simple.JSONObject result = new org.json.simple.JSONObject();   // 화면에 돌려줄 최종 JSON 객체를 만든다
			result.put("ok", Boolean.TRUE);   // 성공했다는 표시
			result.put("orders", arr);   // 위에서 만든 예약 목록
			/*
			 [보안] 조회 결과가 0건일 때 "연락처가 없다 / 비밀번호가 틀리다" 를 구분해 알려주지 않는다.
			        구분해 주면 공격자가 "이 번호는 가입되어 있다"는 사실을 알아낼 수 있다.
			        (계정 존재 여부 노출 - account enumeration)
			*/
			result.put("message", myOrders.isEmpty()
					? "조회된 예약이 없습니다. 연락처와 비밀번호를 확인해주세요."
					: "예약 " + myOrders.size() + "건을 찾았습니다.");
			out.write(result.toJSONString());   // JSON 을 글자로 바꿔 응답에 쓴다
			return;   // 여기서 끝낸다

		}else if(action.equals("/orderCancelJson.do")) {   // 챗봇이 부르는 "예약 취소" 요청이면
			/*
			 [신규] 챗봇 예약 취소 (JSON 응답)

			   화면용 취소(/deletePro.do)와 같은 Service 메소드를 부른다.
			   비밀번호 검증은 CarService.deleteOrder 안에서 이뤄지므로
			   여기서 따로 검사하지 않는다(검증 로직이 두 벌이 되지 않게).

			   취소는 되돌릴 수 없는 동작이라 POST 로만 받고 CSRF 토큰을 요구한다.
			   (CsrfFilter 가 이 주소에도 적용된다)
			*/
			response.setContentType("application/json;charset=UTF-8");

			int cancelId;   // 취소할 예약번호를 담을 변수
			try {
				cancelId = Integer.parseInt(request.getParameter("orderid"));   // 글자로 온 예약번호를 숫자로 바꾼다
			} catch (Exception e) {
				out.write("{\"ok\":false,\"message\":\"예약번호가 올바르지 않습니다.\"}");   // 숫자가 아니면 이유를 JSON 으로 알려 준다
				return;   // 여기서 끝낸다
			}

			String cancelPass = request.getParameter("memberpass");   // 본인 확인용 비밀번호
			if (cancelPass == null || cancelPass.isEmpty()) {   // 비밀번호가 없으면 취소할 수 없다
				out.write("{\"ok\":false,\"message\":\"예약 비밀번호를 입력해주세요.\"}");   // 이유를 JSON 으로 알려 준다
				return;   // 여기서 끝낸다
			}

			int canceled = carService.deleteOrder(cancelId, cancelPass);   // 비밀번호 검증까지 Service 가 하고 지워진 행 수를 돌려준다

			if (canceled == 1) {   // 1건이 지워졌으면 = 취소 성공
				out.write("{\"ok\":true,\"message\":\"예약이 취소되었습니다.\"}");   // 성공을 JSON 으로 알려 준다
			} else {
				out.write("{\"ok\":false,\"message\":\"취소하지 못했습니다. 비밀번호를 확인해주세요.\"}");   // 실패 이유를 JSON 으로 알려 준다 (비밀번호가 틀렸을 가능성이 높다)
			}
			return;   // 여기서 끝낸다

		}else if(action.equals("/CarReserveConfirm.do")) {//입력한 핸드폰번호와 비밀번호로 예약한 정보 조회 요청!(예약확인을 위함)
		
			//2.1. 요청한 데이터 얻기 
			//     (입력한 핸드폰번호와 비밀번호 얻기)
			String memberphone = request.getParameter("memberphone");
			String memberpass  = request.getParameter("memberpass");   // 예약 확인에 쓸 비밀번호
			
			//2.2. 입력한 핸드폰번호와 비밀번호를 이용해 예약한 정보들을 조회 하기 위해 CarDAO객체의 getAllCarOrder메소드 호출해 명령!
			//참고.  getAllCarOrder메소드를 호출할때 매개변수로 각각 입력한 핸드폰번호와 비밀번호 전달 !
			//참고2. getAllCarOrder메소드 내부에서  조회된 예약 정보들을 Vector배열에 담아 Vector배열 자체를 반환 해줍니다!
			List<CarConfirmVo> vector = carService.findOrders(memberphone, memberpass);
			
			//2.2.3. 조회된 예약정보들을 중앙 VIEW(CarReserveResult.jsp)에 보여주기 위해
			//       먼저~  request 내장객체 메모리에  Vector배열 자체를 바인딩
			request.setAttribute("v", vector);
			
			//2.2.4. 조회된 예약정보들을 중앙 VIEW(CarReserveResult.jsp)에 보여주기 위해 
			//       request 내장객체 멤보리에 VIEW주소 바인딩
			request.setAttribute("center", "CarReserveResult.jsp");
			
			//2.2.5. 중앙 VIEW페이지에 보여질 예약 수정 <a>, 예약삭제 <a>에 설정을 위해 입력한 휴대폰 번호와 비밀번호 함께 request내장객체 메모리에 바인딩
			request.setAttribute("memberphone", memberphone);
			/* [보안] 입력받은 예약 비밀번호를 request 에 담지 않는다.
			   기존에는 화면(CarReserveResult.jsp)이 이 값을 hidden 필드에 심어
			   "페이지 소스 보기"로 평문 비밀번호가 보였다.
			   수정/취소 화면에서 사용자가 직접 다시 입력하므로 넘길 필요가 없다. */		
			//3. 메인 화면 포워딩 할 주소 저장
			nextPage = "/CarMain.jsp";
				
		}else if(action.equals("/update.do")) {//예약 정보를 수정하기 위해 예약한 아이디로 예약정보 조회 요청을 받았을때...
			/*
			 CarReserveResult.jsp 의 "예약 수정" 버튼으로 요청이 들어온다.

			   (기존 코드 - GET 링크)
			       <a href="${contextPath}/Car/update.do"
			               + "?orderid=${carConfirmVo.orderid}"
			               + "&carimg=${carConfirmVo.carimg}"
			               + "&memberpass=${requestScope.memberpass}"     <- 비밀번호가 주소창에!
			               + "&memberphone=${requestScope.memberphone}">예약수정</a>

			   (지금 - POST 폼)
			       <form action="${contextPath}/Car/update.do" method="post">
			           <input type="hidden" name="orderid" ...>
			           <input type="hidden" name="carimg"  ...>
			       </form>

			   비밀번호는 아예 보내지 않는다.
			   수정 화면(CarConfirmUpdate.jsp)에서 사용자가 직접 다시 입력한다.
			   연락처도 예약번호로 DB에서 다시 조회하므로 넘겨받을 필요가 없다.
			*/
			//2.1. 요청한 데이터 얻기 (예약번호, 차량 이미지명 두 개면 충분하다)
			int orderid = Integer.parseInt(request.getParameter("orderid"));//예약 아이디
			String carimg = request.getParameter("carimg"); //예약한 차량 이미지명

			//2.2. 예약 아이디를 이용해 예약한 정보 한쌍을 DB에서 조회 하기 위해 
			//     CarDAO객체의 getOneOrder메소드를 호출할때.. 매개변수로 orderid예약 아이디 전달해서 조회해 옵니다.
			//없는 예약번호면 CarService가 404 예외를 던진다 (기존에는 null -> NPE 500)
			CarConfirmVo carConfirmVo = carService.findOrder(orderid);
						 carConfirmVo.setCarimg(carimg); //추가로  예약한 차량 이미지명 저장 
				
			//2.3. 예약 아이디를 이요해 조회된 예약 정보를 보여줄 중앙화면 VIEW("CarConfirmUpdate.jsp")주소 request에 바인딩
			request.setAttribute("center", "CarConfirmUpdate.jsp");
			
			//2.4.    2.2.에서 조회된 예약한 정보(CarConfirmVo)를 중앙 VIEW에 보여주기 위해 request에 바인딩
			request.setAttribute("vo", carConfirmVo);
			
			//2.5. 메인 화면 포워딩 할 주소 저장
			nextPage = "/CarMain.jsp";	
			
			
		}else if(action.equals("/updatePro.do")) { //입력한 정보를 DB의 테이블에 수정(UPDATE)해 주세요 요청을 받았을때
			
			System.out.println( request.getParameter("carins")  + "-------------------");   // 보험 값이 무엇으로 들어왔는지 콘솔에 찍는다 (수업 중 확인용)
			
			//2.1. 수정을 위해 입력한 정보들은  request내장객체 메모리 영역에 저장되어 있으므로
			//     DB의 non_carorder테이블의 컬럼 정보를 update수정하기 위해 
			//     CarDAO객체의 carOrderUpdate메소드를 호출 해 수정 명령 합니다.
			/*
			 [계층 분리] DAO에 request 를 그대로 넘기던 것을 고쳤다.

			   기존 : cardao.carOrderUpdate(request);
			          -> DAO 안에서 request.getParameter(...) 와 Integer.parseInt(...) 를 했다.
			             DAO가 HTTP를 알게 되어 재사용이 불가능하고,
			             잘못된 입력이 SQL 오류로 나타났다.

			   지금 : 컨트롤러가 값을 꺼내 검증하고 VO로 만들어 Service에 넘긴다.
			          ParamUtil 이 형식과 범위(수량 1~5, 기간 1~30)를 서버에서 강제한다.
			          화면의 select 태그만 믿으면 주소창으로 얼마든지 바꿔 보낼 수 있다.
			*/
			CarOrderVO updateVo = new CarOrderVO();
			updateVo.setOrderid(ParamUtil.getRequiredInt(request, "orderid"));   // 어느 예약을 고칠지
			updateVo.setCarbegindate(ParamUtil.getRequiredDate(request, "carbegindate"));   // 바뀐 시작일. 형식(yyyy-MM-dd)과 실제 존재하는 날짜인지까지 검사한다
			updateVo.setCarreserveday(ParamUtil.getRequiredInt(request, "carreserveday", 1, 30));   // 바뀐 대여 일수. 1~30 범위를 서버에서 강제한다
			updateVo.setCarqty(ParamUtil.getRequiredInt(request, "carqty", 1, 5));   // 바뀐 대여 수량. 1~5 범위를 서버에서 강제한다
			updateVo.setCarins(ParamUtil.getFlag(request, "carins"));   // 보험 선택 여부. 0 또는 1 만 통과시킨다
			updateVo.setCarwifi(ParamUtil.getFlag(request, "carwifi"));   // 와이파이 선택 여부
			updateVo.setCarnave(ParamUtil.getFlag(request, "carnave"));   // 네비게이션 선택 여부
			updateVo.setCarbabyseat(ParamUtil.getFlag(request, "carbabyseat"));   // 베이비시트 선택 여부
			updateVo.setMemberpass(ParamUtil.getRequiredString(request, "memberpass"));   // 본인 확인용 비밀번호. 없으면 여기서 400 예외가 난다

			int result = carService.updateOrder(updateVo);   // 검증과 재계산까지 Service 가 하고 바뀐 행 수를 돌려준다
			
			String memberphone = request.getParameter("memberphone");//예약시 입력했던 비회원 휴대폰번호
			
			//2.2. UPDATE 예약정보 수정에 성공하면 
			//     -> "예약정보 수정 성공" 메세지를 브라우저로 띄워 보여주고
			//     -> "예약정보 수정하기 위해 요청하는 디자인 화면 "-> /Car/update.do 포워딩 해서 보여지게 하기 
			//	   -> return; <-을 작성해서  doHandle메소드 종료 
			if(result == 1) {
				/*
				 ============================================================================
				   [수정 2가지]

				   (기존 코드)
				       location.href='/Car/update.do?orderid=3&carimg=k5.jpg&memberphone=010-...'

				   문제1. 연락처가 주소(URL)에 실려 나갔다.
				          주소창에 담긴 값은 브라우저 기록과 서버 접속 로그에 남는다.
				          개인정보를 이런 곳에 남기지 않는다.

				   문제2. 수정에 성공했는데 "수정 화면으로 다시" 보냈다.
				          사용자는 방금 저장한 화면을 또 보게 되어
				          "저장이 된 것인지" 확신할 수 없었다.

				   (지금)
				     수정된 결과가 반영된 예약 목록으로 보내고, 완료 메시지를 함께 보여준다.
				     주소로 다시 요청하지 않고 서버 안에서 조회 후 포워딩하므로
				     비밀번호와 연락처가 주소창에 나타나지 않는다.
				 ============================================================================
				*/
				String updatedPass = updateVo.getMemberpass();

				List<CarConfirmVo> updatedOrders = carService.findOrders(memberphone, updatedPass);   // 수정된 결과가 반영된 예약 목록을 다시 조회한다

				request.setAttribute("v", updatedOrders);   // 조회한 목록을 요청에 담는다. 화면이 ${v} 로 꺼낸다
				request.setAttribute("memberphone", memberphone);   // 연락처도 함께 전달한다 (화면에서 다시 쓸 수 있게)
				request.setAttribute("center", "CarReserveResult.jsp");   // 가운데에 예약 결과 화면을 끼우라고 알린다
				request.setAttribute("flashMessage", "예약 정보가 수정되었습니다.");   // 완료 안내 문구도 함께 전달한다

				request.getRequestDispatcher("/CarMain.jsp").forward(request, response);   // 틀이 되는 CarMain.jsp 로 넘긴다. 주소창에 비밀번호가 남지 않는다
				return;   // 여기서 끝낸다

			//     UPDATE 예약정보 수정에 실패하면 
			//	   -> "예약정보 수정 실패" 메세지를 브라우저로 띄워 보여주고
			//	   -> history객체의 back(); 메소드를 호출해서  이전 페이지로 되돌아가 보여지게 하기 
			//     -> return; <-을 작성해서 doHandle메소드 종료 	
			}else {
				out.print("<script>");   // 브라우저가 실행할 스크립트를 만들어 보낸다
				out.print(" alert('예약 정보 수정 실패');");   // 실패를 알린다
				out.print(" history.back();");   // 이전 화면으로 되돌린다
				out.print("</script>");   // 스크립트를 닫는다
				return;   // 여기서 끝낸다
			}
			
		}else if(action.equals("/delete.do")) {//예약 취소(삭제)를 위해 비밀번호를 입력하여 취소 요청하는 중앙 VIEW화면 요청을 받았을떄
		/*
		 CarReserveResult.jsp 의 "예약 취소" 버튼으로 요청이 들어온다.

		   (기존 코드 - GET 링크)
		       <a href="${contextPath}/Car/delete.do?"
		               + "orderid=${carConfirmVo.orderid}"
		               + "&memberphone=${requestScope.memberphone}"
		               + "&center=Delete.jsp">예약취소</a>

		   (지금 - POST 폼)
		       <form action="${contextPath}/Car/delete.do" method="post"> ... </form>

		   취소는 "되돌릴 수 없는 동작"이다.
		   GET 링크는 브라우저나 크롤러가 미리 열어보기(prefetch)만 해도 실행될 수 있으므로
		   삭제·취소 같은 동작은 반드시 POST 로 받는다.
		*/
			//2.1. 요청한 데이터 얻기( 비밀번호 입력해서 예약취소 요청하는 중앙 VIEW "Delete.jsp" 경로 얻기)
			String center = request.getParameter("center"); //"Delete.jsp"
			
			//2.2. request내장객체 메모리에 중앙 VIEW "Delete.jsp"경로 바인딩
			//[보안] CenterView 허용 목록에 등록된 화면만 통과시킨다.
			//       (기존에는 ?center=WEB-INF/web.xml 로 서버 파일 내용이 화면에 노출됐다)
			request.setAttribute("center", center);
		//	request.setAttribute("center", "Delete.jsp");
			
			//2.3. nextPage변수에 CarMain.jsp 메인 화면 요청 포워딩 주소 저장
			nextPage = "/CarMain.jsp";
		
		
		}else if(action.equals("/deletePro.do")) {//예약 취소 요청을 받았다면
			
			//2.1. 요청한 데이터 얻기
			//     1. 예약 취소시 사용할 예약한 아이디,
			//	   2. 예약 취소를 위해 입력한 비밀번호,
			//     3. 예약 취소시 사용할 예약자의 휴대폰번호 
			int orderid = Integer.parseInt(request.getParameter("orderid"));
			String memberpass = request.getParameter("memberpass");   // 본인 확인용 비밀번호
			String memberphone = request.getParameter("memberphone");   // 예약할 때 넣은 연락처 (취소 후 목록을 다시 조회하는 데 쓴다)

			//2.2. 응답할 값 마련
			//예약 정보를 삭제(취소)하기 위해 CarDAO객체의 OrderDelete메소드를 호출할떄
			//매개변수로 삭제(취소)할 예약아이디와 입력한 비밀번호를 전달하여 DB의 non_order테이블에서 삭제(delete) 시키자.
			//삭제(delete)에 성공하면  OrderDelete메소드의 반환값은  삭제에 성공한 레코드 개수 1을 반환받고 , 삭제(delete)에 실패하면 0을 반환 받습니다.
			int result = carService.deleteOrder(orderid, memberpass);
			
			//2.3. 예약 정보 삭제(취소)에 성공하면?  
			//     1. "예약 정보가 취소 되었습니다" <- 클라이언트가 요청한 브라우저 화면에 메세지를 보여주고
			//     2. "비회원 휴대번호와 비밀번호로 예약한 정보를 모두 보여주기 위해   모든 예약 정보 조회할수 있도록 포워딩"
			//     3.  디스패처 방식으로 포워딩 되는 코드의 실행을 맊기 위해  doHandle 메소드 종료  <- return;
			if(result == 1) {
				/*
				 ============================================================================
				   [보안 수정] 비밀번호를 URL 로 다시 보내지 않는다.

				   (기존 코드)
				       location.href='/Car/CarReserveConfirm.do?memberphone=010-...&memberpass=1234'

				     예약 취소에 성공한 뒤, 목록을 다시 보여주려고 연락처와 비밀번호를
				     주소(GET 쿼리스트링)에 담아 다시 요청했다.

				   무엇이 문제인가
				     주소창에 담긴 값은 아래에 모두 남는다.
				       - 브라우저 방문 기록 (다음 사용자가 주소창에서 볼 수 있다)
				       - 서버 접속 로그 (access log)
				       - 다른 사이트로 이동할 때 Referer 헤더
				       - 어깨너머로 보는 사람의 눈
				     비밀번호를 이런 곳에 남겨서는 안 된다.

				   (지금)
				     주소로 다시 요청하지 않고, 서버 안에서 목록을 다시 조회해
				     결과 화면으로 바로 포워딩한다.
				     비밀번호는 서버 메모리에만 있고 주소창에는 나타나지 않는다.
				     (POST 로 받은 값을 그대로 재사용하므로 사용자는 다시 입력하지 않아도 된다)
				 ============================================================================
				*/
				List<CarConfirmVo> remainOrders = carService.findOrders(memberphone, memberpass);

				request.setAttribute("v", remainOrders);   // 남은 예약 목록을 요청에 담는다
				request.setAttribute("memberphone", memberphone);   // 연락처도 함께 전달한다
				request.setAttribute("center", "CarReserveResult.jsp");   // 가운데에 예약 결과 화면을 끼우라고 알린다

				//취소 완료를 화면에서 안내하기 위한 메시지
				request.setAttribute("flashMessage", "예약이 취소되었습니다.");

				request.getRequestDispatcher("/CarMain.jsp").forward(request, response);   // 틀이 되는 CarMain.jsp 로 넘긴다. 주소창에 비밀번호가 남지 않는다
				return;   // 여기서 끝낸다

			}else {//2.4. 예약 정보 삭제(취소)에 실패하면?
				   //     1. "예약 정보 삭제 실패" <- 클라이언트가 요청한 브라우저 화면에 메세지를 보여주고
				   //     2.  예약 취소 요청 했던 VIEW 이전 화면(Delete.jsp중앙화면)을 재요청해서 다시 보여줌 
				  //	  3.  디스패처 방식으로 포워딩 되는 코드의 실행을 맊기 위해  doHandle 메소드 종료  <- return;
				  out.print("<script>");
				  out.print(" alert('예약 정보 삭제 실패');");   // 실패를 알린다
				  out.print(" history.back();");   // 이전 화면으로 되돌린다
				  out.print("</script>");   // 스크립트를 닫는다
				  return;   // 여기서 끝낸다
			}
				
			//네이버 블로그 검색 API 요청을 한 2단계 요청 주소와 같다면?	
			}else if(action.equals("/NaverSearchAPI.do")) {
				
				/*
				 1. 인증 정보 설정

				    [변경] 소스코드에 하드코딩  ->  설정 파일(AppConfig) 조회

				    (이전 코드)
				        final String clientId = "실제_클라이언트_아이디";
				        final String clientSecret = "실제_시크릿키";

				    (문제점)
				      - .java 파일에 시크릿이 들어 있어 소스를 공유하면 키도 함께 유출된다
				      - 키를 재발급할 때마다 소스를 고치고 다시 컴파일해야 한다
				      - 컴파일된 .class 파일에도 문자열이 그대로 남는다

				    (지금)
				      WEB-INF/app.properties 의 naver.client.id / naver.client.secret 를 읽는다.
				      app.properties 는 .gitignore 로 저장소/배포본에서 제외된다.
				*/
				final String clientId = NAVER_CLIENT_ID;
				final String clientSecret = NAVER_CLIENT_SECRET;   // 위에 적어 둔 시크릿 값을 읽어 온다

				//설정이 비어 있으면 API 호출 자체가 실패하므로 먼저 안내한다.
				if(clientId == null || clientSecret == null) {
					out.print("<script>");
					out.print(" alert('네이버 검색 API 키가 설정되지 않았습니다.\\nWEB-INF/app.properties 를 확인해주세요.');");   // 무엇을 해야 하는지까지 알려 준다 (\\n 은 알림창 안에서 줄을 바꾸라는 뜻)
					out.print(" history.back();");   // 이전 화면으로 되돌린다
					out.print("</script>");   // 스크립트를 닫는다
					return; //doHandle 메소드 종료
				}
				
				//2.검색 조건 설정
				int startNum = 0;  //검색 시작 위치를 저장하는 변수
				String text = null; //검색어를 저장하는 변수 
				
				try {
					//사용자가 선택한 검색 시작위치를 가져옵니다.
					String startNumStr = request.getParameter("startNum");
					//만약 사용자가 숫자가 아닌 값을 선택하면 NumberFormatException예외 발생할수 있으므로 예외처리필요
					
					//사용자가 select option중 하나를 선택했다면 (검색 시작위치를 받아오면?)
					if(startNumStr != null  && !startNumStr.isEmpty()) {
						
						startNum = Integer.parseInt(startNumStr);//문자열을 정수로 변환해서 검색시작위치를 저장
						
					}else {
						startNum = 1; //기본값 1 로 설정 
					}
					
					//사용자가 입력한 검색어를 가져옵니다
					String searchText = request.getParameter("keyword");
					
					//검색어 문자를 UTF-8로 인코딩해서 다시 반환
					//검색어를 네이버 검색API서버에 요청하는 URL에 포함 가능한 문자열로 변환해서 반환합니다.
					text = URLEncoder.encode(searchText, "UTF-8");
									
				} catch (UnsupportedEncodingException e) {
					//UTF-8 인코딩에 실패 한경우, 오류메세지를 출력하고 프로그램 실행 중단합니다.
					throw new RuntimeException("검색어 인코딩 실패 : " + e.getMessage(), e);
					
				} catch (NumberFormatException e) {
					//사용자가 입력한 startNum이 숫자가 아닌 경우 발생하는 예외처리를 합니다.
					response.setContentType("text/html; charset=utf-8");
					response.getWriter().write("잘못된 검색 시작 위치입니다. 숫자를 입력해주세요.");   // 숫자가 아니라고 화면에 알려 준다
					return; //웹브라우저로 응답을 보내고, 더이상 실행하지 않도록 합니다.
				}
				
				
				//3. Naver 블로그 검색 API서버에 요청을 보낼 URL 만들기 
				
				//3.1. 요청 검색 결과 데이터를 JSON데이터로 받기 위한 URL 
				String apiURL = "https://openapi.naver.com/v1/search/blog.json?query="+text+"&display=10&start="+startNum;
						
				   // ?query=: 검색어를 지정하는 쿼리 파라미터.
		           // text:  인코딩된 검색어가 들어있는 변수.
		          // &display=10: 한 번에 가져올 검색 결과의 개수를 10개로 지정.
		          // &start= : 검색 시작 위치를 지정하는 쿼리 파라미터.
		          // startNum: 검색 시작 위치가 들어있는 변수
				
				//4. API호출
				//네이버 검색 API서버에 블로그검색 요청을 하기 위해 필요한 정보를 설정합니다
				Map<String , String> requestHeaders = new HashMap<>();
				requestHeaders.put("X-Naver-Client-Id", clientId); //클라이언트 아이디를 맵에 추가 
				requestHeaders.put("X-Naver-Client-Secret", clientSecret);//클라이언트 시크릿 키 맵에 추가
				
				//API를 호출하고 , 결과를 문자열로 받습니다.
				String responseBody = CarController.get(apiURL, requestHeaders);	
				 // apiURL : 호출할 API의 URL
		         // requestHeaders : API 호출에 필요한 헤더 정보
				
				//=====================================================
				//5.
				//네이버 검색 결과를JSON으로 받아서  request에 바인딩
				request.setAttribute("searchData", responseBody);
				
				//네비어 검색 결과를 보여줄 중앙 VIEW 경로를 request에 바인딩
				request.setAttribute("center", "SearchResult.jsp");
				//=======================================================
				
				//6. 실제 메인 페이지를 포워딩 하기 위해 주소 저장
				nextPage = "/CarMain.jsp";
			}
		
		
		//3. 디스패처 방식으로 포워딩(재요청)
		RequestDispatcher requestDispatcher = request.getRequestDispatcher(nextPage);
						  requestDispatcher.forward(request, response);   // 지정한 화면으로 요청을 넘긴다. 주소창은 그대로고 화면만 바뀐다
		
	}//---------------------doHandle메소드 
	
	//==================================================================================================================
	
	//6. HTTP GET 요청 메소드 
	private static String get(String apiUrl, Map<String, String> requestHeaders) {
		
		HttpURLConnection con	= connect(apiUrl); //URL연결을 생성합니다.
		
		try {
			con.setRequestMethod("GET");//요청방식 GET으로 설정
			
			//요청 헤더를 설정합니다.
			//requestHeaders(MAP)에 담긴 모든 요청 데이터가 HTTP요청메세지에 반복해서 추가시킴
			for( Map.Entry<String, String>  header   :  requestHeaders.entrySet()   ) {
				// requestHeaders는 Map<String, String> 형태를 취합니다.
                // Map.Entry는 Map 내에 저장된 각 키-값 쌍을 지칭합니다.
                // 이 반복문은 Map 내에 존재하는 모든 키-값 쌍에 대하여 실행됩니다.
				
				con.setRequestProperty(  header.getKey() , header.getValue());

			}//for
			
			//4. 응답 코드 확인 : 서버로부터의 응답코드를 확인하여 요청이 성공했는지 확입니다
			int responseCode  = con.getResponseCode(); //네이버 서버로 부터 응답 코드를 받습니다.
			 // responseCode는 서버가 보낸 응답의 상태를 나타내는 숫자입니다.
            // 예를 들어, 200은 "성공", 400은 "잘못된 요청", 500은 "서버 내부 오류"를 의미합니다.
			
			if(responseCode == HttpURLConnection.HTTP_OK) {//200 ok : 정상적으로 응답할수 있음
							 							   //요청이 성공적으로 처리되었음을 의미합니다.
			      return  readBody(  con.getInputStream() ); //응답 내용을 읽어서 문자열로 반환함 
			      // con.getInputStream()은 응답 데이터를 읽을 수 있는 InputStream 객체를 반환합니다.
	              // readBody()는 이 InputStream으로부터 데이터를 읽어 문자열로 변환하는 사용자 정의 메서드입니다.
			       
			}else {//에러 발생 : 요청 처리 중에 오류가 발생했습니다
				 
				  return readBody( con.getErrorStream() );//에러 응답 내용을 읽어서 문자열로 반환합니다.
				  // con.getErrorStream()은 오류 응답 데이터를 읽을 수 있는 InputStream 객체를 반환합니다.
	               // readBody()는 이 InputStream으로부터 데이터를 읽어 문자열로 변환하는 사용자 정의 메서드입니다.
			}
				
		} catch (IOException e) {
			throw new RuntimeException("API 요청과 응답 실패 : " + e.getMessage(), e);   // 통신 자체가 실패한 경우. 원인을 담아 위로 던진다
		}finally {
			//HttpURLConnection객체 를 사용후 연결을 끊기 위해 자원을 해제 합니다.
			con.disconnect();
		}
	}
	
	//7. HTTP 연결 메소드 
	//주어진 요청 URL에 대한 HttpURLConnection객체를 생성하는 메소드입니다
	private static HttpURLConnection connect(String apiUrl) {
		try {
			//URI를 거쳐 URL 생성 (new URL(String) 생성자는 최신 JDK에서 deprecated)
			URL url = java.net.URI.create(apiUrl).toURL(); //요청하는 주소URL을 보관하는 객체 생성
				
			//URL 연결 열고 HttpURLConnection객체를 반환합니다.
			return  (HttpURLConnection)url.openConnection();
			
		} catch (MalformedURLException e) {//URL형식이 잘못된 경우 발생하는 예외처리
			throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl + " - " + e.getMessage(), e);   // 주소 형식이 잘못된 경우. 원인을 담아 위로 던진다
		} catch (IOException e) {// 연결 과정에서 오류가 발생한 경우 발생하는 예외를 처리합니다.
			throw new RuntimeException("연결이 실패했습니다. : " + apiUrl + " - " + e.getMessage(), e);   // 연결에 실패한 경우. 원인을 담아 위로 던진다
		}
	}
	
	//8. 네이버 서버가 JSON형태의 문자열로 응답한 내용을 InputStream으로 부터 읽어들여 문자열로 반환하는 메소드
	private static String readBody(InputStream body) {
		
		    //바이트 스트림을 문자 스트림으로 변환
			InputStreamReader streamReader	= new InputStreamReader(body); 
	        // body: 응답 데이터의 InputStream
	        // InputStreamReader: 바이트 스트림을 문자 스트림으로 변환
			
			//BufferedReader로 감싸서 한줄 씩 읽을수 있도록합니다.
			try(BufferedReader lineReader = new BufferedReader(streamReader)) {
				
				//네이버 서버가 응답한 조회된 블로그결과 데이터를 저장할 StringBuilder객체 생성
				StringBuilder responseBody = new StringBuilder();
				// BufferedReader: 텍스트 데이터를 한 줄씩 읽기 위한 버퍼 기능 제공
	            // StringBuilder: 문자열을 효율적으로 추가하기 위한 클래스
				
				String line;
				
				//BufferedReader입력스트림 통로에서 네이버 서버가 조회결과로 보낸 전체 데이터중에서
				//한 줄씩 읽어와서 StringBuilder객체 메모리에 반복해서 추가 
				while(  (line = lineReader.readLine()) != null ) {
					
					responseBody.append(line); //읽어온 한줄을 StringBuilder객체 메모리에 추가추가추가추가 					
				}
				
				return responseBody.toString(); // StringBuilder의 내용을 문자열로 반환합니다.
					
			} catch (IOException e) {//응답 내용을 읽는 도중에 인터넷이 끈겨 오류가 발생하는 경우 예외처리
				throw new RuntimeException("API 응답을 읽는데 실패 했습니다. - " + e.getMessage(),e);   // 응답을 읽는 도중 끊긴 경우. 원인을 담아 위로 던진다
			}
			
	}//readBody

	//===========================================================
	// 요청 진입점 : BaseController 가 GET/POST 를 한곳으로 모아준다
	//===========================================================
	/*
	  [왜 doGet/doPost 를 직접 만들지 않는가]
	    공통 예외 처리를 BaseController 한곳에 모으기 위해서다.
	    업무 예외를 상황에 맞는 상태코드(400/403/404/500)와 안내 화면으로
	    연결하는 코드가 예전에는 컨트롤러마다 달랐다.
	    이 메소드는 BaseController 와 기존 doHandle 을 이어주는 다리다.
	 */
	@Override
	protected void process(String action, HttpServletRequest request, HttpServletResponse response) throws Exception {
		doHandle(request, response);   // 실제 처리는 위에 있는 doHandle 이 한다
	}
}












