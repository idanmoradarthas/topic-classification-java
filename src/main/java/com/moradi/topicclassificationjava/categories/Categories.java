package com.moradi.topicclassificationjava.categories;

public enum Categories {
  WEAPONS("weapons"),
  NUDITY("nudity"),
  DRUGS("drugs"),
  CYBER("cyber-security");

  private String text;

  Categories(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }
}
