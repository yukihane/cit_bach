package v1;

public class ParameterModel {
	// ���q�̐�
	public final int size;
	// �e���q�̃��x����
	public final byte[] range;

	public ParameterModel(PList plist) {
		size = plist.size();
		this.range = new byte[size];
		for (int i = 0; i < size; i++) {
			range[i] = (byte) plist.get(i).value_name.size();
		}
	}

}
