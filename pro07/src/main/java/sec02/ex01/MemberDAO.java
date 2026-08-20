package sec02.ex01;

/*
================================================================
MemberDAO 클래스 (Data Access Object : 데이터 접근 전담 객체)
================================================================

[1] 이 클래스의 역할
- 오라클 DBMS 서버의 XE 데이터베이스 안에 있는 t_member 테이블에 접근해서
  데이터베이스 접근 작업(SELECT, INSERT, UPDATE, DELETE)만 전담하는 클래스.
- 판단/계산 같은 비즈니스 로직은 이 클래스가 아니라 Service 계층이 담당한다.
  (Spring 구조 연결 지점 : Controller -> Service -> Repository(DAO) )

[2] 전체 요청/응답 흐름 (이 파일의 위치)

[브라우저] --요청--> [MemberServlet] --listMembers() 호출--> [MemberDAO] --SQL 전송--> [오라클 DBMS(t_member)]
[브라우저] <--응답-- [MemberServlet] <--ArrayList 반환------ [MemberDAO] <--조회 결과-- [오라클 DBMS(t_member)]

[3] JDBC 작업 전체 순서 (이 파일에서 번호로 표시됨)
순서1. DB 연결 정보 4가지를 상수로 준비 (DRIVER, URL, USER, PWD)
순서2. 드라이버 클래스 로딩          -> Class.forName(DRIVER)
순서3. DB 접속(연결 통로 객체 얻기)   -> DriverManager.getConnection(...)
순서4. SQL 실행 객체 얻기            -> con.createStatement()
순서5. SQL 문장 작성                 -> "select * from t_member"
순서6. SQL 전송·실행, 커서 얻기       -> stmt.executeQuery(query)
순서7. 커서를 이동시키며 한 행씩 읽기  -> while(rs.next()) { rs.getString(...) }
순서8. DB 자원 반납                  -> rs.close() -> stmt.close() -> con.close()
*/

import java.sql.*;
//java.sql 패키지의 모든 클래스/인터페이스를 사용하겠다는 선언.
//이 파일에서 실제로 사용하는 것 : Connection, Statement, ResultSet,
//                             DriverManager, Date, SQLException

import java.util.ArrayList;
//조회된 회원 객체들을 담을 가변길이 배열 클래스 ArrayList를 사용하겠다는 선언.

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;


public class MemberDAO {
		
	// DB 작업 3총사 객체를 참조할 변수 (초기값 null, 사용 시점에 채워짐)
	private Connection con;          // 커넥션풀(DataSource)에서 "빌려온" Connection 연결통로 객체 1개를 담을 변수
	private PreparedStatement pstmt; // SQL을 미리 담아 실행하는 PreparedStatement 객체를 담을 변수
	private ResultSet rs;            // 조회 결과를 한 행씩 읽는 ResultSet 객체를 담을 변수
	
	//톰캣이 context.xml의 <Resource .../> 코드를 읽어 들여  미리만들어둔 커넥션풀(DataSource) 객체의 주소를 저장할 변수 
	private DataSource dataSource;

	/*
    ============================================================
    MemberDAO 생성자 : 톰캣의 커넥션풀을 찾아와 dataSource에 저장
    ============================================================
    - 실행 시점 : 서블릿에서 new MemberDAO(); 를 호출하는 순간 자동 실행.
    - 목적     : context.xml에 등록된 <Resource>(커넥션풀)를
                 JNDI 이름 "jdbc/oracle"로 찾아와 dataSource 변수를 초기화.

    +--- 시작 전에 알아야 할 것 : JNDI 저장소의 전체 구조 ---------------+

    [텍스트 모델링 (0)] 톰캣이 시작될 때 서버 메모리 안에 만들어 두는 것

    [톰캣 서버 메모리]
     |
     +-- [커넥션풀 객체]  <- context.xml의 <Resource>를 읽고
     |    (DB와 이미 연결된 Connection 50개 보관: maxActive=50)
     |
     +-- [JNDI 저장소]    <- key와 value를 짝지어 보관하는 공간
          |
          +-- "java:" 영역
                +-- "comp"                (component : 이 웹앱의 구역)
                      +-- "env"           (environment : 환경설정 모음 구역)
                            +-- key : "jdbc/oracle"
                                value : [커넥션풀 DataSource객체]의 주소  <- 위의 풀과 연결!

    -> 즉 커넥션풀은 "java:/comp/env" 구역 안에
       "jdbc/oracle" 이라는 이름표(key)를 달고 이미 보관되어 있다.
       생성자가 할 일은 이 보관된 주소를 "꺼내오는 것" 뿐이다. (만드는 게 아님!)
    +--------------------------------------------------------------------+
	 */
	public MemberDAO() {		
		try {
			/*
		    ----------------------------------------------------
		    순서1. InitialContext 객체 생성 = 탐색의 "출발점" 만들기
		    ----------------------------------------------------
		    - InitialContext는 JNDI 저장소를 검색할 수 있게 해 주는 객체.
		    - "Initial(최초의)" : 아직 아무 구역에도 들어가지 않은,
		      저장소 전체의 가장 바깥(뿌리)에 서 있는 상태를 뜻한다.

		    [텍스트 모델링 (1)] 이 줄 실행 직후의 메모리

		    [Stack]                     [Heap]
		    ctx ---------주소---------> [InitialContext 객체]
		                                 현재 위치 : JNDI 저장소의 뿌리(최상위)
		                                 할 수 있는 일 : lookup("경로")로 하위 구역 탐색

		    [JNDI 저장소]
		    (뿌리) <== ctx가 여기 서 있음
		     +-- "java:"
		           +-- "comp"
		                 +-- "env"
		                       +-- "jdbc/oracle" = [커넥션풀 주소]
		*/			
			Context  ctx = new InitialContext();
			
			/*
		    ----------------------------------------------------
		    순서2. "java:/comp/env" 구역까지 이동한 위치 객체 얻기
		    ----------------------------------------------------
		    - ctx.lookup("java:/comp/env")
		      : 뿌리에서 출발해 java: -> comp -> env 구역까지 내려간 뒤,
		        그 "env 구역을 가리키는 위치 객체(Context)"를 반환한다.
		    - 왜 이 경로인가?
		      : 톰캣은 context.xml에 등록한 자원들을 반드시
		        "java:/comp/env" 구역 아래에 넣어 두기 때문. (톰캣의 규칙)
		    - (Context) 형변환 이유
		      : lookup()의 반환 타입은 Object라서,
		        "구역 위치"로 쓰려면 Context 타입으로 되돌려야 한다.

		    [텍스트 모델링 (2)] 이 줄 실행 직후의 메모리

		    [Stack]                     [Heap]
		    ctx --------주소----------> [InitialContext 객체] (뿌리 위치)
		    envCtx -----주소----------> [Context 객체]        (env 구역 위치)

		    [JNDI 저장소]
		    (뿌리)  <== ctx는 계속 여기
		     +-- "java:"
		           +-- "comp"
		                 +-- "env"  <== envCtx가 여기까지 들어와 서 있음!
		                       +-- "jdbc/oracle" = [커넥션풀 DataSource객체 주소]
		                       			key           value
		       			
			*/						
			Context envCtx = (Context) ctx.lookup("java:/comp/env");
			// new InitialContext("java:/comp/env");
			
			/*
		    ----------------------------------------------------
		    순서3. key "jdbc/oracle" 로 커넥션풀(value) 꺼내오기
		    ----------------------------------------------------
		    - envCtx.lookup("jdbc/oracle")
		      : env 구역 안에서 key가 "jdbc/oracle"인 항목을 찾아
		        그 value(커넥션풀 객체의 주소)를 반환한다.
		    - 이 key는 context.xml의 name 속성과 "철자까지 정확히" 일치해야 한다.
		      다르면 NameNotFoundException이 발생한다. (아래 catch로 이동)
		    - (DataSource) 형변환 이유
		      : 여기서도 반환 타입이 Object이므로,
		        커넥션풀로 쓰려면 표준 타입인 DataSource로 되돌린다.

		    [텍스트 모델링 (3)] key와 context.xml이 짝을 이루는 순간

		    자바 코드                          톰캣의 context.xml
		    lookup( "jdbc/oracle" )  <--일치-->  <Resource name="jdbc/oracle"
		              |                                    type="javax.sql.DataSource"
		              | 일치하면 value 반환                driverClassName="oracle.jdbc.OracleDriver"
		              v                                    url="jdbc:oracle:thin:@localhost:1521:xe"
		    [커넥션풀 객체의 주소]                         username="scott"  password="tiger"
		                                                   maxActive="50" maxIdle="10" maxWait="-1" />
		    -> URL, 계정, 비밀번호는 전부 context.xml 쪽에만 있다.
		       자바 코드에는 이름("jdbc/oracle") 하나만 있으면 된다!

		    [텍스트 모델링 (4)] 이 줄 실행 직후의 최종 메모리 (생성자의 목표 달성)

		    [Stack/멤버]                 [Heap : 톰캣이 미리 만들어 둔 것]
		    dataSource ----주소--------> [커넥션풀 객체 (DataSource)]
		                                  +-------------------------------+
		                                  | 연결1  연결2  연결3 ... 연결50 |  <- DB와 이미 접속 완료
		                                  +-------------------------------+
		                                  getConnection() 호출 시 1개를 빌려줌

		    정리 : 이제부터 dataSource.getConnection() 을 호출하면
		           새로 접속하는 것이 아니라, 이미 접속된 연결 1개를 즉시 빌려온다!
		*/			
			dataSource = (DataSource) envCtx.lookup("jdbc/oracle");
						
		} catch (Exception e) {
			// 실패하는 대표 원인 3가지
			// 1) lookup의 key와 context.xml의 name 철자가 다름
			// 2) WEB-INF/lib에 ojdbc6.jar가 없음
			// 3) context.xml의 url/계정 오류로 톰캣이 풀 생성에 실패함
			System.out.println("DataSource 커넥션풀 객체 얻기 실패  : " + e.toString());
			
		}	
	}
	
	
		
	//=========================================================================
	// listMembers() 메소드 정의  :  t_member 테이블의 전체 회원을 조회해 반환하는 메소드
	//=========================================================================
	public ArrayList<MemberVO>  listMembers(){		
		
		ArrayList<MemberVO> list = new ArrayList<MemberVO>(); //조회 결과를 담을 비어 있는 ArrayList 를 생성		
		
		try {		
			/*
		    [핵심 변화] 접속 생성이 아니라 커넥션 풀(DataSource 객체 공간)에서 "빌려오기"		  
		    - dataSource.getConnection()       -> 이미 접속된 연결을 즉시 대여

		    [텍스트 모델링] 빌려오는 순간 모습
		    ==================================================================================
		    [커넥션풀] 연결1 연결2 연결3 ...   -> 연결1을 꺼내 con에 저장
		    [커넥션풀] (빈자리) 연결2 연결3 ...   con --> [연결1 : 사용중]
		*/
			con = dataSource.getConnection();
			
			
			// 실행할 SQL 문장 작성 (전체 회원 조회)
			String query = "select * from t_member";
			
			// SQL(select)을 미리 전달해서 보관 중인  PreparedStatement 실행객체 얻기 
			pstmt = con.prepareStatement(query);
			/*
		    PreparedStatemnt select 실행 객체 메모리 안에 저장된 모습
		    +--------------------------------+
		    | select * from t_member         |
		    +--------------------------------+ 
			 */
			
			//PreparedStatemnt 객체에 보관된 select * from t_member 문장을 Oralce DBMS의 t_member테이블로 전송해서 실행후
			//조회된 표 형태의 결과를 ResultSet 임시 객체 메모리에 담아 ResultSet 객체 자체주소 반환
			//참고. ResultSet 객체 메모리 영역에서 커서 위치는 열 행 줄을 가리키고 있다.
			rs = pstmt.executeQuery(); 
					
			// next() : 커서를 한 행 이동 + 조회된 행이 있으면 true 반환. 조회된 행이 없으면 false로 반복 종료.
			while(rs.next()) {
				
				// 커서가 현재 가리키는 행에서 열(컬럼) 이름으로 조회 값을 꺼낸다.
				// getString("열이름") : 해당 열의 조회 값을 String으로 꺼내는 메소드.
				String id = rs.getString("ID");   		// 1행 예 : "hong"
				String pwd = rs.getString("PWD"); 		// 1행 예 : "1212"
				String name = rs.getString("NAME");		// 1행 예 : "홍길동"
				String email = rs.getString("EMAIL");	// 1행 예 : "hong@gamil.com"				
				// 날짜 열은 getDate()로 꺼내며, 반환 타입은 java.sql.Date 이다.
				// (import java.sql.*; 이므로 여기의 Date는 java.sql.Date)
				Date  joinDate = rs.getDate("JOINDATE"); //1행 예 : new Date("2026-08-19");
				
				//-------------------------------------------------------------------
				//꺼낸 한 행의 조회값 5개를  MemberVO 객체 1개에 저장
				//-------------------------------------------------------------------
				// MemberVO  : 회원 1명의 데이터를 담는 클래스 (DTO/VO 역할)
				// new MemberVO() : 회원 1명 정보 담을 객체 생성
				MemberVO vo  = new MemberVO();
				vo.setId(id); 					//MemberVO객체의 id 인스턴스 변수에 "hong" 저장
				vo.setPwd(pwd);					//MemberVO객체의 pwd 인스턴스 변수에 "1212" 저장
				vo.setName(name);               //MemberVO객체의 name 인스턴스 변수에 "홍길동" 저장
				vo.setEmail(email);             //MemberVO객체의 email 인스턴스 변수에 "hong@gamil.com" 저장
				vo.setJoinDate(joinDate);		//MemberVO객체의 joinDate 인스턴스 변수에 new Date("2026-08-19"); 저장 
				
				//완성된  MemberVO 객체를 ArrayList 배열 끝 칸에 추가 
				list.add(vo);
				
				// 반복이 3회 끝난 후 ArrayList 내부 모습 (주소를 담고 있음)
				// [ new MemberVO(hong), new MemberVO(lee), new MemberVO(kim) ]
				//        0                      1              2         index
				
			} //while 반복문 
			
					
		}catch(Exception e) {			
			e.printStackTrace(); //SQL 오타,  테이블 없음,  접속 끊김 등 실행 중 모든 예외를 받아 출력
		}finally {
			// ----------------------------------------------------
			// 순서8. finally : 성공/예외 발생과 관계없이 "무조건" 실행되는 영역
			// ----------------------------------------------------
			// DB 연결 자원은 예외가 나도 반드시 반납해야 하므로 finally에 작성한다.
			// (반납하지 않으면 DB 서버의 연결 수가 계속 쌓여 톰캣 서버가 느려진다.)
			ResourceClose();
			
		}
		
		// 조회 결과가 담긴 ArrayList배열 주소를 호출한 쪽(MemberServlet 사장님)으로 반환.
		return list; 	// [ new MemberVO(hong), new MemberVO(lee), new MemberVO(kim) ]  
	}

	//===============================================================================
	//ResourceClose() :  DB 연결 자원 반납 메소드 (순서8.에서 호출)
	public void ResourceClose() {
		
		try {
			// 닫는 순서 규칙 : 연 순서(con->pstmt->rs)의 "반대"로 닫는다.
			// 이유 : rs는 pstmt를 통해, pstmt는 con을 통해 만들어진
			//        의존 관계이므로 안쪽 자원부터 닫는 것이 안전하다.
			// if(변수 != null) : connDB() 실패로 객체가 안 만들어졌을 수 있으므로
			//                    null 검사 후 닫아야 NullPointerException을 막는다.
			if( rs  != null ) rs.close();   //1) 조회 결과 임시 공간 반납
			if( pstmt != null) pstmt.close(); //2) SQL 실행 객체 반납
			if( con  != null) con.close();  //3) DB 연결 통로 객체 Connection 를 DataSource 커넥션풀 공간으로 반납
		} catch (SQLException e) {			
			e.printStackTrace(); // close() 실패 시(이미 끊긴 연결 등) 발생하는 예외 처리
		}
		
	}

} //<==== class MemberDAO


/*
    ================================================================
    [예상 동작] MemberServlet에서 dao.listMembers() 호출 시
    ================================================================
    t_member에 3명이 저장되어 있다면 아래 배열이 반환된다.
    list = [ MemberVO(hong, 홍길동, ...), MemberVO(lee, 이순신, ...), MemberVO(kim, 김유신, ...) ]

    [메모리 구조 요약]
    [Stack] list변수 --주소--> [Heap] ArrayList --각 칸의 주소--> [Heap] MemberVO 객체 3개

    ================================================================
    [핵심 정리 3줄]
    ================================================================
    1. DAO는 DB 접근 작업만 전담하고, 비즈니스 로직은 Service 계층이 담당한다.
    2. JDBC 순서 : 드라이버 로딩 -> Connection -> Statement -> executeQuery
                  -> ResultSet(next로 한 행씩) -> close(연 순서의 반대로).
    3. close()는 메모리 제거가 아니라 DB 자원 반납이며, 메모리는 GC가 회수한다.
*/













