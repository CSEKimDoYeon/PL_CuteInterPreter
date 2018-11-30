package parser.ast;

public class IdNode implements ValueNode {
	public String idString;

	public IdNode(String text) {
		idString = text;
	}
	
	public String toString() {
		return "ID: " + idString;
	}
}
