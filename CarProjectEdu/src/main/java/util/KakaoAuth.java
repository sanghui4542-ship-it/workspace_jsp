// 이 파일이 속한 폴더(패키지) 이름. 실제 폴더 경로 util 와 반드시 같아야 한다
package util;   // 이 파일이 속한 패키지(=폴더) 이름. 실제 폴더 구조와 반드시 같아야 한다
// java.io 안의 BufferedReader 를 이 파일에서 쓰겠다는 선언
import java.io.BufferedReader;   // 글자를 줄 단위로 읽는 도구 (외부 API 응답을 읽을 때 사용)
// java.io 안의 InputStream 를 이 파일에서 쓰겠다는 선언
import java.io.InputStream;   // 바이트를 읽어 들이는 통로
// java.io 안의 InputStreamReader 를 이 파일에서 쓰겠다는 선언
import java.io.InputStreamReader;   // 바이트 통로를 글자 통로로 바꿔 주는 도구
// java.io 안의 OutputStream 를 이 파일에서 쓰겠다는 선언
import java.io.OutputStream;   // 바이트를 내보내는 통로
// java.net 안의 HttpURLConnection 를 이 파일에서 쓰겠다는 선언
import java.net.HttpURLConnection;   // 자바가 직접 다른 서버에 요청을 보낼 때 쓰는 도구
// java.net 안의 URL 를 이 파일에서 쓰겠다는 선언
import java.net.URL;   // 인터넷 주소 하나를 나타내는 객체
// java.net 안의 URLEncoder 를 이 파일에서 쓰겠다는 선언
import java.net.URLEncoder;   // 한글·공백을 주소에 넣을 수 있는 형태로 바꿔 준다
// java.nio.charset 안의 StandardCharsets 를 이 파일에서 쓰겠다는 선언
import java.nio.charset.StandardCharsets;   // "UTF-8" 을 오타 없이 쓰기 위한 상수 모음
// java.security 안의 SecureRandom 를 이 파일에서 쓰겠다는 선언
import java.security.SecureRandom;   // 예측하기 어려운 난수 생성기 (보안용. Math.random 보다 안전하다)
// java.util 안의 Base64 를 이 파일에서 쓰겠다는 선언
import java.util.Base64;   // 바이트를 글자로 바꿔 주는 인코딩 도구 (주소에 실어 보낼 수 있는 형태로)
// org.json.simple 안의 JSONObject 를 이 파일에서 쓰겠다는 선언
import org.json.simple.JSONObject;   // { "key": "value" } 형태의 JSON 을 만드는 도구
// org.json.simple.parser 안의 JSONParser 를 이 파일에서 쓰겠다는 선언
import org.json.simple.parser.JSONParser;   // JSON 글자를 자바 객체로 바꿔 주는 도구
/*
 ================================================================================
   KakaoAuth  -  카카오 로그인 (OAuth 2.0 인가코드 방식)
   [전체 흐름 - 이 순서 그대로 외우면 된다]
     ① 사용자가 "카카오 로그인" 버튼 클릭
            -> 우리 서버(/member/kakaoLogin.me)가 카카오 인증 페이지로 redirect
               buildAuthorizeUrl() 이 그 주소를 만든다
     ② 사용자가 카카오 화면에서 아이디/비밀번호 입력 + 동의
            -> 카카오가 우리 서버(/member/kakaoCallback.me)로 되돌려 보낸다
               이때 주소에 "인가코드(code)" 가 붙어서 온다
     ③ 우리 서버가 인가코드를 카카오에 제출하고 "액세스 토큰"을 받는다
               requestAccessToken()   (서버 <-> 카카오 직접 통신. 브라우저 안 거침)
     ④ 액세스 토큰으로 사용자 정보(회원번호/닉네임/이메일)를 조회한다
               requestUserInfo()
     ⑤ 그 회원번호로 우리 DB의 member 테이블에서 회원을 찾고
        (처음이면 자동 가입) 세션에 로그인 처리한다  -> MemberService 담당
   [왜 비밀번호를 우리가 안 받는가]
     사용자의 카카오 비밀번호는 카카오 화면(②)에만 입력된다.
     우리 서버는 비밀번호를 만질 일이 없고, "이 사람이 카카오 회원 12345다"라는
     증명(토큰)만 받는다. 비밀번호를 저장할 필요가 없으니 유출될 일도 없다.
   [state 파라미터 - CSRF 방어]
     ①에서 무작위 문자열(state)을 만들어 세션에 저장하고 주소에 실어 보낸다.
     ②에서 카카오가 같은 값을 그대로 돌려준다.
     콜백에서 "세션의 state == 돌려받은 state" 인지 확인해서
     공격자가 자기 인가코드를 남의 브라우저에 심는 공격(CSRF)을 막는다.
   [설정 - WEB-INF/app.properties]
     kakao.rest.api.key = 카카오 개발자 콘솔의 "REST API 키"
     kakao.redirect.uri = http://localhost:8081/CarProject/member/kakaoCallback.me
                          (콘솔의 Redirect URI 등록값과 글자 하나까지 같아야 한다)
 ================================================================================
*/
public class KakaoAuth {
	/** 카카오 인증 서버 (사용자가 로그인하는 화면) */
	private static final String AUTHORIZE_URL = "https://kauth.kakao.com/oauth/authorize";
	/** 인가코드 -> 액세스 토큰 교환 주소 */
	private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
	/** 액세스 토큰 -> 사용자 정보 조회 주소 */
	private static final String USERINFO_URL = "https://kapi.kakao.com/v2/user/me";
	// state 값을 만들 때 쓸 난수 생성기.
	// 딱 하나만 만들어 재사용한다(static). 호출할 때마다 새로 만들면 느리고 안전성도 떨어진다.
	private static final SecureRandom RANDOM = new SecureRandom();
	/* ── 카카오 개발자 콘솔(developers.kakao.com)에서 발급받은 본인 값으로 바꾸세요 ── */
	// 지금은 "여기에_..." 라는 안내 글자가 들어 있다. 본인 키로 바꿔야 카카오 로그인이 동작한다.
	// ※ 키는 비밀번호와 같다. 공개 저장소(깃허브)에 올리면 남이 내 계정으로 API 를 쓴다.
	private static final String REST_API_KEY = "여기에_본인_REST_API_키";
	// 카카오가 로그인 후 사용자를 되돌려 보낼 우리 서버 주소.
	// 카카오 콘솔에 등록한 값과 글자 하나까지 같아야 한다. 다르면 카카오가 거부한다.
	private static final String REDIRECT_URI = "http://localhost:8081/CarProjectEdu/member/kakaoCallback.me";
	private static final String CLIENT_SECRET = "";   // 콘솔에서 켰을 때만 채운다
	// 도구 모음 클래스라 객체를 만들지 못하게 막는다
	private KakaoAuth() {}
	//===========================================================
	// 설정 확인
	//===========================================================
	/** REST API 키가 설정되어 있는가 (없으면 버튼을 숨기거나 안내를 띄운다) */
	public static boolean isConfigured() {
		// 위에 적어 둔 키를 읽어 온다
		String key = REST_API_KEY;
		// 세 가지를 모두 만족해야 "설정됐다" 고 본다.  && 는 "그리고" 라는 뜻이다.
		//   1) null 이 아니다      2) 빈 값이 아니다
		//   3) "여기에" 라는 안내 글자가 남아 있지 않다 (= 아직 안 바꿨다는 뜻이므로)
		return key != null && !key.trim().isEmpty() && !key.contains("여기에");
	}
	//===========================================================
	// ① 인증 페이지 주소 만들기
	//===========================================================
	/** CSRF 방어용 무작위 state 문자열 생성 */
	public static String newState() {
		// 16바이트(=128비트) 짜리 빈 바구니를 만든다. 남이 맞힐 수 없을 만큼 충분한 크기다.
		byte[] bytes = new byte[16];
		// 그 바구니를 무작위 값으로 채운다
		RANDOM.nextBytes(bytes);
		// 바이트 그대로는 주소에 실을 수 없으므로 글자로 바꾼다.
		//   getUrlEncoder()    : 주소에 써도 안전한 글자만 쓰는 방식(+ 와 / 를 안 쓴다)
		//   withoutPadding()   : 끝에 붙는 = 기호를 없앤다 (주소에서 지저분해 보이므로)
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}
	/**
	 * 사용자를 보낼 카카오 로그인 화면 주소를 만든다.
	 *
	 * 완성 예:
	 *   https://kauth.kakao.com/oauth/authorize?client_id=REST키
	 *       &redirect_uri=콜백주소&response_type=code&state=무작위값
	 */
	public static String buildAuthorizeUrl(String state) throws Exception {
		// 위에 적어 둔 설정값을 읽기 쉬운 이름으로 옮겨 담는다
		String clientId    = REST_API_KEY;
		String redirectUri = REDIRECT_URI;   // ①에서 보낸 것과 같은 콜백 주소여야 카카오가 인정한다
		// 주소를 조각조각 이어 붙여 만든다.
		// 첫 값 앞에는 ? 를, 두 번째부터는 & 를 붙이는 것이 주소의 규칙이다.
		return AUTHORIZE_URL
			// client_id = "우리 앱이 누구인지" 카카오에 알리는 값
			+ "?client_id="     + URLEncoder.encode(clientId, "UTF-8")
			// redirect_uri = 로그인이 끝나면 사용자를 어디로 돌려보낼지.
			// URLEncoder.encode 는 : 나 / 처럼 주소에서 특별한 뜻을 가진 글자를 안전하게 바꿔 준다.
			+ "&redirect_uri="  + URLEncoder.encode(redirectUri, "UTF-8")
			// response_type=code = "인가코드 방식으로 주세요" 라는 고정값
			+ "&response_type=code"
			// state = 위에서 만든 무작위 값. 콜백에서 이 값이 그대로 돌아왔는지 확인한다(CSRF 방어)
			+ "&state="         + URLEncoder.encode(state, "UTF-8");
	}
	//===========================================================
	// ③ 인가코드 -> 액세스 토큰
	//===========================================================
	/**
	 * 콜백으로 받은 인가코드를 카카오에 제출하고 액세스 토큰을 받아온다.
	 * (브라우저를 거치지 않는 서버 <-> 카카오 직접 통신)
	 *
	 * @return 액세스 토큰 문자열
	 * @throws Exception 통신 실패, 카카오가 오류 응답을 준 경우
	 */
	public static String requestAccessToken(String code) throws Exception {
		// 설정값을 꺼내 온다
		String clientId    = REST_API_KEY;
		String redirectUri = REDIRECT_URI;   // ①에서 보낸 것과 같은 콜백 주소여야 카카오가 인정한다
		//보낼 본문 : application/x-www-form-urlencoded 형식
		// 주소 뒤에 붙이는 형태(이름=값&이름=값)와 똑같지만, 주소가 아니라 "본문" 에 실어 보낸다.
		// 인가코드가 주소에 남으면 브라우저 기록에 남기 때문에 본문으로 보내는 것이다.
		String body = "grant_type=authorization_code"
				// 우리 앱 식별값
				+ "&client_id="    + URLEncoder.encode(clientId, "UTF-8")
				// ①에서 보낸 것과 똑같은 값이어야 카카오가 인정한다
				+ "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8")
				// ②에서 카카오가 준 인가코드. 한 번 쓰면 만료된다.
				+ "&code="         + URLEncoder.encode(code, "UTF-8");
		//(선택) 콘솔에서 Client Secret 을 켰다면 함께 보내야 한다
		String clientSecret = CLIENT_SECRET;
		// 값이 있고, 비어 있지 않고, 안내 글자도 아니라면 = 실제 값이 채워져 있다면
		if (clientSecret != null && !clientSecret.trim().isEmpty() && !clientSecret.contains("여기에")) {
			// += 는 "기존 값 뒤에 이어 붙인다" 는 뜻이다
			body += "&client_secret=" + URLEncoder.encode(clientSecret, "UTF-8");
		}
		// 아래에 만들어 둔 공통 POST 메소드로 카카오에 요청을 보낸다.
		// 세 번째 인자가 null 인 이유 : 아직 토큰이 없으니 Authorization 헤더를 붙일 수 없다.
		String json = post(TOKEN_URL, body, null);
		// 받은 JSON 글자를 자바가 다룰 수 있는 객체로 바꾼다.
		// (JSONObject) 는 형변환. parse 의 결과가 Object 타입이라 우리가 원하는 타입임을 알려 주는 것이다.
		JSONObject obj = (JSONObject) new JSONParser().parse(json);
		// "access_token" 이라는 이름표로 값을 꺼낸다. 없으면 null 이 나온다.
		Object token = obj.get("access_token");
		// 토큰이 없다 = 발급에 실패했다
		if (token == null) {
			//error / error_description 을 로그로 남긴다 (토큰은 애초에 없음)
			// 받은 JSON 을 통째로 메시지에 담는다. 카카오가 알려 준 실패 이유가 그 안에 들어 있다.
			throw new IllegalStateException("카카오 토큰 발급 실패: " + json);
		}
		// Object 타입이므로 toString() 으로 글자로 바꿔 돌려준다
		return token.toString();
	}
	//===========================================================
	// ④ 액세스 토큰 -> 사용자 정보
	//===========================================================
	/**
	 * 사용자 정보를 조회해서 필요한 3가지만 담아 돌려준다.
	 *
	 * 카카오 응답(필요한 부분만):
	 *   {
	 *     "id": 4392817465,                          <- 카카오 회원번호 (앱마다 고유, 불변)
	 *     "kakao_account": {
	 *        "profile": { "nickname": "상국" },
	 *        "email": "xxx@kakao.com"                <- 동의 항목에 따라 없을 수 있다
	 *     }
	 *   }
	 */
	public static KakaoUser requestUserInfo(String accessToken) throws Exception {
		// 본문은 빈 문자열("")이고, 대신 세 번째 인자로 토큰을 넘긴다.
		// 토큰은 Authorization 헤더에 실려 나간다 = "나는 아까 인증받은 그 사람이다" 라는 증명서.
		String json = post(USERINFO_URL, "", accessToken);
		// 받은 JSON 글자를 자바 객체로 바꾼다
		JSONObject obj = (JSONObject) new JSONParser().parse(json);
		// "id" = 카카오 회원번호. 우리 DB 에서 이 사람을 알아보는 유일한 근거다.
		Object id = obj.get("id");
		// 회원번호조차 없다면 응답이 잘못된 것이다
		if (id == null) {
			// IllegalStateException 를 던져 여기서 중단한다. 위쪽 공통 처리기가 받아 알맞은 오류 화면을 보여준다
			throw new IllegalStateException("카카오 사용자 정보 조회 실패: " + json);
		}
		// 닉네임과 이메일은 "동의 안 하면 안 올 수도 있는" 값이다.
		// 그래서 일단 null 로 시작해 두고, 있으면 채운다.
		String nickname = null;
		String email = null;   // 이메일도 동의 안 하면 안 올 수 있으므로 일단 비워 둔다
		// kakao_account 는 JSON 안에 들어 있는 또 다른 덩어리다(중첩 구조).
		JSONObject account = (JSONObject) obj.get("kakao_account");
		// 그 덩어리 자체가 없을 수도 있으므로 확인하고 들어간다
		if (account != null) {
			// profile 은 kakao_account 안에 또 들어 있는 덩어리다
			JSONObject profile = (JSONObject) account.get("profile");
			// profile 이 있고, 그 안에 nickname 도 있을 때만
			if (profile != null && profile.get("nickname") != null) {
				// 닉네임을 글자로 바꿔 담는다
				nickname = profile.get("nickname").toString();
			}
			// 이메일은 profile 이 아니라 kakao_account 바로 아래에 있다
			if (account.get("email") != null) {
				// 이메일을 글자로 바꿔 담는다
				email = account.get("email").toString();
			}
		}
		// 필요한 3가지만 작은 상자에 담아 돌려준다.
		// 카카오 응답 전체를 그대로 넘기지 않는 이유 : 위 계층이 카카오 JSON 구조를 몰라도 되게 하려는 것이다.
		return new KakaoUser(id.toString(), nickname, email);
	}
	//===========================================================
	// 공통 : POST 통신 (챗봇의 OpenRouter 호출과 같은 방식)
	//===========================================================
	private static String post(String urlText, String body, String bearerToken) throws Exception {
		//new URL("주소") 는 자바 20부터 권장하지 않는다. URI 를 거쳐 만드는 것이 표준이다.
		URL url = java.net.URI.create(urlText).toURL();
		// 실제로 연결을 여는 객체를 얻는다. 아직 통신이 시작된 것은 아니다.
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		// 요청 방식을 POST 로 지정한다 (기본값은 GET)
		con.setRequestMethod("POST");
		// con 의 setConnectTimeout( ) 를 실행한다 — 넘기는 값이 다음 줄로 이어진다
		con.setConnectTimeout(5000);   //5초 안에 연결 안 되면 포기 (무한 대기 방지)
		// 연결은 됐는데 답이 안 오는 경우를 대비해 읽기도 10초로 제한한다.
		// 시간 제한이 없으면 카카오가 응답을 안 줄 때 우리 서버 스레드가 영원히 묶인다.
		con.setReadTimeout(10000);
		// 보내는 본문의 형식을 알려 준다. "이름=값&이름=값" 형태이고 한글은 UTF-8 이라는 뜻이다.
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		// 토큰이 있으면(=사용자 정보 조회일 때) 신분증을 헤더에 붙인다
		if (bearerToken != null) {
			// "Bearer " 라는 접두어는 OAuth 2.0 의 약속이다. 띄어쓰기까지 정확해야 한다.
			con.setRequestProperty("Authorization", "Bearer " + bearerToken);
		}
		// "본문을 보낼 것" 이라고 미리 선언해야 출력 통로를 열 수 있다
		con.setDoOutput(true);
		// try( ) 안에 선언하면 블록이 끝날 때 자동으로 닫힌다(try-with-resources).
		// 통로를 안 닫으면 자원이 새어 나가는데, 이 문법을 쓰면 잊어버릴 수가 없다.
		try (OutputStream os = con.getOutputStream()) {
			// 글자를 UTF-8 바이트로 바꿔서 내보낸다. 이 순간 실제 통신이 시작된다.
			os.write(body.getBytes(StandardCharsets.UTF_8));
		}
		// 응답 상태 코드를 읽는다. 200=성공, 400=요청 잘못, 401=인증 실패 ...
		int status = con.getResponseCode();
		//성공(2xx)이면 본문, 실패면 오류 본문을 읽는다 (원인 파악용)
		// 실패했을 때 getInputStream() 을 부르면 예외가 나서 원인을 못 본다.
		// 그래서 실패 시에는 getErrorStream() 으로 읽어야 카카오가 알려 준 이유를 확인할 수 있다.
		InputStream is = (status >= 200 && status < 300)
				? con.getInputStream()
				: con.getErrorStream();
		// 응답을 한 줄씩 읽어 이어 붙일 준비
		StringBuilder sb = new StringBuilder();
		// 통로가 아예 없을 수도 있으므로 확인하고 읽는다
		if (is != null) {
			// 바이트 통로 -> 글자 통로 -> 줄 단위 읽기, 3겹으로 감싼다. 다 쓰면 자동으로 닫힌다.
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
				// 읽은 한 줄을 담을 변수
				String line;
				// readLine() 은 더 읽을 것이 없으면 null 을 준다. 그때까지 반복한다.
				while ((line = br.readLine()) != null) {
					// 읽은 줄을 뒤에 이어 붙인다
					sb.append(line);
				}
			}
		}
		// 2xx 가 아니면 실패다. 위에서 읽어 둔 오류 본문을 메시지에 담아 던진다.
		if (status < 200 || status >= 300) {
			// IllegalStateException 를 던져 여기서 중단한다. 위쪽 공통 처리기가 받아 알맞은 오류 화면을 보여준다
			throw new IllegalStateException("카카오 API 오류 (HTTP " + status + "): " + sb);
		}
		// 성공했으면 응답 본문(JSON 글자)을 돌려준다
		return sb.toString();
	}
	//===========================================================
	// 사용자 정보 3가지를 담는 작은 상자
	//===========================================================
	// 클래스 안에 또 클래스를 넣은 것을 "중첩 클래스" 라고 한다.
	// KakaoUser 는 KakaoAuth 에서만 쓰는 작은 상자라 파일을 따로 만들지 않고 안에 넣었다.
	// static 을 붙였으므로 KakaoAuth 객체 없이 new KakaoAuth.KakaoUser(...) 로 만들 수 있다.
	public static class KakaoUser {
		/** 카카오 회원번호 (이 앱 기준 고유·불변 - 우리 DB 아이디의 근거) */
		// public final = 바깥에서 바로 읽을 수 있지만(public), 한 번 정하면 못 바꾼다(final).
		// 값이 바뀔 일이 없는 작은 상자라 getter 를 만들지 않고 이렇게 간단히 처리했다.
		public final String id;
		/** 프로필 닉네임 (동의 안 하면 null) */
		public final String nickname;
		/** 이메일 (동의 항목에 따라 null 가능) */
		public final String email;
		// 세 값을 받아 한 번에 채우는 생성자. final 필드는 여기서만 값을 정할 수 있다.
		public KakaoUser(String id, String nickname, String email) {
			// id 에 계산한 값을 담는다
			this.id = id;   // 받은 값을 이 상자의 id 칸에 저장
			// nickname 에 계산한 값을 담는다
			this.nickname = nickname;   // 받은 값을 이 상자의 nickname 칸에 저장
			// email 에 계산한 값을 담는다
			this.email = email;   // 받은 값을 이 상자의 email 칸에 저장
		}
	}
}