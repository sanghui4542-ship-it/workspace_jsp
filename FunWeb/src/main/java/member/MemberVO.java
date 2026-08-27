package member;


/*
================================================================
MemberVO : 회원 데이터 "운반" 전용 클래스   [MVC 에서의 위치 : Model]

[역할]
아이디, 비밀번호 같은 회원 정보를 변수 여러 개로 흩어 다니지 않고
객체 1개에 담아서 계층 사이(Controller -> Service -> DAO)를
이동시키는 상자 역할을 한다.
t_member 테이블의 한 행(row) = MemberVO 객체 하나에 대응된다.

[작성 규칙 3가지]
1. 변수는 private  : 외부에서 직접 못 만지게 감춘다.
2. 기본 생성자     : 프레임워크들이 객체를 만들 때 필요하다.
3. getter / setter : 감춘 변수를 꺼내고(get) 넣는(set) 공식 통로.
================================================================
*/
public class MemberVO {
	
	    private String id;  // 회원 아이디. t_member 테이블의 id 열에 대응된다.
	    private String pwd; // 비밀번호.   t_member 테이블의 pwd 열에 대응된다.
	    
	    public MemberVO() {} //기본생성자
	    
	    public MemberVO(String id, String pwd) { //회원 아이디, 비밀번호 초기화 생성자 
	    	this.id = id;
	    	this.pwd = pwd;
	    }
	    
	    //getter / setter 메소드들 
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
	    
	    
	
}










