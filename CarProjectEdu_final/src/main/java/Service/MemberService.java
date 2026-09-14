package Service;

/*
 * ============================================================================
 *  MemberService  -  부장(Service) : 회원 업무 규칙을 처리하는 클래스
 *
 *  사장(MemberController)에게 명령을 받아 업무 규칙을 처리하고,
 *  DB 작업은 사원(MemberDAO)에게 시킨 뒤 결과를 사장에게 보고(반환)한다.
 *
 *  DB 작업을 시키는 두 가지 방법 (util/DBCPUtil.java)
 *    DBCPUtil.query(con -> ...)   : 조회(select)만 할 때
 *    DBCPUtil.execute(con -> ...) : 추가·수정·삭제(insert, update, delete)를 할 때
 *                                   -> 전부 성공하면 commit, 하나라도 실패하면 rollback
 *    con -> ... 는 "DB 연결(con)을 받아서 -> 뒤의 일을 해라" 라는 뜻이다 (람다식)
 * ============================================================================
 */

// 요청 객체, 세션 객체
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

// 사원(MemberDAO), 상자(MemberVO), DB 연결 도우미(DBCPUtil)
import Dao.MemberDAO;
import Vo.MemberVO;
import util.DBCPUtil;

public class MemberService {

	// 사원(MemberDAO) 객체의 주소를 저장할 참조변수
	// final : 한 번 저장하면 다른 사원으로 바꿀 수 없다
	private final MemberDAO memberDao;

	//----------------------------------------------------------------
	// 생성자 : 부장(MemberService)이 만들어질 때 사원(MemberDAO)도 함께 만든다
	//----------------------------------------------------------------
	public MemberService() {

		// 사원(MemberDAO) 객체를 만들어서 memberDao 참조변수에 저장
		this.memberDao = new MemberDAO();
	}

	//----------------------------------------------------------------
	// serviceJoinName : 회원가입 화면 주소를 사장(MemberController)에게 반환한다
	//  사장의 "/join.me" 요청에서 부른다
	//----------------------------------------------------------------
	public String serviceJoinName(HttpServletRequest request) {

		// 요청 주소의 center 값 꺼내기  예) /member/join.me?center=members/join.jsp  ->  "members/join.jsp"
		String center = request.getParameter("center");

		// center 값이 없거나 비어 있으면 회원가입 화면 주소를 사장(MemberController)로 반환
		if (center == null || center.trim().isEmpty()) {
			return "members/join.jsp";
		}

		// center 값이 있으면 그 값을 그대로 사장(MemberController)로 반환
		// (보여줘도 되는 화면인지는 CarMain.jsp 가 util/CenterView 허용 목록으로 한 번 더 검사한다)
		return center;
	}

	//----------------------------------------------------------------
	// serviceOverLappedId : 가입하려는 아이디가 이미 있는지 확인한다
	//  사장의 "/joinIdCheck.me" 요청에서 부른다
	//----------------------------------------------------------------
	public boolean serviceOverLappedId(HttpServletRequest request) {

		// join.js 의 fetch 가 보낸 아이디 꺼내기  (id=hong  ->  "hong")
		final String id = request.getParameter("id");

		//사원(MemberDAO)에게 시키기 : 입력한 아이디를 MemberDAO 의 overlappedId(con, id) 호출해서 member 테이블에 같은 아이디가 있는지 조회(select) 작업 명령
		// 같은 아이디가 있으면 true(중복), 없으면 false(사용 가능)를 사장(MemberController)로 반환
		return DBCPUtil.query(con -> memberDao.overlappedId(con, id));
	}

	//----------------------------------------------------------------
	// serviceInsertMember : 새 회원을 추가(insert)한다
	//  사장의 "/joinPro.me" 요청에서 부른다
	//----------------------------------------------------------------
	public boolean serviceInsertMember(HttpServletRequest request) {

		// members/join.jsp 화면에서 입력한 정보들을 꺼내 MemberVO 상자에 담는다 (아래 buildMemberFromRequest 메소드)
		final MemberVO memberVo = buildMemberFromRequest(request);

		//사원(MemberDAO)에게 시키기 : members/join.jsp 화면에서 가입을 위해 입력한 정보들이 저장된 MemberVO 객체를 MemberDAO 의 insertMember(con, memberVo) 호출해서 새 회원 추가(insert) 작업 명령
		int result = DBCPUtil.execute(con -> memberDao.insertMember(con, memberVo));

		// 새 회원 추가(insert)에 성공한 행이 1건이면 가입 성공이므로 true 를 사장(MemberController)로 반환
		// 새 회원 추가(insert)에 성공한 행이 0건이면 가입 실패이므로 false 를 사장(MemberController)로 반환
		return result == 1;
	}

	//----------------------------------------------------------------
	// buildMemberFromRequest : join.jsp 에서 입력한 값들을 꺼내 MemberVO 상자에 담아 반환한다
	//  serviceInsertMember() 에서만 부른다
	//----------------------------------------------------------------
	private MemberVO buildMemberFromRequest(HttpServletRequest request) {

		// join.jsp 의 입력칸 name 속성 이름으로 입력값을 꺼내고, 앞뒤 공백을 지운다 (아래 trim 메소드)
		String id     = trim(request.getParameter("id"));
		String pass   = request.getParameter("pass");
		String name   = trim(request.getParameter("name"));
		String gender = trim(request.getParameter("gender"));
		String email  = trim(request.getParameter("email"));
		String tel    = trim(request.getParameter("tel"));
		String hp     = trim(request.getParameter("hp"));

		// 나이는 글자("25")로 오므로 숫자(25)로 바꾼다. 숫자가 아니면 0 으로 저장한다
		int age = 0;
		try {
			age = Integer.parseInt(trim(request.getParameter("age")));
		} catch (Exception e) {
			age = 0;
		}

		// 주소 입력칸 5개(우편번호, 도로명, 지번, 상세, 참고항목)를 하나의 글자로 이어 붙인다
		String address = trim(request.getParameter("address1"))
					   + trim(request.getParameter("address2"))
					   + trim(request.getParameter("address3"))
					   + trim(request.getParameter("address4"))
					   + trim(request.getParameter("address5"));

		// 비밀번호가 없으면 잘못된 입력이므로 400 예외를 던진다 -> 사장(BaseController)이 에러 화면을 보여준다
		if (pass == null || pass.trim().isEmpty()) {
			throw new exception.InvalidInputException("비밀번호를 입력해주세요");
		}

		// 비밀번호의 앞뒤 공백을 지운다
		String encodedPass = (pass.trim());

		// 꺼낸 값들을 MemberVO 상자에 담아서 반환
		return new MemberVO(id, encodedPass, name, age, gender, address, email, tel, hp);
	}

	//----------------------------------------------------------------
	// trim : 글자의 앞뒤 공백을 지운다. 값이 없으면(null) 빈 글자("")를 반환한다
	//  예) "  hong  " -> "hong" / null -> ""
	//----------------------------------------------------------------
	private String trim(String value) {
		return (value == null) ? "" : value.trim();
	}

	//----------------------------------------------------------------
	// serviceLoginMember : 로그인 화면 주소를 사장(MemberController)에게 반환한다
	//  사장의 "/login.me" 요청에서 부른다
	//----------------------------------------------------------------
	public String serviceLoginMember() {
		return "members/login.jsp";
	}

	//----------------------------------------------------------------
	// serviceUserCheck : 로그인 처리
	//  사장의 "/loginPro.me" 요청에서 부른다
	//  반환값 -> 1 : 로그인 성공 / 0 : 비밀번호 틀림 / -1 : 아이디 없음
	//----------------------------------------------------------------
	public int serviceUserCheck(HttpServletRequest request) {

		// login.jsp 에서 입력한 아이디, 비밀번호 꺼내기
		final String login_id = request.getParameter("id");
		final String login_pass = request.getParameter("pass");

		// 아래 con -> { } 안의 작업을 DB 연결 1개로 처리하고, 결과 숫자를 check 에 저장한다
		int check = DBCPUtil.execute(con -> {

			//사원(MemberDAO)에게 시키기 : 입력한 아이디를 MemberDAO 의 findPasswordById(con, login_id) 호출해서 DB 에 저장된 비밀번호 조회(select) 작업 명령
			String storedPassword = memberDao.findPasswordById(con, login_id);

			// 조회된 비밀번호가 없으면 아이디가 없는 것이므로 -1 반환
			if (storedPassword == null) {
				return -1;
			}

			// 입력한 비밀번호와 DB 의 비밀번호가 다르면 0 반환
			if (login_pass == null || !java.util.Objects.equals(login_pass, storedPassword)) {
				return 0;
			}

			// 아이디, 비밀번호가 모두 맞으면 1 반환
			return 1;
		});

		// 로그인 정보를 저장할 세션 꺼내기
		HttpSession session = request.getSession();

		// 로그인 성공이면
		if (check == 1) {

			// 이전 세션을 없애고 새 세션을 만든다 (다른 사람이 쓰던 세션 번호를 이어받지 않기 위해)
			session.invalidate();
			session = request.getSession(true);

			// 새 세션에 로그인 아이디 저장 -> Top.jsp 가 이 값으로 로그인 상태를 판단한다
			session.setAttribute("id", login_id);

		// 로그인 실패면 세션에 남아 있을지 모르는 로그인 아이디를 지운다
		} else {

			session.removeAttribute("id");
		}

		// 로그인 결과 숫자(1, 0, -1)를 사장(MemberController)로 반환
		return check;
	}

	//----------------------------------------------------------------
	// serviceKakaoLogin : 카카오 회원 로그인 처리 (처음 온 회원은 자동 가입)
	//  사장의 "/kakaoCallback.me" 요청에서 부른다
	//----------------------------------------------------------------
	public String serviceKakaoLogin(HttpServletRequest request, util.KakaoAuth.KakaoUser kakaoUser) {

		// 우리 사이트에서 쓸 아이디 만들기  예) 카카오 회원번호 12345  ->  "kakao_12345"
		final String memberId = "kakao_" + kakaoUser.id;

		// 카카오 닉네임이 없으면 "카카오회원", 있으면 닉네임을 이름으로 쓴다
		final String name  = (kakaoUser.nickname == null || kakaoUser.nickname.trim().isEmpty())
				? "카카오회원" : kakaoUser.nickname.trim();

		// 카카오 이메일 (동의하지 않았으면 null)
		final String email = kakaoUser.email;

		// 아래 con -> { } 안의 작업을 DB 연결 1개로 처리한다
		DBCPUtil.execute(con -> {

			//사원(MemberDAO)에게 시키기 : 만든 아이디를 MemberDAO 의 overlappedId(con, memberId) 호출해서 이미 가입된 카카오 회원인지 조회(select) 작업 명령
			// 이미 가입된 회원이면 가입하지 않고 0 반환
			if (memberDao.overlappedId(con, memberId)) {
				return 0;
			}

			// 처음 온 카카오 회원이면 가입할 정보를 MemberVO 상자에 담는다
			// 비밀번호는 카카오 회원이 쓰지 않으므로 아무도 알 수 없는 무작위 글자로 채운다
			MemberVO vo = new MemberVO(
					memberId,
					(util.KakaoAuth.newState() + util.KakaoAuth.newState()),
					name,
					0,
					"",
					"",
					email == null ? "" : email,
					"",
					"");

			//사원(MemberDAO)에게 시키기 : 카카오 회원 정보가 저장된 MemberVO 객체를 MemberDAO 의 insertMember(con, vo) 호출해서 새 회원 추가(insert) 작업 명령
			int inserted = memberDao.insertMember(con, vo);

			// 이클립스 콘솔에 자동 가입된 카카오 회원 출력
			System.out.println("[MemberService] 카카오 자동가입 : " + memberId + " (" + name + ")");

			// 추가(insert)에 성공한 행의 수 반환
			return inserted;
		});

		// 이전 세션을 없애고 새 세션을 만든다
		request.getSession().invalidate();
		HttpSession session = request.getSession(true);

		// 새 세션에 로그인 아이디와 이름 저장
		session.setAttribute("id", memberId);
		session.setAttribute("loginName", name);

		// 로그인한 아이디를 사장(MemberController)로 반환
		return memberId;
	}

	//----------------------------------------------------------------
	// serviceLogout : 로그아웃 처리
	//  사장의 "/logout.me" 요청에서 부른다
	//----------------------------------------------------------------
	public void serviceLogout(HttpServletRequest request) {

		// 세션을 없애서 로그인 정보를 모두 지우고, 빈 새 세션을 만든다
		request.getSession().invalidate(); request.getSession(true);
	}

	//----------------------------------------------------------------
	// serviceMemberDetail : 회원 1명의 정보를 조회한다
	//  사장의 "/memberUpdate.me" 요청에서 부른다
	//----------------------------------------------------------------
	public MemberVO serviceMemberDetail(String id) {

		//사원(MemberDAO)에게 시키기 : 로그인 아이디를 MemberDAO 의 memberDetail(con, id) 호출해서 회원 1명의 정보 조회(select) 작업 명령
		// 조회한 정보가 담긴 MemberVO 객체를 사장(MemberController)로 반환
		return DBCPUtil.query(con -> memberDao.memberDetail(con, id));
	}

	//----------------------------------------------------------------
	// serviceUpdateMember : 회원정보를 수정(update)한다
	//  사장의 "/memberUpdatePro.me" 요청에서 부른다
	//  반환값 -> "수정성공" / "수정실패" / "현재비밀번호불일치" / "비밀번호길이부족"
	//----------------------------------------------------------------
	public String serviceUpdateMember(HttpServletRequest request, String loginId) {

		// memberUpdate.jsp 에서 보낸 값들 꺼내기 (비밀번호 2개는 아래에서 따로 검사하므로 여기서는 trim 하지 않는다)
		final String newPass     = request.getParameter("pass");
		final String currentPass = request.getParameter("currentPass");
		final String name        = trim(request.getParameter("name"));
		final String gender      = trim(request.getParameter("gender"));
		final String address     = trim(request.getParameter("address"));
		final String email       = trim(request.getParameter("email"));

		// 나이는 글자("25")로 오므로 숫자(25)로 바꾼다. 숫자가 아니면 0 으로 저장한다
		int parsedAge = 0;
		try {
			parsedAge = Integer.parseInt(trim(request.getParameter("age")));
		} catch (Exception e) {
			parsedAge = 0;
		}

		// 람다식(con -> { }) 안에서 쓰려면 값이 바뀌지 않는 final 변수여야 하므로 옮겨 담는다
		final int age = parsedAge;

		// 새 비밀번호를 입력했으면 비밀번호도 바꾸고(true), 비워 두었으면 바꾸지 않는다(false)
		final boolean changePassword = (newPass != null && !newPass.trim().isEmpty());

		// 새 비밀번호가 4글자보다 짧으면 "비밀번호길이부족" 을 사장(MemberController)로 반환
		if (changePassword && newPass.trim().length() < 4) {
			return "비밀번호길이부족";
		}

		// 아래 con -> { } 안의 작업을 한 덩어리로 처리하고, 결과 글자를 사장(MemberController)로 반환
		return DBCPUtil.execute(con -> {

			// DB 에 저장할 새 비밀번호 (비밀번호를 바꾸지 않으면 null 그대로)
			String encodedNewPass = null;

			// 비밀번호를 바꾸는 경우에만 현재 비밀번호가 맞는지 확인한다
			if (changePassword) {

				//사원(MemberDAO)에게 시키기 : 로그인 아이디를 MemberDAO 의 findPasswordById(con, loginId) 호출해서 DB 에 저장된 현재 비밀번호 조회(select) 작업 명령
				String stored = memberDao.findPasswordById(con, loginId);

				// 조회된 비밀번호가 없으면 (회원이 없으면) "수정실패" 반환
				if (stored == null) {
					return "수정실패";
				}

				// 입력한 현재 비밀번호가 DB 의 비밀번호와 다르면 "현재비밀번호불일치" 반환
				if (currentPass == null || !java.util.Objects.equals(currentPass, stored)) {
					return "현재비밀번호불일치";
				}

				// 확인이 끝났으면 새 비밀번호의 앞뒤 공백을 지워서 저장
				encodedNewPass = (newPass.trim());
			}

			// 수정할 정보들을 MemberVO 상자에 담는다
			MemberVO vo = new MemberVO();
			vo.setId(loginId);
			vo.setPass(encodedNewPass);
			vo.setName(name);
			vo.setAge(age);
			vo.setGender(gender);
			vo.setAddress(address);
			vo.setEmail(email);

			//사원(MemberDAO)에게 시키기 : 수정할 정보가 저장된 MemberVO 객체를 MemberDAO 의 updateMember(con, vo) 호출해서 회원정보 수정(update) 작업 명령
			int result = memberDao.updateMember(con, vo);

			// 수정(update)에 성공한 행이 1건이면 "수정성공", 아니면 "수정실패" 반환
			return (result == 1) ? "수정성공" : "수정실패";
		});
	}

	//----------------------------------------------------------------
	// serviceDeleteMember : 회원을 탈퇴(delete) 처리한다
	//  사장의 "/memberDelete.me" 요청에서 부른다
	//----------------------------------------------------------------
	public String serviceDeleteMember(String id) {

		//사원(MemberDAO)에게 시키기 : 로그인 아이디를 MemberDAO 의 deleteMember(con, id) 호출해서 회원 삭제(delete) 작업 명령
		int result = DBCPUtil.execute(con -> memberDao.deleteMember(con, id));

		// 삭제(delete)에 성공한 행이 1건이면 "삭제성공", 아니면 "삭제실패" 를 사장(MemberController)로 반환
		return (result == 1) ? "삭제성공" : "삭제실패";
	}
}
