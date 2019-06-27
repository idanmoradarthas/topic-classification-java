package com.moradi.topicclassificationjava.categories;

public enum Categories {
    WEAPONS("weapons"),
    NUDITY("nudity");

    private String text;

    Categories(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
