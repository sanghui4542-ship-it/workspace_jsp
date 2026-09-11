package Dao;   // 이 파일이 속한 패키지(=폴더) 이름. 실제 폴더 구조와 반드시 같아야 한다

import java.sql.Connection;   // DB 와 연결된 통로. 이걸 통해 SQL 을 보낸다
import java.sql.PreparedStatement;   // 물음표(?)가 있는 SQL 을 안전하게 실행하는 도구. SQL 인젝션을 막아 준다
import java.sql.ResultSet;   // select 결과를 한 줄씩 읽어 오는 도구
import java.sql.SQLException;   // DB 작업이 실패했을 때 자바가 던지는 예외
import java.util.ArrayList;   // List 를 실제로 만들 때 쓰는 구현체. new ArrayList<>() 형태로 쓴다
import java.util.List;   // 순서가 있는 목록 (게시글 5건이 순서대로 들어간다)

import Vo.CarConfirmVo;   // 값을 담아 나르는 상자(VO) 클래스
import Vo.CarListVo;   // 값을 담아 나르는 상자(VO) 클래스
import Vo.CarOrderVO;   // 값을 담아 나르는 상자(VO) 클래스

/*
 ================================================================================
   CarDAO  -  차량(carlist) / 예약(non_carorder) SQL 실행 담당 클래스 ("사원")

 ================================================================================
   [3단계 수정 내용]

   ------------------------------------------------------------------
   1. con / pstmt / rs 를 지역변수로 내렸다 (동시 접속 시 데이터 섞임 방지)
   ------------------------------------------------------------------
      다른 DAO와 같은 문제였다. 서블릿과 DAO는 객체가 하나뿐인데
      동시에 들어온 여러 요청이 같은 필드를 덮어써서
      "어쩌다 한 번" 다른 사람의 조회 결과가 섞이거나 ResultSet closed 예외가 났다.

   ------------------------------------------------------------------
   2. DAO에서 HttpServletRequest 를 걷어냈다  ★ 계층 분리의 핵심
   ------------------------------------------------------------------
      (기존 코드)
          public int carOrderUpdate(HttpServletRequest request) {
              ...
              pstmt.setString(1, request.getParameter("carbegindate"));
              pstmt.setInt(2, Integer.parseInt(request.getParameter("carreserveday")));
              ...
          }

      무엇이 문제인가
        DAO가 "웹 요청"을 직접 알고 있다. 그래서
          - 화면의 input name 이 바뀌면 DAO를 수정해야 한다
          - 웹이 아닌 곳(배치 프로그램, 테스트 코드)에서 재사용할 수 없다
          - 값 검증을 어느 계층에서 하는지 불분명해진다
          - parseInt 가 DAO 안에 있어 잘못된 입력이 SQL 오류로 나타난다

        DAO는 "SQL을 실행하는 계층"이다. HTTP를 알아야 할 이유가 없다.

      (바뀐 방식)
        request 에서 값을 꺼내고 검증하는 일은 CarService 가 맡고,
        DAO는 VO(값 객체)만 받는다.

          Controller  : 요청을 받아 Service 호출
          Service     : request 에서 값 추출 + 검증 + 트랜잭션
          DAO         : VO를 받아 SQL 실행

   ------------------------------------------------------------------
   3. NATURAL JOIN 을 명시적 JOIN 으로 바꿨다
   ------------------------------------------------------------------
      (기존) select * from non_carorder natural join carlist where ...

      NATURAL JOIN 은 "두 테이블에서 이름이 같은 모든 컬럼"을 조인 조건으로 삼는다.
      지금은 carno 하나뿐이라 우연히 잘 동작한다.
      그러나 나중에 non_carorder 에 carprice 나 carname 같은 컬럼을 추가하면
      조인 조건이 자동으로 늘어나 예약 조회 결과가 조용히 0건이 된다.
      오류도 나지 않아 원인을 찾기 매우 어렵다.

      (지금) join carlist c on o.carno = c.carno
             -> 조인 조건이 코드에 눈으로 보인다. 컬럼을 추가해도 영향이 없다.
 ================================================================================
*/

/*
  [이 파일을 읽기 전에 알아 둘 4가지 - DAO 는 전부 이 모양이다]

   1) 물음표(?) 와 setXxx
        "where carno=?" 처럼 값이 들어갈 자리를 ? 로 비워 둔다.
        그리고 pstmt.setInt(1, carno) 로 첫 번째 ? 에 값을 끼워 넣는다.
        번호는 1부터 시작한다(0 이 아니다).

        왜 이렇게 하나? 문자열을 그대로 이어 붙이면
            "where id='" + input + "'"
        input 에   ' or '1'='1  을 넣는 것만으로 남의 정보가 조회된다.
        이것을 SQL 인젝션이라고 한다. ? 를 쓰면 값은 항상 "값" 으로만 취급되어 막힌다.

   2) try ( ... ) 문법
        try 괄호 안에서 만든 것은 블록이 끝날 때 자동으로 닫힌다.
        예전처럼 finally 에서 하나하나 close() 하지 않아도 되고, 빠뜨릴 수도 없다.

   3) executeQuery 와 executeUpdate
        executeQuery()  : select 용. 결과표(ResultSet)를 돌려준다.
        executeUpdate() : insert/update/delete 용. "바뀐 행 수" 를 숫자로 돌려준다.
                          0 이 돌아오면 "조건에 맞는 행이 없었다" 는 뜻이다.

   4) Connection 을 인자로 받는 이유
        연결을 DAO 가 직접 만들지 않고 Service 에게서 받는다.
        그래야 여러 SQL 을 하나의 트랜잭션으로 묶을 수 있다(DBCPUtil.execute 참고).
*/
public class CarDAO {

	/* 인스턴스 변수 없음 = 무상태 = 여러 스레드가 동시에 써도 안전 */
	// (멤버변수를 두면 동시에 들어온 두 요청이 같은 변수를 덮어써서 남의 결과가 섞인다)

	//===========================================================
	// 1. 전체 차량 목록 조회
	//===========================================================
	// throws SQLException = "여기서 DB 오류가 나면 내가 처리하지 않고 위로 넘기겠다" 는 선언.
	// 넘겨받은 Service 가 DBCPUtil 을 통해 한 곳에서 처리한다.
	public List<CarListVo> selectAllCars(Connection con) throws SQLException {

		// 결과를 담을 빈 목록을 먼저 만든다. 차가 0대여도 null 이 아닌 "빈 목록" 을 돌려주기 위해서다.
		List<CarListVo> list = new ArrayList<CarListVo>();

		// 실행할 SQL. 필요한 컬럼만 이름으로 적는다.
		// select * 를 안 쓰는 이유 : 나중에 컬럼이 추가되면 필요 없는 데이터까지 읽어 오고,
		// 컬럼 순서가 바뀌면 코드가 조용히 틀린 값을 읽을 수 있기 때문이다.
		String sql = "select carno, carname, carcompany, carprice, carusepeople, carinfo, carimg, carcategory"
				   // order by carno asc = 차량번호 오름차순 정렬. 정렬을 안 하면 순서가 매번 달라질 수 있다.
				   + " from carlist order by carno asc";

		// SQL 을 실행할 도구와 결과표를 한 번에 만든다. 블록이 끝나면 둘 다 자동으로 닫힌다.
		try (PreparedStatement pstmt = con.prepareStatement(sql);
			 ResultSet rs = pstmt.executeQuery()) {

			// rs.next() = 결과표의 다음 줄로 이동. 더 이상 줄이 없으면 false 가 되어 반복이 끝난다.
			while (rs.next()) {
				// 한 줄을 CarListVo 로 바꿔 목록에 담는다 (변환은 아래 mapCar 가 맡는다)
				list.add(mapCar(rs));
			}
		}

		// 채워진 목록을 Service 에게 돌려준다
		return list;
	}

	//===========================================================
	// 2. 차량 유형별(소형/중형/대형) 목록 조회
	//    category : "Small" / "Mid" / "Big"
	//===========================================================		"Mid"
	public List<CarListVo> selectCarsByCategory(Connection con, String category) throws SQLException {

		// 결과를 담을 빈 목록
		List<CarListVo> list = new ArrayList<CarListVo>();

		// 1번과 같지만 where 조건이 하나 붙었다. 값 자리는 ? 로 비워 둔다.
		String sql = "select carno, carname, carcompany, carprice, carusepeople, carinfo, carimg, carcategory"
				   + " from carlist where carcategory=? order by carno asc";

		// 실행 도구를 만든다. 아직 ? 에 값이 안 들어갔으므로 여기서는 실행하지 않는다.
		try (PreparedStatement pstmt = con.prepareStatement(sql)) {

			// 첫 번째 ? 에 등급 값을 끼워 넣는다. 글자이므로 setString.
			pstmt.setString(1, category);

			// 값을 다 끼운 뒤에 실행한다. 결과표도 블록이 끝나면 자동으로 닫힌다.
			try (ResultSet rs = pstmt.executeQuery()) {
				// 결과를 한 줄씩 읽는다
				while (rs.next()) {
					// 한 줄을 VO 로 바꿔 담는다
					list.add(mapCar(rs));
				}
			}
		}

		// 해당 등급의 조회한 차량 목록(ArrayList<CarListVO객체>(); 배열) 을 CarService 부장에게 돌려준다 (없으면 빈 목록)
		return list;
	}

	//===========================================================
	// 3. 차량 1대 조회 (상세보기 / 금액 계산용)
	//    반환 : CarListVo,  없으면 null
	//===========================================================
	public CarListVo selectOneCar(Connection con, int carno) throws SQLException {

		// 기본키(carno)로 찾으므로 결과는 0건 아니면 1건이다
		String sql = "select carno, carname, carcompany, carprice, carusepeople, carinfo, carimg, carcategory"
				   + " from carlist where carno=?";

		// 실행 도구 준비
		try (PreparedStatement pstmt = con.prepareStatement(sql)) {

			// 첫 번째 ? 에 차량번호를 끼운다. 숫자이므로 setInt.
			pstmt.setInt(1, carno);

			// 실행하고 결과표를 받는다
			try (ResultSet rs = pstmt.executeQuery()) {
				// while 이 아니라 if 인 이유 : 결과가 많아야 1건이기 때문이다
				if (rs.next()) {
					// 찾았으면 VO 로 바꿔 바로 돌려준다
					return mapCar(rs);
				}
			}
		}

		// 여기까지 왔다 = 그 번호의 차가 없다.
		// null 을 돌려주면 Service 가 NotFoundException(404) 으로 바꿔 던진다.
		return null;
	}

	//===========================================================
	// 4. 예약 등록
	//    반환 : 추가된 행 수 (성공 1)
	//===========================================================
	public int insertOrder(Connection con, CarOrderVO vo, int totalPrice) throws SQLException {

		// insert into 테이블(컬럼들) values(?,?,...) 이 기본 형태다.
		// 컬럼 개수와 ? 개수가 정확히 같아야 한다 (여기서는 12개).
		String sql = "insert into non_carorder(carno, carqty, carreserveday, carbegindate,"
				   + " carins, carwifi, carnave, carbabyseat, memberphone, memberpass, member_id, total_price)"
				   + " values(?,?,?,?,?,?,?,?,?,?,?,?)";

		// 실행 도구 준비
		try (PreparedStatement pstmt = con.prepareStatement(sql)) {

			// ? 에 값을 순서대로 끼운다. 위 컬럼 순서와 번호가 정확히 짝을 이뤄야 한다.
			pstmt.setInt(1, vo.getCarno());              // 1번 ? : 차량번호
			pstmt.setInt(2, vo.getCarqty());             // 2번 ? : 대여 수량
			pstmt.setInt(3, vo.getCarreserveday());      // 3번 ? : 대여 일수
			pstmt.setString(4, vo.getCarbegindate());    // 4번 ? : 대여 시작일
			pstmt.setInt(5, vo.getCarins());             // 5번 ? : 보험 선택(0/1)
			pstmt.setInt(6, vo.getCarwifi());            // 6번 ? : 와이파이 선택(0/1)
			pstmt.setInt(7, vo.getCarnave());            // 7번 ? : 네비게이션 선택(0/1)
			pstmt.setInt(8, vo.getCarbabyseat());        // 8번 ? : 카시트 선택(0/1)
			pstmt.setString(9, vo.getMemberphone());     // 9번 ? : 연락처(나중에 조회할 때 쓴다)
			pstmt.setString(10, vo.getMemberpass());     // 10번 ? : 예약 비밀번호(이미 해시된 값)

			//회원 예약이면 아이디, 비회원이면 null
			if (vo.getId() == null || vo.getId().trim().isEmpty()) {
				
				// setNull = "이 칸은 값이 없다" 를 DB 에 정확히 알린다.
				// 빈 문자열("")을 넣으면 "값이 없음" 이 아니라 "빈 글자" 가 저장되어 뜻이 달라진다.
				pstmt.setNull(11, java.sql.Types.VARCHAR);
			} else {
				// 로그인한 회원의 예약이면 아이디를 저장한다 (나중에 "내 예약" 조회에 쓴다)
				pstmt.setString(11, vo.getId());
			}

			// 12번 ? : 최종 결제 금액. 나중에 얼마를 받았는지 확인할 수 있게 저장해 둔다.
			pstmt.setInt(12, totalPrice);

			// 전체 insert 문장 실행하고 "저장된 행 수" 를 돌려준다. 정상이면 1 이다.
			return pstmt.executeUpdate();
		}
	}

	//===========================================================
	// 5. 예약 조회 (연락처 + 예약 비밀번호)
	//    아직 시작하지 않은(대여 시작일이 오늘 이후인) 예약만 조회한다
	//===========================================================

	public List<CarConfirmVo> selectOrdersByPhone(Connection con, String memberphone)
			throws SQLException {

		// 결과를 담을 빈 목록
		List<CarConfirmVo> list = new ArrayList<CarConfirmVo>();

		// o 와 c 는 테이블에 붙인 별명(alias)이다. o=non_carorder, c=carlist.
		// 별명을 쓰면 어느 테이블의 컬럼인지 한눈에 보인다.
		String sql = "select o.non_orderid, o.carno, o.carqty, o.carreserveday, o.carbegindate,"
				   + "       o.carins, o.carwifi, o.carnave, o.carbabyseat,"
				   + "       o.memberphone, o.memberpass, o.total_price,"
				   + "       c.carname, c.carimg, c.carprice"
				   + "  from non_carorder o  join carlist c "
				   + " on o.carno = c.carno"
				   + " where o.carbegindate > current_date()"
				   + "   and o.memberphone = ?"
				   + " order by o.carbegindate asc";

		// 실행 도구 준비
		try (PreparedStatement pstmt = con.prepareStatement(sql)) {

			// 첫 번째(그리고 유일한) ? 에 예약 확인을 위해 입력한 비밀번호로 설정 
			pstmt.setString(1, memberphone);

			// 실행하고 결과표를 받는다
			try (ResultSet rs = pstmt.executeQuery()) {
			
				while (rs.next()) {
					
					list.add(mapOrder(rs, true));
				}
			}
		}

		// 이 연락처로 된 "앞으로의 예약" 목록을 돌려준다
		return list;
	}

	//===========================================================
	// 6. 예약 1건 조회 (예약번호)
	//    반환 : CarConfirmVo,  없으면 null
	//===========================================================
	public CarConfirmVo selectOrderById(Connection con, int orderid) throws SQLException {

		// 5번과 같은 조인이지만, 조건이 예약번호 하나다 (결과는 0건 또는 1건)
		String sql = "select o.non_orderid, o.carno, o.carqty, o.carreserveday, o.carbegindate,"
				   + "       o.carins, o.carwifi, o.carnave, o.carbabyseat,"
				   + "       o.memberphone, o.memberpass, o.total_price,"
				   + "       c.carname, c.carimg, c.carprice"
				   + "  from non_carorder o"
				   + "  join carlist c on o.carno = c.carno"
				   + " where o.non_orderid = ?";

		// 실행 도구 준비
		try (PreparedStatement pstmt = con.prepareStatement(sql)) {

			// 첫 번째 ? 에 예약번호를 끼운다
			pstmt.setInt(1, orderid);

			// 실행하고 결과표를 받는다
			try (ResultSet rs = pstmt.executeQuery()) {
				// 결과가 있으면(=예약이 있으면)
				if (rs.next()) {
					// CarService로  CarConfirmVo 객체를  돌려준다
					return mapOrder(rs, true);
				}
			}
		}

		// 그 번호의 예약이 없다는 뜻
		return null;
	}

	//===========================================================
	// 7. 예약 정보 수정
	//    예약번호 + 예약 비밀번호가 모두 맞아야 수정된다
	//===========================================================
	public int updateOrder(Connection con, CarOrderVO vo, int totalPrice) throws SQLException {

		/*
		 [변경] where 절에서 memberpass 조건을 제거했다.

		   해시는 = 비교가 불가능하므로 비밀번호 검증을 SQL 로 할 수 없다.
		   Service 가 저장된 해시를 꺼내 java.util.Objects.equals() 로 확인한 뒤에만
		   이 메소드를 호출한다. (검증을 통과하지 못하면 아예 호출되지 않는다)
		*/
		// update 테이블 set 컬럼=? , 컬럼=? where 조건 형태다.
		// where 를 빠뜨리면 모든 행이 수정되므로 절대 잊으면 안 된다.
		String sql = "update non_carorder set"
				   + " carbegindate=?, carreserveday=?, carqty=?,"
				   + " carins=?, carwifi=?, carnave=?, carbabyseat=?, total_price=?"
				   + " where non_orderid=?";

		// 실행 도구 준비
		try (PreparedStatement pstmt = con.prepareStatement(sql)) {

			// set 절에 나온 순서대로 ? 를 채운다
			pstmt.setString(1, vo.getCarbegindate());    // 1번 ? : 바뀐 시작일
			pstmt.setInt(2, vo.getCarreserveday());      // 2번 ? : 바뀐 대여 일수
			pstmt.setInt(3, vo.getCarqty());             // 3번 ? : 바뀐 수량
			pstmt.setInt(4, vo.getCarins());             // 4번 ? : 보험
			pstmt.setInt(5, vo.getCarwifi());            // 5번 ? : 와이파이
			pstmt.setInt(6, vo.getCarnave());            // 6번 ? : 네비게이션
			pstmt.setInt(7, vo.getCarbabyseat());        // 7번 ? : 카시트
			pstmt.setInt(8, totalPrice);                 // 8번 ? : 다시 계산한 총액

			//수정 조건 : 예약번호 (비밀번호 검증은 Service 가 미리 끝냈다)
			pstmt.setInt(9, vo.getOrderid());

			// 바뀐 행 수를 돌려준다. 0 이면 그 예약번호가 없었다는 뜻이다.
			return pstmt.executeUpdate();
		}
	}

	//===========================================================
	// 8. 예약 취소(삭제)
	//    비밀번호 검증은 Service 가 하고, 여기서는 예약번호로만 삭제한다
	//===========================================================
	public int deleteOrder(Connection con, int orderid) throws SQLException {

		/*
		 [변경] where 절에서 memberpass 조건을 제거했다.
		        해시는 = 비교가 불가능하므로 Service 가 matches() 로 검증한 뒤 호출한다.
		*/
		// delete from 테이블 where 조건. where 가 없으면 표 전체가 지워진다.
		String sql = "delete from non_carorder where non_orderid=?";

		// 실행 도구 준비
		try (PreparedStatement pstmt = con.prepareStatement(sql)) {
			// 지울 예약의 번호를 끼운다
			pstmt.setInt(1, orderid);
			// 지워진 행 수를 돌려준다 (1이면 성공, 0이면 그런 예약이 없었다)
			return pstmt.executeUpdate();
		}
	}

	//===========================================================
	// 9. 예약 비밀번호만 교체 (평문 -> 해시 자동 이관에 사용)
	//===========================================================
	/*
	   기존 예약들은 비밀번호가 평문으로 저장되어 있다.
	   원래 비밀번호를 알 수 없으므로 일괄 변환이 불가능하다.

	   그래서 사용자가 예약을 조회하며 비밀번호를 입력해 맞춘 그 순간
	   (= 원래 비밀번호를 알 수 있는 유일한 시점) 해시로 바꿔 저장한다.
	   회원 로그인 시 비밀번호를 이관하는 것과 같은 방식이다.
	*/
	public int updateOrderPassword(Connection con, int orderid, String encodedPassword)
			throws SQLException {

		// 비밀번호 한 칸만 바꾸는 update 문
		String sql = "update non_carorder set memberpass=? where non_orderid=?";

		// 실행 도구 준비
		try (PreparedStatement pstmt = con.prepareStatement(sql)) {
			// 1번 ? : 새로 만든 해시 값
			pstmt.setString(1, encodedPassword);
			// 2번 ? : 어느 예약인지
			pstmt.setInt(2, orderid);
			// 바뀐 행 수를 돌려준다
			return pstmt.executeUpdate();
		}
	}

	//===========================================================
	// 내부 공통 : ResultSet -> VO 변환
	//===========================================================
	// 같은 변환 코드를 메소드마다 복사하면, 컬럼이 하나 늘 때 여러 곳을 고쳐야 하고
	// 한 곳만 빠뜨리면 화면마다 값이 달라진다. 그래서 한 곳에 모아 둔다.

	/** 차량 한 행을 CarListVo 로 변환 */
	private CarListVo mapCar(ResultSet rs) throws SQLException {

		// 생성자에 8개 값을 순서대로 넘겨 VO 를 만든다.
		// rs.getInt("컬럼명") / rs.getString("컬럼명") 으로 현재 줄의 값을 꺼낸다.
		// 번호 대신 컬럼 "이름" 으로 꺼내는 이유 : select 순서가 바뀌어도 안 깨지기 때문이다.
		return new CarListVo(
				rs.getInt("carno"),           //차량번호
				rs.getString("carname"),      //차량명
				rs.getString("carcompany"),   //제조사
				rs.getInt("carprice"),        //1일 대여료
				rs.getInt("carusepeople"),    //탑승 인원
				rs.getString("carinfo"),      //차량 설명
				rs.getString("carimg"),       //이미지 파일명
				rs.getString("carcategory")); //등급
	}

	/**
	 * 예약 한 행을 CarConfirmVo 로 변환
	 * @param withCarInfo 차량 정보(carname/carimg/carprice)까지 함께 조회했는지 여부
	 */
	private CarConfirmVo mapOrder(ResultSet rs, boolean withCarInfo) throws SQLException {

		// 빈 상자를 먼저 만들고 setter 로 하나씩 채우는 방식이다.
		// CarListVo 와 달리 채울 값이 많고 상황에 따라 일부만 채우기 때문이다.
		CarConfirmVo vo = new CarConfirmVo();

		//예약 정보 (non_carorder)
		vo.setOrderid(rs.getInt("non_orderid"));            // 예약번호
		vo.setCarno(rs.getInt("carno"));                    // 차량번호
		vo.setCarqty(rs.getInt("carqty"));                  // 대여 수량
		vo.setCarreserveday(rs.getInt("carreserveday"));    // 대여 일수
		vo.setCarbegindate(rs.getString("carbegindate"));   // 대여 시작일
		vo.setCarins(rs.getInt("carins"));                  // 보험 선택 여부
		vo.setCarwifi(rs.getInt("carwifi"));                // 와이파이 선택 여부
		vo.setCarnave(rs.getInt("carnave"));                // 네비게이션 선택 여부
		vo.setCarbabyseat(rs.getInt("carbabyseat"));        // 카시트 선택 여부
		vo.setMemberphone(rs.getString("memberphone"));     // 연락처
		vo.setMemberpass(rs.getString("memberpass"));       // 저장된 비밀번호(해시)

		//차량 정보 (carlist)
		// 조인해서 가져온 경우에만 채운다. 안 가져왔는데 꺼내려 하면 예외가 난다.
		if (withCarInfo) {
			vo.setCarname(rs.getString("carname"));    // 차량명
			vo.setCarimg(rs.getString("carimg"));      // 이미지 파일명
			vo.setCarprice(rs.getInt("carprice"));     // 1일 대여료
		}

		// 채워진 상자를 돌려준다
		return vo;
	}

}//CarDAO 클래스
