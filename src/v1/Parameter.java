package v1;

import java.util.*;

public class Parameter {
	String name;
	List<String> value_name = new LinkedList<String>();

	Parameter(String name) {
		this.name = name;
	}
	
	void addName(String name) {
		value_name.add(name);
	}

	// 値名の重複のチェック　重複していればエラー
	void check() {
		if (value_name.size() <= 0 || value_name.size() > Main.MAX_LEVEL) {
			Error.printError("水準数に誤りがあります");
		}
		for (int i = 0; i < value_name.size() - 1; i++) {
			for (int j = i+1; j < value_name.size(); j++) {
				if (value_name.get(i).equals(value_name.get(j))) 
					Error.printError("水準名が重複しています");
			}
		}
	}
	
	int getID(String str) throws NoValueNameException {
		for (int i = 0; i < value_name.size(); i++) {
			if (value_name.get(i).equals(str))
				return i;
		}
		throw new NoValueNameException();
	}

}

class PList extends LinkedList<Parameter> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	boolean checkNameDuplication() {
		for (int i = 0; i < this.size() - 1; i++)
			for (int j = i + 1; j < this.size(); j++)  {
				if (this.get(i).name.equals(this.get(j).name)) {
					return true;
				}
			}
		return false;
	}

	int getID(String str) throws NoParameterNameException {
		for (int i = 0; i < this.size(); i++) {
			if (this.get(i).name.equals(str)) 
				return i;
		}
		throw new NoParameterNameException();
	}
}

class NoParameterNameException extends Exception {
}
class NoValueNameException extends Exception {
}
