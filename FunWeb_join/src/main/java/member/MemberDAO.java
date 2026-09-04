package member;

// DB 작업에 필요한 java.sql 도구 3종 import (Connection, PreparedStatement, ResultSet)
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// 커넥션풀을 찾을 때 필요한 javax.naming, javax.sql 도구 import
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/*
================================================================
 [실습 2] MemberDAO 에 회원가입 기능 추가하기

 완성 목표 2가지
   1. isDuplicated() : 아이디가 이미 있는지 검사 (중복 확인)
   2. insertMember() : t_member 테이블에 회원 정보 저장 (INSERT)

 ** 주석 아래 빈 줄에 코드를 직접 작성한다. **
 ** 이미 작성된 isExisted() 를 그대로 참고하면 된다. **
================================================================
*/
public class MemberDAO {

	// 커넥션풀에서 연결을 꺼내 줄 DataSource 변수 dataFactory 선언
	private DataSource dataFactory;

	// 생성자 : 톰캣에 등록된 커넥션풀(jdbc/jspdb)을 찾아 dataFactory 에 저장
	public MemberDAO() {
		try {
			// JNDI 저장소에 접근하는 통로 열기
			Context ctx = new InitialContext();

			// 웹앱 자원 구역 "java:/comp/env" 로 이동
			Context envContext = (Context) ctx.lookup("java:/comp/env");

			// 그 안에서 "jdbc/jspdb" 이름의 커넥션풀 찾아 DataSource 로 형변환
			dataFactory = (DataSource) envContext.lookup("jdbc/jspdb");

		} catch (Exception e) {
			// 오류 원인을 콘솔에 출력
			e.printStackTrace();
		}
	}//생성자

	/*==============================================================
	  isExisted() : 로그인 검증 (완성된 예제 - 아래 실습의 참고용)
	==============================================================*/
	public boolean isExisted(MemberVO memberVO) {

		// 결과를 담을 boolean 변수 result 를 false 로 초기화
		boolean result = false;

		// VO 상자에서 id, pwd 꺼내기
		String id  = memberVO.getId();
		String pwd = memberVO.getPwd();

		// 아이디와 비밀번호가 모두 일치하는 행의 개수를 세는 SQL 작성
		String query = "select case when count(*) = 1 then 'true' else 'false' end as result "
		             + "from t_member "
		             + "where id=? and pwd=?";

		// try-with-resources 로 연결과 실행객체 열기 (블록 끝나면 자동 반납)
		try (Connection con = dataFactory.getConnection();
		     PreparedStatement pstmt = con.prepareStatement(query)) {

			// 1번 ? 에 id, 2번 ? 에 pwd 채우기
			pstmt.setString(1, id);
			pstmt.setString(2, pwd);

			// 조회 실행해서 결과표 받기
			try (ResultSet rs = pstmt.executeQuery()) {

				// 결과표의 첫 행으로 이동
				rs.next();

				// result 열의 값을 boolean 으로 꺼내 저장
				result = rs.getBoolean("result");
			}

		} catch (Exception e) {
			// 오류 원인을 콘솔에 출력
			e.printStackTrace();
		}

		// 판정 결과 반환
		return result;

	}//isExisted

	/*==============================================================
	  [실습 2-1] isDuplicated() : 아이디 중복 검사
	  - 매개변수 : String id
	  - 반환값   : true = 이미 있는 아이디 / false = 사용 가능
	==============================================================*/
	// 아이디 중복 여부를 반환하는 public boolean isDuplicated(String id) 메소드 선언
	public boolean isDuplicated(String id) {

		// 결과를 담을 boolean 변수 result 를 false 로 초기화
		boolean result = false;

		// 아이디가 일치하는 행의 개수를 세는 SQL 작성 (조건은 id 하나뿐)
		String query = "select case when count(*) = 1 then 'true' else 'false' end as result "
		             + "from t_member "
		             + "where id=?";

		// try-with-resources 로 연결과 실행객체 열기
		try (Connection con = dataFactory.getConnection();
		     PreparedStatement pstmt = con.prepareStatement(query)) {

			// 1번 ? 에 id 채우기
			pstmt.setString(1, id);

			// 조회 실행해서 결과표 받기
			try (ResultSet rs = pstmt.executeQuery()) {

				// 결과표의 첫 행으로 이동
				rs.next();

				// result 열의 값을 boolean 으로 꺼내 저장
				result = rs.getBoolean("result");
			}

		// 오류가 나면 콘솔에 출력
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 아이디 중복 여부 true(가입 아이디가 DB에 존재) 또는 false(가입 아이디가 DB에 미존재)를  MemberService 부장에게 반환
		return result;

	}//isDuplicated


	/*==============================================================
	  [실습 2-2] insertMember() : 회원 정보 저장 (INSERT)
	  - 매개변수 : MemberVO memberVO (id, pwd, name, email 이 담긴 상자)
	  - 반환값   : true = 저장 성공 / false = 실패
	==============================================================*/
	// 회원 저장 결과를 반환하는 public boolean insertMember(MemberVO memberVO) 메소드 선언
	public boolean insertMember(MemberVO memberVO) {

		// 결과를 담을 boolean 변수 result 를 false 로 초기화
		boolean result = false;

		// VO 상자에서 id, pwd, name, email 네 개 꺼내기
		String id  = memberVO.getId(); 
		String pwd = memberVO.getPwd();
		String name = memberVO.getName();
		String email = memberVO.getEmail();

		// t_member 에 id, pwd, name 을 저장하는 INSERT SQL 작성 (값은 ? 세 개)
		// 예) insert into t_member (id, pwd, name) values (?, ?, ?)
		String query = "insert into t_member (id, pwd, name) values (? ,?, ?)";
				
		// try-with-resources 로 연결과 실행객체 열기
		try(Connection con =  dataFactory.getConnection();    
			PreparedStatement pstmt = con.prepareStatement(query)){

			// 1번 ? 에 id, 2번 ? 에 pwd, 3번 ? 에 name 채우기
			pstmt.setString(1, id);  pstmt.setString(2, pwd);  pstmt.setString(3, name);

			// executeUpdate() 로 실행하고 처리된 행 개수를 int 변수에 저장
			// ** 조회는 executeQuery, 입력/수정/삭제는 executeUpdate **
			int count = pstmt.executeUpdate();  //완성된 전체 "insert ........" 문장 DB로 전송해서 실행!

			// 처리된 행이 1개면 result 를 true 로 변경
			if(count == 1) {
				result = true;
			}
					
		// 오류가 나면 콘솔에 출력
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		// 저장(회원 추가,가입) 결과 반환
		return result;   //회원가입 성공시   MemberDAO.java ---- true 반환 ----> MemberService.java의 join 메소드 내부로 
	}

	//idCheck메소드 기능
	//- 회원가입 전! 입력한 아이디가 DB에 저장되어 있는지 없는지 유무 체크 하는 기능 
	//- 설명 : 회원가입을 위해 입력한 아이디를 매개변수 String id로 전달 받아
	//		 DB의 테이블에 저장되어 있는지 유무를 검사하는 메소드 입니다.
	//		 만약 입력한 아이디가 DB의 테이블에 저장되어 있으면? 1을 check변수에 저장하고 반환하며,
	//	     만약 입력한 아이디가 DB의 테이블에 저장되어 있지 않으면? 0을 check변수에 저장하여 반환 시킵니다. 
	public int idCheck(String id) {
		
		int check = 0;
		
		//아이디 중복 검사를 위해 사용자가 입력한 아이디에 해당하는 회원레코드 조회 하는 select문 만들기 
		String sql = "select * from t_member where id='"+ id + "'";
		
		try(Connection con = dataFactory.getConnection();
			PreparedStatement pstmt = con.prepareStatement(sql)) {
			
			//조회 실행해서 결과 ReusltSet으로 받기
			try(ResultSet rs  = pstmt.executeQuery()){
				
				//입력한 아이디에 해당하는 회원레코드 한행이 조회 되면?(아이디 중복)
				if(rs.next()) {
					check = 1;
				}else { //입력한 아이디에 해당하는 회원레코드 한행이 조회되지 않으면?(가입가능한 아이디)
					check = 0;
				}
			}		
		} catch (Exception e) {
			e.printStackTrace();
		} 
		//입력한 아이디가 가입가능한지 불가능한지 유무 1(아이디 중복) 또는 0(가입가능한 아이디) 부장(MemberService클래스)으로 반환 
		return  check;
	}	

}//MemberDAO (사원)







