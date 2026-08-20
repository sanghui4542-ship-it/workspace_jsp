package sec03.ex02;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//서블릿 역할 : 클라이언트의 요청을 받고  브라우저로 응답할 데이터를 생성하여 
//            클라이언트의 브라우저로 응답하는 서버페이지 
/*
login3.html(아이디와 비밀번호를 입력해서 로그인버튼을 눌러 서블릿을 요청하는 화면)에서 
로그인 버튼을 눌러  http://localhost:8181/pro06/login3 
요청주소를 톰캣 서버에 전달하여 요청하면
요청을 받는 LoginServlet클래스 입니다.

순서1. 클라이언트가 웹브라우저 주소창에 
      http://localhost:8181/pro06/login3.html을 입력하여 
           정적인 페이지인 login3.html 디자인 화면을 요청합니다.

순서2. 웹서버 (아파치)는  login3.html파일을 찾아 실행한 HTML코드 디자인을 
           클라이언트의 웹브라우저 화면에 표시 해 줍니다.

순서3. login3.html화면을 본~ 클라이언트는 아이디와 비밀번호를 입력하고  
      로그인(submit)버튼을 눌러 <form>태그의 action 속성에 설정된 login3 요청주소로 
      LoginServlet서블릿(서버페이지)를 요청하게 됩니다.

순서4. LoginServlet서블릿은 웹브라우저를 통해 전송한 정보(입력한 아이디,비밀번호)를  
  	   톰캣 서버가  HttpServletRequest객체메모리를 생성한 후 담아서    
  	  doGet메소드의 매개변수로 전달합니다.
  	  
순서5. 우리 백엔드 개발자가 doGet메소드에서 요청한 데이터를 받아서 
      이클립스의 console 탭에 요청한 값들을 얻어서 확인차 출력시킵니다.      

	<form action="login3" method="get">
		.....
	</form>

login3.html(아이디와 비밀번호를 입력해서 로그인버튼을 눌러 서블릿을 요청하는 화면)에서 
로그인 버튼을 눌러  http://localhost:8181/pro06/login3 
요청주소를 톰캣 서버에 전달하여 요청하면
요청을 받는 LoginServlet클래스 입니다.
*/
@WebServlet("/login4")
public class LoginServlet4 extends HttpServlet {

//doGet, doPost 메소드 모두 오버라이딩 후 각 메소드 내부에서 doHande메소드를 다시 호출해서 요청 받고 응답처리 하자
	
	//클라이언트가 GET요청 방식으로 요청하면 호출되는 콜백메소드로
	//(웹브라우저 주소창에 주소 입력 GET요청 또는  <form method="get"> 태그에 대한 GET요청을 설정 후 요청할때)
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
		System.out.println("login4.html로 부터 GET전송 요청 방법으로 로그인 요청 받았다. doGet() 메소드 내부 코드이다.");
		doHandle(request, response);
	}

	//클라이언트가 POST요청 방식으로 요청하면 호출되는 콜백메소드로
	//(<form method="post"> 태그에 의한 POST요청을 설정 후 요청할때)
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("login4.thml로 부터 POST전송 요청 방법으로 로그인 요청 받았다. doPost() 메소드 내부 코드이다.");
		doHandle(request, response);
	}
	
	
	protected void doHandle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("이 코드는 doHandle 메소드 내부의 코드가 실행 된 것이다.");
		
		/*  순서1. 클라이언트가 요청한 요청 데이터들은 매개변수 request 로 전달 받는 HttpServletRequest 객체 메모리에 저장되어 있으므로
				  요청 한 데이터 들 중에서 한글 문자가 하나라도 존재하면 HttpServletRequest 객체 메모리에서 꺼내올떄
				  한글 문자만 꺠져서 꺼내와 지므로 전세계 문자를 처리할수 있는 방식(인코딩 방식)의 값을 UTF-8로 설정 해서 
				  한글 깨짐을 방지 하자.
		    요약 :  클라이언트가 요청한 데이터들을 HttpServletRequest객체 메모리에서 꺼내오기 전에 문자 한글처리 
		*/
		request.setCharacterEncoding("UTF-8");	//ISO-8859-1  : 유럽 문자 처리(인코딩)
												//EUC-KR      : 한글 문자 처리(인코딩)
												//UTF-8       : 전 세계 문자 처리(인코딩)
		
		/* 순서2. login2.html에서 입력한 클라이언트의 로그인 요청시 전달 한 값들을 HttpServletRequest객체 메모리 안에서 얻기 
		   요약 :  클라이언트가 요청한 값 얻기  => 방법 : HttpServletRequest클래스의 getParameter("<input>의 name속성의 값"); 메소드 호출!
		*/
		String user_id = request.getParameter("user_id");    //입력한 아이디가 admin 이라면?  "admin" 문자열로 얻기
		String user_pw = request.getParameter("user_pw");    //입력한 비밀번호 1234 라면?    "1234" 문자열로 얻기 
		
		/*
		  순서3.1. 요청한 클라이언트의 웹브라우저로 응답할 데이터 종류(MIME-TYPE)을 
		  		  HttpServletResponse 객체 메모리에 설정
		  		  설정할 MIME-TYPE => 응답할 데이터는 text 기반의 HTML 데이터로 설정,
		  		                     모든 응답 문자들이 깨짐을 방지 하기 위해 UTF-8 방식으로 처리해서 응답하겠다고 설정!
		*/
		response.setContentType("text/html; charset=UTF-8");
		
		/*
		  순서3.2. 요청한 클라이언트의 웹브라우저로 응답할(출력할) 출력스트림 통로 (PrintWriter객체) 얻기
		  얻는 방법 :  HttpServletResponse 객체의 PrintWriter  getWriter() 메소드를 호출하면 PrintWriter 객체 주소 반환 받음
		*/
		PrintWriter  out = response.getWriter();
		
		/* 순서3.3. 클라이언트로 부터 요청받은 데이터를 이용해 클라이언트의 브라우저로 응답할(출력할) 데이터 생성 */
		String data = "<html>";
			   data += "<body>";
			   data += "클라이언트가 입력한 아이디 : " + user_id + "<br>";
			   data += "클라이언트가 입력한 비밀번호 : " + user_pw + "<br>";
			   data += "</body>";
			   data += "</html>";
		
	   /* 순서4. LoginServlet2.class 서블릿으로 요청한 클라이언트의 웹브라우저로 응답할 데이터 응답(출력) */
	   // 응답 방법 : 브라우저와 연결된 출력스트림(PrintWriter객체)을 통해 응답
	   out.print(data);
		
	}



}










