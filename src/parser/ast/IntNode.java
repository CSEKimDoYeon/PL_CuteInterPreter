package parser.ast;

public class IntNode implements ValueNode {
	public Integer value;
	
	public String toString() {
		return "INT: " + value;
	}
	
	public IntNode(String text) {
		this.value = new Integer(text);
	}
}
