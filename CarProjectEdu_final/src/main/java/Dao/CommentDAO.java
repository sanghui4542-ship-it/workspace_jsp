package Dao;

/*
 * ============================================================================
 *  CommentDAO  -  사원(DAO) : 자유게시판 댓글(board_comment) 테이블에 SQL 을 실행하는 클래스
 *
 *  이 DAO 가 다루는 테이블 2개
 *    board_comment       댓글 1건 = 행 1개 (부모 댓글번호를 스스로 참조하는 자기참조 구조)
 *    board_comment_like  누가 어느 댓글을 추천했는지 (댓글번호 + 회원아이디 조합)
 *
 *  [이 프로젝트 DAO 공통 규칙]
 *    1. Connection 을 인스턴스 필드로 갖지 않고 메소드 매개변수로 받는다
 *       (필드로 두면 동시에 두 사람이 요청할 때 서로의 연결을 덮어써
 *        데이터가 뒤섞이거나 "ResultSet closed" 오류가 날 수 있다)
 *    2. PreparedStatement / ResultSet 은 try-with-resources 지역변수로 만든다
 *       (괄호 안에서 만들면 블록이 끝날 때 자동으로 닫힌다 — 닫는 코드를 잊을 수 없다)
 *    3. SQL 에 값을 문자열로 이어 붙이지 않고 반드시 ? 로 바인딩한다 (SQL 인젝션 차단)
 *    4. 예외를 삼키지 않고 SQLException 을 그대로 위로 던진다
 *       (여기서 조용히 처리해 버리면 "실패"가 "댓글 없음" 처럼 보이게 된다.
 *        트랜잭션을 되돌릴지 말지도 이 DAO 가 아니라 CommentService 가 결정한다)
 * ============================================================================
 */

// DB 연결, SQL 실행 도구, 조회 결과, DB 예외
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// insert 후 자동 생성된 번호(auto_increment)를 돌려받을 때 쓰는 도구
import java.sql.Statement;

// 여러 건의 댓글을 순서대로 담을 목록 도구
import java.util.ArrayList;
import java.util.List;

// 값을 담아 나르는 상자
import Vo.CommentVo;

public class CommentDAO {

	//===========================================================
	// 1. 글 하나의 댓글 전체 조회 (평면 목록 — 아직 부모-자식으로 묶이지 않은 상태)
	//===========================================================
	/**
	 * 원글 번호로 그 글에 달린 댓글을 전부 가져온다. 트리로 묶는 일은 CommentService 가 한다.
	 *
	 * [부르는 곳] CommentService 의 getComments()
	 * [매개변수] loginId 지금 로그인한 아이디. 비로그인이면 null 을 넘긴다
	 * [반환값]   부모 번호(parentIdx)만 갖고 있는 평면 목록. 댓글이 없어도 빈 목록
	 *
	 * [추천 수와 "내가 추천했는지" 를 SQL 한 번으로 함께 가져오는 방법]
	 *   댓글 목록을 가져온 뒤, 댓글마다 "추천 수는 몇 개인지" 를 따로 조회하면
	 *   댓글이 20개일 때 추가로 SQL을 20번 더 보내야 한다(N+1 문제라고 부른다).
	 *   이 메소드는 select 문 안에 작은 서브쿼리 두 개를 넣어(아래 SQL의 괄호 부분)
	 *     - board_comment_like 에서 그 댓글번호의 행 개수를 세어 추천 수로,
	 *     - 그중 내 아이디로 남긴 행이 있는지를 세어 "내가 추천했는지" 로
	 *   댓글 목록 SQL 단 한 번으로 모두 함께 가져온다.
	 */
	public List<CommentVo> selectByBoard(Connection con, int bIdx, String loginId) throws SQLException {

		String sql =   // 댓글 목록 SQL. 괄호 안의 작은 select 두 개가 추천 수와 내 추천 여부를 함께 세어 온다
			"select c.c_idx, c.b_idx, c.parent_idx, c.c_id, c.c_name, c.c_content,"
		  + "       date_format(c.c_date,   '%Y-%m-%d %H:%i') as c_date,"
		  + "       date_format(c.c_update, '%Y-%m-%d %H:%i') as c_update,"
		  + "       c.del_flag,"
		  + "       (select count(*) from board_comment_like l"
		  + "         where l.c_idx = c.c_idx) as like_count,"
		  + "       (select count(*) from board_comment_like l2"
		  + "         where l2.c_idx = c.c_idx and l2.member_id = ?) as liked_by_me"
		  + "  from board_comment c"
		  + " where c.b_idx = ?"
		  /* 부모 번호가 작은 순 -> 같은 부모 안에서는 작성 순서(c_idx).
		     Service 가 트리로 묶을 때 부모가 먼저 나오면 조립이 쉬워진다. */
		  + " order by c.c_idx asc";

		List<CommentVo> list = new ArrayList<CommentVo>();   // 결과를 담을 빈 목록. 댓글이 없어도 null 이 아닌 빈 목록을 돌려준다

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다

			//비로그인이면 "" 를 넘긴다 (어떤 회원 아이디와도 같지 않으므로 항상 0이 된다)
			pstmt.setString(1, (loginId == null) ? "" : loginId);
			pstmt.setInt(2, bIdx);   // 2번 ? : 어느 글의 댓글인지 (where c.b_idx = ?)

			try (ResultSet rs = pstmt.executeQuery()) {   // 값을 다 끼운 뒤 실행한다
				while (rs.next()) {   // 댓글이 여러 건이므로 while 로 전부 읽는다
					list.add(mapRow(rs));   // 한 줄을 CommentVo 로 바꿔 목록에 담는다
				}
			}
		}
		return list;   // 이 글의 댓글 목록(평면)을 돌려준다. 트리로 묶는 일은 Service 가 한다
	}

	/**
	 * ResultSet 한 행을 CommentVo 로 옮긴다.
	 * [왜 따로 뽑았나] selectByBoard() 와 selectOne() 두 조회 메소드가 이 변환 코드를
	 *   똑같이 반복해서 갖고 있었다. 한 곳으로 모으면 컬럼이 바뀔 때 고칠 곳도 한 곳이다.
	 */
	private CommentVo mapRow(ResultSet rs) throws SQLException {

		CommentVo vo = new CommentVo();   // 값을 담을 빈 상자를 만든다
		vo.setCIdx(rs.getInt("c_idx"));   // 댓글번호
		vo.setBIdx(rs.getInt("b_idx"));   // 원글 번호

		/*
		 parent_idx 는 NULL 일 수 있다(최상위 댓글).
		 rs.getInt() 는 NULL 을 0 으로 돌려주므로 그대로 쓰면
		 "0번 댓글의 자식" 이라는 잘못된 뜻이 된다.
		 그래서 getObject 로 받아 null 인지 먼저 확인한다.
		*/
		Object parent = rs.getObject("parent_idx");
		vo.setParentIdx(parent == null ? null : Integer.valueOf(((Number) parent).intValue()));   // null 이면 그대로 null, 아니면 숫자로 바꿔 담는다 (위 설명 참고)

		vo.setCId(rs.getString("c_id"));   // 작성자 아이디
		vo.setCName(rs.getString("c_name"));   // 작성자 이름
		vo.setCContent(rs.getString("c_content"));   // 댓글 내용
		vo.setCDate(rs.getString("c_date"));   // 작성일시 (SQL 에서 이미 보기 좋은 형태로 만들어 왔다)
		vo.setCUpdate(rs.getString("c_update"));   // 수정일시 (수정한 적 없으면 null)
		vo.setDelFlag(rs.getString("del_flag"));   // 삭제 표시 Y/N
		vo.setLikeCount(rs.getInt("like_count"));   // 추천 수 (SQL 의 like_count)
		vo.setLikedByMe(rs.getInt("liked_by_me") > 0);   // 0보다 크면 내가 이미 추천한 것이다 → true/false 로 바꿔 담는다
		return vo;   // 채워진 상자를 돌려준다
	}

	//===========================================================
	// 2. 댓글 한 건 조회 (수정/삭제 권한 확인용)
	//===========================================================
	/**
	 * [부르는 곳] CommentService 의 addComment(), editComment(), deleteComment(), toggleLike()
	 *            — 모두 "먼저 그 댓글이 존재하는지, 작성자가 누구인지" 확인할 때 쓴다
	 * [반환값]   조회된 CommentVo. 그 번호의 댓글이 없으면 null
	 *
	 * 목록 조회(selectByBoard)와 달리 추천 수는 이 화면에서 필요 없으므로
	 * SQL 에서 0 으로 고정해 둔다 (굳이 서브쿼리로 세지 않아 더 가볍다).
	 */
	public CommentVo selectOne(Connection con, int cIdx) throws SQLException {

		String sql =   // 댓글 한 건 조회 SQL. 목록과 달리 추천 수는 필요 없어 0 으로 고정해 둔다
			"select c_idx, b_idx, parent_idx, c_id, c_name, c_content,"
		  + "       date_format(c_date,   '%Y-%m-%d %H:%i') as c_date,"
		  + "       date_format(c_update, '%Y-%m-%d %H:%i') as c_update,"
		  + "       del_flag, 0 as like_count, 0 as liked_by_me"
		  + "  from board_comment where c_idx = ?";

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다
			pstmt.setInt(1, cIdx);   // 1번 ? : 찾을 댓글번호
			try (ResultSet rs = pstmt.executeQuery()) {   // 실행하고 결과표를 받는다
				//rs.next() 결과를 반드시 확인한다. 없는 번호면 null 을 돌려준다.
				return rs.next() ? mapRow(rs) : null;
			}
		}
	}

	//===========================================================
	// 3. 댓글 등록
	//===========================================================
	/**
	 * [부르는 곳] CommentService 의 addComment() — 답글이면 부모 댓글 확인까지 마친 뒤 호출된다
	 * [매개변수] vo 에는 bIdx, parentIdx, cId, cName, cContent 가 채워져 있어야 한다
	 * [반환값]   새로 만들어진 댓글번호 (화면이 그 댓글로 스크롤하는 데 사용). 못 받으면 0
	 */
	public int insert(Connection con, CommentVo vo) throws SQLException {

		String sql = "insert into board_comment (b_idx, parent_idx, c_id, c_name, c_content)"   // 댓글 저장 SQL. c_date 는 DB 가 자동으로 넣어 준다
				   + " values (?, ?, ?, ?, ?)";

		//RETURN_GENERATED_KEYS : auto_increment 로 만들어진 번호를 돌려받겠다는 표시
		try (PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			pstmt.setInt(1, vo.getBIdx());

			//최상위 댓글이면 parent_idx 에 NULL 을 넣는다 (0 이 아니다)
			if (vo.getParentIdx() == null) {
				pstmt.setNull(2, java.sql.Types.INTEGER);
			} else {
				pstmt.setInt(2, vo.getParentIdx().intValue());   // 답글이면 부모 댓글번호를 넣는다
			}

			pstmt.setString(3, vo.getCId());   // 3번 ? : 작성자 아이디
			pstmt.setString(4, vo.getCName());   // 4번 ? : 작성자 이름
			pstmt.setString(5, vo.getCContent());   // 5번 ? : 댓글 내용

			pstmt.executeUpdate();   // 실행해서 실제로 저장한다

			try (ResultSet keys = pstmt.getGeneratedKeys()) {   // 방금 만들어진 번호를 받아 오는 통로를 연다
				return keys.next() ? keys.getInt(1) : 0;   // 번호가 있으면 그 값을, 없으면 0 을 돌려준다
			}
		}
	}

	//===========================================================
	// 4. 댓글 수정
	//===========================================================
	/**
	 * 내용만 바꾸고 c_update 에 지금 시각을 남긴다 (화면의 "(수정됨)" 표시 근거가 된다).
	 *
	 * [부르는 곳] CommentService 의 editComment() — 이미 본인 확인을 마친 뒤 호출된다
	 * [반환값]   수정된 행 수 (성공 1 / 권한 없음 또는 없는 댓글이면 0)
	 *
	 * [where 절에 c_id = ? 를 함께 둔 이유 - 방어적 이중 확인]
	 *   CommentService 가 이미 작성자 본인인지 확인하지만, 이 DAO 의 SQL
	 *   조건에도 c_id = ? 를 한 번 더 넣어 둔다. 나중에 누군가 Service 의
	 *   확인 코드를 실수로 지우거나 다른 경로로 이 메소드를 직접 부르더라도,
	 *   DB 단계에서 한 번 더 막히므로 남의 댓글이 바뀌는 일이 없다.
	 */
	public int update(Connection con, int cIdx, String loginId, String content) throws SQLException {

		String sql = "update board_comment"   // 댓글 수정 SQL. now() 로 수정 시각을 함께 기록한다
				   + "    set c_content = ?, c_update = now()"
				   + "  where c_idx = ? and c_id = ? and del_flag = 'N'";

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다
			pstmt.setString(1, content);   // 1번 ? : 새 내용
			pstmt.setInt(2, cIdx);   // 2번 ? : 어느 댓글인지
			pstmt.setString(3, loginId);   // 3번 ? : 작성자 본인인지 (남의 댓글이면 조건에 안 맞아 0건 수정된다)
			return pstmt.executeUpdate();   // 바뀐 행 수를 돌려준다. 0 이면 권한이 없거나 없는 댓글이다
		}
	}

	//===========================================================
	// 5. 댓글 삭제 (두 가지 방식)
	//===========================================================

	/**
	 * 이 댓글에 달린 답글(자식)이 있는지 확인한다.
	 * [부르는 곳] CommentService 의 deleteComment() — 소프트 삭제할지 물리 삭제할지 결정하는 기준
	 */
	public boolean hasChildren(Connection con, int cIdx) throws SQLException {

		String sql = "select count(*) as cnt from board_comment where parent_idx = ?";   // 이 댓글을 부모로 삼는 답글이 몇 개인지 센다

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다
			pstmt.setInt(1, cIdx);   // 1번 ? : 확인할 댓글번호
			try (ResultSet rs = pstmt.executeQuery()) {   // 실행하고 결과표를 받는다
				return rs.next() && rs.getInt("cnt") > 0;   // 읽을 줄이 있고(&&) 개수가 0보다 크면 자식이 있는 것이다
			}
		}
	}

	/**
	 * 소프트 삭제 : 답글(자식)이 있는 댓글에 사용한다.
	 * [부르는 곳] CommentService 의 deleteComment() — hasChildren() 이 true 일 때
	 * [반환값]   수정된 행 수 (성공 1)
	 *
	 * 이 댓글을 실제로 지워 버리면, 이 댓글에 달려 있던 답글들이 부모를 잃어
	 * 화면에 붙일 자리가 사라진다. 그래서 실제로 지우지 않고 내용만 비우고
	 * del_flag 를 'Y' 로 바꿔 "삭제된 댓글입니다" 라는 뼈대만 남긴다.
	 */
	public int softDelete(Connection con, int cIdx, String loginId) throws SQLException {

		String sql = "update board_comment"   // 지우지 않고 표시만 바꾸는 소프트 삭제 SQL
				   + "    set del_flag = 'Y', c_content = '', c_update = now()"
				   + "  where c_idx = ? and c_id = ?";

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다
			pstmt.setInt(1, cIdx);   // 1번 ? : 어느 댓글인지
			pstmt.setString(2, loginId);   // 2번 ? : 작성자 본인 확인
			return pstmt.executeUpdate();   // 바뀐 행 수를 돌려준다
		}
	}

	/**
	 * 물리 삭제 : 답글(자식)이 없는 댓글에 사용한다.
	 * [부르는 곳] CommentService 의 deleteComment() — hasChildren() 이 false 일 때
	 * [반환값]   삭제된 행 수 (성공 1)
	 *
	 * board_comment_like 테이블은 외래키가 on delete cascade 로 설정되어 있어,
	 * 이 댓글이 지워지면 이 댓글에 달려 있던 추천 기록도 DB 가 자동으로 함께 지운다.
	 */
	public int hardDelete(Connection con, int cIdx, String loginId) throws SQLException {

		String sql = "delete from board_comment where c_idx = ? and c_id = ?";   // 실제로 지우는 물리 삭제 SQL

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다
			pstmt.setInt(1, cIdx);   // 1번 ? : 어느 댓글인지
			pstmt.setString(2, loginId);   // 2번 ? : 작성자 본인 확인
			return pstmt.executeUpdate();   // 지워진 행 수를 돌려준다
		}
	}

	/**
	 * 삭제 표시(del_flag='Y')된 댓글 중, 자식이 하나도 남지 않은 것을 정리(완전 삭제)한다.
	 *
	 * [부르는 곳] CommentService 의 deleteComment() — 물리 삭제를 한 직후에만 호출된다
	 * [반환값]   정리한(완전히 지운) 행 수
	 *
	 * [왜 필요한가] "삭제된 댓글입니다" 뼈대는 오직 그 자식(답글)들을 보여주기
	 *   위해 남겨 둔 것이다. 그 자식들이 나중에 모두 지워지면, 더 이상 아무도
	 *   보여줄 것이 없는 뼈대만 화면에 덩그러니 남는다. 댓글을 물리 삭제할
	 *   때마다 이 정리를 한 번 실행해서 그런 빈 뼈대를 치운다.
	 *
	 * [MySQL 의 제약을 우회하는 방법]
	 *   MySQL 은 "지금 지우려는 바로 그 테이블을 서브쿼리에서 다시 읽는 것"을
	 *   허용하지 않는다(자기 자신을 수정하며 동시에 읽는 모순을 막기 위해서다).
	 *   그래서 서브쿼리를 한 번 더 감싸서(파생 테이블, as t) 별개의 결과처럼
	 *   보이게 만들어 이 제약을 피해 간다.
	 */
	public int cleanupOrphanDeleted(Connection con, int bIdx) throws SQLException {

		String sql = "delete from board_comment"
				   + " where b_idx = ? and del_flag = 'Y'"
				   + "   and c_idx not in ("
				   + "        select parent_idx from ("
				   + "            select parent_idx from board_comment"
				   + "             where b_idx = ? and parent_idx is not null"
				   + "        ) as t"
				   + "   )";

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다
			pstmt.setInt(1, bIdx);   // 1번 ? : 어느 글의 댓글을 정리할지
			pstmt.setInt(2, bIdx);   // 2번 ? : 서브쿼리에도 같은 글번호가 필요하다
			return pstmt.executeUpdate();   // 정리한 행 수를 돌려준다
		}
	}

	//===========================================================
	// 6. 추천 (토글 - 누르면 추천, 다시 누르면 취소)
	//===========================================================

	/**
	 * 이 사람이 이 댓글을 이미 추천했는지 확인한다.
	 * [부르는 곳] CommentService 의 toggleLike() — 추천할지 취소할지 판단하는 기준
	 */
	public boolean existsLike(Connection con, int cIdx, String memberId) throws SQLException {

		String sql = "select count(*) as cnt from board_comment_like where c_idx = ? and member_id = ?";   // 그 사람이 그 댓글에 남긴 추천 기록이 있는지 센다

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다
			pstmt.setInt(1, cIdx);   // 1번 ? : 댓글번호
			pstmt.setString(2, memberId);   // 2번 ? : 추천한 사람 아이디
			try (ResultSet rs = pstmt.executeQuery()) {   // 실행하고 결과표를 받는다
				return rs.next() && rs.getInt("cnt") > 0;   // 기록이 1건이라도 있으면 이미 추천한 것이다
			}
		}
	}

	/**
	 * 추천 기록을 한 줄 추가한다 (추천 누르기).
	 * [부르는 곳] CommentService 의 toggleLike() — existsLike() 가 false 일 때
	 */
	public int insertLike(Connection con, int cIdx, String memberId) throws SQLException {

		String sql = "insert into board_comment_like (c_idx, member_id) values (?, ?)";   // 추천 기록 저장 SQL

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다
			pstmt.setInt(1, cIdx);   // 1번 ? : 댓글번호
			pstmt.setString(2, memberId);   // 2번 ? : 추천한 사람 아이디
			return pstmt.executeUpdate();   // 저장된 행 수를 돌려준다
		}
	}

	/**
	 * 그 사람의 추천 기록을 지운다 (추천 취소).
	 * [부르는 곳] CommentService 의 toggleLike() — existsLike() 가 true 일 때
	 */
	public int deleteLike(Connection con, int cIdx, String memberId) throws SQLException {

		String sql = "delete from board_comment_like where c_idx = ? and member_id = ?";   // 추천 기록 삭제 SQL

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다
			pstmt.setInt(1, cIdx);   // 1번 ? : 댓글번호
			pstmt.setString(2, memberId);   // 2번 ? : 추천을 취소하는 사람 아이디
			return pstmt.executeUpdate();   // 지워진 행 수를 돌려준다
		}
	}

	/**
	 * 이 댓글의 추천 수를 다시 센다 (추천/취소 직후 화면에 새 숫자를 곧바로 돌려주기 위해).
	 * [부르는 곳] CommentService 의 toggleLike() — insertLike()/deleteLike() 바로 다음
	 */
	public int countLike(Connection con, int cIdx) throws SQLException {

		String sql = "select count(*) as cnt from board_comment_like where c_idx = ?";   // 이 댓글에 달린 추천이 모두 몇 개인지 센다

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다
			pstmt.setInt(1, cIdx);   // 1번 ? : 댓글번호
			try (ResultSet rs = pstmt.executeQuery()) {   // 실행하고 결과표를 받는다
				return rs.next() ? rs.getInt("cnt") : 0;   // 읽을 줄이 있으면 그 개수를, 없으면 0 을 돌려준다
			}
		}
	}

	/* [정리] 이 자리에 있던 countByBoard() 를 지웠다.
	   게시글 목록에 댓글 개수를 표시하려고 만들어 둔 메소드인데,
	   이 메소드를 부르던 CommentService.countComments() 자체가
	   어디서도 호출되지 않아 함께 정리했다. */
}
