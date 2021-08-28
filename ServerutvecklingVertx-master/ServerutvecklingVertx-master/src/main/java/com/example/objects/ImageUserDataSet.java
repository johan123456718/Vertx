package com.example.objects;

public class ImageUserDataSet {

  private String uuid;

  private int count;

  private int day_of_the_week;

  public ImageUserDataSet(){
    this.uuid = "";
    this.count = 0;
    this.day_of_the_week = 0;
  }

  public ImageUserDataSet(String uuid, int count, int day_of_the_week){
    this.uuid = uuid;
    this.count = count;
    this.day_of_the_week = day_of_the_week;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public int getDay_of_the_week() {
    return day_of_the_week;
  }

  public void setDay_of_the_week(int day_of_the_week) {
    this.day_of_the_week = day_of_the_week;
  }
}
