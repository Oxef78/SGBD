public class ParserV2 {

    public static Query parse(String sql) {

        sql = sql.toUpperCase();

        Query q = new Query();

        // ---------------- SELECT ----------------
        String selectPart = sql.split("FROM")[0].replace("SELECT", "").trim();

        if (selectPart.startsWith("SUM") ||
            selectPart.startsWith("COUNT") ||
            selectPart.startsWith("MIN") ||
            selectPart.startsWith("MAX") ||
            selectPart.startsWith("AVG")) {

            q.isAggregate = true;

            if (selectPart.startsWith("SUM")) q.aggregateType = Aggregate.SUM;
            else if (selectPart.startsWith("COUNT")) q.aggregateType = Aggregate.COUNT;
            else if (selectPart.startsWith("MIN")) q.aggregateType = Aggregate.MIN;
            else if (selectPart.startsWith("MAX")) q.aggregateType = Aggregate.MAX;
            else q.aggregateType = Aggregate.AVG;

            String col = selectPart.substring(selectPart.indexOf("(")+1, selectPart.indexOf(")"));
            q.aggregateColumn = Integer.parseInt(col.replace("A", "").trim());
        } else {
            for (String c : selectPart.split(",")) {
                q.projections.add(Integer.parseInt(c.replace("A", "").trim()));
            }
        }

        // ---------------- FROM ----------------
        String fromPart = sql.split("FROM")[1].split("WHERE")[0].trim();

        for (String t : fromPart.split(",")) {
            q.tables.add(t.trim());
        }

        // ---------------- WHERE ----------------
        if (sql.contains("WHERE")) {
            String wherePart = sql.split("WHERE")[1];

            for (String condStr : wherePart.split("AND")) {

                String[] parts = condStr.split("=");

                Condition c = new Condition();

                String left = parts[0].trim();
                String right = parts[1].trim();

                // gauche
                if (left.contains(".")) {
                    c.leftTable = left.split("\\.")[0];
                    c.leftCol = Integer.parseInt(left.split("\\.")[1].replace("A", ""));
                } else {
                    c.leftTable = q.tables.get(0);
                    c.leftCol = Integer.parseInt(left.replace("A", ""));
                }

               // droite
                if (right.contains(".")) {
                    c.rightTable = right.split("\\.")[0];
                    c.rightCol = Integer.parseInt(right.split("\\.")[1].replace("A", "").trim());
                    c.rightValue = null;
                } else {
                    c.rightTable = null;
                    c.rightValue = Integer.parseInt(right.replace("\"", "").trim());
                }

                q.conditions.add(c);
            }
        }

        return q;
    }
}