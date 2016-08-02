import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.*;

import net.sourceforge.plantuml.SourceStringReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;


public class UMLParserTool {
	static InputStream inputStream;
	static String intermediateOutput="";
	static String relationsOutput="";
	static ArrayList<String> classList = new ArrayList<String>();
	static ArrayList<String> interfaceList = new ArrayList<String>();
	static HashMap<String,String> usesMap = new HashMap<String,String>();
	static ArrayList<MyRelationMaps> usesList = new ArrayList<MyRelationMaps>();
	static ArrayList<AssociationRelationStruct> associationList = new ArrayList<AssociationRelationStruct>();

	public static void main(String[] args) throws Exception {
		File folder = new File(args[0]);
		File[] fileList = folder.listFiles();

		UMLParserListener extractor;
		for(File file : fileList){
			if(file.isFile() && file.getName().contains(".java")){
				inputStream =new FileInputStream(file);
				ANTLRInputStream input = new ANTLRInputStream(inputStream);

				JavaLexer lexer = new JavaLexer(input);
				CommonTokenStream tokens = new CommonTokenStream(lexer);
				JavaParser parser = new JavaParser(tokens);
				ParseTree tree = parser.compilationUnit(); // parse

				ParseTreeWalker walker = new ParseTreeWalker(); // create standard walker
				extractor = new UMLParserListener(parser);
				walker.walk(extractor, tree); // initiate walk of tree with listener
			} 
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
							relationsOutput+=class1+"\"*\"--\"*\" "+class2+"\n";
						else if(hasManyRelation2)
							relationsOutput+=class1+" \"*\"-- "+class2+"\n";
						else if(hasManyRelation1)
							relationsOutput+=class1+" --\"*\" "+class2+"\n";
						else if(!hasManyRelation1 && !hasManyRelation2)
							relationsOutput+=class1+" -- "+class2+"\n";
						associationList.get(j).isParsed = true;
						associationList.get(i).isParsed = true;
					}
				}					
				if(!associationList.get(i).isParsed){	
					if(hasManyRelation1){
						relationsOutput+=class1+" --\"*\" "+class2+"\n";
						associationList.get(i).isParsed = true;
					}
					else {
						relationsOutput+=class1+" -- "+class2+"\n";
						associationList.get(i).isParsed = true;
					}
				}

				if(i == associationList.size()-1 && !associationList.get(i).isParsed){
					if(hasManyRelation1 && !associationList.get(i).isParsed)
						relationsOutput+=class1+" --\"*\" "+class2+"\n";
					else if (!associationList.get(i).isParsed)
						relationsOutput+=class1+" -- "+class2+"\n";
				}
			}
		}
		for(int i=0;i<usesList.size();i++){
			if(interfaceList.contains(usesList.get(i).class2)){
				if(!relationsOutput.contains(usesList.get(i).class1+"..>"+usesList.get(i).class2))
					relationsOutput+=usesList.get(i).class1+"..>"+usesList.get(i).class2+"\n";
			}
		}

		String inputToPlantUml ="@startuml\n"+ intermediateOutput +  relationsOutput +"\n@enduml";
		try	{		
			String outputFileName = args[1]+".jpg";
			OutputStream png = new FileOutputStream(outputFileName);

			SourceStringReader reader = new SourceStringReader(inputToPlantUml);
			String outputImage = reader.generateImage(png);
		}
		catch(Exception e)
		{

		}
	}
}