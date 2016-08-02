import java.util.Hashtable;

public class MyMethod {
	String accessType;
	String return_type;
	String method_name;
	Hashtable<String,String> formalParameters; 
	public MyMethod(String a, String rt, String mn, Hashtable<String,String> myp) {
		this.accessType = a;
		this.return_type = rt;
		this.method_name = mn;
		this.formalParameters = myp; 
		
	}

}
