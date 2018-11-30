package lexer;

import static lexer.TokenType.ID;
import static lexer.TokenType.INT;
import static lexer.TransitionOutput.GOTO_ACCEPT_ID;
import static lexer.TransitionOutput.GOTO_ACCEPT_INT;
import static lexer.TransitionOutput.GOTO_EOS;
import static lexer.TransitionOutput.GOTO_FAILED;
import static lexer.TransitionOutput.GOTO_MATCHED;
import static lexer.TransitionOutput.GOTO_SHARP;
import static lexer.TransitionOutput.GOTO_SIGN;
import static lexer.TransitionOutput.GOTO_START;


enum State {
	// State의 처음 상태 token의 type을 판별하여 각 state로 goto한다.
	START {                          
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			char v = ch.value();
			switch ( ch.type() ) {
				case LETTER:                    // type이 LETTER(ID)이면  ACCEPT_ID state로 GOTO한다.
					context.append(v);             
					return GOTO_ACCEPT_ID;
				case DIGIT:                     // type이 DIGIT(INT)이면  ACCEPT_INT state로 GOTO한다.
					context.append(v);
					return GOTO_ACCEPT_INT;
				case SPECIAL_CHAR:              // type이 SPECIAL_CHAR이면 SIGN state로 GOTO한다.
					context.append(v);
					return GOTO_SIGN;
				case WS:                        // type이 ws(공백)이면 START state로 GOTO한다.
					return GOTO_START;
				case END_OF_STREAM:             // type이 END_OF_STREAM(context의 끝)이면 EOS state로 GOTO한다. 
					return GOTO_EOS;
				case SHARP:                     // type이 SHARP(#)이면 SHARP state로 GOTO한다.(#T와 #F처리를 위함)
					context.append(v);
					return GOTO_SHARP;
				default:
					throw new AssertionError();
			}
		}
	},
	ACCEPT_ID {
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			char v = ch.value();
			switch ( ch.type() ) {
				case LETTER:                     // type이 LETTER(ID)이면(문자다음에 문자가오면)  ACCEPT_ID state로 GOTO한다.
					context.append(v);
					return GOTO_ACCEPT_ID;
				case DIGIT:                      // type이 DIGIT(INT)이면(문자다음에 숫자가오면) ACCEPT_ID state로 GOTO한다. mDFA에서 ID에서 DIGIT이 들어오면 ID로 간주한다. 
					context.append(v);
					return GOTO_ACCEPT_ID;
				case SPECIAL_CHAR:               // type이 SPECIAL_CHAR이면(문자다음에 특수문자가옴면)  FAILED state로 GOTO한다. mDFA에서 ID에서 ID,INT,KEYWORDS가 아니면 모두 FAILED로 GOTO한다. 
					if (v == '?') {              // 단, '?'라는  문자가 들어왔을 경우는 ACCEPT_ID로 보낸다.(Token클래스의 ofName메소드를 이용하여 KEYWORDS로 처리하기 위함)
						context.append(v);
						return GOTO_ACCEPT_ID;
					}
					return GOTO_FAILED;
				case WS:
				case END_OF_STREAM:              // type이 END_OF_STREAM(context의 끝)이면 MATCHED상태로 GOTO한다.(type에 ID(state)를 저장하고 append한 context을 리턴한다.)
					return GOTO_MATCHED(Token.ofName(context.getLexime()));
				default:
					throw new AssertionError();
			}
		}
	},
	ACCEPT_INT {
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			switch ( ch.type() ) {
				case LETTER:                     // type이 LETTER(ID)이면(숫자다음에 문자가오면) FAILED state로 GOTO한다.
					return GOTO_FAILED;
				case DIGIT:                      // type이 DIGIT(INT)이면(숫자다음에 숫자가오면) ACCEPT_INT state로 GOTO한다.
					context.append(ch.value());
					return GOTO_ACCEPT_INT;
				case SPECIAL_CHAR:               // type이 SPECIAL_CHAR(ID)이면(숫자다음에 특수문자가오면) FAILED state로 GOTO한다.   
					return GOTO_FAILED;
				case WS:
				case END_OF_STREAM:              // type이 END_OF_STREAM(context의 끝)이면 MATCHED상태로 GOTO한다.(type에 INT(state)를 저장하고 append한 context를 가져온다.)
					return GOTO_MATCHED(INT, context.getLexime());
				default:
					throw new AssertionError();
			}
		}
	},
	SIGN {
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			String x = context.getLexime();
			char v = ch.value();  
			switch ( ch.type() ) {
				case LETTER:                     // type이 LETTER(ID)이면(특수문자다음에 문자가오면) FAILED state로 GOTO한다.
					return GOTO_FAILED;
				case DIGIT:                      // type이 DIGIT(INT)이면(특수문자다음에 숫자가오면) ACCEPT_INT state로 GOTO한다.
					if (x.charAt(0) == '+' || x.charAt(0) == '-') {
					context.append(x.charAt(0)); // type이 DIGIT(INT)이고, 그 전 value '+'이거나 '-'이면 GOTO하기 전에 '+'또는 '-'를 append한다.
					context.append(v);
					}
					else {                       // 특수문자다음에 숫자가 오지만 그 특수문자가 '+' 또는 '-'가 아닐경우 FAILED state로 GOTO한다.
						return GOTO_FAILED;
					}
					return GOTO_ACCEPT_INT;
				case WS:
				case END_OF_STREAM:
				case SPECIAL_CHAR:               // type이 SPECIAL_CHAR이면 MATCHED상태로 GOTO한다.(TokenType클래스의 fromSpecialCharacter메소드를 이용하여 KEYWORDS를 가져온다.)
					return GOTO_MATCHED(TokenType.fromSpecialCharacter(x.charAt(0)), x);
				default:
					throw new AssertionError();
			}
		}
	},
	MATCHED {
		@Override
		public TransitionOutput transit(ScanContext context) {
			throw new IllegalStateException("at final state");
		}
	},
	FAILED{
		@Override
		public TransitionOutput transit(ScanContext context) {
			throw new IllegalStateException("at final state");
		}
	},
	EOS {
		@Override
		public TransitionOutput transit(ScanContext context) {
			return GOTO_EOS;
		}
	},
	// '#'이라는 특수문자가 들어왔을 때 다음 token이 'T'나 'F'가 왔을 때 처리를 해주기 위해 새로운 State를 만들었다.
	SHAHRP {
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			char v = ch.value();
			if (v == 'T') {                        //'T'가 나왔을 경우 TokenType을 TRUE로 하고, append된 context를 가져온다.
				context.append(v);
				return GOTO_MATCHED(TokenType.TRUE, context.getLexime());
			} 
			else if (v == 'F') {                   //'F'가 나왔을 경우 TokenType을 FALSE로 하고, append된 context를 가져온다.
				context.append(v);
				return GOTO_MATCHED(TokenType.FALSE, context.getLexime());
			}
			return GOTO_FAILED;
		}
	};
	
	abstract TransitionOutput transit(ScanContext context);
}
