package sec04.ex01;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
   두번째 서블릿 SecondServlet 클래스 
   -> 첫번째 서블릿 FirstServlet으로 부터 포워딩(재요청)되어 요청받는 두번째 서블릿 SecondServlet 입니다.
   -> 포워딩(재요청) 받는 주소 : http://localhost:8181/pro08/second?name=lee
*/

//@WebServlet("/second")
public class SecondServlet extends HttpServlet {

	//alt + shift + s  누른 후 v  <--- 메소드 오버라이딩 
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
						throws ServletException, IOException {
		//1. 포워딩(재요청)한 클라이언트의  웹브라우저 창으로 응답할 데이터 유형(MIME-TYPE)을 
		//   텍스트기반의 HTML파일에 작성하는 코드로 설정하고, 문자 처리방식을 UTF-8 로 설정
		response.setContentType("text/HTML; charset=UTF-8");
		
		//1.1. 첫번쨰 서블릿 FirstServlet 클래스 내부에서 작성한
		//     request.setAttribute("address", "서울시 성북구"); 코드에 의해 바인딩된 정보를  한번 꺼내와 공유해서 사용해보자!
		//결론 : 공유 안됨
		String address = (String)request.getAttribute("address");
		
		
		//2. 요청한 클라이언트의 브라우저와 연결된 데이터를 내보내어 전송할 출력스트림 통로 PrintWriter객체 얻기
		PrintWriter out = response.getWriter();
		
		//3. 현재 두번째 서블릿인 SecondServlet 내부의 코드에서 응답할 메세지를 생성하고 브라우저로 응답(출력)
		out.print("<html><body>");
		out.print("FirstServlet 클래스에서 공유받아 출력 하는 값 : " + address); //null 로 출력됨 
		out.print("</body></html>");
		
	}
	
	

}







