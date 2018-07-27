package v1;

import java.util.List;
import java.util.TreeSet;

public class InputFileData {
	public PList parameterList;
	public GList groupList;
	public List<Node> constraintList;
	public TreeSet<Integer> constrainedParameters;

	InputFileData(PList parameterList, GList groupList,
			List<Node> constraintList, TreeSet<Integer> constrainedParameters) {
		this.parameterList = parameterList;
		this.groupList = groupList;
		this.constraintList = constraintList;
		this.constrainedParameters = constrainedParameters;
	}
}
