public class Parser {

    public static Operateur parse(String sql, TableMemoire table) {

        sql = sql.toUpperCase();

        String selectPart = sql.split("FROM")[0].replace("SELECT", "").trim();
        String fromPart = sql.split("FROM")[1].split("WHERE")[0].trim();

        String wherePart = null;
        if (sql.contains("WHERE")) {
            wherePart = sql.split("WHERE")[1].trim();
        }

        Operateur op;

        // -------------------------
        // FROM (gestion simple 1 table)
        // -------------------------
        op = new FullScanTableMemoire(table);

        // -------------------------
        // WHERE simple (col = valeur)
        // -------------------------
        if (wherePart != null && wherePart.contains("=") && !wherePart.contains(".")) {
            String[] parts = wherePart.split("=");
            int col = Integer.parseInt(parts[0].replace("A", "").trim());
            int val = Integer.parseInt(parts[1].trim());

            op = new Restrict(op, col, val, Restrict.EGAL);
        }

        // -------------------------
        // SELECT (AGG ou projection)
        // -------------------------

        // 🔹 Cas AGGREGATE
        if (selectPart.startsWith("SUM") ||
            selectPart.startsWith("COUNT") ||
            selectPart.startsWith("MIN") ||
            selectPart.startsWith("MAX") ||
            selectPart.startsWith("AVG")) {

            int type;
            if (selectPart.startsWith("SUM")) type = Aggregate.SUM;
            else if (selectPart.startsWith("COUNT")) type = Aggregate.COUNT;
            else if (selectPart.startsWith("MIN")) type = Aggregate.MIN;
            else if (selectPart.startsWith("MAX")) type = Aggregate.MAX;
            else type = Aggregate.AVG;

            String colStr = selectPart.substring(selectPart.indexOf("(")+1, selectPart.indexOf(")"));
            int col = Integer.parseInt(colStr.replace("A", "").trim());

            op = new Project(op, new int[]{col});
            op = new Aggregate(op, type);

            return op;
        }

        // 🔹 Cas SELECT *
        if (selectPart.equals("*")) {
            return op; // pas de projection
        }

        // 🔹 Cas SELECT A1,A2,A3
        String[] colsStr = selectPart.split(",");
        int[] cols = new int[colsStr.length];

        for (int i = 0; i < colsStr.length; i++) {
            cols[i] = Integer.parseInt(colsStr[i].replace("A", "").trim());
        }

        op = new Project(op, cols);

        return op;
    }
}