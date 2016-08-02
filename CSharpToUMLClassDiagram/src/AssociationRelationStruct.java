
public class AssociationRelationStruct {
	String class1;
	String class2;
	boolean hasManyRelation;
	boolean isParsed =false;
	
	AssociationRelationStruct(String c1,String c2, boolean rel){
		this.class1=c1;
		this.class2=c2;
		this.hasManyRelation=rel;
		
	}
}
