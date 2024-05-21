package com.example.pocwaiters.waitersAws;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretResponse;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;

@Component
public class SecretManagerTest {

  private static final String ENDPOINT_URL = "http://localhost:4566";

  public void test(){
    String secretName = "/secrets/idt/example/jarc";
    Region region = Region.US_EAST_1;
    SecretsManagerClient secretsClient = SecretsManagerClient.builder()
        .region(region)
        .endpointOverride(URI.create(ENDPOINT_URL))
        .build();
    SecretsManagerAsyncClient secretsManagerAsyncClient = SecretsManagerAsyncClient.builder().region(region)
        .endpointOverride(URI.create(ENDPOINT_URL))
        .build();

    createSecret(secretsClient, secretName);
    secretsClient.close();
  }

  public static void getValue(SecretsManagerClient secretsClient, String secretName) {
    try {
      GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
          .secretId(secretName)
          .build();

      GetSecretValueResponse valueResponse = secretsClient.getSecretValue(valueRequest);
      String secret = valueResponse.secretString();
      System.out.println(secret);

    } catch (SecretsManagerException e) {
      System.err.println(e.awsErrorDetails().errorMessage());
      System.exit(1);
    }
  }

  public static void createSecret(SecretsManagerClient secretsClient, String secretName) {
    try {
      CreateSecretRequest valueRequest = CreateSecretRequest.builder()
          .name(secretName)
          .secretString("secret test")
          .build();

      CreateSecretResponse valueResponse = secretsClient.createSecret(valueRequest);
      String secret = valueResponse.name();
      System.out.println(secret);

    } catch (SecretsManagerException e) {
      System.err.println(e.awsErrorDetails().errorMessage());
      System.exit(1);
    }
  }

  public static void createSecretAsync(SecretsManagerAsyncClient secretsClient, String secretName) {
    try {
      CreateSecretRequest valueRequest = CreateSecretRequest.builder()
          .name(secretName)
          .secretString("secret test")
          .build();

      CompletableFuture<CreateSecretResponse> valueResponse = secretsClient.createSecret(valueRequest);
      while (!valueResponse.isDone()) Thread.sleep(1000);
      String secret = valueResponse.get().name();
      String arn = valueResponse.get().arn();
      System.out.println(secret);
      System.out.println(arn);
    } catch (SecretsManagerException e) {
      System.err.println(e.awsErrorDetails().errorMessage());
      System.exit(1);
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

}
