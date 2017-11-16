package org.bitcoinj.core;

public class ArchiveHash {
    private Sha256Hash header;
    private Sha256Hash merkleRoot;
    private Sha256Hash merkleWitnessRoot;

    public ArchiveHash(Sha256Hash header, Sha256Hash merkleRoot, Sha256Hash merkleWitnessRoot)
    {
        this.header = header;
        this.merkleRoot = merkleRoot;
        this.merkleWitnessRoot = merkleWitnessRoot;
    }

    public Sha256Hash getHeader() {
        return header;
    }

    public Sha256Hash getMerkleRoot() {
        return merkleRoot;
    }

    public Sha256Hash getMerkleWitnessRoot() {
        return merkleWitnessRoot;
    }

}
