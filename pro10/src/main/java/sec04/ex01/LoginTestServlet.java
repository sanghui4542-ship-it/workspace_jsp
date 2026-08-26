package sec04.ex01;

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
import javax.servlet.http.HttpSession;


//loginTest.html 로그인 요청하는 화면에서 아이디, 비밀번호 입력후 로그인 요청하면 요청을 받아 처리 하는 서블릿 
//요청 URL :  http://localhost:8181/pro10/loginTest

@WebServlet("/loginTest")
public class LoginTestServlet extends HttpServlet {
	
	ServletContext context = null;
	List user_list = new ArrayList();
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//재료
		request.setCharacterEncoding("utf-8");
		response.setContentType("text/html; charset=utf-8");
		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();
		
		//로그인 접속 요청한 값 얻기
		String user_id = request.getParameter("user_id");
		String user_pw = request.getParameter("user_pw");
		
		//이벤트 핸들러 클래스의 객체 ?   이벤트가 발생하면 이벤트를 처리하는 코드가 작성된 클래스의 객체
		//-> 이벤트 핸들러(리스너) 역할을 하는  LoginImpl 클래스의 객체 생성시 로그인 요청시 입력받은 아이디, 비밀번호를 생성자로 전달해 저장시킴
		LoginImpl loginUser = new LoginImpl(user_id,  user_pw);
		
		//로그인 접속 요청 하나에 대한 HttpSession 객체 메모리 생성
		HttpSession session = request.getSession();
		
		//사용자 로그인 접속시 요청한 새로만든 HttpSession이면?
		if(session.isNew()) {
			//HttpSession 객체 메모리 영역에  이벤트 핸들러 역할을 하는 LoginImpl클래스의 객체 주소 바인딩
			session.setAttribute("loginUser", loginUser); //===> 바인딩 하는 순간! 
														  //     톰캣에 의해 LoginImple 객체 내부의 valueBound 메소드 자동호출
		}
		//브라우저에 현재 접속자 수 표시(클라이언트의 웹브라우저로 응답)
		out.print("<html>");
			out.print("<head>");
				out.print("<script type='text/javascript'>");
				//자바스크립트의  window 객체의 setTimeout() 메소드를 이용해  5초 간격으로 LoginTestServlet 서블릿 재요청하여
				// 현재 접속자 수를 표시하여 브라우저에 출력!
				out.print("window.setTimeout('history.go(0);',5000)");		
				out.print("</script>");
			out.print("</head>");
			out.print("<body>");
				//현재 실시간 총 접속자 수  브라우저로 보여주자(출력해 주자)
				out.print("접속한 사용자 아이디 : " + loginUser.user_id + "<br>");
				out.print("총 접속자 수  : " +  LoginImpl.total_user + "<br>");			
			out.print("</body>");
		out.print("</html>");
		
	}

}








