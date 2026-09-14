package Dao;

/*
 * ============================================================================
 *  FileBoardDAO  -  사원(DAO) : 자료실(fileboard) 테이블에 SQL 을 실행하는 클래스
 *
 *  구조는 BoardDAO(자유게시판)와 거의 같고, 첨부파일 관련 열(ofile, sfile,
 *  downcount)과 그것을 다루는 메소드 몇 개가 추가된다.
 *
 *  [글번호를 DB 의 AUTO_INCREMENT 에 맡기는 이유]
 *
 *    (예전 방식)
 *        select max(b_idx) from fileboard   →  가장 큰 번호를 조회
 *        그 번호 + 1 을 계산해서 insert 문에 직접 넣는다
 *
 *    무엇이 문제였나
 *      두 사람이 동시에 글을 쓰면 둘 다 "지금 가장 큰 번호"를 똑같이 읽는다.
 *      예를 들어 지금 최댓값이 10이면 두 사람 모두 "다음 번호는 11"이라고 계산한다.
 *
 *          사람A : max(b_idx) 조회 -> 10  ->  11 로 insert (성공)
 *          사람B : max(b_idx) 조회 -> 10  ->  11 로 insert (기본키 중복 오류!)
 *
 *      사람B 는 글이 등록되지 않는데, 이미 올려 둔 첨부파일은 temp 폴더에
 *      남아 버린다. 또한 글을 지운 뒤 새 글을 쓰면 삭제된 번호가 재사용되어
 *      예전 글의 첨부파일 폴더와 뒤섞일 위험도 있다.
 *
 *    (지금 방식) 번호 발급을 DB(AUTO_INCREMENT)에게 완전히 맡긴다.
 *      insert 를 실행한 뒤 Statement.getGeneratedKeys() 로 "방금 이 insert 로
 *      만들어진 번호"를 돌려받는다. DB 는 여러 요청이 몰려도 번호를 하나씩만
 *      순서대로 내어주므로 겹칠 수가 없다.
 * ============================================================================
 */

// DB 연결, SQL 실행 도구, 조회 결과, DB 예외
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// insert 후 자동 생성된 번호(auto_increment)를 돌려받을 때 쓰는 도구
import java.sql.Statement;

// 여러 건의 글을 순서대로 담을 목록 도구
import java.util.ArrayList;
import java.util.List;

// 값을 담아 나르는 상자
import Vo.FileBoardVo;

public class FileBoardDAO {

	/* 인스턴스 변수가 없다 = 무상태(stateless) = 여러 스레드가 동시에 써도 안전하다
	   (자세한 이유는 BoardDAO 클래스 설명의 "동시 접속 위험" 부분과 같다) */

	/* [정리] 이 자리에 있던 조회 메소드 2개를 지웠다.
	       boardList(con)                  전체 글목록 (검색·페이징 없음)
	       boardList(con, key, word)       검색 결과 전체 (페이징 없음)
	   두 메소드는 FileBoardService 의 serviceBoardList() / serviceBoardKeyWord() 만
	   호출했는데, 그 두 Service 메소드를 부르는 곳이 없어 함께 정리했다.
	   지금 목록·검색은 모두 아래 countBoards() + boardList(...offset, limit) 조합
	   (SQL 페이징)으로 처리한다. */

	//===========================================================
	// 2-1. 검색 조건에 맞는 전체 글 개수 (페이징용)
	//===========================================================
	/**
	 * [부르는 곳] FileBoardService 의 serviceBoardPage()
	 * 전체 목록을 읽어 size()로 세는 대신 DB가 개수만 세게 한다 (BoardDAO 와 같은 개선 이유)
	 */
	public int countBoards(Connection con, String key, String word) throws SQLException {

		boolean hasWord = (word != null && !word.trim().isEmpty());   // 검색어가 실제로 들어왔는지 확인
		boolean searchTitleContent = "titleContent".equals(key);   // 검색 종류가 "제목+내용" 인가

		String sql = "select count(*) as cnt from fileboard" + buildWhere(hasWord, searchTitleContent);   // where 절은 아래 buildWhere 가 만들어 준다. 목록 조회와 똑같은 조건을 쓰기 위해서다

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다

			bindWord(pstmt, hasWord, searchTitleContent, word);   // 검색어 ? 를 채운다 (검색어가 없으면 아무것도 안 채운다)

			try (ResultSet rs = pstmt.executeQuery()) {   // 실행하고 결과표를 받는다
				if (rs.next()) {   // count 결과는 항상 한 줄이다
					return rs.getInt("cnt");   // 그 개수를 돌려준다. 페이징이 이 숫자로 전체 페이지 수를 계산한다
				}
			}
		}

		return 0;   // 못 읽었으면 0건으로 본다
	}

	//===========================================================
	// 2-2. 검색 조건 + 페이지 범위의 글목록만 조회 (페이징용)
	//===========================================================
	/**
	 * [부르는 곳] FileBoardService 의 serviceBoardPage()
	 * [매개변수] offset 건너뛸 글 개수, limit 가져올 글 개수 (자세한 limit 문법은 BoardDAO 참고)
	 */
	public List<FileBoardVo> boardList(Connection con, String key, String word, int offset, int limit)
			throws SQLException {

		List<FileBoardVo> list = new ArrayList<FileBoardVo>();   // 결과를 담을 빈 목록

		boolean hasWord = (word != null && !word.trim().isEmpty());   // 검색어가 실제로 들어왔는지 확인
		boolean searchTitleContent = "titleContent".equals(key);   // 검색 종류가 "제목+내용" 인가

		String sql = "select * from fileboard"   // 개수 조회와 같은 where 를 쓰고, 뒤에 limit 를 붙여 이 페이지 몫만 가져온다
				   + buildWhere(hasWord, searchTitleContent)
				   + " order by b_group asc limit ?, ?";

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다

			int idx = bindWord(pstmt, hasWord, searchTitleContent, word);   // 검색어 ? 를 먼저 채우고, 이어서 쓸 번호를 돌려받는다

			pstmt.setInt(idx++, offset); //건너뛸 개수
			pstmt.setInt(idx++, limit);  //가져올 개수

			try (ResultSet rs = pstmt.executeQuery()) {   // 값을 다 끼운 뒤 실행한다
				while (rs.next()) {   // 이 페이지 몫만 읽힌다
					list.add(mapRow(rs));   // 한 줄을 FileBoardVo 로 바꿔 담는다
				}
			}
		}

		return list;   // 이 페이지에 보여줄 공지 목록을 돌려준다
	}

	/** 개수 조회와 목록 조회가 같은 조건을 쓰도록 WHERE 절을 한 곳에서 만든다 (BoardDAO 와 같은 이유) */
	private String buildWhere(boolean hasWord, boolean searchTitleContent) {

		if (!hasWord) {   // 검색어가 없으면
			return "";   // where 절 자체가 없다 (빈 문자열을 이어 붙이면 전체 조회가 된다)
		}
		return searchTitleContent   // 검색 종류에 따라 두 가지 where 중 하나를 돌려준다
				? " where b_title like ? or b_content like ?"
				: " where b_name like ?";
	}

	/** 검색어 ? 를 채우고 다음에 사용할 ? 번호를 반환한다 (buildWhere() 와 같은 이유로 한 곳에 모았다) */
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
	/** [부르는 곳] FileBoardService 의 serviceBoardRead() (글 조회와 함께 트랜잭션으로 묶여 실행된다) */
	public int increaseReadCount(Connection con, int b_idx) throws SQLException {

		String sql = "update fileboard set b_cnt = b_cnt + 1 where b_idx = ?";   // 조회수를 지금 값에서 1 늘린다

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다
			pstmt.setInt(1, b_idx);   // 1번 ? : 어느 글인지
			return pstmt.executeUpdate();   // 바뀐 행 수를 돌려준다
		}
	}

	//===========================================================
	// 4. 글 1건 조회 (없으면 null)
	//===========================================================
	/** [부르는 곳] FileBoardService 의 serviceBoardRead() */
	public FileBoardVo selectOne(Connection con, int b_idx) throws SQLException {

		String sql = "select * from fileboard where b_idx = ?";   // 글번호 하나로 공지 한 건을 찾는다

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다

			pstmt.setInt(1, b_idx);   // 1번 ? : 찾을 글번호

			try (ResultSet rs = pstmt.executeQuery()) {   // 실행하고 결과표를 받는다
				if (rs.next()) {   // 글번호는 기본키라 결과는 0건 아니면 1건이다
					return mapRow(rs);   // 찾았으면 FileBoardVo 로 바꿔 돌려준다
				}
			}
		}

		return null;   // 그 번호의 공지가 없다는 뜻. Service 가 404 로 바꿔 던진다
	}

	//===========================================================
	// 5. 글번호로 저장된 글 비밀번호 조회 (없으면 null)
	//===========================================================
	/** [부르는 곳] FileBoardService 의 servicePassCheck(), serviceUpdateBoard(), serviceDeleteBoard() */
	public String findPasswordByIdx(Connection con, int b_idx) throws SQLException {

		String sql = "select b_pw from fileboard where b_idx=?";   // 저장된 글 비밀번호 한 칸만 꺼내 온다 (비교는 Service 가 한다)

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
	// 6. 첨부파일 정보 조회 (다운로드 검증용)
	//===========================================================
	/**
	 * [부르는 곳] FileBoardService 의 serviecDownload() — 다운로드 요청을 처리하는 1차 방어선
	 * [반환값]   String[2] { ofile(원본명 목록), sfile(저장명 목록) }. 글이 없으면 null
	 *
	 * [경로 조작(Path Traversal) 방어의 핵심]
	 *   예전 다운로드 기능은 사용자가 보낸 파일명을 검증 없이 그대로 경로에 이어
	 *   붙였다. 그래서 아래 같은 요청으로 저장 폴더를 벗어나 서버 안의 다른
	 *   파일(설정 파일 등)을 받아낼 수 있었다.
	 *       /FileBoard/Download.do?path=..&fileName=..\web.xml
	 *   지금은 사용자가 보낸 파일명을 절대 그대로 쓰지 않는다. 먼저 글번호로
	 *   DB 를 조회해서 "이 글에 실제로 첨부된 파일명 목록"을 가져오고, 사용자가
	 *   요청한 이름이 그 목록에 실제로 있을 때만 다운로드를 허락한다. DB 에
	 *   없는 이름은 무엇을 보내도 통과할 수 없다.
	 */
	public String[] findAttachNames(Connection con, int b_idx) throws SQLException {

		String sql = "select ofile, sfile from fileboard where b_idx=?";   // 원본 파일명과 저장 파일명 두 칸만 꺼내 온다 (삭제·다운로드에 쓴다)

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다

			pstmt.setInt(1, b_idx);   // 1번 ? : 어느 글인지

			try (ResultSet rs = pstmt.executeQuery()) {   // 실행하고 결과표를 받는다
				if (rs.next()) {   // 그 글이 있으면
					return new String[] { rs.getString("ofile"), rs.getString("sfile") };   // 두 값을 길이 2짜리 배열에 담아 한 번에 돌려준다
				}
			}
		}

		return null;   // 그 번호의 글이 없다는 뜻
	}

	//===========================================================
	// 7. 글 수정
	//===========================================================
	/** [부르는 곳] FileBoardService 의 serviceUpdateBoard() — 첨부파일은 이 메소드가 손대지 않는다 */
	public int updateBoard(Connection con, int b_idx, String email, String title, String content)
			throws SQLException {

		String sql = "update fileboard set b_email=?, b_title=?, b_content=? where b_idx=?";   // 공지 수정 SQL. 제목·내용·이메일 세 칸만 바꾼다 (첨부파일은 건드리지 않는다)

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다
			pstmt.setString(1, email);   // 1번 ? : 이메일
			pstmt.setString(2, title);   // 2번 ? : 제목
			pstmt.setString(3, content);   // 3번 ? : 내용
			pstmt.setInt(4, b_idx);   // 4번 ? : 어느 글인지 (where 조건)
			return pstmt.executeUpdate();   // 바뀐 행 수를 돌려준다
		}
	}

	//===========================================================
	// 8. 글 삭제
	//===========================================================
	/** [부르는 곳] FileBoardService 의 serviceDeleteBoard() — DB 삭제만 하고, 첨부파일 폴더 삭제는 Service 가 별도로 처리한다 */
	public int deleteBoard(Connection con, int b_idx) throws SQLException {

		String sql = "delete from fileboard where b_idx=?";   // 공지 삭제 SQL. where 를 빠뜨리면 게시판 전체가 지워진다

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다
			pstmt.setInt(1, b_idx);   // 1번 ? : 지울 글번호
			return pstmt.executeUpdate();   // 지워진 행 수를 돌려준다
		}
	}

	//===========================================================
	// 9. 새 글 등록 (1) 기존 모든 글의 정렬 그룹 한 칸 밀기
	//===========================================================
	/** [부르는 곳] FileBoardService 의 serviceInsertBoard() — 아래 insertBoard() 와 한 트랜잭션으로 함께 실행된다 */
	public int shiftAllGroups(Connection con) throws SQLException {

		String sql = "update fileboard set b_group = b_group + 1";   // 모든 글의 그룹번호를 1씩 밀어 준다. where 가 없으므로 전체가 대상이다

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다
			return pstmt.executeUpdate();   // 밀린 행 수를 돌려준다
		}
	}

	//===========================================================
	// 10. 새 글 등록 (2) 실제 INSERT + 부여된 글번호 반환  ★
	//===========================================================
	/**
	 * [부르는 곳] FileBoardService 의 serviceInsertBoard() — shiftAllGroups() 바로 다음에 실행된다
	 * [반환값]   DB 가 새로 부여한 글번호 (첨부파일을 이 번호의 폴더로 옮기는 데 쓴다)
	 * [예외]     저장에 실패했거나 번호를 못 받으면 SQLException 을 직접 던진다
	 *
	 * [getGeneratedKeys() 사용법]
	 *   1) prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) 로 준비한다
	 *      → "이 INSERT 로 만들어진 자동 번호를 알려 달라" 는 요청
	 *   2) executeUpdate() 실행 후 getGeneratedKeys() 로 ResultSet 을 받는다
	 *      → 그 첫 번째 컬럼에 방금 부여된 b_idx 가 들어 있다
	 *   이 방식은 동시에 여러 명이 등록해도 "내가 방금 넣은 행의 번호"만
	 *   정확히 돌려준다 (select max(b_idx) 는 그 사이 다른 사람이 넣은
	 *   번호를 가져올 위험이 있어 쓰지 않는다).
	 *
	 * [왜 글번호가 꼭 필요한가]
	 *   업로드된 첨부파일을 최종적으로 C:\file_repo_edu\{글번호}\ 폴더로
	 *   옮겨야 하는데, 그 폴더 이름 자체가 이 글번호이기 때문이다.
	 */
	public int insertBoard(Connection con, FileBoardVo vo) throws SQLException {

		//b_idx 는 AUTO_INCREMENT 이므로 컬럼 목록에서 제외한다
		String sql = "insert into fileboard(b_id, b_pw, b_name, b_email, b_title, b_content,"
				   + " b_group, b_level, b_date, b_cnt, ofile, sfile, downcount)"
				   + " values(?,?,?,?,?,?, 0, 0, now(), 0, ?, ?, 0)";

		try (PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {   // 두 번째 인자가 "자동 번호를 알려 달라" 는 요청이다

			pstmt.setString(1, vo.getB_id());   // 1번 ? : 작성자 아이디
			pstmt.setString(2, vo.getB_pw());     //해시된 글 비밀번호
			pstmt.setString(3, vo.getB_name());   // 3번 ? : 작성자 이름
			pstmt.setString(4, vo.getB_email());   // 4번 ? : 이메일
			pstmt.setString(5, vo.getB_title());   // 5번 ? : 제목
			pstmt.setString(6, vo.getB_content());   // 6번 ? : 내용
			pstmt.setString(7, vo.getOfile());    //원본 파일명 목록 (; 구분)
			pstmt.setString(8, vo.getSfile());    //저장 파일명 목록 (; 구분)

			int affected = pstmt.executeUpdate();   // 실행하고 저장된 행 수를 받는다

			if (affected == 0) {   // 0건이면 저장이 안 된 것이다
				throw new SQLException("게시글 등록에 실패했습니다");   // 조용히 넘어가면 없는 글번호로 파일을 옮기게 되므로 예외를 던진다
			}

			//DB가 방금 부여한 글번호를 받아온다
			try (ResultSet keys = pstmt.getGeneratedKeys()) {
				if (keys.next()) {
					return keys.getInt(1);   // 첫 번째 컬럼에 방금 부여된 글번호가 들어 있다
				}
			}

			throw new SQLException("등록된 글번호를 가져오지 못했습니다");   // 저장은 됐는데 번호를 못 받은 경우. 파일을 옮길 수 없으므로 예외를 던진다
		}
	}

	//===========================================================
	// 11. 답글 등록 (1) 원글의 그룹/깊이 조회 (없으면 null)
	//===========================================================
	/** [부르는 곳] FileBoardService 의 serviceReplyInsertBoard() — 답글의 위치를 계산하는 첫 단계 */
	public int[] findGroupAndLevel(Connection con, int parentIdx) throws SQLException {

		String sql = "select b_group, b_level from fileboard where b_idx=?";   // 원글의 그룹번호와 들여쓰기 깊이를 함께 꺼내 온다

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다

			pstmt.setInt(1, parentIdx);   // 1번 ? : 답글을 달 원글의 번호

			try (ResultSet rs = pstmt.executeQuery()) {   // 실행하고 결과표를 받는다
				if (rs.next()) {   // 원글이 있으면
					return new int[] { rs.getInt("b_group"), rs.getInt("b_level") };   // 두 값을 배열에 담아 한 번에 돌려준다
				}
			}
		}

		return null;   // 원글이 없다는 뜻 (없는 글에 답글을 달려 한 경우)
	}

	//===========================================================
	// 12. 답글 등록 (2) 원글보다 아래쪽 글들의 그룹값 밀기
	//===========================================================
	/** [부르는 곳] FileBoardService 의 serviceReplyInsertBoard() — findGroupAndLevel() 다음, insertReply() 전 */
	public int shiftGroupsGreaterThan(Connection con, int group) throws SQLException {

		String sql = "update fileboard set b_group = b_group + 1 where b_group > ?";   // 이 그룹번호보다 뒤에 있는 글들만 한 칸씩 민다

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다
			pstmt.setInt(1, group);   // 1번 ? : 기준이 되는 원글의 그룹번호
			return pstmt.executeUpdate();   // 밀린 행 수를 돌려준다
		}
	}

	//===========================================================
	// 13. 답글 등록 (3) 실제 INSERT
	//===========================================================
	/**
	 * [부르는 곳] FileBoardService 의 serviceReplyInsertBoard() — shiftGroupsGreaterThan() 바로 다음
	 * 첨부파일이 없는 답글이므로 ofile / sfile 은 null 로 둔다.
	 *
	 * [예전에 있던 버그 - 참고]
	 *   과거 버전은 답글 INSERT 문에 b_idx 컬럼이 아예 빠져 있었다. 원글
	 *   등록(insertBoard)은 b_idx 를 직접 지정해 넣었는데 답글 등록만
	 *   b_idx 를 빼고 INSERT 했다. 만약 b_idx 가 AUTO_INCREMENT 가
	 *   아니었다면 두 번째 답글부터 기본키 중복으로 저장이 실패했을
	 *   상황이다. 지금은 b_idx 가 AUTO_INCREMENT 이고 원글과 답글 모두
	 *   b_idx 를 지정하지 않는 방식으로 통일되어 있어 이런 불일치가 생기지 않는다.
	 */
	public int insertReply(Connection con, String reply_id, String encodedPass, String reply_name,
						   String reply_email, String reply_title, String reply_content,
						   int group, int level) throws SQLException {

		String sql = "insert into fileboard(b_id, b_pw, b_name, b_email, b_title, b_content,"
				   + " b_group, b_level, b_date, b_cnt, ofile, sfile, downcount)"
				   + " values(?,?,?,?,?,?, ?, ?, now(), 0, null, null, 0)";

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다
			pstmt.setString(1, reply_id);   // 1번 ? : 답글 작성자 아이디
			pstmt.setString(2, encodedPass);   // 2번 ? : 해시된 글 비밀번호
			pstmt.setString(3, reply_name);   // 3번 ? : 작성자 이름
			pstmt.setString(4, reply_email);   // 4번 ? : 이메일
			pstmt.setString(5, reply_title);   // 5번 ? : 제목
			pstmt.setString(6, reply_content);   // 6번 ? : 내용
			pstmt.setInt(7, group); //원글 그룹값 + 1
			pstmt.setInt(8, level); //원글 깊이  + 1
			return pstmt.executeUpdate();   // 저장된 행 수를 돌려준다
		}
	}

	//===========================================================
	// 14. 다운로드 횟수 1 증가
	//===========================================================
	/** [부르는 곳] FileBoardService 의 serviecDownload() — 파일 전송이 끝난 뒤 마지막에 실행된다 */
	public int increaseDownloadCount(Connection con, int b_idx) throws SQLException {

		String sql = "update fileboard set downcount = downcount + 1 where b_idx=?";   // 다운로드 횟수를 1 늘린다

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다
			pstmt.setInt(1, b_idx);   // 1번 ? : 어느 글의 첨부파일인지
			return pstmt.executeUpdate();   // 바뀐 행 수를 돌려준다
		}
	}

	//===========================================================
	// 내부 공통 : ResultSet 한 행 -> FileBoardVo 객체
	//===========================================================
	/** [왜 따로 뽑았나] 이 14줄짜리 객체 생성 코드를 여러 조회 메소드가 반복해서 갖고 있던 것을 한 곳으로 모았다 (BoardDAO 의 mapRow() 와 같은 이유) */
	private FileBoardVo mapRow(ResultSet rs) throws SQLException {

		return new FileBoardVo(   // 결과표의 현재 줄을 FileBoardVo 생성자에 순서대로 넘겨 상자를 만든다
				rs.getInt("b_idx"),
				rs.getString("b_id"),
				rs.getString("b_pw"),
				rs.getString("b_name"),
				rs.getString("b_email"),
				rs.getString("b_title"),
				rs.getString("b_content"),
				rs.getInt("b_group"),
				rs.getInt("b_level"),
				rs.getDate("b_date"),
				rs.getInt("b_cnt"),
				rs.getString("ofile"),     //원본 파일명 목록
				rs.getString("sfile"),     //저장 파일명 목록
				rs.getInt("downcount"));   //다운로드 횟수
	}
}
