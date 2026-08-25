package sec02.ex01;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
  URL ?  클라이언트가 서블릿을 요청하는 전체주소경로 
  
  요청하는 전체 URL    ->   http://localhost:8181/pro10/첫번째매핑주소/두번째매핑주소 
  실제요청하는 전체 URL ->   http://localhost:8181/pro10/first/test

  컨텍스트 주소 ? 클라이언트의 요청하는 전체 URL을 받았을때  톰캣서버가 pro10프로젝트에 접근할수 있는 주소 경로.
    		  -> /pro10
    		  -> server.xml 설정 파일에 가장 아랫쪽에 <Context path="/pro10" docBase="pro10"....  />
*/
//           /first/* 
//					=>  URL 전체 요청 주소 패턴은  첫번째매핑주소 /first 무조건 일치 해야 하나
//											  두번째미핑주소는  /뒤에 여러가지 주소를 작성하여 TestServlet2 서블릿을 요청할수 있다.

//http://localhost:8181/pro10/first/test
//http://localhost:8181/pro10/first/test2
//http://localhost:8181/pro10/first/test3
//........
@WebServlet("/first/*")
public class TestServlet2 extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//재료
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
	
 // 실제요청하는 전체 URL ->   http://localhost:8181/pro10/first/test  
	
	
		//실제 작업 : HttpServletRequest 객체의 메소드를 사용
		//1. 위 요청한 전체 URL 주소 중에서 컨텍스트 주소 /pro10 을 문자열 형태로 얻기
		String contextPath = request.getContextPath();
		System.out.println("HttpServletRequest의 getContextPath() 호출하면 얻는 컨텍스트 주소 = " + contextPath);
		
		//2. 위 요청한 전체 URL 주소 전체 얻기 
		String url = request.getRequestURL().toString();
		System.out.println("HttpServletRequest의 getRequestURL() 호출하면 얻는 요청한 전체 URL = " + url);
		
		//3. 위 요청한 전체 URL 주소 중에서  TestServlet1 서블릿 클래스를 요청할 "/first/test" 매핑 주소만  얻기 
		String mappings = request.getServletPath();
		System.out.println("HttpServletRequest의 getServletPath() 호출하면 얻는 서블릿 요청 매핑 주소 = " + mappings);
		
		//4. 위 요청한 전체 URL 주소 http://localhost:8181/pro10/first/test  중에서..........
		//   TestServlet1 서블릿을 요청한 URI 주소 "/pro10/first/test" 만 얻기 
		String uri = request.getRequestURI();
		System.out.println("HttpServletRequest의 getRequestURI() 호출하면 요청한 URI 주소 = " + uri);
		
		
	}

}









