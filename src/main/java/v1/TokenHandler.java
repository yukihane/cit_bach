package v1;

import java.util.List;

public class TokenHandler {
	final List<String> tokenList;
	int index = 0;

	TokenHandler(List<String> tokenList) {
		this.tokenList = tokenList;
	}

	// 次のToken を取り出す
	String getToken() throws OutOfTokenStreamException {
		if (index >= tokenList.size())
			throw new OutOfTokenStreamException();
		String str = tokenList.get(index++);
		return str;
	}

	// 次のToken を見る．取り出さない
	String peepToken() {
		if (index >= tokenList.size())
			return null;
		return tokenList.get(index);
	}

	// 次の次のToken を見る．取り出さない
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
