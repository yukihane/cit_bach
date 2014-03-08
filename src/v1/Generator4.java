package v1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Generator4 extends Generator {

	Generator4(ParameterModel parametermodel, GList groupList,
			ConstraintHandler constrainthandler, List<Testcase> seed,
			long randomseed) {
		super(parametermodel, groupList, constrainthandler, seed, randomseed);
	}

	@Override
	List<Testcase> generate() throws OutOfMaxNumOfTestcasesException {

		List<Testcase> res = new ArrayList<Testcase>();
		QuadTable tab = new QuadTable(parametermodel);

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
			ResultOfGenerateOneTest newresult 
			= generateOneTest(tab, seedrownum, uncovTab, tupleSequenceList);

			res.add(newresult.test);
			if (res.size() > MaxNumOfTestcases) 
				throw new OutOfMaxNumOfTestcasesException();
			
			numOfUncoveredTuples -= newresult.numOfCoveredTuples;
			seedrownum = newresult.nextSeedRow;
		}
		return res;
	}
	
	private int checkAllTuples(QuadTable tab) {
		// strength = 4
		int numOfTuples = 0;
		for (int p0 = 0; p0 < numOfParameters - 3; p0++) {
			for (int p1 = p0 + 1; p1 < numOfParameters - 2; p1++) {
				for (int p2 = p1 + 1; p2 < numOfParameters - 1; p2++) {
					for (int p3 = p2 + 1; p3 < numOfParameters; p3++) {
						for (byte v0 = 0; v0 < parametermodel.range[p0]; v0++) {
							for (byte v1 = 0; v1 < parametermodel.range[p1]; v1++) {
								for (byte v2 = 0; v2 < parametermodel.range[p2]; v2++) {
									for (byte v3 = 0; v3 < parametermodel.range[p3]; v3++) {
										// tuple�̐���
										Testcase tuple = new Testcase(numOfParameters);
										tuple.quantify();
										tuple.set(p0, v0);
										tuple.set(p1, v1);
										tuple.set(p2, v2);
										tuple.set(p3, v3);
										// tuple�̃`�F�b�N
										// �֑��ᔽ�Ȃ�set
										if (constrainthandler.isPossible(tuple) == false) {
											tab.set(p0, v0, p1, v1, p2, v2, p3, v3);
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
		return numOfTuples;
	}
	
	private void initializeUncovTab(ArrayList<Integer>[] uncovTab, QuadTable tab) {
		assert (parametermodel.size == uncovTab.length);
		// uncovTab�̌v�Z�D strength = 4 �̏ꍇ
		for (int p = 0; p < parametermodel.size; p++) {
			uncovTab[p] = new ArrayList<Integer>();
			for (byte v = 0; v < parametermodel.range[p]; v++) {
				int sum = 0;
				
				for (int p1 = 0; p1 < parametermodel.size - 2; p1++) {
					for (int p2 = p1 + 1; p2 < parametermodel.size -1; p2++) {
						for (int p3 = p2 + 1; p3 < parametermodel.size; p3++) {
							if (p == p1 || p == p2 || p == p3)
								continue;
							for (byte v1 = 0; v1 < parametermodel.range[p1]; v1++) {
								for (byte v2 = 0; v2 < parametermodel.range[p2]; v2++) {
									for (byte v3 = 0; v3 < parametermodel.range[p3]; v3++) {
										if (tab.get(p, v, p1, v1, p2, v2, p3, v3) == false)
											sum++;
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
	
	private ResultOfGenerateOneTest generateOneTest(QuadTable tab, int seedrownum, ArrayList<Integer>[] uncovTab, List<List<Testcase>> tupleSequenceList) {
		// ��̃e�X�g�P�[�X��1����
		Testcase tmp = new Testcase(parametermodel.size);
		tmp.quantify();
		
		boolean isSeedUsed = false;
		
		// seed�̃R�s�[�@����𖞂����Ȃ�������G���[
		if (seed.size() > 0 && seedrownum < seed.size() ) {
			isSeedUsed = true;
			Testcase seedrow = seed.get(seedrownum);			
			for (int i = 0; i < parametermodel.size; i++) {
				tmp.set(i, seedrow.get(i));
			}
		}
		if (constrainthandler.isPossible(tmp) == false) {
			Error.printError("seed��" + (seedrownum + 1) + "�s�ڂ�����ᔽ�ł�");
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
		if (isSeedUsed == false && isGroupUsed == false && computeNewlyCoveredTuples(tab, temptest) == 0) {
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
			QuadTable tab, Testcase temptest) {
		for (int p = 0; p < this.parametermodel.size; p++) {
			int numCovered = 0;
			byte v = temptest.get(p);
			if (v < 0) continue;
			
			for (int p1 = 0; p1 < this.parametermodel.size - 2; p1++) {
				for (int p2 = p1 + 1; p2 < this.parametermodel.size - 1; p2++) {
					for (int p3 = p2 + 1; p3 < this.parametermodel.size; p3++) {
						if (p == p1 || p == p2 || p == p3) continue;
						byte v1 = temptest.get(p1);
						if (v1 < 0) continue;
						byte v2 = temptest.get(p2);
						if (v2 < 0) continue;
						byte v3 = temptest.get(p3);
						if (v3 < 0) continue;
						if (tab.get(p, v, p1, v1, p2, v2, p3, v3) == false)
							numCovered++;
					}
				}
			}
			
			int numUncovered = uncovTab[p].get(v);
			uncovTab[p].set(v, numUncovered - numCovered);
		}
		
	}
	
	private int finalizeTupleTable(QuadTable tab, Testcase test) {
		int numOfNewlyCoveredTuples = 0;
		for (int p0 = 0; p0 < numOfParameters - 3; p0++) {
			for (int p1 = p0 + 1; p1 < numOfParameters - 2; p1++) {
				for (int p2 = p1 + 1; p2 < numOfParameters - 1; p2++) {
					for (int p3 = p2 + 1; p3 < numOfParameters; p3++) {
						if (tab.get(p0, test.get(p0), p1, test.get(p1), p2, test.get(p2), p3, test.get(p3)) == false) {
							tab.set(p0, test.get(p0), p1, test.get(p1), p2, test.get(p2), p3, test.get(p3));
							numOfNewlyCoveredTuples++;
						}
					}
				}
			}
		}
		return numOfNewlyCoveredTuples;
	}
	
	
	// 2.20
	// �O�̈ʒu�����ڂ��Ă���
	private void addUncoveredTuple(Testcase tmp, QuadTable tab, ArrayList<Integer>[] uncovTab) {
		for (int p0 = 0; p0 < numOfParameters - 3; p0++) 
			for (byte v0 = 0; v0 < this.parametermodel.range[p0]; v0++) {
				if (uncovTab[p0].get(v0) == 0) continue; 
				for (int p1 = p0 + 1; p1 < numOfParameters - 2; p1++) 
					for (byte v1 = 0; v1 < this.parametermodel.range[p1]; v1++) {
						if (uncovTab[p1].get(v1) == 0) continue; 
						for (int p2 = p1 + 1; p2 < numOfParameters - 1; p2++)
							for (byte v2 = 0; v2 < this.parametermodel.range[p2]; v2++) {
								if (uncovTab[p2].get(v2) == 0) continue; 
								for (int p3 = p2 + 1; p3 < numOfParameters; p3++) 
									for (byte v3 = 0; v3 < this.parametermodel.range[p3]; v3++) {
										if (tab.get(p0, v0, p1, v1, p2, v2, p3, v3) == false) {
											tmp.set(p0, v0);
											tmp.set(p1, v1);
											tmp.set(p2, v2);
											tmp.set(p3, v3);
											return;
										}
								}
							}
					}
			}
	}
	
	// 2.20
	// return true if tuples from at least one group are added.
	private boolean addGroupedTuples(Testcase tmp, List<List<Testcase>> tupleSequenceList) {
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

	private Testcase generateTempTest(Testcase seedrow, QuadTable tab, ArrayList<Integer>[] uncovTab) {
		
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
					Error.printError("seed�ɐ���ᔽ�̍s������܂�");
					return null;
				}
				if (newlyCoveredTuples == 0) {
					// TODO �J�o�[�� 0 �Ȃ�C���҂����y�A���𐔂��C�ő�̂��̂�I��
					// TODO ���҂���y�A���ɂ́C��΂ɂނ�Ȃ��̂�����i���łɒl�����܂��Ă�����q�Ƃ̃y�A�j
					bestValue = -1;
					int possibleTuples = -1; 
					// for tie breaking
					List<Byte>candidateValues = new ArrayList<Byte>(); 
					
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
						bestValue = candidateValues.get(this.rnd.nextInt(candidateValues.size()));
				}
				tmp.set(p, bestValue);
			}
		}
		
		// �V�J�o�[��0�Ƃ������Ƃ�����
		return tmp;
	}

	private int computeNewlyCoveredTuples(Testcase test, int p, QuadTable tab) {
		int numOfNewlyCoveredTuples = 0;
		for (int p1 = 0; p1 < numOfParameters - 2; p1++) {
			for (int p2 = p1 + 1; p2 < numOfParameters - 1; p2++) {
				for (int p3 = p2 + 1; p3 < numOfParameters; p3++) {
					if (p == p1 || p == p2 || p == p3) continue;
					if (test.get(p1) < 0 || test.get(p2) < 0 || test.get(p3) < 0) continue;
					if (tab.get(p, test.get(p), p1, test.get(p1), p2, test.get(p2), p3, test.get(p3)) == false) {
						numOfNewlyCoveredTuples++;
					}
				}
			}
		}
		return numOfNewlyCoveredTuples;
	}
	
	
	// 2.20
	// copy from finalize....
	private int computeNewlyCoveredTuples(QuadTable tab, Testcase test) {
		int numOfNewlyCoveredTuples = 0;
		for (int p0 = 0; p0 < numOfParameters - 3; p0++) {
			for (int p1 = p0 + 1; p1 < numOfParameters - 2; p1++) {
				for (int p2 = p1 + 1; p2 < numOfParameters - 1; p2++) {
					for (int p3 = p2 + 1; p3 < numOfParameters; p3++) {
						if (tab.get(p0, test.get(p0), p1, test.get(p1), p2, test.get(p2), p3, test.get(p3)) == false) {
							numOfNewlyCoveredTuples++;
						}
					}
				}
			}
		}
		return numOfNewlyCoveredTuples;
	}
}


class QuadTable extends TupleTable {
	QuadList[][][][] table;
	ParameterModel parametermodel;
	
	QuadTable(ParameterModel parametermodel) {
		this.parametermodel = parametermodel;
		int n = parametermodel.size;
		table = new QuadList[n][n][n][n];
		for (int p0 = 0; p0 < n - 3; p0++) {
			for (int p1 = p0 + 1; p1 < n - 2; p1++) {
				for (int p2 = p1 + 1; p2 < n - 1; p2++) {
					for (int p3 = p2 + 1; p3 < n; p3++) {
						assert (p0 < p1 && p1 < p2 && p2 < p3);
						table[p0][p1][p2][p3] = new QuadList(parametermodel.range[p0],
								parametermodel.range[p1], parametermodel.range[p2], parametermodel.range[p3]);
					}
				}
			}
		}
	}
	
	// requires p1 != p2 != p3 != p4
	boolean get(int p1, byte v1, int p2, byte v2, int p3, byte v3, int p4, byte v4) {
		// pair�̏ꍇ�ɂ����f�H
		// ���q�̏����Ƀ\�[�g
		ParameterValuePair[] pv = new ParameterValuePair[4];
		pv[0] = new ParameterValuePair(p1, v1);
		pv[1] = new ParameterValuePair(p2, v2);
		pv[2] = new ParameterValuePair(p3, v3);
		pv[3] = new ParameterValuePair(p4, v4);
		Arrays.sort(pv, new ParameterValuePairComparator());
		
		return this.table[pv[0].p][pv[1].p][pv[2].p][pv[3].p].list[getOffset(pv)];
	}

	// ����Ȃ��ꍇ�D���łɃJ�o�[�����ꍇ
	// requires p1 != p2 != p3 != p4
	void set(int p1, byte v1, int p2,  byte v2, int p3, byte v3, int p4, byte v4) {
		ParameterValuePair[] pv = new ParameterValuePair[4];
		pv[0] = new ParameterValuePair(p1, v1);
		pv[1] = new ParameterValuePair(p2, v2);
		pv[2] = new ParameterValuePair(p3, v3);
		pv[3] = new ParameterValuePair(p4, v4);
		Arrays.sort(pv, new ParameterValuePairComparator());
		
		this.table[pv[0].p][pv[1].p][pv[2].p][pv[3].p].list[getOffset(pv)] = true;
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


class QuadList {
	boolean[] list;
	QuadList(byte range1, byte range2, byte range3, byte range4) {
		this.list = new boolean[range1 * range2 * range3 * range4];
	}
}
