package com.example.pocwaiters.waitersAws;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateUserPoolRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateUserPoolResponse;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.LambdaClientBuilder;
import software.amazon.awssdk.services.lambda.model.AddPermissionRequest;
import software.amazon.awssdk.services.lambda.model.AddPermissionResponse;
import software.amazon.awssdk.services.lambda.model.CreateFunctionRequest;
import software.amazon.awssdk.services.lambda.model.CreateFunctionResponse;
import software.amazon.awssdk.services.lambda.model.FunctionCode;
import software.amazon.awssdk.services.lambda.model.GetFunctionConfigurationRequest;
import software.amazon.awssdk.services.lambda.model.GetFunctionRequest;
import software.amazon.awssdk.services.lambda.model.GetFunctionResponse;
import software.amazon.awssdk.services.lambda.model.LambdaException;
import software.amazon.awssdk.services.lambda.model.Runtime;
import software.amazon.awssdk.services.lambda.model.ServiceException;
import software.amazon.awssdk.services.lambda.waiters.LambdaWaiter;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;

/**
 * https://docs.aws.amazon.com/code-library/latest/ug/lambda_example_lambda_Scenario_GettingStartedFunctions_section.html
 */
@Component
public class LambdaTest {

  private static final String ENDPOINT_URL = "http://localhost:4566";


  public void test(){
    String functionName = "test";

    LambdaClient awsLambda = LambdaClient.builder()
        .region(Region.US_EAST_1)
        .endpointOverride(URI.create(ENDPOINT_URL))
        .build();

    createLambdaFunction(awsLambda,
        functionName,
        "helloworld.jar",
        "arn:aws:iam::123456789012:role/myLambdaRole",
        "handler");

    LambdaWaiter lambdaWaiter = awsLambda.waiter();

    //El servicio que tendrá permiso
    String principal = "s3.amazonaws.com";

    // El ARN del recurso que puede invocar la función
    String sourceArn = "arn:aws:s3:::myBucket";

    //accion requerida para ejecutar el servicio
    String action = "lambda:InvokeFunction";

    GetFunctionRequest getFunctionRequest = GetFunctionRequest.builder()
                                                              .functionName(functionName)
                                                              .build();

    WaiterResponse<GetFunctionResponse> waiterResponse =  lambdaWaiter.waitUntilFunctionExists(getFunctionRequest);
    Optional<GetFunctionResponse> functionResponseOptional = waiterResponse.matched().response();
    if (functionResponseOptional.isPresent()){
        addLambdaPermission(awsLambda, functionName, "statement", action, principal, sourceArn);
    }
  }

  public static String createLambdaFunction(LambdaClient awsLambda,
      String functionName,
      String filePath,
      String role,
      String handler) {

    try {
      LambdaWaiter waiter = awsLambda.waiter();
      InputStream is = new FileInputStream(filePath);
      SdkBytes fileToUpload = SdkBytes.fromInputStream(is);

      FunctionCode code = FunctionCode.builder()
          .zipFile(fileToUpload)
          .build();

      CreateFunctionRequest functionRequest = CreateFunctionRequest.builder()
          .functionName(functionName)
          .description("Created by the Lambda Java API")
          .code(code)
          .handler(handler)
          .runtime(Runtime.JAVA8)
          .role(role)
          .build();

      // Create a Lambda function using a waiter
      CreateFunctionResponse functionResponse = awsLambda.createFunction(functionRequest);
      GetFunctionRequest getFunctionRequest = GetFunctionRequest.builder()
          .functionName(functionName)
          .build();
      WaiterResponse<GetFunctionResponse> waiterResponse = waiter.waitUntilFunctionExists(getFunctionRequest);
      waiterResponse.matched().response().ifPresent(System.out::println);
      System.out.println(functionName + " is ready to use");
      return functionResponse.functionArn();

    } catch (LambdaException | FileNotFoundException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
    return "";
  }

  public static void addLambdaPermission(LambdaClient lambdaClient, String functionName, String statementId,
      String action, String principal, String sourceArn) {

    try {
      AddPermissionRequest request = AddPermissionRequest.builder()
          .functionName(functionName)
          .statementId(statementId)
          .action(action)
          .principal(principal)
          .sourceArn(sourceArn)
          .build();

      AddPermissionResponse response = lambdaClient.addPermission(request);

      GetFunctionConfigurationRequest getFunctionConfigurationRequest = GetFunctionConfigurationRequest.builder().functionName(functionName).build();
      LambdaWaiter lambdaWaiter = lambdaClient.waiter();
      lambdaWaiter.waitUntilFunctionUpdated(getFunctionConfigurationRequest).matched().response().ifPresent(System.out::println);
      System.out.println("Successfully added permission: " + response.statement());

    } catch (ServiceException e) {
      e.printStackTrace();
      System.err.println(e.awsErrorDetails().errorMessage());
    }
  }

}
