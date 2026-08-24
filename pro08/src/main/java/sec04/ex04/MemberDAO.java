package sec04.ex04;

// 주제 : try-with-resources 문법으로 DB 자원을 자동 반납하는 MemberDAO

/*
    try-with-resources 핵심 정리

    문법 : try ( 자원 생성 코드 ) { 실행 코드 }
    효과 : try 블록이 끝나면 소괄호 안 자원을 자바가 자동으로 close() 한다.
    조건 : AutoCloseable을 구현한 객체만 가능 (Connection, PreparedStatement, ResultSet 모두 해당)

    자동으로 닫히는 순서 (생성 순서의 반대)

    try ( con 생성 ; pstmt 생성 ; rs 생성 ) { 실행 }  블록 종료
                                                        |
                                                        v
                       자동 실행 : rs.close() -> pstmt.close() -> con.close()

    이 버전에서 사라진 것 : 멤버 변수 con/pstmt/rs, finally 블록, ResourceClose() 메소드
*/

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class MemberDAO { // 오라클 XE의 t_member 테이블과 연결해서 DB 작업하는 클래스

	// 커넥션풀은 닫으면 안 되는 공용 자원 -> try 소괄호가 아닌 멤버 변수로 유지
	private DataSource dataSource;

	// 생성자 : 톰캣 JNDI 저장소에서 커넥션풀(DataSource)을 찾아와 저장
	public MemberDAO() {
		try {
			// JNDI 탐색 시작점 생성 후 톰캣 자원 구역(java:/comp/env)으로 이동
			Context ctx = new InitialContext();
			Context envCtx = (Context) ctx.lookup("java:/comp/env");

			// context.xml의 name="jdbc/oracle" 키로 커넥션풀 꺼내오기
			dataSource = (DataSource) envCtx.lookup("jdbc/oracle");

		} catch (Exception e) {
			System.out.println("DataSource 커넥션풀 객체 얻기 실패 : " + e.toString());
		}
	}

	//================================================================
	// 1. 전체 회원 조회 (SELECT 여러 행)
	//================================================================
	public ArrayList<MemberVO> listMembers() {

		// 조회된 회원(MemberVO)들을 담아 반환할 가변길이 배열
		ArrayList<MemberVO> list = new ArrayList<MemberVO>();

		// ? 가 없는 SQL -> 소괄호 안에서 바로 executeQuery() 실행 가능
		String query = "select * from t_member";

		// 자원 3개를 의존 순서대로 생성 : con -> pstmt -> rs (종료 시 반대 순서로 자동 close)
		try (Connection con = dataSource.getConnection();
			 PreparedStatement pstmt = con.prepareStatement(query);
			 ResultSet rs = pstmt.executeQuery()) {

			// 커서를 한 행씩 이동시키며 행이 있는 동안 반복
			while (rs.next()) {

				// 현재 행의 열 값들을 MemberVO 객체 1개에 담기
				MemberVO vo = new MemberVO();
				vo.setId(rs.getString("id"));
				vo.setPwd(rs.getString("pwd"));
				vo.setName(rs.getString("name"));
				vo.setEmail(rs.getString("email"));
				vo.setJoinDate(rs.getDate("joinDate"));

				// 완성된 회원 객체를 배열 끝에 추가
				list.add(vo);
			}

		} catch (Exception e) {
			System.out.println("MemberDAO의 listMembers메소드에서 select문 실행 오류 : " + e);
			e.printStackTrace();
		}

		return list; // 조회된 회원 수만큼 담긴 배열 반환 (0명이면 빈 배열)
	}

	//================================================================
	// 2. 수정할 회원 한 명 조회 (SELECT 한 행, ? 조건 있음)
	//================================================================
	public MemberVO modMember(String modId) {

		// 조회한 회원 1명의 정보를 담아 반환할 객체
		MemberVO vo = new MemberVO();

		// ? 가 있는 SQL -> 생성 후 ?를 채우고 나서 실행해야 한다
		String query = "select * from t_member where id = ?";

		// 순서가 [생성 -> ?채움 -> 실행] 이므로 rs는 소괄호에 못 넣고 안쪽 try로 따로 연다
		try (Connection con = dataSource.getConnection();
			 PreparedStatement pstmt = con.prepareStatement(query)) {

			// 첫번째 ? 자리에 수정할 회원의 아이디 채우기
			pstmt.setString(1, modId);

			// ?를 채운 뒤 실행, rs는 안쪽 try 종료 시 먼저 자동 반납된다
			try (ResultSet rs = pstmt.executeQuery()) {

				// 조회 결과는 최대 1행 -> if로 커서를 1번만 이동
				if (rs.next()) {
					vo.setId(rs.getString("id"));
					vo.setPwd(rs.getString("pwd"));
					vo.setName(rs.getString("name"));
					vo.setEmail(rs.getString("email"));
					vo.setJoinDate(rs.getDate("joinDate"));
				}
			}

		} catch (Exception e) {
			System.out.println("MemberDAO의 modMember메소드에서 select문 실행 오류 : " + e);
			e.printStackTrace();
		}

		return vo; // 조회된 회원 정보 반환 (없으면 빈 vo)
	}

	//================================================================
	// 3. 새 회원 추가 (INSERT, ? 4개)
	//================================================================
	public int addMember(MemberVO vo) {

		// 추가 성공 1 / 실패 0 이 저장될 변수
		int result = 0;

		// 가입날짜는 오라클의 현재날짜 sysdate 사용
		String query = "insert into t_member(id, pwd, name, email, joinDate) values(?,?,?,?,sysdate)";

		// INSERT는 조회 결과(rs)가 없으므로 자원 2개만 소괄호에 생성
		try (Connection con = dataSource.getConnection();
			 PreparedStatement pstmt = con.prepareStatement(query)) {

			// ? 4개를 왼쪽부터 순서대로(1~4) 가입값으로 채우기
			pstmt.setString(1, vo.getId());
			pstmt.setString(2, vo.getPwd());
			pstmt.setString(3, vo.getName());
			pstmt.setString(4, vo.getEmail());

			// 완성된 INSERT 실행 -> 추가된 행 개수(성공 1) 반환
			result = pstmt.executeUpdate();

		} catch (Exception e) {
			System.out.println("MemberDAO의 addMember메소드에서 insert문 실행 오류 : " + e);
			e.printStackTrace();
		}

		return result;
	}

	//================================================================
	// 4. 회원 한 명 수정 (UPDATE, ? 4개)
	//================================================================
	public int updateMember(MemberVO vo) {

		// 수정 성공 1 / 실패 0 이 저장될 변수
		int result = 0;

		// set 뒤 = 바꿀 열 3개(?), where 뒤 = 대상 조건(?)
		String query = "update t_member set pwd=?, name=?, email=? where id=?";

		try (Connection con = dataSource.getConnection();
			 PreparedStatement pstmt = con.prepareStatement(query)) {

			// ? 4개를 순서대로 : 수정값 3개 + where 조건 아이디
			pstmt.setString(1, vo.getPwd());
			pstmt.setString(2, vo.getName());
			pstmt.setString(3, vo.getEmail());
			pstmt.setString(4, vo.getId());

			// 완성된 UPDATE 실행 -> 수정된 행 개수(성공 1) 반환
			result = pstmt.executeUpdate();

		} catch (Exception e) {
			System.out.println("MemberDAO의 updateMember메소드에서 update문 실행 오류 : " + e);
			e.printStackTrace();
		}

		return result;
	}

	//================================================================
	// 5. 회원 한 명 삭제 (DELETE, ? 1개)
	//================================================================
	public void delMember(String id) {

		// where 조건 ? 1개짜리 DELETE SQL 준비
		String query = "delete from t_member where id = ?";

		try (Connection con = dataSource.getConnection();
			 PreparedStatement pstmt = con.prepareStatement(query)) {

			// 첫번째 ? 에 삭제할 회원 아이디 채우기
			pstmt.setString(1, id);

			// 완성된 DELETE 실행
			pstmt.executeUpdate();

		} catch (Exception e) {
			System.out.println("MemberDAO의 delMember메소드에서 delete문 실행 오류 : " + e);
			e.printStackTrace();
		}
	}

} // class MemberDAO 끝

/*
    핵심 정리 3줄
    1. try ( 자원 생성 ) { } 소괄호 안 자원은 블록 종료 시 생성의 반대 순서로 자동 close 된다.
    2. ? 가 있는 SELECT는 [생성 -> ?채움 -> 실행] 순서 때문에 rs만 안쪽 try로 따로 연다.
    3. 멤버 변수 con/pstmt/rs, finally, ResourceClose()가 사라져 자원 누수 실수가 원천 차단된다.
*/
