package sec01.ex01;

import java.io.File;
import java.io.IOException;
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


/*
	FIleUpload 서블릿
	- 브라우저 화면(uploadForm.jsp)에서 첨부한 파일에 관한  파일업로드 요청을 받았을때 그 기능을 처리하는 서블릿 클래스 입니다.

	하는일 1. commons-fileupload-1.3.3.jar 라이브러리 압축파일 안에서 제공하는  DiskFileItemFactory 클래스를 이용해
	         업로드되는 톰캣 서버의 하드디스크 경로 C:\\file_repo 위치와  한번에 업로드 가능한 최대 파일 크기를 설정합니다.
	         
	하는일 2. 그리고 ServletFileUpload 클래스를 이용해 파일 업로드 요청화면(uploadForm.jsp)에서 업로드 요청한 파일과
	         요청한 파라미터에 대한 정보를 가져와 파일업로드 기능을 처리하고 요청한 파라미터들을 얻어 브라우저에 출력합니다.        
*/

@WebServlet("/upload.do")
public class FIleUpload extends HttpServlet {
	
	//클라이언트가 GET  또는 POST 요청을 하면 모든 요청(request)을 받아서 처리 하는 메소드 
	protected void doHandle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//1. 요청한 파라미터 한글처리 
		request.setCharacterEncoding("UTF-8"); 
		
		//2. 업로드되는 톰캣서버의 하드디스크의 경로(업로드할 파일의 경로)와 연결된 File 클래스의 객체 메모리 생성
		//요약 : 업로드될 폴더 경로 설정
		File  currentDirPath = new File("C:\\file_repo");
		
		//2-1. 업로드할 파일 데이터를 임시로 저장할 DiskFileItemFactory 클래스의 객체 메모리 생성 
		DiskFileItemFactory  factory = new DiskFileItemFactory();
		
		//2-1-1. 파일 업로드시 ~~ 사용할 임시로 저장할 DiskFileItemFactory객체 메모리 최대 크기를 1MB로 설정
		factory.setSizeThreshold(1024 * 1024 * 1);
		
		//2-1-2. 임시로 저장할 DiskFileItemFactory 객체 메모리에 파일업로드시~ 설정한 1MB 크기를 넘을 경우!
		//		 실제 업로드될 C:\\file_repo 폴더에 접근하기 위한 FIle클래스의 객체를 DikFileItemFactory객체의 생성자로 전달해 저장
		factory.setRepository(currentDirPath);
		
//참고.   DiskFileItemFactory클래스는 업로드할 파일의 크기가 설정한 임시메모리의 크기(1MB)를 넘기전까지는
//	     업로드한 파일 데이터를 임시메모리에 저장하고 설정한 크기(1MB)를 넘길 경우 
//	     업로드할 C:\\file_repo폴더로 업로드해서 저장시키는 역할을 함		
		
		//3. 업로드할 파일의 임시 메모리 DiskFileItemFactory객체의 주소를 생성자로 전달해 저장시킨!!!!
		//   파일 업로드 기능을 실제 처리하는 ServletFileUpload클래스의 객체 생성
		ServletFileUpload upload = new ServletFileUpload( factory );
		
		try {
			/*
			4. ServletFileUpload클래스의 parseRequest메소드
			- HttpServletRequest객체를 인자로 전달 하면?
			  uploadForm.jsp 파일업로드 요청하는 디자인 페이지에서  
			    첨부한 파일 2개와  입력한 파라미터3개의 요청 정보들 HttpServletRequest객체에서 꺼내와서  
			    각각의 DiskFileItem객체들에 저장한 후 각각의 DiskFileItem객체들을  ArrayList배열에 추가 하게 됩니다. 
			    그후 ~ ArrayList배열 자체를 반환 해 줍니다.
			 */	
			List items = upload.parseRequest(request);
			/*
				ArrayList 배열
				[ DiskFileItem 객체,   DiskFileItem 객체,  DiskFileItem 객체,  DiskFileItem 객체,  DiskFileItem 객체 ]		
			*/
			
			//5. ArrayList 배열에 저장된 DiskFileItem객체(요청한 아이템)들의 갯수 만큼 반복
			for(int i=0;    i<items.size();    i++) {
				
				//ArrayList배열에 저장된 DiskFileItem객체를 하나씩 반복해서 얻는다.
				FileItem fileItem = (FileItem)items.get(i);
				
				//얻은 DiskFileItem객체의 정보가 첨부한 파일 요청에 대한 요청이 아니고
				//입력한 텍스트 요청 정보가 저장된 DiskFileItem객체일 경우?
				if(fileItem.isFormField()) {
					
					System.out.println( fileItem.getFieldName() + "=" + fileItem.getString("UTF-8") );
					
				}else { //얻은 DiskFileItem객체의 정보가 첨부한 파일의 요청정보가 저장된 경우?
					
					System.out.println("<input type='file'>의 name속성값 : " + fileItem.getFieldName() );					
					System.out.println("업로드 요청시 첨부한 파일명 : " + fileItem.getName());			
					System.out.println("업로드 요청시 첨부한 파일크기 : " + fileItem.getSize() + "bytes");
					
					//업로드 요청시 첨부한 파일의 크기가 0bytes 보다 크면? (파일첨부후 서블릿으로 파일 업로드 요청 했다면?)
					if(fileItem.getSize() > 0) {
						
						//업로드시 첨부해서 업로드 요청한 파일명뒤에서부터 \\문자열이 포함되어 있는지 찾는데.. 포함되어 있으면?
						//시작 문자의 index 위치 번호를 반환해 오고, 만약  \\문자열이 포함되어 있지 않으면 -1을 반환해서 저장
						int idx = fileItem.getName().lastIndexOf("\\");
													 
						if(idx == -1) { //업로드시 첨부해서 업로드 요청한 파일명에  \\  가 포함되어 있지 않으면?
							
							//  '/' 문자열이 업로드요청한 파일명에 포함되어 있지 않는지? 검사
							//  '/' 포함되어 있지 않으면? lastIndexOf메소드는 -1 을 반환 합니다.
							idx = fileItem.getName().lastIndexOf("/");
							System.out.println("업로드시 첨부해서 업로드 요청한 파일명에  / 기호는 포함되어 있지 않다.");
						}
						//6. 업로드시 첨부해서 업로드 요청한 파일명 얻어 변수에저장
						String fileName = fileItem.getName().substring(idx+1);
						
						//7. 업로드 첨부해서 업로드 요청한 파일명  + 파일이 업로드될 폴더 경로를 하나로 합쳐서  전체 업로드 경로를 만들어 
						//	 File클래스의 객체로 접근해서 업로드 하기 위해 File클래스의 객체 생성
						File uploadFile = new File( currentDirPath + "\\" + fileName     );
											//          "C:\\file_repo\\업로드 요청한 파일명"
						
						//8. 실제 "C:\\file_repo\\업로드 요청한 파일명"   업로드 처리 
						fileItem.write(uploadFile);
						
					} //바깥 else 안의 바깥 if		
					
				} //바깥 else
			
			} //for
			
		}catch (Exception e) {
			e.printStackTrace(); //파일업로드 처리시 오류 발생하면 메세지 출력 
		}
		
		
	}

	@Override  //클라이언트가 GET 방식으로 요청하면 요청을 받아 처리 하는 메소드 오버라이딩 
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doHandle(request, response);
	}

	@Override  //클라이언트가 POST 방식으로 요청하면 요청을 받아 처리 하는 메소드 오버라이딩 
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doHandle(request, response);
	}

}
