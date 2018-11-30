package parser.parse;

import java.util.Iterator;

import lexer.Scanner;
import lexer.Token;
import lexer.TokenType;
import parser.ast.BinaryOpNode;
import parser.ast.BooleanNode;
import parser.ast.FunctionNode;
import parser.ast.IdNode;
import parser.ast.IntNode;
import parser.ast.ListNode;
import parser.ast.Node;
import parser.ast.QuoteNode;

import java.io.File;
import java.io.FileNotFoundException;

public class CuteParser {
	private Iterator<Token> tokens;
	private static Node END_OF_LIST = new Node() {};
	
	public CuteParser(String list) {
		try {
			tokens = Scanner.scan(list);
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private Token getNextToken() {
		if(!tokens.hasNext())
			return null;
		return tokens.next();
	}
	public Node parseExpr() {
		Token t = getNextToken();
		if(t == null) {
			System.out.println("No more token");
			return null;
		}
		TokenType tType = t.type();
		String tLexeme = t.lexme();
		switch(tType) {
		//TokenType이 ID일때 수행한다.
		case ID:
			return new IdNode(tLexeme);
		//TokenType이 INT일때 수행한다.
		case INT:
			if(tLexeme == null)
				System.out.println("???");
			return new IntNode(tLexeme);
		//TokenType이 BinaryOp일때 수행한다.
		case DIV:
		case EQ:
		case MINUS:
		case GT:
		case PLUS:
		case TIMES:
		case LT:
			BinaryOpNode binaryNode = new BinaryOpNode();
			binaryNode.setValue(tType);
			return binaryNode;
		
		//TokenType이 FunctionType일 때 수행한다.
		case ATOM_Q:
		case CAR:
		case CDR:
		case COND:
		case CONS:
		case DEFINE:
		case EQ_Q:
		case LAMBDA:
		case NOT:
		case NULL_Q:
			return new FunctionNode(tType);
			
		case FALSE:
			return BooleanNode.FALSE_NODE;
			
		case TRUE:
			return BooleanNode.TRUE_NODE;
			
		case L_PAREN:
			return parseExprList();
			
		case R_PAREN:
			return END_OF_LIST;
			
		case APOSTROPHE:
			return new QuoteNode(parseExpr());
			
		case QUOTE:
			return new QuoteNode(parseExpr());
			
			default:
				System.out.println("Parsing Error!");
				return null;
		}
	}
	
	public ListNode parseExprList() {
		Node head = parseExpr();
		
		if(head == null)
			return null;
		
		if(head == END_OF_LIST)
			return ListNode.ENDLIST;
		
		ListNode tail = parseExprList();
		
		if(tail == null)
			return null;
		
		return ListNode.cons(head,  tail);
	}
}
