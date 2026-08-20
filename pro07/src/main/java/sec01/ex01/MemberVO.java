package sec01.ex01;

import java.sql.Date;

public class MemberVO { //  t_member 테이블의 행 1개 (회원 1명)를 저장하는 클래스
	
	//======================================
	//1단계. 변수 선언 (테이블의 열 5개와 1:1 대응)
	//======================================
	//private 선언 이유 (캡슐화)
	//- 외부 클래스가 변수에 직접 접근하는 것을 막고,
	//  반드시 아래의 getter/setter 메소드를 거쳐서만 읽고 쓰게 하기 위함.
	//- 직접 접근을 막으면 잘못된 값 저장을 메소드 안에서 걸러 낼 수 있다.
	private String id;    			//ID 열값 저장.  예 : "hong"		
	private String pwd;   			//PWD 열값 저장. 예 : "1212"            
	private String name;            //NAME 열값 저장. 예 : "홍길동   
	private String email;           //EMAIL 열값 저장. 예 : "hong@gamil.com"  
	private Date joindate;          //JOINDATE 열값 저장. 예 : 2026-08-19 값이 저장된 new Date 객체 주소 저장

	//========================================
	//기본 생성자(매개변수가 없는 생성자)
	//========================================
	public MemberVO() { System.out.println("MemberVO 클래스의 기본생성자 호출되어 new MemberVO(...); 객체 생성됨");}

	public String getId() {
		return id;
	
	}
	//외부 클래스에서 매개변수로 받은 값을 id변수에 저장. 예: vo.setId("hong");
	public void setId(String id) {
		this.id = id;   //this.id (객체의 변수) = id(매개변수);
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getJoindate() {
		return joindate;
	}

	public void setJoindate(Date joindate) {
		this.joindate = joindate;
	}
	
}
