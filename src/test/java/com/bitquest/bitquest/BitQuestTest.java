package com.bitquest.bitquest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
    // Test Node
    System.out.println(node.getBlockchainInfo());
    // Test wallet
    Wallet alice = new Wallet(node, "alice");
    int minimumConfirmations = 3;
    System.out.println(alice.balance(minimumConfirmations));
    System.out.println(alice.address());
    System.out.println(alice.addressUrl());
    Wallet bob = new Wallet(node, "bob");
    System.out.println(bob.balance(minimumConfirmations));
    System.out.println(bob.address());
    System.out.println(bob.addressUrl());
    Double amount = 10.0;
    if (alice.balance(minimumConfirmations) > amount) {
      alice.send(bob.address(), amount);
    } else if (bob.balance(minimumConfirmations) > amount) {
      bob.send(alice.address(), amount);
    }
  }
  
  @Test
  public void testLandOwnership() throws Exception {
    Class.forName("org.postgresql.Driver");
    Connection conn = DriverManager.getConnection(BitQuest.databaseUrl(), BitQuest.POSTGRES_USER, BitQuest.POSTGRES_PASSWORD);
    Land land = new Land(conn);
    land.runMigrations();
    String uuid = "63d9719e-571a-4963-ac11-2f1233393580";
    int x = 0;
    int z = 0;
    LandChunk chunk = land.chunk(x, z);
    if (chunk == null) {
      land.claim(x, z, uuid, "Land");
    } else {
      System.out.println(chunk.name);
      System.out.println(chunk.permission);
      System.out.println(chunk.owner);
      if (chunk.permission == ChunkPermission.PRIVATE) {
        land.changePermission(x, z, ChunkPermission.CLAN);
      } else if (chunk.permission == ChunkPermission.CLAN) {
        land.changePermission(x, z, ChunkPermission.PUBLIC);
      } else {
        land.changePermission(x, z, ChunkPermission.PRIVATE);
      }
      if (chunk.name.equals("Good Land")) {
        land.rename(x, z, "Bad Land");
      } else {
        land.rename(x, z, "Good Land");
      }
    }
  }

  @Test
  public void testClans() throws Exception {
    Class.forName("org.postgresql.Driver");
    Connection conn = DriverManager.getConnection(BitQuest.databaseUrl(), BitQuest.POSTGRES_USER, BitQuest.POSTGRES_PASSWORD);
    Population players = new Population(conn);
    players.runMigrations();
    String sql = "DELETE FROM players";
    System.out.println(sql);
    PreparedStatement ps = conn.prepareStatement(sql);
    ps.executeUpdate();
    ps.close();
    String aliceId = "63d9719e-571a-4963-ac11-2f1233393580";
    BitQuestPlayer alice = players.player(aliceId);
    BitQuestPlayer bob = players.player("35714b7c-fde7-4ebd-af32-7fd106c881a0");
    System.out.println(alice.clan);
    assertFalse(alice.invitedToClan("FakeClan"));
    String clanName = "Clan";
    if (alice.clan == null) {
      assertFalse(alice.inviteToClan(bob));
      assertFalse(players.leaveClan(alice.uuid));
      assertFalse(players.joinClan(alice.uuid, clanName));
      assertTrue(players.createClan(alice.uuid, clanName));
      assertFalse(players.createClan(alice.uuid, clanName));
      alice = players.player(aliceId);
      assertFalse(bob.invitedToClan(clanName));
      assertFalse(players.joinClan(bob.uuid, clanName));
      assertTrue(alice.inviteToClan(bob));
      assertTrue(bob.invitedToClan(clanName));
      assertTrue(players.joinClan(bob.uuid, clanName));
      assertFalse(bob.invitedToClan(clanName));
    } else {
      assertFalse(players.joinClan(alice.uuid, clanName));
      assertTrue(players.leaveClan(alice.uuid));
    }
  }
}
