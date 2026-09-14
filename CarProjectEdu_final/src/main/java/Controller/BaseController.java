package Controller;

/*
 * ============================================================================
 *  BaseController  -  모든 사장(Controller)의 부모 클래스
 *
 *  하는 일
 *    1. 고객(웹브라우저)의 요청을 GET 이든 POST 든 한 곳(handle)으로 모은다
 *    2. 로그인이 필요한 요청인지 먼저 검사한다
 *    3. 자식 사장(MemberController 등)의 process() 를 불러 실제 일을 시킨다
 *    4. 처리 중 예외가 나면 알맞은 에러 화면(400/403/404/500)으로 응답한다
 *
 *  요청 흐름
 *    고객 요청 -> doGet() 또는 doPost() -> handle() -> 자식 사장의 process()
 *                                            |
 *                                            +-- 예외 발생 -> sendProblem() -> 에러 화면
 * ============================================================================
 */

// 입출력 예외, 응답에 글자를 쓰는 도구
import java.io.IOException;
import java.io.PrintWriter;

// 서블릿 기본 도구 (요청, 응답, 세션)
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// 우리가 직접 만든 예외 클래스들
import exception.ForbiddenException;
import exception.InvalidInputException;
import exception.NotFoundException;

// abstract : 이 클래스는 직접 객체로 만들 수 없고, 자식 사장이 상속해서만 쓴다
public abstract class BaseController extends HttpServlet {

	// 직렬화 버전 번호 (HttpServlet 이 요구하는 값, 경고 방지용)
	private static final long serialVersionUID = 1L;

	// 로그인에 성공하면 세션에 아이디를 저장할 때 쓰는 이름 -> session.setAttribute("id", 아이디)
	protected static final String SESSION_LOGIN_ID = "id";

	//----------------------------------------------------------------
	// doGet : 고객이 GET 방식(주소창 입력, 링크 클릭)으로 요청하면 Tomcat 이 자동으로 부른다
	//----------------------------------------------------------------
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// GET 요청을 공통 처리 메소드 handle() 로 넘긴다
		handle(request, response);
	}

	//----------------------------------------------------------------
	// doPost : 고객이 POST 방식(form 전송, fetch POST)으로 요청하면 Tomcat 이 자동으로 부른다
	//----------------------------------------------------------------
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// POST 요청도 공통 처리 메소드 handle() 로 넘긴다
		handle(request, response);
	}

	//----------------------------------------------------------------
	// handle : GET/POST 요청을 모두 받아서 공통 검사 후 자식 사장에게 일을 시킨다
	//----------------------------------------------------------------
	private void handle(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// 요청 주소 중 2단계 주소 얻기  예) /CarProject/member/join.me  ->  "/join.me"
		String action = request.getPathInfo();

		try {

			// 2단계 주소가 없으면 (예: /CarProject/member) 없는 주소이므로 404 예외를 던진다
			if (action == null || action.equals("/")) {
				throw new NotFoundException("요청 주소가 올바르지 않습니다");
			}

			// 이클립스 콘솔에 어떤 사장이 어떤 주소를 받았는지 출력  예) [MemberController] 요청 주소 : /join.me
			System.out.println("[" + getClass().getSimpleName() + "] 요청 주소 : " + action);

			// 로그인이 필요한 주소라면, 로그인했는지 먼저 검사한다 (안 했으면 403 예외)
			if (requiresLogin(action)) {
				requireLoginId(request);
			}

			// 자식 사장(MemberController 등)의 process() 를 불러 실제 일을 시킨다
			process(action, request, response);

		// 입력값이 잘못된 경우 -> 400 (요청 잘못)
		} catch (InvalidInputException e) {

			sendProblem(request, response, HttpServletResponse.SC_BAD_REQUEST,
					"/error/error.jsp", e.getMessage());

		// 권한이 없는 경우 (로그인 안 함, 남의 글 수정 등) -> 403 (금지)
		} catch (ForbiddenException e) {

			sendProblem(request, response, HttpServletResponse.SC_FORBIDDEN,
					"/error/error.jsp", e.getMessage());

		// 찾는 대상이 없는 경우 (없는 주소, 없는 글번호 등) -> 404 (없음)
		} catch (NotFoundException e) {

			sendProblem(request, response, HttpServletResponse.SC_NOT_FOUND,
					"/error/404.jsp", e.getMessage());

		// 그 밖의 모든 예외 (DB 오류 등) -> 500 (서버 오류)
		} catch (Exception e) {

			// 개발자가 원인을 찾을 수 있도록 콘솔에 오류 내용을 출력한다
			System.out.println("[" + getClass().getSimpleName() + "] 처리 중 오류 : " + action);
			e.printStackTrace();

			// 고객에게는 자세한 오류 내용 대신 일반 안내 문구만 보여준다
			sendProblem(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"/error/500.jsp", "요청을 처리하는 중 문제가 발생했습니다.");
		}
	}

	//----------------------------------------------------------------
	// process : 자식 사장이 반드시 만들어야 하는 메소드 (실제 요청 처리 내용)
	//----------------------------------------------------------------
	protected abstract void process(String action, HttpServletRequest request, HttpServletResponse response)
			throws Exception;

	//----------------------------------------------------------------
	// requiresLogin : 이 주소가 로그인이 필요한 주소인지 알려준다
	//  기본값은 false(필요 없음). 로그인이 필요한 사장은 이 메소드를 덮어써서 true 를 돌려준다
	//----------------------------------------------------------------
	protected boolean requiresLogin(String action) {
		return false;
	}

	//----------------------------------------------------------------
	// writeText : 고객(웹브라우저)에게 화면(HTML) 대신 글자만 응답한다 (fetch 요청의 응답용)
	//----------------------------------------------------------------
	protected void writeText(HttpServletResponse response, String text) throws IOException {

		// 응답 내용이 일반 글자(text/plain)이고 한글은 UTF-8 이라고 브라우저에 알린다
		response.setContentType("text/plain;charset=UTF-8");

		// 응답에 글자를 쓸 수 있는 출력 도구를 얻는다
		PrintWriter out = response.getWriter();

		// 글자가 null 이면 빈 글자("")를, 아니면 그 글자를 응답에 쓴다
		out.write(text == null ? "" : text);

		// 쓴 글자를 즉시 브라우저로 내보낸다
		out.flush();
	}

	//----------------------------------------------------------------
	// writeJson : 고객(웹브라우저)에게 JSON 형식의 글자를 응답한다 (fetch 요청의 응답용)
	//----------------------------------------------------------------
	protected void writeJson(HttpServletResponse response, String json) throws IOException {

		// 응답 내용이 JSON 이고 한글은 UTF-8 이라고 브라우저에 알린다
		response.setContentType("application/json;charset=UTF-8");

		// 응답에 글자를 쓸 수 있는 출력 도구를 얻는다
		PrintWriter out = response.getWriter();

		// JSON 이 null 이면 빈 JSON("{}")을, 아니면 그 JSON 을 응답에 쓴다
		out.write(json == null ? "{}" : json);

		// 쓴 글자를 즉시 브라우저로 내보낸다
		out.flush();
	}

	//----------------------------------------------------------------
	// getLoginId : 세션에 저장된 로그인 아이디를 꺼낸다. 로그인 안 했으면 null
	//----------------------------------------------------------------
	protected String getLoginId(HttpServletRequest request) {

		// 이미 있는 세션만 꺼낸다 (false : 세션이 없으면 새로 만들지 말고 null 을 달라)
		HttpSession session = request.getSession(false);

		// 세션 자체가 없으면 로그인한 적이 없으므로 null 반환
		if (session == null) {
			return null;
		}

		// 세션에서 "id" 이름으로 저장된 로그인 아이디를 꺼낸다
		Object id = session.getAttribute(SESSION_LOGIN_ID);

		// 꺼낸 값이 글자(String)이고 비어 있지 않으면 로그인 아이디로 인정해서 반환
		if (id instanceof String && !((String) id).trim().isEmpty()) {
			return (String) id;
		}

		// 그 외에는 로그인하지 않은 것으로 보고 null 반환
		return null;
	}

	//----------------------------------------------------------------
	// requireLoginId : 로그인 아이디를 꺼내되, 로그인 안 했으면 403 예외를 던진다
	//----------------------------------------------------------------
	protected String requireLoginId(HttpServletRequest request) {

		// 세션에서 로그인 아이디를 꺼낸다
		String id = getLoginId(request);

		// 로그인하지 않았으면 403 예외를 던진다 -> handle() 의 catch 가 받아서 에러 화면을 보여준다
		if (id == null) {
			throw new ForbiddenException("로그인이 필요한 기능입니다");
		}

		// 로그인했으면 아이디를 반환
		return id;
	}

	//----------------------------------------------------------------
	// sendProblem : 예외가 났을 때 고객에게 에러 응답을 보낸다
	//  fetch 요청이면 글자로, 일반 화면 요청이면 에러 JSP 화면으로 응답한다
	//----------------------------------------------------------------
	private void sendProblem(HttpServletRequest request, HttpServletResponse response,
							 int status, String errorPage, String message)
			throws ServletException, IOException {

		// 이미 응답이 브라우저로 나가기 시작했다면 더 이상 바꿀 수 없으므로 그냥 끝낸다
		if (response.isCommitted()) {
			return;
		}

		// 응답 상태 번호를 정한다  예) 400, 403, 404, 500
		response.setStatus(status);

		// fetch(비동기) 요청이면 화면 대신 오류 문구만 글자로 응답하고 끝낸다
		if (isAjax(request)) {

			writeText(response, message);
			return;
		}

		// 에러 화면(JSP)에서 꺼내 쓸 수 있도록 오류 문구와 상태 번호를 request 에 저장한다
		request.setAttribute("errorMessage", message);
		request.setAttribute("errorStatus", Integer.valueOf(status));

		// 에러 화면(JSP)으로 포워딩한다  예) /error/404.jsp
		request.getRequestDispatcher(errorPage).forward(request, response);
	}

	//----------------------------------------------------------------
	// isAjax : 이 요청이 화면 이동이 아닌 fetch(비동기) 요청인지 알려준다
	//----------------------------------------------------------------
	private boolean isAjax(HttpServletRequest request) {

		// 요청 머리말(헤더)의 X-Requested-With 값을 꺼낸다 (js/app.js 의 CarApp.postForm 이 붙여 보낸다)
		String requestedWith = request.getHeader("X-Requested-With");

		// 값이 XMLHttpRequest 이면 비동기 요청이다
		if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
			return true;
		}

		// 요청 머리말의 Accept 값에 application/json 이 들어 있어도 비동기 요청으로 본다
		String accept = request.getHeader("Accept");
		return accept != null && accept.contains("application/json");
	}
}
