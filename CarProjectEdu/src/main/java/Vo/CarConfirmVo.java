package Vo;   // 이 파일이 속한 패키지(=폴더) 이름. 실제 폴더 구조와 반드시 같아야 한다


//CarConfirmVo클래스 역할 :  예약한 정보를 DB에서 조회해 와서 변수에 저장후 제공할 클래스 

//참고. 2개의 테이블(carlist, non_carorder)을  자연 JOIN하여 조회된 컬럼들의 결과 데이터를 함께 얻어 저장 

public class CarConfirmVo {

	//멤버변수들 선언
		//1. carlist테이블에서 조회된 컬럼의 정보들을 저장할 변수들 선언
		private String carname, carimg;
		private int carprice;   // 차 한 대의 하루 렌트 가격 (carlist 에서 조인해 가져온다)
	
		//2. non_carorder테이블에서 조회된 컬럼의 정보들을 저장할 변수들 선언
		private int orderid, carno, carreserveday, carqty, carins, carwifi, carnave, carbabyseat;
		private String carbegindate, memberpass, memberphone;   // 대여 시작일, 예약 비밀번호, 연락처 (non_carorder 에서 가져온다)
		
		
		//getter, setter 역할을 하는 메소드들 선언	
		public int getCarno() {
			return carno;   // carno 칸에 든 값을 그대로 돌려준다
		}
		// carno 칸에 값을 넣어 준다.
		public void setCarno(int carno) {
			this.carno = carno;   // 받은 값을 이 상자의 carno 칸에 저장
		}
		// memberpass 값을 꺼내 준다.
		public String getMemberpass() {
			return memberpass;   // memberpass 칸에 든 값을 그대로 돌려준다
		}
		// memberpass 칸에 값을 넣어 준다.
		public void setMemberpass(String memberpass) {
			this.memberpass = memberpass;   // 받은 값을 이 상자의 memberpass 칸에 저장
		}
		// memberphone 값을 꺼내 준다.
		public String getMemberphone() {
			return memberphone;   // memberphone 칸에 든 값을 그대로 돌려준다
		}
		// memberphone 칸에 값을 넣어 준다.
		public void setMemberphone(String memberphone) {
			this.memberphone = memberphone;   // 받은 값을 이 상자의 memberphone 칸에 저장
		}

		// carname 값을 꺼내 준다.
		public String getCarname() {
			return carname;   // carname 칸에 든 값을 그대로 돌려준다
		}
		// carname 칸에 값을 넣어 준다.
		public void setCarname(String carname) {
			this.carname = carname;   // 받은 값을 이 상자의 carname 칸에 저장
		}
		// carimg 값을 꺼내 준다.
		public String getCarimg() {
			return carimg;   // carimg 칸에 든 값을 그대로 돌려준다
		}
		// carimg 칸에 값을 넣어 준다.
		public void setCarimg(String carimg) {
			this.carimg = carimg;   // 받은 값을 이 상자의 carimg 칸에 저장
		}
		// carprice 값을 꺼내 준다.
		public int getCarprice() {
			return carprice;   // carprice 칸에 든 값을 그대로 돌려준다
		}
		// carprice 칸에 값을 넣어 준다.
		public void setCarprice(int carprice) {
			this.carprice = carprice;   // 받은 값을 이 상자의 carprice 칸에 저장
		}
		// orderid 값을 꺼내 준다.
		public int getOrderid() {
			return orderid;   // orderid 칸에 든 값을 그대로 돌려준다
		}
		// orderid 칸에 값을 넣어 준다.
		public void setOrderid(int orderid) {
			this.orderid = orderid;   // 받은 값을 이 상자의 orderid 칸에 저장
		}
		// carreserveday 값을 꺼내 준다.
		public int getCarreserveday() {
			return carreserveday;   // carreserveday 칸에 든 값을 그대로 돌려준다
		}
		// carreserveday 칸에 값을 넣어 준다.
		public void setCarreserveday(int carreserveday) {
			this.carreserveday = carreserveday;   // 받은 값을 이 상자의 carreserveday 칸에 저장
		}
		// carqty 값을 꺼내 준다.
		public int getCarqty() {
			return carqty;   // carqty 칸에 든 값을 그대로 돌려준다
		}
		// carqty 칸에 값을 넣어 준다.
		public void setCarqty(int carqty) {
			this.carqty = carqty;   // 받은 값을 이 상자의 carqty 칸에 저장
		}
		// carins 값을 꺼내 준다.
		public int getCarins() {
			return carins;   // carins 칸에 든 값을 그대로 돌려준다
		}
		// carins 칸에 값을 넣어 준다.
		public void setCarins(int carins) {
			this.carins = carins;   // 받은 값을 이 상자의 carins 칸에 저장
		}
		// carwifi 값을 꺼내 준다.
		public int getCarwifi() {
			return carwifi;   // carwifi 칸에 든 값을 그대로 돌려준다
		}
		// carwifi 칸에 값을 넣어 준다.
		public void setCarwifi(int carwifi) {
			this.carwifi = carwifi;   // 받은 값을 이 상자의 carwifi 칸에 저장
		}
		// carnave 값을 꺼내 준다.
		public int getCarnave() {
			return carnave;   // carnave 칸에 든 값을 그대로 돌려준다
		}
		// carnave 칸에 값을 넣어 준다.
		public void setCarnave(int carnave) {
			this.carnave = carnave;   // 받은 값을 이 상자의 carnave 칸에 저장
		}
		// carbabyseat 값을 꺼내 준다.
		public int getCarbabyseat() {
			return carbabyseat;   // carbabyseat 칸에 든 값을 그대로 돌려준다
		}
		// carbabyseat 칸에 값을 넣어 준다.
		public void setCarbabyseat(int carbabyseat) {
			this.carbabyseat = carbabyseat;   // 받은 값을 이 상자의 carbabyseat 칸에 저장
		}
		// carbegindate 값을 꺼내 준다.
		public String getCarbegindate() {
			return carbegindate;   // carbegindate 칸에 든 값을 그대로 돌려준다
		}
		// carbegindate 칸에 값을 넣어 준다.
		public void setCarbegindate(String carbegindate) {
			this.carbegindate = carbegindate;   // 받은 값을 이 상자의 carbegindate 칸에 저장
		}

	
	
}
