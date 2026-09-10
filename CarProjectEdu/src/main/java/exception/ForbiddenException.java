package exception;   // 이 파일이 속한 패키지(=폴더) 이름. 실제 폴더 구조와 반드시 같아야 한다

/*
 ================================================================================
   ForbiddenException  -  권한이 없을 때의 예외 (HTTP 403 대응)

   [왜 만들었나]
     기존 게시판은 "글 비밀번호 확인"을 아래 방식으로 처리했다.

        1) 비밀번호 입력 -> AJAX(/Board/password.do) 로 확인
        2) 맞으면 자바스크립트로 [수정][삭제] 버튼을 보이게 함
        3) 버튼을 누르면 /Board/updateBoard.do , /Board/deleteBoard.do 요청

     문제는 3번 요청이 비밀번호를 다시 확인하지 않는다는 점이다.
     즉 2번(화면 버튼)을 건너뛰고 3번 주소로 직접 요청하면
     비밀번호를 몰라도 남의 글을 수정/삭제할 수 있었다.

     화면에서 버튼을 숨기는 것은 "안내"일 뿐 "보안"이 아니다.
     권한 검사는 반드시 서버에서 다시 해야 하고, 실패하면 이 예외를 던진다.
 ================================================================================
*/

// RuntimeException 을 물려받는(extends) 예외 클래스.
// 하는 일은 "이름을 붙이는 것" 하나다.
// 똑같이 예외를 던져도 이름이 ForbiddenException 이면
// BaseController 가 "아, 권한 문제구나" 하고 403 으로 응답할 수 있다.
// 이렇게 예외마다 이름을 나눠 두면 잡는 쪽에서 종류별로 다르게 처리할 수 있다.
public class ForbiddenException extends RuntimeException {

	// 자바가 예외 객체를 저장/전송할 때 쓰는 버전 번호.
	// 없어도 동작하지만 이클립스 경고를 없애려고 형식적으로 1L 을 적어 둔다.
	private static final long serialVersionUID = 1L;

	// 생성자 : new ForbiddenException("...") 로 만들 때 실행된다.
	// message 에는 "왜 막혔는지" 를 적는다.
	//   예) throw new ForbiddenException("비밀번호가 일치하지 않습니다");
	//   예) throw new ForbiddenException("로그인이 필요합니다");
	public ForbiddenException(String message) {
		// 부모(RuntimeException)에게 설명 문장을 넘겨 보관시킨다.
		// 나중에 e.getMessage() 로 이 문장을 다시 꺼낼 수 있다.
		super(message);
	}
}
