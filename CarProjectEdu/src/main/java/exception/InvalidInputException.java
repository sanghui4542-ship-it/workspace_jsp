package exception;   // 이 파일이 속한 패키지(=폴더) 이름. 실제 폴더 구조와 반드시 같아야 한다

/*
 ================================================================================
   InvalidInputException  -  잘못된 "요청 값" 예외 (HTTP 400 대응)

   [왜 만들었나]
     기존 코드는 아래처럼 검증 없이 형변환을 했다.

         int carno = Integer.parseInt(request.getParameter("carno"));

     주소창에서 carno를 지우거나 문자를 넣으면 NumberFormatException이 그대로 터져
     톰캣 기본 500 에러 페이지(자바 스택트레이스)가 사용자에게 노출됐다.

     입력이 잘못된 것은 "서버 잘못(500)"이 아니라 "요청 잘못(400)"이다.
     그래서 입력 검증 실패는 이 예외로 통일해서 던지고,
     BaseController가 400으로 응답하도록 한 곳에서 처리한다.

   [실무 포인트]
     RuntimeException을 상속하면 메소드마다 throws를 붙이지 않아도 되어
     Service/DAO 코드가 검증 코드로 지저분해지지 않는다.
 ================================================================================
*/

// RuntimeException 을 물려받는 예외 클래스.
// ParamUtil 이 파라미터를 숫자로 바꾸다 실패하면 이 예외를 던진다.
public class InvalidInputException extends RuntimeException {

	// 자바가 예외 객체를 저장/전송할 때 쓰는 버전 번호. 경고를 없애려고 형식적으로 적는다.
	private static final long serialVersionUID = 1L;

	// 생성자 1 : 설명 문장만 넘길 때 쓴다.
	//   예) throw new InvalidInputException("차량번호(carno)가 없습니다");
	public InvalidInputException(String message) {
		// 부모에게 설명 문장을 넘겨 보관시킨다.
		super(message);
	}

	// 생성자 2 : 설명 문장 + "진짜 원인이 된 예외" 를 함께 넘길 때 쓴다.
	//   예) catch (NumberFormatException e) {
	//           throw new InvalidInputException("carno 는 숫자여야 합니다", e);
	//       }
	// 원인(e)을 같이 넘겨 두면 서버 로그에 "무엇 때문에 터졌는지" 까지 함께 찍혀서
	// 나중에 원인을 추적하기 쉽다. 이것을 예외 체이닝(chaining)이라고 부른다.
	public InvalidInputException(String message, Throwable cause) {
		// cause = 이 예외가 터지게 만든 원래 예외. 함께 보관한다.
		// Throwable 은 모든 예외(Exception)와 오류(Error)의 부모 타입이라
		// 어떤 종류의 예외가 와도 받을 수 있다.
		super(message, cause);
	}
}
