package lexer;


public enum TokenType {
	// mDFA를 구현하기 위해 사용할 TokenType들을 지정해주엇다.
	INT,
	ID, QUESTION,
	TRUE, FALSE, NOT,
	PLUS, MINUS, TIMES, DIV,
	LT, GT, EQ, APOSTROPHE,
	L_PAREN, R_PAREN,
	DEFINE, LAMBDA, COND, QUOTE,
	CAR, CDR, CONS,
	ATOM_Q, NULL_Q, EQ_Q, SHARP;
	
	static TokenType fromSpecialCharacter(char ch) {
		switch (ch) {
		// SPECIAL_CHAR들의 Type을 설정해주었다.
		case '(' : 
			return L_PAREN;
		case ')' : 
			return R_PAREN;
		case '+' : 
			return PLUS;
		case '-' : 
			return MINUS;
		case '*' : 
			return TIMES;
		case '/' : 
			return DIV;
		case '<' : 
			return LT;
		case '=' : 
			return EQ;
		case '>' : 
			return GT;
		case '\'' : 
			return APOSTROPHE;
		case '?' :
			return QUESTION;
		case '#' : 
			return SHARP;
		case 'T' :
			return TRUE;
		case 'F' :
			return FALSE;
	    default:
	    	throw new IllegalArgumentException("unregistered char: " + ch);
		}
	}
}
