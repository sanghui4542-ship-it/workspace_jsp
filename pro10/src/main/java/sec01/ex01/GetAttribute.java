package sec01.ex01;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;




//GET 요청 주소  :  http://localhost:8181/pro10/get

@WebServlet("/get")
public class GetAttribute extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//재료
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		
		//1. ServletContext 객체 메모리(웹프로젝트 하나당 생성되는 하나의 공유 메모리) 얻기
		//얻는 이유 : 바인딩한 자원을 공유 받아 사용하기 위해
		ServletContext servletContext = this.getServletContext();
		
		//SetAttribute.java 파일의 서블릿 코드에서 setAttribute("context",ctxMesg); 바인딩 했던  
		//ctxMesg변수에 저장된 문자열 값 얻기
		//방법  : getAttribute("키"); ->"값" 을 반환
		String ctxMesg = (String)servletContext.getAttribute("context");		
		out.print("ServletContext 서블릿 관련 객체 메모리 영역에서 꺼내와 공유받은 값 : " + ctxMesg + "<br>");
		
		
		//2. HttpSession 객체메모리(요청한 클라이언트 브라우저창 하나당 생성되는 하나의 공유메모리) 얻기 
		HttpSession httpSession = request.getSession();
		
		//SetAttribute.java 파일의 서블릿 코드에서 setAttribute("session", sesMesg); 바인딩 했던  
		//sesMesg변수에 저장된 문자열 얻기 
		//방법 : getAttribute("키"); -> "값"을 HttpSession객체 메모리에서 반환 
		String sesMesg = (String)httpSession.getAttribute("session");
		out.print("HttpSession 서블릿 관련 객체 메모리 영역에서 꺼내와 공유받은 값 : " + sesMesg + "<br>");
		
		
		//3. HttpServletRquest 객체메모리(클라이언트가 서블릿을 톰캣에 요청하는 순간 톰캣이 생성해주는 하나의 공유메모리) 얻기
		//얻는 방법 : doGet 메소드의 매개변수 request 로 전달 받습니다.
		
		//SetAttribute.java 파일의 서블릿 코드에서 setAttribute("request",reqMesg); 바인딩 했던 
		//reqMesg변수에 저장된 문자열을 HttpServletRequest에서 얻기
		//방법 : getAttribute("키"); -> "값"을 HttpServletRequest 객체 메모리에서 반환
		String reqMesg = (String)request.getAttribute("request");
		out.print("HttpServletRequest 서블릿 관련 객체 메모리 영역에서 꺼내와 공유받은 값 : " + reqMesg + "<br>");
	
	}
	
	

}























