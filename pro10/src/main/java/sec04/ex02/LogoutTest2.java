package sec04.ex02;

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


//사용자가  <a>링크를 클릭하여  로그아웃 요청을 하면 
//HttpSession 세션 객체를 TOMCAT 서버에서 제거 하고,
//ArrayList 배열에서  로그아웃 요청한 사용자의 ID 문자열객체를 삭제하여 로그아웃된 화면을 브라우저로 보여주는 서블릿 

@WebServlet("/logout")
public class LogoutTest2 extends HttpServlet {
	
	//웹 애플리케이션 전체 공유 메모리 영역인 ServletContext 객체 메모리 저장할 변수
	ServletContext context;
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//재료
		request.setCharacterEncoding("utf-8");
		response.setContentType("text/html; charset=utf-8");
		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();
		
		//웹 애플리케이션 전체 공유 메모리 영역인 ServletContext 객체 얻어 저장
		context = this.getServletContext();
		
		//현재 로그인 된 사용자(로그아웃 요청한 사용자)의 HttpSession객체 메모리를 TOMCAT 영역에서 얻기
		HttpSession  session = request.getSession();
		
		//클라이언트가 로그아웃 요청시  보낸 user_id 값을 HttpServletRequest 메모리에서 꺼내오기 
		//===> <a href='logout?user_id="+ user_id +"'>로그아웃</a>
		String user_id = request.getParameter("user_id");
		
		//현재 로그 아웃 요청한 사용자의 HttpSession 객체 메모리를 TOMCAT 메모리 전체영역에서 제거 
		//이유 : 로그아웃 처리 하기 위해
		session.invalidate();   // <== 이 시점에 LoginImple 이벤트 핸들러 객체의 sessionDestroyed 메소드 톰캣에 의해 자동호출
								// 코드실행 결과 :  로그인 한 사용자 수 1 감소  total_user--;
		
		//로그인한 사용자들의 ID 목록들이 저장된 ArrayList 배열을 ServletContext 공유 객체에서 가져와서 
		List<String> user_list = (ArrayList<String>)context.getAttribute("user_list");
		
		//현재 로그아웃 요청한 사용자의 ID 문자열객체를 ArrayList배열에서 삭제
		user_list.remove(user_id);
		
		//기존의 사용자 ID 문자열이 저장된 ArrayList 배열을 ServletContext 메모리 영역에서 제거 하고,
		//변경된 ~~ ArrayList 배열을 다시~~ ServletContext 메모리 영역에 저장(바인딩) 해서 업데이트 함
		context.removeAttribute("user_list"); //제거 하고
		context.setAttribute("user_list", user_list);// 저장(바인딩)
		
		//로그아웃 완료 메세지를 클라이언트의 웹브라우저로 응답(출력)
		out.println("<br> 로그아웃 했습니다.");
		
	}

}











