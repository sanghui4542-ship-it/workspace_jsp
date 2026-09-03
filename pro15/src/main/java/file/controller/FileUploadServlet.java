package file.controller;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;

import file.dao.FileDAO;

/*
참고. 이 서블릿 하나가 두 가지 일을 담당합니다.

	GET방식 요청  http://localhost:8181/pro15/upload.do   -> 파일 첨부후 업로드 하는 화면 요청       (DB작업 없음)
	POST방식 요청 http://localhost:8181/pro15/upload.do   -> 실제 파일 업로드 + DB에 정보 저장 요청  (DB작업 있음)

	하나의 주소(/upload.do)로 두 가지 처리를 나누는 기준은 요청 방식(GET/POST) 입니다.
*/

@WebServlet("/upload.do")
public class FileUploadServlet extends HttpServlet {
	
	//1. 직렬화 버전 번호 (이클립스 경고 제거용)
	private static final long serialVersionUID = 1L;
	
	//2. 업로드된 파일이 저장될 폴더 이름
	private static final String UPLOAD_DIR = "upload";
	
	//3. 한번에 업로드할 수 있는 파일의 최대 크기를 10MB로 설정
	//   1MB = 1024 * 1024  byte 이므로  10 * 1024  * 1024  =  10MB
	private static final int MAX_SIZE = 10 * 1024 * 1024;
	
	//4. DB작업을 대신 시킬 FileDAO클래스의 객체 주소를 저장할 변수 선언
	private FileDAO fileDao;
	
	//5. FileUploadServlet 서블릿 객체가 최초로 톰캣 서버의 메모리에 올라 갈때 딱 한번만 실행되는 메소드 로 
	//   하는일 : FileDAO 객체 생성 후 위 fileDao변수에 저장
	@Override
	public void init() throws ServletException {
		fileDao  =  new FileDAO();
	}

	// GET   http://localhost:8181/pro15/upload.do  
	// 클라이언트가 브라우저 주소창에 파일첨부 후 업로드 요청하는 디자인 화면 VIEW(WEB-INF/views/fileUpload.jsp) 보여줘 요청 URL입력후 
	// Controller( FileUploadServlet )로 요청 들어 왔을떄 호출되는 콜백 메소드 
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//1. 파일 업로드 폼 디자인 화면 VIEW (WEB-INF/views/fileUpload.jsp)로 재요청 (포워딩)
		RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/fileUpload.jsp");
		dispatcher.forward(request, response);
		
	}

	/*
	POST방식 요청 처리

	폼 화면(fileUpload.jsp)에서 업로드할 파일을 고르고 첨부후  [업로드 요청] 버튼을 클릭했을 때 실행됩니다.

	참고. 파일 업로드가 반드시 POST방식이어야 하는 이유 2가지
		1. GET방식은 데이터를 주소창에 실어 보내므로 길이 제한이 있습니다. (약 2KB)
		2. GET방식은 파일의 실제 내용인 이진(바이너리) 데이터를 담아 보낼 수 없습니다.

	참고. <form> 태그에 반드시 enctype="multipart/form-data"를 지정해야 합니다.
		이 속성이 없으면 파일의 실제 내용은 오지 않고 파일명 문자열만 전송됩니다.
	*/
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		/*
		참고.
			getServletContext()
			 -> 이 웹프로젝트 전체의 정보를 가지고 있는 ServletContext객체를 얻는 메소드 입니다.
			 -> .JSP에서 사용하던 application 내장객체와 완전히 같은 객체입니다.


			getRealPath("/upload")
			 -> 웹 경로를 톰캣서버에 배포된 실제 물리(절대) 경로로 바꿔 반환하는 메소드 입니다.

			    웹 경로   : /upload
			    실제 경로 : C:\workspace_jsp\pro15\src\main\webapp\ upload
						  
						  
						  
			왜 실제 경로가 필요한가?
			 -> 파일을 저장하는 것은 자바의 File 기능이고
			    자바는 웹 주소를 이해하지 못하고 컴퓨터의 실제 폴더 경로만 이해하기 때문입니다.
	   */
		
		//1. 업로드할 실제 톰캣 서버의 하드디스크 내부의 
		//   upload 폴더의 절대 경로(  C:\workspace_jsp\pro15\src\main\webapp\ upload  ) 얻기 
		String savePath = request.getServletContext().getRealPath("/" + UPLOAD_DIR);
		System.out.println(savePath);		
		
		//2. upload 폴더가 없으면 새로 만들어 주기 
		//   이 검사를 하지 않으면 upload 폴더가 없을때 FileNotFoundException이 발생하며 업로드에 실패합니다.
		File  saveDir = new File(savePath);  // new File("C:\workspace_jsp\pro15\src\main\webapp\ upload");		
		if(!saveDir.exists()) {//만들어져 있지 않으면?
			saveDir.mkdirs();  //upload 폴더 자동 생성 
		}
		/*
		참고.
			오렐리 출판사에서 제공하는 파일업로드 관련 cos.jar 라이브러리 파일 내부에 포함된
			MultipartRequest 클래스의 객체 생성시 생성자로 업로드할 파일 정보를 전달해서 저장하면?
			자동으로 우리가 지정한 폴더 경로 upload에 파일 업로드가 됩니다.

			실제 파일 업로드 기능을 담당하는 클래스는? MultipartRequest 클래스 입니다.

			중요. 아래 한 줄이 실행되는 "그 순간" 파일이 이미 서버에 저장됩니다.
			     별도의 저장 메소드를 호출할 필요가 없습니다.

			중요. MultipartRequest객체를 만든 뒤에는
			     request.getParameter()가 동작하지 않고 null을 반환합니다.
			     대신 multipartRequest.getParameter()를 사용해야 합니다.
	   */
		//3. MultipartRequest 클래스의 객체 생성시 생성자로 업로드할 파일 정보를 전달해서 저장하면? 업로드 됩니다.
		//요약 : 첨부한 파일들 업로드 처리 
		
		//-  MultipartRequest 클래스의 객체 생성시 생성자의 매개변수로 전달할 데이터들
		//   전달1.  fileUpload.jsp의 form 태그에 의해서 전달 받은 업로드할 파일의 정보가 저장된 request 내장객체 메모리 주소 전달
		//   전달2.  업로드할 파일의 실제 톰캣 서버의 폴더 경로 (/upload) 전달
		//   전달3.  한번에 업로드할 수 있는 파일의 최대 크기 전달 (10MB) 
		//   전달4.  첨부해서 업로드하는 파일명에 한글문자가 포함되어 있을 경우 파일명이 깨진 채로 업로드되는 것을 방지 하기 위해 UTF-8 설정 전달 
		//   전달5.  같은 이름의 파일 업로드시 파일명 끝에 1을 자동으로 붙여주는 new DefaultFileRenamePolicy() 객체 주소 전달
		MultipartRequest  multipartRequest = new MultipartRequest(request, 
																  savePath, 
																  MAX_SIZE, 
																  "UTF-8", 
																  new DefaultFileRenamePolicy()  );
																// 보고서.hwp, 보고서1.hwp, 보고서2.hwp 형태로 만들어 주는 역할		
		/* 참고. getFileNames()가 반환하는 것

			화면의 폼이 아래와 같다면

				<input type="file" name="file1">
				<input type="file" name="file2">
				<input type="file" name="file3">

			getFileNames()가 반환하는 값

				["file1", "file2", "file3"]   <- 파일의 내용이 아니라 name속성값의 목록

			Enumeration<?> 에서 <?> 의 의미
			 -> 어떤 타입이든 담을 수 있다는 뜻입니다.
			 -> cos 라이브러리가 제네릭이 없던 시절에 만들어져 타입이 지정되어 있지 않습니다.
			 -> 그래서 아래에서 (String)으로 강제 형변환이 필요합니다.
	   */			
		//4. 업로드 요청으로 전송된 첨부할때 이용했던 <input>의 name속성값들을 모두 Enumeration배열에 담아 봔환 받습니다.
	    Enumeration<?> files =  multipartRequest.getFileNames();
		
		//5. DB저장(insert)에 성공한 파일의 개수를 셀 변수 선언
	    //   아직 한 건도 저장하지 않았으므로 0으로 초기화 합니다.
	    int  successCount = 0;
	    
	    //6. 업로드한 파일 개수만큼 반복
	    //   hasMoreElements() : 아직 꺼내지 않은 name값이 남아 있으면 true를 반환
		//   nextElement()     : 다음 name값을 하나 꺼내고 커서를 한 칸 이동
	    while(files.hasMoreElements()) {
	    	
	    	//6.1. <input type="file" name="????"> 의 name속성값 얻기
	    	String inputName = (String)files.nextElement();
	    	
	    	//6.2 톰캣 서버가 관리하는 실제 /upload/ 폴더에 업로드 하기전에 첨부한 파일의 [  원본파일명  ] 얻기 
			//    예 : 사용자 컴퓨터에 있던 첨부한 원본파일명 => "03이벤트.zip"
			//    이 이름은 나중에 다운로드할 때 사용자에게 보여줄 이름으로 사용합니다.
	    	String fileName = multipartRequest.getOriginalFileName(inputName);
	    	
	    	//6.3. 톰캣 서버가 관리하는 실제 /upload/ 폴더에 업로드된 파일의 [ 실제 파일명 ] 얻기 
	    	//    예 : 같은 이름이 이미 있었다면 "03이벤트1.zip"
			//    이 이름은 서버 폴더에서 파일을 찾을 때 사용합니다.
	    	String fileRealName = multipartRequest.getFilesystemName(inputName);
	    	
			/*
			참고. 원본파일명과 실제파일명을 둘 다 저장하는 이유

				첨부한 원본파일명 : 다운로드 링크에 사용자에게 보여주는 용도   
				-> "03이벤트.zip"
				
				업로드한 실제파일명 : 다운로드시 톰캣 서버가 관리하는 실제 /upload/ 폴더에서 브라우저로 다운로드 시킬 파일을 찾는 용도 
				-> "03이벤트1.zip"

				같은 이름의 파일을 두 번 업로드하면 이 둘이 서로 달라지기 때문에
				반드시 두 가지를 모두 저장해야 합니다.
			 */
	    	
	    	/* 6.4.  3개의 <input type="file"> 중  특정 <input>에 업로드할 파일을 첨부하지 않고 업로드 요청한 경우 건너뛰기
	    	         <input type="file"> 가 3개인데 특정 <input> 1개만 업로드할 파일을 첨부하고  업로드 요청하면 
	    	         나머지 <input type="file> 2개 로 요청한 값을 얻으면 모두 null이 됩니다.
	    	         이 검사를 하지 않으면 null이 그대로 DB에 저장되어 목록 화면이 깨집니다.
	    	         break가 아니라 continue를 쓰는 이유는 뒤에 다른 <input>에 첨부해서 업로드 요청한 파일이 있을 수 있기 때문입니다.
	    	*/
	    	if(fileName == null  || fileRealName == null) {
	    		continue;
	    	}
	    	
	    	/* 6.5.  업로드 요청시 첨부한 파일의 원본이름과 실제 업로드한 파일이름을 FILE 데이터베이스에 만들어 놓은 file테이블에 INSERT(추가)
	    	 		 반환값 : 추가에 성공하면 1,  추가에 실패 하면 0 */
	    	int result = fileDao.upload(fileName,  fileRealName);
	    	
	    	/* 6.6. DB에 insert(추가)까지 성공한 경우에만 성공 개수 1증가*/
	    	if(result == 1) {
	    		successCount++;
	    	}
	    	
	    } //end while
		
	    /* 7. 업로드 결과 건수를 request 내장객체 메모리에 바인딩
	          uploadResult.jsp화면에서 ${requestScope.successCount}로 꺼내 사용합니다.*/
	    request.setAttribute("successCount", successCount);
	    
	    
	    /*8. 업로드 결과 안내 화면인  uploadResult.jsp VIEW화면으로  재요청(포워딩)*/
	    request.getRequestDispatcher("/WEB-INF/views/uploadResult.jsp").forward(request, response);
	    
		/*
		참고. 목록 화면(/WEB-INF/views/fileList.jsp)으로 바로 가지 않고 
		     업로드 결과 안내 화면(/WEB-INF/views/uploadResult.jsp)을 재요청(포워딩) 해서 거치는 이유

			업로드 직후 사용자가 F5(새로고침)를 누르면
			방금 업로드 요청 전송한 POST 요청이 그대로 다시 요청 전송되어
			똑같은 파일이 한 번 더 업로드되는 문제가 발생합니다.

			업로드 결과 안내 화면(/WEB-INF/views/uploadResult.jsp)을 거쳐 
			링크를 클릭해 목록으로 이동하도록 유도하면 이 문제가 줄어듭니다.
	  */
	}

}









