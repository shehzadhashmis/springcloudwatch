# A project for capturing cloudwatch metrics in Spring Boot.
This project is a demonstration of connecting a Spring Boot application with  Amazon Cloudwatch and start sending application metrics using micrometer.
![AWS-cloudwatch](https://user-images.githubusercontent.com/91077741/134038916-51cbb008-5fef-4693-a2be-a86773ff8bdd.jpg)


# How to run it?
1. Checkout this branch
2. In order to run this application, please arrange an active AWS account
3. Configure the following properties in environment variables
   **`CLOUDWATCH_ENABLED=true
   ENV_NAME=production
   AWS_REGION=XXXXXXXXX
   AWS_ACCESS_KEY_ID=AWS_ACESS_KEY_ID_XXXXXXXXXXX
   AWS_SECRET_ACCESS_KEY=AWS_SECRET_ACCESS_KEY_XXXXXXXXXXX
   CLOUDWATCH_STEP=10s
   CLOUDWATCH_NUM_THREADS=10
   CLOUDWATCH_BATCH_SIZE=10`**
4. `mvn clean install`
5. Run as Spring Boot application
