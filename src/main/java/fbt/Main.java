package fbt;
import v1.*;
import v1.Error;

import java.util.List;
import java.util.TreeSet;

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
	// static final int Max_RandomSeed = 10;

	static boolean debugMode = false;

	enum Language {
		JP, EN
	};

	static Language language = Language.JP;

	// Start the whole process
	public static void main(String[] args) {

		long start = System.currentTimeMillis();

		try {
			// �R�}���h��������
			String errorMessage = processCommandArgument(args);

			// �G���[�o�͐�ݒ�
			Error.setOutputFile(outputFile);

			// �R�}���h�����ł̃G���[�o��
			if (errorMessage != null)
				Error.printError(errorMessage);

			// ���f���ǂݍ���
			// System.err.println("starting reading model");
			InputFileData inputfiledata = Inputer.readModel(modelFile);

			// ���񏈗� BDD�쐬
			ConstraintHandler conhndl = new ConstraintHandler(
					inputfiledata.parameterList, inputfiledata.constraintList, inputfiledata.constrainedParameters);

			//
			ParameterModel parametermodel = new ParameterModel(inputfiledata.parameterList);

			checkAllTuples(parametermodel, conhndl, inputfiledata);
			
		} catch (OutOfMemoryError e) {
			Error.printError(Main.language == Main.Language.JP ? "�������s���ł��D"
					: "Out of memory");
		} catch (Exception e) {
			Error.printError(Main.language == Main.Language.JP ? "�v���O�������ُ�I�����܂����D"
					: "Abnormal termination");
		}

		//		long end = System.currentTimeMillis();
		//		System.err.println("time: " + (end - start) + "ms");
	}

	// �R�}���h��������
	private static String processCommandArgument(String[] args) {
		if (args.length == 0) {
			Error.printError("usage: java -jar Program.jar [-i input] [-o output] [-policy] ...");
		}

		// policy�̕\��
		if (args.length == 1 && args[0].equals("-policy")) {
			System.out
			.println("This software (CIT-BACH 1.1) is distributed under the zlib license.\n"
					+ "The software contains Java classes from JDD, a Java BDD library "
					+ "developed by Arash Vahidi.\n"
					+ "JDD is free software distributed under the zlib license.\n"
					+ "\n"
					+ "Copyright (c) 2015, Tatsuhiro Tsuchiya\n"
					+ "This software is provided 'as-is', without any express or implied \n"
					+ "warranty. In no event will the authors be held liable for any damages \n"
					+ "arising from the use of this software. \n"
					+ "\n"
					+ "Permission is granted to anyone to use this software for any purpose, \n"
					+ "including commercial applications, and to alter it and redistribute it \n"
					+ "freely, subject to the following restrictions: \n"
					+ " \n"
					+ "   1. The origin of this software must not be misrepresented; you must not \n"
					+ "   claim that you wrote the original software. If you use this software \n"
					+ "   in a product, an acknowledgment in the product documentation would be \n"
					+ "   appreciated but is not required. \n"
					+ "   \n"
					+ "   2. Altered source versions must be plainly marked as such, and must not be \n"
					+ "   misrepresented as being the original software. \n"
					+ "   \n"
					+ "   3. This notice may not be removed or altered from any source \n"
					+ "   distribution. \n");
			System.exit(0);
		}

		// �G���[�\�����o�̓t�@�C�����w�肳���܂Œx�点��
		String errorMessage = null;

		for (int i = 0; i + 1 < args.length; i += 2) {
			String option = args[i];
			String str = args[i + 1];
			if (option.equals("-i")) {
				modelFile = str;
			} else if (option.equals("-o")) {
				outputFile = str;
			} else if (option.equals("-random")) {
				try {
					randomSeed = Integer.parseInt(str);
				} catch (NumberFormatException e) {
					// Error.printError("invalid number");
					errorMessage = Main.language == Main.Language.JP ? "�����_���V�[�h�ɖ����Ȓl���w�肳��Ă��܂��D"
							: "Invalid random seed";
					continue;
				}
				randomSeed = Math.abs(randomSeed) % (Max_RandomSeed + 1);
			} else if (option.equals("-c")) {
				if (str.equals("all")) {
					// �S�ԗ�
					strength = -1;
				} else {
					try {
						strength = Integer.parseInt(str);
					} catch (NumberFormatException e) {
						// Error.printError("invalid number");
						errorMessage = Main.language == Main.Language.JP ? "�ԗ��x�ɖ����Ȓl���w�肳��Ă��܂��D"
								: "Invalid strength";
						continue;
					}
					if (strength < 2 || MAX_STRENGTH < strength) {
						// Error.printError("invalid strength");
						errorMessage = Main.language == Main.Language.JP ? "�ԗ��x�ɖ����Ȓl���w�肳��Ă��܂��D"
								: "Invalid strength";
						continue;
					}
				}
			}
			// �J��Ԃ���
			else if (option.equals("-repeat")) {
				try {
					numOfIterations = Integer.parseInt(str);
				} catch (NumberFormatException e) {
					// Error.printError("invalid repeating number");
					errorMessage = Main.language == Main.Language.JP ? "����Ԃ����ɖ����Ȓl���w�肳��Ă��܂��D"
							: "Invalid number of repetition times";
					continue;
				}
				if (numOfIterations <= 0 || numOfIterations > MAX_ITERATIONS) {
					// Error.printError("invalid repeating number");
					errorMessage = Main.language == Main.Language.JP ? "����Ԃ����ɖ����Ȓl���w�肳��Ă��܂��D"
							: "Invalid number of repetition times";
					continue;
				}
			} else if (option.equals("-s")) {
				seedFile = str;
			} else if (option.equals("-debug")) {
				debugMode = true;
				// ���̈����̓_�~�[
			} else if (option.equals("-lang")) {
				if (str.matches("JP|jp")) {
					Main.language = Main.Language.JP;
				} else if (str.matches("EN|en")) {
					Main.language = Main.Language.EN;
				} else {
					errorMessage = "�����Ȍ��ꂪ�w�肳��Ă��܂� (Invalid Language)";
					continue;
				}
			} else {
				// Error.printError("Invalid option");
				errorMessage = Main.language == Main.Language.JP ? "�����ȃI�v�V�������w�肳��Ă��܂��D"
						: "Invalid option";
				continue;
			}
		}

		if (randomSeed == -1) {
			randomSeed = (int) Math.floor(Math.random() * (Max_RandomSeed + 1));
		}

		return errorMessage;
	}

	private static void checkAllTuples(ParameterModel parametermodel, ConstraintHandler conhndl,
			InputFileData inputfiledata) {
		// strength = 2
		int numOfParameters = parametermodel.size;
		for (int i = 0; i < numOfParameters - 1; i++) {
			for (int j = i + 1; j < numOfParameters; j++) {
				for (byte v1 = 0; v1 < parametermodel.range[i]; v1++) {
					for (byte v2 = 0; v2 < parametermodel.range[j]; v2++) {
						// pair�̐���
						Testcase pair = new Testcase(numOfParameters);
						pair.quantify();
						pair.set(i, v1);
						pair.set(j, v2);
						// pair�̃`�F�b�N
						// �֑��ᔽ�Ȃ�set
						if (conhndl.isPossible(pair) == false) {
//							System.out.println("[" + i + ", " + v1 + "]" + ", " +
	//								"[" + j + ", " + v2 + "]]");
							String para1 = inputfiledata.parameterList.get(i).name;
							String str1 = inputfiledata.parameterList.get(i).value_name.get(v1);
							String para2 = inputfiledata.parameterList.get(j).name;
							String str2 = inputfiledata.parameterList.get(j).value_name.get(v2);
							System.out.println("[" + para1 + ", " + str1 + "]" + ", " +
									"[" + para2 + ", " + str2 + "]");						
						}
					}
				}
			}
		}
	}
}
