package org.bitcoinj.params;

import org.bitcoinj.core.ArchiveHashParameters;
import org.bitcoinj.core.Utils;

public class BSafeNetParams extends AbstractBitcoinNetParams {
    public BSafeNetParams() {
        super();
        interval = INTERVAL;
        targetTimespan = TARGET_TIMESPAN;
        maxTarget = Utils.decodeCompactBits(0x1e01ffff);
        dumpedPrivateKeyHeader = 128;
        addressHeader = 30;
        p2shHeader = 50;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        port = 34821;
        packetMagic = 0xbe469ec5L;
        bip32HeaderPub = 0x0488B21E; //The 4 byte header that serializes in base58 to "xpub".
        bip32HeaderPriv = 0x0488ADE4; //The 4 byte header that serializes in base58 to "xprv"

        majorityEnforceBlockUpgrade = 7;
        majorityRejectBlockOutdated = 9;
        majorityWindow = 10;

        genesisBlock.setDifficultyTarget(0x1e01ffff);
        genesisBlock.setTime(1464966958);
        genesisBlock.setNonce(0);
        id = ID_BSAFENET;
        subsidyDecreaseBlockCount = 210000;
        spendableCoinbaseDepth = 100;
        //String genesisHash = genesisBlock.getHashAsString();
        //checkState(genesisHash.equals("000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f"),
        //        genesisHash);

        newPoWHashStartHeight = 120;

        archiveHashParameters = new ArchiveHashParameters[1];
        archiveHashParameters[0] = new ArchiveHashParameters();
        archiveHashParameters[0].startHeight = 140;
        archiveHashParameters[0].blocksPerHash = 10;
        archiveHashParameters[0].nBlocks = 5;
    }

    private static BSafeNetParams instance;
    public static synchronized  BSafeNetParams get() {
        if (instance == null) {
            instance = new BSafeNetParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return PAYMENT_PROTOCOL_ID_BSAFENET;
    }
}
