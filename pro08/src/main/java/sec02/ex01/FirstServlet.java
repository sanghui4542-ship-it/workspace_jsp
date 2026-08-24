package sec02.ex01;

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

순서1. 클라이언트가 웹브라우저 주소창에 첫번째 서블릿FistServlet을 요청할 요청 주소를 입력해서 요청한다.
      요청할 주소 -> http://localhost:8181/pro08/first
           
순서2. FirstServlet 클래스 내부의 doGet메소드 재구현 코드를 작성하는데
      두번째 서블릿인 SecondServlet을 포워딩(재요청기술)할 코드를 작성 함.  
                     	
*/
//@WebServlet("/first")
public class FirstServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/*
		포워딩 : 다른 서블릿인 서버페이지를 재요청 하는 기술.
		
		포워딩 방법4. RequestDispatcher 객체의 forward 메소드를 이용해서 포워딩(재요청) 하는 방법
				    	
		->  첫번째 서블릿을 처음 요청했지만 첫번째 서블릿 클래스 내부 코드에서 두번째 서블릿을 재요청할때 
		    클라이언트가  첫번째 서블릿을 처음 요청했던 주소 /first 가 웹브라우저 주소창에 그대로 유지 하게 되지만
		    포워딩(재요청)당한 두번째 서블릿이 응답한 결과가 보여지는 포워딩(재요청) 방법 입니다.
		    		        	
		-> 작성 방법
					//재요청할 두번쨰 서블릿의 매핑주소가 저장된? RequestDispatcher객체 주소를 얻어 저장  
					RequestDispatcher  dispatcher = request.getRequestDispatcher("재요청할 두번째 서블릿의 매핑주소    또는    재요청할 .jsp의 주소");
					
					//두번째 서블릿를 재요청 시~  
					//doGet메소드의 매개변수로 전달받은 
					///HttpServletRequest객체 메모리와 HttpServletResponse객체 주소 전달해서 공유할수 있음
					dispatcher.forward(request,  response);
				
		 */
		
		//해설1.  1. getRequestDispatcher 메소드를 호출하면 재요청할 두번째 서블릿 매핑주소 second를 
		//          RequestDispatcher 객체를 생성해서 저장시킵니다. 
		//		 2. RequestDispatcher 객체 주소를 getRequestDispatcher 메소드가 반환 해줍니다.
		RequestDispatcher dispatcher = request.getRequestDispatcher("second?name=lee");
		
		//해설2. 첫번째 서블릿 FirstServlet이 클라이언트의 브라우저로 부터 처음 요청 받았을때의 데이터가 보관된 HttpServletRequest 객체메모리와
		//		클라이언트의 브라우저로 응답할 메세지가 설정될 HttpServletResponse 객체 메모리 주소를 ~~~
		//		forward 메소드 호출시  매개변수로 각각 전달하여  두번째 서블릿 SecondServlet 페이지로 톰캣이 포워딩(재요청) 시 공유 합니다.
						  dispatcher.forward(request, response);
		
		
	
	}
	
	
}







