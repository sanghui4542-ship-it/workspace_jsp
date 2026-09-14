package Dao;

/*
 * ============================================================================
 *  MemberDAO  -  사원(DAO) : member 테이블에 SQL 을 실행하는 클래스
 *
 *  부장(MemberService)에게 명령을 받아 DB 작업을 하고, 결과를 부장에게 보고(반환)한다.
 *
 *  모든 메소드의 첫 번째 매개변수 con
 *    부장이 DBCPUtil 로 빌려서 넘겨준 DB 연결이다.
 *    사원은 연결을 직접 만들거나 닫지 않고, 받은 연결로 SQL 만 실행한다.
 *
 *  SQL 실행 5단계 리듬
 *    1. SQL 문 작성 (값이 들어갈 자리는 ? 로 비워 둔다)
 *    2. PreparedStatement 준비 : con.prepareStatement(sql)
 *    3. ? 자리에 값 채우기     : pstmt.setString(1, 값)
 *    4. SQL 실행               : 조회 executeQuery() / 추가·수정·삭제 executeUpdate()
 *    5. 결과를 부장에게 반환
 *
 *  try ( ... ) { } 괄호 안에서 만든 pstmt, rs 는 { } 가 끝나면 자동으로 닫힌다
 * ============================================================================
 */

// DB 연결, SQL 실행 도구, 조회 결과, DB 예외
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// 값을 담아 나르는 상자
import Vo.MemberVO;

public class MemberDAO {

	//----------------------------------------------------------------
	// overlappedId : 아이디가 member 테이블에 이미 있는지 조회(select)한다
	//  부르는 곳 : 부장(MemberService) 의 serviceOverLappedId(), serviceKakaoLogin()
	//  반환값   : 있으면 true / 없으면 false
	//----------------------------------------------------------------
	public boolean overlappedId(Connection con, String id) throws SQLException {

		// 1. 같은 아이디를 가진 행의 개수를 세는 SQL  (count(*) 결과를 cnt 라는 이름으로 받는다)
		String sql = "select count(*) as cnt from member where id=?";

		// 2. SQL 을 실행할 준비
		try (PreparedStatement pstmt = con.prepareStatement(sql)) {

			// 3. 첫 번째 ? 자리에 확인할 아이디를 채운다
			pstmt.setString(1, id);

			// 4. 조회(select) SQL 실행 -> 결과 표를 rs 에 저장
			try (ResultSet rs = pstmt.executeQuery()) {

				// 결과 표의 첫 번째 줄로 이동
				if (rs.next()) {

					// 5. 개수(cnt)가 0 보다 크면 true(중복), 0 이면 false(사용 가능)를 부장(MemberService)로 반환
					return rs.getInt("cnt") > 0;
				}
			}
		}

		// 결과가 없으면 중복이 아니므로 false 반환
		return false;
	}

	//----------------------------------------------------------------
	// insertMember : 새 회원 1명을 member 테이블에 추가(insert)한다
	//  부르는 곳 : 부장(MemberService) 의 serviceInsertMember(), serviceKakaoLogin()
	//  반환값   : 추가된 행의 수 (성공 1 / 실패 0)
	//----------------------------------------------------------------
	public int insertMember(Connection con, MemberVO vo) throws SQLException {

		// 1. 새 회원을 추가하는 SQL  (가입일 reg_date 는 DB 의 현재 시각 now() 로 채운다)
		String sql = "insert into member(id, pass, name, email, reg_date, age, gender, address, tel, hp)"
				   + "            values(?,     ?,    ?,     ?,    now(),   ?,      ?,       ?,   ?,  ?)";

		// 2. SQL 을 실행할 준비
		try (PreparedStatement pstmt = con.prepareStatement(sql)) {

			// 3. ? 자리 9개에 MemberVO 상자에 담긴 값을 순서대로 채운다
			pstmt.setString(1, vo.getId());
			pstmt.setString(2, vo.getPass());
			pstmt.setString(3, vo.getName());
			pstmt.setString(4, vo.getEmail());
			pstmt.setInt(5, vo.getAge());
			pstmt.setString(6, vo.getGender());
			pstmt.setString(7, vo.getAddress());
			pstmt.setString(8, vo.getTel());
			pstmt.setString(9, vo.getHp());

			// 4~5. 추가(insert) SQL 실행 -> 추가된 행의 수(1 또는 0)를 부장(MemberService)로 반환
			return pstmt.executeUpdate();
		}
	}

	//----------------------------------------------------------------
	// findPasswordById : 아이디로 DB 에 저장된 비밀번호를 조회(select)한다
	//  부르는 곳 : 부장(MemberService) 의 serviceUserCheck(), serviceUpdateMember()
	//  반환값   : 비밀번호 / 아이디가 없으면 null
	//----------------------------------------------------------------
	public String findPasswordById(Connection con, String id) throws SQLException {

		// 1. 아이디로 비밀번호를 조회하는 SQL
		String sql = "select pass from member where id=?";

		// 2. SQL 을 실행할 준비
		try (PreparedStatement pstmt = con.prepareStatement(sql)) {

			// 3. 첫 번째 ? 자리에 아이디를 채운다
			pstmt.setString(1, id);

			// 4. 조회(select) SQL 실행 -> 결과 표를 rs 에 저장
			try (ResultSet rs = pstmt.executeQuery()) {

				// 결과 표에 줄이 있으면 (아이디가 있으면)
				if (rs.next()) {

					// 5. pass 열의 값을 부장(MemberService)로 반환
					return rs.getString("pass");
				}
			}
		}

		// 결과가 없으면 (아이디가 없으면) null 반환
		return null;
	}

	//----------------------------------------------------------------
	// memberOne : 글쓰기 화면에 보여줄 회원의 이메일, 이름, 아이디를 조회(select)한다
	//  부르는 곳 : 부장(BoardService, FileBoardService) 의 글쓰기 화면 요청
	//  반환값   : 조회한 정보가 담긴 MemberVO / 없으면 null
	//----------------------------------------------------------------
	public MemberVO memberOne(Connection con, String memberid) throws SQLException {

		// 1. 아이디로 이메일, 이름, 아이디를 조회하는 SQL
		String sql = "select email, name, id from member where id=?";

		// 2. SQL 을 실행할 준비
		try (PreparedStatement pstmt = con.prepareStatement(sql)) {

			// 3. 첫 번째 ? 자리에 아이디를 채운다
			pstmt.setString(1, memberid);

			// 4. 조회(select) SQL 실행 -> 결과 표를 rs 에 저장
			try (ResultSet rs = pstmt.executeQuery()) {

				// 결과 표에 줄이 있으면
				if (rs.next()) {

					// 조회한 값들을 새 MemberVO 상자에 담는다
					MemberVO vo = new MemberVO();
					vo.setEmail(rs.getString("email"));
					vo.setName(rs.getString("name"));
					vo.setId(rs.getString("id"));

					// 5. 값이 담긴 MemberVO 를 부장으로 반환
					return vo;
				}
			}
		}

		// 결과가 없으면 null 반환
		return null;
	}

	//----------------------------------------------------------------
	// memberDetail : 회원정보 수정 화면에 보여줄 회원 1명의 정보를 조회(select)한다
	//  부르는 곳 : 부장(MemberService) 의 serviceMemberDetail()
	//  반환값   : 조회한 정보가 담긴 MemberVO / 없으면 null
	//----------------------------------------------------------------
	public MemberVO memberDetail(Connection con, String id) throws SQLException {

		// 1. 아이디로 회원 정보 8개 열을 조회하는 SQL  (비밀번호 pass 는 화면에 보여주지 않으므로 조회하지 않는다)
		String sql = "select id, name, email, age, gender, address, tel, hp from member where id=?";

		// 2. SQL 을 실행할 준비
		try (PreparedStatement pstmt = con.prepareStatement(sql)) {

			// 3. 첫 번째 ? 자리에 아이디를 채운다
			pstmt.setString(1, id);

			// 4. 조회(select) SQL 실행 -> 결과 표를 rs 에 저장
			try (ResultSet rs = pstmt.executeQuery()) {

				// 결과 표에 줄이 있으면
				if (rs.next()) {

					// 조회한 값들을 새 MemberVO 상자에 담는다
					MemberVO vo = new MemberVO();
					vo.setId(rs.getString("id"));
					vo.setName(rs.getString("name"));
					vo.setEmail(rs.getString("email"));
					vo.setAge(rs.getInt("age"));
					vo.setGender(rs.getString("gender"));
					vo.setAddress(rs.getString("address"));
					vo.setTel(rs.getString("tel"));
					vo.setHp(rs.getString("hp"));

					// 5. 값이 담긴 MemberVO 를 부장(MemberService)로 반환
					return vo;
				}
			}
		}

		// 결과가 없으면 null 반환
		return null;
	}

	//----------------------------------------------------------------
	// updateMember : 회원정보를 수정(update)한다
	//  부르는 곳 : 부장(MemberService) 의 serviceUpdateMember()
	//  반환값   : 수정된 행의 수 (성공 1 / 실패 0)
	//----------------------------------------------------------------
	public int updateMember(Connection con, MemberVO vo) throws SQLException {

		// 상자에 새 비밀번호가 들어 있으면 비밀번호도 수정(true), 없으면 비밀번호는 그대로 둔다(false)
		boolean changePassword = (vo.getPass() != null && !vo.getPass().isEmpty());

		// 1. 비밀번호를 바꾸는지에 따라 SQL 을 둘 중 하나로 정한다
		String sql;
		if (changePassword) {
			sql = "update member set pass=?, name=?, age=?, gender=?, address=?, email=? where id=?";
		} else {
			sql = "update member set name=?, age=?, gender=?, address=?, email=? where id=?";
		}

		// 2. SQL 을 실행할 준비
		try (PreparedStatement pstmt = con.prepareStatement(sql)) {

			// 3. ? 자리 번호. 값을 하나 채울 때마다 idx++ 로 1씩 늘린다
			int idx = 1;

			// 비밀번호를 바꾸는 경우에만 첫 번째 ? 자리에 새 비밀번호를 채운다
			if (changePassword) {
				pstmt.setString(idx++, vo.getPass());
			}

			// 나머지 ? 자리에 MemberVO 상자의 값을 순서대로 채운다
			pstmt.setString(idx++, vo.getName());
			pstmt.setInt(idx++, vo.getAge());
			pstmt.setString(idx++, vo.getGender());
			pstmt.setString(idx++, vo.getAddress());
			pstmt.setString(idx++, vo.getEmail());
			pstmt.setString(idx++, vo.getId());

			// 4~5. 수정(update) SQL 실행 -> 수정된 행의 수(1 또는 0)를 부장(MemberService)로 반환
			return pstmt.executeUpdate();
		}
	}

	//----------------------------------------------------------------
	// deleteMember : 회원 1명을 member 테이블에서 삭제(delete)한다
	//  부르는 곳 : 부장(MemberService) 의 serviceDeleteMember()
	//  반환값   : 삭제된 행의 수 (성공 1 / 실패 0)
	//----------------------------------------------------------------
	public int deleteMember(Connection con, String id) throws SQLException {

		// 1. 아이디로 회원을 삭제하는 SQL
		String sql = "delete from member where id=?";

		// 2. SQL 을 실행할 준비
		try (PreparedStatement pstmt = con.prepareStatement(sql)) {

			// 3. 첫 번째 ? 자리에 아이디를 채운다
			pstmt.setString(1, id);

			// 4~5. 삭제(delete) SQL 실행 -> 삭제된 행의 수(1 또는 0)를 부장(MemberService)로 반환
			return pstmt.executeUpdate();
		}
	}
}
