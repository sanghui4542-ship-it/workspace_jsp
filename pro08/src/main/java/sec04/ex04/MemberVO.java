package sec04.ex04;
/*
================================================================
MemberVO 클래스 (VO : Value Object, 값 저장 전용 객체)
================================================================

[1] 이 클래스의 역할
- t_member 테이블의 "행(레코드) 1개" = "회원 1명의 정보"를
  자바 프로그램 안에서 담아 옮기기 위한 데이터 보관 전용 클래스.
- 사용 방향 2가지
  1) 조회 시 : DB에서 조회한 한 행의 열 값들을 -> MemberVO 객체 1개에 저장
  2) 추가 시 : 사용자가 입력한 값들을 MemberVO 객체 1개에 담아 -> DB에 INSERT
- 계산이나 판단 기능 없이 변수 + getter/setter만 가진다.
  (Spring 연결 지점 : 실무에서는 계층 간 데이터 전달 목적일 때 DTO라고도 부른다.)

[2] 테이블의 행과 이 클래스의 대응 관계

t_member 테이블의 한 행          MemberVO 객체 1개
+------+------+--------+-----+   +--------------------+
| ID   | PWD  | NAME   | ... |   | id       = "hong"  |
+------+------+--------+-----+   | pwd      = "1212"  |
| hong | 1212 | 홍길동   | ... |-> | name     = "홍길동"  |
+------+------+--------+-----+   | email    = ...     |
                                 | joinDate = ...     |
                                 +--------------------+
-> 행이 3개 조회되면 MemberVO 객체도 3개 만들어진다.

[3] VO 클래스를 만드는 방법 2단계
1단계. 테이블의 열(컬럼)과 같은 이름·같은 자료형으로 변수를 선언한다.
       (열과 변수가 1:1로 짝을 이뤄야 값을 옮겨 담기 쉽다.)
2단계. 변수 1개당 getter/setter 메소드를 각각 1개씩(총 2개씩) 만든다.
*/

import java.sql.Date;
//날짜 열(JOINDATE) 값을 담기 위한 클래스.
//주의 : 자바에는 Date가 2종류 있다.
//- java.util.Date : 자바의 일반 날짜+시간 클래스
//- java.sql.Date  : DB의 DATE 타입 전용 클래스 (JDBC가 사용)
//-> ResultSet의 getDate()가 java.sql.Date를 반환하므로 여기서는 sql쪽을 import한다.


public class MemberVO { //  t_member 테이블의 행 1개 (회원 1명)를 저장하는 클래스 
	
	//=====================================
	//1단계. 변수 선언 (테이블의 열 5개와 1:1 대응)
	//=====================================
	//private 선언 이유 (캡슐화)
	//- 외부 클래스가 변수에 직접 접근하는 것을 막고,
	//  반드시 아래의 getter/setter 메소드를 거쳐서만 읽고 쓰게 하기 위함.
	//- 직접 접근을 막으면 잘못된 값 저장을 메소드 안에서 걸러 낼수 있다.
	private String   id;     // ID 열값 저장.   예 : "hong"        
	private String  pwd;     // PWD 열값 저장.  예 : "1212"        
	private String  name;    // NAME 열값 저장. 예 : "홍길동"            
	private String  email;   // EMAIL 열값 저장. 예 : "hong@gmail.com"        
	private Date    joinDate;// JOINDATE 열값 저장. 예 : 2026-08-19 값이 저장된 new Date 객체 주소 저장
	
	//======================================
	//기본 생성자 (매개변수가 없는 생성자)
	//======================================
	public MemberVO() {  System.out.println("MemberVO 클래스의 기본생성자 호출되어 new MemberVO(...); 객체 생성됨");                }
	
	//=======================================
	//모든 인스턴스 변수 초기화 할 생성자 (단!!! joinDate인스턴스 변수 값은  insert 문장에서 sysdate 예약어로 추가 할것이다)
	//=======================================
	//단축키로 만들자 ->  alt + shift + s 누른 후  o
	public MemberVO(String id, String pwd, String name, String email) {
		super();
		this.id = id;
		this.pwd = pwd;
		this.name = name;
		this.email = email;
	}

	// ============================================================
	// 2단계. getter / setter 메소드 (변수 5개 x 2 = 총 10개)
	// ============================================================
	/*
	    getter 메소드란?
	    - private 변수에 저장된 값을 외부 클래스에게 "반환"해 주는 메소드.
	    - 작성 규칙 : public 변수자료형 get변수명() { return 변수명; }

	    setter 메소드란?
	    - private 변수의 값을 외부에서 전달받은 값으로 "변경"해 주는 메소드.
	    - 작성 규칙 : public void set변수명(자료형 매개변수) { this.변수 = 매개변수; }

	    이클립스 자동 생성 단축키 : alt + shift + s 누른 후 r
	*/
	//id 변수값을 외부클래스로 반환. 예: vo.getId(); ---> "hong" 반환
	public String getId() {
		return id;
	}
	//외부 클래스에서 매개변수로 받은 값을 id변수에 저장.  예:  vo.setId("hong");
	public void  setId(String id) {
		this.id = id;   //thid.id(객체의 변수)  =  id(매개변수);
	}

	// pwd 값을 외부로 반환
	public String getPwd() {
		return pwd;
	}
	// 외부에서 받은 값을 pwd 변수에 저장
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	// name 값을 외부로 반환
	public String getName() {
		return name;
	}
	// 외부에서 받은 값을 name 변수에 저장
	public void setName(String name) {
		this.name = name;
	}

	// email 값을 외부로 반환
	public String getEmail() {
		return email;
	}
	// 외부에서 받은 값을 email 변수에 저장
	public void setEmail(String email) {
		this.email = email;
	}

	// joinDate 값을 외부로 반환 (반환 타입이 java.sql.Date)
	public Date getJoinDate() {
		return joinDate;
	}
	// 외부에서 받은 날짜 값을 joinDate 변수에 저장
	public void setJoinDate(Date joinDate) {
		this.joinDate = joinDate;
	}
	
}










