package exception;

/*
 * ============================================================================
 *  InvalidInputException  -  고객이 보낸 입력값이 잘못됐을 때 던지는 예외 (400 요청 잘못)
 *
 *  던지는 곳 예) 필수 입력값이 없을 때, 숫자 자리에 글자가 왔을 때 (util/ParamUtil)
 *               비밀번호를 비워서 가입하려고 할 때 (MemberService)
 *  받는 곳   : 사장(BaseController) 의 handle() -> 400 에러 화면
 * ============================================================================
 */
public class InvalidInputException extends RuntimeException {

	// 직렬화 버전 번호 (예외 클래스가 요구하는 값, 경고 방지용)
	private static final long serialVersionUID = 1L;

	//----------------------------------------------------------------
	// 생성자 : 고객에게 보여줄 오류 문구(message)를 담는다
	//  사용 예) throw new InvalidInputException("필수 입력값이 없습니다 : id");
	//----------------------------------------------------------------
	public InvalidInputException(String message) {

		// 부모(RuntimeException)에게 오류 문구를 넘겨 저장한다
		super(message);
	}
}
