package awltour2013.geoloc;

import android.app.Application;

import com.google.android.gms.maps.model.Marker;

public class GeoLocApp extends Application{
    private Marker tmpMarker;

    public void setTmpMarker(Marker marker)
    {
        this.tmpMarker = marker;
    }

    public Marker getTmpMarker()
    {
        return tmpMarker;
    }
}
