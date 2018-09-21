package v1;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class MainTest {

	@Test
	public void testMain() throws Exception {
		File resourcesRoot = new File("src/test/resources");
		File inputFolder = new File(resourcesRoot, "input");
		File extractFolder = new File(resourcesRoot, "extract");

		PrintStream nativeStram = System.out;

		for (File inputFile : inputFolder.listFiles()) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			System.setOut(new PrintStream(new BufferedOutputStream(baos)));

			File extractFile = new File(extractFolder, inputFile.getName());
			BufferedReader reader = new BufferedReader(new FileReader(extractFile));

			try {
				List<String> argList = new ArrayList<String>();
				argList.add("-i");
				argList.add(inputFile.getCanonicalPath());
				argList.add("-random");
				argList.add("1234"); // randomを指定して、生成結果を固定する
				String[] args = (String[]) argList.toArray(new String[argList.size()]);

				v1.Main.main(args);

				System.setOut(nativeStram);

				List<String> outputLines = new ArrayList<String>(
						Arrays.asList(baos.toString().split("\\r\\n|\\n|\\r")));
				outputLines.remove(0); // 1行目のコメントを削除

				String line;
				int rowCount = 0;
				while ((line = reader.readLine()) != null) {
					assertThat(outputLines.get(rowCount++), is(line));
				}
				System.out.println(inputFile.getName() + " is success.");
			} finally {
				baos.close();
				reader.close();
			}
		}

	}

}
