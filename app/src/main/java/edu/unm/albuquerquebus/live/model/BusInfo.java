package edu.unm.albuquerquebus.live.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by saikrishna on 11/22/17.
 */

public class BusInfo {

    private String vehicleNumber;
    private String speedInMPH;
    private String nextStop;
    private String nextStopId;
    private String tripId;
    private double latitude;
    private double longitude;

    private Date msgDateTime;
    private Date nextStopScheduleTime;

    private int direction;
    private double scale;

    private String busShortName;

    private String busColor;

    private HashMap<String, HashMap<String, String>> tripDetailsMap = new HashMap<>();

    private List<StopDetails> listOfTripStopDetails = new ArrayList<>();

    public BusInfo() {
    }


    public BusInfo(String vehicleNumber, String speedInMPH, String nextStop, double latitude, double longitude, Date msgDateTime, int direction, double scale, String busShortName) {
        this.vehicleNumber = vehicleNumber;
        this.speedInMPH = speedInMPH;
        this.nextStop = nextStop;
        this.latitude = latitude;
        this.longitude = longitude;
        this.msgDateTime = msgDateTime;
        this.direction = direction;
        this.scale = scale;
        this.busShortName = busShortName;
    }

    @Override
    public String toString() {
        return "BusInfo{" +
                "vehicleNumber='" + vehicleNumber + '\'' +
                ", speedInMPH='" + speedInMPH + '\'' +
                ", nextStop='" + nextStop + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", msgDateTime=" + msgDateTime +
                ", direction=" + direction +
                ", scale=" + scale +
                ", busShortName='" + busShortName + '\'' +
                '}';
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getSpeedInMPH() {
        return speedInMPH;
    }

    public void setSpeedInMPH(String speedInMPH) {
        this.speedInMPH = speedInMPH;
    }

    public String getNextStop() {
        return nextStop;
    }

    public void setNextStop(String nextStop) {
        this.nextStop = nextStop;
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

    public Date getMsgDateTime() {
        return msgDateTime;
    }

    public void setMsgDateTime(Date msgDateTime) {
        this.msgDateTime = msgDateTime;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public String getBusShortName() {
        return busShortName;
    }

    public void setBusShortName(String busShortName) {
        this.busShortName = busShortName;
    }

    public String getNextStopId() {
        return nextStopId;
    }

    public void setNextStopId(String nextStopId) {
        this.nextStopId = nextStopId;
    }

    public Date getNextStopScheduleTime() {
        return nextStopScheduleTime;
    }

    public void setNextStopScheduleTime(Date nextStopScheduleTime) {
        this.nextStopScheduleTime = nextStopScheduleTime;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getBusColor() {
        return busColor;
    }

    public void setBusColor(String busColor) {
        this.busColor = busColor;
    }

    public HashMap<String, HashMap<String, String>> getTripDetailsMap() {
        return tripDetailsMap;
    }

    public void setTripDetailsMap(HashMap<String, HashMap<String, String>> tripDetailsMap) {
        this.tripDetailsMap = tripDetailsMap;
    }

    public List<StopDetails> getListOfTripStopDetails() {
        return listOfTripStopDetails;
    }

    public void setListOfTripStopDetails(List<StopDetails> listOfTripStopDetails) {
        this.listOfTripStopDetails = listOfTripStopDetails;
    }

    public static class StopDetails {

        String stopId;
        String stopName;
        String stopTime;

        public StopDetails(String stopId, String stopName, String stopTime) {
            this.stopId = stopId;
            this.stopName = stopName;
            this.stopTime = stopTime;
        }

        public String getStopId() {
            return stopId;
        }

        public void setStopId(String stopId) {
            this.stopId = stopId;
        }

        public String getStopName() {
            return stopName;
        }

        public void setStopName(String stopName) {
            this.stopName = stopName;
        }

        public String getStopTime() {
            return stopTime;
        }

        public void setStopTime(String stopTime) {
            this.stopTime = stopTime;
        }
    }
}
