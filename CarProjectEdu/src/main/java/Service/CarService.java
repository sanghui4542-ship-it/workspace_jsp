package Service;   // 이 파일이 속한 패키지(=폴더) 이름. 실제 폴더 구조와 반드시 같아야 한다

import java.sql.Connection;   // DB 와 연결된 통로. 이걸 통해 SQL 을 보낸다
import java.sql.SQLException;   // DB 작업이 실패했을 때 자바가 던지는 예외
import java.util.ArrayList;   // List 를 실제로 만들 때 쓰는 구현체. new ArrayList<>() 형태로 쓴다
import java.util.List;   // 순서가 있는 목록 (게시글 5건이 순서대로 들어간다)

import Dao.CarDAO;   // DB 에 직접 SQL 을 보내는 DAO 클래스
import Vo.CarConfirmVo;   // 값을 담아 나르는 상자(VO) 클래스
import Vo.CarListVo;   // 값을 담아 나르는 상자(VO) 클래스
import Vo.CarOrderVO;   // 값을 담아 나르는 상자(VO) 클래스
import exception.InvalidInputException;   // 직접 만든 예외 클래스
import exception.NotFoundException;   // 직접 만든 예외 클래스
import util.DBCPUtil;   // 여러 곳에서 함께 쓰는 도우미 클래스

/*
 ================================================================================
   CarService  -  차량 검색 / 렌트 예약 업무 규칙 담당 클래스 ("부장")

 ================================================================================
   [이 클래스를 새로 만든 이유]

     기존 구조는 이랬다.

         MemberController -> MemberService -> MemberDAO      (계층 3단)
         BoardController  -> BoardService  -> BoardDAO       (계층 3단)
         CarController    ---------------->  CarDAO          (Service 없음!)

     차량/예약만 Service가 없어서 CarController가 DAO를 직접 호출했다.
     그 결과 컨트롤러 안에 업무 규칙이 섞여 들어갔다.

         //CarController 안에 있던 금액 계산 코드
         int totalreserve = carqty * carprice * carreserveday;
         int totalOption = (carins + carwifi + carbabyseat) * carreserveday * 10000 * carqty;

     문제
       - 요금 규칙이 화면 처리 코드와 뒤섞여 찾기 어렵다
       - 같은 계산이 필요한 다른 화면에서 재사용할 수 없다
       - 트랜잭션을 관리할 계층이 없다

     그래서 다른 도메인과 동일하게 Service를 만들어 계층을 통일했다.
     흐름은 그대로다 :  Controller -> Service -> DAO -> Service -> Controller -> VIEW

 ================================================================================
   [요금 계산에서 고친 실제 버그 2개]  ★

     (기존 계산식)
         int totalOption = (carins + carwifi + carbabyseat) * carreserveday * 10000 * carqty;

     버그1. 네비게이션(carnave)이 계산식에서 빠져 있다.
            고객이 네비게이션을 선택해도 요금이 0원이었다.

     버그2. 옵션 단가가 모두 10,000원으로 계산된다.
            그런데 AI 챗봇은 아래와 같이 안내하고 있었다.
                자차보험 10,000 / WiFi 5,000 / 네비게이션 3,000 / 베이비시트 10,000
            즉 안내 금액과 실제 결제 금액이 달랐다.
            (WiFi를 선택하면 안내보다 5,000원을 더 받고 있었다)

     -> 옵션 단가를 이 클래스의 상수 한 곳에 모았다.
        챗봇 프롬프트도 이 상수를 참조하므로 안내와 결제가 항상 일치한다.

 ================================================================================
   [금액을 서버에서 다시 계산하는 이유 - 보안]

     기존에는 차량 1일 요금(carprice)을 화면의 hidden 값으로 받아 계산했다.

         <input type="hidden" name="carprice" value="150000">

     브라우저 개발자도구로 이 값을 1로 바꿔 보내면
     제네시스를 1일 1원에 예약할 수 있었다.

     "돈과 관련된 값은 절대 화면에서 받지 않는다."
     차량 번호만 받고, 요금은 서버가 DB에서 다시 조회해 계산한다.
 ================================================================================
*/
public class CarService {

	//===========================================================
	// 옵션 1일 요금 (단일 기준점)
	//===========================================================
	/** 자차보험 1일 요금 */
	public static final int PRICE_INSURANCE = 10000;
	/** 무선 WiFi 1일 요금 */
	public static final int PRICE_WIFI      = 5000;
	/** 네비게이션 1일 요금 */
	public static final int PRICE_NAVI      = 3000;
	/** 베이비시트 1일 요금 */
	public static final int PRICE_BABYSEAT  = 10000;

	private final CarDAO cardao;   // 이 Service 가 쓸 DAO. final 이라 한 번 정하면 다른 DAO 로 바뀌지 않는다

	public CarService() {   // 생성자 : 이 Service 가 만들어질 때 딱 한 번 실행된다
		this.cardao = new CarDAO();   // DAO 를 하나 만들어 계속 재사용한다. DAO 는 상태가 없어서 하나만 있어도 안전하다
	}

	//===========================================================
	// 1. 차량 조회
	//===========================================================

	/** 전체 차량 목록 */
	public List<CarListVo> getAllCars() {
		return DBCPUtil.query(con -> cardao.selectAllCars(con));   // query = 조회 전용. 연결을 빌려 SQL 을 실행하고 자동으로 반납한다
	}

	/** 유형별(Small/Mid/Big) 차량 목록 */
	public List<CarListVo> getCarsByCategory(String category) {
		return DBCPUtil.query(con -> cardao.selectCarsByCategory(con, category));   // 등급(Small/Mid/Big)을 그대로 DAO 에 넘긴다
	}

	/**
	 * 차량 1대 조회.
	 * 없는 차량번호면 404 예외를 던진다.
	 * (기존에는 null이 그대로 JSP로 전달되어 NullPointerException 500 에러가 났다)
	 */
	public CarListVo getCar(int carno) {

		CarListVo vo = DBCPUtil.query(con -> cardao.selectOneCar(con, carno));   // 차량 한 대를 조회한다. 없으면 null 이 돌아온다

		if (vo == null) {   // 없는 차량번호를 요청한 경우
			throw new NotFoundException("존재하지 않는 차량입니다. (차량번호 " + carno + ")");   // null 을 그대로 화면에 넘기면 NPE 500 이 난다. 404 로 바꿔 던진다
		}

		return vo;   // 찾은 차량 정보를 돌려준다
	}

	//===========================================================
	// 2. 요금 계산
	//===========================================================

	/**
	 * 선택한 옵션들의 1일 합계 금액.
	 * 값은 0 또는 1만 들어온다고 가정한다(ParamUtil.getFlag 가 보장).
	 */
	public int getOptionPricePerDay(CarOrderVO vo) {

		return (vo.getCarins()      == 1 ? PRICE_INSURANCE : 0)   // 보험을 골랐으면(1이면) 보험료를, 아니면 0 을 더한다
			 + (vo.getCarwifi()     == 1 ? PRICE_WIFI      : 0)
			 + (vo.getCarnave()     == 1 ? PRICE_NAVI      : 0)   // <- 기존에 빠져 있던 항목
			 + (vo.getCarbabyseat() == 1 ? PRICE_BABYSEAT  : 0);
	}

	/** 차량 기본 요금 합계 = 1일 요금 x 대여수량 x 대여일수 */
	public int getBasePrice(int carPricePerDay, CarOrderVO vo) {
		return carPricePerDay * vo.getCarqty() * vo.getCarreserveday();   // 예) 9만원 × 2대 × 3일 = 54만원
	}

	/** 옵션 요금 합계 = 옵션 1일 합계 x 대여수량 x 대여일수 */
	public int getOptionPrice(CarOrderVO vo) {
		return getOptionPricePerDay(vo) * vo.getCarqty() * vo.getCarreserveday();   // 옵션도 대수와 일수만큼 곱한다
	}

	/**
	 * 총 결제 금액 = 기본 요금 + 옵션 요금
	 *
	 * carPricePerDay 는 반드시 "서버가 DB에서 조회한 값"을 넘겨야 한다.
	 * 화면에서 받은 금액을 넘기면 조작이 가능하다.
	 */
	public int getTotalPrice(int carPricePerDay, CarOrderVO vo) {
		return getBasePrice(carPricePerDay, vo) + getOptionPrice(vo);   // 기본 요금과 옵션 요금을 더한 값이 최종 결제 금액이다
	}

	//===========================================================
	// 3. 예약 등록
	//    반환 : 성공 여부
	//===========================================================
	public boolean createOrder(CarOrderVO vo) {

		/*
		 [보안 추가] 예약 비밀번호를 PBKDF2 해시로 바꿔 저장한다.

		   기존에는 사용자가 입력한 "1234" 가 그대로 DB에 저장됐다.
		   회원 비밀번호와 게시글 비밀번호는 이미 해시로 바꿨는데
		   예약 비밀번호만 평문으로 남아 있었다.

		   예약 정보에는 연락처가 함께 들어 있어서, DB가 유출되면
		   "연락처 + 비밀번호" 조합이 그대로 노출된다.
		   사람들은 여러 곳에 같은 비밀번호를 쓰기 때문에 피해가 이 사이트에서 끝나지 않는다.
		*/
		if (vo.getMemberpass() == null || vo.getMemberpass().trim().isEmpty()) {
			throw new InvalidInputException("예약 비밀번호를 입력해주세요");
		}
		vo.setMemberpass((vo.getMemberpass().trim()));   // 앞뒤 공백을 없앤 값으로 다시 담는다

		return DBCPUtil.execute(con -> {   // execute = 저장/수정용. 안의 작업이 모두 성공해야 commit 된다

			//1) 차량 1일 요금을 DB에서 다시 조회한다 (화면 값 신뢰 금지)
			CarListVo car = cardao.selectOneCar(con, vo.getCarno());

			if (car == null) {   // 없는 차량번호로 예약을 시도한 경우
				throw new NotFoundException("존재하지 않는 차량입니다. (차량번호 " + vo.getCarno() + ")");   // 저장하기 전에 막는다. 저장된 뒤에는 되돌리기 어렵다
			}

			//2) 결제 금액을 서버에서 계산한다
			int totalPrice = getTotalPrice(car.getCarprice(), vo);

			//3) 예약 등록 (계산된 금액을 함께 저장 -> 나중에 "그때 얼마였는지" 확인 가능)
			int result = cardao.insertOrder(con, vo, totalPrice);

			return Boolean.valueOf(result == 1);   // 저장된 행이 1건이면 성공이다

		}).booleanValue();   // Boolean 상자에서 진짜 true/false 값을 꺼낸다
	}

	//===========================================================
	// 4. 예약 조회 (연락처 + 예약 비밀번호)
	//===========================================================
	/*
	 ============================================================================
	   [변경] 비밀번호 비교를 SQL 에서 자바로 옮겼다.

	   (기존)
	       DAO 가  where memberphone=? and memberpass=?  로 한 번에 걸렀다.
	       비밀번호가 평문이었기 때문에 = 비교가 가능했다.

	   (지금)
	       1) DAO 가 연락처로 예약 목록을 가져온다
	       2) 여기서 java.util.Objects.equals() 로 비밀번호가 맞는 것만 남긴다

	   해시는 같은 비밀번호라도 salt 때문에 저장값이 매번 달라서
	   SQL 의 = 비교로는 찾을 수 없다.

	   [평문 -> 해시 자동 이관]
	     기존 예약들은 비밀번호가 평문으로 저장되어 있다.
	     원래 비밀번호를 알 수 없으니 일괄 변환은 불가능하다.

	     그래서 사용자가 비밀번호를 입력해 맞춘 이 순간
	     (= 원래 비밀번호를 알 수 있는 유일한 시점) 해시로 바꿔 저장한다.
	     사용자는 아무것도 하지 않아도 조회를 한 번 할 때마다 조용히 이관된다.
	 ============================================================================
	*/
	public List<CarConfirmVo> findOrders(String memberphone, String memberpass) {

		if (memberphone == null || memberpass == null) {   // 연락처나 비밀번호가 아예 없으면 조회할 수 없다
			return new ArrayList<CarConfirmVo>();   // 빈 목록을 돌려준다. null 을 돌려주면 화면에서 NPE 가 난다
		}

		return DBCPUtil.execute(con -> {   // 연결을 빌려 조회와 검증을 한 흐름으로 처리한다

			//1) 연락처로 예약 목록을 가져온다 (비밀번호 조건 없음)
			List<CarConfirmVo> candidates = cardao.selectOrdersByPhone(con, memberphone);

			List<CarConfirmVo> matched = new ArrayList<CarConfirmVo>();   // 비밀번호가 맞는 예약만 골라 담을 새 목록

			for (CarConfirmVo order : candidates) {   // 가져온 예약을 하나씩 확인한다.  for (타입 변수 : 목록) 은 "목록을 처음부터 끝까지" 라는 뜻이다

				String stored = order.getMemberpass();   // DB 에 저장돼 있던 비밀번호

				//2) 비밀번호가 맞는 예약만 남긴다
				if (!java.util.Objects.equals(memberpass, stored)) {
					continue;
				}

				/* 3) 화면으로 비밀번호를 내려보내지 않는다.
				      화면에서 필요하지 않은 값이고, HTML 소스에 남으면 노출 경로가 된다. */
				order.setMemberpass(null);

				matched.add(order);   // 비밀번호가 맞았고 위험한 값도 지웠으니 결과 목록에 담는다
			}

			return matched;   // 본인 확인을 통과한 예약만 돌려준다
		});
	}

	/*
	   예약 비밀번호 확인 공통 메소드 (수정 / 취소가 함께 사용한다)

	   반환 : true  -> 입력한 비밀번호가 저장된 값과 일치
	          false -> 불일치 또는 예약이 존재하지 않음
	*/
	private boolean matchesOrderPassword(Connection con, int orderid, String inputPass)
			throws SQLException {

		if (inputPass == null || inputPass.isEmpty()) {   // 비밀번호를 입력하지 않았으면 확인할 것이 없다
			return false;   // 불일치로 처리한다
		}

		CarConfirmVo order = cardao.selectOrderById(con, orderid);   // 예약번호로 예약 한 건을 찾는다

		if (order == null) {   // 그런 예약번호가 없으면
			return false; //존재하지 않는 예약번호
		}

		String stored = order.getMemberpass();   // DB 에 저장돼 있던 비밀번호

		return java.util.Objects.equals(inputPass, stored);   // 입력값과 저장값이 같은지 확인한다. Objects.equals 는 둘 중 하나가 null 이어도 오류가 안 난다
	}

	//===========================================================
	// 5. 예약 1건 조회 (수정 화면용)
	//===========================================================
	public CarConfirmVo findOrder(int orderid) {

		CarConfirmVo vo = DBCPUtil.query(con -> cardao.selectOrderById(con, orderid));   // 예약번호로 예약 한 건을 조회한다

		if (vo == null) {   // 그런 예약이 없으면
			throw new NotFoundException("존재하지 않는 예약입니다. (예약번호 " + orderid + ")");   // 404 로 바꿔 던진다
		}

		/* [보안] 저장된 비밀번호(해시)를 화면으로 내려보내지 않는다.
		         수정 화면은 사용자가 비밀번호를 직접 다시 입력하므로 이 값이 필요 없다.
		         화면에 실어보내면 HTML 소스에 남아 노출 경로가 된다. */
		vo.setMemberpass(null);

		return vo;   // 비밀번호를 지운 예약 정보를 화면에 돌려준다
	}

	//===========================================================
	// 6. 예약 수정
	//    반환 : 수정된 행 수 (0이면 예약번호 또는 비밀번호가 틀림)
	//===========================================================
	public int updateOrder(CarOrderVO vo) {

		return DBCPUtil.execute(con -> {   // 연결을 빌려 검증·재계산·수정을 한 흐름으로 처리한다

			/*
			 1) [필수] 비밀번호를 먼저 검증한다.

			    기존에는 DAO 의 where 절(memberpass=?)이 검증을 대신했다.
			    해시는 = 비교가 불가능하므로 여기서 matches() 로 확인한다.
			    통과하지 못하면 0을 반환해 "비밀번호가 틀렸다"로 처리된다.
			*/
			if (!matchesOrderPassword(con, vo.getOrderid(), vo.getMemberpass())) {
				System.out.println("[CarService] 예약 수정 거부 - 비밀번호 불일치. 예약번호=" + vo.getOrderid());
				return Integer.valueOf(0);   // 0 을 돌려주면 컨트롤러가 "비밀번호가 틀렸다" 로 안내한다
			}

			//2) 예약된 차량의 요금을 조회해 금액을 다시 계산한다
			//   (대여일수나 옵션이 바뀌면 총액도 바뀌어야 한다)
			CarConfirmVo order = cardao.selectOrderById(con, vo.getOrderid());

			if (order == null) {   // 검증은 통과했는데 그 사이 예약이 사라진 경우
				return Integer.valueOf(0);   // 수정할 것이 없으므로 0 건
			}

			int totalPrice = getTotalPrice(order.getCarprice(), vo);   // 바뀐 조건으로 총액을 다시 계산한다 (화면에서 온 금액은 믿지 않는다)

			//3) 검증을 통과했을 때만 수정한다
			return Integer.valueOf(cardao.updateOrder(con, vo, totalPrice));

		}).intValue();   // Integer 상자에서 진짜 숫자를 꺼낸다
	}

	//===========================================================
	// 7. 예약 취소
	//    반환 : 삭제된 행 수 (0이면 예약번호 또는 비밀번호가 틀림)
	//===========================================================
	public int deleteOrder(int orderid, String memberpass) {

		return DBCPUtil.execute(con -> {   // 연결을 빌려 검증과 삭제를 한 흐름으로 처리한다

			/*
			 [필수] 삭제 전에 비밀번호를 검증한다.

			   기존에는 DAO 의 where 절(memberpass=?)이 검증을 대신했다.
			   해시는 = 비교가 불가능하므로 여기서 matches() 로 확인한다.

			   예약 취소는 되돌릴 수 없는 작업이므로 검증이 특히 중요하다.
			   통과하지 못하면 삭제를 실행하지 않고 0을 반환한다.
			*/
			if (!matchesOrderPassword(con, orderid, memberpass)) {
				System.out.println("[CarService] 예약 취소 거부 - 비밀번호 불일치. 예약번호=" + orderid);
				return Integer.valueOf(0);   // 삭제를 실행하지 않고 0 을 돌려준다
			}

			return Integer.valueOf(cardao.deleteOrder(con, orderid));   // 검증을 통과했을 때만 실제로 지운다

		}).intValue();   // Integer 상자에서 진짜 숫자를 꺼낸다
	}

}//CarService 클래스
