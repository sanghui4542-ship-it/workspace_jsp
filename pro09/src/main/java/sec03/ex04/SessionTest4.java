package sec03.ex04;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/*
순서1. login4.html 에서 아이디, 비밀번호 입력후 로그인 버튼을 클릭해 아래의 서블릿으로 로그인 요청합니다.

순서2. doPost메소드가 호출되면서 매개변수로 request 를 받습니다. 
     request 에는 입력한 아이디, 비번이 저장되어 있음
     doPost메소드에서 doHandle메소드를 호출시 request 를 매개변수로 전달해서 사용합니다.
     
순서3. doHandle메소드 내부에서 로그인 처리 로직을 작성 합니다.

*/
@WebServlet("/login4")
public class SessionTest4 extends HttpServlet{
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doHandle(request, response);
	}	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doHandle(request, response);
	}		
	protected void doHandle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//재료
		//1. 서블릿이 요청 받은 데이터 중 한글문자 인코딩 방식 UTF-8 설정
		request.setCharacterEncoding("UTF-8");
		
		//2. 요청한 클라이언트의 브라우저에 응답할 데이터 유형을 설정하고 응답할 데이터 인코딩 방식 UTF-8설정
		response.setContentType("text/html; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		//2.1. 요청한 클라이언트의 브라우저 창과 연결된 데이터를 바이트 단위로 내보낼 출력스트림 PrintWriter생성
		PrintWriter out = response.getWriter();
		
		//실제 작업
		//1. 세션(HttpSession) 메모리 영역 새로 얻어 저장
		//방법  : HttpServletRequest객체의 getSession()메소드를 호출하면 HttpSession메모리를 만들어 반환 해준다.
		HttpSession session = request.getSession();
		
		//2. 클라이언트가  login4.html 화면에서 로그인 요청시 입력한 아이디, 비밀번호 (요청한 데이터들) 얻기
		String user_id = request.getParameter("user_id");
		String user_pw = request.getParameter("user_pw");
		
		//3. HttpSession 객체 메모리가 새로 생성된 메모리이면?(최초로 클라이언트가 처음 로그인 요청했을때 만들어지는 HttpSession 이라면?)
		if( session.isNew() ) {
			
			//만약 로그인 요청시 입력한 아이디가 있다면?(아이디 입력하고 로그인 요청 했다면?)
			if(user_id != null  &&  !user_id.trim().isEmpty()) {
				
				//HttpSession객체 메모리에 user_id변수의 값을 저장(바인딩)하고 로그인 상태를 확인하는 <a>링크를 표시
				session.setAttribute("user_id", user_id);
				out.print("<a href='login4'>로그인 상태 확인 요청</a>");
			
			}else {//만약 로그인 요청시~ 아이디 입력하지 않고 요청 했다면?
				
				out.print("<a href='login4.html'>다시 로그인 요청화면으로 가기</a>");
				session.invalidate(); //TomCat 서버메모리에 만약 HttpSession 객체 메모리가 존재하면 제거 				
			}	
			
		} else {// HttpSession객체 메모리가 이미 존재 하는 경우(사용자가 이미 로그인을 한후 "login4"로 인해 서블릿 페이지를 방문한 경우)
			
			//HttpSession 객체 메모리에 바인딩 된 "user_id" 키와 같이 저장된 로그인요청시 입력한 아이디(세션값)을 얻어 확입니다.
			user_id = (String)session.getAttribute("user_id");
			
			if(user_id != null && user_id.length() != 0) {
				
				out.print("안녕하세요 " + user_id + "님!!!  로그인 중...........");
			
			}else {
				out.print("<a href='login4.html'>다시 로그인 하러가기</a>");
				session.invalidate();
			}
		}
	}
}








