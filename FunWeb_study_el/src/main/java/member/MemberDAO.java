package member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
/* DB 작업에 쓰는 표준 도구 3종 (JDBC)
     Connection        : DB 와 연결된 통로
     PreparedStatement : 물음표가 있는 SQL 을 실행 준비 상태로 만든 것
     ResultSet         : 조회 결과가 담겨 돌아온 표
   ** 이 셋은 자바 표준이라 MySQL 이든 오라클이든 이름이 같다.
      DB 를 바꿔도 import 는 그대로다. ** */

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
/* 톰캣이 미리 만들어 둔 DB 연결 묶음을 찾아오는 도구
     Context / InitialContext : 톰캣의 자원 목록을 뒤지는 도구
     DataSource               : 연결을 꺼내 주는 공급 담당 */

/*
================================================================
 [복습 2] MemberDAO.java   (DAO = Data Access Object)

 ── 이 파일이 하는 일 ──────────────────────────────────────
 t_member 테이블에 SQL 을 실행하는 일만 담당한다.

 ── 이 파일이 모르는 것 (이게 더 중요하다) ─────────────────
   화면을 모른다     -> out.print 가 한 줄도 없다
   업무 순서를 모른다 -> "중복이면 저장 안 함" 같은 판단이 없다
   ** 직접 확인해 보자. 이 파일에 if 문은 몇 개나 있는가?
      결과를 읽을 때 쓰는 것 말고는 거의 없다. **

 ── 왜 이렇게 쪼개는가 ─────────────────────────────────────
   DB 를 오라클로 바꾼다면?  -> 이 파일의 SQL 만 고치면 된다
   화면을 전부 바꾼다면?     -> 이 파일은 손대지 않는다

 ── 커넥션풀이란 ───────────────────────────────────────────
 DB 연결을 만드는 일은 시간이 오래 걸린다.
 요청마다 새로 만들면 사이트가 매우 느려진다.
 그래서 톰캣이 시작할 때 연결을 여러 개 미리 만들어 두고
 필요할 때 빌려주고 돌려받는다. 이 묶음이 커넥션풀이다.
   설정 위치 : META-INF/context.xml 의 maxTotal="20"
   -> 최대 20개까지 만들어 돌려쓴다는 뜻이다.

 ── 모든 메소드의 공통 8단계 ───────────────────────────────
 아래 9개 메소드가 전부 이 순서를 따른다. 하나만 익히면 된다.

   1) 결과를 담을 변수를 준비한다 (실패값으로 초기화)
   2) SQL 문자열을 만든다 (값 자리는 물음표로 비워 둔다)
   3) try-with-resources 로 연결과 실행객체를 연다
   4) 물음표 자리에 값을 채운다 (setString, 번호는 1부터)
   5) 실행한다 (조회는 executeQuery / 그 외는 executeUpdate)
   6) 결과를 읽어 변수에 담는다
   7) 예외가 나면 콘솔에 출력한다
   8) 결과를 반환한다

 ── 메소드 9개 한눈에 ──────────────────────────────────────
   isExisted     select  로그인 검증
   isDuplicated  select  아이디 중복 검사
   selectMember  select  회원 1명 조회
   findId        select  아이디 찾기
   findPwd       select  비밀번호 찾기
   insertMember  insert  회원 저장
   updateMember  update  회원 수정
   deleteMember  delete  회원 삭제
   생성자                커넥션풀 준비
================================================================
*/
public class MemberDAO {

	private DataSource dataFactory;
	/* 커넥션풀에서 연결을 꺼내 주는 공급 객체를 담아 둘 변수.
	   생성자에서 한 번 채워 두고 모든 메소드가 함께 쓴다. */

	/*==============================================================
	  생성자 : 톰캣에 등록된 커넥션풀을 찾아 준비한다

	  [찾는 순서 - 폴더를 두 번 들어가는 것과 같다]
	    java:/comp/env          <- 웹앱 자원이 모이는 고정 경로
	       └ jdbc/jspdb         <- 우리가 등록한 커넥션풀

	  [이름 "jdbc/jspdb" 가 적힌 곳 3군데 - 전부 같아야 한다]
	    1) WebContent/META-INF/context.xml
	         <Resource name="jdbc/jspdb" ... />
	    2) WebContent/WEB-INF/web.xml
	         <res-ref-name>jdbc/jspdb</res-ref-name>   (생략 가능)
	    3) 이 파일의 lookup("jdbc/jspdb")
	  ** 한 글자라도 다르면 NameNotFoundException 이 발생한다. **

	  [이름표와 실제 DB 이름은 다른 것이다]
	    jdbc/jspdb  = 톰캣에 붙인 이름표 (마음대로 정해도 됨)
	    .../jspdb   = context.xml url 끝의 실제 DB 이름
	  글자가 같을 뿐 서로 다른 것이니 혼동하지 말자.
	==============================================================*/
	public MemberDAO() {

		try {
		/* JNDI 조회는 실패할 수 있어 try ~ catch 로 감싼다. */

			Context ctx = new InitialContext();
			/* 톰캣의 자원 목록(JNDI 저장소)에 접근하는 통로를 연다.

			   ** JNDI 를 쓰는 이유 **
			   DB 주소와 비밀번호를 자바 코드에 직접 쓰지 않고
			   "jdbc/jspdb 라는 이름의 것을 주세요" 라고만 요청한다.
			   -> 비밀번호가 소스에 노출되지 않고
			      DB 주소가 바뀌어도 설정 파일만 고치면 된다. */

			Context envContext = (Context) ctx.lookup("java:/comp/env");
			/* 웹 애플리케이션 전용 구역으로 먼저 들어간다.

			   경로 이름의 뜻
			     java: = JNDI 전용 주소 표시 (http: 같은 구분자)
			     comp  = component (이 웹 애플리케이션 자신)
			     env   = environment (설정해 준 자원들이 놓이는 자리)

			   ** "java:/comp/env" 는 규격으로 정해진 이름이라
			      바꿀 수 없고 그대로 적어야 한다. ** */

			dataFactory = (DataSource) envContext.lookup("jdbc/jspdb");
			/* 그 구역 안에서 커넥션풀을 이름으로 찾아 받는다.

			   (DataSource) : lookup 의 반환 타입이 Object 이므로
			                  원래 타입으로 되돌리는 형변환이 필요하다. */

		} catch (Exception e) {

			e.printStackTrace();
			/* 오류 내용을 서버 콘솔(이클립스 Console 창)에 출력한다.

			   [자주 만나는 원인 3가지]
			     1. context.xml 의 name 과 lookup 이름의 철자가 다름
			     2. context.xml 이 META-INF 폴더에 없음
			     3. WEB-INF/lib 에 커넥션풀 라이브러리(dbcp)가 없음 */

		}

	}//생성자

	/*==============================================================
	  isExisted() : 로그인 검증
	  - 매개변수 : 아이디와 비밀번호가 담긴 상자
	  - 반환값   : true = 회원 있음 / false = 없음

	  [SQL 을 4단계로 뜯어보기]
	    (1) where id=? and pwd=?
	        두 값이 "모두" 일치하는 행을 찾는다.
	        and 이므로 하나만 맞아서는 찾아지지 않는다.
	        ** or 로 쓰면 아이디만 맞아도 통과해 비밀번호가 무의미해진다 **
	    (2) count(*)
	        찾은 행이 몇 개인지 센다.
	        id 가 기본키라 중복이 없으므로 결과는 0 또는 1 이다.
	    (3) case when count(*) = 1 then 'true' else 'false' end
	        SQL 안에서 쓰는 if ~ else 문이다.
	        자바로 바꾸면 : if(개수==1) "true"; else "false";
	    (4) as result
	        만들어진 값이 담길 열의 이름을 result 로 정한다.
	        아래 rs.getBoolean("result") 가 이 이름을 사용한다.

	  [돌아오는 결과표의 모습]
	      result
	      ------
	      true        <- 딱 1행 1열짜리 표

	  ** 회원이 없어도 행이 0개가 아니라 'false' 가 담긴 1행이 온다.
	     count 는 셀 것이 없어도 0 이라는 숫자를 만들기 때문이다.
	     그래서 rs.next() 를 if 없이 한 번만 불러도 안전하다. **
	==============================================================*/
	public boolean isExisted(MemberVO memberVO) {

		boolean result = false;
		/* [1단계] 결과 변수를 실패값(false)으로 초기화한다.

		   ** 왜 false 로 시작하는가 **
		   중간에 오류가 나서 조회를 못 마쳐도 false 가 반환된다.
		   "확인되지 않으면 로그인 불가" 가 안전한 태도다.
		   true 로 두면 오류 시 아무나 로그인되는 사고가 난다. */

		String id = memberVO.getId();
		String pwd = memberVO.getPwd();
		/* 상자에서 값을 꺼낸다. private 변수라 getter 로만 꺼낼 수 있다. */

		String query = "select case when count(*) = 1 then 'true' else 'false' end as result "
		             + "from t_member "
		             + "where id=? and pwd=?";
		/* [2단계] SQL 문자열을 만든다.

		   ** 조각 끝의 공백에 주의 **
		     "... as result "   <- 끝에 공백 있음 (O)
		   + "from t_member "   <- 끝에 공백 있음 (O)
		   공백을 빼먹으면 "as resultfrom t_member" 처럼 붙어 오류가 난다.

		   ** case when ~ end 는 표준 SQL 이라
		      MySQL 8 과 오라클에서 똑같이 동작한다.
		      오라클 전용 decode 를 쓰면 MySQL 에서 오류가 난다. ** */

		try (Connection con = dataFactory.getConnection();
		     PreparedStatement pstmt = con.prepareStatement(query)) {
		/* [3단계] try-with-resources 문법.
		   괄호 안에서 만든 자원은 블록이 끝나면 자동으로 닫힌다.

		     getConnection()      : 커넥션풀에서 연결 1개를 빌린다
		     prepareStatement(sql): SQL 을 실행 준비 상태로 만든다

		   ** 자동으로 닫히는 것이 왜 중요한가 **
		   빌린 연결을 반납하지 않으면 풀의 연결이 계속 줄어든다.
		   20개를 다 쓰고 반납이 없으면 그 다음 사용자는
		   연결을 못 받아 사이트 전체가 멈춘다. (커넥션 누수)
		   예전에는 finally 에서 close() 를 일일이 적었지만
		   이 문법을 쓰면 오류가 나도 반드시 반납된다. */

			pstmt.setString(1, id);
			/* [4단계] 1번 물음표 자리에 아이디를 채운다.
			   ** 번호는 0 이 아니라 1 부터 시작한다. 배열과 다르다. ** */

			pstmt.setString(2, pwd);
			/* 2번 물음표 자리에 비밀번호를 채운다.

			   ** 물음표를 쓰는 진짜 이유 (매우 중요) **
			   글자를 이어붙여 SQL 을 만들면
			     "... where id='" + id + "'"
			   사용자가 아이디 칸에 특수한 글자를 넣어
			   SQL 의 의미 자체를 바꿀 수 있다.
			   -> 비밀번호를 몰라도 로그인되는 사고가 발생한다.
			      (SQL 삽입 공격, SQL Injection)
			   setString 은 입력값을 "값" 으로만 취급하므로
			   무엇을 넣어도 SQL 명령으로 해석되지 않는다.
			   -> 실무에서 글자 이어붙이기는 금지된다. */

			try (ResultSet rs = pstmt.executeQuery()) {
			/* [5단계] 조회를 실행하고 결과표를 받는다.
			   조회는 executeQuery, 입력/수정/삭제는 executeUpdate.
			   결과표도 자원이므로 역시 자동으로 닫히게 감쌌다. */

				rs.next();
				/* [6단계] 결과표의 첫 행으로 이동한다.

				   ** ResultSet 은 처음에 "첫 행의 바로 앞" 을 가리킨다.
				      next() 를 한 번 불러야 첫 행을 읽을 수 있다. **
				   count 조회는 행이 무조건 1개이므로
				   if 없이 한 번만 불러도 안전하다.
				   (여러 행을 읽을 때는 while(rs.next()) 를 쓴다) */

				result = rs.getBoolean("result");
				/* result 열의 값을 꺼낸다.
				   DB 가 준 것은 글자 'true'/'false' 인데
				   getBoolean() 이 boolean 으로 자동 변환해 준다.
				   "result" 는 SQL 의 as result 에서 정한 이름이다.
				   철자가 다르면 열을 못 찾아 오류가 난다. */

			}

		} catch (Exception e) {
			e.printStackTrace();
			/* [7단계] 오류를 콘솔에 출력한다. result 는 false 로 남는다.

			   [자주 만나는 원인 3가지]
			     1. MySQL 서버가 꺼져 있음
			     2. t_member 테이블이 없거나 열 이름이 다름
			     3. context.xml 의 계정/비밀번호가 틀림 */
		}
		/* 여기서 Connection 과 PreparedStatement 가 자동으로 닫히고
		   빌렸던 연결이 커넥션풀로 반납된다. */

		return result;
		/* [8단계] 판정 결과를 MemberService 에게 돌려준다. */

	}//isExisted

	/*==============================================================
	  isDuplicated() : 아이디 중복 검사
	  - 매개변수 : 검사할 아이디
	  - 반환값   : true = 이미 있음 / false = 사용 가능

	  ** isExisted 와 거의 같다. 조건이 id 하나뿐인 것만 다르다. **
	  같은 8단계를 따르므로 위 설명을 그대로 적용하면 된다.
	==============================================================*/
	public boolean isDuplicated(String id) {

		boolean result = false;
		/* [1단계] 실패값으로 초기화 */

		String query = "select case when count(*) = 1 then 'true' else 'false' end as result "
		             + "from t_member "
		             + "where id=?";
		/* [2단계] 조건이 id 하나뿐이라 물음표도 1개다. */

		try (Connection con = dataFactory.getConnection();
		     PreparedStatement pstmt = con.prepareStatement(query)) {
		/* [3단계] 연결 열기 */

			pstmt.setString(1, id);
			/* [4단계] 1번 물음표에 아이디 채우기 */

			try (ResultSet rs = pstmt.executeQuery()) {
			/* [5단계] 조회 실행 */

				rs.next();
				result = rs.getBoolean("result");
				/* [6단계] 결과 읽기 */

			}

		} catch (Exception e) {
			e.printStackTrace();
			/* [7단계] 예외 처리 */
		}

		return result;
		/* [8단계] 반환

		   [이 결과가 쓰이는 곳]
		     회원가입 : true 면 가입 거부
		     회원수정 : false 면 "없는 회원" 이므로 수정 거부
		   -> 같은 메소드를 정반대 목적으로 쓰는 재사용의 예다. */

	}//isDuplicated

	/*==============================================================
	  insertMember() : 회원 정보 저장
	  - 매개변수 : 네 값이 담긴 상자
	  - 반환값   : true = 저장 성공 / false = 실패

	  [조회와 다른 점 2가지]
	    1) executeQuery 가 아니라 executeUpdate 를 쓴다
	    2) ResultSet 이 없다. 대신 int(처리된 행 개수)를 받는다
	==============================================================*/
	public boolean insertMember(MemberVO memberVO) {

		boolean result = false;

		String id    = memberVO.getId();
		String pwd   = memberVO.getPwd();
		String name  = memberVO.getName();
		String email = memberVO.getEmail();
		/* 저장할 값 네 개를 상자에서 꺼낸다. */

		String query = "insert into t_member (id, pwd, name, email) values (?, ?, ?, ?)";
		/* [INSERT 문법]
		     insert into 테이블명 (열1, 열2, ...) values (값1, 값2, ...)
		   앞의 열 개수와 뒤의 값 개수가 반드시 같아야 한다.
		   여기서는 열 4개, 물음표 4개다.

		   ** joinDate 를 적지 않은 이유 **
		   테이블을 만들 때 DEFAULT CURRENT_TIMESTAMP 로 지정해서
		   값을 안 넣으면 지금 시각이 자동으로 들어간다. */

		try (Connection con = dataFactory.getConnection();
		     PreparedStatement pstmt = con.prepareStatement(query)) {

			pstmt.setString(1, id);
			pstmt.setString(2, pwd);
			pstmt.setString(3, name);
			pstmt.setString(4, email);
			/* 물음표 4개를 순서대로 채운다.
			   ** 순서가 뒤바뀌면 비밀번호 자리에 이름이 들어가는
			      엉뚱한 저장이 일어난다. 오류는 나지 않아 더 위험하다. ** */

			int count = pstmt.executeUpdate();
			/* [실행 - 조회가 아닐 때]
			   executeUpdate() 는 "몇 개의 행이 처리되었는지" 를
			   숫자로 돌려준다.
			     저장 성공 -> 1
			     저장 실패 -> 0

			   ** executeQuery 를 쓰면 오류가 난다.
			      조회가 아니어서 돌려줄 결과표가 없기 때문이다. ** */

			if (count == 1) {
				result = true;
			}
			/* 1개가 처리되었으면 성공으로 본다. */

		} catch (Exception e) {
			e.printStackTrace();
			/* [자주 만나는 원인]
			   Duplicate entry : 이미 있는 아이디를 또 넣으려 함
			   -> 그래서 Service 가 미리 중복 검사를 하는 것이다. */
		}

		return result;

	}//insertMember

	/*==============================================================
	  selectMember() : 아이디로 회원 1명의 정보 조회
	  - 매개변수 : 아이디
	  - 반환값   : 정보가 담긴 상자 / 없으면 null

	  [지금까지와 결정적으로 다른 점]
	  회원이 없으면 행이 0개다!
	  count 조회는 항상 1행이 왔지만 이것은 아니다.
	  -> rs.next() 를 반드시 if 로 검사해야 한다.
	     검사 없이 값을 꺼내면 오류가 난다.
	==============================================================*/
	public MemberVO selectMember(String id) {

		MemberVO memberVO = null;
		/* 결과 상자를 null 로 초기화한다.
		   회원이 없으면 null 그대로 반환된다. */

		String query = "select id, pwd, name, email from t_member where id=?";
		/* 필요한 열 4개를 지정해 조회한다.

		   ** select * 를 쓰지 않은 이유 **
		   * 는 모든 열을 가져오라는 뜻이다. 편해 보이지만
		     - 안 쓰는 열까지 가져와 낭비이고
		     - 나중에 열이 추가되면 예상 밖으로 동작할 수 있다
		   필요한 열만 적는 습관이 안전하다. */

		try (Connection con = dataFactory.getConnection();
		     PreparedStatement pstmt = con.prepareStatement(query)) {

			pstmt.setString(1, id);

			try (ResultSet rs = pstmt.executeQuery()) {

				if (rs.next()) {
				/* ** 여기가 핵심이다 **
				   rs.next() 는 다음 행으로 이동하고
				   이동에 성공했으면 true, 행이 없으면 false 를 준다.
				   if 로 감싸면 "행이 있을 때만" 안쪽이 실행된다.

				   ** if 없이 rs.next(); 만 쓰면? **
				   회원이 없을 때 아래 getString 에서
				   "Illegal operation on empty result set" 오류가 난다. */

					memberVO = new MemberVO();
					/* 담을 상자를 이때 만든다.
					   행이 있을 때만 만들므로, 없으면 null 이 유지된다. */

					memberVO.setId(rs.getString("id"));
					memberVO.setPwd(rs.getString("pwd"));
					memberVO.setName(rs.getString("name"));
					memberVO.setEmail(rs.getString("email"));
					/* rs.getString("열이름") 으로 값을 꺼내
					   setter 로 상자에 담는다.

					   ** 열 이름은 SQL 의 select 에 적은 이름과 같아야 한다.
					      오타가 있으면 열을 못 찾아 오류가 난다. ** */

				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return memberVO;
		/* 회원 정보 상자를 반환한다. 없으면 null 이다.

		   ** 받는 쪽(Service, Controller)은 null 검사를 해야 한다.
		      null 인데 getName() 을 부르면 오류가 난다. ** */

	}//selectMember

	/*==============================================================
	  updateMember() : 비밀번호, 이름, 이메일 수정
	  - 매개변수 : 수정할 값이 담긴 상자
	  - 반환값   : true = 수정 성공 / false = 실패

	  ** 가장 조심해야 할 것 : where 절 **
	  where id=? 를 빠뜨리면 모든 회원의 정보가 한꺼번에 바뀐다.
	  실행 전에 where 가 있는지 반드시 확인하는 습관을 들이자.
	==============================================================*/
	public boolean updateMember(MemberVO memberVO) {

		boolean result = false;

		String id    = memberVO.getId();
		String pwd   = memberVO.getPwd();
		String name  = memberVO.getName();
		String email = memberVO.getEmail();

		String query = "update t_member set pwd=?, name=?, email=? where id=?";
		/* [UPDATE 문법]
		     update 테이블명 set 열1=값1, 열2=값2 where 조건

		   ** 물음표 번호는 SQL 에 나온 순서대로다 **
		     set pwd=?  name=?  email=?  where id=?
		         1번    2번     3번            4번
		   where 의 값이 마지막 번호가 되는 점을 놓치기 쉽다.

		   ** id 를 set 에 넣지 않은 이유 **
		   아이디는 회원을 구분하는 기본키라 바꾸지 않는다.
		   그래서 modify.jsp 의 아이디 칸도 readonly 로 되어 있다. */

		try (Connection con = dataFactory.getConnection();
		     PreparedStatement pstmt = con.prepareStatement(query)) {

			pstmt.setString(1, pwd);
			pstmt.setString(2, name);
			pstmt.setString(3, email);
			pstmt.setString(4, id);
			/* SQL 에 나온 순서대로 채운다.
			   ** 순서를 헷갈리면 아이디 자리에 이메일이 들어가
			      아무 회원도 수정되지 않는다. ** */

			int count = pstmt.executeUpdate();
			/* 수정된 행의 개수를 받는다.
			     수정 성공     -> 1
			     대상이 없음   -> 0 (오류는 나지 않는다!)
			   ** 없는 회원을 수정해도 오류가 안 나므로
			      count 를 확인하지 않으면 성공한 줄 착각한다. ** */

			if (count == 1) {
				result = true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;

	}//updateMember

	/*==============================================================
	  deleteMember() : 회원 정보 삭제
	  - 매개변수 : 아이디
	  - 반환값   : true = 삭제 성공 / false = 실패

	  ** 이 프로젝트에서 가장 위험한 메소드다 **
	  삭제는 되돌릴 수 없다.
	  where 를 빠뜨리면 전체 회원이 사라진다.
	==============================================================*/
	public boolean deleteMember(String id) {

		boolean result = false;

		String query = "delete from t_member where id=?";
		/* [DELETE 문법]
		     delete from 테이블명 where 조건

		   ** delete from t_member  <- where 가 없으면 전체 삭제! **
		   실무에서는 실행 전에 같은 조건으로 select 를 먼저 해 보고
		   지워질 행이 맞는지 확인하는 습관을 들인다. */

		try (Connection con = dataFactory.getConnection();
		     PreparedStatement pstmt = con.prepareStatement(query)) {

			pstmt.setString(1, id);

			int count = pstmt.executeUpdate();
			/* 삭제된 행의 개수를 받는다. */

			if (count == 1) {
				result = true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;

	}//deleteMember

	/*==============================================================
	  findId() : 이름 + 이메일로 아이디 찾기
	  - 매개변수 : 이름, 이메일
	  - 반환값   : 찾은 아이디 / 없으면 null

	  [selectMember 와 구조가 같다]
	  행이 없을 수 있으므로 if (rs.next()) 로 검사한다.
	==============================================================*/
	public String findId(String name, String email) {

		String id = null;
		/* 못 찾았을 때를 대비해 null 로 초기화한다. */

		String query = "select id from t_member where name=? and email=?";
		/* ** and 로 두 조건을 연결한 이유 **
		   or 로 쓰면 이름만 맞아도 알려주게 되어
		   본인 확인이 무의미해진다. */

		try (Connection con = dataFactory.getConnection();
		     PreparedStatement pstmt = con.prepareStatement(query)) {

			pstmt.setString(1, name);
			pstmt.setString(2, email);

			try (ResultSet rs = pstmt.executeQuery()) {

				if (rs.next()) {
					id = rs.getString("id");
					/* 찾았으면 아이디를 꺼내 담는다. */
				}
				/* 못 찾으면 if 안이 실행되지 않아 id 는 null 로 남는다. */

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return id;

	}//findId

	/*==============================================================
	  findPwd() : 아이디 + 이름 + 이메일로 비밀번호 찾기
	  - 매개변수 : 아이디, 이름, 이메일
	  - 반환값   : 찾은 비밀번호 / 없으면 null

	  [조건이 3개인 이유]
	  비밀번호는 아이디보다 중요한 정보이므로
	  본인 확인 항목을 하나 더 늘렸다. (아이디 찾기는 2개)

	  [실무와의 차이]
	  실제 사이트는 비밀번호를 알려주지 않는다.
	  알아볼 수 없는 형태로 저장하기 때문에 조회 자체가 불가능하다.
	  본인 확인 후 새 비밀번호를 정하게 하는 것이 올바른 방식이다.
	==============================================================*/
	public String findPwd(String id, String name, String email) {

		String pwd = null;

		String query = "select pwd from t_member where id=? and name=? and email=?";
		/* and 로 세 조건을 모두 연결했다.
		   셋 다 맞아야 비밀번호를 알려준다. */

		try (Connection con = dataFactory.getConnection();
		     PreparedStatement pstmt = con.prepareStatement(query)) {

			pstmt.setString(1, id);
			pstmt.setString(2, name);
			pstmt.setString(3, email);

			try (ResultSet rs = pstmt.executeQuery()) {

				if (rs.next()) {
					pwd = rs.getString("pwd");
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return pwd;

	}//findPwd

}//MemberDAO

/*
================================================================
 [혼자 확인해 보기]

  1. 조회와 입력에서 실행 메소드가 어떻게 다른가?
     -> select 는 executeQuery (결과표 반환)
        insert/update/delete 는 executeUpdate (행 개수 반환)

  2. rs.next() 를 if 로 감싸야 하는 메소드는 무엇인가?
     -> selectMember, findId, findPwd (행이 0개일 수 있음)
        isExisted, isDuplicated 는 count 조회라 항상 1행

  3. 물음표 자리번호는 몇 번부터 시작하는가?
     -> 1번부터 (배열의 0번과 다르다)

  4. updateMember 에서 where 를 빼면?
     -> 모든 회원의 정보가 같은 값으로 바뀌고 되돌릴 수 없다

  5. try-with-resources 를 쓰는 이유는?
     -> 오류가 나도 연결이 반드시 반납되어 커넥션 누수를 막는다

  6. 물음표 대신 글자를 이어붙이면 어떤 위험이 있는가?
     -> SQL 삽입 공격에 뚫려 비밀번호 없이 로그인될 수 있다

 [연결해서 보기]
   MemberService 가 이 파일의 메소드를 부른다.
   이 파일은 MySQL 에 SQL 을 보내고 결과를 받아
   Service 에게 true/false 또는 상자를 돌려준다.
================================================================
*/
