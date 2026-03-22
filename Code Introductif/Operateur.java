
public interface Operateur {

	public void open();
	public Tuple next();
	public void close();
	public Operateur[] getSources();  //permettra de suivre la construction de l'arbre d'execution
	
}
