package sec02.ex01;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/*
동작 순서
	순서1. 클라이언트가 웹브라우저 주소창에 http://localhost:8181/pro09/set 주소를 입력하여 
	      SetCookieValue 서블릿페이지 요청합니다.

	순서2. SetCookieValue 서블릿페이지(톰캣 서버가 실행하는 서버페이지)내부에서
	      Cookie 클래스의 객체를 생성한 후  쿠키이름을 cooikeTest로  쿠키값을 저장합니다.
    	   그리고 setMaxAge()메소드를 사용하여 생성한 Cookie 객체의 유효시간을 24시간으로 설정합니다.
    	  그런 다음 HttpServletResponse객체의 addCookie()메소드를 사용해 생성된 Cookie 객체를 추가한 후
    	  응답 메세지 + 생성된 Cookie 객체를  브라우저로 전송해서 응답합니다.
    
        순서3. 요청한 클라이언트의 브라우저에 쿠키 저장소에 전달받은 Cookie 객체가 저장됩니다.
*/

//톰캣 서버측의 서버페이지(SetCookieValue 서블릿페이지)
@WebServlet("/set")
public class SetCookieValue extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//재료
		//1. 요청한 데이터 한글처리
		request.setCharacterEncoding("UTF-8");
		//2. 브라우저로 응답할 메세지 유형 설정 및 한글 처리 설정
		response.setContentType("text/html; charset=UTF-8");
		//3. 브라우저에 응답할 출력스트림 통로 생성
		PrintWriter  out = response.getWriter();
		
		//실제 작업
		//1. 요청한 데이터 얻기(요청 주소 URL 존재 하니 작성하지 않는다.)
		//2. 브라우저로 응답할 데이터 생성 
		//	 2.1. 현재 날짜 정보를 응답할데이터로 생성
			 Date date = new Date();
		//   2.2. 쿠키 생성 (Cookie 클래스의 객체 생성)
			 Cookie  cookie = new Cookie("cookieTest", URLEncoder.encode("JSP프로그래밍입니다.", "utf-8"));
		//									쿠키이름   ,         쿠키값		 
		//	 2.3. Cookie 객체 메모리의 유효 기간 설정 - 하루 24시간 유효
		//	 cookie.setMaxAge(60 * 60 * 24); //<- 쿠키파일이 생성되면서 Cookie 객체 데이터 저장 후 클라이언트 PC에 저장됨 
			 
			 //유효 기간을 음수 -1로 지정하면 쿠키 메모리 생성되면서 Cookie객체의 데이터의 파일이 저장하는 것이아니라
			 //클라이언트의 웹브라우저로가 사용하는 쿠키저장소에 session쿠키 메모리로 저장후 내보내 집니다.
			 cookie.setMaxAge(-1);
					 
		//   2.4. 생성된 Cookie 객체 메모리를 클라이언트 요청시 사용된 클라이언트의 브라우저로 전달하기 위해 
		//        HttpServletResponse 객체 메모리 내부에 추가 해서 저장
			 response.addCookie(cookie);
			 
		//3. 클라이언트의 웹브라우저로  응답할 현재 날짜데이터  +  Cookie 객체 메모리의 정보가 저장된 HttpServletResponse객체 정보를
		//   PrintWriter 출력스트림 통로를 통해 내보내어 응답 해줍니다.
		out.println("현재 날짜 및 시간 정보 : " + date.toString());
		out.println("Cookie 객체를 생성해서 웹브라우저의 쿠키 저장소 공간으로 보냈습니다.");
		
		
	}


}










