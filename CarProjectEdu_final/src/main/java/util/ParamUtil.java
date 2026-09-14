package util;

/*
 * ============================================================================
 *  ParamUtil  -  요청 파라미터(고객이 보낸 값)를 안전하게 꺼내는 도우미 클래스
 *
 *  파라미터란?
 *    고객(웹브라우저)이 서버로 보내는 값이다. 주소창(?carno=3)이나 form 입력칸으로 온다.
 *    서버는 request.getParameter("이름") 으로 꺼내며, 값은 항상 글자(String)다.
 *    고객이 주소창에서 마음대로 바꿔 보낼 수 있으므로 반드시 검사해야 한다.
 *
 *  이 클래스가 하는 검사
 *    공백 제거  : "  hong  " -> "hong"
 *    필수 검사  : 값이 없으면 400 예외
 *    숫자 검사  : "abc" 처럼 숫자가 아니면 400 예외
 *    범위 검사  : 대여수량 1~5 처럼 정해진 범위 밖이면 400 예외
 *    날짜 검사  : yyyy-MM-dd 형식이 아니거나 없는 날짜(2월 30일)면 400 예외
 *    400 예외(InvalidInputException) 는 사장(BaseController)이 받아 에러 화면을 보여준다
 *
 *  사용 예) 부장(Service) 또는 사장(Controller)에서
 *    int carno  = ParamUtil.getRequiredInt(request, "carno");
 *    int carqty = ParamUtil.getRequiredInt(request, "carqty", 1, 5);
 * ============================================================================
 */

// 요청 객체
import javax.servlet.http.HttpServletRequest;

// 입력값이 잘못됐을 때 던지는 예외
import exception.InvalidInputException;

public class ParamUtil {

	// 유틸 클래스이므로 객체를 만들지 못하게 막는다 (모든 메소드가 static)
	private ParamUtil() {}

	//----------------------------------------------------------------
	// getString : 파라미터를 꺼내 앞뒤 공백을 지워 반환한다. 값이 없거나 공백뿐이면 null
	//  아래 모든 메소드가 이 메소드로 값을 꺼낸다
	//  예) "  hong  " -> "hong" / "   " -> null / 파라미터 없음 -> null
	//----------------------------------------------------------------
	public static String getString(HttpServletRequest request, String name) {

		// 요청에서 name 이름의 파라미터 값을 꺼낸다 (없으면 null)
		String value = request.getParameter(name);

		// 파라미터가 아예 없으면 null 반환
		if (value == null) {
			return null;
		}

		// 앞뒤 공백을 지운다
		value = value.trim();

		// 공백을 지웠더니 빈 글자("")면 null, 아니면 그 값을 반환
		return value.isEmpty() ? null : value;
	}

	//----------------------------------------------------------------
	// getRequiredString : 반드시 있어야 하는 글자 파라미터를 꺼낸다. 없으면 400 예외
	//  예) ParamUtil.getRequiredString(request, "memberpass")
	//----------------------------------------------------------------
	public static String getRequiredString(HttpServletRequest request, String name) {

		// 공백을 지운 값을 꺼낸다
		String value = getString(request, name);

		// 값이 없으면 400 예외를 던진다 (여기서 실행이 멈추고 사장이 에러 화면을 보여준다)
		if (value == null) {
			throw new InvalidInputException("필수 입력값이 없습니다 : " + name);
		}

		// 값이 있으면 반환
		return value;
	}

	//----------------------------------------------------------------
	// getInt : 숫자 파라미터를 꺼낸다. 없거나 숫자가 아니면 기본값을 반환한다 (예외 없음)
	//  페이지 번호처럼 잘못 와도 화면은 정상으로 보여야 하는 값에 쓴다
	//  예) ParamUtil.getInt(request, "nowPage", 0)  :  ?nowPage=abc  ->  0
	//----------------------------------------------------------------
	public static int getInt(HttpServletRequest request, String name, int defaultValue) {

		// 공백을 지운 값을 꺼낸다
		String value = getString(request, name);

		// 값이 없으면 기본값 반환
		if (value == null) {
			return defaultValue;
		}

		// 글자("12")를 숫자(12)로 바꿔서 반환. 바꿀 수 없으면(abc) 기본값 반환
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	//----------------------------------------------------------------
	// getRequiredInt : 반드시 있어야 하는 숫자 파라미터를 꺼낸다. 없거나 숫자가 아니면 400 예외
	//  글번호, 차량번호처럼 없으면 아무 일도 할 수 없는 값에 쓴다
	//  예) ParamUtil.getRequiredInt(request, "carno")
	//----------------------------------------------------------------
	public static int getRequiredInt(HttpServletRequest request, String name) {

		// 필수 글자로 꺼낸다 (없으면 여기서 400 예외)
		String value = getRequiredString(request, name);

		// 글자를 숫자로 바꿔서 반환. 바꿀 수 없으면 400 예외
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new InvalidInputException(name + " 값은 숫자여야 합니다 : " + value);
		}
	}

	//----------------------------------------------------------------
	// getRequiredInt : 반드시 있어야 하는 숫자 + 범위(min ~ max) 검사. 범위 밖이면 400 예외
	//  화면의 select 에 1~5 만 있어도 주소창으로 999 를 보낼 수 있으므로 서버에서 다시 막는다
	//  예) ParamUtil.getRequiredInt(request, "carqty", 1, 5)
	//----------------------------------------------------------------
	public static int getRequiredInt(HttpServletRequest request, String name, int min, int max) {

		// 필수 숫자로 꺼낸다 (없거나 숫자가 아니면 여기서 400 예외)
		int value = getRequiredInt(request, name);

		// min 보다 작거나 max 보다 크면 400 예외
		if (value < min || value > max) {
			throw new InvalidInputException(name + " 값은 " + min + " ~ " + max + " 사이여야 합니다 : " + value);
		}

		// 범위 안이면 반환
		return value;
	}

	//----------------------------------------------------------------
	// getFlag : 옵션 선택값을 0(선택 안 함) 또는 1(선택함)로만 반환한다
	//  보험, WiFi, 네비게이션, 베이비시트 옵션에 쓴다
	//  999 같은 값이 와도 0 이 되므로 옵션 금액이 999배로 계산되지 않는다
	//----------------------------------------------------------------
	public static int getFlag(HttpServletRequest request, String name) {

		// 숫자로 꺼낸다. 없거나 숫자가 아니면 0 (체크박스는 선택 안 하면 아예 안 넘어온다)
		int value = getInt(request, name, 0);

		// 1 이면 1, 그 외에는 모두 0 반환
		return (value == 1) ? 1 : 0;
	}

	//----------------------------------------------------------------
	// getRequiredDate : 반드시 있어야 하는 날짜(yyyy-MM-dd) 파라미터를 꺼낸다
	//  형식이 틀리거나 없는 날짜면 400 예외
	//  예) "2026-09-11" -> 통과 / "2026/09/11" -> 형식 오류 / "2026-02-30" -> 없는 날짜
	//----------------------------------------------------------------
	public static String getRequiredDate(HttpServletRequest request, String name) {

		// 필수 글자로 꺼낸다 (없으면 여기서 400 예외)
		String value = getRequiredString(request, name);

		// 1차 검사 : 숫자4개-숫자2개-숫자2개 모양인지 확인 (\\d = 숫자 한 글자, {4} = 4번)
		if (!value.matches("\\d{4}-\\d{2}-\\d{2}")) {
			throw new InvalidInputException(name + " 날짜 형식이 올바르지 않습니다 (yyyy-MM-dd) : " + value);
		}

		// 2차 검사 : 실제로 있는 날짜인지 확인 (LocalDate.parse 는 없는 날짜면 예외를 던진다)
		try {
			java.time.LocalDate.parse(value);
		} catch (Exception e) {
			throw new InvalidInputException(name + " 에 존재하지 않는 날짜입니다 : " + value);
		}

		// 두 검사를 통과한 날짜 글자를 반환
		return value;
	}
}
