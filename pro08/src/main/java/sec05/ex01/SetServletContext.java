package sec05.ex01;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/* 
  주제 : ServletContext 서블릿 관련 객체 메모리에 바인딩 실습
  		(실습에 사용할 파일 2개 - SetServletContext.java, GetServletContext.java)


실습순서
		순서1. 클라이언트가 크롬 웹브라우저를 이용해 아래의  SetServletContext 서블릿 클래스를 톰캣 서버에 요청합니다.
		      클라이언트가 요청한 URL -> http://localhost:8181/pro08/cset

		순서2. 개발자가 SetServletContext 서블릿 클래스 내부에  
		      톰캣 서버가 pro08 웹프로젝트 하나당 생성 해주는 ServletContext객체를 메모리를 얻기 위해 
		      getServletContext() 메소드를 호출해서 얻은 다음~~~
		      ArrayList 배열을 생성해서 이름, 나이 를 저장한  후 ~~~~
		      ArrayList 배열 메모리 주소 자체를? 얻은 ServletContext객체 메모리에 바인딩 합니다.
*/

@WebServlet("/cset")
public class SetServletContext extends HttpServlet{
	
		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			//재료
			//1.  요청한 클라이언트의 브라우저로 응답할 데이터 유형 설정 및 한글처리
			response.setContentType("text/html; charset=utf-8");
			
			//2.  요청한 클라이언트의 브라우저와 연결된 출력스트림 통로 PrintWriter 얻기
			PrintWriter  out = response.getWriter();
			
			//본격적으로 작업
			//1. 톰캣 (웹 애플리케이션) 서버가  생성 해 놓은 ServletContext 객체 메모리 주소를 얻어 저장
			//설명 : 현재 개발자가 작성하고 있는 SetServletContext class가 속한 웹프로젝트(pro08) 전체에서
			//      데이터를 공유할 공간 메모리인? ServletContext 객체 메모리를 톰캣으로 부터 얻어 저장
			ServletContext servletContext = this.getServletContext();
			//참고. ServletContext 객체 메모리는  
			//     웹프로젝트(pro08) 전체 에서 공유되는 데이터들을 저장(key-value 한쌍 묶어 바인딩) 해 놓고
			//     모든 서버페이지(서블릿 또는 jsp)에서 공유하는 메모리 입니다.
			
			//2. ArrayList 배열 (ServletContext 객체 메모리 내부에 바인딩할 자원)을 생성해서 "이순신" 과 30을 저장해 놓자.
			List  list = new ArrayList();   
			list.add("이순신");  list.add(30);
			
			//3. ArrayList 배열 메모리 주소번지를? ServletContext객체 메모리 내부에 바인딩 하자.
			servletContext.setAttribute("member", list);   
			//							  key   ,  value
			// ServletContext 객체 메모리 내부에 바인딩 하면 좋은점?  
			//  - pro08 웹프로젝트내부에 만들어져 있는 모든 서블릿 클래스에서 이 ArrayList배열을 공유해서 사용할수 있다.
		
			out.print("이순신 과 30을 ArrayList배열에 저장 하고, ArrayList배열 자체를? ServletContext객체 메모리 내부에 바인딩 했음");
			
			
			
			
		}

}




















