package lexer;

class Char {
	private final char value;
	private final CharacterType type;

	enum CharacterType {
		LETTER, DIGIT, SPECIAL_CHAR, WS, END_OF_STREAM, SHARP;
	} // 기존에 주어진 State 외에 SHARP State를 추가해 주었다.(#T와 #F를 처리해주기 위함)
	
	static Char of(char ch) {
		return new Char(ch, getType(ch));
	}
	
	static Char end() {
		return new Char(Character.MIN_VALUE, CharacterType.END_OF_STREAM);
	}
	
	private Char(char ch, CharacterType type) {
		this.value = ch;
		this.type = type;
	}
	
	char value() {
		return this.value;
	}
	
	CharacterType type() {
		return this.type;
	}
	
	private static CharacterType getType(char ch) {
		int code = (int)ch;
		if ( (code >= (int)'A' && code <= (int)'Z')
			|| (code >= (int)'a' && code <= (int)'z')) {
			return CharacterType.LETTER;
		}
		
		if ( Character.isDigit(ch) ) {
			return CharacterType.DIGIT;
		}
		
		switch ( ch ) {
		//특수문자가 나왔을 경우의 Type을 SPECIAL_CHAR로 해준다.(단, '#'이 나왔을 경우는 Type을 SHARP로 한다.)
			case '-': 
				return CharacterType.SPECIAL_CHAR;
			case '(': 
				return CharacterType.SPECIAL_CHAR;
			case ')': 
				return CharacterType.SPECIAL_CHAR;
			case '+': 
				return CharacterType.SPECIAL_CHAR;
			case '*': 
				return CharacterType.SPECIAL_CHAR;
			case '/': 
				return CharacterType.SPECIAL_CHAR;
			case '<': 
				return CharacterType.SPECIAL_CHAR;
			case '=': 
				return CharacterType.SPECIAL_CHAR;
			case '>': 
				return CharacterType.SPECIAL_CHAR;
			case '\'': 
				return CharacterType.SPECIAL_CHAR;
			case '?':
				return CharacterType.SPECIAL_CHAR;
			case '#': 
				return CharacterType.SHARP;
		}
		
		if ( Character.isWhitespace(ch) ) {
			return CharacterType.WS;
		}
		
		throw new IllegalArgumentException("input=" + ch);
	}
}
