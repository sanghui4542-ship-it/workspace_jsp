package Service;

/*
 * ============================================================================
 *  BoardService  -  부장(Service) : 자유게시판 업무 규칙을 담당하는 클래스
 *
 *  사장(BoardController)의 지시를 받아
 *    - 업무 규칙(비밀번호 검증, 계층형 정렬 규칙, 필수값 검사)을 적용하고
 *    - 여러 SQL 을 하나의 작업 단위(트랜잭션)로 묶어
 *    - 사원(BoardDAO)에게 실행을 시킨다.
 *
 *  [이 클래스가 트랜잭션을 관리하는 이유]
 *    사원(BoardDAO)은 SQL 을 한 개씩만 실행한다. 여러 SQL 을 묶어서 실행할지는
 *    이 부장이 판단한다.
 *
 *      예) 새 글 등록
 *          DBCPUtil.execute(con -> {
 *              boarddao.shiftAllGroups(con);   // 1) 기존 글 순서 밀기
 *              boarddao.insertBoard(con, ...); // 2) 새 글 넣기
 *          });
 *          -> 둘 다 성공하면 commit, 하나라도 실패하면 rollback
 *
 *    두 SQL 이 각자 따로 즉시 확정(자동 커밋)되면, 1번만 성공하고 2번이
 *    실패했을 때 모든 글의 순서만 밀려나고 새 글은 없는 상태로 영구히 남는다.
 *
 *  DB 작업을 시키는 두 가지 방법 (util/DBCPUtil.java)
 *    DBCPUtil.query(con -> ...)   : 조회(select)만 할 때
 *    DBCPUtil.execute(con -> ...) : 추가·수정·삭제를 하거나, 여러 SQL 을 하나로 묶을 때
 * ============================================================================
 */

// 여러 건의 글을 순서대로 담을 목록 도구
import java.util.List;

// 사원(BoardDAO, MemberDAO), 상자(BoardVo, MemberVO, PageResult)
import Dao.BoardDAO;
import Dao.MemberDAO;
import Vo.BoardVo;
import Vo.MemberVO;
import Vo.PageResult;

// 우리가 직접 만든 예외 클래스, DB 연결 도우미
import exception.InvalidInputException;
import exception.NotFoundException;
import util.DBCPUtil;

public class BoardService {

	/*
	 [페이징 설정을 여기에 둔 이유]
	   이 숫자들을 JSP 스크립트릿 안에 직접 적어 두면, 페이지 계산이라는 업무
	   규칙이 화면 코드 속에 흩어지게 된다. 게다가 자유게시판(list.jsp)과
	   자료실(fileboardlist.jsp) 두 화면에 같은 숫자를 각각 적어 두면,
	   한쪽만 고쳤을 때 두 게시판의 동작이 서로 달라진다. 상수 하나로
	   모아 두면 고칠 곳도 한 곳, 두 게시판이 항상 같은 기준을 쓴다.
	*/
	/** 한 페이지에 보여줄 글 수 */
	public static final int NUM_PER_PAGE = 5;

	/** 한 번에 보여줄 페이지 번호 개수 ( [1][2][3][4][5] 다음 ) */
	public static final int PAGE_PER_BLOCK = 5;

	// 사원(DAO) 객체의 주소를 저장할 참조변수. final : 한 번 저장하면 다른 사원으로 바꿀 수 없다
	private final BoardDAO boarddao;
	private final MemberDAO memberdao;   // 글쓰기 화면에 작성자 정보를 채우려면 회원 표도 봐야 해서 DAO 를 하나 더 갖는다

	//----------------------------------------------------------------
	// 생성자 : 부장(BoardService)이 만들어질 때 사원 둘을 함께 만든다
	//----------------------------------------------------------------
	public BoardService() {
		this.boarddao = new BoardDAO();   // 게시판 DAO 를 하나 만들어 재사용한다
		this.memberdao = new MemberDAO();   // 회원 DAO 도 하나 만들어 재사용한다
	}

	//----------------------------------------------------------------
	// serviceMemberOne : 회원 1명 조회 (글쓰기 화면에 작성자 정보를 채우기 위해)
	//  사장의 "/write.bo" 요청, "/reply.do" 요청에서 부른다
	//----------------------------------------------------------------
	public MemberVO serviceMemberOne(String memberid) {
		return DBCPUtil.query(con -> memberdao.memberOne(con, memberid));   // 로그인한 회원의 이름·이메일을 조회해 글쓰기 화면에 미리 채워 준다
	}

	//----------------------------------------------------------------
	// serviceInsertBoard : 새 글을 등록한다
	//  사장의 "/writePro.bo" 요청에서 부른다
	//  반환값 -> 1(성공) / 0(비밀번호가 비어 있어 등록하지 않음)
	//----------------------------------------------------------------
	/**
	 * [처리 순서]
	 *   1. 비밀번호·제목·내용이 비어 있지 않은지 검사 (DB 에 가기 전에 먼저 거른다)
	 *   2. 비밀번호를 저장용으로 정리한다
	 *   3. 트랜잭션으로 "기존 글 순서 밀기 + 새 글 등록" 두 SQL 을 묶어 실행한다
	 *
	 * [왜 내용(content)까지 검사하는가]
	 *   제목과 비밀번호만 검사하고 내용은 검사하지 않으면, 내용을 비운 채
	 *   요청을 보내도 통과해 빈 글이 그대로 저장된다. 화면에는 required
	 *   속성이 있어 평소에는 막히지만, 화면을 거치지 않고 주소로 직접
	 *   요청을 보내면 화면의 제한은 아무 의미가 없다. "화면에서 막는 것"과
	 *   "서버에서 막는 것"은 다르다 — 서버에서도 반드시 다시 검사해야 한다.
	 *   (같은 이유로 비밀번호 검증도 화면이 아니라 이 서버 코드에서 한다)
	 */
	public int serviceInsertBoard(String writer, String id, String email,
								  String title, String content, String pass) {

		// 1. 업무 규칙 검증 : 글 비밀번호가 비어 있으면 등록을 막는다
		//    비밀번호를 검사하지 않으면 빈 문자열이 그대로 저장되어,
		//    나중에 아무 값(빈 문자열)으로도 수정·삭제 검증이 통과할 위험이 있다
		if (pass == null || pass.trim().isEmpty()) {
			System.out.println("[BoardService] 글 비밀번호가 비어 있어 등록을 중단합니다");
			return 0;   // 0 을 돌려주면 사장(BoardController)이 "등록 실패" 로 안내한다
		}
		if (title == null || title.trim().isEmpty()) {   // 제목이 비어 있으면
			throw new InvalidInputException("글 제목을 입력해주세요");   // 제목 없는 글은 목록에서 알아볼 수 없으므로 400 으로 막는다
		}
		if (content == null || content.trim().isEmpty()) {   // 내용이 비어 있으면 (위 설명 참고)
			throw new InvalidInputException("글 내용을 입력해주세요");
		}

		// 2. 비밀번호를 저장용으로 정리한다 (앞뒤 공백 제거)
		final String encodedPass = (pass.trim());

		/*
		 3. [트랜잭션] 두 SQL 을 하나의 작업으로 묶는다.
		    계층형 게시판은 새 글을 항상 맨 위(b_group=0)에 놓는다. 그래서
		    기존 글 전체의 b_group 을 1씩 늘려 자리를 만든 뒤에 INSERT 한다.
		    둘 중 하나만 성공하면 정렬이 영구히 어긋나므로 반드시 함께 성공해야 한다.
		*/
		return DBCPUtil.execute(con -> {

			//사원(BoardDAO)에게 시키기 : 기존 글 전체의 정렬 그룹을 BoardDAO 의 shiftAllGroups(con) 호출해서 한 칸씩 밀기 작업 명령
			boarddao.shiftAllGroups(con);

			//사원(BoardDAO)에게 시키기 : 작성자·제목·내용 등이 저장된 값을 BoardDAO 의 insertBoard(con, ...) 호출해서 새 글 추가(insert) 작업 명령
			return boarddao.insertBoard(con, writer, id, email, title, content, encodedPass);
		});
	}

	/* [정리] 이 자리에 있던 serviceBoardList() 와 serviceBoardKeyWord() 를 지웠다.
	   페이징이 없던 시절 전체 목록을 한 번에 가져오던 메소드들인데,
	   지금은 사장(BoardController)이 목록·검색 모두 serviceBoardPage() 만
	   부르기 때문에 두 메소드를 호출하는 곳이 한 군데도 없었다.
	   (이 메소드들만 쓰던 BoardDAO 의 boardList(con) / boardList(con,key,word)
	    두 개도 함께 지웠다) */

	//----------------------------------------------------------------
	// serviceBoardPage : 게시글 목록을 페이지 단위로 조회한다 (SQL 페이징)
	//  사장의 "/list.bo", "/searchlist.bo" 요청에서 부른다
	//----------------------------------------------------------------
	/**
	 * [처리 순서]
	 *   1. 조건에 맞는 전체 글 개수를 센다  (select count(*))
	 *   2. 전체 개수로 전체 페이지 수를 계산하고, 요청 페이지 번호를 범위 안으로 보정한다
	 *   3. 보정된 페이지가 시작할 위치(offset)를 계산해 그 페이지 몫만 조회한다  (limit ?, ?)
	 *   4. 목록과 계산된 페이지 정보를 PageResult 상자에 담아 반환한다
	 *
	 * [예전 방식과 무엇이 다른가]
	 *   예전에는 DAO 가 조건에 맞는 글 전체를 조회해서 List 로 돌려주면,
	 *   JSP 의 스크립트릿이 그 목록에서 5건만 잘라 화면에 출력했다. 그러면
	 *   1페이지(5건)를 보여주기 위해 글이 10,000건이어도 10,000개의 객체를
	 *   전부 만들어 놓고 9,995개는 버리는 셈이 된다. 글이 늘어날수록,
	 *   동시 접속자가 늘수록 서버 메모리와 속도에 부담이 커진다.
	 *   지금은 count(*) 로 개수만 먼저 세고, limit 으로 그 페이지 몫(5건)만
	 *   가져오므로 글이 아무리 많아도 매번 5건만 읽어 속도가 일정하다.
	 *
	 * [개수 조회와 목록 조회를 한 트랜잭션으로 묶는 이유]
	 *   두 SQL 사이에 다른 사람이 글을 쓰거나 지우면, "전체는 23건이라고
	 *   방금 셌는데 목록은 24건 기준으로 잘린" 것처럼 살짝 어긋날 수 있다.
	 *   같은 연결에서 곧바로 이어서 실행해 그 틈을 최대한 줄인다.
	 *
	 * @param key     검색 기준 ("titleContent" 또는 작성자명, 검색 없으면 null)
	 * @param word    검색어 (검색 없으면 null 또는 빈 문자열)
	 * @param nowPage 사장(Controller)이 요청 주소에서 읽어 넘긴 현재 페이지 번호 (0부터 시작)
	 * @return 이 페이지의 글 목록 + 계산된 페이지 정보가 함께 담긴 PageResult
	 */
	public PageResult<BoardVo> serviceBoardPage(String key, String word, int nowPage) {

		final int requestedPage = (nowPage < 0) ? 0 : nowPage;   // 음수 페이지(예: 주소창에 nowPage=-3)로 들어오면 0페이지로 끌어올린다

		return DBCPUtil.query(con -> {   // 조회만 하므로 query 로 연결을 빌린다

			//1) 조건에 맞는 전체 글 개수
			int totalRecord = boarddao.countBoards(con, key, word);

			/*
			 2) 페이지 번호 보정
			    글이 삭제되어 페이지 수가 줄었는데 사용자가 예전 페이지 번호로 들어오면
			    빈 화면이 나온다. 범위를 넘으면 마지막 페이지로 맞춘다.
			*/
			int totalPage = (totalRecord == 0) ? 0
							: (int) Math.ceil((double) totalRecord / NUM_PER_PAGE);

			int safePage = requestedPage;   // 보정할 페이지 번호를 담을 변수
			if (totalPage > 0 && safePage >= totalPage) {   // 페이지가 있는데 요청 번호가 마지막을 넘어섰다면
				safePage = totalPage - 1;   // 마지막 페이지로 맞춘다
			}
			
			System.out.println("safePage : " + safePage );
			
			//3) 그 페이지번호에 해당하는 글만 조회
			int offset = safePage * NUM_PER_PAGE;
			List<BoardVo> list = boarddao.boardList(con, key, word, offset, NUM_PER_PAGE);   // offset 만큼 건너뛰고 5건만 가져온다 (limit ?, ?)

			//4) 화면이 그대로 출력할 수 있도록 페이지 정보를 계산해 담는다
			return new PageResult<BoardVo>(list, totalRecord, safePage, NUM_PER_PAGE, PAGE_PER_BLOCK);
		});
	}

	//----------------------------------------------------------------
	// serviceBoardRead : 게시글 1건을 조회하면서 조회수를 1 올린다
	//  사장의 "/read.bo" 요청에서 부른다
	//----------------------------------------------------------------
	/**
	 * [처리 순서]
	 *   1. 주소로 받은 글번호(문자)를 숫자로 바꾼다
	 *   2. 그 번호의 글이 있는지 먼저 확인한다
	 *   3. 있으면 조회수를 1 올리고, 올라간 값을 반영한 글 정보를 돌려준다
	 *
	 * [글 조회와 조회수 증가를 한 트랜잭션(execute)으로 묶는 이유]
	 *   같은 연결(con) 안에서 실행하기 때문에, "조회수를 올린 바로 그 순간의
	 *   값"을 안정적으로 화면에 보여줄 수 있다. 연결이 서로 다르면 두 작업
	 *   사이에 다른 사람의 조회가 끼어들어 값이 어긋날 가능성이 생긴다.
	 *
	 * [없는 글을 요청했을 때 - 404 로 응답하는 이유]
	 *   예전에는 없는 글이면 null 을 그대로 화면(JSP)에 넘겼다. 화면은 빈
	 *   값으로 상세 화면을 억지로 그리거나, null 을 다루다 500 오류를 냈다.
	 *   둘 다 "이 글은 존재하지 않는다"는 사실을 사용자에게 명확히
	 *   전달하지 못한다. 없는 자원을 요청했을 때의 올바른 응답은 404 다.
	 *   NotFoundException 을 던지면 BaseController 가 상태코드 404 와
	 *   /error/404.jsp 안내 화면으로 자동 연결해 준다.
	 *
	 * @param b_idx_ 사장(Controller)이 주소에서 읽어 넘긴 글번호 (아직 문자열)
	 * @return 조회수가 1 반영된 글 정보
	 * @throws InvalidInputException 글번호가 숫자가 아닐 때 (parseIdx 에서 발생)
	 * @throws NotFoundException     그 번호의 글이 없을 때
	 */
	public BoardVo serviceBoardRead(String b_idx_) {

		final int b_idx = parseIdx(b_idx_);   // 주소로 온 글번호를 숫자로 바꾼다 (숫자가 아니면 여기서 400 예외)

		return DBCPUtil.execute(con -> {   // 조회수 증가(수정)가 섞이므로 query 가 아니라 execute 를 쓴다

			//1) 글이 있는지부터 확인한다 (없는 글에 조회수를 올릴 필요가 없다)
			BoardVo found = boarddao.selectOne(con, b_idx);

			if (found == null) {
				throw new NotFoundException("요청한 게시글을 찾을 수 없습니다 (글번호 " + b_idx + ")");
			}

			boarddao.increaseReadCount(con, b_idx); //2) 조회수 +1

			//조회수를 올린 뒤의 값을 화면에 보여주기 위해 1 더해서 넘긴다
			found.setB_cnt(found.getB_cnt() + 1);

			return found;   // 조회수까지 반영된 글 한 건을 화면에 돌려준다
		});
	}

	//----------------------------------------------------------------
	// servicePassCheck : 글 비밀번호가 맞는지 확인한다 (수정·삭제 버튼 활성화 용도)
	//  사장의 "/password.do" 요청에서 부른다
	//----------------------------------------------------------------
	public boolean servicePassCheck(String b_idx, String password) {
		return matchesBoardPassword(b_idx, password);   // 아래에 만들어 둔 공통 검증 메소드를 그대로 쓴다
	}

	//----------------------------------------------------------------
	// serviceUpdateBoard : 게시글을 수정한다
	//  사장의 "/updateBoard.do" 요청에서 부른다
	//  반환값 -> "수정성공" / "수정실패" / "비밀번호틀림"
	//----------------------------------------------------------------
	/**
	 * [처리 순서]
	 *   1. 저장된 글 비밀번호를 꺼내 입력값과 비교한다
	 *   2. 비교를 통과했을 때만 실제 수정(update)을 실행한다
	 *   3. 비밀번호 검증과 수정을 하나의 트랜잭션으로 묶어, 검증 직후 다른
	 *      요청이 끼어들어 값이 바뀌는 상황을 줄인다
	 *
	 * [권한 우회 취약점을 고친 부분]
	 *   예전 버전의 이 메소드는 매개변수에 비밀번호가 아예 없었다. 비밀번호
	 *   확인은 별도의 AJAX 요청(/Board/password.do)으로만 하고, 그 결과로
	 *   화면의 [수정] 버튼을 보이거나 숨기는 방식이었다. 그래서 화면을
	 *   거치지 않고 아래 요청을 그대로 보내면 비밀번호 없이도 수정이 됐다.
	 *
	 *       POST /CarProject/Board/updateBoard.do
	 *       idx=1&title=바뀐제목&content=바뀐내용&email=a@a.com
	 *
	 *   화면에서 버튼을 숨기는 것은 사용자를 위한 "안내"일 뿐이고, 실제
	 *   권한 검사는 반드시 서버(이 메소드)에서 해야 한다.
	 */
	public String serviceUpdateBoard(String idx_, String email_, String title_, String content_, String pass_) {

		final int b_idx = parseIdx(idx_);   // 주소로 온 글번호를 숫자로 바꾼다

		//검증과 수정을 한 트랜잭션으로 묶는다 (검증 직후 값이 바뀌는 것을 막는다)
		return DBCPUtil.execute(con -> {

			//1. 저장된 비밀번호를 꺼내 입력값과 비교
			String stored = boarddao.findPasswordByIdx(con, b_idx);

			if (stored == null) {   // 그 글번호의 저장값을 못 찾은 경우 = 없는 글
				return "수정실패"; //존재하지 않는 글
			}
			if (pass_ == null || !java.util.Objects.equals(pass_, stored)) {   // 비밀번호를 안 보냈거나 저장값과 다르면
				System.out.println("[BoardService] 글 수정 거부 - 비밀번호 불일치 b_idx=" + b_idx);   // 누가 언제 실패했는지 콘솔에 남겨 둔다
				return "비밀번호틀림";   // 화면에 "비밀번호가 틀렸습니다" 를 띄우게 한다
			}

			//2. 검증을 통과했을 때만 수정한다
			int result = boarddao.updateBoard(con, b_idx, email_, title_, content_);

			return (result == 1) ? "수정성공" : "수정실패";   // 1건이 바뀌었으면 성공, 아니면 실패 문구를 돌려준다
		});
	}

	//----------------------------------------------------------------
	// serviceDeleteBoard : 게시글을 삭제한다
	//  사장의 "/deleteBoard.do" 요청에서 부른다
	//  반환값 -> "삭제성공" / "삭제실패" / "비밀번호틀림"
	//----------------------------------------------------------------
	/**
	 * [예전 버전의 심각한 문제]
	 *   과거 이 메소드는 아래처럼 글번호만 받아 바로 삭제했다.
	 *       public String serviceDeleteBoard(String delete_idx) {
	 *           return boarddao.deleteBoard(delete_idx);   // 글번호만 있으면 삭제
	 *       }
	 *   즉 아래 요청 주소 한 줄로 1번 글이 삭제됐다. 비밀번호도, 로그인도
	 *   전혀 필요 없었다.
	 *       GET /CarProject/Board/deleteBoard.do?b_idx=1
	 *   글번호가 1,2,3... 순서대로 증가하므로, 반복문으로 전체 글 삭제도
	 *   가능한 상태였다. 지금은 반드시 저장된 비밀번호와 일치할 때만 지운다.
	 */
	public String serviceDeleteBoard(String delete_idx, String pass_) {

		final int b_idx = parseIdx(delete_idx);   // 주소로 온 글번호를 숫자로 바꾼다

		return DBCPUtil.execute(con -> {   // 검증과 삭제를 한 흐름으로 묶는다

			//1. 비밀번호 검증
			String stored = boarddao.findPasswordByIdx(con, b_idx);

			if (stored == null) {   // 그 글번호의 저장값을 못 찾은 경우 = 없는 글
				return "삭제실패";   // 지울 것이 없으므로 실패로 돌려준다
			}
			if (pass_ == null || !java.util.Objects.equals(pass_, stored)) {   // 비밀번호를 안 보냈거나 저장값과 다르면
				System.out.println("[BoardService] 글 삭제 거부 - 비밀번호 불일치 b_idx=" + b_idx);   // 누가 언제 실패했는지 콘솔에 남겨 둔다
				return "비밀번호틀림";   // 삭제를 실행하지 않고 실패 문구를 돌려준다
			}

			//2. 검증 통과 후 삭제
			int result = boarddao.deleteBoard(con, b_idx);

			return (result == 1) ? "삭제성공" : "삭제실패";   // 1건이 지워졌으면 성공, 아니면 실패 문구를 돌려준다
		});
	}

	//----------------------------------------------------------------
	// serviceReplyInsertBoard : 답글을 등록한다
	//  사장의 "/replyPro.do" 요청에서 부른다
	//----------------------------------------------------------------
	/**
	 * [계층형 게시판 답글 규칙 3가지]
	 *   규칙1. 원글보다 아래에 있는(= b_group 이 더 큰) 글들의 b_group 을 1씩
	 *          늘려 답글이 들어갈 자리를 만든다
	 *   규칙2. 답글의 b_group = 원글의 b_group + 1   (원글 바로 아래에 표시)
	 *   규칙3. 답글의 b_level = 원글의 b_level + 1   (한 칸 더 들여쓰기)
	 *
	 * [처리 순서]
	 *   1. 답글 비밀번호가 비어 있지 않은지 확인한다
	 *   2. [트랜잭션] 원글의 위치 조회 -> 아래쪽 글들 밀기 -> 답글 등록, 세 SQL 을 하나로 묶는다
	 *
	 * [트랜잭션이 꼭 필요한 이유]
	 *   SQL 이 3개(조회 + 밀기 + 등록)이고, "밀기"만 성공하고 "등록"이
	 *   실패하면 밀어서 만든 빈 자리가 그대로 남아 게시판 정렬이 깨진다.
	 */
	public void serviceReplyInsertBoard(String super_b_idx, String reply_id, String reply_name,
										String reply_email, String reply_title, String reply_content,
										String reply_pass) {

		//1. 답글 비밀번호도 원글과 같은 기준으로 검사한다
		if (reply_pass == null || reply_pass.trim().isEmpty()) {
			System.out.println("[BoardService] 답글 비밀번호가 비어 있어 등록을 중단합니다");
			return;   // 아무것도 하지 않고 메소드를 끝낸다 (return 뒤에 값이 없는 이유는 반환형이 void 라서다)
		}
		final String encodedPass = (reply_pass.trim());   // 앞뒤 공백을 없앤 답글 비밀번호
		final int parentIdx = parseIdx(super_b_idx);   // 답글을 달 원글의 번호를 숫자로 바꾼다

		//2. [트랜잭션] 조회 + 밀기 + 등록을 한 작업으로 묶는다
		DBCPUtil.execute(con -> {

			//2.1. 원글의 정렬 그룹 / 들여쓰기 깊이 조회
			int[] groupLevel = boarddao.findGroupAndLevel(con, parentIdx);

			if (groupLevel == null) {   // 원글을 못 찾은 경우
				//원글이 없으면 답글도 만들지 않는다 (기존에는 여기서 SQLException 이 났다)
				System.out.println("[BoardService] 답글을 달 원글이 없습니다. b_idx=" + parentIdx);
				return 0;   // 아무것도 저장하지 않고 0 을 돌려준다
			}

			int parentGroup = groupLevel[0];   // 원글의 정렬 그룹번호
			int parentLevel = groupLevel[1];   // 원글의 들여쓰기 깊이

			//2.2. 원글보다 아래쪽 글들의 그룹값 밀기 (규칙1)
			boarddao.shiftGroupsGreaterThan(con, parentGroup);

			//2.3. 답글 등록 (규칙2, 규칙3)
			return boarddao.insertReply(con, reply_id, encodedPass, reply_name, reply_email,
										reply_title, reply_content,
										parentGroup + 1, parentLevel + 1);
		});
	}

	//===========================================================
	// 내부 공통
	//===========================================================

	/**
	 * 글번호 문자열을 정수로 바꾼다.
	 *
	 * 예전에는 Integer.parseInt() 를 검사 없이 그대로 호출해서, 주소창에서
	 * 글번호를 지우거나 문자를 넣으면 NumberFormatException 때문에 500 오류가
	 * 났다. 잘못된 입력은 서버의 잘못(500)이 아니라 요청 자체의 잘못(400)이므로
	 * InvalidInputException 을 던져 BaseController 가 400 으로 응답하게 한다.
	 */
	private int parseIdx(String value) {
		try {
			return Integer.parseInt(value.trim());   // 글자를 숫자로 바꾼다
		} catch (Exception e) {
			throw new InvalidInputException("글번호가 올바르지 않습니다 : " + value);   // 숫자가 아니면 400 으로 막는다. 여기서 막지 않으면 SQL 단계에서 알 수 없는 오류가 난다
		}
	}

	/**
	 * 글 비밀번호가 맞는지 확인한다 (단독 확인 요청용).
	 * @return true = 일치,  false = 불일치 또는 글 없음 또는 입력값 자체가 없음
	 */
	private boolean matchesBoardPassword(String b_idx, String password) {

		if (b_idx == null || password == null || password.isEmpty()) {   // 글번호나 비밀번호가 없으면 확인할 수 없다
			return false;   // 불일치로 처리한다
		}

		final int idx = parseIdx(b_idx);   // 글번호를 숫자로 바꾼다

		return DBCPUtil.query(con -> {   // 조회만 하므로 query 로 연결을 빌린다
			String stored = boarddao.findPasswordByIdx(con, idx);   // 저장된 글 비밀번호를 꺼낸다
			if (stored == null) {   // 그런 글이 없으면
				return Boolean.FALSE;   // 불일치로 처리한다
			}
			return Boolean.valueOf(java.util.Objects.equals(password, stored));   // 입력값과 저장값이 같은지 확인해 true/false 로 돌려준다
		}).booleanValue();   // Boolean 상자에서 진짜 true/false 값을 꺼낸다
	}
}
