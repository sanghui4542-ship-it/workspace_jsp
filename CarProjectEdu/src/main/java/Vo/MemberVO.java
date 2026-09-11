// 이 파일이 속한 폴더(패키지) 이름. 실제 폴더 경로 Vo 와 반드시 같아야 한다
package Vo;   // 이 파일이 속한 패키지(=폴더) 이름. 실제 폴더 구조와 반드시 같아야 한다
// java.sql 안의 Date 를 이 파일에서 쓰겠다는 선언
import java.sql.Date;   // DB 의 날짜(date) 타입을 자바에서 다루는 타입
/*
	MemberVO 클래스 
	1. VO란? Value Object의 줄임말
	2. 하는역할 : 
		- 회원 한 사람의 정보를 저장하는 "데이터 상자"
		- DB에서 조회한 데이터를 담을 때 사용 
		- 회원가입 시 입력한 데이터를 임시 저장할 때 사용 
*/
public class MemberVO {
	//================================
	//1. 회원 한 사람의 정보들 저장할 용도의 변수 들 
	//================================
	private String id, pass, name;   //아이디, 비밀번호, 이름 
	private Date reg_date;           //회원가입한 날짜
	private int age;                 //나이 
	private String gender, address, email, tel, hp; //성별, 주소, 이메일, 전화번호, 휴대폰번호
	//===========================
	//2. 기본 생성자
	//===========================
	//객체만 먼저 만들때 사용
	public MemberVO() {}
	//============================
	//3. 회원가입용 생성자
	//===========================
	//가입시 입력한 정보들을 저장할 용도의 생성자
	//(reg_date는  DB에서 now()함수를 사용하여 insert시 자동으로 저장되는 경우가 많음)
	public MemberVO(String id, String pass, String name, int age, String gender, String address, String email,
					String tel, String hp) {
		// super( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
		super();   // 부모 클래스의 생성자를 먼저 부른다. 안 적어도 자동으로 실행되는 형식적인 줄이다
		// id 에 계산한 값을 담는다
		this.id = id;   // 받은 아이디, 비밀번호, 이름 를 이 상자의 id 칸에 저장
		// pass 에 계산한 값을 담는다
		this.pass = pass;   // 받은 아이디, 비밀번호, 이름 를 이 상자의 pass 칸에 저장
		// name 에 계산한 값을 담는다
		this.name = name;   // 받은 아이디, 비밀번호, 이름 를 이 상자의 name 칸에 저장
		// age 에 계산한 값을 담는다
		this.age = age;   // 받은 나이 를 이 상자의 age 칸에 저장
		// gender 에 계산한 값을 담는다
		this.gender = gender;   // 받은 성별, 주소, 이메일, 전화번호, 휴대폰번호 를 이 상자의 gender 칸에 저장
		// address 에 계산한 값을 담는다
		this.address = address;   // 받은 성별, 주소, 이메일, 전화번호, 휴대폰번호 를 이 상자의 address 칸에 저장
		// email 에 계산한 값을 담는다
		this.email = email;   // 받은 성별, 주소, 이메일, 전화번호, 휴대폰번호 를 이 상자의 email 칸에 저장
		// tel 에 계산한 값을 담는다
		this.tel = tel;   // 받은 성별, 주소, 이메일, 전화번호, 휴대폰번호 를 이 상자의 tel 칸에 저장
		// hp 에 계산한 값을 담는다
		this.hp = hp;   // 받은 성별, 주소, 이메일, 전화번호, 휴대폰번호 를 이 상자의 hp 칸에 저장
	}
	//===============================
	//4. DB 조회용 생성자 
	//================================
	//DB에서 조회한 회원 한사람 정보가 저장될 생성자 (reg_date)도 필요
	public MemberVO(String id, String pass, String name, Date reg_date, int age, String gender, String address,
			String email, String tel, String hp) {
		// super( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
		super();   // 부모 클래스의 생성자를 먼저 부른다. 안 적어도 자동으로 실행되는 형식적인 줄이다
		// id 에 계산한 값을 담는다
		this.id = id;   // 받은 아이디, 비밀번호, 이름 를 이 상자의 id 칸에 저장
		// pass 에 계산한 값을 담는다
		this.pass = pass;   // 받은 아이디, 비밀번호, 이름 를 이 상자의 pass 칸에 저장
		// name 에 계산한 값을 담는다
		this.name = name;   // 받은 아이디, 비밀번호, 이름 를 이 상자의 name 칸에 저장
		// reg_date 에 계산한 값을 담는다
		this.reg_date = reg_date;   // 받은 회원가입한 날짜 를 이 상자의 reg_date 칸에 저장
		// age 에 계산한 값을 담는다
		this.age = age;   // 받은 나이 를 이 상자의 age 칸에 저장
		// gender 에 계산한 값을 담는다
		this.gender = gender;   // 받은 성별, 주소, 이메일, 전화번호, 휴대폰번호 를 이 상자의 gender 칸에 저장
		// address 에 계산한 값을 담는다
		this.address = address;   // 받은 성별, 주소, 이메일, 전화번호, 휴대폰번호 를 이 상자의 address 칸에 저장
		// email 에 계산한 값을 담는다
		this.email = email;   // 받은 성별, 주소, 이메일, 전화번호, 휴대폰번호 를 이 상자의 email 칸에 저장
		// tel 에 계산한 값을 담는다
		this.tel = tel;   // 받은 성별, 주소, 이메일, 전화번호, 휴대폰번호 를 이 상자의 tel 칸에 저장
		// hp 에 계산한 값을 담는다
		this.hp = hp;   // 받은 성별, 주소, 이메일, 전화번호, 휴대폰번호 를 이 상자의 hp 칸에 저장
	}
	//============================================
	//6. getter  /  setter 메소드
	//==========================================
	// getter  : 값을 꺼낼때 사용
	// setter  : 값을 저장(변경)할때 사용 
	// id 값을 꺼내 준다.  — 아이디, 비밀번호, 이름
	public String getId() {
		return id;   // id 칸에 든 값을 그대로 돌려준다  — 아이디, 비밀번호, 이름
	}
	// id 칸에 값을 넣어 준다.  — 아이디, 비밀번호, 이름
	public void setId(String id) {
		// id 에 계산한 값을 담는다
		this.id = id;   // 받은 아이디, 비밀번호, 이름 를 이 상자의 id 칸에 저장
	}
	// pass 값을 꺼내 준다.  — 아이디, 비밀번호, 이름
	public String getPass() {
		return pass;   // pass 칸에 든 값을 그대로 돌려준다  — 아이디, 비밀번호, 이름
	}
	// pass 칸에 값을 넣어 준다.  — 아이디, 비밀번호, 이름
	public void setPass(String pass) {
		// pass 에 계산한 값을 담는다
		this.pass = pass;   // 받은 아이디, 비밀번호, 이름 를 이 상자의 pass 칸에 저장
	}
	// name 값을 꺼내 준다.  — 아이디, 비밀번호, 이름
	public String getName() {
		return name;   // name 칸에 든 값을 그대로 돌려준다  — 아이디, 비밀번호, 이름
	}
	// name 칸에 값을 넣어 준다.  — 아이디, 비밀번호, 이름
	public void setName(String name) {
		// name 에 계산한 값을 담는다
		this.name = name;   // 받은 아이디, 비밀번호, 이름 를 이 상자의 name 칸에 저장
	}
	// reg_date 값을 꺼내 준다.  — 회원가입한 날짜
	public Date getReg_date() {
		return reg_date;   // reg_date 칸에 든 값을 그대로 돌려준다  — 회원가입한 날짜
	}
	// reg_date 칸에 값을 넣어 준다.  — 회원가입한 날짜
	public void setReg_date(Date reg_date) {
		// reg_date 에 계산한 값을 담는다
		this.reg_date = reg_date;   // 받은 회원가입한 날짜 를 이 상자의 reg_date 칸에 저장
	}
	// age 값을 꺼내 준다.  — 나이
	public int getAge() {
		return age;   // age 칸에 든 값을 그대로 돌려준다  — 나이
	}
	// age 칸에 값을 넣어 준다.  — 나이
	public void setAge(int age) {
		// age 에 계산한 값을 담는다
		this.age = age;   // 받은 나이 를 이 상자의 age 칸에 저장
	}
	// gender 값을 꺼내 준다.  — 성별, 주소, 이메일, 전화번호, 휴대폰번호
	public String getGender() {
		return gender;   // gender 칸에 든 값을 그대로 돌려준다  — 성별, 주소, 이메일, 전화번호, 휴대폰번호
	}
	// gender 칸에 값을 넣어 준다.  — 성별, 주소, 이메일, 전화번호, 휴대폰번호
	public void setGender(String gender) {
		// gender 에 계산한 값을 담는다
		this.gender = gender;   // 받은 성별, 주소, 이메일, 전화번호, 휴대폰번호 를 이 상자의 gender 칸에 저장
	}
	// address 값을 꺼내 준다.  — 성별, 주소, 이메일, 전화번호, 휴대폰번호
	public String getAddress() {
		return address;   // address 칸에 든 값을 그대로 돌려준다  — 성별, 주소, 이메일, 전화번호, 휴대폰번호
	}
	// address 칸에 값을 넣어 준다.  — 성별, 주소, 이메일, 전화번호, 휴대폰번호
	public void setAddress(String address) {
		// address 에 계산한 값을 담는다
		this.address = address;   // 받은 성별, 주소, 이메일, 전화번호, 휴대폰번호 를 이 상자의 address 칸에 저장
	}
	// email 값을 꺼내 준다.  — 성별, 주소, 이메일, 전화번호, 휴대폰번호
	public String getEmail() {
		return email;   // email 칸에 든 값을 그대로 돌려준다  — 성별, 주소, 이메일, 전화번호, 휴대폰번호
	}
	// email 칸에 값을 넣어 준다.  — 성별, 주소, 이메일, 전화번호, 휴대폰번호
	public void setEmail(String email) {
		// email 에 계산한 값을 담는다
		this.email = email;   // 받은 성별, 주소, 이메일, 전화번호, 휴대폰번호 를 이 상자의 email 칸에 저장
	}
	// tel 값을 꺼내 준다.  — 성별, 주소, 이메일, 전화번호, 휴대폰번호
	public String getTel() {
		return tel;   // tel 칸에 든 값을 그대로 돌려준다  — 성별, 주소, 이메일, 전화번호, 휴대폰번호
	}
	// tel 칸에 값을 넣어 준다.  — 성별, 주소, 이메일, 전화번호, 휴대폰번호
	public void setTel(String tel) {
		// tel 에 계산한 값을 담는다
		this.tel = tel;   // 받은 성별, 주소, 이메일, 전화번호, 휴대폰번호 를 이 상자의 tel 칸에 저장
	}
	// hp 값을 꺼내 준다.  — 성별, 주소, 이메일, 전화번호, 휴대폰번호
	public String getHp() {
		return hp;   // hp 칸에 든 값을 그대로 돌려준다  — 성별, 주소, 이메일, 전화번호, 휴대폰번호
	}
	// hp 칸에 값을 넣어 준다.  — 성별, 주소, 이메일, 전화번호, 휴대폰번호
	public void setHp(String hp) {
		// hp 에 계산한 값을 담는다
		this.hp = hp;   // 받은 성별, 주소, 이메일, 전화번호, 휴대폰번호 를 이 상자의 hp 칸에 저장
	}
}