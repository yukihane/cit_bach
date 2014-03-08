package v1;

import java.util.ArrayList;
import java.util.List;

public class Parse {
	private TokenHandler t;
	private PList parameterList;
	Parse(TokenHandler t, PList parameterList) {
		this.t = t; 
		this.parameterList = parameterList;
	}
	
	public Node parseExpression() {
		String token = t.peepToken();
		try {
			if (token == null) {
				Error.printError("制約式に誤りがあります");
				return null;
			} else if (token.equals("("))
				return expressionWithParentheses();
			else {
				// error
				Error.printError("制約に'('がありません");
				return null;
			}
		} catch (OutOfTokenStreamException e) {
			Error.printError("制約式に誤りがあります");
			return null;
		}
	}
	
	private Node expressionWithParentheses() throws OutOfTokenStreamException {
		Node res; // 戻り値
		String token = t.getToken();
		if (token.equals("(") == false) {
			// error
			// this block is unreachable
			Error.printError("制約に'('がありません");
			return null;
		}
		// expression :: (expression)
		if (t.peepToken() == null) 
			throw new OutOfTokenStreamException();
		if (t.peepToken().equals("(")) 
			res = expressionWithParentheses();
		else  // otherwise
			res = expressionBody();
		// closed with ')' ?
		if (t.getToken().equals(")") == false) {
			// error
			Error.printError("制約に')'がありません");
			return null;
		}
		return res;
	}

	private Node expressionBody() throws OutOfTokenStreamException {
		// 演算子の次のトークンが ( か どうかで判断
		// case 1: ( <> (
		// case 2: ( <> [ foo,  ( <> foo 
		String token = t.peepNextToken();
		if (token == null) 
			throw new OutOfTokenStreamException();
		if (token.equals("("))
			return boolExpression();
		else 
			return atomExpression();
	}

	private Node boolExpression() throws OutOfTokenStreamException {
		// boolean expression with operator
		String token = t.peepToken();
		if (t.peepToken() == null) 
			throw new OutOfTokenStreamException();
		if (token.equals("not"))
			return notExpression();
		else if (token.equals("=="))
			return equalityExpression();
		else if (token.equals("<>"))
			return inequalityExpression();
		else if (token.equals("or"))
			return orExpression();
		else if (token.equals("and"))
			return andExpression();
		else if (token.equals("if"))
			return ifExpression();
		else if (token.equals("ite"))
			return iteExpression();
		else 
			Error.printError(token + " is not a valid operator");
		return null; // unreachable
	}

	private Node notExpression() throws OutOfTokenStreamException {
		BooleanUnaryOperator res = new NotOperator();
		t.getToken();
		res.Child = parseExpression();
		return res;
	}
	
	private Node equalityExpression() throws OutOfTokenStreamException {
		BooleanBinaryOperator res = new EqualityOperator();
		t.getToken();
		res.Left = parseExpression();
		res.Right = parseExpression();
		return res;
	}

	private Node inequalityExpression() throws OutOfTokenStreamException {
		BooleanBinaryOperator res = new InequalityOperator();
		t.getToken();
		res.Left = parseExpression();
		res.Right = parseExpression();
		return res;
	}
	
	private Node orExpression() throws OutOfTokenStreamException {
		BooleanMultinaryOperator res = new OrOperator();
		t.getToken();
		res.ChildList.add(parseExpression());
		res.ChildList.add(parseExpression());
		if (t.peepToken() == null) 
			throw new OutOfTokenStreamException();
		while (t.peepToken().equals(")") == false) {
			res.ChildList.add(parseExpression());
			if (t.peepToken() == null) 
				throw new OutOfTokenStreamException();
		}
		return res;
	}

	private Node andExpression() throws OutOfTokenStreamException {
		BooleanMultinaryOperator res = new AndOperator();
		t.getToken();
		res.ChildList.add(parseExpression());
		res.ChildList.add(parseExpression());
		if (t.peepToken() == null) 
			throw new OutOfTokenStreamException();
		while (t.peepToken().equals(")") == false) {
			res.ChildList.add(parseExpression());
			if (t.peepToken() == null) 
				throw new OutOfTokenStreamException();
		}
		return res;
	}
	
	private Node ifExpression() throws OutOfTokenStreamException {
		BooleanBinaryOperator res = new IfOperator();
		t.getToken();
		res.Left = parseExpression();
		res.Right = parseExpression();
		return res;
	}
	
	private Node iteExpression() throws OutOfTokenStreamException {
		BooleanTrinaryOperator res = new IfthenelseOperator();
		t.getToken();
		res.Left = parseExpression();
		res.Middle = parseExpression();
		res.Right = parseExpression();
		return res;
	}
	
	private	Node atomExpression() throws OutOfTokenStreamException {
		// 次のトークンをチェック: 演算子でないといけない
		String token = t.getToken();
		if (token.equals("==")) 
			return equalityAtomExpression();
		else if (token.equals("<>")) 
			return inequalityAtomExpression();
		else 
			Error.printError("制約式に == か <> が必要です");
		return null;
	}

	private Node inequalityAtomExpression() throws OutOfTokenStreamException {
		BooleanUnaryOperator res = new NotOperator();
		res.Child = equalityAtomExpression();			
		return res;
	}

	private Node equalityAtomExpression() throws OutOfTokenStreamException {
		// case 1 val1 val2
		// case 2 val1 [para1] 
		// case 3 [para1] val1
		// case 4 [para1] [para2]
		String val1, val2, para1, para2;
		String token1, token2;
		
		token1 = t.peepToken();
		token2 = t.peepNextToken();
		
		if (token1 == null || token2 == null) 
			throw new OutOfTokenStreamException();
		
		//case 1
		if ((token1.equals("[") == false) && (token2.equals("[") == false)) {
			val1 = t.getToken(); 
			val2 = t.getToken();
			return compareValueAndValue(val1, val2);
		}
		
		//case 2
		if ((token1.equals("[") == false) && (token2.equals("[") == true)) {
			val1 = t.getToken();
			t.getToken(); // must be [
			para1 = t.getToken();
			if (t.getToken().equals("]") == false) {
				Error.printError("制約式に]が必要です");
			}
			return compareParameterAndValue(para1, val1);
		}

		// case 3, 4
		t.getToken(); // must be "["
		para1 = t.getToken();
		if (t.getToken().equals("]") == false) {
			Error.printError("制約式に]が必要です");
		}
		token1 = t.peepToken();
		if (token1 == null) 
			throw new OutOfTokenStreamException();
		
		// case 3
		if (token1.equals("[") == false) {
			val1 = t.getToken();
			return compareParameterAndValue(para1, val1);
		}
		
		// case 4
		t.getToken(); // must be [
		para2 = t.getToken();
		if (t.getToken().equals("]") == false) {
			Error.printError("制約式に]が必要です");
		}
		return compareParameterAndParameter(para1, para2);
	}
	
	private Node compareValueAndValue(String val1, String val2) {
		if (val1.equals(val2)) 
			return new TrueValue();
		else 
			return new FalseValue();
	}
	
	private Node compareParameterAndValue(String para, String val) {
		int parameterID = 0;
		Parameter p;
		int value = 0;
		// 因子名が正しいかチェック
		try {
			parameterID = parameterList.getID(para);
		} catch (NoParameterNameException e) {
			Error.printError("制約中の因子名に誤りがあります");
		}
		p = parameterList.get(parameterID);
		
		// 値名が正しいかチェック
		try {
			value = p.getID(val);
		} catch (NoValueNameException e) {
			Error.printError("制約中の値名に誤りがあります");
		}	
		
		ComparisonOfParameterAndValue res = new EqualityOfParameterAndValue();
		res.p = parameterID;
		res.v = value;
		return res;
	}
	
	private Node compareParameterAndParameter(String para1, String para2) {
		int parameterID1 = 0;
		int parameterID2 = 0;
		Parameter p1, p2;
		// 因子名が正しいかチェック
		try {
			parameterID1 = parameterList.getID(para1);
			parameterID2 = parameterList.getID(para2);
		} catch (NoParameterNameException e) {
			Error.printError("制約中の因子名に誤りがあります");
		}
		p1 = parameterList.get(parameterID1);
		p2 = parameterList.get(parameterID2);
		
		List<String> commonValueName = new ArrayList<String>(p1.value_name);
		commonValueName.retainAll(p2.value_name);

		// case 1: 値名で同じものがない
		if (commonValueName.size() == 0) 
			return new FalseValue();
		
		// case 2: 値名で同じものがひとつ
		if (commonValueName.size() == 1) {
			String valueName = commonValueName.get(0);
			BooleanMultinaryOperator res = new AndOperator();
			ComparisonOfParameterAndValue sub1 = new EqualityOfParameterAndValue();
			ComparisonOfParameterAndValue sub2 = new EqualityOfParameterAndValue();
			try {
				sub1.p = parameterID1;
				sub1.v = p1.getID(valueName);	
				sub2.p = parameterID2;
				sub2.v = p2.getID(valueName);	
			} catch (NoValueNameException e) {
				Error.printError("内部エラー");
			}
			res.ChildList.add(sub1);
			res.ChildList.add(sub2);		
			return res;
		}
				
		// case 3: 値名で同じものが2個以上
		BooleanMultinaryOperator res = new OrOperator();
		for (String valueName: commonValueName) {
			BooleanMultinaryOperator child = new AndOperator();
			ComparisonOfParameterAndValue sub1 = new EqualityOfParameterAndValue();
			ComparisonOfParameterAndValue sub2 = new EqualityOfParameterAndValue();
			try {
				sub1.p = parameterID1;
				sub1.v = p1.getID(valueName);	
				sub2.p = parameterID2;
				sub2.v = p2.getID(valueName);	
			} catch (NoValueNameException e) {
				Error.printError("内部エラー");
			}
			child.ChildList.add(sub1);
			child.ChildList.add(sub2);
			res.ChildList.add(child);
		}
		return res;
	}
}
