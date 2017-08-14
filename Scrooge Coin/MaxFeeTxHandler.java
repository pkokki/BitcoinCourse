import java.util.ArrayList;
import java.security.PublicKey;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.Set;

public class MaxFeeTxHandler {
    private UTXOPool ledger;
    
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public MaxFeeTxHandler(UTXOPool utxoPool) {
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
        Set<Transaction> sortedTxs = sortByFee(possibleTxs);
        for (Transaction tx : sortedTxs) {
            if (isValidTx(tx)) {
                tx.finalize();
                updateLedger(tx);
                acceptedTxs.add(tx);
            }
        }
        return acceptedTxs.toArray(new Transaction[0]);
    }
    
    private Set<Transaction> sortByFee(Transaction[] possibleTxs) {
        Set<Transaction> set = new TreeSet<>(new Comparator<Transaction>() {
            @Override
            public int compare(Transaction o1, Transaction o2) {
                
                // Define comparing logic here
                return calcFees(o2).compareTo(calcFees(o1));
            }
        });
        for (Transaction tx : possibleTxs) {
            set.add(tx);
        }
        return set;
    }
    
    private Double calcFees(Transaction tx) {
        Double inSum = new Double(0);
        for (Transaction.Input ip : tx.getInputs()) {
            UTXO prevTx = new UTXO(ip.prevTxHash, ip.outputIndex);
            Transaction.Output prevOutput = ledger.getTxOutput(prevTx);
            if (prevOutput != null)
                inSum += prevOutput.value;
            else
                return new Double(0);
        }
        Double outSum = new Double(0);
        for (Transaction.Output op : tx.getOutputs()) {
            if (op != null)
                outSum += op.value;
            else
                return new Double(0);
        }
        return inSum - outSum;
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