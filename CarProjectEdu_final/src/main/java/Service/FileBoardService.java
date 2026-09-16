package Service;

/*
 * ============================================================================
 *  FileBoardService  -  부장(Service) : 자료실(파일게시판) 업무 규칙 담당 클래스
 *
 *  파일 업로드/다운로드 + 게시판 CRUD(등록·조회·수정·삭제) 업무 규칙을 모두 담당한다.
 *
 *  [synchronized 를 쓰지 않는 이유]
 *    "동시에 여러 명이 파일을 올릴 때 섞이지 않게" 막으려면 흔히
 *    synchronized 로 메소드나 블록을 잠그는 방법을 떠올리기 쉽다. 하지만
 *    여기서는 그 방법을 쓰지 않는다.
 *
 *      문제1. 성능 : synchronized 로 잠그면 글 등록 전체가 한 줄로 세워진다.
 *             파일 업로드는 수 초가 걸리는 작업인데, 한 사람이 10MB를
 *             올리는 동안 나머지 전원이 대기해야 한다. 여러 명이 동시에
 *             제출하면 마지막 사람은 몇 분을 기다리게 된다.
 *
 *      문제2. 애초에 필요하지 않다 : synchronized 는 "여러 스레드가 공유하는
 *             자원"을 보호할 때 쓰는 도구다. 그런데 실제로 공유되어 문제가
 *             됐던 것은
 *               - 파일명 충돌 (같은 이름을 올리면 앞 파일을 덮어씀) → 저장할
 *                 때 이름을 UUID 로 새로 만들어서 해결했다 (다른 사람과 절대
 *                 겹치지 않는다)
 *               - 글번호 충돌 (max+1 계산 방식의 경쟁 상태) → AUTO_INCREMENT
 *                 로 DB 에게 번호 발급을 맡겨서 해결했다
 *             즉 "공유되던 원인 자체"를 없앴으므로 잠금이 더 이상 필요 없다.
 *
 *    실무 원칙 : 잠금(synchronized)으로 증상만 막기 전에, "무엇이 공유되고
 *    있어서 문제가 생기는가"를 먼저 찾아 그 원인 자체를 없애는 편이 훨씬
 *    낫다. 원인이 없으면 잠금 없이도 안전하고, 성능도 그대로 유지된다.
 * ============================================================================
 */

// 파일 다루기, 입출력 예외, 파일 내용 읽고 내보내기, 주소에 넣을 글자 인코딩
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;

// 목록·이름표로찾는자료구조 도구
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 서블릿 기본 도구
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// 파일 업로드(multipart 요청)를 해석해 주는 외부 라이브러리 (commons-fileupload)
import org.apache.commons.fileupload.FileItem;
//용량 초과 예외(FileSizeLimitExceededException / SizeLimitExceededException)가 이 클래스 안에 있다
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
// 폴더째 지우기·파일 옮기기를 쉽게 해 주는 외부 라이브러리 (commons-io)
import org.apache.commons.io.FileUtils;

// 사원(FileBoardDAO, MemberDAO), 상자(FileBoardVo, MemberVO, PageResult)
import Dao.FileBoardDAO;
import Dao.MemberDAO;
import Vo.FileBoardVo;
import Vo.MemberVO;
import Vo.PageResult;

// 우리가 직접 만든 예외 클래스, 파일 저장 위치·안전 검사 도우미, DB 연결 도우미
import exception.InvalidInputException;
import exception.NotFoundException;
import util.FileRepo;
import util.DBCPUtil;

public class FileBoardService {

	/* 페이징 설정 - 자유게시판과 같은 기준을 사용한다 (BoardService 주석 참고) */
	/** 한 페이지에 보여줄 글 수 */
	public static final int NUM_PER_PAGE = 5;

	/** 한 번에 보여줄 페이지 번호 개수 */
	public static final int PAGE_PER_BLOCK = 5;

	// 사원(DAO) 객체의 주소를 저장할 참조변수. final : 한 번 저장하면 다른 사원으로 바꿀 수 없다
	private final FileBoardDAO boarddao;
	private final MemberDAO memberdao;   // 글쓰기 화면에 작성자 정보를 채우려면 회원 표도 봐야 해서 DAO 를 하나 더 갖는다

	//----------------------------------------------------------------
	// 생성자 : 부장(FileBoardService)이 만들어질 때 사원 둘을 함께 만든다
	//----------------------------------------------------------------
	public FileBoardService() {
		this.boarddao = new FileBoardDAO();   // 공지(파일게시판) DAO 를 하나 만들어 재사용한다
		this.memberdao = new MemberDAO();   // 회원 DAO 도 하나 만들어 재사용한다
	}

	//----------------------------------------------------------------
	// serviceMemberOne : 회원 1명 조회 (글쓰기 화면에 작성자 정보를 채우기 위해)
	//  사장의 "/write.bo" 요청에서 부른다
	//----------------------------------------------------------------
	public MemberVO serviceMemberOne(String memberid) {
		return DBCPUtil.query(con -> memberdao.memberOne(con, memberid));   // 로그인한 회원의 이름·이메일을 조회해 글쓰기 화면에 미리 채워 준다
	}

	//----------------------------------------------------------------
	// serviceInsertBoard : 새 글을 등록한다 (첨부파일 업로드 포함)
	//  사장의 "/writePro.bo" 요청에서 부른다
	//----------------------------------------------------------------
	/**
	 * [처리 순서]
	 *   1. 첨부파일을 먼저 임시(temp) 폴더에 저장하고, 함께 온 입력값들을 Map 으로 받는다
	 *   2. 비밀번호·제목·내용이 비어 있지 않은지 검사한다 (실패하면 temp 파일을 지운다)
	 *   3. 저장할 정보를 FileBoardVo 상자에 담는다
	 *   4. 트랜잭션으로 "기존 글 순서 밀기 + 글 등록 + 첨부파일을 글번호 폴더로 이동"을 묶어 실행한다
	 *
	 * [검증에 실패하면 이미 올린 임시 파일을 지우는 이유]
	 *   첨부파일은 이 메소드의 맨 처음(1단계)에서 먼저 temp 폴더에 저장된다.
	 *   그 다음(2단계)에 비밀번호나 제목이 비어 있는 것이 확인되면, 글은
	 *   등록되지 않고 여기서 곧바로 끝난다. 이때 temp 파일을 지우지 않으면
	 *   그 파일은 어떤 글에도 속하지 않은 채 디스크에 영원히 남는다 — 사용자가
	 *   입력을 실수할 때마다 아무도 접근할 수 없는 "고아 파일"이 계속 쌓이는
	 *   것이다. 그래서 "여기서 등록을 중단할 때는 내가 만든 파일도 함께
	 *   치운다"를 원칙으로 삼는다 (파일을 만든 쪽이 정리까지 책임진다).
	 *
	 * [파일 이동을 DB 트랜잭션 안에서 함께 하는 이유와 한계]
	 *   글 INSERT 가 성공한 뒤 첨부파일을 옮기다 실패하면(디스크가 꽉 참,
	 *   폴더 권한 없음 등) 예외가 발생해 앞서 실행한 "글 순서 밀기 + 글
	 *   INSERT"까지 함께 rollback 된다. 즉 "첨부파일 없는 빈 글"이 DB 에
	 *   남는 일이 없다. 다만 DB 트랜잭션과 파일시스템은 완전히 하나로
	 *   묶을 수 없는 서로 다른 시스템이라는 한계는 있다 — 파일을 옮긴
	 *   "직후, commit 되기 바로 그 순간" 에 서버가 죽는 것처럼 극히
	 *   드문 경우에는 DB 에는 글이 없는데 파일만 남는 상태가 이론적으로
	 *   가능하다. 실무에서는 이런 고아 파일을 찾아 주기적으로 지우는
	 *   별도의 정리 배치(스케줄 작업)를 함께 두는 것이 일반적이다.
	 *
	 * @return 새로 등록된 글번호
	 * @throws InvalidInputException 비밀번호·제목·내용이 비어 있을 때, 첨부파일이
	 *         허용되지 않는 확장자이거나 용량 제한을 넘었을 때 (upload() 안에서 던져진다)
	 */
	public int serviceInsertBoard(HttpServletRequest request, HttpServletResponse response) throws Exception {

		//1. 파일을 임시 폴더에 업로드하고, 입력값들을 Map으로 받는다
		Map<String, String> articleMap = upload(request, response);

		//2. 입력값 꺼내기
		String writer  = articleMap.get("writer");     //작성자
		String email   = articleMap.get("email");      //이메일
		String title   = articleMap.get("title");      //제목
		String content = articleMap.get("content");    //내용
		String pass    = articleMap.get("pass");       //글 비밀번호
		String id      = articleMap.get("writer_id");  //작성자 아이디

		//';' 로 연결된 파일명 목록 (첨부가 없으면 빈 문자열)
		final String sfileNames = articleMap.getOrDefault("sfileNames", ""); //저장명(UUID)
		final String ofileNames = articleMap.getOrDefault("ofileNames", ""); //원본명

		//3. 업무 규칙 검증 (자세한 이유는 위 메소드 설명 [검증에 실패하면...] 참고)
		if (pass == null || pass.trim().isEmpty()) {
			deleteTempFiles(sfileNames);
			throw new InvalidInputException("글 비밀번호를 입력해주세요");   // 임시 파일을 치운 뒤에 400 으로 막는다
		}
		if (title == null || title.trim().isEmpty()) {   // 제목이 비어 있으면
			deleteTempFiles(sfileNames);   // 이미 올려 둔 임시 파일부터 치운다 (안 치우면 아무도 못 쓰는 파일이 쌓인다)
			throw new InvalidInputException("글 제목을 입력해주세요");   // 그 다음 400 으로 막는다
		}
		if (content == null || content.trim().isEmpty()) {   // 내용이 비어 있으면 (자유게시판과 같은 이유)
			deleteTempFiles(sfileNames);
			throw new InvalidInputException("글 내용을 입력해주세요");   // 임시 파일을 치운 뒤 400 으로 막는다
		}

		//4. 저장할 정보를 상자에 담는다 (비밀번호는 앞뒤 공백만 정리한다)
		final FileBoardVo vo = new FileBoardVo();
		vo.setB_name(writer);   // 작성자 이름
		vo.setB_email(email);   // 이메일
		vo.setB_title(title);   // 제목
		vo.setB_content(content);   // 내용
		vo.setB_pw((pass.trim()));   // 앞뒤 공백을 없앤 글 비밀번호
		vo.setB_id(id);   // 작성자 아이디
		vo.setSfile(sfileNames);   // 저장 파일명 목록 (UUID, ; 로 이어 붙인 값)
		vo.setOfile(ofileNames);   // 원본 파일명 목록 (사용자가 올린 이름, ; 로 이어 붙인 값)

		//5. [트랜잭션] 글 등록 + 첨부파일 이동을 한 작업으로 처리한다 (자세한 이유는 위 메소드 설명 참고)
		return DBCPUtil.execute(con -> {

			//사원(FileBoardDAO)에게 시키기 : FileBoardDAO 의 shiftAllGroups(con) 호출해서 기존 글 순서 한 칸 밀기 작업 명령
			boarddao.shiftAllGroups(con);

			//사원(FileBoardDAO)에게 시키기 : 준비된 FileBoardVo 상자를 FileBoardDAO 의 insertBoard(con, vo) 호출해서 새 글 추가(insert) + 부여된 글번호 받기 작업 명령
			int articleNO = boarddao.insertBoard(con, vo);

			//첨부파일을 글번호 폴더로 이동 (DB 작업이 아니므로 DAO 가 아니라 이 Service 가 직접 처리한다)
			moveUploadedFiles(articleNO, sfileNames);

			return articleNO;   // 새로 만들어진 글번호를 돌려준다 (화면이 그 글로 이동하는 데 쓴다)
		});
	}

	/**
	 * temp 폴더에 올라간 파일들을 {글번호} 폴더로 옮긴다.
	 * [부르는 곳] serviceInsertBoard() — DB 트랜잭션 안에서, insertBoard() 로 글번호를 받은 직후
	 *
	 * 저장 경로는 "C:\..." 같은 문자열을 이 코드 안에 직접 적지 않고, util/FileRepo
	 * 가 설정값(file.repo.path)을 기준으로 만들어 준다 (경로를 한 곳에서만 관리한다).
	 */
	private void moveUploadedFiles(int articleNO, String sfileNames) throws IOException {

		if (sfileNames == null || sfileNames.trim().isEmpty()) {   // 첨부파일이 없으면 옮길 것도 없다
			return; //첨부파일 없음
		}

		File destDir = FileRepo.getArticleDir(articleNO);   // 이 글의 첨부파일이 모일 폴더 (예: C:\file_repo_edu\7)

		if (!destDir.exists() && !destDir.mkdirs()) {   // 폴더가 없는데 만들기도 실패했다면
			throw new IOException("첨부파일 폴더를 만들 수 없습니다 : " + destDir.getAbsolutePath());   // 예외를 던져 트랜잭션을 되돌린다. 글만 남고 파일이 없는 상태를 막는다
		}

		for (String storedName : sfileNames.split(";")) {   // ; 를 기준으로 잘라 파일 하나씩 처리한다

			if (storedName == null || storedName.trim().isEmpty()) {   // 빈 조각이면 건너뛴다 ("a;;b" 처럼 이어 붙이면 빈 값이 생긴다)
				continue;
			}

			File srcFile = new File(FileRepo.getTempDir(), storedName.trim());   // 임시 폴더에 있는 그 파일을 가리킨다

			if (srcFile.exists()) {   // 실제로 그 파일이 있으면
				//true = 대상 폴더가 없으면 만들면서 이동
				FileUtils.moveFileToDirectory(srcFile, destDir, true);
			} else {
				System.out.println("[FileBoardService] 임시 파일을 찾을 수 없습니다 : " + srcFile.getAbsolutePath());   // 파일이 없어도 글 등록은 계속한다. 원인만 콘솔에 남긴다
			}
		}
	}

	//----------------------------------------------------------------
	// upload : multipart 요청(파일+입력값이 섞인 요청)을 해석해 임시 폴더에 저장한다
	//  serviceInsertBoard() 의 1단계에서 부른다
	//----------------------------------------------------------------
	/**
	 * [매개변수] response 는 실제로 응답을 쓰지 않지만, 업로드 처리 도중 필요할
	 *           수 있는 서블릿 컨텍스트 정보 때문에 매개변수로 함께 받는다
	 * [반환값]   화면에서 입력한 값들 + 저장된 파일명 목록("sfileNames", "ofileNames")이
	 *           함께 담긴 Map
	 *
	 * [multipart/form-data 요청을 다루는 기본 원리]
	 *   이 방식의 요청은 일반 입력값(제목, 내용 등)과 파일이 하나의 요청 안에
	 *   섞여서 온다. commons-fileupload 라이브러리가 이 요청을 해석해서
	 *   FileItem 목록으로 나눠 준다.
	 *     isFormField() == true  → 일반 입력값 (제목, 내용 등)
	 *     isFormField() == false → 첨부파일
	 *
	 * [업로드 파일명 처리 방식 - 원본명과 저장명을 분리한다]
	 *   예전에는 사용자가 보낸 파일명에서 경로만 잘라내고 그 이름을 그대로
	 *   디스크 저장명으로 썼다(원본명 = 저장명). 이 방식에는 문제가 있었다.
	 *     1) 확장자에 제한이 없어 실행 파일 등 무엇이든 올릴 수 있었다
	 *     2) 같은 이름의 파일을 올리면 먼저 있던 파일을 덮어써 버렸다
	 *     3) 파일명에 세미콜론(;)이 들어 있으면 DB 저장 목록의 구분자가 깨졌다
	 *     4) 사용자가 보낸 문자열이 검증 없이 파일 경로에 그대로 들어갔다
	 *   지금은 원본명(ofile)과 저장명(sfile)을 완전히 분리한다.
	 *     원본명(ofile) : 화면 표시와 다운로드할 때 사용자에게 보여줄 이름 → DB 에만 저장
	 *     저장명(sfile) : 디스크에 실제로 저장되는 이름            → UUID 로 새로 생성
	 *       예) 여행 사진.jpg  →  3f2b9c1e8a7d4f60b1c2d3e4f5a6b7c8.jpg
	 */
	private Map<String, String> upload(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		Map<String, String> articleMap = new HashMap<String, String>();   // 화면에서 온 입력값들을 담아 컨트롤러에 돌려줄 상자

		//1. 한글 처리
		request.setCharacterEncoding("UTF-8");

		//2. 업로드 임시 저장 설정
		DiskFileItemFactory factory = new DiskFileItemFactory();

		//메모리에 담을 최대 크기(1MB). 이보다 크면 아래 지정한 폴더에 임시 파일로 쓴다.
		factory.setSizeThreshold(1024 * 1024);
		factory.setRepository(FileRepo.getRoot());   // 임시 파일을 쓸 폴더를 지정한다

		ServletFileUpload upload = new ServletFileUpload(factory);   // 실제로 요청을 해석할 도구를 만든다

		/*
		 [보안] 업로드 용량 제한

		   제한을 두지 않으면 누군가 매우 큰 파일을 계속 올려 디스크를
		   가득 채워서 서버 전체를 멈추게 만들 수 있다(서비스 거부 공격).
		   화면(자바스크립트)에서 크기를 검사해도, 요청을 화면 없이 직접
		   조립해 보내면 그 검사는 아무 의미가 없으므로 서버에서 제한한다.
		*/
		upload.setFileSizeMax(FileRepo.getMaxFileSize());     //파일 1개 최대
		upload.setSizeMax(FileRepo.getMaxRequestSize());      //요청 전체 최대

		List<String> sfileNameList = new ArrayList<String>(); //저장명(UUID) 목록
		List<String> ofileNameList = new ArrayList<String>(); //원본명 목록

		try {
			List<FileItem> items = upload.parseRequest(request);   // 요청을 해석해 입력값과 첨부파일을 항목 목록으로 받는다

			for (FileItem fileitem : items) {   // 항목을 하나씩 확인한다. 입력값일 수도 있고 첨부파일일 수도 있다

				//---- 일반 입력값 ----
				if (fileitem.isFormField()) {
					articleMap.put(fileitem.getFieldName(), fileitem.getString("UTF-8"));
					continue;
				}

				//---- 첨부파일 ----
				if (fileitem.getSize() <= 0) {
					continue; //파일을 선택하지 않은 빈 항목
				}

				//1) 경로 제거 + 위험문자 정리 (IE는 전체 경로를 보낸다)
				String originalName = FileRepo.cleanOriginalName(fileitem.getName());

				//2) 허용된 확장자인지 검사 (목록에 없으면 예외 -> 업로드 중단)
				FileRepo.validateExtension(originalName);

				//3) 디스크 저장명을 UUID로 새로 만든다
				String storedName = FileRepo.newStoredName(originalName);

				System.out.println("업로드 : " + originalName + "  ->  " + storedName);   // 무엇이 무엇으로 바뀌어 저장됐는지 콘솔에 남긴다

				//4) 임시 폴더에 저장 (글번호가 정해진 뒤 글번호 폴더로 이동)
				File uploadFile = new File(FileRepo.getTempDir(), storedName);

				if (!uploadFile.getParentFile().exists()) {   // 임시 폴더가 아직 없으면
					uploadFile.getParentFile().mkdirs();   // 폴더를 먼저 만든다. 없으면 파일 쓰기가 실패한다
				}

				//5) 목록에 담기
				sfileNameList.add(storedName);
				ofileNameList.add(originalName);   // 원본 이름도 같은 순서로 담는다 (두 목록의 순서가 짝을 이뤄야 한다)

				//6) 실제 파일 쓰기
				fileitem.write(uploadFile);
			}

		} catch (InvalidInputException e) {
			/* 확장자 위반 등 사용자에게 알려야 하는 오류는 그대로 올려보낸다.
			   다만 그 전에 이미 temp 에 쓴 파일은 지운다.
			   예) 파일 3개 중 3번째가 허용되지 않는 확장자였다면
			       1·2번째는 이미 디스크에 써 있다. 글은 만들어지지 않으므로 지운다. */
			deleteTempFiles(String.join(";", sfileNameList));
			throw e;   // 정리를 마친 뒤 예외를 다시 던져 위에서 처리하게 한다

		} catch (FileUploadBase.FileSizeLimitExceededException e) {
			/*
			  파일 1개가 용량 제한을 넘은 경우.
			  이 예외를 따로 잡지 않으면 아래 Exception 으로 떨어져
			  "알 수 없는 오류" 로 처리된다. 사용자는 파일을 줄이면 되는데
			  무엇이 문제인지 몰라 같은 파일을 계속 다시 올린다.
			  사용자가 고칠 수 있는 문제이므로 제한 값을 숫자로 알려준다.
			*/
			deleteTempFiles(String.join(";", sfileNameList));
			throw new InvalidInputException(   // 몇 MB 까지 되는지 숫자로 알려 줘야 사용자가 고칠 수 있다
					"첨부파일 1개의 크기는 " + (FileRepo.getMaxFileSize() / (1024 * 1024))
					+ "MB 까지 올릴 수 있습니다.");

		} catch (FileUploadBase.SizeLimitExceededException e) {
			/* 파일 여러 개의 합계가 요청 제한을 넘은 경우
			   (위 FileSizeLimitExceededException 이 이 예외의 자식이므로 반드시 아래에 둔다) */
			deleteTempFiles(String.join(";", sfileNameList));
			throw new InvalidInputException(   // 전체 합계 제한도 숫자로 알려 준다
					"첨부파일 전체 크기는 " + (FileRepo.getMaxRequestSize() / (1024 * 1024))
					+ "MB 까지 올릴 수 있습니다. 파일 수를 줄여주세요.");

		} catch (Exception e) {
			/* 용량 초과·디스크 오류 등 예상하지 못한 이유로 중단될 때도 temp 를 정리한다.
			   (정리하지 않으면 업로드가 실패했는데도 글은 등록되어
			    "첨부파일이 사라진 글"이 만들어지는 문제가 생긴다) */
			deleteTempFiles(String.join(";", sfileNameList));
			throw new ServletException("파일 업로드 처리 중 오류가 발생했습니다 : " + e.getMessage(), e);   // 임시 파일을 치운 뒤 예외를 던진다. 첨부 없는 글이 만들어지지 않게 하려는 것이다
		}

		//';' 로 이어 붙여 저장 (컬럼 하나에 여러 파일명을 담는 방식)
		articleMap.put("sfileNames", String.join(";", sfileNameList));
		articleMap.put("ofileNames", String.join(";", ofileNameList));   // 원본 이름 목록도 ; 로 이어 붙여 담는다

		return articleMap;   // 입력값 + 파일명 목록이 담긴 상자를 컨트롤러에 돌려준다
	}

	//----------------------------------------------------------------
	// serviecDownload : 첨부파일을 내려주고 다운로드 횟수를 1 올린다
	//  사장의 "/Download.do" 요청에서 부른다
	//----------------------------------------------------------------
	/**
	 * [처리 순서]
	 *   1. 요청받은 글번호(path)와 파일명(fileName)을 확인한다
	 *   2. [1차 방어] 글번호로 DB 를 조회해 그 글의 진짜 첨부파일 목록을 가져온다
	 *   3. 요청받은 파일명이 그 목록에 실제로 있는지 확인한다 (없으면 404)
	 *   4. [2차 방어] FileRepo 가 최종 경로가 저장 폴더 안인지 다시 한 번 확인한다
	 *   5. 응답 헤더를 설정하고 파일 내용을 잘게 나눠 전송한다
	 *   6. 전송이 끝나면 다운로드 횟수를 1 올린다
	 *
	 * [경로 조작(Path Traversal) 취약점을 고친 부분 - 2단계 방어]
	 *   예전 코드는 아래처럼 사용자가 보낸 두 값을 검증 없이 경로 문자열에
	 *   그대로 이어 붙였다.
	 *       String idx  = request.getParameter("path");
	 *       String name = request.getParameter("fileName");
	 *       File f = new File("C:\\file_repo\\" + idx + "\\" + name);
	 *   그래서 아래 같은 요청이 그대로 통과했다.
	 *       /FileBoard/Download.do?path=..&fileName=..\web.xml
	 *   이 요청은 저장 폴더 바깥, 즉 서버 안의 임의의 파일(web.xml 등,
	 *   그 안에는 API 키 같은 민감한 값이 들어 있을 수 있다)까지 내려받을
	 *   수 있게 만든다.
	 *   지금은 두 겹으로 막는다.
	 *     1차 : 사용자가 보낸 파일명을 그대로 믿지 않고, 먼저 글번호로
	 *          DB 를 조회해 "이 글에 실제로 첨부된 파일 목록"을 가져와
	 *          그 목록에 있는 이름인지부터 확인한다
	 *     2차 : 그렇게 확인된 이름이라도, FileRepo 가 실제 경로를 계산해
	 *          그 경로가 저장 폴더 밖으로 벗어나지 않는지 한 번 더 확인한다
	 *
	 * @throws InvalidInputException 요청 값이 없거나 글번호가 숫자가 아닐 때
	 * @throws NotFoundException     그 글이 없을 때, 요청한 파일이 그 글의 첨부 목록에 없을 때
	 */
	public void serviecDownload(HttpServletRequest request, HttpServletResponse response) throws IOException {

		//1. 요청 값 얻기
		String idxParam = request.getParameter("path");      //글번호
		String requestName = request.getParameter("fileName"); //저장 파일명

		if (idxParam == null || requestName == null) {   // 글번호나 파일명이 없으면 무엇을 내려받을지 알 수 없다
			throw new InvalidInputException("다운로드 요청 값이 올바르지 않습니다");   // 400 으로 막는다
		}

		final int articleNo;   // 글번호를 담을 변수. 아래 try 안에서만 값을 정한다
		try {
			articleNo = Integer.parseInt(idxParam.trim());   // 글자로 온 글번호를 숫자로 바꾼다
		} catch (NumberFormatException e) {
			throw new InvalidInputException("글번호가 올바르지 않습니다 : " + idxParam);   // 숫자가 아니면 400 으로 막는다
		}

		//2. [1차 방어] DB에서 이 글의 첨부파일 목록을 가져온다
		String[] attach = DBCPUtil.query(con -> boarddao.findAttachNames(con, articleNo));

		if (attach == null) {   // 그런 글이 없으면
			throw new NotFoundException("게시글을 찾을 수 없습니다");   // 404 로 막는다
		}

		String[] ofileArr = (attach[0] == null ? "" : attach[0]).split(";");   // 원본 이름 목록을 ; 기준으로 잘라 배열로 만든다
		String[] sfileArr = (attach[1] == null ? "" : attach[1]).split(";");   // 저장 이름 목록도 같은 방법으로 배열로 만든다

		//3. 요청한 파일명이 이 글의 첨부 목록에 실제로 있는지 확인
		int found = -1;
		for (int i = 0; i < sfileArr.length; i++) {   // 저장 이름 배열을 앞에서부터 훑는다
			if (sfileArr[i] != null && sfileArr[i].trim().equals(requestName.trim())) {   // 요청한 파일명과 똑같은 것을 찾으면
				found = i;   // 몇 번째인지 기억해 둔다 (원본 이름도 같은 번째에 있다)
				break;
			}
		}

		if (found < 0) {   // 목록에 없는 파일을 요청한 경우
			System.out.println("[FileBoardService] 첨부 목록에 없는 파일 요청을 차단했습니다 : " + requestName);   // 공격 시도일 수 있으므로 콘솔에 남긴다
			throw new NotFoundException("첨부파일을 찾을 수 없습니다");   // 404 로 막는다. 목록에 없는 파일은 절대 내려주지 않는다
		}

		String storedName = sfileArr[found].trim();   // 확인을 통과한 저장 파일명

		//사용자에게 보여줄 이름은 원본 파일명 (목록 길이가 어긋나면 저장명으로 대체)
		String downloadName = storedName;
		if (found < ofileArr.length && ofileArr[found] != null && !ofileArr[found].trim().isEmpty()) {   // 같은 번째에 원본 이름이 제대로 들어 있으면
			downloadName = ofileArr[found].trim();   // 사용자에게는 원본 이름으로 보이게 한다
		}

		//4. [2차 방어] 최종 경로가 저장 폴더 안인지 확인한 File 객체를 얻는다
		File target = FileRepo.resolveStoredFile(articleNo, storedName);

		//5. 응답 헤더 설정
		response.setContentType("application/octet-stream");
		response.setHeader("Cache-Control", "no-cache");   // 브라우저가 예전에 받은 파일을 다시 쓰지 않도록 캐시를 끈다
		response.addHeader("Cache-Control", "no-store");   // 저장조차 하지 말라는 지시를 한 줄 더 붙인다
		response.setContentLength((int) target.length()); //진행률 표시용 (업로드 상한 10MB라 int로 충분)

		//한글 파일명이 깨지지 않도록 인코딩 (filename*=최신 브라우저, filename=구형 브라우저)
		String encodedName = URLEncoder.encode(downloadName, "UTF-8").replace("+", "%20");
		response.setHeader("Content-Disposition",   // attachment 를 쓰면 브라우저가 화면에 열지 않고 "다운로드" 한다
				"attachment; filename=\"" + encodedName + "\"; filename*=UTF-8''" + encodedName);

		/*
		 6. 파일 내용을 8KB씩 읽어 브라우저로 전송

		    try-with-resources 를 쓰는 이유
		      마지막에 in.close() / out.close() 를 직접 호출하는 방식은, 중간에
		      예외가 나면 그 줄에 도달하지 못해 파일 핸들이 열린 채로 남는다.
		      (이런 누수가 쌓이면 "너무 많은 파일이 열렸습니다" 오류로 서버가
		       멈출 수 있다) try-with-resources 는 블록을 벗어나는 순간
		      예외가 있든 없든 자동으로 닫아 준다.
		*/
		try (FileInputStream in = new FileInputStream(target);
			 OutputStream out = response.getOutputStream()) {

			byte[] buffer = new byte[1024 * 8];   // 8KB 짜리 바구니를 만든다. 파일 전체를 한 번에 읽으면 큰 파일에서 메모리가 터진다
			int count;   // 이번에 읽은 바이트 수를 담을 변수
			while ((count = in.read(buffer)) != -1) {   // 읽을 것이 없으면 -1 이 나온다. 그때까지 반복한다
				out.write(buffer, 0, count);   // 읽은 만큼만 브라우저로 내보낸다
			}
			out.flush();   // 남아 있는 것을 마저 밀어낸다
		}

		//7. 다운로드 횟수 1 증가
		//사원(FileBoardDAO)에게 시키기 : 글번호를 FileBoardDAO 의 increaseDownloadCount(con, articleNo) 호출해서 다운로드 횟수 1 증가(update) 작업 명령
		DBCPUtil.execute(con -> boarddao.increaseDownloadCount(con, articleNo));
	}

	/* [정리] 이 자리에 있던 serviceBoardList() 와 serviceBoardKeyWord() 를 지웠다.
	   자유게시판(BoardService)과 같은 이유다. 페이징이 없던 시절 전체 목록을
	   한 번에 가져오던 메소드들인데, 지금은 사장(FileBoardController)이
	   목록·검색 모두 serviceBoardPage() 만 부르므로 호출하는 곳이 없었다.
	   (이 메소드들만 쓰던 FileBoardDAO 의 boardList(con) / boardList(con,key,word)
	    두 개도 함께 지웠다) */

	//----------------------------------------------------------------
	// serviceBoardPage : 게시글 목록을 페이지 단위로 조회한다 (SQL 페이징)
	//  사장의 "/list.bo", "/searchlist.bo" 요청에서 부른다
	//----------------------------------------------------------------
	/**
	 * 전체를 읽어 JSP 에서 자르던 방식을 SQL 페이징으로 바꿨다.
	 * 자세한 설명(왜 이렇게 바꿨는지, 트랜잭션으로 묶는 이유)은
	 * BoardService.serviceBoardPage() 의 주석을 참고 — 이 메소드도 완전히 같은 방식이다.
	 *
	 * @param key     검색 기준 ("titleContent" 또는 작성자명, 검색 없으면 null)
	 * @param word    검색어 (검색 없으면 null 또는 빈 문자열)
	 * @param nowPage 사장(Controller)이 요청 주소에서 읽어 넘긴 현재 페이지 번호 (0부터 시작)
	 */
	public PageResult<FileBoardVo> serviceBoardPage(String key, String word, int nowPage) {

		final int requestedPage = (nowPage < 0) ? 0 : nowPage;   // 음수 페이지로 들어오면 0페이지로 끌어올린다

		return DBCPUtil.query(con -> {   // 조회만 하므로 query 로 연결을 빌린다

			//1) 조건에 맞는 전체 글 개수
			int totalRecord = boarddao.countBoards(con, key, word);

			//2) 페이지 번호 보정 (글이 줄어 범위를 넘으면 마지막 페이지로)
			int totalPage = (totalRecord == 0) ? 0
							: (int) Math.ceil((double) totalRecord / NUM_PER_PAGE);

			int safePage = requestedPage;   // 보정할 페이지 번호를 담을 변수
			if (totalPage > 0 && safePage >= totalPage) {   // 페이지가 있는데 요청 번호가 마지막을 넘어섰다면
				safePage = totalPage - 1;   // 마지막 페이지로 맞춘다
			}

			//3) 그 페이지에 해당하는 글만 조회
			int offset = safePage * NUM_PER_PAGE;
			List<FileBoardVo> list = boarddao.boardList(con, key, word, offset, NUM_PER_PAGE);   // offset 만큼 건너뛰고 이 페이지 몫만 가져온다

			//4) 페이지 정보를 계산해 담아 반환
			return new PageResult<FileBoardVo>(list, totalRecord, safePage, NUM_PER_PAGE, PAGE_PER_BLOCK);
		});
	}

	//----------------------------------------------------------------
	// serviceBoardRead : 게시글 1건을 조회하면서 조회수를 1 올린다
	//  사장의 "/read.bo" 요청에서 부른다
	//----------------------------------------------------------------
	/**
	 * 자유게시판의 BoardService.serviceBoardRead() 와 완전히 같은 방식이다
	 * (조회 + 조회수 증가를 트랜잭션으로 묶는 이유, 없는 글을 404 로 응답하는
	 *  이유는 그쪽 설명을 참고).
	 *
	 * @throws InvalidInputException 글번호가 숫자가 아닐 때
	 * @throws NotFoundException     그 번호의 글이 없을 때
	 */
	public FileBoardVo serviceBoardRead(String b_idx_) {

		final int b_idx = parseIdx(b_idx_);   // 주소로 온 글번호를 숫자로 바꾼다

		return DBCPUtil.execute(con -> {   // 조회수 증가(수정)가 섞이므로 execute 를 쓴다

			//1) 글이 있는지부터 확인한다 (없는 글에 조회수를 올릴 필요가 없다)
			FileBoardVo found = boarddao.selectOne(con, b_idx);

			if (found == null) {
				throw new NotFoundException("요청한 게시글을 찾을 수 없습니다 (글번호 " + b_idx + ")");
			}

			boarddao.increaseReadCount(con, b_idx); //2) 조회수 +1

			//조회수를 올린 뒤의 값을 화면에 보여주기 위해 1 더해서 넘긴다
			found.setB_cnt(found.getB_cnt() + 1);

			return found;   // 조회수까지 반영된 글 한 건을 화면에 돌려준다
		});
	}

	//----------------------------------------------------------------
	// servicePassCheck : 글 비밀번호가 맞는지 확인한다 (수정·삭제 버튼 활성화 용도)
	//  사장의 "/password.do" 요청에서 부른다
	//----------------------------------------------------------------
	public boolean servicePassCheck(String b_idx, String password) {

		if (b_idx == null || password == null || password.isEmpty()) {   // 글번호나 비밀번호가 없으면 확인할 수 없다
			return false;   // 불일치로 처리한다
		}

		final int idx = parseIdx(b_idx);   // 글번호를 숫자로 바꾼다

		return DBCPUtil.query(con -> {   // 조회만 하므로 query 로 연결을 빌린다
			String stored = boarddao.findPasswordByIdx(con, idx);   // 저장된 글 비밀번호를 꺼낸다
			if (stored == null) {   // 그런 글이 없으면
				return Boolean.FALSE;   // 불일치로 처리한다
			}
			return Boolean.valueOf(java.util.Objects.equals(password, stored));   // 입력값과 저장값이 같은지 확인해 true/false 로 돌려준다
		}).booleanValue();   // Boolean 상자에서 진짜 true/false 값을 꺼낸다
	}

	//----------------------------------------------------------------
	// serviceUpdateBoard : 게시글을 수정한다 (첨부파일은 건드리지 않는다)
	//  사장의 "/updateBoard.do" 요청에서 부른다
	//  반환값 -> "수정성공" / "수정실패" / "비밀번호틀림"
	//----------------------------------------------------------------
	public String serviceUpdateBoard(String idx_, String email_, String title_, String content_, String pass_) {

		final int b_idx = parseIdx(idx_);   // 주소로 온 글번호를 숫자로 바꾼다

		return DBCPUtil.execute(con -> {   // 검증과 수정을 한 흐름으로 묶는다

			//1. 서버에서 비밀번호를 다시 검증한다 (화면 버튼 숨김은 보안이 아니다)
			String stored = boarddao.findPasswordByIdx(con, b_idx);

			if (stored == null) {   // 그 글번호의 저장값을 못 찾은 경우 = 없는 글
				return "수정실패";   // 수정할 것이 없으므로 실패로 돌려준다
			}
			if (pass_ == null || !java.util.Objects.equals(pass_, stored)) {   // 비밀번호를 안 보냈거나 저장값과 다르면
				System.out.println("[FileBoardService] 글 수정 거부 - 비밀번호 불일치 b_idx=" + b_idx);   // 누가 언제 실패했는지 콘솔에 남겨 둔다
				return "비밀번호틀림";   // 화면에 "비밀번호가 틀렸습니다" 를 띄우게 한다
			}

			//2. 검증 통과 후 수정
			int result = boarddao.updateBoard(con, b_idx, email_, title_, content_);

			return (result == 1) ? "수정성공" : "수정실패";   // 1건이 바뀌었으면 성공, 아니면 실패 문구를 돌려준다
		});
	}

	//----------------------------------------------------------------
	// serviceDeleteBoard : 게시글을 삭제한다 (DB 삭제 + 첨부파일 폴더 삭제)
	//  사장의 "/deleteBoard.do" 요청에서 부른다
	//  반환값 -> "삭제성공" / "삭제실패" / "비밀번호틀림"
	//----------------------------------------------------------------
	/**
	 * [글 삭제 후 첨부파일도 함께 지우는 이유]
	 *   과거에는 DB 에서 글만 지우고 디스크의 첨부파일은 그대로 남겨 두었다.
	 *   글을 삭제한 뒤에도 C:\file_repo\5\ 폴더 안의 파일이 계속 남아 있었던
	 *   것이다. DB 에는 그 글이 이미 없으니 화면에서 지울 방법도, 다시
	 *   다운로드할 방법도 없다 — 즉 아무도 손댈 수 없는 "고아 파일"이
	 *   디스크에 계속 쌓인다. 첨부파일 용량이 큰 서비스에서는 이것만으로도
	 *   디스크 공간이 부족해질 수 있다.
	 *
	 * [삭제 순서가 중요한 이유 - DB 를 먼저, 파일은 그 다음]
	 *   반드시 DB 삭제가 성공한 뒤에 파일을 지운다. 만약 파일을 먼저
	 *   지운다면, 그 직후 DB 삭제가 실패했을 때 "글은 남아 있는데 첨부
	 *   파일만 사라진" 상태가 된다 — 다운로드 버튼은 보이는데 눌러도
	 *   파일이 없는, DB 만 삭제 실패했을 때보다 더 나쁜 상태다.
	 *
	 * [파일 삭제 실패는 트랜잭션을 되돌리지(rollback) 않는 이유]
	 *   파일 삭제가 어떤 이유로 실패하더라도, "그 글이 DB 에서 삭제된 것"
	 *   자체는 이미 사실이고 올바른 결과다. 여기서 예외를 던져 DB 삭제까지
	 *   rollback 시키면 사용자에게는 "삭제가 안 된 것"처럼 보이게 되어
	 *   오히려 더 혼란스럽다. 그래서 파일 삭제 실패는 rollback 하지 않고
	 *   콘솔 로그로만 남긴다 (남은 파일은 나중에 정리 배치로 치우는 것이 실무 방식이다).
	 */
	public String serviceDeleteBoard(String delete_idx, String pass_) {

		final int b_idx = parseIdx(delete_idx);   // 주소로 온 글번호를 숫자로 바꾼다

		return DBCPUtil.execute(con -> {   // 검증·삭제·파일 정리를 한 흐름으로 묶는다

			//1. 비밀번호 검증
			String stored = boarddao.findPasswordByIdx(con, b_idx);

			if (stored == null) {   // 그 글번호의 저장값을 못 찾은 경우 = 없는 글
				return "삭제실패";   // 지울 것이 없으므로 실패로 돌려준다
			}
			if (pass_ == null || !java.util.Objects.equals(pass_, stored)) {   // 비밀번호를 안 보냈거나 저장값과 다르면
				System.out.println("[FileBoardService] 글 삭제 거부 - 비밀번호 불일치 b_idx=" + b_idx);   // 누가 언제 실패했는지 콘솔에 남겨 둔다
				return "비밀번호틀림";   // 삭제를 실행하지 않고 실패 문구를 돌려준다
			}

			//2. 검증 통과 후 삭제
			int result = boarddao.deleteBoard(con, b_idx);

			//3. DB 삭제가 성공했을 때만 첨부파일 폴더도 지운다 (자세한 순서 이유는 위 메소드 설명 참고)
			if (result == 1) {
				deleteArticleFiles(b_idx);
			}

			return (result == 1) ? "삭제성공" : "삭제실패";   // 1건이 지워졌으면 성공, 아니면 실패 문구를 돌려준다
		});
	}

	/**
	 * temp 폴더에 써 둔 파일들을 지운다. (업로드 후 글 등록이 검증에서 중단됐을 때 호출)
	 * @param sfileNames ';' 로 이어진 저장명 목록 (upload() 가 만든 값)
	 */
	private void deleteTempFiles(String sfileNames) {

		if (sfileNames == null || sfileNames.trim().isEmpty()) {   // 첨부가 없었으면 지울 것도 없다
			return;   //첨부가 없었다
		}

		for (String stored : sfileNames.split(";")) {   // ; 를 기준으로 잘라 파일 하나씩 처리한다

			if (stored == null || stored.trim().isEmpty()) {   // 빈 조각이면 건너뛴다
				continue;
			}

			try {
				File f = new File(FileRepo.getTempDir(), stored.trim());   // 임시 폴더에 있는 그 파일을 가리킨다
				if (f.exists() && f.delete()) {   // 실제로 있고 지우기도 성공했으면
					System.out.println("[FileBoardService] 임시 파일 정리 : " + f.getName());   // 무엇을 치웠는지 콘솔에 남긴다
				}
			} catch (Exception e) {
				//정리 실패는 기능에 영향이 없다. 원인만 남긴다.
				System.out.println("[FileBoardService] 임시 파일 정리 실패 : " + stored + " - " + e.getMessage());
			}
		}
	}

	/**
	 * 글 하나의 첨부파일 폴더를 통째로 지운다. (file_repo/글번호/)
	 * [부르는 곳] serviceDeleteBoard() — DB 삭제가 성공했을 때만 호출된다
	 *
	 * 폴더 단위로 지우는 이유
	 *   파일명이 UUID 로 저장되어 있어, DB 의 sfile 값과 하나하나 맞춰
	 *   봐야만 어떤 파일이 이 글의 것인지 알 수 있다. 그런데 이 글의 첨부
	 *   폴더(FileRepo.getArticleDir 이 관리하는 글번호별 폴더)에는 애초에
	 *   이 글의 첨부파일만 들어 있으므로, 폴더 전체를 지우면 이름을 하나씩
	 *   대조할 필요 없이 한 번에 깔끔하게 정리된다.
	 */
	private void deleteArticleFiles(int b_idx) {

		try {
			File dir = util.FileRepo.getArticleDir(b_idx);   // 이 글의 첨부파일 폴더를 가리킨다

			if (dir == null || !dir.exists()) {   // 폴더가 아예 없으면 = 첨부가 없던 글이다
				return;   //첨부가 없던 글
			}

			//commons-io 의 FileUtils : 폴더와 그 안의 파일을 함께 지운다
			FileUtils.deleteDirectory(dir);
			System.out.println("[FileBoardService] 첨부파일 폴더 삭제 완료 : " + dir.getAbsolutePath());   // 무엇을 지웠는지 콘솔에 남긴다

		} catch (Exception e) {
			//실패해도 글 삭제는 유효하다. 원인만 남긴다.
			System.out.println("[FileBoardService] 첨부파일 삭제 실패 (글은 삭제됨) b_idx=" + b_idx + " : " + e.getMessage());
		}
	}

	//----------------------------------------------------------------
	// serviceReplyInsertBoard : 답글을 등록한다 (첨부파일이 없는 답글)
	//  사장의 "/replyPro.do" 요청에서 부른다
	//----------------------------------------------------------------
	/**
	 * 계층형 정렬 규칙과 트랜잭션이 필요한 이유는 자유게시판의
	 * BoardService.serviceReplyInsertBoard() 와 완전히 같다. 다른 점은 답글에
	 * 첨부파일이 없어서 ofile/sfile 을 null 로 저장한다는 것뿐이다.
	 */
	public void serviceReplyInsertBoard(String super_b_idx, String reply_id, String reply_name,
										String reply_email, String reply_title, String reply_content,
										String reply_pass) {

		if (reply_pass == null || reply_pass.trim().isEmpty()) {   // 답글 비밀번호가 비어 있으면
			System.out.println("[FileBoardService] 답글 비밀번호가 비어 있어 등록을 중단합니다");   // 왜 중단했는지 콘솔에 남긴다
			return;   // 아무것도 하지 않고 메소드를 끝낸다
		}

		final String encodedPass = (reply_pass.trim());   // 앞뒤 공백을 없앤 답글 비밀번호
		final int parentIdx = parseIdx(super_b_idx);   // 답글을 달 원글의 번호를 숫자로 바꾼다

		//[트랜잭션] 조회 + 밀기 + 등록을 한 작업으로 묶는다
		DBCPUtil.execute(con -> {

			int[] groupLevel = boarddao.findGroupAndLevel(con, parentIdx);   // 원글의 정렬 그룹번호와 들여쓰기 깊이를 함께 가져온다

			if (groupLevel == null) {   // 원글을 못 찾은 경우
				System.out.println("[FileBoardService] 답글을 달 원글이 없습니다. b_idx=" + parentIdx);   // 왜 중단했는지 콘솔에 남긴다
				return 0;   // 아무것도 저장하지 않고 0 을 돌려준다
			}
			
			//답변글을 작성하는 부모글의 b_group 열의 값보다 큰 기존 주글의 b_group열의 값을 1증가(update) 시키는 명령
			boarddao.shiftGroupsGreaterThan(con, groupLevel[0]);   

			//답변글 추가. 작성한 답글 정보 DB의 테이블에 INSERT 추가 해~~ 명령
			//답변글 추가 조건2. 답변글의 들여쓰기 정도값(b_level 열의 값)은 부모글의 b_level열 값 + 1 한 값 insert  
			//답변글 추가 조건3. 답변글의 그룹 정렬값(b_group 열의 값)은 부모글의 b_group열 값 + 1 한 값 insert
			return boarddao.insertReply(con, reply_id, encodedPass, reply_name, reply_email,   // 만들어진 자리에 답글을 저장한다. 그룹은 원글과 같고 깊이는 한 단계 아래다
										reply_title, reply_content,
										groupLevel[0] + 1, groupLevel[1] + 1);
		});
	}

	//===========================================================
	// 내부 공통
	//===========================================================
	/** 글번호 문자열 -> 정수. 잘못된 값이면 400 예외 (자유게시판의 parseIdx() 와 같은 이유) */
	private int parseIdx(String value) {
		try {
			return Integer.parseInt(value.trim());   // 글자를 숫자로 바꾼다
		} catch (Exception e) {
			throw new InvalidInputException("글번호가 올바르지 않습니다 : " + value);   // 숫자가 아니면 400 으로 막는다
		}
	}
}
