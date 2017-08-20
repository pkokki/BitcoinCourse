import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    double p_graph; // parameter for random graph: prob. that an edge will exist
    double p_malicious; // prob. that a node will be set to be malicious
    double p_txDistribution; // probability of assigning an initial transaction to each node 
    int numRounds; // number of simulation rounds your nodes will run for
    boolean[] followees;
    HashMap<Integer, boolean[]> matrix = new HashMap<>();
    int round = 1;
    
    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        this.p_graph = p_graph;
        this.p_malicious = p_malicious;
        this.p_txDistribution = p_txDistribution;
        this.numRounds = numRounds;
    }

    public void setFollowees(boolean[] followees) {
        this.followees = followees;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        for (Transaction tx : pendingTransactions) {
            boolean[] flags = new boolean[this.followees.length];
            for (int i = 0; i < flags.length; i++) 
                flags[i] = true;
            matrix.put(tx.id, flags);
        }
    }

    public Set<Transaction> sendToFollowers() {
        HashSet<Transaction> txs = new HashSet<>();
        round += 1;
        if (round > numRounds) {
            double threshold = (1.0 - this.p_malicious) * p_graph * p_txDistribution * this.followees.length;
            for (Integer txId : matrix.keySet()) {
                boolean[] flags = matrix.get(txId);
                int sum = 0;
                for (boolean flag : flags) {
                    sum += flag ? 1 : 0;
                }
                if (sum >= threshold) {
                    txs.add(new Transaction(txId));
                }
            }
        }
        else {
            for (Integer txId : matrix.keySet()) {
                txs.add(new Transaction(txId));
            }
        }
        return txs;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        for (Candidate candidate : candidates) {
            Transaction tx = candidate.tx;
            int sender = candidate.sender;
            if (!matrix.containsKey(tx.id)) {
                matrix.put(tx.id, new boolean[this.followees.length]);
            }
            matrix.get(tx.id)[sender] = true;
        }
    }
}
