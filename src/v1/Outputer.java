package v1;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class Outputer {
	
	BufferedWriter writer;
	
	Outputer(String filename) {
		this.writer = openFile(filename);
	}

	private BufferedWriter openFile(String filename) {
		BufferedWriter writer = null;
		if (filename == null) {
			// default: standard output
			return new BufferedWriter(new OutputStreamWriter(System.out));
		}
		
		try {
			writer = new BufferedWriter(new FileWriter(filename));
		} catch (IOException e) {
			//System.err.print(filename + " cannot be created.");
			// エラーを書き込めないので直接標準エラーへ
			System.err.print("出力ファイル" + filename + "が作成できません．");
			System.exit(1);
		}
		return writer;
	}
	
	void outputResult(List<Testcase> testSet, InputFileData inputfiledata, 
			int randomSeed, String modelFile, String seedFile, String outputFile, int strength, int numOfIterations){
		try {
			String firstline = "#SUCCESS" + "," + randomSeed + "," 
			+ "i" + "," + (modelFile == null ? "" : modelFile) + ","
			+ "s" + "," + (seedFile == null ? "" : seedFile) + ","
			+ "o" + "," + (outputFile == null ? "" : outputFile) + ","
			+ "c" + "," + (strength < 0 ? "all" : strength) + ","
			+ "random" + "," + randomSeed + ","
			+ "repeat" + "," + numOfIterations + "\n"
			;
			this.writer.write(firstline);
			
			for (int i = 0; i < inputfiledata.parameterList.size(); i++) {
				if (i > 0)
					writer.write(",");
				writer.write(inputfiledata.parameterList.get(i).name);
			}
			writer.write("\n");
				
			for (Testcase test : testSet)
				test.print(writer, inputfiledata);
			
			//close 
			this.writer.close();
		} catch (IOException e) {
			System.err.print("Cannot write the file");
		}
	}

	// 全網羅用
	public void outputResult(List<Testcase> testSet,
			InputFileData inputfiledata, String modelFile, String outputFile) {
		// TODO Auto-generated method stub
		try {
			String firstline = "#SUCCESS" + "," + 0 + "," 
			+ "i" + "," + (modelFile == null ? "" : modelFile) + ","
			+ "s" + "," + ","
			+ "o" + "," + (outputFile == null ? "" : outputFile) + ","
			+ "c" + "," + "all" + ","
			+ "random" + "," + 0 + ","
			+ "repeat" + "," + 1 + "\n"
			;
			this.writer.write(firstline);
			
			for (int i = 0; i < inputfiledata.parameterList.size(); i++) {
				if (i > 0)
					writer.write(",");
				writer.write(inputfiledata.parameterList.get(i).name);
			}
			writer.write("\n");
				
			for (Testcase test : testSet)
				test.print(writer, inputfiledata);
			
			//close 
			this.writer.close();
		} catch (IOException e) {
			System.err.print("Cannot write the file");
		}		
	}
}
