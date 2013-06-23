package awltour2013.geoloc;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import us.bpsm.edn.Keyword;
import us.bpsm.edn.parser.Parseable;
import us.bpsm.edn.parser.Parser;
import us.bpsm.edn.parser.Parsers;

import static us.bpsm.edn.parser.Parsers.defaultConfiguration;
import static us.bpsm.edn.Keyword.newKeyword;

public class GeoLoc extends android.support.v4.app.FragmentActivity implements DialogInterface.OnCancelListener {

    private static final int GOOGLE_PLAY_SERVICE_REQUEST_CODE = 1;
    private GoogleMap map;

    private static final Keyword latK = newKeyword("coord", "lat");
    private static final Keyword lngK = newKeyword("coord", "lng");
    private static final Keyword instK = newKeyword("coord", "orig-tx-inst");
    private static final Keyword distK = newKeyword("coord", "distance");
    private static final Keyword speedK = newKeyword("coord", "speed");
    private static final Keyword minDistdK = newKeyword("min-dist");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.geoloc_layout);

        int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if (errorCode != ConnectionResult.SUCCESS)
        {
            GooglePlayServicesUtil.getErrorDialog(errorCode,this,GOOGLE_PLAY_SERVICE_REQUEST_CODE,this);
        }

        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .getMap();

        fireTask();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.geo_loc, menu);
        return true;
    }


    @Override
    public void onCancel(DialogInterface dialogInterface) {
        finish();
    }

    public void fireTask() {
        final Http http = new Http("http://www.awl-tour-2013.com/getdata");
        new ManagedAsyncTask<Void, Void, List>(this) {

            protected List doInBackground(Void... params) {
                try {
                    return http.execute();
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    Log.w(this.getClass().getName(), e);
                }
                return new ArrayList();
            }

            protected void onPostExecute(List result) {
                //Toast.makeText(((GeoLoc) getActivity()), result.get(0).toString(), Toast.LENGTH_LONG).show();
                //Toast.makeText(((GeoLoc) getActivity()), result.get(1).toString(), Toast.LENGTH_LONG).show();
                if ((Integer)result.get(0) == 200)
                {
                    parseData(result.get(1).toString());
                }
            };

        }.execute();
    }

    private void parseData(String data)
    {
        map.clear();
        PolylineOptions poly = new PolylineOptions();
        poly.color(Color.RED);
        poly.width(5);

        Parseable pbr = Parsers.newParseable(data);
        Parser p = Parsers.newParser(defaultConfiguration());
        List<Map> list = (List<Map>) p.nextValue(pbr);
        List<Map> listM = new ArrayList<Map>();
        listM.addAll(list);
        Collections.sort(listM, new Comparator<Map>(){
            @Override
            public int compare(Map m1, Map m2) {
                return ((Date)m1.get(instK)).compareTo((Date)m2.get(instK));
            }
        });
        for(Map m : listM)
        {
            if (m.containsKey(latK) && m.containsKey(lngK))
            {
                poly.add(new LatLng((Double)m.get(latK), (Double)m.get(lngK)));
            }
            if(m.containsKey(minDistdK) && (Boolean)m.get(minDistdK))
            {
                Marker marker = map.addMarker(new MarkerOptions()
                        .position(new LatLng((Double)m.get(latK), (Double)m.get(lngK)))
                .title(formatTime((Date) m.get(instK))));
            }
        }
        Map m = listM.get(listM.size()-1);
        if (((GeoLocApp)getApplication()).getTmpMarker() != null)
        {
            ((GeoLocApp)getApplication()).getTmpMarker().remove();
            ((GeoLocApp)getApplication()).setTmpMarker(null);
        }
        if ((!m.containsKey(minDistdK) || !(Boolean)m.get(minDistdK)) && m.containsKey(latK) && m.containsKey(lngK))
        {
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(new LatLng((Double)m.get(latK), (Double)m.get(lngK)))
                    .title(formatTime((Date) m.get(instK))));
            ((GeoLocApp)getApplication()).setTmpMarker(marker);
            Toast.makeText(this,""+m.get(instK),Toast.LENGTH_LONG).show();
        }

        map.addPolyline(poly);
    }

    private static final String[] MONTHS = {"janvier","février","mars","avril","mai","juin","juillet","août","septembre","octobre","novembre","décembre"};
    private static final String[] DAYS = {"", "Dimanche","Lundi","Mardi","Mercredi","Jeudi","Vendredi","Samedi"};

    private String formatTime(Date date)
    {
        Calendar cal=Calendar.getInstance();
        cal.setTime(date);
        String day = DAYS[cal.get(Calendar.DAY_OF_WEEK)];
        String month = MONTHS[cal.get(Calendar.MONTH)];
        return day + " " + cal.get(Calendar.DAY_OF_MONTH) + " " + month + ", " + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
    }
}
