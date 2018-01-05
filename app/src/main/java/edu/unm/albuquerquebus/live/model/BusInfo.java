package edu.unm.albuquerquebus.live.model;

import java.util.Date;

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
}
