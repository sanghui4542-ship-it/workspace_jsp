package sec02.ex01;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

//오라클 DBMS서버의 XE 데이터베이스 내부에 만들어 놓은 t_member테이블과 연결해서 DB작업 하는 클래스.

public class MemberDAO {

	//DB 와 연결해서 작업할 삼총사 객체들을 저장할 참조변수들 선언 	
	private Connection con;
	private PreparedStatement pstmt;
	private ResultSet rs;
	
	//DataSource(커넥션풀) 객체의 주소를 저장할 변수 선언
	private DataSource dataSource;
	
	
	//MemberDAO 생성자 
	//-> 이 생성자는 new MemberDAO();를 호출하면 자동으로 실행!
	//-> 목적 : 톰캣서버(context.xml)에 등록된 "DB 연결 정보"를 찾아서, 
	//         그안에 있는 "DataSource"커넥션 풀 객체를 얻어 초기화 하는 작업
	public MemberDAO() {
		try {
			//순서1. InitialContext 객체 생성
			//-> 이 객체를 통해 우리는 톰캣서버의 context.xml에 설정된 커넥션풀 자원 <Resource>를 찾을수 있게 됩니다.
			//-> 톰캣 서버 내부에 등록된 <Resource>태그의 자원을 검색할수 있게 도와주는 객체.
			Context ctx = new InitialContext();
			
			//순서2. "톰캣 서버 내부 환경" 에 접근
			//-> lookup("java:/comp/env")  이경로는 톰캣 서버에서 설정된 환경 변수들이 모여있는 경로로 
			//   context.xml 파일에 등록된 DB 정보들이 이안에 위치하기 떄문에 이경로를 저장 해 준다.
			Context envCtx = (Context)ctx.lookup("java:/comp/env");
			
			//순서3. jdbc/oracle 이라는 JNDI이름을 이용하여 
			//      context.xml 설정파일에 설정 해 놓은 <Resource>태그의 DataSource객체를 만들어 불러옵니다.
			dataSource = (DataSource)envCtx.lookup("jdbc/oracle");
			
	        // ────────────── 📌 텍스트 메모리 모델링 ③ ──────────────
	        // [context.xml 내부에 이런 설정이 있어야 함]
	        /*
	            <Resource name="jdbc/oracle"
	                      type="javax.sql.DataSource"
	                      driverClassName="oracle.jdbc.OracleDriver"
	                      url="jdbc:oracle:thin:@localhost:1521:xe"
	                      username="scott"
	                      password="tiger"
	                      maxActive="50"
	                      maxIdle="10"
	                      maxWait="-1"/>
	        */
	        // 정리:   이제부터는 dataSource.getConnection()을 호출하면
	        //         실제 DB에 접속된 Connection을 꺼내서 사용할 수 있음!
		} catch (Exception e) {
			//context.xml에 작성한 DataSource커넥션 객체를 얻지 못할때 발생할 예외메세지 출력
			System.out.println("DataSource 커넥션풀 객체 얻기 실패 : " + e.toString());
		}
		
		
	}
	
	
	//addMember메소드 정의
		//- memberForm.html화면에서 입력한 가입할 새회원 데이터들을~~
		//  MemberVO객체를 생성해서 각 인스턴스변수에 저장한 뒤~~
		//  member.jsp내부에서  addMember메소드 호출시~ 매개변수 MemberVO vo로  전달받아 INSERT SQL문을 만들고
		// 만든 INSERT SQL문을 DB의 t_member테이블에 전송해서 새회원 정보를 추가 시키는 기능을 하는 메소드.
	
	//요약 : 입력한 새회원 정보를 DB의 테이블에 추가 
	public int addMember(MemberVO vo) {
			
		int result = 0; //회원가입에 성공하면 1을 저장시키고, 실패하면 0을 저장시킬 변수 선언
		
		try {
			//순서1. 커넥션풀(DataSource)에 미리 DB와 연결을 맺은 T4CConnection객체 빌려오기
			//요약 : DB연결
			con = dataSource.getConnection();
			
			//순서2. SQL문(insert문) 만들기
			//방법 : 매개변수 MemberVO vo로 전달 받는 MemberVO객체의 인스턴스변수값들을 모두얻어 insert문장을 완성 시킨다.
			String id = vo.getId(); //입력한 아이디
			String pwd = vo.getPwd(); //입력한 비밀번호
			String name = vo.getName(); //입력한 이름 
			String email = vo.getEmail();//입력한 이메일 
			
			String query = "insert into t_member(id, pwd, name, email)" + 
						   				 "values(?,    ?,    ?,     ?)";
			
			//순서3. query 변수에 저장된 "insert into t_member(id, pwd, name, email) values( ?,   ?,    ?,     ?)"
			//      미리 올려놓고 동적으로 insert 문장을 완성 시킬 PreparedStatement실행 객체 얻기
			pstmt = con.prepareStatement(query);
			//-----------------------------------
			//PreaparedStaement 실행 객체 메모리에 insert 문장이 저장된 모습
			//->  "insert into t_member(id, pwd, name, email) values( ?,   ?,    ?,     ?)"
			
			//순서3.1. PreaparedStatement 실행 객체 메모리에 insert 문장의 ? 기호 대신  입력한 가입할 데이터들로 설정!
			//요약 : ? 설정
			//방법 ->  setter 메소드를 호출해서 설정
			pstmt.setString(1, id);  //첫번째 ?  대신 id변수에 저장된 입력한 아이디로 설정
			pstmt.setString(2, pwd); //두번째 ? 대신 pwd변수에 저장된 입력한 비밀번호로 설정  
			pstmt.setString(3, name);//세번쨰 ? 대신 name변수에 저장된 입력한 이름을 설정 
			pstmt.setString(4, email);//네번째 ? 대신 email변수에 저장된 입력한 이메일로 설정 
			
			//PreaparedStaement 실행 객체 메모리에 insert 문장이 저장된 모습
			//->  "insert into t_member(id, pwd, name, email) values('admin', '1234', '홍길동2', 'admin@naver.com')"
			
			//순서4. PreaparedStaement 실행 객체 메모리에 완성된 전체 insert 문장을 DB의 t_member테이블에 전송해서 실행!
			result = pstmt.executeUpdate(); //insert 문장 실행에 성공하면 insert에 성공한 레코드 갯수 1반환
											//insert 문장 실행에 실패하면 0을 반환
			
		} catch (Exception e) {
			System.out.println("MemberDAO클래스의 addMember메소드 내부의 코드에서 insert SQL문 실행 오류 : " + e);
			e.printStackTrace();
		} finally {
			//순서5.DB 작업 관련 객체 메모리들 사용이 끝난 후 자원 해제
			ResourceClose();
		}
		
		//순서6. 새회원 추가(회원가입 성공) 1  또는    (실패) 0을  member_action.jsp으로 반환  
		return result;
		
	} //==================> addMember() 메소드 
	
/*	
 참고용
	public boolean addMember(MemberVO vo) {

		boolean result = false;

		String query = "insert into t_member (id, pwd, name, email) "
		             + "values (?, ?, ?, ?)";

		try (Connection con = dataSource.getConnection();
		     PreparedStatement pstmt = con.prepareStatement(query)) {

			pstmt.setString(1, vo.getId());
			pstmt.setString(2, vo.getPwd());
			pstmt.setString(3, vo.getName());
			pstmt.setString(4, vo.getEmail());

			int count = pstmt.executeUpdate();
			result = (count == 1);

		} catch (SQLException e) {
			System.err.println("[MemberDAO.addMember] SQL 실행 오류: " + e);
		}

		return result;
	}//addMember
*/	
	
	

	//listMembers 메소드 정의
	//- 오라클 DBMS 서버 내부의 XE데이터베이스에 만들어진 t_member테이블에 저장된 모든 회원 레코드들을 한번에 조회 해서 가져와 
	//  레코드(행) 단위로 MemberVO클래스의 객체에 저장 후   
	//  MemberVO객체 들을  ArrayList 배열에 추가 해서 저장 후  조회된 회원정보들이 저장된 ArrayList 배열 자체를 반환 하는 메소드 
	public ArrayList listMembers() {
		
		//t_member테이블에 저장된 모든 회원 레코드들을 조회 해서 가져와 
		//가변길이 배열의 각 index위치 칸에 임시로 저장할 배열공간인? ArrayList 배열 생성 
		ArrayList  list = new ArrayList();
		
		try {
			//DataSource(커넥션풀)공간에서  미리 DB의 테이블과 연결을 맺어 놓은 T4CConnection 접속 객체하나 빌려오기 
			//이유 : DB와 연결해서 작업하기 위함
			con = dataSource.getConnection();
			
			//순서5. SQL문장 (SELECT 문장) 작성
			//-> t_member 테이블에 저장된 모든 회원 레코드들을 조회하는 SQL문장 작성
			String query = "select * from t_member";
			
			//순서5.1. T4CConnection객체의 prepareStatement메소드 호출시~~~ 매개변수로 순서5.에서 미리 준비한 select * from t_member 문장을 전달하면
			//        OraclePreparedStatementWrapper 실행 객체 메모리에  select * from t_member 문장 전체를 저장 한 후 
			//         OraclePreparedStatementWrapper 실행 객체 주소 자체를 반환 해 줍니다.
			pstmt = con.prepareStatement(query);
			/*
			OraclePreparedStatementWrapper 실행 객체 메모리 안에 저장된 모습
			----------------------------------------------
			select * from t_member
			-----------------------------------------------	
			*/
					
			//순서6. SQL문장을  오라클 DBMS 서버의 XE 데이터베이스의 t_member테이블에 전송(전달)해서 실행!(조회)
			//"select * from t_member" SQL문을 이용하여 조회 후 조회한 결과 데이터들을 ResultSet객체 메모리에 저장후 반환 받습니다.
			//단! 조회된 화면의 커서(화살표) 위치는 가장 처음에 조회된 표형태의 제목열 행 가리키고 있다.
			rs = pstmt.executeQuery(); //<- ResultSet 객체 메모리 반환 
			
			
			//순서7. 조회된 회원 레코드들이 ResultSet임시 객체 메모리에 표형태로 저장되어 있으면 계속반복해서
			//회원 레코드(행)단위의 조회된 열(컬럼)값들을 차례대로 얻어
			//MemberVO객체를 행단위로 생성하여 각 인스턴스변수에 저장시킵니다.
			//마지막으로 생성된 MemberVO객체들을 차례대로 ArrayList배열에 반복해서 추가시킵니다.
			while(rs.next()) {
				
				//커서가 위치한 조회된 회원 레코드(한 행의 데이터)의 열의 값들을 차례대로 얻어 변수에 저장
				String id = rs.getString("ID"); //"hong" , "lee",    "kim"
				String pwd = rs.getString("PWD");//"1212",  "1212",  "1212"
				String name = rs.getString("NAME");//"홍길동", "이순신",  "김유신"
				String email = rs.getString("EMAIL");//"hong@gamil.com", "lee@test.com", "kim@web.com"
				Date   joinDate =  rs.getDate("JOINDATE"); // new Date("2026/01/27"); 
														   // new Date("2026/01/27");
														   // new Date("2026/01/27");
										
				
				//MemberVO객체를 행단위로 생성하여 각 인스턴스변수에 저장시킵니다.
				MemberVO vo = new MemberVO();
						 vo.setId(id);
						 vo.setPwd(pwd);
						 vo.setName(name);
						 vo.setEmail(email);
						 vo.setJoinDate(joinDate);
						 
				//마지막으로 생성된 MemberVO객체를 차례대로 ArrayList배열에 반복해서 추가시킵니다.
				list.add(vo);
				
				//ArrayList가변길이 배열 모습
				//[ MemberVO,  MemberVO,  MemberVO ]
				//    0          1            2       index	
			}		
					
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			//순서9. DB작업 관련 객체 메모리를 모두 사용하고 난 다음  필요 없으면 메모리 낭비 이므로 메모리 톰캣에서 제거 
			ResourceClose();
		}
		
		return  list; //ArrayList 배열 메모리 자체를 member_action.jsp으로 반환
	}	
	
	
	
	
	
	
	
	//DB 작업 관련 객체 메모리들 사용이 끝난 후 자원 해제 하는 기능의 메소드
	public void ResourceClose() {		
		try {
			//ResultSet객체는 SQL문(SELECT)의 조회된 결과데이터를 임시로 저장하는 객체 입니다.
			//이 객체를 사용하고 난다음에  제거시키자.
			if(rs != null) rs.close();
			
			//PreparedStatement객체는 SQL문(SELECT)를 실행하는 객체 입니다.
			//이 객체를 사용하고 난 다음에 제거 시키자.
			if(pstmt != null) pstmt.close();
			
			//T4CConnection객체는  데이터베이스와의 연결을 관리하는 객체로, 더이상 데이터베이스 연결할 필요가 없으면 
			//이객체를 사용하고 난 다음에 제거 시키자.
			if(con != null) con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
}








