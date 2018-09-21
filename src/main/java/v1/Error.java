package v1;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Error {

	// default でエラーは標準出力
	static String filename = null;

	public static void setOutputFile(String filename) {
		Error.filename = filename;
	}

	public static void printError(String str) {
		BufferedWriter writer;
		try {
			if (filename == null) {
				writer = new BufferedWriter(new OutputStreamWriter(System.out));
			} else
				writer = new BufferedWriter(new FileWriter(filename));
			writer.write("#ERROR," + str + "\n");
			writer.close();
		} catch (IOException e) {
			System.err.print(Main.language == Main.Language.JP ? "出力ファイル"
					+ filename + "が作成できません．" : "Output file " + filename
					+ " could not be created.");
		}
		System.exit(1);
	}
}
