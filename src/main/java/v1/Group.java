package v1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

class Group {
	int[] member;
	int size;

	Group(Set<Integer> memberSet) {
		member = new int[memberSet.size()];
		int i = 0;
		for (Integer p : memberSet) {
			member[i] = p.intValue();
			i++;
		}
		Arrays.sort(member);
		size = member.length;
	}

	boolean equiv(Group g1) {
		Group g0 = this;
		if (g0.member.length != g1.member.length)
			return false;
		for (int i = 0; i < g0.member.length; i++) {
			if (g0.member[i] != g1.member[i])
				return false;
		}
		return true;
	}
}

// TODO TreeSetにする→重複を削除
class GList extends ArrayList<Group> {
	private static final long serialVersionUID = -6705998890411938435L;

	// TODO
	void sort() {
		Collections.sort(this, new MyComparator());
	}
}

class MyComparator implements Comparator<Group> {
	public int compare(Group arg0, Group arg1) {
		Group g0 = (Group) arg0;
		Group g1 = (Group) arg1;
		if (g0.size < g1.size) {
			return 1;
		} else if (g0.size > g1.size) {
			return -1;
		}

		// g0 と g1が同サイズ
		for (int i = 0; i < g0.member.length; i++) {
			if (g0.member[i] > g1.member[i])
				return 1;
			else if (g0.member[i] < g1.member[i])
				return -1;
		}

		return 0; // unreachable

	}

}
