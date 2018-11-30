package parser.ast;

import java.util.HashMap;
import java.util.Map;

import lexer.TokenType;
import parser.ast.BinaryOpNode.BinType;

public class FunctionNode implements ValueNode {
	//FunctionType으로 지정할 type들을 만든다.
	public enum FunctionType{
		ATOM_Q { TokenType tokenType() {return TokenType.ATOM_Q;} },
		CAR { TokenType tokenType() {return TokenType.CAR;} },
		CDR { TokenType tokenType() {return TokenType.CDR;} },
		COND { TokenType tokenType() {return TokenType.COND;} },
		CONS { TokenType tokenType() {return TokenType.CONS;} },
		DEFINE { TokenType tokenType() {return TokenType.DEFINE;} },
		EQ_Q { TokenType tokenType() {return TokenType.EQ_Q;} },
		LAMBDA { TokenType tokenType() {return TokenType.LAMBDA;} },
		NOT { TokenType tokenType() {return TokenType.NOT;} },
		NULL_Q { TokenType tokenType() {return TokenType.NULL_Q;} };
		
		private static Map<TokenType, FunctionType> fromTokenType = new HashMap<TokenType, FunctionType>();
		static {
			for(FunctionType funcType : FunctionType.values()) {
				fromTokenType.put(funcType.tokenType(), funcType);		//Map에 tokentype과 FunctionType을 저장한다.
			}
		}
		static FunctionType getFuncType(TokenType tType) {
			return fromTokenType.get(tType);
		}
		abstract TokenType tokenType();
	}
	public FunctionType value;
	
	public FunctionNode(TokenType tType) {
		// TODO Auto-generated constructor stub
		FunctionType fType = FunctionType.getFuncType(tType);
		value = fType;
	}
	
	public String toString() {
		return value.name();	//FunctionType의 name을 리턴한다.(ex. ATOM_Q, CAR 등등)
	}
	
	public void setValue(TokenType tType) {
		FunctionType fType = FunctionType.getFuncType(tType);
		value = fType;			//FunctionType의 type을 value에 저장한다.
	}
}
