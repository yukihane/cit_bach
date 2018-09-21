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

	// 　シードで指定された部分テストの列をかえす
	// filename がnullなら大きさ0の列を返す
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

		// seed file は空白
		if (row == null)
			return seed;
		// parameters
		if (isParameterConsistent(row, inputfiledata) == false) {
			Error.printError(Main.language == Main.Language.JP ? "seedファイルの因子の記述に誤りがあります"
					: "There is an invalid parameter in the seeding file.");
			return null;
		}

		while ((row = parseCSVRow(reader)) != null) {
			// TODO debug
			// debug
			/**
			  System.out.print("seed:"); for (String str: row) {
			  System.out.print(str + "\t"); } System.out.println();
			 

			
			  System.err.print(row.size() +": "); for (String str: row) {
			  System.err.print(str + ","); } System.err.println();
			 

			
			 * if (isValueConsistent(row, inputfiledata) == false) {
			 * Error.printError("seedファイルの値の記述に誤りがあります"); return null; }
			 */

			// Testcase 生成と追加
			Testcase newtest = createTestcase(row, inputfiledata);

			if (newtest == null) {
				Error.printError(Main.language == Main.Language.JP ? "seedファイルの値の記述に誤りがあります"
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
			Error.printError(Main.language == Main.Language.JP ? "seedファイルにアクセスできません"
					: "Cannot access the seeding file.");
		}

		return seed;
	}

	private static Testcase createTestcase(List<String> row,
			InputFileData inputfiledata) {
		// 値が正しいか？
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
				// 複数同じ値があれば，最初のものとみなす
				// newtest.set(i, (byte)
				// inputfiledata.parameterList.get(i).getID(valuename));
				newtest.set(i,
						inputfiledata.parameterList.get(i).getID(valuename)
								.get(0).byteValue());
			} catch (NoValueNameException e) {
				return null;
			}
		}
		// 行に全パラメータ分の値がなければ，空白を入れる
		for (; i < inputfiledata.parameterList.size(); i++) {
			newtest.setWildCard(i);
		}
		return newtest;
	}

	private static boolean isParameterConsistent(List<String> row,
			InputFileData inputfiledata) {
		// 　因子数のチェック
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

				// TODO ,の前後の空白と#の前の空白をとって、,をセパレータに
				// line = line.replaceAll("#", ",#,");
				// #以降を消去
				line = line.replaceAll("#.*", "");
				
				// ,から始まる場合，行頭にスペースを入れる
				// line = line.replaceAll(",", " ,");
				// ,の前後にスペースを入れる．2016/2/19以下を追加
				line = line.replaceAll(",", " , ");
				StringTokenizer st = new StringTokenizer(line, ",");
				while (st.hasMoreTokens()) {
					String token = st.nextToken();
					token = token.trim();
					// if (token.equals("#"))
					// break;
					// token = token.trim(); unnecessary
					tokenList.add(token);
				}
			} catch (IOException e) {
				Error.printError("IO　error");
				return null;
			}
			// 空白だけ，コメントだけなら次の行をよむ
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

	public static InputFileData readModel(String filename) {

		BufferedReader reader = openFile(filename);
		List<String> tokenList = makeTokenList(reader);
		TokenHandler t = new TokenHandler(tokenList);

		// 因子、値のよみこみ
		PList parameterList = readParameter(t);

		// テスト
		/*
		 * for(Parameter p: parameterList) { System.err.print(p.name + ": ");
		 * for (String name : p.value_name) { System.err.print(name + ", "); }
		 * System.err.println(); }
		 */

		// グループ
		GList groupList = readGroup(t, parameterList);

		// テスト

		/*
		 * for(Group g: groupList) { for (int i = 0; i < g.size; i++)
		 * System.out.print(g.member[i] + ", "); System.out.println(); }
		 */

		// 制約
		
		// List<Node> constraintList = readConstraint(t, parameterList);
		ConstraintListAndConstrainedParameters constraints = readConstraint(t, parameterList);
		
		// close
		try {
			reader.close();
		} catch (IOException e) {
			Error.printError(Main.language == Main.Language.JP ? "入力ファイルにアクセスできません"
					: "Cannot access the input file");
		}
//		return new InputFileData(parameterList, groupList, constraintList);
		return new InputFileData(parameterList, groupList, constraints.constraintList, constraints.constrainedParameters);
	}

	private static ConstraintListAndConstrainedParameters readConstraint(TokenHandler t, PList parameterList) {
		List<Node> constraintList = new ArrayList<Node>();
		TreeSet<Integer> constrainedParameters = new TreeSet<Integer>();
		while (true) {
			if (t.peepToken() == null) {
				break;
			}
			//Node n = new Parse(t, parameterList).parseExpression();
			NodeAndConstrainedParameters res = new Parse(t, parameterList).extendedParseExpression();
			constraintList.add(res.node);
			constrainedParameters.addAll(res.constrainedParameters);
		}
		return new ConstraintListAndConstrainedParameters(constraintList, constrainedParameters);
	}

	// グループの読み込み
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
				Error.printError(Main.language == Main.Language.JP ? "パラメータ指定に誤りがあります"
						: "Invalid parameter");
			}
			// グループのパラメータ
			Set<Integer> memberSet = new TreeSet<Integer>();
			do {
				String name = null;
				try {
					name = t.getToken(); // チェックしてない
				} catch (OutOfTokenStreamException e) {
					Error.printError(Main.language == Main.Language.JP ? "グループ指定に誤りがあります"
							: "Invalid grouping");
				}
				try {
					/*
					 * debug System.out.print(name + " " +
					 * parameterList.getID(name) + ", ");
					 */
					memberSet.add(Integer.valueOf(parameterList.getID(name)));
				} catch (NoParameterNameException e) {
					Error.printError(Main.language == Main.Language.JP ? "グループ指定で因子名に誤りがあります"
							: "Invalid parameter in group");
				}
				if (t.peepToken() == null) {
					Error.printError(Main.language == Main.Language.JP ? "グループ指定に誤りがあります"
							: "Invalid grouping");
				}
			} while (t.peepToken().equals("}") == false);
			Group g = new Group(memberSet);
			groupList.add(g);

			// } のよみこみ
			try {
				t.getToken();
			} catch (OutOfTokenStreamException e) {
				Error.printError(Main.language == Main.Language.JP ? "グループ指定に誤りがあります"
						: "Invalid grouping");
			}
		}
		// TODO groupの整列
		groupList.sort();
		// TODO 重複要素の削除

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
			Error.printError(Main.language == Main.Language.JP ? "ファイル"
					+ filename + "が見つかりません．" : "Cannot find file " + filename);
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

	// パラメータの読み込み
	private static PList readParameter(TokenHandler t) {
		PList parameterList = new PList();

		while (true) {
			try {
				if (t.peepToken() == null || t.peepToken().equals("{")
						|| t.peepToken().equals("(")) {
					break;
				}

				// パラメータの名前
				// parameter name. Should be non-null
				String parameter_name = t.getToken();
				checkParameterName(parameter_name);
				Parameter p = new Parameter(parameter_name);

				if (t.getToken().equals("(") == false) {
					Error.printError(Main.language == Main.Language.JP ? "( がありません．"
							: "( expected");
				}
				// レベルの名前
				do {
					String level_name = t.getToken(); // チェックしてない
					checkLevelName(level_name);
					p.addName(level_name);
					if (t.peepToken() == null) {
						Error.printError(Main.language == Main.Language.JP ? "パラメータ指定に誤りがあります"
								: "Invalid parameters");
					}
				} while (t.peepToken().equals(")") == false);
				// ) のよみこみ
				t.getToken();

				// 値名の重複チェック
				p.check();

				parameterList.add(p);
			} catch (OutOfTokenStreamException e) {
				Error.printError(Main.language == Main.Language.JP ? "パラメータ指定に誤りがあります"
						: "Invalid parameters");
			}
		}

		// 　因子名の重複チェック
		if (parameterList.checkNameDuplication())
			Error.printError(Main.language == Main.Language.JP ? "因子名が重複しています"
					: "Duplicated parameters");

		// 因子数 >= 2
		if (parameterList.size() < 2)
			Error.printError(Main.language == Main.Language.JP ? "因子は2個以上必要です"
					: "Multiple parameters required");

		return parameterList;
	}

	private static void checkParameterName(String name) {
		// TODO Auto-generated method stub
		if (name.contains("(") || name.contains(")") || name.contains("{")
				|| name.contains("}") || name.contains("[")
				|| name.contains("]") || name.contains(";")
				|| name.contains(",")) {
			Error.printError(Main.language == Main.Language.JP ? "因子名に禁止文字が含まれています"
					: "Invalid symbol in parameter name");
		}
	}

	private static void checkLevelName(String name) {
		// TODO Auto-generated method stub
		if (name.contains("(") || name.contains(")") || name.contains("{")
				|| name.contains("}") || name.contains("[")
				|| name.contains("]") || name.contains(";")
				|| name.contains(",")) {
			Error.printError(Main.language == Main.Language.JP ? "水準名に禁止文字が含まれています"
					: "Invalid symbol in parameter value");
		}
	}

}

class ConstraintListAndConstrainedParameters {
	List<Node> constraintList;
	TreeSet<Integer> constrainedParameters;
	ConstraintListAndConstrainedParameters (List<Node> constraintList, TreeSet<Integer> constrainedParameters) {
		this.constraintList = constraintList;
		this.constrainedParameters = constrainedParameters;
	}
}

