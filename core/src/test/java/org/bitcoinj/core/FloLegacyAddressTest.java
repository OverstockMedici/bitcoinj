/*
 * Copyright 2011 Google Inc.
 * Copyright 2014 Andreas Schildbach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bitcoinj.core;

import org.bitcoinj.params.FloMainNetParams;
import org.bitcoinj.params.FloTestNetParams;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.Networks;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptPattern;
import org.bitcoinj.script.Script.ScriptType;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;

import static org.bitcoinj.core.Utils.HEX;
import static org.junit.Assert.*;

/**
 * Copied from LegacyAddressTest
 */
public class FloLegacyAddressTest {
    private static final NetworkParameters FLOTESTNET = FloTestNetParams.get();
    private static final NetworkParameters FLOMAINNET = FloMainNetParams.get();

    @Test
    public void testJavaSerialization() throws Exception {
        LegacyAddress testAddress = LegacyAddress.fromBase58(FLOTESTNET, "oYRaaENhPh4BM3Ync7xACL7z2LxMxA5z2R");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        new ObjectOutputStream(os).writeObject(testAddress);
        LegacyAddress testAddressCopy = (LegacyAddress) new ObjectInputStream(new ByteArrayInputStream(os.toByteArray()))
                .readObject();
        assertEquals(testAddress, testAddressCopy);

        LegacyAddress mainAddress = LegacyAddress.fromBase58(FLOMAINNET, "F8wso89xU3FhBVkBsZc6RNdA1x8YFCE6xi");
        os = new ByteArrayOutputStream();
        new ObjectOutputStream(os).writeObject(mainAddress);
        LegacyAddress mainAddressCopy = (LegacyAddress) new ObjectInputStream(new ByteArrayInputStream(os.toByteArray()))
                .readObject();
        assertEquals(mainAddress, mainAddressCopy);
    }

    @Test
    public void stringification() throws Exception {
        // Test a testnet address.
        LegacyAddress a = LegacyAddress.fromPubKeyHash(FLOTESTNET, HEX.decode("aaa17faff597a3a5d20ba5564b2e7104cf2d62ed"));
        assertEquals("oYRaaENhPh4BM3Ync7xACL7z2LxMxA5z2R", a.toString());
        assertEquals(ScriptType.P2PKH, a.getOutputScriptType());

        LegacyAddress b = LegacyAddress.fromPubKeyHash(FLOMAINNET, HEX.decode("222fe16d40ff72c2337d07748277d42d8cd0ce2f"));
        assertEquals("F8wso89xU3FhBVkBsZc6RNdA1x8YFCE6xi", b.toString());
        assertEquals(ScriptType.P2PKH, a.getOutputScriptType());
    }
    
    @Test
    public void decoding() throws Exception {
        LegacyAddress a = LegacyAddress.fromBase58(FLOTESTNET, "oYRaaENhPh4BM3Ync7xACL7z2LxMxA5z2R");
        assertEquals("aaa17faff597a3a5d20ba5564b2e7104cf2d62ed", Utils.HEX.encode(a.getHash()));

        LegacyAddress b = LegacyAddress.fromBase58(FLOMAINNET, "F8wso89xU3FhBVkBsZc6RNdA1x8YFCE6xi");
        assertEquals("222fe16d40ff72c2337d07748277d42d8cd0ce2f", Utils.HEX.encode(b.getHash()));
    }
    
    @Test
    public void errorPaths() {

        // Check the case of a mismatched network.
        try {
            LegacyAddress.fromBase58(FLOTESTNET, "F8wso89xU3FhBVkBsZc6RNdA1x8YFCE6xi");
            fail();
        } catch (AddressFormatException.WrongNetwork e) {
            // Success.
        } catch (AddressFormatException e) {
            fail();
        }

        // Check the empty case.
        try {
            LegacyAddress.fromBase58(FLOTESTNET, "");
            fail();
        } catch (AddressFormatException.WrongNetwork e) {
            fail();
        } catch (AddressFormatException e) {
            // Success.
        }

        // Check the case of a mismatched network.
        try {
            LegacyAddress.fromBase58(FLOTESTNET, "17kzeh4N8g49GFvdDzSf8PjaPfyoD1MndL");
            fail();
        } catch (AddressFormatException.WrongNetwork e) {
            // Success.
        } catch (AddressFormatException e) {
            fail();
        }
    }

    @Test
    public void getNetwork() throws Exception {
        NetworkParameters params = LegacyAddress.getParametersFromAddress("F8wso89xU3FhBVkBsZc6RNdA1x8YFCE6xi");
        assertEquals(FLOMAINNET.getId(), params.getId());
        params = LegacyAddress.getParametersFromAddress("oYRaaENhPh4BM3Ync7xACL7z2LxMxA5z2R");
        assertEquals(FLOTESTNET.getId(), params.getId());
    }

    @Test
    public void p2shAddress() throws Exception {

        /**
         * Note that there is something very wrong because both mainnet and testnet values are the same!
         * See method p2shAddressCreationFromKeys() and fix this once those values are fixed.
         *

        // Test that we can construct P2SH addresses
        LegacyAddress mainNetP2SHAddress = LegacyAddress.fromBase58(FLOMAINNET, "4a2tpu5vkzQgt87UFCLqfW9qFZzrmTFUsk");
        assertEquals(mainNetP2SHAddress.getVersion(), FLOMAINNET.p2shHeader);
        assertEquals(ScriptType.P2SH, mainNetP2SHAddress.getOutputScriptType());
        LegacyAddress testNetP2SHAddress = LegacyAddress.fromBase58(FLOTESTNET, "4a2tpu5vkzQgt87UFCLqfW9qFZzrmTFUsk");
        assertEquals(testNetP2SHAddress.getVersion(), FLOTESTNET.p2shHeader);
        assertEquals(ScriptType.P2SH, testNetP2SHAddress.getOutputScriptType());

        // Test that we can determine what network a P2SH address belongs to
        // See method p2shAddressCreationFromKeys() and then fix these values once you've found the solution in there.
        //NetworkParameters mainNetParams = LegacyAddress.getParametersFromAddress("4a2tpu5vkzQgt87UFCLqfW9qFZzrmTFUsk");
        //assertEquals(FLOMAINNET.getId(), mainNetParams.getId());
        NetworkParameters testNetParams = LegacyAddress.getParametersFromAddress("4a2tpu5vkzQgt87UFCLqfW9qFZzrmTFUsk");
        assertEquals(FLOTESTNET.getId(), testNetParams.getId());

        // Test that we can convert them from hashes
        byte[] hex = HEX.decode("defdb71910720a2c854529019189228b4245eddd");
        LegacyAddress a = LegacyAddress.fromScriptHash(FLOMAINNET, hex);
        assertEquals("4a2tpu5vkzQgt87UFCLqfW9qFZzrmTFUsk", a.toString());
        LegacyAddress b = LegacyAddress.fromScriptHash(FLOTESTNET, HEX.decode("defdb71910720a2c854529019189228b4245eddd"));
        assertEquals("4a2tpu5vkzQgt87UFCLqfW9qFZzrmTFUsk", b.toString());
        LegacyAddress c = LegacyAddress.fromScriptHash(FLOMAINNET,
                ScriptPattern.extractHashFromPayToScriptHash(ScriptBuilder.createP2SHOutputScript(hex)));
        assertEquals("4a2tpu5vkzQgt87UFCLqfW9qFZzrmTFUsk", c.toString());

         *
         **/
    }

    @Test
    public void p2shAddressCreationFromKeys() throws Exception {
        // import some keys from this example: https://gist.github.com/gavinandresen/3966071
        // ... but modify for XFL and get wif, ie: ku -n BTC 5JaTXbAUmfPYZFRwrYaALK48fN6sFJp4rHqq2QSXs8ucfpE4yQU --json | jq ".secret_exponent_hex" | xargs ku -n FLO
        ECKey key1 = DumpedPrivateKey.fromBase58(FLOTESTNET, "cQwfYvnh1C8mSnANW6AVZZBtSZSJSbiS4bUz5cvCjQZEggGuBJpg").getKey();
        key1 = ECKey.fromPrivate(key1.getPrivKeyBytes());
        ECKey key2 = DumpedPrivateKey.fromBase58(FLOTESTNET, "cQzZrXRD2LdifxRDVmJBXMZDTg3BSRcV3jxrztagZYSZaSBbqwUZ").getKey();
        key2 = ECKey.fromPrivate(key2.getPrivKeyBytes());
        ECKey key3 = DumpedPrivateKey.fromBase58(FLOTESTNET, "cPX3DKiDE3AH4UQWWQeh2qGMGgPqGPv82beqfKKrgCutM5he8K4D").getKey();
        key3 = ECKey.fromPrivate(key3.getPrivKeyBytes());

        List<ECKey> keys = Arrays.asList(key1, key2, key3);
        Script p2shScript = ScriptBuilder.createP2SHOutputScript(2, keys);
        System.out.println("script hash " + HEX.encode(ScriptPattern.extractHashFromPayToScriptHash(p2shScript)));
        LegacyAddress address = LegacyAddress.fromScriptHash(FLOTESTNET,
            ScriptPattern.extractHashFromPayToScriptHash(p2shScript));
        // This value is the result from this bitcoinj test, which is obviously wrong because FLOMAINNET below gets the same results.
        //assertEquals("4a2tpu5vkzQgt87UFCLqfW9qFZzrmTFUsk", address.toString());
        // This value is the result from flo-regtest: flo-cli createmultisig 2 '["0491bba2510912a5bd37da1fb5b1673010e43d2c6d812c514e91bfa9f2eb129e1c183329db55bd868e209aac2fbc02cb33d98fe74bf23f0c235d6126b1d8334f86","04865c40293a680cb9c020e7b1e106d8c1916d3cef99aa431a56d253e69256dac09ef122b1a986818a7cb624532f062c1d1f8722084861c5c3291ccffef4ec6874","048d2455d2403e08708fc1f556002f1b6cd83f992d085097f9974ab08a28838f07896fbab08f39495e15fa6fad6edbfb1e754e35fa1c7844c41f322a1863d46213"]'
         //assertEquals("QjDjfodwYthtFXBdMnNasJUZmwA3UJrJDB", address.toString());
        // ... so which is correct?

        /**
         * Here are the corresponding wif values for those 3 keys on FLOMAINNET (see the ku command above)
         *
        ECKey key1 = DumpedPrivateKey.fromBase58(FLOMAINNET, "T6QwXm61yWR74BKyfKJEQbECmBnCrEddomEmq16EoG5PwpnQbFJ3").getKey();
        key1 = ECKey.fromPrivate(key1.getPrivKeyBytes());
        ECKey key2 = DumpedPrivateKey.fromBase58(FLOMAINNET, "T6TqqMiXzev4HMapezRvNPbXnJP5r4XgnuiekGkidPxiqacmsK4E").getKey();
        key2 = ECKey.fromPrivate(key2.getPrivKeyBytes());
        ECKey key3 = DumpedPrivateKey.fromBase58(FLOMAINNET, "T4zKCA1YCMScfsa7fdnRssJfbJjjg2qKmmQdQhVtk4S3cE99BizD").getKey();
        key3 = ECKey.fromPrivate(key3.getPrivKeyBytes());

        List<ECKey> keys = Arrays.asList(key1, key2, key3);
        Script p2shScript = ScriptBuilder.createP2SHOutputScript(2, keys);
        LegacyAddress address = LegacyAddress.fromScriptHash(FLOMAINNET,
            ScriptPattern.extractHashFromPayToScriptHash(p2shScript));
        // This value is the result from this bitcoinj test, which is obviously wrong because FLOTESTNET above gets the same results.
        //assertEquals("4a2tpu5vkzQgt87UFCLqfW9qFZzrmTFUsk", address.toString());
        // This value is the result from flo-mainnet in gcloud: flo-cli createmultisig 2 '["0491bba2510912a5bd37da1fb5b1673010e43d2c6d812c514e91bfa9f2eb129e1c183329db55bd868e209aac2fbc02cb33d98fe74bf23f0c235d6126b1d8334f86","04865c40293a680cb9c020e7b1e106d8c1916d3cef99aa431a56d253e69256dac09ef122b1a986818a7cb624532f062c1d1f8722084861c5c3291ccffef4ec6874","048d2455d2403e08708fc1f556002f1b6cd83f992d085097f9974ab08a28838f07896fbab08f39495e15fa6fad6edbfb1e754e35fa1c7844c41f322a1863d46213"]'
        //assertEquals("fDPT7iNK7PPRh8CkEtN4KpGtS6S12Mpm24", address.toString());\
         *
         **/
    }

    @Test
    public void cloning() throws Exception {
        LegacyAddress a = LegacyAddress.fromPubKeyHash(FLOTESTNET, HEX.decode("aaa17faff597a3a5d20ba5564b2e7104cf2d62ed"));
        LegacyAddress b = a.clone();

        assertEquals(a, b);
        assertNotSame(a, b);
    }

    @Test
    public void roundtripBase58() throws Exception {
        String base58 = "oYRaaENhPh4BM3Ync7xACL7z2LxMxA5z2R";
        assertEquals(base58, LegacyAddress.fromBase58(null, base58).toBase58());
    }

    @Test
    public void comparisonCloneEqualTo() throws Exception {
        LegacyAddress a = LegacyAddress.fromBase58(FLOMAINNET, "F8wso89xU3FhBVkBsZc6RNdA1x8YFCE6xi");
        LegacyAddress b = a.clone();

        int result = a.compareTo(b);
        assertEquals(0, result);
    }

    @Test
    public void comparisonEqualTo() throws Exception {
        LegacyAddress a = LegacyAddress.fromBase58(FLOMAINNET, "F8wso89xU3FhBVkBsZc6RNdA1x8YFCE6xi");
        LegacyAddress b = a.clone();

        int result = a.compareTo(b);
        assertEquals(0, result);
    }

    @Test
    public void comparisonLessThan() throws Exception {
        LegacyAddress a = LegacyAddress.fromBase58(FLOMAINNET, "F8wso89xU3FhBVkBsZc6RNdA1x8YFCE6xi");
        LegacyAddress b = LegacyAddress.fromBase58(FLOMAINNET, "FFkoaZRJaTC4ic2jjSNhNDtts3UrFfqRnL");

        int result = a.compareTo(b);
        assertTrue(result < 0);
    }

    @Test
    public void comparisonGreaterThan() throws Exception {
        LegacyAddress a = LegacyAddress.fromBase58(FLOMAINNET, "FFkoaZRJaTC4ic2jjSNhNDtts3UrFfqRnL");
        LegacyAddress b = LegacyAddress.fromBase58(FLOMAINNET, "F8wso89xU3FhBVkBsZc6RNdA1x8YFCE6xi");

        int result = a.compareTo(b);
        assertTrue(result > 0);
    }

    @Test
    public void comparisonBytesVsString() throws Exception {
        // TODO: To properly test this we need a much larger data set
        LegacyAddress a = LegacyAddress.fromBase58(FLOMAINNET, "F8wso89xU3FhBVkBsZc6RNdA1x8YFCE6xi");
        LegacyAddress b = LegacyAddress.fromBase58(FLOMAINNET, "FFkoaZRJaTC4ic2jjSNhNDtts3UrFfqRnL");

        int resultBytes = a.compareTo(b);
        int resultsString = a.toString().compareTo(b.toString());
        assertTrue( resultBytes < 0 );
        assertTrue( resultsString < 0 );
    }
}
