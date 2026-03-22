import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class FullScanTableDisque implements Operateur {

	private String filePath = "";
	private int taille = 0;
	private int tupleSize = 0;
	private int range = 100;
	private int blockSize = 4;
	private int blockCursor = 0;
	private int memorySize = 3;
	private Tuple[][] cache = new Tuple[memorySize][blockSize];
	private Queue<Integer> q = new LinkedList<>();
	private int currentMemoryBlock = 0;
	private FileWriter myWriter;
	private FileReader myReader;
	private Boolean start = true;
	public int reads = 0;

	// instrumentation ajoutée proprement
	private Instrumentation instrumentation = new Instrumentation("FullScanDisque" + Instrumentation.number++);

	@Override
	public void open() {
		this.instrumentation.reset();
		this.openFile();
		this.start = true;
		q = new LinkedList<>();
		cache = new Tuple[memorySize][blockSize];
	}

	@Override
	public void close() {
		try {
			this.myReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setFilePath(String fp) {
		this.filePath = fp;
	}

	@Override
	public Tuple next() {

		this.instrumentation.start();

		if (this.start || this.blockCursor == this.blockSize) {
			this.readNextBlock();
			this.blockCursor = 0;
			this.start = false;
		}

		Tuple t = this.cache[this.currentMemoryBlock][this.blockCursor++];

		if (t != null) {
			this.instrumentation.produit(t);
		}

		this.instrumentation.stop();

		return t;
	}

	public void openFile() {
		try {
			this.myReader = new FileReader(filePath);
			this.taille = this.myReader.read();
			this.tupleSize = this.myReader.read();
		} catch (IOException e) {
			System.out.println("Erreur de lecture");
			e.printStackTrace();
		}
	}

	public void readNextBlock() {
		try {

			if (q.size() < this.memorySize) {
				this.currentMemoryBlock = q.size();
				q.add(q.size());
			} else {
				int lastBlock = q.remove();
				this.currentMemoryBlock = lastBlock;
				q.add(lastBlock);
			}

			for (int i = 0; i < this.blockSize; i++) {
				Tuple t = new Tuple(this.tupleSize);

				for (int j = 0; j < this.tupleSize; j++) {
					t.val[j] = this.myReader.read();
				}

				if (t.val[0] != -1)
					this.cache[this.currentMemoryBlock][i] = t;
				else
					this.cache[this.currentMemoryBlock][i] = null;
			}

			this.reads++;

		} catch (IOException e) {
			System.err.println("Erreur de lecture.");
		}
	}

	public void randomize(int tuplesize, int tablesize) {
		try {
			this.myWriter = new FileWriter(filePath);

			this.myWriter.write(tablesize);
			this.myWriter.write(tuplesize);

			for (int i = 0; i < tablesize; i++) {
				Tuple t = new Tuple(tuplesize);

				for (int j = 0; j < tuplesize; j++) {
					t.val[j] = (int) (Math.random() * this.range);
					this.myWriter.write(t.val[j]);
				}
			}

			myWriter.close();
			System.out.println("Table générée");

			this.taille = tablesize;

		} catch (IOException e) {
			System.out.println("Erreur de création ou d'écriture de fichier.");
			e.printStackTrace();
		}
	}

	@Override
	public Operateur[] getSources() {
		return new Operateur[]{};
	}

	@Override
	public String toString() {
		return instrumentation.toString();
	}
}