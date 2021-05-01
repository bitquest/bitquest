package com.bitquest.bitquest;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException.MnemonicLengthException;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.DeterministicSeed;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.IOException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class BitQuestTest {

    @Test
    public void testWallet() throws SQLException, ParseException, java.text.ParseException, MnemonicLengthException, IOException {
        // generate new mnemonic code
        MnemonicCode mnemonicCode = new MnemonicCode();
        // Create some random entropy.
        SecureRandom random = new SecureRandom();
        byte[] entropy = random.generateSeed(20);

        List<String> mnemonic_words = mnemonicCode.toMnemonic(entropy);
        System.out.println(mnemonic_words);
        DeterministicSeed seed = new DeterministicSeed(entropy, "", 0);
        System.out.println(seed.toHexString());
        System.out.println(seed.getMnemonicCode());
        // Recovering a wallet with Mnemonic code
        List<String> backup_words = Arrays.asList("enroll","cover","practice","bullet","dad","surround","install","match","fault","dragon","innocent","blame","brown","bind","alcohol");
        seed = new DeterministicSeed(backup_words, null,"", 0);
        System.out.println(seed.toHexString());
        System.out.println(seed.getMnemonicCode());
        NetworkParameters network = TestNet3Params.get();
        DeterministicKey masterKey = HDKeyDerivation.createMasterPrivateKey(seed.getSeedBytes());
        String xpub = masterKey.serializePubB58( network );
        System.out.println(xpub);
        // Check if HD wallet exists
        Node node = new Node();
        node.host=System.getenv("BITQUEST_NODE_HOST");
        node.rpcUsername=System.getenv("BITQUEST_NODE_RPC_USER");
        node.rpcUsername=System.getenv("BITQUEST_NODE_RPC_USER");
        
    }
}
