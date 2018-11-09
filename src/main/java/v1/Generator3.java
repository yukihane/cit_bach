package v1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

class Generator3 extends Generator {

	Generator3(ParameterModel parametermodel, GList groupList,
			ConstraintHandler constrainthandler, List<Testcase> seed,
			long randomseed) {
		super(parametermodel, groupList, constrainthandler, seed, randomseed);
	}

	@Override
	List<Testcase> generate() throws OutOfMaxNumOfTestcasesException {

		List<Testcase> res = new ArrayList<Testcase>();
		TripleTable tab = new TripleTable(parametermodel);

		// group毎，tuple列の作成
		List<List<Testcase>> tupleSequenceList = generateTupleSequenceList();

		//
		int numOfUncoveredTuples = checkAllTuples(tab);

		// 各<因子・値> に それが含まれる未カバーのtupleの総数を設定
		ArrayList<Integer>[] uncovTab = new ArrayList[parametermodel.size];
		initializeUncovTab(uncovTab, tab);

		int seedrownum = 0;
		while (numOfUncoveredTuples > 0 || hasTuplesToCover(tupleSequenceList)) {
			// testcase 1個生成
			ResultOfGenerateOneTest newresult = generateOneTest(tab,
					seedrownum, uncovTab, tupleSequenceList);

			res.add(newresult.test);
			if (res.size() > MaxNumOfTestcases)
				throw new OutOfMaxNumOfTestcasesException();

			numOfUncoveredTuples -= newresult.numOfCoveredTuples;
			seedrownum = newresult.nextSeedRow;
		}
		return res;
	}

	private int checkAllTuples(TripleTable tab) {
		// strength = 3
		int numOfTriples = 0;
		for (int i = 0; i < numOfParameters - 2; i++) {
			for (int j = i + 1; j < numOfParameters - 1; j++) {
				for (int k = j + 1; k < numOfParameters; k++) {
					for (byte v1 = 0; v1 < parametermodel.range[i]; v1++)
						for (byte v2 = 0; v2 < parametermodel.range[j]; v2++)
							for (byte v3 = 0; v3 < parametermodel.range[k]; v3++) {
								assert (i < j && j < k);
								// tripleの生成
								Testcase triple = new Testcase(numOfParameters);
								triple.quantify();
								triple.set(i, v1);
								triple.set(j, v2);
								triple.set(k, v3);
								// pairのチェック
								// 禁則違反ならset
								if (constrainthandler.isPossible(triple) == false) {
									tab.set(i, v1, j, v2, k, v3);
								} else
									numOfTriples++;
							}
				}
			}
		}
		return numOfTriples;
	}

	private void initializeUncovTab(ArrayList<Integer>[] uncovTab,
			TripleTable tab) {
		assert (parametermodel.size == uncovTab.length);
		// uncovTabの計算．triple (strength = 3) の場合
		for (int p = 0; p < parametermodel.size; p++) {
			uncovTab[p] = new ArrayList<Integer>();
			for (byte v = 0; v < parametermodel.range[p]; v++) {
				int sum = 0;
				for (int p1 = 0; p1 < parametermodel.size - 1; p1++) {
					for (int p2 = p1 + 1; p2 < parametermodel.size; p2++) {
						if (p == p1 || p == p2)
							continue;
						for (byte v1 = 0; v1 < parametermodel.range[p1]; v1++) {
							for (byte v2 = 0; v2 < parametermodel.range[p2]; v2++) {
								if (tab.get(p, v, p1, v1, p2, v2) == false)
									sum++;
							}
						}
					}
				}
				uncovTab[p].add(sum);
			}
		}
	}

	private ResultOfGenerateOneTest generateOneTest(TripleTable tab,
			int seedrownum, ArrayList<Integer>[] uncovTab,
			List<List<Testcase>> tupleSequenceList) {
		// 空のテストケースを1つつくる
		Testcase tmp = new Testcase(parametermodel.size);
		tmp.quantify();

		boolean isSeedUsed = false;
		// seedのコピー　制約を満たさなかったらエラー
		if (seed.size() > 0 && seedrownum < seed.size()) {
			isSeedUsed = true;
			Testcase seedrow = seed.get(seedrownum);
			for (int i = 0; i < parametermodel.size; i++) {
				tmp.set(i, seedrow.get(i));
			}
		}
		if (constrainthandler.isPossible(tmp) == false) {
			Error.printError(Main.language == Main.Language.JP ? "seedの"
					+ (seedrownum + 1) + "行目が制約違反です" : "The" + (seedrownum + 1)
					+ "th seeding row violates the constraints.");

			return null;
		}

		// 2.20
		// tmpにグループを追加
		boolean isGroupUsed = addGroupedTuples(tmp, tupleSequenceList);

		// TODO 繰り返させる
		// generateTempTest では tabを更新しない
		Testcase temptest = generateTempTest(tmp, tab, uncovTab);
		// 2.20
		// 0カバーの場合
		if (isSeedUsed == false && isGroupUsed == false
				&& computeNewlyCoveredTuples(tab, temptest) == 0) {
			addUncoveredTuple(tmp, tab, uncovTab);
			temptest = generateTempTest(tmp, tab, uncovTab);
		}

		// カバーしたペアーを実際にuncovTabに反映
		// finalizePairTableより前でないとだめ
		finallizeUncoverTable(uncovTab, tab, temptest);

		// カバーしたペアーを実際にtabに反映
		int newtuples = finalizeTupleTable(tab, temptest);

		// 返り値の設定
		ResultOfGenerateOneTest res = new ResultOfGenerateOneTest();
		res.test = temptest;
		res.numOfCoveredTuples = newtuples;
		if (isSeedUsed) {
			res.nextSeedRow = seedrownum + 1;
		} else
			res.nextSeedRow = seedrownum;
		return res;
	}

	private void finallizeUncoverTable(ArrayList<Integer>[] uncovTab,
			TripleTable tab, Testcase temptest) {
		for (int p = 0; p < this.parametermodel.size; p++) {
			int numCovered = 0;
			byte v = temptest.get(p);
			if (v < 0)
				continue;

			for (int p1 = 0; p1 < this.parametermodel.size - 1; p1++) {
				for (int p2 = p1 + 1; p2 < this.parametermodel.size; p2++) {
					if (p == p1 || p == p2)
						continue;
					byte v1 = temptest.get(p1);
					if (v1 < 0)
						continue;
					byte v2 = temptest.get(p2);
					if (v2 < 0)
						continue;
					if (tab.get(p, v, p1, v1, p2, v2) == false)
						numCovered++;
				}
			}

			int numUncovered = uncovTab[p].get(v);
			uncovTab[p].set(v, numUncovered - numCovered);
		}

	}

	private int finalizeTupleTable(TripleTable tab, Testcase test) {
		int numOfNewlyCoveredTuples = 0;
		for (int p0 = 0; p0 < numOfParameters - 2; p0++) {
			for (int p1 = p0 + 1; p1 < numOfParameters - 1; p1++) {
				for (int p2 = p1 + 1; p2 < numOfParameters; p2++) {
					if (tab.get(p0, test.get(p0), p1, test.get(p1), p2,
							test.get(p2)) == false) {
						tab.set(p0, test.get(p0), p1, test.get(p1), p2,
								test.get(p2));
						numOfNewlyCoveredTuples++;
					}
				}
			}
		}
		return numOfNewlyCoveredTuples;
	}

	// 2.20
	// 前の位置をおぼえておく
	private void addUncoveredTuple(Testcase tmp, TripleTable tab,
			ArrayList<Integer>[] uncovTab) {

		for (int p0 = 0; p0 < numOfParameters - 2; p0++)
			for (byte v0 = 0; v0 < this.parametermodel.range[p0]; v0++) {
				if (uncovTab[p0].get(v0) == 0)
					continue;
				for (int p1 = p0 + 1; p1 < numOfParameters - 1; p1++)
					for (byte v1 = 0; v1 < this.parametermodel.range[p1]; v1++) {
						if (uncovTab[p1].get(v1) == 0)
							continue;
						for (int p2 = p1 + 1; p2 < numOfParameters; p2++)
							for (byte v2 = 0; v2 < this.parametermodel.range[p2]; v2++)
								if (tab.get(p0, v0, p1, v1, p2, v2) == false) {
									tmp.set(p0, v0);
									tmp.set(p1, v1);
									tmp.set(p2, v2);
									return;
								}
					}
			}
	}

	// 2.20
	// return true if tuples from at least one group are added.
	private boolean addGroupedTuples(Testcase tmp,
			List<List<Testcase>> tupleSequenceList) {
		boolean isGroupAdded = false;
		for (List<Testcase> TupleSequence : tupleSequenceList) {
			for (int i = 0; i < TupleSequence.size(); i++) {
				Testcase tuple = TupleSequence.get(i);
				if (tmp.superimpose(tuple, this.constrainthandler)) {
					TupleSequence.remove(i);
					isGroupAdded = true;
					break;
				}
			}
		}
		return isGroupAdded;
	}

	private Testcase generateTempTest(Testcase seedrow, TripleTable tab,
			ArrayList<Integer>[] uncovTab) {

		// tmpをコピー
		Testcase tmp = seedrow.makeClone();

		// TODO ランダムな因子列を生成
		int[] parametersequence = new int[parametermodel.size];
		for (int i = 0; i < parametermodel.size; i++)
			parametersequence[i] = i;
		// シャッフル
		for (int i = 1; i < parametermodel.size; i++) {
			int dst = this.rnd.nextInt(i + 1);
			int tmppara = parametersequence[i];
			parametersequence[i] = parametersequence[dst];
			parametersequence[dst] = tmppara;
		}

		// 各因子について
		for (int i = 0; i < parametermodel.size; i++) {
			int p = parametersequence[i];
			// 値がきまっていないなら
			if (tmp.get(p) < 0) {
				// 各値によってカバーされるペアを数え，最大のものを選択
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
					Error.printError(Main.language == Main.Language.JP ? "seedに制約違反の行があります"
							: "Some seeding row violates the constraints.");
					return null;
				}
				if (newlyCoveredTuples == 0) {
					// TODO カバー数 0 なら，期待されるペア数を数え，最大のものを選択
					// TODO 期待するペア数には，絶対にむりなものもある（すでに値が決まっている因子とのペア）
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
					// どれを選んでも同じなら，ランダムに選ぶ
					// for tie breaking
					if (possibleTuples == 0)
						bestValue = candidateValues.get(this.rnd
								.nextInt(candidateValues.size()));
				}
				tmp.set(p, bestValue);
			}
		}

		// 新カバーが0ということもある
		return tmp;
	}

	private int computeNewlyCoveredTuples(Testcase test, int p, TripleTable tab) {
		int numOfNewlyCoveredTuples = 0;
		for (int p1 = 0; p1 < numOfParameters - 1; p1++) {
			for (int p2 = p1 + 1; p2 < numOfParameters; p2++) {
				if (p == p1 || p == p2)
					continue;
				if (test.get(p1) < 0 || test.get(p2) < 0)
					continue;
				if (tab.get(p, test.get(p), p1, test.get(p1), p2, test.get(p2)) == false) {
					numOfNewlyCoveredTuples++;
				}
			}
		}
		return numOfNewlyCoveredTuples;
	}

	// 2.20
	// copy from finalize....
	private int computeNewlyCoveredTuples(TripleTable tab, Testcase test) {
		int numOfNewlyCoveredTuples = 0;
		for (int p0 = 0; p0 < numOfParameters - 2; p0++) {
			for (int p1 = p0 + 1; p1 < numOfParameters - 1; p1++) {
				for (int p2 = p1 + 1; p2 < numOfParameters; p2++) {
					if (tab.get(p0, test.get(p0), p1, test.get(p1), p2,
							test.get(p2)) == false) {
						numOfNewlyCoveredTuples++;
					}
				}
			}
		}
		return numOfNewlyCoveredTuples;
	}
}

class TripleTable extends TupleTable {
	TripleList[][][] table;
	ParameterModel parametermodel;

	TripleTable(ParameterModel parametermodel) {
		this.parametermodel = parametermodel;
		int n = parametermodel.size;
		table = new TripleList[n][n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				for (int k = 0; k < n; k++) {
					if (i < j && j < k)
						table[i][j][k] = new TripleList(
								parametermodel.range[i],
								parametermodel.range[j],
								parametermodel.range[k]);
					else if (i > j) {
						// TODO: エラーがでる？なんで？
						// table[i][j].list = table[j][i].list.clone();
					}
				}
			}
		}
	}

	// requires p1 != p2 != p3
	boolean get(int p1, byte v1, int p2, byte v2, int p3, byte v3) {
		// TODO Auto-generated method stub
		// pairの場合にも反映？
		// 因子の昇順にソート
		ParameterValuePair[] pv = new ParameterValuePair[3];
		pv[0] = new ParameterValuePair(p1, v1);
		pv[1] = new ParameterValuePair(p2, v2);
		pv[2] = new ParameterValuePair(p3, v3);
		Arrays.sort(pv, new ParameterValuePairComparator());

		// return this.table[pv[1].p][pv[2].p][pv[3].p].list[getOffset(p1, v1,
		// p2, v2, p3, v3)];
		return this.table[pv[0].p][pv[1].p][pv[2].p].list[getOffset(pv)];

	}

	// 現れない場合．すでにカバーした場合
	// requires p1 != p2 != p3
	void set(int p1, byte v1, int p2, byte v2, int p3, byte v3) {
		ParameterValuePair[] pv = new ParameterValuePair[3];
		pv[0] = new ParameterValuePair(p1, v1);
		pv[1] = new ParameterValuePair(p2, v2);
		pv[2] = new ParameterValuePair(p3, v3);
		Arrays.sort(pv, new ParameterValuePairComparator());

		this.table[pv[0].p][pv[1].p][pv[2].p].list[getOffset(pv)] = true;
	}

	private int getOffset(ParameterValuePair[] pv) {
		int offset = pv[0].v;
		for (int i = 1; i < pv.length; i++) {
			int width = 1;
			for (int j = 0; j < i; j++) {
				width *= parametermodel.range[pv[j].p];
			}
			offset += pv[i].v * width;
		}
		return offset;
	}
}

class TripleList {
	boolean[] list;

	TripleList(byte range1, byte range2, byte range3) {
		this.list = new boolean[range1 * range2 * range3];
	}
}

class ParameterValuePair {
	int p;
	byte v;

	ParameterValuePair(int p, byte v) {
		this.p = p;
		this.v = v;
	}
}

class ParameterValuePairComparator implements Comparator<ParameterValuePair> {
	@Override
	public int compare(ParameterValuePair o1, ParameterValuePair o2) {
		// TODO Auto-generated method stub
		if (o1.p < o2.p)
			return -1;
		if (o1.p > o2.p)
			return 1;
		return 0;
	}

}
