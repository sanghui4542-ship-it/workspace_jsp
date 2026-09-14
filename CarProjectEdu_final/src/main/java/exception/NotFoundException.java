package exception;

/*
 * ============================================================================
 *  NotFoundException  -  찾는 대상이 없을 때 던지는 예외 (404 없음)
 *
 *  던지는 곳 예) 없는 요청 주소 (BaseController), 없는 글번호 (게시판 Service),
 *               없는 첨부파일 (util/FileRepo)
 *  받는 곳   : 사장(BaseController) 의 handle() -> 404 에러 화면
 * ============================================================================
 */
public class NotFoundException extends RuntimeException {

	// 직렬화 버전 번호 (예외 클래스가 요구하는 값, 경고 방지용)
	private static final long serialVersionUID = 1L;

	//----------------------------------------------------------------
	// 생성자 : 고객에게 보여줄 오류 문구(message)를 담는다
	//  사용 예) throw new NotFoundException("글이 존재하지 않습니다");
	//----------------------------------------------------------------
	public NotFoundException(String message) {

		// 부모(RuntimeException)에게 오류 문구를 넘겨 저장한다
		super(message);
	}
}
