package v1;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class GeneratorFactor {

	// requires parametermodel.size >= 2
	static Generator newGenerator(ParameterModel parametermodel,
			GList groupList, ConstraintHandler constrainthandler,
			List<Testcase> seed, long randomseed, int strength) {
		if (strength > parametermodel.size)
			strength = parametermodel.size;

		if (strength == 2) {
			return new Generator2(parametermodel, groupList, constrainthandler,
					seed, randomseed);
		}
		if (strength == 3) {
			return new Generator3(parametermodel, groupList, constrainthandler,
					seed, randomseed);
		}
		if (strength == 4) {
			return new Generator4(parametermodel, groupList, constrainthandler,
					seed, randomseed);
		}
		if (strength == 5) {
			return new Generator5(parametermodel, groupList, constrainthandler,
					seed, randomseed);
		}

		return null;
	}
}

abstract class Generator {
	final ParameterModel parametermodel;
	final GList groupList;
	final ConstraintHandler constrainthandler;
	final List<Testcase> seed;
	final int numOfParameters;
	final Random rnd;

	static final int MaxNumOfTestcases = 65532;

	Generator(ParameterModel parametermodel, GList groupList,
			ConstraintHandler constrainthandler, List<Testcase> seed,
			long randomseed) {
		this.parametermodel = parametermodel;
		this.groupList = groupList;
		this.constrainthandler = constrainthandler;
		this.seed = seed;
		this.numOfParameters = parametermodel.size;
		this.rnd = new Random(randomseed);
	}

	abstract List<Testcase> generate() throws OutOfMaxNumOfTestcasesException;

	// group�őS�ԗ�����tuple�̗�𐶐�
	protected List<List<Testcase>> generateTupleSequenceList() {
		// protected List<TupleSequence> generateTupleSequence() {
		// TODO Auto-generated method stub
		List<List<Testcase>> listOfTupleSequence = new ArrayList<List<Testcase>>();
		for (Group g : groupList) {
			listOfTupleSequence.add(generateTupleSequence(g));
		}
		return listOfTupleSequence;
	}

	private List<Testcase> generateTupleSequence(Group g) {
		int numOfCombinations = 1;
		for (int i = 0; i < g.member.length; i++) {
			numOfCombinations *= parametermodel.range[g.member[i]];
		}

		List<Testcase> testSet = new ArrayList<Testcase>();

		Testcase tmptest = new Testcase(parametermodel.size);
		tmptest.quantify();
		for (int i = 0; i < g.member.length; i++) {
			tmptest.set(g.member[i], (byte) 0);
		}
		if (constrainthandler.isPossible(tmptest))
			testSet.add(tmptest);

		for (int i = 1; i < numOfCombinations; i++) {
			tmptest = tmptest.makeClone();
			for (int j = 0; j < g.member.length; j++) {
				int p = g.member[j];
				if (tmptest.get(p) + 1 >= parametermodel.range[p]) // ��������
					tmptest.set(p, (byte) 0);
				else {
					tmptest.set(p, (byte) (tmptest.get(p) + 1));
					break;
				}
			}
			if (constrainthandler.isPossible(tmptest)) {
				testSet.add(tmptest);
				if (testSet.size() > MaxNumOfTestcases)
					Error.printError(Main.language == Main.Language.JP ? "������q�̑S�ԗ��ɏ��"
							+ MaxNumOfTestcases + "�𒴂���e�X�g�P�[�X���K�v�ł�"
							: "The number of test cases exceeds the upper bound "
									+ MaxNumOfTestcases);
			}
		}
		return testSet;
	}

	protected boolean hasTuplesToCover(List<List<Testcase>> tupleSequenceList) {
		// TODO Auto-generated method stub
		for (List<Testcase> tupleSequence : tupleSequenceList) {
			if (tupleSequence.size() > 0)
				return true;
		}
		return false;
	}

}

class Generator2 extends Generator {
	/*
	 * final ParameterModel parametermodel; final GList groupList; final
	 * ConstraintHandler constrainthandler; final List<Testcase> seed; final int
	 * numOfParameters; final Random rnd;
	 * 
	 * //TODO group�̒ǉ� Generator2 (ParameterModel parametermodel, GList
	 * groupList, ConstraintHandler constrainthandler, List<Testcase> seed, long
	 * randomseed){ this.parametermodel = parametermodel; this.groupList =
	 * groupList; this.constrainthandler = constrainthandler; this.seed = seed;
	 * this.numOfParameters = parametermodel.size; this.rnd = new
	 * Random(randomseed); }
	 */

	// for regression testing  it should be reduced to 1
	final int NumOfIterationForEachTest = 20;
	// final int NumOfIterationForEachTest = 1;

	Generator2(ParameterModel parametermodel, GList groupList,
			ConstraintHandler constrainthandler, List<Testcase> seed,
			long randomseed) {
		super(parametermodel, groupList, constrainthandler, seed, randomseed);
	}

	List<Testcase> generate() throws OutOfMaxNumOfTestcasesException {

		List<Testcase> res = new ArrayList<Testcase>();
		PairTable tab = new PairTable(parametermodel);

		// group���Ctuple��̍쐬
		List<List<Testcase>> tupleSequenceList = generateTupleSequenceList();

		//
		int numOfUncoveredTuples = checkAllTuples(tab);

		// �e<���q�E�l> �� ���ꂪ�܂܂�関�J�o�[��tuple�̑�����ݒ�
		ArrayList<Integer>[] uncovTab = new ArrayList[parametermodel.size];
		initializeUncovTab(uncovTab, tab);

		// debug
		/*
		 * for (int p1 = 0; p1 < parametermodel.size; p1++) {
		 * System.err.print(p1 + ": "); for (int i : uncovTab[p1]) {
		 * System.err.print(i + ", "); } System.err.println(); }
		 */

		int seedrownum = 0;
		while (numOfUncoveredTuples > 0 || hasTuplesToCover(tupleSequenceList)) {
			// testcase 1����
			ResultOfGenerateOneTest newresult = generateOneTest(tab,
					seedrownum, uncovTab, tupleSequenceList);

			// debug
			/*
			 * newresult.test.print();
			 */

			res.add(newresult.test);
			if (res.size() > MaxNumOfTestcases)
				throw new OutOfMaxNumOfTestcasesException();

			numOfUncoveredTuples -= newresult.numOfCoveredTuples;
			seedrownum = newresult.nextSeedRow;

			// debug
			/*
			 * System.err.println(numOfUncoveredTuples + ", " + seedrownum);
			 */

			// debug
			/*
			 * for (int p1 = 0; p1 < parametermodel.size; p1++) {
			 * System.err.print(p1 + ": "); for (int i : uncovTab[p1]) {
			 * System.err.print(i + ", "); } System.err.println(); }
			 */
		}
		return res;
	}

	/*
	 * private boolean hasTuplesToCover(List<List<Testcase>> tupleSequenceList)
	 * { // TODO Auto-generated method stub for (List<Testcase> tupleSequence :
	 * tupleSequenceList) { if (tupleSequence.size() > 0) return true; } return
	 * false; }
	 */

	private void initializeUncovTab(ArrayList<Integer>[] uncovTab, PairTable tab) {
		assert (parametermodel.size == uncovTab.length);
		// uncovTab�̌v�Z�Dpair (strength = 2) �̏ꍇ
		for (int p1 = 0; p1 < parametermodel.size; p1++) {
			uncovTab[p1] = new ArrayList<Integer>();
			for (byte v1 = 0; v1 < parametermodel.range[p1]; v1++) {
				int sum = 0;
				for (int p2 = 0; p2 < parametermodel.size; p2++) {
					if (p1 == p2)
						continue;
					for (byte v2 = 0; v2 < parametermodel.range[p2]; v2++) {
						if (tab.get(p1, v1, p2, v2) == false)
							sum++;
					}
				}
				uncovTab[p1].add(sum);
			}
		}
	}

	private ResultOfGenerateOneTest generateOneTest(PairTable tab,
			int seedrownum, ArrayList<Integer>[] uncovTab,
			List<List<Testcase>> tupleSequenceList) {
		// ��̃e�X�g�P�[�X��1����
		Testcase tmp = new Testcase(parametermodel.size);
		tmp.quantify();

		boolean isSeedUsed = false;

		// seed�̃R�s�[�@����𖞂����Ȃ�������G���[
		if (seed.size() > 0 && seedrownum < seed.size()) {
			isSeedUsed = true;
			Testcase seedrow = seed.get(seedrownum);
			for (int i = 0; i < parametermodel.size; i++) {
				tmp.set(i, seedrow.get(i));
			}
		}
		if (constrainthandler.isPossible(tmp) == false) {
			Error.printError(Main.language == Main.Language.JP ? "seed��"
					+ (seedrownum + 1) + "�s�ڂ�����ᔽ�ł�" : "The" + (seedrownum + 1)
					+ "th seeding row violates the constraints.");
			return null;
		}

		// TODO tmp�ɃO���[�v��ǉ�
		addGroupedTuples(tmp, tupleSequenceList);

		// TODO �J��Ԃ�����
		// generateTempTest �ł� tab���X�V���Ȃ�
		Testcase temptest = generateTempTest(tmp, tab, uncovTab);
		int count = this.computeNewlyCoveredTuples(tab, temptest);

		// System.err.print(count + ", ");
		for (int i = 1; i < this.NumOfIterationForEachTest; i++) {
			Testcase newtemptest = generateTempTest(tmp, tab, uncovTab);
			int newcount = this.computeNewlyCoveredTuples(tab, newtemptest);

			// System.err.print(newcount + ", ");

			if (count < newcount) {
				count = newcount;
				temptest = newtemptest;
			}
		}

		// System.err.println(computeNewlyCoveredTuples(tab, temptest));

		// �J�o�[�����y�A�[�����ۂ�uncovTab�ɔ��f
		// finalizePairTable���O�łȂ��Ƃ���
		finallizeUncoverTable(uncovTab, tab, temptest);

		// �J�o�[�����y�A�[�����ۂ�tab�ɔ��f
		int newtuples = finalizePairTable(tab, temptest);

		// �Ԃ�l�̐ݒ�
		ResultOfGenerateOneTest res = new ResultOfGenerateOneTest();
		res.test = temptest;
		res.numOfCoveredTuples = newtuples;
		if (isSeedUsed) {
			res.nextSeedRow = seedrownum + 1;
		} else
			res.nextSeedRow = seedrownum;
		return res;
	}

	private void addGroupedTuples(Testcase tmp,
			List<List<Testcase>> tupleSequenceList) {
		// TODO Auto-generated method stub
		for (List<Testcase> TupleSequence : tupleSequenceList) {
			for (int i = 0; i < TupleSequence.size(); i++) {
				Testcase tuple = TupleSequence.get(i);
				if (tmp.superimpose(tuple, this.constrainthandler)) {
					TupleSequence.remove(i);
					break;
				}
			}
		}
	}

	private void finallizeUncoverTable(ArrayList<Integer>[] uncovTab,
			PairTable tab, Testcase temptest) {
		for (int p1 = 0; p1 < this.parametermodel.size; p1++) {
			int numCovered = 0;
			byte v1 = temptest.get(p1);
			if (v1 < 0)
				continue;
			for (int p2 = 0; p2 < this.parametermodel.size; p2++) {
				if (p1 == p2)
					continue;
				byte v2 = temptest.get(p2);
				if (v2 < 0)
					continue;
				if (tab.get(p1, v1, p2, v2) == false)
					numCovered++;
			}
			int numUncovered = uncovTab[p1].get(v1);
			uncovTab[p1].set(v1, numUncovered - numCovered);
		}

	}

	private Testcase generateTempTest(Testcase seedrow, PairTable tab,
			ArrayList<Integer>[] uncovTab) {

		// tmp���R�s�[
		Testcase tmp = seedrow.makeClone();

		// TODO �����_���Ȉ��q��𐶐�
		int[] parametersequence = new int[parametermodel.size];
		for (int i = 0; i < parametermodel.size; i++)
			parametersequence[i] = i;
		// �V���b�t��
		for (int i = 1; i < parametermodel.size; i++) {
			int dst = this.rnd.nextInt(i + 1);
			int tmppara = parametersequence[i];
			parametersequence[i] = parametersequence[dst];
			parametersequence[dst] = tmppara;
		}

		/*
		 * debug for (int i = 0; i < parametermodel.size; i++)
		 * System.out.print(parametersequence[i] + " "); System.out.println();
		 */

		// �e���q�ɂ���
		for (int i = 0; i < parametermodel.size; i++) {
			int p = parametersequence[i];
			// �l�����܂��Ă��Ȃ��Ȃ�
			if (tmp.get(p) < 0) {
				// �e�l�ɂ���ăJ�o�[�����y�A�𐔂��C�ő�̂��̂�I��
				int newlyCoveredTuples = -1;
				byte bestValue = -1;
				for (byte v = 0; v < this.parametermodel.range[p]; v++) {
					tmp.set(p, v);
					if (constrainthandler.isPossible(tmp)) {
						int newtuples = computeNewlyCoveredTuples(tmp, p, tab);
						if (newtuples > newlyCoveredTuples) {
							bestValue = v;
							newlyCoveredTuples = newtuples;
						}
					}
				}
				// assert (bestValue >= 0) : "error in chosing a value";
				if (bestValue == -1) {
					Error.printError(Main.language == Main.Language.JP ? "seed�ɐ���ᔽ�̍s������܂�"
							: "Some seeding row violates the constraints.");

					return null;
				}
				if (newlyCoveredTuples == 0) {
					// TODO �J�o�[�� 0 �Ȃ�C���҂����y�A���𐔂��C�ő�̂��̂�I��
					// TODO ���҂���y�A���ɂ́C��΂ɂނ�Ȃ��̂�����i���łɒl�����܂��Ă�����q�Ƃ̃y�A�j
					bestValue = -1;
					int possibleTuples = -1;

					// for tie breaking
					List<Byte> candidateValues = new ArrayList<Byte>();

					for (byte v = 0; v < this.parametermodel.range[p]; v++) {
						tmp.set(p, v);
						if (constrainthandler.isPossible(tmp)) {
							int newtuples = uncovTab[p].get(v);
							if (newtuples > possibleTuples) {
								bestValue = v;
								possibleTuples = newtuples;
							}
							// for tie breaking
							if (newtuples == 0 && possibleTuples == 0)
								candidateValues.add(v);
						}
					}
					// �ǂ��I��ł������Ȃ�C�����_���ɑI��
					// for tie breaking
					if (possibleTuples == 0)
						bestValue = candidateValues.get(this.rnd
								.nextInt(candidateValues.size()));
				}
				tmp.set(p, bestValue);
			}
		}

		// �V�J�o�[��0�Ƃ������Ƃ�����
		return tmp;
	}

	private int computeNewlyCoveredTuples(Testcase test, int p, PairTable tab) {
		int numOfNewlyCoveredTuples = 0;
		// bug?
		// for (int i = 0; i < numOfParameters - 1; i++) {
		for (int i = 0; i < numOfParameters; i++) {
			if (p == i)
				continue;
			if (test.get(i) < 0)
				continue;
			if (tab.get(p, test.get(p), i, test.get(i)) == false) {
				numOfNewlyCoveredTuples++;
			}
		}
		return numOfNewlyCoveredTuples;
	}

	private int finalizePairTable(PairTable tab, Testcase test) {
		int numOfNewlyCoveredTuples = 0;
		for (int i = 0; i < numOfParameters - 1; i++) {
			for (int j = i + 1; j < numOfParameters; j++) {
				if (tab.get(i, test.get(i), j, test.get(j)) == false) {
					tab.set(i, test.get(i), j, test.get(j));
					numOfNewlyCoveredTuples++;
				}
			}
		}
		return numOfNewlyCoveredTuples;
	}

	private int checkAllTuples(PairTable tab) {
		// TODO Auto-generated method stub
		// strength = 2
		int numOfPairs = 0;
		for (int i = 0; i < numOfParameters - 1; i++) {
			for (int j = i + 1; j < numOfParameters; j++) {
				for (byte v1 = 0; v1 < parametermodel.range[i]; v1++)
					for (byte v2 = 0; v2 < parametermodel.range[j]; v2++) {
						// pair�̐���
						Testcase pair = new Testcase(numOfParameters);
						pair.quantify();
						pair.set(i, v1);
						pair.set(j, v2);
						// pair�̃`�F�b�N
						// �֑��ᔽ�Ȃ�set
						if (constrainthandler.isPossible(pair) == false) {
							tab.set(i, v1, j, v2);
						} else
							numOfPairs++;
					}
			}
		}
		return numOfPairs;
	}

	// 2.20
	// copy from finalize....
	private int computeNewlyCoveredTuples(PairTable tab, Testcase test) {
		int numOfNewlyCoveredTuples = 0;
		for (int p0 = 0; p0 < numOfParameters - 1; p0++) {
			for (int p1 = p0 + 1; p1 < numOfParameters; p1++) {
				if (tab.get(p0, test.get(p0), p1, test.get(p1)) == false) {
					numOfNewlyCoveredTuples++;
				}
			}
		}
		return numOfNewlyCoveredTuples;
	}
}

class ResultOfGenerateOneTest {
	Testcase test;
	int numOfCoveredTuples;
	int nextSeedRow;
}

abstract class TupleTable {
}

class PairTable extends TupleTable {
	PairList[][] table;
	ParameterModel parametermodel;

	PairTable(ParameterModel parametermodel) {
		this.parametermodel = parametermodel;
		int n = parametermodel.size;
		table = new PairList[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (i < j)
					table[i][j] = new PairList(parametermodel.range[i],
							parametermodel.range[j]);
				else if (i > j) {
					// TODO: �G���[���ł�H�Ȃ�ŁH
					// table[i][j].list = table[j][i].list.clone();
				}

			}
		}
	}

	boolean get(int p1, byte v1, int p2, byte v2) {
		// TODO Auto-generated method stub
		if (p2 < p1) {
			int tmp = p1;
			p1 = p2;
			p2 = tmp;
			byte tmv = v1;
			v1 = v2;
			v2 = tmv;
		}
		// y * xrange + x
		/*
		 * if (this.table[p1][p2].list.length <= v1 + v2 *
		 * (parametermodel.range[p1])) { if (0 > v1 + v2 *
		 * (parametermodel.range[p1])) { System.out.println("size p1 p2 v1 v2" +
		 * this.table[p1][p2].list.length + "," + p1+","+p2+"," + v1 + "," +v2);
		 * }
		 */
		return this.table[p1][p2].list[v1 + v2 * (parametermodel.range[p1])];
	}

	// ����Ȃ��ꍇ�D���łɃJ�o�[�����ꍇ
	void set(int p1, byte v1, int p2, byte v2) {
		if (p2 < p1) {
			int tmp = p1;
			p1 = p2;
			p2 = tmp;
			byte tmv = v1;
			v1 = v2;
			v2 = tmv;
		}
		// y * xrange + x
		this.table[p1][p2].list[v1 + v2 * (parametermodel.range[p1])] = true;
	}
}

class PairList {
	boolean[] list;

	PairList(byte range1, byte range2) {
		this.list = new boolean[range1 * range2];
	}
}

class OutOfMaxNumOfTestcasesException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5868569262849102341L;

}
