package com.android.googledriveapi.model;

public class BoxItems {
    private final String name;
    private final String modifiedBy;

    public BoxItems(String name, String modifiedBy) {
        this.name = name;
        this.modifiedBy = modifiedBy;
    }

    public String getName() {
        return name;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }
}