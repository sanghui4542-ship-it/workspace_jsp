package member;

/*
================================================================
MemberService : 업무 규칙 담당 "중간" 계층   [MVC 에서의 위치 : Model]

[역할]
Controller 와 DAO 사이에서 "로그인이라는 업무"를 담당한다.
- Controller 는 요청/응답만 알고, DB를 몰라야 한다.
- DAO 는 SQL 만 알고, 업무 순서를 몰라야 한다.
- 그 사이의 업무 절차(무엇을 어떤 순서로 확인할지)가
  바로 이 Service 의 자리다.

[지금은 한 줄뿐인데 왜 만드나?]
현재 login()은 DAO 호출 한 줄이지만, 실무에서는 이 자리에
  - 로그인 실패 5회 시 잠금 처리
  - 마지막 접속 시각 기록
  - 비밀번호 암호화 비교
같은 "업무 규칙"들이 쌓인다. 계층을 미리 분리해 두면
규칙이 늘어나도 Controller 와 DAO 는 그대로 유지된다.

[호출 관계]
MemberController --> MemberService --> MemberDAO --> MySQL 8
================================================================
*/

public class MemberService { //부장
	
	/* DB 접근을 맡길 DAO 사원 객체를 담을 변수 */
	private MemberDAO memberDAO;
	
	/* MemberController의 init 메소드내부에서 호출하는 생성자로 new MemberService(); 객체가 만들어질때 
	   new MemberDAO(); 객체도 함께 만들어서 저장할 생성자 */
	public MemberService() {
		memberDAO = new MemberDAO();
	}
	
	/*==============================================================
	  login() : 로그인 업무 처리

	  매개변수  memberVO : MemberController 가 포장해 준 아이디/비밀번호 상자
	  반환값    true = 로그인 성공 / false = 실패
	==============================================================*/
	public boolean login(MemberVO memberVO) {
		
		/* DAO 사원에게 MemberVO 상자를 그대로 전달해 회원 존재 여부를 물어 본다.*/
		boolean result = memberDAO.isExisted(memberVO);
		
		/* 판정 결과를 MemberController 사장에게 보고(돌려준다) */
		return result;
		
	}
}















