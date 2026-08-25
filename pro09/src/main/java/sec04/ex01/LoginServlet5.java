package sec04.ex01;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


//login5.html 화면에서 클라이언트가 아이디,비밀번호 입력후 로그인 요청 을 할때 로그인서비스를 하는 서블릿 클래스 
//-> http://localhost:8181/pro09/login5  요청 주소에 의해 post 전송 요청 방식으로 로그인 요청을 받았습니다.

@WebServlet("/login5")
public class LoginServlet5 extends HttpServlet {
	
	protected void doHandle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
		//순서1. 재료 준비
		//1. 서블릿이 요청 받은 데이터 중 한글문자 인코딩 방식 UTF-8 설정
		request.setCharacterEncoding("UTF-8");
		
		//2. 요청한 클라이언트의 브라우저에 응답할 데이터 유형을 설정하고 응답할 데이터 인코딩 방식 UTF-8설정
		response.setContentType("text/html; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		//2.1. 요청한 클라이언트의 브라우저 창과 연결된 데이터를 바이트 단위로 내보낼 출력스트림 PrintWriter생성
		PrintWriter out = response.getWriter();
		
		//순서2. 클라이언트가 로그인 요청시 입력한 아이디, 비밀번호 얻기 (요청한 데이터들 얻기)
		String user_id = request.getParameter("user_id");
		String user_pw = request.getParameter("user_pw");
		
		//순서3. 클라이언트가 로그인 요청시 입력한 아이디, 비밀번호를 MemberVO클래스의 객체를 생성해서 인스턴스변수에 각각 저장
		MemberVO memberVO = new MemberVO();   memberVO.setId(user_id);   memberVO.setPwd(user_pw);
		
		//순서4. 클라이언트가 로그인 요청시 입력한 아이디, 비밀번호가 XE데이터베이스의 t_member테이블에 저장되어 있는지 확인을 위해
		//      MemberDAO 클래스의 객체를 생성해서 메소드 호출하여 확인하는 명령 합니다.
		MemberDAO memberDAO = new MemberDAO();
		
		//클라이언트가 로그인 요청시 입력한 아이디, 비밀번호가 t_member테이블에 저장되어 조회가 되면? true 반환 받고,
		//                                                               조회가 되지 않으면? false 반환받아 저장 
		boolean result = memberDAO.isExisted(memberVO);
		
		//순서5. 클라이언트가 로그인 요청시 입력한 아이디, 비밀번호가 데이터베이스의 t_member테이블에서 조회되면?
		//      HttpSession 객체 메모리에  로그인 처리 인증할 값,  입력한 아이디, 비밀번호 저장(바인딩)
		if(result) {
			//로그인 처리 작업
			HttpSession session = request.getSession(); //새로운 HttpSession 생성하여 얻기
			session.setAttribute("isLogon", true); //로그인 처리 인증할 값 바인딩
			session.setAttribute("login.id", user_id); //로그인 시 입력한 아이디 바인딩 
			session.setAttribute("login.pw", user_pw); //로그인 시 입력한 비밀번호 바인딩 
			
			//그런 후  로그인 요청한 클라이언트의 브라우저로  로그인된 사용자 화면을 보여줌(응답)
			out.print("<html>");
				out.print("<body>");
				out.print(user_id + "님 로그인 중입니다..... 환영합니다!");
				out.print("<a href='show'>회원정보 조회</a>");
				out.print("</body>");
			out.print("</html>"); 
			
		}else {//클라이언트가 로그인 요청시 입력한 아이디, 비밀번호가 데이터베이스의 t_member 테이블에서 조회되지 않으면?
    	       //클라이언트의 브라우저로 미 로그인 된 화면으로 응답하고, 다시 로그인을 유도 하기 위한 <a>링크 화면을 보여주자.
			out.print("<html>");
				out.print("<body>");
				out.print("<center>회원 아이디 또는 비밀번호가 틀립니다. 다시 확인 해 주세요.</center>");
				out.print("<a href='login5.html'>다시 로그인 요청 하러 가기</a>");
				out.print("</body>");
			out.print("</html>");     
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
