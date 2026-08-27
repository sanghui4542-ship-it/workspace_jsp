package member;

// 입출력 예외 처리를 위한 IOException import
import java.io.IOException;

// 서블릿 작성에 필요한 javax.servlet 도구들 import
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.rowset.Joinable;

/*
================================================================
 [실습 4] MemberController 에 회원가입 기능 추가하기

 완성 목표 2가지
   1. doHandle() 에 /join.do 분기 추가
   2. join() 메소드 작성
        입력값 꺼내기 -> VO 포장 -> Service 호출
        성공: index.jsp 로 redirect
        실패: joinMsg 담아 join.jsp 로 forward

 ** 창구(*.do)는 그대로 두고 분기만 추가하는 것이 핵심이다. **
 ** 주석 아래 빈 줄에 코드를 직접 작성한다. **
================================================================
*/
// .do 로 끝나는 모든 요청을 받도록 @WebServlet("*.do") 등록
@WebServlet("*.do")
public class MemberController extends HttpServlet {

	// 업무를 맡길 MemberService 변수 memberService 선언
	private MemberService memberService;

	// init() : 서블릿이 만들어질 때 1번만 실행되어 Service 객체 생성
	@Override
	public void init() throws ServletException {
		memberService = new MemberService();
	}

	// doGet() : GET 요청을 공용 처리 doHandle 로 넘기기
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doHandle(request, response);
	}

	// doPost() : POST 요청을 공용 처리 doHandle 로 넘기기
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doHandle(request, response);
	}

	/*==============================================================
	  doHandle() : 요청 주소를 보고 담당 기능으로 나눠 주는 공용 처리
	==============================================================*/
	private void doHandle(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// 요청 본문의 한글 처리 (getParameter 보다 먼저!)
		request.setCharacterEncoding("UTF-8");

		// 요청 주소에서 프로젝트 이름을 뺀 경로 얻기 (예: /member/login.do)
		String action = request.getServletPath();

		// 주소가 /login.do 로 끝나면 login() 호출
		if (action.endsWith("/login.do")) {
			login(request, response);

		// 주소가 /logout.do 로 끝나면 logout() 호출
		} else if (action.endsWith("/logout.do")) {
			logout(request, response);

		// [실습 4-1] 주소가 /join.do 로 끝나면 join() 호출하는 else if 추가
		} else if(action.endsWith("/join.do")) {
			
			join(request, response);

		// 등록되지 않은 주소는 메인 화면으로 redirect
		} else {
			response.sendRedirect(request.getContextPath() + "/index.jsp");
		}

	}//doHandle

	/*==============================================================
	  login() : 로그인 처리 (완성된 예제 - 아래 실습의 참고용)
	==============================================================*/
	private void login(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// 폼에서 id, pass 입력값 꺼내기
		String id   = request.getParameter("id");
		String pass = request.getParameter("pass");

		// 입력값 2개를 MemberVO 상자에 포장
		MemberVO memberVO = new MemberVO(id, pass);

		// Service 에 로그인 업무를 맡기고 결과 받기
		boolean isMember = memberService.login(memberVO);

		// 성공이면 세션 저장 후 메인으로 redirect
		if (isMember) {
			HttpSession session = request.getSession();
			session.setAttribute("userId", id);
			response.sendRedirect(request.getContextPath() + "/index.jsp");

		// 실패면 안내 문구를 담아 login.jsp 로 forward
		} else {
			request.setAttribute("loginMsg", "아이디 또는 비밀번호가 틀렸습니다.");
			request.getRequestDispatcher("/member/login.jsp").forward(request, response);
		}

	}//login

	/*==============================================================
	  logout() : 로그아웃 처리 (완성된 예제)
	==============================================================*/
	private void logout(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// 세션이 있으면 가져오고 없으면 null (새로 만들지 않음)
		HttpSession session = request.getSession(false);

		// 세션이 있을 때만 삭제해서 로그아웃 처리
		if (session != null) {
			session.invalidate();
		}

		// 메인 화면으로 redirect
		response.sendRedirect(request.getContextPath() + "/index.jsp");

	}//logout

	/*==============================================================
	  [실습 4-2] join() : 회원가입 처리
	  - join.jsp 폼에서 넘어온 입력값을 저장한다
	==============================================================*/
	// 회원가입을 처리하는 private void join(...) 메소드 선언
	// (매개변수와 throws 는 위 login() 과 똑같이 작성)
	private void join(HttpServletRequest request, HttpServletResponse response) 
				 throws ServletException, IOException {
		
		// 폼에서 id, pass, name, email 입력값 4개 꺼내기
		String id = request.getParameter("id");
		String pass = request.getParameter("pass");
		String name = request.getParameter("name");
		String email = request.getParameter("email");

		// 입력값 4개를 MemberVO 상자에 포장 (회원가입용 생성자 사용)
		MemberVO memberVO = new MemberVO(id, pass, name, email);


		// Service 에 회원가입 업무를 맡기고 결과 받기
		boolean isJoined = memberService.join(memberVO);

		// 가입 성공이면 세션에 userId 저장 (가입 즉시 로그인 처리)
		if(isJoined) {
			HttpSession session = request.getSession();
			session.setAttribute("userId", id);
			// 성공 시 메인 화면으로 redirect
			response.sendRedirect(request.getContextPath() + "/index.jsp");
			
		}else {
			// 가입 실패면 "이미 사용 중인 아이디입니다." 를 joinMsg 로 request 에 담기
			request.setAttribute("joinMsg", "이미 사용 중인 아이디입니다.");
			
			// 실패 시 join.jsp 로 forward (loginMsg 처럼 화면에서 꺼내 쓰게)
			request.getRequestDispatcher("/member/join.jsp").forward(request, response);		
		}
		
	}

}//MemberController
