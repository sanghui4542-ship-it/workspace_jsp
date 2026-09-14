package Dao;

/*
 * ============================================================================
 *  BoardDAO  -  사원(DAO) : 자유게시판(board) 테이블에 SQL 을 실행하는 클래스
 *
 *  이 클래스에 인스턴스 변수가 하나도 없다 = 무상태(stateless)
 *    Connection, PreparedStatement, ResultSet 을 전부 지역변수로 쓴다.
 *    그래서 여러 사람이 동시에 요청해도 서로 값을 덮어쓰지 않는다.
 *
 *    [과거의 위험했던 방식 - 왜 지역변수를 쓰는가]
 *      con, pstmt, rs 를 이 클래스의 필드(인스턴스 변수)로 두면,
 *      서블릿(BoardController)은 객체가 딱 1개만 만들어지고 그 안의 DAO 도 1개뿐이라
 *      동시에 들어온 요청(스레드) 여러 개가 같은 변수 칸을 함께 쓰게 된다.
 *
 *          A 요청 : con = 연결1  저장
 *          B 요청 : con = 연결2  저장   <- A 가 저장해 둔 값을 덮어쓴다
 *          A 요청 : con.여기서작업()   <- 이미 B 의 연결로 바뀌어 있다
 *
 *      혼자 테스트할 때는 절대 드러나지 않고, 여러 명이 동시에 클릭할 때만
 *      가끔 나타나서 원인을 찾기 매우 어렵다. 지역변수로 두면 요청(스레드)마다
 *      자기만의 변수를 새로 만들므로 애초에 공유될 일이 없다.
 *
 *  이 DAO 는 SQL 을 "한 개당 메소드 하나"로만 쪼갠다.
 *    여러 SQL 을 묶어서 실행할지(트랜잭션) 말지는 부장(BoardService)이 결정한다.
 *    예) 새 글 등록 = shiftAllGroups() + insertBoard() 두 메소드를
 *        BoardService 가 DBCPUtil.execute() 로 하나의 작업으로 묶는다.
 *
 *  이 DAO 는 예외(SQLException)를 삼키지 않는다.
 *    DB 작업이 실패하면 그 예외를 그대로 위(Service)로 던진다.
 *    여기서 catch 해서 조용히 빈 목록을 돌려주면, DB 가 완전히 멈춰도
 *    화면에는 "게시글이 없습니다" 라고만 떠서 진짜 문제를 알아챌 수 없다.
 * ============================================================================
 */

// DB 연결, SQL 실행 도구, 조회 결과, DB 예외
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// 여러 건의 글을 순서대로 담을 목록 도구
import java.util.ArrayList;
import java.util.List;

// 값을 담아 나르는 상자
import Vo.BoardVo;

public class BoardDAO {

	/* 인스턴스 변수가 없다 = 무상태(stateless) = 여러 스레드가 동시에 써도 안전하다 (자세한 이유는 위 클래스 설명 참고) */

	/* [정리] 이 자리에 있던 조회 메소드 2개를 지웠다.
	       boardList(con)                  전체 글목록 (검색·페이징 없음)
	       boardList(con, key, word)       검색 결과 전체 (페이징 없음)
	   두 메소드는 BoardService 의 serviceBoardList() / serviceBoardKeyWord() 만
	   호출했는데, 그 두 Service 메소드를 부르는 곳이 없어 함께 정리했다.
	   지금 목록·검색은 모두 아래 countBoards() + boardList(...offset, limit) 조합
	   (SQL 페이징)으로 처리한다. */

	//===========================================================
	// 2-1. 검색 조건에 맞는 전체 글 개수  (페이징용)
	//===========================================================
	/**
	 * 조건에 맞는 글이 총 몇 건인지 센다. 페이지 번호를 계산하는 데 쓴다.
	 *
	 * [부르는 곳] BoardService 의 serviceBoardPage()
	 * [반환값]   조건에 맞는 글의 개수
	 *
	 * [왜 개수를 목록과 따로 조회하는가]
	 *   페이지 번호를 만들려면 "전체가 몇 건인지"를 알아야 한다.
	 *       전체 페이지 수 = 올림(전체 건수 / 한 페이지 건수)
	 *   전체 목록을 다 읽어서 list.size() 로 개수를 구하면, 글이 10,000건일 때
	 *   객체 10,000개를 만들어 놓고 개수 하나만 쓰고 나머지는 버리는 셈이 된다.
	 *   count(*) 는 DB 안에서 개수만 세어 숫자 하나만 돌려주므로 훨씬 가볍다.
	 */
	public int countBoards(Connection con, String key, String word) throws SQLException {

		boolean hasWord = (word != null && !word.trim().isEmpty());   // 검색어가 실제로 들어왔는지 확인 (공백만 있으면 없는 것으로 본다)
		boolean searchTitleContent = "titleContent".equals(key);   // 검색 종류가 "제목+내용" 인가

		String sql = "select count(*) as cnt from board" + buildWhere(hasWord, searchTitleContent);   // where 절은 아래 buildWhere 가 만들어 준다. 목록 조회와 똑같은 조건을 쓰기 위해서다

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다

			bindWord(pstmt, hasWord, searchTitleContent, word);   // 검색어 ? 를 채운다 (검색어가 없으면 아무것도 안 채운다)

			try (ResultSet rs = pstmt.executeQuery()) {   // 실행하고 결과표를 받는다
				if (rs.next()) {   // count 결과는 항상 한 줄이다
					return rs.getInt("cnt");   // 그 개수를 돌려준다. 페이징이 이 숫자로 전체 페이지 수를 계산한다
				}
			}
		}

		return 0;   // 여기까지 오는 일은 없지만, 못 읽었으면 0건으로 본다
	}

	//===========================================================
	// 2-2. 검색 조건 + 페이지 범위에 해당하는 글목록만 조회  (페이징용)
	//===========================================================
	/**
	 * 조건에 맞는 글 중 이 페이지에 보여줄 만큼(offset~offset+limit)만 가져온다.
	 *
	 * [부르는 곳] BoardService 의 serviceBoardPage()
	 * [매개변수]
	 *   offset  건너뛸 글 개수  (1페이지=0, 2페이지=5, 3페이지=10 ...)
	 *   limit   가져올 글 개수  (한 페이지당 글 수, 보통 5)
	 * [반환값]   이 페이지 몫의 글 목록
	 *
	 * [limit 문법]
	 *     limit 건너뛸개수, 가져올개수
	 *
	 *   1페이지(5건) -> limit 0, 5    (0건 건너뛰고 5건)
	 *   2페이지(5건) -> limit 5, 5    (5건 건너뛰고 5건)
	 *   3페이지(5건) -> limit 10, 5
	 *
	 * [주의] limit 에 들어갈 값(offset, limit)도 ? 로 바인딩한다.
	 *   숫자라서 문자열로 그냥 이어 붙여도 동작은 하지만, 그 값이 사용자 입력
	 *   (주소창의 페이지 번호)에서 오는 순간 SQL 인젝션 통로가 될 수 있다.
	 *   "사용자 입력은 숫자든 문자든 무조건 ? 로 바인딩한다" 가 안전한 원칙이다.
	 */
	public List<BoardVo> boardList(Connection con, String key, String word, int offset, int limit)
			throws SQLException {

		List<BoardVo> list = new ArrayList<BoardVo>();   // 결과를 담을 빈 목록

		boolean hasWord = (word != null && !word.trim().isEmpty());   // 검색어가 실제로 들어왔는지 확인
		boolean searchTitleContent = "titleContent".equals(key);   // 검색 종류가 "제목+내용" 인가

		String sql = "select * from board"   // 개수 조회와 같은 where 를 쓰고, 뒤에 limit 를 붙여 이 페이지 몫만 가져온다
				   + buildWhere(hasWord, searchTitleContent)
				   + " order by b_group asc limit ?, ?";

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다

			//검색어 ? 를 먼저 채우고, 그 다음 번호부터 limit 값을 채운다
			int idx = bindWord(pstmt, hasWord, searchTitleContent, word);

			pstmt.setInt(idx++, offset); //건너뛸 개수
			pstmt.setInt(idx++, limit);  //가져올 개수

			try (ResultSet rs = pstmt.executeQuery()) {   // 값을 다 끼운 뒤 실행한다
				while (rs.next()) {   // 이 페이지 몫(예: 5건)만 읽힌다
					list.add(mapRow(rs));   // 한 줄을 BoardVo 로 바꿔 담는다
				}
			}
		}

		return list;   // 이 페이지에 보여줄 글 목록을 돌려준다
	}

	/**
	 * 검색 조건 WHERE 절을 글자로 만들어 반환한다 (SQL 실행은 하지 않는다).
	 *
	 * [왜 따로 뽑았나] 글 개수를 세는 SQL(countBoards)과 글 목록을 가져오는
	 *   SQL(boardList)이 정확히 같은 조건을 써야 한다. 조건이 아주 조금이라도
	 *   다르면 "전체 10페이지인데 목록은 3페이지에서 끊기는" 것처럼 개수와
	 *   실제 목록이 어긋난다. 조건을 만드는 코드를 한 곳에 모아 두면 그럴 일이 없다.
	 *
	 * @return 검색어가 없으면 빈 문자열, 있으면 " where ..." 로 시작하는 조건절
	 */
	private String buildWhere(boolean hasWord, boolean searchTitleContent) {

		if (!hasWord) {   // 검색어가 없으면
			return "";   // where 절 자체가 없다 (빈 문자열을 이어 붙이면 전체 조회가 된다)
		}
		return searchTitleContent   // 검색 종류에 따라 두 가지 where 중 하나를 돌려준다
				? " where b_title like ? or b_content like ?"
				: " where b_name like ?";
	}

	/**
	 * buildWhere() 가 만든 조건절의 ? 자리에 검색어를 채운다.
	 * countBoards() 와 boardList(페이징용)가 이 메소드를 함께 써서, "조건을 만드는 코드"와
	 * "그 조건에 값을 채우는 코드"를 각각 한 곳으로 모았다 (buildWhere() 와 같은 이유).
	 *
	 * @return 이 메소드가 다음으로 채워야 할 ? 번호 (예: ?를 1개 썼으면 2 를 반환)
	 *         — boardList(페이징용) 가 이 값을 이어받아 offset/limit 의 ? 번호로 쓴다
	 */
	private int bindWord(PreparedStatement pstmt, boolean hasWord, boolean searchTitleContent, String word)
			throws SQLException {

		if (!hasWord) {   // 검색어가 없으면 채울 ? 도 없다
			return 1;   // 다음에 쓸 번호는 그대로 1번이다
		}

		String likeWord = "%" + word.trim() + "%";   // like 검색용으로 앞뒤에 % 를 붙인다

		pstmt.setString(1, likeWord);   // 1번 ? 에 검색어를 채운다
		if (searchTitleContent) {   // 제목+내용 검색이면 ? 가 하나 더 있다
			pstmt.setString(2, likeWord);   // 2번 ? 에도 같은 검색어를 채운다
			return 3;   // 두 개를 썼으니 다음 번호는 3번이다
		}
		return 2;   // 하나만 썼으니 다음 번호는 2번이다
	}

	//===========================================================
	// 3. 조회수 1 증가
	//===========================================================
	/**
	 * 글 하나의 조회수를 1 늘린다.
	 *
	 * [부르는 곳] BoardService 의 serviceBoardRead() (글 조회와 함께 트랜잭션으로 묶여 실행된다)
	 * [반환값]   수정된 행 수 (그 글이 있으면 1, 없으면 0)
	 *
	 * [왜 조회(select) 와 증가(update)를 이 클래스 안에서 한 메소드로 합치지 않는가]
	 *   SQL 하나 = 메소드 하나로 나눠 두면, 부장(BoardService)이 필요한 조합을
	 *   자유롭게 만들 수 있다. 조회수만 올리고 싶을 때, 글만 읽고 싶을 때를
	 *   각각 재사용할 수 있고, "두 SQL 을 묶어서 실행할지"도 Service 가 정한다.
	 *
	 * [b_cnt = b_cnt + 1 로 쓰는 이유 - 동시성 문제]
	 *   자바 코드에서 값을 읽어 온 뒤(read) +1 을 계산해서(modify) 다시 저장하면(write),
	 *   두 사람이 정확히 같은 순간에 글을 읽으면 아래처럼 조회수가 한 번만 올라간다.
	 *
	 *       사람A : 값 읽음(10) -> 11 계산 -> 저장(11)
	 *       사람B : 값 읽음(10) -> 11 계산 -> 저장(11)     <- A 의 조회는 사라진다
	 *
	 *   "지금 저장된 값에 1을 더해라" 라고 DB 에게 그대로 맡기면(b_cnt = b_cnt + 1),
	 *   DB 가 각 요청을 순서대로 처리해 정확하게 계산해 준다.
	 */
	public int increaseReadCount(Connection con, int b_idx) throws SQLException {

		String sql = "update board set b_cnt = b_cnt + 1 where b_idx = ?";   // 조회수를 지금 값에서 1 늘린다. 자바로 읽어와 더하지 않는 이유는 동시에 읽으면 값이 어긋나기 때문이다

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다
			pstmt.setInt(1, b_idx);   // 1번 ? : 어느 글인지
			return pstmt.executeUpdate();   // 바뀐 행 수를 돌려준다
		}
	}

	//===========================================================
	// 4. 글 1건 조회
	//===========================================================
	/**
	 * 글번호로 글 1건을 조회한다.
	 *
	 * [부르는 곳] BoardService 의 serviceBoardRead()
	 * [반환값]   조회된 BoardVo. 그 번호의 글이 없으면 null
	 *            (부장이 null 을 받으면 NotFoundException 을 던져 404 로 응답한다)
	 */
	public BoardVo selectOne(Connection con, int b_idx) throws SQLException {

		String sql = "select * from board where b_idx = ?";   // 글번호 하나로 글 한 건을 찾는다

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다

			pstmt.setInt(1, b_idx);   // 1번 ? : 찾을 글번호

			try (ResultSet rs = pstmt.executeQuery()) {   // 실행하고 결과표를 받는다
				if (rs.next()) {   // 글번호는 기본키라 결과는 0건 아니면 1건이다
					return mapRow(rs);   // 찾았으면 BoardVo 로 바꿔 돌려준다
				}
			}
		}

		return null;   // 그 번호의 글이 없다는 뜻. Service 가 404 로 바꿔 던진다
	}

	//===========================================================
	// 5. 글번호로 저장된 글 비밀번호 조회
	//===========================================================
	/**
	 * 글번호로 DB 에 저장된 글 비밀번호를 조회한다.
	 *
	 * [부르는 곳] BoardService 의 servicePassCheck(), serviceUpdateBoard(), serviceDeleteBoard()
	 * [반환값]   저장된 비밀번호 글자. 그 번호의 글이 없으면 null
	 *
	 * [비밀번호 "비교"는 왜 여기서 하지 않는가]
	 *   이 메소드는 저장된 값을 그대로 꺼내 오기만 한다. 실제로 입력값과 같은지
	 *   비교하는 것은 BoardService 가 java.util.Objects.equals() 로 한다.
	 *   (참고: 비밀번호를 해시로 저장했다면 매번 다른 값이 나오므로 SQL 의
	 *    "b_pw = 입력값" 조건으로는 비교할 수 없다 — 그래서 이 프로젝트는
	 *    저장된 값을 그대로 꺼내와 자바에서 문자열을 직접 비교하는 방식을 쓴다)
	 */
	public String findPasswordByIdx(Connection con, int b_idx) throws SQLException {

		String sql = "select b_pw from board where b_idx=?";   // 저장된 글 비밀번호 한 칸만 꺼내 온다 (비교는 Service 가 한다)

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다

			pstmt.setInt(1, b_idx);   // 1번 ? : 어느 글인지

			try (ResultSet rs = pstmt.executeQuery()) {   // 실행하고 결과표를 받는다
				if (rs.next()) {   // 그 글이 있으면
					return rs.getString("b_pw");   // 저장된 비밀번호(해시)를 그대로 돌려준다
				}
			}
		}

		return null;   // 그 번호의 글이 없다는 뜻
	}

	//===========================================================
	// 6. 글 수정
	//===========================================================
	/**
	 * 글의 이메일·제목·내용 세 칸을 수정한다.
	 *
	 * [부르는 곳] BoardService 의 serviceUpdateBoard() — 비밀번호 검증을 통과한 뒤에만 호출된다
	 * [반환값]   수정된 행 수 (성공 1 / 그 글이 없으면 0)
	 */
	public int updateBoard(Connection con, int b_idx, String email, String title, String content)
			throws SQLException {

		String sql = "update board set b_email=?, b_title=?, b_content=? where b_idx=?";   // 글 수정 SQL. 제목·내용·이메일 세 칸만 바꾼다

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다
			pstmt.setString(1, email);   // 1번 ? : 이메일
			pstmt.setString(2, title);   // 2번 ? : 제목
			pstmt.setString(3, content);   // 3번 ? : 내용
			pstmt.setInt(4, b_idx);   // 4번 ? : 어느 글인지 (where 조건)
			return pstmt.executeUpdate();   // 바뀐 행 수를 돌려준다
		}
	}

	//===========================================================
	// 7. 글 삭제
	//===========================================================
	/**
	 * 글 1건을 삭제한다.
	 *
	 * [부르는 곳] BoardService 의 serviceDeleteBoard() — 비밀번호 검증을 통과한 뒤에만 호출된다
	 * [반환값]   삭제된 행 수 (성공 1)
	 *
	 * [DAO 는 결과를 숫자로만 알려준다]
	 *   "삭제성공"/"삭제실패" 처럼 사람이 읽을 문구로 바꾸는 일은 이 메소드가
	 *   아니라 상위 계층(BoardService)의 몫이다. 화면 문구가 바뀔 때마다
	 *   DAO 까지 고쳐야 한다면, DAO 와 화면이 너무 강하게 얽혀 있는 것이다.
	 */
	public int deleteBoard(Connection con, int b_idx) throws SQLException {

		String sql = "delete from board where b_idx=?";   // 글 삭제 SQL. where 를 빠뜨리면 게시판 전체가 지워진다

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다
			pstmt.setInt(1, b_idx);   // 1번 ? : 지울 글번호
			return pstmt.executeUpdate();   // 지워진 행 수를 돌려준다
		}
	}

	//===========================================================
	// 8. 새 글 등록  (1) 기존 모든 글의 정렬 그룹을 한 칸 밀기
	//===========================================================
	/**
	 * board 테이블에 있는 모든 글의 b_group 을 1씩 늘려 맨 위 자리(0번)를 비운다.
	 *
	 * [부르는 곳] BoardService 의 serviceInsertBoard() — 아래 insertBoard() 와
	 *            반드시 하나의 트랜잭션으로 함께 실행되어야 한다
	 *            (이 메소드만 성공하고 insertBoard() 가 실패하면, 모든 글의
	 *             순서만 밀려나고 0번 자리는 영구히 빈 채로 남아 정렬이 깨진다)
	 * [반환값]   밀린 행 수 (전체 글 개수와 같다)
	 *
	 * [계층형 게시판 정렬 규칙] 새 글은 항상 맨 위(b_group = 0)에 놓는다.
	 *   그래서 새 글을 넣기 전에 기존 글 전부의 b_group 을 1씩 밀어 0번 자리를 비운다.
	 */
	public int shiftAllGroups(Connection con) throws SQLException {

		String sql = "update board set b_group = b_group + 1";   // 모든 글의 그룹번호를 1씩 밀어 준다. where 가 없으므로 전체가 대상이다

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다
			return pstmt.executeUpdate();   // 밀린 행 수를 돌려준다
		}
	}

	//===========================================================
	// 9. 새 글 등록  (2) 실제 INSERT
	//===========================================================
	/**
	 * 새 글을 board 테이블에 저장한다. 항상 맨 위(b_group=0, b_level=0)로 등록된다.
	 *
	 * [부르는 곳] BoardService 의 serviceInsertBoard() — shiftAllGroups() 바로 다음에 실행된다
	 * [매개변수]
	 *   writer      작성자 이름 (화면에 표시)
	 *   id          작성자 아이디 (로그인 아이디)
	 *   encodedPass 저장할 글 비밀번호
	 * [반환값]   저장된 행 수 (성공하면 1)
	 */
	public int insertBoard(Connection con, String writer, String id, String email,
						   String title, String content, String encodedPass) throws SQLException {

		//b_idx 는 AUTO_INCREMENT 이므로 지정하지 않는다 (DB가 겹치지 않는 번호를 부여)
		//새 글이므로 b_group=0, b_level=0
		String sql = "insert into board(b_id, b_pw, b_name, b_email, b_title, b_content, b_group, b_level, b_date, b_cnt)"
				   + "          values(   ?,    ?,      ?,       ?,       ?,         ?,       0,       0,  now(),    0 )";

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다
			pstmt.setString(1, id);          //작성자 아이디
			pstmt.setString(2, encodedPass); //해시된 글 비밀번호
			pstmt.setString(3, writer);      //작성자 이름
			pstmt.setString(4, email);   // 4번 ? : 이메일
			pstmt.setString(5, title);   // 5번 ? : 제목
			pstmt.setString(6, content);   // 6번 ? : 내용
			return pstmt.executeUpdate();   // 저장된 행 수를 돌려준다 (성공하면 1)
		}
	}

	//===========================================================
	// 10. 답글 등록  (1) 원글의 정렬 그룹 / 들여쓰기 깊이 조회
	//===========================================================
	/**
	 * 답글을 달 원글의 b_group, b_level 값을 함께 조회한다.
	 *
	 * [부르는 곳] BoardService 의 serviceReplyInsertBoard() — 답글의 위치를 계산하는 첫 단계
	 * [반환값]   int[2] 배열 { b_group값, b_level값 }. 원글이 없으면 null
	 *            (원글이 없는데 배열의 [0], [1] 을 그대로 꺼내 쓰면 존재하지 않는
	 *             글번호로 답글을 달 때 SQLException 이 나던 예전 문제가 있었다.
	 *             지금은 호출한 쪽이 null 을 먼저 확인하고 처리한다)
	 */
	public int[] findGroupAndLevel(Connection con, int parentIdx) throws SQLException {

		String sql = "select b_group, b_level from board where b_idx=?";   // 원글의 그룹번호와 들여쓰기 깊이를 함께 꺼내 온다

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다

			pstmt.setInt(1, parentIdx);   // 1번 ? : 답글을 달 원글의 번호

			try (ResultSet rs = pstmt.executeQuery()) {   // 실행하고 결과표를 받는다
				if (rs.next()) {
					return new int[] { rs.getInt("b_group"), rs.getInt("b_level") };
				}
			}
		}

		return null; //원글이 존재하지 않음
	}

	//===========================================================
	// 11. 답글 등록  (2) 원글보다 아래쪽에 있는 글들의 그룹값 밀기
	//===========================================================
	/**
	 * 주어진 그룹번호보다 큰(= 화면에서 더 아래에 있는) 글들의 b_group 을 1씩 늘려
	 * 답글이 들어갈 자리를 비운다.
	 *
	 * [부르는 곳] BoardService 의 serviceReplyInsertBoard() — findGroupAndLevel() 다음,
	 *            insertReply() 전에 실행된다
	 * [반환값]   밀린 행 수
	 *
	 * [정수 컬럼은 정수로 비교한다]
	 *   b_group 을 문자열처럼 비교하면(예: "'10' > '3'") 자릿수 기준으로 비교되어
	 *   '10' 이 '3' 보다 작다고 판정되는 등 순서가 어긋날 수 있고, 인덱스도
	 *   제대로 활용하지 못한다. ? 에 정수(int)를 그대로 바인딩하면 DB 가 숫자
	 *   비교로 처리하므로 이런 문제가 없다.
	 */
	public int shiftGroupsGreaterThan(Connection con, int group) throws SQLException {

		String sql = "update board set b_group = b_group + 1 where b_group > ?";

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다
			pstmt.setInt(1, group);   // 1번 ? : 이 그룹번호보다 뒤에 있는 글들만 민다
			return pstmt.executeUpdate();   // 밀린 행 수를 돌려준다
		}
	}

	//===========================================================
	// 12. 답글 등록  (3) 실제 INSERT
	//===========================================================
	/**
	 * 답글을 board 테이블에 저장한다.
	 *
	 * [부르는 곳] BoardService 의 serviceReplyInsertBoard() — shiftGroupsGreaterThan() 바로 다음
	 * [매개변수]
	 *   group  답글에 매길 b_group  (보통 원글의 b_group + 1)
	 *   level  답글에 매길 b_level  (보통 원글의 b_level + 1)
	 * [반환값]   저장된 행 수 (성공하면 1)
	 */
	public int insertReply(Connection con, String reply_id, String encodedPass, String reply_name,
						   String reply_email, String reply_title, String reply_content,
						   int group, int level) throws SQLException {

		String sql = "insert into board(b_id, b_pw, b_name, b_email, b_title, b_content, b_group, b_level, b_date, b_cnt)"   // 답글 저장 SQL. 새 글과 달리 b_group·b_level 도 ? 로 받아 넣는다
				   + "          values(   ?,    ?,      ?,       ?,       ?,         ?,       ?,       ?,  now(),    0 )";

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다
			pstmt.setString(1, reply_id);   // 1번 ? : 답글 작성자 아이디
			pstmt.setString(2, encodedPass);   // 2번 ? : 해시된 글 비밀번호
			pstmt.setString(3, reply_name);   // 3번 ? : 작성자 이름
			pstmt.setString(4, reply_email);   // 4번 ? : 이메일
			pstmt.setString(5, reply_title);   // 5번 ? : 제목
			pstmt.setString(6, reply_content);   // 6번 ? : 내용
			pstmt.setInt(7, group);  //원글 그룹값 + 1  (원글 바로 아래에 표시)
			pstmt.setInt(8, level);  //원글 깊이  + 1  (한 칸 더 들여쓰기)
			return pstmt.executeUpdate();   // 저장된 행 수를 돌려준다
		}
	}

	//===========================================================
	// 내부 공통 : ResultSet 한 행 -> BoardVo 객체
	//===========================================================
	/**
	 * 조회 결과(ResultSet)의 현재 한 줄을 BoardVo 상자로 바꿔 반환한다.
	 *
	 * [왜 따로 뽑았나] boardList(), boardList(key,word), selectOne() 등 여러 메소드가
	 *   조회 결과를 BoardVo 로 바꾸는 이 11줄짜리 코드를 똑같이 반복해서 갖고 있었다.
	 *   컬럼을 하나 추가하면 그 모든 곳을 다 고쳐야 하고, 한 곳이라도 빼먹으면
	 *   "이 화면에서만 값이 안 나오는" 찾기 어려운 버그가 생긴다.
	 *   한 곳으로 모아 두면 고칠 곳도 언제나 이 메소드 하나뿐이다.
	 */
	private BoardVo mapRow(ResultSet rs) throws SQLException {

		return new BoardVo(   // 결과표의 현재 줄을 BoardVo 생성자에 순서대로 넘겨 상자를 만든다
				rs.getInt("b_idx"),        //글번호
				rs.getString("b_id"),      //작성자 아이디
				rs.getString("b_pw"),      //글 비밀번호(해시)
				rs.getString("b_name"),    //작성자 이름
				rs.getString("b_email"),   //작성자 이메일
				rs.getString("b_title"),   //글 제목
				rs.getString("b_content"), //글 내용
				rs.getInt("b_group"),      //정렬 그룹
				rs.getInt("b_level"),      //들여쓰기 깊이
				rs.getDate("b_date"),      //작성일
				rs.getInt("b_cnt"));       //조회수
	}
}
