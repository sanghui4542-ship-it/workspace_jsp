package Controller;

/*
 * ============================================================================
 *  FileBoardController  -  사장(Controller) : 자료실(첨부파일 게시판) 요청을 가장 먼저 받는 서블릿
 *
 *  구조는 BoardController(자유게시판)와 거의 같고, 첨부파일 업로드(writePro.bo)와
 *  다운로드(Download.do) 처리가 추가된다.
 *
 *  이 사장이 받는 요청 주소 (2단계 주소)
 *    /list.bo         전체 글목록 조회 (페이징)
 *    /searchlist.bo   검색 기준 + 검색어로 글목록 조회 (페이징)
 *    /read.bo         글 1건 상세 조회 (+ 조회수 1 증가)                (로그인 필요)
 *    /write.bo        글쓰기 화면 보여주기                              (로그인 필요)
 *    /writePro.bo     새 글 등록 (첨부파일 업로드 포함, AJAX)            (로그인 필요)
 *    /password.do     글 비밀번호 확인 (AJAX, 수정·삭제 버튼 활성화용)   (로그인 필요)
 *    /updateBoard.do  글 수정 (AJAX)                                   (로그인 필요)
 *    /deleteBoard.do  글 삭제 (AJAX)                                   (로그인 필요)
 *    /reply.do        답글 작성 화면 보여주기                          (로그인 필요)
 *    /replyPro.do     답글 등록                                       (로그인 필요)
 *    /Download.do     첨부파일 다운로드 (+ 다운로드 횟수 1 증가)
 *
 *  [응답 붓(PrintWriter)을 미리 만들지 않는 이유 - 이 컨트롤러만의 특징]
 *    다른 컨트롤러는 메소드 맨 위에서 response.getWriter() 를 한 번만
 *    불러 응답에 글자를 쓴다. 그런데 이 컨트롤러는 /Download.do 처리에서
 *    response.getOutputStream() (파일의 순수 바이트를 그대로 내보내는 통로)
 *    을 쓴다. 같은 응답 안에서 getWriter() 와 getOutputStream() 을 함께
 *    부르면 IllegalStateException 이 난다. 그래서 이 컨트롤러는 응답에
 *    글자를 직접 써야 하는 case 안에서만, 그때그때 getWriter() 를 부른다.
 * ============================================================================
 */

// 응답에 글자를 쓰는 도구
import java.io.PrintWriter;

// 서블릿 기본 도구 (설정, 예외, 주소 연결, 요청, 응답, 세션)
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// 부장(FileBoardService), 상자(FileBoardVo, MemberVO, PageResult)
import Service.FileBoardService;
import Vo.FileBoardVo;
import Vo.MemberVO;
import Vo.PageResult;

// 요청 파라미터를 안전하게 꺼내는 도우미
import util.ParamUtil;

// 고객이 /프로젝트명/FileBoard/ 로 시작하는 주소를 요청하면 이 사장(FileBoardController)이 받는다
@WebServlet("/FileBoard/*")
public class FileBoardController extends BaseController {

	// 직렬화 버전 번호 (HttpServlet 이 요구하는 값, 경고 방지용)
	private static final long serialVersionUID = 1L;

	// 부장(FileBoardService) 객체의 주소를 저장할 참조변수
	// transient : 서블릿을 파일로 저장(직렬화)할 때 제외한다 (init() 에서 다시 만들기 때문)
	private transient FileBoardService boardService;

	//----------------------------------------------------------------
	// init : Tomcat 이 이 서블릿을 처음 만들 때 딱 1번 자동으로 부른다
	//----------------------------------------------------------------
	@Override
	public void init(ServletConfig config) throws ServletException {

		// 부장(FileBoardService) 객체를 만들어서 boardService 참조변수에 저장
		boardService = new FileBoardService();
	}

	//----------------------------------------------------------------
	// process : 부모 사장(BaseController)이 공통 검사를 끝낸 뒤 부르는 메소드
	//----------------------------------------------------------------
	@Override
	protected void process(String action, HttpServletRequest request, HttpServletResponse response) throws Exception {

		// 실제 요청 처리는 doHandle() 이 한다
		doHandle(request, response);
	}

	//----------------------------------------------------------------
	// requiresLogin : 로그인해야만 쓸 수 있는 주소를 부모 사장(BaseController)에게 알려준다
	//  목록 조회·검색·다운로드는 누구나, "글을 쓰고 고치고 지우는" 주소와
	//  글을 읽는 주소(read.bo)만 로그인을 요구한다
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
			|| action.equals("/read.bo");
	}

	//----------------------------------------------------------------
	// doHandle : 2단계 요청 주소에 따라 부장에게 일을 시키고 응답한다
	//----------------------------------------------------------------
	protected void doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {

		// 고객이 보낸 한글 입력값이 깨지지 않도록 요청 글자 방식을 UTF-8 로 정한다
		request.setCharacterEncoding("UTF-8");

		// 응답은 HTML 화면이고 한글은 UTF-8 이라고 브라우저에 알린다
		response.setContentType("text/html;charset=UTF-8");
		response.setCharacterEncoding("UTF-8");

		// 응답에 글자를 쓸 붓은 미리 만들지 않는다. 글자를 실제로 써야 하는 case 안에서만 그때 만든다
		// (이유는 클래스 위쪽 설명 [응답 붓을 미리 만들지 않는 이유] 참고 - 파일 다운로드와 붓이 충돌하기 때문이다)
		PrintWriter out = null;

		// 마지막에 포워딩할 화면 주소를 저장할 변수 (예: "/CarMain.jsp")
		String nextPage = null;

		// 요청 주소 중 2단계 주소 얻기  예) /CarProject/FileBoard/list.bo  ->  "/list.bo"
		String action = request.getPathInfo();

		// 이클립스 콘솔에 2단계 요청 주소 출력 (화면이 안 뜰 때 여기부터 확인한다)
		System.out.println("클라이언트가 요청한 2단계 요청 주소  : " + action);

		// 2단계 주소가 없으면 (예: /CarProject/FileBoard 로만 접근) 없는 주소이므로 404 로 응답한다
		if (action == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		// 2단계 요청 주소가 무엇인지에 따라 나누어 처리한다
		switch (action) {

			//=============================================================
			// 요청1. fileboardreply.jsp 의 답변등록 버튼 클릭 -> 답글 등록
			//        요청 주소 : /FileBoard/replyPro.do  (form 이 POST 로 전송)
			//=============================================================
			case "/replyPro.do": {

				// fileboardreply.jsp 화면에서 입력한 답글 정보 꺼내기
				String super_b_idx = request.getParameter("super_b_idx"); // 답글을 달 원글(부모)의 글번호
				String reply_id = request.getParameter("id");             // 로그인한 답글 작성자 아이디
				String reply_name = request.getParameter("writer");       // 답글 작성자 이름
				String reply_email = request.getParameter("email");       // 답글 작성자 이메일
				String reply_title = request.getParameter("title");       // 답글 제목
				String reply_content = request.getParameter("content");   // 답글 내용
				String reply_pass = request.getParameter("pass");         // 답글 비밀번호

				//부장(FileBoardService)에게 시키기 : 원글 번호와 답글 작성 정보를 FileBoardService 의 serviceReplyInsertBoard(...) 호출해서 계층형 정렬 규칙에 맞게 답글 추가(insert) 작업 명령
				boardService.serviceReplyInsertBoard(super_b_idx, reply_id, reply_name,
													  reply_email, reply_title, reply_content, reply_pass);

				// 답글 등록이 끝나면 전체 글목록 화면(/list.bo)으로 다시 요청해 최신 목록을 보여준다
				nextPage = "/FileBoard/list.bo";
				break;
			}

			//=============================================================
			// 요청2. fileboardread.jsp 의 답변 버튼 클릭 -> 답글 작성 화면 보여주기
			//        요청 주소 : /FileBoard/reply.do?b_idx=글번호
			//=============================================================
			case "/reply.do": {

				String parentIdx = request.getParameter("b_idx");   // 답글을 달 원글(부모)의 글번호
				String replyLoginId = request.getParameter("id");   // 답글을 작성하는 로그인 아이디

				//부장(FileBoardService)에게 시키기 : 로그인 아이디를 FileBoardService 의 serviceMemberOne(replyLoginId) 호출해서 답글 작성 화면에 미리 채울 회원 정보(이름·이메일) 조회(select) 명령
				MemberVO reply_vo = boardService.serviceMemberOne(replyLoginId);

				// 답글 작성 화면(board/fileboardreply.jsp)에서 꺼내 쓸 수 있도록 원글 번호와 회원 정보를 request 에 저장
				request.setAttribute("b_idx", parentIdx);
				request.setAttribute("vo", reply_vo);

				// CarMain.jsp 가운데에 보여줄 화면 주소를 request 에 저장
				request.setAttribute("center", "board/fileboardreply.jsp");

				// 포워딩할 메인 화면 주소 저장
				nextPage = "/CarMain.jsp";
				break;
			}

			//=============================================================
			// 요청3. fileboardwrite.jsp 의 등록 버튼 클릭 -> 새 글 등록 (첨부파일 업로드 포함)
			//        요청 주소 : /FileBoard/writePro.bo  (multipart/form-data 로 POST 전송)
			//        응답      : alert 스크립트 + 목록 화면 이동 (또는 실패 안내)
			//=============================================================
			case "/writePro.bo": {

				try {
					//부장(FileBoardService)에게 시키기 : request·response 를 FileBoardService 의 serviceInsertBoard(...) 호출해서 첨부파일 업로드 + 새 글 추가(insert) 작업 명령
					// 반환값 -> 새로 등록된 글번호
					int num = boardService.serviceInsertBoard(request, response);

					// 자바스크립트로 성공 안내를 띄우고 목록 화면으로 이동시킨다
					out = response.getWriter();
					out.print("<script>");
					out.print(" alert('" + num + " 글 추가 성공!');");   // 몇 번 글이 추가됐는지 알려 준다
					out.print(" location.href='" + request.getContextPath() + "/FileBoard/list.bo';");   // 공지 목록 화면으로 이동시킨다
					out.print("</script>");
					out.close();

					return;   // doHandle 메소드를 여기서 끝낸다 (아래 공통 포워딩으로 내려가면 안 된다)

				/*
				 [사용자가 고칠 수 있는 문제 - 이유를 그대로 알려준다]

				   업무 예외(InvalidInputException)는 제목 누락, 비밀번호 누락,
				   허용되지 않는 확장자, 용량 초과처럼 "사용자가 직접 고칠 수
				   있는 문제"다. 이런 경우까지 뭉뚱그려 "오류가 발생했습니다"
				   라고만 안내하면, 확장자 제한처럼 의도대로 잘 동작한 상황도
				   마치 기능이 고장난 것처럼 보인다. 그래서 이 예외의 메시지는
				   그대로 사용자에게 보여준다.

				   [보안] 메시지를 자바스크립트 문자열 안에 넣을 때는 반드시
				   이스케이프(escapeJs)한다. 홑따옴표나 줄바꿈이 섞인 메시지를
				   그대로 넣으면 스크립트 문법이 깨지거나, 더 나쁘면 악의적인
				   스크립트가 실행될 수 있다.
				*/
				} catch (exception.InvalidInputException e) {

					out = response.getWriter();
					out.print("<script>");
					out.print(" alert('" + util.HtmlUtil.escapeJs(e.getMessage()) + "');");
					out.print(" history.back();");   // 입력한 내용을 잃지 않도록 이전 화면으로 되돌린다
					out.print("</script>");
					out.close();
					return;   // 여기서 끝낸다. 아래 공통 화면 이동을 하면 안 된다

				/*
				 [사용자가 고칠 수 없는 문제 - 내부 사정은 감추고 무난한 안내만]

				   DB 연결 실패 같은 예상하지 못한 오류는 사용자가 스스로
				   해결할 방법이 없다. 자세한 오류 내용을 그대로 보여주면
				   서버 내부 구조가 드러나 공격에 악용될 수 있으므로, 화면에는
				   일반적인 안내만 보여주고 실제 원인은 서버 콘솔에만 남긴다.
				*/
				} catch (Exception e) {

					System.out.println("[FileBoardController] 글 추가 중 예상하지 못한 오류");
					e.printStackTrace();   // 예상 못 한 오류는 서버 콘솔에 자세히 남긴다

					out = response.getWriter();
					out.print("<script>");
					out.print(" alert('새 글 추가 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');");
					out.print(" location.href='" + request.getContextPath() + "/FileBoard/write.bo';");   // 글쓰기 화면으로 되돌려 다시 시도하게 한다
					out.print("</script>");
					out.close();
					return;
				}
			}

			//=============================================================
			// 요청4. fileboardread.jsp 의 삭제 버튼 클릭 -> 글 삭제 (+ 첨부파일 폴더 삭제)
			//        요청 주소 : /FileBoard/deleteBoard.do  (CarApp.postForm 이 POST 로 요청)
			//        응답      : "삭제성공" / "삭제실패" / "비밀번호틀림" 글자만 응답 (AJAX)
			//=============================================================
			case "/deleteBoard.do": {

				String delete_idx = request.getParameter("b_idx");   // 삭제할 글번호
				String delete_pass = request.getParameter("pass");   // 본인 확인용 글 비밀번호

				//부장(FileBoardService)에게 시키기 : 삭제할 글번호와 비밀번호를 FileBoardService 의 serviceDeleteBoard(...) 호출해서 비밀번호 검증 후 글 삭제(delete) + 첨부파일 폴더 삭제 작업 명령
				// 반환값 -> "삭제성공" / "삭제실패" / "비밀번호틀림"
				String result = boardService.serviceDeleteBoard(delete_idx, delete_pass);

				// 결과 글자를 응답에 쓰기 위해 여기서 붓을 얻는다
				out = response.getWriter();
				out.write(result);   // 결과 문구를 그대로 응답에 쓴다. 화면의 AJAX 가 이 글자를 보고 안내를 띄운다
				return;
			}

			//=============================================================
			// 요청5. fileboardread.jsp 의 수정 버튼 클릭 -> 글 수정
			//        요청 주소 : /FileBoard/updateBoard.do  (CarApp.postForm 이 POST 로 요청)
			//        응답      : "수정성공" / "수정실패" / "비밀번호틀림" 글자만 응답 (AJAX)
			//=============================================================
			case "/updateBoard.do": {

				String idx = request.getParameter("idx");         // 수정할 글번호
				String email = request.getParameter("email");     // 수정할 이메일
				String title = request.getParameter("title");     // 수정할 제목
				String content = request.getParameter("content"); // 수정할 내용
				String pass = request.getParameter("pass");       // 본인 확인용 글 비밀번호

				//부장(FileBoardService)에게 시키기 : 수정할 정보와 비밀번호를 FileBoardService 의 serviceUpdateBoard(...) 호출해서 비밀번호 검증 후 글 수정(update) 작업 명령
				// 반환값 -> "수정성공" / "수정실패" / "비밀번호틀림"
				String result = boardService.serviceUpdateBoard(idx, email, title, content, pass);

				out = response.getWriter();
				out.write(result);   // 결과 문구를 그대로 응답에 쓴다
				return;
			}

			//=============================================================
			// 요청6. fileboardread.jsp 에서 수정·삭제 버튼을 보여주기 전 비밀번호 확인
			//        요청 주소 : /FileBoard/password.do  (CarApp.postForm 이 POST 로 요청)
			//        응답      : "비밀번호 맞음" / "비밀번호 틀림" 글자만 응답 (AJAX)
			//=============================================================
			case "/password.do": {

				String b_idx = request.getParameter("b_idx");   // 확인할 글번호
				String password = request.getParameter("pass"); // 입력한 비밀번호

				//부장(FileBoardService)에게 시키기 : 글번호와 입력한 비밀번호를 FileBoardService 의 servicePassCheck(...) 호출해서 DB 에 저장된 비밀번호와 일치하는지 조회(select) 명령
				// 반환값 -> true : 일치함 / false : 일치하지 않음(또는 글 없음)
				boolean resultPass = boardService.servicePassCheck(b_idx, password);

				out = response.getWriter();

				// 일치 여부에 따라 정해진 글자를 fileboardread.jsp 의 CarApp.postForm 으로 응답하고 메소드를 끝낸다
				out.write(resultPass ? "비밀번호 맞음" : "비밀번호 틀림");
				return;
			}

			//=============================================================
			// 요청7. fileboardlist.jsp 의 글 제목 클릭 -> 글 1건 상세 조회 (+ 조회수 1 증가)
			//        요청 주소 : /FileBoard/read.bo?b_idx=글번호&nowPage=..&nowBlock=..
			//=============================================================
			case "/read.bo": {

				String b_idx = request.getParameter("b_idx");           // 조회할 글번호
				String nowPage = request.getParameter("nowPage");       // 목록에서 보고 있던 페이지 번호 (읽고 나서 그 페이지로 돌아가기 위해)
				String nowBlock = request.getParameter("nowBlock");     // 그 페이지가 속한 블록 번호

				//부장(FileBoardService)에게 시키기 : 글번호를 FileBoardService 의 serviceBoardRead(b_idx) 호출해서 글 1건 조회(select) + 조회수 1 증가(update) 작업 명령
				FileBoardVo vo = boardService.serviceBoardRead(b_idx);

				// 상세 화면(board/fileboardread.jsp)에서 꺼내 쓸 수 있도록 조회한 글 정보와 페이지 정보를 request 에 저장
				request.setAttribute("vo", vo);
				request.setAttribute("center", "board/fileboardread.jsp");
				request.setAttribute("nowPage", nowPage);
				request.setAttribute("nowBlock", nowBlock);
				request.setAttribute("b_idx", b_idx);   // 글번호도 함께 전달한다 (수정·삭제 요청에 다시 쓰인다)

				// 포워딩할 메인 화면 주소 저장
				nextPage = "/CarMain.jsp";
				break;
			}

			//=============================================================
			// 요청8. fileboardlist.jsp 의 검색 버튼 클릭 -> 검색 조건에 맞는 글목록 조회 (페이징)
			//        요청 주소 : /FileBoard/searchlist.bo?key=..&word=..&nowPage=..
			//=============================================================
			case "/searchlist.bo": {

				String key = request.getParameter("key");     // 검색 기준. 작성자면 "name", 제목+내용이면 "titleContent"
				String word = request.getParameter("word");   // 입력한 검색어

				// 현재 페이지 번호 (없으면 0 = 첫 페이지). 숫자가 아니어도 예외 없이 기본값(0)을 쓴다
				int searchPage = ParamUtil.getInt(request, "nowPage", 0);

				//부장(FileBoardService)에게 시키기 : 검색 기준·검색어·페이지 번호를 FileBoardService 의 serviceBoardPage(...) 호출해서 조건에 맞는 글 개수 조회(count) + 그 페이지 몫만 조회(select, limit) 작업 명령
				PageResult<FileBoardVo> searchResult = boardService.serviceBoardPage(key, word, searchPage);

				// 목록 화면(board/fileboardlist.jsp)에서 꺼내 쓸 수 있도록 페이지 정보·글 목록·검색 조건을 request 에 저장
				request.setAttribute("page", searchResult);             // 페이지 정보(전체 개수, 전체 페이지 수, 블록 번호 등 계산 결과 포함)
				request.setAttribute("list", searchResult.getList());   // 이 페이지에 보여줄 글 목록

				// 페이지 번호를 눌러도 검색 조건이 그대로 유지되도록 함께 전달한다
				request.setAttribute("key", key);
				request.setAttribute("word", word);   // 검색창에 입력했던 검색어를 그대로 남겨 보여주기 위해

				// CarMain.jsp 가운데에 보여줄 화면 주소를 request 에 저장
				request.setAttribute("center", "board/fileboardlist.jsp");

				// 포워딩할 메인 화면 주소 저장
				nextPage = "/CarMain.jsp";
				break;
			}

			//=============================================================
			// 요청9. Top.jsp 의 공지사항 메뉴 클릭 -> 전체 글목록 조회 (페이징)
			//        요청 주소 : /FileBoard/list.bo?nowPage=..
			//=============================================================
			case "/list.bo": {

				// 세션에서 로그인한 아이디를 꺼낸다 (목록에서 "내가 쓴 글" 을 구분해 보여주기 위해)
				HttpSession session = request.getSession();
				String loginid = (String) session.getAttribute("id");

				// 현재 페이지 번호 (없으면 0 = 첫 페이지)
				int nowPage = ParamUtil.getInt(request, "nowPage", 0);

				//부장(FileBoardService)에게 시키기 : 페이지 번호를 FileBoardService 의 serviceBoardPage(null, null, nowPage) 호출해서 검색 조건 없이(전체 대상) 그 페이지 몫만 조회 작업 명령
				PageResult<FileBoardVo> boardPage = boardService.serviceBoardPage(null, null, nowPage);

				// 목록 화면(board/fileboardlist.jsp)에서 꺼내 쓸 수 있도록 페이지 정보와 로그인 아이디를 request 에 저장
				request.setAttribute("page", boardPage);             // 페이지 정보 (JSP 에서 ${page.xxx} 로 꺼내 쓴다)
				request.setAttribute("list", boardPage.getList());   // 이 페이지에 보여줄 글 목록
				request.setAttribute("center", "board/fileboardlist.jsp");
				request.setAttribute("id", loginid);

				// 페이지 번호 <a> 링크를 눌렀을 때 넘어온 원래 요청값도 함께 전달한다
				//   예) 2페이지 클릭 시 요청 : /FileBoard/list.bo?nowBlock=0&nowPage=1
				request.setAttribute("nowPage", request.getParameter("nowPage"));
				request.setAttribute("nowBlock", request.getParameter("nowBlock"));

				// 포워딩할 메인 화면 주소 저장
				nextPage = "/CarMain.jsp";
				break;
			}

			//=============================================================
			// 요청10. Top.jsp 의 공지사항 -> fileboardlist.jsp 의 글쓰기 버튼 클릭 -> 글쓰기 화면 보여주기
			//         요청 주소 : /FileBoard/write.bo
			//=============================================================
			case "/write.bo": {

				// 세션에서 로그인한 아이디를 꺼낸다
				HttpSession session = request.getSession();
				String loginid = (String) session.getAttribute("id");

				//부장(FileBoardService)에게 시키기 : 로그인 아이디를 FileBoardService 의 serviceMemberOne(loginid) 호출해서 글쓰기 화면에 미리 채울 회원 정보(이름·이메일) 조회(select) 명령
				MemberVO memberVO = boardService.serviceMemberOne(loginid);

				// 글쓰기 화면(board/fileboardwrite.jsp)에서 꺼내 쓸 수 있도록 회원 정보와 이전 페이지 정보를 request 에 저장
				request.setAttribute("membervo", memberVO);
				request.setAttribute("center", "board/fileboardwrite.jsp");
				request.setAttribute("nowPage", request.getParameter("nowPage"));     // 글쓰기를 마친 뒤 돌아갈 페이지 번호
				request.setAttribute("nowBlock", request.getParameter("nowBlock"));   // 그 페이지가 속한 블록 번호

				// 포워딩할 메인 화면 주소 저장
				nextPage = "/CarMain.jsp";
				break;
			}

			//=============================================================
			// 요청11. fileboardread.jsp 의 첨부파일 링크 클릭 -> 파일 다운로드 (+ 다운로드 횟수 1 증가)
			//         요청 주소 : /FileBoard/Download.do?path=글번호&fileName=저장파일명
			//=============================================================
			case "/Download.do": {

				//부장(FileBoardService)에게 시키기 : request·response 를 FileBoardService 의 serviecDownload(...) 호출해서 첨부파일 목록 검증 후 파일 전송 + 다운로드 횟수 1 증가(update) 작업 명령
				boardService.serviecDownload(request, response);

				// 파일 전송은 이미 response 에 직접 쓰였으므로, 화면(HTML) 포워딩을 하면 안 된다
				return;
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
}
