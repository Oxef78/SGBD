public class Aggregate extends Instrumentation implements Operateur {

    public static final int SUM = 0;
    public static final int COUNT = 1;
    public static final int MIN = 2;
    public static final int MAX = 3;
    public static final int AVG = 4;

    private Operateur source;
    private int type;

    private boolean done = false;
    private Tuple resultat;

    public Aggregate(Operateur src, int type) {
        this.source = src;
        this.type = type;
    }

    @Override
    public void open() {
        this.start();
        source.open();

        int sum = 0;
        int count = 0;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        Tuple t;

        switch (type) {

            case SUM:
                while ((t = source.next()) != null) {
                    sum += t.val[0];
                }
                break;

            case COUNT:
                while ((t = source.next()) != null) {
                    count++;
                }
                break;

            case MIN:
                while ((t = source.next()) != null) {
                    int val = t.val[0];
                    if (val < min) min = val;
                }
                break;

            case MAX:
                while ((t = source.next()) != null) {
                    int val = t.val[0];
                    if (val > max) max = val;
                }
                break;

            case AVG:
                while ((t = source.next()) != null) {
                    int val = t.val[0];
                    sum += val;
                    count++;
                }
                break;
        }

        resultat = new Tuple(1);

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

        this.stop();
    }

    @Override
    public Tuple next() {
        this.start();

        if (done) {
            this.stop();
            return null;
        }

        done = true;
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
}