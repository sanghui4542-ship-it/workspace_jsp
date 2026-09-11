package Controller;   // 이 파일이 속한 패키지(=폴더) 이름. 실제 폴더 구조와 반드시 같아야 한다

import java.io.IOException;   // 파일/네트워크 입출력이 실패했을 때의 예외
import java.io.PrintWriter;   // 응답 화면에 글자를 직접 찍어 보낼 때 쓰는 도구 (AJAX 응답에 사용)

import javax.servlet.ServletConfig;   // 서블릿의 설정값을 읽는 객체
import javax.servlet.ServletException;   // 서블릿이 처리 중 실패했을 때의 예외
import javax.servlet.annotation.WebServlet;   // 이 서블릿이 어떤 주소의 요청을 받을지 정하는 표시 (아래 클래스 위에 붙어 있다)
import javax.servlet.http.HttpServletRequest;   // 브라우저가 보낸 요청(주소·파라미터·세션)이 담긴 객체
import javax.servlet.http.HttpServletResponse;   // 브라우저에게 돌려줄 응답을 담는 객체

import Service.MemberService;   // 업무 규칙을 담당하는 Service 클래스
import javax.servlet.http.HttpSession;   // 로그인 정보처럼 사용자별로 서버에 보관하는 저장소
import Vo.MemberVO;   // 값을 담아 나르는 상자(VO) 클래스
import util.KakaoAuth;   // 여러 곳에서 함께 쓰는 도우미 클래스

/*
 MVC 디자인 패턴 개발 방식
 
  M : Model ->  클라이언트의 브라우저로 응답해서 보여줄 데이터 (예: DB에서 조회된 정보, 계산된 정보) 
            ->  Model은  MemberDAO.java 와 MemberVO.java에 의해서 생성 됨
            
  V : View  ->  클라이언트의 브라우저로 응답해서 보여줄 디자인 페이지 (예: DB에서 조회된 회원정보 디자인 )
            ->  View는   Members.jsp 또는 Members.html
 
  C : Controller -> 클라이언트의 요청을 처음 받아  응답하는 서블릿 (예: MemberController, CarController)
  			     ->  Controller는    .java
 
  S : Service -> 비즈니스 로직을 계산 하기 위한  자바 파일
			  ->  MemberService.java 
			  
MVC 디자인 패턴 개발 방식  외우기
     웹브라우저          -> 고객(클라이언트)
  Controller -> 사장 
  Service -> 부장		
  DAO     -> 사원 
  	
요청 및 응답 순서
  1. 웹브라우저를 사용하는 고객(클라이언트)가  TOMCAT서버가 실행하는 사장에게 회원 관련 요청 합니다.
  2. 사장(MemberController)은 요청 주소를 받아 부장(Service)에 시킵니다.
  3. 부장(Service)는  사원(DAO)에게 DB관련 작업을 시킵니다.
  4. 사원(DAO)는 DB와 연동해서 DB에서 회원정보를 처리합니다.
  
  5. 사원(DAO)는 DB작업한 정보를 부장(Service)에게 전달해 보고 합니다.
  6. 부장(Service)은 사장(MemberController)에게 전달해 다시 보고 합니다.
  7. 사장(MemberController)은 고객에게 Model을 전달해 응답합니다.
  
*/

//...사장 

//MVC 디자인 패턴 개발 방법 중에서   C의 역할 을 하는 회원관련 처리  서블릿 

//1.           /member/join.me?center=members/join.jsp    회원가입 요청 VIEW 보여줘
//2.           /member/joinPro.me   입력한 회원 정보를 DB의 member 테이블에 insert 해줘~ 요청!(회원 가입 요청!)
@WebServlet("/member/*")   
public class MemberController extends BaseController {

	//직렬화 버전 번호 (HttpServlet이 Serializable을 구현하므로 경고 방지용으로 선언)
	private static final long serialVersionUID = 1L;

	//MemberService 부장 객체의 주소번지를 저장시킬 참조변수 선언
	//(transient : 서블릿 직렬화 대상에서 제외 - init()에서 다시 생성되므로 저장할 필요 없음)
	transient MemberService memberService;
	
	@Override   // 부모(HttpServlet)의 init 을 덮어쓴다는 표시
	public void init(ServletConfig config) throws ServletException {
		
		//init메소드가 처음 호출되면!~  MemberService객체를 생성해서 주소번지  위 memberService참조변수에 저장
		memberService = new MemberService();
	}
	
	protected void doHandle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {   // 실제 처리를 담당하는 메소드. 아래 process() 가 이걸 부른다
		//재료 준비 
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=UTF-8");   // 응답이 HTML 이고 한글은 UTF-8 이라고 브라우저에 알린다
		response.setCharacterEncoding("UTF-8");   // 응답 글자를 UTF-8 로 내보낸다 (안 하면 한글이 깨진다)
		PrintWriter out = response.getWriter();   // 응답에 글자를 쓸 수 있는 붓을 얻는다
		
		String nextPage = null;   //2단계 요청한 주소에 따라 포워딩 또는 보여줄 VIEW주소 경로가 저장될 변수 선언 (재료)
		String center = null;     //2단계 요청한 주소에 따라 보여줄 중앙 VIEW주소 경로가 저장될 변수 선언(재료)
		
		/*
			요청1. Top.jsp화면에서 회원가입 <button>을 클릭하여 회원 가입 요청 디자인 중앙화면 VIEW요청 주소를 받는다.
			           요청한 전체  URL-> /member/join.me?center=members/join.jsp 중에서 2단계 요청한 주소 "/join.me" 얻기 
				   결론 : 2단계 요청한 주소 -> "/join.me" 얻기 
				   
			요청2. join.jsp화면 에서 회원가입을 하기전 입력한 아이디가 DB의 member테이블에 존재하는지 확인하는 
			           요청 전체 URL-> /member/joinIdCheck.me?id=입력한아이디    중에서 2단계 요청한 주소 "/joinIdCheck.me" 얻기 
			           결론: 2단계 요청한 주소 -> "/joinIdCheck.me" 얻기  
			         
			요청3. join.jsp화면에서 가입할 새 회원 정보를 입력하고 가입요청을 받았을때
			         요청 전체 URL-> /member/joinPro.me 중에서  2단계 요청한 주소 "/joinPro.me" 얻기 
			         결론 : 2단계 요청한 주소 -> "/joinPro.me" 얻기 
			         
			요청4. Top.jsp 상단의 로그인 버튼을 클릭하여 아이디,비밀번호를 입력받을수 있는 중앙 VIEW요청을 받았을때..
			         요청 전체 URL->   /member/login.me 중에서 2단계 요청한 주소 "/login.me" 얻기       
			         결론 : 2단계 요청한 주소 -> "/login.me" 얻기 
			         
			요청5. members/login.jsp화면에서 아이디, 비밀번호 입력후 로그인 처리 요청을 받았을때....
			          요청 전체 URL-> /member/loginPro.me 중에서 2단계 요청한 주소 "/loginPro.me" 얻기
			         결론 : 2단계 요청한 주소 -> "/loginPro.me" 얻기        
			         
			요청6. Top.jsp 상단의 로그아웃 버튼을 클릭하여 로그아웃 요청을 받았을때...
			          요청 전체 URL -> /member/logout.me  중에서 2단계 요청한 주소 "/logout.me" 얻기 
			          결론 : 2단계 요청한 주소 -> "/logout.me" 얻기             
		*/
		String action = request.getPathInfo();   
		System.out.println("클라이언트가 요청한 2단계 요청 주소  : " + action);   // 어떤 주소가 들어왔는지 이클립스 콘솔에 찍는다. 화면이 안 뜰 때 여기부터 확인한다

		/*
		 [추가] 2단계 주소가 없는 요청 차단

		   /CarProject/member  처럼 2단계 주소 없이 요청하면 getPathInfo()가 null을 반환한다.
		   그 상태로 switch(null) 을 실행하면 NullPointerException 500 에러가 났다.
		   "없는 주소"는 서버 고장(500)이 아니라 404로 답해야 한다.
		*/
		if(action == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;   // 여기서 doHandle 메소드를 끝낸다. 아래 코드를 실행하면 안 되기 때문이다
		}
			   
		switch(action) {//클라이언트가 요청한 2단계 요청주소가?
			
			case "/join.me": //회원가입 작성 후 가입요청 하는 중앙 디자인 VIEW요청 2단계 요청 주소와 같다면?
				
				//1. 부장(MemberService)에게 시키기 : 회원가입 작성 후 가입 요청하는 중앙 디자인 VIEW 주소 얻기
				center = memberService.serviceJoinName(request);
			  //center = "members/join.jsp";	
			
				//2. 회원가입 요청 하는 중앙 VIEW 주소 경로 -> "members/join.jsp" 를 request 에 바인딩 
				request.setAttribute("center", center);
				
				//3. 실제 포워딩할 메인 화면 주소를 저장
				nextPage = "/CarMain.jsp";
						
				break;	//switch종료 
				
			case "/joinIdCheck.me": //가입시 입력한 아이디가 DB의 member테이블에 저장되어 있는지 아이디 유무 체크 2단계 요청주소와 같다면?
				
				//부장(MmemberService)에게 시키기 : 입력한 아이디가 DB의 member테이블에 저장되어 있는지 확인 하는 작업
				//							   serviceOverLappedId메소드의 반환 값 ->  true(중복), false(중복아님) 둘중 하나
				boolean result = memberService.serviceOverLappedId(request);
				
				//사용자가 가입을 위해 입력한 아이디  중복 결과를 다시 한번 확인 하여  
				//확인된 결과 값을 join.jsp파일과 연결된 join.js파일에 작성 해 놓은 $.ajax메소드 내부의 success:function의 data매개변수로 
				//웹브라우저를 거쳐 보냅니다!
				if(result == true) {
					
					out.write("not_usable");
					return; //doHandle 메소드 종료 
					
				}else if(result == false) {   // 중복이 아니면 = 쓸 수 있는 아이디면
					
					out.write("usable");   // "usable" 이라고 답한다. join.js 의 success 함수가 이 글자를 받아 안내를 띄운다
					return; //doHandle 메소드 종료 
				}
				break;
				
			case "/joinPro.me": //"회원가입 2단계 요청 주소와 같다면?"
				
				//부장(MmemberService)에게 시키기 : 가입을 위해 입력한 정보들이 저장된 request객체를 전달해서 새회원 추가 작업
				
				boolean joined = memberService.serviceInsertMember(request);

				if(!joined) {   // 가입에 실패했으면
					out.println("<script>");   // 브라우저가 실행할 스크립트를 만들어 보낸다
					out.println(" alert('회원가입에 실패했습니다. 아이디를 다시 확인해주세요.');");   // 실패를 알린다
					out.println(" history.back();");   // 이전 join.jsp 화면으로 되돌린다. 입력하던 값이 살아 있어 다시 치지 않아도 된다
					out.println("</script>");   // 스크립트를 닫는다
					return;   //doHandle 메소드 종료하면서  디스패처 방식의 포워딩  안함 
				}

				//새회원 추가에 성공하면 포워딩 해서 보려질 메인페이지 주소 저장
				nextPage = "/Car/Main";  //메인홈페이지 화면 CarMain.jsp를  CarContrller로 재요청 하기 위해 요청URL저장 

				break;//switch문 종료
				
			case "/login.me": // 로그인 버튼을 클릭하여 아이디,비밀번호를 입력받을수 있는 중앙 VIEW 2단계 요청 주소와 같다면?
				
				//부장(MemberService)에게 시키기 : 아이디 비밀번호 작성 후 로그인 요청하는 중앙 디자인 VIEW주소 얻기
				center = memberService.serviceLoginMember(); //"members/login.jsp"
				
				//request내장객체 메모리에 "members/login.jsp" 중앙 디자인 VIEW 주소 바인딩 
				request.setAttribute("center", center);
				
				//실제 포워딩할 메인 화면 주소를 저장
				nextPage = "/CarMain.jsp";
				
				break;//switch문 종료 
		
			case "/loginPro.me": //action변수에 저장된 2단계 요청한 주소가? "/loginPro.me"로그인처리 요청 처리 2단계주소와 같다면?
				
				//부장(MemberService)에게 시키기 : 로그인 처리 작업 
				int check = memberService.serviceUserCheck(request);
				//check값이    1이면 입력한 아이디,비밀번호가 DB에 존재함
				//         0이면 입력한 아이디만 DB에 존재함, 비밀번호 틀림 
				//        -1이면 입력한 아이디 DB에 존재하지 않음 
				
				if(check == 0) {//아이디 맞음, 비밀번호틀림
					out.println("<script>");
					out.println(" window.alert('비밀번호 틀림'); ");   // 비밀번호가 틀렸다고 알린다
					out.println(" history.go(-1);");   // 이전 화면으로 되돌린다 (-1 = 한 페이지 뒤로)
					out.println("</script>");   // 스크립트를 닫는다
					return;//doHandle메소드 종료 
			
				}else if(check == -1) {//아이디 틀림
					out.println("<script>");   // 브라우저가 실행할 스크립트를 만들어 보낸다
					out.println(" window.alert('아이디 틀림'); ");   // 아이디가 없다고 알린다
					out.println(" history.go(-1);");   // 이전 화면으로 되돌린다
					out.println("</script>");   // 스크립트를 닫는다
					return; //doHandle메소드 종료 
				}
				
				//check==1 일 경우 (로그인시 입력한 아이디, 비밀번호가  DB의 member테이블에 저장된 아이디, 비밀번호는 같다.)
				
				//메인화면 재요청 VIEW주소 저장
				//-> 실제 CarMain.jsp메인화면 포워딩 되면  include된 Top.jsp의 session.getAttribute("id"); 코드가 실행될것임 
				nextPage = "/CarMain.jsp"; 
				
				//switch문 종료
				break;			
				
			case "/kakaoLogin.me": { //카카오 로그인 버튼 클릭 -> 카카오 인증 화면으로 보낸다
				/*
				 [카카오 로그인 ① 단계]
				   우리가 로그인 화면을 만드는 것이 아니라,
				   사용자를 카카오의 로그인 화면으로 "보내는" 것이 전부다.
				   (전체 흐름 설명은 util/KakaoAuth.java 상단 주석)
				*/

				//키를 아직 설정하지 않은 상태에서 눌렀을 때의 안내
				if (!KakaoAuth.isConfigured()) {
					out.println("<script>");
					out.println(" alert('카카오 로그인이 아직 설정되지 않았습니다.\\n"   // 무엇을 해야 하는지까지 알려 준다 (\\n 은 알림창 안에서 줄을 바꾸라는 뜻)
							+ "WEB-INF/app.properties 의 kakao.rest.api.key 를 확인해주세요.');");
					out.println(" history.back();");   // 이전 화면으로 되돌린다
					out.println("</script>");   // 스크립트를 닫는다
					return;   // 여기서 끝낸다
				}

				try {
					/*
					 [CSRF 방어 - state]
					   무작위 문자열을 세션에 저장해 두고 카카오에 함께 보낸다.
					   콜백에서 같은 값이 돌아오는지 확인한다.
					   (공격자가 "자기" 인가코드를 남의 브라우저에 심어
					    남의 계정을 공격자 카카오와 연결시키는 공격을 막는다)
					*/
					String state = KakaoAuth.newState();
									  //MemberController String stat=ggsgFKLeUqd2pcXODvhFhg
					System.out.println("MemberController String stat=" + state);
					
					request.getSession().setAttribute("KAKAO_STATE", state);   // 만든 무작위 값을 세션에 보관해 둔다. 콜백에서 이 값과 맞춰 볼 것이다

					//카카오 인증 화면으로 이동 (redirect - 주소창이 kauth.kakao.com 으로 바뀐다)
					response.sendRedirect(KakaoAuth.buildAuthorizeUrl(state));
					System.out.println(KakaoAuth.buildAuthorizeUrl(state));

				} catch (Exception e) {
					System.out.println("[MemberController] 카카오 인증 주소 생성 실패 : " + e.getMessage());   // 실패 원인은 서버 콘솔에만 남긴다 (사용자에게 내부 사정을 보여주지 않는다)
					out.println("<script>alert('카카오 로그인 준비 중 오류가 발생했습니다.'); history.back();</script>");   // 사용자에게는 무난한 안내만 띄우고 되돌린다
				}
				return; //redirect 했으므로 doHandle 종료
			}

			case "/kakaoCallback.me": { //카카오가 인증을 마치고 되돌려 보내는 자리
				/*
				 [카카오 로그인 ②~⑤ 단계]
				   카카오가 이 주소로 사용자를 되돌려 보낸다.
				   주소에 붙어 오는 것 : code(인가코드), state(우리가 보낸 값)
				   사용자가 취소했으면 : error=access_denied
				*/

				//사용자가 카카오 화면에서 "취소"를 누른 경우
				String kakaoError = request.getParameter("error");
				if (kakaoError != null) {   // error 파라미터가 있으면 사용자가 카카오 화면에서 취소를 누른 것이다
					out.println("<script>");   // 브라우저가 실행할 스크립트를 만들어 보낸다
					out.println(" alert('카카오 로그인이 취소되었습니다.');");   // 취소되었다고 알린다
					out.println(" location.href='" + request.getContextPath() + "/member/login.me';");   // 로그인 화면으로 되돌려 보낸다
					out.println("</script>");   // 스크립트를 닫는다
					return;   // 여기서 끝낸다
				}

				//[CSRF 방어] 보낸 state 와 돌아온 state 가 같은지 확인
				HttpSession kakaoSession = request.getSession(false);
				String savedState = (kakaoSession == null) ? null : (String)kakaoSession.getAttribute("KAKAO_STATE");   // 세션에 넣어 둔 무작위 값을 꺼낸다 (세션이 없으면 null)
				String returnedState = request.getParameter("state");   // 카카오가 돌려준 값을 꺼낸다

				if (savedState == null || !savedState.equals(returnedState)) {   // 둘이 다르면 = 우리가 보낸 요청이 아니다
					System.out.println("[MemberController] 카카오 state 불일치 - 요청 차단");   // 차단했다는 사실을 콘솔에 남긴다
					response.sendError(HttpServletResponse.SC_FORBIDDEN);   // 403(권한 없음)으로 응답하고 더 진행하지 않는다
					return;   // 여기서 끝낸다
				}
				kakaoSession.removeAttribute("KAKAO_STATE"); //한 번 쓴 state 는 재사용 금지

				String code = request.getParameter("code");   // 카카오가 붙여 준 인가코드를 꺼낸다
				if (code == null || code.trim().isEmpty()) {   // 인가코드가 없으면 정상적인 콜백이 아니다
					response.sendError(HttpServletResponse.SC_BAD_REQUEST);   // 400(요청 잘못)으로 응답한다
					return;   // 여기서 끝낸다
				}

				try {
					//③ 인가코드 -> 액세스 토큰   ④ 토큰 -> 사용자 정보
					String accessToken = KakaoAuth.requestAccessToken(code);
					KakaoAuth.KakaoUser kakaoUser = KakaoAuth.requestUserInfo(accessToken);   // 받은 토큰으로 회원번호·닉네임·이메일을 조회한다

					//⑤ 우리 회원으로 로그인 (처음이면 자동 가입) - 세션 처리까지 Service 가 한다
					memberService.serviceKakaoLogin(request, kakaoUser);

					/*
					 로그인 후 메인으로 redirect 한다 (forward 가 아니라).
					   주소창에 code=... state=... 가 남아 있는 상태에서 forward 하면
					   새로고침(F5) 할 때마다 이미 사용된 인가코드로 재요청되어 오류가 난다.
					   redirect 로 주소를 깨끗하게 바꿔준다. (PRG 패턴)
					*/
					response.sendRedirect(request.getContextPath() + "/Car/Main");

				} catch (Exception e) {
					//토큰 교환 실패, 카카오 응답 오류 등 - 원인은 서버 로그로만 남긴다
					System.out.println("[MemberController] 카카오 로그인 실패 : " + e.getMessage());
					out.println("<script>");   // 브라우저가 실행할 스크립트를 만들어 보낸다
					out.println(" alert('카카오 로그인에 실패했습니다. 잠시 후 다시 시도해주세요.');");   // 실패를 알린다
					out.println(" location.href='" + request.getContextPath() + "/member/login.me';");   // 로그인 화면으로 되돌려 보낸다
					out.println("</script>");   // 스크립트를 닫는다
				}
				return;   // 성공이든 실패든 여기서 끝낸다 (아래 공통 화면 이동을 하지 않는다)
			}

			case "/logout.me":  //로그아웃 처리 2단계 요청 주소와 같다면?
				
				//부장(MemberService)에게 시키기 : 로그아웃된 화면을 보여주기 위해 session영역에 저장된 아이디를 제거 
				memberService.serviceLogout(request);
				
				//메인화면 재요청을 위해 VIEW주소 저장
				//-> 실제 CarMain.jsp메인화면 포워딩 되면  include된 Top.jsp의 session.getAttribute("id"); 코드가 실행될것임 
				//참고. /CarMain.jsp 메인 화면을 재요청하면 중앙 페이지는 Center.jsp의 디자인이 나올 것이다.
				nextPage = "/CarMain.jsp";
				
				//switch문 종료 
				break;
				
		case "/memberUpdate.me": // 회원정보 수정 화면 요청

			//[보안 추가] 로그인하지 않았으면 수정 화면 자체를 주지 않는다
			HttpSession updateSession = request.getSession();
			String updateId = (String)updateSession.getAttribute("id");   // 세션에서 로그인한 아이디를 꺼낸다

			if(updateId == null) {   // 로그인하지 않았으면
				out.println("<script>");   // 브라우저가 실행할 스크립트를 만들어 보낸다
				out.println(" alert('로그인이 필요한 기능입니다.');");   // 로그인이 필요하다고 알린다
				out.println(" location.href='" + request.getContextPath() + "/member/login.me';");   // 로그인 화면으로 보낸다
				out.println("</script>");   // 스크립트를 닫는다
				return;   // 여기서 끝낸다
			}

			MemberVO updateVo = memberService.serviceMemberDetail(updateId);   // 회원 전체 정보를 조회한다 (수정 화면의 입력칸을 채우는 데 쓴다)
			request.setAttribute("membervo", updateVo);   // 조회한 정보를 요청에 담아 화면이 꺼내 쓸 수 있게 한다
			request.setAttribute("center", "members/memberUpdate.jsp");   // 가운데에 회원정보 수정 화면을 끼우라고 알린다
			nextPage = "/CarMain.jsp";   // 틀이 되는 CarMain.jsp 로 넘긴다
			break;

		case "/memberUpdatePro.me": // 회원정보 수정 AJAX 처리
			/*
			 [핵심 보안 수정]

			   기존 : memberService.serviceUpdateMember(request);
			          -> Service가 request.getParameter("id") 로 "누구를 수정할지" 결정했다.
			          -> 로그인하지 않은 사람이 id=admin 을 보내면 관리자 비밀번호가 바뀌었다.

			   지금 : 세션에서 로그인 아이디를 꺼내 Service에 직접 넘긴다.
			          -> 화면이 보낸 id 파라미터는 사용되지 않는다.
			          -> 결과적으로 "자기 정보만" 수정할 수 있다.
			*/
			HttpSession proSession = request.getSession(false);
			String proLoginId = (proSession == null) ? null : (String)proSession.getAttribute("id");   // 세션에서 로그인한 아이디를 꺼낸다 (세션이 없으면 null)

			String updateResult = memberService.serviceUpdateMember(request, proLoginId);   // 세션의 아이디만 넘긴다. 화면에서 온 id 는 믿지 않는다 (남의 정보를 고칠 수 있으므로)
			out.write(updateResult);   // 결과 문구를 그대로 응답에 쓴다. 화면의 AJAX 가 이 글자를 보고 안내를 띄운다
			return;   // AJAX 응답이므로 화면 이동 없이 끝낸다

		case "/memberDelete.me": // 회원 탈퇴 AJAX 처리

			HttpSession deleteSession = request.getSession(false);   // 세션을 가져온다 (없으면 만들지 않는다)
			String deleteId = (deleteSession == null) ? null : (String)deleteSession.getAttribute("id");   // 세션에서 로그인한 아이디를 꺼낸다

			//[보안 추가] 미로그인 상태의 탈퇴 요청 차단
			if(deleteId == null) {
				out.write("로그인필요");
				return;   // AJAX 응답이므로 화면 이동 없이 끝낸다
			}

			String deleteResult = memberService.serviceDeleteMember(deleteId);   // 탈퇴 처리를 맡긴다
			if(deleteResult.equals("삭제성공")) {   // 탈퇴에 성공했으면
				deleteSession.removeAttribute("id");   // 세션에서 아이디를 지워 로그아웃 상태로 만든다
			}
			out.write(deleteResult);   // 결과 문구를 그대로 응답에 쓴다
			return;   // AJAX 응답이므로 화면 이동 없이 끝낸다

			default:   // 위 어느 주소에도 걸리지 않은 경우
				break;
					
		}//switch문

		/*
		 [추가] 매칭되지 않은 주소 처리

		   switch 의 어느 case 에도 걸리지 않으면 nextPage 는 null 그대로다.
		   기존에는 그 상태로 getRequestDispatcher(null) 을 호출해
		   NullPointerException 500 에러가 났다.
		*/
		if(nextPage == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;   //doHandle 메소드 종료 해서 아래  디스패처 방식 포워딩 막는다.
		}

		//디스패처 방식으로  /CarMain.jsp 포워딩(재요청)
		request.getRequestDispatcher(nextPage).forward(request, response);
									
											
	} //========================================================================> doHandle 메소드 }
	
	
	
	//===========================================================
	// 요청 진입점 : BaseController 가 GET/POST 를 한곳으로 모아준다
	//===========================================================
	/*
	  [왜 doGet/doPost 를 직접 만들지 않는가]
	    공통 예외 처리를 BaseController 한곳에 모으기 위해서다.
	    이 메소드는 BaseController 와 기존 doHandle 을 이어주는 다리다.
	 */
	@Override
	protected void process(String action, HttpServletRequest request, HttpServletResponse response) throws Exception {
		doHandle(request, response);   // 실제 처리는 위에 있는 doHandle 이 한다
	}

	/* 로그인해야만 쓸 수 있는 주소 (예전 web.xml AuthFilter 대신) */
	@Override
	protected boolean requiresLogin(String action) {
		return action.equals("/memberUpdate.me")   // 세 주소만 로그인이 필요하다. 나머지(가입·로그인 화면 등)는 누구나 쓸 수 있어야 한다
			|| action.equals("/memberUpdatePro.me")
			|| action.equals("/memberDelete.me");
	}

}

