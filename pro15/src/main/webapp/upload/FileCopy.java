


/*
    Window 운영체제에서 파일 복사 방법
    -> copy.exe 프로그램 사용
    
    예) cmd(명령프롬포터창) 열기
       
        copy 원본파일명 복사해서새로생성할파일명
        
       
 주제 : 윈도우 운영체제에서 제공하는 파일을 복사할수 있는 기능의 copy.exe프로그램 처럼
       FileCopy.class 자프로그램을 자바언어로 만들자. 
*/

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileCopy { 
	//명령 프롬포터 창에 입력해서 테스트할 구문
	
	// java FileCopy Test.txt Test2.txt  
				
	//              ["원본파일명", "원본파일을 복사 해서 새로 생성 되는 파일명"]
	//				["Test.txt", "Test2.txt"					   ] <- String[] 배열 
	// 					0           1          index
	public static void main(String[] args)  throws FileNotFoundException, IOException{
		
		int data = 0;  //"원본 파일" 전체 데이터 중에서 1바이트크기의 데이터만 읽어 저장할 변수 
		int size = 0;  //"원본 파일" 에서 읽어들인 bytes 크기를 누적해서 저장할 변수 
		
		//java FileCopy Test.txt  <--- 원본 파일명 만 입력하고  복사될 새파일명을 입력하지 않았다면?
		if(args.length < 2) {
			
			System.out.println("java FileCopy 원본파일 복사될새파일명");
			
			//잘못된 입력은 FileCopy.class 자바프로그램 강제 정상 종료 
			System.exit(0);
		}
		
		//java FileCopy Test.txt Test2.txt <- 원본 파일명과 복사될_새파일명을 모두 정상 입력해서 파일 복사 프로그램을 만든다면?
		
		//첫번쨰로 입력받은 "원본파일명" : Test.txt 문자열 경로를 넣어  바이트 단위로 읽어들일 FileInputStream 입력스트림 통로 생성
		FileInputStream  fis = new FileInputStream(args[0]);
//		FileInputStream  fis = new FileInputStream("Test.txt");		
		
		//두번쨰로 입력받은 "복사될_새파일명" : Test2.txt 문자열 경로를 또 넣어 바이트 단위로 내보내어 기록할 FileOutputStream 출력스트림 통로 생성
		FileOutputStream fos = new FileOutputStream(args[1]);
		
		//"원본파일" Test.txt파일에 저장되어 있는 데이터를 한 바이트씩(한 문자씩, 1byte) 읽어 들여 
		//"복사될 새파일" Test2.txt파일에 기록(출력, 내보냄, 쓰기) 하자.
		while(  (data = fis.read() ) != -1   ) {
			
			//"복사될 새파일"에 읽어들인 한 바이트의 데이터 단위로 기록(내보내어 쓰기)
			fos.write(data);
			
			//한바이트 읽어 들인 바이트 크기(한 문자를 읽어들인 크기)를  ++ 해서 1증가 시키자
			//이유 : 읽어들인 총 문자 갯수 구하기 위해
			size++;
		}
		
		//자원해제 (FileInputStream, FileOutputStream 스트림 통로 역할을 하는 객체 메모리 모두 사용후 JVM에서 제거)
		fis.close();               fos.close();
		
		//Test.txt파일의 데이터르  Test2.txt로 복사한 문자 갯수 출력
		System.out.println(size + "bytes are copied..................");

	} // ---------- main 

} // -------------------------------- FileCopy.class  카피 프로그램 끝 







