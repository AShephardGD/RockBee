package com.example.rockbee;

public class User {
    String name, UUID, nameString;

    public User(String name, String UUID, String a) {
        this.name = name;
        this.UUID = UUID;
        nameString = a;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    @Override
    public String toString() {
        return  nameString  + " " + name + '\n' +
                "UUID: " + UUID;
    }
}
