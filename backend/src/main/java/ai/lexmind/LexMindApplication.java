package ai.lexmind;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * LexMind AI — application tier entrypoint.
 * Legal Intelligence, Case Analysis & Litigation Strategy Platform.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableAsync
public class LexMindApplication {

    public static void main(String[] args) {
        SpringApplication.run(LexMindApplication.class, args);
    }
}
