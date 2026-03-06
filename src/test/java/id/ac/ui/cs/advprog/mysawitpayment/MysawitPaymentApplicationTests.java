package id.ac.ui.cs.advprog.mysawitpayment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class MysawitPaymentApplicationTests {

	@Test
	void contextLoads() {
		MysawitPaymentApplication.main(new String[] {});
		assertTrue(true, "Main method successfully executed");
	}

}
