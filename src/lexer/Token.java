package lexer;

import java.util.HashMap;
import java.util.Map;

public class Token {
	private final TokenType type;
	private final String lexme;
	
	static Token ofName(String lexme) {
		// token이 ID(알파벳문자열)일 때 type에 ID를 저장하고, lexme에 문자열(token)을 저장한다. '?'가 ID의 끝에 왔을 경우 처리를 해주기도 한다.
		TokenType type = KEYWORDS.get(lexme);
		if (type != null) {
			return new Token(type, lexme);
		}
		
		else if (lexme.endsWith("?")) {
			if (lexme.substring(0, lexme.length()-1).contains("?")) {
				throw new ScannerException("invalid ID=" + lexme);
			}
			
			return new Token(TokenType.QUESTION, lexme);
		}
		
		else if (lexme.contains("?")) {
			throw new ScannerException("invalid ID=" + lexme);
		}
		
		else {
			return new Token(TokenType.ID, lexme);
		}
	}
	
	Token(TokenType type, String lexme) {
		this.type = type;
		this.lexme = lexme;
	}
	
	public TokenType type() {
		return this.type;
	}
	
	public String lexme() {
		return this.lexme;
	}
	
	@Override
	public String toString() {
		return String.format("%s(%s)", type, lexme);
	}
	
	private static final Map<String,TokenType> KEYWORDS = new HashMap<>();
	static {
		// KEYWORDS들이 token으로 나왔을 때 ID로 처리하지 않고, KEYWORDS로 처리하기 위해 추가해주었다.
		KEYWORDS.put("define", TokenType.DEFINE);
		KEYWORDS.put("lambda", TokenType.LAMBDA);
		KEYWORDS.put("cond", TokenType.COND);
		KEYWORDS.put("quote", TokenType.QUOTE);
		KEYWORDS.put("not", TokenType.NOT);
		KEYWORDS.put("cdr", TokenType.CDR);
		KEYWORDS.put("car", TokenType.CAR);
		KEYWORDS.put("cons", TokenType.CONS);
		KEYWORDS.put("eq?", TokenType.EQ_Q);
		KEYWORDS.put("null?", TokenType.NULL_Q);
		KEYWORDS.put("atom?", TokenType.ATOM_Q); 
	}
}
