import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.GetItemRequest
import com.amazonaws.services.dynamodbv2.model.PutItemRequest
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.restall.hittgv.query.dynamo.DynamoDBConfig
import io.restall.hittgv.query.dynamo.DynamoDBModule
import ratpack.config.ConfigData
import ratpack.http.client.HttpClient

import static ratpack.groovy.Groovy.ratpack
import static ratpack.jackson.Jackson.json
import static ratpack.jackson.Jackson.toJson

ratpack {

    serverConfig {
        env()
    }

    bindings {
        ConfigData configData = ConfigData.of { c ->
            c.env()
        }

        moduleConfig(DynamoDBModule, configData.get(DynamoDBConfig))
    }

    handlers {
        all { ctx ->
            def headers = response.getHeaders()
            headers.set("Access-Control-Allow-Origin", "*")
            headers.set("Access-Control-Allow-Headers", "x-requested-with, origin, content-type, accept")

            ctx.next()
        }
        get('rating') { AmazonDynamoDBAsync client ->

            def url = request.queryParams.get('url')
            if (url == null) {
                clientError(400)
                return
            }

            // It's a hackathon sorry for it being blocking

            def urlKey = url.replaceAll('https?://', '').replaceAll(/\?.*/, '')

            def key = [URLKey: new AttributeValue().withS(urlKey)]

            GetItemRequest getItemRequest = new GetItemRequest().withTableName("test").withKey(key)

            def response = client.getItem(getItemRequest)

            if (response && response.getItem()) {
                render(json([rating: response.getItem().get("Rating").getN()]))
            } else {
                final MANAGER_BASE_URL = System.getenv("MANAGER_BASE_URL")
                def jsonSlurper = new JsonSlurper()
                HttpClient httpClient = context.get(HttpClient.class)
                String requestBody = JsonOutput.toJson([url: url])

                httpClient.post(new URI(MANAGER_BASE_URL), {req -> req.body.text(requestBody)}).then({managerResponse ->
                    if(managerResponse.getStatusCode() == 200) {
                        final def managerResponseObject = jsonSlurper.parseText(managerResponse.getBody().getText())
                        final def rating = managerResponseObject.assignedScore

                        def item = [URLKey: new AttributeValue().withS(urlKey), Rating: new AttributeValue().withN(rating)]
                        PutItemRequest putItemRequest = new PutItemRequest().withTableName("test").withItem(item)
                        client.putItem(putItemRequest)

                        render(json([rating: rating]))
                    } else {
                        clientError(500)
                    }
                })
            }
        }
    }
}