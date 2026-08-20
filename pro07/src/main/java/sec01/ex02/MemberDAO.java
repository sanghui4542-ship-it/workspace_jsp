package sec01.ex02;

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


public class MemberDAO {
	
//순서1. DB 연결 정보 4가지를 상수 메모리에 저장
	
	//연결정보1.  JDBC 드라이버 (ojdbc6.jar 파일에 포함된 OracleDriver.class)의 전체 경로(패키지 포함)를 문자열로 저장
	//이 문자열은 순서2의 Class.forName()이 클래스를 찾을 때 사용된다.
	private static final String DRIVER = "oracle.jdbc.driver.OracleDriver";

	//연결정보2. DB 접속 주소(URL)를 문자열로 저장.
	private static final String URL = "jdbc:oracle:thin:@localhost:1521:XE";
	
	//연결정보3.  XE 데이터베이스에 접속할 계정 아이디 저장.
	private static final String USER = "scott";
	
	//연결정보4.  scott 계정의 비밀번호 저장
	private static final String PWD = "tiger";
	
	//==================================================================
	//DB 작업에 필요한 3가지 객체를 참조할 변수 선언
	//=================================================================
	//아래 3개 변수는 인스턴스변수이므로 JVM의 [HEAP] 영역의 MemberDAO객체 안에 만들어지고,
	//초기값은 null이다.  connDB() 가 실행되어야 실제 객체 주소가 저장된다.

	private Connection con;
		

	// ============================================================
	// [핵심] PreparedStatement 를 참조할 변수
	// ============================================================
	// PreparedStatement 란? ("Prepared" = 미리 준비된)
	// - SQL 문장을 "실행 전에 미리" 객체 안에 저장해 두고 사용하는 SQL 실행 객체.
	// - Statement 의 자식 인터페이스이며, 오라클 thin 드라이버 사용 시
	//   실제 생성되는 구현 객체의 클래스명은 OraclePreparedStatementWrapper 이다.
	//
	// Statement 와의 결정적 차이 3가지
	// 1) SQL을 미리 저장 : prepareStatement(SQL) 호출 시점에 SQL이 객체에 담기고,
	//    DB 서버가 이 SQL의 실행 계획을 미리 분석(파싱)해 둔다.
	// 2) ? (물음표, 위치홀더) 사용 가능 : 값이 들어갈 자리를 ?로 비워 두고,
	//    setString(순번, 값) 등으로 나중에 채울 수 있다.
	// 3) 값과 문장을 분리 처리 : ?에 들어간 값은 "SQL 문법"이 아니라
	//    "순수한 값"으로만 취급된다. -> SQL 인젝션 공격이 차단되는 이유.
	private PreparedStatement pstmt;
	
	
	
	// ResultSet : SELECT 실행 결과를 "한 행씩 읽어오는 커서" 객체를 참조할 변수.
	// - 조회 결과 전체를 이 객체가 통째로 담고 있는 것이 아니라,
	//   DB 서버의 결과를 next() 호출 시마다 읽어오는 통로 역할을 한다.
	//   (오라클은 기본적으로 10행씩 나눠서 가져온다 : fetch size)
	private ResultSet rs;
	
//순서2 ~ 4를 처리하는 connDB() 메소드 : (드라이버 로딩 -> 접속 -> 실행 객체)
	private void connDB() {
		
		// try-catch : 아래 코드들은 실패 가능성이 있어 예외 처리가 "강제"된다.
		// (ClassNotFoundException, SQLException 은 checked 예외라서
		//  처리하지 않으면 컴파일 자체가 되지 않는다.)
		try {
			//=========================================================
			//순서2. JDBC 드라이버(OracleDriver.class)를 JVM 메모리에 로딩
			//=========================================================
			// Class.forName(문자열) 의 정확한 동작
			// - 문자열로 전달된 이름의 클래스를 ClassLoader가 찾아 JVM에 로딩한다.
			// - 주의 : 객체(new)를 만드는 것이 아니라 "클래스 자체"를 읽어들이는 작업이다.
			//
			// 로딩되면 드라이버가 등록되는 과정
			// - OracleDriver 클래스 내부에는 static 초기화 블록이 있다.
			// - 클래스가 로딩되는 순간 static 블록이 자동 실행되고,
			//   그 안의 DriverManager.registerDriver(new OracleDriver()); 가 실행된다.
			// - 결과 : DriverManager에 오라클 드라이버 객체가 등록 완료됨.
			//
			// 참고 : JDBC 4.0(자바 6)부터는 드라이버가 자동 로딩되어 이 줄을
			// 생략해도 동작하지만, 구버전 호환과 학습을 위해 관례적으로 작성한다.
			Class.forName(DRIVER);
			
			//===============================================================
			//순서3. DB에 접속해서 Connection(연결 통로) 객체 얻기
			//===============================================================
			// DriverManager.getConnection(URL, USER, PWD) 의 동작
			// 1) 순서2에서 등록된 드라이버 객체에게 URL을 전달한다.
			// 2) new OracleDriver(); 드라이버가 localhost:1521의 오라클 서버에 접속을 시도한다.
			// 3) scott/tiger 계정 인증에 성공하면 연결이 맺어진다.
			// 4) 그 연결 정보를 관리하는 T4CConnection 객체가 [Heap]에 생성되고
			//    그 주소가 반환되어 con 변수에 저장된다.
			// 실패 시(서버 꺼짐, 계정 오류 등) SQLException이 발생한다.
			con = DriverManager.getConnection(URL, USER, PWD);
			
		
			
		} catch (ClassNotFoundException e) {
			// Class.forName()이 해당 이름의 클래스를 못 찾으면 발생.
			// 원인 예 : ojdbc6 라이브러리(jar)를 프로젝트에 추가하지 않은 경우, 오타.
			e.printStackTrace(); //예외 발생 위치와 원인을 콘솔에 출력
			
		} catch (SQLException  e) {
			// getConnection(), createStatement() 실패 시 발생.
			// 원인 예 : 오라클 서버 미실행, URL 오타, 계정/비밀번호 오류.
			e.printStackTrace(); //예외 발생 위치와 원인을 콘솔에 출력
		}
		
	}
	
	//=========================================================================
	// listMembers() 메소드 정의  :  t_member 테이블의 전체 회원을 조회해 반환하는 메소드
	//=========================================================================
	public ArrayList<MemberVO>  listMembers(){		
		
		ArrayList<MemberVO> list = new ArrayList<MemberVO>(); //조회 결과를 담을 비어 있는 ArrayList 를 생성		
		
		try {
			//순서2 ~ 순서3를 한번에 처리 : 드라이버 로딩  +  DB 접속 
			connDB();
			
			// ----------------------------------------------------
			// 순서5. 실행할 SQL(Query) 문장을 문자열로 작성
			// -> t_member 테이블의 모든 행, 모든 열을 조회 하라는 의미 
			// ----------------------------------------------------
			String query = "select * from t_member";
			
			//----------------------------------------------------------------------------
			//순서4. 대신 순서5.1. 추가  [핵심] SQL을 미리전달하며 PreparedStatement 부모인터페이스의 자식 구현 실행 객체 얻기
			//-------------------------------------------------------------------------
			// con.prepareStatement(query) 의 내부 동작
			// 1) query 의 SQL 문장이 실행 객체 안에 미리 저장된다.
			// 2) SQL이 DB 서버로 먼저 전송되어 문법 검사와 실행 계획 분석이 이 시점에 미리 끝난다. (사전 컴파일)
			// 3) 준비를 마친 OraclePreparedStatementWrapper 객체 주소가 반환된다.
			// -> 같은 SQL을 반복 실행할 때 분석을 다시 안 하므로 Statement 보다 빠르다.
			pstmt = con.prepareStatement(query);
			/*
		    OraclePreparedStatementWrapper 실행 객체 메모리 안에 저장된 모습
		    +--------------------------------+
		    | select * from t_member         |
		    +--------------------------------+




		    [? 위치홀더를 쓰는 경우 예시] (이번 SQL엔 조건이 없어 사용 안 함)
		    
		    pstmt = con.prepareStatement("select * from t_member where id = ?");
		    
		    +----------------------------------------+
		    | select * from t_member where id = ?    |  <- ?는 값이 들어올 빈 자리
		    +----------------------------------------+
		    
		    pstmt.setString(1, "hong");  // 1 = 첫 번째 ? 에 "hong"을 채워라
		    
		    +----------------------------------------+
		    | select * from t_member where id='hong' |  <- 값이 채워진 상태로 실행됨
		    +----------------------------------------+
		    - setString의 첫 매개변수는 ?의 순번(1부터 시작), 두 번째는 넣을 값.
		    - 자료형별 메소드 : setString(문자), setInt(정수), setDate(날짜) 등.


		    [SQL 인젝션이 차단되는 이유]
		    
		    - Statement 로 문자열을 + 연결하면 입력값이 SQL "문장의 일부"가 된다.
		      예) "... where id='" + 입력값 + "'" 에 입력값으로  ' or '1'='1  이 들어오면
		          -> where id='' or '1'='1'  이 되어 조건이 항상 참 = 전체 조회됨 (공격 성공)
		          
		    - PreparedStatement의 ?에 같은 값을 setString으로 넣으면
		      그 문자열 전체가 "id 값 하나"로만 취급된다.
		          -> where id = ''' or ''1''=''1'  (일치하는 id 없음 = 공격 실패)
		          
		    - 즉 ?에 들어온 값은 절대 SQL 문법으로 해석되지 않는다.
		*/
			//-----------------------------------------------------------------------------------------
			//순서6. SQL실행 : 	OraclePreparedStatementWrapper 실행객체의 executeQuery(); 에 매개변수가 없다!	
			//-----------------------------------------------------------------------------------------
			// SQL은 순서5.1에서 이미 OraclePreparedStatementWrapper 실행객체객체 안에 select 문장이  저장·분석되어 있으므로
			// 실행 명령만 내리면 된다. (ex01은 executeQuery(query)로 SQL을 이때 전달했음)
			// 반환된 ResultSet의 커서는 첫 데이터 행 "직전" 위치에서 시작한다.
			rs = pstmt.executeQuery(); 
					
			// ----------------------------------------------------
			// 순서7. 커서를 한 행씩 이동시키며 데이터 읽기
			// ----------------------------------------------------
			// rs.next() 의 동작  (한번씩 호출할떄 마다 2가지 일을 한다)
			// 1) 커서(화살표)를 다음 행으로 1칸 이동시킨다.
			// 2) 이동한 위치에 조회된 행이 있으면 true, 더 이상 없으면 false 반환.
			// -> 첫 행을 읽으려면 반드시 next()를 먼저 1번 호출해야 한다.
			// -> false가 반환되는 순간 while 반복이 끝난다. (행 3개면 3회 반복)
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
			if( con  != null) con.close();  //3) DB 연결 통로 객체 반납
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













