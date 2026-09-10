package Vo;   // 이 파일이 속한 패키지(=폴더) 이름. 실제 폴더 구조와 반드시 같아야 한다


//렌트 예약 한  하나의 정보가 저장되는 VO클래스 
public class CarOrderVO {

//멤버변수 	
	private int orderid; //렌트 예약 아이디
	private String id;   // 예약한 회원의 아이디. 비회원 예약이면 null 이다
	
	/*
	차번호를 이용해 2개의 테이블(carlist테이블, non_carorder테이블)의 열정보를 
	JOIN해서 조회 해서 저장할 변수들
	*/
	private int carno; //렌트 예약한 차번호
	private int carqty; //렌트 예약 수량
	private int carreserveday; //렌트 예약 대여기간
	private String carbegindate; //렌트 예약 대여날짜
	
	private int carins;//보험 적용 여부 값   1(적용) 또는 0(미적용)
	private int carwifi;//무선 WIFI 적용 여부 값   1(적용) 또는 0(미적용)
	private int carnave;//네비게이션   적용 여부 값   1(적용) 또는 0(미적용)
	private int carbabyseat;//베이비시트  적용 여부 값   1(적용) 또는 0(미적용)
	
	private String memberphone; //비회원으로 예약시 입력한 폰번호 저장
	private String memberpass; //비회원으로 예약시 입력한 예약 비밀번호 저장 
	
//생성자
	public CarOrderVO() {}

	public CarOrderVO(int orderid, String id, int carno, int carqty, int carreserveday, String carbegindate, int carins,   // 값 12개를 한꺼번에 넣으면서 상자를 만드는 생성자
			int carwifi, int carnave, int carbabyseat, String memberphone, String memberpass) {
		super();   // 부모 클래스의 생성자를 먼저 부른다. 안 적어도 자동으로 실행되는 형식적인 줄이다
		this.orderid = orderid;   // 받은 렌트 예약 아이디 를 이 상자의 orderid 칸에 저장
		this.id = id;   // 받은 값을 이 상자의 id 칸에 저장
		this.carno = carno;   // 받은 렌트 예약한 차번호 를 이 상자의 carno 칸에 저장
		this.carqty = carqty;   // 받은 렌트 예약 수량 를 이 상자의 carqty 칸에 저장
		this.carreserveday = carreserveday;   // 받은 렌트 예약 대여기간 를 이 상자의 carreserveday 칸에 저장
		this.carbegindate = carbegindate;   // 받은 렌트 예약 대여날짜 를 이 상자의 carbegindate 칸에 저장
		this.carins = carins;   // 받은 보험 적용 여부 값   1(적용) 또는 0(미적용) 를 이 상자의 carins 칸에 저장
		this.carwifi = carwifi;   // 받은 무선 WIFI 적용 여부 값   1(적용) 또는 0(미적용) 를 이 상자의 carwifi 칸에 저장
		this.carnave = carnave;   // 받은 네비게이션   적용 여부 값   1(적용) 또는 0(미적용) 를 이 상자의 carnave 칸에 저장
		this.carbabyseat = carbabyseat;   // 받은 베이비시트  적용 여부 값   1(적용) 또는 0(미적용) 를 이 상자의 carbabyseat 칸에 저장
		this.memberphone = memberphone;   // 받은 비회원으로 예약시 입력한 폰번호 저장 를 이 상자의 memberphone 칸에 저장
		this.memberpass = memberpass;   // 받은 비회원으로 예약시 입력한 예약 비밀번호 저장 를 이 상자의 memberpass 칸에 저장
	}
	
	//getter, setter 메소드 	
		
	// orderid 값을 꺼내 준다.  — 렌트 예약 아이디
	public int getOrderid() {
		return orderid;   // orderid 칸에 든 값을 그대로 돌려준다  — 렌트 예약 아이디
	}

	// orderid 칸에 값을 넣어 준다.  — 렌트 예약 아이디
	public void setOrderid(int orderid) {
		this.orderid = orderid;   // 받은 렌트 예약 아이디 를 이 상자의 orderid 칸에 저장
	}

	// id 값을 꺼내 준다.
	public String getId() {
		return id;   // id 칸에 든 값을 그대로 돌려준다
	}

	// id 칸에 값을 넣어 준다.
	public void setId(String id) {
		this.id = id;   // 받은 값을 이 상자의 id 칸에 저장
	}

	// carno 값을 꺼내 준다.  — 렌트 예약한 차번호
	public int getCarno() {
		return carno;   // carno 칸에 든 값을 그대로 돌려준다  — 렌트 예약한 차번호
	}

	// carno 칸에 값을 넣어 준다.  — 렌트 예약한 차번호
	public void setCarno(int carno) {
		this.carno = carno;   // 받은 렌트 예약한 차번호 를 이 상자의 carno 칸에 저장
	}

	// carqty 값을 꺼내 준다.  — 렌트 예약 수량
	public int getCarqty() {
		return carqty;   // carqty 칸에 든 값을 그대로 돌려준다  — 렌트 예약 수량
	}

	// carqty 칸에 값을 넣어 준다.  — 렌트 예약 수량
	public void setCarqty(int carqty) {
		this.carqty = carqty;   // 받은 렌트 예약 수량 를 이 상자의 carqty 칸에 저장
	}

	// carreserveday 값을 꺼내 준다.  — 렌트 예약 대여기간
	public int getCarreserveday() {
		return carreserveday;   // carreserveday 칸에 든 값을 그대로 돌려준다  — 렌트 예약 대여기간
	}

	// carreserveday 칸에 값을 넣어 준다.  — 렌트 예약 대여기간
	public void setCarreserveday(int carreserveday) {
		this.carreserveday = carreserveday;   // 받은 렌트 예약 대여기간 를 이 상자의 carreserveday 칸에 저장
	}

	// carbegindate 값을 꺼내 준다.  — 렌트 예약 대여날짜
	public String getCarbegindate() {
		return carbegindate;   // carbegindate 칸에 든 값을 그대로 돌려준다  — 렌트 예약 대여날짜
	}

	// carbegindate 칸에 값을 넣어 준다.  — 렌트 예약 대여날짜
	public void setCarbegindate(String carbegindate) {
		this.carbegindate = carbegindate;   // 받은 렌트 예약 대여날짜 를 이 상자의 carbegindate 칸에 저장
	}

	// carins 값을 꺼내 준다.  — 보험 적용 여부 값   1(적용) 또는 0(미적용)
	public int getCarins() {
		return carins;   // carins 칸에 든 값을 그대로 돌려준다  — 보험 적용 여부 값   1(적용) 또는 0(미적용)
	}

	// carins 칸에 값을 넣어 준다.  — 보험 적용 여부 값   1(적용) 또는 0(미적용)
	public void setCarins(int carins) {
		this.carins = carins;   // 받은 보험 적용 여부 값   1(적용) 또는 0(미적용) 를 이 상자의 carins 칸에 저장
	}

	// carwifi 값을 꺼내 준다.  — 무선 WIFI 적용 여부 값   1(적용) 또는 0(미적용)
	public int getCarwifi() {
		return carwifi;   // carwifi 칸에 든 값을 그대로 돌려준다  — 무선 WIFI 적용 여부 값   1(적용) 또는 0(미적용)
	}

	// carwifi 칸에 값을 넣어 준다.  — 무선 WIFI 적용 여부 값   1(적용) 또는 0(미적용)
	public void setCarwifi(int carwifi) {
		this.carwifi = carwifi;   // 받은 무선 WIFI 적용 여부 값   1(적용) 또는 0(미적용) 를 이 상자의 carwifi 칸에 저장
	}

	// carnave 값을 꺼내 준다.  — 네비게이션   적용 여부 값   1(적용) 또는 0(미적용)
	public int getCarnave() {
		return carnave;   // carnave 칸에 든 값을 그대로 돌려준다  — 네비게이션   적용 여부 값   1(적용) 또는 0(미적용)
	}

	// carnave 칸에 값을 넣어 준다.  — 네비게이션   적용 여부 값   1(적용) 또는 0(미적용)
	public void setCarnave(int carnave) {
		this.carnave = carnave;   // 받은 네비게이션   적용 여부 값   1(적용) 또는 0(미적용) 를 이 상자의 carnave 칸에 저장
	}

	// carbabyseat 값을 꺼내 준다.  — 베이비시트  적용 여부 값   1(적용) 또는 0(미적용)
	public int getCarbabyseat() {
		return carbabyseat;   // carbabyseat 칸에 든 값을 그대로 돌려준다  — 베이비시트  적용 여부 값   1(적용) 또는 0(미적용)
	}

	// carbabyseat 칸에 값을 넣어 준다.  — 베이비시트  적용 여부 값   1(적용) 또는 0(미적용)
	public void setCarbabyseat(int carbabyseat) {
		this.carbabyseat = carbabyseat;   // 받은 베이비시트  적용 여부 값   1(적용) 또는 0(미적용) 를 이 상자의 carbabyseat 칸에 저장
	}

	// memberphone 값을 꺼내 준다.  — 비회원으로 예약시 입력한 폰번호 저장
	public String getMemberphone() {
		return memberphone;   // memberphone 칸에 든 값을 그대로 돌려준다  — 비회원으로 예약시 입력한 폰번호 저장
	}

	// memberphone 칸에 값을 넣어 준다.  — 비회원으로 예약시 입력한 폰번호 저장
	public void setMemberphone(String memberphone) {
		this.memberphone = memberphone;   // 받은 비회원으로 예약시 입력한 폰번호 저장 를 이 상자의 memberphone 칸에 저장
	}

	// memberpass 값을 꺼내 준다.  — 비회원으로 예약시 입력한 예약 비밀번호 저장
	public String getMemberpass() {
		return memberpass;   // memberpass 칸에 든 값을 그대로 돌려준다  — 비회원으로 예약시 입력한 예약 비밀번호 저장
	}

	// memberpass 칸에 값을 넣어 준다.  — 비회원으로 예약시 입력한 예약 비밀번호 저장
	public void setMemberpass(String memberpass) {
		this.memberpass = memberpass;   // 받은 비회원으로 예약시 입력한 예약 비밀번호 저장 를 이 상자의 memberpass 칸에 저장
	}

	

}








