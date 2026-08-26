package sec03.ex01;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;

/*
사용자 정의 EncoderFilter 클래스를 만들때...
반드시 제공되는 Filter 인터페이스 내부에 작성된 추상메소드들(init, doFilter, destory)을 강제로 메소드 오버라이딩 해서 만듭니다.
*/
@WebFilter("/*")
public class EncoderFilter extends HttpFilter implements Filter {
	
	ServletContext servletContext;
	
	/*==================================================================
	  init() : EncoderFilter 객체가 톰캣에 만들어질 때 딱 1번 호출되는 준비 메소드
	==================================================================*/   
	/* init 메소드 
	   - 클라이언트가 웹브라우저를 이용해 LoginTest 서블릿 요청시 
	     톰캣 서버가 웹프로젝트 하나당 만들어 주는 ServletContext 서블릿 관련 객체 메모리를 하나 얻어 
	     인스턴스변수에 저장 하는 역할.
	*/
	@Override
	public void init(FilterConfig fConfig) throws ServletException {
		
		System.out.println("UTF-8 방식으로 인코딩........");
		
		this.servletContext = fConfig.getServletContext();
	}
	/*==================================================================
	  doFilter() : 요청이 올 때마다 "매번" 호출되는 핵심 메소드

	  매개변수 3개
	    request  : 브라우저의 요청 정보가 담긴 HttpServletRequest 객체 
	    response : 브라우저로 보낼 응답 정보를 담을 HttpServletResponse 객체
	    chain    : 다음 차례(다음 필터 또는 서블릿)로 넘겨주는 연결 객체

	  ** 가장 중요한 규칙 **
	    chain.doFilter() 호출줄을 기준으로
	      윗부분  = 서블릿 실행 "전"에 동작  -> 요청 필터
	      아랫부분 = 서블릿 실행 "후"에 동작  -> 응답 필터
	==================================================================*/	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		System.out.println("doFilter 메소드가 호출되어 실행중!");
		/* 요청이 들어 올때 마다 이 줄이 콘솔에 찍힌다.(요청 횟수 확인용)*/
		
		//-----------------------------------------------------------
		//[요청 필터 구간 시작] - 서블릿 실행 전 공통 작업
		//------------------------------------------------------------
		
		/* 공통작업1 : 한글처리 3종 세트 
		   모든 서블릿에서 반복하던 코드를 여기 한 곳으로 모았다.
		   이제 각 서블릿에서는 이 3줄을 지워도 한글이 깨지지 않는다.*/
		request.setCharacterEncoding("UTF-8"); //요청한 데이터 한글 처리 
		response.setContentType("text/html; charset=UTF-8"); // 응답할 데이터 유형 HTML문서의 데이터이고, 응답할 데이터 한글처리
		response.setCharacterEncoding("UTF-8");//톰캣서버가 응답할 한글 글자를 만들때 사용한 문자방식을 UTF-8로 설정 (응답할 데이터 한글처리)
		
		/* 공통작업 2 : 요청 주소 정보 3가지 확인 
		   ServletRequest 부모 인터페이스 타입에는 주소 관련 추상메소드가 없어서 
		   (HttpServletRequest)로 형변환한 뒤 메소드를 호출한다.
		*/
		String contextPath = ((HttpServletRequest)request).getContextPath(); //전체 URL 중에서 "/pro10" 컨텍스트 주소 얻기
		String pathInfo    = ((HttpServletRequest)request).getRequestURI(); //전체 URL 중에서 "/pro10/login" URI 주소 얻기 
		String realPath    = request.getRealPath(pathInfo);
		/* URI가 서버 컴퓨터의 실제 폴더 어디에 해당하는지 물리 경로(실제 전체 경로)를 얻는다.
		   예) -> C:\...\wtpwebapps\pro10\login

		   ** 주의 : request.getRealPath()는 폐기(deprecated)된 메소드다.
		      실무에서는 아래처럼 ServletContext의 것을 사용한다.
		      String realPath = servletContext.getRealPath(pathInfo);  
		*/
		String mesg = "ContextPath : " + contextPath
				    + "\n URI 정보 : " + pathInfo
				    + "\n 물리적 URI 정보 : " + realPath;
		System.out.println(mesg);
		
		/*공통 작업3 : 처리 시간 측정 - 시작 시각 기록*/
		long begin = System.currentTimeMillis();
		/* 현재 시각을 1/1000초(밀리초) 단위 숫자로 얻는다.
		   LoginTest 서블릿 실행 "전"의 시각이다. */
		
		/* 시간 차이를 눈으로 확인하기 위한 부하(일부러 만든 작업) :
		   1을 1000번 출력해서 처리 시간을 늘려 본다. 실무 코드는 아니다. */
		for (int i = 0; i < 1000; i++) {
			System.out.println("1");
		}
		
		//========================================================================================================================		
		/*--------------------------------------------------------------
		  [경계선] 다음 차례로 넘기기
		  - 다음 필터가 또 있으면 그 필터로,
		    없으면 요청받은 서블릿(예: LoginTest)으로 진행시킨다.
		  - 이 줄을 빼먹으면 서블릿이 아예 실행되지 않고
		    브라우저는 빈 화면을 받게 된다. (필터 단골 사고!)
		--------------------------------------------------------------*/
		chain.doFilter(request, response);		
		/* 여기서 서블릿이 실행을 마칠 때까지 기다렸다가
		   끝나면 아랫줄부터 이어서 실행된다. */
		//========================================================================================================================
		
		//-----------------------------------------------------------------------
		//[응답 필터 구간 시작] - LoginTest 서블릿 클래스의 코드 모두 실행후  공통작업 코드 작성
		//-----------------------------------------------------------------------
		
		long end = System.currentTimeMillis();
		/* LoginTest 서블릿 실행이 "끝난 후"의 시각을 기록한다. */
		
		System.out.println("작업 수행시간 : " + (end - begin) + "ms");
		/* (끝시각 - 시작시각) = 요청 하나를 처리하는 데 걸린 시간.
		   LoginTest 서블릿 실행 시간을 자동 측정하는 성능 확인 기법이다. */
		
	} // doFilter 메소드 

	/*==================================================================
	  destroy() : 톰캣 서버가 종료될 때 딱 1번 호출되는 정리 메소드
	==================================================================*/
	@Override
	public void destroy() {
		System.out.println("destroy 메소드 호출 됨");
		
	}

}






