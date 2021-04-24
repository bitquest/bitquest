package com.bitquest.bitquest;

import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException.MnemonicLengthException;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.IOException;
import java.security.SecureRandom;
import java.sql.SQLException;

public class BitQuestTest {

    @Test
    public void testWallet() throws SQLException, IOException, ParseException, java.text.ParseException, MnemonicLengthException {
        final String db_url = "jdbc:postgresql://localhost:5432/bitquest";
        // generate new mnemonic code
        MnemonicCode mnemonicCode = new MnemonicCode();
        // Create some random entropy.
        SecureRandom random = new SecureRandom();
        byte[] entropy = random.generateSeed(20);
        System.out.println(mnemonicCode.toMnemonic(entropy));
    }
}
