package pheonix.classconnect.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import pheonix.classconnect.backend.security.config.AimProperties;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableConfigurationProperties({AimProperties.class})
@EnableScheduling
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
