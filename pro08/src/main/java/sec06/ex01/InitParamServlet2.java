package sec06.ex01;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet(
		urlPatterns = { 
				"/sInit4", 
				"/sInit3"
		}, 
		initParams = { 
				@WebInitParam(name = "email", value = "admin@test.com"), 
				@WebInitParam(name = "tel", value = "010-1234-5678")
		})
public class InitParamServlet2 extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//1. 요청한 클라이언트의 브라우저에 응답할 데이터 유형(MIME-TYPE) 설정  및 인코딩 방식 설정
		response.setContentType("text/html; charset=utf-8");
		
		//2. 요청한 클라이언트의 브라우저와 연결된 데이터를 내보낼 출력스트림 통로 얻기
		PrintWriter out = response.getWriter();
		
		//3. InitParamServlet 서블릿 클래스 내부에서 사용할 변수들의 값을 ServletConfig 객체 메모리 내부에서 얻어 저장
		//방법 -> getInitParameter("변수명"); 메소드를 이용하여  변수에 저장된 값을 얻어 옵니다.
		String email = this.getInitParameter("email");
		//     email = "admin@test.com"
		
		String tel  = this.getInitParameter("tel");
		//	   tel  = "010-1234-5678"
		
		//4. 요청한 클라이언트의 브라우저 화면으로 응답
		out.print("email : " + email + "<br>");
		out.print("tel : " + tel  + "<br>");
		
	}

}
