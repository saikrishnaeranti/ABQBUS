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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.unm.albuquerquebus.live.MainActivity;
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

    public static Map<String, String> readStopsFileToMapBusStopwithStopId(Activity activity) {
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
                System.out.print(listOfStrings);
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

    public static void readStopsTimingFileToMapBusStopId(MainActivity activity) {
        BufferedReader reader = null;
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        Map<String, String> stopIdMapToStopCode = readStopsFileToMapBusStopwithStopId(activity);
        try {
            reader = new BufferedReader(
                    new InputStreamReader(activity.getAssets().open("stop_times.txt")));

            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                //process line
                String[] listOfStrings = mLine.split(",");
                String stopCode = stopIdMapToStopCode.get(listOfStrings[3]);

                mDatabase.child("bus-trips").child(listOfStrings[0]).child(listOfStrings[4]).child("stop_code").setValue(stopCode);
                mDatabase.child("bus-trips").child(listOfStrings[0]).child(listOfStrings[4]).child("depart_time").setValue(listOfStrings[2]);
                mDatabase.child("bus-trips").child(listOfStrings[0]).child(listOfStrings[4]).child("arrival_time").setValue(listOfStrings[1]);
                if (Integer.parseInt(listOfStrings[4]) > 1) {

                    mDatabase.child("bus-trips").child(listOfStrings[0]).child(listOfStrings[4]).child("dist").setValue(listOfStrings[8]);
                }

                System.out.print(listOfStrings);
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
    }

    public static void readFromDatabaseGetTripsForEachRoute(final List<BusStop> busStopList) {

        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        Query query = mDatabase.child("bus-trips");


        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, List<String>> mapofBusNumberAndTrips = new HashMap<>();
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


                        /*Query subQuery = mDatabase.child("bus-stops").child(stopCode).child("listOfBusServing");
                        subQuery.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot data : dataSnapshot.getChildren()) {

                                    if (!mapFromCommonBusInThatRoute.containsKey(data.getValue().toString())) {
                                        mapFromCommonBusInThatRoute.put(data.getValue().toString(), 1);
                                    } else {
                                        mapFromCommonBusInThatRoute.put(data.getValue().toString(), mapFromCommonBusInThatRoute.get(data.getValue().toString()) + 1);
                                    }

                                    Log.d("example", data.toString());
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });*/
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
                    if (!mapofBusNumberAndTrips.containsKey(busNumber))
                        mapofBusNumberAndTrips.put(busNumber, new ArrayList<String>());

                    mapofBusNumberAndTrips.get(busNumber).add(tripName);


                    //If email exists then toast shows else store the data on new key
                    Log.d("example", data.toString());
                }
                mDatabase.child("busNumber-with-trips").setValue(mapofBusNumberAndTrips);

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
}
