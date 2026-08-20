package sec01.ex01;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//서블릿 역할 : 클라이언트의 요청을 받고  브라우저로 응답할 데이터를 생성하여 
//            클라이언트의 브라우저로 응답하는 서버페이지 
/*
login.html(아이디와 비밀번호를 입력해서 로그인버튼을 눌러 서블릿을 요청하는 화면)에서 
로그인 버튼을 눌러  http://localhost:8181/pro06/login 
요청주소를 톰캣 서버에 전달하여 요청하면
요청을 받는 LoginServlet클래스 입니다.

순서1. 클라이언트가 웹브라우저 주소창에 
      http://localhost:8181/pro06/login.html을 입력하여 
           정적인 페이지인 login.html 디자인 화면을 요청합니다.

순서2. 웹서버 (아파치)는  login.html파일을 찾아 실행한 HTML코드 디자인을 
           클라이언트의 웹브라우저 화면에 표시 해 줍니다.

순서3. login.html화면을 본~ 클라이언트는 아이디와 비밀번호를 입력하고  
      로그인(submit)버튼을 눌러 <form>태그의 action 속성에 설정된 login 요청주소로 
      LoginServlet서블릿(서버페이지)를 요청하게 됩니다.

순서4. LoginServlet서블릿은 웹브라우저를 통해 전송한 정보(입력한 아이디,비밀번호)를  
  	   톰캣 서버가  HttpServletRequest객체메모리를 생성한 후 담아서    
  	  doGet메소드의 매개변수로 전달합니다.
  	  
순서5. 우리 백엔드 개발자가 doGet메소드에서 요청한 데이터를 받아서 
      이클립스의 console 탭에 요청한 값들을 얻어서 확인차 출력시킵니다.      



	<form action="login" method="get">
		.....
	</form>

login.html(아이디와 비밀번호를 입력해서 로그인버튼을 눌러 서블릿을 요청하는 화면)에서 
로그인 버튼을 눌러  http://localhost:8181/pro06/login 
요청주소를 톰캣 서버에 전달하여 요청하면
요청을 받는 LoginServlet클래스 입니다.
*/
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

	//클라이언트의 get 주소 요청을 받았을때 호출되는 콜백메소드로 
	//로그인 요청시 입력한 정보를 받아서 브라우저로 응답하는 기능의 doGet 메소드 오버라이딩
	// 메소드 오버라이딩 -> alt + shift  + s    v
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/*  순서1. 클라이언트가 요청한 요청 데이터들은 매개변수 request 로 전달 받는 HttpServletRequest 객체 메모리에 저장되어 있으므로
				  요청 한 데이터 들 중에서 한글 문자가 하나라도 존재하면 HttpServletRequest 객체 메모리에서 꺼내올떄
				  한글 문자만 꺠져서 꺼내와 지므로 전세계 문자를 처리할수 있는 방식(인코딩 방식)의 값을 UTF-8로 설정 해서 
				  한글 깨짐을 방지 하자.
		    요약 :  클라이언트가 요청한 데이터들을 HttpServletRequest객체 메모리에서 꺼내오기 전에 문자 한글처리 
		*/
		request.setCharacterEncoding("UTF-8");	
		
		/* 순서2. login.html에서 입력한 클라이언트의 로그인 요청시 전달 한 값들을 HttpServletRequest객체 메모리 안에서 얻기 
		   요약 :  클라이언트가 요청한 값 얻기  => 방법 : HttpServletRequest클래스의 getParameter("<input>의 name속성의 값"); 메소드 호출!
		*/
		String user_id = request.getParameter("user_id");    //입력한 아이디가 admin 이라면?  "admin" 문자열로 얻기
		String user_pw = request.getParameter("user_pw");    //입력한 비밀번호 1234 라면?    "1234" 문자열로 얻기 
		
		/* 순서3. login.html에서 입력해서 요청한 아이디, 비밀번호를 서블릿(서버페이지)로 제대로 얻는지 확인차 이클립스의 console 탭에 출력 */
		System.out.println("클라이언트가 요청한(입력한) 아이디 : " + user_id);
		System.out.println("클라이언트가 요청한(입력한) 비밀번호 : " + user_pw);
		
	}

}











