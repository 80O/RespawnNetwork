package com.respawnnetwork.respawnlib.network.accounts;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests basic mojang account functionality.
 *
 * @author TomShar
 * @author spaceemotion
 * @version 1.0.1
 */
@RunWith(JUnit4.class)
public class AccountTest {
    public static final String NOTCH_NAME = "Notch";
    public static final String NOTCH_UUID = "069a79f444e94726a5befca90e38aaf5";


    @Test
    public void testUuid() {
        MojangAccount account = new MojangAccount(NOTCH_NAME);

        Assert.assertEquals("Notch account is invalid!", true, account.isValid());
        Assert.assertEquals("UUID mismatch", NOTCH_UUID, account.getUuid());
    }

    @Test
    public void testName() throws Exception {
        MojangAccount account = MojangAccount.fromUUID(NOTCH_UUID);

        Assert.assertEquals("Notch account is invalid!", true, account.isValid());
        Assert.assertEquals("Name mismatch", NOTCH_NAME, account.getName());
    }

}
