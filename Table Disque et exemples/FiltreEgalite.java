public class FiltreEgalite extends Instrumentation implements Operateur {

	private Operateur source;
	private int colonneATester;
	private int valeurATester;

	public FiltreEgalite(Operateur in, int col, int val) {
		super("FiltreEgalite" + Instrumentation.number++);
		this.source = in;
		this.colonneATester = col;
		this.valeurATester = val;
	}

	@Override
	public void open() {
		this.start();
		this.source.open();
		this.tuplesProduits = 0;
		this.memoire = 0;
		this.stop();
	}

	@Override
	public Tuple next() {
		this.start();
		Tuple retour;
		while ((retour = this.source.next()) != null) {
			if (retour.val[this.colonneATester] == this.valeurATester) {
				this.produit(retour);
				this.stop();
				return retour;
			}
		}
		this.stop();
		return null;
	}

	@Override
	public void close() {
		this.start();
		this.source.close();
		this.stop();
	}
}
