public static void executeQuery(String sql, TableMemoire tm) {

    System.out.println("\nSQL = " + sql);

    Operateur op = Parser.parse(sql, tm);

    op.open();

    Tuple t;
    while ((t = op.next()) != null) {
        System.out.println(t);
    }

    op.close();
}