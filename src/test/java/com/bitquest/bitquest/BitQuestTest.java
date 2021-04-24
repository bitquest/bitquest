package com.bitquest.bitquest;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException.MnemonicLengthException;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.DeterministicSeed;
import org.json.simple.JSONObject;
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
        // Generating a new Wallet with Mnemonic code
        final String db_url = "jdbc:postgresql://localhost:5432/bitquest";
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
        JSONObject get_wallet_response=BlockCypher.get("wallets/hd/alice");
        System.out.println(get_wallet_response);
        if(get_wallet_response==null) {
            // Create new HD wallet in Blockcypher if it doesn't exist already
            final JSONObject create_wallet_params = new JSONObject();
            create_wallet_params.put("extended_public_key", xpub);
            create_wallet_params.put("name", "alice");
            JSONObject create_wallet_response=BlockCypher.post("wallets/hd",create_wallet_params);
            System.out.println(create_wallet_response);
    
            // derive address from pubkey
            final JSONObject derive_address_params = new JSONObject();
            JSONObject derive_address_response=BlockCypher.post("wallets/hd/alice/addresses/derive",derive_address_params);
            System.out.println(derive_address_params);
        }
        JSONObject balance_response=BlockCypher.get("addrs/alice/balance");
        System.out.println(balance_response);
        
    }
}
