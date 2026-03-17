import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HashJoinDisque extends Instrumentation implements Operateur {

	private Operateur buildInput;
	private Operateur probeInput;
	private int buildCol;
	private int probeCol;

	private Map<Integer, List<Tuple>> hashTable;
	private Tuple currentProbeTuple;
	private List<Tuple> currentMatches;
	private int matchCursor;
	private boolean opened;

	private int buildTuples;
	private int probeTuples;
	private int savedBucketCount;

	public HashJoinDisque(Operateur buildInput, Operateur probeInput, int buildCol, int probeCol) {
		super("HashJoinDisque" + Instrumentation.number++);
		this.buildInput = buildInput;
		this.probeInput = probeInput;
		this.buildCol = buildCol;
		this.probeCol = probeCol;
	}

	@Override
	public void open() {
		this.start();
		this.hashTable = new HashMap<Integer, List<Tuple>>();
		this.currentProbeTuple = null;
		this.currentMatches = null;
		this.matchCursor = 0;
		this.buildTuples = 0;
		this.probeTuples = 0;
		this.tuplesProduits = 0;
		this.memoire = 0;

		this.buildInput.open();
		Tuple buildTuple;
		while ((buildTuple = this.buildInput.next()) != null) {
			int key = buildTuple.val[this.buildCol];
			List<Tuple> bucket = this.hashTable.get(key);
			if (bucket == null) {
				bucket = new ArrayList<Tuple>();
				this.hashTable.put(key, bucket);
			}
			bucket.add(buildTuple);
			this.buildTuples++;
		}
		this.buildInput.close();

		this.savedBucketCount = this.hashTable.size();
		this.probeInput.open();
		this.opened = true;
		this.stop();
	}

	@Override
	public Tuple next() {
		this.start();
		if (!this.opened) {
			this.stop();
			return null;
		}

		while (true) {
			if (this.currentMatches != null && this.matchCursor < this.currentMatches.size()) {
				Tuple buildTuple = this.currentMatches.get(this.matchCursor++);
				Tuple joined = this.concat(buildTuple, this.currentProbeTuple);
				this.produit(joined);
				this.stop();
				return joined;
			}

			this.currentProbeTuple = this.probeInput.next();
			if (this.currentProbeTuple == null) {
				this.stop();
				return null;
			}
			this.probeTuples++;

			int key = this.currentProbeTuple.val[this.probeCol];
			this.currentMatches = this.hashTable.get(key);
			this.matchCursor = 0;
		}
	}

	@Override
	public void close() {
		this.start();
		this.probeInput.close();
		if (this.hashTable != null) {
			this.hashTable.clear();
		}
		this.opened = false;
		this.stop();
	}

	public int getBuildTuples() {
		return this.buildTuples;
	}

	public int getProbeTuples() {
		return this.probeTuples;
	}

	public int getBucketCount() {
		return this.savedBucketCount;
	}

	private Tuple concat(Tuple left, Tuple right) {
		Tuple ret = new Tuple(left.val.length + right.val.length);
		for (int i = 0; i < left.val.length; i++) {
			ret.val[i] = left.val[i];
		}
		for (int i = 0; i < right.val.length; i++) {
			ret.val[i + left.val.length] = right.val[i];
		}
		return ret;
	}
}
