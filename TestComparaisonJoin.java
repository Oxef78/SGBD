import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestComparaisonJoin {

	public static void main(String[] args) {
		testJoinMemoire();
		testJoinDisque();
		System.out.println("OK: DBI et HashJoin produisent les memes resultats (memoire + disque).");
	}

	private static void testJoinMemoire() {
		TableMemoire t1 = buildMemoryTable(new int[][] {
			{1, 10}, {2, 20}, {1, 30}, {3, 40}, {2, 50}
		});
		TableMemoire t2 = buildMemoryTable(new int[][] {
			{1, 100}, {3, 300}, {1, 101}, {4, 400}, {2, 200}
		});

		Operateur dbi = new DBI(new FullScanTableMemoire(t1), new FullScanTableMemoire(t2), 0, 0);
		Operateur hash = new HashJoin(new FullScanTableMemoire(t1), new FullScanTableMemoire(t2), 0, 0);
		assertSameResults("memoire", dbi, hash);
	}

	private static void testJoinDisque() {
		String table1Path = "Table Disque et exemples/table1";
		String table2Path = "Table Disque et exemples/table2";

		Operateur dbi = new DBI(scanOn(table1Path), scanOn(table2Path), 0, 0);
		Operateur hash = new HashJoin(scanOn(table1Path), scanOn(table2Path), 0, 0);
		assertSameResults("disque", dbi, hash);
	}

	private static FullScanTableDisque scanOn(String path) {
		FullScanTableDisque scan = new FullScanTableDisque();
		scan.setFilePath(path);
		return scan;
	}

	private static TableMemoire buildMemoryTable(int[][] data) {
		TableMemoire table = new TableMemoire(data[0].length);
		for (int i = 0; i < data.length; i++) {
			Tuple t = new Tuple(data[i].length);
			for (int j = 0; j < data[i].length; j++) {
				t.val[j] = data[i][j];
			}
			table.valeurs.add(t);
		}
		return table;
	}

	private static void assertSameResults(String label, Operateur left, Operateur right) {
		List<Tuple> leftRows = collect(left);
		List<Tuple> rightRows = collect(right);

		Map<String, Integer> leftMultiset = toMultiset(leftRows);
		Map<String, Integer> rightMultiset = toMultiset(rightRows);

		if (!leftMultiset.equals(rightMultiset)) {
			throw new RuntimeException("Echec test " + label + " : DBI != HashJoin\n"
					+ "DBI tuples=" + leftRows.size() + " Hash tuples=" + rightRows.size());
		}

		System.out.println("Test " + label + " OK: " + leftRows.size() + " tuples compares.");
	}

	private static List<Tuple> collect(Operateur op) {
		List<Tuple> out = new ArrayList<Tuple>();
		op.open();
		Tuple t;
		while ((t = op.next()) != null) {
			out.add(copyTuple(t));
		}
		op.close();
		return out;
	}

	private static Tuple copyTuple(Tuple in) {
		Tuple copy = new Tuple(in.val.length);
		for (int i = 0; i < in.val.length; i++) {
			copy.val[i] = in.val[i];
		}
		return copy;
	}

	private static Map<String, Integer> toMultiset(List<Tuple> rows) {
		Map<String, Integer> multiset = new HashMap<String, Integer>();
		for (int i = 0; i < rows.size(); i++) {
			String key = tupleKey(rows.get(i));
			Integer c = multiset.get(key);
			if (c == null) {
				multiset.put(key, 1);
			} else {
				multiset.put(key, c + 1);
			}
		}
		return multiset;
	}

	private static String tupleKey(Tuple t) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < t.val.length; i++) {
			if (i > 0) {
				sb.append('|');
			}
			sb.append(t.val[i]);
		}
		return sb.toString();
	}
}