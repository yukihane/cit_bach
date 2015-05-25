package v1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Generator5 extends Generator {

	Generator5(ParameterModel parametermodel, GList groupList,
			ConstraintHandler constrainthandler, List<Testcase> seed,
			long randomseed) {
		super(parametermodel, groupList, constrainthandler, seed, randomseed);
	}

	@Override
	List<Testcase> generate() throws OutOfMaxNumOfTestcasesException {

		List<Testcase> res = new ArrayList<Testcase>();
		// TODO: �e�X�g���̉������ς��� if > ��� then�@�G���[
		QuintTable tab = new QuintTable(parametermodel);

		// group���Ctuple��̍쐬
		List<List<Testcase>> tupleSequenceList = generateTupleSequenceList();

		//
		int numOfUncoveredTuples = checkAllTuples(tab);

		// �e<���q�E�l> �� ���ꂪ�܂܂�関�J�o�[��tuple�̑�����ݒ�
		ArrayList<Integer>[] uncovTab = new ArrayList[parametermodel.size];
		initializeUncovTab(uncovTab, tab);

		int seedrownum = 0;
		while (numOfUncoveredTuples > 0 || hasTuplesToCover(tupleSequenceList)) {
			// testcase 1����
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

	private int checkAllTuples(QuintTable tab) {
		// strength = 5
		int numOfTuples = 0;
		for (int p0 = 0; p0 < numOfParameters - 4; p0++) {
			for (int p1 = p0 + 1; p1 < numOfParameters - 3; p1++) {
				for (int p2 = p1 + 1; p2 < numOfParameters - 2; p2++) {
					for (int p3 = p2 + 1; p3 < numOfParameters - 1; p3++) {
						for (int p4 = p3 + 1; p4 < numOfParameters; p4++) {
							for (byte v0 = 0; v0 < parametermodel.range[p0]; v0++) {
								for (byte v1 = 0; v1 < parametermodel.range[p1]; v1++) {
									for (byte v2 = 0; v2 < parametermodel.range[p2]; v2++) {
										for (byte v3 = 0; v3 < parametermodel.range[p3]; v3++) {
											for (byte v4 = 0; v4 < parametermodel.range[p4]; v4++) {
												// tuple�̐���
												Testcase tuple = new Testcase(
														numOfParameters);
												tuple.quantify();
												tuple.set(p0, v0);
												tuple.set(p1, v1);
												tuple.set(p2, v2);
												tuple.set(p3, v3);
												tuple.set(p4, v4);
												// tuple�̃`�F�b�N
												// �֑��ᔽ�Ȃ�set
												if (constrainthandler
														.isPossible(tuple) == false) {
													tab.set(p0, v0, p1, v1, p2,
															v2, p3, v3, p4, v4);
												} else
													numOfTuples++;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return numOfTuples;
	}

	private void initializeUncovTab(ArrayList<Integer>[] uncovTab,
			QuintTable tab) {
		assert (parametermodel.size == uncovTab.length);
		// uncovTab�̌v�Z�D strength = 5 �̏ꍇ
		for (int p = 0; p < parametermodel.size; p++) {
			uncovTab[p] = new ArrayList<Integer>();
			for (byte v = 0; v < parametermodel.range[p]; v++) {
				int sum = 0;

				for (int p1 = 0; p1 < parametermodel.size - 3; p1++) {
					for (int p2 = p1 + 1; p2 < parametermodel.size - 2; p2++) {
						for (int p3 = p2 + 1; p3 < parametermodel.size - 1; p3++) {
							for (int p4 = p3 + 1; p4 < parametermodel.size; p4++) {
								if (p == p1 || p == p2 || p == p3 || p == p4)
									continue;
								for (byte v1 = 0; v1 < parametermodel.range[p1]; v1++) {
									for (byte v2 = 0; v2 < parametermodel.range[p2]; v2++) {
										for (byte v3 = 0; v3 < parametermodel.range[p3]; v3++) {
											for (byte v4 = 0; v4 < parametermodel.range[p4]; v4++) {
												if (tab.get(p, v, p1, v1, p2,
														v2, p3, v3, p4, v4) == false)
													sum++;
											}
										}
									}
								}
							}
						}
					}
				}

				uncovTab[p].add(sum);
			}
		}
	}

	private ResultOfGenerateOneTest generateOneTest(QuintTable tab,
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

		// tmp�ɃO���[�v��ǉ�
		// 2.20
		boolean isGroupUsed = addGroupedTuples(tmp, tupleSequenceList);

		// TODO �J��Ԃ�����
		// generateTempTest �ł� tab���X�V���Ȃ�
		Testcase temptest = generateTempTest(tmp, tab, uncovTab);
		// 2.20
		// 0�J�o�[�̏ꍇ
		if (isSeedUsed == false && isGroupUsed == false
				&& computeNewlyCoveredTuples(tab, temptest) == 0) {
			addUncoveredTuple(tmp, tab, uncovTab);
			temptest = generateTempTest(tmp, tab, uncovTab);
		}

		// �J�o�[�����y�A�[�����ۂ�uncovTab�ɔ��f
		// finalizePairTable���O�łȂ��Ƃ���
		finallizeUncoverTable(uncovTab, tab, temptest);

		// �J�o�[�����y�A�[�����ۂ�tab�ɔ��f
		int newtuples = finalizeTupleTable(tab, temptest);

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

	private void finallizeUncoverTable(ArrayList<Integer>[] uncovTab,
			QuintTable tab, Testcase temptest) {
		for (int p = 0; p < this.parametermodel.size; p++) {
			int numCovered = 0;
			byte v = temptest.get(p);
			if (v < 0)
				continue;

			for (int p1 = 0; p1 < this.parametermodel.size - 3; p1++) {
				for (int p2 = p1 + 1; p2 < this.parametermodel.size - 2; p2++) {
					for (int p3 = p2 + 1; p3 < this.parametermodel.size - 1; p3++) {
						for (int p4 = p3 + 1; p4 < this.parametermodel.size; p4++) {
							if (p == p1 || p == p2 || p == p3 || p == p4)
								continue;
							byte v1 = temptest.get(p1);
							if (v1 < 0)
								continue;
							byte v2 = temptest.get(p2);
							if (v2 < 0)
								continue;
							byte v3 = temptest.get(p3);
							if (v3 < 0)
								continue;
							byte v4 = temptest.get(p4);
							if (v4 < 0)
								continue;
							if (tab.get(p, v, p1, v1, p2, v2, p3, v3, p4, v4) == false)
								numCovered++;
						}
					}
				}
			}

			int numUncovered = uncovTab[p].get(v);
			uncovTab[p].set(v, numUncovered - numCovered);
		}

	}

	private int finalizeTupleTable(QuintTable tab, Testcase test) {
		int numOfNewlyCoveredTuples = 0;
		for (int p0 = 0; p0 < numOfParameters - 4; p0++) {
			for (int p1 = p0 + 1; p1 < numOfParameters - 3; p1++) {
				for (int p2 = p1 + 1; p2 < numOfParameters - 2; p2++) {
					for (int p3 = p2 + 1; p3 < numOfParameters - 1; p3++) {
						for (int p4 = p3 + 1; p4 < numOfParameters; p4++) {
							if (tab.get(p0, test.get(p0), p1, test.get(p1), p2,
									test.get(p2), p3, test.get(p3), p4,
									test.get(p4)) == false) {
								tab.set(p0, test.get(p0), p1, test.get(p1), p2,
										test.get(p2), p3, test.get(p3), p4,
										test.get(p4));
								numOfNewlyCoveredTuples++;
							}
						}
					}
				}
			}
		}
		return numOfNewlyCoveredTuples;
	}

	// 2.20
	// �O�̈ʒu�����ڂ��Ă���
	private void addUncoveredTuple(Testcase tmp, QuintTable tab,
			ArrayList<Integer>[] uncovTab) {
		for (int p0 = 0; p0 < numOfParameters - 4; p0++)
			for (byte v0 = 0; v0 < this.parametermodel.range[p0]; v0++) {
				if (uncovTab[p0].get(v0) == 0)
					continue;
				for (int p1 = p0 + 1; p1 < numOfParameters - 3; p1++)
					for (byte v1 = 0; v1 < this.parametermodel.range[p1]; v1++) {
						if (uncovTab[p1].get(v1) == 0)
							continue;
						for (int p2 = p1 + 1; p2 < numOfParameters - 2; p2++)
							for (byte v2 = 0; v2 < this.parametermodel.range[p2]; v2++) {
								if (uncovTab[p2].get(v2) == 0)
									continue;
								for (int p3 = p2 + 1; p3 < numOfParameters - 1; p3++)
									for (byte v3 = 0; v3 < this.parametermodel.range[p3]; v3++) {
										if (uncovTab[p3].get(v3) == 0)
											continue;
										for (int p4 = p3 + 1; p4 < numOfParameters; p4++)
											for (byte v4 = 0; v4 < this.parametermodel.range[p4]; v4++) {
												if (tab.get(p0, v0, p1, v1, p2,
														v2, p3, v3, p4, v4) == false) {
													tmp.set(p0, v0);
													tmp.set(p1, v1);
													tmp.set(p2, v2);
													tmp.set(p3, v3);
													tmp.set(p4, v4);
													return;
												}
											}
									}
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

	private Testcase generateTempTest(Testcase seedrow, QuintTable tab,
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

	private int computeNewlyCoveredTuples(Testcase test, int p, QuintTable tab) {
		int numOfNewlyCoveredTuples = 0;
		for (int p1 = 0; p1 < numOfParameters - 3; p1++) {
			for (int p2 = p1 + 1; p2 < numOfParameters - 2; p2++) {
				for (int p3 = p2 + 1; p3 < numOfParameters - 1; p3++) {
					for (int p4 = p3 + 1; p4 < numOfParameters; p4++) {
						if (p == p1 || p == p2 || p == p3 || p == p4)
							continue;
						if (test.get(p1) < 0 || test.get(p2) < 0
								|| test.get(p3) < 0 || test.get(p4) < 0)
							continue;
						if (tab.get(p, test.get(p), p1, test.get(p1), p2,
								test.get(p2), p3, test.get(p3), p4,
								test.get(p4)) == false) {
							numOfNewlyCoveredTuples++;
						}
					}
				}
			}
		}
		return numOfNewlyCoveredTuples;
	}

	// 2.20
	// copy from finalize....
	private int computeNewlyCoveredTuples(QuintTable tab, Testcase test) {
		int numOfNewlyCoveredTuples = 0;
		for (int p0 = 0; p0 < numOfParameters - 4; p0++) {
			for (int p1 = p0 + 1; p1 < numOfParameters - 3; p1++) {
				for (int p2 = p1 + 1; p2 < numOfParameters - 2; p2++) {
					for (int p3 = p2 + 1; p3 < numOfParameters - 1; p3++) {
						for (int p4 = p3 + 1; p4 < numOfParameters; p4++) {
							if (tab.get(p0, test.get(p0), p1, test.get(p1), p2,
									test.get(p2), p3, test.get(p3), p4,
									test.get(p4)) == false) {
								numOfNewlyCoveredTuples++;
							}
						}
					}
				}
			}
		}
		return numOfNewlyCoveredTuples;
	}
}

class QuintTable extends TupleTable {
	QuintList[][][][][] table;
	ParameterModel parametermodel;

	QuintTable(ParameterModel parametermodel) {
		this.parametermodel = parametermodel;
		int n = parametermodel.size;
		table = new QuintList[n][n][n][n][n];
		for (int p0 = 0; p0 < n - 4; p0++) {
			for (int p1 = p0 + 1; p1 < n - 3; p1++) {
				for (int p2 = p1 + 1; p2 < n - 2; p2++) {
					for (int p3 = p2 + 1; p3 < n - 1; p3++) {
						for (int p4 = p3 + 1; p4 < n; p4++) {
							assert (p0 < p1 && p1 < p2 && p2 < p3 && p3 < p4);
							table[p0][p1][p2][p3][p4] = new QuintList(
									parametermodel.range[p0],
									parametermodel.range[p1],
									parametermodel.range[p2],
									parametermodel.range[p3],
									parametermodel.range[p4]);
						}
					}
				}
			}
		}
	}

	// requires p1 != p2 != p3 != p4
	boolean get(int p1, byte v1, int p2, byte v2, int p3, byte v3, int p4,
			byte v4, int p5, byte v5) {
		// pair�̏ꍇ�ɂ����f�H
		// ���q�̏����Ƀ\�[�g
		ParameterValuePair[] pv = new ParameterValuePair[5];
		pv[0] = new ParameterValuePair(p1, v1);
		pv[1] = new ParameterValuePair(p2, v2);
		pv[2] = new ParameterValuePair(p3, v3);
		pv[3] = new ParameterValuePair(p4, v4);
		pv[4] = new ParameterValuePair(p5, v5);
		Arrays.sort(pv, new ParameterValuePairComparator());

		return this.table[pv[0].p][pv[1].p][pv[2].p][pv[3].p][pv[4].p].list[getOffset(pv)];
	}

	// ����Ȃ��ꍇ�D���łɃJ�o�[�����ꍇ
	// requires p1 != p2 != p3 != p4
	void set(int p1, byte v1, int p2, byte v2, int p3, byte v3, int p4,
			byte v4, int p5, byte v5) {
		ParameterValuePair[] pv = new ParameterValuePair[5];
		pv[0] = new ParameterValuePair(p1, v1);
		pv[1] = new ParameterValuePair(p2, v2);
		pv[2] = new ParameterValuePair(p3, v3);
		pv[3] = new ParameterValuePair(p4, v4);
		pv[4] = new ParameterValuePair(p5, v5);
		Arrays.sort(pv, new ParameterValuePairComparator());

		this.table[pv[0].p][pv[1].p][pv[2].p][pv[3].p][pv[4].p].list[getOffset(pv)] = true;
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

class QuintList {
	boolean[] list;

	QuintList(byte range1, byte range2, byte range3, byte range4, byte range5) {
		this.list = new boolean[range1 * range2 * range3 * range4 * range5];
	}
}
