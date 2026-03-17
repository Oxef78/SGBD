
public class ExempleTableDisque {

	private static FullScanTableDisque scanOn(String path) {
		FullScanTableDisque scan = new FullScanTableDisque();
		scan.setFilePath(path);
		return scan;
	}

	public static void main(String[] args) {
		String table1Path = "Table Disque et exemples/table1";
		String table2Path = "Table Disque et exemples/table2";
		Tuple t = null;

		FullScanTableDisque T4 = scanOn(table1Path);
		FullScanTableDisque T5 = scanOn(table2Path);

		T4.open();
		T5.open();
		System.out.println("Table T4 ****");
		while((t = T4.next())!=null)
			System.out.println(t);
		System.out.println("Nb blocs lus :"+T4.reads);
		System.out.println("Table T5 ****");
		while((t = T5.next())!=null)
			System.out.println(t);
		T4.close();
		T5.close();
		
		FiltreEgalite f = new FiltreEgalite(T4, 2, 1);
		f.open();
		System.out.println("Filtre sur T4 ****");
		while((t = f.next())!=null)
			System.out.println(t);
		f.close();
		
		DBI join = new DBI(scanOn(table1Path), scanOn(table2Path), 0, 0);
		join.open();
		System.out.println("JOIN ****");
		while((t= join.next())!=null)
			System.out.println(t);
		join.close();

		HashJoinDisque hashJoin = new HashJoinDisque(scanOn(table1Path), scanOn(table2Path), 0, 0);
		hashJoin.open();
		System.out.println("HASH JOIN ****");
		while((t = hashJoin.next())!=null)
			System.out.println(t);
		hashJoin.close();

		System.out.println("HashJoin stats: build=" + hashJoin.getBuildTuples() +
				" probe=" + hashJoin.getProbeTuples() + " buckets=" + hashJoin.getBucketCount());
	
	}


}
