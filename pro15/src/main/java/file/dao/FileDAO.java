package file.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import file.vo.FileVO;

public class FileDAO {
	
	/* 1. 톰캣이 만들어 놓은 커넥션 풀(DataSource) 객체의 주소를 저장할 변수 선언*/
	private static DataSource dataSource;
	
	/*
	참고. static 초기화 블록

		이 클래스가 메모리에 최초로 올라갈 때 딱 한 번만 자동 실행되는 블록 입니다.
		생성자는 객체를 만들 때마다 실행되지만 static블록은 프로그램 전체에서 단 한 번만 실행됩니다.

		요약 : 커넥션 풀을 찾는 작업은 한 번만 하면 되므로 static블록에 작성합니다.
	*/
	static {
		
		try {
			/* 2. 톰캣이 관리하는 JNDI 저장소의 시작 경로에 접근하는 객체 얻기*/
			Context ctx = new InitialContext();
			
			/* 3. context.xml에  name="jdbc/file"로 등록해 놓은 커넥션 풀을 이름으로 찾아 얻기*/
			dataSource = (DataSource) ctx.lookup("java:comp/env/jdbc/file");
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	/* 4. 커넥션 풀(DataSource)에서  Connection 객체 하나 빌려 반환하는 메소드*/
	private Connection getConnection() throws Exception {
		
		return  dataSource.getConnection(); //return  new Connection();
	}
	
	/* 5. 업로드한 파일 정보를 file테이블에 추가(INSERT)하는 메소드 
		
		  매개변수 fileName     :  업로드 요청시 첨부했던 원본파일명 전달 받음
		  매개변수 fileRealName : 실제 upload 폴더에 업로드된 파일명 전달 받음
		  반환 값 			  : 추가(INSERT)에 성공하면 1,  실패하면 -1 
	*/
	public int upload(String fileName,  String fileRealName) {
		
		/*5.1. file 테이블에 추가할 insert 문장 만들기 
		       참고. downloadcount 열에는 처음 업로드한 상태이므로 0을 직접 지정 */		
		String sql = "insert into file(filename, filerealname, downloadcount) values(?,  ?,  0)";
		
		/*5.2. 커넥션 풀에서 Connection객체 하나 빌려와 DB와 연결, insert문을 미리 로드한 PreparedStatementg 실행객체 얻기*/
		try(Connection conn = this.getConnection();    
			PreparedStatement pstmt = conn.prepareStatement(sql)){
			
			pstmt.setString(1, fileName);     /*5.3. 첫번째 ? 자리에 첨부한 원본파일명 채우기*/
			pstmt.setString(2, fileRealName); /*5.4. 두번째 ? 자리에 실제 upload폴더에 업로드된 파일명 채우기*/
			
			/*5.5. 완성된 insert 문장 전체를 file 테이블에 전송해서 실행
			       executeUpdate()는 추가/수정/삭제된 행의 개수를 반환합니다. (insert 추가 성공시 1 반환)*/
			return pstmt.executeUpdate();
		
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		/*6. 예외가 발생해서 insert(추가)에 실패 했으면 개발자가 정한 -1을 반환*/
		return -1;
		
	}//upload 메소드 
	
	
	/* 6. file 테이블에 저장된 업로드한 모든 파일 정보를 조회(SELECT)하는 메소드 
		
		 반환값 : 조회된 FileVO객체들이 저장된 ArrayList배열
		  		조회된 레코드가 0건이어도 null이 아닌  비어 있는 ArrayList배열을 반환합니다.
		  		
		 참고. FileListServlet.java의 doGet메소드 안에서 호출하는 메소드 이다.
	*/
	public ArrayList<FileVO> selectAll(){
		
		/* 6.1. 조회된 FileVO객체들을 저장할 ArrayList 배열 생성 */
		ArrayList<FileVO> list = new ArrayList<FileVO>();
		
		/* 6.2. 조회한 select 문장 만들기
		        order by filerealname desc : 최근에 올린 파일이 목록 맨 위로 오도록 역순 정렬 */
		String sql = "select  filename,  filerealname, downloadcount from file order by filerealname desc";
		
		/* 6.3. Connection 빌리기 -> PreparedStatement 실행 객체 얻기 -> select 실행해서 조회결과를 ResultSet에 담아 얻기 */
		try(Connection conn = this.getConnection();  
			PreparedStatement pstmt = conn.prepareStatement(sql);  
			ResultSet rs = pstmt.executeQuery()){
			/*
			참고. ResultSet 임시 객체 메모리 모습

			커서 ->    열 명       filename      filerealname     downloadcount
			          조회데이터   보고서.hwp     보고서.hwp             0
			          조회데이터   보고서.hwp     보고서1.hwp            3

			-----------------------------------------------------------

			rs.next()를 1번 호출했을 경우

			           열 명       filename      filerealname     downloadcount
			커서 ->     조회데이터   보고서.hwp     보고서.hwp             0
			           조회데이터   보고서.hwp     보고서1.hwp            3

			요약 : 처음 커서는 첫 번째 데이터 줄의 "위"를 가리키고 있습니다.
			      그래서 반드시 next()를 먼저 호출해야 첫 줄의 데이터를 읽을 수 있습니다.
		  */			
			
			/*6.4. 커서를 한줄 씩 아래로 내려서 가리키는 조회된 레코드 줄이 있으면 계속 반복
			       더 이상 읽을 줄이 없으면 next()가 false 반환하여 반복이 끝납니다.*/
			while(rs.next()) {
				
				/*6.5. 커서가 가리키는 한 줄의 조회 레코드 정보 3개를 각각 얻어 FileVO객체를 생성하면서 생성자로 전달해 저장*/
				FileVO filevo = new FileVO(rs.getString("filename"), 
						                   rs.getString("filerealname"), 
						                   rs.getInt("downloadcount"));
				
				/*6.6. ArrayList배열의 index 위치 칸에 FileVO객체 주소 추가 */
				list.add(filevo);
				//   ArrayList배열 모습
				//   [ new FileVO(..), new FileVO(..), new FileVO(..) ]
			}			
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		/*7. selectAll() 메소드를 호출한 FileListServlet으로 Model(ArrayList배열)을 반환*/
		return list;
		
	} //selectAll()메소드 끝
	
	
	
	
	/* 7. 실제 다운로드할_파일명으로  첨부해서 업로드 요청했던 원본파일명 하나만 조회하는 메소드 
	 
	 	  매개변수 fileRealName :  실제 upload 폴더에 저장된 업로드된(다운로드할) 파일명 전달 받음 
	 	  반환 값               :  업로드 요청시 첨부했던 원본파일명, 조회된 레코드가 없으면 null
	 	  
	 	  참고. FileDownloadServlet.java의 doGet메소드 안에서 호출하는 메소드 입니다.
	 	  	 	  
	  이 메소드가 필요한 이유
		 -> 같은 이름의 파일을 두 번 업로드하면 서버에는 보고서1.hwp 처럼 숫자가 붙어 저장됩니다.
		 -> 다운로드 요청하는 사용자에게는 원래 이름인 보고서.hwp 로 다운로드되어야 하므로
		    실제 파일명으로 원본 파일명을 다시 조회해서 찾아와야 합니다.
	 */
	public String selectOriginName(String fileRealName) {
		
		//1. 실제 다운로드할 파일명이 일치하는 행의 원본 파일명만 조회하는 select 문장 만들기
		String sql = "select filename from file where filerealname = ?";
		
		//2. Connection 연결통로 빌리기 -> select문을 미리 로드한 PreparedStatement 실행 객체 얻기
		try(Connection conn = this.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)){
			
			//3. ? 자리에 조건값인 실제 다운로드할 파일명으로 채우기 
			pstmt.setString(1, fileRealName);
			
			//4. select문을 실행해서 조회 결과를 ResultSet객체에 담아 얻기 
			try(ResultSet rs = pstmt.executeQuery()){
				
				//5. 조회된 레코드가 한 줄이라도 있으면 원본 파일명을 반환 
				if(rs.next()) {
					return rs.getString("filename");   // <===== 다운로드할 파일명으로 조회한 원본파일명 
				}
			}		
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		//6. 조회된 레코드가 없거나 예외가 발생했으면 null을 반환
		//   이 메소드를 호출한 쪽(FileDownloadServlet.java 서블릿의 doGet메소드 안쪽)에서  null을 검사해 실제파일명을 그대로 사용되도록 처리함
		return null;
			
	} //selectOriginName 메소드 끝 
	
	
	
	/* 8. 다운로드 할때 마다 해당 파일의 다운로드 횟수를 1 증가 시키는 메소드 
	 
	 	  매개변수 fileRealName : 다운로드를 시도한 파일의 실제 파일명 전달 받음 
	 	  반환값                : UPDATE(수정)에 성공하면 1,  실패하면 -1 
	 	  
	 	  참고. FileDownloadServlet.java의 doGet메소드 안에서 호출하는 메소드 입니다.
	 */
	public int hit(String fileRealName) {
		
		/*참고. UPDATE 작성 방법
			
				 update 테이블명 set 수정할값이저장될_열명 = 수정할값, ......
				 where  조건열명 = 조건값;
				 
			downloadcount = downloadcount + 1 의 의미
			 -> 현재 열에 저장되어 있는 값을 읽어서 1을 더한 값으로 다시 저장하라는 뜻입니다.
			 -> 자바에서 값을 읽어와 1을 더해 다시 update하는 방식보다 안전합니다.
			    (여러 사용자가 동시에 다운로드해도 횟수가 정확하게 누적되기 때문)

			주의. where절을 빼먹으면 file테이블의 모든 행의 다운로드 횟수가 함께 증가합니다.
		*/
		
		//1. 다운로드 횟수 1증가 시키는 update 문장 만들기
		String sql = "update file set downloadcount = downloadcount + 1 "
				   + "where filerealname = ?";
		
		//2. Connection DB연결 통로 빌리기 -> update문을 미리로드한 PreparedStatement 실행 객체 얻기
		try(Connection conn = this.getConnection();   
		    PreparedStatement pstmt = conn.prepareStatement(sql)){
			
			//3. ? 자리에 조건값인 실제 다운로드할 파일명 채우기 
			pstmt.setString(1, fileRealName);
			
			//4. 완성된 update 전체 문장을 file 테이블에 전송해서 실행!
			//   반환값 - 수정된 행의 갯수를 반환합니다. (update 구문 실행 성공시 1반환)
			return  pstmt.executeUpdate(); 
		//  return  1;	
				
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		//5. 예외가 발생해서 update 수정에 실패 했으면 
		//   개발자가 정한 -1 값 FileDownloadServlet.java의 doGet메소드 안으로 반환
		return -1;
		
	} //hit 메소드 끝 
	
	

} //FileDAO 클래스 끝

















