import java.util.*;

public class ExempleFSTM {

	public static void main(String[] args) {

		Tuple t;

		// Table RAM
		TableMemoire tm = TableMemoire.randomize(3, 10, 5);

		// Mapping des tables
		Map<String, TableMemoire> tables = new HashMap<>();
		tables.put("T1", tm); // RAM

		System.out.println(" TABLE ");
		Operateur scanAffichage = new FullScanTableMemoire(tm);
		scanAffichage.open();
		while ((t = scanAffichage.next()) != null) {
			System.out.println(t);
		}
		scanAffichage.close();

		System.out.println("\n--- METRICS SCAN ---");
		System.out.println(scanAffichage);

		//------------------------------------------------------------------
		System.out.println("\n 1. PIPELINE MANUEL (REFERENCE) ");
		System.out.println("SUM(col0) WHERE col1 = 1");

		Operateur scan = new FullScanTableMemoire(tm);
		Operateur filtre = new Restrict(scan, 1, 1, Restrict.EGAL);
		Operateur project = new Project(filtre, new int[]{0});
		Operateur agg = new Aggregate(project, Aggregate.SUM);

		agg.open();
		Tuple res = agg.next();
		agg.close();

		System.out.println("Résultat = " + res);

		System.out.println("\nPLAN (manuel) :");
		PlanPrinter.print(agg);

		//------------------------------------------------------------------
		System.out.println("\n 2. MEME REQUETE VIA PARSEUR ");

		String sql = "SELECT SUM(A0) FROM T1 WHERE A1 = 1";
		System.out.println("SQL = " + sql);

		Operateur op = Parser.parse(sql, tables);

		op.open();
		Tuple resParse = op.next();
		op.close();

		System.out.println("Résultat = " + resParse);

		System.out.println("\nPLAN (parseur) :");
		PlanPrinter.print(op);

		//------------------------------------------------------------------
		System.out.println("\n=== 5. CHOIX DU JOIN (DBI vs HashJoin) ===");

		String sqlJoin = "SELECT A0 FROM T1, table1 WHERE T1.A0 = table1.A0";

		// CAS 1 : PETITE TABLE → DBI
		System.out.println("\n--- CAS 1 : PETITE TABLE (DBI attendu) ---");

		TableMemoire small = TableMemoire.randomize(3, 10, 5);
		Map<String, TableMemoire> tablesSmall = new HashMap<>();
		tablesSmall.put("T1", small);

		Operateur opSmall = Parser.parse(sqlJoin, tablesSmall);

		opSmall.open();
		while ((t = opSmall.next()) != null) {
			System.out.println("RESULT = " + t);
		}
		opSmall.close();

		System.out.println("\nPLAN (petite table) :");
		PlanPrinter.print(opSmall);

		// CAS 2 : GRANDE TABLE → HashJoin
		System.out.println("\n--- CAS 2 : GRANDE TABLE (HashJoin attendu) ---");

		TableMemoire big = TableMemoire.randomize(3, 10, 100);
		Map<String, TableMemoire> tablesBig = new HashMap<>();
		tablesBig.put("T1", big);

		Operateur opBig = Parser.parse(sqlJoin, tablesBig);

		opBig.open();
		while ((t = opBig.next()) != null) {
			System.out.println("RESULT = " + t);
		}
		opBig.close();

		System.out.println("\nPLAN (grande table) :");
		PlanPrinter.print(opBig);
	}
}