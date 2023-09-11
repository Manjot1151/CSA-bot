package org.manjot;

import java.io.FileNotFoundException;
import java.io.FileReader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class Auth {
    public static String token;
    public static String mongo;
    public static void initialize() throws FileNotFoundException {
        JsonReader reader = new JsonReader(new FileReader("auth.json"));
        JsonObject auth = JsonParser.parseReader(reader).getAsJsonObject();
        token = auth.get("token").getAsString();
        mongo = auth.get("mongo").getAsString();
    }
}
