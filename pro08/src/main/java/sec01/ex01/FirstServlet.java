package sec01.ex01;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
첫번째 서블릿(톰캣 서버가 실행하는 서버페이지)

순서1. 클라이언트가 웹브라우저 주소창에 첫번째 서블릿FistServlet을 요청할 요청 주소를 입력해서 요청한다.
      요청할 주소 -> http://localhost:8181/pro08/first
           
순서2. FirstServlet 클래스 내부의 doGet메소드 재구현 코드를 작성하는데
      두번째 서블릿인 SecondServlet을 포워딩(재요청기술)할 코드를 작성 함.            
		예)  response.sendRedirect("두번째 서블릿인 SecondServlet을 요청할 매핑주소");
*/
//@WebServlet("/first")
public class FirstServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/*
		   포워딩 :  다른 서블릿인 서버페이지를 재요청 하는 기술.
		   
		   포워딩 방법1.  Redirect 방법
		   -> HttpServletResponse 객체의 sendRedirect(); 메소드를 호출해서 포워딩(재요청) 하는 방법
		   -> 첫 번째 서블릿을 처음 요청했지만 첫번째 서블릿 클래스 내부에서 두번째 서블릿을 재요청할떄~
		     웹브라우저 주소창에~~~ 두번째 서블릿의 포워딩(재요청) 주소가 자동으로 작성되면서 엔터 까지 자동으로 눌러져서 
		     재요청이 이루어 지는 방법입니다.
		   -> 작성방법
		     			response.sendRedirect("재요청할 두번째 서블릿의 요청 매핑주소");
		 */
		 //Redirect 방법으로 두번째 SendServlet을 포워딩(재요청) 하는 코드 작성
		 response.sendRedirect("second");
	
	}
	
	
}







