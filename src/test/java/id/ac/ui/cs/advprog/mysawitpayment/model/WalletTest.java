package id.ac.ui.cs.advprog.mysawitpayment.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WalletTest {

    @Test
    void wallet_settersAndGetters_workCorrectly() {
        Wallet wallet = new Wallet();

        wallet.setId(1L);
        wallet.setUserId(10L);
        wallet.setBalance(500.0);

        assertEquals(1L, wallet.getId());
        assertEquals(10L, wallet.getUserId());
        assertEquals(500.0, wallet.getBalance());
    }

    @Test
    void wallet_defaultConstructor_createsObject() {
        Wallet wallet = new Wallet();
        assertNotNull(wallet);
    }
}