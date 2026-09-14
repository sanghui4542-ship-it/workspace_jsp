package Controller;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

import Service.BoardService;

//고객이  /컨트롤주소/Board/ 로 시작하는 주소를 요청하면 이 사장(BoardController)이 받는다.
@WebServlet("/Board/*")
public class BoardController {

	//부장(BoardService) 객체의 주소를 저장할 참조변수
	private transient BoardService boardService;
	
	//부장(CommentService) 객체의 주소를 저장할 참조변수 (무한 대댓글 + 추천 기능 담당)
	//private transient CommentService commentService;
	
	//-----------------------------------------------------------------
	// init : TomCat 이 이 서블릿을 처음 만들때 딱 1번 자동으로 부른다.
	//-----------------------------------------------------------------
	@Override
	public void init(ServletConfig config) throws ServletException {
		
		// 부장(BoardService, CommentService) 객체를 만들어서 참조변수에 저장
		boardService = new BoardService();
		//commentService = new CommentService();
	}
	//---------------------------------------------------------------
	//process : 부모 사장(BaseController)이 공통 검사를 끝낸 뒤 부르는 메소드
	//---------------------------------------------------------------
	@Override
	protected void process(String action, HttpServletRequest request, HttpServletRequest response)
			throws Exception {

		// 실제 요청 처리하는 doHandle 메소드() 호출해서 처리 한다.
		doHandle(request, response);

	

		
	}

	
	
	
	
	
	
	
	
	
	
}
