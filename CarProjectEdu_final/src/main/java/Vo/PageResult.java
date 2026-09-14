package Vo;   // 이 파일이 속한 패키지(=폴더) 이름. 실제 폴더 구조와 반드시 같아야 한다

// import = "이 파일에서 저 클래스를 쓰겠다" 는 선언. 안 적으면 이름을 못 찾는다.
import java.util.List;   // 순서가 있는 목록 (게시글 5건이 순서대로 들어간다)

/*
 ================================================================================
   PageResult  -  페이징(쪽 나누기) 결과를 담는 클래스

 ================================================================================
   [왜 만들었나 - 기존 페이징 방식의 문제]

     기존에는 이렇게 동작했다.

       1) DAO가 board 테이블의 "모든 글"을 조회해서 List로 반환
       2) Controller가 그 List를 request에 담아 JSP로 전달
       3) JSP의 스크립트릿이 목록을 잘라서 5건만 출력

          int totalRecord = list.size();              // 전체 개수
          int beginPerPage = nowPage * numPerPage;    // 시작 위치
          for(int i=beginPerPage; i<beginPerPage+numPerPage; i++){ ... }

     문제점

       문제1. 1페이지를 보려고 전체를 다 읽는다.
              글이 10,000건이면 1페이지(5건)를 보여주기 위해
              10,000건을 DB에서 읽어 자바 객체 10,000개를 만들고
              그중 9,995개를 버린다.
              메모리와 네트워크를 모두 낭비하며, 글이 늘어날수록 느려진다.
              (사용자가 늘면 서버가 먼저 죽는다)

       문제2. 계산 로직이 화면(JSP)에 있다.
              페이지 계산은 화면 장식이 아니라 업무 로직인데
              JSP 스크립트릿 안에 200줄 가까이 들어 있었다.
              파일게시판에도 똑같은 코드가 복사되어 있어
              한쪽만 고치면 두 화면의 동작이 달라진다.

     (바뀐 방식 - 실무 표준)

       1) DB에게 "몇 건인지"만 묻는다        -> select count(*)
       2) DB에게 "그 페이지만" 달라고 한다   -> limit ?, ?
       3) 계산된 페이지 정보를 이 객체에 담아 화면에 전달한다
       4) JSP는 계산하지 않고 "받은 값을 그대로 출력"만 한다

       10,000건이어도 화면에 필요한 5건만 읽으므로 글 개수와 무관하게 빠르다.

 ================================================================================
   [페이징 용어 정리]

     예) 전체 23건, 한 페이지 5건, 한 블록에 페이지번호 5개

       nowPage      : 현재 페이지 번호        (0부터 시작. 0=1페이지)
       numPerPage   : 한 페이지에 보여줄 글 수 (5)
       totalRecord  : 전체 글 수              (23)
       totalPage    : 전체 페이지 수          (23/5 = 4.6 -> 올림 -> 5)
       offset       : SQL이 건너뛸 개수       (nowPage * numPerPage)

       pagePerBlock : 한 번에 보여줄 페이지번호 개수 (5)
       nowBlock     : 현재 블록 번호          (0부터 시작)
       totalBlock   : 전체 블록 수            (5/5 = 1)

     블록이 필요한 이유 : 페이지가 100개면 [1][2]...[100] 을 전부 못 보여주므로
                        [1][2][3][4][5] [다음] 처럼 묶어서 보여준다.
 ================================================================================
*/

// <T> 는 제네릭(generic)이라고 부른다. "담을 것의 종류를 나중에 정하겠다" 는 뜻이다.
//   PageResult<BoardVo>     -> 자유게시판 글 목록을 담는 상자
//   PageResult<FileBoardVo> -> 공지(파일게시판) 글 목록을 담는 상자
// 이렇게 해 두면 게시판마다 페이징 클래스를 따로 만들 필요가 없다. 하나로 돌려 쓴다.
public class PageResult<T> {

	/** 현재 페이지에 보여줄 목록 (딱 numPerPage 개수만 들어 있다) */
	// final = 생성자에서 한 번 정하면 그 뒤로는 절대 바뀌지 않는다는 뜻.
	// 페이지 정보는 만들어진 뒤 바뀔 일이 없으므로 전부 final 로 잠가 실수를 막는다.
	private final List<T> list;

	/** 전체 글 수 (select count(*) 결과) */
	private final int totalRecord;

	/** 한 페이지에 보여줄 글 수 */
	private final int numPerPage;

	/** 현재 페이지 번호 (0부터 시작) */
	private final int nowPage;

	/** 전체 페이지 수 */
	private final int totalPage;

	/** 한 블록에 보여줄 페이지 번호 개수 */
	private final int pagePerBlock;

	/** 현재 블록 번호 (0부터 시작) */
	private final int nowBlock;

	/** 전체 블록 수 */
	private final int totalBlock;

	// 생성자 : 이 상자를 만들 때 딱 한 번 실행된다.
	// Service 가 DB 에서 얻은 "목록" 과 "전체 건수" 를 넘겨 주면,
	// 나머지 페이지 계산(전체 페이지 수, 블록 번호 등)은 여기서 전부 끝낸다.
	// 계산을 여기에 모아 두는 이유 : 화면(JSP)은 계산하지 말고 출력만 하게 하려고.
	public PageResult(List<T> list, int totalRecord, int nowPage, int numPerPage, int pagePerBlock) {

		this.list = list;                 // 이 페이지에 보여줄 목록을 그대로 보관
		this.totalRecord = totalRecord;   // 전체 글 수를 그대로 보관

		// numPerPage 가 0 이나 음수로 들어오면 나눗셈에서 오류가 나므로 5 로 바로잡는다.
		//   (조건) ? 참일때값 : 거짓일때값   <- 삼항 연산자. if/else 를 한 줄로 쓴 것이다.
		this.numPerPage = (numPerPage < 1) ? 5 : numPerPage;

		// 블록당 페이지 개수도 마찬가지로 0 이하가 들어오면 5 로 바로잡는다.
		this.pagePerBlock = (pagePerBlock < 1) ? 5 : pagePerBlock;

		/*
		 전체 페이지 수 = 올림(전체 글 수 / 한 페이지 글 수)

		   23건 / 5건 = 4.6  ->  5페이지
		   Math.ceil 은 실수 나눗셈이 필요하므로 (double) 로 형변환한다.
		   (int / int 는 소수점이 버려져 4가 되어 마지막 페이지가 사라진다)
		*/
		// 글이 0건이면 페이지도 0개. 아니면 올림 계산을 한다.
		//   Math.ceil(4.6) = 5.0  ->  (int) 를 붙여 5 로 바꾼다.
		this.totalPage = (totalRecord == 0) ? 0 : (int) Math.ceil((double) totalRecord / this.numPerPage);

		/*
		 현재 페이지 보정

		   글을 삭제해서 페이지 수가 줄었는데 사용자가 이전 페이지 번호로
		   접속하면 빈 화면이 나온다. 범위를 벗어나면 마지막 페이지로 맞춘다.
		*/
		// 음수 페이지(예: 주소창에 nowPage=-3)로 들어오면 0페이지로 끌어올린다.
		int page = (nowPage < 0) ? 0 : nowPage;
		// 페이지가 존재하는데 요청 번호가 마지막을 넘어섰다면
		if (this.totalPage > 0 && page >= this.totalPage) {
			// 마지막 페이지 번호로 맞춘다. (0부터 세므로 전체 페이지 수 - 1)
			page = this.totalPage - 1;
		}
		// 보정이 끝난 값을 최종 저장한다.
		this.nowPage = page;

		//전체 블록 수 = 올림(전체 페이지 수 / 블록당 페이지 수)
		// 페이지가 0개면 블록도 0개. 아니면 페이지 수를 블록 크기로 나눠 올림한다.
		this.totalBlock = (this.totalPage == 0) ? 0
						: (int) Math.ceil((double) this.totalPage / this.pagePerBlock);

		//현재 페이지가 속한 블록 번호
		// 정수 나눗셈은 소수점을 버리므로 그대로 블록 번호가 된다.
		//   7페이지 / 5 = 1  ->  두 번째 블록([6][7][8][9][10])
		this.nowBlock = this.nowPage / this.pagePerBlock;
	}

	//===========================================================
	// getter  (JSP에서 ${page.xxx} 로 사용한다)
	//===========================================================
	// 값을 바꾸는 setter 는 일부러 만들지 않았다.
	// 페이지 정보는 만들어진 뒤 바뀌면 안 되는 값이기 때문이다(위의 final 과 같은 이유).

	// list 값을 꺼내 준다.  — 이 페이지에 보여줄 글 목록.  ${page.list}
	public List<T> getList()      { return list; }
	// totalRecord 값을 꺼내 준다.  — 전체 글 수.  ${page.totalRecord}
	public int getTotalRecord()   { return totalRecord; }
	// numPerPage 값을 꺼내 준다.  — 한 페이지에 보여줄 글 수
	public int getNumPerPage()    { return numPerPage; }
	// nowPage 값을 꺼내 준다.  — 현재 페이지 번호(0부터)
	public int getNowPage()       { return nowPage; }
	// totalPage 값을 꺼내 준다.  — 전체 페이지 수
	public int getTotalPage()     { return totalPage; }
	// pagePerBlock 값을 꺼내 준다.  — 한 블록에 보여줄 페이지 번호 개수
	public int getPagePerBlock()  { return pagePerBlock; }
	// nowBlock 값을 꺼내 준다.  — 현재 블록 번호(0부터)
	public int getNowBlock()      { return nowBlock; }
	// totalBlock 값을 꺼내 준다.  — 전체 블록 수
	public int getTotalBlock()    { return totalBlock; }

	//===========================================================
	// 화면에서 바로 쓰기 좋은 계산값들
	//===========================================================
	// 아래 메소드들은 저장된 값이 아니라 "그때그때 계산해서 돌려주는" 값이다.
	// 이런 걸 화면에서 계산하지 않고 여기서 만들어 주면 JSP 가 훨씬 단순해진다.
	// ※ isXxx() 형태로 이름을 지으면 JSP 에서 ${page.hasPrevBlock} 처럼 is 를 떼고 쓸 수 있다.

	/** 목록이 비어 있는가 */
	public boolean isEmpty() {
		// 전체 글 수가 0이면 "글이 없습니다" 를 띄우면 된다.
		return totalRecord == 0;
	}

	/** 이전 블록이 있는가 ([이전] 버튼 표시 여부) */
	public boolean isHasPrevBlock() {
		// 현재 블록이 0번(첫 블록)이 아니면 앞쪽에 블록이 더 있다는 뜻이다.
		return nowBlock > 0;
	}

	/** 다음 블록이 있는가 ([다음] 버튼 표시 여부) */
	public boolean isHasNextBlock() {
		// 전체 블록 수가 "현재 블록 번호 + 1" 보다 크면 뒤에 블록이 더 남아 있다.
		//   예) 전체 3블록, 현재 0번 -> 3 > 1 이므로 [다음] 있음
		//       전체 3블록, 현재 2번 -> 3 > 3 이 아니므로 [다음] 없음
		return totalBlock > nowBlock + 1;
	}

	/** 이전 블록의 첫 페이지 번호 */
	public int getPrevBlockPage() {
		// [이전] 을 누르면 갈 페이지. 앞 블록의 시작 페이지로 보낸다.
		//   예) 현재 1번 블록, 블록당 5페이지 -> (1-1)*5 = 0페이지
		return (nowBlock - 1) * pagePerBlock;
	}

	/** 다음 블록의 첫 페이지 번호 */
	public int getNextBlockPage() {
		// [다음] 을 누르면 갈 페이지. 뒤 블록의 시작 페이지로 보낸다.
		//   예) 현재 0번 블록, 블록당 5페이지 -> (0+1)*5 = 5페이지
		return (nowBlock + 1) * pagePerBlock;
	}

	/** 현재 블록에서 시작하는 페이지 번호 (0부터) */
	public int getBlockStartPage() {
		// 화면에 [1][2][3][4][5] 를 그릴 때 첫 번호가 될 값.
		//   예) 1번 블록 -> 1*5 = 5페이지(=화면에는 6페이지로 표시)
		return nowBlock * pagePerBlock;
	}

	/**
	 * 현재 블록에서 끝나는 페이지 번호 (포함).
	 * 마지막 블록은 페이지가 덜 찰 수 있으므로 전체 페이지 수를 넘지 않게 한다.
	 */
	public int getBlockEndPage() {
		// 일단 "시작 + 블록크기 - 1" 로 끝 번호를 구한다.
		//   예) 시작 0, 블록크기 5 -> 끝 4 (즉 0~4, 화면에는 1~5페이지)
		int end = getBlockStartPage() + pagePerBlock - 1;
		// 그런데 전체가 3페이지뿐이면 끝이 4가 되어 없는 페이지 번호가 찍힌다.
		// 그래서 마지막 페이지 번호(totalPage-1)를 넘으면 거기서 잘라 준다.
		return (end > totalPage - 1) ? totalPage - 1 : end;
	}

	/**
	 * 목록 화면에 표시할 "글 번호"의 시작값.
	 *
	 * 게시판은 보통 최신 글이 큰 번호를 갖도록 보여준다.
	 * 1페이지 첫 줄 = 전체 개수,  2페이지 첫 줄 = 전체 개수 - 5 ...
	 */
	public int getDisplayStartNo() {
		// 여기서 말하는 번호는 DB 의 글번호(b_idx)가 아니라 "화면에 보이는 순번" 이다.
		//   예) 전체 23건, 1페이지(nowPage=0) -> 23 - 0 = 23 부터 아래로 22, 21 ...
		//       전체 23건, 2페이지(nowPage=1) -> 23 - 5 = 18 부터 아래로 17, 16 ...
		return totalRecord - (nowPage * numPerPage);
	}
}
