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


//loginTest2.html 로그인 요청하는 화면에서 아이디, 비밀번호 입력후 로그인 요청하면 요청을 받아 처리 하는 서블릿 
//요청 URL :  http://localhost:8181/pro10/loginTest2

@WebServlet("/loginTest2")
public class LoginTestServlet2 extends HttpServlet {
	
	ServletContext context = null; 	//웹 애플리케이션(prp10)의 정보를 저장하는 공유객체(서블릿 페이지간의 값을 공유 할수 있음)

	List<String> user_list = new ArrayList<>(); // 로그인한 사용자들의 ID를 저장하는 리스트(모든 사용자의 로그인 ID 정보를 저장)
	
	//loginTest2.html 에서 로그인 요청하면 실행되는 콜백메소드(POST 로그인 요청 받아 처리)
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//재료
		request.setCharacterEncoding("utf-8");
		response.setContentType("text/html; charset=utf-8");
		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();	
		context = this.getServletContext(); //현재 실행 중인 웹 애플리케이션의 ServletContext객체를 가져옴 (톰캣이 미리 생성 해놓음)

	
		//로그인 접속 요청한 값 얻기
		String user_id = request.getParameter("user_id");
		String user_pw = request.getParameter("user_pw");
		
		//이벤트 핸들러 클래스의 객체 ?   이벤트가 발생하면 이벤트를 처리하는 코드가 작성된 클래스의 객체
		//-> 이벤트 핸들러(리스너) 역할을 하는  LoginImpl 클래스의 객체 생성시 로그인 요청시 입력받은 아이디, 비밀번호를 생성자로 전달해 저장시킴
		LoginImpl loginUser = new LoginImpl(user_id,  user_pw);
		
		//로그인 접속 요청 하나에 대한 HttpSession 객체 메모리 생성 (만약 존재 하지 않으면 새로운 HttpSession 객체 생성)
		HttpSession session = request.getSession(); //<====  이 시점에 LoginImpl객체의 sessionCreated 콜백 메소드 자동 호출됨
		
		//사용자 로그인 접속시 요청한 새로만든 HttpSession이면?
		if(session.isNew()) {
			//HttpSession 객체 메모리 영역에  이벤트 핸들러 역할을 하는 LoginImpl클래스의 객체 주소 바인딩
			session.setAttribute("loginUser", loginUser); 
			
			//로그인한 사용자의 ID(문자열 객체)를 리스트에 추가(로그인 한 사용자 관리 하기 위해)
			user_list.add(user_id);
			
			//ServletContext 공유 객체 메모리에 접속한 사용자 ID가 저장된 ArrayList 배열 바인딩 
			context.setAttribute("user_list", user_list); //<=== ("user_list 키", ArrayList 배열 값) 묶어서 저장
			
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
				out.print("총 접속자 수  : " +  LoginImpl.total_user + "명<br><br>");
								
				out.print("접속 아이디 목록 : <br>");
				//현재 로그인 사용자 아이디 목록 출력
				//=> ServletContext 공유 객체 메모리영역에 바인딩된 ArrayList 배열 주소를 값으로 얻는다
				List<String> list = (ArrayList<String>)context.getAttribute("user_list");					
				for(String id  : list ) {
					out.print(id  +  "<br>");  //로그인 한 사용자 ID를 반복해서 출력 
				}
				
				//로그 아웃 요청 링크 제공( 클릭 하면 로그아웃 요청을 LogoutTest2.java 서블릿으로 합니다. )
				out.print("<a href='logout?user_id="+ user_id +"'>로그아웃</a>");
				
			out.print("</body>");
		out.print("</html>");
		
	}

}








