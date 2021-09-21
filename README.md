# A project for publishing AWS CloudWatch metrics in Spring Boot
This project is a demonstration of connecting a Spring Boot application with  Amazon Cloudwatch and start sending application metrics using micrometer.

![AWS-cloudwatch](https://user-images.githubusercontent.com/91077741/134038916-51cbb008-5fef-4693-a2be-a86773ff8bdd.jpg)

# Application Details
This is a Spring Boot application configured with Amazon Web Services (AWS) Cloud Watch to send application metrics. For this we have used micrometer, cloudwatch and cloudwatch2 dependencies.

## Maven Dependencies
```
<dependency>
   <groupId>io.micrometer</groupId>
   <artifactId>micrometer-core</artifactId>
   <version>1.5.1</version>
</dependency>
<dependency>
  <groupId>software.amazon.awssdk</groupId>
  <artifactId>aws-query-protocol</artifactId>
  <version>2.13.4</version>
</dependency>
<dependency>
  <groupId>software.amazon.awssdk</groupId>
  <artifactId>protocol-core</artifactId>
  <version>2.13.4</version>
</dependency>
<dependency>
  <groupId>software.amazon.awssdk</groupId>
  <artifactId>cloudwatch</artifactId>
  <version>2.13.4</version>
</dependency>
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-registry-cloudwatch2</artifactId>
  <version>1.5.1</version>
  <exclusions>
    <exclusion>
      <groupId>software.amazon.awssdk</groupId>
      <artifactId>cloudwatch</artifactId>
    </exclusion>
  </exclusions>
</dependency>
  ```

## application.properties file
We have used H2 database for this demo. The important properties are starting with prefix cloudwatch.
```
######### [Application Properties] ##################
server.port=8080
spring.application.name=cloudwatch

######### [H2 Database Properties] ##################
spring.datasource.url=jdbc:h2:mem:cloudwatchdemo
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

######### [AWS CloudWatch Properties] ##################
cloudwatch.enabled=${CLOUDWATCH_ENABLED}
cloudwatch.namespace=${spring.application.name}_${ENV_NAME}
cloudwatch.step=${CLOUDWATCH_STEP}
cloudwatch.numThreads=${CLOUDWATCH_NUM_THREADS}
cloudwatch.batchSize=${CLOUDWATCH_BATCH_SIZE}
cloud.aws.stack.auto=false
cloud.aws.region.static=${AWS_REGION}
```

## Environment Variables
The following properties need to be configured in application environment variables.
### CLOUDWATCH_ENABLED
We want to have a control where we can enable/disable cloudwatch. For this we have property with value true/false.
### ENV_NAME
This is the name of the environment where we need to configure cloudwatch. Its possible values can be dev, integration, sandbox or production.
### AWS_REGION
Specifies an AWS region associated with an IAM user or role.
### AWS_ACCESS_KEY_ID
Specifies an AWS access key associated with an IAM user or role.
### AWS_SECRET_ACCESS_KEY
Specifies an AWS secret access key associated with an IAM user or role.
### CLOUDWATCH_STEP
Number of seconds after which we need to publish metrics to cloudwatch i.e. 10s.
### CLOUDWATCH_NUM_THREADS
Number of threads used for cloudwatch metric publishing i.e. 10.
### CLOUDWATCH_BATCH_SIZE
The batch size used for cloudwatch metrics publishing  i.e. 10.

## Spring Configuration File
There is only one magical Spring configuration file that does all the trick. It creates and initializes the beans for the following classes.
```
io.micrometer.core.instrument.Clock
software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient
io.micrometer.cloudwatch2.CloudWatchMeterRegistry
io.micrometer.cloudwatch2.CloudWatchConfig
```

```
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
```

# How to run it?
1. Checkout this branch.
2. In order to run this application, please arrange an active AWS account.
3. Configure the following properties in environment variables.

```
CLOUDWATCH_ENABLED=true
ENV_NAME=production
AWS_REGION=XXXXXXXXX
AWS_ACCESS_KEY_ID=AWS_ACESS_KEY_ID_XXXXXXXXXXX
AWS_SECRET_ACCESS_KEY=AWS_SECRET_ACCESS_KEY_XXXXXXXXXXX
CLOUDWATCH_STEP=10s
CLOUDWATCH_NUM_THREADS=10
CLOUDWATCH_BATCH_SIZE=10
```

4. Execute the following maven command.
```
mvn clean install
```

6. Run as Spring Boot application.

## Debug Application
This application publishes metrics to cloudwatch which can be debugged by putting a break point in publish method of class [io.micrometer.cloudwatch2.CloudWatchMeterRegistry](https://github.com/micrometer-metrics/micrometer/blob/main/implementations/micrometer-registry-cloudwatch2/src/main/java/io/micrometer/cloudwatch2/CloudWatchMeterRegistry.java).
```
   @Override
    protected void publish() {
        boolean interrupted = false;
        try {
            for (List<MetricDatum> batch : MetricDatumPartition.partition(metricData(), config.batchSize())) {
                try {
                    sendMetricData(batch);
                } catch (InterruptedException ex) {
                    interrupted = true;
                }
            }
        }
        finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
```
<img width="1679" alt="Screen Shot 2021-09-21 at 10 02 08 AM" src="https://user-images.githubusercontent.com/91077741/134117249-735f4fa9-aa50-48b9-82ec-da00fe84f90d.png">


## Access CloudWatch From AWS Console
Login to AWS Console [https://console.aws.amazon.com](https://console.aws.amazon.com). Select CloudWatch from the available services and then from the left side panel expand Metrics where you can find All metrics.

<img width="1666" alt="Screen Shot 2021-09-21 at 9 34 50 AM" src="https://user-images.githubusercontent.com/91077741/134118172-bfb33548-857d-4760-8f85-e980e533441a.png">

Here you can see our custom namespace cloudwatch_production. Please click on it.

<img width="1679" alt="Screen Shot 2021-09-21 at 10 48 36 AM" src="https://user-images.githubusercontent.com/91077741/134118557-b25653a5-a4a1-4ef7-a215-5f89b7cb5a1e.png">


## Creating Dashboards
There are plenty of metrics that we can use and build dashboards based on the particular requirement or use case. Here are some of the dashboards that have been created.

<img width="1679" alt="Screen Shot 2021-09-21 at 10 05 28 AM" src="https://user-images.githubusercontent.com/91077741/134118334-7280a3ed-a4e2-47aa-840e-e74cf405e2d1.png">

<img width="1679" alt="Screen Shot 2021-09-21 at 10 06 20 AM" src="https://user-images.githubusercontent.com/91077741/134118345-c173289f-7496-4698-87c0-fba433fcd22a.png">

<img width="1679" alt="Screen Shot 2021-09-21 at 10 07 42 AM" src="https://user-images.githubusercontent.com/91077741/134118347-e8c82bab-bd2f-405e-a5cb-6a2d1fc30be4.png">

## Explore it more
Its very useful to use CloudWatch metrics to analyze application internals in terms of database connections, cpu processing, heap memory usage etc. so we can benefit from it and build more robust applications.
