package v1;

import java.util.List;

public class TokenHandler {
	final List<String> tokenList;
	int index = 0;

	TokenHandler(List<String> tokenList) {
		this.tokenList = tokenList;
	}

	// ����Token �����o��
	String getToken() throws OutOfTokenStreamException {
		if (index >= tokenList.size())
			throw new OutOfTokenStreamException();
		String str = tokenList.get(index++);
		return str;
	}

	// ����Token ������D���o���Ȃ�
	String peepToken() {
		if (index >= tokenList.size())
			return null;
		return tokenList.get(index);
	}

	// ���̎���Token ������D���o���Ȃ�
	String peepNextToken() {
		if (index + 1 >= tokenList.size())
			return null;
		return tokenList.get(index + 1);
	}
}

class OutOfTokenStreamException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1260796740785437458L;
}
