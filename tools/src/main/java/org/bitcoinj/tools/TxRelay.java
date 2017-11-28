package org.bitcoinj.tools;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bitcoinj.core.*;
import org.bitcoinj.core.listeners.OnTransactionBroadcastListener;
import org.bitcoinj.params.MainNetParams;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TxRelay {

    private static NetworkParameters params;
    private static PeerGroup mPeerGroup;

    public static void main(String[] args) throws Exception {
        params = MainNetParams.get();
        mPeerGroup = new PeerGroup(params);

        mPeerGroup.addOnTransactionBroadcastListener(new OnTransactionBroadcastListener() {
            @Override
            public void onTransaction(Peer peer, Transaction transaction) {
                relay(transaction);
            }
        });
        mPeerGroup.addAddress(new PeerAddress(params, InetAddress.getByName("192.168.56.1"), 8333));

        mPeerGroup.start();
        while (true) {

            if (!mPeerGroup.isRunning())
                mPeerGroup.start();
            try {
                Thread.sleep(2000);  // in milliseconds, so we sleep for 2 seconds here
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void relay(Transaction transaction) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost("http://localhost:8080/post");

            Map<String, Long> amounts = new HashMap<String, Long>();
            for (TransactionOutput output: transaction.getOutputs()) {
                Address address = output.getAddressFromP2PKHScript(params);
                if (address == null) address = output.getAddressFromP2SH(params);
                if (address != null) {
                    amounts.put(address.toBase58(), output.getValue().getValue());
                }
            }

            JSONObject json = sendmany(amounts);

            post.setEntity(new StringEntity(json.toJSONString(), StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = client.execute(post)) {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    HttpEntity entity = response.getEntity();
                    System.out.println(EntityUtils.toString(entity,
                            StandardCharsets.UTF_8));
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static JSONObject sendmany(Map<String, Long> amountsMap)
    {
        JSONObject json = new JSONObject();

        json.put("id", UUID.randomUUID().toString());
        json.put("method", "sendmany");

        JSONArray params = new JSONArray();
        params.add(""); // fromaddress

        JSONObject amounts = new JSONObject();
        amounts.putAll(amountsMap);
        params.add(amounts);

        json.put("params", params);

        return json;
    }
}
