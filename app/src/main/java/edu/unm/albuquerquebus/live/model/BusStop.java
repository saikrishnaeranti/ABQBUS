package edu.unm.albuquerquebus.live.model;

import java.util.ArrayList;

/**
 * Created by saikrishna on 11/21/17.
 */

public class BusStop {

    private String id;
    private String name;
    private ArrayList<String> listOfBusServing;
    private double latitude;
    private double longitude;

    public BusStop() {

    }

    public BusStop(String id, String name, ArrayList<String> listOfBusServing, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.listOfBusServing = listOfBusServing;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getListOfBusServing() {
        return listOfBusServing;
    }

    public void setListOfBusServing(ArrayList<String> listOfBusServing) {
        this.listOfBusServing = listOfBusServing;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
