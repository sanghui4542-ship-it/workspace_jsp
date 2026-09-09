package Controller;   // 이 파일이 속한 패키지(=폴더) 이름. 실제 폴더 구조와 반드시 같아야 한다

import java.io.IOException;   // 파일/네트워크 입출력이 실패했을 때의 예외
import java.io.PrintWriter;   // 응답 화면에 글자를 직접 찍어 보낼 때 쓰는 도구 (AJAX 응답에 사용)

import javax.servlet.ServletException;   // 서블릿이 처리 중 실패했을 때의 예외
import javax.servlet.http.HttpServlet;   // 서블릿을 만들 때 물려받는 부모 클래스
import javax.servlet.http.HttpServletRequest;   // 브라우저가 보낸 요청(주소·파라미터·세션)이 담긴 객체
import javax.servlet.http.HttpServletResponse;   // 브라우저에게 돌려줄 응답을 담는 객체
import javax.servlet.http.HttpSession;   // 로그인 정보처럼 사용자별로 서버에 보관하는 저장소

import exception.ForbiddenException;   // 직접 만든 예외 클래스
import exception.InvalidInputException;   // 직접 만든 예외 클래스
import exception.NotFoundException;   // 직접 만든 예외 클래스
import util.HtmlUtil;   // 여러 곳에서 함께 쓰는 도우미 클래스

/*
 ================================================================================
   BaseController  -  모든 컨트롤러(서블릿)의 공통 부모 클래스

   [기존 코드에서 4개 컨트롤러가 똑같이 반복하던 것들]
     1) doGet / doPost 가 각각 doHandle 을 호출하는 코드
     2) request.setCharacterEncoding / response.setContentType 3줄
     3) String nextPage 변수를 만들고 마지막에 forward 하는 코드
     4) out.print("<script>alert(...)</script>") 문자열 조립

   [기존 코드의 구조적 문제]
     switch(action) 에서 어느 case 에도 걸리지 않으면 nextPage 가 null 인 채로

         request.getRequestDispatcher(nextPage).forward(request, response);

     이 실행되어 NullPointerException 500 이 났다.
     또 request.getPathInfo() 가 null 이면 (예: /Car 로 요청) switch(null) 에서 바로 NPE 였다.

   [바뀐 구조]
     - 요청 처리 흐름(예외 -> 상태코드 -> 화면)을 이 클래스 한 곳에서 관리한다
     - 자식 컨트롤러는 process() 안에서 "무슨 일을 할지"만 작성한다
     - 흐름 자체는 그대로다.  Controller -> Service -> DAO -> Service -> Controller -> VIEW

   [MVC 역할 복습]
     Controller(사장) : 요청을 받아 판단하고 Service에 지시, 결과를 VIEW로 전달
     Service(부장)    : 업무 규칙 판단 + 트랜잭션 관리
     DAO(사원)        : SQL 실행
 ================================================================================
*/
public abstract class BaseController extends HttpServlet {

	private static final long serialVersionUID = 1L;   // 서블릿을 저장/전송할 때 쓰는 버전 번호. 경고를 없애려고 형식적으로 적는다

	/** Model2 메인 레이아웃 (Top + center + Bottom 을 include 하는 화면) */
	protected static final String MAIN_PAGE = "/CarMain.jsp";

	/** 세션에 로그인 아이디를 담아둔 이름 (기존 코드와 동일) */
	protected static final String SESSION_LOGIN_ID = "id";

	//===========================================================
	// 1. 요청 진입점 : GET / POST 모두 같은 흐름으로 처리
	//===========================================================
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		handle(request, response);   // GET 이든 POST 든 아래 handle() 하나로 모아 처리한다. 두 곳에 같은 코드를 두지 않기 위해서다
	}

	@Override   // 부모(HttpServlet)의 doPost 를 덮어쓴다는 표시
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		handle(request, response);   // POST 도 GET 과 똑같이 handle() 로 보낸다
	}

	private void handle(HttpServletRequest request, HttpServletResponse response)   // 모든 요청이 반드시 지나가는 공통 통로. 주소 확인·로그인 검문·예외 처리를 여기서 한다
			throws ServletException, IOException {

		// 2단계 요청 주소 얻기  (예: /CarList.do , /list.bo , /joinPro.me)
		String action = request.getPathInfo();

		try {
			// /Car 처럼 2단계 주소가 없는 요청은 404 로 처리한다 (기존에는 NPE 500)
			if (action == null || action.equals("/")) {
				throw new NotFoundException("요청 주소가 올바르지 않습니다");
			}

			System.out.println("[" + getClass().getSimpleName() + "] 요청 주소 : " + action);   // 어떤 주소가 들어왔는지 이클립스 콘솔에 찍는다. 화면이 안 뜰 때 여기부터 확인하면 된다

			// [필터 대신] 로그인이 필요한 주소면 여기서 먼저 막는다.
			// 예전에는 filter/AuthFilter 가 했지만, 필터를 없앤 교육판에서는
			// 이 공통 부모가 대신 검문한다. 목록은 각 컨트롤러가 requiresLogin() 으로 알려준다.
			if (requiresLogin(action)) {
				requireLoginId(request);   // 미로그인이면 ForbiddenException(403) 을 던진다
			}

			process(action, request, response);   // 실제 처리는 자식 컨트롤러에게 맡긴다 (CarController, BoardController 등)

		} catch (InvalidInputException e) {
			// 400 : 요청 값이 잘못됨
			sendProblem(request, response, HttpServletResponse.SC_BAD_REQUEST,
					"/error/error.jsp", e.getMessage());

		} catch (ForbiddenException e) {
			// 403 : 권한 없음
			sendProblem(request, response, HttpServletResponse.SC_FORBIDDEN,
					"/error/error.jsp", e.getMessage());

		} catch (NotFoundException e) {
			// 404 : 자원 없음
			sendProblem(request, response, HttpServletResponse.SC_NOT_FOUND,
					"/error/404.jsp", e.getMessage());

		} catch (Exception e) {
			// 500 : 예상하지 못한 오류 -> 사용자에게는 내부 정보를 보여주지 않는다
			System.out.println("[" + getClass().getSimpleName() + "] 처리 중 오류 : " + action);
			e.printStackTrace();   // 예상 못 한 오류는 서버 콘솔에 자세히 남긴다. 사용자에게는 보여주지 않는다
			sendProblem(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,   // 사용자에게는 500 화면과 무난한 안내 문구만 보낸다
					"/error/500.jsp", "요청을 처리하는 중 문제가 발생했습니다.");
		}
	}

	/**
	 * 자식 컨트롤러가 구현할 실제 처리 메소드.
	 * @param action 2단계 요청 주소 (null 이 아님이 보장된다)
	 */
	protected abstract void process(String action, HttpServletRequest request, HttpServletResponse response)
			throws Exception;

	/**
	 * 로그인이 있어야만 처리하는 주소인가?
	 * 기본은 "전부 허용(false)". 로그인이 필요한 컨트롤러만 재정의해서 목록을 준다.
	 * (예전 web.xml 의 AuthFilter url-pattern 을 여기로 옮긴 것)
	 */
	protected boolean requiresLogin(String action) {
		return false;   // 기본은 "로그인 없이도 된다". 필요한 컨트롤러만 이 메소드를 덮어쓴다
	}

	//===========================================================
	// 2. 화면 이동 도우미
	//===========================================================

	/**
	 * 중앙화면(center)을 지정하고 메인 레이아웃(CarMain.jsp)으로 포워딩한다.
	 */
	protected void forwardMain(HttpServletRequest request, HttpServletResponse response, String center)
			throws ServletException, IOException {

		request.setAttribute("center", center);   // "가운데에 이 화면을 끼워라" 를 요청에 담는다. CarMain.jsp 가 이 값을 읽는다

		request.getRequestDispatcher(MAIN_PAGE).forward(request, response);   // CarMain.jsp 로 넘긴다. 주소창은 그대로고 화면만 바뀐다 (forward)
	}

	/** 지정한 경로로 그대로 포워딩 (내부 재요청) */
	protected void forward(HttpServletRequest request, HttpServletResponse response, String path)
			throws ServletException, IOException {
		request.getRequestDispatcher(path).forward(request, response);   // 지정한 화면으로 요청을 그대로 넘긴다
	}

	/** 다른 주소로 리다이렉트 (브라우저 주소창이 바뀐다. POST 후 새로고침 중복 방지에 사용) */
	protected void redirect(HttpServletRequest request, HttpServletResponse response, String pathWithinApp)
			throws IOException {
		response.sendRedirect(request.getContextPath() + pathWithinApp);   // 브라우저에게 "저 주소로 다시 가라" 고 시킨다. 주소창이 바뀐다
	}

	//===========================================================
	// 3. 응답 출력 도우미
	//===========================================================

	/** AJAX 응답용 : 순수 텍스트 */
	protected void writeText(HttpServletResponse response, String text) throws IOException {
		response.setContentType("text/plain;charset=UTF-8");   // 응답이 순수 글자이고 한글은 UTF-8 이라고 브라우저에 알린다
		PrintWriter out = response.getWriter();   // 응답에 글자를 쓸 수 있는 붓을 얻는다
		out.write(text == null ? "" : text);   // 넘어온 글자를 쓴다. null 이면 빈 문자열로 (화면에 "null" 이 찍히는 것을 막는다)
		out.flush();   // 남아 있는 것을 마저 내보낸다
	}

	/** AJAX 응답용 : JSON 문자열 (이미 완성된 JSON을 넘긴다) */
	protected void writeJson(HttpServletResponse response, String json) throws IOException {
		response.setContentType("application/json;charset=UTF-8");   // 응답이 JSON 이라고 브라우저에 알린다. 이래야 자바스크립트가 바로 객체로 읽는다
		PrintWriter out = response.getWriter();   // 응답에 글자를 쓸 수 있는 붓을 얻는다
		out.write(json == null ? "{}" : json);   // 완성된 JSON 을 쓴다. null 이면 빈 객체 {} 를 보낸다
		out.flush();   // 남아 있는 것을 마저 내보낸다
	}

	/**
	 * 알림창을 띄우고 지정한 주소로 이동시킨다.
	 * 메시지에 홑따옴표나 태그가 섞여도 스크립트가 깨지지 않도록 이스케이프한다.
	 */
	protected void alertAndGo(HttpServletRequest request, HttpServletResponse response,
							  String message, String pathWithinApp) throws IOException {

		response.setContentType("text/html;charset=UTF-8");   // 응답이 HTML 이라고 알린다. 아래에서 <script> 를 만들어 보낼 것이기 때문이다
		PrintWriter out = response.getWriter();   // 응답에 글자를 쓸 수 있는 붓을 얻는다
		out.print("<script>");   // 브라우저가 실행할 스크립트를 직접 만들어 보낸다
		out.print("alert('" + HtmlUtil.escapeJs(message) + "');");   // 알림창을 띄운다. escapeJs 를 거쳐야 홑따옴표가 섞여도 스크립트가 안 깨진다
		out.print("location.href='" + request.getContextPath() + pathWithinApp + "';");   // 알림창을 닫으면 지정한 주소로 이동시킨다
		out.print("</script>");   // 스크립트를 닫는다
		out.flush();   // 남아 있는 것을 마저 내보낸다
	}

	/** 알림창을 띄우고 이전 화면으로 되돌린다 */
	protected void alertAndBack(HttpServletResponse response, String message) throws IOException {

		response.setContentType("text/html;charset=UTF-8");   // 응답이 HTML 이라고 알린다
		PrintWriter out = response.getWriter();   // 응답에 글자를 쓸 수 있는 붓을 얻는다
		out.print("<script>");   // 브라우저가 실행할 스크립트를 만들어 보낸다
		out.print("alert('" + HtmlUtil.escapeJs(message) + "');");   // 알림창을 띄운다
		out.print("history.back();");   // 알림창을 닫으면 이전 화면으로 되돌린다 (입력하던 값이 살아 있다)
		out.print("</script>");   // 스크립트를 닫는다
		out.flush();   // 남아 있는 것을 마저 내보낸다
	}

	//===========================================================
	// 4. 로그인 정보 도우미
	//===========================================================

	/** 로그인한 아이디 (미로그인이면 null) */
	protected String getLoginId(HttpServletRequest request) {
		HttpSession session = request.getSession(false);   // false = "세션이 없으면 새로 만들지 마라". true 로 하면 비로그인에게도 세션이 생겨 낭비된다
		if (session == null) {   // 세션 자체가 없으면 로그인한 적이 없다는 뜻이다
			return null;   // 로그인 아이디가 없다는 표시로 null 을 돌려준다
		}
		Object id = session.getAttribute(SESSION_LOGIN_ID);   // 세션에 저장해 둔 로그인 아이디를 꺼낸다
		if (id instanceof String && !((String) id).trim().isEmpty()) {   // 글자 타입이고 비어 있지 않을 때만 진짜 로그인으로 인정한다
			return (String) id;   // Object 를 String 으로 바꿔 돌려준다
		}
		return null;   // 세션은 있는데 아이디가 없으면 비로그인이다
	}

	/**
	 * 로그인이 필수인 기능에서 사용한다.
	 * 미로그인이면 403 예외를 던진다.
	 *
	 * [중요] 화면에 넘어온 id 파라미터를 믿지 않고 "세션 값"만 사용한다.
	 *        기존 회원정보 수정 기능은 request.getParameter("id") 를 그대로 신뢰해서
	 *        로그인하지 않은 사람도 남의 비밀번호를 바꿀 수 있었다.
	 */
	protected String requireLoginId(HttpServletRequest request) {
		String id = getLoginId(request);   // 먼저 로그인 아이디를 꺼내 본다
		if (id == null) {   // 아이디가 없으면 = 로그인하지 않았으면
			throw new ForbiddenException("로그인이 필요한 기능입니다");   // 403 예외를 던진다. 위 handle() 이 받아 안내 화면으로 연결한다
		}
		return id;   // 로그인한 아이디를 돌려준다
	}

	//===========================================================
	// 5. 오류 응답
	//===========================================================
	private void sendProblem(HttpServletRequest request, HttpServletResponse response,
							 int status, String errorPage, String message)
			throws ServletException, IOException {

		// 이미 응답이 나가버린 뒤라면 더 손댈 수 없다
		if (response.isCommitted()) {
			return;
		}

		response.setStatus(status);   // 응답 상태 코드를 정한다 (400/403/404/500). 브라우저와 검색엔진이 이 값을 본다

		if (isAjax(request)) {   // AJAX 요청이면
			// AJAX 요청에 HTML 에러 페이지를 주면 화면에 깨진 문자열이 찍힌다
			writeText(response, message);
			return;   // 여기서 끝낸다. 아래 화면 이동은 하지 않는다
		}

		request.setAttribute("errorMessage", message);   // 오류 문구를 요청에 담아 화면이 꺼내 쓸 수 있게 한다
		request.setAttribute("errorStatus", Integer.valueOf(status));   // 상태 코드도 함께 담는다 (화면에 404/500 을 크게 보여주려고)
		request.getRequestDispatcher(errorPage).forward(request, response);   // 오류 안내 화면으로 넘긴다
	}

	private boolean isAjax(HttpServletRequest request) {   // 이 요청이 AJAX 인지(=화면 전체가 아니라 데이터만 원하는지) 판단한다
		String requestedWith = request.getHeader("X-Requested-With");   // jQuery 는 AJAX 요청에 이 헤더를 자동으로 붙인다
		if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {   // 그 값이 XMLHttpRequest 면 AJAX 가 맞다
			return true;   // AJAX 로 판정한다
		}
		String accept = request.getHeader("Accept");   // 헤더가 없을 수도 있으니 "어떤 형식을 원하는지" 도 확인한다
		return accept != null && accept.contains("application/json");   // JSON 을 원한다고 적혀 있으면 AJAX 로 본다
	}
}
