package v1;

import java.util.ArrayList;
import java.util.List;

class GeneratorAll {

	static List<Testcase> generate(ParameterModel parameterModel, ConstraintHandler conhndl) throws OutOfMaxNumOfTestcasesException {
		long  numOfAllCombinations = 1;
		for (int p = 0; p < parameterModel.size; p++) {
			numOfAllCombinations *= parameterModel.range[p];
		}
		
		List<Testcase> testSet = new ArrayList<Testcase>();
		Testcase tmptest = new Testcase(parameterModel.size);
		if (conhndl.isPossible(tmptest))
			testSet.add(tmptest);
		
		for (int i = 1; i < numOfAllCombinations; i++) {
			tmptest = tmptest.makeClone();
			for (int p = 0; p < parameterModel.size; p++) {
				if (tmptest.get(p) + 1 >= parameterModel.range[p]) // ‚¯‚½‚ ‚°
					tmptest.set(p, (byte) 0);
				else {
					tmptest.set(p, (byte) (tmptest.get(p) + 1));
					break;
				}
			}
			if (conhndl.isPossible(tmptest)) {
				testSet.add(tmptest);
				if (testSet.size() > Generator.MaxNumOfTestcases) 
					throw new OutOfMaxNumOfTestcasesException();
			}
		}
		return testSet;
	}

}
