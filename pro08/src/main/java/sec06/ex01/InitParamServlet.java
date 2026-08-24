package sec06.ex01;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
브라우저 주소창에 아래 주소를 입력하면 InitParamServlet 서블릿 클래스가 요청 주소를 받아 처리한다.

http://localhost:8181/pro08/sInit

또는

http://localhost:8181/pro08/sInit2

*/
/*
@WebServlet 어노테이션  : 아래의 클래스는 서블릿 역할을 하는 클래스이며, ( ) 사이의 설정 정보를 따르겠다고 톰캣 서버에 알려주는 어노테이션
*/
@WebServlet(
	    /*
	    ---------------------------------------------------------------------------
	    urlPatterns 속성
	    ---------------------------------------------------------------------------
	    ■ 역할
	    - 클라이언트가 이 InitParamServlet 서블릿  요청 하는  URL 매핑 주소를 하나 이상 설정하는 속성
	    - 클라이언트가 브라우저에서 이 요청 하는 URL 주소로 InitParamServlet 서블릿에 요청하면 톰캣이 이 서블릿 클래스를 찾아 실행한다.
	    */
		urlPatterns = { 
				"/sInit",  //서블릿을 요청할 첫번째 매핑 주소 설정 (메인)
				"/sInit2"  //서블릿을 요청할 두번째 미핑 주소 설정 (서브)
		}, 
	    /*
	    ---------------------------------------------------------------------------
	    initParams 속성
	    ---------------------------------------------------------------------------
	    ■ 역할
	    - 이 서블릿이 사용할 "초기 설정값"을 저장하는 공간
	    - 프로그램 시작 시 단 1번만 저장됨
	    - 이후 계속 재사용

	    ■ 어디에 저장되나?
	        - 톰캣이 자동으로 InitParamServlet 클래스 하나에 관한 
	          new ServletConfig() 객체 메모리를 생성해서 메모리 내부에  미리 변수를 만들어 저장(초기화)한다.
	          개발자가 new ServletConfig() 하지 않는다.
	    */	
		initParams = { 
	    		/*
	    		 @WebInitParam 어노테이션
	    		 --------------------------------------------------------------------
	    		 - 이 서블릿이 사용할 "변수의 초기 설정값 1개"를 등록하는 표식(어노테이션)
	    		 - 톰캣 서버가 이 정보를 읽어서 ServletConfig 객체에 자동 저장한다.
	    		 - 개발자가 직접 객체를 만들거나 저장 코드를 작성하지 않는다.
	    		 - 나중에 getInitParameter("email") 로 꺼내 사용한다.
	    		*/
									// -> ServletConfig 객체 메모리 내부에 email=admin@jweb.com 형태로 저장됨 
								    //변수이름,       //변수에 실제 저장될 초기 값 
				@WebInitParam(name = "email", value = "admin@jweb.com"), 
				
				@WebInitParam(name = "tel", value = "010-1111-2222")
		})
public class InitParamServlet extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//1. 요청한 클라이언트의 브라우저에 응답할 데이터 유형(MIME-TYPE) 설정 및 인코딩 방식 설정
		response.setContentType("text/html; charset=utf-8");
		
		//2. 요청한 클라이언트의 브라우저와 연결된 데이터를 내보낼 출력스트림 통로 얻기
		PrintWriter  out = response.getWriter();
		
		//3. InitParameServlet 서블릿 클래스 내부에서 사용할 초기변수의 값들을 ServletConfig 객체 메모리 내부에서 얻어 저장
		//방법 -> getInitParameter("변수명");  메소드를 이용하여 변수에 저장된 값을 얻어옵니다.
		String email = this.getInitParameter("email");
		//     email = "admin@jweb.com"
		
		String tel =  this.getInitParameter("tel");
		//     tel =  "010-1111-2222"
		
		//4. 요청한 클라이언트의 브라우저 화면으로 응답(출력)
		out.print("email : " + email + "<br>");
		out.print("tel : " +  tel + "<br>");
		
		/*
		=========================================================
		전체 동작 흐름 요약
		=========================================================
		1. 사용자가 /sInit 또는 /sInit2 주소 요청
		2. 톰캣 서버가 InitParamServlet 실행
		3. ServletConfig 안에 저장된 email, tel 값 꺼냄
		4. 브라우저 화면에 출력
		=========================================================
		*/			
		
	}

}









