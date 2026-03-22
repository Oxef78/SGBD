import java.util.*;

public class Query {

    public List<String> tables = new ArrayList<>();
    public List<Integer> projections = new ArrayList<>();
    public List<Condition> conditions = new ArrayList<>();

    public boolean isAggregate = false;
    public int aggregateType;
    public int aggregateColumn;
}