package com.example.pocwaiters.waitersAws;

import java.net.URI;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.GetChangeRequest;
import software.amazon.awssdk.services.route53.model.GetChangeResponse;
import software.amazon.awssdk.services.route53.waiters.Route53Waiter;

public class Route53Test {

  private static final String ENDPOINT_URL = "http://localhost:4566";


  public void test(){
    Route53Client route53Client = Route53Client.builder()
        .region(Region.US_EAST_1)
        .endpointOverride(URI.create(ENDPOINT_URL))
        .build();

    String resourceId = "resourceId";

    GetChangeRequest getChangeRequest = GetChangeRequest.builder().id(resourceId).build();

    Route53Waiter route53Waiter = route53Client.waiter();
    WaiterResponse<GetChangeResponse> waiterResponse = route53Waiter.waitUntilResourceRecordSetsChanged(getChangeRequest);

    waiterResponse.matched().response().ifPresent(System.out::println);
    System.out.println(resourceId + " is ready to use");
  }

}
