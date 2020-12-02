package com.company;


import java.io.*;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.time.Instant;

public class Main {
    private static final Scanner in = new Scanner(System.in);
    private static String keyStoreFilePath = "key_store_db.json";//static file obj
    private static File keyStore = new File(keyStoreFilePath);
    private static boolean isRun = true;

    public static void main(String[] args) throws IOException {
        if (!keyStore.exists()) {
            keyStore.createNewFile();

            JSONObject emptyJSONObject = new JSONObject();
            emptyJSONObject.put("hello", "world");
            FileWriter keyStoreWrite = new FileWriter(keyStoreFilePath);
            keyStoreWrite.write(emptyJSONObject.toString());
            keyStoreWrite.flush();
        }

        while (isRun) run();//input
    }

    private static void run() throws IOException {
        System.out.print("> ");
        String[] command = in.nextLine().split(" ", 3);
        if (command.length < 1) {
            System.out.println("Please enter a command to continue");
            return;
        }

        switch (command[0]) {
            case "create":
                if (command.length != 3)
                    System.out.println("Usage: create <key> <value>");
                else
                    create(command[1], command[2]);
                break;
            case "read":
                if (command.length != 2)
                    System.out.println("Usage: read <key>");
                else
                    read(command[1]);
                break;
            case "update":
                if (command.length != 3)
                    System.out.println("Usage: update <key> <value>");
                else
                    update(command[1], command[2]);
                break;
            case "delete":
                if (command.length != 2)
                    System.out.println("Usage: delete <key>");
                else
                    delete(command[1]);
                break;
            case "help":
                System.out.println("Use: create Keyname {\"value\":\"TimeToliveValue\"}");
                System.out.println("Use: read Keyname ");
                System.out.println("Use: delete Keyname ");
                System.out.println("Use: update Keyname {\"value\":\"TimeToliveValue\"}");
                break;
            case "exit":
                isRun = false;
                break;
            default:
                System.out.println("Enter valid command. Use help to understand list of commands");
        }
    }
    //int Syseconds=0;
    private static void create(String key, String value) throws IOException {
        Instant instant=Instant.now();
        ZonedDateTime Syseconds = instant.atZone(ZoneOffset.UTC);

        InputStream keyStoreRead = new FileInputStream(keyStore);
        JSONTokener keyStoreTokener = new JSONTokener(keyStoreRead);
        JSONObject keyStoreObject = new JSONObject(keyStoreTokener);

        if (keyStoreObject.has(key)) {
            System.out.println("Key already exists. Please provide a new key.");
            return;
        }

        try {
            JSONTokener valueTokener = new JSONTokener(value);
            JSONObject valueObject = new JSONObject(valueTokener);

            if (valueObject.has("ttl")) {
                valueObject.put("ttl", Syseconds.plusSeconds(valueObject.getLong("ttl")).toString());
            }

            keyStoreObject.put(key, valueObject);
        } catch (JSONException e) {
            System.out.println("Enter valid JSON data for value.");
        }

        FileWriter keyStoreWrite = new FileWriter(keyStoreFilePath);

        keyStoreWrite.write(keyStoreObject.toString());
        keyStoreWrite.flush();//writes content
    }

    private static void read (String key) throws IOException {
        Instant instant=Instant.now();

        ZonedDateTime Syseconds = instant.atZone(ZoneOffset.UTC);

        InputStream keyStoreRead = new FileInputStream(keyStore);
        JSONTokener keyStoreTokener = new JSONTokener(keyStoreRead); //string passed
        JSONObject keyStoreObject = new JSONObject(keyStoreTokener);
        //int diff=0;
        if (keyStoreObject.has(key)) {
            JSONTokener valueTokener = new JSONTokener(keyStoreObject.getJSONObject(key).toString());
            JSONObject valueObject = new JSONObject(valueTokener);

            if (valueObject.has("ttl")) {
                ZonedDateTime ttlTime = ZonedDateTime.parse(valueObject.getString("ttl"), DateTimeFormatter.ISO_ZONED_DATE_TIME);

                long diff = ttlTime.compareTo(Syseconds);
                if (diff <= 0) {
                    keyStoreObject.remove(key);
                    FileWriter keyStoreWrite = new FileWriter(keyStoreFilePath);
                    keyStoreWrite.write(keyStoreObject.toString());
                    keyStoreWrite.flush();
                    System.out.println("Time to live for key has expired. Please add new value to key.");
                    return;
                }
            }

            System.out.println(keyStoreObject.get(key).toString());
        } else {
            System.out.println("key does not exist. Please create one before reading.");
        }
    }

    private static void update(String key, String value) throws IOException {
        Instant instant = Instant.now();
        ZonedDateTime Syseconds = instant.atZone(ZoneOffset.UTC);

        InputStream keyStoreRead = new FileInputStream(keyStore);
        JSONTokener keyStoreTokener = new JSONTokener(keyStoreRead);
        JSONObject keyStoreObject = new JSONObject(keyStoreTokener);

        if (!keyStoreObject.has(key)) {
            System.out.println("key does not exist. Please create one before updating.");
            return;
        }

        try {
            JSONTokener valueTokener = new JSONTokener(value);
            JSONObject valueObject = new JSONObject(valueTokener);

            if (valueObject.has("ttl"))
                valueObject.put("ttl", Syseconds.plusSeconds(valueObject.getLong("ttl")).toString());

            keyStoreObject.put(key, valueObject);
        } catch (JSONException e) {
            System.out.println("Enter valid JSON data for value");
        }

        FileWriter keyStoreWrite = new FileWriter(keyStoreFilePath);
        keyStoreWrite.write(keyStoreObject.toString());
        keyStoreWrite.flush();
    }

    private static void delete(String key) throws IOException {
        InputStream keyStoreRead = new FileInputStream(keyStore);
        JSONTokener keyStoreTokener = new JSONTokener(keyStoreRead);
        JSONObject keyStoreObject = new JSONObject(keyStoreTokener);

        if (!keyStoreObject.has(key)) {
            System.out.println("key does not exist. Cannot delete a non-existing entry.");
            return;
        }

        keyStoreObject.remove(key);
        System.out.println("key " + key + " successfully deleted.");
        FileWriter keyStoreWrite = new FileWriter(keyStoreFilePath);
        keyStoreWrite.write(keyStoreObject.toString());
        keyStoreWrite.flush();
    }
}