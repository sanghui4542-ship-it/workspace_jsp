package Vo;

/*
 * ============================================================================
 *  BoardVo  -  상자(VO) : 자유게시판 글 1건의 정보를 담아 나르는 객체
 *
 *  DB 의 board 테이블 한 행(row) = BoardVo 객체 1개
 *  변수 이름은 board 테이블의 열 이름과 1:1로 같다.
 *
 *  board 테이블 열 목록
 *    b_idx     글번호       (자동 증가, 기본키)
 *    b_id      작성자 아이디
 *    b_pw      글 비밀번호   (수정·삭제할 때 본인 확인용)
 *    b_name    작성자 이름
 *    b_email   작성자 이메일
 *    b_title   글 제목
 *    b_content 글 내용
 *    b_group   정렬 그룹번호 (화면에 보이는 순서. 작을수록 위에 보인다)
 *    b_level   답글 들여쓰기 깊이 (0=원글, 1=답글, 2=답글의 답글 ...)
 *    b_date    작성일        (DB 가 now() 로 자동 채운다)
 *    b_cnt     조회수
 *
 *  [계층형 정렬 규칙 - b_group, b_level 이 하는 일]
 *    이 게시판은 "답글이 원글 바로 아래에 붙어 보이는" 계층형 구조다.
 *    화면은 항상 b_group 순서로 정렬해서 보여준다.
 *
 *      새 글 작성 : 기존 글 전체의 b_group 을 1씩 밀고, 새 글은 b_group=0 (맨 위)
 *      답글 작성 : 원글보다 큰 b_group 을 가진 글들을 1씩 밀고,
 *                 답글은 b_group = 원글의 b_group + 1 (원글 바로 아래)
 *                 답글은 b_level = 원글의 b_level + 1 (한 칸 더 들여쓰기)
 *
 *  getXxx() : 상자에서 값을 꺼낸다      예) vo.getB_title()
 *  setXxx() : 상자에 값을 넣는다(바꾼다)  예) vo.setB_title("제목")
 * ============================================================================
 */

// DB 의 날짜(date) 타입을 자바에서 다루는 타입
import java.sql.Date;

public class BoardVo {

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

	//----------------------------------------------------------------
	// 기본 생성자 : 빈 상자를 만든다. 값은 나중에 setXxx() 로 넣는다
	//----------------------------------------------------------------
	public BoardVo() {}

	//----------------------------------------------------------------
	// 생성자 : 상자를 만들면서 글 정보 11개를 한 번에 넣는다
	//  부르는 곳 : BoardDAO 의 mapRow() (DB 조회 결과 한 줄을 이 상자로 바꿀 때)
	//----------------------------------------------------------------
	public BoardVo(int b_idx, String b_id, String b_pw, String b_name, String b_email, String b_title, String b_content,
			int b_group, int b_level, Date b_date, int b_cnt) {

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
}
