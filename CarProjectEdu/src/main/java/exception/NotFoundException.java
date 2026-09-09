package exception;   // 이 파일이 속한 패키지(=폴더) 이름. 실제 폴더 구조와 반드시 같아야 한다

/*
 ================================================================================
   NotFoundException  -  요청한 자원이 없을 때의 예외 (HTTP 404 대응)

   [왜 만들었나]
     1. 존재하지 않는 글번호로 상세보기를 요청하면
        DAO가 null을 반환하고, JSP가 그 null의 getter를 호출해 NPE 500이 났다.
            BoardVo vo = (BoardVo)request.getAttribute("vo");
            String name = vo.getB_name();   // <- vo가 null이면 NPE

     2. /Car/없는주소 처럼 매칭되지 않는 2단계 주소를 요청하면
        nextPage 변수가 null인 채로 forward(null)이 호출되어 역시 NPE 500이 났다.

     "없는 것"은 500(서버 고장)이 아니라 404(자원 없음)로 답해야 한다.
 ================================================================================
*/

// public          : 어느 패키지에서든 이 클래스를 쓸 수 있다는 표시
// class           : 클래스(설계도)를 만든다는 뜻
// NotFoundException : 클래스 이름. 파일 이름과 반드시 같아야 한다(NotFoundException.java)
// extends RuntimeException : "물려받는다" 는 뜻.
//                   RuntimeException 이 이미 갖고 있는 기능(메시지 보관, 스택 추적 등)을
//                   그대로 받아 쓰고, 우리는 이름만 새로 붙이는 것이다.
//                   RuntimeException 계열을 물려받으면 이 예외를 던지는 메소드마다
//                   throws 를 적지 않아도 되어 Service·DAO 코드가 깨끗해진다.
public class NotFoundException extends RuntimeException {

	// 자바가 예외 객체를 파일로 저장하거나 네트워크로 보낼 때 쓰는 "버전 번호".
	// 안 적어도 동작은 하지만 이클립스가 노란 경고를 띄우기 때문에 형식적으로 1L 을 적어 둔다.
	//   private = 이 클래스 안에서만 접근 가능
	//   static  = 객체를 만들지 않아도 클래스에 하나만 존재
	//   final   = 한 번 정한 값을 바꿀 수 없음
	//   1L      = long 타입 숫자 1 (뒤의 L 이 long 이라는 표시)
	private static final long serialVersionUID = 1L;

	// 생성자(Constructor) : new NotFoundException("...") 라고 쓸 때 실행되는 부분.
	// message 자리에 "무엇을 못 찾았는지" 설명 문장을 넣는다.
	//   예) throw new NotFoundException("글번호 " + b_idx + " 를 찾을 수 없습니다");
	public NotFoundException(String message) {
		// super(...) = 부모 클래스(RuntimeException)의 생성자를 대신 호출한다.
		// 넘긴 설명 문장은 부모가 보관해 두고, 나중에 getMessage() 로 꺼내 쓸 수 있다.
		// BaseController 가 이 문장을 받아 404 화면에 뿌린다.
		super(message);
	}
}
