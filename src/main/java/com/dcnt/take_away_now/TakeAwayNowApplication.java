package com.dcnt.take_away_now;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("com/dcnt/take_away_now/domain")
public class TakeAwayNowApplication {

	public static void main(String[] args) {
		SpringApplication.run(TakeAwayNowApplication.class, args);
	}

}
