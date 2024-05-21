package com.example.pocwaiters.waitersAws;

import java.net.URI;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogGroupRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogGroupResponse;

@Component
public class LogsGroupTest {


  private static final String ENDPOINT_URL = "http://localhost:4566";


  public void test(){
    String logGroupName = "logGroup-name";
    CloudWatchLogsClient cloudWatchLogsClient = CloudWatchLogsClient.builder()
        .region(Region.US_EAST_1)
        .endpointOverride(URI.create(ENDPOINT_URL))
        .build();

    createLogGroup(cloudWatchLogsClient, logGroupName);
  }

  /**
   * We can validate if the response was success or not depending of the http code which returns back the request
   * @param cloudWatchLogsClient
   * @param logGroupName
   */
  public void createLogGroup(CloudWatchLogsClient cloudWatchLogsClient, String logGroupName){

    CreateLogGroupRequest createLogGroupRequest = CreateLogGroupRequest.builder().logGroupName(logGroupName).build();
    CreateLogGroupResponse createLogGroupResponse = cloudWatchLogsClient.createLogGroup(createLogGroupRequest);
    if(!createLogGroupResponse.sdkHttpResponse().isSuccessful()){
        throw new RuntimeException();
    }
  }

}
