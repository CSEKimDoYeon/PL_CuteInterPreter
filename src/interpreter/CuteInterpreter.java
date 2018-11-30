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

	private Node insertTable(IdNode id, ListNode value){//insertTable����
		//head�� define ������ tail
		/*IdNode ttt =(IdNode) id;
		String key = ttt.idString;*/
		if(listNode.containsKey(id.idString)){	//�ش� id�� �����ϸ�
			listNode.remove(id.idString);	//���� id�� value ����
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
			if(operand.cdr().car() instanceof ListNode){	//define�ڿ� listnode�� binary�϶�
				return insertTable((IdNode) operand.car(), ListNode.cons(runExpr(operand.cdr().car()), ListNode.ENDLIST));
			}
			return insertTable((IdNode) operand.car(), operand.cdr());
		case CAR: // runQuote�Լ��� �̿��Ͽ� ��ȯ�� ���� head�� ��ȯ�Ѵ�.
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
		case CDR: // runQuote�Լ��� �̿��Ͽ� ��ȯ�� ���� tail�� �����´��� QuoteNode�� ���� ��ȯ�Ѵ�.
			if(operand.car() instanceof QuoteNode) {
				Node aaa = (((ListNode)runQuote((ListNode)operand)).cdr());
				
			}
			return new QuoteNode(((ListNode) runQuote((ListNode)operand)).cdr());
		case CONS: // ListNode�� ����� cons�Լ��� �̿��ϰ�, QuoteNode�� ���� ��ȯ�Ѵ�.
			if (operand.car() instanceof QuoteNode) { // ������ �����Ǿ��ִ� cons�Լ� ���
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
		case NOT: // ListNode�� ���� BooleanNode�϶��� �����Ͽ� �����Ѵ�.
					// ListNode�� ���� runBinary�Լ��� �����Ͽ� ���迬���� ������ �� �������� �����Ѵ�.
			if (operand.car() instanceof ListNode) {
				if (runBinary((ListNode) operand.car()) == BooleanNode.TRUE_NODE)
					return BooleanNode.FALSE_NODE;
				else
					return BooleanNode.TRUE_NODE;
			}

			// BooleanNode�� ���� �ٷ� �������� �����Ѵ�.
			if (operand.car() == BooleanNode.TRUE_NODE)
				return BooleanNode.FALSE_NODE;
			else
				return BooleanNode.TRUE_NODE;
		case ATOM_Q:
			// runQuote�Լ��� �����Ͽ� ��ȯ�� ���� ListNode�ϰ�쿡�� false �ƴ� ��쿡�� true�� ��ȯ�Ѵ�.
			if ((runQuote(operand)) instanceof ListNode)
				return BooleanNode.FALSE_NODE;
			else
				return BooleanNode.TRUE_NODE;
		case NULL_Q:
			// runQuote�Լ��� �����Ͽ� ��ȯ�� ���� head�� tail�� null�ϰ�쿡 true �ƴ� ��쿡�� false�� ��ȯ�Ѵ�.
			if(operand.car() instanceof IdNode){
				if(((ListNode) runQuote((ListNode) lookupTable(((IdNode)operand.car()).idString))).car() == null){
					return BooleanNode.TRUE_NODE;
				}
				return BooleanNode.FALSE_NODE;
			}
			if ((((ListNode) runQuote(operand)).car()) == null) // ���� null���� Ȯ��
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
		Node one = list.cdr().car(); // ListNode�� tail�� head�� �����´�
		Node next = list.cdr().cdr().car(); // ListNode�� tail�� tail�� head�� �����´�.

		if (one instanceof ListNode) { // ������ head�� ListNode�� ��쿡�� �ѹ� �� �����Ѵ�.
			one = runBinary((ListNode) one);
		}

		if (next instanceof ListNode) { // ������ head�� ListNode�� ��쿡�� �ѹ� �� �����Ѵ�.
			next = runBinary((ListNode) next);
		}

		switch (operator.value) {
		// +,-,/ � ���� ���̳ʸ� ���� ���� ����
		case PLUS: // ������ ������ IntNode�� ��ȯ�� �Ŀ� ������ �����Ѵ�.(IntNode�� Interger�� value�� ����ϱ����� ��ȯ) ������
					// ������ �Ŀ� Interger������ ���� ���� String����
					// ��ȯ�Ͽ� IntNode�� ��ȯ�Ѵ�.
			return new IntNode(Integer.toString(((IntNode)runExpr(one)).value + ((IntNode)runExpr(next)).value));
		case MINUS:
			return new IntNode(Integer.toString(((IntNode)runExpr(one)).value - ((IntNode)runExpr(next)).value));
		case DIV:
			return new IntNode(Integer.toString(((IntNode)runExpr(one)).value / ((IntNode)runExpr(next)).value));
		case TIMES:
			return new IntNode(Integer.toString(((IntNode)runExpr(one)).value * ((IntNode)runExpr(next)).value));
		case LT: // ���� ������ ��������� true�� false�������� ��ȯ�ؾ��ϱ� ������ BooleanNode�� ����Ͽ� ��ȯ�Ѵ�.
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
