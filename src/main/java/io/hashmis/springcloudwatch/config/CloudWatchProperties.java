package io.hashmis.springcloudwatch.config;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.actuate.autoconfigure.metrics.export.properties.StepRegistryProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import java.time.Duration;

/**
 * A configuration class that configures a metrics registry to push aggregated metrics on Cloud Watch on a regular
 * interval.
 * 
 * @author hashmis
 */
@Getter
@Setter
@Component
@ConfigurationProperties("cloudwatch")
public class CloudWatchProperties extends StepRegistryProperties {

    private String namespace = "";
    private boolean enabled = true;

    @Bean
    public Clock micrometerClock() {
        return Clock.SYSTEM;
    }

    @Bean
    public CloudWatchAsyncClient cloudWatchAsyncClient() {
        return CloudWatchAsyncClient.builder().build();
    }

    @Bean
    public CloudWatchMeterRegistry cloudWatchMeterRegistry(CloudWatchConfig config, Clock clock,
            CloudWatchAsyncClient client) {
        return new CloudWatchMeterRegistry(config, clock, client);
    }

    @Bean
    public CloudWatchConfig cloudWatchConfig(CloudWatchProperties properties) {
        return new CloudWatchConfig() {
            @Override
            public String prefix() {
                return "cloudwatch";
            }

            @Override
            public String namespace() {
                return properties.getNamespace();
            }

            @Override
            public Duration step() {
                return properties.getStep();
            }

            @Override
            public boolean enabled() {
                return properties.isEnabled();
            }

            @Override
            public int batchSize() {
                return properties.getBatchSize();
            }

            @Override
            public String get(String s) {
                return null;
            }
        };
    }
}