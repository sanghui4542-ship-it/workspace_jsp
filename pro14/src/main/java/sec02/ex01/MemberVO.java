package sec02.ex01;

import java.sql.Date;

/*

VO(Value Object)역할을 하는 클래스?
- DB의 테이블에서 조회한 행(레코드)의 열 값을 조회 해서 가져와 변수에 저장하거나
  변수에 저장된 값들을 클래스의 객체를 생성해서 한번에 DB의 테이블에 추가할 역할.

VO 역할을 하는 클래스를 만드는 방법
1. 테이블에서 조회한 레코드의 열(컬럼)값을 변수에 저장해야하므로 
      테이블의 열명과 동일한 이름과 자료형으로 변수를 만든다.
   
2. getter, setter 역할을 하는 메소드들을 변수 하나당 각각 2개씩 만든다.   
*/

public class MemberVO { // <- t_member 테이블의  하나의 레코드(회원 한사람의 정보)가 저장되는 클래스 

	//변수
    private String id;     // 회원 아이디
    private String pwd;    // 회원 비밀번호 
    private String name;   // 회원 이름
    private String email;  // 회원 이메일 
    private Date  joinDate;// 회원 가입날짜 
    
    //생성자? 기본생성자 직접 만들어 놓자
    public MemberVO() {
    	System.out.println("MemberVO 생성자 호출되어 객체 생성됨");
    }
    
    //id, pwd, name, email 인스턴스 변수값들을 모두 저장(초기화) 시킬 생성자 
    //생성자 만들기 단축키 -> alt + shift + s  o	
    public MemberVO(String id, String pwd, String name, String email) {
		this.id = id;
		this.pwd = pwd;
		this.name = name;
		this.email = email;
	}

	//getter, setter 메소드
	/*
	  getter 역할을 하는 메소드란?
	  	private 으로 선언된 위 변수값을 외부클래스에 반환할 목적으로 사용됨
	
	  setter 역할을 하는 메소드란?
	  	private 으로 선언된 위 변수값을 새롭게 변경할 목적으로 사용됨
	
	 getter, setter 메소드 만드는 방법 단축 키
	 -> alt  + shift  + s   r
	*/   
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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
	public Date getJoinDate() {
		return joinDate;
	}
	public void setJoinDate(Date joinDate) {
		this.joinDate = joinDate;
	}	
	
	
}
