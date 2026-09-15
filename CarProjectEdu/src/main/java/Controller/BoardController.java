// 이 파일이 속한 폴더(패키지) 이름. 실제 폴더 경로 Controller 와 반드시 같아야 한다
package Controller;   // 이 파일이 속한 패키지(=폴더) 이름. 실제 폴더 구조와 반드시 같아야 한다
// IOException 를 가져온다 — 입출력 중 생기는 오류를 받기 위해
import java.io.IOException;   // 파일/네트워크 입출력이 실패했을 때의 예외
// java.io 안의 PrintWriter 를 이 파일에서 쓰겠다는 선언
import java.io.PrintWriter;   // 응답 화면에 글자를 직접 찍어 보낼 때 쓰는 도구 (AJAX 응답에 사용)
// List 를 가져온다 — 목록 타입을 쓰기 위해
import java.util.List;   // 순서가 있는 목록 (게시글 5건이 순서대로 들어간다)
// javax.servlet 안의 ServletConfig 를 이 파일에서 쓰겠다는 선언
import javax.servlet.ServletConfig;   // 서블릿의 설정값을 읽는 객체
// javax.servlet 안의 ServletException 를 이 파일에서 쓰겠다는 선언
import javax.servlet.ServletException;   // 서블릿이 처리 중 실패했을 때의 예외
// javax.servlet.annotation 안의 WebServlet 를 이 파일에서 쓰겠다는 선언
import javax.servlet.annotation.WebServlet;   // 이 서블릿이 어떤 주소의 요청을 받을지 정하는 표시 (아래 클래스 위에 붙어 있다)
// HttpServletRequest 를 가져온다 — 브라우저가 보낸 요청(파라미터·세션)을 담은 객체를 쓰기 위해
import javax.servlet.http.HttpServletRequest;   // 브라우저가 보낸 요청(주소·파라미터·세션)이 담긴 객체
// HttpServletResponse 를 가져온다 — 브라우저로 보낼 응답을 다루기 위해
import javax.servlet.http.HttpServletResponse;   // 브라우저에게 돌려줄 응답을 담는 객체
// HttpSession 를 가져온다 — 로그인 정보를 서버에 기억시키기 위해
import javax.servlet.http.HttpSession;   // 로그인 정보처럼 사용자별로 서버에 보관하는 저장소
// Service 안의 BoardService 를 이 파일에서 쓰겠다는 선언
import Service.BoardService;   // 업무 규칙을 담당하는 Service 클래스
// Service 안의 CommentService 를 이 파일에서 쓰겠다는 선언
import Service.CommentService;   // 업무 규칙을 담당하는 Service 클래스
// Vo 안의 BoardVo 를 이 파일에서 쓰겠다는 선언
import Vo.BoardVo;   // 값을 담아 나르는 상자(VO) 클래스
// Vo 안의 CommentVo 를 이 파일에서 쓰겠다는 선언
import Vo.CommentVo;   // 값을 담아 나르는 상자(VO) 클래스
// Vo 안의 MemberVO 를 이 파일에서 쓰겠다는 선언
import Vo.MemberVO;   // 값을 담아 나르는 상자(VO) 클래스
// Vo 안의 PageResult 를 이 파일에서 쓰겠다는 선언
import Vo.PageResult;   // 값을 담아 나르는 상자(VO) 클래스
// util 안의 ParamUtil 를 이 파일에서 쓰겠다는 선언
import util.ParamUtil;   // 여러 곳에서 함께 쓰는 도우미 클래스
/*
==========================================================
📌 BoardController 클래스
이 클래스는 사용자가 게시판에서 어떤 버튼을 누르면
가장 먼저 실행되는 곳입니다.
쉽게 말하면:
👉 사용자의 요청을 받아서
👉 어떤 작업을 해야 하는지 판단하고
👉 Service에게 일을 시키는 역할입니다.
MVC 구조에서 C(Controller)에 해당합니다.
==========================================================
*/
//...사장 
//MVC 디자인 패턴 개발 방법 중에서   C의 역할 을 하는 회원관련 처리  서블릿 
@WebServlet("/Board/*")   
// BoardController 클래스를 만든다. BaseController 를 상속받아 공통 기능(요청 분기·오류 처리)을 그대로 물려받는다
public class BoardController extends BaseController {
	//직렬화 버전 번호 (HttpServlet이 Serializable을 구현하므로 경고 방지용으로 선언)
	private static final long serialVersionUID = 1L;
	//BoardService 부장 객체의 주소번지를 저장시킬 참조변수 선언
	//(transient : 서블릿 직렬화 대상에서 제외 - init()에서 다시 생성되므로 저장할 필요 없음)
	private transient BoardService boardService;
	//댓글 기능 담당 부장 (무한 대댓글 + 추천)
	private transient CommentService commentService;
	// 부모 클래스에 이미 있는 메서드를 여기서 다시 만든다는 표시. 이름을 잘못 쓰면 컴파일 오류로 잡아준다
	@Override   // 부모(HttpServlet)의 init 을 덮어쓴다는 표시
	// init( ) 메서드 — 1개 값을 받아 돌려주는 값 없이 일만 한다
	public void init(ServletConfig config) throws ServletException {
		//init메소드가 처음 호출되면!~  BoardService객체를 생성해서 주소번지  위 boardService참조변수에 저장
		boardService = new BoardService();
		// commentService 에 새 CommentService 을 만들어 담는다
		commentService = new CommentService();   // 댓글 기능을 담당할 Service 도 하나 만들어 둔다
	}
	protected void doHandle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {   // 실제 처리를 담당하는 메소드. 아래 process() 가 이걸 부른다
		//재료 준비 
		request.setCharacterEncoding("UTF-8");
		// 응답이 어떤 형식(HTML·JSON 등)인지 브라우저에게 알려준다
		response.setContentType("text/html;charset=UTF-8");   // 응답이 HTML 이고 한글은 UTF-8 이라고 브라우저에 알린다
		// 응답의 한글이 깨지지 않게 UTF-8 로 보낸다
		response.setCharacterEncoding("UTF-8");   // 응답 글자를 UTF-8 로 내보낸다 (안 하면 한글이 깨진다)
		// 브라우저로 글자를 내보낼 출력 통로를 얻는다
		PrintWriter out = response.getWriter();   // 응답에 글자를 쓸 수 있는 붓을 얻는다
		String nextPage = null;   //2단계 요청한 주소에 따라 포워딩 또는 보여줄 VIEW주소 경로가 저장될 변수 선언 (재료)
		//클라이언트가 BoardController서블릿으로 요청한 전체 주소(URL) 중에서
		//2단계 요청한 주소만 얻어 action변수에 저장
		String action = request.getPathInfo();
		/*
		 ============================================================================
		   [댓글 요청은 예외를 JSON 으로 답한다]
		   댓글 기능은 자바스크립트가 fetch 로 호출하고 응답을 JSON 으로 읽는다.
		   그런데 Service 가 던지는 예외(로그인 필요 / 권한 없음 / 빈 내용 등)를
		   그대로 두면 web.xml 의 error-page 설정에 따라 500 HTML 이 돌아온다.
		       response.json()  ->  HTML 을 JSON 으로 읽으려다 실패
		       화면에는 "댓글 등록 실패" 같은 애매한 문구만 남고 이유를 알 수 없다
		   그래서 댓글 주소만 따로 감싸서, 예외 메시지를 그대로 JSON 으로 내려준다.
		   사용자는 "본인이 쓴 댓글만 수정할 수 있습니다" 처럼 정확한 이유를 보게 된다.
		   [주의] 예상하지 못한 오류(NullPointerException 등)의 메시지는 내려보내지 않는다.
		          내부 구조가 드러나면 공격에 쓰일 수 있으므로 서버 로그에만 남긴다.
		 ============================================================================
		*/
		if (action != null && action.startsWith("/comment")) {
			// 오류가 날 수 있는 구간의 시작. 실패해도 프로그램이 멈추지 않게 감싼다
			try {
				// handleComment( ) 를 실행한다 (직접 만든 도우미)
				handleComment(action, request, response, out);
			// 앞 구간에서 exception 오류가 나면 여기로 온다
			} catch (exception.ForbiddenException e) {
				// writeJsonError( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
				writeJsonError(out, e.getMessage());   // 권한이 없다는 뜻을 JSON 으로 내려보낸다
			// 앞 구간에서 exception 오류가 나면 여기로 온다
			} catch (exception.NotFoundException e) {
				// writeJsonError( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
				writeJsonError(out, e.getMessage());   // 찾는 것이 없다는 뜻을 JSON 으로 내려보낸다
			// 앞 구간에서 exception 오류가 나면 여기로 온다
			} catch (exception.InvalidInputException e) {
				// writeJsonError( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
				writeJsonError(out, e.getMessage());   // 입력이 잘못됐다는 뜻을 JSON 으로 내려보낸다
			// 앞 구간에서 Exception 오류가 나면 여기로 온다
			} catch (Exception e) {
				//원인은 서버 로그에만 남기고, 화면에는 일반적인 안내만 보낸다
				System.out.println("[BoardController] 댓글 처리 오류 : " + e);
				// 오류의 자세한 내용을 콘솔에 남긴다
				e.printStackTrace();   // 예상 못 한 오류는 서버 콘솔에 자세히 남긴다
				// writeJsonError( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
				writeJsonError(out, "처리 중 문제가 발생했습니다. 잠시 후 다시 시도해주세요.");   // 화면에는 내부 사정을 감춘 무난한 안내만 보낸다
			}
			return;   // 댓글 요청은 여기서 끝낸다. 아래 게시판 처리로 내려가면 안 된다
		}
/*
============================================================================================================================		     		
		 	 1페이지 번호를 클릭 해서 요청하면   http://localhost:8090/CarProject/Board/list.bo?nowBlock=0&nowPage=0
		   "/Board/list.bo" -> 요청 URL 의미  : DB의 board테이블에 저장된 모든 글목록 조회 요청 전체 주소!  
			action = "/list.bo" 2단계 요청한 주소만 저장 
============================================================================================================================		     		
		   "/Board/write.bo"-> 요청 URL 의미 : 	새 글을 입력해서 DB에 board테이블에 추가 요청하는 VIEW(중앙화면) 요청 전체 주소!
		    action = "/wirte.bo" 2단계 요청한 주소만 저장
============================================================================================================================		     		
		    "/Board/searchlist.bo"-> 요청 URL 의미 : DB의 board테이블에 저장된 글목록을 조회하되, 입력한 검색기준 열의 값과 검색어를 포함하는 글목조회 요청 전체 주소!
		    action = "/searchlist.bo" 2단계 요청한 주소만 저장
============================================================================================================================		     		
		    "/Board/read.bo"  -> 요청 URL 의미 : list.jsp요청화면에서 조회된 글제목 하나를 클릭했을때..
		    								 글번호를 이용해 글 하나를 조회해서 중앙화면VIEW에 보여줘~ 요청 전체 주소!
		    action = "/read.bo" 2단계 요청한 주소만 얻어 저장 
============================================================================================================================		     		
		    "/Board/password.do" -> 요청 URL 의미 : 글 상세 화면(read.jsp)에서 글 수정 또는 글삭제를 위해 
		      						     	      글 비밀번호를 입력해서 DB의 board테이블에 저장된 비밀번호와 비교해서
		     						                      비밀번호가 저장되어 있느냐? 저장되어 있지 않느냐? 판단 하는  AJAX 요청 전체 주소!
		 	action = "/passowrd.do" 2단계 요청한 주소만 얻어 저장
============================================================================================================================		     		
		    "/Board/updateBoard.do" -> 요청 URL 의미 : 글 상세 화면(read.jsp)에서  수정할 글의 정보를 입력하고, 수정 이미지 버튼을 눌러
		    										글 수정 AJAX 요청 전체 주소!
		    action = "/updateBoard.do" 2단계 요청 주소만 얻어 저장		
============================================================================================================================		     			    								
		    "/Board/deleteBoard.do" -> 요청 URL 의미 : 글 상세 화면(read.jsp)에서 글 삭제 이미지 버튼을 눌러 ajax통신으로 삭제요청한 전체 주소!
		    action = "/deleteBoard.do" 2단계 요청한 주소만 얻어 저장 
============================================================================================================================		     		
		    "/Board/writePro.bo" -> 요청 URL 의미 :  글 쓰기화면(write.jsp)에서 입력한 새글 정보를 DB의 board테이블에  ajax통신으로 추가 요청한 전체 주소!
		     action = "/writePro.bo" 2단계 요청한 주소만 얻어 저장 
============================================================================================================================		     		
		    "/Board/reply.do" ->  요청 URL 의미 : 글 상세 화면(read.jsp)에서  답변이미지 버튼을 눌렀을때  주 글에 대한 답변들을 작성할수 있는 VIEW화면 요청한 전체주소!
		    action = "/reply.do" 2단계 요청한 주소만 얻어 저장	
 ============================================================================================================================		     		
  			"/Board/replyPro.do" -> 요청 URL 의미  : 답변글을 작성하는 화면(reply.jsp)에서 주글에 대한 답변글 내용을 작성하고 
  											         작성한 답변글을  DB의 board테이블에 추가 (insert) 요청한  전체 주소!
    		action = "/replyPro.do"    2단계 요청한 주소만 request메모리에서 얻어 저장 						       			
*/
		System.out.println("클라이언트가 요청한 2단계 요청 주소  : " + action);
		//[추가] 2단계 주소가 없는 요청(/Board 로만 접근)은 404로 응답한다. 기존에는 switch(null) NPE 500이었다.
		if(action == null) {
			// response 의 sendError( ) 를 실행한다
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;   // 여기서 끝낸다
		}
		// 값에 따라 갈래를 나눈다
		switch(action) {//클라이언트가 요청한 2단계 요청주소가?
			// ◀◀ 붙여넣는 자리 1 ◀◀ — 기능 장에서 "자리 1" 라고 알려주는 조각을 이 줄 아래에
default:   // 위 어느 주소에도 걸리지 않은 경우
				// 이 갈래는 여기까지. 아래 갈래로 넘어가지 않게 막는다
				break;
		}//switch문
		//[추가] 어느 case에도 걸리지 않으면 nextPage가 null이다. forward(null)은 NPE 500이므로 404로 응답한다.
		if(nextPage == null) {
			// response 의 sendError( ) 를 실행한다
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;   // 여기서 끝낸다
		}
		//디스패처 방식으로 /CarMain.jsp 포워딩(재요청)
		request.getRequestDispatcher(nextPage).forward(request, response);
	}//doHandle메소드
	//================================================================================
	// 댓글 기능 (무한 대댓글 + 추천)  -  응답은 모두 JSON
	//
	//   [왜 화면(HTML)이 아니라 JSON 으로 답하는가]
	//     댓글을 하나 쓸 때마다 글 전체를 다시 그리면
	//       - 읽던 위치를 잃어버리고
	//       - 화면 전체를 다시 받아 느리다
	//     그래서 댓글 영역만 자바스크립트로 다시 그린다.
	//
	//   [모두 POST + CSRF 토큰]
	//     등록/수정/삭제/추천은 상태를 바꾸는 동작이다.
	//     GET 으로 두면 <img src="...commentDelete.do?c_idx=1"> 한 줄로 삭제된다.
	//     목록 조회도 POST 로 두어 토큰 처리 방식을 하나로 통일했다.
	//
	//   [화면이 보낸 값을 믿지 않는 지점]
	//     - 작성자 이름 : 화면이 보낸 값을 쓰지 않고 세션 아이디로 DB 에서 다시 조회한다
	//     - "내 댓글인가" : 화면이 아이디를 비교하지 않고 서버가 판정해 내려준다
	//     - 권한 검증    : CommentService 가 한다 (버튼을 숨기는 것은 검증이 아니다)
	//================================================================================
	@SuppressWarnings("unchecked")
	// handleComment( ) 메소드를 만든다 — 받는 값이 길어 다음 줄로 이어진다
	private void handleComment(String action, HttpServletRequest request,
			HttpServletResponse response, PrintWriter out) throws Exception {
		// 응답이 어떤 형식(HTML·JSON 등)인지 브라우저에게 알려준다
		response.setContentType("application/json;charset=UTF-8");   // 댓글 기능은 화면 전체가 아니라 데이터만 주고받으므로 JSON 이라고 알린다
		//세션에서 로그인 아이디를 꺼낸다 (비로그인이면 null)
		HttpSession session = request.getSession(false);
		// 서버가 기억하고 있는 로그인 정보를 꺼낸다
		String loginId = (session == null) ? null : (String)session.getAttribute("id");   // 세션에서 로그인 아이디를 꺼낸다 (세션이 없으면 null = 비로그인)
		// 값에 따라 갈래를 나눈다
		switch (action) {   // 댓글 관련 2단계 주소에 따라 갈라 처리한다
			// ◀◀ 붙여넣는 자리 2 ◀◀ — 기능 장에서 "자리 2" 라고 알려주는 조각을 이 줄 아래에
default:   // /comment 로 시작하지만 아는 주소가 아닌 경우
				// response 의 sendError( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
				response.sendError(HttpServletResponse.SC_NOT_FOUND);   // 404 로 응답한다
		}
	}
	/**
	 * 댓글 기능의 실패 응답을 JSON 으로 통일해서 내려준다.
	 *
	 * 화면(read.jsp)은 ok 값만 보고 성공/실패를 판단하고,
	 * message 를 그대로 사용자에게 보여준다.
	 */
	@SuppressWarnings("unchecked")
	// writeJsonError( ) 메서드 — 2개 값을 받아 돌려주는 값 없이 일만 한다
	private void writeJsonError(PrintWriter out, String message) {
		org.json.simple.JSONObject res = new org.json.simple.JSONObject();   // 실패를 알리는 JSON 객체를 만든다
		// "ok" 이라는 이름으로 값을 담는다 (JSON 응답에 들어감)
		res.put("ok", Boolean.FALSE);   // 실패했다는 표시. 화면은 이 값을 보고 오류로 처리한다
		// "message" 이라는 이름으로 값을 담는다 (JSON 응답에 들어감)
		res.put("message", (message == null || message.trim().isEmpty())   // 메시지가 비었으면 기본 문구로 대신한다
				? "요청을 처리할 수 없습니다." : message);
		// 괄호 안의 값을 브라우저로 내보낸다 (응답 본문)
		out.write(res.toJSONString());   // JSON 을 글자로 바꿔 응답에 쓴다
	}
	//===========================================================
	// 요청 진입점 : BaseController 가 GET/POST 를 한곳으로 모아준다
	//===========================================================
	/*
	  [왜 doGet/doPost 를 직접 만들지 않는가]
	    예전에는 컨트롤러마다 doGet/doPost 를 똑같이 두 개씩 만들고
	    그 안에서 doHandle 을 불렀다. 5개 컨트롤러 = 같은 코드 10벌이다.
	    게다가 업무 예외(NotFoundException 등)를 잡아 사용자에게 안내하는 코드가
	    컨트롤러마다 달라서, 어떤 화면은 404 대신 500이 뜨고
	    어떤 화면은 빈 화면만 나왔다.
	    그래서 공통 처리를 BaseController 한곳으로 모았다.
	      - GET/POST 를 process() 하나로 합침
	      - 2단계 주소가 없으면 404
	      - InvalidInput -> 400 / Forbidden -> 403 / NotFound -> 404 / 나머지 -> 500
	      - AJAX 요청에는 HTML 에러페이지 대신 메시지 문자열로 응답
	    이 메소드는 BaseController 와 기존 doHandle 을 이어주는 다리다.
	 */
	@Override
	// process( ) 메서드 — 3개 값을 받아 돌려주는 값 없이 일만 한다
	protected void process(String action, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// doHandle( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
		doHandle(request, response);   // 실제 처리는 위에 있는 doHandle 이 한다
	}
	/* 로그인해야만 쓸 수 있는 주소 (예전 web.xml AuthFilter 대신)
	   목록·검색·읽기는 누구나, "쓰고 고치고 지우는" 주소만 막는다 */
	@Override
	// requiresLogin( ) 메서드 — 1개 값을 받아 boolean 를 돌려준다
	protected boolean requiresLogin(String action) {
		return action.equals("/write.bo")   // 아래 주소들은 로그인해야 쓸 수 있다 — 글쓰기·답글·수정·삭제·댓글, 그리고 글 읽기(read.bo)도 포함이다
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
}

	
	
	
	
	
	
	
	
	
	

