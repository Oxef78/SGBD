public class PlanPrinter {

    public static void print(Operateur op) {
        print(op, 0);
    }

    private static void print(Operateur op, int level) {
        if (op == null) return;

        for (int i = 0; i < level; i++) {
            System.out.print("  ");
        }

        System.out.println(formatOperateur(op));

        for (Operateur child : op.getSources()) {
            print(child, level + 1);
        }
    }

    private static String formatOperateur(Operateur op) {
        String base = op.toString();

        if (op instanceof HashJoin) return base + " (HashJoin)";
        if (op instanceof DBI) return base + " (DBI)";
        if (op instanceof FullScanTableMemoire) return base + " (RAM)";
        if (op instanceof FullScanTableDisque) return base + " (DISK)";

        return base;
    }
}