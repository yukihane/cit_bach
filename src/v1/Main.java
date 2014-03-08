package v1;

import java.util.List;

public class Main {
	static int randomSeed = -1;
	static String modelFile;
	static int numOfIterations = 1;
	static String seedFile;
	static String outputFile;
	static int strength = 2; // default strength
	
	static final int MAX_LEVEL = 63;
	
	static final int MAX_ITERATIONS = 100000;
	static final int MAX_STRENGTH = 5;
	static final int Max_RandomSeed = 65535;
	//static final int Max_RandomSeed = 10;
	
	static boolean debugMode = false;
	
	// Start the whole process
	public static void main(String[] args) {

		try {
			// コマンド引数処理
			String errorMessage = processCommandArgument(args);

			// エラー出力先設定
			Error.setOutputFile(outputFile);
			
			// コマンド引数でのエラー出力
			if (errorMessage != null) 
				Error.printError(errorMessage);
			
			// モデル読み込み 	
			InputFileData inputfiledata = Inputer.readModel(modelFile);  

			// 制約処理 BDD作成
			ConstraintHandler conhndl = new ConstraintHandler(inputfiledata.parameterList, inputfiledata.constraintList);
			// DEBUG: BDDの表示
			/* conhndl.printConstraintBDD(); */

			//　シード読み込み
			List<Testcase> seed = Inputer.readSeed(seedFile, inputfiledata);


			//テストケース生成
			List<Testcase> testSet = null;
			if (strength == -1) {
				// 全網羅
				try {
					testSet = GeneratorAll.generate(new ParameterModel(inputfiledata.parameterList), conhndl);
				} catch (OutOfMaxNumOfTestcasesException e) {
					Error.printError("テストケース数が上限" + Generator.MaxNumOfTestcases + "を超えました");
				}

				new Outputer(outputFile).outputResult(testSet, inputfiledata, 
						modelFile, outputFile);
			}
			else { // strength >= 2
				Generator generator = GeneratorFactor.newGenerator(
						new ParameterModel(inputfiledata.parameterList),
						inputfiledata.groupList,
						conhndl, seed,	randomSeed, strength);
				try {
					testSet = generator.generate();
				} catch (OutOfMaxNumOfTestcasesException e) {
					testSet = null;
				}

				if (debugMode)
					System.err.println("random seed: " + randomSeed);
				// 繰り返す場合
				for (int i = 2; i < numOfIterations; i++) {
					int nextRandomSeed = (int) Math.floor(Math.random() * (Max_RandomSeed + 1));
					generator = GeneratorFactor.newGenerator(
							new ParameterModel(inputfiledata.parameterList),
							inputfiledata.groupList,
							conhndl, seed,	nextRandomSeed, strength);

					if (debugMode)
						System.err.println("random seed: " + nextRandomSeed);

					List<Testcase> nextTestSet = null;
					try {
						nextTestSet = generator.generate();
					} catch (OutOfMaxNumOfTestcasesException e) {
						nextTestSet = null;
					}

					if (testSet != null && nextTestSet != null) {
						if (nextTestSet.size() < testSet.size()) {
							testSet = nextTestSet;
							randomSeed = nextRandomSeed;
						}
					}
					else if (testSet == null && nextTestSet != null) {
						testSet = nextTestSet;
						randomSeed = nextRandomSeed;
					}
				}
				if (testSet == null) 
					Error.printError("テストケース数が上限" + Generator.MaxNumOfTestcases + "を超えました");

				new Outputer(outputFile).outputResult(testSet, inputfiledata, 
						randomSeed, modelFile, seedFile, outputFile, strength, numOfIterations);
			}

			/* debug */
			if (debugMode) {
				System.err.println("test set size: " + testSet.size());
			}
		} catch (OutOfMemoryError e) {
			Error.printError("メモリ不足です．");
		} catch (Exception e) {
			Error.printError("プログラムが異常終了しました．");
		}
	}	

	// コマンド引数処理
	private static String processCommandArgument(String[] args) {
		if (args.length == 0) {
			Error.printError("usage: java -jar Program.jar ...");
		}
		
		// エラー表示を出力ファイルが指定されるまで遅らせる
		String errorMessage = null; 
		
		for (int i = 0; i + 1 < args.length; i += 2) {
			String option = args[i];
			String str = args[i+1];
			if (option.equals("-i")) {
				modelFile = str;
			}
			else if (option.equals("-o")) {
				outputFile = str;
			}
			else if (option.equals("-random")) {
				try {
					randomSeed = Integer.parseInt(str);
				} catch (NumberFormatException e) {
					//Error.printError("invalid number");
					errorMessage = "ランダムシードに無効な値が指定されています．";
					continue;
				}
				randomSeed = Math.abs(randomSeed) % (Max_RandomSeed + 1);
			}
			else if (option.equals("-c")) {
				if (str.equals("all")) {
					// 全網羅
					strength = -1;
				} 
				else {
					try {
						strength = Integer.parseInt(str);
					} catch (NumberFormatException e) {
						// Error.printError("invalid number");
						errorMessage = "網羅度に無効な値が指定されています．";
						continue;
					}
					if (strength < 2 || MAX_STRENGTH < strength) {
						//Error.printError("invalid strength");
						errorMessage = "網羅度に無効な値が指定されています．";
						continue;
					}
				}
			}
			// 繰り返し数
			else if (option.equals("-repeat")) {
				try {
					numOfIterations = Integer.parseInt(str);
				} catch (NumberFormatException e) {
					//Error.printError("invalid repeating number");
					errorMessage = "くり返し数に無効な値が指定されています．";
					continue;
				}
				if (numOfIterations <= 0 || numOfIterations > MAX_ITERATIONS) {
					// Error.printError("invalid repeating number");
					errorMessage = "くり返し数に無効な値が指定されています．";
					continue;
				}
			}
			else if (option.equals("-s")) {
				seedFile  = str;
			}
			else if (option.equals("-debug")) {
				debugMode = true;
				// 次の引数はダミー
			}
			else {
				// Error.printError("Invalid option");
				errorMessage = "無効なオプションが指定されています．";
				continue;
			}
		}
		
		if (randomSeed == -1) {
			randomSeed = (int) Math.floor(Math.random() * (Max_RandomSeed + 1));
		}
		
		return errorMessage;
	}
}

class InputFileData {
	PList parameterList;
	GList groupList;
	List<Node> constraintList;

	InputFileData(PList parameterList, GList groupList,
			List<Node> constraintList) {
		this.parameterList = parameterList;
		this.groupList = groupList;
		this.constraintList = constraintList;
	}
}
