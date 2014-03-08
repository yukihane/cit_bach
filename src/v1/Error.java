package v1;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Error {
	
	// default �ŃG���[�͕W���o��
	static String filename = null;

	static void setOutputFile(String filename) {
		Error.filename = filename;
	}
		
	static void printError(String str) {
		BufferedWriter writer;
		try {
			if (filename == null) {
				writer = new BufferedWriter(new OutputStreamWriter(System.out));
			} else
				writer = new BufferedWriter(new FileWriter(filename));
			writer.write("#ERROR," + str + "\n");
			writer.close();
		} catch (IOException e) {
			System.err.print("�o�̓t�@�C��" + filename + "���쐬�ł��܂���D");
		}
		System.exit(1);
	}
}
