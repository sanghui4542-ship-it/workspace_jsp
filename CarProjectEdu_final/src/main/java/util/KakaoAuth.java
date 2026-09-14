package util;

/*
 * ============================================================================
 *  KakaoAuth  -  카카오 로그인을 도와주는 클래스
 *
 *  카카오 로그인 흐름 (사장 MemberController 와 함께 동작)
 *    1. login.jsp 의 카카오 버튼 -> /member/kakaoLogin.me
 *    2. 사장이 buildAuthorizeUrl() 로 만든 카카오 로그인 주소로 고객을 보낸다
 *    3. 고객이 카카오 화면에서 로그인하면, 카카오가 /member/kakaoCallback.me?code=... 로 돌려보낸다
 *    4. 사장이 requestAccessToken(code) 로 접근 토큰을 받는다
 *    5. 사장이 requestUserInfo(토큰) 으로 회원 번호, 닉네임, 이메일을 받는다
 *    6. 부장(MemberService)이 처음 온 회원이면 자동 가입시키고 로그인 처리한다
 *
 *  우리 서버는 카카오 비밀번호를 받지 않는다. 카카오가 확인해 준 결과만 받는다.
 *
 *  설정값 (카카오 개발자 사이트 https://developers.kakao.com 에서 발급)
 *    REST_API_KEY  : 내 애플리케이션의 REST API 키
 *    REDIRECT_URI  : 카카오에 등록한 Redirect URI 와 한 글자도 다르면 안 된다 (포트, 프로젝트명 주의)
 *    CLIENT_SECRET : 카카오 보안 설정에서 Client Secret 을 켰을 때만 채운다 (안 쓰면 "")
 * ============================================================================
 */

// 서버 응답을 읽고 요청 본문을 쓰는 입출력 도구
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

// 인터넷 주소로 요청을 보내는 도구, 주소에 넣을 글자를 변환하는 도구
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

// UTF-8 글자 방식, 예측할 수 없는 무작위 숫자 생성기, 글자 변환 도구
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

// JSON 글자를 자바 객체로 바꿔 주는 도구 (WEB-INF/lib/json-simple-1.1.1.jar)
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class KakaoAuth {

	// 카카오 로그인 화면 주소
	private static final String AUTHORIZE_URL = "https://kauth.kakao.com/oauth/authorize";

	// 인가 코드(code)를 접근 토큰으로 바꿔 주는 카카오 주소
	private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";

	// 접근 토큰으로 회원 정보를 알려주는 카카오 주소
	private static final String USERINFO_URL = "https://kapi.kakao.com/v2/user/me";

	// 예측할 수 없는 무작위 값을 만드는 도구 (state 값 만들 때 사용)
	private static final SecureRandom RANDOM = new SecureRandom();

	// 카카오 개발자 사이트에서 발급받은 REST API 키
	private static final String REST_API_KEY = "239d5b5c94535d722f29edcd15529830";

	// 카카오 로그인이 끝나면 돌아올 우리 사이트 주소 (카카오 개발자 사이트에 등록한 주소와 같아야 한다)
	private static final String REDIRECT_URI = "http://localhost:8081/CarProjectEdu/member/kakaoCallback.me";

	// 카카오 Client Secret (보안 설정을 켜지 않았으면 빈 글자로 둔다)
	private static final String CLIENT_SECRET = "";

	// 유틸 클래스이므로 객체를 만들지 못하게 막는다 (모든 메소드가 static)
	private KakaoAuth() {}

	//----------------------------------------------------------------
	// isConfigured : REST API 키가 설정되어 있는지 알려준다
	//  키가 비어 있거나 "여기에..." 같은 안내 문구 그대로면 false
	//----------------------------------------------------------------
	public static boolean isConfigured() {

		// 설정된 REST API 키
		String key = REST_API_KEY;

		// 키가 있고, 비어 있지 않고, 안내 문구("여기에")가 아니면 true 반환
		return key != null && !key.trim().isEmpty() && !key.contains("여기에");
	}

	//----------------------------------------------------------------
	// newState : 무작위 글자(state)를 만들어 반환한다
	//  카카오에서 돌아왔을 때 우리 사이트에서 시작한 로그인이 맞는지 확인하는 데 쓴다
	//  예) "Xk3fP9qL0aZ..."  (22글자)
	//----------------------------------------------------------------
	public static String newState() {

		// 16바이트짜리 빈 배열을 만들고 무작위 값으로 채운다
		byte[] bytes = new byte[16];
		RANDOM.nextBytes(bytes);

		// 주소에 넣어도 안전한 글자(Base64 URL 방식)로 바꿔 반환
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	//----------------------------------------------------------------
	// buildAuthorizeUrl : 고객을 보낼 카카오 로그인 화면 주소를 만들어 반환한다
	//  예) https://kauth.kakao.com/oauth/authorize?client_id=...&redirect_uri=...&response_type=code&state=...
	//----------------------------------------------------------------
	public static String buildAuthorizeUrl(String state) throws Exception {

		// 주소에 넣을 REST API 키와 돌아올 주소
		String clientId    = REST_API_KEY;
		String redirectUri = REDIRECT_URI;

		// 카카오 로그인 주소 뒤에 ?이름=값&이름=값 형태로 붙인다 (URLEncoder : 주소에 넣을 수 없는 글자를 %XX 로 변환)
		return AUTHORIZE_URL
			+ "?client_id="     + URLEncoder.encode(clientId, "UTF-8")
			+ "&redirect_uri="  + URLEncoder.encode(redirectUri, "UTF-8")
			+ "&response_type=code"
			+ "&state="         + URLEncoder.encode(state, "UTF-8");
	}

	//----------------------------------------------------------------
	// requestAccessToken : 카카오가 준 인가 코드(code)를 접근 토큰(access_token)으로 바꿔 반환한다
	//  접근 토큰 = 카카오에게 "이 회원의 정보를 알려주세요" 라고 요청할 때 보여주는 출입증
	//----------------------------------------------------------------
	public static String requestAccessToken(String code) throws Exception {

		// 요청에 넣을 REST API 키와 돌아올 주소
		String clientId    = REST_API_KEY;
		String redirectUri = REDIRECT_URI;

		// 카카오에 보낼 요청 본문 만들기 (이름=값&이름=값 형식)
		String body = "grant_type=authorization_code"
				+ "&client_id="    + URLEncoder.encode(clientId, "UTF-8")
				+ "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8")
				+ "&code="         + URLEncoder.encode(code, "UTF-8");

		// Client Secret 을 설정했을 때만 요청 본문에 함께 넣는다 (CLIENT_SECRET 을 채우면 실행된다)
		String clientSecret = CLIENT_SECRET;
		if (clientSecret != null && !clientSecret.trim().isEmpty() && !clientSecret.contains("여기에")) {
			body += "&client_secret=" + URLEncoder.encode(clientSecret, "UTF-8");
		}

		// 카카오 토큰 주소로 POST 요청을 보내고 응답(JSON 글자)을 받는다
		String json = post(TOKEN_URL, body, null);

		// JSON 글자를 자바 객체로 바꾼다  예) {"access_token":"abc...", ...}
		JSONObject obj = (JSONObject) new JSONParser().parse(json);

		// access_token 값을 꺼낸다
		Object token = obj.get("access_token");

		// 토큰이 없으면 발급 실패이므로 예외를 던진다 (사장의 catch 가 받아 안내창을 띄운다)
		if (token == null) {
			throw new IllegalStateException("카카오 토큰 발급 실패: " + json);
		}

		// 접근 토큰을 글자로 반환
		return token.toString();
	}

	//----------------------------------------------------------------
	// requestUserInfo : 접근 토큰으로 카카오 회원 정보(번호, 닉네임, 이메일)를 받아 KakaoUser 상자에 담아 반환한다
	//----------------------------------------------------------------
	public static KakaoUser requestUserInfo(String accessToken) throws Exception {

		// 카카오 회원 정보 주소로 POST 요청을 보내고 응답(JSON 글자)을 받는다
		String json = post(USERINFO_URL, "", accessToken);

		// JSON 글자를 자바 객체로 바꾼다
		JSONObject obj = (JSONObject) new JSONParser().parse(json);

		// 카카오 회원 번호를 꺼낸다. 없으면 조회 실패이므로 예외를 던진다
		Object id = obj.get("id");
		if (id == null) {
			throw new IllegalStateException("카카오 사용자 정보 조회 실패: " + json);
		}

		// 닉네임과 이메일은 회원이 동의하지 않았으면 없을 수 있으므로 null 로 시작한다
		String nickname = null;
		String email = null;

		// 응답 구조 : { "id":123, "kakao_account":{ "profile":{ "nickname":"홍길동" }, "email":"a@b.com" } }
		JSONObject account = (JSONObject) obj.get("kakao_account");
		if (account != null) {

			// kakao_account 안의 profile 안에서 닉네임 꺼내기
			JSONObject profile = (JSONObject) account.get("profile");
			if (profile != null && profile.get("nickname") != null) {
				nickname = profile.get("nickname").toString();
			}

			// kakao_account 안에서 이메일 꺼내기
			if (account.get("email") != null) {
				email = account.get("email").toString();
			}
		}

		// 꺼낸 정보를 KakaoUser 상자에 담아 반환
		return new KakaoUser(id.toString(), nickname, email);
	}

	//----------------------------------------------------------------
	// post : 카카오 서버에 POST 요청을 보내고 응답 글자를 반환한다 (이 클래스 안에서만 사용)
	//  bearerToken : 접근 토큰 (회원 정보 요청 때만 넣고, 토큰 요청 때는 null)
	//----------------------------------------------------------------
	private static String post(String urlText, String body, String bearerToken) throws Exception {

		// 요청할 주소 객체를 만들고 연결을 연다
		URL url = java.net.URI.create(urlText).toURL();
		HttpURLConnection con = (HttpURLConnection) url.openConnection();

		// POST 방식, 연결 대기 5초, 응답 대기 10초
		con.setRequestMethod("POST");
		con.setConnectTimeout(5000);
		con.setReadTimeout(10000);

		// 요청 본문이 이름=값 형식이고 한글은 UTF-8 이라고 알린다
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

		// 접근 토큰이 있으면 요청 머리말(헤더)에 "Bearer 토큰" 으로 넣는다
		if (bearerToken != null) {
			con.setRequestProperty("Authorization", "Bearer " + bearerToken);
		}

		// 요청 본문을 보낼 수 있게 설정하고, 본문을 UTF-8 바이트로 바꿔 보낸다
		con.setDoOutput(true);
		try (OutputStream os = con.getOutputStream()) {
			os.write(body.getBytes(StandardCharsets.UTF_8));
		}

		// 응답 상태 번호를 받는다 (200번대 = 성공)
		int status = con.getResponseCode();

		// 성공이면 정상 응답 통로를, 실패면 오류 응답 통로를 연다
		InputStream is = (status >= 200 && status < 300)
				? con.getInputStream()
				: con.getErrorStream();

		// 응답 글자를 한 줄씩 읽어 sb 에 이어 붙인다
		StringBuilder sb = new StringBuilder();
		if (is != null) {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
			}
		}

		// 실패 응답이면 상태 번호와 응답 내용을 담아 예외를 던진다
		if (status < 200 || status >= 300) {
			throw new IllegalStateException("카카오 API 오류 (HTTP " + status + "): " + sb);
		}

		// 성공 응답 글자 반환
		return sb.toString();
	}

	//----------------------------------------------------------------
	// KakaoUser : 카카오에서 받은 회원 정보 3개를 담아 나르는 상자 (클래스 안의 클래스)
	//  final : 한 번 넣으면 바꿀 수 없다
	//----------------------------------------------------------------
	public static class KakaoUser {

		// 카카오 회원 번호, 닉네임, 이메일
		public final String id;
		public final String nickname;
		public final String email;

		// 생성자 : 회원 정보 3개를 받아 상자에 저장한다
		public KakaoUser(String id, String nickname, String email) {
			this.id = id;
			this.nickname = nickname;
			this.email = email;
		}
	}
}
