package util;

/*
 * ============================================================================
 *  HtmlUtil  -  화면에 출력할 글자를 안전한 글자로 바꿔 주는 도우미 클래스
 *
 *  왜 필요한가 (XSS 공격 막기)
 *    회원이 이름이나 글 제목에 <script>alert('해킹')</script> 를 저장해 두면,
 *    그 값을 그대로 화면에 출력했을 때 다른 사람의 브라우저에서 스크립트가 실행된다.
 *    출력하기 전에 < > 같은 특수문자를 &lt; &gt; 로 바꾸면 글자로만 보이고 실행되지 않는다.
 *
 *  사용하는 곳
 *    escape()   : JSP 화면에 값을 출력할 때       예) <%=HtmlUtil.escape(name)%>
 *    escapeJs() : 자바스크립트 문자열 안에 넣을 때  예) alert('<%=HtmlUtil.escapeJs(msg)%>')
 * ============================================================================
 */
public class HtmlUtil {

	// 유틸 클래스이므로 객체를 만들지 못하게 막는다 (모든 메소드가 static)
	private HtmlUtil() {}

	//----------------------------------------------------------------
	// escape : HTML 특수문자 5개를 안전한 글자(엔티티)로 바꿔서 반환한다
	//  예) <b>홍길동</b>  ->  &lt;b&gt;홍길동&lt;/b&gt;   (화면에는 <b>홍길동</b> 글자 그대로 보인다)
	//----------------------------------------------------------------
	public static String escape(String text) {

		// 값이 없으면(null) 화면에 "null" 이 찍히지 않도록 빈 글자를 반환
		if (text == null) {
			return "";
		}

		// 바꾼 글자를 차곡차곡 이어 붙일 그릇 (원래 길이 + 여유 16칸)
		StringBuilder sb = new StringBuilder(text.length() + 16);

		// 글자를 한 글자씩 꺼내 확인한다
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);

			// 특수문자이면 안전한 글자로 바꿔 붙이고, 아니면 그대로 붙인다
			switch (c) {
				case '&':  sb.append("&amp;");  break;
				case '<':  sb.append("&lt;");   break;
				case '>':  sb.append("&gt;");   break;
				case '"':  sb.append("&quot;"); break;
				case '\'': sb.append("&#39;");  break;
				default:   sb.append(c);
			}
		}

		// 다 바꾼 글자를 반환
		return sb.toString();
	}

	//----------------------------------------------------------------
	// escapeJs : 자바스크립트 문자열('...') 안에 넣어도 문자열이 끊기지 않도록 특수문자를 바꿔서 반환한다
	//  예) 홍's 글  ->  홍\'s 글      (따옴표 앞에 \ 를 붙여 문자열이 끝나지 않게 한다)
	//      줄바꿈   ->  \n            (문자열 중간에서 줄이 끊기지 않게 한다)
	//      < >      ->  \u003c \u003e (</script> 로 스크립트가 강제로 닫히지 않게 한다)
	//----------------------------------------------------------------
	public static String escapeJs(String text) {

		// 값이 없으면(null) 빈 글자를 반환
		if (text == null) {
			return "";
		}

		// 바꾼 글자를 차곡차곡 이어 붙일 그릇
		StringBuilder sb = new StringBuilder(text.length() + 16);

		// 글자를 한 글자씩 꺼내 확인한다
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);

			// 특수문자이면 자바스크립트용 안전한 글자로 바꿔 붙이고, 아니면 그대로 붙인다
			switch (c) {
				case '\\': sb.append("\\\\"); break;
				case '\'': sb.append("\\'");  break;
				case '"':  sb.append("\\\""); break;
				case '\r': sb.append("\\r");  break;
				case '\n': sb.append("\\n");  break;
				case '<':  sb.append("\\u003c"); break;
				case '>':  sb.append("\\u003e"); break;
				default:   sb.append(c);
			}
		}

		// 다 바꾼 글자를 반환
		return sb.toString();
	}
}
