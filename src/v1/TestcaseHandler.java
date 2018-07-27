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


class Testsuite {
	List<Testcase> test;

	Testsuite() {
		test = new LinkedList<Testcase>();
	}

	void add(Testcase newtest) {
		this.test.add(newtest);
	}
}
