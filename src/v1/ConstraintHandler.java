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

		// parameter�̃��X�g
		parameters = setBDDforParameter(parameterList);

		// contrainList����A�m�[�h���Ă�
		bddConstraint = setBddConstraint(constraintList);

		// boolean �ϐ��̑������v�Z
		// numOfBooleanVariable = computeNumOfBooleanVariables
	}

	
	// With constrainedParameters BDD is reduced by excluding irrelevant parameters
	ConstraintHandler(PList parameterList, List<Node> constraintList, TreeSet<Integer> constrainedParameters) {
		bdd = new BDD(sizeOfNodetable, sizeOfCache);
//		bdd = new jdd.bdd.debug.DebugBDD(1000,1000);

		this.constrainedParameters = constrainedParameters;

		// parameter�̃��X�g
		parameters = setBDDforParameter(parameterList);

		// contrainList����A�m�[�h���Ă�
		bddConstraint = setBddConstraint(constraintList);

		// boolean �ϐ��̑������v�Z
		// numOfBooleanVariable = computeNumOfBooleanVariables
	}
	
	void printConstraintBDD() {
		bdd.printSet(bddConstraint);
	}

	// �e�p�����[�^��boolean�ϐ������蓖�āD�������v�Z
	private List<VariableAndBDD> setBDDforParameter(PList parameterList) {
		List<VariableAndBDD> res = new ArrayList<VariableAndBDD>();
		this.numOfBDDvariables = 0;

		for (Parameter p : parameterList) {
			// BDD�ϐ��̐ݒ�
			int num_vars = 1;
			for (int levels = 2;; levels *= 2) {
				if (p.value_name.size() < levels)
					break;
				num_vars++;
			}
			// BDD�ϐ��̑������v�Z
			numOfBDDvariables += num_vars;

			// boolean variables
			// �������ꂽ���� getVar(v): 0, 1, 2, ..
			int[] var = new int[num_vars];
			for (int i = num_vars - 1; i >= 0; i--) {
				var[i] = bdd.createVar();
			}

			// �����BDD�̐ݒ�
			// constraint for invalid values
			// domain-1�ȉ��̐����̂ݗL��
			// bool variables �̐���domain-1������킹�邾���͂���
			//
			// domain-1��2�i�\���ł́C�ŏ�ʂ̕ϐ��ɂ�����r�b�g�͏��1
			// �Ƃ͌���Ȃ�
			int f = bdd.getZero();
			bdd.ref(f);
			// domain-1��菬������
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

			// domain - 1���g
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

			// var, d �� list�ɒǉ�
			res.add(new VariableAndBDD(var, d));
		}
		return res;
	}

	private int setBddConstraint(List<Node> constraintList) {
		int f = bdd.getOne();
		bdd.ref(f);

		// �p�����[�^�ł���Ȃ��̈��false�ɂ���
		for (VariableAndBDD vb : parameters) {
			int tmp = bdd.ref(bdd.and(f, vb.constraint));
			bdd.deref(f);
			f = tmp;
		}

		// ���񎮂̘_���ς��Ƃ�
		for (Node n : constraintList) {
			int g = n.evaluate(bdd, parameters);
			int tmp = bdd.ref(bdd.and(f, g));
			bdd.deref(f);
			bdd.deref(g);
			f = tmp;
		}

		// *��t��
		f = extendBddConstraint(f);

		return f;
	}

	
	private int setBddConstraintNewBuggy(List<Node> constraintList) {
		int f = bdd.getOne();
		bdd.ref(f);

		// ���񎮂̘_���ς��Ƃ�
		for (Node n : constraintList) {
			int g = n.evaluate(bdd, parameters);
			int tmp = bdd.ref(bdd.and(f, g));
			bdd.deref(f);
			bdd.deref(g);
			f = tmp;
		}

		// �p�����[�^�ł���Ȃ��̈��false�ɂ���
		int fordebug = 0;
		for (VariableAndBDD vb : parameters) {
			int cube = vb.var[0];
			bdd.ref(cube);
			for (int i = 1; i < vb.var.length; i++) {
				int tmp = bdd.ref(bdd.and(cube, vb.var[i]));
				bdd.deref(cube);
				cube = tmp;
			}
			int tmp = bdd.ref(bdd.exists(f, cube));
			// ����Ɋ֌W����ꍇ�̂�
			if (tmp != f) {
				fordebug++;
				int tmp1 = bdd.ref(bdd.and(f, vb.constraint));
				bdd.deref(f);
				f = tmp1;
			}
			bdd.deref(cube);
			bdd.deref(tmp);
		}
		System.out.println(fordebug);
		// *��t��
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

	// �e�X�g�P�[�X������𖞂�����
	boolean isPossible(Testcase test) {
		int node = bddConstraint;
		boolean[] bv = binarize(test);

		while (true) {
			// �P�^�C�P�U
			if (node == 0)
				return false;
			else if (node == 1)
				return true;

			// ����pos��0, 1�̓m�[�h�Ȃ�
			if (bv[bdd.getVar(node)] == true)
				node = bdd.getHigh(node);
			else
				node = bdd.getLow(node);
		}
	}

	private boolean[] binarize(Testcase test) {
		// assert(testcase�̒��� = parameter�̐�)
		boolean[] res = new boolean[numOfBDDvariables];
		int pos = 0;
		// �e���q�̒l��2�l�ŕ\��
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
		/*
		 * this.print(); for (int k = 0; k < res.length; k++)
		 * System.out.print(res[k] ? 1 : 0); System.out.println("<-");
		 */
		return res;
	}
}

class VariableAndBDD {
	int[] var; // bdd nodes
	// TODO ���O constraint -> �����悢���̂�
	int constraint; // bdd for invalid values

	VariableAndBDD(int[] var, int constraint) {
		this.var = var;
		this.constraint = constraint;
	}
}