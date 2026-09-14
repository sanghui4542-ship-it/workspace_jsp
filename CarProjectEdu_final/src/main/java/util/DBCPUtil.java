package util;

/*
 * ============================================================================
 *  DBCPUtil  -  DB 연결을 빌려주고 돌려받는 도우미 클래스
 *
 *  DB 연결은 우리가 만들지 않는다.
 *  Tomcat 이 서버를 켤 때 META-INF/context.xml 설정을 읽고 DB 연결을 미리 여러 개 만들어 둔다.
 *  이렇게 미리 만들어 둔 연결 보관함을 커넥션 풀이라고 한다.
 *
 *      커넥션 풀 (이름 : jdbc/jspbeginner)
 *      +--------+--------+--------+--------+
 *      | 연결1  | 연결2  | 연결3  |  ...   |
 *      +--------+--------+--------+--------+
 *           |  빌려주기            ^  돌려받기
 *           v                      |
 *      부장(Service) : DBCPUtil.query(...) 또는 DBCPUtil.execute(...)
 *
 *  부장(Service)에서 쓰는 방법
 *    조회(select)만 할 때
 *        MemberVO vo = DBCPUtil.query(con -> memberDao.memberDetail(con, id));
 *
 *    추가·수정·삭제(insert, update, delete)를 할 때
 *        int result = DBCPUtil.execute(con -> memberDao.insertMember(con, memberVo));
 *
 *    con -> ... 는 "DB 연결(con)을 받아서 -> 뒤의 일을 해라" 라는 뜻이다 (람다식)
 * ============================================================================
 */

// DB 연결, DB 예외
import java.sql.Connection;
import java.sql.SQLException;

// 서버 안의 자원을 이름으로 찾는 도구 (JNDI)
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

// 커넥션 풀을 다루는 객체
import javax.sql.DataSource;

// 우리가 직접 만든 DB 오류 예외
import exception.DataAccessException;

public class DBCPUtil {

	// 커넥션 풀을 가리킬 변수
	// static : 프로그램 전체에 1개만 만들어져서 모든 부장(Service)이 같은 커넥션 풀을 함께 쓴다
	private static DataSource dataSource;

	//----------------------------------------------------------------
	// static 블럭 : DBCPUtil 을 처음 사용하는 순간 딱 1번 자동으로 실행된다
	//  context.xml 에 적어 둔 <Resource name="jdbc/jspbeginner" ...> 커넥션 풀을 찾아 dataSource 에 저장한다
	//
	//  찾아가는 길 (폴더에 들어가듯이)
	//    InitialContext  ->  java:comp/env  ->  jdbc/jspbeginner
	//       (입구)          (Tomcat 자원 방)     (우리 커넥션 풀)
	//----------------------------------------------------------------
	static {

		try {

			// 1단계 : 서버 자원을 찾기 위한 입구를 연다
			Context initContext = new InitialContext();

			// 2단계 : Tomcat 자원이 모여 있는 방 "java:comp/env" 로 들어간다
			Context envContext = (Context) initContext.lookup("java:comp/env");

			// 3단계 : 그 방에서 "jdbc/jspbeginner" 이름의 커넥션 풀을 찾아 dataSource 변수에 저장한다
			dataSource = (DataSource) envContext.lookup("jdbc/jspbeginner");

		// 이름으로 찾지 못했을 때 (context.xml 에 Resource 가 없음, 이름 오타, 서버 재시작 안 함)
		} catch (NamingException e) {

			// 이클립스 콘솔에 오류 내용을 출력한다
			e.printStackTrace();
		}
	}

	//----------------------------------------------------------------
	// getConnection : 커넥션 풀에서 DB 연결 1개를 빌려준다
	//  새로 만드는 것이 아니라 미리 만들어 둔 연결을 꺼내 주므로 빠르다
	//----------------------------------------------------------------
	public static Connection getConnection() throws SQLException {

		// 커넥션 풀(dataSource)에서 연결 1개를 꺼내 반환
		return dataSource.getConnection();
	}

	//----------------------------------------------------------------
	// TxWork : "DB 연결(con)을 받아서 할 일" 의 모양을 정해 둔 약속(interface)
	//  무슨 SQL 을 실행할지는 부장(Service)이 con -> ... 람다식으로 채워서 넘긴다
	//  <T> : 돌려줄 값의 종류는 쓰는 쪽이 정한다 (int, boolean, MemberVO 등)
	//----------------------------------------------------------------
	public interface TxWork<T> {

		// DB 연결(con)을 받아서 일을 하고 결과(T)를 반환한다
		T doInTransaction(Connection con) throws Exception;
	}

	//----------------------------------------------------------------
	// execute : 추가·수정·삭제(insert, update, delete)를 한 덩어리(트랜잭션)로 실행한다
	//
	//  트랜잭션 = 은행 송금처럼 "전부 성공" 아니면 "전부 취소" 해야 하는 작업
	//    commit   : 전부 성공했으니 DB 에 확정한다
	//    rollback : 하나라도 실패했으니 전부 없던 일로 되돌린다
	//
	//  흐름
	//    연결 빌리기 -> 자동 확정 끄기 -> 부장이 넘긴 일 실행 -> 성공 commit / 실패 rollback -> 연결 반납
	//----------------------------------------------------------------
	public static <T> T execute(TxWork<T> work) {

		// 빌린 연결을 저장할 변수 (아직 빌리지 않았으므로 null)
		Connection con = null;

		try {

			// 커넥션 풀에서 연결 1개를 빌린다
			con = getConnection();

			// 자동 확정을 끈다 -> 이제부터 SQL 을 실행해도 commit() 전까지는 DB 에 확정되지 않는다
			con.setAutoCommit(false);

			// 부장(Service)이 넘겨준 일(SQL 실행)을 하고 결과를 result 에 저장한다
			T result = work.doInTransaction(con);

			// 오류 없이 여기까지 왔으면 전부 성공이므로 DB 에 확정한다
			con.commit();

			// 결과를 부장(Service)으로 반환
			return result;

		// 우리가 만든 업무 예외(ForbiddenException 등)가 났을 때
		} catch (RuntimeException e) {

			// 지금까지 실행한 SQL 을 전부 되돌린다
			rollbackQuietly(con);

			// 받은 예외를 그대로 다시 던진다 (403 이 500 으로 바뀌지 않게 하기 위해)
			throw e;

		// 그 밖의 예외(대부분 SQLException = DB 오류)가 났을 때
		} catch (Exception e) {

			// 지금까지 실행한 SQL 을 전부 되돌린다
			rollbackQuietly(con);

			// 우리가 만든 DataAccessException 으로 포장해서 던진다
			throw new DataAccessException("DB 작업 처리 중 오류가 발생했습니다 : " + e.getMessage(), e);

		// 성공하든 실패하든 마지막에 반드시 실행된다
		} finally {

			// 빌린 연결을 커넥션 풀에 반납한다 (반납하지 않으면 연결이 바닥나 서버가 멈춘다)
			closeQuietly(con);
		}
	}

	//----------------------------------------------------------------
	// query : 조회(select)만 실행한다
	//  데이터를 바꾸지 않으므로 commit, rollback 없이 "빌리기 -> 실행 -> 반납" 만 한다
	//----------------------------------------------------------------
	public static <T> T query(TxWork<T> work) {

		// 빌린 연결을 저장할 변수 (아직 빌리지 않았으므로 null)
		Connection con = null;

		try {

			// 커넥션 풀에서 연결 1개를 빌린다
			con = getConnection();

			// 부장(Service)이 넘겨준 조회 작업을 실행하고 결과를 부장으로 반환
			return work.doInTransaction(con);

		// 우리가 만든 업무 예외는 그대로 다시 던진다
		} catch (RuntimeException e) {

			throw e;

		// DB 오류는 DataAccessException 으로 포장해서 던진다
		} catch (Exception e) {

			throw new DataAccessException("DB 조회 중 오류가 발생했습니다 : " + e.getMessage(), e);

		// 성공하든 실패하든 마지막에 반드시 실행된다
		} finally {

			// 빌린 연결을 커넥션 풀에 반납한다
			closeQuietly(con);
		}
	}

	//----------------------------------------------------------------
	// rollbackQuietly : 실행한 SQL 을 되돌린다
	//  Quietly(조용히) : 되돌리기마저 실패해도 예외를 던지지 않는다 (원래 오류가 가려지지 않게 하기 위해)
	//----------------------------------------------------------------
	private static void rollbackQuietly(Connection con) {

		// 연결을 빌리기 전에 실패했다면 되돌릴 것이 없으므로 끝낸다
		if (con == null) return;

		// 되돌리기를 시도하고, 실패하면 콘솔에 메시지만 출력한다
		try { con.rollback(); } catch (SQLException e) { System.out.println("[DBCPUtil] rollback 실패 : " + e.getMessage()); }
	}

	//----------------------------------------------------------------
	// closeQuietly : 자동 확정을 원래대로(true) 돌려놓고 연결을 커넥션 풀에 반납한다
	//----------------------------------------------------------------
	private static void closeQuietly(Connection con) {

		// 빌린 연결이 없으면 반납할 것이 없으므로 끝낸다
		if (con == null) return;

		// 자동 확정을 다시 켠다 (다음에 이 연결을 빌려 가는 곳이 자동 확정이 꺼진 상태를 물려받지 않도록)
		try { con.setAutoCommit(true); } catch (SQLException e) { }

		// 연결을 커넥션 풀에 반납한다 (close() 는 연결을 끊는 것이 아니라 "다 썼어요" 하고 돌려주는 것)
		try { con.close(); } catch (SQLException e) { }
	}
}
