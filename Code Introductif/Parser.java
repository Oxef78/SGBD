import java.util.*;

public class Parser {

    public static Operateur parse(String sql, Map<String, TableMemoire> tablesMemoire) {

        Query q = ParserV2.parse(sql);
        return PlanBuilder.build(q, tablesMemoire);
    }
}