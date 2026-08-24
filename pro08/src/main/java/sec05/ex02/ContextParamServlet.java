package sec05.ex02;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
ContextParamServlet 서블릿 클래스 내부 코드의 실행 흐름

순서1.  클라이언트 브라우저 요청 http://localhost:8181/pro08/initMenu

순서2. this.getServletContext()메소드를 이용해 변수들이 저장된 ServletContext객체 메모리 주소를 얻어 
	   접근하고  그리고 getInitParameter()메소드의 매개변수로 초기화 파라미터이름을 전달한후 
	   메뉴 텍스트 항목을 가져와 웹브라우저로 출력합니다. 
*/
@WebServlet("/initMenu")
public class ContextParamServlet extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//재료 
		//1. 웹브라우저로 응답할 MIME-TYPE(데이터 유형)을 text/html; 로 설정하고
		//   문자 처리방식 utf-8방식의 값으로 처리 하겠다고!  HttpServletResponse 객체에 설정
		response.setContentType("text/html; charset=utf-8");
		
		//2. 요청한 클라이언트의 브라우저와 연결된 출력스트림 생성
		PrintWriter  out = response.getWriter();
		
		//실제작업
		//1. 클라이언트가 이 서블릿을 요청하면 톰캣서버가 웹프로젝트 하나의 ServletContext 공유 객체 메모리가 생성됩니다.
		//   ContextParamServlet 서블릿 클래스 내부의 코드에서  
		//   ==> 이 공유 객체 메모리에 접근해서 사용하기 위해 ServletContext 공유 객체 메모리 얻기
		ServletContext servletContext = this.getServletContext();
		
		//2. web.xml 에 설정된 전역 변수의 값들을 ServletContext객체 메모리 내부에서 얻기 
		String menu_member = servletContext.getInitParameter("menu_member");
		//"회원등록 회원조회 회원수정"
		
		String menu_order = servletContext.getInitParameter("menu_order");
		//"주문조회 주문등록 주문수정 주문취소"
		
		String menu_goods = servletContext.getInitParameter("menu_goods");
		//"상품조회 상품등록 상품수정 상품삭제"
		
		//3. 요청한 클라이언트의 웹브라우저로 PrintWriter 출력 스트림 통로를 통해 응답메세지를 내보내어 출력(응답)
		out.print("<html><body>");
		out.print("<table border='1' cellspacing='0'>");
		out.print("<tr><td>메뉴명</td></tr>");
		out.print("<tr><td>" + menu_member  + "</td></tr>");
		out.print("<tr><td>" + menu_order  + "</td></tr>");
		out.print("<tr><td>" + menu_goods + "</td></tr>");
		out.print("</table>");
		out.print("</body></html>");
		
	}

}


















