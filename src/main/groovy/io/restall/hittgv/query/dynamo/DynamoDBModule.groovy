package io.restall.hittgv.query.dynamo

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder
import com.google.inject.Provides
import com.google.inject.Singleton
import groovy.transform.CompileStatic
import ratpack.guice.ConfigurableModule

@CompileStatic
class DynamoDBModule extends ConfigurableModule<DynamoDBConfig> {

    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    AmazonDynamoDBAsync dynamoDBClient(final DynamoDBConfig config) {
        AWSCredentials credentials = new BasicAWSCredentials(config.accessKey, config.secretKey)

        AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials)

//        def endpointConfiguration = new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "eu-west-1")

        AmazonDynamoDBAsyncClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(Regions.EU_CENTRAL_1)
//                .withEndpointConfiguration(endpointConfiguration)
                .build()
    }
}