package Vo;

/*
 * ============================================================================
 *  FileBoardVo  -  상자(VO) : 자료실(파일게시판) 글 1건의 정보를 담아 나르는 객체
 *
 *  DB 의 fileboard 테이블 한 행(row) = FileBoardVo 객체 1개
 *  구조는 BoardVo(자유게시판)와 거의 같고, 첨부파일 관련 열 3개가 추가된다.
 *
 *  fileboard 테이블 열 목록
 *    b_idx      글번호       (자동 증가, 기본키)
 *    b_id       작성자 아이디
 *    b_pw       글 비밀번호   (수정·삭제할 때 본인 확인용)
 *    b_name     작성자 이름
 *    b_email    작성자 이메일
 *    b_title    글 제목
 *    b_content  글 내용
 *    b_group    정렬 그룹번호 (화면에 보이는 순서. 작을수록 위에 보인다)
 *    b_level    답글 들여쓰기 깊이 (0=원글, 1=답글, 2=답글의 답글 ...)
 *    b_date     작성일        (DB 가 now() 로 자동 채운다)
 *    b_cnt      조회수
 *    ofile      원본 파일명 목록 (사용자가 올린 그대로의 이름. 여러 개면 ; 로 이어 붙인다)
 *    sfile      저장 파일명 목록 (디스크에 실제로 저장된 UUID 이름. 여러 개면 ; 로 이어 붙인다)
 *    downcount  이 글의 첨부파일이 다운로드된 횟수
 *
 *  [ofile / sfile 을 세미콜론(;)으로 이어 붙여 저장하는 이유]
 *    한 글에 여러 개의 첨부파일이 달릴 수 있는데, 테이블 열 하나에는 글자
 *    하나만 담을 수 있다. 그래서 "a.pdf;b.txt;c.jpg" 처럼 세미콜론으로
 *    이어 붙여 한 칸에 여러 파일명을 담고, 화면에서 쓸 때는 getSfileList()
 *    / getOfileList() 로 다시 배열로 쪼갠다.
 *
 *  [원본 파일명과 저장 파일명을 다르게 쓰는 이유]
 *    ofile(원본명)은 화면에 보여주고 다운로드할 때 사용자가 보는 이름이다.
 *    sfile(저장명)은 디스크에 실제로 저장되는, 겹칠 걱정이 없는 무작위(UUID)
 *    이름이다. 이렇게 나누면 같은 이름의 파일을 여러 명이 올려도 서로
 *    덮어쓰지 않고, 파일명에 위험한 문자가 섞여 있어도 디스크에는 안전한
 *    이름으로만 저장된다.
 *
 *  getXxx() : 상자에서 값을 꺼낸다      예) vo.getB_title()
 *  setXxx() : 상자에 값을 넣는다(바꾼다)  예) vo.setB_title("제목")
 * ============================================================================
 */

// DB 의 날짜(date) 타입을 자바에서 다루는 타입
import java.sql.Date;

public class FileBoardVo {

	// 글 1건의 정보를 저장할 변수들 (private : 이 클래스 밖에서는 get/set 메소드로만 사용할 수 있다)
	private int b_idx;
	private String b_id;
	private String b_pw;
	private String b_name;
	private String b_email;
	private String b_title;
	private String b_content;
	private int b_group;
	private int b_level;
	private Date b_date;
	private int b_cnt;
	private String ofile;   // 원본 파일명 목록 (; 로 이어 붙인 값)
	private String sfile;   // 저장 파일명 목록 (; 로 이어 붙인 값)
	private int downcount;  // 첨부파일 다운로드 횟수

	//----------------------------------------------------------------
	// 기본 생성자 : 빈 상자를 만든다. 값은 나중에 setXxx() 로 넣는다
	//  부르는 곳 : FileBoardService 의 serviceInsertBoard() (새 글을 만들 때)
	//----------------------------------------------------------------
	public FileBoardVo() {}

	//----------------------------------------------------------------
	// 생성자 : 상자를 만들면서 글 정보 14개를 한 번에 넣는다
	//  부르는 곳 : FileBoardDAO 의 mapRow() (DB 조회 결과 한 줄을 이 상자로 바꿀 때)
	//----------------------------------------------------------------
	public FileBoardVo(int b_idx, String b_id, String b_pw, String b_name, String b_email, String b_title,
			String b_content, int b_group, int b_level, Date b_date, int b_cnt, String ofile, String sfile,
			int downcount) {

		// 부모 클래스(Object)의 생성자를 먼저 부른다
		super();

		// 매개변수로 받은 값을 이 상자의 변수에 저장한다 (this.b_idx = 상자의 b_idx 변수)
		this.b_idx = b_idx;
		this.b_id = b_id;
		this.b_pw = b_pw;
		this.b_name = b_name;
		this.b_email = b_email;
		this.b_title = b_title;
		this.b_content = b_content;
		this.b_group = b_group;
		this.b_level = b_level;
		this.b_date = b_date;
		this.b_cnt = b_cnt;
		this.ofile = ofile;
		this.sfile = sfile;
		this.downcount = downcount;
	}

	// 글번호 꺼내기 / 넣기
	public int getB_idx() {
		return b_idx;
	}

	public void setB_idx(int b_idx) {
		this.b_idx = b_idx;
	}

	// 작성자 아이디 꺼내기 / 넣기
	public String getB_id() {
		return b_id;
	}

	public void setB_id(String b_id) {
		this.b_id = b_id;
	}

	// 글 비밀번호 꺼내기 / 넣기
	public String getB_pw() {
		return b_pw;
	}

	public void setB_pw(String b_pw) {
		this.b_pw = b_pw;
	}

	// 작성자 이름 꺼내기 / 넣기
	public String getB_name() {
		return b_name;
	}

	public void setB_name(String b_name) {
		this.b_name = b_name;
	}

	// 작성자 이메일 꺼내기 / 넣기
	public String getB_email() {
		return b_email;
	}

	public void setB_email(String b_email) {
		this.b_email = b_email;
	}

	// 글 제목 꺼내기 / 넣기
	public String getB_title() {
		return b_title;
	}

	public void setB_title(String b_title) {
		this.b_title = b_title;
	}

	// 글 내용 꺼내기 / 넣기
	public String getB_content() {
		return b_content;
	}

	public void setB_content(String b_content) {
		this.b_content = b_content;
	}

	// 정렬 그룹번호 꺼내기 / 넣기
	public int getB_group() {
		return b_group;
	}

	public void setB_group(int b_group) {
		this.b_group = b_group;
	}

	// 답글 들여쓰기 깊이 꺼내기 / 넣기
	public int getB_level() {
		return b_level;
	}

	public void setB_level(int b_level) {
		this.b_level = b_level;
	}

	// 작성일 꺼내기 / 넣기
	public Date getB_date() {
		return b_date;
	}

	public void setB_date(Date b_date) {
		this.b_date = b_date;
	}

	// 조회수 꺼내기 / 넣기
	public int getB_cnt() {
		return b_cnt;
	}

	public void setB_cnt(int b_cnt) {
		this.b_cnt = b_cnt;
	}

	// 원본 파일명 목록(; 로 이어 붙인 값) 꺼내기 / 넣기
	public String getOfile() {
		return ofile;
	}

	public void setOfile(String ofile) {
		this.ofile = ofile;
	}

	// 저장 파일명 목록(; 로 이어 붙인 값) 꺼내기 / 넣기
	public String getSfile() {
		return sfile;
	}

	public void setSfile(String sfile) {
		this.sfile = sfile;
	}

	// 다운로드 횟수 꺼내기 / 넣기
	public int getDowncount() {
		return downcount;
	}

	public void setDowncount(int downcount) {
		this.downcount = downcount;
	}

	// ============================================================
	// 다중 파일 헬퍼 메소드
	//  sfile, ofile 은 ';' 로 이어 붙인 여러 파일명 문자열이다
	//  예) sfile = "file1.pdf;file2.txt;file3.jpg"
	//  화면(JSP)에서 한 줄씩 나열하기 쉽도록 배열로 쪼개 준다
	// ============================================================

	/**
	 * 저장된 파일명 목록을 배열로 반환한다.
	 * 예: "a.pdf;b.txt" → ["a.pdf", "b.txt"]
	 */
	public String[] getSfileList() {
		if (sfile == null || sfile.trim().isEmpty()) return new String[0];   // 첨부가 없으면 길이 0짜리 빈 배열을 돌려준다 (null 을 돌려주면 화면에서 오류가 난다)
		return sfile.split(";");   // ";" 를 기준으로 잘라 파일명 배열로 만든다
	}

	/**
	 * 원본 파일명 목록을 배열로 반환한다.
	 * 예: "a.pdf;b.txt" → ["a.pdf", "b.txt"]
	 */
	public String[] getOfileList() {
		if (ofile == null || ofile.trim().isEmpty()) return new String[0];   // 첨부가 없으면 길이 0짜리 빈 배열을 돌려준다
		return ofile.split(";");   // ";" 를 기준으로 잘라 원본 파일명 배열로 만든다
	}
}
