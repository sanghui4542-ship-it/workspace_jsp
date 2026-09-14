package Service;

/*
 * ============================================================================
 *  CommentService  -  부장(Service) : 자유게시판 댓글(무한 대댓글 + 추천) 업무 규칙 담당
 *
 *  이 계층이 책임지는 것 5가지
 *    1. 입력 검증        빈 내용, 너무 긴 내용, 로그인 여부
 *    2. 권한 판단        본인이 쓴 댓글만 고치거나 지울 수 있다
 *    3. 삭제 방식 결정   답글이 달려 있으면 소프트 삭제, 없으면 물리 삭제
 *    4. 트리 조립        DB 에서 평면(한 줄씩)으로 온 댓글을 부모-자식 구조로 묶는다
 *    5. 트랜잭션 경계    여러 SQL 을 하나로 묶어야 할 때 DBCPUtil 로 감싼다
 *
 *  [사원(DAO)과의 역할 분담]
 *    사원(CommentDAO)은 SQL 한 문장만 안다. "언제 어떤 SQL 을 부를지",
 *    "이 사람이 이 댓글을 수정해도 되는지" 같은 업무 규칙은 이 부장이 결정한다.
 * ============================================================================
 */

// 목록·순서없는집합·이름표로찾는자료구조 도구
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 사원(CommentDAO), 상자(CommentVo)
import Dao.CommentDAO;
import Vo.CommentVo;

// 우리가 직접 만든 예외 클래스, DB 연결 도우미
import exception.ForbiddenException;
import exception.InvalidInputException;
import exception.NotFoundException;
import util.DBCPUtil;

public class CommentService {

	// 사원(CommentDAO) 객체의 주소를 저장할 참조변수. final : 다른 DAO 로 바뀌지 않는다
	private final CommentDAO commentDao;

	/** 댓글 내용 최대 길이. 화면(input 의 maxlength)과 서버 두 곳에서 함께 막는다 */
	public static final int MAX_CONTENT_LENGTH = 1000;

	/** 화면에서 들여쓰기를 적용할 최대 단계. 저장 구조 자체의 깊이 제한은 없다 */
	public static final int MAX_VISUAL_DEPTH = 5;

	//----------------------------------------------------------------
	// 생성자 : 부장(CommentService)이 만들어질 때 사원(CommentDAO)도 함께 만든다
	//----------------------------------------------------------------
	public CommentService() {
		this.commentDao = new CommentDAO();   // DAO 를 하나 만들어 계속 재사용한다
	}

	//----------------------------------------------------------------
	// getComments : 글 하나의 댓글을 트리(부모-자식) 구조로 조립해 돌려준다
	//  사장(BoardController)의 "/comment/commentList.do" 요청에서 부른다
	//----------------------------------------------------------------
	/**
	 * [평면 목록을 트리로 묶는 방법]
	 *
	 *   DB 에서 온 것 (부모 번호만 있는 평면 목록)
	 *       c_idx=1 parent=null
	 *       c_idx=2 parent=null
	 *       c_idx=3 parent=1
	 *       c_idx=4 parent=3
	 *
	 *   1) 댓글번호 -> 댓글 로 즉시 찾을 수 있는 Map 을 먼저 만든다
	 *   2) 각 댓글을 훑으며
	 *        parent 가 null 이면  -> 최상위 목록(뿌리)에 담는다
	 *        parent 가 있으면     -> Map 으로 그 부모를 찾아 부모의 children 에 담는다
	 *   3) 결과 (들여쓰기로 표현하면)
	 *        1
	 *        └ 3
	 *          └ 4
	 *        2
	 *
	 * [SQL 재귀(WITH RECURSIVE)를 쓰지 않고 자바에서 조립하는 이유]
	 *   MySQL 8 은 재귀 쿼리를 지원하지만, 게시글 하나에 달리는 댓글 수는
	 *   많아도 수백 건 수준이다. 그 정도 양은 한 번의 SQL 로 전부 읽어와서
	 *   자바 코드로 묶는 편이 SQL 문법도 단순하고, 문제가 생겼을 때
	 *   디버깅(어디서 잘못됐는지 찾기)도 훨씬 쉽다. "DB 가 할 수 있는 일"과
	 *   "DB 가 하는 것이 더 나은 일"은 다르다.
	 *
	 * @param loginId 지금 로그인한 아이디 (비로그인이면 null). 추천 버튼 상태를 만드는 데 쓴다
	 * @return 최상위 댓글 목록. 각 댓글의 children 안에 그 답글들이 들어 있다
	 */
	public List<CommentVo> getComments(int bIdx, String loginId) {

		//사원(CommentDAO)에게 시키기 : 글번호와 로그인 아이디를 CommentDAO 의 selectByBoard(...) 호출해서 그 글의 댓글 전체를 평면 목록으로 조회(select) 명령
		List<CommentVo> flat = DBCPUtil.query(con -> commentDao.selectByBoard(con, bIdx, loginId));

		//1) 번호로 즉시 찾을 수 있게 Map 에 담는다
		Map<Integer, CommentVo> byId = new HashMap<Integer, CommentVo>();
		for (CommentVo c : flat) {   // 가져온 댓글을 하나씩 훑는다
			byId.put(Integer.valueOf(c.getCIdx()), c);   // "댓글번호 → 댓글" 로 담아 두면 부모를 번호만으로 바로 찾을 수 있다
		}

		//2) 부모-자식으로 연결한다
		List<CommentVo> roots = new ArrayList<CommentVo>();

		for (CommentVo c : flat) {   // 이번에는 부모-자식을 연결하려고 다시 한 번 훑는다

			Integer parentIdx = c.getParentIdx();   // 이 댓글의 부모 번호. 최상위 댓글이면 null 이다

			if (parentIdx == null) {   // 부모가 없으면 최상위 댓글이다
				roots.add(c);   // 뿌리 목록에 담는다
				continue;
			}

			CommentVo parent = byId.get(parentIdx);   // 부모 번호로 부모 댓글을 찾는다

			if (parent == null) {   // 부모를 못 찾은 경우
				/* 부모를 찾지 못한 경우.
				   정상적으로는 일어나지 않지만(외래키가 막는다),
				   데이터를 직접 손댔을 때를 대비해 최상위로 올려 화면에서 사라지지 않게 한다. */
				roots.add(c);
			} else {
				parent.getChildren().add(c);   // 찾았으면 그 부모의 자식 목록에 매단다
			}
		}

		//3) 들여쓰기 단계를 계산해 둔다 (화면이 계산하지 않도록)
		for (CommentVo root : roots) {
			assignDepth(root, 0);
		}

		return roots;   // 최상위 댓글들을 돌려준다. 답글은 각자의 children 안에 들어 있다
	}

	/**
	 * 트리를 따라 내려가며 depth(들여쓰기 단계)를 채운다.
	 * 자기 자신을 다시 부르는 방식(재귀)이라, 답글이 몇 단계로 깊어지든
	 * 같은 코드 한 벌로 전부 처리된다.
	 */
	private void assignDepth(CommentVo node, int depth) {

		//들여쓰기는 화면이 무너지지 않도록 최대 단계에서 멈춘다 (저장 구조의 깊이는 무제한이다)
		node.setDepth(Math.min(depth, MAX_VISUAL_DEPTH));

		for (CommentVo child : node.getChildren()) {   // 자식들도 같은 방법으로 한 단계 더 깊게 계산한다
			assignDepth(child, depth + 1);   // 자기 자신을 다시 부르는 것을 재귀(recursion)라고 한다. 깊이가 얼마든 처리된다
		}
	}

	//----------------------------------------------------------------
	// flatten : 트리를 "화면에 출력할 순서" 그대로 한 줄(1차원 목록)로 펼친다
	//  사장(BoardController)의 "/comment/commentList.do" 요청에서 getComments() 다음에 부른다
	//  (JSP/자바스크립트가 재귀 호출 없이 for 문 하나로 그릴 수 있게 하기 위해서다)
	//----------------------------------------------------------------
	public List<CommentVo> flatten(List<CommentVo> roots) {

		List<CommentVo> out = new ArrayList<CommentVo>();   // 펼친 결과를 담을 빈 목록
		for (CommentVo root : roots) {   // 뿌리 댓글부터 차례로
			flattenInto(root, out);   // 그 아래 답글까지 순서대로 펼쳐 담는다
		}
		return out;   // 화면이 위에서 아래로 그대로 출력하면 되는 한 줄 목록
	}

	// 한 노드와 그 자식들을 "부모 다음에 자식" 순서로 목록에 담는 재귀 메소드
	private void flattenInto(CommentVo node, List<CommentVo> out) {
		out.add(node);   // 먼저 자기 자신을 담고
		for (CommentVo child : node.getChildren()) {   // 그 다음 자식들을 차례로
			flattenInto(child, out);   // 같은 방법으로 펼쳐 담는다 (자기 자신을 다시 부른다)
		}
	}

	/* [정리] 이 자리에 있던 countComments() 를 지웠다.
	   게시글 목록 화면에 "댓글 3" 처럼 개수를 표시하려고 만들어 둔 메소드인데,
	   목록 화면(board/list.jsp)은 실제로 그 숫자를 보여주지 않아서
	   호출하는 곳이 한 군데도 없었다. 나중에 목록에 댓글 수를 표시하려면
	   이 메소드와 CommentDAO.countByBoard() 를 다시 만들면 된다.
	   (그 메소드만 쓰던 CommentDAO.countByBoard() 도 함께 지웠다) */

	//----------------------------------------------------------------
	// addComment : 새 댓글(또는 답글)을 등록한다
	//  사장의 "/comment/commentAdd.do" 요청에서 부른다
	//----------------------------------------------------------------
	/**
	 * [처리 순서]
	 *   1. 로그인 여부를 확인한다
	 *   2. 내용이 비었거나 너무 길지 않은지 확인하고, 앞뒤 공백을 정리한다
	 *   3. 저장할 정보를 CommentVo 상자에 담는다
	 *   4. 답글이면, 부모 댓글이 실제로 있는지 + 같은 글의 댓글이 맞는지 확인한다
	 *   5. 확인을 모두 통과하면 저장하고 새 댓글번호를 돌려준다
	 *
	 * [답글일 때 부모를 검증하는 이유]
	 *   parent_idx 값은 화면(자바스크립트)이 요청에 실어 보내는 값이다.
	 *   브라우저 개발자도구로 이 값을 다른 글의 댓글번호로 바꿔 보내면,
	 *   A 게시글의 댓글이 엉뚱하게 B 게시글의 답글로 붙어버릴 수 있다.
	 *   DB 의 외래키 제약은 "그 댓글번호가 실제로 존재하는지" 만 확인해 줄 뿐,
	 *   "같은 게시글에 속한 댓글인지" 까지는 확인해 주지 않는다. 그래서 이
	 *   서비스 계층에서 부모 댓글을 직접 조회해 글번호(bIdx)가 같은지 확인한다.
	 *
	 * @param parentIdx 답글이면 부모 댓글번호, 최상위 댓글이면 null
	 * @return 새로 만들어진 댓글번호
	 * @throws ForbiddenException     로그인하지 않았을 때
	 * @throws InvalidInputException  내용이 비었거나 너무 길 때, 부모 댓글이 다른 글의 것일 때
	 * @throws NotFoundException      답글의 부모로 지정한 댓글이 없을 때
	 */
	public int addComment(int bIdx, Integer parentIdx, String loginId, String loginName, String content) {

		requireLogin(loginId);   // 로그인하지 않았으면 여기서 403 예외가 난다
		String clean = requireContent(content);   // 내용이 비었거나 너무 길면 여기서 400 예외가 난다

		final CommentVo vo = new CommentVo();   // 저장할 댓글 정보를 담을 상자를 만든다
		vo.setBIdx(bIdx);   // 어느 글의 댓글인지
		vo.setParentIdx(parentIdx);   // 부모 댓글번호 (최상위면 null)
		vo.setCId(loginId);   // 작성자 아이디
		//이름이 없으면 아이디로 대체한다 (member 에 이름이 비어 있는 예외 상황 방어)
		vo.setCName((loginName == null || loginName.trim().isEmpty()) ? loginId : loginName.trim());
		vo.setCContent(clean);   // 검사를 통과한 내용

		return DBCPUtil.execute(con -> {   // 확인과 저장을 한 흐름으로 묶는다

			/*
			 답글이라면 부모 댓글이 정말 존재하는지, 그리고 "같은 글" 의 댓글인지 확인한다.
			 (자세한 이유는 위 메소드 설명 [답글일 때 부모를 검증하는 이유] 참고)
			*/
			if (parentIdx != null) {

				//사원(CommentDAO)에게 시키기 : 부모 댓글번호를 CommentDAO 의 selectOne(...) 호출해서 그 댓글이 실제로 있는지 조회(select) 명령
				CommentVo parent = commentDao.selectOne(con, parentIdx.intValue());
				if (parent == null) {   // 부모로 지정한 댓글이 없으면
					throw new NotFoundException("답글을 달 댓글을 찾을 수 없습니다");   // 없는 댓글에 답글을 달 수는 없으므로 404 로 막는다
				}
				if (parent.getBIdx() != bIdx) {   // 부모 댓글이 다른 글의 것이면
					throw new InvalidInputException("잘못된 요청입니다");   // A 글의 댓글이 B 글 밑에 붙는 것을 막는다
				}
			}

			//사원(CommentDAO)에게 시키기 : 준비된 CommentVo 상자를 CommentDAO 의 insert(con, vo) 호출해서 댓글 추가(insert) 작업 명령
			return Integer.valueOf(commentDao.insert(con, vo));   // 확인을 통과했으니 저장하고 새 댓글번호를 받는다

		}).intValue();   // Integer 상자에서 진짜 숫자를 꺼낸다
	}

	//----------------------------------------------------------------
	// editComment : 댓글 내용을 수정한다
	//  사장의 "/comment/commentEdit.do" 요청에서 부른다
	//----------------------------------------------------------------
	/**
	 * [처리 순서]
	 *   1. 로그인 여부, 내용 유효성을 확인한다
	 *   2. 수정할 댓글이 실제로 있는지, 이미 삭제된 것은 아닌지 확인한다
	 *   3. 로그인한 사람이 그 댓글의 작성자 본인인지 확인한다 (권한 검사)
	 *   4. 모두 통과했을 때만 실제로 수정한다
	 *
	 * [작성자 본인 확인을 반드시 서버에서 해야 하는 이유]
	 *   화면(read.jsp)은 [수정] 버튼을 본인이 쓴 댓글에만 보여주지만, 그것은
	 *   화면이 사용자에게 주는 "안내" 일 뿐이다. 주소나 요청을 직접 조립해
	 *   보내면 화면의 버튼 숨김은 아무 의미가 없다. 실제 권한 검사는 반드시
	 *   여기(서버)에서 다시 해야 한다 — 게시글 수정·삭제가 예전에 화면
	 *   검증에만 의존해 누구나 수정할 수 있었던 것과 완전히 같은 이유다.
	 *
	 * @throws ForbiddenException    로그인하지 않았을 때, 본인 댓글이 아닐 때
	 * @throws InvalidInputException 내용이 비었거나 너무 길 때, 이미 삭제된 댓글일 때, DB 수정이 0건일 때
	 * @throws NotFoundException     그 번호의 댓글이 없을 때
	 */
	public void editComment(int cIdx, String loginId, String content) {

		requireLogin(loginId);   // 로그인하지 않았으면 여기서 403 예외가 난다
		final String clean = requireContent(content);   // 내용이 비었거나 너무 길면 여기서 400 예외가 난다

		DBCPUtil.execute(con -> {   // 확인과 수정을 한 흐름으로 묶는다

			//사원(CommentDAO)에게 시키기 : 댓글번호를 CommentDAO 의 selectOne(...) 호출해서 수정할 댓글 조회(select) 명령
			CommentVo target = commentDao.selectOne(con, cIdx);   // 수정할 댓글을 먼저 찾는다

			if (target == null) {   // 그런 댓글이 없으면
				throw new NotFoundException("댓글을 찾을 수 없습니다");   // 404 로 막는다
			}
			if (target.isDeleted()) {   // 이미 삭제 표시된 댓글이면
				throw new InvalidInputException("삭제된 댓글은 수정할 수 없습니다");   // 내용이 비어 있는 뼈대라 수정할 것이 없다
			}

			//[권한 확인] 작성자 본인만 수정할 수 있다 (자세한 이유는 위 메소드 설명 참고)
			if (!loginId.equals(target.getCId())) {
				throw new ForbiddenException("본인이 쓴 댓글만 수정할 수 있습니다");
			}

			//사원(CommentDAO)에게 시키기 : 댓글번호·로그인 아이디·새 내용을 CommentDAO 의 update(...) 호출해서 댓글 수정(update) 작업 명령
			int updated = commentDao.update(con, cIdx, loginId, clean);   // 작성자 본인일 때만 바뀐다 (DAO 의 where 에 c_id 조건이 들어 있다)
			if (updated != 1) {   // 1건이 안 바뀌었다면 권한이 없거나 조건에 안 맞은 것이다
				throw new InvalidInputException("댓글을 수정하지 못했습니다");   // 조용히 넘어가면 사용자는 수정된 줄 안다
			}
			return Integer.valueOf(updated);   // 바뀐 행 수를 돌려준다
		});
	}

	//----------------------------------------------------------------
	// deleteComment : 댓글을 삭제한다 (답글이 있으면 뼈대만 남기고, 없으면 완전히 지운다)
	//  사장의 "/comment/commentDelete.do" 요청에서 부른다
	//----------------------------------------------------------------
	/**
	 * [처리 순서]
	 *   1. 로그인 여부를 확인한다
	 *   2. 지울 댓글이 실제로 있는지, 작성자가 나인지 확인한다
	 *   3. 답글이 달려 있는지 확인해 소프트 삭제(뼈대 남기기)와 물리 삭제(완전 삭제) 중 고른다
	 *   4. 삭제를 실행하고, 물리 삭제였다면 이제 자식이 없어져 불필요해진
	 *      "삭제된 댓글입니다" 뼈대가 있으면 함께 정리한다
	 *
	 * @return true = 답글이 있어 뼈대(소프트 삭제)를 남겼음 / false = 완전히 지웠음(물리 삭제)
	 * @throws ForbiddenException    로그인하지 않았을 때, 본인 댓글이 아닐 때
	 * @throws InvalidInputException 삭제(update/delete)가 0건으로 끝났을 때
	 * @throws NotFoundException     그 번호의 댓글이 없을 때
	 */
	public boolean deleteComment(int cIdx, String loginId) {

		requireLogin(loginId);   // 로그인하지 않았으면 여기서 403 예외가 난다

		return DBCPUtil.execute(con -> {   // 확인·삭제·정리를 한 흐름으로 묶는다

			//사원(CommentDAO)에게 시키기 : 댓글번호를 CommentDAO 의 selectOne(...) 호출해서 지울 댓글 조회(select) 명령
			CommentVo target = commentDao.selectOne(con, cIdx);   // 지울 댓글을 먼저 찾는다

			if (target == null) {   // 그런 댓글이 없으면
				throw new NotFoundException("댓글을 찾을 수 없습니다");   // 404 로 막는다
			}
			if (!loginId.equals(target.getCId())) {   // 작성자가 내가 아니면
				throw new ForbiddenException("본인이 쓴 댓글만 삭제할 수 있습니다");   // 남의 댓글은 지울 수 없으므로 403 으로 막는다
			}

			//사원(CommentDAO)에게 시키기 : 댓글번호를 CommentDAO 의 hasChildren(...) 호출해서 답글이 달려 있는지 조회(select) 명령
			boolean soft = commentDao.hasChildren(con, cIdx);   // 답글이 달려 있으면 소프트 삭제, 없으면 물리 삭제로 결정한다

			//사원(CommentDAO)에게 시키기 : 위에서 정한 방식에 따라 softDelete(뼈대만 남김) 또는 hardDelete(완전 삭제) 호출해서 댓글 삭제 작업 명령
			int affected = soft ? commentDao.softDelete(con, cIdx, loginId)   // 삼항 연산자로 두 메소드 중 하나를 고른다
						        : commentDao.hardDelete(con, cIdx, loginId);

			if (affected != 1) {   // 1건이 처리되지 않았다면
				throw new InvalidInputException("댓글을 삭제하지 못했습니다");   // 권한이 없거나 조건에 안 맞은 것이므로 알린다
			}

			/*
			 물리 삭제를 한 뒤에는 "자식이 없어진 삭제 뼈대" 가 남을 수 있다.

			   삭제된 댓글입니다   <- 뼈대 (자식 때문에 남겨둔 것)
			   └ 답글             <- 이 답글을 지우면 위 뼈대는 남을 이유가 없다

			 그래서 같은 트랜잭션 안에서 한 번 정리한다.
			*/
			if (!soft) {
				//사원(CommentDAO)에게 시키기 : 글번호를 CommentDAO 의 cleanupOrphanDeleted(...) 호출해서 자식 없는 삭제 뼈대 정리(delete) 작업 명령
				commentDao.cleanupOrphanDeleted(con, target.getBIdx());
			}

			return Boolean.valueOf(soft);   // true 면 "삭제된 댓글입니다" 뼈대가 남았다는 뜻이다

		}).booleanValue();   // Boolean 상자에서 진짜 true/false 값을 꺼낸다
	}

	//----------------------------------------------------------------
	// toggleLike : 댓글 추천을 반전시킨다 (안 눌렀으면 추천, 이미 눌렀으면 추천 취소)
	//  사장의 "/comment/commentLike.do" 요청에서 부른다
	//----------------------------------------------------------------
	/**
	 * [처리 순서]
	 *   1. 로그인 여부를 확인한다
	 *   2. 추천할 댓글이 실제로 있는지, 삭제된 것은 아닌지 확인한다
	 *   3. 자기 자신이 쓴 댓글은 아닌지 확인한다 (자기 추천 방지 - 업무 규칙)
	 *   4. 이미 추천했는지 확인해 추천/취소 중 하나를 실행한다
	 *   5. 반영된 뒤의 추천 수를 다시 세어 함께 돌려준다
	 *
	 * [자기 댓글 추천을 막는 것이 기술적 제약이 아니라 서비스 규칙인 이유]
	 *   DB 구조상으로는 자기 댓글을 자기가 추천하는 것이 얼마든지 가능하다.
	 *   막지 않으면 자기 댓글만 계속 추천해 인기 순위를 조작하는 문제가
	 *   생긴다. 이런 "무엇을 허용하고 금지할지"는 기술이 아니라 서비스가
	 *   정하는 정책이므로, DB 나 DAO 가 아니라 이 Service 계층에서 판단한다.
	 *
	 * @return int[2] 배열 { 지금 추천 상태(1=추천중/0=추천안함), 반영된 뒤의 총 추천 수 }
	 * @throws ForbiddenException    로그인하지 않았을 때
	 * @throws InvalidInputException 삭제된 댓글일 때, 자기 자신이 쓴 댓글일 때
	 * @throws NotFoundException     그 번호의 댓글이 없을 때
	 */
	public int[] toggleLike(int cIdx, String loginId) {

		requireLogin(loginId);   // 로그인하지 않았으면 여기서 403 예외가 난다

		return DBCPUtil.execute(con -> {   // 확인·추천·개수 세기를 한 흐름으로 묶는다

			//사원(CommentDAO)에게 시키기 : 댓글번호를 CommentDAO 의 selectOne(...) 호출해서 추천할 댓글 조회(select) 명령
			CommentVo target = commentDao.selectOne(con, cIdx);   // 추천할 댓글을 먼저 찾는다

			if (target == null) {   // 그런 댓글이 없으면
				throw new NotFoundException("댓글을 찾을 수 없습니다");   // 404 로 막는다
			}
			if (target.isDeleted()) {   // 이미 삭제 표시된 댓글이면
				throw new InvalidInputException("삭제된 댓글은 추천할 수 없습니다");   // 빈 뼈대를 추천할 이유가 없다
			}

			//[업무 규칙] 자기 댓글은 추천할 수 없다 (자세한 이유는 위 메소드 설명 참고)
			if (loginId.equals(target.getCId())) {
				throw new InvalidInputException("자신이 쓴 댓글은 추천할 수 없습니다");
			}

			//사원(CommentDAO)에게 시키기 : 댓글번호와 로그인 아이디를 CommentDAO 의 existsLike(...) 호출해서 이미 추천했는지 조회(select) 명령
			boolean already = commentDao.existsLike(con, cIdx, loginId);   // 이 사람이 이미 추천했는지 확인한다

			//사원(CommentDAO)에게 시키기 : 이미 추천했으면 deleteLike(추천 취소), 아니면 insertLike(추천) 호출해서 반전(toggle) 작업 명령
			if (already) {   // 이미 추천한 상태였으면 취소, 아니면 추천 (토글)
				commentDao.deleteLike(con, cIdx, loginId);   //추천 취소
			} else {
				commentDao.insertLike(con, cIdx, loginId);   //추천
			}

			//사원(CommentDAO)에게 시키기 : 댓글번호를 CommentDAO 의 countLike(...) 호출해서 바뀐 뒤의 추천 수 다시 조회(count) 명령
			int count = commentDao.countLike(con, cIdx);   // 바뀐 뒤의 추천 수를 다시 센다. 화면이 이 숫자를 바로 보여 준다

			//[0]=추천중 여부(1/0), [1]=추천 수
			return new int[] { already ? 0 : 1, count };
		});
	}

	//===========================================================
	// 공통 검증
	//===========================================================

	/** 로그인하지 않았으면 403 예외를 던진다. 댓글 관련 대부분의 동작이 이 검사로 시작한다 */
	private void requireLogin(String loginId) {
		if (loginId == null || loginId.trim().isEmpty()) {   // 로그인 아이디가 없거나 공백뿐이면
			throw new ForbiddenException("로그인이 필요합니다");   // 403 으로 막는다. 댓글은 로그인해야 쓸 수 있다
		}
	}

	/**
	 * 댓글 내용을 검증하고, 앞뒤 공백을 없앤 값을 돌려준다.
	 * @throws InvalidInputException 내용이 없거나 공백뿐일 때, MAX_CONTENT_LENGTH 를 넘을 때
	 */
	private String requireContent(String content) {

		if (content == null || content.trim().isEmpty()) {   // 내용이 없거나 공백뿐이면
			throw new InvalidInputException("내용을 입력해주세요");   // 400 으로 막는다
		}

		String clean = content.trim();   // 앞뒤 공백을 없앤 내용

		if (clean.length() > MAX_CONTENT_LENGTH) {   // 정해 둔 최대 길이를 넘으면
			throw new InvalidInputException("내용은 " + MAX_CONTENT_LENGTH + "자까지 쓸 수 있습니다");   // DB 컬럼 길이를 넘어 SQL 오류가 나기 전에 여기서 막는다
		}
		return clean;   // 검사를 통과한 깨끗한 내용을 돌려준다
	}
}
