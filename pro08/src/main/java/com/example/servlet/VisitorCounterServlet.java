package com.example.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
웹사이트(웹프로젝트 내부의 서블릿페이지)에 방문하는 방문자 수를 카운트 하는 기능의 서블릿 을 만들자.
	
	- ServletContext 공유 객체 메모리를 활용하여 웹프로젝트 전역에서 방문자수를 관리함
	
	- 클라이언트가 브라우저 주소창에 "http://localhost:8181/pro08/visitorCounter"를 입력하여 
	  VisitorCounterServlet 서블릿 페이지에 요청을 보내면  방문자수를 증가시키고 브라우저로 출력하여 응답함.
	
	- 방문자 수는 ServletContext 공유 객체메모리에 저장되므로, 톰캣서버가 종료되지 않는 한 방문자수는 유지됨.
*/

@WebServlet("/visitorCounter")
public class VisitorCounterServlet extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//1. ServletContext 공유 객체 메모리 얻기 
		ServletContext servletContext = this.getServletContext();
		
		//2. ServletContext 공유 객체 에 저장된 방문자수 가져오기
		//   - getAttribute("visitCount"); 를 사용하여 현재 저장된 방문자수를 가져옴
		//   - 만약 방문자 수 데이터가 없으면(null), 방문한 사람이 없기때문에 처음 방문자수를 0으로 설정하여 시작하도록 함
		Integer visitCount = (Integer)servletContext.getAttribute("visitCount");
		
		if(visitCount == null) { //이 VisitorCounterServlet 서블릿 사이트에 방문요청한 클라이언트가 없을 경우(즉, 첫번쨰 방문한 경우)
			visitCount = 1; //처음 방문한 방문자수를 1로 저장해서 사용하자.
		}else {
			visitCount++;   //기존 누적된 방문자 수에 1을 누적하여 방문할때 마다 증가 시키자.
		}
		
		//3. 방문자 수를 ServletContext 공유 객체 메모리 내부에 저장(바인딩)
		//- setAttribute("visitCount", visitCount); 를 사용하여 방문자 수를 업데이트 함.
		//- 이 방문자 수 데이터는 모든 클라이언트의 브라우저에서 공유하므로, VisitorCounterServlet 서블릿 사이트로 새로 방문할떄 마다 방문자 수가 증가함.  
		servletContext.setAttribute("visitCount", visitCount);
		
		//4. 톰캣 서버가 관리하는 ServletContext 공유 객체 메모리에 방문자수를 얻어 기록 후  출력
		// -  ServletContext에서 제공해주는 log() 사용하여 기록후 출력할수 있다.
		//-   개발자가 방문자수를 확인하거나 디버깅(테스트)할떄 유용하게 사용할수 있는 log()메소드 이다.
		servletContext.log("현재 방문한 방문자수 : " + visitCount); //이클립스의 콘솔에 출력됨
		
		//5. VisitorCounterServlet 서블릿 페이지가 ~~ 요청한 클라이언트의 웹브라우저로 응답해서 현재 방문자수를 출력(응답)해서 보여주자
		//순서1. 웹브라우저로 응답할 데이터 유형을 설정하고, 응답할 한글 문자 처리 방식의 값을 utf-8로 설정
		response.setContentType("text/html; charset=utf-8");
		
		//순서2. 웹브라우저와 연결된 출력스트림 통로 얻기 
		PrintWriter out = response.getWriter();
		
		//순서3. 웹브라우저로 응답(출력)
		out.print("<html>");
		out.print("<head><title>방문자수 표시 </title></head>");
		out.print("<body>");
		out.print("<h2> VisitorCounterServlet 사이트 방문자 수 </h2>");
		out.print("<p><strong>현재 방문자 수  :</strong>"  +  visitCount  + "</p>");
		out.print("</body>");
		out.print("</html>");		
		
		
	}

}











