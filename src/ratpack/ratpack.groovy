import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.GetItemRequest
import com.amazonaws.services.dynamodbv2.model.PutItemRequest
import io.restall.hittgv.query.dynamo.DynamoDBConfig
import io.restall.hittgv.query.dynamo.DynamoDBModule
import ratpack.config.ConfigData

import static ratpack.groovy.Groovy.ratpack
import static ratpack.jackson.Jackson.json

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
                def item = [URLKey: new AttributeValue().withS(urlKey), Rating: new AttributeValue().withN("5")]
                PutItemRequest putItemRequest = new PutItemRequest().withTableName("test").withItem(item)
                client.putItem(putItemRequest)

                render(json([rating: "4"]))
            }
        }
    }
}