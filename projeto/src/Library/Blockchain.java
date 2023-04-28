package Library;

import java.security.Signature;

public class Blockchain {
    byte[] hash;
    byte[] previousHash;

    int blkID;
    int numOfTransactions;

    private Signature signature;

    private int id = 1;

    public Blockchain(byte[] hash, byte[] previousHash, int blkID, int numOfTransactions) {
        this.hash = hash;
        this.blkID = id;
        id++;
        this.numOfTransactions = numOfTransactions;
    }

    public byte[] getHash() {
        return hash;
    }

    public byte[] getPreviousHash() {
        return previousHash;
    }

    public int getBlkID() {
        return blkID;
    }

    public int getNumOfTransactions() {
        return numOfTransactions;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public void setPreviousHash(byte[] previousHash) {
        this.previousHash = previousHash;
    }
}
