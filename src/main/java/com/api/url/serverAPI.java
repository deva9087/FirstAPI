package com.api.url;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.mongodb.client.model.Filters.eq;

@Path("test")
@Produces(MediaType.APPLICATION_JSON)
public class serverAPI {
    MongoClientURI uri = new MongoClientURI("mongodb+srv://Deva:incorrect1@deva-gxfnf.mongodb.net/test?retryWrites=true&w=majority");

    MongoClient mongoClient = new MongoClient(uri);
    MongoDatabase database = mongoClient.getDatabase("kaiburr");
    MongoCollection<Document> collection = database.getCollection("server");

    private JSONArray extractData(FindIterable<Document> serverColl) {
        JSONArray fullJSON = new JSONArray();
        for (Document temp : serverColl) {
            String result = temp.toJson();
            JSONObject json = new JSONObject(result);
            json.remove("_id");
            fullJSON.put(json);
        }
        return fullJSON;
    }

    @GET
    public String getAllServersJSON() {
        FindIterable<Document> serverColl = collection.find();
        JSONArray fullJSON = extractData(serverColl);
        return fullJSON.toString();
    }

    @GET
    @Path("/{id}")
    public String getServerByID(@PathParam("id") String id) {
        FindIterable<Document> serverColl = collection.find(eq("id", id));
        JSONArray fullJSON = extractData(serverColl);
        return fullJSON.toString();
    }

    @GET
    @Path("/name/{name}")
    public String getServerByName(@PathParam("name") String name) {
        FindIterable<Document> serverColl = collection.find(eq("name", name));
        JSONArray fullJSON = extractData(serverColl);
        return fullJSON.toString();
    }

    @DELETE
    @Path("/{id}")
    public String deleteServerByID(@PathParam("id") String id) {
        collection.deleteOne(eq("id", id));
        return getAllServersJSON();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public String putServer(String obj) {
        Document doc = Document.parse(obj);
        String id = doc.getString("id");
        FindIterable<Document> serverColl = collection.find(eq("id", id));
        int count = 0;
        for (Document docs : serverColl) {
            Object temp = docs.get("_id");
            collection.replaceOne(eq("_id", temp), doc);
            ++count;
        }
        if (count == 0) {
            collection.insertOne(doc);
        }
        return getAllServersJSON();
    }

    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    public String patchServer(String obj) {
        Document doc = Document.parse(obj);
        String id = doc.getString("id");
        UpdateResult result = collection.updateMany(eq("id", id), new Document("$set", doc));
        return getAllServersJSON();
    }
}
