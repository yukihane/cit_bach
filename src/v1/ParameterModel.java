package v1;

public class ParameterModel {
	// 因子の数
	final int size;
	// 各因子のレベル数
	final byte[] range;

	ParameterModel(PList plist) {
		size = plist.size();
		this.range = new byte[size];
		for (int i = 0; i < size; i++) {
			range[i] = (byte) plist.get(i).value_name.size();
		}
	}

}
