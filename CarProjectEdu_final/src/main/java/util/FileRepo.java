package util;

/*
 * ============================================================================
 *  FileRepo  -  자료실 게시판 첨부파일의 저장 위치와 안전 검사를 담당하는 도우미 클래스
 *
 *  저장 폴더 구조
 *    C:\file_repo_edu\              <- 저장소 최상위 폴더 (getRoot)
 *        temp\                      <- 업로드 중 임시 파일 폴더 (getTempDir)
 *        1\  2\  3\ ...             <- 글번호별 첨부파일 폴더 (getArticleDir)
 *            5f3a...c1.png          <- 실제 저장 파일 (겹치지 않는 무작위 이름)
 *
 *  안전 검사
 *    파일 이름 정리    : 경로 기호(\ / :), 위험 문자 제거 (cleanOriginalName)
 *    확장자 검사       : 허용 목록에 있는 확장자만 업로드 (validateExtension)
 *    다운로드 경로 검사 : 저장 폴더 밖의 파일을 내려받지 못하게 막기 (resolveStoredFile)
 *
 *  사용하는 곳 : 부장(FileBoardService), 사장(FileBoardController)
 * ============================================================================
 */

// 파일, 입출력 예외
import java.io.File;
import java.io.IOException;

// 목록 도구, 무작위 이름 생성 도구
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

// 우리가 직접 만든 예외 클래스들
import exception.ForbiddenException;
import exception.InvalidInputException;
import exception.NotFoundException;

public class FileRepo {

	// 첨부파일 저장소 최상위 폴더 (윈도우 경로의 \ 는 자바 글자 안에서 \\ 로 쓴다)
	private static final String DEFAULT_ROOT   = "C:\\file_repo_edu";

	// 파일 1개 최대 크기 : 10MB (10 x 1024 x 1024 바이트)
	private static final long   DEFAULT_MAX_FILE = 10L * 1024 * 1024;

	// 요청 1번(파일 여러 개 합계) 최대 크기 : 30MB
	private static final long   DEFAULT_MAX_REQ  = 30L * 1024 * 1024;

	// 업로드를 허용하는 확장자 목록 (아래 static 블럭에서 채운다)
	private static final Set<String> DEFAULT_ALLOWED_EXT;

	// static 블럭 : FileRepo 를 처음 사용할 때 딱 1번 실행되어 허용 확장자 목록을 만든다
	static {

		// 허용 확장자들을 목록(Set)에 담는다 (실행 파일 exe, jsp 등은 올릴 수 없다)
		Set<String> s = new HashSet<String>(Arrays.asList(
				"jpg", "jpeg", "png", "gif", "bmp", "webp",
				"pdf", "txt", "csv", "hwp", "hwpx",
				"doc", "docx", "xls", "xlsx", "ppt", "pptx",
				"zip", "7z"
		));

		// 다른 곳에서 목록을 바꾸지 못하도록 읽기 전용 목록으로 만들어 저장한다
		DEFAULT_ALLOWED_EXT = Collections.unmodifiableSet(s);
	}

	// 유틸 클래스이므로 객체를 만들지 못하게 막는다 (모든 메소드가 static)
	private FileRepo() {}

	//----------------------------------------------------------------
	// getRoot : 저장소 최상위 폴더를 반환한다  ->  C:\file_repo_edu
	//----------------------------------------------------------------
	public static File getRoot() {
		return new File(DEFAULT_ROOT);
	}

	//----------------------------------------------------------------
	// getTempDir : 업로드 중 임시 파일을 두는 폴더를 반환한다  ->  C:\file_repo_edu\temp
	//----------------------------------------------------------------
	public static File getTempDir() {
		return new File(getRoot(), "temp");
	}

	//----------------------------------------------------------------
	// getArticleDir : 글번호별 첨부파일 폴더를 반환한다  예) 글번호 7  ->  C:\file_repo_edu\7
	//----------------------------------------------------------------
	public static File getArticleDir(int articleNo) {
		return new File(getRoot(), String.valueOf(articleNo));
	}

	//----------------------------------------------------------------
	// getMaxFileSize : 파일 1개 최대 크기(10MB)를 반환한다
	//----------------------------------------------------------------
	public static long getMaxFileSize() {
		return DEFAULT_MAX_FILE;
	}

	//----------------------------------------------------------------
	// getMaxRequestSize : 요청 1번의 최대 크기(30MB)를 반환한다
	//----------------------------------------------------------------
	public static long getMaxRequestSize() {
		return DEFAULT_MAX_REQ;
	}

	//----------------------------------------------------------------
	// getAllowedExtensions : 업로드 허용 확장자 목록을 반환한다
	//----------------------------------------------------------------
	public static Set<String> getAllowedExtensions() {
		return DEFAULT_ALLOWED_EXT;
	}

	//----------------------------------------------------------------
	// cleanOriginalName : 고객이 올린 파일 이름에서 위험한 부분을 지워 안전한 이름으로 반환한다
	//  예) "C:\Users\me\사진.png"  ->  "사진.png"   (폴더 경로 제거)
	//      "..\..\web.xml"         ->  "web.xml"    (앞의 점 제거)
	//      "a:b*c?.txt"            ->  "a_b_c_.txt" (파일 이름에 쓸 수 없는 문자를 _ 로)
	//----------------------------------------------------------------
	public static String cleanOriginalName(String rawName) {

		// 파일 이름이 없으면 400 예외
		if (rawName == null) {
			throw new InvalidInputException("첨부파일 이름이 없습니다");
		}

		// 정리할 이름을 name 변수에 복사
		String name = rawName;

		// 마지막 \ 또는 / 의 위치를 찾아서, 그 뒤의 글자(파일 이름)만 남긴다
		int slash = Math.max(name.lastIndexOf('\\'), name.lastIndexOf('/'));
		if (slash >= 0) {
			name = name.substring(slash + 1);
		}

		// 파일 이름에 쓸 수 없는 문자( \ / : * ? " < > | ; 줄바꿈 탭)를 _ 로 바꾸고 앞뒤 공백을 지운다
		name = name.replaceAll("[\\\\/:*?\"<>|;\\r\\n\\t]", "_").trim();

		// 이름이 점(.)으로 시작하면 점을 모두 지운다 (숨김 파일, 상위 폴더 이동 방지)
		while (name.startsWith(".")) {
			name = name.substring(1);
		}

		// 정리했더니 이름이 비어 있으면 400 예외
		if (name.isEmpty()) {
			throw new InvalidInputException("첨부파일 이름이 올바르지 않습니다");
		}

		// 이름이 120글자보다 길면 확장자는 살리고 앞부분만 잘라 120글자로 맞춘다
		if (name.length() > 120) {
			String ext = getExtension(name);
			int keep = 120 - (ext.isEmpty() ? 0 : ext.length() + 1);
			name = name.substring(0, Math.max(1, keep)) + (ext.isEmpty() ? "" : "." + ext);
		}

		// 정리된 파일 이름 반환
		return name;
	}

	//----------------------------------------------------------------
	// getExtension : 파일 이름에서 확장자를 소문자로 꺼내 반환한다. 없으면 빈 글자
	//  예) "사진.PNG" -> "png" / "readme" -> "" / "a." -> ""
	//----------------------------------------------------------------
	public static String getExtension(String fileName) {
		
		// 파일 이름이 없으면 빈 글자 반환
		if (fileName == null) {
			return "";
		}

		// 마지막 점(.)의 위치를 찾는다
		int dot = fileName.lastIndexOf('.');

		// 점이 없거나 맨 끝에 있으면 확장자가 없으므로 빈 글자 반환
		if (dot < 0 || dot == fileName.length() - 1) {
			return "";
		}

		// 점 뒤의 글자를 소문자로 바꿔 반환
		return fileName.substring(dot + 1).toLowerCase();
	}

	//----------------------------------------------------------------
	// validateExtension : 확장자가 허용 목록에 있는지 검사한다. 없으면 400 예외
	//----------------------------------------------------------------
	public static void validateExtension(String fileName) {

		// 확장자를 꺼낸다
		String ext = getExtension(fileName);

		// 확장자가 없으면 400 예외
		if (ext.isEmpty()) {
			throw new InvalidInputException("확장자가 없는 파일은 올릴 수 없습니다 : " + fileName);
		}

		// 허용 목록에 없는 확장자면 400 예외 (허용 확장자 목록을 함께 알려준다)
		if (!getAllowedExtensions().contains(ext)) {
			throw new InvalidInputException(
					"허용되지 않는 파일 형식입니다 : ." + ext
					+ " (허용 : " + String.join(", ", getAllowedExtensions()) + ")");
		}
	}

	//----------------------------------------------------------------
	// newStoredName : 실제로 저장할 때 쓸 겹치지 않는 무작위 파일 이름을 만들어 반환한다
	//  같은 이름의 파일을 여러 명이 올려도 서로 덮어쓰지 않게 하기 위해서다
	//  예) "사진.png"  ->  "5f3a9c...e1.png"  (UUID 32글자 + 원래 확장자)
	//----------------------------------------------------------------
	public static String newStoredName(String originalName) {

		// 원래 파일의 확장자를 꺼낸다
		String ext = getExtension(originalName);

		// 겹칠 일이 거의 없는 무작위 글자(UUID)를 만들고 - 기호를 지운다
		String uuid = UUID.randomUUID().toString().replace("-", "");

		// 확장자가 없으면 무작위 글자만, 있으면 "무작위글자.확장자" 반환
		return ext.isEmpty() ? uuid : (uuid + "." + ext);
	}

	//----------------------------------------------------------------
	// resolveStoredFile : 다운로드할 파일을 찾아 반환한다 (저장 폴더 밖의 파일은 막는다)
	//  예) ?storedName=../../WEB-INF/web.xml  ->  403 예외
	//----------------------------------------------------------------
	public static File resolveStoredFile(int articleNo, String storedName) {

		// 파일 이름이 없으면 400 예외
		if (storedName == null || storedName.trim().isEmpty()) {
			throw new InvalidInputException("다운로드할 파일명이 없습니다");
		}

		// 파일 이름에 폴더 이동 기호(/ \ ..)가 들어 있으면 403 예외
		if (storedName.contains("/") || storedName.contains("\\") || storedName.contains("..")) {
			throw new ForbiddenException("잘못된 파일 경로 요청입니다 : " + storedName);
		}

		// 글번호 폴더 안의 파일 경로를 만든다  예) C:\file_repo_edu\7\5f3a...e1.png
		File target = new File(getArticleDir(articleNo), storedName);

		try {

			// 저장소 폴더와 대상 파일의 실제 전체 경로를 구한다 (.. 같은 표현을 모두 풀어낸 경로)
			String rootPath   = getRoot().getCanonicalPath();
			String targetPath = target.getCanonicalPath();

			// 대상 파일이 저장소 폴더 안에 있지 않으면 403 예외
			if (!targetPath.startsWith(rootPath + File.separator)) {
				throw new ForbiddenException("저장 폴더를 벗어난 파일 요청입니다");
			}

			// 파일이 없거나 폴더라면 404 예외
			if (!target.exists() || !target.isFile()) {
				throw new NotFoundException("파일을 찾을 수 없습니다 : " + storedName);
			}

			// 검사를 모두 통과한 파일 반환
			return target;

		// 실제 경로를 구하다 실패하면 404 예외
		} catch (IOException e) {
			throw new NotFoundException("파일 경로를 확인할 수 없습니다 : " + storedName);
		}
	}
}
