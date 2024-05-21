package com.example.pocwaiters.waitersAws;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbAsyncWaiter;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

@Component
public class DynamoDbTest {

  private static final String ENDPOINT_URL = "http://localhost:4566";


  public void test(){
    DynamoDbClient dynamo = DynamoDbClient.builder()
        .region(Region.US_EAST_1)
        .endpointOverride(URI.create(ENDPOINT_URL))
        .build();

    syncCreateDynamodbTable(dynamo);
  }

  public void test2(){
    DynamoDbAsyncClient dynamo = DynamoDbAsyncClient.builder()
        .region(Region.US_EAST_1)
        .endpointOverride(URI.create(ENDPOINT_URL))
        .build();

    asyncCreateDynamodbTable(dynamo);
  }

  public void syncCreateDynamodbTable(DynamoDbClient dynamo){

    DynamoDbWaiter waiter = dynamo.waiter();

    WaiterResponse<DescribeTableResponse> waiterResponse =
        waiter.waitUntilTableExists(r -> r.tableName("myTable"));

    // print out the matched response with a tableStatus of ACTIVE
    waiterResponse.matched().response().ifPresent(System.out::println);
  }

  public void asyncCreateDynamodbTable(DynamoDbAsyncClient dynamo){

    DynamoDbAsyncClient asyncDynamo = DynamoDbAsyncClient.create();
    DynamoDbAsyncWaiter asyncWaiter = asyncDynamo.waiter();

    CompletableFuture<WaiterResponse<DescribeTableResponse>> waiterResponse =
        asyncWaiter.waitUntilTableNotExists(r -> r.tableName("myTable"));

    waiterResponse.whenComplete((r, t) -> {
      if (t == null) {
        // print out the matched ResourceNotFoundException
        r.matched().exception().ifPresent(System.out::println);
      }
    }).join();
  }



}
