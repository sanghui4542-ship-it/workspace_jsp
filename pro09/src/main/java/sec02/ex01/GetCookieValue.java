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


/*
같은 브라우저창 를 이용하는 클라이언트가~ pro09 웹사이트에 포함된 GetCookieValue 서블릿 페이지를 재요청 하는 경우!
 
순서1. 두번쨰 서블릿인 GetCookieValue 요청시에는 request의 getCookies()메소드를 호출해 웹브라우저로부터 Cookie객체의 정보를 전달 받습니다.

순서2. 그리고 전달된 Cookie객체를 웹브라우저로 전달 해서 쿠키저장소에 저장할떄 사용한 이름인 "cookieTest"로 검색한  쿠키값 "JSP프로그래밍입니다." 을 가져 와서
      같은 클라이언트가 한번더 재접속 요청 했구나~~ 라고 판단 해서 기능을 구현합니다.
*/

//톰캣 서버 측의 서버페이지(GetCookieValue서블릿)

@WebServlet("/get")
public class GetCookieValue extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//재료
		//1. 요청한 데이터 한글처리
		request.setCharacterEncoding("UTF-8");
		//2. 브라우저로 응답할 메세지 유형 설정 및 한글 처리 설정
		response.setContentType("text/html; charset=UTF-8");
		//3. 브라우저에 응답할 출력스트림 통로 생성
		PrintWriter  out = response.getWriter();
		
		//실제작업 
		//1. HttpServletRequest 객체의 getCookies()메소드를 호출해 웹브라우저에게 new Cookie 객체들의 정보를 요청한 후 
		//   Cookie[] 배열에 담아 Cookie[] 배열 주소 자체를 반환 받습니다.
		//참고. 쿠키(new Cookie() 객체)가  하나도 없으면 빈 배열이 아니라 null 이 반환된다.
		Cookie[] allValue = request.getCookies();
		
		if(allValue == null) { //null 먼저 검사한다.(pro09 웹사이트프로젝트를 처음 요청한 클라이언트의 브라우저인지 검사)
			out.print("처음 접속요청했군요! 저장된 Cookie 객체가 없습니다.");
			return; //doGet 메소드 즉시 종료 
		}
		//2. Cookie[] 배열에 저장된 new Cookie("쿠키이름","쿠키값"); 객체를 반복해서 얻기 
		for(int i=0;   i<allValue.length;  i++) {
			
			//조건식 : 쿠키명 = "cookieTest"으로 저장된 new Cookie(); 객체이면?"
			if(allValue[i].getName().equals("cookieTest")) {
				
				//3. 재요청한 클라이언트의 브라우저로 응답
				out.print("웹브라우저 쿠키 저장소에서 요청할때 가져온 Cookie 객체 내부의 쿠키명 = cookieTest 이고,");
				out.print("함께 저장된 쿠키 값은? " +  URLDecoder.decode(allValue[i].getValue(),"UTF-8"));
			}
			
		}
		
		
		
		
	}

}









