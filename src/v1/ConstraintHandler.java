package v1;

import jdd.bdd.*;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

class ConstraintHandler {
	static final int sizeOfNodetable = 10000;
	static final int sizeOfCache = 10000;

	List<VariableAndBDD> parameters = null;
	BDD bdd;
	int bddConstraint;
	int numOfBDDvariables;
	TreeSet<Integer> constrainedParameters; 
	
	// older one 
	ConstraintHandler(PList parameterList, List<Node> constraintList) {
		bdd = new BDD(sizeOfNodetable, sizeOfCache);
//		bdd = new jdd.bdd.debug.DebugBDD(1000,1000);

		// parameterのリスト
		parameters = setBDDforParameter(parameterList);

		// contrainListから、ノードを呼ぶ
		bddConstraint = setBddConstraint(constraintList);

		// boolean 変数の総数を計算
		// numOfBooleanVariable = computeNumOfBooleanVariables
	}

	
	// With constrainedParameters BDD is reduced by excluding irrelevant parameters
	ConstraintHandler(PList parameterList, List<Node> constraintList, TreeSet<Integer> constrainedParameters) {
		bdd = new BDD(sizeOfNodetable, sizeOfCache);
//		bdd = new jdd.bdd.debug.DebugBDD(1000,1000);

		this.constrainedParameters = constrainedParameters;

		// parameterのリスト
		PList constrainedParameterList = new PList();
		for (Integer factor: constrainedParameters) {
			constrainedParameterList.add(parameterList.get(factor));
		}
		parameters = setBDDforParameter(constrainedParameterList);

		// contrainListから、ノードを呼ぶ
		bddConstraint = setBddConstraint(constraintList);

		// boolean 変数の総数を計算
		// numOfBooleanVariable = computeNumOfBooleanVariables
	}
	
	void printConstraintBDD() {
		bdd.printSet(bddConstraint);
	}

	// 各パラメータにboolean変数を割り当て．総数も計算
	private List<VariableAndBDD> setBDDforParameter(PList parameterList) {
		List<VariableAndBDD> res = new ArrayList<VariableAndBDD>();
		this.numOfBDDvariables = 0;

		for (Parameter p : parameterList) {
			// BDD変数の設定
			int num_vars = 1;
			for (int levels = 2;; levels *= 2) {
				if (p.value_name.size() < levels)
					break;
				num_vars++;
			}
			// BDD変数の総数を計算
			numOfBDDvariables += num_vars;

			// boolean variables
			// 生成された順に getVar(v): 0, 1, 2, ..
			int[] var = new int[num_vars];
			for (int i = num_vars - 1; i >= 0; i--) {
				var[i] = bdd.createVar();
			}

			// 制約のBDDの設定
			// constraint for invalid values
			// domain-1以下の数字のみ有効
			// bool variables の数はdomain-1をあらわせるだけはある
			//
			// domain-1の2進表現では，最上位の変数にあたるビットは常に1
			// とは限らない
			int f = bdd.getZero();
			bdd.ref(f);
			// domain-1より小さい数
			for (int i = var.length - 1; i >= 0; i--) {
				if ((p.value_name.size() - 1 & (0x01 << i)) > 0) {
					int g = bdd.getOne();
					bdd.ref(g);
					for (int j = var.length - 1; j > i; j--) {
						if ((p.value_name.size() - 1 & (0x01 << j)) > 0) {
							int tmp = bdd.ref(bdd.and(g, var[j]));
							bdd.deref(g);
							g = tmp;
						} else {
							int tmp = bdd.ref(bdd.and(g, bdd.not(var[j])));
							bdd.deref(g);
							g = tmp;
						}
					}
					int tmp = bdd.ref(bdd.and(g, bdd.not(var[i])));
					bdd.deref(g);
					g = tmp;
					tmp = bdd.ref(bdd.or(f, g));
					bdd.deref(g);
					f = tmp;
				}
			}

			// domain - 1自身
			int g = bdd.getOne();
			bdd.ref(g);
			for (int i = var.length - 1; i >= 0; i--) {
				if ((p.value_name.size() - 1 & (0x01 << i)) > 0) {
					int tmp = bdd.ref(bdd.and(g, var[i]));
					bdd.deref(g);
					g = tmp;
				} else {
					int tmp = bdd.ref(bdd.and(g, bdd.not(var[i])));
					bdd.deref(g);
					g = tmp;
				}
			}

			int d = bdd.or(f, g);
			bdd.ref(d);
			bdd.deref(f);
			bdd.deref(g);

			// var, d を listに追加
			res.add(new VariableAndBDD(var, d));
		}
		return res;
	}

	private int setBddConstraint(List<Node> constraintList) {
		int f = bdd.getOne();
		bdd.ref(f);

		// パラメータでつかわない値をとった場合にfalseとなるようにする
		for (VariableAndBDD vb : parameters) {
			int tmp = bdd.ref(bdd.and(f, vb.constraint));
			bdd.deref(f);
			f = tmp;
		}

		// 制約式の論理積をとる
		for (Node n : constraintList) {		
			int g = n.evaluate(bdd, parameters, constrainedParameters);
			int tmp = bdd.ref(bdd.and(f, g));
			bdd.deref(f);
			bdd.deref(g);
			f = tmp;
		}

		// *を付加
		f = extendBddConstraint(f);

		return f;
	}

		
	private int extendBddConstraint(int constraint) {
		int f = constraint;
		for (VariableAndBDD p : parameters) {
			int cube = p.var[0];
			bdd.ref(cube);
			for (int i = 1; i < p.var.length; i++) {
				int tmp = bdd.ref(bdd.and(cube, p.var[i]));
				bdd.deref(cube);
				cube = tmp;
			}
			int tmp0 = bdd.ref(bdd.exists(f, cube));
			int tmp = bdd.ref(bdd.and(tmp0, cube));
			int newf = bdd.ref(bdd.or(f, tmp));

			bdd.deref(cube);
			bdd.deref(tmp0);
			bdd.deref(tmp);
			bdd.deref(f);
			f = newf;
		}
		return f;
	}

	// テストケースが制約を満たすか
	boolean isPossibleOld(Testcase test) {
		int node = bddConstraint;
		boolean[] bv = binarize(test);

		while (true) {
			// 恒真，恒偽
			if (node == 0)
				return false;
			else if (node == 1)
				return true;

			// このposの0, 1はノードなし
			if (bv[bdd.getVar(node)] == true)
				node = bdd.getHigh(node);
			else
				node = bdd.getLow(node);
		}
	}

	boolean isPossible(Testcase test) {
		int node = bddConstraint;
		boolean[] bv = binarizeReduced(test);

		while (true) {
			// 恒真，恒偽
			if (node == 0)
				return false;
			else if (node == 1)
				return true;

			// このposの0, 1はノードなし
			if (bv[bdd.getVar(node)] == true)
				node = bdd.getHigh(node);
			else
				node = bdd.getLow(node);
		}
	}
	
	private boolean[] binarize(Testcase test) {
		// assert(testcaseの長さ = parameterの数)
		boolean[] res = new boolean[numOfBDDvariables];
		int pos = 0;
		// 各因子の値を2値で表現
		for (int i = 0; i < test.value.length; i++) {
			VariableAndBDD p = parameters.get(i);
			int lv = test.get(i);
			if (lv < 0) { // wild card
				for (int j = 0; j < p.var.length; j++)
					res[pos + j] = true;
			} else {
				int j0 = 0;
				for (int j = p.var.length - 1; j >= 0; j--) {
					if ((lv & (0x01 << j)) > 0)
						res[pos + j0] = true;
					else
						res[pos + j0] = false;
					j0++;
				}
			}
			pos += p.var.length;
		}
		
		/* for debug
		 for (int k = 0; k < res.length; k++)
		 System.err.print(res[k] ? 1 : 0); System.err.println("<-");
		 */
		
		return res;
	}
	
	// TreeSet<Integer> constrainedParameters にあるparameterだけを2値化
	private boolean[] binarizeReduced(Testcase test) {
		boolean[] res = new boolean[numOfBDDvariables];
		int pos = 0;
		int i = 0;
		for (Integer factor: constrainedParameters) {
			// VariableAndBDD p = parameters.get(i); <- 
			// parameters が relevantなものだけになれば，上記に変更
			VariableAndBDD p = parameters.get(i);

			int lv = test.get(factor);
			if (lv < 0) {
				for (int j = 0; j < p.var.length; j++) 
					res[pos + j] = true;
			} else {
				int j0 = 0;
				for (int j = p.var.length -1; j >=0; j--) {
					if ((lv & (0x01 << j)) > 0) 
						res[pos + j0] = true;
					else
						res[pos + j0] = false;
					j0++;
				}
			}
			pos += p.var.length;
			i++;
		}
		
		
		/* for debug
		 test.print(); for (int k = 0; k < res.length; k++)
		 System.err.print(res[k] ? 1 : 0); System.err.println("<*");
		 */
		 
		return res;
	}
}

class VariableAndBDD {
	int[] var; // bdd nodes
	// TODO 名前 constraint -> 何かよいものに
	int constraint; // bdd for invalid values

	VariableAndBDD(int[] var, int constraint) {
		this.var = var;
		this.constraint = constraint;
	}
}