package member;

import java.io.IOException;
/* 응답을 쓰는 도중 통신이 끊기는 등의 입출력 문제를 다루는 예외 클래스 */

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
/* 서블릿을 만들 때 쓰는 표준 도구들
     HttpServlet         : 서블릿의 부모 클래스
     HttpServletRequest  : 브라우저가 보낸 요청 정보 (입력값, 주소, 세션)
     HttpServletResponse : 브라우저로 보낼 응답 정보 (이동 지시 등)
     HttpSession         : 사용자 1명당 1개인 서버 메모리 저장 공간
     @WebServlet         : 담당할 주소를 등록하는 표식 */

/*
================================================================
 [복습 4] MemberController.java

 ── 이 파일이 하는 일 ──────────────────────────────────────
   1) 브라우저의 요청을 받는다
   2) 입력값을 꺼내 VO 상자에 담는다
   3) Service 에 일을 맡긴다
   4) 결과에 따라 어느 화면으로 보낼지 정한다

 ── 이 파일이 하지 않는 일 ─────────────────────────────────
   HTML 을 만들지 않는다  -> out.print 가 한 줄도 없다
   SQL 을 쓰지 않는다     -> select, insert 라는 글자가 없다
   업무 판단을 하지 않는다 -> "중복이면 거부" 같은 규칙이 없다
 ** 서블릿 단원에서 배운 out.print("<html>...") 방식과의
    결정적인 차이가 바로 이 지점이다. **

 ── 서블릿의 일생 ──────────────────────────────────────────
   1. 첫 요청이 오면 톰캣이 이 클래스의 객체를 1개 만든다
   2. init() 이 딱 1번 실행된다
   3. 요청이 올 때마다 doGet 또는 doPost 가 실행된다
   4. 서버가 꺼질 때 destroy() 가 실행된다
   ** 객체가 1개뿐이라 모든 사용자가 같은 서블릿을 함께 쓴다.
      그래서 회원 정보 같은 것을 필드에 저장하면 안 된다.
      (다른 사람의 정보가 섞인다) **

 ── 주소 설계 : 왜 창구를 하나로 모았나 ────────────────────
   @WebServlet("*.do") -> .do 로 끝나는 모든 요청이 여기로 온다.

   기능마다 서블릿을 따로 만들면 파일이 11개가 된다.
   하나로 모으면 새 기능이 생겨도 아래 doHandle 에
   분기 한 줄만 추가하면 된다.

   [처리하는 주소 11개]
     login.do        logout.do        join.do
     modifyForm.do   modify.do
     withdrawForm.do withdraw.do
     findIdForm.do   findId.do
     findPwdForm.do  findPwd.do

 ── Form 이 붙은 주소와 안 붙은 주소의 차이 ────────────────
   modifyForm.do : 수정 "화면을 보여주는" 기능 (DB 조회 후 화면에 채움)
   modify.do     : 수정을 "실제로 저장하는" 기능
   -> 화면을 보여줄 때도 준비 작업이 필요하면 컨트롤러를 거친다.

 ── redirect 와 forward (이 파일의 핵심 개념) ──────────────
   redirect : response.sendRedirect(주소)
              브라우저에게 "그 주소로 다시 요청해" 라고 시킨다.
              요청이 2번 일어난다.
              주소창이 바뀐다.
              request 에 담은 값은 사라진다.
              -> 성공 후 이동에 사용
                 (새로고침해도 폼이 재전송되지 않는다)

   forward  : request.getRequestDispatcher(주소).forward(req, res)
              서버 안에서 화면에게 처리를 넘긴다.
              요청은 1번 그대로다.
              주소창이 바뀌지 않는다.
              request 에 담은 값이 그대로 전달된다.
              -> 실패 안내처럼 화면에 넘길 값이 있을 때 사용

   ** 실습 확인법 **
   로그인에 실패하면 주소창이 login.do 인 채로 로그인 화면이 보인다.
   성공하면 주소창이 index.jsp 로 바뀐다. 직접 확인해 보자.
================================================================
*/

@WebServlet("*.do")
/* 주소가 .do 로 끝나는 모든 요청을 이 서블릿이 받는다. (확장자 매핑)
     /FunWeb/member/login.do   -> 이 서블릿이 받음
     /FunWeb/index.jsp         -> .do 가 아니므로 JSP 가 직접 처리
   ** 옛날 방식(web.xml 에 servlet-mapping 작성)을 대체한 것으로
      둘 중 하나만 쓰면 된다. ** */
public class MemberController extends HttpServlet {
/* extends HttpServlet
   : 이것을 상속해야 톰캣이 "서블릿이구나" 하고 인식해
     doGet, doPost 를 자동으로 불러 준다.
     상속하지 않으면 아무리 잘 짜도 실행되지 않는다. */

	private MemberService memberService;
	/* 업무를 맡길 Service 객체를 담아 둘 변수.

	   ** 여기에 회원 정보를 저장하면 안 되는 이유 **
	   서블릿 객체는 1개뿐이라 모든 사용자가 공유한다.
	   여기에 아이디를 저장하면 다른 사람이 로그인할 때
	   덮어써져 정보가 섞인다.
	   -> 사용자별 정보는 반드시 session 에 저장한다. */

	/*==============================================================
	  init() : 서블릿 준비 메소드

	  언제 실행되나 : 이 서블릿이 처음 만들어질 때 딱 1번
	  하는 일       : Service 객체를 1개 만들어 보관
	==============================================================*/
	@Override
	public void init() throws ServletException {
	/* @Override : 부모(HttpServlet)의 메소드를 다시 정의한다는 표시.
	   오타로 다른 이름을 쓰면 컴파일 단계에서 오류로 잡아 준다. */

		memberService = new MemberService();
		/* Service 객체를 만든다.
		   이 순간 Service 의 생성자가 실행되고,
		   그 안에서 DAO 가 만들어지며 커넥션풀까지 준비된다.

		   ** 요청마다 new 하지 않는 이유 **
		   요청 100번에 100개를 만들면 커넥션풀도 100번 찾게 되어
		   낭비다. 1번 만들어 계속 재사용한다. */

	}//init

	/*==============================================================
	  doGet() : GET 방식 요청을 받는다

	  GET 이 언제 오나 : 주소창 직접 입력, 링크 클릭
	    이 프로젝트에서는 logout.do, modifyForm.do 등
	  하는 일 : 직접 처리하지 않고 공용 처리 doHandle 로 넘긴다
	==============================================================*/
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	/* throws ServletException, IOException
	   : 처리 중 문제가 생기면 톰캣에게 넘긴다는 선언.
	     부모 메소드의 규격이므로 그대로 적어야 한다. */

		doHandle(request, response);
		/* GET 이든 POST 든 처리 방법이 같으므로 한 곳으로 모은다.
		   -> 같은 코드를 두 번 쓰지 않게 된다. */

	}

	/*==============================================================
	  doPost() : POST 방식 요청을 받는다

	  POST 가 언제 오나 : 폼(form)의 method="post" 전송
	    이 프로젝트에서는 login.do, join.do, modify.do 등
	  하는 일 : 역시 doHandle 로 넘긴다

	  ** GET 과 POST 의 차이 **
	    GET  : 값이 주소 뒤에 붙는다 (?id=hong) -> 주소창에 보인다
	    POST : 값이 본문에 담긴다              -> 주소창에 안 보인다
	    -> 비밀번호가 있는 폼은 반드시 POST 를 쓴다.
	==============================================================*/
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doHandle(request, response);

	}

	/*==============================================================
	  doHandle() : 주소를 보고 담당 기능으로 나눠 주는 공용 처리

	  [하는 일 순서]
	    1) 요청 본문의 한글 처리 설정
	    2) 주소를 얻는다
	    3) 주소 끝을 검사해 담당 메소드를 부른다
	    4) 해당 없으면 메인 화면으로 보낸다

	  private 인 이유 : 브라우저가 직접 부르는 통로가 아니라
	                    내부 정리용이기 때문이다.
	==============================================================*/
	private void doHandle(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
		/* 요청 본문의 글자를 UTF-8 로 해석하라고 설정한다.

		   ** 반드시 getParameter 보다 먼저 실행해야 한다 **
		   값을 이미 꺼낸 뒤에 설정하면 이미 깨진 상태라
		   되돌릴 수 없다. 그래서 분기보다도 앞에 두었다.
		   -> 한글 이름이 ??? 로 저장된다면 이 줄의 위치를 의심하자. */

		String action = request.getServletPath();
		/* 요청 주소에서 "프로젝트 이름을 뺀 나머지" 를 얻는다.

		   예) 브라우저 주소 : http://localhost:8080/FunWeb/member/login.do
		       getContextPath() -> "/FunWeb"           (프로젝트 이름)
		       getServletPath() -> "/member/login.do"  (담당 판단 재료)
		       getRequestURI()  -> "/FunWeb/member/login.do" (전체)

		   ** 왜 getServletPath 를 쓰는가 **
		   프로젝트 이름이 FunWeb 이든 pro13 이든 영향받지 않기 때문이다. */

		if (action.endsWith("/login.do")) {
			login(request, response);
		/* endsWith : 글자가 "~로 끝나는가?" 를 검사한다.
		   equals 로 정확히 비교해도 되지만
		   폴더 구조가 바뀌어도 동작하도록 끝부분만 확인한다. */

		} else if (action.endsWith("/logout.do")) {
			logout(request, response);

		} else if (action.endsWith("/join.do")) {
			join(request, response);

		} else if (action.endsWith("/modifyForm.do")) {
			modifyForm(request, response);

		} else if (action.endsWith("/modify.do")) {
			modify(request, response);

		} else if (action.endsWith("/withdrawForm.do")) {
			withdrawForm(request, response);

		} else if (action.endsWith("/withdraw.do")) {
			withdraw(request, response);

		} else if (action.endsWith("/findIdForm.do")) {
			findIdForm(request, response);

		} else if (action.endsWith("/findId.do")) {
			findId(request, response);

		} else if (action.endsWith("/findPwdForm.do")) {
			findPwdForm(request, response);

		} else if (action.endsWith("/findPwd.do")) {
			findPwd(request, response);

		} else {
			response.sendRedirect(request.getContextPath() + "/index.jsp");
			/* 등록되지 않은 .do 주소로 들어온 경우다.
			   예) 오타로 /member/logn.do 를 요청한 상황.
			   오류 화면 대신 메인으로 안전하게 돌려보낸다. */
		}

		/* ** 새 기능을 추가하려면 **
		   여기에 else if 한 줄과 아래에 메소드 하나만 만들면 된다.
		   서블릿 파일을 새로 만들 필요가 없다. */

	}//doHandle

	/*==============================================================
	  login() : 로그인 처리

	  [흐름]
	    login.jsp 의 폼 전송
	      -> 여기서 입력값을 꺼내 상자에 담고
	        -> Service 에 맡기고
	          -> 결과에 따라 화면을 고른다
	==============================================================*/
	private void login(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String id = request.getParameter("id");
		String pass = request.getParameter("pass");
		/* 폼의 입력칸 name 으로 값을 꺼낸다.
		     <input type="text"     name="id">   -> "hong"
		     <input type="password" name="pass"> -> "1234"

		   ** 값이 계속 null 이라면 **
		   폼의 name 철자와 여기 적은 이름이 다른 것이다.
		   가장 흔한 원인이므로 제일 먼저 확인하자. */

		MemberVO memberVO = new MemberVO(id, pass);
		/* 입력값 2개를 상자 하나로 포장한다.
		   2개짜리 생성자(로그인용)를 사용했다. */

		boolean isMember = memberService.login(memberVO);
		/* Service 에 맡기고 결과만 받는다.

		   이 한 줄 뒤에서 벌어지는 일
		     Service -> DAO -> MySQL 조회 -> 결과가 거꾸로 돌아옴
		   ** Controller 는 그 과정을 전혀 모른다.
		      DB 가 오라클로 바뀌어도 이 줄은 수정할 필요가 없다. ** */

		if (isMember) {
		/* [로그인 성공] */

			HttpSession session = request.getSession();
			/* 세션 객체를 얻는다.
			   세션ID 가 없으면 새로 만들고, 있으면 기존 것을 준다.
			   로그인 직후이므로 보통 새로 만들어진다. */

			session.setAttribute("userId", id);
			/* ** 이 한 줄이 "로그인 상태" 의 실체다 **
			   세션 객체에 "userId" 라는 이름표로 아이디를 저장한다.
			     값은 서버 메모리에 있고,
			     브라우저에는 세션ID 만 쿠키로 전달된다.
			   -> inc/top.jsp 와 index.jsp 의 헤더가
			      이 값이 있는지 없는지로 화면을 나눈다. */

			response.sendRedirect(request.getContextPath() + "/index.jsp");
			/* 메인 화면으로 이동 지시(302).

			   getContextPath() 를 붙이는 이유
			     프로젝트 이름이 무엇이든 자동으로 맞춰지므로
			     배포 환경이 바뀌어도 안전하다.

			   ** redirect 를 쓰는 이유 **
			   브라우저에게 새 주소로 다시 요청하게 하므로
			   주소창이 index.jsp 로 바뀐다.
			   -> F5(새로고침)를 눌러도 로그인 폼이 재전송되지 않는다.
			      forward 였다면 주소가 login.do 로 남아
			      새로고침 때마다 로그인이 다시 전송된다. */

		} else {
		/* [로그인 실패] */

			request.setAttribute("loginMsg", "아이디 또는 비밀번호가 틀렸습니다.");
			/* 실패 안내 문구를 request 에 담는다.
			   login.jsp 가 이 값을 꺼내 경고창으로 보여준다.

			   ** session 이 아니라 request 에 담는 이유 **
			   이 메시지는 지금 이 화면에서 한 번만 필요하다.
			   session 에 담으면 다음 페이지에서도 남아
			   엉뚱한 때에 경고창이 뜬다. */

			request.getRequestDispatcher("/member/login.jsp")
			       .forward(request, response);
			/* login.jsp 에게 처리를 넘긴다.

			   ** forward 를 쓰는 이유 **
			   서버 안에서 이동하므로 같은 request 가 그대로 전달된다.
			   -> 방금 담은 loginMsg 가 login.jsp 까지 살아서 도착한다.
			   -> redirect 였다면 새 요청이 되어 loginMsg 가 사라진다! */
		}

	}//login

	/*==============================================================
	  logout() : 로그아웃 처리
	  ** Service 와 DAO 를 부르지 않는다 **
	     로그아웃은 DB 와 무관한 세션 정리 작업이라
	     Controller 선에서 끝난다.
	==============================================================*/
	private void logout(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession(false);
		/* getSession(false) : "있으면 주고, 없으면 새로 만들지 말고 null"

		     getSession()      -> 없으면 새로 만들어서 준다
		     getSession(true)  -> 위와 같다 (기본값)
		     getSession(false) -> 없으면 null 을 준다

		   ** 왜 false 인가 **
		   로그아웃하러 온 사람에게 세션을 새로 만들어 주면
		   "만들자마자 지우는" 낭비가 된다. */

		if (session != null) {
			session.invalidate();
			/* 세션 객체를 서버 메모리에서 통째로 삭제한다.
			   저장돼 있던 userId 도 함께 사라진다 = 로그아웃 완료.
			   -> 브라우저에 세션ID 쿠키가 남아 있어도
			      서버에 짝이 되는 객체가 없으므로 무효가 된다. */
		}
		/* ** null 검사를 반드시 해야 한다 **
		   이미 로그아웃한 사람이 또 눌렀거나
		   세션 유효시간(기본 30분)이 지난 뒤 눌렀다면 null 이다.
		   검사 없이 invalidate() 를 부르면 500 오류가 난다. */

		response.sendRedirect(request.getContextPath() + "/index.jsp");
		/* ** if 블록 "바깥" 에 있는 것이 중요하다 **
		   세션이 있었든 없었든 결과는 똑같이 메인 화면이어야
		   사용자가 혼란스럽지 않다. */

	}//logout

	/*==============================================================
	  join() : 회원가입 처리
	  ** 가입 성공 시 세션에 아이디를 저장해
	     "가입하자마자 로그인된 상태" 로 만든다. **
	==============================================================*/
	private void join(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String id    = request.getParameter("id");
		String pass  = request.getParameter("pass");
		String name  = request.getParameter("name");
		String email = request.getParameter("email");
		/* 가입 폼의 입력값 4개를 꺼낸다. */

		MemberVO memberVO = new MemberVO(id, pass, name, email);
		/* 4개짜리 생성자(회원가입용)를 사용했다. */

		boolean isJoined = memberService.join(memberVO);
		/* Service 가 중복 검사 후 저장까지 처리하고 결과를 준다. */

		if (isJoined) {

			HttpSession session = request.getSession();
			session.setAttribute("userId", id);
			/* 가입 성공 시 바로 로그인 상태로 만든다.
			   사용자가 가입 후 다시 로그인하는 번거로움을 없앤 것이다. */

			response.sendRedirect(request.getContextPath() + "/index.jsp");

		} else {

			request.setAttribute("joinMsg", "이미 사용 중인 아이디입니다.");
			/* 가입 실패의 대부분은 아이디 중복이다. */

			request.getRequestDispatcher("/member/join.jsp")
			       .forward(request, response);
		}

	}//join

	/*==============================================================
	  modifyForm() : 수정 화면 보여주기

	  [왜 화면만 보여주는데 컨트롤러를 거치나]
	  입력칸에 기존 정보를 미리 채워 넣으려면
	  DB 에서 조회해 request 에 담아 줘야 하기 때문이다.
	  modify.jsp 를 주소창에 직접 입력하면 화면이 비어 버린다.

	  ** 수정 기능이 2단계인 이유가 이것이다 **
	    1단계 modifyForm.do : 조회해서 화면에 채워 보여주기
	    2단계 modify.do     : 실제로 저장하기
	==============================================================*/
	private void modifyForm(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession(false);

		if (session == null || session.getAttribute("userId") == null) {
			response.sendRedirect(request.getContextPath() + "/member/login.jsp");
			return;
		}
		/* [로그인 검사]
		   로그인하지 않은 사람이 수정 화면에 들어오면 안 된다.

		   ** return 을 반드시 써야 한다 **
		   return 이 없으면 redirect 를 해 놓고도
		   아래 코드가 계속 실행되어
		   "응답을 두 번 보내려 한다" 는 오류가 난다.

		   ** || 는 "또는" 이다 **
		   세션이 없거나, 있어도 userId 가 없으면 -> 로그인 화면으로 */

		String userId = (String) session.getAttribute("userId");
		/* 세션에서 아이디를 꺼낸다.
		   getAttribute 의 반환은 Object 라 (String) 으로 되돌린다. */

		MemberVO memberVO = memberService.getMember(userId);
		/* DB 에서 이 회원의 현재 정보를 조회한다. */

		request.setAttribute("member", memberVO);
		/* 조회한 상자를 "member" 라는 이름으로 request 에 담는다.
		   modify.jsp 가 이 상자를 꺼내 입력칸에 값을 채운다. */

		request.getRequestDispatcher("/member/modify.jsp")
		       .forward(request, response);
		/* forward 로 넘겨야 방금 담은 member 가 전달된다. */

	}//modifyForm

	/*==============================================================
	  modify() : 수정 처리

	  ** 이 메소드에서 가장 중요한 것 **
	  수정할 아이디를 폼이 아니라 "세션에서" 가져온다는 점이다.

	  폼에서 받은 아이디를 그대로 믿으면
	  남의 아이디를 적어 보내 다른 사람 정보를 고칠 수 있다.
	  세션의 아이디를 쓰면 본인 것만 수정된다.
	  -> 그래서 modify.jsp 의 아이디 칸에는 name 속성이 없고
	     readonly 로 되어 있다. (보여주기만 하고 전송하지 않음)
	==============================================================*/
	private void modify(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			response.sendRedirect(request.getContextPath() + "/member/login.jsp");
			return;
		}
		/* 로그인 검사 (modifyForm 과 동일) */

		String id = (String) session.getAttribute("userId");
		/* ** 폼이 아니라 세션에서 아이디를 가져온다 ** */

		String pwd   = request.getParameter("pwd");
		String name  = request.getParameter("name");
		String email = request.getParameter("email");
		/* 수정할 값 3개는 폼에서 가져온다. */

		MemberVO memberVO = new MemberVO();
		memberVO.setId(id);
		memberVO.setPwd(pwd);
		memberVO.setName(name);
		memberVO.setEmail(email);
		/* 빈 상자를 만들고 setter 로 하나씩 담았다.

		   ** 4개짜리 생성자를 안 쓴 이유 **
		   생성자는 (id, pwd, name, email) 순서인데
		   여기서는 아이디의 출처가 달라(세션) 구분해서 보여주려고
		   setter 방식을 썼다. 생성자를 써도 결과는 같다. */

		boolean isModified = memberService.modify(memberVO);

		if (isModified) {
			response.sendRedirect(request.getContextPath() + "/index.jsp");

		} else {

			request.setAttribute("modifyMsg", "회원정보 수정에 실패했습니다.");

			request.setAttribute("member", memberService.getMember(id));
			/* ** 실패 시 조회를 한 번 더 하는 이유 **
			   수정 화면으로 되돌아가는데 입력칸이 비어 있으면
			   사용자가 처음부터 다시 입력해야 한다.
			   기존 정보를 다시 담아 채워 준다. */

			request.getRequestDispatcher("/member/modify.jsp")
			       .forward(request, response);
		}

	}//modify

	/*==============================================================
	  withdrawForm() : 탈퇴 확인 화면 보여주기

	  ** 수정 화면과 달리 DB 조회가 없다 **
	  탈퇴는 비밀번호만 새로 입력받으면 되므로
	  미리 채워 넣을 정보가 없기 때문이다.
	==============================================================*/
	private void withdrawForm(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			response.sendRedirect(request.getContextPath() + "/member/login.jsp");
			return;
		}

		request.getRequestDispatcher("/member/withdraw.jsp")
		       .forward(request, response);

	}//withdrawForm

	/*==============================================================
	  withdraw() : 탈퇴 처리

	  ** 탈퇴에만 있는 특별한 처리 **
	  삭제 성공 후 세션도 함께 지워야 한다.
	  회원 정보는 지웠는데 로그인 상태가 남으면
	  헤더에 없는 회원의 아이디가 계속 표시된다.
	==============================================================*/
	private void withdraw(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			response.sendRedirect(request.getContextPath() + "/member/login.jsp");
			return;
		}

		String id = (String) session.getAttribute("userId");
		/* 탈퇴할 아이디도 세션에서 가져온다. 본인만 탈퇴 가능. */

		String pwd = request.getParameter("pwd");
		/* 본인 확인용 비밀번호는 폼에서 받는다. */

		MemberVO memberVO = new MemberVO(id, pwd);
		/* 2개짜리 생성자를 사용했다. (로그인 검증과 같은 구성) */

		boolean isWithdrawn = memberService.withdraw(memberVO);
		/* Service 가 비밀번호를 확인한 뒤 삭제한다. */

		if (isWithdrawn) {

			session.invalidate();
			/* ** 회원 삭제 후 세션도 지운다 **
			   순서가 중요하다. 세션을 먼저 지우면
			   위에서 꺼낸 id 는 이미 변수에 있으니 괜찮지만,
			   삭제가 실패했을 때 로그아웃까지 되어 버린다.
			   그래서 성공했을 때만 지운다. */

			response.sendRedirect(request.getContextPath() + "/index.jsp");

		} else {

			request.setAttribute("withdrawMsg", "비밀번호가 일치하지 않습니다.");

			request.getRequestDispatcher("/member/withdraw.jsp")
			       .forward(request, response);
		}

	}//withdraw

	/*==============================================================
	  findIdForm() : 아이디 찾기 화면 보여주기

	  ** 세션 검사를 하지 않는다 **
	  로그인 전에 사용하는 기능이기 때문이다.
	  수정/탈퇴는 로그인한 사람만, 찾기는 로그인 못 한 사람이 쓴다.

	  [mode 값이 필요한 이유]
	  아이디 찾기와 비밀번호 찾기가 화면 하나(find.jsp)를 함께 쓴다.
	  어느 기능으로 들어왔는지 알려 줘야 화면이 알맞게 그려진다.
	==============================================================*/
	private void findIdForm(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setAttribute("mode", "id");
		/* "아이디 찾기 화면" 이라는 표시를 담는다.
		   find.jsp 가 이 값으로 제목과 입력칸을 결정한다. */

		request.getRequestDispatcher("/member/find.jsp")
		       .forward(request, response);

	}//findIdForm

	/*==============================================================
	  findId() : 아이디 찾기 처리
	==============================================================*/
	private void findId(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String name  = request.getParameter("name");
		String email = request.getParameter("email");
		/* 본인 확인 항목 2개를 꺼낸다. */

		String foundId = memberService.findId(name, email);
		/* Service 가 빈 값을 걸러내고 DAO 에게 조회를 시킨다. */

		request.setAttribute("mode", "id");
		/* ** mode 를 다시 담는 이유 **
		   forward 로 find.jsp 에 돌아가는데,
		   이것은 새 요청이라 앞서 담은 mode 가 없다.
		   화면이 어느 기능인지 알아야 하므로 다시 담아 준다. */

		if (foundId != null) {
			request.setAttribute("resultMsg", "회원님의 아이디는 [ " + foundId + " ] 입니다.");
			/* 찾았으면 아이디를 문구에 넣어 담는다. */

		} else {
			request.setAttribute("resultMsg", "일치하는 회원 정보가 없습니다.");
			/* ** 못 찾은 이유를 자세히 알려주지 않는 것이 좋다 **
			   "그런 이름은 없습니다" 처럼 알려주면
			   어떤 이름이 가입되어 있는지 추측할 수 있게 된다. */
		}

		request.getRequestDispatcher("/member/find.jsp")
		       .forward(request, response);
		/* ** 반드시 forward 여야 한다 **
		   redirect 를 쓰면 새 요청이 되어
		   방금 담은 resultMsg 가 사라지고 결과가 보이지 않는다. */

	}//findId

	/*==============================================================
	  findPwdForm() : 비밀번호 찾기 화면 보여주기
	  - findIdForm 과 같고 mode 값만 "pwd" 다
	==============================================================*/
	private void findPwdForm(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setAttribute("mode", "pwd");
		/* "비밀번호 찾기 화면" 이라는 표시.
		   find.jsp 는 이 값을 보고 아이디 입력칸을 하나 더 보여준다. */

		request.getRequestDispatcher("/member/find.jsp")
		       .forward(request, response);

	}//findPwdForm

	/*==============================================================
	  findPwd() : 비밀번호 찾기 처리
	  - findId 와 같고 꺼낼 값이 3개다
	==============================================================*/
	private void findPwd(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String id    = request.getParameter("id");
		String name  = request.getParameter("name");
		String email = request.getParameter("email");
		/* 본인 확인 항목 3개를 꺼낸다.
		   비밀번호는 더 중요한 정보라 아이디 찾기보다 하나 더 요구한다. */

		String foundPwd = memberService.findPwd(id, name, email);

		request.setAttribute("mode", "pwd");

		if (foundPwd != null) {
			request.setAttribute("resultMsg", "회원님의 비밀번호는 [ " + foundPwd + " ] 입니다.");

		} else {
			request.setAttribute("resultMsg", "일치하는 회원 정보가 없습니다.");
		}

		request.getRequestDispatcher("/member/find.jsp")
		       .forward(request, response);

	}//findPwd

}//MemberController

/*
================================================================
 [혼자 확인해 보기]

  1. Controller 가 화면을 만들지 않는다는 것을 어떻게 확인하는가?
     -> 이 파일에 out.print 가 한 줄도 없다

  2. redirect 와 forward 를 눈으로 구분하는 방법은?
     -> 주소창을 본다. redirect 는 바뀌고 forward 는 그대로다

  3. 로그인 실패에 redirect 를 쓰면 어떻게 되는가?
     -> loginMsg 가 사라져 경고창이 뜨지 않는다

  4. 세션 검사에서 return 을 빼면?
     -> redirect 후에도 아래 코드가 실행되어 오류가 난다

  5. 수정할 아이디를 폼에서 받으면 어떤 위험이 있는가?
     -> 남의 아이디를 적어 보내 타인 정보를 고칠 수 있다

  6. 수정 기능만 2단계(modifyForm/modify)인 이유는?
     -> 화면에 기존 정보를 미리 채우려면 먼저 조회해야 한다

  7. 새 기능(예: 회원목록)을 추가하려면 무엇을 해야 하는가?
     -> doHandle 에 else if 한 줄 + 메소드 하나

 [전체 흐름 복습]
   브라우저 -> 톰캣 -> Controller -> Service -> DAO -> MySQL
                          ^                              |
                          +-- 결과가 거꾸로 돌아온다 ----+
                          |
              성공: session 저장 + redirect
              실패: request 에 메시지 + forward
================================================================
*/
