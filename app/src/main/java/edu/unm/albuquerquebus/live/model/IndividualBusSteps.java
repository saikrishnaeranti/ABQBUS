package edu.unm.albuquerquebus.live.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by saikrishna on 10/28/17.
 */

public class IndividualBusSteps {


    private LatLng arrivalStopLocation;
    private String arrivalStopName;
    private long arrivalTime;

    private LatLng departureStopLocation;
    private String departureStopName;
    private long departureTime;

    private String headSign;

    private String busName;
    private String busShortName;
    private int noOfBusStops;

    public LatLng getArrivalStopLocation() {
        return arrivalStopLocation;
    }

    public void setArrivalStopLocation(LatLng arrivalStopLocation) {
        this.arrivalStopLocation = arrivalStopLocation;
    }

    public String getArrivalStopName() {
        return arrivalStopName;
    }

    public void setArrivalStopName(String arrivalStopName) {
        this.arrivalStopName = arrivalStopName;
    }

    public long getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(long arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public LatLng getDepartureStopLocation() {
        return departureStopLocation;
    }

    public void setDepartureStopLocation(LatLng departureStopLocation) {
        this.departureStopLocation = departureStopLocation;
    }

    public String getDepartureStopName() {
        return departureStopName;
    }

    public void setDepartureStopName(String departureStopName) {
        this.departureStopName = departureStopName;
    }

    public long getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(long departureTime) {
        this.departureTime = departureTime;
    }

    public String getHeadSign() {
        return headSign;
    }

    public void setHeadSign(String headSign) {
        this.headSign = headSign;
    }

    public String getBusName() {
        return busName;
    }

    public void setBusName(String busName) {
        this.busName = busName;
    }

    public String getBusShortName() {
        return busShortName;
    }

    public void setBusShortName(String busShortName) {
        this.busShortName = busShortName;
    }

    public int getNoOfBusStops() {
        return noOfBusStops;
    }

    public void setNoOfBusStops(int noOfBusStops) {
        this.noOfBusStops = noOfBusStops;
    }
}
