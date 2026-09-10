package util;   // 이 파일이 속한 패키지(=폴더) 이름. 실제 폴더 구조와 반드시 같아야 한다

/*
 ================================================================================
   파일명 : DBCPUtil.java

   사용 환경 :
     - JSP / Servlet 기반 웹 프로젝트
     - WAS : Apache Tomcat
     - DB  : MySQL
     - 커넥션 풀 : Tomcat 내부 DBCP2

   이 파일의 목적 :
     1. Tomcat이 미리 생성해 둔 커넥션 풀(DataSource)을
        JNDI(Java Naming and Directory Interface)를 통해 가져온다.
     2. DAO에서 DB 연결(Connection)을 필요로 할 때
                   커넥션 풀에서 하나를 빌려준다.

   핵심 개념 요약 :

     우리는 DB 연결을 직접 만들지 않는다.
     우리는 Tomcat이 만들어 둔 DB 연결을 "빌려서 사용"한다.

 ================================================================================
*/


// DB 연결 객체 (SQL 실행을 위해 필요)
import java.sql.Connection;
import java.sql.SQLException;   // DB 작업이 실패했을 때 자바가 던지는 예외

// JNDI 관련 클래스 (서버 자원을 이름으로 찾기 위해 사용)
import javax.naming.Context;
import javax.naming.InitialContext;   // JNDI 검색을 시작하는 출발점
import javax.naming.NamingException;   // JNDI 이름을 못 찾았을 때의 예외

// DB 연결을 관리하는 표준 인터페이스
import javax.sql.DataSource;

import exception.DataAccessException;   // 직접 만든 예외 클래스


public class DBCPUtil {   // DB 연결을 빌려주고 트랜잭션을 굴려 주는 도구 모음 클래스

    /*
     =============================================================================
       1. DataSource 변수 선언

       DataSource란?

         → 여러 개의 DB 연결(Connection)을 내부에 보관하고 있는
           "커넥션 풀 관리자" 객체

	       여기서 중요한 점 :
	
	         우리가 new DataSource()로 생성하지 않는다.
	         Tomcat이 서버 시작 시 이미 생성해 둔다.
	
	       왜 static으로 선언할까?
	
	         - 이 객체는 애플리케이션 전체에서 하나만 존재해야 한다.
	         - 모든 DAO가 동일한 커넥션 풀을 공유해야 한다.
	         - 여러 개 만들면 DB 과부하 발생 가능
	
	       따라서 static으로 선언하여
	       프로그램 전체에서 하나만 사용하도록 한다.
     =============================================================================
    */
    private static DataSource dataSource;



    /*
     =============================================================================
       2. static 초기화 블럭

	       실행 시점 :
	
	         - 이 클래스가 JVM 메모리에 처음 로딩될 때
	         - 단 한 번만 실행된다.
	
	       즉,
	         DBCPUtil.getConnection()이 처음 호출되는 순간 이 블럭이 먼저 실행된다.
	
	       역할 :
	
	         Tomcat 내부 JNDI 저장소에서
	                      우리가 설정해 둔 DataSource를 찾아와
	        dataSource 변수에 저장한다.

     =============================================================================
    */
    static {

        try {

            /*
             ---------------------------------------------------------------------
              1단계 : InitialContext 생성

              InitialContext란?

                → JNDI 시스템에 접근하기 위한 시작 객체
                → 서버 내부 자원을 검색하기 위한 "입구"

		              쉽게 말하면 :
		                서버 내부 자원을 찾기 위한 검색 시작 버튼
             ---------------------------------------------------------------------
            */
            Context initContext = new InitialContext();



            /*
             ---------------------------------------------------------------------
              2단계 : java:comp/env 영역 접근

              Tomcat은 모든 자원(Resource)을
              "java:comp/env"라는 내부 영역에 저장한다.

		              구조 예시 (서버 내부 메모리 구조) :
		
		                  java:comp/env
		                      └── jdbc/jspbeginner
		                           └── DataSource 객체
		                                ├── Connection1
		                                ├── Connection2
		                                ├── Connection3
		                                └── ...

              				따라서 먼저 이 영역으로 들어가야 한다.
             ---------------------------------------------------------------------
            */
            Context envContext = (Context) initContext.lookup("java:comp/env");



            /*
             ---------------------------------------------------------------------
              3단계 : DataSource 검색

		              우리가 Tomcat의 context.xml 파일에
		              아래와 같이 설정했다고 가정하자:
		
		                  <Resource name="jdbc/jspbeginner"
		                            type="javax.sql.DataSource"
		                            ... />
		
		              이제 그 이름("jdbc/jspbeginner")으로 검색한다.
		
		              검색 결과 :
		
		                Tomcat이 미리 생성해 둔 커넥션 풀 객체 반환
		
		              그 객체를 DataSource 타입으로 형변환하여 저장
             ---------------------------------------------------------------------
            */
            dataSource = (DataSource) envContext.lookup("jdbc/jspbeginner");


            /*
		             여기까지 성공하면 :
		
		               dataSource 안에는
		               Tomcat이 관리하는 커넥션 풀이 들어 있다.
		
		               우리는 이제 이 객체를 통해 DB 연결을 빌려 쓸 수 있다.
            */

        } catch (NamingException e) {

            /*
             NamingException 발생 가능 상황 :

               1. context.xml에 Resource 등록 안 했을 경우
               2. name 오타
               3. Tomcat 설정 오류
               4. 서버 재시작 안 한 경우

             	이 경우 콘솔에 오류 출력
            */
            e.printStackTrace();
        }
    }



    /*
     =============================================================================
       3. Connection 반환 메소드

       DAO에서 사용 예:

           Connection con = DBCPUtil.getConnection();

	       내부 동작 과정 :
	
	         1. dataSource는 커넥션 풀을 관리하는 객체
	         2. getConnection() 호출 시
	         3. 이미 생성되어 있는 Connection 중 하나를 반환
	
	       중요한 점 :
	
	         이때 새로운 DB 연결을 만드는 것이 아니다.
	         이미 만들어 둔 연결을 "재사용"한다.
	
	       왜 빠른가?
	
	         DB 연결 생성은 매우 무거운 작업
	                       커넥션 풀은 그 비용을 줄이기 위해 존재한다.
     =============================================================================
    */
    public static Connection getConnection() throws SQLException {

        /*
		         내부 동작 흐름 :
		
		           - 커넥션 풀에 사용 가능한 연결이 있는지 확인
		           - 있으면 즉시 반환
		           - 없으면 대기
		           - 대기 시간 초과 시 SQLException 발생
		
		         사용 후 반드시 close() 호출해야 한다.
		
		         단,
		         close()는 실제 종료가 아니라
		         "사용 완료" 표시 후 풀로 반환하는 동작이다.
        */
        return dataSource.getConnection();
    }

    //===========================================================
    // 4. 여러 SQL 을 한 작업으로 묶어 실행 (트랜잭션)
    //    전부 성공하면 commit, 하나라도 실패하면 rollback.
    //    사용법:  DBCPUtil.execute(con -> { dao.a(con); dao.b(con); return null; });
    //===========================================================
    /*
      [트랜잭션이 무엇인가 - 은행 송금으로 이해하기]

        A 통장에서 1만원 빼기  +  B 통장에 1만원 넣기
        이 둘은 "반드시 같이 성공하거나 같이 실패해야" 한다.
        빼기만 성공하고 넣기가 실패하면 돈이 공중분해된다.

        이렇게 "쪼갤 수 없는 한 덩어리 작업" 을 트랜잭션이라고 한다.
          commit   = 여기까지 전부 성공했으니 확정한다
          rollback = 하나라도 실패했으니 전부 없던 일로 되돌린다

        이 프로젝트에서는 "글 삭제 + 그 글의 댓글 삭제" 같은 것이 여기 해당한다.
    */

    // interface = "이런 모양의 메소드를 갖추겠다" 는 약속만 적어 둔 것.
    // 실제 내용(무슨 SQL 을 실행할지)은 이걸 쓰는 Service 가 채운다.
    // <T> 는 "돌려줄 값의 종류는 쓰는 쪽이 정한다" 는 뜻이다(제네릭).
    public interface TxWork<T> {
        // 이 메소드 하나만 약속한다. con(연결)을 받아 작업하고 결과를 돌려준다.
        // throws Exception = 이 안에서 어떤 예외든 던져도 된다는 허락.
        T doInTransaction(Connection con) throws Exception;
    }

    // 실제로 트랜잭션을 굴려 주는 메소드.
    // Service 는 "무엇을 할지" 만 넘기고, 연결·commit·rollback·반납은 전부 여기서 처리한다.
    // 이렇게 모아 두면 Service 마다 try~catch~finally 를 반복해 쓰지 않아도 된다.
    public static <T> T execute(TxWork<T> work) {
        // 연결을 담을 변수를 미리 만들어 둔다. null 로 시작하는 이유는
        // 아래 finally 에서 "연결을 얻기도 전에 실패했는가" 를 구분해야 하기 때문이다.
        Connection con = null;
        try {
            // 커넥션 풀에서 연결을 하나 빌린다
            con = getConnection();
            con.setAutoCommit(false);          //이제부터는 commit() 전까지 확정되지 않는다
                                               // 기본값은 true 라서 SQL 한 줄마다 바로 확정된다.
                                               // false 로 꺼야 여러 SQL 을 한 덩어리로 묶을 수 있다.
            // Service 가 넘겨준 실제 작업(여러 개의 SQL)을 실행한다
            T result = work.doInTransaction(con);
            // 여기까지 예외 없이 왔다 = 전부 성공했다 -> 확정한다
            con.commit();
            // 작업 결과를 부르는 쪽에 돌려준다
            return result;
        // RuntimeException = 우리가 만든 업무 예외(ForbiddenException 등)가 여기 해당한다
        } catch (RuntimeException e) {
            rollbackQuietly(con);              //업무 예외는 타입을 유지한 채 되던진다
            // 그대로 다시 던진다. 감싸면 "권한 없음(403)" 이 "DB 오류(500)" 로 둔갑해 버린다.
            throw e;
        // 그 외 예외 = 대부분 SQLException 같은 진짜 DB 오류
        } catch (Exception e) {
            // 실패했으니 지금까지 한 것을 전부 되돌린다
            rollbackQuietly(con);
            // 우리 예외로 감싸서 던진다. 위 계층이 JDBC 를 몰라도 되게 하려는 것이다.
            throw new DataAccessException("DB 작업 처리 중 오류가 발생했습니다 : " + e.getMessage(), e);
        // finally = 성공하든 실패하든 무조건 실행되는 부분
        } finally {
            // 빌린 연결은 반드시 반납한다. 이걸 빠뜨리면 풀이 말라 서버가 멈춘다.
            closeQuietly(con);
        }
    }

    //===========================================================
    // 5. 조회 전용 (커밋할 것이 없어 연결만 빌려 실행)
    //===========================================================
    // select 만 하는 경우다. 데이터를 바꾸지 않으니 commit/rollback 이 필요 없고,
    // 연결을 빌려 주고 끝나면 반납하는 일만 한다.
    public static <T> T query(TxWork<T> work) {
        // 연결 변수 준비 (execute 와 같은 이유로 null 로 시작)
        Connection con = null;
        try {
            // 연결을 빌린다. setAutoCommit(false) 를 하지 않는 것이 execute 와의 차이다.
            con = getConnection();
            // 넘겨받은 조회 작업을 실행하고 그 결과를 그대로 돌려준다
            return work.doInTransaction(con);
        } catch (RuntimeException e) {
            // 업무 예외는 그대로 통과시킨다 (감싸면 원래 뜻이 사라진다)
            throw e;
        } catch (Exception e) {
            // 진짜 DB 오류는 우리 예외로 감싸 던진다
            throw new DataAccessException("DB 조회 중 오류가 발생했습니다 : " + e.getMessage(), e);
        } finally {
            // 조회든 저장이든 연결 반납은 똑같이 필수다
            closeQuietly(con);
        }
    }

    // "조용히(Quietly)" 라는 이름을 붙인 이유 :
    // 되돌리기에 또 실패해도 예외를 밖으로 던지지 않기 때문이다.
    // 이미 원래 예외를 던지는 중인데 여기서 새 예외를 던지면 진짜 원인이 가려진다.
    private static void rollbackQuietly(Connection con) {
        // 연결을 얻기도 전에 실패했으면 되돌릴 것도 없다
        if (con == null) return;
        // 되돌리기를 시도하고, 그것마저 실패하면 콘솔에만 남긴다
        try { con.rollback(); } catch (SQLException e) { System.out.println("[DBCPUtil] rollback 실패 : " + e.getMessage()); }
    }

    /** autoCommit 을 원래대로 돌려놓고 커넥션풀에 반납한다 (반납 전 원복이 중요) */
    private static void closeQuietly(Connection con) {
        // 빌린 적이 없으면 반납할 것도 없다
        if (con == null) return;
        // autoCommit 을 true 로 되돌린다.
        // 풀은 연결을 재사용하므로, false 인 채로 반납하면 다음에 빌려 가는 쪽이
        // 영문도 모르고 commit 이 안 되는 상태를 물려받는다. 찾기 매우 어려운 버그다.
        try { con.setAutoCommit(true); } catch (SQLException e) { }
        // close() 는 진짜 종료가 아니라 "다 썼습니다" 하고 풀에 돌려주는 동작이다
        try { con.close(); } catch (SQLException e) { }
    }
}



