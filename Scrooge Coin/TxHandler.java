import java.util.ArrayList;

public class TxHandler {
    private UTXOPool ledger;
    
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        ledger = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // (1) all outputs claimed by {@code tx} are in the current UTXO pool
        for (Transaction.Output op : tx.getOutputs()) {
        }
        // (2) the signatures on each input of {@code tx} are valid
        
        // (3) no UTXO is claimed multiple times by {@code tx}
        
        // (4) all of {@code tx}s output values are non-negative
        for (Transaction.Output op : tx.getOutputs()) {
            if (op.value < 0) {
                return false;
            }
        }
        // (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output values
        double inSum = 0;
        for (Transaction.Input ip : tx.getInputs()) {
            byte[] prevTxHash = ip.prevTxHash;
            int outputIndex = ip.outputIndex;
            byte[] signature = ip.signature;
            UTXO prevTx = new UTXO(prevTxHash, outputIndex);
            inSum += op.value;
        }
        double outSum = 0;
        for (Transaction.Output op : tx.getOutputs()) {
            outSum += op.value;
        }
        if (inSum >= outSum) {
            return false;
        }
        // All tests passed...
        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        ArrayList<Transaction> acceptedTX = new ArrayList<Transaction>();
        return acceptedTX.toArray(new Transaction[0]);
    }

}