import java.util.ArrayList;
import java.security.PublicKey;

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
        for (Transaction.Input ip : tx.getInputs()) {
            UTXO prevTx = new UTXO(ip.prevTxHash, ip.outputIndex);
            if (!ledger.contains(prevTx)) {
                System.out.println("(1) failed");
                return false;
            }
        }
        // (2) the signatures on each input of {@code tx} are valid
        for (int index = 0; index < tx.numInputs(); index++) {
            Transaction.Input ip = tx.getInput(index);
            byte[] signature = ip.signature;
            
            byte[] message = tx.getRawDataToSign(index);
            
            UTXO prevTx = new UTXO(ip.prevTxHash, ip.outputIndex);
            Transaction.Output prevOutput = ledger.getTxOutput(prevTx);
            PublicKey pubKey = prevOutput.address;
            
            if (!Crypto.verifySignature(pubKey, message, signature)) {
                System.out.println("(2) failed");
                return false;
            }
        }
        // (3) no UTXO is claimed multiple times by {@code tx}
        ArrayList<UTXO> claimed = new ArrayList<UTXO>();
        for (Transaction.Input ip : tx.getInputs()) {
            UTXO utxo = new UTXO(ip.prevTxHash, ip.outputIndex);
            if (claimed.contains(utxo)) {
                System.out.println("(3) failed");
                return false;
            }
            claimed.add(utxo);
        }
        // (4) all of {@code tx}s output values are non-negative
        for (Transaction.Output op : tx.getOutputs()) {
            if (op.value < 0) {
                System.out.println("(4) failed");
                return false;
            }
        }
        // (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output values
        double inSum = 0;
        for (Transaction.Input ip : tx.getInputs()) {
            UTXO prevTx = new UTXO(ip.prevTxHash, ip.outputIndex);
            Transaction.Output prevOutput = ledger.getTxOutput(prevTx);
            inSum += prevOutput.value;
        }
        double outSum = 0;
        for (Transaction.Output op : tx.getOutputs()) {
            outSum += op.value;
        }
        if (inSum < outSum) {
            System.out.println("(5) failed");
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
        ArrayList<Transaction> acceptedTxs = new ArrayList<Transaction>();
        for (Transaction tx : possibleTxs) {
            if (isValidTx(tx)) {
                tx.finalize();
                updateLedger(tx);
                acceptedTxs.add(tx);
            }
        }
        return acceptedTxs.toArray(new Transaction[0]);
    }
    
    protected void updateLedger(Transaction tx) {
        for (Transaction.Input ip : tx.getInputs()) {
            UTXO prevTx = new UTXO(ip.prevTxHash, ip.outputIndex);
            ledger.removeUTXO(prevTx);
        }
        
        byte[] txHash = tx.getHash();
        for (int index = 0; index < tx.numOutputs(); index++) {
            UTXO utxo = new UTXO(txHash, index);
            Transaction.Output op = tx.getOutput(index);
            ledger.addUTXO(utxo, op);
        }
    }

}