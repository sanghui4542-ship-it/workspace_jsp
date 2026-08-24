package sec01.ex02;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


//<a href="http://localhost:8181/second?user_id=admin&user_pw=1234&user_address="%EC%84%9C%EC%9A%B8%EC%8B%9C+%EC%84%B1%EB%B6%81%EA%B5%AC"">두번째 서블릿을 재요청시 데이터 보내기</a>

//@WebServlet(urlPatterns = { "/second" } , name="second")
@WebServlet("/second")
public class SecondServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//순서1. 요청한 데이터들 한글처리
		//   첫번째 서블릿 LoginServlet에서 <a> 태그를 클릭해 재요청 받은 요청데이터는 모두 HttpServletRequest객체 메모리에 저장되어 있다.
		//   "서울시 성북구" 요청한 주소는  한글이므로  현재 SecondServlet 에서 꺼내서 사용하면 한글 깨져서 얻어질것이다.
		//   HttpServletReuqest객체 메모리 내부의 요청데이터 인코딩 방식 UTF-8 로 설정
		request.setCharacterEncoding("UTF-8");
		
//"http://localhost:8181/second?user_id=admin&user_pw=1234&user_address="서울시 성북구"
		
		//순서2. 요청받은 데이터들 얻기 
		String user_id = request.getParameter("user_id"); //"admin"
		String user_pw = request.getParameter("user_pw"); //"1234"
		String user_address = request.getParameter("user_address"); //"서울시 성북구"
		
		//순서3.1. 브라우저로 응답할 데이터 유형 설정 및 한글처리
		response.setContentType("text/html; charset=utf-8");
		
		//순서3.2. 출력스트림 생성
		PrintWriter out = response.getWriter();
		
		//순서3.3. 조건에 따라 응답할 메세지 생성해서 브라우저로 응답
		//조건1. 입력한 아이디가 존재 하면? (첫번째 서블릿에서 <a>를 눌러 요청시 전달한 아이디를 두번째 서블릿에서 받을수 있다면?)
		if(user_id != null  && user_id.length() != 0) {
		
			out.print("이미 로그인 된 상태 입니다.<br><br>");
			out.print("첫번쨰 서블릿 LoginServlet으로 부터 <a>태그로 인해 재요청 받아 공유받은 입력한 아이디 : " + user_id + "<br>");
			out.print("첫번쨰 서블릿 LoginServlet으로 부터 <a>태그로 인해 재요청 받아 공유받은 입력한 비밀번호 : " + user_pw + "<br>");
			out.print("첫번쨰 서블릿 LoginServlet으로 부터 <a>태그로 인해 재요청 받아 공유받은 주소 : " + user_address + "<br>");
	
		}else { //조건2. 입력한 아이디가 존재하지 않으면? (첫번째 서블릿에서 <a>를 눌러 요청시 전달한 아이디를 두번쨰 서블릿에서 받을수 없다면?)
			
			out.print("로그인 하지 않고 두번째 서블릿인 SecondServlet 페이지를 보여 주고 있습니다. <br><br>");
			out.print("다시 로그인 하고 오세요.<br>");
			out.print("<a href='/pro09/login2.html'>로그인 요청 하러가기</a>");
		}	
	} 
}










