package member;

/*
================================================================
 [실습 3] MemberService 에 회원가입 업무 추가하기

 완성 목표 2가지
   1. isDuplicated() : 아이디 중복 검사를 DAO 에 전달
   2. join()         : 회원가입 업무 처리
                       (중복이면 가입 불가, 아니면 저장)

 ** Service 는 SQL 을 모른다. DAO 에게 시키고 결과만 받는다. **
 ** 주석 아래 빈 줄에 코드를 직접 작성한다. **
================================================================
*/
public class MemberService {

	// DB 작업을 맡길 MemberDAO 변수 memberDAO 선언
	private MemberDAO memberDAO;

	// 생성자 : MemberDAO 객체를 생성해 memberDAO 에 저장
	public MemberService() {
		memberDAO = new MemberDAO();
	}

	/*==============================================================
	  login() : 로그인 업무 (완성된 예제 - 아래 실습의 참고용)
	==============================================================*/
	public boolean login(MemberVO memberVO) {

		// DAO 에게 상자를 넘겨 회원 존재 여부를 물어보고 결과 저장
		boolean result = memberDAO.isExisted(memberVO);

		// 판정 결과를 Controller 에게 반환
		return result;
	}

	/*==============================================================
	  [실습 3-1] isDuplicated() : 아이디 중복 검사 업무
	  - 매개변수 : String id
	  - 반환값   : true = 이미 있는 아이디 / false = 사용 가능
	==============================================================*/
	// 중복 여부를 반환하는 public boolean isDuplicated(String id) 메소드 선언
	public boolean isDuplicated(String id) {

		// DAO 의 isDuplicated(id) 를 호출해 결과 저장
		boolean result =   memberDAO.isDuplicated(id);

		// 아이디 중복 여부 결과를 MemberService의 join 메소드 안으로  반환
		return result;

	}
	/*==============================================================
	  [실습 3-2] join() : 회원가입 업무 처리
	  - 매개변수 : MemberVO memberVO
	  - 반환값   : true = 가입 성공 / false = 중복이거나 실패

	  [업무 순서]
	    1) 아이디가 이미 있는지 확인한다
	    2) 있으면 가입시키지 않고 false 반환
	    3) 없으면 DAO 에게 저장을 시키고 그 결과를 반환
	  ** 이런 "순서 판단"이 Service 계층의 역할이다. **
	==============================================================*/
	// 가입 결과를 반환하는 public boolean join(MemberVO memberVO) 메소드 선언
	public boolean join(MemberVO memberVO) {

		// VO 상자에서 아이디를 꺼내 중복 검사 실행 (isDuplicated 호출)
		boolean duplicated   =  isDuplicated(memberVO.getId());

		// 이미 있는 아이디면 저장하지 않고 false 반환
		if(duplicated) { //아이디 중복
			return false;
		}

		// 중복이 아니면 DAO 에게 저장을 시키고 결과 저장
		boolean result  = memberDAO.insertMember(memberVO);

		// 가입할 아이디가 DB에 존재하지 않고, 회원 저장(추가,가입) 결과 => true 또는 false 를 MemberController 사장의 join 메소드 내부에  반환
		return result;
	}

	//아이디 중복 검사 기능 
	public int idCheck(String id) {
		
		//사원 인 MemberDAO야 ~ 입력한 아이디 전달 할테이 니가 DB작업해서 결과만 줘~ 
		return   memberDAO.idCheck(id);
		
		//=> 끝으로 MemberController 서블릿(사장)에게 보고 (반환)
	}

}//MemberService (부장)











