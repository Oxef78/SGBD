public class Aggregate extends Instrumentation implements Operateur {

    public static final int SUM = 0;
    public static final int COUNT = 1;
    public static final int MIN = 2;
    public static final int MAX = 3;
    public static final int AVG = 4;

    private Operateur source;
    private int type;

    private boolean computed = false;
    private boolean returned = false;

    private int sum;
    private int count;
    private int min;
    private int max;

    public Aggregate(Operateur src, int type) {
        super("Aggregate" + Instrumentation.number++);
        this.source = src;
        this.type = type;
    }

    @Override
    public void open() {
        this.start();

        source.open();

        this.sum = 0;
        this.count = 0;
        this.min = Integer.MAX_VALUE;
        this.max = Integer.MIN_VALUE;

        this.computed = false;
        this.returned = false;

        this.tuplesProduits = 0;
        this.memoire = 0;

        this.stop();
    }

    private void computeIfNeeded() {
        if (computed) return;

        Tuple t;

        while ((t = source.next()) != null) {
            int val = t.val[0];

            switch (type) {
                case SUM:
                    sum += val;
                    break;

                case COUNT:
                    count++;
                    break;

                case MIN:
                    if (val < min) min = val;
                    break;

                case MAX:
                    if (val > max) max = val;
                    break;

                case AVG:
                    sum += val;
                    count++;
                    break;
            }
        }

        computed = true;
    }

    @Override
    public Tuple next() {
        this.start();

        if (returned) {
            this.stop();
            return null;
        }

        computeIfNeeded();

        Tuple resultat = new Tuple(1);

        switch (type) {
            case SUM:
                resultat.val[0] = sum;
                break;

            case COUNT:
                resultat.val[0] = count;
                break;

            case MIN:
                resultat.val[0] = (min == Integer.MAX_VALUE ? 0 : min);
                break;

            case MAX:
                resultat.val[0] = (max == Integer.MIN_VALUE ? 0 : max);
                break;

            case AVG:
                resultat.val[0] = (count == 0 ? 0 : sum / count);
                break;
        }

        returned = true;

        this.produit(resultat);
        this.stop();

        return resultat;
    }

    @Override
    public void close() {
        this.start();
        source.close();
        this.stop();
    }
    
    @Override
    public Operateur[] getSources() {
        return new Operateur[]{source};
    }
}