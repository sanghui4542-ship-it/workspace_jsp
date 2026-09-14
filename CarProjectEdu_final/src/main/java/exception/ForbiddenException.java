package exception;

/*
 * ============================================================================
 *  ForbiddenException  -  권한이 없는 요청일 때 던지는 예외 (403 금지)
 *
 *  던지는 곳 예) 로그인하지 않고 로그인 전용 주소를 요청했을 때 (BaseController)
 *               남의 글을 수정·삭제하려고 할 때 (게시판 Service)
 *  받는 곳   : 사장(BaseController) 의 handle() -> 403 에러 화면
 * ============================================================================
 */
public class ForbiddenException extends RuntimeException {

	// 직렬화 버전 번호 (예외 클래스가 요구하는 값, 경고 방지용)
	private static final long serialVersionUID = 1L;

	//----------------------------------------------------------------
	// 생성자 : 고객에게 보여줄 오류 문구(message)를 담는다
	//  사용 예) throw new ForbiddenException("로그인이 필요한 기능입니다");
	//----------------------------------------------------------------
	public ForbiddenException(String message) {

		// 부모(RuntimeException)에게 오류 문구를 넘겨 저장한다
		super(message);
	}
}
