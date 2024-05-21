package com.example.pocwaiters.waitersAws;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderAsyncClient;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateResourceServerRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateResourceServerResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateUserPoolClientRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateUserPoolClientResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateUserPoolDomainRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateUserPoolDomainResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateUserPoolRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateUserPoolResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CustomDomainConfigType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.OAuthFlowType;


/**
 * Given that this is using localstack to test its needed to pay localstack pro
 * otherwise the response of the service will be "API for service 'cognito-idp' not yet implemented or pro feature "
 */
@Component
public class CognitoTest {

  private static final String ENDPOINT_URL = "http://localhost:4566";


  public void test(){
    String userPoolName = "test";

    CognitoIdentityProviderAsyncClient cognitoProviderClientAsync = CognitoIdentityProviderAsyncClient.builder()
        .region(Region.US_EAST_1)
        .endpointOverride(URI.create(ENDPOINT_URL))
        .build();

    CognitoIdentityProviderClient cognitoProviderClient = CognitoIdentityProviderClient.builder()
        .region(Region.US_EAST_1)
        .endpointOverride(URI.create(ENDPOINT_URL))
        .build();


    //String idAsync = createPoolAsync(cognitoProviderClientAsync, userPoolName);
    String idSync =  createPoolSync(cognitoProviderClient, userPoolName);

   // System.out.println("User pool ID: " + idAsync);
    System.out.println("User pool ID Sync: " + idSync);
  }

  /**
   * This version will take some seconds until resource is created then we can verified using the completable future
   *
   * ths operation does not block the main thread, instead of returns a "CompletableFuture" which will notify
   * once the operation has finished
   * @param cognitoClient
   * @param userPoolName
   * @return
   */
  public static String createPoolAsync(CognitoIdentityProviderAsyncClient cognitoClient, String userPoolName) {
    try {
      CreateUserPoolRequest request = CreateUserPoolRequest.builder()
          .poolName(userPoolName)
          .build();

      CompletableFuture<CreateUserPoolResponse> response = cognitoClient.createUserPool(request);
      while (!response.isDone()) Thread.sleep(1000);

      return response.get().userPool().id();
    } catch (CognitoIdentityProviderException e) {
      System.err.println(e.awsErrorDetails().errorMessage());
      System.exit(1);
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(e);
    }
    return "";
  }

  /**
   * This version can take some seconds until resource is created
   * The thread is getting held until the resource is created
   *
   * El hilo desde el cual se llama a createUserPool se bloqueará hasta que la operación complete y se
   * reciba una respuesta del servicio de AWS Cognito.
   * @param cognitoClient
   * @param userPoolName
   * @return
   */
  public static String createPoolSync(CognitoIdentityProviderClient cognitoClient, String userPoolName) {
    try {
      CreateUserPoolRequest request = CreateUserPoolRequest.builder()
          .poolName(userPoolName)
          .build();

      CreateUserPoolResponse response = cognitoClient.createUserPool(request);
      return response.userPool().id();
    } catch (CognitoIdentityProviderException e) {
      System.err.println(e.awsErrorDetails().errorMessage());
      System.exit(1);
    }
    return "";
  }

  public static String createUserPoolResourceServer(CognitoIdentityProviderClient cognitoClient, String userPoolName) {
    try {
      CreateResourceServerRequest request = CreateResourceServerRequest.builder()
          .name(userPoolName)
          .userPoolId("UserPoolId")
          .identifier("identifier")
          .build();

      CreateResourceServerResponse response = cognitoClient.createResourceServer(request);
      if (response.sdkHttpResponse().isSuccessful()) return response.resourceServer().toString();
    } catch (CognitoIdentityProviderException e) {
      System.err.println(e.awsErrorDetails().errorMessage());
      System.exit(1);
    }
    return "";
  }

  public static String createUserPoolClient(CognitoIdentityProviderClient cognitoClient, String userPoolName) {
    try {
      CreateUserPoolClientRequest request = CreateUserPoolClientRequest.builder()
          .userPoolId("UserPoolId")
          .allowedOAuthFlows(OAuthFlowType.CLIENT_CREDENTIALS)
          .build();

      CreateUserPoolClientResponse response = cognitoClient.createUserPoolClient(request);
      if (response.sdkHttpResponse().isSuccessful()) return response.userPoolClient().clientId();
    } catch (CognitoIdentityProviderException e) {
      System.err.println(e.awsErrorDetails().errorMessage());
      System.exit(1);
    }
    return "";
  }

  public static String createUserPoolDomain(CognitoIdentityProviderClient cognitoClient, String userPoolId) {
    try {
      CreateUserPoolDomainRequest createUserPoolDomainRequest = CreateUserPoolDomainRequest.builder()
          .userPoolId(userPoolId)
          .customDomainConfig(CustomDomainConfigType.builder().build())
          .build();

      CreateUserPoolDomainResponse response = cognitoClient.createUserPoolDomain(createUserPoolDomainRequest);
      if (response.sdkHttpResponse().isSuccessful()) return response.cloudFrontDomain();
    } catch (CognitoIdentityProviderException e) {
      System.err.println(e.awsErrorDetails().errorMessage());
      System.exit(1);
    }
    return "";
  }

}
