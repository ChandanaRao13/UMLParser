import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import net.sourceforge.plantuml.SourceStringReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class UMLParserToolCSharp {

	static InputStream inputStream;
	static String classInterfaceOutput = "";
	static String classStructureString="";
	static String relationsString="";
	static ArrayList<String> classList = new ArrayList<String>();
	static ArrayList<String> interfaceList = new ArrayList<String>();
	static ArrayList<MyRelationMaps> usesList = new ArrayList<MyRelationMaps>();
	static ArrayList<AssociationRelationStruct> associationList = new ArrayList<AssociationRelationStruct>();
	static HashMap<String, ArrayList<String>> extendsImpMap = new HashMap<String, ArrayList<String>>();


	public static void main(String[] args) throws Exception {

		File folder = new File(args[0]);
		File[] listOfFiles = folder.listFiles();
	
		for(File file : listOfFiles){
			if(file.isFile() && file.getName().contains(".cs")){
			
				inputStream = new FileInputStream(file);
				ANTLRInputStream input = new ANTLRInputStream(inputStream);

				CSharp4Lexer lexer = new CSharp4Lexer(input);
				CommonTokenStream tokens = new CommonTokenStream(lexer);
				CSharp4Parser parser = new CSharp4Parser(tokens);
				ParseTree tree = parser.compilation_unit(); 

				ParseTreeWalker walker = new ParseTreeWalker();
				UMLParserListener extractor = new UMLParserListener(parser);
				walker.walk(extractor, tree);
			}
		}

		//Defines Extends and implements relationship between Csharp files
		Iterator it = extendsImpMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			String currentClass = (String) entry.getKey();
			ArrayList<String> extendsImplementsList = (ArrayList<String>) entry.getValue();
			for(int i = 0;i< extendsImplementsList.size();i++){
				if(!currentClass.equals(extendsImplementsList.get(i))){
					if(classList.contains(extendsImplementsList.get(i))){
						if(!relationsString.contains(currentClass + "--|>"+extendsImplementsList.get(i)))
							relationsString+=currentClass + "--|>"+extendsImplementsList.get(i)+"\n";
					}else if(interfaceList.contains(extendsImplementsList.get(i))){
						if(!relationsString.contains(currentClass + "..|>"+extendsImplementsList.get(i)))
							relationsString+=currentClass + "..|>"+extendsImplementsList.get(i)+"\n";
					}
				}
			}
			it.remove(); // avoids a ConcurrentModificationException
		}
		
		for(int i = 0; i < associationList.size() ; i++){
			String class1 = associationList.get(i).class1;
			String class2 = associationList.get(i).class2;
			if(!associationList.get(i).isParsed){
				for(int j=i+1; j < associationList.size(); j++){
					if(class1.equals(associationList.get(j).class1) && class2.equals(associationList.get(j).class2)){
						associationList.get(i).hasManyRelation = true;
						associationList.remove(j);
					}
				}					
		
			}
		}
		
		
		for(int i = 0; i < associationList.size() ; i++){
			String class1 = associationList.get(i).class1;
			String class2 = associationList.get(i).class2;
			boolean hasManyRelation1 = associationList.get(i).hasManyRelation;
			if(!associationList.get(i).isParsed){
				for(int j=i+1; j < associationList.size(); j++){
					if(class2.equals(associationList.get(j).class1) && class1.equals(associationList.get(j).class2)){
						boolean hasManyRelation2 = associationList.get(j).hasManyRelation;
						if(hasManyRelation1 && hasManyRelation2)
							relationsString+=class1+" \"*\"--\"*\" "+class2+"\n";
						else if(hasManyRelation2)
							relationsString+=class1+" \"*\"-- "+class2+"\n";
						else if(hasManyRelation1)
							relationsString+=class1+" --\"*\" "+class2+"\n";
						else if(!hasManyRelation1 && !hasManyRelation2)
							relationsString+=class1+" -- "+class2+"\n";
						associationList.get(j).isParsed = true;
						associationList.get(i).isParsed = true;
					}
				}					
				if(!associationList.get(i).isParsed){	
					if(hasManyRelation1){
						relationsString+=class1+" --\"*\" "+class2+"\n";
						associationList.get(i).isParsed = true;
					}
					else {
						relationsString+=class1+" -- "+class2+"\n";
						associationList.get(i).isParsed = true;
					}
				}
				if(i == associationList.size()-1 && !associationList.get(i).isParsed){
					if(hasManyRelation1 && !associationList.get(i).isParsed)
						relationsString+=class1+" --\"*\" "+class2+"\n";
					else if (!associationList.get(i).isParsed)
						relationsString+=class1+" -- "+class2+"\n";
				}
			}
		}
		for(int i=0;i<usesList.size();i++){
			if(interfaceList.contains(usesList.get(i).class2)){
				if(!relationsString.contains(usesList.get(i).class1+"..>"+usesList.get(i).class2))
					relationsString+=usesList.get(i).class1+"..>"+usesList.get(i).class2+"\n";
			}
		}
		String inputToPlantUml = "@startuml\n" + classStructureString + relationsString + "@enduml\n";
		
		
		try
		{		
			OutputStream png = new FileOutputStream(args[args.length-1]+".jpg");

			SourceStringReader reader = new SourceStringReader(inputToPlantUml);
			reader.generateImage(png);
		}
		catch(Exception e)
		{
			System.out.println(e.getStackTrace());
		}
	}
}