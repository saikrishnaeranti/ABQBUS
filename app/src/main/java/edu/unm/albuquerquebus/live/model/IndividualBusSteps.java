package edu.unm.albuquerquebus.live.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by saikrishna on 10/28/17.
 */

public class IndividualBusSteps {


    private LatLng arrivalStopLocation;
    private String arrivalStopName;
    private long arrivalTime;
    private String arrivalTimeString;

    private LatLng departureStopLocation;
    private String departureStopName;
    private long departureTime;
    private String departureTimeString;

    private String headSign;

    private String busName;
    private String busShortName;
    private String busColor;
    private int noOfBusStops;

    private List<BusInfo> mBusInfoList;

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

    public String getBusColor() {
        return busColor;
    }

    public void setBusColor(String busColor) {
        this.busColor = busColor;
    }

    public List<BusInfo> getBusInfoList() {
        return mBusInfoList;
    }

    public void setBusInfoList(List<BusInfo> busInfoList) {
        mBusInfoList = busInfoList;
    }

    public String getArrivalTimeString() {
        return arrivalTimeString;
    }

    public void setArrivalTimeString(String arrivalTimeString) {
        this.arrivalTimeString = arrivalTimeString;
    }

    public String getDepartureTimeString() {
        return departureTimeString;
    }

    public void setDepartureTimeString(String departureTimeString) {
        this.departureTimeString = departureTimeString;
    }
}
