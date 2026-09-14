package Vo;

import java.sql.Date;

public class BoardVo {

	//글 1건의 정보를 저장할 변수들 (private)
	private int b_idx;  //글번호(PK)
	private String b_id; //작성자 아이디
	private String b_pw; //글 비밀번호'
	private String b_name; //작성자 이름
	private String b_email; //작성자 이메일
	private String b_title; //글 제목
	private String b_content; //글 내용
	private int b_group; //부모글(주글)과 답변글을 묶는 그룹번호
	private int b_level; //부모글과 답변글의 들여쓰기 깊이
	private Date b_date; //글 작성일시
	private int b_cnt; //글 조회수
	
	//기본 생성자
	public BoardVo(){}

	public BoardVo(int b_idx, String b_id, String b_pw, String b_name, String b_email, String b_title, String b_content,
			int b_group, int b_level, Date b_date, int b_cnt) {
		super();
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

	//getter, setter 역할을 하는 메소드들
	public int getB_idx() {
		return b_idx;
	}

	public void setB_idx(int b_idx) {
		this.b_idx = b_idx;
	}

	public String getB_id() {
		return b_id;
	}

	public void setB_id(String b_id) {
		this.b_id = b_id;
	}

	public String getB_pw() {
		return b_pw;
	}

	public void setB_pw(String b_pw) {
		this.b_pw = b_pw;
	}

	public String getB_name() {
		return b_name;
	}

	public void setB_name(String b_name) {
		this.b_name = b_name;
	}

	public String getB_email() {
		return b_email;
	}

	public void setB_email(String b_email) {
		this.b_email = b_email;
	}

	public String getB_title() {
		return b_title;
	}

	public void setB_title(String b_title) {
		this.b_title = b_title;
	}

	public String getB_content() {
		return b_content;
	}

	public void setB_content(String b_content) {
		this.b_content = b_content;
	}

	public int getB_group() {
		return b_group;
	}

	public void setB_group(int b_group) {
		this.b_group = b_group;
	}

	public int getB_level() {
		return b_level;
	}

	public void setB_level(int b_level) {
		this.b_level = b_level;
	}

	public Date getB_date() {
		return b_date;
	}

	public void setB_date(Date b_date) {
		this.b_date = b_date;
	}

	public int getB_cnt() {
		return b_cnt;
	}

	public void setB_cnt(int b_cnt) {
		this.b_cnt = b_cnt;
	}
    
   
	
   
  

   
  
	
	
	
}
