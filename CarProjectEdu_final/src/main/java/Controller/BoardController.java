package Controller;

/*
 * ============================================================================
 *  BoardController  -  사장(Controller) : 자유게시판 관련 요청을 가장 먼저 받는 서블릿
 *
 *  MVC 역할 분담
 *    고객(웹브라우저)  : 요청하는 사람
 *    사장(Controller)  : 요청을 받아 부장에게 일을 시키고, 결과 화면으로 응답한다
 *    부장(Service)     : 업무 규칙을 처리하고, DB 일은 사원에게 시킨다
 *    사원(DAO)         : DB 와 연결해서 SQL 을 실행한다
 *    상자(VO)          : 값을 담아 나르는 객체
 *
 *  이 사장이 받는 요청 주소 (2단계 주소)
 *    /list.bo         전체 글목록 조회 (페이징)
 *    /searchlist.bo   검색 기준 + 검색어로 글목록 조회 (페이징)
 *    /read.bo         글 1건 상세 조회 (+ 조회수 1 증가)                (로그인 필요)
 *    /write.bo        글쓰기 화면 보여주기                              (로그인 필요)
 *    /writePro.bo     새 글 등록 (AJAX)                                (로그인 필요)
 *    /password.do     글 비밀번호 확인 (AJAX, 수정·삭제 버튼 활성화용)   (로그인 필요)
 *    /updateBoard.do  글 수정 (AJAX)                                   (로그인 필요)
 *    /deleteBoard.do  글 삭제 (AJAX)                                   (로그인 필요)
 *    /reply.do        답글 작성 화면 보여주기                          (로그인 필요)
 *    /replyPro.do     답글 등록                                       (로그인 필요)
 *    /comment*.do     댓글 목록·등록·수정·삭제·추천 (모두 JSON 응답)     (조회 제외 로그인 필요)
 *
 *  [댓글 요청만 별도로 처리하는 이유]
 *    댓글은 자바스크립트가 fetch 로 호출하고 응답을 JSON 으로 읽는다.
 *    그런데 부장(Service)이 던지는 예외(로그인 필요, 권한 없음, 빈 내용 등)를
 *    그대로 두면 web.xml 의 error-page 설정에 따라 500 HTML 오류 화면이
 *    그대로 돌아온다. 자바스크립트가 response.json() 으로 그 HTML 을
 *    읽으려다 실패하면, 화면에는 "댓글 등록 실패" 같은 애매한 문구만 남고
 *    진짜 이유(예: "본인이 쓴 댓글만 수정할 수 있습니다")를 알 수 없다.
 *    그래서 댓글 주소(/comment 로 시작)만 따로 감싸서, 업무 예외의 메시지를
 *    그대로 JSON 으로 내려준다. 다만 예상하지 못한 오류(NullPointerException
 *    등)의 내용까지 그대로 내려보내면 내부 구조가 드러나 공격에 쓰일 수
 *    있으므로, 그런 오류의 자세한 내용은 서버 콘솔에만 남기고 화면에는
 *    무난한 안내 문구만 보낸다.
 * ============================================================================
 */

// 입출력 예외, 응답에 글자를 쓰는 도구
import java.io.IOException;
import java.io.PrintWriter;

// 여러 건의 글/댓글을 순서대로 담을 목록 도구
import java.util.List;

// 서블릿 기본 도구 (설정, 예외, 주소 연결, 요청, 응답, 세션)
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// 부장(BoardService, CommentService), 상자(BoardVo, CommentVo, MemberVO, PageResult)
import Service.BoardService;
import Service.CommentService;
import Vo.BoardVo;
import Vo.CommentVo;
import Vo.MemberVO;
import Vo.PageResult;

// 요청 파라미터를 안전하게 꺼내는 도우미
import util.ParamUtil;

// 고객이 /프로젝트명/Board/ 로 시작하는 주소를 요청하면 이 사장(BoardController)이 받는다
@WebServlet("/Board/*")
public class BoardController extends BaseController {

	// 직렬화 버전 번호 (HttpServlet 이 요구하는 값, 경고 방지용)
	private static final long serialVersionUID = 1L;

	// 부장(BoardService) 객체의 주소를 저장할 참조변수
	// transient : 서블릿을 파일로 저장(직렬화)할 때 제외한다 (init() 에서 다시 만들기 때문)
	private transient BoardService boardService;

	// 부장(CommentService) 객체의 주소를 저장할 참조변수 (무한 대댓글 + 추천 기능 담당)
	private transient CommentService commentService;

	//----------------------------------------------------------------
	// init : Tomcat 이 이 서블릿을 처음 만들 때 딱 1번 자동으로 부른다
	//----------------------------------------------------------------
	@Override
	public void init(ServletConfig config) throws ServletException {

		// 부장(BoardService, CommentService) 객체를 만들어서 참조변수에 저장
		boardService = new BoardService();
		commentService = new CommentService();
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
	//  목록 조회·검색은 누구나 볼 수 있고, "글을 쓰고 고치고 지우는" 주소와
	//  글을 읽는 주소(read.bo), 댓글 작성 관련 주소만 로그인을 요구한다
	//----------------------------------------------------------------
	@Override
	protected boolean requiresLogin(String action) {
		return action.equals("/write.bo")
			|| action.equals("/writePro.bo")
			|| action.equals("/reply.do")
			|| action.equals("/replyPro.do")
			|| action.equals("/password.do")
			|| action.equals("/updateBoard.do")
			|| action.equals("/deleteBoard.do")
			|| action.equals("/read.bo")
			|| action.equals("/commentAdd.do")
			|| action.equals("/commentEdit.do")
			|| action.equals("/commentDelete.do")
			|| action.equals("/commentLike.do");
	}

	//----------------------------------------------------------------
	// doHandle : 2단계 요청 주소에 따라 부장에게 일을 시키고 응답한다
	//----------------------------------------------------------------
	protected void doHandle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// 고객이 보낸 한글 입력값이 깨지지 않도록 요청 글자 방식을 UTF-8 로 정한다
		request.setCharacterEncoding("UTF-8");

		// 응답은 HTML 화면이고 한글은 UTF-8 이라고 브라우저에 알린다
		response.setContentType("text/html;charset=UTF-8");
		response.setCharacterEncoding("UTF-8");

		// 응답에 글자(AJAX 결과 등)를 쓸 수 있는 출력 도구를 얻는다
		PrintWriter out = response.getWriter();

		// 마지막에 포워딩할 화면 주소를 저장할 변수 (예: "/CarMain.jsp")
		String nextPage = null;

		// 요청 주소 중 2단계 주소 얻기  예) /CarProject/Board/list.bo  ->  "/list.bo"
		String action = request.getPathInfo();

		//=============================================================
		// 댓글 관련 요청(/comment... 로 시작)은 별도 메소드로 넘기고, 여기서 끝낸다
		// (자세한 이유는 위 클래스 설명 [댓글 요청만 별도로 처리하는 이유] 참고)
		//=============================================================
		if (action != null && action.startsWith("/comment")) {

			try {
				handleComment(action, request, response, out);

			} catch (exception.ForbiddenException e) {
				writeJsonError(out, e.getMessage());   // 권한이 없다는 뜻을 JSON 으로 내려보낸다

			} catch (exception.NotFoundException e) {
				writeJsonError(out, e.getMessage());   // 찾는 것이 없다는 뜻을 JSON 으로 내려보낸다

			} catch (exception.InvalidInputException e) {
				writeJsonError(out, e.getMessage());   // 입력이 잘못됐다는 뜻을 JSON 으로 내려보낸다

			} catch (Exception e) {
				// 예상하지 못한 오류는 원인을 서버 콘솔에만 자세히 남기고, 화면에는 무난한 안내만 보낸다
				System.out.println("[BoardController] 댓글 처리 오류 : " + e);
				e.printStackTrace();
				writeJsonError(out, "처리 중 문제가 발생했습니다. 잠시 후 다시 시도해주세요.");
			}
			return;   // 댓글 요청은 여기서 끝낸다. 아래 게시판 처리로 내려가면 안 된다
		}

		// 이클립스 콘솔에 2단계 요청 주소 출력 (화면이 안 뜰 때 여기부터 확인한다)
		System.out.println("클라이언트가 요청한 2단계 요청 주소  : " + action);

		// 2단계 주소가 없으면 (예: /CarProject/Board 로만 접근) 없는 주소이므로 404 로 응답한다
		if (action == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		// 2단계 요청 주소가 무엇인지에 따라 나누어 처리한다
		switch (action) {

			//=============================================================
			// 요청1. reply.jsp 의 답변등록 버튼 클릭 -> 답글 등록
			//        요청 주소 : /Board/replyPro.do  (form 이 POST 로 전송)
			//=============================================================
			case "/replyPro.do": {

				// reply.jsp 화면에서 입력한 답글 정보 꺼내기
				String super_b_idx = request.getParameter("super_b_idx"); // 답글을 달 원글(부모)의 글번호
				String reply_id = request.getParameter("id");             // 로그인한 답글 작성자 아이디
				String reply_name = request.getParameter("writer");       // 답글 작성자 이름
				String reply_email = request.getParameter("email");       // 답글 작성자 이메일
				String reply_title = request.getParameter("title");       // 답글 제목
				String reply_content = request.getParameter("content");   // 답글 내용
				String reply_pass = request.getParameter("pass");         // 답글 비밀번호

				//부장(BoardService)에게 시키기 : 원글 번호와 답글 작성 정보를 BoardService 의 serviceReplyInsertBoard(...) 호출해서 계층형 정렬 규칙에 맞게 답글 추가(insert) 작업 명령
				boardService.serviceReplyInsertBoard(super_b_idx, reply_id, reply_name,
													  reply_email, reply_title, reply_content, reply_pass);

				// 답글 등록이 끝나면 전체 글목록 화면(/list.bo)으로 다시 요청해 최신 목록을 보여준다
				nextPage = "/Board/list.bo";
				break;
			}

			//=============================================================
			// 요청2. read.jsp 의 답변 버튼 클릭 -> 답글 작성 화면 보여주기
			//        요청 주소 : /Board/reply.do?b_idx=글번호
			//=============================================================
			case "/reply.do": {

				String parentIdx = request.getParameter("b_idx");   // 답글을 달 원글(부모)의 글번호
				String replyLoginId = request.getParameter("id");   // 답글을 작성하는 로그인 아이디

				//부장(BoardService)에게 시키기 : 로그인 아이디를 BoardService 의 serviceMemberOne(replyLoginId) 호출해서 답글 작성 화면에 미리 채울 회원 정보(이름·이메일) 조회(select) 명령
				MemberVO reply_vo = boardService.serviceMemberOne(replyLoginId);

				// 답글 작성 화면(board/reply.jsp)에서 꺼내 쓸 수 있도록 원글 번호와 회원 정보를 request 에 저장
				request.setAttribute("b_idx", parentIdx);
				request.setAttribute("vo", reply_vo);

				// CarMain.jsp 가운데에 보여줄 화면 주소를 request 에 저장
				request.setAttribute("center", "board/reply.jsp");

				// 포워딩할 메인 화면 주소 저장
				nextPage = "/CarMain.jsp";
				break;
			}

			//=============================================================
			// 요청3. write.jsp 의 등록 버튼 클릭 -> 새 글 등록
			//        요청 주소 : /Board/writePro.bo  (CarApp.postForm 이 POST 로 요청)
			//        응답      : "1"(성공) 또는 "0"(실패) 글자만 응답 (AJAX)
			//=============================================================
			case "/writePro.bo": {

				// write.jsp 화면에서 입력한 새 글 정보 꺼내기 (입력칸 name 속성이 짧은 한 글자로 되어 있다)
				String writer = request.getParameter("w");   // 작성자 이름
				String id = request.getParameter("i");       // 작성자 아이디
				String email = request.getParameter("e");    // 작성자 이메일
				String title = request.getParameter("t");    // 글 제목
				String content = request.getParameter("c");  // 글 내용
				String pass = request.getParameter("p");     // 글 비밀번호

				//부장(BoardService)에게 시키기 : 입력한 새 글 정보를 BoardService 의 serviceInsertBoard(...) 호출해서 새 글 추가(insert) 작업 명령
				// 반환값 -> 1 : 추가 성공 / 0 : 추가 실패(비밀번호가 비어 있음)
				int result = boardService.serviceInsertBoard(writer, id, email, title, content, pass);

				// 결과 숫자(1 또는 0)를 글자로 바꿔서 write.jsp 의 CarApp.postForm 으로 응답하고 메소드를 끝낸다
				out.write(String.valueOf(result));
				return;
			}

			//=============================================================
			// 요청4. read.jsp 의 삭제 버튼 클릭 -> 글 삭제
			//        요청 주소 : /Board/deleteBoard.do  (CarApp.postForm 이 POST 로 요청)
			//        응답      : "삭제성공" / "삭제실패" / "비밀번호틀림" 글자만 응답 (AJAX)
			//=============================================================
			case "/deleteBoard.do": {

				String delete_idx = request.getParameter("b_idx");   // 삭제할 글번호
				String delete_pass = request.getParameter("pass");   // 본인 확인용 글 비밀번호

				//부장(BoardService)에게 시키기 : 삭제할 글번호와 비밀번호를 BoardService 의 serviceDeleteBoard(...) 호출해서 비밀번호 검증 후 글 삭제(delete) 작업 명령
				// 반환값 -> "삭제성공" / "삭제실패" / "비밀번호틀림"
				String result = boardService.serviceDeleteBoard(delete_idx, delete_pass);

				// 결과 글자를 read.jsp 의 CarApp.postForm 으로 응답하고 메소드를 끝낸다
				out.write(result);
				return;
			}

			//=============================================================
			// 요청5. read.jsp 의 수정 버튼 클릭 -> 글 수정
			//        요청 주소 : /Board/updateBoard.do  (CarApp.postForm 이 POST 로 요청)
			//        응답      : "수정성공" / "수정실패" / "비밀번호틀림" 글자만 응답 (AJAX)
			//=============================================================
			case "/updateBoard.do": {

				String idx = request.getParameter("idx");         // 수정할 글번호
				String email = request.getParameter("email");     // 수정할 이메일
				String title = request.getParameter("title");     // 수정할 제목
				String content = request.getParameter("content"); // 수정할 내용
				String pass = request.getParameter("pass");       // 본인 확인용 글 비밀번호

				//부장(BoardService)에게 시키기 : 수정할 정보와 비밀번호를 BoardService 의 serviceUpdateBoard(...) 호출해서 비밀번호 검증 후 글 수정(update) 작업 명령
				// 반환값 -> "수정성공" / "수정실패" / "비밀번호틀림"
				String result = boardService.serviceUpdateBoard(idx, email, title, content, pass);

				// 결과 글자를 read.jsp 의 CarApp.postForm 으로 응답하고 메소드를 끝낸다
				out.write(result);
				return;
			}

			//=============================================================
			// 요청6. read.jsp 에서 수정·삭제 버튼을 보여주기 전 비밀번호 확인
			//        요청 주소 : /Board/password.do  (CarApp.postForm 이 POST 로 요청)
			//        응답      : "비밀번호 맞음" / "비밀번호 틀림" 글자만 응답 (AJAX)
			//=============================================================
			case "/password.do": {

				String b_idx = request.getParameter("b_idx");   // 확인할 글번호
				String password = request.getParameter("pass"); // 입력한 비밀번호

				//부장(BoardService)에게 시키기 : 글번호와 입력한 비밀번호를 BoardService 의 servicePassCheck(...) 호출해서 DB 에 저장된 비밀번호와 일치하는지 조회(select) 명령
				// 반환값 -> true : 일치함 / false : 일치하지 않음(또는 글 없음)
				boolean resultPass = boardService.servicePassCheck(b_idx, password);

				// 일치 여부에 따라 정해진 글자를 read.jsp 의 CarApp.postForm 으로 응답하고 메소드를 끝낸다
				out.write(resultPass ? "비밀번호 맞음" : "비밀번호 틀림");
				return;
			}

			//=============================================================
			// 요청7. list.jsp 의 글 제목 클릭 -> 글 1건 상세 조회 (+ 조회수 1 증가)
			//        요청 주소 : /Board/read.bo?b_idx=글번호&nowPage=..&nowBlock=..
			//=============================================================
			case "/read.bo": {

				String b_idx = request.getParameter("b_idx");           // 조회할 글번호
				String nowPage = request.getParameter("nowPage");       // 목록에서 보고 있던 페이지 번호 (읽고 나서 그 페이지로 돌아가기 위해)
				String nowBlock = request.getParameter("nowBlock");     // 그 페이지가 속한 블록 번호

				//부장(BoardService)에게 시키기 : 글번호를 BoardService 의 serviceBoardRead(b_idx) 호출해서 글 1건 조회(select) + 조회수 1 증가(update) 작업 명령
				BoardVo vo = boardService.serviceBoardRead(b_idx);

				// 상세 화면(board/read.jsp)에서 꺼내 쓸 수 있도록 조회한 글 정보와 페이지 정보를 request 에 저장
				request.setAttribute("vo", vo);
				request.setAttribute("center", "board/read.jsp");
				request.setAttribute("nowPage", nowPage);
				request.setAttribute("nowBlock", nowBlock);
				request.setAttribute("b_idx", b_idx);   // 글번호도 함께 전달한다 (수정·삭제 요청에 다시 쓰인다)

				// 포워딩할 메인 화면 주소 저장
				nextPage = "/CarMain.jsp";
				break;
			}

			//=============================================================
			// 요청8. list.jsp 의 검색 버튼 클릭 -> 검색 조건에 맞는 글목록 조회 (페이징)
			//        요청 주소 : /Board/searchlist.bo?key=..&word=..&nowPage=..
			//=============================================================
			case "/searchlist.bo": {

				String key = request.getParameter("key");     // 검색 기준. 작성자면 "name", 제목+내용이면 "titleContent"
				String word = request.getParameter("word");   // 입력한 검색어

				// 현재 페이지 번호 (없으면 0 = 첫 페이지). 숫자가 아니어도 예외 없이 기본값(0)을 쓴다
				int searchPage = ParamUtil.getInt(request, "nowPage", 0);

				//부장(BoardService)에게 시키기 : 검색 기준·검색어·페이지 번호를 BoardService 의 serviceBoardPage(...) 호출해서 조건에 맞는 글 개수 조회(count) + 그 페이지 몫만 조회(select, limit) 작업 명령
				PageResult<BoardVo> searchResult = boardService.serviceBoardPage(key, word, searchPage);

				// 목록 화면(board/list.jsp)에서 꺼내 쓸 수 있도록 페이지 정보·글 목록·검색 조건을 request 에 저장
				request.setAttribute("page", searchResult);             // 페이지 정보(전체 개수, 전체 페이지 수, 블록 번호 등 계산 결과 포함)
				request.setAttribute("list", searchResult.getList());   // 이 페이지에 보여줄 글 목록

				// 페이지 번호를 눌러도 검색 조건이 그대로 유지되도록 함께 전달한다
				request.setAttribute("key", key);
				request.setAttribute("word", word);   // 검색창에 입력했던 검색어를 그대로 남겨 보여주기 위해

				// CarMain.jsp 가운데에 보여줄 화면 주소를 request 에 저장
				request.setAttribute("center", "board/list.jsp");

				// 포워딩할 메인 화면 주소 저장
				nextPage = "/CarMain.jsp";
				break;
			}

			//=============================================================
			// 요청9. Top.jsp 의 자유게시판 메뉴 클릭 -> 전체 글목록 조회 (페이징)
			//        요청 주소 : /Board/list.bo?nowPage=..
			//=============================================================
			case "/list.bo": {

				// 세션에서 로그인한 아이디를 꺼낸다 (목록에서 "내가 쓴 글" 을 구분해 보여주기 위해)
				HttpSession session = request.getSession();
				String loginid = (String) session.getAttribute("id");

				// 현재 페이지 번호 (없으면 0 = 첫 페이지)
				int nowPage = ParamUtil.getInt(request, "nowPage", 0);

				//부장(BoardService)에게 시키기 : 페이지 번호를 BoardService 의 serviceBoardPage(null, null, nowPage) 호출해서 검색 조건 없이(전체 대상) 그 페이지 몫만 조회 작업 명령
				PageResult<BoardVo> boardPage = boardService.serviceBoardPage(null, null, nowPage);

				// 목록 화면(board/list.jsp)에서 꺼내 쓸 수 있도록 페이지 정보와 로그인 아이디를 request 에 저장
				request.setAttribute("page", boardPage);             // 페이지 정보 (JSP 에서 ${page.xxx} 로 꺼내 쓴다)
				request.setAttribute("list", boardPage.getList());   // 이 페이지에 보여줄 글 목록
				request.setAttribute("center", "board/list.jsp");
				request.setAttribute("id", loginid);

				// 포워딩할 메인 화면 주소 저장
				nextPage = "/CarMain.jsp";
				break;
			}

			//=============================================================
			// 요청10. Top.jsp의 자유게시판 -> list.jsp 의 글쓰기 버튼 클릭 -> 글쓰기 화면 보여주기
			//         요청 주소 : /Board/write.bo
			//=============================================================
			case "/write.bo": {

				// 세션에서 로그인한 아이디를 꺼낸다
				HttpSession session = request.getSession();
				String loginid = (String) session.getAttribute("id");

				//부장(BoardService)에게 시키기 : 로그인 아이디를 BoardService 의 serviceMemberOne(loginid) 호출해서 글쓰기 화면에 미리 채울 회원 정보(이름·이메일) 조회(select) 명령
				MemberVO memberVO = boardService.serviceMemberOne(loginid);

				// 글쓰기 화면(board/write.jsp)에서 꺼내 쓸 수 있도록 회원 정보와 이전 페이지 정보를 request 에 저장
				request.setAttribute("membervo", memberVO);
				request.setAttribute("center", "board/write.jsp");
				request.setAttribute("nowPage", request.getParameter("nowPage"));     // 글쓰기를 마친 뒤 돌아갈 페이지 번호
				request.setAttribute("nowBlock", request.getParameter("nowBlock"));   // 그 페이지가 속한 블록 번호

				// 포워딩할 메인 화면 주소 저장
				nextPage = "/CarMain.jsp";
				break;
			}

			// 위에 없는 2단계 주소라면 아무것도 하지 않는다 (아래에서 404 로 응답)
			default:
				break;
		}

		// 포워딩할 화면 주소가 정해지지 않았다면 없는 주소이므로 404 로 응답한다
		if (nextPage == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		// nextPage 에 저장된 화면 주소로 포워딩해서 고객(웹브라우저)에게 응답한다
		request.getRequestDispatcher(nextPage).forward(request, response);
	}

	//================================================================================
	// 댓글 기능 (무한 대댓글 + 추천)  -  응답은 모두 JSON
	//
	//   [왜 화면(HTML)이 아니라 JSON 으로 답하는가]
	//     댓글을 하나 쓸 때마다 글 페이지 전체를 다시 그리면, 읽던 스크롤
	//     위치를 잃어버리고 화면 전체를 다시 받아오느라 느려진다. 그래서
	//     댓글이 있는 영역만 자바스크립트가 fetch 로 받아서 다시 그린다.
	//
	//   [모두 POST 방식으로 받는 이유]
	//     등록·수정·삭제·추천은 서버의 데이터를 바꾸는 동작이다. 만약 GET
	//     으로 받으면 <img src="...commentDelete.do?c_idx=1"> 한 줄만
	//     화면 어딘가에 있어도 이미지를 불러오는 순간 삭제가 실행되어
	//     버릴 수 있다. 목록 조회까지 POST 로 통일해 요청 방식을 하나로 맞췄다.
	//
	//   [화면이 보낸 값을 그대로 믿지 않는 지점 3가지]
	//     - 작성자 이름 : 화면이 보낸 이름을 쓰지 않고, 세션의 로그인 아이디로
	//                    DB 를 다시 조회해 이름을 가져온다 (화면 값은 조작될 수 있다)
	//     - "내 댓글인가" : 화면이 아이디를 직접 비교하지 않고, 서버가 세션
	//                      아이디와 댓글 작성자 아이디를 비교해 판정해 내려준다
	//     - 수정·삭제·추천 권한 : CommentService 가 서버에서 검증한다
	//                          (화면에서 버튼을 숨기는 것은 검증이 아니다)
	//================================================================================
	@SuppressWarnings("unchecked")
	private void handleComment(String action, HttpServletRequest request,
			HttpServletResponse response, PrintWriter out) throws Exception {

		response.setContentType("application/json;charset=UTF-8");   // 댓글 기능은 화면 전체가 아니라 데이터만 주고받으므로 JSON 이라고 알린다

		// 세션에서 로그인 아이디를 꺼낸다 (세션이 없거나 로그인하지 않았으면 null)
		HttpSession session = request.getSession(false);
		String loginId = (session == null) ? null : (String) session.getAttribute("id");

		switch (action) {   // 댓글 관련 2단계 주소에 따라 갈라 처리한다

			//=============================================================
			// 댓글 목록 조회 : board/read.jsp 가 글 상세 화면을 열 때마다 fetch 로 부른다
			//=============================================================
			case "/commentList.do": {

				int bIdx = ParamUtil.getRequiredInt(request, "b_idx");   // 어느 글의 댓글인지. 없거나 숫자가 아니면 여기서 400 예외가 난다

				//부장(CommentService)에게 시키기 : 글번호를 CommentService 의 getComments(bIdx, loginId) 호출해서 그 글의 댓글을 부모-자식 트리 구조로 조회(select) 명령
				List<CommentVo> tree = commentService.getComments(bIdx, loginId);

				//부장(CommentService)에게 시키기 : 조회한 트리를 CommentService 의 flatten(tree) 호출해서 화면에 순서대로 출력할 1차원 목록으로 펼치기 명령 (화면은 depth 값만 보고 들여쓰기하면 된다)
				List<CommentVo> flatList = commentService.flatten(tree);

				org.json.simple.JSONArray arr = new org.json.simple.JSONArray();   // JSON 목록 [ ... ] 을 만든다. 댓글 하나가 항목 하나가 된다
				for (CommentVo c : flatList) {   // 펼친 댓글을 하나씩 JSON 으로 옮긴다

					org.json.simple.JSONObject item = new org.json.simple.JSONObject();   // 댓글 하나를 담을 JSON 객체 { ... } 를 만든다
					item.put("cIdx",      Integer.valueOf(c.getCIdx()));      // 댓글번호
					item.put("parentIdx", c.getParentIdx());                  // 부모 댓글번호. 최상위 댓글이면 null (JSON 에서도 null)
					item.put("name",      c.getCName());                      // 작성자 이름
					item.put("content",   c.getCContent());                   // 댓글 내용
					item.put("date",      c.getCDate());                      // 작성일시
					item.put("edited",    Boolean.valueOf(c.isEdited()));     // 수정된 적이 있는가 (화면이 "(수정됨)" 을 붙일지 판단)
					item.put("deleted",   Boolean.valueOf(c.isDeleted()));    // 삭제 표시된 뼈대인가 (내용 대신 "삭제된 댓글입니다" 를 보여준다)
					item.put("depth",     Integer.valueOf(c.getDepth()));     // 들여쓰기 단계 (화면은 이 숫자만큼 왼쪽 여백을 준다)
					item.put("likeCount", Integer.valueOf(c.getLikeCount())); // 추천 수
					item.put("likedByMe", Boolean.valueOf(c.isLikedByMe()));  // 내가 이미 추천했는가 (추천 버튼 색을 바꾸는 데 쓴다)
					item.put("mine",      Boolean.valueOf(loginId != null && loginId.equals(c.getCId())));   // 내가 쓴 댓글인가 (수정·삭제 버튼을 보여줄지 판단)
					arr.add(item);   // 완성된 댓글 하나를 목록에 담는다
				}

				org.json.simple.JSONObject res = new org.json.simple.JSONObject();   // 화면에 돌려줄 최종 JSON 객체를 만든다
				res.put("ok", Boolean.TRUE);                                  // 성공했다는 표시. 화면은 이 값부터 확인한다
				res.put("loggedIn", Boolean.valueOf(loginId != null));        // 로그인 상태인지 (화면이 댓글 입력칸을 보일지 결정한다)
				res.put("count", Integer.valueOf(flatList.size()));           // 댓글이 몇 개인지
				res.put("comments", arr);                                     // 위에서 만든 댓글 목록
				out.write(res.toJSONString());   // JSON 을 글자로 바꿔 응답에 쓴다
				return;
			}

			//=============================================================
			// 댓글 등록 : 새 댓글(또는 답글)을 저장한다
			//=============================================================
			case "/commentAdd.do": {

				if (loginId == null) {   // 로그인하지 않았으면
					writeJsonError(out, "로그인 후 댓글을 쓸 수 있습니다.");
					return;
				}

				int bIdx = ParamUtil.getRequiredInt(request, "b_idx");   // 어느 글에 다는 댓글인지

				// 부모 댓글번호는 없을 수도 있다(최상위 댓글일 때). 숫자 모양일 때만 부모 번호로 인정하고, 그 외에는 null(=최상위 댓글)로 본다
				String parentRaw = request.getParameter("parent_idx");
				Integer parentIdx = (parentRaw != null && parentRaw.matches("\\d+"))
						? Integer.valueOf(parentRaw) : null;

				//부장(BoardService)에게 시키기 : 로그인 아이디를 BoardService 의 serviceMemberOne(loginId) 호출해서 실제 작성자 이름 조회(select) 명령
				// (화면이 보낸 이름을 쓰지 않고 서버가 다시 조회하는 이유는 위 클래스 설명을 참고)
				MemberVO writer = boardService.serviceMemberOne(loginId);
				String writerName = (writer == null) ? loginId : writer.getName();   // 회원 정보를 못 찾으면 아이디를 이름 대신 쓴다

				//부장(CommentService)에게 시키기 : 글번호·부모댓글번호·작성자정보·내용을 CommentService 의 addComment(...) 호출해서 댓글 추가(insert) 작업 명령
				int newIdx = commentService.addComment(bIdx, parentIdx, loginId, writerName, request.getParameter("content"));

				org.json.simple.JSONObject res = new org.json.simple.JSONObject();   // 화면에 돌려줄 JSON 객체를 만든다
				res.put("ok", Boolean.TRUE);
				res.put("cIdx", Integer.valueOf(newIdx));   // 새 댓글번호 (화면이 그 댓글로 스크롤하는 데 쓴다)
				res.put("message", parentIdx == null ? "댓글을 등록했습니다." : "답글을 등록했습니다.");   // 댓글인지 답글인지에 따라 안내 문구를 달리한다
				out.write(res.toJSONString());
				return;
			}

			//=============================================================
			// 댓글 수정 : 본인이 쓴 댓글의 내용을 바꾼다
			//=============================================================
			case "/commentEdit.do": {

				if (loginId == null) {   // 로그인하지 않았으면
					writeJsonError(out, "로그인이 필요합니다.");
					return;
				}

				//부장(CommentService)에게 시키기 : 수정할 댓글번호·로그인 아이디·새 내용을 CommentService 의 editComment(...) 호출해서 본인 확인 후 댓글 수정(update) 작업 명령
				// (본인 댓글이 아니면 CommentService 가 ForbiddenException 을 던져 handleComment 의 catch 가 JSON 오류로 응답한다)
				commentService.editComment(ParamUtil.getRequiredInt(request, "c_idx"), loginId, request.getParameter("content"));

				out.write("{\"ok\":true,\"message\":\"댓글을 수정했습니다.\"}");   // 간단한 응답이라 JSON 을 직접 글자로 적었다
				return;
			}

			//=============================================================
			// 댓글 삭제 : 본인이 쓴 댓글을 지운다 (답글이 달려 있으면 뼈대만 남긴다)
			//=============================================================
			case "/commentDelete.do": {

				if (loginId == null) {   // 로그인하지 않았으면
					writeJsonError(out, "로그인이 필요합니다.");
					return;
				}

				//부장(CommentService)에게 시키기 : 삭제할 댓글번호와 로그인 아이디를 CommentService 의 deleteComment(...) 호출해서 본인 확인 후 댓글 삭제(delete) 작업 명령
				// 반환값 -> true : 답글이 있어 완전히 지우지 못하고 "삭제된 댓글입니다" 뼈대만 남김 / false : 완전히 삭제됨
				boolean keptSkeleton = commentService.deleteComment(ParamUtil.getRequiredInt(request, "c_idx"), loginId);

				org.json.simple.JSONObject res = new org.json.simple.JSONObject();   // 화면에 돌려줄 JSON 객체를 만든다
				res.put("ok", Boolean.TRUE);
				// 답글이 있어서 뼈대를 남긴 경우에는 그 사실을 안내에 포함한다 (사용자가 왜 흔적이 남았는지 의아해하지 않도록)
				res.put("message", keptSkeleton
						? "댓글을 삭제했습니다. (답글이 있어 자리는 남습니다)"
						: "댓글을 삭제했습니다.");
				out.write(res.toJSONString());
				return;
			}

			//=============================================================
			// 댓글 추천 : 누르면 추천, 이미 추천한 상태에서 다시 누르면 추천 취소
			//=============================================================
			case "/commentLike.do": {

				if (loginId == null) {   // 로그인하지 않았으면
					writeJsonError(out, "로그인 후 추천할 수 있습니다.");
					return;
				}

				//부장(CommentService)에게 시키기 : 댓글번호와 로그인 아이디를 CommentService 의 toggleLike(...) 호출해서 추천/추천취소를 반전(toggle)시키는 작업 명령
				// 반환값 -> int[2] { 지금 추천 상태(1=추천중/0=추천안함), 반영된 뒤의 총 추천 수 }
				int[] result = commentService.toggleLike(ParamUtil.getRequiredInt(request, "c_idx"), loginId);

				org.json.simple.JSONObject res = new org.json.simple.JSONObject();   // 화면에 돌려줄 JSON 객체를 만든다
				res.put("ok", Boolean.TRUE);
				res.put("liked", Boolean.valueOf(result[0] == 1));       // 지금 추천 상태인가 (버튼 색을 바꾸는 데 쓴다)
				res.put("likeCount", Integer.valueOf(result[1]));        // 바뀐 뒤의 추천 수 (화면이 숫자를 바로 갱신한다)
				res.put("message", result[0] == 1 ? "추천했습니다." : "추천을 취소했습니다.");
				out.write(res.toJSONString());
				return;
			}

			// /comment 로 시작하지만 위 어느 주소에도 해당하지 않는 경우
			default:
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	//----------------------------------------------------------------
	// writeJsonError : 댓글 기능이 실패했을 때 응답을 JSON 형태로 통일해서 내려준다
	//  화면(read.jsp)은 ok 값만 보고 성공·실패를 판단하고, message 를 그대로 사용자에게 보여준다
	//----------------------------------------------------------------
	@SuppressWarnings("unchecked")
	private void writeJsonError(PrintWriter out, String message) {

		org.json.simple.JSONObject res = new org.json.simple.JSONObject();   // 실패를 알리는 JSON 객체를 만든다
		res.put("ok", Boolean.FALSE);   // 실패했다는 표시. 화면은 이 값을 보고 오류로 처리한다
		res.put("message", (message == null || message.trim().isEmpty())   // 메시지가 비었으면 기본 문구로 대신한다
				? "요청을 처리할 수 없습니다." : message);
		out.write(res.toJSONString());   // JSON 을 글자로 바꿔 응답에 쓴다
	}
}
