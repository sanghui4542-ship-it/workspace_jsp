package sec04.ex01;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/*
ShowMemberServlet 두번째 서블릿 클래스 역할

 - 로그인 된 회원의 정보를 조회해 보여주면서 로그인된 클라이언트가 보는 브라우저 화면을 띄워주는 클래스 

ShowMemberServlet 두번쨰 서블릿 클래스 작업
 
 작업1. 먼저 로그인된 화면을 보여주기 위해 기존 LoginServlet5 내부에서 만들었었던  HttpSession객체 메모리를 얻은 다음
       내부에 바인딩된 isLogOn key에 해당하는 true값을 가져와 로그인 된 상태의 화면을 보여줍니다.
           
  작업2. 그리고 isLogOn key와 같이 바인딩 했던 값이 true이면  바인딩된 입력한 아이디,비밀번호를 얻어 같이 브라우저 화면에 보여줍니다.
           
 작업3.  마지막으로  HttpSession객체 메모리가 존재하지 않거나 isLogOn key와 같이 바인딩 했던 값이 false이면 
        다시 로그인 요청할수 있도록 포워딩(재요청)해서 login5.html 화면을 브라우저 화면으로 보여 줍니다.
*/
@WebServlet("/show")
public class ShowMemberServlet extends HttpServlet {
	
	protected void doHandle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
		//순서1. 재료 준비
		//1. 서블릿이 요청 받은 데이터 중 한글문자 인코딩 방식 UTF-8 설정
		request.setCharacterEncoding("UTF-8");
		
		//2. 요청한 클라이언트의 브라우저에 응답할 데이터 유형을 설정하고 응답할 데이터 인코딩 방식 UTF-8설정
		response.setContentType("text/html; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		//2.1. 요청한 클라이언트의 브라우저 창과 연결된 데이터를 바이트 단위로 내보낼 출력스트림 PrintWriter생성
		PrintWriter out = response.getWriter();
		
		String id = "", pwd = "";     Boolean isLogon = false;
		
		//순서2. 
		/*
		  작업1. 먼저 로그인된 화면을 보여주기 위해 기존 LoginServlet5 내부에서 만들었었던  HttpSession객체 메모리를 얻은 다음
                내부에 바인딩된 isLogOn key에 해당하는 true 값을 가져와 로그인 상태의 화면을 보여줍니다.
                
         	session.setAttribute("isLogon", true); //로그인 인증할 값
	    	session.setAttribute("login.id", user_id);//입력한 아이디 
	    	session.setAttribute("login.pw", user_pw);//입력한 비밀번호 
        */
		HttpSession session = request.getSession(false);	
		
		if(session != null) {
			//로그인 인증시 처리 할 값 true 를 HttSession객체 메모리 내부에서 얻기
			isLogon = (Boolean)session.getAttribute("isLogon");
			
			/*작업2. 그리고 isLogOn key 와 같이 묶어서 바인딩 했던 값이 true 와 같다면? 
			 *      바인딩된 입력한 아이디, 비밀번호를 얻어 브라우저 와면에 보여줍니다.*/
			if(isLogon) {
				id = (String)session.getAttribute("login.id");
				pwd = (String)session.getAttribute("login.pw");
				
				//그런 후  로그인 요청한 클라이언트의 브라우저로  로그인된 사용자 화면을 보여줌(응답)
				out.print("<html>");
					out.print("<body>");
					out.print(id + "님 로그인 중입니다..... 환영합니다!");
					out.print("당신의 비밀번호는 ? " + pwd + " 입니다!");
					out.print("</body>");
				out.print("</html>"); 
			}else {
				//포워딩(재요청) - login5.html
				response.sendRedirect("login5.html");
			}
			
		/*  작업3.  마지막으로  HttpSession객체 메모리가 존재하지 않거나 isLogOn key 와 같이 바인딩 했던 값이 false 이면 
		           다시 로그인 요청할수 있도록 포워딩(재요청)해서 login5.html 화면을 브라우저 화면으로 보여 줍니다.  */			
		} else {
			//포워딩(재요청) - login5.html
			response.sendRedirect("login5.html");		
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doHandle(request, response);
	}
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doHandle(request, response);
	}
}
