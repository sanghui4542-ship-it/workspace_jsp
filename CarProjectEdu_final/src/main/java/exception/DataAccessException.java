package exception;

/*
 * ============================================================================
 *  DataAccessException  -  DB 작업이 실패했을 때 던지는 예외
 *
 *  던지는 곳 : util/DBCPUtil 의 execute(), query()
 *              (SQLException 같은 DB 오류를 이 예외로 포장해서 던진다)
 *  받는 곳   : 사장(BaseController) 의 handle() -> 500 에러 화면
 *
 *  RuntimeException 을 상속하므로 메소드마다 throws 를 적지 않아도 된다
 * ============================================================================
 */
public class DataAccessException extends RuntimeException {

	// 직렬화 버전 번호 (예외 클래스가 요구하는 값, 경고 방지용)
	private static final long serialVersionUID = 1L;

	//----------------------------------------------------------------
	// 생성자 : 오류 문구(message)와 원래 오류(cause)를 함께 담는다
	//  사용 예) throw new DataAccessException("DB 작업 처리 중 오류", e);
	//----------------------------------------------------------------
	public DataAccessException(String message, Throwable cause) {

		// 부모(RuntimeException)에게 오류 문구와 원래 오류를 넘겨 저장한다
		super(message, cause);
	}
}
