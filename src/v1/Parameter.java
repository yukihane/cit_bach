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
			Error.printError(Main.language == Main.Language.JP ? "水準数に誤りがあります"
					: "Invalid number of values");
		}

		/* 水準名の重複を禁止-> comment out */
		/*
		 * for (int i = 0; i < value_name.size() - 1; i++) { for (int j = i+1; j
		 * < value_name.size(); j++) { if
		 * (value_name.get(i).equals(value_name.get(j)))
		 * Error.printError(Main.language == Main.Language.JP ? "水準名が重複しています" :
		 * "Overlap of parameter value name"); } }
		 */
	}

	/*
	 * int getID(String str) throws NoValueNameException { for (int i = 0; i <
	 * value_name.size(); i++) { if (value_name.get(i).equals(str)) return i; }
	 * throw new NoValueNameException(); }
	 */

	List<Integer> getID(String str) throws NoValueNameException {
		List<Integer> ids = new ArrayList<Integer>();
		for (int i = 0; i < value_name.size(); i++) {
			if (value_name.get(i).equals(str))
				ids.add(i);
		}
		if (ids.size() == 0)
			throw new NoValueNameException();
		else
			return ids;
	}

	// numberと算術的に同じ水準のidをとりだす→つかってない
	List<Integer> getID(double number) {
		List<Integer> ids = new ArrayList<Integer>();
		for (int i = 0; i < value_name.size(); i++) {
			double level;
			try {
				level = Double.parseDouble(value_name.get(i));
				if (level == number)
					ids.add(i);
			} catch (NumberFormatException e) {}
		}
		return ids;
	}
	
	// numberと算術的に関係のある水準のidをとりだす
	// level ～ number
	List<Integer> getID(double number, RelationOverDoublePair com) {
		List<Integer> ids = new ArrayList<Integer>();
		for (int i = 0; i < value_name.size(); i++) {
			double level;
			try {
				level = Double.parseDouble(value_name.get(i));
				if (com.hasRelation(level, number))
					ids.add(i);
			} catch (NumberFormatException e) {}
		}
		return ids;
	}

	
}

class PList extends LinkedList<Parameter> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	boolean checkNameDuplication() {
		for (int i = 0; i < this.size() - 1; i++)
			for (int j = i + 1; j < this.size(); j++) {
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

	/**
	 * 
	 */
	private static final long serialVersionUID = 6603037538755301907L;
}

class NoValueNameException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -92079148371461108L;
}
