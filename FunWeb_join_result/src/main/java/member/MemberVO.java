package member;

/*
================================================================
 [실습 1] MemberVO 에 회원가입용 항목 추가하기

 지금은 id, pwd 만 있다. 회원가입 화면(join.jsp)에서 입력받는
 name, email 을 추가로 담을 수 있게 확장하는 것이 목표다.

 ** 주석 아래 빈 줄에 코드를 직접 작성한다. **
================================================================
*/
public class MemberVO {

	// 회원 아이디를 저장할 private String 변수 id 선언
	private String id;

	// 비밀번호를 저장할 private String 변수 pwd 선언
	private String pwd;

	// 이름을 저장할 private String 변수 name 선언
	private String name;

	// 이메일을 저장할 private String 변수 email 선언
	private String email;
	

	// 기본 생성자 (매개변수 없음) 작성
	public MemberVO() {}

	// 로그인용 생성자 : id, pwd 두 개를 받아 this 로 초기화
	public MemberVO(String id, String pwd) {
		this.id = id;
		this.pwd = pwd;
	}

	// 회원가입용 생성자 : id, pwd, name, email 네 개를 받아 this 로 초기화
	public MemberVO(String id, String pwd, String name, String email) {
		this.id = id;
		this.pwd = pwd;
		this.name = name;
		this.email = email;
	}
	


	// id 를 꺼내는 getId() 메소드 작성
	public String getId() {
		return id;
	}

	// id 를 넣는 setId(String id) 메소드 작성
	public void setId(String id) {
		this.id = id;
	}

	// pwd 를 꺼내는 getPwd() 메소드 작성
	public String getPwd() {
		return pwd;
	}

	// pwd 를 넣는 setPwd(String pwd) 메소드 작성
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	// name 을 꺼내는 getName() 메소드 작성
	public String getName() {
		return name;
	}

	// name 을 넣는 setName(String name) 메소드 작성
	public void setName(String name) {
		this.name = name;
	}

	// email 을 꺼내는 getEmail() 메소드 작성
	public String getEmail() {
		return email;
	}

	// email 을 넣는 setEmail(String email) 메소드 작성
	public void setEmail(String email) {
		this.email = email;
	}


}//MemberVO






