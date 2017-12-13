package org.bitcoinj.tools;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bitcoinj.core.*;
import org.bitcoinj.core.listeners.OnTransactionBroadcastListener;
import org.bitcoinj.params.BSafeNetParams;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet2Params;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TxRelay {

    private static NetworkParameters params;
    private static NetworkParameters dstParams;
    private static PeerGroup mPeerGroup;

    private static String srcAddress;
    private static int srcPort;
    private static String dstAddress;
    private static int dstPort;
    private static String dstUser;
    private static String dstPassword;

    public static void main(String[] args) throws Exception {
        srcAddress = args[0];
        srcPort = Integer.parseInt(args[1]);
        dstAddress = args[2];
        dstPort = Integer.parseInt(args[3]);
        dstUser = args[4];
        dstPassword = args[5];

        params = MainNetParams.get();
        dstParams = BSafeNetParams.get();
        mPeerGroup = new PeerGroup(params);

        mPeerGroup.addOnTransactionBroadcastListener(new OnTransactionBroadcastListener() {
            @Override
            public void onTransaction(Peer peer, Transaction transaction) {
                relay(transaction);
            }
        });
        mPeerGroup.addAddress(new PeerAddress(params, InetAddress.getByName(srcAddress), srcPort));

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
        System.err.println("relay transaction: " + transaction);

        CredentialsProvider credentials = new BasicCredentialsProvider();
        credentials.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(dstUser, dstPassword));

        try (CloseableHttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(credentials).build()) {
            HttpPost post = new HttpPost(new URL("http", dstAddress, dstPort, "/").toURI());

            Map<String, Double> amounts = new HashMap<String, Double>();
            for (TransactionOutput output: transaction.getOutputs()) {
                Address address = output.getAddressFromP2PKHScript(params);
                if (address == null) {
                    address = output.getAddressFromP2SH(params);
                }
                if (address != null) {
                    if (address.isP2SHAddress()) {
                        address = new Address(dstParams, dstParams.getP2SHHeader(), address.getHash160());
                    } else {
                        address = new Address(dstParams, dstParams.getAddressHeader(), address.getHash160());
                    }
                    amounts.put(address.toBase58(),
                            (double)output.getValue().getValue() / Coin.COIN.getValue());
                }
            }

            JSONObject json = sendmany(amounts);
            System.err.println(json.toJSONString());

            post.setEntity(new StringEntity(json.toJSONString(), StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = client.execute(post)) {
                System.err.println("response: " + response);
                System.err.println("response: " + EntityUtils.toString(response.getEntity()));
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private static JSONObject sendmany(Map<String, Double> amountsMap)
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
