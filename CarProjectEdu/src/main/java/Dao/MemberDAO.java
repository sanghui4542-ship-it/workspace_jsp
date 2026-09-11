package Dao;   // 이 파일이 속한 패키지(=폴더) 이름. 실제 폴더 구조와 반드시 같아야 한다

import java.sql.Connection;   // DB 와 연결된 통로. 이걸 통해 SQL 을 보낸다
import java.sql.PreparedStatement;   // 물음표(?)가 있는 SQL 을 안전하게 실행하는 도구. SQL 인젝션을 막아 준다
import java.sql.ResultSet;   // select 결과를 한 줄씩 읽어 오는 도구
import java.sql.SQLException;   // DB 작업이 실패했을 때 자바가 던지는 예외

import Vo.MemberVO;   // 값을 담아 나르는 상자(VO) 클래스

/*
 ================================================================================
   MemberDAO  -  회원 테이블(member) SQL 실행 담당 클래스  (MVC의 Model 담당, "사원")

 ================================================================================
   [3단계에서 이 클래스를 전면 수정한 이유 - 매우 중요]

   (기존 코드)

       public class MemberDAO {

           Connection con;          // <-- 인스턴스 변수(필드)
           PreparedStatement pstmt; // <-- 인스턴스 변수
           ResultSet rs;            // <-- 인스턴스 변수

           public boolean overlappedId(String id) {
               con = DBCPUtil.getConnection();
               pstmt = con.prepareStatement(sql);
               rs = pstmt.executeQuery();
               ...
               closeResource();
           }
       }

   무엇이 문제인가 : "서블릿은 객체가 하나만 만들어진다"

     톰캣은 서블릿을 요청마다 새로 만들지 않는다. 딱 하나만 만들어 재사용한다.
     그리고 이 프로젝트는 그 하나의 서블릿이 init()에서 Service를,
     Service가 생성자에서 DAO를 딱 하나 만든다.

         MemberController (1개) -> MemberService (1개) -> MemberDAO (1개)

     반면 요청은 여러 개가 "동시에" 들어온다. 각 요청은 별도의 스레드다.
     즉 여러 스레드가 "같은 MemberDAO 객체 하나"를 함께 사용한다.

     그런데 con / pstmt / rs 가 인스턴스 변수라면 그 값도 공유된다.

         [시간 흐름]
         A 회원 요청 : con = 연결1        (자기 연결을 필드에 저장)
         B 회원 요청 : con = 연결2        (A의 값을 덮어씀!)
         A 회원 요청 : rs = pstmt.executeQuery()   (연결2로 실행됨)
         B 회원 요청 : closeResource()             (A가 쓰던 것을 닫아버림)
         A 회원 요청 : rs.next()  ->  "ResultSet closed" 예외

     실제로 나타나는 증상
       - 어쩌다 한 번 "Operation not allowed after ResultSet closed" 예외
       - 다른 사람의 조회 결과가 내 화면에 나오는 사고
       - 개발자 혼자 테스트할 때는 절대 재현되지 않는다 (동시 요청이 없으므로)

     혼자 클릭할 때는 멀쩡한데 수강생 30명이 동시에 접속하면 깨지는,
     찾기 가장 어려운 종류의 버그다.

   (바뀐 방식 - 실무 표준)

     1. con / pstmt / rs 를 전부 "메소드 안의 지역변수"로 내렸다.
        지역변수는 스레드마다 따로 만들어지므로 절대 섞이지 않는다.

     2. Connection 은 Service가 열어서 매개변수로 넘겨준다.
        -> 여러 SQL을 하나의 트랜잭션으로 묶을 수 있게 된다. (util.DBCPUtil 참고)
        -> DAO는 연결을 열거나 닫지 않는다. "빌린 연결로 SQL만 실행"한다.

     3. try-with-resources 로 PreparedStatement / ResultSet 을 자동으로 닫는다.
        기존 코드는 같은 메소드에서 pstmt를 두 번 만들면 첫 번째를 닫지 않아
        statement 가 누수됐다.

     4. 예외를 삼키지 않고 SQLException 을 그대로 던진다.
        기존에는 catch에서 println만 하고 빈 결과를 반환해서
        "DB 오류"가 "결과 없음"으로 보였다.
 ================================================================================
*/
public class MemberDAO {

	/*
	 [주의] 이 클래스에는 인스턴스 변수가 하나도 없다.
	        상태(state)를 갖지 않으므로 여러 스레드가 동시에 사용해도 안전하다.
	        이런 클래스를 "무상태(stateless) 객체"라고 부른다.
	*/

	//===========================================================
	// 1. 아이디 중복 확인
	//    true  -> 이미 사용 중인 아이디 (가입 불가)
	//    false -> 사용 가능한 아이디
	//===========================================================
	public boolean overlappedId(Connection con, String id) throws SQLException {

		String sql = "select count(*) as cnt from member where id=?";   // count(*) 는 조건에 맞는 행이 몇 개인지 세어 준다. as cnt 는 그 결과에 붙인 이름표다

		/*
		 try-with-resources 문법
		   try( 자원 선언 ) { ... }  로 쓰면 블록을 벗어날 때 close()가 자동 호출된다.
		   예외가 발생해도 반드시 닫히므로 finally 에 close 코드를 쓰지 않아도 된다.
		*/
		try (PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setString(1, id);

			try (ResultSet rs = pstmt.executeQuery()) {   // 값을 다 끼운 뒤 실행한다. 결과표도 블록이 끝나면 자동으로 닫힌다
				if (rs.next()) {   // 결과가 한 줄 있으므로 if 로 한 번만 읽는다
					//count(*) 는 조건에 맞는 행의 개수를 돌려준다 (0 또는 1)
					return rs.getInt("cnt") > 0;
				}
			}
		}

		return false;   // 여기까지 왔다 = 결과를 못 읽었다는 뜻이므로 "사용 가능" 으로 본다
	}

	//===========================================================
	// 2. 새 회원 추가
	//    반환 : 추가된 행 수 (성공 1)
	//===========================================================
	public int insertMember(Connection con, MemberVO vo) throws SQLException {

		/*
		   tel / hp 컬럼 포함 (기존 INSERT문에는 없어서 입력한 연락처가 버려졌다)
		   비밀번호는 MemberService에서 이미 PBKDF2로 해시한 값이 넘어온다.
		   가입일시는 DB의 now() 함수로 서버 시간을 넣는다.
		*/
		String sql = "insert into member(id, pass, name, email, reg_date, age, gender, address, tel, hp)"
				   + "            values(?,     ?,    ?,     ?,    now(),   ?,      ?,       ?,   ?,  ?)";

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다

			pstmt.setString(1, vo.getId());   // 1번 ? : 아이디
			pstmt.setString(2, vo.getPass());    //해시된 비밀번호
			pstmt.setString(3, vo.getName());   // 3번 ? : 이름
			pstmt.setString(4, vo.getEmail());   // 4번 ? : 이메일
			pstmt.setInt(5, vo.getAge());   // 5번 ? : 나이 (숫자라서 setInt)
			pstmt.setString(6, vo.getGender());   // 6번 ? : 성별
			pstmt.setString(7, vo.getAddress());   // 7번 ? : 주소
			pstmt.setString(8, vo.getTel());   // 8번 ? : 전화번호
			pstmt.setString(9, vo.getHp());   // 9번 ? : 휴대폰번호

			return pstmt.executeUpdate();   // 실행하고 저장된 행 수를 돌려준다 (성공하면 1)
		}
	}

	//===========================================================
	// 3. 로그인 검증용 : 저장된 비밀번호 조회
	//    반환 : 저장된 비밀번호(해시 또는 예전 평문), 아이디가 없으면 null
	//===========================================================
	/*
	   [2단계에서 SQL 인젝션을 고친 메소드]

	   (기존) "select pass from member where id='" + login_id + "'"
	          -> 아이디 칸에  ' or '1'='1  을 넣으면 조건이 항상 참이 된다.

	   (지금) ? 바인딩. 입력값은 "값"으로만 전달되어 SQL 문법으로 해석되지 않는다.

	   비밀번호 "비교"는 여기서 하지 않는다.
	   해시는 같은 비밀번호라도 매번 값이 달라 equals 비교가 불가능하므로
	   MemberService가 java.util.Objects.equals() 로 판단한다.
	*/
	public String findPasswordById(Connection con, String id) throws SQLException {

		String sql = "select pass from member where id=?";   // 아이디로 저장된 비밀번호 한 칸만 꺼내 온다

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다

			pstmt.setString(1, id);   // 1번 ? : 찾을 아이디

			try (ResultSet rs = pstmt.executeQuery()) {   // 실행하고 결과표를 받는다
				if (rs.next()) {   // 아이디는 기본키라 결과는 0건 아니면 1건이다
					return rs.getString("pass");   // 저장된 비밀번호(해시)를 그대로 돌려준다. 비교는 Service 가 한다
				}
			}
		}

		return null; //존재하지 않는 아이디
	}

	//===========================================================
	// 4. 비밀번호만 교체 (해시 자동 이관 / 비밀번호 변경)
	//===========================================================
	public int updatePassword(Connection con, String id, String encodedPassword) throws SQLException {

		String sql = "update member set pass=? where id=?";   // 비밀번호 한 칸만 바꾸는 update 문

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다
			pstmt.setString(1, encodedPassword);   // 1번 ? : 새로 만든 해시 값
			pstmt.setString(2, id);   // 2번 ? : 어느 회원인지
			return pstmt.executeUpdate();   // 바뀐 행 수를 돌려준다
		}
	}

	//===========================================================
	// 5. 글쓰기 화면용 : 로그인한 회원의 이메일/이름/아이디 조회
	//===========================================================
	public MemberVO memberOne(Connection con, String memberid) throws SQLException {

		//필요한 컬럼만 조회한다 (select * 는 불필요한 데이터까지 가져와 느려진다)
		String sql = "select email, name, id from member where id=?";

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다

			pstmt.setString(1, memberid);   // 1번 ? : 로그인한 회원의 아이디

			try (ResultSet rs = pstmt.executeQuery()) {   // 실행하고 결과표를 받는다
				if (rs.next()) {   // 결과가 있으면(=그런 회원이 있으면)
					MemberVO vo = new MemberVO();   // 값을 담을 빈 상자를 만든다
					vo.setEmail(rs.getString("email"));   // 이메일 칸을 채운다
					vo.setName(rs.getString("name"));   // 이름 칸을 채운다
					vo.setId(rs.getString("id"));   // 아이디 칸을 채운다
					return vo;   // 채워진 상자를 돌려준다
				}
			}
		}

		return null;   // 그런 아이디의 회원이 없다는 뜻
	}

	//===========================================================
	// 6. 회원정보 수정 화면용 : 회원 전체 정보 조회
	//===========================================================
	public MemberVO memberDetail(Connection con, String id) throws SQLException {

		/*
		 [변경] pass 컬럼을 조회하지 않는다.

		   기존에는 비밀번호까지 조회해 화면의 hidden 필드에 심어두었다.
		   화면에서 "소스 보기"만 해도 비밀번호가 보였고,
		   해시로 저장한 뒤에는 그 값을 되돌려받아 이중 해시가 되는 문제도 있었다.
		   화면은 비밀번호를 알 필요가 없다.
		*/
		String sql = "select id, name, email, age, gender, address, tel, hp from member where id=?";

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다

			pstmt.setString(1, id);   // 1번 ? : 조회할 회원 아이디

			try (ResultSet rs = pstmt.executeQuery()) {   // 실행하고 결과표를 받는다
				if (rs.next()) {   // 그 회원이 있으면
					MemberVO vo = new MemberVO();   // 값을 담을 빈 상자를 만든다
					vo.setId(rs.getString("id"));   // 아이디
					vo.setName(rs.getString("name"));   // 이름
					vo.setEmail(rs.getString("email"));   // 이메일
					vo.setAge(rs.getInt("age"));   // 나이 (숫자라서 getInt)
					vo.setGender(rs.getString("gender"));   // 성별
					vo.setAddress(rs.getString("address"));   // 주소
					vo.setTel(rs.getString("tel"));   // 전화번호
					vo.setHp(rs.getString("hp"));   // 휴대폰번호
					return vo;   // 여덟 칸을 다 채운 상자를 돌려준다
				}
			}
		}

		return null;   // 그런 아이디의 회원이 없다는 뜻
	}

	//===========================================================
	// 7. 회원 정보 수정
	//    vo.getPass() 가 null 이면 비밀번호는 수정하지 않는다
	//===========================================================
	public int updateMember(Connection con, MemberVO vo) throws SQLException {

		//비밀번호를 함께 바꿀 것인지에 따라 SQL이 달라진다
		boolean changePassword = (vo.getPass() != null && !vo.getPass().isEmpty());

		String sql;   // 실행할 SQL 을 담을 변수. 아래 조건에 따라 둘 중 하나가 들어간다
		if (changePassword) {   // 비밀번호도 함께 바꾸는 경우
			sql = "update member set pass=?, name=?, age=?, gender=?, address=?, email=? where id=?";   // pass 까지 포함해 6칸을 수정한다
		} else {
			sql = "update member set name=?, age=?, gender=?, address=?, email=? where id=?";   // 비밀번호는 그대로 두고 5칸만 수정한다
		}

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // 위에서 정해진 SQL 로 실행 도구를 만든다

			/*
			 ? 순서가 두 경우에 따라 달라지므로 번호를 직접 적지 않고 변수로 센다.
			 (숫자를 직접 적으면 SQL을 고칠 때 순서가 어긋나는 실수가 자주 난다)
			*/
			int idx = 1;
			if (changePassword) {   // 비밀번호를 바꿀 때만 pass 자리를 채운다
				pstmt.setString(idx++, vo.getPass()); //이미 해시된 값
			}
			pstmt.setString(idx++, vo.getName());   // 다음 ? 에 이름
			pstmt.setInt(idx++, vo.getAge());   // 다음 ? 에 나이
			pstmt.setString(idx++, vo.getGender());   // 다음 ? 에 성별
			pstmt.setString(idx++, vo.getAddress());   // 다음 ? 에 주소
			pstmt.setString(idx++, vo.getEmail());   // 다음 ? 에 이메일
			pstmt.setString(idx++, vo.getId());      //수정 조건 : 회원 아이디

			return pstmt.executeUpdate();   // 바뀐 행 수를 돌려준다 (1이면 수정 성공)
		}
	}

	//===========================================================
	// 8. 회원 삭제 (탈퇴)
	//===========================================================
	public int deleteMember(Connection con, String id) throws SQLException {

		String sql = "delete from member where id=?";   // delete 는 where 를 빠뜨리면 회원 전체가 지워진다. 조건을 반드시 붙인다

		try (PreparedStatement pstmt = con.prepareStatement(sql)) {   // SQL 실행 도구를 만든다
			pstmt.setString(1, id);   // 1번 ? : 탈퇴할 회원 아이디
			return pstmt.executeUpdate();   // 지워진 행 수를 돌려준다 (1이면 성공)
		}
	}

}//MemberDAO 클래스
