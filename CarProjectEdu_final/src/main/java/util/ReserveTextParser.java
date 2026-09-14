package util;

/*
 * ============================================================================
 *  ReserveTextParser  -  챗봇에 입력한 한국어 문장에서 예약 조건을 뽑아내는 클래스
 *
 *  예) "다음주 금요일부터 2박3일 4명 SUV 10만원 이하"
 *        begindate = 다음주 금요일 날짜
 *        days      = 3   (2박3일 -> 3일)
 *        people    = 4
 *        category  = "Mid"  (SUV -> 중형)
 *        maxprice  = 100000
 *
 *  사용하는 곳 : 사장(ChatbotController)
 *    먼저 이 클래스로 규칙에 따라 뽑아 보고, 부족하면 AI 에게 한 번 더 물어본다
 *
 *  정규식(Pattern) 기호 읽는 법
 *    \\d      숫자 한 글자          \\s     공백 한 글자
 *    {1,2}    앞 글자가 1~2번       *      앞 글자가 0번 이상
 *    ( )      꺼내 쓸 부분(그룹)    m.group(1) = 첫 번째 ( ) 에 걸린 글자
 *    (?<!x)   앞에 x 가 없을 때만   (?!x)  뒤에 x 가 없을 때만
 * ============================================================================
 */

// 날짜 객체, 정규식 검사 도구
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReserveTextParser {

	// 유틸 클래스이므로 객체를 만들지 못하게 막는다 (모든 메소드가 static)
	private ReserveTextParser() {}

	//----------------------------------------------------------------
	// Result : 뽑아낸 예약 조건 5개를 담아 나르는 상자 (못 찾은 값은 null 또는 -1)
	//----------------------------------------------------------------
	public static class Result {

		// 대여 시작일, 대여 일수, 인원, 차종(Big/Mid/Small), 최대 금액
		public LocalDate begindate;
		public int days     = -1;
		public int people   = -1;
		public String category;
		public int maxprice = -1;

		// 조건을 하나라도 찾았으면 true
		public boolean hasAny() {
			return begindate != null || days > 0 || people > 0 || category != null || maxprice > 0;
		}

		// 인원을 찾았고, 시작일이나 일수 중 하나라도 찾았으면 true (AI 에게 다시 묻지 않아도 되는 상태)
		public boolean isConfident() {
			return people > 0 && (begindate != null || days > 0);
		}

		// 이클립스 콘솔에 출력할 때 보여줄 글자
		@Override
		public String toString() {
			return "begindate=" + begindate + ", days=" + days + ", people=" + people
				 + ", category=" + category + ", maxprice=" + maxprice;
		}
	}

	//----------------------------------------------------------------
	// parse : 문장(text)에서 예약 조건 5개를 뽑아 Result 상자에 담아 반환한다
	//  today : 오늘 날짜 ("내일", "다음주" 같은 말을 실제 날짜로 바꿀 때 기준)
	//----------------------------------------------------------------
	public static Result parse(String text, LocalDate today) {

		// 결과를 담을 빈 상자
		Result r = new Result();

		// 문장이 없으면 빈 상자 그대로 반환
		if (text == null || text.trim().isEmpty()) {
			return r;
		}

		// 여러 칸 공백을 한 칸으로 줄이고 앞뒤 공백을 지운다
		String s = text.replaceAll("\\s+", " ").trim();

		// 조건별로 뽑아서 상자에 담는다
		r.begindate = parseBeginDate(s, today);
		r.days      = parseDays(s);
		r.people    = parsePeople(s);
		r.category  = parseCategory(s);
		r.maxprice  = parseMaxPrice(s);

		// 조건이 담긴 상자 반환
		return r;
	}

	//----------------------------------------------------------------
	// parseBeginDate : 대여 시작일을 찾는다. 위에서부터 차례로 확인하고 먼저 찾은 것을 반환한다
	//----------------------------------------------------------------
	private static LocalDate parseBeginDate(String s, LocalDate today) {

		// 1. "2026-09-11", "2026.9.11", "2026/9/11" 형식
		Matcher m = Pattern.compile("(20\\d{2})[-./](\\d{1,2})[-./](\\d{1,2})").matcher(s);
		if (m.find()) {
			LocalDate d = safeDate(num(m.group(1)), num(m.group(2)), num(m.group(3)));
			if (d != null) return d;
		}

		// 2. "9월 11일" 형식 (연도는 올해, 이미 지난 날짜면 내년)
		m = Pattern.compile("(\\d{1,2})\\s*월\\s*(\\d{1,2})\\s*일").matcher(s);
		if (m.find()) {
			LocalDate d = safeDate(today.getYear(), num(m.group(1)), num(m.group(2)));
			if (d != null) {
				return d.isBefore(today) ? d.plusYears(1) : d;
			}
		}

		// 3. "9/11" 형식 (연도는 올해, 이미 지난 날짜면 내년)
		m = Pattern.compile("(?<!\\d)(\\d{1,2})/(\\d{1,2})(?!\\d)").matcher(s);
		if (m.find()) {
			LocalDate d = safeDate(today.getYear(), num(m.group(1)), num(m.group(2)));
			if (d != null) {
				return d.isBefore(today) ? d.plusYears(1) : d;
			}
		}

		// 4. "3일 후", "3일 뒤" 형식 -> 오늘 + 3일
		m = Pattern.compile("(\\d{1,2})\\s*일\\s*(후|뒤)").matcher(s);
		if (m.find()) {
			return today.plusDays(num(m.group(1)));
		}

		// 5. "금요일", "다음주 금요일" 같은 요일 표현 (아래 parseWeekday 메소드)
		LocalDate byWeekday = parseWeekday(s, today);
		if (byWeekday != null) {
			return byWeekday;
		}

		// 6. "어제, 오늘, 내일, 모레, 글피" 같은 날짜 낱말
		if (s.contains("그제") || s.contains("그저께")) return today.minusDays(2);
		if (s.contains("어제") || s.contains("어저께")) return today.minusDays(1);
		if (s.contains("글피"))                       return today.plusDays(3);
		if (s.contains("모레") || s.contains("이틀 후")) return today.plusDays(2);
		if (s.contains("내일") || s.contains("내일부터")) return today.plusDays(1);
		if (s.contains("오늘") || s.contains("당장") || s.contains("지금")) return today;

		// 7. 요일 없이 "다다음주" -> 2주 뒤 월요일
		if (s.contains("다다음 주") || s.contains("다다음주")) {
			return mondayOfWeek(today).plusDays(14);
		}

		// 8. 요일 없이 "다음주" -> 다음 주 월요일
		if (s.contains("다음 주") || s.contains("다음주")) {
			return mondayOfWeek(today).plusDays(7);
		}

		// 날짜 표현을 찾지 못했으면 null 반환
		return null;
	}

	//----------------------------------------------------------------
	// parseWeekday : "금요일", "다음주 금요일" 같은 요일 표현을 실제 날짜로 바꿔 반환한다. 없으면 null
	//----------------------------------------------------------------
	private static LocalDate parseWeekday(String s, LocalDate today) {

		// 요일 이름 (배열 순서 0~6 -> 요일 번호 1~7 : 월=1 ... 일=7)
		String[] names = { "월", "화", "수", "목", "금", "토", "일" };

		// 찾은 요일 번호 (못 찾으면 -1)
		int targetDow = -1;

		// "금요일" 또는 "금욜" 이 들어 있는지 월요일부터 차례로 확인한다
		for (int i = 0; i < names.length; i++) {
			if (s.contains(names[i] + "요일") || s.contains(names[i] + "욜")) {
				targetDow = i + 1;
				break;
			}
		}

		// 요일 표현이 없으면 null 반환
		if (targetDow < 0) {
			return null;
		}

		// 어느 주의 요일인지 확인 ("다다음주" 에도 "다음주" 가 들어 있으므로 다다음주를 먼저 확인한다)
		boolean nextWeek  = s.contains("다음 주") || s.contains("다음주");
		boolean afterNext = s.contains("다다음 주") || s.contains("다다음주");
		boolean thisWeek  = s.contains("이번 주") || s.contains("이번주");

		// 이번 주 월요일 날짜
		LocalDate monday = mondayOfWeek(today);

		// 다다음주 요일 = 이번 주 월요일 + 14일 + (요일번호 - 1)
		if (afterNext) {
			return monday.plusDays(14 + (targetDow - 1));
		}

		// 다음주 요일 = 이번 주 월요일 + 7일 + (요일번호 - 1)
		if (nextWeek) {
			return monday.plusDays(7 + (targetDow - 1));
		}

		// 이번주 요일 = 이번 주 월요일 + (요일번호 - 1)
		if (thisWeek) {
			return monday.plusDays(targetDow - 1);
		}

		// 주 표현 없이 요일만 있으면 오늘부터 하루씩 넘기며 가장 가까운 그 요일을 찾는다
		LocalDate d = today;
		while (d.getDayOfWeek().getValue() != targetDow) {
			d = d.plusDays(1);
		}
		return d;
	}

	//----------------------------------------------------------------
	// mondayOfWeek : 날짜 d 가 속한 주의 월요일 날짜를 반환한다  예) 목요일(4) -> 3일 전 월요일
	//----------------------------------------------------------------
	private static LocalDate mondayOfWeek(LocalDate d) {
		return d.minusDays(d.getDayOfWeek().getValue() - 1);
	}

	//----------------------------------------------------------------
	// parseDays : 대여 일수를 찾는다. 못 찾으면 -1
	//----------------------------------------------------------------
	private static int parseDays(String s) {

		// 1. "2박3일" -> 뒤의 숫자 3
		Matcher m = Pattern.compile("(\\d{1,3})\\s*박\\s*(\\d{1,3})\\s*일").matcher(s);
		if (m.find()) {
			return num(m.group(2));
		}

		// 2. "2박" 만 있으면 -> 2 + 1 = 3일
		m = Pattern.compile("(\\d{1,3})\\s*박(?!\\s*\\d)").matcher(s);
		if (m.find()) {
			return num(m.group(1)) + 1;
		}

		// 3. "당일" -> 1일
		if (s.contains("당일")) {
			return 1;
		}

		// 4. "3일간", "3일 동안", "3일짜리", "3일 정도" -> 3 (앞에 "월"이 붙은 "9월 3일"은 제외)
		m = Pattern.compile("(?<![월0-9])(\\d{1,3})\\s*일\\s*(간|동안|짜리|정도)").matcher(s);
		if (m.find()) {
			return num(m.group(1));
		}

		// 5. "2주" -> 14일 ("다음 주", "이번 주" 는 기간이 아니므로 제외)
		m = Pattern.compile("(\\d{1,3})\\s*주").matcher(s);
		if (m.find() && !s.contains("다음 주") && !s.contains("이번 주")) {
			return num(m.group(1)) * 7;
		}

		// 6. "일주일", "한 주" -> 7일
		if (s.contains("일주일") || s.contains("한 주") || s.contains("한주")) {
			return 7;
		}

		// 7. "한 달", "1개월" -> 30일
		if (s.contains("한 달") || s.contains("한달") || s.contains("1개월")) {
			return 30;
		}

		// 8. "2개월" -> 60일이지만 최대 대여 기간이 30일이므로 30일
		m = Pattern.compile("(\\d{1,2})\\s*개월").matcher(s);
		if (m.find()) {
			return Math.min(num(m.group(1)) * 30, 30);
		}

		// 9. 우리말 날수 : 이레(7) 엿새(6) 닷새(5) 나흘(4) 사흘(3) 이틀(2)
		if (s.contains("이레"))  return 7;
		if (s.contains("엿새"))  return 6;
		if (s.contains("닷새"))  return 5;
		if (s.contains("나흘"))  return 4;
		if (s.contains("사흘"))  return 3;
		if (s.contains("이틀"))  return 2;

		// 10. "하루" -> 1일 (뒤에 숫자가 오는 "하루 3번" 같은 말은 제외)
		if (Pattern.compile("하루(?!\\s*\\d)").matcher(s).find()) {
			return 1;
		}

		// 11. 마지막으로 "3일" 을 찾는다. 단, "9월 3일"(날짜)과 "3일 후"(시작일)는 건너뛴다
		m = Pattern.compile("(\\d{1,3})\\s*일").matcher(s);
		while (m.find()) {

			// 숫자 앞 3글자 안에 "월"이 있으면 날짜이므로 건너뛴다
			int before = m.start();
			String head = s.substring(Math.max(0, before - 3), before);
			if (head.contains("월")) continue;

			// "일" 바로 뒤가 "후" 또는 "뒤"이면 시작일 표현이므로 건너뛴다
			String tail = s.substring(m.end(), Math.min(s.length(), m.end() + 2));
			if (tail.startsWith("후") || tail.startsWith("뒤")) continue;

			// 조건을 통과한 숫자를 일수로 반환
			return num(m.group(1));
		}

		// 일수를 찾지 못했으면 -1 반환
		return -1;
	}

	//----------------------------------------------------------------
	// parsePeople : 인원을 찾는다. 못 찾으면 -1
	//----------------------------------------------------------------
	private static int parsePeople(String s) {

		// 1. "어른 2명 아이 1명" 처럼 여러 번 나오면 모두 더한다 ("12인승" 의 "인"은 제외)
		int total = 0;
		Matcher m = Pattern.compile("(\\d{1,3})\\s*(명|인(?!승))").matcher(s);
		while (m.find()) {
			total += num(m.group(1));
		}
		if (total > 0) {
			return total;
		}

		// 2. "세 명", "다섯명" 처럼 우리말 숫자 -> 배열 위치 + 1
		String[] words  = { "한", "두", "세", "네", "다섯", "여섯", "일곱", "여덟", "아홉", "열" };
		for (int i = 0; i < words.length; i++) {
			if (s.contains(words[i] + " 명") || s.contains(words[i] + "명")) {
				return i + 1;
			}
		}

		// 3. "혼자" -> 1명, "둘이", "커플" -> 2명
		if (s.contains("혼자")) return 1;
		if (s.contains("둘이") || s.contains("커플")) return 2;

		// 인원을 찾지 못했으면 -1 반환
		return -1;
	}

	//----------------------------------------------------------------
	// parseCategory : 차종을 찾는다. Big(대형) / Mid(중형) / Small(소형), 못 찾으면 null
	//----------------------------------------------------------------
	private static String parseCategory(String s) {

		// 대형 : 승합차, 버스, 9·12인승, 카니발, 스타렉스
		if (s.contains("대형") || s.contains("승합") || s.contains("버스")
				|| s.contains("12인승") || s.contains("9인승")
				|| s.contains("카니발") || s.contains("스타렉스")) {
			return "Big";
		}

		// 중형 : SUV, 세단, 패밀리카
		if (s.contains("중형") || s.contains("SUV") || s.contains("suv")
				|| s.contains("세단") || s.contains("패밀리")) {
			return "Mid";
		}

		// 소형 : 경차, 준중형, 작은 차, 저렴한 차
		if (s.contains("소형") || s.contains("경차") || s.contains("준중형")
				|| s.contains("작은 차") || s.contains("저렴한 차")) {
			return "Small";
		}

		// 차종을 찾지 못했으면 null 반환
		return null;
	}

	//----------------------------------------------------------------
	// parseMaxPrice : 최대 금액을 찾는다. "이하, 이내, 까지" 같은 한도 낱말이 있을 때만 찾는다. 못 찾으면 -1
	//----------------------------------------------------------------
	private static int parseMaxPrice(String s) {

		// 한도를 뜻하는 낱말이 있는지 확인
		boolean hasLimitWord = s.contains("이하") || s.contains("이내")
							|| s.contains("미만") || s.contains("아래")
							|| s.contains("까지") || s.contains("안쪽");

		// 한도 낱말이 없으면 금액 조건이 아니므로 -1 반환
		if (!hasLimitWord) {
			return -1;
		}

		// 1. "10만원", "10만" -> 10 x 10000 = 100000
		Matcher m = Pattern.compile("(\\d{1,3})\\s*만\\s*원?").matcher(s);
		if (m.find()) {
			return num(m.group(1)) * 10000;
		}

		// 2. "50000원" (숫자 4~7자리 + 원) -> 50000
		m = Pattern.compile("(\\d{4,7})\\s*원").matcher(s);
		if (m.find()) {
			return num(m.group(1));
		}

		// 금액을 찾지 못했으면 -1 반환
		return -1;
	}

	//----------------------------------------------------------------
	// num : 글자를 숫자로 바꿔 반환한다. 바꿀 수 없으면 0
	//----------------------------------------------------------------
	private static int num(String text) {
		try {
			return Integer.parseInt(text.trim());
		} catch (Exception e) {
			return 0;
		}
	}

	//----------------------------------------------------------------
	// safeDate : 연, 월, 일로 날짜를 만들어 반환한다. 없는 날짜(2월 30일)면 null
	//----------------------------------------------------------------
	private static LocalDate safeDate(int year, int month, int day) {
		try {
			return LocalDate.of(year, month, day);
		} catch (Exception e) {
			return null;
		}
	}
}
