package sec02.ex01;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



//두번쨰 서블릿 페이지 :  사용자가 로그인 했는지 Cookie 객체의 정보를 확인 하고, 로그인 유지 상태를 화면에 보여주는 서블릿 

@WebServlet("/welcome")
public class WelcomeServlet  extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse resposne) throws ServletException, IOException {
		//재료
		//1. 재요청한 데이터 한글처리
		request.setCharacterEncoding("UTF-8");
		
		//2. 브라우저로 응답할 메세지 유형 설정 및 응답 문자 한글처리 1
		//   코드가 실행되는 시점 : 브라우저가 응답메세지를 해석할 문자셋 방식의 값을 utf-8을 결정하며 클라이언트의 브라우저가 문서를 해석할때 실행됨
		resposne.setContentType("text/html; charset=UTF-8"); //응답할 메세지 종류 설정  +  한글문자를 처리할수 있는 문자셋 방식을 utf-8값으로 설정
		
		//2.1.브라우저로 응답할 메세지 한글처리 2
		//   코드가 실행되는 시점 : 서블릿이 응답 메세지를 UTF-8로 설정하며 톰캣서버가 응답메세지를 생성할때 실행됨
		resposne.setCharacterEncoding("UTF-8");
		
		//3. 요청한 브라우저와 연결된 출력스트림 통로 생성
		PrintWriter out = resposne.getWriter(); 
		
		//실제 작업
		//---------------------------------------------------------------------
		//1. 사용자의 브라우저가 설치된 PC의 쿠키파일로 저장된 Cookie 객체의 정보 모두를 가져온다.
		//  -> HttpServletRequest객체의 getCookies() 메소드는 브라우저가 서버로 보낸 Cookie 객체가 저장된 배열을 반환해주는 메소드이다.
		//     첫 방문자라면 Cookie 객체가 없을수 있으므로 null 값을 반환 하는 메소드이다.
		Cookie[] cookies = request.getCookies();
		//[ new Cookie("user_id", "admin")  ]  <====== Cookie[] 배열 모습 
		//              쿠키명    ,  쿠키값
		//				0                     index
		
		//--------------------------------------------------------------------------
		//2.  Cookie 객체를 얻었으면 Cookie 객체에 저장되어 있었던 쿠키값(로그인 처리시 저장했던 아이디)를 저장할 변수 선언
		String userid = null;
		
		//----------------------------------------------------------------------------
		//3.  Cookie 객체가 Cookie[] 배열에 저장되어 있는지 확인 
		if(cookies != null) {			
			//---------------------------------------------------------------
			//4. Cookie[] 배열에 저장되어 있는 Cookie 객체를 꺼내서 확인 
			//  -> Cookie 객체가 여러개 있을 수 있으므로 검사 
			for(Cookie cookie  :  cookies ) {				
				//-------------------------------------------------
				//5. Cookie 객체 정보 중에서 쿠키명이 "user_id"인 Cookie 객체가 있는지 검사 
				// ===> (LoginServlet 메인페이지에 로그인요청했던 사람인지 검사)
				if("user_id".equals(cookie.getName())) {				
					//-----------------------------------------------
					//6. 찾은 Cookie 객체 안에 실제 저장된 쿠키값(로그인 처리시 저장했었던 입력한 아이디 "admin")을 가져온다.
					userid = URLDecoder.decode(cookie.getValue(), "UTF-8");
				} // if	
			} // for
		} // if
		
		//----------------------------------------------------------------------
		//7. 이미 로그인한 사용자로 판단 (userid 변수에 로그인 처리시 입력한 아이디 "admin"이 저장되어 있는지 판단)
		if(userid != null) {
			//------------------------------------------------------
			//8. 로그인 요청한 사용자 브라우저에 로그인된 환영 메세지 출력
			out.print("<h1>환영합니다," + userid + "님! 로그인중........</h1>");
			
			//------------------------------------------------------
			//9. 로그아웃 처리 요청 할수 있는 LogoutServlet를 포워딩(재요청)할 <a>링크 제공 
			out.print("<a href='logout'>로그아웃</a>");
			
		}else {
			//---------------------------------------------------------
			//10. 로그인 된 사용자가 아닌데... 이 두번째 WelcomeServlet 페이지를 보고 있다면?
			//    미로그인 된 상태이기 떄문에 로그인 요청할수 있는 디자인 페이지 포워딩 (재요청)
			resposne.sendRedirect("login3.html");
		} 
	} //doGet
} // WelcomeServlet (두번째 서브 페이지)







