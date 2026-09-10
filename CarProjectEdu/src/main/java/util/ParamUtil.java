package util;   // 이 파일이 속한 패키지(=폴더) 이름. 실제 폴더 구조와 반드시 같아야 한다

import javax.servlet.http.HttpServletRequest;   // 브라우저가 보낸 요청(주소·파라미터·세션)이 담긴 객체

import exception.InvalidInputException;   // 직접 만든 예외 클래스

/*
 ================================================================================
   ParamUtil  -  요청 파라미터를 "안전하게" 꺼내는 도우미 클래스

   [기존 코드의 문제]
     컨트롤러마다 아래 코드가 반복됐다.

         int carno = Integer.parseInt(request.getParameter("carno"));
         int carqty = Integer.parseInt(request.getParameter("carqty"));
         ...

     - 파라미터가 없으면          -> null 이 들어가 NumberFormatException
     - 사용자가 문자를 넣으면      -> NumberFormatException
     - 앞뒤 공백이 붙어 오면       -> 검색/로그인이 조용히 실패
     - 음수/과도한 값을 넣으면     -> 금액이 음수가 되는 등 업무 오류

   [바뀐 방식]
     파라미터 읽기를 이 클래스 한 곳으로 모아
     "형식 검사 + 범위 검사 + 공백 제거"를 표준화한다.
     검사에 실패하면 InvalidInputException을 던져 400으로 응답하게 한다.
 ================================================================================
*/

// [파라미터가 무엇인가]
//   브라우저가 서버로 보내는 "값" 이다. 두 가지 경로로 온다.
//     주소창  : /Car/CarInfo.do?carno=3     <- ? 뒤에 붙는 것 (GET)
//     입력폼  : <input name="carqty">        <- 폼을 제출할 때 (POST)
//   서버에서는 둘 다 request.getParameter("이름") 으로 똑같이 꺼낸다.
//   중요한 점 : 꺼낸 값은 항상 String 이고, 사용자가 마음대로 바꿔 보낼 수 있다.
public class ParamUtil {

	// 유틸 클래스이므로 객체 생성을 막는다 (전부 static 메소드)
	// HtmlUtil 과 같은 이유다. 이 클래스는 도구 모음이라 객체를 만들 이유가 없다.
	private ParamUtil() {}

	//===========================================================
	// 1. 문자열 파라미터
	//===========================================================

	/** 값을 trim해서 반환. 값이 없거나 빈 문자열이면 null 반환 */
	public static String getString(HttpServletRequest request, String name) {
		
		// 요청에서 name 이라는 이름의 값을 꺼낸다. 없으면 null 이 나온다.
		String value = request.getParameter(name);
		// 아예 안 넘어온 경우
		if (value == null) {
			// 그대로 null 을 돌려준다. 부르는 쪽에서 "없구나" 를 판단하게 한다.
			return null;
		}
		// trim() = 앞뒤 공백을 잘라낸다.  "  홍길동  " -> "홍길동"
		// 이걸 안 하면 사용자가 실수로 넣은 공백 때문에 로그인·검색이 조용히 실패한다.
		value = value.trim();
		// 공백만 있던 값은 trim 후 빈 문자열("")이 된다. 그것도 "값 없음" 으로 취급해 null 로 통일한다.
		//   (조건) ? 참일때 : 거짓일때   <- 삼항 연산자
		return value.isEmpty() ? null : value;
	}

	/** 값이 없으면 기본값을 반환 */
	// 같은 이름의 메소드가 여러 개 있는 것을 오버로딩(overloading)이라고 한다.
	// 괄호 안의 인자 개수·종류가 다르면 이름이 같아도 자바가 구분해 준다.
	public static String getString(HttpServletRequest request, String name, String defaultValue) {
		// 위에서 만든 2개짜리 메소드를 그대로 재사용한다 (공백 제거·빈값 처리까지 같이 얻는다)
		String value = getString(request, name);
		// 값이 없으면 넘겨받은 기본값을, 있으면 그 값을 돌려준다
		return (value == null) ? defaultValue : value;
	}

	/** 반드시 있어야 하는 값. 없으면 400 예외 */
	public static String getRequiredString(HttpServletRequest request, String name) {
		
		// 먼저 평소대로 꺼내 본다
		String value = getString(request, name);
		
		// 없으면 그냥 넘어가면 안 되는 값이다
		if (value == null) {
			// 예외를 던진다. BaseController 가 이걸 받아 400(요청 잘못) 으로 응답한다.
			// throw = "예외를 발생시켜 여기서 실행을 중단하고 위로 던진다" 는 뜻이다.
			throw new InvalidInputException("필수 입력값이 없습니다 : " + name);
		}
		// 값이 있으면 그대로 돌려준다
		return value;
	}

	/**
	 * 길이 제한이 있는 필수 문자열.
	 * DB 컬럼 길이를 넘는 값이 들어와 SQL 오류가 나는 것을 미리 막는다.
	 */
	public static String getRequiredString(HttpServletRequest request, String name, int maxLength) {
		
		// 우선 "반드시 있어야 하는 값" 규칙을 먼저 통과시킨다
		String value = getRequiredString(request, name);
		
		// 글자 수가 허용치를 넘었는지 검사. length() 는 글자 수를 돌려준다.
		if (value.length() > maxLength) {
			// 넘었으면 DB 에 넣기 전에 여기서 막는다. (넣고 나서 터지면 원인을 찾기 어렵다)
			throw new InvalidInputException(name + " 값이 너무 깁니다 (최대 " + maxLength + "자)");
		}
		// 길이까지 통과한 값을 돌려준다
		return value;
	}

	//===========================================================
	// 2. 정수 파라미터
	//===========================================================

	/** 값이 없거나 숫자가 아니면 기본값을 반환 (예외 없음 - 페이지번호처럼 실패해도 되는 값에 사용) */
	public static int getInt(HttpServletRequest request, String name, int defaultValue) {
		
		// 문자열로 먼저 꺼낸다 (파라미터는 언제나 문자열로 도착한다)
		String value = getString(request, name);
		// 아예 없으면 기본값으로 (예: 페이지 번호가 없으면 0페이지)
		if (value == null) {
			return defaultValue;
		}
		// try = "여기서 예외가 날 수 있으니 감시해 달라" 는 뜻
		try {
			// "12" 같은 문자열을 숫자 12 로 바꾼다. 못 바꾸면 예외가 난다.
			return Integer.parseInt(value);
		// catch = 예외가 났을 때 실행할 부분
		} catch (NumberFormatException e) {
			// 숫자가 아니어도 화면이 깨지면 안 되는 값이므로 조용히 기본값을 쓴다
			return defaultValue;
		}
	}

	/** 반드시 있어야 하는 정수. 없거나 숫자가 아니면 400 예외 */
	public static int getRequiredInt(HttpServletRequest request, String name) {
		
	
		// 없으면 여기서 이미 예외가 난다
		String value = getRequiredString(request, name);
		
		// 숫자로 바꾸기를 시도한다
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			// 위 getInt 와 달리 여기서는 조용히 넘어가면 안 된다.
			// 글번호·차량번호처럼 없으면 아무 일도 못 하는 값이기 때문이다.
			throw new InvalidInputException(name + " 값은 숫자여야 합니다 : " + value);
		}
	}

	/**
	 * 범위까지 검사하는 정수.
	 * 예) 대여수량은 1~5, 대여기간은 1~30 처럼 업무 규칙을 서버에서 강제한다.
	 * (화면 select 태그만 믿으면 안 된다. 주소창으로 얼마든지 바꿔 보낼 수 있다.)
	 */
	public static int getRequiredInt(HttpServletRequest request, String name, int min, int max) {
		// 먼저 "숫자인가" 를 통과시킨다
		int value = getRequiredInt(request, name);
		// 그 다음 "허용 범위 안인가" 를 본다.  || 는 "또는" 이라는 뜻이다.
		if (value < min || value > max) {
			// 화면에서 1~5 만 고르게 만들어 놨어도, 주소창으로 999 를 보낼 수 있다.
			// 그래서 서버에서 한 번 더 막는다. 이것이 "서버 검증" 이다.
			throw new InvalidInputException(name + " 값은 " + min + " ~ " + max + " 사이여야 합니다 : " + value);
		}
		// 범위까지 통과한 값
		return value;
	}

	/**
	 * 0 또는 1만 허용하는 옵션 선택값 (보험/WiFi/네비게이션/베이비시트).
	 * 기존 코드는 검사 없이 parseInt만 해서 carins=999 같은 값도 그대로 금액 계산에 들어갔다.
	 */
	public static int getFlag(HttpServletRequest request, String name) {
		// 체크박스는 안 고르면 아예 안 넘어오므로, 없으면 0(선택 안 함)으로 본다
		int value = getInt(request, name, 0);
		// 1 이면 1, 그 외에는 전부 0 으로 강제한다.
		// 이렇게 하면 999 가 들어와도 금액 계산에 999배가 곱해지는 일이 없다.
		return (value == 1) ? 1 : 0;
	}

	//===========================================================
	// 3. 날짜 파라미터
	//===========================================================

	/**
	 * yyyy-MM-dd 형식인지 검사한 뒤 문자열 그대로 반환.
	 * 기존에는 carbegindate를 검사 없이 VARCHAR 컬럼에 넣어
	 * "2026-13-99" 같은 값도 저장됐고, 조회 시 str_to_date가 NULL이 되어
	 * 예약이 조용히 사라졌다.
	 */
	public static String getRequiredDate(HttpServletRequest request, String name) {
		// 우선 값이 있는지부터 확인
		String value = getRequiredString(request, name);
		// matches() = 정해진 형태(정규표현식)와 맞는지 검사한다.
		//   \\d 는 "숫자 한 글자", {4} 는 "4번 반복" 이라는 뜻이다.
		//   즉 숫자4개-숫자2개-숫자2개 형태인지 본다.   ! 는 "아니면" 이라는 뜻.
		if (!value.matches("\\d{4}-\\d{2}-\\d{2}")) {
			// 형태부터 틀렸으면 여기서 막는다
			throw new InvalidInputException(name + " 날짜 형식이 올바르지 않습니다 (yyyy-MM-dd) : " + value);
		}
		// 형태가 맞아도 실제로 있는 날짜인지는 아직 모른다
		try {
			// 실제로 존재하는 날짜인지 확인 (2026-02-30 같은 값 차단)
			// LocalDate.parse 는 없는 날짜면 예외를 던진다. 그 성질을 검사에 이용한다.
			java.time.LocalDate.parse(value);
		} catch (Exception e) {
			// 2월 30일처럼 형태는 맞지만 존재하지 않는 날짜를 여기서 걸러 낸다
			throw new InvalidInputException(name + " 에 존재하지 않는 날짜입니다 : " + value);
		}
		// 두 검사를 모두 통과한 날짜 문자열을 그대로 돌려준다
		return value;
	}
}
