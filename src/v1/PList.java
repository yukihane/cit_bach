package v1;

import java.util.LinkedList;
import java.util.TreeSet;

public class PList extends LinkedList<Parameter> {

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
	
	// useless?
	int getRestrictedID(String str, TreeSet<Integer> RestrictedParameters) 
		throws NoParameterNameException {
		try {
			int parameter = this.getID(str);
			int num = 0;
			for (Integer i: RestrictedParameters) {
				if (i == parameter)
					return num;
				num++;
			}
		} catch (NoParameterNameException e) {
			throw e;
		}
		// if the parameter is not a relevant one
		throw new NoParameterNameException();
	}
}
