package exception;   // 이 파일이 속한 패키지(=폴더) 이름. 실제 폴더 구조와 반드시 같아야 한다

/*
 ================================================================================
   DataAccessException  -  DB 작업 실패를 알리는 예외 (HTTP 500 대응)

   [왜 만들었나 - 기존 코드의 가장 위험한 습관]

     기존 DAO는 모든 메소드에서 이렇게 처리했다.

         } catch (Exception e) {
             System.out.println("...SQL문 실행 오류" + e);
         }
         return list;   // <- 비어 있는 목록을 그대로 반환

     무엇이 문제인가

       1. "실패"가 "성공(결과 0건)"처럼 보인다.
          DB가 죽어도 화면에는 "게시글이 없습니다"가 뜬다.
          사용자는 데이터가 사라진 줄 알고, 개발자는 원인을 못 찾는다.

       2. 예약 INSERT가 실패해도 화면은 "예약되었습니다"를 출력했다.
          (insertCarOrder는 void 반환이라 성공/실패를 알 방법조차 없었다)

       3. 오류 메시지가 System.out 으로만 나가 서버 콘솔을 닫으면 사라진다.

     실무 원칙 : 예외를 삼키지 않는다(swallow).
                복구할 수 없는 오류는 상위로 던지고, 한 곳에서 처리한다.

   [SQLException 을 왜 그대로 던지지 않는가]
     SQLException 은 checked exception 이라 Service·Controller의 모든 메소드에
     throws SQLException 을 붙여야 한다. 그러면 상위 계층이 JDBC 기술에 묶인다.
     그래서 DAO 경계에서 이 런타임 예외로 감싸 던진다.
     (Spring의 DataAccessException 도 같은 이유로 만들어진 것이다)
 ================================================================================
*/

// RuntimeException 을 물려받는 예외 클래스.
// DAO 에서 SQLException 이 터지면 이 예외로 감싸서 위(Service·Controller)로 던진다.
// "감싼다(wrapping)" 는 표현을 쓰는 이유 : 원래 예외를 버리지 않고 안에 품고 가기 때문이다.
public class DataAccessException extends RuntimeException {

	// 자바가 예외 객체를 저장/전송할 때 쓰는 버전 번호. 경고를 없애려고 형식적으로 적는다.
	private static final long serialVersionUID = 1L;

	// 생성자 1 : 설명 문장 + 진짜 원인이 된 예외를 함께 넘길 때 쓴다. (DAO 에서 주로 이걸 쓴다)
	//   예) catch (SQLException e) {
	//           throw new DataAccessException("차량 목록 조회 실패", e);
	//       }
	public DataAccessException(String message, Throwable cause) {
		// message = 사람이 읽을 설명, cause = 원래 터진 SQLException.
		// 둘 다 부모에게 넘겨 보관시키면 로그에 "차량 목록 조회 실패" 아래에
		// 진짜 SQL 오류 내용까지 이어서 찍힌다.
		super(message, cause);
	}

	// 생성자 2 : 원인이 될 예외가 따로 없고 설명 문장만 있을 때 쓴다.
	//   예) throw new DataAccessException("저장된 행이 0건입니다");
	public DataAccessException(String message) {
		// 부모에게 설명 문장만 넘긴다.
		super(message);
	}
}
