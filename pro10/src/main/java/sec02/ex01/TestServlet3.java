package sec02.ex01;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 요청하는 전체 주소 URL : http://localhost:8181/pro10/first/test 
 
 컨텍스트 주소 ? 톰캣 서버가  요청하는 전체 주소 URL를 받았을때 톰캣서버가  pro10프로젝트에 접근할수 있는 주소경로.
            ->    /pro10
			->   server.xml 설정 파일에  <Context path="/pro10" ..../>

*/


// 컨텍스트 주소 /pro10/ 뒤에 작성하는 모든 요청하는 매핑 주소에 대해  현재 서블릿을 요청할 수있게 전체 URL작성.
//@WebServlet("/*")

	//예 : 게시판 서비스를 하는 서블릿의 매핑주소
	//http://localhost:8181/pro10/board/wirter <-글쓰기 요청
	//http://localhost:8181/pro10/board/read   <-글하나 조회 요청 
	//http://localhost:8181/pro10/board/update <-글하나 수정 요청
	//http://localhost:8181/pro10/board/delete <-글하나 삭제 요청


//요청하는 전체 URL주소에서 가장 뒷주소의 확장자가 .do로 끝나는 전체 URL요청 주소를 작성해서 요청하면 현재 서블릿을 요청할 수 있게 URL 작성

	//예  :  http://localhost:8181/pro10/index.do
	//      http://localhost:8181/pro10/index2.do
	//      http://localhost:8181/pro10/index3.do
@WebServlet("*.do")
public class TestServlet3 extends HttpServlet{
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//재료
		request.setCharacterEncoding("utf-8");
		response.setContentType("text/html; charset=utf-8");
		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();
		
		//실제 작업 : HttpServletRequest 객체의 메소드들  사용
		//1. 위 요청한 전체 URL 주소  http://localhost:8090/pro10/first/test 중에서  컨텍스트 주소 /pro10 만 얻기
		String contextPath = request.getContextPath();
		System.out.println("HttpServletRequest의  getContextPath()메소드를 호출하면 컨텍스트 주소 /pro10 을 얻을수 있다. " + contextPath);

		//2. 위 요청한 전체 URL 주소  http://localhost:8090/pro10/first/test 얻기 
		String url = request.getRequestURL().toString();
		System.out.println("HttpServletRequest의  getRequestURL()메소드를 호출하면 요청한 전체 URL을 얻을 수 있다. " + url);
		
		//3. 위 요청한 전체 URL 주소 http://localhost:8090/pro10/first/test 중에서  서블릿 페이지를 요청할 매핑 주소 /first/test 만 얻기
		String mappings = request.getServletPath();
		System.out.println("HttpServletRequest의 getServletPath()메소드를 호출하면 요청한 매핑 주소를 얻을 수 있다." + mappings);
		
		//4. 위 요청한 전체 URL 주소 http://localhost:8090/pro10/first/test 중에서 요청한 URI주소만 얻기
		String uri = request.getRequestURI();
		System.out.println("HttpServletRequest의 getRequestURI()메소드를 호출하면 요청한 URI주소 /pro10/first/test를 얻을수 있다. "  +   uri);
		
		
		
	}
	

}













