package sec02.ex01;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//<a href='logout'>로그아웃</a> 클릭하여 !!!!! 로그아웃 요청이 들어오면 
//웹브라우저를 사용하는 사용자PC에 저장된 쿠키파일의 정보를 삭제해서 
//로그아웃된 화면을 보이게 하는 서블릿
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//재료
		//1. 요청한 데이터 한글처리
		request.setCharacterEncoding("UTF-8");
		//2. 브라우저로 응답할 메세지 유형 설정 및 한글처리
		response.setContentType("text/html; charset=UTF-8");
		//3. 브라우저에 응답할 출력스트림 얻기
		PrintWriter  out = response.getWriter();
		
		//실제 로그 아웃 처리 작업
		//1. "user_id"라는 쿠키명의 Cookie 객체를 삭제 하기 위해
		//    동일한 쿠키명을 가진 새로운 Cookie 객체를 하나 만든다.
		//    (쿠키값은 의미가 없으므로 "" 빈문자열로 넣어서 만든다.)
		//    => Cookie 객체는 "쿠키명"(user_id)이 같아야 덮어쓰기/삭제 가 가능하다.
		Cookie cookie = new Cookie("user_id","");
		
		//2. Cookie 객체의 정보가 쿠키파일에 저장되는 유효시간을 0초로 설정한다.
		//   -> 브라우저에게 "이  Cookie 객체의 정보는 지금 당장 삭제해라"라는 의미
		//   -> setMaxAge(0); = 즉시 삭제 
		cookie.setMaxAge(0);
		
		//3. Cookie 객체의 정보를 사용하기 위한 클라이언트의 요청 URL 경로를 Cookie 객체 설정
		//  -> 처음 LoginServlet.java 파일에서 Cookie 객체를 만들어 설정한것 처럼 동일하게 설정해야 한다.
		//	     이유 : 브라우저가 같은 Cookie 객체임을 인식하고 정상적으로 삭제한다.
		cookie.setPath("/");
		
		//4. 삭제 설정된 new Cookie("user_id","") 객체의 정보를 브라우저에게 다시 내보내어 응답하기 위해
		//   HttpServletResponse 객체에 새로 생성된 위 Cookie 객체를 추가해서 저장시킴
		//   => 브라우저는 이 new Cookie("user_id","") 객체의 정보를 보고 
		//      기존 new Cookie("user_id,"admin") 객체임을 인식하고 정삭적으로 덮어 써서 삭제해 버린다.
		response.addCookie(cookie);
		
		//5. 4.에서 로그아웃 처리 후 로그인 요청 화면(login3.html)을 포워딩(재요청)해서 보여준다.
		response.sendRedirect("login3.html");
	}
	
}




