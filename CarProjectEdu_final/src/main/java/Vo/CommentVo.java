package Vo;

/*
 * ============================================================================
 *  CommentVo  -  상자(VO) : 자유게시판 댓글(대댓글 포함) 1건을 담아 나르는 객체
 *
 *  이 상자에는 세 종류의 값이 섞여 있다.
 *
 *  1) DB 의 board_comment 테이블에서 그대로 읽어 오는 값
 *       cIdx, bIdx, parentIdx, cId, cName, cContent, cDate, cUpdate, delFlag
 *
 *  2) 조회할 때 계산해서 채우는 값 (CommentDAO 가 SQL 로 함께 계산해 온다)
 *       likeCount  추천 수      (board_comment_like 테이블의 행 개수)
 *       likedByMe  지금 로그인한 사람이 이미 추천했는가 (추천 버튼 색을 바꾸는 데 쓴다)
 *
 *  3) 트리(부모-자식 구조)를 만들 때 CommentService 가 채우는 값
 *       depth      들여쓰기 단계 (0 = 최상위 댓글)
 *       children   이 댓글에 달린 답글 목록
 *
 *  [댓글이 무한 답글 구조인 이유와 children 을 상자가 직접 들고 있는 이유]
 *    댓글에는 답글을, 그 답글에는 또 답글을 다는 식으로 깊이 제한이 없다.
 *    화면에서 "부모를 거슬러 올라가며" 순서를 알아내기는 번거롭다.
 *    그래서 CommentService.getComments() 가 평면 목록(모든 댓글이 한 줄씩)을
 *    부모-자식 관계로 미리 묶어 트리 모양으로 만들어 주면, 화면은 위에서
 *    아래로 그냥 순서대로 출력하기만 하면 된다.
 *
 *  getXxx() : 상자에서 값을 꺼낸다      예) vo.getCContent()
 *  setXxx() : 상자에 값을 넣는다(바꾼다)  예) vo.setCContent("댓글 내용")
 * ============================================================================
 */

// 답글 목록을 담을 목록 도구
import java.util.ArrayList;
import java.util.List;

public class CommentVo {

	// DB 에서 그대로 읽어 오는 값들
	private int cIdx;              // 댓글번호 (기본키)
	private int bIdx;               // 이 댓글이 달린 원글의 글번호
	private Integer parentIdx;      // 부모 댓글번호. 최상위 댓글이면 null (기본형 int 가 아니라 Integer 를 쓰는 이유는 "0번" 과 "부모 없음"을 구별하기 위해서다)
	private String cId;             // 작성자 아이디
	private String cName;           // 작성자 이름 (작성 당시 이름을 그대로 저장해 둔다)
	private String cContent;        // 댓글 내용
	private String cDate;           // 작성일시 (화면에 보여주기 좋은 문자열로 이미 가공되어 들어온다)
	private String cUpdate;         // 수정일시 (한 번도 수정하지 않았으면 null)
	private String delFlag;         // 삭제 여부 ("Y" 또는 "N")

	// 조회할 때 계산해서 채워지는 값들
	private int likeCount;          // 추천 수
	private boolean likedByMe;      // 지금 로그인한 사람이 이미 추천했는가

	// 트리를 만들 때 채워지는 값들
	private int depth;                                              // 들여쓰기 단계 (0 = 최상위)
	private List<CommentVo> children = new ArrayList<CommentVo>();  // 이 댓글에 달린 답글 목록 (빈 목록으로 시작한다)

	//----------------------------------------------------------------
	// 기본 생성자 : 빈 상자를 만든다. 값은 CommentDAO 가 setXxx() 로 하나씩 채운다
	//----------------------------------------------------------------
	public CommentVo() {}

	// 댓글번호(기본키) 꺼내기 / 넣기
	public int getCIdx() {
		return cIdx;
	}

	public void setCIdx(int cIdx) {
		this.cIdx = cIdx;
	}

	// 원글 번호 꺼내기 / 넣기
	public int getBIdx() {
		return bIdx;
	}

	public void setBIdx(int bIdx) {
		this.bIdx = bIdx;
	}

	// 부모 댓글번호 꺼내기 / 넣기 (최상위 댓글이면 null)
	public Integer getParentIdx() {
		return parentIdx;
	}

	public void setParentIdx(Integer parentIdx) {
		this.parentIdx = parentIdx;
	}

	// 작성자 아이디 꺼내기 / 넣기
	public String getCId() {
		return cId;
	}

	public void setCId(String cId) {
		this.cId = cId;
	}

	// 작성자 이름 꺼내기 / 넣기
	public String getCName() {
		return cName;
	}

	public void setCName(String cName) {
		this.cName = cName;
	}

	// 댓글 내용 꺼내기 / 넣기
	public String getCContent() {
		return cContent;
	}

	public void setCContent(String cContent) {
		this.cContent = cContent;
	}

	// 작성일시 꺼내기 / 넣기
	public String getCDate() {
		return cDate;
	}

	public void setCDate(String cDate) {
		this.cDate = cDate;
	}

	// 수정일시 꺼내기 / 넣기 (없으면 null)
	public String getCUpdate() {
		return cUpdate;
	}

	public void setCUpdate(String cUpdate) {
		this.cUpdate = cUpdate;
	}

	// 삭제 여부(Y/N) 꺼내기 / 넣기
	public String getDelFlag() {
		return delFlag;
	}

	public void setDelFlag(String delFlag) {
		this.delFlag = delFlag;
	}

	// 추천 수 꺼내기 / 넣기
	public int getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(int likeCount) {
		this.likeCount = likeCount;
	}

	// 내가 이미 추천했는지 꺼내기 / 넣기 (boolean 은 관례상 getXxx 대신 isXxx 로 이름 짓는다)
	public boolean isLikedByMe() {
		return likedByMe;
	}

	public void setLikedByMe(boolean likedByMe) {
		this.likedByMe = likedByMe;
	}

	// 들여쓰기 단계 꺼내기 / 넣기
	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	// 답글 목록 꺼내기 / 넣기
	public List<CommentVo> getChildren() {
		return children;
	}

	public void setChildren(List<CommentVo> children) {
		this.children = children;
	}

	//----------------------------------------------------------------
	// isDeleted : 삭제 표시된 댓글인가 (내용 대신 "삭제된 댓글입니다" 를 보여줘야 한다)
	//  "Y".equals(delFlag) 순서로 비교하는 이유 : delFlag 가 null 이어도
	//  NullPointerException 없이 안전하게 false 를 돌려주기 때문이다
	//  (반대로 delFlag.equals("Y") 로 쓰면 delFlag 가 null 일 때 오류가 난다)
	//----------------------------------------------------------------
	public boolean isDeleted() {
		return "Y".equals(delFlag);
	}

	//----------------------------------------------------------------
	// isEdited : 한 번이라도 수정된 적이 있는가 (화면에 "(수정됨)" 을 붙일지 판단)
	//----------------------------------------------------------------
	public boolean isEdited() {
		return cUpdate != null && !cUpdate.trim().isEmpty();   // 수정일시가 있고 비어 있지도 않으면 한 번이라도 수정한 것이다
	}
}
