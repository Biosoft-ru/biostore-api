package ru.biosoft.biostoreapi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONObject;

/**
 * Utility functions to communicate with biostore server
 */
public class BiostoreConnector
{
    protected Map<String, String> sessionCookies = new HashMap<>();

    protected String serverLink;

    protected String serverKey;

    public static final String BIOSTORE_DEFAULT_URL = "https://bio-store.org/biostore";

    public static String getDefaultBiostoreLink()
    {
        return BIOSTORE_DEFAULT_URL;
    }

    static BiostoreConnector getDefaultConnector(String serverName)
    {
        return new BiostoreConnector( BIOSTORE_DEFAULT_URL + "/permission", serverName );
    }

    static BiostoreConnector getConnector(String bioStoreUrl, String serverName)
    {
        return new BiostoreConnector( bioStoreUrl + "/permission", serverName );
    }

    private BiostoreConnector(String serverLink, String serverKey)
    {
        this.serverLink = serverLink;
        this.serverKey = serverKey;
    }

    /**
     * Check is session exist for user
     * @param username current user name
     * @return
     */
    public boolean checkSession(String username)
    {
        return sessionCookies.containsKey( username );
    }

    /**
     * Request biostore server using HTTPS protocol
     * @param username current user name
     * @param action name of biostore action
     * @param parameters action parameters
     * @return request result as JSON object
     */
    public JSONObject askServer(String username, String action, Map<String, String> parameters)
    {
        //TODO: check if network configuration is necessary
        try
        {
            StringBuilder urlParameters = new StringBuilder();
            urlParameters.append( "action=" ).append( encodeURL( action ) );
            if( serverKey != null )
            {
                urlParameters.append( "&serverName=" ).append( encodeURL( serverKey ) );
            }
            if( parameters != null )
            {
                for( Map.Entry<String, String> entry : parameters.entrySet() )
                {
                    urlParameters.append( "&" ).append( entry.getKey() ).append( "=" ).append( encodeURL( entry.getValue() ) );
                }
            }

            HttpURLConnection urlc = (HttpURLConnection) new URL( serverLink ).openConnection();
            urlc.setRequestMethod("POST");

            urlc.setUseCaches( false ); // Don't look at possibly cached data
            final int TIMEOUT_TEN_MINUTES = 10 * 60 * 1000;
            urlc.setConnectTimeout( TIMEOUT_TEN_MINUTES );
            urlc.setReadTimeout( TIMEOUT_TEN_MINUTES );
            String oldCookies = username == null ? null : sessionCookies.get( username );
            if( oldCookies != null )
            {
                //set cookie for session support
                urlc.setRequestProperty( "Cookie", oldCookies );
            }

            urlc.setDoOutput( true );
            try( DataOutputStream wr = new DataOutputStream( urlc.getOutputStream() ) )
            {
                wr.writeBytes( urlParameters.toString() );
                wr.flush();
            }

            //read cookies from server response
            List<String> cookies = urlc.getHeaderFields().get( "Set-Cookie" );
            if( cookies != null )
            {
                String cookieHeader = cookies.stream().map( cookie -> cookie.split( ";" )[0] ).collect( Collectors.joining( "; " ) );
                if( username != null )
                    sessionCookies.put( username, cookieHeader );
            }

            return new JSONObject( readAsString( urlc.getInputStream() ) );
        }
        catch( Exception e )
        {
            throw new RuntimeException( "Error during connection to server", e ); //TODO: rework
        }
    }

    public static String encodeURL(String src)
    {
        try
        {
            return URLEncoder.encode( src, "UTF-8" );
        }
        catch( UnsupportedEncodingException e )
        {
            throw new RuntimeException( "Incorrect symbols in URL", e ); //TODO: rework
        }
    }

    private static String readAsString(InputStream src) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final int BUFFER_SIZE = 64 * 1024;
        try( BufferedOutputStream bos = new BufferedOutputStream( baos );
                BufferedInputStream bis = src instanceof BufferedInputStream ? (BufferedInputStream)src : new BufferedInputStream( src ) )
        {

            byte[] buffer = new byte[BUFFER_SIZE];
            int len;

            while( ( len = bis.read( buffer ) ) != -1 )
            {
                bos.write( buffer, 0, len );
            }
        }
        return baos.toString( "UTF-8" );
    }
}
