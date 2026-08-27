package member;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/*
================================================================
MemberController : 요청 접수 창구 서블릿   [MVC 에서의 위치 : Controller]

[MVC 3계층의 역할 분담]
  Model      : MemberVO(데이터) + MemberService(업무) + MemberDAO(DB)
  View       : login.jsp, index.jsp 등 화면 담당 JSP
  Controller : 이 클래스. 요청을 받아 Model 에 일을 시키고,
               결과에 따라 어느 View 로 보낼지 "결정만" 한다.
               화면 HTML 을 직접 만들지 않는 것이 핵심이다.

[요청 주소 설계]
  *.do 로 끝나는 모든 요청이 이 컨트롤러로 모인다.
    /member/login.do   -> 로그인 처리
    /member/logout.do  -> 로그아웃 처리
  -> 창구를 하나로 모으면 기능이 늘어도 여기서 분기만 추가한다.

[전체 흐름]
login.jsp --> Controller --> Service --> DAO --> MySQL
                 ^                                  |
                 +---- true/false <-----------------+
                 |
         성공: session 저장 + index.jsp 로 이동(redirect)
         실패: 안내 문구를 담아 login.jsp 로 전달(forward)
================================================================
*/


/* 이 클래스를 서블릿으로 등록하는 표식(애노테이션)이다.
"*.do" 는 확장자 매핑 : 주소가 무엇이든 .do 로 끝나면 여기로 온다.
  /FunWeb/member/login.do   -> 이 서블릿이 받음
  /FunWeb/member/logout.do  -> 이 서블릿이 받음
  /FunWeb/index.jsp         -> .do 가 아니므로 JSP 가 직접 처리
*/
@WebServlet("*.do") /* 요청 주소가 .do 로 끝나는 모든 요청 주소를  이 서블릿이 받아 처리 합니다.  */
public class MemberController extends HttpServlet {  //사장 
	
	// 업무를 맡길 Service 객체를 담을 변수 
	private MemberService memberService;
		
	@Override
	public void init() throws ServletException {
		
		memberService = new MemberService();
		
	}
	/*===========================================================================
	 	doHandle() : 요청 주소를 받아 응답을 처리하는 공용 일반 메소드
	 ============================================================================*/
	protected void doHandle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		request.setCharacterEncoding("UTF-8");    /* 요청 받은 데이터 한글처리 */
		
		String action = request.getServletPath(); /*  /member/logout.do   */
	
		
		/* endsWith : 문자열이 "~로 끝나는가?" 를 검사한다.
		   equals("/member/login.do") 로 정확히 비교해도 되지만,
		   폴더 구조가 바뀌어도 동작하도록 끝부분만 확인한다. */
		if(action.endsWith("/login.do")) { //서블릿이 로그인 요청 주소를 받았을때 
						
			login(request, response); /* 로그인 요청이면 로그인 담당 메소드를 호출해 처리 */
			
			
		} else if(action.endsWith("/logout.do") ) { //서블릿이 로그 아웃 요청 주소를 받았을때
			
			logout(request, response); /* 로그아웃 요청이면 로그아웃 담당 메소드를 호출해 처리 */
			
		} else { //등록되지 않은 .do 주소요청은 메인화면(index.jsp)을 재요청(포워딩)해서 보여준다.
			
			response.sendRedirect(request.getContextPath() + "/index.jsp");
								 //                   /FunWeb/index.jsp		
		}
		
	} //doHandle
	
	/*==============================================================
	  logout() : 로그아웃 요청 처리

	  호출 시점 :  요청 주소가 /logout.do 로 끝날 때! 즉! 로그아웃요청 주소일때! (doHandle 메소드 내부에서 이 호출)
	  매개변수  :   request  = 로그인 성공한 사용자의 아이디가 보관된 HttpSession객체를 얻기 위한  HttpServletRequest객체 
	              response = 포워딩 으로 페이지 이동 지시를 할 HttpServletResponse 객체 
	  하는 일   : HttpSession 객체를 톰캣 메모리영역 내부에서 삭제해서 로그인 상태를 끝낸다

	  ** 이 메소드는 MemberService 와 MemberDAO 를 부르지 않는다!
	     로그아웃은 DB 와 무관한 "HttpSession 세션객체 메모리를  톰캣에에서  삭제 정리 작업"이므로
	     MemberController 사장 선에서 끝난다. **
	==============================================================*/
	private void logout(HttpServletRequest request, HttpServletResponse response) 
														throws ServletException, IOException {
		//1. 로그인 성공한 사용자의 아이디가 보관된 기존 HttpSession 객체 얻기
		HttpSession session = request.getSession(false);
		/* getSession(false) : "있으면 주고, 없으면 새로 만들지 말고 null"

		   getSession()      -> 없으면 새로 만들어서 준다
		   getSession(true)  -> 위와 같다 (기본값)
		   getSession(false) -> 없으면 null 을 준다

		   ** 왜 false 인가?
		      로그아웃하러 온 사용자에게 세션을 새로 만들어 주면 "만들자마자 지우는" 낭비가 된다.
		      게다가 세션 생성 이벤트(리스너)가 불필요하게 실행된다. ** */
		
		/* 2. null 검사를 반드시 해야 한다.
			   이미 로그아웃한 사람이 [logout] 을 또 눌렀거나,
			   HttpSession세션 유효시간이 지난 뒤 눌렀다면 HttpSession 이 null 이다.
			   검사 없이 invalidate() 를 부르면 NullPointerException(500) 이 난다. */
		if(session != null) {
			
			/* HttpSession 세션 객체를 톰캣서버 메모리에서 통째로 삭제한다.
			   안에 저장돼 있던 userId 도 함께 사라진다 = 로그아웃 완료.
			   -> 브라우저에 세션ID 쿠키가 남아 있어도
			      서버에 짝이 되는 HttpSession세션 객체가 없으므로 무효가 된다. */
			session.invalidate();
		}
		
		/*3. 리다이렉트 방식으로 재요청(포워딩)해서  메인 화면으로 이동 지시.*/
		response.sendRedirect(request.getContextPath() + "/index.jsp");
		
		
	}
	
	
	
	/*==============================================================
	  login() : 로그인 요청 처리

	  호출 시점 : 주소가 /login.do 로 끝날 때 (doHandle 이 호출)
	  매개변수  : request  = 아이디/비밀번호가 담긴 요청
	            response = 이동 지시를 실을 응답
	  하는 일   : 입력값 포장 -> Service 에 판정 요청 -> 결과별 View 선택
	  끝난 뒤   : 성공이면 index.jsp, 실패면 login.jsp 가 화면을 그린다
	==============================================================*/
	private void login(HttpServletRequest request,  HttpServletResponse response) throws ServletException, IOException {
		
		/* login.jsp 폼의 입력칸 name 으로 값을 꺼낸다.
		     <input type="text"     name="id">    -> "hong"
		     <input type="password" name="pass">  -> "1234"  */
		String id = request.getParameter("id");
		String pass = request.getParameter("pass");
		
		/* MemberService 부장의 메소드를 호출하면서 요청한 아이디, 비밀번호 전달시 하나씩 전달하기 번거로우므로 
		   MemberVO 클래스의 객체 하나에 보관후 전달 합니다.*/
		MemberVO memberVO = new MemberVO(id, pass);
		
		boolean isMember = memberService.login(memberVO);
		
		/*[로그인 성공] DB에 로그인요청시 입력한 아이디, 비밀번호의 회원레코드가 조회 되면?*/
		if(isMember) {
			
			HttpSession session = request.getSession();  /*HttpSession 세션 객체를 얻는다.*/			
			session.setAttribute("userId", id);    /* HttpSession 세션 객체에  "userId" 키와 함께 로그인 요청시 입력한 아이디 바인딩 */
			
			/*MemberController ---- 리다이렉방식의 포워딩-----> /FunWeb/index.jsp  */
			response.sendRedirect( request.getContextPath()  + "/index.jsp" );
						
		}else { /*[로그인 실패] DB에 로그인요청시 입력한 아이디, 비밀번호의 회원레코드가 조회 되지 않으면?*/
			
			/* 실패 안내 문구를 request 에 담는다.
			   ** session 이 아니라 request 에 담는 이유 :
			      이 메시지는 "지금 이 화면에서 한 번만" 필요하다.
			      session 에 담으면 다음 페이지에서도 계속 남아
			      엉뚱한 때에 경고창이 뜰 수 있다. ** */
			request.setAttribute("loginMsg","아이디 또는 비밀번호가 틀렸습니다.");

			/* MemberController  ---디스패처방식의 포워딩----> /member/login.jsp (VIEW) */
			request.getRequestDispatcher("/member/login.jsp").forward(request, response);
						
		}
	}

	
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doHandle(request, response);
	}
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doHandle(request, response);
	}

}
