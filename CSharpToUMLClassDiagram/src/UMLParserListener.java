import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

public class UMLParserListener extends CSharp4BaseListener {

	CSharp4Parser parser;

	//To store the current class name and current Interface name being parsed
	String currentClass,currentInterface;

	//Class 
	ArrayList<MyProperty> myClassProperties = new ArrayList<MyProperty>();
	ArrayList<MyMethod> myClassMethods = new ArrayList<MyMethod>();
	ArrayList<MyConstructor> myClassConstructors = new ArrayList<MyConstructor>();

	//Interface
	ArrayList<MyProperty> myInterfaceProperties = new ArrayList<MyProperty>();
	ArrayList<MyMethod> myInterfaceMethods = new ArrayList<MyMethod>();



	public UMLParserListener(CSharp4Parser parser){
		this.parser = parser;
	}

	@Override
	public void enterClass_definition(CSharp4Parser.Class_definitionContext ctx) {

		currentClass = ctx.getChild(1).getText(); //UMLParserToolCSharp class name
		ArrayList<String> extendsImplementsList = new ArrayList<String>();
		UMLParserToolCSharp.extendsImpMap.put(currentClass ,extendsImplementsList);
		UMLParserToolCSharp.classList.add(currentClass);
		if(ctx.class_base() != null){
			if(ctx.class_base().getChildCount() > 1){
				for(int index = 1 ;index < ctx.class_base().getChildCount() ; index = index+2){
					if(ctx.class_base().getChild(index) != null){
						if(ctx.class_base().getChild(index).getChild(0).getChild(0).getChild(0)!=null){
							extendsImplementsList.add(ctx.class_base().getChild(index).getChild(0).getChild(0).getChild(0).getText());
						}
					}
				}
			}
		}
		UMLParserToolCSharp.extendsImpMap.put(currentClass ,extendsImplementsList);
	}

	@Override
	public void enterInterface_definition(CSharp4Parser.Interface_definitionContext ctx) {
		currentInterface = ctx.getChild(1).getText();
		UMLParserToolCSharp.interfaceList.add(currentInterface);

	}

	@Override
	public void enterTyped_member_declaration(CSharp4Parser.Typed_member_declarationContext ctx) {
		String symbol = "";
		String accessModifier = ctx.getParent().getParent().getChild(0).getChild(0).getText();
		switch(accessModifier){
		case "public":symbol = "+";break;
		case "private": symbol = "-";break;
		}
		if (ctx.type() != null && ctx.getParent().getParent().getChildCount() > 1) {
			if (ctx.type().base_type().simple_type() != null && !(accessModifier.equals("protected"))) {
				myClassProperties.add(new MyProperty(symbol, ctx.getChild(1).getChild(0).getChild(0).getText(),ctx.getChild(0).getText()));
			}else if(ctx.type().base_type().class_type() != null){
				if (isPrimitiveType(ctx.type().base_type().class_type().getText()) && !(accessModifier.equals("protected")) && ctx.field_declaration2() != null) {
					myClassProperties.add(new MyProperty(symbol, ctx.field_declaration2().variable_declarators().getChild(0).getText(),
							ctx.type().base_type().class_type().getText()));
				}
				if(ctx.type().base_type().class_type().type_name() !=  null) {

					if(ctx.type().base_type().class_type().type_name().namespace_or_type_name().getText().contains("<")) {
						String collClass = ctx.type().base_type().class_type().type_name().namespace_or_type_name().type_argument_list_opt().get(0).getText();
						String finalClass = collClass.substring(1, collClass.length()-1);
						if(! isPrimitiveType(finalClass))
							UMLParserToolCSharp.associationList.add(new AssociationRelationStruct(currentClass,finalClass,true));
					}else{
						UMLParserToolCSharp.associationList.add(new AssociationRelationStruct(currentClass,ctx.type().base_type().class_type().type_name().namespace_or_type_name().identifier().get(0).getText(),false));
					}
				}

			}
		}
		if(ctx.method_declaration2()!=null){
			if(symbol.equals("+")){
				Hashtable<String,String> formalParameters = new Hashtable<String,String>();
				if(ctx.getChild(1).getChildCount()>4){
					formalParameters.put(ctx.method_declaration2().formal_parameter_list().fixed_parameters().fixed_parameter().get(0).getChild(0).getText(),ctx.method_declaration2().formal_parameter_list().fixed_parameters().fixed_parameter().get(0).getChild(1).getText());
				}
				myClassMethods.add(new MyMethod("+",ctx.getChild(0).getText(),ctx.method_declaration2().method_member_name().getText(),formalParameters));
			}
			if(ctx.method_declaration2().formal_parameter_list()!=null){
				UMLParserToolCSharp.usesList.add(new MyRelationMaps(currentClass,ctx.method_declaration2().formal_parameter_list().fixed_parameters().fixed_parameter().get(0).getText()));
			}
		}
	}

	@Override
	public void enterCommon_member_declaration(CSharp4Parser.Common_member_declarationContext ctx){
		if(ctx.getParent().getChild(0)!=null){
			if(ctx.getParent().getChild(0).getText().equals("public")){
				String return_type = ctx.getChild(0).getText();
				Hashtable<String,String> formalParameters = new Hashtable<String,String>();
				if(ctx.getChild(1)!=null){
					if(ctx.getChild(1).getChildCount()>4){
						formalParameters.put(ctx.method_declaration2().formal_parameter_list().fixed_parameters().fixed_parameter().get(0).getChild(1).getText(),ctx.method_declaration2().formal_parameter_list().fixed_parameters().fixed_parameter().get(0).getChild(0).getText());
					}
					myClassMethods.add(new MyMethod("+",return_type,ctx.method_declaration2().method_member_name().getText(),formalParameters));
				}
			}
			else if(ctx.getParent().getChild(0).getChild(0)!=null && ctx.getParent().getChild(0).getChild(1)!=null){
				if(ctx.getParent().getChild(0).getChild(0).getText().equals("public")&& ctx.getParent().getChild(0).getChild(1).getText().equals("static")){
					String return_type = ctx.getChild(0).getText();
					Hashtable<String,String> formalParameters = new Hashtable<String,String>();
					if(ctx.getChild(1).getChildCount()>4){
						formalParameters.put(ctx.method_declaration2().formal_parameter_list().fixed_parameters().fixed_parameter().get(0).getChild(1).getText(),ctx.method_declaration2().formal_parameter_list().fixed_parameters().fixed_parameter().get(0).getChild(0).getText());
					}
					myClassMethods.add(new MyMethod("+",return_type,ctx.method_declaration2().method_member_name().getText(),formalParameters));

				}
			}
		}

		if(ctx.method_declaration2()!=null){
			if(ctx.method_declaration2().formal_parameter_list()!=null){
				UMLParserToolCSharp.usesList.add(new MyRelationMaps(currentClass,ctx.method_declaration2().formal_parameter_list().fixed_parameters().fixed_parameter().get(0).getChild(0).getText()));
			}
		}
	}

	@Override
	public void enterProperty_declaration2(CSharp4Parser.Property_declaration2Context ctx) {
		String accessModifier = "";
		if((ctx.getParent().getParent().getParent().getChild(0).getText().contains("public"))) {
			accessModifier = "public";
		}

		if (accessModifier.equals("public")) {
			myClassMethods.add(new MyMethod("+",ctx.getParent().getChild(0).getChild(0).getText(),ctx.member_name().interface_type().type_name().namespace_or_type_name().identifier().get(0).getText(),null));			
		}
	}

	@Override
	public void enterInterface_member_declaration(CSharp4Parser.Interface_member_declarationContext ctx) {

		if (ctx.formal_parameter_list() != null) {
			Hashtable<String,String> formalParameters = new Hashtable<String,String>();
			formalParameters.put(ctx.formal_parameter_list().fixed_parameters().fixed_parameter().get(0).getChild(0).getText(),ctx.formal_parameter_list().fixed_parameters().fixed_parameter().get(0).getChild(1).getText());
			myInterfaceMethods.add(new MyMethod("+",ctx.getChild(0).getText(),ctx.identifier().getText(),formalParameters));
		}else
			myInterfaceMethods.add(new MyMethod("+",ctx.getChild(0).getText(),ctx.identifier().getText(),null));
	}

	@Override
	public void enterConstructor_declaration2(CSharp4Parser.Constructor_declaration2Context ctx) {
		String accessModifier = "";
		if(ctx.getParent().getParent().getChild(0).getText().contains("public")){
			accessModifier = "public";
		}
		if (accessModifier.equals("public")) {
			if(ctx.formal_parameter_list() != null) {
				Hashtable<String,String> formalParameters = new Hashtable<String,String>();
				formalParameters.put(ctx.formal_parameter_list().fixed_parameters().fixed_parameter().get(0).getChild(1).getText(),ctx.formal_parameter_list().fixed_parameters().fixed_parameter().get(0).getChild(0).getText());
				myClassConstructors.add(new MyConstructor("+",ctx.identifier().getText(),formalParameters));
			}else
				myClassConstructors.add(new MyConstructor("+",ctx.identifier().getText(),null));
		}
		if(ctx.formal_parameter_list()!=null){
			UMLParserToolCSharp.usesList.add(new MyRelationMaps(currentClass,ctx.formal_parameter_list().fixed_parameters().fixed_parameter().get(0).getChild(0).getText()));
		}
	}

	@Override
	public void enterLocal_variable_declaration(CSharp4Parser.Local_variable_declarationContext ctx) {
		if(ctx.local_variable_type()!=null){
			if(ctx.local_variable_type().type()!=null){
				if(ctx.local_variable_type().type().base_type()!=null){
					if(ctx.local_variable_type().type().base_type().class_type()!=null){
						if(ctx.local_variable_type().type().base_type().class_type().type_name()!=null){
							if(ctx.local_variable_type().type().base_type().class_type().type_name().namespace_or_type_name()!=null){
								UMLParserToolCSharp.usesList.add(new MyRelationMaps(currentClass,ctx.local_variable_type().type().base_type().class_type().type_name().namespace_or_type_name().getChild(0).getText()));
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void exitInterface_definition(CSharp4Parser.Interface_definitionContext ctx) {
		UMLParserToolCSharp.classStructureString +="Interface "+currentInterface+"{\n";
		for(int i=0;i<myInterfaceMethods.size();i++){
			UMLParserToolCSharp.classStructureString +=myInterfaceMethods.get(i).accessType+myInterfaceMethods.get(i).method_name+"(";
			if(myInterfaceMethods.get(i).formalParameters!=null){
				Enumeration<String> e = myInterfaceMethods.get(i).formalParameters.keys();
				while (e.hasMoreElements()) {
					String keyFormalParam = (String) e.nextElement();
					UMLParserToolCSharp.classStructureString += myInterfaceMethods.get(i).formalParameters.get(keyFormalParam)+" : "+keyFormalParam;
				}
			}
			UMLParserToolCSharp.classStructureString +=")"+" : "+myInterfaceMethods.get(i).return_type+"\n";
		}
		UMLParserToolCSharp.classStructureString +="}\n";
	}

	@Override
	public void exitClass_definition(CSharp4Parser.Class_definitionContext ctx) {

		UMLParserToolCSharp.classStructureString +="class "+currentClass+"{\n";
		if(!myClassProperties.isEmpty()){
			for (int i=0;i<myClassProperties.size();i++){
				boolean getter=false,setter=false;
				String prop_name = myClassProperties.get(i).property_name;
				prop_name = prop_name.substring(0,1).toUpperCase() + prop_name.substring(1);
				if(!myClassMethods.isEmpty()){
					for(int j=0;j<myClassMethods.size();j++){
						String method_name = myClassMethods.get(j).method_name;
						if(method_name.equals("get"+prop_name))
							getter=true;
						else if (method_name.equals("set"+prop_name))
							setter=true;
					}
				}
				if(setter && getter)
					myClassProperties.get(i).accessType="+";
				UMLParserToolCSharp.classStructureString += myClassProperties.get(i).accessType+myClassProperties.get(i).property_name+" : "+myClassProperties.get(i).property_type+"\n";
			}				
		}

		if(!myClassConstructors.isEmpty()){
			for(int i=0;i<myClassConstructors.size();i++){
				UMLParserToolCSharp.classStructureString += myClassConstructors.get(i).accessType+myClassConstructors.get(i).method_name+"(";
				if(myClassConstructors.get(i).formalParameters!=null){
					Enumeration<String> en = myClassConstructors.get(i).formalParameters.keys();
					while (en.hasMoreElements()) {
						String keyFormalParam = (String) en.nextElement();
						UMLParserToolCSharp.classStructureString +=keyFormalParam + " : " + myClassConstructors.get(i).formalParameters.get(keyFormalParam);
					}
				}
				UMLParserToolCSharp.classStructureString +=")\n";
			}
		}
		if(!myClassMethods.isEmpty()){
			for (int i=0;i<myClassMethods.size();i++){
				boolean hide_method=false;
				String meth_name = myClassMethods.get(i).method_name;
				meth_name = meth_name.substring(3);
				meth_name = meth_name.substring(0,1).toLowerCase()+meth_name.substring(1);
				if(!myClassProperties.isEmpty()){
					for (int j=0;j<myClassProperties.size();j++){
						String prop_name = myClassProperties.get(j).property_name;
						if(prop_name.equals(meth_name))
							hide_method=true;
					}
				}
				if(!hide_method){
					UMLParserToolCSharp.classStructureString += myClassMethods.get(i).accessType+myClassMethods.get(i).method_name+"(";
					if(myClassMethods.get(i).formalParameters!=null){
						Enumeration<String> e = myClassMethods.get(i).formalParameters.keys();
						while (e.hasMoreElements()) {
							String keyFormalParam = (String) e.nextElement();
							UMLParserToolCSharp.classStructureString +=keyFormalParam + " : " + myClassMethods.get(i).formalParameters.get(keyFormalParam);
						}
					}
					UMLParserToolCSharp.classStructureString +=")"+" : "+myClassMethods.get(i).return_type+"\n";
				}
			}
		}
		UMLParserToolCSharp.classStructureString +="}\n";
	}

	private Boolean isPrimitiveType(String str){
		ArrayList<String> primitiveTypeList=new ArrayList<String>();
		primitiveTypeList.add("bool");
		primitiveTypeList.add("byte");
		primitiveTypeList.add("sbyte");
		primitiveTypeList.add("char");
		primitiveTypeList.add("decimal");
		primitiveTypeList.add("double");
		primitiveTypeList.add("enum");
		primitiveTypeList.add("float");
		primitiveTypeList.add("int");
		primitiveTypeList.add("long");
		primitiveTypeList.add("sbyte");
		primitiveTypeList.add("short");
		primitiveTypeList.add("struct");
		primitiveTypeList.add("uint");
		primitiveTypeList.add("ulong");
		primitiveTypeList.add("ushort");
		primitiveTypeList.add("object");
		primitiveTypeList.add("string");
		primitiveTypeList.add("bool[]");
		primitiveTypeList.add("byte[]");
		primitiveTypeList.add("sbyte[]");
		primitiveTypeList.add("char[]");
		primitiveTypeList.add("decimal[]");
		primitiveTypeList.add("double[]");
		primitiveTypeList.add("enum[]");
		primitiveTypeList.add("float[]");
		primitiveTypeList.add("int[]");
		primitiveTypeList.add("long[]");
		primitiveTypeList.add("sbyte[]");
		primitiveTypeList.add("short[]");
		primitiveTypeList.add("struct[]");
		primitiveTypeList.add("uint[]");
		primitiveTypeList.add("ulong[]");
		primitiveTypeList.add("ushort[]");
		primitiveTypeList.add("object[]");
		primitiveTypeList.add("string[]");

		for(String primitiveType : primitiveTypeList){
			if(str.contains(primitiveType)){
				return true;
			}
		}
		return false;
	}
}
