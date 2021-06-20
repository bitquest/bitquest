package com.bitquest.bitquest;

import java.io.IOException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException.MnemonicLengthException;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.DeterministicSeed;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import redis.clients.jedis.Jedis;

public class BitQuestTest {

  @Test
  public void testWallet()
      throws Exception {
    // generate new mnemonic code
    MnemonicCode mnemonicCode = new MnemonicCode();
    // Create some random entropy.
    SecureRandom random = new SecureRandom();
    byte[] entropy = random.generateSeed(20);

    List<String> mnemonicWords = mnemonicCode.toMnemonic(entropy);
    System.out.println(mnemonicWords);
    DeterministicSeed seed = new DeterministicSeed(entropy, "", 0);
    System.out.println(seed.toHexString());
    System.out.println(seed.getMnemonicCode());
    // Recovering a wallet with Mnemonic code
    List<String> backupWords = Arrays
        .asList("enroll", "cover", "practice", "bullet", "dad", "surround", "install", "match",
            "fault", "dragon", "innocent", "blame", "brown", "bind", "alcohol");
    seed = new DeterministicSeed(backupWords, null, "", 0);
    System.out.println(seed.toHexString());
    System.out.println(seed.getMnemonicCode());
    NetworkParameters network = TestNet3Params.get();
    DeterministicKey masterKey = HDKeyDerivation.createMasterPrivateKey(seed.getSeedBytes());
    String xpub = masterKey.serializePubB58(network);
    System.out.println(xpub);
    Node node = new Node();
    node.host = BitQuest.NODE_HOST;
    node.port = BitQuest.NODE_PORT;
    node.rpcUsername = BitQuest.NODE_RPC_USERNAME;
    node.rpcPassword = BitQuest.NODE_RPC_PASSWORD;
    // Test wallet
    Wallet alice = new Wallet(node, "alice");
    int minimumConfirmations = 3;
    System.out.println(alice.balance(minimumConfirmations));
    System.out.println(alice.address());
    Wallet bob = new Wallet(node, "bob");
    System.out.println(bob.balance(minimumConfirmations));
    System.out.println(bob.address());
    Double amount = 100.0;
    if (alice.balance(minimumConfirmations) > amount) {
      alice.send(bob.address(), amount);
    } else if (bob.balance(minimumConfirmations) > amount) {
      bob.send(alice.address(), amount);
    }
  }
  @Test
  public void testLandOwnership() {
    Jedis redis = new Jedis(BitQuest.REDIS_HOST, BitQuest.REDIS_PORT);
    redis.flushAll();
  }
}
