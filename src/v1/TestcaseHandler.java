/**
 * 
 */
package v1;

/**
 * @author Tsuchiya
 *
 */

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class TestcaseHandler {
	final int numOfParameters;

	TestcaseHandler(int numOfParameters) {
		this.numOfParameters = numOfParameters;
	}

	Testcase getTestcase() {
		return new Testcase(numOfParameters);
	}
}

class Testcase {
	byte[] value; // 0..level-1, or <0 (wildcard)

	// これを他から読んでるとまずいかも？
	Testcase(int n) {
		this.value = new byte[n];
	}

	void set(int parameter, byte value) {
		this.value[parameter] = value;
	}

	void setWildCard(int parameter) {
		this.value[parameter] = -1;
	}

	byte get(int parameter) {
		return value[parameter];
	}

	int getint(int parameter) {
		return (int) value[parameter];
	}

	void quantify() {
		for (int i = 0; i < this.value.length; i++)
			this.value[i] = -1;
	}

	Testcase makeClone() {
		Testcase newtest = new Testcase(this.value.length);
		for (int i = 0; i < newtest.value.length; i++) {
			newtest.value[i] = this.value[i];
		}
		return newtest;
	}

	void print() {
		for (int i = 0; i < value.length; i++)
			System.err.print(value[i] + ",");
		System.err.println();
	}

	// TODO Outputer.java に移動
	void print(BufferedWriter writer, InputFileData inputfiledata)
			throws IOException {
		for (int i = 0; i < value.length; i++)
			writer.write((i == 0 ? "" : ",")
					+ inputfiledata.parameterList.get(i).value_name
							.get(value[i]));
		writer.write("\n");
	}

	// tupleを重ねる
	// return true if a tuple is superimposed
	// 重ねた時に禁則に違反することあり->チェックする
	boolean superimpose(Testcase tuple, ConstraintHandler h) {
		Testcase tmp = this.makeClone();
		if (tmp.superimpose(tuple) == false)
			return false;
		if (h.isPossible(tmp) == false)
			return false;
		return this.superimpose(tuple);
		// must be true;
	}

	// tupleを重ねる
	// return true if a tuple is superimposed
	// 重ねた時に禁則に違反することあり->チェックしない
	private boolean superimpose(Testcase tuple) {
		// TODO Auto-generated method stu
		for (int i = 0; i < value.length; i++) {
			if (value[i] < 0 || tuple.value[i] < 0)
				continue;
			if (value[i] == tuple.value[i])
				continue;
			return false;
		}

		for (int i = 0; i < value.length; i++) {
			if (value[i] < 0)
				this.set(i, tuple.get(i));
		}
		return true;
	}
}

class Testsuite {
	List<Testcase> test;

	Testsuite() {
		test = new LinkedList<Testcase>();
	}

	void add(Testcase newtest) {
		this.test.add(newtest);
	}
}
