package edu.unm.albuquerquebus.live.utils;

import android.app.Activity;
import android.content.res.AssetManager;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.unm.albuquerquebus.live.model.BusInfo;
import edu.unm.albuquerquebus.live.model.BusStop;

/**
 * Created by saikrishna on 11/22/17.
 */

public class KmlUtils {

    public static List<BusStop> readBusStopsFromKml(Activity activity) {

        AssetManager am = activity.getAssets();
        try {
            InputStream is = am.open("busstops.kml");
            XMLPullParserHandler parserHandler = new XMLPullParserHandler();
            return parserHandler.parseBusStopsKml(is);
        } catch (IOException e) {
            e.printStackTrace();
       /* } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();*/
        }
        return null;
    }

    private static Map<String, String> readStopsFileToMapBusStopwithStopId(Activity activity) {
        BufferedReader reader = null;
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        Map<String, String> stopIdMapToStopCode = new HashMap<>();
        try {
            reader = new BufferedReader(
                    new InputStreamReader(activity.getAssets().open("stops.csv")));

            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                //process line
                String[] listOfStrings = mLine.split(",");
                stopIdMapToStopCode.put(listOfStrings[3], listOfStrings[1]);
                mDatabase.child("bus-stops").child(listOfStrings[1]).child("stop_id").setValue(listOfStrings[3]);
                mDatabase.child("bus-stops").child(listOfStrings[1]).child("stop_desc").setValue(listOfStrings[6]);
                System.out.print(Arrays.toString(listOfStrings));
            }
        } catch (IOException e) {
            //log the exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }

        return stopIdMapToStopCode;
    }

    private static Map<String, ArrayList<BusTripDetails>> readStopsTimingFileToMapBusStopId(Activity activity) {
        BufferedReader reader = null;
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        Map<String, String> stopIdMapToStopCode = readStopsFileToMapBusStopwithStopId(activity);
        Map<String, ArrayList<BusTripDetails>> tripMapWithDetails = new HashMap<>();
        try {
            reader = new BufferedReader(
                    new InputStreamReader(activity.getAssets().open("stop_times.txt")));

            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                //process line
                String[] listOfStrings = mLine.split(",");
                String stopCode = stopIdMapToStopCode.get(listOfStrings[3]);
                String departTime = listOfStrings[2];
                String arrivalTime = listOfStrings[1];
                String stopPosition = listOfStrings[4];


//                mDatabase.child("bus-trips").child(listOfStrings[0]).child(listOfStrings[4]).child("stop_code").setValue(stopCode);
//                mDatabase.child("bus-trips").child(listOfStrings[0]).child(listOfStrings[4]).child("depart_time").setValue(departTime);
//                mDatabase.child("bus-trips").child(listOfStrings[0]).child(listOfStrings[4]).child("arrival_time").setValue(arrivalTime);
//
                if (!tripMapWithDetails.containsKey(listOfStrings[0]))
                    tripMapWithDetails.put(listOfStrings[0], new ArrayList<BusTripDetails>());


                if (Integer.parseInt(listOfStrings[4]) > 1) {

//                    mDatabase.child("bus-trips").child(listOfStrings[0]).child(listOfStrings[4]).child("dist").setValue(listOfStrings[8]);
                    tripMapWithDetails.get(listOfStrings[0]).add(new BusTripDetails(stopCode, departTime, arrivalTime, stopPosition, listOfStrings[8]));
                } else {
                    tripMapWithDetails.get(listOfStrings[0]).add(new BusTripDetails(stopCode, departTime, arrivalTime, stopPosition));
                }

                System.out.print(Arrays.toString(listOfStrings));
            }
        } catch (IOException e) {
            //log the exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                    e.fillInStackTrace();
                }
            }
        }

        return tripMapWithDetails;
    }


    public static void readFromDatabaseGetTripsForEachRoute(final List<BusStop> busStopList, Activity activity) {

        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        Query query = mDatabase.child("bus-trips");
        final Map<String, ArrayList<BusTripDetails>> tripMapWithDetails = readStopsTimingFileToMapBusStopId(activity);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, List<String>> mapofBusNumberAndTrips = new HashMap<>();
                Map<String, Map<String, ArrayList<BusTripDetails>>> mapofBusNumberAndTripDetails = new HashMap<>();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Map<String, Integer> mapFromCommonBusInThatRoute = new HashMap<>();
                    String tripName = data.getKey();
                    Log.d("trips name", data.getKey());
                    for (DataSnapshot data1 : data.getChildren()
                            ) {
                        Log.d("example", data1.toString());
                        Log.d("example", data1.child("stop_code").getValue().toString());
                        String stopCode = data1.child("stop_code").getValue().toString();
                        BusStop busStop = getBusStopFromListById(busStopList, stopCode);
                        Log.d("example", stopCode);
                        if (busStop != null) {
                            Log.d("example", busStop.getId());

                            for (String busName :
                                    busStop.getListOfBusServing()) {
                                if (!mapFromCommonBusInThatRoute.containsKey(busName)) {
                                    mapFromCommonBusInThatRoute.put(busName, 1);
                                } else {
                                    mapFromCommonBusInThatRoute.put(busName, mapFromCommonBusInThatRoute.get(busName) + 1);
                                }
                            }

                        }

                    }
                    String finalBusName = "";
                    int numberOfStops = 0;
                    Iterator it = mapFromCommonBusInThatRoute.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();

                        int count = (int) pair.getValue();
                        if (count > numberOfStops) {
                            numberOfStops = count;
                            finalBusName = (String) pair.getKey();
                        }
                        System.out.println(pair.getKey() + " = " + pair.getValue());
                        it.remove(); // avoids a ConcurrentModificationException
                    }

                    String busNumber = finalBusName.split("-")[0].trim();
                    if (!mapofBusNumberAndTrips.containsKey(busNumber)) {
                        mapofBusNumberAndTrips.put(busNumber, new ArrayList<String>());
                        mapofBusNumberAndTripDetails.put(busNumber, new HashMap<String, ArrayList<BusTripDetails>>());
                    }

                    mapofBusNumberAndTrips.get(busNumber).add(tripName);
                    mapofBusNumberAndTripDetails.get(busNumber).put(tripName, tripMapWithDetails.get(tripName));


                    //If email exists then toast shows else store the data on new key
                    Log.d("example", data.toString());
                }
                mDatabase.child("busNumber-with-trips").setValue(mapofBusNumberAndTrips);
                mDatabase.child("busNumber-with-trips1").setValue(mapofBusNumberAndTripDetails);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    public static BusStop getBusStopFromListById(List<BusStop> busStopList, String id) {

        for (BusStop busStop :
                busStopList) {
            if (busStop.getId().equalsIgnoreCase(id)) {
                return busStop;
            }
        }
        return null;

    }

    public static HashMap<String, List<String>> getBusStopNameToStopCodeMapping(List<BusStop> busStopList) {
        HashMap<String, List<String>> busStopNameToStopCodeMap = new HashMap<>();


        for (BusStop busStop :
                busStopList) {
            if (!busStopNameToStopCodeMap.containsKey(busStop.getName())) {
                busStopNameToStopCodeMap.put(busStop.getName(), new ArrayList<String>());
            }
            busStopNameToStopCodeMap.get(busStop.getName()).add(busStop.getId());
        }

        return busStopNameToStopCodeMap;
    }


    public static void findStartAndEndTimeOfEachBus() {
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        Query query = database.child("busNumber-with-trips2");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, HashMap<String, HashMap<String, HashMap<String, String>>>> map = (HashMap<String, HashMap<String, HashMap<String, HashMap<String, String>>>>) dataSnapshot.getValue();
                Iterator<Map.Entry<String, HashMap<String, HashMap<String, HashMap<String, String>>>>> it = map.entrySet().iterator();
                String allBusStartTime = "";
                String allBusEndTime = "";
                while (it.hasNext()) {
                    Map.Entry<String, HashMap<String, HashMap<String, HashMap<String, String>>>> pair = it.next();
                    String busNum = pair.getKey();
                    String startTime = "";
                    String endTime = "";
                    //Log.d("tripss", pair.getKey() + " = " + pair.getValue());
                    HashMap<String, HashMap<String, HashMap<String, String>>> tripMap = pair.getValue();

                    Iterator<Map.Entry<String, HashMap<String, HashMap<String, String>>>> iterator = tripMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, HashMap<String, HashMap<String, String>>> pair2 = iterator.next();
                        HashMap<String, HashMap<String, String>> eachTripDetailsList = pair2.getValue();

                        Iterator<Map.Entry<String, HashMap<String, String>>> iterator2 = eachTripDetailsList.entrySet().iterator();
                        while (iterator2.hasNext()) {
                            Map.Entry<String, HashMap<String, String>> pair3 = iterator2.next();
                            if (startTime == "") {
                                startTime = pair3.getKey();
                                endTime = pair3.getKey();

                            }

                            if (allBusStartTime.equalsIgnoreCase("")) {
                                allBusStartTime = pair3.getKey();
                                allBusEndTime = pair3.getKey();
                            }

                            try {
                                Date newDate = new SimpleDateFormat("HH:mm:ss").parse(pair3.getKey());
                                Date oldStartDate = new SimpleDateFormat("HH:mm:ss").parse(startTime);

                                if (newDate.compareTo(oldStartDate) == -1) {
                                    startTime = new SimpleDateFormat("HH:mm:ss").format(newDate);
                                }
                                if (newDate.compareTo(oldStartDate) == 1) {
                                    endTime = new SimpleDateFormat("HH:mm:ss").format(newDate);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            iterator2.remove();
                        }

                        iterator.remove();
                    }

                    Log.d("BUS_Timing", busNum + " - " + startTime + " - " + endTime);
                    database.child("busNumber-timings").child(busNum).child("startTime").setValue(startTime);
                    database.child("busNumber-timings").child(busNum).child("endTime").setValue(endTime);
                    try {
                        Date oldStartDate = new SimpleDateFormat("HH:mm:ss").parse(startTime);
                        Date oldEndDate = new SimpleDateFormat("HH:mm:ss").parse(endTime);
                        Date oldAllBusStartTimeDate = new SimpleDateFormat("HH:mm:ss").parse(allBusStartTime);
                        Date oldAllBusEndTimeDate = new SimpleDateFormat("HH:mm:ss").parse(allBusEndTime);

                        if (oldStartDate.compareTo(oldAllBusStartTimeDate) == -1) {
                            allBusStartTime = startTime;
                        }

                        if (oldEndDate.compareTo(oldAllBusEndTimeDate) == 1) {
                            allBusEndTime = endTime;
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    it.remove(); // avoids a ConcurrentModificationException
                }
                database.child("busNumber-timings").child("allBus").child("startTime").setValue(allBusStartTime);
                database.child("busNumber-timings").child("allBus").child("endTime").setValue(allBusEndTime);
                Log.d("sample", "sample");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public static void readAllTripsAndBusLocationDataFromAllRouteJson(String response) {

        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        try {
            JSONObject mainJsonObject = new JSONObject(response);
            JSONArray allRoutesJsonArray = mainJsonObject.getJSONArray(Constants.ALL_ROUTES);
            int sizeOfArray = allRoutesJsonArray.length();
            for (int i = 0; i < sizeOfArray; i++) {

                BusInfo busInfo = new BusInfo();
                JSONObject eachBusInformationJsonObject = allRoutesJsonArray.getJSONObject(i);

                String vehicle_id = eachBusInformationJsonObject.getString(Constants.VEHICLE_ID);
                String msg_time = eachBusInformationJsonObject.getString(Constants.MESSAGE_TIME);
                String route_short_name = eachBusInformationJsonObject.getString(Constants.ROUTE_SHORT_NAME);
                String trip_id = eachBusInformationJsonObject.getString(Constants.TRIP_ID);
                String next_stop_id = eachBusInformationJsonObject.getString(Constants.NEXT_STOP_ID);
                String next_stop_name = eachBusInformationJsonObject.getString(Constants.NEXT_STOP_NAME);
                String next_stop_sched_time = eachBusInformationJsonObject.getString(Constants.NEXT_STOP_SCHEDULE_TIME);

                double busLatitude = eachBusInformationJsonObject.getDouble(Constants.LATITUDE);
                double busLongitude = eachBusInformationJsonObject.getDouble(Constants.LONGITUDE);

                int heading = eachBusInformationJsonObject.getInt(Constants.HEADING);
                int speed_mph = eachBusInformationJsonObject.getInt(Constants.SPEED_IN_MPH);


                busInfo.setVehicleNumber(vehicle_id);
                busInfo.setBusShortName(route_short_name);
                busInfo.setLongitude(busLongitude);
                busInfo.setLatitude(busLatitude);
                busInfo.setDirection(heading);
                busInfo.setSpeedInMPH(String.valueOf(speed_mph));
                busInfo.setNextStop(next_stop_name);
                busInfo.setNextStopId(next_stop_id);
                busInfo.setTripId(trip_id);

                Date msgDate = new SimpleDateFormat("HH:mm:ss").parse(msg_time);
                Date nextStopScheduleDate = new SimpleDateFormat("HH:mm:ss").parse(next_stop_sched_time);
                long time = nextStopScheduleDate.getTime();

                busInfo.setMsgDateTime(msgDate);
                busInfo.setNextStopScheduleTime(nextStopScheduleDate);


                database.child("bus-running-info").child(route_short_name).child(vehicle_id).setValue(busInfo);
                database.child("busNumber-with-trips2").child(route_short_name).child(trip_id).child(String.valueOf(next_stop_sched_time)).child(Constants.STOP_ID).setValue(next_stop_id);
                database.child("busNumber-with-trips2").child(route_short_name).child(trip_id).child(String.valueOf(next_stop_sched_time)).child(Constants.STOP_NAME).setValue(next_stop_name);
                database.child("busNumber-with-trips2").child(route_short_name).child(trip_id).child(String.valueOf(next_stop_sched_time)).child(Constants.STOP_TIME).setValue(next_stop_sched_time);


            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static class BusTripDetails {

        String stopCode;
        String departTime;
        String arrivalTime;
        String id;
        String dist;

        public BusTripDetails(String stopCode, String departTime, String arrivalTime, String id) {
            this.stopCode = stopCode;
            this.departTime = departTime;
            this.arrivalTime = arrivalTime;
            this.id = id;
        }

        public BusTripDetails(String stopCode, String departTime, String arrivalTime, String id, String dist) {
            this.stopCode = stopCode;
            this.departTime = departTime;
            this.arrivalTime = arrivalTime;
            this.id = id;
            this.dist = dist;
        }

        public String getStopCode() {
            return stopCode;
        }

        public void setStopCode(String stopCode) {
            this.stopCode = stopCode;
        }

        public String getDepartTime() {
            return departTime;
        }

        public void setDepartTime(String departTime) {
            this.departTime = departTime;
        }

        public String getArrivalTime() {
            return arrivalTime;
        }

        public void setArrivalTime(String arrivalTime) {
            this.arrivalTime = arrivalTime;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDist() {
            return dist;
        }

        public void setDist(String dist) {
            this.dist = dist;
        }
    }

}
