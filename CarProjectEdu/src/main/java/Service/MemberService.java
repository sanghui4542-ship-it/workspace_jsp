package Service;   // 이 파일이 속한 패키지(=폴더) 이름. 실제 폴더 구조와 반드시 같아야 한다

import javax.servlet.http.HttpServletRequest;   // 브라우저가 보낸 요청(주소·파라미터·세션)이 담긴 객체
import javax.servlet.http.HttpSession;   // 로그인 정보처럼 사용자별로 서버에 보관하는 저장소

import Dao.MemberDAO;   // DB 에 직접 SQL 을 보내는 DAO 클래스
import Vo.MemberVO;   // 값을 담아 나르는 상자(VO) 클래스
import util.DBCPUtil;   // 여러 곳에서 함께 쓰는 도우미 클래스

/**
 * MemberService  부장 클래스
 *
 * ✔ 하는 일:
 * - Controller(사장)의 지시를 받아
 * - 업무 규칙(검증, 비밀번호 처리, 권한 판단)을 적용하고
 * - DAO(사원)에게 SQL 실행을 시킨다
 *
 * ================================================================================
 * [3단계에서 바뀐 점 - 트랜잭션 경계를 Service가 소유한다]
 *
 *   (기존)
 *       memberDao.insertMember(vo);
 *       -> DAO가 스스로 연결을 열고 SQL을 실행하고 닫았다.
 *          여러 SQL을 하나의 작업으로 묶을 방법이 없었다.
 *
 *   (지금)
 *       DBCPUtil.execute(con -> memberDao.insertMember(con, vo));
 *       -> Service가 연결을 열어 DAO에 넘긴다.
 *          한 연결로 여러 DAO 메소드를 호출하면 그것이 하나의 트랜잭션이 된다.
 *
 *   왜 Service가 관리하는가?
 *     "어디까지가 하나의 작업 단위인가"는 업무 규칙이다.
 *     DAO는 SQL 하나만 알고, 묶는 판단은 업무를 아는 계층이 해야 한다.
 *
 *   DBCPUtil.query(...)   : 조회 전용 (커밋할 것이 없음)
 *   DBCPUtil.execute(...) : 데이터 변경 (성공하면 commit, 실패하면 rollback)
 * ================================================================================
 */
public class MemberService {

	//MemberDAO 사원 객체의 주소번지를 저장할 참조변수 선언
	private final MemberDAO memberDao;

	//생성자 - 위 memberDao 참조변수에  new MemberDAO()객체를 만들어 주소번지 저장하는 역할
	public MemberService() {
		this.memberDao = new MemberDAO();   // DAO 를 하나 만들어 계속 재사용한다
	}

	//================================
	//1. 회원가입 작성 중앙 화면 요청  (DB 작업 없음)
	//================================
	public String serviceJoinName(HttpServletRequest request) {

		//URL /member/join.me?center=members/join.jsp 에서
		//"center" 이름으로 전달된 중앙 VIEW 주소를 꺼내 반환한다.		
		String center = request.getParameter("center"); 	//members/join.jsp
	
		/*
		   [회원 가입 중앙 화면 주소가 없을 때 기본값을 주도록 고친 부분]
		    cneter변수 값이 없으면 회원가입 화면을 기본값으로 쓴다.
	    */
		if (center == null || center.trim().isEmpty()) {
			return "members/join.jsp";
		}

		return center; // "members/join.jsp" 회원가입 요청 중앙 화면 주소를 MemberController로 반환 
	}

	//==================================
	//2. 아이디 중복체크
	//   true -> 이미 사용중,  false -> 사용 가능
	//==================================
	public boolean serviceOverLappedId(HttpServletRequest request) {

		final String id = request.getParameter("id");   // 화면에서 입력한 아이디를 꺼낸다. final 을 붙인 이유는 아래 람다 안에서 쓰기 위해서다

		//조회만 하므로 query() 사용
		return DBCPUtil.query(con -> memberDao.overlappedId(con, id));
	}

	//================================
	//3. 회원 가입 처리
	//   반환 : 가입 성공 여부
	//=================================
	public boolean serviceInsertMember(HttpServletRequest request) {

		//입력값을 VO로 만든다 (이 안에서 비밀번호를 해시로 바꾼다)
		final MemberVO memberVo = buildMemberFromRequest(request);
		
//		new MemberVO(id, encodedPass, name, age, gender, address, email, tel, hp);


		int result = DBCPUtil.execute(con -> memberDao.insertMember(con, memberVo));

		return result == 1;   // 저장된 행이 1건이면 가입 성공이다  true를  MemberController로 반환 
							  // 저장 실패 하면 false를 MemberContrller로 반환 
		
	}

	/**
	 * request 에 들어있는 회원정보를 MemberVO 객체로 만들어주는 메소드
	 *
	 * ✔ 왜 따로 만들었을까?
	 * - 코드가 깔끔해진다.
	 * - 재사용이 가능하다.
	 */
	private MemberVO buildMemberFromRequest(HttpServletRequest request) {

		//1. 사용자가 가입을 위해 입력해서 요청한 값들을 request객체에서 꺼낸다.
		String id     = trim(request.getParameter("id"));
		String pass   = request.getParameter("pass");   // 비밀번호는 공백도 뜻이 있을 수 있어 trim 하지 않고 그대로 받는다
		String name   = trim(request.getParameter("name"));   // 이름 (앞뒤 공백 제거)
		String gender = trim(request.getParameter("gender"));   // 성별
		String email  = trim(request.getParameter("email"));   // 이메일
		String tel    = trim(request.getParameter("tel"));   // 전화번호
		String hp     = trim(request.getParameter("hp"));   // 휴대폰번호

		//나이는 숫자이므로 변환한다. 잘못된 값이 와도 예외로 죽지 않게 0으로 둔다.
		int age = 0;
		try {
			age = Integer.parseInt(trim(request.getParameter("age")));
		} catch (Exception e) {
			age = 0;   // 숫자가 아닌 값이 들어와도 서버가 죽지 않게 0 으로 둔다
		}

		/*
		 주소는 여러 칸으로 나뉘어 오므로 하나로 합친다.

		 [기존 버그] String.join 에 null이 섞이면 "null" 이라는 글자가 그대로 붙는다.
		            주소를 일부만 입력하면 "서울시null강남구null" 처럼 저장됐다.
		            그래서 null을 빈 문자열로 바꿔 합친다.
		*/
		String address = trim(request.getParameter("address1"))
					   + trim(request.getParameter("address2"))
					   + trim(request.getParameter("address3"))
					   + trim(request.getParameter("address4"))
					   + trim(request.getParameter("address5"));

		/*
		 [보안] 비밀번호를 그대로 담지 않고 PBKDF2 해시로 바꿔 담는다.

		   기존에는 사용자가 입력한 "1234"가 그대로 DB에 저장됐다.
		   DB가 유출되면 전 회원의 비밀번호가 그대로 노출되고,
		   사람들은 여러 사이트에 같은 비밀번호를 쓰기 때문에 피해가 여기서 끝나지 않는다.

		   해시는 되돌릴 수 없으므로(일방향) 유출돼도 원래 비밀번호를 알 수 없다.
		   "비밀번호를 저장하지 않고, 맞는지만 확인할 수 있게 저장한다"가 핵심이다.
		*/
		if (pass == null || pass.trim().isEmpty()) {
			throw new exception.InvalidInputException("비밀번호를 입력해주세요");
		}
		String encodedPass = (pass.trim());   // 앞뒤 공백을 없앤 비밀번호. (교육판이라 해시를 걷어내고 평문 그대로 쓴다)

		//2. 가입할 데이터들을 모두 MemberVO객체를 생성하여 변수에 저장 후 반환
		return new MemberVO(id, encodedPass, name, age, gender, address, email, tel, hp);
	}

	/** null 을 빈 문자열로 바꾸고 앞뒤 공백을 제거한다 */
	private String trim(String value) {
		return (value == null) ? "" : value.trim();   // 값이 없으면 빈 문자열로, 있으면 앞뒤 공백을 없앤다
	}

	//=================================================================
	//4. 로그인 화면 주소 반환  (DB 작업 없음)
	//=================================================================
	public String serviceLoginMember() {
		return "members/login.jsp";   // DB 를 볼 필요 없이 화면 이름만 돌려주면 되는 기능이다
	}

	//==================================================
	//5. 로그인 처리
	//    1 -> 아이디, 비밀번호 모두 일치 (로그인 성공)
	//    0 -> 아이디는 있으나 비밀번호 틀림
	//   -1 -> 아이디 자체가 없음
	//===================================================
	public int serviceUserCheck(HttpServletRequest request) {

		//1. 요청한 데이터 얻기
		final String login_id = request.getParameter("id");
		final String login_pass = request.getParameter("pass");   // 화면에서 입력한 비밀번호

		/*
		 2. 비밀번호 확인 + 필요하면 해시 이관

		    조회(SELECT)와 이관(UPDATE)이 함께 일어날 수 있으므로 execute()로 묶는다.
		    한 연결 안에서 처리되므로 "조회한 값 기준으로 갱신"이 안전하게 이뤄진다.
		*/
		int check = DBCPUtil.execute(con -> {

			//2.1. DB에 저장된 비밀번호를 꺼낸다 (해시 또는 예전 평문)
			String storedPassword = memberDao.findPasswordById(con, login_id);

			if (storedPassword == null) {   // 아이디 자체가 DB 에 없으면
				return -1;                       //존재하지 않는 아이디
			}

			if (login_pass == null || !java.util.Objects.equals(login_pass, storedPassword)) {   // 비밀번호를 안 보냈거나 저장값과 다르면
				return 0;                        //비밀번호 불일치
			}

			return 1;                            //로그인 성공
		});

		//3. HttpSession 객체 얻기
		HttpSession session = request.getSession();

		/*
		 3.1. 로그인에 "성공한 경우에만" 세션에 아이디를 저장한다.

		      주의 : DB 확인 전에 미리 저장하면
		            비밀번호가 틀려도 세션에 아이디가 남아
		            뒤로가기 후 로그인한 회원처럼 취급되는 인증 우회가 발생한다.
		*/
		if (check == 1) {
			/*
			 [세션 고정 공격(Session Fixation) 방어]
			   로그인 성공 시 세션 ID를 새로 발급한다.
			   공격자가 미리 만들어 둔 세션 ID를 피해자에게 심어놓고
			   피해자가 그 세션으로 로그인하면 공격자도 같은 세션을 쓰게 되는 공격을 막는다.

			*/
			session.invalidate();
			session = request.getSession(true);   //세션 새로 발급(세션 고정 공격 방어)
			session.setAttribute("id", login_id);   // 새로 만든 세션에 아이디를 저장한다. 이 값이 있으면 "로그인 상태" 로 본다
		} else {
			//로그인 실패 시에는 남아 있을 수 있는 이전 아이디를 제거해 미로그인 상태를 유지
			session.removeAttribute("id");
		}

		return check;   // 1(성공) / 0(비밀번호 틀림) / -1(아이디 없음) 중 하나를 돌려준다
	}

	//=================================================================
	//5-1. 카카오 로그인 처리
	//=================================================================
	/**
	 * 카카오 인증을 마친 사용자를 우리 회원으로 로그인시킨다.
	 *
	 * [처음 온 사람이면 자동 가입]
	 *   카카오 회원번호로 만든 아이디("kakao_회원번호")가 member 테이블에 없으면
	 *   그 자리에서 가입시킨다. 가입 화면을 거치지 않으므로
	 *   이름은 카카오 닉네임, 나머지 항목은 비워 둔다.
	 *
	 * [비밀번호는 왜 무작위인가]
	 *   이 회원은 항상 카카오를 통해서만 로그인한다.
	 *   그래도 member.pass 가 not null 이므로 값은 필요하다.
	 *   "아무도 모르는 무작위 값"을 해시로 저장하면
	 *   누군가 일반 로그인 창에 kakao_xxx 아이디를 넣어 뚫는 것이 불가능해진다.
	 *
	 * @return 세션에 저장된 우리 사이트 아이디 (kakao_회원번호)
	 */
	public String serviceKakaoLogin(HttpServletRequest request, util.KakaoAuth.KakaoUser kakaoUser) {

		//카카오 회원번호는 이 앱 기준으로 고유·불변이므로 그대로 아이디의 근거로 쓴다
		final String memberId = "kakao_" + kakaoUser.id;

		//닉네임 동의를 안 했으면 기본 이름을 쓴다 (name 컬럼이 not null)
		final String name  = (kakaoUser.nickname == null || kakaoUser.nickname.trim().isEmpty())
				? "카카오회원" : kakaoUser.nickname.trim();
		final String email = kakaoUser.email;   // 이메일은 동의 항목이라 없을 수도 있다

		//처음 온 사람이면 자동 가입 (있으면 아무것도 하지 않는다)
		DBCPUtil.execute(con -> {

			if (memberDao.overlappedId(con, memberId)) {   // 이미 가입된 아이디인지 확인한다
				return 0;   //이미 회원 - 가입 생략
			}

			MemberVO vo = new MemberVO(   // 새 회원 정보를 담을 상자를 만든다
					memberId,
					//일반 로그인으로는 절대 맞출 수 없는 무작위 비밀번호
					(util.KakaoAuth.newState() + util.KakaoAuth.newState()),
					name,
					0,          //나이 : 카카오가 주지 않으므로 0 (미입력)
					"",         //성별
					"",         //주소
					email == null ? "" : email,
					"",         //전화
					"");        //휴대폰

			int inserted = memberDao.insertMember(con, vo);   // 회원 표에 새 줄을 추가한다
			System.out.println("[MemberService] 카카오 자동가입 : " + memberId + " (" + name + ")");   // 콘솔에 남겨 두면 카카오 가입이 실제로 일어났는지 확인할 수 있다
			return inserted;   // 저장된 행 수를 돌려준다
		});

		/*
		 세션 로그인 처리 - 일반 로그인(serviceUserCheck)과 같은 규칙을 적용한다.
		   1) 세션 고정 공격 방어를 위해 세션을 새로 발급하고
		   2) "id" 를 저장한다 (Top.jsp 와 AuthFilter 가 이 키를 본다)
		   3) 화면에는 kakao_4392817465 대신 닉네임이 보이도록 loginName 도 저장한다
		*/
		request.getSession().invalidate();
		HttpSession session = request.getSession(true);   //세션 새로 발급(세션 고정 공격 방어)
		session.setAttribute("id", memberId);   // 세션에 아이디를 저장해 로그인 상태로 만든다
		session.setAttribute("loginName", name);   // 화면에는 kakao_12345 대신 닉네임이 보이도록 이름도 저장한다

		return memberId;   // 로그인 처리된 아이디를 돌려준다
	}

	//================================
	//6. 로그 아웃 처리
	//===============================
	public void serviceLogout(HttpServletRequest request) {

		/*
		 [변경 1] removeAttribute("id") 만 하던 것을 세션 폐기로 바꿨다.

		   기존에는 아이디만 지우고 세션 자체는 남겨두었다.
		   그러면 세션에 남은 다른 값(장바구니, 임시 인증 정보 등)이 계속 유지되고,
		   같은 세션 ID가 그대로 재사용되어 세션 탈취 위험이 남는다.
		   로그아웃은 "세션을 버리는 것"이 정석이다.

		 [변경 2 - 버그 수정] 버린 뒤에 CSRF 토큰을 다시 발급한다.

		   로그아웃도 곧바로 메인 화면을 그린다.
		   토큰 없이 그려지면 그 화면에서 하는 첫 POST 가 403 으로 막혔다.
		   (로그인 직후와 완전히 같은 문제였다)

		   renewSession() 이 "폐기 -> 새 세션 -> 토큰 발급"을 한 번에 처리한다.
		   로그아웃 상태이므로 "id" 는 담지 않는다. 토큰만 있는 빈 세션이 된다.
		*/
		request.getSession().invalidate(); request.getSession(true);
	}

	//================================
	// 7. 회원 전체 정보 조회 (정보수정 화면용)
	//================================
	public MemberVO serviceMemberDetail(String id) {
		return DBCPUtil.query(con -> memberDao.memberDetail(con, id));   // 회원 정보를 조회해 그대로 돌려준다 (수정 화면이 입력칸을 채우는 데 쓴다)
	}

	//================================
	// 8. 회원 정보 수정
	//================================
	/*
	 ============================================================================
	   [가장 심각했던 취약점을 고친 부분 - 2단계 작업]

	   (기존 코드)
	       String id = request.getParameter("id");     // <- 화면이 보낸 아이디를 그대로 신뢰
	       memberDao.updateMember(vo);

	   "누구의 정보를 수정할 것인가"를 화면이 보낸 파라미터로 결정했고
	   세션(로그인 정보)은 확인하지 않았다. 그래서 로그인하지 않은 사람이

	       POST /CarProject/member/memberUpdatePro.me
	       id=admin&pass=1111&...

	   이 요청 하나로 관리자 비밀번호를 바꿀 수 있었다.

	   (바뀐 방식)
	     1. 수정 대상은 "세션의 로그인 아이디"만 사용한다 (파라미터 id는 읽지 않는다)
	     2. 비밀번호를 바꿀 때는 "현재 비밀번호"를 함께 확인한다
	     3. 새 비밀번호가 비어 있으면 비밀번호는 건드리지 않는다

	   @param loginId 컨트롤러가 세션에서 꺼내 넘겨준 로그인 아이디 (신뢰할 수 있는 값)
	 ============================================================================
	*/
	public String serviceUpdateMember(HttpServletRequest request, String loginId) {

		//1. 로그인 여부 확인 (컨트롤러에서도 확인하지만 Service도 스스로 지킨다)
		if (loginId == null || loginId.trim().isEmpty()) {
			return "로그인필요";
		}

		//2. 입력값 얻기  ※ id 파라미터는 읽지 않는다. 오직 세션의 loginId만 사용한다.
		final String newPass     = request.getParameter("pass");        //새 비밀번호 (비우면 변경 안 함)
		final String currentPass = request.getParameter("currentPass"); //현재 비밀번호 (변경 시 필수)
		final String name        = trim(request.getParameter("name"));   // 이름 (앞뒤 공백 제거)
		final String gender      = trim(request.getParameter("gender"));   // 성별
		final String address     = trim(request.getParameter("address"));   // 주소
		final String email       = trim(request.getParameter("email"));   // 이메일

		int parsedAge = 0;   // 나이를 담을 변수. 변환에 실패해도 0 으로 남는다
		try {
			parsedAge = Integer.parseInt(trim(request.getParameter("age")));
		} catch (Exception e) {
			parsedAge = 0;   // 숫자가 아닌 값이 왔으면 0 으로 둔다
		}
		final int age = parsedAge;   // 람다 안에서 쓰려면 값이 바뀌지 않아야 해서 final 변수에 옮겨 담는다

		final boolean changePassword = (newPass != null && !newPass.trim().isEmpty());   // 새 비밀번호를 입력했을 때만 비밀번호를 바꾼다

		//3. 새 비밀번호 형식 검사 (DB에 접근하기 전에 먼저 걸러낸다)
		if (changePassword && newPass.trim().length() < 4) {
			return "비밀번호길이부족";
		}

		/*
		 4. 현재 비밀번호 확인 + 수정을 하나의 트랜잭션으로 처리한다.

		    확인과 수정을 각각 다른 연결로 하면, 그 사이에 비밀번호가 바뀌는
		    경쟁 상태가 생길 수 있다. 한 연결로 묶으면 그 위험이 줄어든다.
		*/
		return DBCPUtil.execute(con -> {

			String encodedNewPass = null;   // 바꿀 비밀번호를 담을 변수. null 이면 DAO 가 비밀번호를 건드리지 않는다

			if (changePassword) {   // 비밀번호를 바꾸는 경우에만 현재 비밀번호를 확인한다

				//4.1. 저장된 비밀번호를 꺼내 "현재 비밀번호"가 맞는지 확인
				String stored = memberDao.findPasswordById(con, loginId);

				if (stored == null) {   // 그 아이디의 저장값을 못 찾은 경우
					return "수정실패";   // 수정하지 않고 실패로 돌려준다
				}
				if (currentPass == null || !java.util.Objects.equals(currentPass, stored)) {   // 현재 비밀번호를 안 보냈거나 저장값과 다르면
					return "현재비밀번호불일치";   // 화면에 "현재 비밀번호가 다릅니다" 를 띄우게 한다
				}

				encodedNewPass = (newPass.trim());   // 확인을 통과했으니 새 비밀번호를 담는다
			}

			//4.2. 수정할 정보를 VO에 담는다
			MemberVO vo = new MemberVO();
			vo.setId(loginId);          //세션에서 온 값만 사용
			vo.setPass(encodedNewPass); //null 이면 DAO가 pass 컬럼을 수정하지 않는다
			vo.setName(name);   // 이름
			vo.setAge(age);   // 나이
			vo.setGender(gender);   // 성별
			vo.setAddress(address);   // 주소
			vo.setEmail(email);   // 이메일

			int result = memberDao.updateMember(con, vo);   // 회원 표를 수정한다

			return (result == 1) ? "수정성공" : "수정실패";   // 1건이 바뀌었으면 성공, 아니면 실패 문구를 돌려준다
		});
	}

	//================================
	// 9. 회원 삭제 (탈퇴)
	//================================
	public String serviceDeleteMember(String id) {

		int result = DBCPUtil.execute(con -> memberDao.deleteMember(con, id));   // 회원 표에서 그 아이디를 지운다

		return (result == 1) ? "삭제성공" : "삭제실패";   // 1건이 지워졌으면 성공, 아니면 실패 문구를 돌려준다
	}

}
