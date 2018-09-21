package v1;

import jdd.bdd.*;

import java.util.*;

abstract class Node {
	abstract int evaluate(BDD bdd, List<VariableAndBDD> parameters);
	abstract int evaluate(BDD bdd, List<VariableAndBDD> parameters, Set<Integer> restricted);
}

abstract class BooleanOperator extends Node {
}

abstract class BooleanUnaryOperator extends BooleanOperator {
	Node Child;
}

class NotOperator extends BooleanUnaryOperator {
	int evaluate(BDD bdd, List<VariableAndBDD> parameters) {
		int tmp = Child.evaluate(bdd, parameters);
		int res = bdd.not(tmp);
		bdd.ref(res);
		bdd.deref(tmp);
		return res;
	}

	int evaluate(BDD bdd, List<VariableAndBDD> parameters, Set<Integer> restricted) {
		int tmp = Child.evaluate(bdd, parameters, restricted);
		int res = bdd.not(tmp);
		bdd.ref(res);
		bdd.deref(tmp);
		return res;
	}
}

abstract class BooleanBinaryOperator extends BooleanOperator {
	Node Left, Right;
}

class IfOperator extends BooleanBinaryOperator {
	int evaluate(BDD bdd, List<VariableAndBDD> parameters) {
		int f1 = Left.evaluate(bdd, parameters);
		int f2 = Right.evaluate(bdd, parameters);
		int f = bdd.imp(f1, f2);
		bdd.ref(f);
		bdd.deref(f1);
		bdd.deref(f2);
		return f;
	}

	int evaluate(BDD bdd, List<VariableAndBDD> parameters, Set<Integer> restricted) {
		int f1 = Left.evaluate(bdd, parameters, restricted);
		int f2 = Right.evaluate(bdd, parameters, restricted);
		int f = bdd.imp(f1, f2);
		bdd.ref(f);
		bdd.deref(f1);
		bdd.deref(f2);
		return f;
	}
}

class EqualityOperator extends BooleanBinaryOperator {
	int evaluate(BDD bdd, List<VariableAndBDD> parameters) {
		int f1 = Left.evaluate(bdd, parameters);
		int f2 = Right.evaluate(bdd, parameters);
		int f = bdd.not(bdd.xor(f1, f2));
		bdd.ref(f);
		bdd.deref(f1);
		bdd.deref(f2);
		return f;
	}
	
	int evaluate(BDD bdd, List<VariableAndBDD> parameters, Set<Integer> restricted) {
		int f1 = Left.evaluate(bdd, parameters, restricted);
		int f2 = Right.evaluate(bdd, parameters, restricted);
		int f = bdd.not(bdd.xor(f1, f2));
		bdd.ref(f);
		bdd.deref(f1);
		bdd.deref(f2);
		return f;
	}
}

class InequalityOperator extends BooleanBinaryOperator {
	int evaluate(BDD bdd, List<VariableAndBDD> parameters) {
		int f1 = Left.evaluate(bdd, parameters);
		int f2 = Right.evaluate(bdd, parameters);
		int f = bdd.xor(f1, f2);
		bdd.ref(f);
		bdd.deref(f1);
		bdd.deref(f2);
		return f;
	}

	int evaluate(BDD bdd, List<VariableAndBDD> parameters, Set<Integer> restricted) {
		int f1 = Left.evaluate(bdd, parameters, restricted);
		int f2 = Right.evaluate(bdd, parameters, restricted);
		int f = bdd.xor(f1, f2);
		bdd.ref(f);
		bdd.deref(f1);
		bdd.deref(f2);
		return f;
	}
}

abstract class BooleanTrinaryOperator extends BooleanOperator {
	Node Left, Middle, Right;
}

class IfthenelseOperator extends BooleanTrinaryOperator {
	int evaluate(BDD bdd, List<VariableAndBDD> parameters) {
		int f1 = Left.evaluate(bdd, parameters);
		int f2 = Middle.evaluate(bdd, parameters);
		int f3 = Right.evaluate(bdd, parameters);
		int f = bdd.ite(f1, f2, f3);
		bdd.ref(f);
		bdd.deref(f1);
		bdd.deref(f2);
		bdd.deref(f3);
		return f;
	}

	int evaluate(BDD bdd, List<VariableAndBDD> parameters, Set<Integer> restricted) {
		int f1 = Left.evaluate(bdd, parameters, restricted);
		int f2 = Middle.evaluate(bdd, parameters, restricted);
		int f3 = Right.evaluate(bdd, parameters, restricted);
		int f = bdd.ite(f1, f2, f3);
		bdd.ref(f);
		bdd.deref(f1);
		bdd.deref(f2);
		bdd.deref(f3);
		return f;
	}
}

abstract class BooleanMultinaryOperator extends BooleanOperator {
	List<Node> ChildList = new ArrayList<Node>();
}

class OrOperator extends BooleanMultinaryOperator {

	int evaluate(BDD bdd, List<VariableAndBDD> parameters) {
		int f1 = ChildList.get(0).evaluate(bdd, parameters);
		int f2 = ChildList.get(1).evaluate(bdd, parameters);
		int f = bdd.or(f1, f2);
		bdd.ref(f);
		bdd.deref(f1);
		bdd.deref(f2);

		for (int i = 2; i < ChildList.size(); i++) {
			f1 = f;
			f2 = ChildList.get(i).evaluate(bdd, parameters);
			f = bdd.or(f1, f2);
			bdd.ref(f);
			bdd.deref(f1);
			bdd.deref(f2);
		}
		return f;
	}
	
	int evaluate(BDD bdd, List<VariableAndBDD> parameters, Set<Integer> restricted) {
		int f1 = ChildList.get(0).evaluate(bdd, parameters, restricted);
		int f2 = ChildList.get(1).evaluate(bdd, parameters, restricted);
		int f = bdd.or(f1, f2);
		bdd.ref(f);
		bdd.deref(f1);
		bdd.deref(f2);

		for (int i = 2; i < ChildList.size(); i++) {
			f1 = f;
			f2 = ChildList.get(i).evaluate(bdd, parameters, restricted);
			f = bdd.or(f1, f2);
			bdd.ref(f);
			bdd.deref(f1);
			bdd.deref(f2);
		}
		return f;
	}
}

class AndOperator extends BooleanMultinaryOperator {
	int evaluate(BDD bdd, List<VariableAndBDD> parameters) {
		int f1 = ChildList.get(0).evaluate(bdd, parameters);
		int f2 = ChildList.get(1).evaluate(bdd, parameters);
		int f = bdd.and(f1, f2);
		bdd.ref(f);
		bdd.deref(f1);
		bdd.deref(f2);

		for (int i = 2; i < ChildList.size(); i++) {
			f1 = f;
			f2 = ChildList.get(i).evaluate(bdd, parameters);
			f = bdd.and(f1, f2);
			bdd.ref(f);
			bdd.deref(f1);
			bdd.deref(f2);
		}
		return f;
	}
	
	int evaluate(BDD bdd, List<VariableAndBDD> parameters, Set<Integer> restricted) {
		int f1 = ChildList.get(0).evaluate(bdd, parameters, restricted);
		int f2 = ChildList.get(1).evaluate(bdd, parameters, restricted);
		int f = bdd.and(f1, f2);
		bdd.ref(f);
		bdd.deref(f1);
		bdd.deref(f2);

		for (int i = 2; i < ChildList.size(); i++) {
			f1 = f;
			f2 = ChildList.get(i).evaluate(bdd, parameters, restricted);
			f = bdd.and(f1, f2);
			bdd.ref(f);
			bdd.deref(f1);
			bdd.deref(f2);
		}
		return f;
	}
}

abstract class AtomicExpression extends Node {
}

abstract class Constant extends AtomicExpression {
}

class TrueValue extends Constant {
	int evaluate(BDD bdd, List<VariableAndBDD> parameters) {

		int f = bdd.getOne();
		bdd.ref(f);
		return f;
	}
	
	int evaluate(BDD bdd, List<VariableAndBDD> parameters, Set<Integer> restricted) {

		int f = bdd.getOne();
		bdd.ref(f);
		return f;
	}
}

class FalseValue extends Constant {
	int evaluate(BDD bdd, List<VariableAndBDD> parameters) {
		int f = bdd.getZero();
		bdd.ref(f);
		return f;
	}
	
	int evaluate(BDD bdd, List<VariableAndBDD> parameters, Set<Integer> restricted) {
		int f = bdd.getZero();
		bdd.ref(f);
		return f;
	}
}

abstract class ComparisonOfValueAndValue extends AtomicExpression {
	int v1;
	int v2;
}

/*
 * not used abstract class ComparisonOfParameterAndParameter extends
 * AtomicExpression { int p1; int p2; }
 */

abstract class ComparisonOfParameterAndValue extends AtomicExpression {
	int p;
	int v;
}

/*
 * class EqualityOfValueAndValue extends ComparisonOfValueAndValue { int
 * evaluate (BDD bdd, List<VariableAndBDD> parameters) { if (v1 == v2) return
 * bdd.getOne(); else return bdd.getZero(); } }
 */

class EqualityOfParameterAndValue extends ComparisonOfParameterAndValue {
	int evaluate(BDD bdd, List<VariableAndBDD> parameters) {
		int res = bdd.getOne();
		bdd.ref(res);
		// pは（絶対値で）パラメータの番号が既にはいっている
		// 
		int[] var = parameters.get(this.p).var;
		for (int i = var.length - 1; i >= 0; i--) {
			if ((this.v & (0x01 << i)) > 0) {
				int tmp = bdd.ref(bdd.and(res, var[i]));
				bdd.deref(res);
				res = tmp;
			} else {
				int tmp = bdd.ref(bdd.and(res, bdd.not(var[i])));
				bdd.deref(res);
				res = tmp;
			}
		}
		bdd.ref(res);
		return res;
	}
	
	
	int evaluate(BDD bdd, List<VariableAndBDD> parameters, Set<Integer> restricted) {
		int res = bdd.getOne();
		bdd.ref(res);
		// pは（絶対値で）パラメータの番号が既にはいっている
		int num = 0;
		for (Integer i: restricted) {
			if (i == this.p) 
				break;
			num++;
		}
		
		int[] var = parameters.get(num).var;
		for (int i = var.length - 1; i >= 0; i--) {
			if ((this.v & (0x01 << i)) > 0) {
				int tmp = bdd.ref(bdd.and(res, var[i]));
				bdd.deref(res);
				res = tmp;
			} else {
				int tmp = bdd.ref(bdd.and(res, bdd.not(var[i])));
				bdd.deref(res);
				res = tmp;
			}
		}
		bdd.ref(res);
		return res;
	}
}