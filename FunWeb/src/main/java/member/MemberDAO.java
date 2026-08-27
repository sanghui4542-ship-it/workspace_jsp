package member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/*
================================================================
MemberDAO : 데이터베이스 접근 "전담" 클래스   [MVC 에서의 위치 : Model]
(DAO = Data Access Object : 데이터 접근 객체)

[역할]
MySQL 8 의 t_member 테이블에 SQL 을 실행하는 일만 담당한다.
- 화면(HTML)을 모른다.
- 로그인 성공 후 무엇을 할지도 모른다.
- 오직 "DB에 물어보고 결과를 돌려주는 일"만 한다.
-> 이렇게 일을 쪼개 두면 DB가 오라클로 바뀌어도
   이 파일만 고치면 되고, 나머지 계층은 손대지 않는다.

[호출 관계]
MemberService --(MemberVO 전달)--> MemberDAO --(SQL)--> MySQL 8
================================================================
*/
public class MemberDAO { //사원
	
	/* 커넥션풀(DataSource객체 공간)에서  DB 연결통로(Connection객체 공간)을 꺼내오기 위한 커넥션풀 변수*/
	private DataSource dataSource;
	
	/*==============================================================
	  생성자 : 커넥션풀을 찾아 준비하는 생성자

	  호출 시점 : MemberService 가 new MemberDAO() 를 실행할 때
	  하는 일   : 톰캣에 등록된 커넥션풀을 이름으로 찾아 변수에 저장
	  끝난 뒤   : isExisted() 에서 연결을 빌릴 수 있는 상태가 된다
	==============================================================*/
	public MemberDAO() {			
		try {
			/*톰캣의 자원 등록 명단(JNDI 저장소)에 접근하는 객체 통로 생성*/
			Context  ctx = new InitialContext();
			
			/* 웹 애플리케이션 전용 등록 구역으로 이동한다.

			   ** 왜 2단계로 나눠 찾는가? **
			   톰캣의 등록 명단은 폴더처럼 층이 나뉘어 있다.
			     java:/comp/env  <- 웹앱 자원이 모여 있는 고정 경로
			        + jdbc/jspdb    <- 우리가 등록한 커넥션풀
			   먼저 그 폴더로 들어간 뒤(이 줄),
			   그 안에서 이름을 찾는(다음 줄) 순서다.
			   ** "java:/comp/env" 는 규격으로 정해진 이름이라
			      바꿀 수 없고, 그대로 적어야 한다. ** */
			Context envContext = (Context)ctx.lookup("java:/comp/env");
			
			/* META-INF/context.xml 에 name="jdbc/jspdb" 로 등록해 둔
			   MySQL 8 커넥션풀을 찾아 공급 객체로 받는다.

			   (DataSource) : lookup 의 반환 타입이 Object 이므로
			                  원래 타입으로 되돌리는 형변환이 필요하다.

			   ** 이 이름은 context.xml 의 name 속성과 반드시 같아야 한다.
			      한 글자라도 다르면 NameNotFoundException 이 발생한다. ** */
			dataSource  = (DataSource)envContext.lookup("jdbc/jspdb");
			
		} catch (Exception e) {
			/* 오류 내용을 서버 콘솔에 출력한다.
			   자주 만나는 원인 3가지 :
			     1. context.xml 의 name 과 철자가 다름
			     2. context.xml 파일이 META-INF 폴더에 없음
			     3. WEB-INF/lib 에 커넥션풀 라이브러리(dbcp)가 없음 */
			e.printStackTrace();
		}	
	} 
	
	/*==============================================================
	  isExisted() : VO 에 담긴 아이디/비밀번호의 회원이 있으면 true

	  호출 시점 : MemberService 의 login() 이 호출할 때
	  매개변수  : memberVO = 로그인 요청시 입력한 아이디와 비밀번호가 담긴 상자
	  하는 일   :   1) 상자에서 값 꺼내기     2) 연결 빌리기
	              3) SQL 의 ? 채우기      4) 조회 실행
	              5) 결과 읽기            6) 연결 반납(자동)
	  반환값    :   true  = 회원 존재 (로그인 성공)
	              false = 없음 또는 오류 (로그인 실패)
	==============================================================*/
	public boolean isExisted(MemberVO  memberVO) {
		
		/* 조회 결과를 담을 변수.
		   ** 실패(false)를 기본값으로 두는 이유 **
		   중간에 오류가 나서 조회를 못 마쳐도 false 가 반환된다.
		   "확인되지 않으면 로그인 불가"가 안전한 기본 태도다.
		   반대로 true 로 두면 오류 시 아무나 로그인되는 사고가 난다. */
		boolean result = false;
		
		/* MemberVO 상자에서 로그인 요청시 입력한 아이디와 비밀번호를 꺼낸다.
		   변수가 private 이라 getter 메소드로만 꺼낼 수 있다. */
		String id = memberVO.getId();    String pwd = memberVO.getPwd();
		
		/* 로그인 요청시 입력한 아이디와 비밀번호가 "둘 다" 일치하는 조회한 행이 1개이면? 'true' 조회한 행이 0개이면 'false' 조회하는 SQL문 */
		String query = "select case when count(*) = 1 then 'true' else 'false' end as result "
					 + "from t_member "
					 + "where id=? and pwd=?";
		
		/* 로그인 검증 SQL 을 문자열로 조립한다.

		   [SQL 을 4단계로 분해하면]
		   (1) where id=? and pwd=?
		       -> 로그인 요청시 입력한 아이디와 비밀번호가 "둘 다" 일치하는 행을 조회 해서 찾는다.
		          and 이므로 하나만 맞아서는 찾아지지 않는다.
		   (2) count(*)
		       -> 찾은 조회 행이 몇 개인지 센다.
		          id 가 기본키(PRIMARY KEY)라 중복이 없으므로
		          결과는 반드시 0 또는 1 이다.
		   (3) case when count(*) = 1 then 'true' else 'false' end
		       -> 개수가 1 이면 글자 'true', 아니면 글자 'false'.
		          자바의 if ~ else 와 같은 역할을 SQL 안에서 한다.
		   (4) as result
		       -> 그 값이 담길 열의 이름을 result 로 정한다.
		          아래 rs.getBoolean("result") 가 이 이름을 사용한다.

		   [실제로 돌아오는 결과표의 모습]
		       result
		       ------
		       true      <- 딱 1행 1열짜리 표

		   ** 왜 이렇게 쓰는가? **
		   회원 정보를 통째로 가져와 자바에서 비교할 수도 있지만,
		   그러면 비밀번호가 서버 메모리로 이동한다.
		   판정을 DB 에게 시키고 결과만 받는 편이 안전하고 빠르다.

		   ** case when ~ end 는 표준 SQL 이라
		      MySQL 8 과 오라클에서 똑같이 동작한다.
		      (오라클 전용 decode 를 쓰면 MySQL 에서는 오류가 난다) */

		/* try-with-resources 문법 :
		   ( ) 안에서 만든 자원은 블록이 끝날 때 자동으로 닫힌다.

		   ** 자동으로 닫히는 것이 왜 중요한가? **
		   빌린 연결을 반납하지 않으면 풀에서 연결이 계속 줄어든다.
		   20개를 다 쓰고 반납이 없으면 그 다음 사용자는
		   연결을 못 받아 사이트 전체가 멈춘다(커넥션 누수).
		   예전에는 finally 블록에서 close() 를 일일이 적었지만,
		   이 문법을 쓰면 오류가 나도 반드시 반납된다. */		
		try(Connection con = dataSource.getConnection();
			PreparedStatement pstmt = con.prepareStatement(query)){
			
			pstmt.setString(1, id); 	/* 1번 ?  자리에 입력한 아이디로 채운다. */
			pstmt.setString(2, pwd);	/* 2번 ?  자리에 입려한 비밀번호로 채운다.*/
			
			/* executeQuery() : 조회(select)를 실행하고 결과표 ResultSet객체 공간을 받는다.
								결과표  ResultSet객체 공간도 자원이므로 역시 자동으로 닫히게 감쌌다. */
			try(ResultSet rs  = pstmt.executeQuery() ){
								
				/* 결과표 ResultSet객체 공간의 첫 번째 행으로 이동한다.
				   ** ResultSet 은 처음에 커서가 "첫 행의 바로 앞"을 가리킨다.
				      next() 를 한 번 불러야 첫 행을 읽을 수 있다. **  */
				rs.next();
				
				/* result 별칭 열의 조회 값을 꺼낸다.
				   ** DB 가 준 것은 글자 'true' / 'false' 인데
				      getBoolean() 이 boolean true / false 로 자동 변환해 준다. **
				   "result" 는 위 SQL 의 as result 에서 정한 이름이다.
				   철자가 다르면 열을 못 찾아 오류가 난다. */
				result = rs.getBoolean("result");
				
			}
			/* 여기서 ResultSet 이 자동으로 닫힌다. */
			
			
		}catch (Exception e) {
			/* 오류 내용을 콘솔에 출력한다. result 는 false 로 반환된다.
			   자주 만나는 원인 3가지 :
			     1. MySQL 서버가 꺼져 있음
			     2. t_member 테이블이 없거나 열 이름이 다름
			     3. context.xml 의 계정/비밀번호가 틀림 */			
			e.printStackTrace();
		}
		/* 여기서 Connection 과 PreparedStatement 가 자동으로 닫히고,
		   빌렸던 연결이 커넥션풀로 반납된다. */
		
		/* 판정 결과를 호출한 곳(MemberService)에 돌려준다.
		   -> MemberService 를 거쳐 MemberController 까지 되돌아간다. */
		return result;
			
	} //isExisted 메소드 
	
}
















