package sec01.ex01;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//예제 주제 : 웹페이지(서블릿페이지)를 연동(연결-재요청)하는 방법 중.... 
//		   <input type="hidden">(<hidden>태그)를 이용해
//		   웹 페이지들(login.html -> LoginServlet.class) 사이의 정보를 공유합니다.

//POST 요청 주소 : http://localhost:8181/pro09/login 

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
													throws ServletException, IOException {
		
    	//1. 서블릿이 요청받은 데이터들 중에서 한글 문자가 존재하면 한글깨져서 HttpServletRequest객체 메모리에서 얻어오기 떄문에
    	//	 미리 HttpServletRequest객체 메모리에 한글 문자를 처리할수 있는 방식(인코딩 방식)을 UTF-8로 설정 한다
    	//요약 : 한글처리
		request.setCharacterEncoding("UTF-8");
		/*
		  +--------------------------+
          | HttpServletRequest #100  |
          |  user_id      -> hong    |
          |  user_pw      -> 1234    |
          |  user_address -> 서울..  |
          |  user_email   -> test..  |
          |  user_hp      -> 010..   |
          +--------------------------+
		*/
		//2. login.html 에서 LoginServlet 서블릿페이지로 요청한 데이터들 HttpServletRequest 객체 메모리 내부에서 얻기
		//요약 : 클라이언트가 요청한 데이터들 얻기 
		String user_id = request.getParameter("user_id"); //입력한 아이디를 문자열로 얻기
		String user_pw = request.getParameter("user_pw"); //입력한 비밀번호를 문자열로 얻기 		
		
		String user_address = request.getParameter("user_address");
		String user_email = request.getParameter("user_email");
		String user_hp    = request.getParameter("user_hp");
		//<input type="hidden"> 태그들에 작성해서 요청했던 주소, 이메일, 전화번호 도 문자열로 얻기
		
		//3. login.html 디자인 화면을 보고 요청했던 클라이언트의 웹브라우저로 응답할 메세지를 만들어서 응답.
		
		//3.1. 응답할 메세지 만들기
    	String data = "안녕하세요!<br> 로그인하셨습니다.<br><br>";
			   data += "입력한 아이디 : " + user_id + "<br>";
			   data += "입력한 비밀번호 : " + user_pw + "<br>";
			   data += "주소 : " + user_address + "<br>";
			   data += "이메일 : " + user_email + "<br>";
			   data += "휴대전화 : " + user_hp + "<br>";
			   
		//3.2.응답할 메세지 유형을 HttpServletResponse객체 메모리에 설정
    	response.setContentType("text/html; charset=UTF-8");
	    	
    	//3.3.출력스트림 통로 PrintWrier객체를 생성해 응답할 메세지를 브라우저로 보내어서 출력
    	response.getWriter().print(data);
	
	}
	
	

}






