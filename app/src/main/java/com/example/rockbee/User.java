package com.example.rockbee;

import androidx.annotation.Nullable;

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

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj == null) return false;
        if(getClass() != obj.getClass()) return false;
        User u = (User) obj;
        return u.getName().equals(this.name) && u.getUUID().equals(this.UUID);
    }

    public void setNameString(String nameString) {
        this.nameString = nameString;
    }
}
