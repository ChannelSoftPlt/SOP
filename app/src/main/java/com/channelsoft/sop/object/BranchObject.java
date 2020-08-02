package com.channelsoft.sop.object;

public class BranchObject {
    private String id, name, address, user_id;

    public BranchObject(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getUser_id() {
        return user_id;
    }

    @Override
    public String toString() {
        return name;
    }
}
