package com.example.pocwaiters.waitersAws;

import java.net.URI;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.ComparisonOperator;
import software.amazon.awssdk.services.cloudwatch.model.DescribeAlarmsRequest;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricAlarmRequest;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;
import software.amazon.awssdk.services.cloudwatch.model.Statistic;
import software.amazon.awssdk.services.cloudwatch.waiters.CloudWatchWaiter;

@Component
public class CloudWatchTest {

  private static final String ENDPOINT_URL = "http://localhost:4566";


  public void test(){
    CloudWatchClient cloudWatchClient = CloudWatchClient.builder()
        .region(Region.US_EAST_1)
        .endpointOverride(URI.create(ENDPOINT_URL))
        .build();

    Dimension dimension = Dimension.builder()
        .name("InstanceId")
        .value("instanceId")
        .build();

    createAlarm(cloudWatchClient, dimension);
  }

  public void createAlarm(CloudWatchClient cloudWatchClient, Dimension dimension){
    String alarmName = "alarmName";

    PutMetricAlarmRequest request = PutMetricAlarmRequest.builder()
        .alarmName(alarmName)
        .comparisonOperator(
            ComparisonOperator.GREATER_THAN_THRESHOLD)
        .evaluationPeriods(1)
        .metricName("CPUUtilization")
        .namespace("AWS/EC2")
        .period(60)
        .statistic(Statistic.AVERAGE)
        .threshold(70.0)
        .actionsEnabled(false)
        .alarmDescription(
            "Alarm when server CPU utilization exceeds 70%")
        .unit(StandardUnit.SECONDS)
        .dimensions(dimension)
        .build();

    cloudWatchClient.putMetricAlarm(request);

    CloudWatchWaiter cloudWatchWaiter = cloudWatchClient.waiter();

    DescribeAlarmsRequest describeAlarmsRequest =DescribeAlarmsRequest.builder().build();

    cloudWatchWaiter.waitUntilAlarmExists(describeAlarmsRequest).matched().response().ifPresent(System.out::println);
    System.out.println(alarmName + " is ready to use");
  }

}
