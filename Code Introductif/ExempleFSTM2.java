public class ExempleFSTM2 {

    public static void main(String[] args) {

        Tuple t;

        // Scan disque
        FullScanTableDisque scan = new FullScanTableDisque();

        scan.setFilePath("./Table Disque et exemples/table1");

        // ouverture
        scan.open();

        System.out.println("=== CONTENU TABLE DISQUE (table1) ===");

        int count = 0;

        while ((t = scan.next()) != null && count < 20) { // limite à 20
            System.out.println(t);
            count++;
        }

        scan.close();

        System.out.println("\n--- METRICS ---");
        System.out.println(scan);
    }
}