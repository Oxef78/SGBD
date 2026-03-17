public class ExempleFSTM {

	public static void main(String[] args) {

		Tuple t;

		// Table : 3 colonnes, 10 lignes, valeurs max 5
		TableMemoire tm = TableMemoire.randomize(3, 10, 5);

		System.out.println("=== TABLE ===");
		Operateur scanAffichage = new FullScanTableMemoire(tm);
		scanAffichage.open();
		while ((t = scanAffichage.next()) != null) {
			System.out.println(t);
		}
		scanAffichage.close();

		System.out.println("\n=== REQUETE : SUM(col0) WHERE col1 = 1 ===");

		Operateur scan = new FullScanTableMemoire(tm);
		Operateur filtre = new Restrict(scan, 1, 1, Restrict.EGAL);
		Operateur agg = new Aggregate(filtre, 0, Aggregate.SUM);

		agg.open();
		Tuple res = agg.next();

		if (res != null) {
			System.out.println("Résultat = " + res);
		} else {
			System.out.println("Aucun résultat");
		}

		agg.close();

		System.out.println("\n=== AUTRES TESTS ===");

		// COUNT
		testAggregation(tm, Aggregate.COUNT, "COUNT");

		// MIN
		testAggregation(tm, Aggregate.MIN, "MIN");

		// MAX
		testAggregation(tm, Aggregate.MAX, "MAX");

		// AVG
		testAggregation(tm, Aggregate.AVG, "AVG");
	}

	private static void testAggregation(TableMemoire tm, int type, String label) {
		Operateur scan = new FullScanTableMemoire(tm);
		Operateur agg = new Aggregate(scan, 0, type);

		agg.open();
		Tuple res = agg.next();
		System.out.println(label + " = " + res);
		agg.close();
	}
}