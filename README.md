# aws sdk waiters resource creation
***The resources created using this approach has two ways of creation and their intention is when the resource can take a longer time to be created/updated***
1. Sync which will be waiting for the resource to be created/updated.
     ```
     LambdaClient awsLambda = LambdaClient.builder().build();
      LambdaWaiter lambdaWaiter = lambdaClient.waiter();
     ```
     **Using this approach the main thread will be held until the resource exist**
     **This is a synchronous and blocking solution**
     ```
     WaiterResponse<GetFunctionResponse> waiterResponse =  lambdaWaiter.waitUntilFunctionExists(getFunctionRequest);`
     Optional<GetFunctionResponse> functionResponseOptional = waiterResponse.matched().response();`
     ```
     **validate if the function was created**
     ```
     if (functionResponseOptional.isPresent()){
          addLambdaPermission(awsLambda, functionName, "statement", action, principal, sourceArn);
      }
     ```
2. ***Async which will execute in a thread aside and will not block the main thread***
     ```
    SecretsManagerAsyncClient secretsManagerAsyncClient = SecretsManagerAsyncClient.builder().region(region)
        .endpointOverride(URI.create(ENDPOINT_URL))
        .build();
     CloudWatchAsyncWaiter cloudWatchWaiter = cloudWatchClient.waiter();

     DescribeAlarmsRequest describeAlarmsRequest =DescribeAlarmsRequest.builder().build();`

     CompletableFuture<WaiterResponse<DescribeAlarmsResponse>> future = cloudWatchWaiter.waitUntilAlarmExists(describeAlarmsRequest);
      if(future.isDone()){
       Optional<DescribeAlarmsResponse> optional = future.get().matched().response();
        if(optional.isPresent()){
          optional.get().metricAlarms();
        }
      }
     ```
# aws sdk clients resource creation using regular approach
***The resources using this approach can take less time to be created/updated***
1. ***Sync this way the main thread is held until the resource is created/updated***
   ```
    CognitoIdentityProviderClient cognitoProviderClient = CognitoIdentityProviderClient.builder().build();

    CreateUserPoolRequest request = CreateUserPoolRequest.builder().poolName(userPoolName).build();

   CreateUserPoolResponse response = cognitoClient.createUserPool(request);
      return response.userPool().id();
   ```
2. ***Async the resource will be created in a thread aside using CompletableFuture api***
     ```
     CreateUserPoolRequest request = CreateUserPoolRequest.builder().poolName(userPoolName).build();

      CompletableFuture<CreateUserPoolResponse> response = cognitoClient.createUserPool(request);
      while (!response.isDone()) Thread.sleep(1000);
      return response.get().userPool().id();
     ```
    

