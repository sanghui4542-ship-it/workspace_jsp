package sec01.ex02;


//회원의 거주 도시이름과  우펴번호를 저장해 놓고 제공하는 클래스
public class Address { //--> new Address(); 객체 생성후 MemberVO클래스의 변수에 저장(포함)할 것입니다.
	
	private String city; 	//도시이름
	private String zipcode; //우편번호

	//getter
	public String getCity() {   return city;  }
	public String getZipcode() { return zipcode; }

	//setter
	public void setCity(String city) { this.city = city; }
	public void setZipcode(String zipcode) { this.zipcode = zipcode; }
	
}

