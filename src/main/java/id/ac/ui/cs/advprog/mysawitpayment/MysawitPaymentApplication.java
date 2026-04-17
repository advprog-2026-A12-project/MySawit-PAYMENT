package id.ac.ui.cs.advprog.mysawitpayment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class MysawitPaymentApplication {

	public static void main(String[] args) {
		SpringApplication.run(MysawitPaymentApplication.class, args);
	}

}
