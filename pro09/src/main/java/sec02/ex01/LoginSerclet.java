package sec02.ex01;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/login3")
public class LoginSerclet extends HttpServlet {
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//재료
		//1. 요청한 데이터 한글처리
		request.setCharacterEncoding("UTF-8");
		//2. 브라우저로 응답할 메세지 유형 설정 및 한글 처리 설정
		response.setContentType("text/html; charset=UTF-8");
		//3. 브라우저에 응답할 출력스트림 통로 생성
		PrintWriter  out = response.getWriter();
		
		String user_id = request.getParameter("user_id");
		String user_pw = request.getParameter("user_pw");
		
		if("admin".equals(user_id) && "1234".equals(user_pw)) {
			
			Cookie userCookie = new Cookie("user_id", user_id);
			
			
			
			
		}
		
		
		
	}

}
