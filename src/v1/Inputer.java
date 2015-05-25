package v1;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

public class Inputer {

	// �@�V�[�h�Ŏw�肳�ꂽ�����e�X�g�̗��������
	// filename ��null�Ȃ�傫��0�̗��Ԃ�
	static List<Testcase> readSeed(String filename, InputFileData inputfiledata) {
		List<Testcase> seed = new ArrayList<Testcase>();
		if (filename == null)
			return seed;

		BufferedReader reader = openFile(filename);
		List<String> row = parseCSVRow(reader);

		// debug
		/*
		 * System.out.print("seed:"); for (String str: row) {
		 * System.out.print(str + "\t"); } System.out.println();
		 */

		// seed file �͋�
		if (row == null)
			return seed;
		// parameters
		if (isParameterConsistent(row, inputfiledata) == false) {
			Error.printError(Main.language == Main.Language.JP ? "seed�t�@�C���̈��q�̋L�q�Ɍ�肪����܂�"
					: "There is an invalid parameter in the seeding file.");
			return null;
		}

		while ((row = parseCSVRow(reader)) != null) {
			// TODO debug
			// debug
			/*
			 * System.out.print("seed:"); for (String str: row) {
			 * System.out.print(str + "\t"); } System.out.println();
			 */

			/*
			 * System.err.print(row.size() +": "); for (String str: row) {
			 * System.err.print(str + ","); } System.err.println();
			 */

			/*
			 * if (isValueConsistent(row, inputfiledata) == false) {
			 * Error.printError("seed�t�@�C���̒l�̋L�q�Ɍ�肪����܂�"); return null; }
			 */

			// Testcase �����ƒǉ�
			Testcase newtest = createTestcase(row, inputfiledata);

			if (newtest == null) {
				Error.printError(Main.language == Main.Language.JP ? "seed�t�@�C���̒l�̋L�q�Ɍ�肪����܂�"
						: "There is an invalid parameter value in the seeding file.");
				return null;
			} else {
				seed.add(newtest);
				// debug
				/*
				 * newtest.print();
				 */
			}
		}
		try {
			reader.close();
		} catch (IOException e) {
			Error.printError(Main.language == Main.Language.JP ? "seed�t�@�C���ɃA�N�Z�X�ł��܂���"
					: "Cannot access the seeding file.");
		}

		return seed;
	}

	private static Testcase createTestcase(List<String> row,
			InputFileData inputfiledata) {
		// �l�����������H
		Testcase newtest = new Testcase(inputfiledata.parameterList.size());
		int i = 0;
		for (i = 0; i < Math
				.min(row.size(), inputfiledata.parameterList.size()); i++) {
			String valuename = row.get(i);
			if (valuename.equals("")) {
				newtest.setWildCard(i);
				continue;
			}
			try {
				// ���������l������΁C�ŏ��̂��̂Ƃ݂Ȃ�
				// newtest.set(i, (byte)
				// inputfiledata.parameterList.get(i).getID(valuename));
				newtest.set(i,
						inputfiledata.parameterList.get(i).getID(valuename)
								.get(0).byteValue());
			} catch (NoValueNameException e) {
				return null;
			}
		}
		// �s�ɑS�p�����[�^���̒l���Ȃ���΁C�󔒂�����
		for (; i < inputfiledata.parameterList.size(); i++) {
			newtest.setWildCard(i);
		}
		return newtest;
	}

	private static boolean isParameterConsistent(List<String> row,
			InputFileData inputfiledata) {
		// �@���q���̃`�F�b�N
		if (inputfiledata.parameterList.size() != row.size())
			return false;

		for (int i = 0; i < row.size(); i++) {
			try {
				if (inputfiledata.parameterList.getID(row.get(i)) != i)
					return false;
			} catch (NoParameterNameException e) {
				return false;
			}
		}
		return true;
	}

	private static List<String> parseCSVRow(BufferedReader reader) {
		String line;
		List<String> tokenList = new ArrayList<String>();
		while (tokenList.size() == 0) {
			try {
				line = reader.readLine();
				if (line == null)
					break;

				// TODO ,�̑O��̋󔒂�#�̑O�̋󔒂��Ƃ��āA,���Z�p���[�^��
				// line = line.replaceAll("#", ",#,");
				// #�ȍ~������
				line = line.replaceAll("#.*", "");
				// ,����n�܂�ꍇ�C�s���ɃX�y�[�X������
				// line = line.replaceAll(",", " ,");
				StringTokenizer st = new StringTokenizer(line, ",");
				while (st.hasMoreTokens()) {
					String token = st.nextToken();
					token = token.trim();
					// if (token.equals("#"))
					// break;
					token = token.trim();
					tokenList.add(token);
				}
			} catch (IOException e) {
				Error.printError("IO�@error");
				return null;
			}
			// �󔒂����C�R�����g�����Ȃ玟�̍s�����
			boolean isAllEmpty = true;
			for (String token : tokenList) {
				if (token.equals("") == false) {
					isAllEmpty = false;
					break;
				}
			}
			if (isAllEmpty)
				tokenList.clear();
			// if (tokenList.size() == 1)
			// if (tokenList.get(0).equals(""))
			// tokenList.remove(0);

		}

		if (tokenList.size() == 0)
			return null;
		else
			return tokenList;
	}

	static InputFileData readModel(String filename) {

		BufferedReader reader = openFile(filename);
		List<String> tokenList = makeTokenList(reader);
		TokenHandler t = new TokenHandler(tokenList);

		// ���q�A�l�̂�݂���
		PList parameterList = readParameter(t);

		// �e�X�g
		/*
		 * for(Parameter p: parameterList) { System.err.print(p.name + ": ");
		 * for (String name : p.value_name) { System.err.print(name + ", "); }
		 * System.err.println(); }
		 */

		// �O���[�v
		GList groupList = readGroup(t, parameterList);

		// �e�X�g

		/*
		 * for(Group g: groupList) { for (int i = 0; i < g.size; i++)
		 * System.out.print(g.member[i] + ", "); System.out.println(); }
		 */

		// ����
		List<Node> constraintList = readConstraint(t, parameterList);

		// close
		try {
			reader.close();
		} catch (IOException e) {
			Error.printError(Main.language == Main.Language.JP ? "���̓t�@�C���ɃA�N�Z�X�ł��܂���"
					: "Cannot access the input file");
		}
		return new InputFileData(parameterList, groupList, constraintList);
	}

	private static List<Node> readConstraint(TokenHandler t, PList parameterList) {
		List<Node> constraintList = new ArrayList<Node>();
		while (true) {
			if (t.peepToken() == null) {
				break;
			}
			Node n = new Parse(t, parameterList).parseExpression();
			constraintList.add(n);
		}
		return constraintList;
	}

	// �O���[�v�̓ǂݍ���
	private static GList readGroup(TokenHandler t, PList parameterList) {
		GList groupList = new GList();
		while (true) {
			if (t.peepToken() == null || t.peepToken().equals("(")) {
				break;
			}
			try {
				if (t.getToken().equals("{") == false) {
					Error.printError("{ expected");
				}
			} catch (OutOfTokenStreamException e) {
				Error.printError(Main.language == Main.Language.JP ? "�p�����[�^�w��Ɍ�肪����܂�"
						: "Invalid parameter");
			}
			// �O���[�v�̃p�����[�^
			Set<Integer> memberSet = new TreeSet<Integer>();
			do {
				String name = null;
				try {
					name = t.getToken(); // �`�F�b�N���ĂȂ�
				} catch (OutOfTokenStreamException e) {
					Error.printError(Main.language == Main.Language.JP ? "�O���[�v�w��Ɍ�肪����܂�"
							: "Invalid grouping");
				}
				try {
					/*
					 * debug System.out.print(name + " " +
					 * parameterList.getID(name) + ", ");
					 */
					memberSet.add(Integer.valueOf(parameterList.getID(name)));
				} catch (NoParameterNameException e) {
					Error.printError(Main.language == Main.Language.JP ? "�O���[�v�w��ň��q���Ɍ�肪����܂�"
							: "Invalid parameter in group");
				}
				if (t.peepToken() == null) {
					Error.printError(Main.language == Main.Language.JP ? "�O���[�v�w��Ɍ�肪����܂�"
							: "Invalid grouping");
				}
			} while (t.peepToken().equals("}") == false);
			Group g = new Group(memberSet);
			groupList.add(g);

			// } �̂�݂���
			try {
				t.getToken();
			} catch (OutOfTokenStreamException e) {
				Error.printError(Main.language == Main.Language.JP ? "�O���[�v�w��Ɍ�肪����܂�"
						: "Invalid grouping");
			}
		}
		// TODO group�̐���
		groupList.sort();
		// TODO �d���v�f�̍폜

		return groupList;
	}

	private static BufferedReader openFile(String filename) {
		BufferedReader reader = null;
		if (filename == null) {
			// default: standard input
			return new BufferedReader(new InputStreamReader(System.in));
		}

		try {
			reader = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			Error.printError(Main.language == Main.Language.JP ? "�t�@�C��"
					+ filename + "��������܂���D" : "Cannot find file " + filename);
		}
		return reader;
	}

	private static List<String> makeTokenList(BufferedReader reader) {
		List<String> tokenList = new ArrayList<String>();
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				line = line.replaceAll("\\(", " ( ");
				line = line.replaceAll("\\)", " ) ");

				line = line.replaceAll("#", " # ");

				line = line.replaceAll("\\{", " { ");
				line = line.replaceAll("\\}", " } ");

				line = line.replaceAll("\\[", " [ ");
				line = line.replaceAll("\\]", " ] ");

				// line = line.replaceAll(":", " : ");
				line = line.replaceAll(";", " ; ");

				StringTokenizer st = new StringTokenizer(line);
				while (st.hasMoreTokens()) {
					String token = st.nextToken();
					if (token.equals("#"))
						break;
					tokenList.add(token);
				}
			}
			reader.close();
		} catch (IOException e) {
			Error.printError(e.getMessage());
		}
		return tokenList;
	}

	// �p�����[�^�̓ǂݍ���
	private static PList readParameter(TokenHandler t) {
		PList parameterList = new PList();

		while (true) {
			try {
				if (t.peepToken() == null || t.peepToken().equals("{")
						|| t.peepToken().equals("(")) {
					break;
				}

				// �p�����[�^�̖��O
				// parameter name. Should be non-null
				String parameter_name = t.getToken();
				checkParameterName(parameter_name);
				Parameter p = new Parameter(parameter_name);

				if (t.getToken().equals("(") == false) {
					Error.printError(Main.language == Main.Language.JP ? "( ������܂���D"
							: "( expected");
				}
				// ���x���̖��O
				do {
					String level_name = t.getToken(); // �`�F�b�N���ĂȂ�
					checkLevelName(level_name);
					p.addName(level_name);
					if (t.peepToken() == null) {
						Error.printError(Main.language == Main.Language.JP ? "�p�����[�^�w��Ɍ�肪����܂�"
								: "Invalid parameters");
					}
				} while (t.peepToken().equals(")") == false);
				// ) �̂�݂���
				t.getToken();

				// �l���̏d���`�F�b�N
				p.check();

				parameterList.add(p);
			} catch (OutOfTokenStreamException e) {
				Error.printError(Main.language == Main.Language.JP ? "�p�����[�^�w��Ɍ�肪����܂�"
						: "Invalid parameters");
			}
		}

		// �@���q���̏d���`�F�b�N
		if (parameterList.checkNameDuplication())
			Error.printError(Main.language == Main.Language.JP ? "���q�����d�����Ă��܂�"
					: "Duplicated parameters");

		// ���q�� >= 2
		if (parameterList.size() < 2)
			Error.printError(Main.language == Main.Language.JP ? "���q��2�ȏ�K�v�ł�"
					: "Multiple parameters required");

		return parameterList;
	}

	private static void checkParameterName(String name) {
		// TODO Auto-generated method stub
		if (name.contains("(") || name.contains(")") || name.contains("{")
				|| name.contains("}") || name.contains("[")
				|| name.contains("]") || name.contains(";")
				|| name.contains(",")) {
			Error.printError(Main.language == Main.Language.JP ? "���q���ɋ֎~�������܂܂�Ă��܂�"
					: "Invalid symbol in parameter name");
		}
	}

	private static void checkLevelName(String name) {
		// TODO Auto-generated method stub
		if (name.contains("(") || name.contains(")") || name.contains("{")
				|| name.contains("}") || name.contains("[")
				|| name.contains("]") || name.contains(";")
				|| name.contains(",")) {
			Error.printError(Main.language == Main.Language.JP ? "�������ɋ֎~�������܂܂�Ă��܂�"
					: "Invalid symbol in parameter value");
		}
	}

}
