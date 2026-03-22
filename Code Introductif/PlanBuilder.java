import java.util.*;

public class PlanBuilder {

    public static Operateur build(Query q, Map<String, TableMemoire> tablesMemoire) {

        // ---------------- SCANS (RAM / DISQUE) ----------------
        Operateur left = createScan(q.tables.get(0), tablesMemoire);
        Operateur right = null;

        if (q.tables.size() > 1) {
            right = createScan(q.tables.get(1), tablesMemoire);
        }

        // ---------------- SPLIT CONDITIONS ----------------
        Condition joinCond = null;
        List<Condition> filters = new ArrayList<>();

        for (Condition c : q.conditions) {
            if (c.isJoin()) {
                joinCond = c;
            } else {
                filters.add(c);
            }
        }

        // ---------------- APPLY FILTERS EARLY ----------------
        for (Condition c : filters) {
            if (c.leftTable.equals(q.tables.get(0))) {
                left = new Restrict(left, c.leftCol, c.rightValue, Restrict.EGAL);
            } else if (right != null) {
                right = new Restrict(right, c.leftCol, c.rightValue, Restrict.EGAL);
            }
        }

        // ---------------- JOIN ----------------
        Operateur current;

        // CAS 1 : UNE SEULE TABLE
        if (right == null) {
            current = left;
        }

        // CAS 2 : JOIN
        else {

            if (joinCond != null) {

                int size = estimateSize(q.tables.get(0), tablesMemoire);

                if (size < 20) {
                    current = new DBI(left, right, joinCond.leftCol, joinCond.rightCol);
                } else {
                    current = new HashJoin(left, right, joinCond.leftCol, joinCond.rightCol);
                }

            } else {
                current = new DBI(left, right, 0, 0);
            }
        }

        // ---------------- PROJECT / AGG ----------------
        if (q.isAggregate) {
            current = new Project(current, new int[]{q.aggregateColumn});
            current = new Aggregate(current, q.aggregateType);
        } else if (!q.projections.isEmpty()) {
            int[] cols = q.projections.stream().mapToInt(i -> i).toArray();
            current = new Project(current, cols);
        }

        return current;
    }

    // ---------------- CREATE SCAN ----------------
    private static Operateur createScan(String tableName, Map<String, TableMemoire> tablesMemoire) {

        // RAM
        if (tablesMemoire.containsKey(tableName)) {
            return new FullScanTableMemoire(tablesMemoire.get(tableName));
        }

        // DISQUE
        FullScanTableDisque fs = new FullScanTableDisque();
        fs.setFilePath("./Table Disque et exemples/" + tableName);

        return fs;
    }

    // ---------------- ESTIMATION SIMPLE ----------------
    private static int estimateSize(String tableName, Map<String, TableMemoire> tablesMemoire) {

        if (tablesMemoire.containsKey(tableName)) {
            return tablesMemoire.get(tableName).valeurs.size();
        }

        // valeur par défaut disque (approximation)
        return 100;
    }
}