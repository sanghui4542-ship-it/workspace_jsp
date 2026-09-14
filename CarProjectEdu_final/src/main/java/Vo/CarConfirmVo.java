package Vo;

/*
 * ============================================================================
 *  CarConfirmVo  -  상자(VO) : "예약 조회" 결과 1건을 담아 나르는 객체
 *
 *  [이 상자만의 특징 - 두 테이블을 합친 결과를 담는다]
 *    다른 VO(BoardVo, MemberVO 등)는 테이블 하나와 1:1로 짝을 이룬다.
 *    그런데 예약 내역을 화면에 보여주려면 두 표의 정보가 동시에 필요하다.
 *
 *      non_carorder 표 : 누가, 언제부터, 며칠, 몇 대, 어떤 옵션으로 예약했는가
 *      carlist 표      : 그 차의 이름은 무엇이고, 사진과 하루 요금은 얼마인가
 *
 *    예약 표에는 차량번호(carno)만 들어 있어서 "3번 차를 2일 빌렸다"까지만
 *    알 수 있다. 화면에는 "쏘나타 사진과 함께 45,000원" 처럼 보여줘야 하므로,
 *    SQL 에서 두 표를 carno 기준으로 JOIN 해서 조회하고 그 합친 결과를
 *    이 상자 하나에 담는다.
 *
 *  담는 값
 *    [carlist 에서 온 값]
 *      carname        차 이름       (예: 쏘나타)
 *      carimg         차 사진 파일명
 *      carprice       하루 대여 요금
 *
 *    [non_carorder 에서 온 값]
 *      orderid        예약번호 (기본키)
 *      carno          어떤 차를 빌렸는지 (carlist 와 이어 주는 열)
 *      carbegindate   대여 시작일
 *      carreserveday  대여 일수
 *      carqty         대여 대수
 *      carins         자차보험 선택 여부    (1 = 선택함, 0 = 선택 안 함)
 *      carwifi        무선 WiFi 선택 여부   (1 / 0)
 *      carnave        내비게이션 선택 여부  (1 / 0)
 *      carbabyseat    베이비시트 선택 여부  (1 / 0)
 *      memberpass     예약 비밀번호 (예약을 조회·취소할 때 본인 확인용)
 *      memberphone    연락처
 *
 *  [옵션을 true/false 가 아니라 1/0 숫자로 저장하는 이유]
 *    MySQL 에는 원래 boolean 타입이 따로 없고 TINYINT(1) 을 그렇게 쓴다.
 *    그래서 DB 에는 1(선택함) / 0(선택 안 함) 숫자로 저장되고, 자바에서도
 *    같은 형태(int)로 받아 둔다. 총 금액을 계산할 때는 CarService 가
 *    "이 값이 1이면 해당 옵션 가격을 더한다" 는 식으로 사용한다.
 *
 *  getXxx() : 상자에서 값을 꺼낸다      예) vo.getCarname()
 *  setXxx() : 상자에 값을 넣는다(바꾼다)  예) vo.setCarname("쏘나타")
 * ============================================================================
 */

public class CarConfirmVo {

	//1. carlist 테이블에서 조인해 가져온 값들
	private String carname, carimg;
	private int carprice;   // 차 한 대의 하루 렌트 가격 (carlist 에서 조인해 가져온다)

	//2. non_carorder 테이블에서 가져온 값들
	private int orderid, carno, carreserveday, carqty, carins, carwifi, carnave, carbabyseat;
	private String carbegindate, memberpass, memberphone;   // 대여 시작일, 예약 비밀번호, 연락처 (non_carorder 에서 가져온다)


	// 차량번호 꺼내기 / 넣기
	public int getCarno() {
		return carno;
	}

	public void setCarno(int carno) {
		this.carno = carno;
	}

	// 예약 비밀번호 꺼내기 / 넣기
	public String getMemberpass() {
		return memberpass;
	}

	public void setMemberpass(String memberpass) {
		this.memberpass = memberpass;
	}

	// 연락처 꺼내기 / 넣기
	public String getMemberphone() {
		return memberphone;
	}

	public void setMemberphone(String memberphone) {
		this.memberphone = memberphone;
	}

	// 차 이름 꺼내기 / 넣기
	public String getCarname() {
		return carname;
	}

	public void setCarname(String carname) {
		this.carname = carname;
	}

	// 차 사진 파일명 꺼내기 / 넣기
	public String getCarimg() {
		return carimg;
	}

	public void setCarimg(String carimg) {
		this.carimg = carimg;
	}

	// 하루 대여 요금 꺼내기 / 넣기
	public int getCarprice() {
		return carprice;
	}

	public void setCarprice(int carprice) {
		this.carprice = carprice;
	}

	// 예약번호 꺼내기 / 넣기
	public int getOrderid() {
		return orderid;
	}

	public void setOrderid(int orderid) {
		this.orderid = orderid;
	}

	// 대여 일수 꺼내기 / 넣기
	public int getCarreserveday() {
		return carreserveday;
	}

	public void setCarreserveday(int carreserveday) {
		this.carreserveday = carreserveday;
	}

	// 대여 대수 꺼내기 / 넣기
	public int getCarqty() {
		return carqty;
	}

	public void setCarqty(int carqty) {
		this.carqty = carqty;
	}

	// 자차보험 선택 여부(1/0) 꺼내기 / 넣기
	public int getCarins() {
		return carins;
	}

	public void setCarins(int carins) {
		this.carins = carins;
	}

	// 무선 WiFi 선택 여부(1/0) 꺼내기 / 넣기
	public int getCarwifi() {
		return carwifi;
	}

	public void setCarwifi(int carwifi) {
		this.carwifi = carwifi;
	}

	// 내비게이션 선택 여부(1/0) 꺼내기 / 넣기
	public int getCarnave() {
		return carnave;
	}

	public void setCarnave(int carnave) {
		this.carnave = carnave;
	}

	// 베이비시트 선택 여부(1/0) 꺼내기 / 넣기
	public int getCarbabyseat() {
		return carbabyseat;
	}

	public void setCarbabyseat(int carbabyseat) {
		this.carbabyseat = carbabyseat;
	}

	// 대여 시작일 꺼내기 / 넣기
	public String getCarbegindate() {
		return carbegindate;
	}

	public void setCarbegindate(String carbegindate) {
		this.carbegindate = carbegindate;
	}
}
