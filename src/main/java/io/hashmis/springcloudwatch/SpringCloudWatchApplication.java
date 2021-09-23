package io.hashmis.springcloudwatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * A spring boot application for publishing metrics to AWS Cloud Watch.
 *
 * @author shehzadhashmis
 */
@SpringBootApplication
public class SpringCloudWatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudWatchApplication.class, args);
    }
}
