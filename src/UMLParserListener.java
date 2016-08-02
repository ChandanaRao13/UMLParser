
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

public class UMLParserListener extends JavaBaseListener {
	//To store the current class name
	String currentClass,currentInterface;
	JavaParser parser;

	//Class 
	ArrayList<MyProperty> myClassProperties = new ArrayList<MyProperty>();
	ArrayList<MyMethod> myClassMethods = new ArrayList<MyMethod>();
	ArrayList<MyConstructor> myClassConstructors = new ArrayList<MyConstructor>();

	//Interface
	ArrayList<MyProperty> myInterfaceProperties = new ArrayList<MyProperty>();
	ArrayList<MyMethod> myInterfaceMethods = new ArrayList<MyMethod>();

	public UMLParserListener(JavaParser parser){
		this.parser = parser;
	}

	@Override
	public void enterClassDeclaration(JavaParser.ClassDeclarationContext ctx){	
		currentClass = ctx.getChild(1).getText();
		UMLParserTool.classList.add(currentClass);
		int index=2;
		while(index<ctx.getChildCount()){
			if(ctx.getChild(index).getText().contains("implements")){
				int countImpl = ctx.getChild(index+1).getChildCount();
				if(countImpl == 1)
					UMLParserTool.relationsOutput = UMLParserTool.relationsOutput +currentClass + "..|>" + ctx.getChild(index+1).getText() + "\n";
				else{
					for(int i=0;i<countImpl;i=i+2)
						UMLParserTool.relationsOutput = UMLParserTool.relationsOutput +currentClass + "..|>" + ctx.getChild(index+1).getChild(i).getText() + "\n";
				}
			}
			if(ctx.getChild(index).getText().contains("extends")){
				int countExtends=ctx.getChild(index+1).getChildCount();
				if(countExtends == 1)
					UMLParserTool.relationsOutput = UMLParserTool.relationsOutput +currentClass + "--|>" + ctx.getChild(index+1).getText() + "\n";
				else{
					for(int i=0;i<countExtends;i=i+2)
						UMLParserTool.relationsOutput = UMLParserTool.relationsOutput +currentClass + "--|>" + ctx.getChild(index+1).getChild(i).getText() + "\n";
				}
			}
			index=index+1;
		}
	}
	
	@Override
	public void enterInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx){	
		currentInterface = ctx.getChild(1).getText();
		UMLParserTool.interfaceList.add(currentInterface);
	}

	@Override
	public void enterFieldDeclaration(JavaParser.FieldDeclarationContext ctx) {
		String symbol="";
		switch(ctx.getParent().getParent().getChild(0).getText()){
		case "private" :
			symbol = "-";
			break;
		case "public" :
			symbol = "+";
			break;
		default: break;
		}
		if(!symbol.isEmpty()){
			if(ctx.type()!=null){
				if(ctx.type().primitiveType() !=null){
					if(ctx.type().primitiveType().getText() != null){
						myClassProperties.add(new MyProperty(symbol,ctx.getChild(1).getText(),ctx.getChild(0).getText()));
					}
				}else if(ctx.type().classOrInterfaceType() != null){
					if(ctx.type().classOrInterfaceType().getText() !=null){
						if(!ctx.getChild(0).getText().contains("<")){
							if(isPrimitiveType(ctx.getChild(0).getText())){
								myClassProperties.add(new MyProperty(symbol,ctx.getChild(1).getText(),ctx.getChild(0).getText()));
							}else{
								UMLParserTool.associationList.add(new AssociationRelationStruct(currentClass,ctx.getChild(0).getText(),false));
							}
						}else if(ctx.getChild(0).getText().contains("<")){
							UMLParserTool.associationList.add(new AssociationRelationStruct(currentClass,ctx.getChild(0).getChild(0).getChild(1).getChild(1).getText(),true));
						}
						else{
							UMLParserTool.associationList.add(new AssociationRelationStruct(currentClass,ctx.getChild(0).getText(),false));
						}
					}
				}
			}
		}
		else if(ctx.type().classOrInterfaceType() != null){
			if(ctx.type().classOrInterfaceType().getText() !=null){
				if(!isPrimitiveType(ctx.getChild(0).getText())){
					if(ctx.getChild(0).getText().contains("<")){
						UMLParserTool.associationList.add(new AssociationRelationStruct(currentClass,ctx.getChild(0).getText(),true));
					}else
						UMLParserTool.associationList.add(new AssociationRelationStruct(currentClass,ctx.getChild(0).getText(),false));
				}
			}
		}
	}



	@Override
	public void enterConstructorDeclaration(JavaParser.ConstructorDeclarationContext ctx) {
		myClassConstructors = new ArrayList<MyConstructor>();
		String containsStatic = "";

		switch(ctx.getParent().getParent().getChild(0).getText()){
		case "public":
			Hashtable<String,String> formalParameters = new Hashtable<String,String>();
			if(ctx.getChild(1).getChildCount()>2){
				formalParameters.put(ctx.formalParameters().formalParameterList().getChild(0).getChild(1).getText(),ctx.formalParameters().formalParameterList().getChild(0).getChild(0).getText());
			}
			myClassConstructors.add(new MyConstructor(containsStatic+"+",currentClass,formalParameters));

			if(ctx.formalParameters().getChildCount()>2){
				UMLParserTool.usesList.add(new MyRelationMaps(currentClass,ctx.formalParameters().formalParameterList().getChild(0).getChild(0).getText()));

			}
			break;
		default:break;
		}
	}


	@Override
	public void enterMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
		switch(ctx.getParent().getParent().getChild(0).getText()){
		case "public":
			String containsStatic = "";

			Hashtable<String,String> formalParameters = new Hashtable<String,String>();
			if(ctx.getChild(2).getChildCount()>2){
				formalParameters.put(ctx.formalParameters().formalParameterList().getChild(0).getChild(1).getText(),ctx.formalParameters().formalParameterList().getChild(0).getChild(0).getText());
			}
			myClassMethods.add(new MyMethod(containsStatic+"+",ctx.getChild(0).getText(),ctx.getChild(1).getText(),formalParameters));
			break;
		default:
			break;
		}
		if(ctx.formalParameters().getChildCount()>2){
			UMLParserTool.usesList.add(new MyRelationMaps(currentClass,ctx.formalParameters().formalParameterList().getChild(0).getChild(0).getText()));

		}

	}

	@Override
	public void enterInterfaceMethodDeclaration(JavaParser.InterfaceMethodDeclarationContext ctx) {
		String accessModifier = "";
		String containsStatic = "";

		switch(ctx.getParent().getParent().getChild(0).getText()){
		case "private" :
			accessModifier = containsStatic+"-";
			break;
		case "public" :
			accessModifier = containsStatic+"+";
			break;
		default: break;
		}
		Hashtable<String,String> formalParameters = new Hashtable<String,String>();
		if(ctx.getChild(2).getChildCount()>2){
			formalParameters.put(ctx.formalParameters().formalParameterList().getChild(0).getChild(1).getText(),ctx.formalParameters().formalParameterList().getChild(0).getChild(0).getText());
		}
		myInterfaceMethods.add(new MyMethod(accessModifier,ctx.getChild(0).getText(),ctx.getChild(1).getText(),formalParameters));

	}

	@Override
	public void enterLocalVariableDeclaration(JavaParser.LocalVariableDeclarationContext ctx) {
		super.enterLocalVariableDeclaration(ctx);
		if(ctx.type().classOrInterfaceType() != null){
			if(ctx.type().classOrInterfaceType().getText() !=null && !ctx.getChild(0).getText().contains("<") && !isPrimitiveType(ctx.getChild(0).getText())){
				UMLParserTool.usesList.add(new MyRelationMaps(currentClass,ctx.type().classOrInterfaceType().getText()));
			}
		}
	}


	@Override
	public void exitInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx){
		UMLParserTool.intermediateOutput +="Interface "+currentInterface+"{\n";
		for(int i=0;i<myInterfaceMethods.size();i++){
			UMLParserTool.intermediateOutput +=myInterfaceMethods.get(i).accessType+myInterfaceMethods.get(i).method_name+"(";
			if(myInterfaceMethods.get(i).formalParameters!=null){
				Enumeration<String> e = myInterfaceMethods.get(i).formalParameters.keys();
				while (e.hasMoreElements()) {
					String keyFormalParam = (String) e.nextElement();
					UMLParserTool.intermediateOutput +=keyFormalParam + " : " + myInterfaceMethods.get(i).formalParameters.get(keyFormalParam);
				}
			}
			UMLParserTool.intermediateOutput +=")"+":"+myInterfaceMethods.get(i).return_type+"\n";
		}
		UMLParserTool.intermediateOutput +="}\n";
	}
	@Override
	public void exitClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
		UMLParserTool.intermediateOutput +="class "+currentClass+"{\n";
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
				UMLParserTool.intermediateOutput += myClassProperties.get(i).accessType+myClassProperties.get(i).property_name+" : "+myClassProperties.get(i).property_type+"\n";
			}				
		}

		if(!myClassConstructors.isEmpty()){
			for(int i=0;i<myClassConstructors.size();i++){
				UMLParserTool.intermediateOutput += myClassConstructors.get(i).accessType+myClassConstructors.get(i).method_name+"(";
				if(myClassConstructors.get(i).formalParameters!=null){
					Enumeration<String> en = myClassConstructors.get(i).formalParameters.keys();
					while (en.hasMoreElements()) {
						String keyFormalParam = (String) en.nextElement();
						UMLParserTool.intermediateOutput +=keyFormalParam + " : " + myClassConstructors.get(i).formalParameters.get(keyFormalParam);
					}
				}
				UMLParserTool.intermediateOutput +=")\n";
			}
		}
		if(!myClassMethods.isEmpty()){
			for (int i=0;i<myClassMethods.size();i++){
				boolean hide_method=false;
				String meth_name = myClassMethods.get(i).method_name;
				if(meth_name.length()>3){
					meth_name = meth_name.substring(3);
					meth_name = meth_name.substring(0,1).toLowerCase()+meth_name.substring(1);
					if(!myClassProperties.isEmpty()){
						for (int j=0;j<myClassProperties.size();j++){
							String prop_name = myClassProperties.get(j).property_name;
							if(prop_name.equals(meth_name))
								hide_method=true;
						}
					}
				}
				if(!hide_method){
					UMLParserTool.intermediateOutput += myClassMethods.get(i).accessType+myClassMethods.get(i).method_name+"(";
					if(myClassMethods.get(i).formalParameters!=null){
						Enumeration<String> e = myClassMethods.get(i).formalParameters.keys();
						while (e.hasMoreElements()) {
							String keyFormalParam = (String) e.nextElement();
							UMLParserTool.intermediateOutput +=keyFormalParam + " : " + myClassMethods.get(i).formalParameters.get(keyFormalParam);
						}
					}
					UMLParserTool.intermediateOutput +=")"+" : "+myClassMethods.get(i).return_type+"\n";
				}
			}
		}
		UMLParserTool.intermediateOutput +="}\n";
	}


	private Boolean isPrimitiveType(String str){
		ArrayList<String> primitiveTypeList=new ArrayList<String>();
		primitiveTypeList.add("Byte");
		primitiveTypeList.add("Short");
		primitiveTypeList.add("Integer");
		primitiveTypeList.add("Long");
		primitiveTypeList.add("Float");
		primitiveTypeList.add("Double");
		primitiveTypeList.add("Character");
		primitiveTypeList.add("Boolean");
		primitiveTypeList.add("Byte[]");
		primitiveTypeList.add("Short[]");
		primitiveTypeList.add("Integer[]");
		primitiveTypeList.add("Long[]");
		primitiveTypeList.add("Float[]");
		primitiveTypeList.add("Double[]");
		primitiveTypeList.add("Character[]");
		primitiveTypeList.add("Boolean[]");
		primitiveTypeList.add("String[]");
		primitiveTypeList.add("String");
		for(String primitiveType : primitiveTypeList){
			if(str.contains(primitiveType)){
				return true;
			}
		}
		return false;
	}

}

