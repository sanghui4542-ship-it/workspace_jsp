package util;

/*
 * ============================================================================
 *  CenterView  -  CarMain.jsp 가운데에 보여줄 화면 주소를 검사하는 클래스
 *
 *  왜 필요한가
 *    가운데 화면 주소(center)는 요청 주소에 붙어서 올 수 있다.
 *        /member/join.me?center=members/join.jsp
 *    CarMain.jsp 는 이 값을 <jsp:include page="${center}"/> 로 그대로 포함하므로
 *    검사하지 않으면 아래처럼 서버 안의 설정 파일이 화면에 그대로 보인다.
 *        /member/join.me?center=WEB-INF/web.xml
 *        /Car/bb?center=META-INF/context.xml      <- DB 아이디, 비밀번호 노출
 *
 *  해결 방법
 *    가운데에 보여줄 수 있는 화면을 아래 ALLOWED 목록에 미리 적어 두고,
 *    목록에 있는 화면만 통과시킨다. 목록에 없으면 기본 화면(Center.jsp)을 보여준다.
 *
 *  새 화면을 추가했는데 가운데에 안 보이고 메인 화면(Center.jsp)이 보이면
 *    -> 아래 ALLOWED 목록에 그 화면 주소를 추가한다
 * ============================================================================
 */

// 중복 없는 값들의 묶음 (목록에 있는지 빠르게 확인할 수 있다)
import java.util.Set;

public class CenterView {

	// 가운데 화면 주소가 없거나, 허용 목록에 없을 때 보여줄 기본 화면
	public static final String DEFAULT_CENTER = "Center.jsp";

	// 가운데에 보여줄 수 있는 화면 주소 목록 (허용 목록)
	// Set.of(...) : 한 번 만들면 바꿀 수 없는 목록을 만든다
	private static final Set<String> ALLOWED = Set.of(

		// 메인
		"Center.jsp",

		// 회원 (MemberController)
		"members/join.jsp",
		"members/login.jsp",
		"members/memberUpdate.jsp",

		// 게시판 (BoardController)
		"board/list.jsp",
		"board/read.jsp",
		"board/write.jsp",
		"board/reply.jsp",

		// 자료실 게시판 (FileBoardController)
		"board/fileboardlist.jsp",
		"board/fileboardread.jsp",
		"board/fileboardwrite.jsp",
		"board/fileboardreply.jsp",

		// 렌터카 예약 (CarController)
		"CarReservation.jsp",
		"CarList.jsp",
		"CarInfo.jsp",
		"CarOption.jsp",
		"CarOrder.jsp",
		"LoginCarOrder.jsp",
		"CarReserveConfirm.jsp",
		"CarReserveResult.jsp",
		"CarConfirmUpdate.jsp",
		"Delete.jsp",
		"SearchResult.jsp",

		// AI 추천 서비스
		"AIService.jsp"
	);

	// 유틸 클래스이므로 객체를 만들지 못하게 막는다 (모든 메소드가 static)
	private CenterView() {}

	//----------------------------------------------------------------
	// resolve : 가운데 화면 주소를 검사해서 보여줘도 되는 화면 주소를 반환한다
	//  부르는 곳 : CarMain.jsp
	//  반환값   : 허용 목록에 있으면 그 주소 / 없거나 목록 밖이면 "Center.jsp"
	//
	//  예) resolve("members/join.jsp")  ->  "members/join.jsp"
	//      resolve(null)                ->  "Center.jsp"
	//      resolve("WEB-INF/web.xml")   ->  "Center.jsp"  (콘솔에 차단 기록)
	//----------------------------------------------------------------
	public static String resolve(String center) {

		// 가운데 화면 주소가 없으면 기본 화면 주소를 반환 (처음 메인 화면에 들어온 경우)
		if (center == null || center.trim().isEmpty()) {
			return DEFAULT_CENTER;
		}

		// 허용 목록에 있는 화면이면 그 주소를 그대로 반환
		if (ALLOWED.contains(center)) {
			return center;
		}

		// 허용 목록에 없는 주소로 요청했으면 이클립스 콘솔에 기록하고 기본 화면 주소를 반환
		System.out.println("[CenterView] 허용되지 않은 화면 요청 차단 : " + center);
		return DEFAULT_CENTER;
	}
}
