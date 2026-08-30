package member;
/* package : 이 클래스가 어느 폴더(묶음)에 속하는지 알려주는 선언이다.
   member 폴더 안에 있으므로 member 라고 적었다.
   같은 package 안의 클래스끼리는 import 없이 서로를 바로 사용할 수 있다.
   그래서 MemberDAO 가 MemberVO 를 쓸 때 import 문이 없는 것이다. */

/*
================================================================
 [복습 1] MemberVO.java

 ── 이 파일이 왜 필요한가 ──────────────────────────────────
 회원 1명의 정보는 아이디, 비밀번호, 이름, 이메일 네 가지다.
 이 값들을 메소드에 넘길 때마다 따로따로 적으면 이렇게 된다.

   login(String id, String pwd)                          매개변수 2개
   join(String id, String pwd, String name, String email) 매개변수 4개
   -> 항목이 늘어날 때마다 모든 메소드를 고쳐야 한다.

 그래서 네 값을 담는 "상자" 클래스를 하나 만들고
 상자만 주고받는다.

   login(MemberVO memberVO)
   join(MemberVO memberVO)     -> 항목이 늘어도 메소드 모양은 그대로!

 ── VO 라는 이름의 뜻 ──────────────────────────────────────
 VO = Value Object (값 객체)
 데이터를 담아 옮기는 것만 담당하는 클래스를 부르는 이름이다.
 계산도 하지 않고, DB 도 모르고, 화면도 모른다.

 ── 테이블과의 관계 ────────────────────────────────────────
   t_member 테이블의 한 행(row)  =  MemberVO 객체 하나

   id       pwd    name     email             <- 테이블의 열(column)
   ------   ----   ------   ---------------
   hong     1234   홍길동   hong@test.com     <- 이 한 줄이 객체 하나가 된다

 ── 캡슐화 규칙 ────────────────────────────────────────────
 변수는 전부 private 으로 숨기고,
 값을 넣고 꺼내는 일은 메소드로만 한다.
   private  : 이 클래스 안에서만 사용 가능
   getter   : 값을 꺼내는 메소드 (get + 변수이름)
   setter   : 값을 넣는 메소드   (set + 변수이름)

 왜 이렇게 하는가?
   변수를 public 으로 열어 두면 어디서든 마음대로 바꿀 수 있어
   값이 잘못됐을 때 원인을 찾기 어렵다.
   메소드를 통하게 하면 나중에 검사 코드를 넣을 자리가 생긴다.
   예) setPwd 안에서 "4글자 미만이면 저장하지 않는다" 같은 규칙 추가
================================================================
*/
public class MemberVO {

	/*--------------------------------------------------------------
	  1. 필드(멤버 변수) : 상자에 담기는 값들
	--------------------------------------------------------------*/

	private String id;
	/* 회원 아이디를 저장한다.
	   t_member 테이블의 id 열과 짝이 된다.
	   String : 글자를 저장하는 타입 */

	private String pwd;
	/* 비밀번호를 저장한다. t_member 의 pwd 열과 짝이 된다. */

	private String name;
	/* 이름을 저장한다. t_member 의 name 열과 짝이 된다. */

	private String email;
	/* 이메일을 저장한다. t_member 의 email 열과 짝이 된다.
	   아이디/비밀번호 찾기에서 본인 확인 수단으로 사용된다. */

	/*--------------------------------------------------------------
	  2. 생성자 3개 : 상자를 만드는 방법이 세 가지다

	  생성자란?
	    객체를 만들 때(new 할 때) 딱 한 번 실행되는 특별한 메소드.
	    클래스 이름과 똑같이 짓고, 반환 타입을 적지 않는다.

	  왜 세 개나 만드는가?
	    상황마다 알고 있는 정보의 개수가 다르기 때문이다.
	    이렇게 같은 이름의 메소드를 매개변수만 다르게
	    여러 개 만드는 것을 "오버로딩" 이라고 한다.
	--------------------------------------------------------------*/

	public MemberVO() {

		/* 기본 생성자 : 매개변수가 없는 생성자.
		   빈 상자를 먼저 만들고 setter 로 하나씩 채울 때 사용한다.

		   [실제 사용 위치]
		     MemberDAO 의 selectMember()
		       DB 에서 값을 꺼내며 하나씩 담아야 해서 빈 상자가 필요하다.
		     MemberController 의 modify()
		       수정할 값 4개를 setter 로 담는다.

		   ** 생성자를 하나도 안 만들면 자바가 기본 생성자를
		      자동으로 만들어 준다. 하지만 아래처럼 매개변수 있는
		      생성자를 만들면 자동 생성이 사라지므로
		      이렇게 직접 적어 줘야 한다. ** */

	}

	public MemberVO(String id, String pwd) {

		this.id = id;
		/* this.id  : 이 객체가 가진 변수 (위에서 선언한 private String id)
		   id       : 지금 넘어온 매개변수
		   두 이름이 같아서 구분이 필요하다. this 가 그 구분 표시다.

		   ** this 를 빼고 id = id; 라고 쓰면?
		      매개변수에 매개변수를 넣는 꼴이라
		      객체의 변수는 계속 null 로 남는다. 흔한 실수다. ** */

		this.pwd = pwd;
		/* 비밀번호도 같은 방식으로 저장한다. */

		/* [로그인용 생성자]
		   로그인은 아이디와 비밀번호만 있으면 되므로 2개짜리다.

		   [실제 사용 위치]
		     MemberController 의 login()   : new MemberVO(id, pass)
		     MemberController 의 withdraw() : new MemberVO(id, pwd)
		     -> 탈퇴도 본인 확인에 두 값만 쓰므로 이 생성자를 쓴다. */

	}

	public MemberVO(String id, String pwd, String name, String email) {

		this.id = id;
		this.pwd = pwd;
		this.name = name;
		this.email = email;
		/* 네 값을 모두 저장한다.

		   [회원가입용 생성자]
		   가입할 때는 네 항목을 모두 입력받으므로 4개짜리가 필요하다.

		   [실제 사용 위치]
		     MemberController 의 join()
		       new MemberVO(id, pass, name, email)

		   ** 위의 2개짜리 생성자와 이름이 같지만
		      매개변수 개수가 달라서 자바가 구분할 수 있다.
		      new MemberVO("a","b")        -> 2개짜리 실행
		      new MemberVO("a","b","c","d") -> 4개짜리 실행 ** */

	}

	/*--------------------------------------------------------------
	  3. getter / setter 8개

	  getter : 값을 꺼내 반환한다     -> return 변수;
	  setter : 값을 넣는다            -> this.변수 = 매개변수;

	  ** 이클립스 단축키 **
	    변수를 선언한 뒤 마우스 오른쪽 클릭
	    -> Source -> Generate Getters and Setters
	    를 고르면 자동으로 만들어 준다.
	    직접 타이핑하다 오타를 내는 것보다 안전하다.
	--------------------------------------------------------------*/

	public String getId() {

		return id;
		/* id 변수의 값을 그대로 돌려준다.

		   [누가 이 메소드를 부르는가]
		     MemberDAO 의 isExisted(), insertMember(), updateMember()
		       -> SQL 의 물음표에 채울 값을 꺼낼 때
		     MemberService 의 join(), modify(), withdraw()
		       -> 중복 검사나 삭제 대상 아이디를 꺼낼 때 */

	}

	public void setId(String id) {

		this.id = id;
		/* void : 돌려줄 값이 없다는 뜻이다. 저장만 하기 때문이다.

		   [누가 이 메소드를 부르는가]
		     MemberDAO 의 selectMember() : DB 에서 꺼낸 값을 담을 때
		     MemberController 의 modify() : 세션의 아이디를 담을 때 */

	}

	public String getPwd() {

		return pwd;
		/* 비밀번호를 꺼낸다.
		   로그인 검증, 회원가입 저장, 수정에서 사용된다. */

	}

	public void setPwd(String pwd) {

		this.pwd = pwd;
		/* 비밀번호를 담는다. */

	}

	public String getName() {

		return name;
		/* 이름을 꺼낸다.
		   회원가입 저장, 수정, 아이디 찾기에서 사용된다. */

	}

	public void setName(String name) {

		this.name = name;
		/* 이름을 담는다. */

	}

	public String getEmail() {

		return email;
		/* 이메일을 꺼낸다.
		   회원가입 저장, 수정, 찾기 기능에서 사용된다. */

	}

	public void setEmail(String email) {

		this.email = email;
		/* 이메일을 담는다. */

	}

}//MemberVO

/*
================================================================
 [혼자 확인해 보기]  답을 가리고 스스로 설명해 보자

  1. 생성자가 3개인데 자바는 어떻게 구분해서 실행하는가?
     -> 매개변수의 개수와 타입으로 구분한다 (오버로딩)

  2. this.id = id; 에서 this 를 빼면 어떻게 되는가?
     -> 매개변수에 매개변수를 대입하는 꼴이라
        객체의 id 변수는 계속 null 로 남는다

  3. 변수를 private 으로 감추는 이유는?
     -> 아무 데서나 값을 바꾸지 못하게 하고,
        나중에 검사 규칙을 넣을 자리를 남겨 두기 위해서다

  4. void 와 String 반환 타입의 차이는?
     -> void 는 돌려줄 값이 없다(setter),
        String 은 글자 하나를 돌려준다(getter)

 [연결해서 보기]
   이 상자가 어디로 가는지 따라가 보자.
     MemberController.login()  에서 상자를 만든다
       -> MemberService.login() 으로 넘어간다
         -> MemberDAO.isExisted() 로 넘어간다
           -> 거기서 getId(), getPwd() 로 값이 꺼내진다
================================================================
*/
