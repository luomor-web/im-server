package org.example.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * docker run --name mongo --restart=always -p 27017:27017 -v /home/mongodb:/data/db -e MONGO_INITDB_ROOT_USERNAME=admin -e MONGO_INITDB_ROOT_PASSWORD=QuniMade!  -d mongo:latest mongod --auth
 */

public class MongoConfig {

    private static MongoClient mongoClient = null;

    public static MongoClient getClient() {
        if (mongoClient == null) {
            CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                    fromProviders(PojoCodecProvider.builder().automatic(true).build()));

            MongoClientSettings settings = MongoClientSettings.builder()
                    .codecRegistry(pojoCodecRegistry)
                    .applyConnectionString(new ConnectionString("mongodb://" + CourierConfig.mongoUserName + ":" + CourierConfig.mongoPassword + "@" + CourierConfig.mongoHost+"/"))
                    .build();

            mongoClient = MongoClients.create(settings);
        }
        return mongoClient;
    }
}
