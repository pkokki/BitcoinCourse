For this assignment, you will design and implement a distributed consensus algorithm given a graph of “trust” relationships between nodes. This is an alternative method of resisting sybil attacks and achieving consensus; it has the benefit of not “wasting” electricity like proof-of-work does.

Each test is measured based on

* How large a set of nodes have reached consensus. A set of nodes only counts as having reached consensus if they all output the same list of transactions.
* The size of the set that consensus is reached on. You should strive to make the consensus set of transactions as large as possible.
* Execution time, which should be within reason (if your code takes too long, the grading script will time out and you will be able to resubmit your code).
