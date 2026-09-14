package Vo;

/*
 * ============================================================================
 *  MemberVO  -  상자(VO) : 회원 1명의 정보를 담아 나르는 객체
 *
 *  사장(Controller) <-> 부장(Service) <-> 사원(DAO) 사이에서
 *  회원 정보를 한 번에 주고받기 위해 사용한다.
 *
 *  변수 이름은 DB member 테이블의 열 이름과 같다
 *    id, pass, name, age, gender, address, email, tel, hp
 *
 *  getXxx() : 상자에서 값을 꺼낸다      예) vo.getName()
 *  setXxx() : 상자에 값을 넣는다(바꾼다)  예) vo.setName("홍길동")
 * ============================================================================
 */
public class MemberVO {

	// 회원 정보를 저장할 변수들 (private : 이 클래스 밖에서는 get/set 메소드로만 사용할 수 있다)
	private String id, pass, name;
	private int age;
	private String gender, address, email, tel, hp;

	//----------------------------------------------------------------
	// 기본 생성자 : 빈 상자를 만든다. 값은 나중에 setXxx() 로 넣는다
	//  사용 예) MemberVO vo = new MemberVO();  vo.setId("hong");
	//----------------------------------------------------------------
	public MemberVO() {}

	//----------------------------------------------------------------
	// 생성자 : 상자를 만들면서 회원 정보 9개를 한 번에 넣는다
	//  사용 예) 부장(MemberService) 의 회원가입, 카카오 자동가입
	//----------------------------------------------------------------
	public MemberVO(String id, String pass, String name, int age, String gender, String address, String email,
					String tel, String hp) {

		// 부모 클래스(Object)의 생성자를 먼저 부른다
		super();

		// 매개변수로 받은 값을 이 상자의 변수에 저장한다 (this.id = 상자의 id 변수)
		this.id = id;
		this.pass = pass;
		this.name = name;
		this.age = age;
		this.gender = gender;
		this.address = address;
		this.email = email;
		this.tel = tel;
		this.hp = hp;
	}

	// 아이디 꺼내기 / 넣기
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	// 비밀번호 꺼내기 / 넣기
	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	// 이름 꺼내기 / 넣기
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	// 나이 꺼내기 / 넣기
	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	// 성별 꺼내기 / 넣기
	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	// 주소 꺼내기 / 넣기
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	// 이메일 꺼내기 / 넣기
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	// 전화번호 꺼내기 / 넣기
	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	// 휴대폰번호 꺼내기 / 넣기
	public String getHp() {
		return hp;
	}

	public void setHp(String hp) {
		this.hp = hp;
	}
}
