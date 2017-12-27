package edu.unm.albuquerquebus.live.utils;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.unm.albuquerquebus.live.model.BusInfo;
import edu.unm.albuquerquebus.live.model.BusStop;

/**
 * Created by saikrishna on 11/21/17.
 */

public class XMLPullParserHandler {

    private List<BusStop> mBusStopList;
    private BusStop mBusStop;
    private List<BusInfo> mBusInfoList;
    private BusInfo mBusInfo;
    private String text;

    public XMLPullParserHandler() {
        mBusStopList = new ArrayList<BusStop>();
        mBusInfoList = new ArrayList<BusInfo>();
    }

    public List<BusStop> getBusStopList() {
        return mBusStopList;
    }

    public List<BusStop> parseBusStopsKml(InputStream is) {
        XmlPullParserFactory factory = null;
        XmlPullParser parser = null;
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mBusStopList.clear();
        try {
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            parser = factory.newPullParser();

            parser.setInput(is, null);

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagname = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tagname.equalsIgnoreCase("Placemark")) {
                            // create a new instance of BusStop
                            mBusStop = new BusStop();
                        }
                        break;

                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        if (text.equalsIgnoreCase("ID")) {
                            mBusStop.setId(getValueOfTableRow(parser));
                            Log.d("this is Id", String.valueOf(mBusStop.getId()));
                        } else if (text.equalsIgnoreCase("Name")) {
                            mBusStop.setName(getValueOfTableRow(parser).trim());

                            Log.d("this is Name", mBusStop.getName());
                        } else if (text.equalsIgnoreCase("Serving")) {
                            mBusStop.setListOfBusServing(getListofServings(parser));

                        } else {
                            text = parser.getText();
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if (tagname.equalsIgnoreCase("Placemark")) {
                            mDatabase.child("bus-stops").child(mBusStop.getId()).setValue(mBusStop);
                            mBusStopList.add(mBusStop);
                        } else if (tagname.equalsIgnoreCase("coordinates")) {
                            String[] temp = text.split(",");
                            Log.d("this is long,lat", text);
                            mBusStop.setLongitude(Double.parseDouble(temp[0].trim()));
                            mBusStop.setLatitude(Double.parseDouble(temp[1]));
                        }
                        break;


                    default:
                        break;
                }
                eventType = parser.next();
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mBusStopList;
    }

    public List<BusInfo> parseSingleRouteKml(String response) {
        XmlPullParserFactory factory = null;
        XmlPullParser parser = null;
        InputStream is = null;
        mBusInfoList.clear();
        try {
            is = new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8.name()));

            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            parser = factory.newPullParser();

            parser.setInput(is, null);

            int eventType = parser.getEventType();
            int busNumber = 0;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagname = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tagname.equalsIgnoreCase("Placemark")) {
                            // create a new instance of BusStop
                            mBusInfo = new BusInfo();
                        }
                        break;

                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        if (text.equalsIgnoreCase("Vehicle #")) {
                            mBusInfo.setVehicleNumber(getValueOfTableRow(parser));
                            Log.d("this is vehicle number", String.valueOf(mBusInfo.getVehicleNumber()));
                        } else if (text.equalsIgnoreCase("Speed")) {
                            mBusInfo.setSpeedInMPH(getValueOfTableRow(parser).trim());

                            Log.d("this is Speed", mBusInfo.getSpeedInMPH());
                        } else if (text.trim().equalsIgnoreCase("Next Stop")) {
                            mBusInfo.setNextStop(getValueOfTableRow(parser).trim());

                            Log.d("this is next Stop", mBusInfo.getNextStop());
                        } else if (text.equalsIgnoreCase("Msg Time")) {
                            String msgTime = getValueOfTableRow(parser);
                            try {
                                Date msgDate = new SimpleDateFormat("h:mm:ss a", Locale.US).parse(msgTime);
                                mBusInfo.setMsgDateTime(msgDate);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            Log.d("this is msg time", mBusInfo.getMsgDateTime().toString());
                        } else {
                            text = parser.getText();
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if (tagname.equals("name")) {
                            mBusInfo.setBusShortName(text);
                            Log.d("this is route shortname", mBusInfo.getBusShortName());
                        } else if (tagname.equalsIgnoreCase("Placemark")) {
                            mDatabase.child("bus-running-info").child(mBusInfo.getBusShortName()).child(mBusInfo.getVehicleNumber()).setValue(mBusInfo);
                            mBusInfoList.add(mBusInfo);
                        } else if (tagname.equalsIgnoreCase("coordinates")) {
                            String[] temp = text.split(",");
                            Log.d("this is long,lat", text);
                            mBusInfo.setLongitude(Double.parseDouble(temp[0].trim()));
                            mBusInfo.setLatitude(Double.parseDouble(temp[1]));
                        } else if (tagname.equalsIgnoreCase("heading")) {
                            mBusInfo.setDirection(Integer.parseInt(text));
                        } else if (tagname.equalsIgnoreCase("scale")) {
                            mBusInfo.setSpeedInMPH(text);
                        }
                        break;


                    default:
                        break;
                }
                eventType = parser.next();
            }

        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return mBusInfoList;
    }

    private ArrayList<String> getListofServings(XmlPullParser parser) {
        ArrayList<String> list = new ArrayList<>();
        String tagname;
        try {
            parser.next();
            parser.next();
            parser.next();
            parser.next();

            if (text == null)
                text = "";
            text = parser.getText();//1st value
            parser.next();
            tagname = parser.getName();
            text = text.replace("\n", "").trim();
            if (!text.equalsIgnoreCase("No Routes Found"))
                list.add(text);
            while (tagname == null || !tagname.equalsIgnoreCase("td")) {
                tagname = parser.getName();
                if (tagname.equalsIgnoreCase("br")) {
                    parser.next();
                    parser.next();
                    tagname = parser.getName();//br
                    if (text == null)
                        text = "";
                    text = parser.getText();// second value
                    text = text.replace("\n", "").trim();
                    if (!text.equalsIgnoreCase("No Routes Found"))
                        list.add(text);
                }
                parser.next();

            }
            if (list.size() >= 2) {
                list.remove(list.size() - 1);
            }
            Log.d("this is Serving", "list of serving");
            for (String temp :
                    list) {
                Log.d("this is Serving", temp);
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    private String getValueOfTableRow(XmlPullParser parser) {
        String text = "";
        try {
            parser.next();
            parser.next();
            parser.next();
            parser.next();
            text = parser.getText();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (text == null)
            text = "";
        return text;

    }
}
