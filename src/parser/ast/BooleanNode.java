package parser.ast;

public class BooleanNode implements ValueNode {
	Boolean value;
	
	public String toString() {
		return value ? "True: #T" : "False: #F";
	}
	
	public static BooleanNode FALSE_NODE = new BooleanNode(false);
	public static BooleanNode TRUE_NODE = new BooleanNode(true);
	
	public BooleanNode(Boolean b) {
		value = b;
	}
}
