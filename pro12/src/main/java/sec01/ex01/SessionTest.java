package sec01.ex01;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/*
클라이언트가 최초 웹브라우저로 요청하면 요청을 받아 처리하는 서블릿 (SessionTest)

하는일  : 요청한 클라이언트 한명에 관한 하나의 HttpSession내장객체 메모리를 만들어 특정 데이터를 바인딩 시킨다.
        그리고 작성 해놓은 <a>링크를 클릭하면  session1.jsp 서버페이지를 요청해서 
        HttpSession내장객체 메모리를 공유 합니다.
*/
@WebServlet("/sess") //  http://localhost:8181/pro12/sess  요청 URL 받으면 실행되는 서블릿
public class SessionTest extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//1. HttpSession 내장 객체 메모리 새로 생성해서 저장
		HttpSession session = request.getSession();
		
		//2. HttpSession 내장 객체 메모리에  키,값 한쌍의 형태로 묶어서 저장(바인딩)
		session.setAttribute("name", "이순신");
		
		//3. <a> 링크 태그를 만들어 웹브라우저 화면에 보여주고, 클라이언트에게 클릭을 유도하여 session1.jsp 서버페이지에 요청합니다.
		//  이유 : SessionTest 서블릿이 클라이언트 브라우저로 부터 처음 요청 받았을때 만들어지는 HttpSession 객체 메모리 공유하기 위해
		
		//3.1. 클라이언트의 웹브라우저로 응답할 데이터유형(MIME-TYPE)을 text/html;로 설정하고
		//	   응답할 데이터에 한글 문자 인코딩 방식의값 UTF-8로 설정
		response.setContentType("text/html; charset=UTF-8");
		
		//3.2. 클라이언트의 웹브라우저와 연결된 출력 스트림 PrintWriter 생성
		PrintWriter out = response.getWriter();
		
		out.print("<html>");
		
			out.print("<body>");
		
				out.print("<h1>HttpSession 내장객체 메모리 내부에 'name'-'이순신' 을 바인딩 했습니다.</h1>");
		
				out.print("<a href='/pro12/test01/session1.jsp'>session1.jsp를 요청하여 보여줘!</a>");
				
			out.print("</body>");
			
		out.print("</html>");
		
		
		
	}

}











