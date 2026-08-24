package sec04.ex02;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/*
첫번째 서블릿(톰캣 서버가 실행하는 서버페이지)

참고. 클라이언트가 웹브라우저 주소창에 첫번째 서블릿FistServlet을 요청할 요청 주소를 입력해서 요청한다.
     요청할 주소 -> http://localhost:8181/pro08/first
		
		요청할 주소로 첫번쨰 서블릿인 FirstServlet을 요청하면 -> 
		
		주소요청정보에 관한 HTTP요청메세지를 브라우저가 만들어 보관한다. ->
		
	    브라우저는 만든 HTTP요청메세지를 TOMCAT 서버에 전달해서 첫번째 서블릿인 FirstServlet을 최종 요청하게 된다. ->
	       
	    TOMCAT 서버는 HTTP요청메세지 정보를 보고 HttpServletRequest객체 메모리를 생성해서 HTTP요청메세지의 정보를 저장 하게 됩니다. ->
	    
	    FirstServlet클래스의 doGet메소드의 매개변수 HttpServletReqeust request로  HttpServletRequest객체를 전달 받아 사용 합니다.

주제 :    첫번쨰 서블릿에서 HttpServletRequest객체 메모리에 바인딩 한 후  두번째 서블릿을  RequestDispatcher객체 방법으로 포워딩(재요청)하면 
         첫번째 서블릿에서 얻은 HttpServletRequest객체 메모리를 두번쨰 서블릿에서 공유받아 사용할수 있다!!!                	
*/
@WebServlet("/first")
public class FirstServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		 //순서1. 두 서블릿 서버페이지간의 재요청(포워딩)을 할 경우!  첫번째 서블릿이 요청한 문자데이터를 공유해야 하므로 
		 //       재요청 받은 두번쨰 서블릿에서 요청해온 데이터를 공유받을때 한글문자가 깨지지 않고 공유받아 웹브라우저로 출력할수 있게
		 //       문자 인코딩 방식을 UTF-8로 설정
		 request.setCharacterEncoding("UTF-8");
	 
		 //순서2. 웹브라우저 주소창에 /first 라고 입력해서 첫번쨰 서블릿을 요청하면
		 //		 클라이언트의 요청 하나당? 톰캣서버는 HttpServletRequest객체 메모리를 생성해 주고  doGet메소드의 매개변수 request로 전달하므로
		 //     HttpServletRequest객체 메모리에 개발자가 직접 setAttribute("키","값"); 사용하여 바인딩 하자.
		 request.setAttribute("address", "서울시 성북구");  // <= "키"-"값" 한쌍으로 묶어서 바인딩
		 						  //키 ,        값
	
		 //순서3.  재요청(포워딩) 기술 몇가지? 4가지~ 중에서 RequestDispatcher 객체의 forward() 메소드 호출 방법 사용하자.
		 // 웹브라우저 주소창을 거치는 것이 아니라  톰캣서버에 의해 곧바로 SecondServlet 두번째 서블릿 페이지를 포워딩(재요청)함.
		 request.getRequestDispatcher("second").forward(request, response);
	 
		 
	}
	
	
}







