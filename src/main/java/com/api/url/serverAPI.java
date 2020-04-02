package com.api.url;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.mongodb.client.model.Filters.eq;

@Path("test")
@Produces(MediaType.APPLICATION_JSON)
public class serverAPI {
    MongoClientURI uri = new MongoClientURI("mongodb+srv://Deva:incorrect1@deva-gxfnf.mongodb.net/test?retryWrites=true&w=majority");

    MongoClient mongoClient = new MongoClient(uri);
    MongoDatabase database = mongoClient.getDatabase("kaiburr");
    MongoCollection<Document> collection = database.getCollection("server");

    @GET
    public Response getAllServersJSON() {
        FindIterable<Document> serverColl = collection.find();
        MongoCursor<Document> cursor = serverColl.cursor();
        if (!cursor.hasNext()) {
            Document errorMessage = new Document();
            errorMessage.put("ErrorMessage","Collection is empty");
            return Response.status(204).entity(errorMessage.toJson()).build();
        }
        JSONArray fullJSON = extractData(serverColl);
        return Response.status(Response.Status.OK).entity(fullJSON.toString()).build();
    }

    @GET
    @Path("/{id}")
    public Response getServerByID(@PathParam("id") String id) {
        FindIterable<Document> serverColl = collection.find(eq("id", id));
        MongoCursor<Document> cursor = serverColl.cursor();
        if (!cursor.hasNext()) {
            Document errorMessage = new Document();
            errorMessage.put("ErrorMessage","The Server with given ID was not found in this collection");
            return Response.status(Response.Status.NOT_FOUND).entity(errorMessage.toJson()).build();
        }
        JSONArray fullJSON = extractData(serverColl);
        return Response.status(Response.Status.OK).entity(fullJSON.toString()).build();
    }

    @GET
    @Path("/name/{name}")
    public Response getServerByName(@PathParam("name") String name) {
        FindIterable<Document> serverColl = collection.find(eq("name", name));
        MongoCursor<Document> cursor = serverColl.cursor();
        if (!cursor.hasNext()) {
            Document errorMessage = new Document();
            errorMessage.put("ErrorMessage","The Server with given name was not found in this collection");
            return Response.status(Response.Status.NOT_FOUND).entity(errorMessage.toJson()).build();
        }
        JSONArray fullJSON = extractData(serverColl);
        return Response.status(Response.Status.OK).entity(fullJSON.toString()).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteServerByID(@PathParam("id") String id) {
        FindIterable<Document> serverColl = collection.find(eq("id", id));
        MongoCursor<Document> cursor = serverColl.cursor();
        if (cursor.hasNext()) {
            collection.deleteOne(eq("id", id));
            Document successMessage = new Document();
            successMessage.put("SuccessMessage", "Successfully Deleted");
            return Response.status(Response.Status.OK).entity(successMessage.toJson()).build();
        }
        Document errorMessage = new Document();
        errorMessage.put("ErrorMessage","In this collection, no Document is present with the given ID");
        return Response.status(Response.Status.NOT_FOUND).entity(errorMessage.toJson()).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putServer(String obj) {
        Document doc = Document.parse(obj);
        Response response = contentVerify(doc);
        if (response != null) {
            return response;
        }
        String id = doc.getString("id");
        FindIterable<Document> serverColl = collection.find(eq("id", id));
        int count = 0;
        for (Document docs : serverColl) {
            Object temp = docs.get("_id");
            collection.replaceOne(eq("_id", temp), doc);
            ++count;
        }
        Document info = new Document();
        if (count == 0) {
            collection.insertOne(doc);
            info.put("SuccessMessage", "New document is created");
            return Response.status(Response.Status.CREATED).entity(info.toJson()).build();
        }
        info.put("SuccessMessage", "Existing documents are replaced");
        info.put("No.of Document replaced", count);
        return Response.status(Response.Status.OK).entity(info.toJson()).build();
    }

    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    public Response patchServer(String obj) {
        Document doc = Document.parse(obj);
        Response response = contentVerify(doc);
        if (response != null) {
            return response;
        }
        String id = doc.getString("id");
        UpdateResult result = collection.updateMany(eq("id", id), new Document("$set", doc));
        if (result.getMatchedCount() == 0) {
            Document errorMessage = new Document();
            errorMessage.put("ErrorMessage", "Enter valid details especially ID, No document found with given ID");
            return Response.status(Response.Status.NOT_FOUND).entity(errorMessage.toJson()).build();
        }
        Document successMessage = new Document();
        successMessage.put("SuccessMessage", "Successfully Patched into existing document");
        successMessage.put("Details", result.toString());
        return Response.status(Response.Status.OK).entity(successMessage.toJson()).build();
    }

    private Response contentVerify(Document doc) {
        if (doc.isEmpty()) {
            Document errorMessage = new Document();
            errorMessage.put("ErrorMessage", "Send the data in application/json format, the current data is empty");
            return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage.toJson()).build();
        }
        return null;
    }

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
}
