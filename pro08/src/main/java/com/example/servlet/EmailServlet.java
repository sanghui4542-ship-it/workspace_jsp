package com.example.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
EmailServlet 클래스의 역할
-  이메일 발송 서비스 기능을 하는 클래스 
-  ServletConfig 객체 메모리 내부 영역에 저장된 도메인주소, 포트번호를 받아와서 
    클라이언트가 이 EmailServlet 서블릿 클래스를 요청하면  설정된 정보를 이용해서 이메일 발송하는 기능을 구현 하는 클래스 
*/

// @WebServlet("/sendEmail")  작성 X -> 현재 web.xml에 <url-pattern> 태그 사이에 /sendEmail 작성하면 중복되기 떄문 
public class EmailServlet extends HttpServlet {
	
	//EmailServlet 클래스의 인스턴스변수 선언
	//-> 이메일 서버 설정시 사용될 도메인, 포트번호를 저장할 인스턴스변수 선언
	private String serverAddress;  //<- 이메일 서버의 도메인주소 (smtp.example.com)
	private int  serverPort;       //<- 이메일 서버의 포트번호 ( 587 )
	
	/*
	  서블릿 초기화(EmailServlet클래스의 변수값 초기화) 메소드
	  - 서블릿이 처음 실행될때 호출됩니다.
	  - 주로 서블릿이 실행되기 전에 필요한 변수값 설정들을 초기화하는데 사용됩니다.
	  - ServletConfig객체 메모리는 이 서블릿에 대한 설정을 가져오는 객체 메모리입니다.
	 */	
	@Override
	public void init(ServletConfig config) throws ServletException {		
		//이메일 서버 도메인주소,  포트번호를 ServletConfig 객체 메모리 내부에서 꺼내오기 -> web.xml 파일에 작성 해 놓은 변수들의 값을 얻는다
		this.serverAddress = config.getInitParameter("serverAddress"); //=> "smtp.example.com"
		String port = config.getInitParameter("serverPort"); //=>"587"
		
		try {
			//문자열로 얻은 포트번호"587"을 숫자 587로 변경해야 하므로 Integer.parseInt("587"); 호출해서 587 숫자로 얻자.
			serverPort = Integer.parseInt(port); //<- 만약 null을 전달하면 숫자로 변경 불가!			
		} catch (NumberFormatException e) {
			//만약 숫자가 아닌 값을 포트번호로 받으면 예외가 발생함
			//이경우, 예외처리를 해서 적절한 오류 메세지를 표시하게끔 해야 합니다.
			//- 예외가 발생하면 서블릿을 실행할수 없으므로 예외메세지를 저장
			throw new ServletException("Invalid port number : " + port);
		}	
		//디버깅(테스트)을 위해 이메일 서버의 포트번호, 도메인 정보 출력
		System.out.println("이메일 서버의 도메인주소 : " + this.serverAddress);
		System.out.println("이메일 서버의 포트번호 : " + this.serverPort);
	} //--------------->>> init 메소드 

	//2. Get 방식으로 이메일 보내기 요청을 받으면 호출되는 실제 구현해야 하는 메소드 
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("text/html; charset=utf-8");
		
		PrintWriter out = response.getWriter();
		
		out.print("<html><body>");
		
		out.print("<h2>이메일 발송 서버 정보</h1>");
		
		out.print("<p><strong>이메일 서버 도메인 주소 :</strong>" +  this.serverAddress  + "</p>");
		
		out.print("<p><strong>이메일 서버 포트번호 :</strong>" +  this.serverPort  + "</p>");
		
		out.print("<p>이메일 발송 하는 기능이 실행 되었습니다.</p>");
		
		out.print("</body></html>");				
		
	}

}













