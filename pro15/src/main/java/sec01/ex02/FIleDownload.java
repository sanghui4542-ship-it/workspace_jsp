package sec01.ex02;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
// commons-fileupload-1.3.3.jar 라이브러리 의 클래스 
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;


// 업로드된 파일을 브라우저로 다운로드 되게 서비스를 제공하는 서블릿 

//@WebServlet("/download.do")
public class FIleDownload extends HttpServlet {
	
	//클라이언트가 GET  또는 POST 요청을 하면 모든 요청(request)을 받아서 처리 하는 메소드 
	protected void doHandle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//1. 요청한 파라미터 한글처리 
		request.setCharacterEncoding("UTF-8"); 
		
		//2. <a>를 클릭해 다운로드 요청한 클라이언트의 브라우저로 다운로드할 파일의 정보를 내보내어 응답하기 위해
		//   응답할 데이터 유형(MIME-TYPE)을  text/html; 로 설정하고, 응답 한글문자 데이터 UTF-8 인코딩 설정
		response.setContentType("text/html; charset=utf-8");
		
		//3. 다운로드할 파일이 저장된 폴더 경로 문자열로 저장
		String file_repo = "C:\\file_repo";
		
		//4. result.jsp 화면에서 <a>를 클릭해 다운로드 요청한 파일명을 request 객체 메모리에서 얻기
		//   http://localhost:8181/pro15/download.do?fileName=eclipse.exe
		//요약 : 다운로드할 파일명 얻기
		String fileName = request.getParameter("fileName"); //"eclipse.exe"
		
		//5.      3. 4.에서 얻은  다운로드할 파일이 저장된 폴더경로  +   다운로드시킬 파일명을 하나의 문자열로 합쳐서 저장
		//                "C:\\file_repo\\eclipse.exe" 로 만들어 서  저장	
		String downFile = file_repo  + "\\" + fileName;
		
	    //6.<a>를 클릭하여 서블릿으로 파일 다운로드 요청한 클라이언트의 브라우저와 연결된 출력스트림 통로 (CoyoeOutputStream 객체) 얻기
		OutputStream outputStream = response.getOutputStream();
		
		//7.    5.에서 얻은 다운로드 시킬 파일 전체 경로에 코드로 접근하기 위해 File 클래스의 객체 생성
		File  file = new File(downFile);  //"C:\\file_repo\\eclipse.exe"  에 접근
		
		/*		
		웹브라우저 캐시에 대해 설명하기 위한 내용
			웹개발을 하다보면 게시글 등의 데이터를  DB에 등록 했는데도 브라우저에서 새로고침 시 
			해당 데이터에 대한 내용이 반영되지 않는 경우가 있습니다. 
			혹은 데이터 뿐만 아니라 javascript나, HTML, CSS 등의 정적자원을 서버에서 수정했는데도
			새로고침시 적용되지 않는 경우도 있습니다.
			
		웹브라우저의 캐시 공간이란?
			브라우저에는 캐시 스토리지 공간이있는데, 
			이것은 서버페이지와 불필요한 통신을 하지 않기 위해 마련된 공간입니다.
			최초 서버로부터 요청한 자원들(javascript,HTML,CSS,이미지 등)을 내려 받고 
			같은 자원을 새로 고침등을 통해서 다시 요청하는 경우
			브라우저는 실제로는 서버로 HTTP요청을 하지 않고 
			브라우저 자신의 캐시 스토리지에 저장해 두었던
			읽어 들였던 자원들 재사용하게 됩니다.
			
			예를 들어 test.jsp를 최초 요청한 경우 서버로부터 응답된 자원들을 
			웹브라우저 캐시 스토리지에 저장하고  F5나 
			주소표시줄에 주소를 다시 입력해 다시 test.jsp를 재요청한경우
			불필요하게 다시 HTTP요청을 하는 것이 아니라 캐시 스토리지에서 저장된 데이터를 꺼내서 
			웹브라우저 화면에 보여줍니다.
			이러한 브라우저 캐시 기능이 성능상 이점을 가져다 줄수 있겠으나
			게시판이나 네이버의 실시간 검색어 처럼 자주 변하는 동적인 데이터 부분까지 
			브라우저의 캐시 스토리를 사용한다면 사용자는 변환된 데이터의 결과를 웹브라우저로 볼수 없고
			계속 같은 화면만 보게 될것입니다.
			
	  응답 헤더를 통한 캐시 스토리지 제어 설명
	  		HTTP 응답 메세지의 몇가지 헤더 속성을 통해서 
	  		웹브라우저가 현재 페이지 내용을 캐시 스토리지 저장하는 것을 사용하지 않도록 할수 있습니다.
	  		response객체의 해당 속성들에 값을 설정해 웹브라우저가 캐시 스토리지를 사용하지 않고
	  		매번 새로운 요청을 통해 응답결과를 얻어오도록 할수 있습니다.		
	*/			
		//8. HTTP1.1 문서 버전에서 지원하는 헤더로  no-cache값을 설정하면 
		//   브라우저는 응답받은 결과데이터를 브라우저의 캐시 스토리지에 저장하지 않습니다.
		response.setHeader("Cache-Control", "no-cache");
		//   또한 뒤로가기 <- 등을 통해서 전에 봤던 페이지로 이동하는 경우  페이지를 캐싱할수 있으므로 
		//   no-stroe 값 또한 추가해 설정 해야 합니다.
		response.addHeader("Cache-Control", "no-store");
		
		//9. 웹브라우저에서 다운로드할 <a>다운로드할파일명.png</a>링크를 클릭시
		//설정1.Content-Disposition속성에 attachment; 값을 지정하여 
		//  다운로드시 무조건 "파일 다운로드 다른이름으로 저장?" 대화상자가 뜨도록 하는 헤더 속성의 설정 
		
		//설정2. 다운로드할 파일명이 한글일 경우 꺠져 내려 받아 지지 않도록하기 위해 
		//   Content-Disposition속성에 다운로드할 파일명을 인코딩 후 설정
		response.setHeader("Content-Disposition", 
				            "attachment; fileName=\"" + URLEncoder.encode(fileName, "UTF-8") + "\";");		
		/*
		 위 9. 참고
			Content-Disposition : attachment
			브라우저 인식 파일 확장자를 포함하여 모든 확장자의 파일들에 대해....
			다운로드시 무조건 "파일 다운로드" 대화 상자가 뜨도록 하는 헤더 속성의 값 입니다.
			
			Content-Disposition : inline
			브라우저 인식 파일 확장자를 가진 파일들에 대해서는 웹브라우저 상에서 바로 파일을 열고
			그외의 파일들에 대해서는 "파일 다운로드" 대화 상자가 뜨도록 하는 헤더 속성의 값 입니다. 			
		 */
		
		//10. 실제 파일 다운로드를 구현 
		//- 다운로드할 파일에 작성된 데이터들을 바이트 단위로 읽어들일 입력스트림 통로 생성
		FileInputStream  fileInputStream = new FileInputStream(file);  //"C:\\file_repo\\eclipse.exe"
		
		//- 다운로드할 파일에서 데이터를 8kb씩 읽어와 저장할  byte배열 생성
		byte[] buffer = new byte[1024*8];
		
		while(true) {
		
			//- 다운로드할 파일의 내용을 약 8kb 단위로 읽어와 위 buffer byte 배열에 저장후 
			//	한번 읽어 들인 byte 갯수를 반환하고 만약 읽어들인 byte갯수가 없으면 -1을 반환
			int count = fileInputStream.read(buffer);
			
			
			//- 더이상 다운로드할 파일에서 읽어들일 데이터가 없으면?
			//	while반복문을 종료해서 더이상  fileinputstream.read(buffer);메소드를 호출해서 읽어들이지 못하게 하자
			if(count == -1) { break;  }
			
			
			//- 출력스트림 CoyoeOutputStream 통로를 통해 위 FileInputStream 통로에서 한번 읽어 들인 데이터가 저장된
			//	byte[] buffer = new byte[1024*8]; 의  0 index 위치 칸 데이터 부터 시작해서  count변수 index 위치의 끝까지의 정보를
			//  다운로드 요청한 클라이언트의 웹브라우저오 내보내어 다운로드 시킵니다.
			outputStream.write(buffer, 0, count);			
		}
			
	} //doHandle 메소드 끝 

	@Override  //클라이언트가 GET 방식으로 요청하면 요청을 받아 처리 하는 메소드 오버라이딩 
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doHandle(request, response);
	}

	@Override  //클라이언트가 POST 방식으로 요청하면 요청을 받아 처리 하는 메소드 오버라이딩 
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doHandle(request, response);
	}

}
