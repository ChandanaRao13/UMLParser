import java.util.Hashtable;

public class MyConstructor {
	String accessType;
	String method_name;
	Hashtable<String,String> formalParameters; 
	public MyConstructor(String a, String mn, Hashtable<String,String> myp) {
		this.accessType = a;
		this.method_name = mn;
		this.formalParameters = myp; 
		
	}

}
