package awltour2013.geoloc;

import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Http {
    protected String address; //The address of the server.
    private HttpClient httpClient;

    private int connectionTimeout = 10000; //Determines the timeout in milliseconds until a connection is established.
    private int soTimeout = 10000; //The maximum period inactivity in milliseconds between two consecutive data packets.

    public Http(String address)
    {
        this.address = address;
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, connectionTimeout);
        HttpConnectionParams.setSoTimeout(httpParams, soTimeout);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        SocketFactory plainSocketFactory = PlainSocketFactory.getSocketFactory();
        schemeRegistry.register(new Scheme("http", plainSocketFactory, 80));
        ClientConnectionManager cm = new ThreadSafeClientConnManager(httpParams, schemeRegistry);

        httpClient = new DefaultHttpClient(cm,httpParams);
    }

    protected List execute() throws URISyntaxException, IOException {
        HttpGet httpGet = new HttpGet(new URI(address));
        Log.d(this.getClass().getName(), "Get coords http request ...");

        HttpResponse httpResponse = httpClient.execute(httpGet);
        ArrayList res = new ArrayList();
        res.add(0, httpResponse.getStatusLine().getStatusCode());
        res.add(1,  IOUtils.toString(httpResponse.getEntity().getContent()));
        return res;
    }
}
