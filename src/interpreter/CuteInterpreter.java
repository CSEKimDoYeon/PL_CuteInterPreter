package interpreter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import lexer.TokenType;
import parser.ast.*;
import parser.parse.CuteParser;
import parser.parse.NodePrinter;

public class CuteInterpreter {
	
	static Map<String, Object> listNode = new HashMap<String, Object>();
	
	private void errorLog(String err) {
		System.out.println(err);
	}

	public Node runExpr(Node rootExpr) {
		if (rootExpr == null)
			return null;

		if (rootExpr instanceof IdNode)
			return lookupTable(((IdNode) rootExpr).idString);

		else if (rootExpr instanceof IntNode)
			return rootExpr;

		else if (rootExpr instanceof BooleanNode)
			return rootExpr;

		else if (rootExpr instanceof ListNode)
			return runList((ListNode) rootExpr);

		else
			errorLog("run Expr error");

		return null;
	}
	
	private Node lookupTable(String list) {
		Node value_node = (Node) listNode.get(list);
		if( ((ListNode) value_node).car() instanceof QuoteNode){
			return value_node;
		}
		return ((ListNode) value_node).car();
	}

	private Node runList(ListNode list) {
		if (list.equals(ListNode.EMPTYLIST))
			return list;

		if (list.car() instanceof FunctionNode) {
			return runFunction((FunctionNode) list.car(), list.cdr());
		}

		if (list.car() instanceof BinaryOpNode) {
			return runBinary(list);
		}

		return list;
	}

	private Node insertTable(IdNode id, ListNode value){//insertTable구현
		//head에 define 나머지 tail
		/*IdNode ttt =(IdNode) id;
		String key = ttt.idString;*/
		if(listNode.containsKey(id.idString)){	//해당 id가 존재하면
			listNode.remove(id.idString);	//기존 id와 value 삭제
			//listNode.put(id.idString, value.car());
			listNode.put(id.idString, value);
			return value.car();
		}else{
			//listNode.put(id.idString, value.car());
			listNode.put(id.idString, value);
			//IntNode value_table = new IntNode(Integer.toString(((IntNode) runExpr(value)).value));
			
		return value.car();
		}
	}
	
	private Node runFunction(FunctionNode operator, ListNode operand) {
		switch (operator.value) {
		case DEFINE: //(ListNode) operand.cdr().car()).car()
			if(operand.cdr().car() instanceof ListNode){	//define뒤에 listnode가 binary일때
				return insertTable((IdNode) operand.car(), ListNode.cons(runExpr(operand.cdr().car()), ListNode.ENDLIST));
			}
			return insertTable((IdNode) operand.car(), operand.cdr());
		case CAR: // runQuote함수를 이용하여 반환된 것의 head를 반환한다.
			if(operand.car() instanceof QuoteNode){
				Node vvv = (((ListNode) runQuote((ListNode)operand)).car());//operand.car();
				if((((ListNode) runQuote((ListNode)operand)).car()) instanceof FunctionNode){
						return new IdNode(vvv.toString());
				}		
				return ((ListNode) runQuote((ListNode)operand)).car();
			}
			else{
				Object var = ((runExpr((IdNode)operand.car())));
				return ((ListNode) runQuote((ListNode) var)).car();
			}
		case CDR: // runQuote함수를 이용하여 반환된 것의 tail을 가져온다음 QuoteNode로 만들어서 반환한다.
			if(operand.car() instanceof QuoteNode) {
				Node aaa = (((ListNode)runQuote((ListNode)operand)).cdr());
				
			}
			return new QuoteNode(((ListNode) runQuote((ListNode)operand)).cdr());
		case CONS: // ListNode에 선언된 cons함수를 이용하고, QuoteNode로 만들어서 반환한다.
			if (operand.car() instanceof QuoteNode) { // 기존에 구현되어있는 cons함수 사용
				return new QuoteNode(ListNode.cons(((ListNode) runQuote((ListNode) operand)),
						(ListNode) runQuote((ListNode) operand.cdr())));
			}else if(operand.cdr().car() instanceof IdNode && lookupTable(((IdNode)operand.cdr().car()).idString) instanceof ListNode){
				return new QuoteNode(ListNode.cons(operand.car(), (ListNode) runQuote((ListNode) lookupTable(((IdNode)operand.cdr().car()).idString))));
			}
			return new QuoteNode(ListNode.cons(operand.car(), (ListNode) runQuote((ListNode) operand.cdr())));
		case COND:
			 if(runExpr(((ListNode)operand.car()).car()).equals(BooleanNode.TRUE_NODE)) {
		            if(((ListNode)operand.car()).cdr().car() instanceof IdNode) {
		               if((lookupTable(((IdNode)(((ListNode)operand.car()).cdr().car())).idString)) instanceof IntNode) {
		                  return lookupTable(((IdNode)(((ListNode)operand.car()).cdr().car())).idString); 
		               }
		               return ((ListNode) lookupTable(((IdNode)(((ListNode)operand.car()).cdr().car())).idString)).car();
		            }
		            return ((ListNode)operand.car()).cdr().car();
		            //return ((ListNode) runExpr(((ListNode)operand.car()).cdr().car())).car();
		            }
		            else {
		               if(((ListNode)(operand.cdr().car())).cdr().car() instanceof IdNode){
		                  return lookupTable(((IdNode)(((ListNode)operand.cdr().car()).cdr().car())).idString);
		               }
		               return ((ListNode)(operand.cdr().car())).cdr().car();
		            }
		case NOT: // ListNode일 때와 BooleanNode일때를 구분하여 실행한다.
					// ListNode일 때는 runBinary함수를 실행하여 관계연산을 진행한 후 논리연산을 실행한다.
			if (operand.car() instanceof ListNode) {
				if (runBinary((ListNode) operand.car()) == BooleanNode.TRUE_NODE)
					return BooleanNode.FALSE_NODE;
				else
					return BooleanNode.TRUE_NODE;
			}

			// BooleanNode일 때는 바로 논리연산을 실행한다.
			if (operand.car() == BooleanNode.TRUE_NODE)
				return BooleanNode.FALSE_NODE;
			else
				return BooleanNode.TRUE_NODE;
		case ATOM_Q:
			// runQuote함수를 실행하여 반환된 것이 ListNode일경우에는 false 아닐 경우에는 true를 반환한다.
			if ((runQuote(operand)) instanceof ListNode)
				return BooleanNode.FALSE_NODE;
			else
				return BooleanNode.TRUE_NODE;
		case NULL_Q:
			// runQuote함수를 실행하여 반환된 것의 head와 tail이 null일경우에 true 아닐 경우에는 false를 반환한다.
			if(operand.car() instanceof IdNode){
				if(((ListNode) runQuote((ListNode) lookupTable(((IdNode)operand.car()).idString))).car() == null){
					return BooleanNode.TRUE_NODE;
				}
				return BooleanNode.FALSE_NODE;
			}
			if ((((ListNode) runQuote(operand)).car()) == null) // 값이 null인지 확인
				return BooleanNode.TRUE_NODE;
			else
				return BooleanNode.FALSE_NODE;
		case EQ_Q:
			if(operand.car() instanceof IdNode && operand.cdr().car() instanceof IdNode){
				if(lookupTable(((IdNode) operand.car()).idString).toString().equals(lookupTable(((IdNode)operand.cdr().car()).idString).toString())){
					return BooleanNode.TRUE_NODE;
				}
				return BooleanNode.FALSE_NODE;
			}
			else if ((operand.car()).toString().equals((operand.cdr().car()).toString()))
				return BooleanNode.TRUE_NODE;
			else
				return BooleanNode.FALSE_NODE;
		default:
			break;
		}

		return null;
	}

	private Node runBinary(ListNode list) {
		BinaryOpNode operator = (BinaryOpNode) list.car();
		Node one = list.cdr().car(); // ListNode의 tail의 head를 가져온다
		Node next = list.cdr().cdr().car(); // ListNode의 tail의 tail의 head를 가져온다.

		if (one instanceof ListNode) { // 가져온 head가 ListNode일 경우에는 한번 더 실행한다.
			one = runBinary((ListNode) one);
		}

		if (next instanceof ListNode) { // 가져온 head가 ListNode일 경우에는 한번 더 실행한다.
			next = runBinary((ListNode) next);
		}

		switch (operator.value) {
		// +,-,/ 등에 대한 바이너리 연산 동작 구현
		case PLUS: // 가져온 노드들을 IntNode로 변환한 후에 연산을 진행한다.(IntNode의 Interger인 value를 사용하기위해 변환) 연산을
					// 진행한 후에 Interger값으로 계산된 것을 String으로
					// 변환하여 IntNode로 반환한다.
			return new IntNode(Integer.toString(((IntNode)runExpr(one)).value + ((IntNode)runExpr(next)).value));
		case MINUS:
			return new IntNode(Integer.toString(((IntNode)runExpr(one)).value - ((IntNode)runExpr(next)).value));
		case DIV:
			return new IntNode(Integer.toString(((IntNode)runExpr(one)).value / ((IntNode)runExpr(next)).value));
		case TIMES:
			return new IntNode(Integer.toString(((IntNode)runExpr(one)).value * ((IntNode)runExpr(next)).value));
		case LT: // 위의 연산들과 비슷하지만 true와 false형식으로 반환해야하기 때문에 BooleanNode를 사용하여 반환한다.
			if (((IntNode)runExpr(one)).value < ((IntNode)runExpr(next)).value)
				return BooleanNode.TRUE_NODE;
			else
				return BooleanNode.FALSE_NODE;
		case GT:
			if (((IntNode)runExpr(one)).value > ((IntNode)runExpr(next)).value)
				return BooleanNode.TRUE_NODE;
			else
				return BooleanNode.FALSE_NODE;
		case EQ:
			if(((IntNode)runExpr(one)).value.equals(((IntNode)runExpr(next)).value))
				return BooleanNode.TRUE_NODE;
			else
				return BooleanNode.FALSE_NODE;
		default:
			break;
		}

		return null;
	}

	private Node runQuote(ListNode node) {
		return ((QuoteNode) node.car()).nodeInside();
	}

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		String list;
		while (true) {
			System.out.print("> ");
			list = sc.nextLine();
			CuteParser cuteParser = new CuteParser(list);
			Node parseTree = cuteParser.parseExpr();
			CuteInterpreter i = new CuteInterpreter();
			Node resultNode = i.runExpr(parseTree);
			System.out.print("... ");
			NodePrinter.getPrinter(System.out).prettyPrint(resultNode);
			System.out.println("");
		}
	}

}
