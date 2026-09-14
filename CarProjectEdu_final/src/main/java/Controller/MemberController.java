package Controller;

/*
 * ============================================================================
 *  MemberController  -  사장(Controller) : 회원 관련 요청을 가장 먼저 받는 서블릿
 *
 *  MVC 역할 분담
 *    고객(웹브라우저)  : 요청하는 사람
 *    사장(Controller)  : 요청을 받아 부장에게 일을 시키고, 결과 화면으로 응답한다
 *    부장(Service)     : 업무 규칙을 처리하고, DB 일은 사원에게 시킨다
 *    사원(DAO)         : DB 와 연결해서 SQL 을 실행한다
 *    상자(VO)          : 값을 담아 나르는 객체
 *
 *  이 사장이 받는 요청 주소 (2단계 주소)
 *    /join.me             회원가입 화면 보여주기
 *    /joinIdCheck.me      아이디 중복 확인 (join.js 의 fetch 가 요청)
 *    /joinPro.me          새 회원 추가(insert)
 *    /login.me            로그인 화면 보여주기
 *    /loginPro.me         로그인 처리
 *    /kakaoLogin.me       카카오 로그인 화면으로 보내기
 *    /kakaoCallback.me    카카오 로그인 결과 받기
 *    /logout.me           로그아웃
 *    /memberUpdate.me     회원정보 수정 화면 보여주기   (로그인 필요)
 *    /memberUpdatePro.me  회원정보 수정(update)          (로그인 필요)
 *    /memberDelete.me     회원 탈퇴(delete)               (로그인 필요)
 * ============================================================================
 */

// 입출력 예외, 응답에 글자를 쓰는 도구
import java.io.IOException;
import java.io.PrintWriter;

// 서블릿 기본 도구 (설정, 예외, 주소 연결, 요청, 응답, 세션)
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// 부장(MemberService), 상자(MemberVO), 카카오 로그인 도우미
import Service.MemberService;
import Vo.MemberVO;
import util.KakaoAuth;

// 고객이 /프로젝트명/member/ 로 시작하는 주소를 요청하면 이 사장(MemberController)이 받는다
@WebServlet("/member/*")
public class MemberController extends BaseController {

	// 직렬화 버전 번호 (HttpServlet 이 요구하는 값, 경고 방지용)
	private static final long serialVersionUID = 1L;

	// 부장(MemberService) 객체의 주소를 저장할 참조변수
	// transient : 서블릿을 파일로 저장(직렬화)할 때 제외한다 (init() 에서 다시 만들기 때문)
	transient MemberService memberService;

	//----------------------------------------------------------------
	// init : Tomcat 이 이 서블릿을 처음 만들 때 딱 1번 자동으로 부른다
	//----------------------------------------------------------------
	@Override
	public void init(ServletConfig config) throws ServletException {

		// 부장(MemberService) 객체를 만들어서 memberService 참조변수에 저장
		memberService = new MemberService();
	}

	//----------------------------------------------------------------
	// process : 부모 사장(BaseController)이 공통 검사를 끝낸 뒤 부르는 메소드
	//----------------------------------------------------------------
	@Override
	protected void process(String action, HttpServletRequest request, HttpServletResponse response) throws Exception {

		// 실제 요청 처리는 doHandle() 에서 한다
		doHandle(request, response);
	}

	//----------------------------------------------------------------
	// requiresLogin : 로그인해야만 쓸 수 있는 주소를 부모 사장(BaseController)에게 알려준다
	//  아래 3개 주소는 로그인하지 않으면 부모 사장이 403 에러 화면으로 막는다
	//----------------------------------------------------------------
	@Override
	protected boolean requiresLogin(String action) {
		return action.equals("/memberUpdate.me")
			|| action.equals("/memberUpdatePro.me")
			|| action.equals("/memberDelete.me");
	}

	//----------------------------------------------------------------
	// doHandle : 2단계 요청 주소에 따라 부장(MemberService)에게 일을 시키고 응답한다
	//----------------------------------------------------------------
	protected void doHandle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// 고객이 보낸 한글 입력값이 깨지지 않도록 요청 글자 방식을 UTF-8 로 정한다
		request.setCharacterEncoding("UTF-8");

		// 응답은 HTML 화면이고 한글은 UTF-8 이라고 브라우저에 알린다
		response.setContentType("text/html;charset=UTF-8");
		response.setCharacterEncoding("UTF-8");

		// 응답에 글자(alert 스크립트, fetch 응답 글자)를 쓸 수 있는 출력 도구를 얻는다
		PrintWriter out = response.getWriter();

		// 마지막에 포워딩할 화면 주소를 저장할 변수 (예: "/CarMain.jsp")
		String nextPage = null;

		// CarMain.jsp 가운데에 끼워 보여줄 화면 주소를 저장할 변수 (예: "members/join.jsp")
		String center = null;

		// 요청 주소 중 2단계 주소 얻기  예) /CarProject/member/join.me  ->  "/join.me"
		String action = request.getPathInfo();

		// 이클립스 콘솔에 2단계 요청 주소 출력 (화면이 안 뜰 때 여기부터 확인한다)
		System.out.println("클라이언트가 요청한 2단계 요청 주소  : " + action);

		// 2단계 요청 주소가 무엇인지에 따라 나누어 처리한다
		switch(action) {

			//=============================================================
			// 요청1. Top.jsp 의 회원가입 버튼 클릭 -> 회원가입 화면 보여주기
			//        요청 주소 : /member/join.me?center=members/join.jsp
			//=============================================================
			case "/join.me":

				//부장(MemberService)에게 시키기 : 요청 주소의 center 값(members/join.jsp)을 꺼내 CarMain.jsp 가운데에 보여줄 화면 주소 얻기 명령
				center = memberService.serviceJoinName(request);

				// CarMain.jsp 가 가운데 화면으로 끼워 넣을 수 있도록 center 값을 request 에 저장
				request.setAttribute("center", center);

				// 포워딩할 메인 화면 주소 저장
				nextPage = "/CarMain.jsp";

				break;

			//=============================================================
			// 요청2. join.jsp 에서 아이디 입력칸을 벗어남 -> 아이디 중복 확인
			//        요청 주소 : /member/joinIdCheck.me  (js/join.js 의 fetch 가 POST 로 요청)
			//=============================================================
			case "/joinIdCheck.me":

				//부장(MemberService)에게 시키기 : join.jsp 에서 입력한 아이디가 DB 의 member 테이블에 이미 있는지 확인 명령
				// 반환값 -> true(이미 있음 = 중복) / false(없음 = 사용 가능)
				boolean result = memberService.serviceOverLappedId(request);

				// 중복이면 "not_usable" 글자를, 아니면 "usable" 글자를 join.js 의 fetch 로 응답
				if(result == true) {
					out.write("not_usable");
				} else {
					out.write("usable");
				}

				// 화면 이동 없이 글자만 응답하므로 여기서 메소드를 끝낸다
				return;

			//=============================================================
			// 요청3. join.jsp 의 회원가입하기 버튼 클릭 -> 새 회원 추가(insert)
			//        요청 주소 : /member/joinPro.me  (form 이 POST 로 전송)
			//=============================================================
			case "/joinPro.me":

				//부장(MemberService)에게 시키기 : members/join.jsp 화면에서 가입을 위해 입력한 정보들이 저장된 request 객체를 MemberService 의 serviceInsertMember(request) 호출해서 새 회원 추가(insert) 작업 명령
				boolean joined = memberService.serviceInsertMember(request);

				// 가입에 실패했으면 (false) 실패 안내창을 띄우고 이전 화면(join.jsp)으로 되돌린다
				if(!joined) {
					out.println("<script>");
					out.println(" alert('회원가입에 실패했습니다. 아이디를 다시 확인해주세요.');");
					out.println(" history.back();");
					out.println("</script>");
					return;
				}

				// 가입에 성공했으면 메인 화면을 다시 요청하는 주소 저장
				nextPage = "/Car/Main";

				break;

			//=============================================================
			// 요청4. Top.jsp 의 로그인 버튼 클릭 -> 로그인 화면 보여주기
			//        요청 주소 : /member/login.me
			//=============================================================
			case "/login.me":

				//부장(MemberService)에게 시키기 : CarMain.jsp 가운데에 보여줄 로그인 화면 주소("members/login.jsp") 얻기 명령
				center = memberService.serviceLoginMember();

				// CarMain.jsp 가 가운데 화면으로 끼워 넣을 수 있도록 center 값을 request 에 저장
				request.setAttribute("center", center);

				// 포워딩할 메인 화면 주소 저장
				nextPage = "/CarMain.jsp";

				break;

			//=============================================================
			// 요청5. login.jsp 의 로그인 버튼 클릭 -> 로그인 처리
			//        요청 주소 : /member/loginPro.me  (form 이 POST 로 전송)
			//=============================================================
			case "/loginPro.me":

				//부장(MemberService)에게 시키기 : login.jsp 에서 입력한 아이디, 비밀번호가 DB 의 member 테이블에 있는지 확인하는 로그인 처리 명령
				// 반환값 ->  1 : 아이디, 비밀번호 모두 맞음 (로그인 성공)
				//            0 : 아이디는 맞고 비밀번호가 틀림
				//           -1 : 아이디가 없음
				int check = memberService.serviceUserCheck(request);

				// 비밀번호가 틀렸으면 안내창을 띄우고 이전 화면(login.jsp)으로 되돌린다
				if(check == 0) {
					out.println("<script>");
					out.println(" window.alert('비밀번호 틀림'); ");
					out.println(" history.go(-1);");
					out.println("</script>");
					return;

				// 아이디가 없으면 안내창을 띄우고 이전 화면(login.jsp)으로 되돌린다
				} else if(check == -1) {
					out.println("<script>");
					out.println(" window.alert('아이디 틀림'); ");
					out.println(" history.go(-1);");
					out.println("</script>");
					return;
				}

				// 로그인 성공(1)이면 메인 화면 주소 저장
				// -> CarMain.jsp 안의 Top.jsp 가 세션의 로그인 아이디를 꺼내 "로그아웃" 버튼을 보여준다
				nextPage = "/CarMain.jsp";

				break;

			//=============================================================
			// 요청6. login.jsp 의 카카오로 시작하기 버튼 클릭 -> 카카오 로그인 화면으로 보내기
			//        요청 주소 : /member/kakaoLogin.me
			//=============================================================
			case "/kakaoLogin.me": {

				// 카카오 REST API 키를 아직 설정하지 않았으면 안내창을 띄우고 이전 화면으로 되돌린다
				if (!KakaoAuth.isConfigured()) {
					out.println("<script>");
					out.println(" alert('카카오 로그인이 아직 설정되지 않았습니다.\\n"
							+ "WEB-INF/app.properties 의 kakao.rest.api.key 를 확인해주세요.');");
					out.println(" history.back();");
					out.println("</script>");
					return;
				}

				try {

					// 카카오에서 돌아올 때 우리 사이트에서 보낸 요청이 맞는지 확인할 무작위 글자(state) 만들기
					String state = KakaoAuth.newState();

					// 이클립스 콘솔에 만든 state 값 출력
					System.out.println("MemberController String stat=" + state);

					// 나중에 비교할 수 있도록 state 값을 세션에 저장
					request.getSession().setAttribute("KAKAO_STATE", state);

					// 고객(웹브라우저)을 카카오 로그인 화면 주소로 이동시킨다
					response.sendRedirect(KakaoAuth.buildAuthorizeUrl(state));

					// 이클립스 콘솔에 이동시킨 카카오 로그인 주소 출력
					System.out.println(KakaoAuth.buildAuthorizeUrl(state));

				// 카카오 로그인 주소를 만들다가 오류가 나면 안내창을 띄우고 이전 화면으로 되돌린다
				} catch (Exception e) {
					System.out.println("[MemberController] 카카오 인증 주소 생성 실패 : " + e.getMessage());
					out.println("<script>alert('카카오 로그인 준비 중 오류가 발생했습니다.'); history.back();</script>");
				}
				return;
			}

			//=============================================================
			// 요청7. 카카오 로그인을 마친 카카오 서버가 고객을 우리 사이트로 돌려보냄
			//        요청 주소 : /member/kakaoCallback.me?code=...&state=...
			//=============================================================
			case "/kakaoCallback.me": {

				// 고객이 카카오 화면에서 취소를 누르면 error 값이 함께 온다
				String kakaoError = request.getParameter("error");

				// 취소했으면 안내창을 띄우고 로그인 화면으로 이동시킨다
				if (kakaoError != null) {
					out.println("<script>");
					out.println(" alert('카카오 로그인이 취소되었습니다.');");
					out.println(" location.href='" + request.getContextPath() + "/member/login.me';");
					out.println("</script>");
					return;
				}

				// 세션에 저장해 둔 state 값과 카카오가 돌려준 state 값을 꺼낸다
				HttpSession kakaoSession = request.getSession(false);
				String savedState = (kakaoSession == null) ? null : (String)kakaoSession.getAttribute("KAKAO_STATE");
				String returnedState = request.getParameter("state");

				// 두 값이 다르면 우리 사이트에서 시작한 로그인이 아니므로 403 으로 막는다
				if (savedState == null || !savedState.equals(returnedState)) {
					System.out.println("[MemberController] 카카오 state 불일치 - 요청 차단");
					response.sendError(HttpServletResponse.SC_FORBIDDEN);
					return;
				}

				// 확인이 끝난 state 값은 세션에서 지운다 (한 번만 쓰는 값)
				kakaoSession.removeAttribute("KAKAO_STATE");

				// 카카오가 보내준 인가 코드(code) 꺼내기. 없으면 잘못된 요청(400)으로 막는다
				String code = request.getParameter("code");
				if (code == null || code.trim().isEmpty()) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST);
					return;
				}

				try {

					// 인가 코드로 카카오 서버에 접근 토큰(accessToken)을 받아온다
					String accessToken = KakaoAuth.requestAccessToken(code);

					// 접근 토큰으로 카카오 서버에 회원 정보(번호, 닉네임, 이메일)를 받아온다
					KakaoAuth.KakaoUser kakaoUser = KakaoAuth.requestUserInfo(accessToken);

					//부장(MemberService)에게 시키기 : 카카오에서 받은 회원 정보가 저장된 kakaoUser 객체를 MemberService 의 serviceKakaoLogin(request, kakaoUser) 호출해서 처음이면 자동 가입 후 로그인 처리 명령
					memberService.serviceKakaoLogin(request, kakaoUser);

					// 로그인이 끝났으면 메인 화면으로 이동시킨다
					response.sendRedirect(request.getContextPath() + "/Car/Main");

				// 카카오 서버와 통신 중 오류가 나면 안내창을 띄우고 로그인 화면으로 이동시킨다
				} catch (Exception e) {

					System.out.println("[MemberController] 카카오 로그인 실패 : " + e.getMessage());
					out.println("<script>");
					out.println(" alert('카카오 로그인에 실패했습니다. 잠시 후 다시 시도해주세요.');");
					out.println(" location.href='" + request.getContextPath() + "/member/login.me';");
					out.println("</script>");
				}
				return;
			}

			//=============================================================
			// 요청8. Top.jsp 의 로그아웃 버튼 클릭 -> 로그아웃
			//        요청 주소 : /member/logout.me
			//=============================================================
			case "/logout.me":

				//부장(MemberService)에게 시키기 : 세션에 저장된 로그인 정보를 모두 지우는 로그아웃 처리 명령
				memberService.serviceLogout(request);

				// 포워딩할 메인 화면 주소 저장
				nextPage = "/CarMain.jsp";

				break;

			//=============================================================
			// 요청9. Top.jsp 의 회원정보 수정 버튼 클릭 -> 회원정보 수정 화면 보여주기  (로그인 필요)
			//        요청 주소 : /member/memberUpdate.me
			//=============================================================
			case "/memberUpdate.me":

				// 세션에서 로그인 아이디 꺼내기 (로그인 여부는 부모 사장이 이미 검사했다)
				HttpSession updateSession = request.getSession();
				String updateId = (String)updateSession.getAttribute("id");

				//부장(MemberService)에게 시키기 : 로그인 아이디를 MemberService 의 serviceMemberDetail(updateId) 호출해서 DB 에 저장된 내 회원 정보 조회(select) 명령
				MemberVO updateVo = memberService.serviceMemberDetail(updateId);

				// memberUpdate.jsp 에서 꺼내 쓸 수 있도록 조회한 회원 정보를 request 에 저장
				request.setAttribute("membervo", updateVo);

				// CarMain.jsp 가운데에 보여줄 화면 주소를 request 에 저장
				request.setAttribute("center", "members/memberUpdate.jsp");

				// 포워딩할 메인 화면 주소 저장
				nextPage = "/CarMain.jsp";

				break;

			//=============================================================
			// 요청10. memberUpdate.jsp 의 수정하기 버튼 클릭 -> 회원정보 수정(update)  (로그인 필요)
			//         요청 주소 : /member/memberUpdatePro.me  (CarApp.postForm 이 POST 로 요청)
			//=============================================================
			case "/memberUpdatePro.me":

				// 세션에서 로그인 아이디 꺼내기 (로그인 여부는 부모 사장이 이미 검사했다)
				String proLoginId = (String)request.getSession(false).getAttribute("id");

				//부장(MemberService)에게 시키기 : memberUpdate.jsp 에서 입력한 정보가 저장된 request 객체와 로그인 아이디를 MemberService 의 serviceUpdateMember(request, proLoginId) 호출해서 회원정보 수정(update) 작업 명령
				// 반환값 -> "수정성공" / "수정실패" / "현재비밀번호불일치" / "비밀번호길이부족"
				String updateResult = memberService.serviceUpdateMember(request, proLoginId);

				// 결과 글자를 memberUpdate.jsp 의 CarApp.postForm 으로 응답하고 메소드를 끝낸다
				out.write(updateResult);
				return;

			//=============================================================
			// 요청11. memberUpdate.jsp 의 회원탈퇴 버튼 클릭 -> 회원 탈퇴(delete)  (로그인 필요)
			//         요청 주소 : /member/memberDelete.me  (CarApp.postForm 이 POST 로 요청)
			//=============================================================
			case "/memberDelete.me":

				// 세션에서 로그인 아이디 꺼내기 (로그인 여부는 부모 사장이 이미 검사했다)
				HttpSession deleteSession = request.getSession(false);
				String deleteId = (String)deleteSession.getAttribute("id");

				//부장(MemberService)에게 시키기 : 로그인 아이디를 MemberService 의 serviceDeleteMember(deleteId) 호출해서 회원 탈퇴(delete) 작업 명령
				// 반환값 -> "삭제성공" / "삭제실패"
				String deleteResult = memberService.serviceDeleteMember(deleteId);

				// 탈퇴에 성공했으면 세션의 로그인 아이디를 지워서 로그아웃 상태로 만든다
				if(deleteResult.equals("삭제성공")) {
					deleteSession.removeAttribute("id");
				}

				// 결과 글자를 memberUpdate.jsp 의 CarApp.postForm 으로 응답하고 메소드를 끝낸다
				out.write(deleteResult);
				return;

			// 위에 없는 2단계 주소라면 아무것도 하지 않는다 (아래에서 404 로 응답)
			default:
				break;
		}

		// 포워딩할 화면 주소가 정해지지 않았다면 없는 주소이므로 404 로 응답한다
		if(nextPage == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		// nextPage 에 저장된 화면 주소로 포워딩해서 고객(웹브라우저)에게 응답한다
		request.getRequestDispatcher(nextPage).forward(request, response);
	}
}
