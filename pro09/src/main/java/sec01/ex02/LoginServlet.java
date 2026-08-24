package sec01.ex02;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//예제 주제 : 웹페이지(서블릿페이지)를 연동(연결-재요청)하는 방법 중.... 
//			URL Rewriting 방법을 이용해
//			웹 페이지들(login2.html -> LoginServlet.class -> SecondServlet.class) 사이의 정보를 공유합니다.

/*
URL Rewriting 이란?
URL Rewriting 은 클라이언트가 쿠키를 지원하지 않는 경우에도 세션을 유지하기 위해 URL에 세션 ID를 추가하는 방식입니다.
즉, 서버에서 클라이언트에게 응답할 때, URL에 세션 ID를 포함하여 클라이언트가 이를 다시 서버로 전송할 수 있도록 합니다.

1. URL Rewriting 의 필요성

1.1.쿠키를 사용할 수 없는 경우
- 클라이언트가 쿠키를 차단한 경우
- 특정 환경(예: 보안이 중요한 환경)에서 쿠키가 비활성화된 경우

1.2. 세션을 유지해야 하는 경우
- 사용자가 로그인 상태를 유지해야 하는 웹 애플리케이션

2.URL Rewriting 방식
- 일반적으로 Java의 Servlet에서 response.encodeURL(String url)을 사용하여 URL을 재작성합니다.


response.encodeURL(String url) 상세 설명
- response.encodeURL(String url)은 세션 유지를 위해 URL을 자동으로 변환해 주는 메서드입니다.
- 쿠키를 사용할 수 없을 때 jsessionid 를 URL에 추가하여 세션을 유지하는 역할을 합니다.


*/

//POST 요청 주소 : http://localhost:8181/pro09/login2 

@WebServlet("/login2")
public class LoginServlet extends HttpServlet {
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
													throws ServletException, IOException {
		
    	//1. 서블릿이 요청받은 데이터들 중에서 한글 문자가 존재하면 한글깨져서 HttpServletRequest객체 메모리에서 얻어오기 떄문에
    	//	 미리 HttpServletRequest객체 메모리에 한글 문자를 처리할수 있는 방식(인코딩 방식)을 UTF-8로 설정 한다
    	//요약 : 한글처리
		request.setCharacterEncoding("UTF-8");
		/*
		  +--------------------------+
          | HttpServletRequest #100  |
          |  user_id      -> hong    |
          |  user_pw      -> 1234    |
          |  user_address -> 서울..  |
          |  user_email   -> test..  |
          |  user_hp      -> 010..   |
          +--------------------------+
		*/
		//2. login.html 에서 LoginServlet 서블릿페이지로 요청한 데이터들 HttpServletRequest 객체 메모리 내부에서 얻기
		//요약 : 클라이언트가 요청한 데이터들 얻기 
		String user_id = request.getParameter("user_id"); //입력한 아이디를 문자열로 얻기
		String user_pw = request.getParameter("user_pw"); //입력한 비밀번호를 문자열로 얻기 		
		
		String user_address = request.getParameter("user_address");
		String user_email = request.getParameter("user_email");
		String user_hp    = request.getParameter("user_hp");
		//<input type="hidden"> 태그들에 작성해서 요청했던 주소, 이메일, 전화번호 도 문자열로 얻기
		
		//3. login.html 디자인 화면을 보고 요청했던 클라이언트의 웹브라우저로 응답할 메세지를 만들어서 응답.
		
		//3.1. 응답할 메세지 만들기
    	String data = "안녕하세요!<br> 로그인하셨습니다.<br><br>";
			   data += "입력한 아이디 : " + user_id + "<br>";
			   data += "입력한 비밀번호 : " + user_pw + "<br>";
			   data += "주소 : " + user_address + "<br>";
			   data += "이메일 : " + user_email + "<br>";
			   data += "휴대전화 : " + user_hp + "<br>";
			   
			   data += "<a href='/pro09/second?user_id="+ user_id
					                        +"&user_pw="+ user_pw 
					                        +"&user_address=" + URLEncoder.encode(user_address,"UTF-8")
					                        +"'>두번째 서블릿을 재요청시 데이터 보내기</a>";			   
/*
<a href="http://localhost:8181/second?user_id=admin&user_pw=1234&user_address=서울시 성북구">두번째 서블릿을 재요청시 데이터 보내기</a>
																				아래처럼 바뀜 
<a href="http://localhost:8181/second?user_id=admin&user_pw=1234&user_address="%EC%84%9C%EC%9A%B8%EC%8B%9C+%EC%84%B1%EB%B6%81%EA%B5%AC"">두번째 서블릿을 재요청시 데이터 보내기</a>

*/      
	  	// URLEncoder.encode("서울시 성북구", "utf-8")
	  	// : "서울시 성북구" 라는 한글 문자열은 URL(인터넷 주소)에 그대로 넣으면
	  	//   글자가 깨지거나 오류가 날 수 있기 때문에,
	  	//   UTF-8 문자 인코딩 방식 규칙을 사용하여
	  	//   웹 브라우저와 서버가 안전하게 인식할 수 있는
	  	//   특수한 영문 기호 형태(%EC%84%9C… 등)로 변환해 주는 코드이다.

	  	// URLEncoder.encode("서울시 성북구", "utf-8");
	  	// --------------------------------------------------------------------
	  	// 1. URLEncoder.encode()
	  	//    : 자바에서 제공하는 “URL 문자열 변환 도구”
	  	//    → 한글, 공백, 특수문자를 인터넷 주소(URL)에 사용할 수 있는  안전한 문자 형태로 바꿔주는 기능

	  	// 2. "서울시 성북구"
	  	//    : 우리가 실제로 서버에 보내고 싶은 한글 주소 데이터
	  	//       하지만 URL은 영어/숫자 위주만 인식하므로
	  	//       그대로 사용하면 글자 깨짐 또는 오류 발생 가능
	  	//
	  	// 3. "utf-8"
	  	//    : 어떤 문자 변환 규칙으로 바꿀지 정하는 표준 방식
	  	//      전 세계 웹에서 가장 많이 사용하는 문자 인코딩 방식
	  	//
	  	// 4. 변환 결과
	  	//    "서울시 성북구" → "%EC%84%9C%EC%9A%B8%EC%8B%9C+%EC%84%B1%EB%B6%81%EA%B5%AC"
	  	//    한글은 %기호가 포함된 영문 코드로,
	  	//    공백(띄어쓰기)은 + 기호로 자동 변경됨
	  	//
	  	// 5. 사용하는 이유
	  	//    - GET 방식 파라미터 전송 시 한글 깨짐 방지
	  	//    - 쿠키에 한글 저장 시 오류 방지
	  	//    - 브라우저와 서버 간 데이터 전달 안정성 확보
	  	//
	  	// 즉, 이 코드는 “한글 주소를 브라우저가 이해할 수 있는 언어로 번역하는 과정”이다.
	  	// --------------------------------------------------------------------				   
			   
		//3.2.응답할 메세지 유형을 HttpServletResponse객체 메모리에 설정
    	response.setContentType("text/html; charset=UTF-8");
	    	
    	//3.3.출력스트림 통로 PrintWrier객체를 생성해 응답할 메세지를 브라우저로 보내어서 출력
    	response.getWriter().print(data);
	
	}
	
	

}






