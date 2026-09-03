package file.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import file.dao.FileDAO;



/*
   다운로드 GET 요청 주소 :  http://loaclhost:8181/pro15/download.do?fileRealName=다운로드시킬파일명

*/
@WebServlet("/download.do")
public class FileDownloadServlet extends HttpServlet{

	/* 업로드된 파일이 저장된 폴더이름 */
	private static final String UPLOAD_DIR = "upload"; //<=== 다운로드할 파일이 저장되어 있는 폴더 이름
	
	/* DB작업을 대신 시킬 FileDAO객체의 주소를 저장할 변수 선언*/
	private FileDAO  fileDao;
	
	/* init 메소드 오버라이딩해서 구현할 내용 : FileDAO객체 생성해서 위 변수에 저장 */
	@Override
	public void init() throws ServletException {
		fileDao = new FileDAO();
	}
	
	/* doGet 메소드 오버라이딩해서 구현할 내용 : 다운로드 시키고 DB작업*/
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		/*1. 브라우저가 보낸 요청 데이터 중 한글 문자 인코딩 방식 UTF-8 설정 */
		request.setCharacterEncoding("UTF-8");

		/*2. 목록화면(fileList.jsp)에서  a 링크로 전달한 실제 다운로드할 파일명을 request 내장객체 메모리에서 얻기*/
		String fileRealName = request.getParameter("fileRealName");
		
		//참고. <a href="http://loaclhost:8181/pro15/download.do?fileRealName=다운로드시킬파일명">다운로드</a>
	
		/*3. 다운로드할 파일명을 다운로드 요청시 전달하지 않았거나 공백만 전달되었으면 목록 화면(fileList.jsp)으로 되돌려 보내기(재요청)*/
		if(fileRealName == null ||  fileRealName.trim().length() == 0) {	
			response.sendRedirect("list.do"); //FileListServlet 재요청  후 내부적으로  uploadResult.jsp로 포워딩해서 보여줌 
			return;
		}
		/*
		참고. 경로 조작 공격(Path Traversal)이란?

			공격자가 주소창에 아래처럼 입력하는 공격입니다.

				download.do?fileRealName=../WEB-INF/web.xml

			이대로 실행되면 upload폴더가 아니라 그 위 폴더의 web.xml 파일이 다운로드되어
			DB 접속 정보 같은 중요한 서버 설정이 그대로 유출됩니다.

			파일명에는 원래 .. 이나 / 나 \ 가 들어갈 이유가 전혀 없으므로
			이 세 가지가 포함되어 있으면 무조건 차단합니다.
	   */
		/*4. 경로 조작 공격 차단 */
		if(fileRealName.contains("..") || fileRealName.contains("/") || fileRealName.contains("\\") ) {
			response.sendRedirect("list.do");
			return;
		}
		
		/* 5.1. 다운로드할 파일이 저장되어 있는 실제 톰캣 서버 하드디스크 내부의 upload폴더의 절대 경로 얻기*/
		String savePath = request.getServletContext().getRealPath("/" + UPLOAD_DIR);
		
		/* 5.2. 다운로드할 파일의 폴더 경로와 다운로드할 파일명을 결합해서 다운로드할 파일에 접근할 File클래스의 객체 생성 */
		// 참고.  new File(다운로드할폴더경로, 다운로드할파일명); 형태로 만들면 운영체제에 맞는 구분자 기호 / 또는 \\ 가 자동으로 엮어서 경로 저장
		File downFile = new File(savePath, fileRealName);
							   //"c:\workspace\pro15\src\webapp\ upload\다운로드할파일명"
		
		/* 6. 실제 다운로드할 파일이 존재하지 않으면 목록화면(fileList.jsp)으로 되돌려 보내기(포워딩)*/
		if(!downFile.exists()) {
			response.sendRedirect("list.do");
			return; //doGet 메소드 종료 시켜  바로 아래 코드가 실행되지 않도록 하자.
		}
		/*
		참고. 원본 파일명을 다시 조회하는 이유

			같은 이름의 파일을 두 번 업로드하면 서버에는 아래처럼 저장됩니다.

				원본파일명 : 보고서.hwp        실제 업로드된 파일명 : 보고서.hwp
				원본파일명 : 보고서.hwp        실제 업로드된 파일명 : 보고서1.hwp

			사용자에게는 보고서1.hwp 가 아니라 원래 이름인 보고서.hwp 로 저장되어야 하므로
			실제 파일명으로 원본 파일명을 DB에서 다시 찾아옵니다.
	   */		
		/*7.1. file테이블에서 실제 파일명에 해당하는 원본파일명 조회*/
		String fileName = fileDao.selectOriginName(fileRealName);
		
		/*7.2. DB에 원본 파일명이 없으면 다운로드시킬 실제 파일명을 그대로 사용*/
		//     이렇게 처리 해야 DB 기록 없어도 다운로드 자체는 정상 동작 합니다.
		if(fileName == null) {
			fileName = fileRealName;
		}
		
		/*8. file 테이블의 downloadcount 열의 값을 1증가 시키기(다운로드 시도한 횟수 1증가 시키기)*/
		//   update 문장의 내용은 FileDAO만 알고 있고 이 서블릿은 알지 못합니다.
		fileDao.hit(fileRealName);
		
		/*
		참고. 응답 헤더(header)란?

			브라우저에게 "지금부터 보내는 내용을 이렇게 처리해라" 라고 알려주는 지시서 입니다.

			중요. 헤더는 반드시 아래 10.의 실제 파일 내용을 내보내기 "전에" 설정해야 합니다.
			     내용을 먼저 내보내면 헤더 설정이 무시됩니다.
		*/
		/* 9.1. 응답할 데이터의 유형(MIME-TYPE)를 지정 
				application/octet-stream : 종류를 알 수 없는 이진 데이터라는 뜻
				이렇게 지정해야 브라우저가 화면에 열지 않고 파일로 저장합니다.
				만약 text/html로 지정하면 파일 내용이 화면에 깨진 글자로 표시됩니다.
		*/
		response.setContentType("application/octet-stream");
		
		/*9.2. 응답할 데이터의 전체 크기를 byte 단위로 블라우저에게 알려주기 
		       크기를 알려줘야 브라우저가 다운로드 진행률과 남은 시간을 표시할 수 있습니다.*/
		response.setContentLengthLong(downFile.length());
		
		/* 참고. 다운로드할 파일의 한글 파일명 처리

			응답 헤더에는 영문과 숫자만 담을 수 있습니다.
			따라서 한글 파일명을 그대로 넣으면 ???.hwp 처럼 깨져서 저장됩니다.

			URLEncoder.encode(문자열, "UTF-8")
			 -> 한글을 %EB%B3%B4 같은 영문 기호로 변환해 줍니다.

			replaceAll("\\+", "%20")
			 -> URLEncoder는 공백을 + 기호로 바꾸는데
			    헤더에서는 +가 공백으로 되돌아가지 않고 + 글자 그대로 남습니다.
			 -> 그래서 공백을 뜻하는 %20으로 다시 바꿔줍니다.
		 */
		/*9.3. 다운로드할 한글 파일명이 꺠지지 않도록 URL 인코딩 처리*/
		String encodeName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
		
		/* 9.4. 다운로드 지시 헤더 설정
		   attachment  : 화면에 열지 말고 파일로 저장하라는 지시
		   filename    : 구형 브라우저가 읽는 다운로드 파일명
		   filename*   : 한글을 지원하는 최신 브라우저가 읽는 다운로드 파일명
		   두 가지를 함께 넣어야 어떤 브라우저에서도 다운로드 파일명이 깨지지 않습니다.		
		*/
		response.setHeader("Content-Disposition", 
				            "attachment; filename=\"" + encodeName + "\"; filename*=UTF-8''" + encodeName);
		
		/* 10. 톰캣 서버 하드디스크 공간 upload폴더에서 파일을 읽어서 브라우저로 그대로 내보내기 (다운로드)*/
		try(FileInputStream fis = new FileInputStream(downFile); //톰캣 서버 다운로드할 파일의 내용을 읽어들일 통로 
			OutputStream  os = response.getOutputStream()){      // 브라우저로 읽어들인 파일 정보 내보내는 출력 스트림 통로 
			
			/*10.1. 한번에 8KB 씩 읽어 옯겨 저장할 임시 저장공간 만들기*/
			byte[] buffer = new byte[8192];
			
			/*10.2. 이번 회차에 실제로 파일에서 읽어온 바이트 수를 저장할 변수 선언*/
			int readCount;
			
			/*10.3. read() 메소드는 읽어온 바이트 수를 반환하고   
			 *      더이상 파일에서 읽을 내용이 없으면 -1을 반환하므로 그때까지 계속 반복 */
			while( (readCount = fis.read(buffer)) != -1 ) {
				
				/*10.4. 실제 읽어 온 바이트수  만큼만 브라우저로 내보내기 
				  		os.write(buffer) 라고 쓰면 안 되는 이유
				  		-> 마지막 회차에는 8192byte를 다 못 채우는데
				  		   배열 전체를 내보내면 앞 회차의 쓰레기 값이 함께 전송되어 파일이 손상됩니다.*/
				os.write(buffer, 0, readCount);
			}
			
			/* 10.5 통로에 남아 있는 마지막 조각 데이터까지 모두 강제로 브라우저로 내보내기 */
			os.flush();
			
		} //end try
	
	} //doGet 메소드 끝 
	
} //FileDownloadServlet클래스 끝










