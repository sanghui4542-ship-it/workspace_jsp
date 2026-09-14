package Vo;   // 이 파일이 속한 패키지(=폴더) 이름. 실제 폴더 구조와 반드시 같아야 한다


//VO역할을 하는 검색된 차량 한대의 정보를 저장할 용도
//또는
//차량 정보 한대의 정보를 DB의 CarList테이블에 INSERT(추가)시킬 용도의 클래스

// [VO 가 무엇인가]
//   VO = Value Object(값 객체). "값을 담아 나르는 상자" 라고 생각하면 된다.
//   DB 의 carlist 테이블에서 한 줄(행)을 읽으면 컬럼이 8개 나오는데,
//   그 8개를 따로따로 들고 다니면 메소드 인자가 8개가 되어 관리가 안 된다.
//   그래서 8개를 한 상자에 담아 CarListVo 하나로 주고받는다.
//   테이블의 컬럼 = 이 클래스의 멤버변수, 라고 짝지어 보면 이해가 빠르다.
public class CarListVo {
//멤버변수

	// 멤버변수(=필드) : 이 상자에 담을 칸들이다. 전부 private 로 잠가 둔다.
	//   private = 다른 클래스에서 vo.carno 처럼 직접 못 건드린다는 뜻.
	//   대신 아래의 getter/setter 를 통해서만 넣고 꺼내게 한다.
	//   이렇게 잠그는 것을 캡슐화(encapsulation)라고 부른다.
    private int carno; // 차량 고유번호 (carlist 테이블의 기본키 PK. 숫자라서 int)
    private String carname; // 차량명 (글자라서 String. 예: "그랜저")
    private String carcompany; // 제조사 (현재, 기아 등)
    private int carprice; //차 한대 하루 렌트 가격 (원 단위 숫자)
    private int carusepeople; //탑승 가능 인원 (명 수)
    private String carinfo; // 차량 설명 (긴 문장)
    private String carimg;// 차량 이미지 파일명 (예: "grandeur.jpg". 이미지 자체가 아니라 "이름" 만 저장)
    private String carcategory; //차량 등급 (소형:Small/중형:Mid/대형:Big)

//생성자

    //기본 생성자
    // 생성자 = new CarListVo() 라고 쓸 때 실행되는 부분. 상자를 처음 만들 때 호출된다.
    // 안이 비어 있는 이유 : 값은 나중에 setter 로 하나씩 채울 것이기 때문이다.
    // DAO 가 DB 를 읽을 때 이 방식(빈 상자 만들고 setter 로 채우기)을 쓴다.
    // ※ 아래처럼 인자 있는 생성자를 하나라도 만들면 자바가 기본 생성자를 자동으로 만들어 주지 않는다.
    //    그래서 필요하면 이렇게 직접 적어 줘야 한다.
    public CarListVo() {}

    //모든 인스턴스변수 값 초기화 할 생성자
    // 값 8개를 한꺼번에 넣으면서 상자를 만드는 방법.
    //   예) new CarListVo(1, "그랜저", "현대", 90000, 5, "설명", "gr.jpg", "Big")
	public CarListVo(int carno, String carname, String carcompany,
					 int carprice, int carusepeople, String carinfo,
					 String carimg, String carcategory) {
		// super() = 부모 클래스(Object)의 생성자를 먼저 호출한다는 뜻.
		// 안 적어도 자바가 자동으로 넣어 주지만, 이클립스가 생성자를 만들어 줄 때 함께 적어 준다.
		super();   // 부모 클래스의 생성자를 먼저 부른다. 안 적어도 자동으로 실행되는 형식적인 줄이다
		// this.carno = 이 상자의 carno 칸,  오른쪽 carno = 괄호로 받은 값.
		// 이름이 같아서 헷갈리기 때문에 "이 상자의 것" 이라는 표시로 this. 를 붙인다.
		this.carno = carno;              // 받은 차량 고유번호를 carno 칸에 넣는다
		this.carname = carname;          // 받은 차량명을 이 상자의 carname 칸에 넣는다
		this.carcompany = carcompany;    // 받은 제조사를 carcompany 칸에 넣는다
		this.carprice = carprice;        // 받은 하루 렌트 가격을 carprice 칸에 넣는다
		this.carusepeople = carusepeople;// 받은 탑승 인원을 carusepeople 칸에 넣는다
		this.carinfo = carinfo;          // 받은 차량 설명을 carinfo 칸에 넣는다
		this.carimg = carimg;            // 받은 이미지 파일명을 carimg 칸에 넣는다
		this.carcategory = carcategory;  // 받은 차량 등급을 carcategory 칸에 넣는다
	}

	//getter,setter메소드

	// [getter / setter 가 무엇인가 - 여기서 한 번만 자세히 설명한다]
	//   멤버변수를 private 로 잠갔기 때문에 바깥에서 직접 못 읽고 못 쓴다.
	//   대신 "꺼내 주는 메소드(getter)" 와 "넣어 주는 메소드(setter)" 를 열어 준다.
	//
	//     getter : get + 변수명(첫 글자 대문자),  값을 return 한다.        예) getCarno()
	//     setter : set + 변수명(첫 글자 대문자),  값을 받아 저장한다.       예) setCarno(3)
	//
	//   왜 이렇게 귀찮게 하는가?
	//     1) 나중에 "가격은 0보다 커야 한다" 같은 검사를 setter 안에 한 줄만 넣으면
	//        모든 코드가 그 검사를 거치게 된다. 직접 접근을 허용하면 그럴 수가 없다.
	//     2) JSP 의 ${vo.carname} 은 사실 vo.getCarname() 을 대신 불러 주는 문법이다.
	//        즉 getter 이름을 규칙대로 지어야 화면에서 ${...} 로 꺼낼 수 있다.
	//
	//   아래는 이 규칙을 멤버변수 8개에 대해 똑같이 반복한 것이다.

	// carno(차량 고유번호) 값을 꺼내 준다. JSP 의 ${vo.carno} 가 이 메소드를 부른다.
	public int getCarno() {
		return carno;   // 이 상자의 carno 칸에 든 값을 그대로 돌려준다
	}

	// carno 칸에 값을 넣어 준다. DAO 가 DB 에서 읽은 값을 여기에 담는다.
	public void setCarno(int carno) {
		this.carno = carno;   // 받은 값을 이 상자의 carno 칸에 저장
	}

	// carname(차량명) 값을 꺼내 준다. ${vo.carname}
	public String getCarname() {
		return carname;   // carname 칸에 든 값을 그대로 돌려준다  — 차량명 (글자라서 String. 예: "그랜저")
	}

	// carname 칸에 차량명을 넣어 준다.
	public void setCarname(String carname) {
		this.carname = carname;   // 받은 차량명 (글자라서 String. 예: "그랜저") 를 이 상자의 carname 칸에 저장
	}

	// carcompany(제조사) 값을 꺼내 준다. ${vo.carcompany}
	public String getCarcompany() {
		return carcompany;   // carcompany 칸에 든 값을 그대로 돌려준다  — 제조사 (현재, 기아 등)
	}

	// carcompany 칸에 제조사를 넣어 준다.
	public void setCarcompany(String carcompany) {
		this.carcompany = carcompany;   // 받은 제조사 (현재, 기아 등) 를 이 상자의 carcompany 칸에 저장
	}

	// carprice(하루 렌트 가격) 값을 꺼내 준다. ${vo.carprice}
	public int getCarprice() {
		return carprice;   // carprice 칸에 든 값을 그대로 돌려준다  — 차 한대 하루 렌트 가격 (원 단위 숫자)
	}

	// carprice 칸에 가격을 넣어 준다.
	public void setCarprice(int carprice) {
		this.carprice = carprice;   // 받은 차 한대 하루 렌트 가격 (원 단위 숫자) 를 이 상자의 carprice 칸에 저장
	}

	// carusepeople(탑승 가능 인원) 값을 꺼내 준다. ${vo.carusepeople}
	public int getCarusepeople() {
		return carusepeople;   // carusepeople 칸에 든 값을 그대로 돌려준다  — 탑승 가능 인원 (명 수)
	}

	// carusepeople 칸에 탑승 인원을 넣어 준다.
	public void setCarusepeople(int carusepeople) {
		this.carusepeople = carusepeople;   // 받은 탑승 가능 인원 (명 수) 를 이 상자의 carusepeople 칸에 저장
	}

	// carinfo(차량 설명) 값을 꺼내 준다. ${vo.carinfo}
	public String getCarinfo() {
		return carinfo;   // carinfo 칸에 든 값을 그대로 돌려준다  — 차량 설명 (긴 문장)
	}

	// carinfo 칸에 설명을 넣어 준다.
	public void setCarinfo(String carinfo) {
		this.carinfo = carinfo;   // 받은 차량 설명 (긴 문장) 를 이 상자의 carinfo 칸에 저장
	}

	// carimg(이미지 파일명) 값을 꺼내 준다.
	// 화면에서는 <img src="${contextPath}/img/${vo.carimg}"> 처럼 폴더 경로와 합쳐서 쓴다.
	public String getCarimg() {
		return carimg;   // carimg 칸에 든 값을 그대로 돌려준다  — 차량 이미지 파일명 (예: "grandeur.jpg". 이미지 자체가 아니라 "이름" 만 저장)
	}

	// carimg 칸에 이미지 파일명을 넣어 준다.
	public void setCarimg(String carimg) {
		this.carimg = carimg;   // 받은 차량 이미지 파일명 (예: "grandeur.jpg". 이미지 자체가 아니라 "이름" 만 저장) 를 이 상자의 carimg 칸에 저장
	}

	// carcategory(차량 등급 Small/Mid/Big) 값을 꺼내 준다.
	public String getCarcategory() {
		return carcategory;   // carcategory 칸에 든 값을 그대로 돌려준다  — 차량 등급 (소형:Small/중형:Mid/대형:Big)
	}

	// carcategory 칸에 등급을 넣어 준다.
	public void setCarcategory(String carcategory) {
		this.carcategory = carcategory;   // 받은 차량 등급 (소형:Small/중형:Mid/대형:Big) 를 이 상자의 carcategory 칸에 저장
	}




}   // CarListVo 클래스 끝






