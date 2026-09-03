package file.vo;

/*
참고.
	VO(Value Object) 클래스는
	DB의 테이블에서 조회된 한 줄(하나의 행 = 레코드)의 정보를 저장해 놓고 관리하는 클래스 입니다.

	실제 File이라는 이름의 데이터베이스 내부에 만들어져 있는 file테이블의 모습

	    열 명       filename        filerealname       downloadcount
	    조회데이터   보고서.hwp      보고서.hwp              0
	    조회데이터   보고서.hwp      보고서1.hwp             3

	위 표에서 한 줄(한 행)의 정보를 저장하는 것이 FileVO객체 1개 입니다.
	따라서 조회된 레코드가 2줄이면 FileVO객체도 2개가 생성됩니다.
*/
public class FileVO {
	
	/* 1. 조회된 한 줄의 레코드 정보를 저장할 변수 선언 
	      private로 선언하는 이유 : 외부 클래스에서 직접 변수값을 바꾸지 못하게 막고 
	      						반드시 아래의 setter 메소드를 통해서만 값을 저장하기 위해서 
	*/	
	private String fileName;     // 업로드 요청시 첨부했던 원본 파일명을 저장할 용도의 변수 	
    private String fileRealName; // 실제 톰캣 서버의  upload 폴더에 업로드된 실제 파일명을 저장할 용도의 변수 
    private int downloadCount;   // 다운로드를 시도한 횟수를 저장할 용도의 변수        
	
	/* 2. 기본생성자 
		  JSP화면에서  EL( ${ vo.fileName } )이 이 객체를 다룰 때 반드시 필요합니다.
		  아래 3. 의 생성자를 만들면 기본생성자가 자동으로 만들어지지 않으므로 개발자가 직접 선언해야 합니다. */
    public FileVO() {}

	
    /* 3. 조회된 레코드 한줄의 정보 3개를 한번에 매개변수로 전달 받아 위 변수에 저장하는 생성자 
          DAO에서 ResultSet으로 조회한 값을 한 줄로 얻어 FileVO객체를 만들때 사용합니다. */
	public FileVO(String fileName, String fileRealName, int downloadCount) {
		this.fileName = fileName;
		this.fileRealName = fileRealName;
		this.downloadCount = downloadCount;
	}
	/*
	참고.
		getter메소드는 private으로 막아 놓은 변수에 저장된 값을 외부에서 얻어갈 때 호출하는 메소드 입니다.

		JSP화면에서 사용하는 EL 표기법과 실제 호출되는 메소드의 관계

			${vo.fileName}      ->  vo.getFileName()      메소드 호출 결과
			${vo.fileRealName}  ->  vo.getFileRealName()  메소드 호출 결과
			${vo.downloadCount} ->  vo.getDownloadCount() 메소드 호출 결과

		요약 : getter메소드가 없으면 JSP화면에서 EL로 값을 꺼낼 수 없습니다.
	 */
    //4. 변수에 저장된 값을 외부로 반환하는 getter 메소드들 
	public String getFileName() {  return fileName; }
	public String getFileRealName() {  return fileRealName; }
    public int    getDownloadCount() { return downloadCount; }
    
    //5. 외부에서 전달한 값을 변수에 저장하는 setter 메소드들 
    //   DAO에서 조회한 값을 FileVO객체의 각 변수에 하나씩 채워 넣을 때 사용합니다.
    public void  setFileName(String fileName)   { this.fileName = fileName;  }
    public void  setFileRealName(String fileRealName) { this.fileRealName = fileRealName; }
	public void setDownloadCount(int downloadCount) {   this.downloadCount = downloadCount; } 
    
    //setter 만들기 단축 메뉴  ->   alt  + shift  + s  누른 후   r 누름 
	
} // FileVO 클래스 







