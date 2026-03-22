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
	
		//------------------------------------------------------------------
		System.out.println("\n=== REQUETE : SUM(col0) WHERE col1 = 1 ===");

		//
		Operateur scan = new FullScanTableMemoire(tm);
		
		// Restrict : source, colonne, valeur, type
		Operateur filtre = new Restrict(scan, 1, 1, Restrict.EGAL);

		// Project : source, colonnes (on garde col0)
		Operateur project = new Project(filtre, new int[]{0});

		// Aggregate : source, type
		Operateur agg = new Aggregate(project, Aggregate.SUM);
		

		agg.open();
		Tuple res = agg.next();

		if (res != null) {
			System.out.println("Résultat(sum de col 0 lorsque col1 = 1) = " + res);
		} else {
			System.out.println("Aucun résultat");
		}

		agg.close();

		//------------------------------------------------------------------
		System.out.println("\n=== REQUETE VIA PARSEUR SQL ===");

		String sql = "SELECT SUM(A0) FROM T1";
		System.out.println("SQL = " + sql);

		Operateur op = Parser.parse(sql, tm);

		op.open();
		Tuple resParse = op.next();

		if (resParse != null) {
			System.out.println("Résultat(parseur) = " + resParse);
		} else {
			System.out.println("Aucun résultat");
		}

		op.close();

		//------------------------------------------------------------------
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

		// scan
		Operateur scan = new FullScanTableMemoire(tm);

		// project (on travaille sur col0)
		Operateur project = new Project(scan, new int[]{0});

		// aggregate
		Operateur agg = new Aggregate(project, type);

		agg.open();
		Tuple res = agg.next();
		System.out.println(label + " = " + res);
		agg.close();
	}
}