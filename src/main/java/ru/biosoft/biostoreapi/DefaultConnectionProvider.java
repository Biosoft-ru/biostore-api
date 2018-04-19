package ru.biosoft.biostoreapi;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class DefaultConnectionProvider
{
    protected static final Logger log = Logger.getLogger( DefaultConnectionProvider.class.getName() );

    public static final String ACTION_LOGIN = "login";
    public static final String ACTION_CREATE_PROJECT = "createProject";

    public static final String TYPE_OK = "ok";
    //    public static final String TYPE_ERROR = "error";
    //    public static final String TYPE_NEED_LOGIN = "unauthorized";

    public static final String ATTR_JWTOKEN = "jwtoken";
    public static final String ATTR_USERNAME = "username";
    public static final String ATTR_PASSWORD = "password";
    public static final String ATTR_IP = "ip";
    public static final String ATTR_SUDO = "sudo";

    public static final String ATTR_GROUP = "group";
    public static final String ATTR_MODULE = "module";
    public static final String ATTR_GROUP_USER = "user";

    public static final String ATTR_TYPE = "type";
    public static final String ATTR_MESSAGE = "message";
    public static final String ATTR_PERMISSION = "permission";

    private static final long MAX_PERMISSION_TIME = 1000L * 60 * 60 * 24 * 365; // 365 days

    protected String serverName;
    protected BiostoreConnector biostoreConnector;

    public DefaultConnectionProvider(String serverName)
    {
        this.serverName = serverName;//TODO: read from props
        biostoreConnector = BiostoreConnector.getDefaultConnector( serverName );
    }

    public List<String> getProjectListWithToken(String username, String jwToken) throws SecurityException
    {
        Map<String, String> parameters = new HashMap<>();
        parameters.put( ATTR_JWTOKEN, jwToken );
        return getProjectList( biostoreConnector, username, parameters );
    }

    public List<String> getProjectList(String username, String password) throws SecurityException
    {
        Map<String, String> parameters = prepareLoginParametersMap( username, password );
        return getProjectList( biostoreConnector, username, parameters );
    }

    private static List<String> getProjectList(BiostoreConnector bc, String username, Map<String, String> parameters)
    {
        JsonObject response = bc.askServer( username, ACTION_LOGIN, parameters );
        try
        {
            String status = response.get( ATTR_TYPE ).asString();
            if( status.equals( TYPE_OK ) )
            {
                return arrayOfObjects( response.get( "permissions" ) )
                        .map( obj -> obj.get( "path" ).asString() )
                        .filter( DefaultConnectionProvider::isProjectPath )
                        .map( DefaultConnectionProvider::getProjectName )
                        .collect( Collectors.toList() );
            }
            else
            {
                if( response.get( ATTR_MESSAGE ) != null )
                {
                    log.severe( "While authorizing " + username + ":" + response.get( ATTR_MESSAGE ) );
                    throw new SecurityException( response.get( ATTR_MESSAGE ).asString() );
                }
                else
                {
                    throw new SecurityException( response.toString() );
                }
            }
        }
        catch( UnsupportedOperationException e )
        {
            log.log( Level.SEVERE, "Invalid JSON response", e );
            throw new SecurityException( "Error communicating to authentication server" );
        }
    }

    //TODO: rework
    private static boolean isProjectPath(String path)
    {
        return path.startsWith( "data/Collaboration/" ) || path.startsWith( "data/Projects/" );
    }
    private static String getProjectName(String path)
    {
        return path.replace( "data/Collaboration/", "" ).replace( "data/Projects/", "" );
    }

    public UserPermissions authorize(String username, String password, String remoteAddress) throws SecurityException
    {
        Map<String, String> parameters = prepareLoginParametersMap( username, password );
        if( remoteAddress != null )
            parameters.put( ATTR_IP, remoteAddress );
        JsonObject response = biostoreConnector.askServer( username, ACTION_LOGIN, parameters );
        try
        {
            String status = response.get( ATTR_TYPE ).asString();
            if( status.equals( TYPE_OK ) )
            {
                String[] products = getProducts( response ).toArray( String[]::new );
                UserPermissions result = new UserPermissions( username, password, products, getLimits( response ) );
                initPermissions( result, response );
                return result;
            }
            else
            {
                if( response.get( ATTR_MESSAGE ) != null )
                {
                    log.severe( "While authorizing " + username + " (" + remoteAddress + "):" + response.get( ATTR_MESSAGE ) );
                    throw new SecurityException( response.get( ATTR_MESSAGE ).asString() );
                }
                else
                {
                    throw new SecurityException( response.toString() );
                }
            }
        }
        catch( UnsupportedOperationException e )
        {
            log.log( Level.SEVERE, "Invalid JSON response", e );
            throw new SecurityException( "Error communicating to authentication server" );
        }
    }

    private static Map<String, Long> getLimits(JsonObject response)
    {
        return arrayOfObjects( response.get( "limits" ) )
                .collect( Collectors.toMap( limit -> limit.get( "name" ).asString(), limit -> limit.get( "value" ).asLong() ) );
    }

    private static Stream<String> getProducts(JsonObject response)
    {
        return arrayOfObjects( response.get( "products" ) ).map( val -> val.get( "name" ).asString() );
    }

    private void initPermissions(UserPermissions userPermissions, JsonObject response)
    {
        Hashtable<String, Permission> dbToPermission = userPermissions.getDbToPermission();
        long time = System.currentTimeMillis() + MAX_PERMISSION_TIME;
        if( response.getBoolean( "admin", false ) )
        {
            dbToPermission.put( "/", new Permission( Permission.ADMIN, userPermissions.getUser(), "", time ) );
        }
        else
        {
            arrayOfObjects( response.get( "permissions" ) ).forEach( obj -> dbToPermission.put( obj.get( "path" ).asString(),
                    new Permission( obj.get( "permissions" ).asInt(), userPermissions.getUser(), "", time ) ) );
        }
        //TODO: rework or remove
        arrayOfObjects( response.get( "groups" ) ).map( val -> val.get( "name" ).asString() )
                .forEach( name -> dbToPermission.put( "groups/" + name,
                        new Permission( Permission.READ, userPermissions.getUser(), "", time ) ) );
    }

    public static Stream<JsonObject> arrayOfObjects(JsonValue value)
    {
        return value.asArray().values().stream().map( JsonValue::asObject );
    }

    private static Map<String, String> prepareLoginParametersMap(String username, String password)
    {
        Map<String, String> parameters = new HashMap<>();
        String[] fields = username.split( "\\$" );
        parameters.put( ATTR_USERNAME, fields[0] );
        parameters.put( ATTR_PASSWORD, password );
        if( fields.length > 1 )
            parameters.put( ATTR_SUDO, fields[1] );
        return parameters;
    }

    public void createProjectWithPermissions(String username, String password, String projectName, int permission) throws Exception
    {
        Map<String, String> parameters = prepareLoginParametersMap( username, password );
        parameters.put( ATTR_GROUP_USER, username );
        parameters.put( ATTR_GROUP, projectName );
        parameters.put( ATTR_MODULE, "data/Collaboration/" + projectName );
        parameters.put( ATTR_PERMISSION, String.valueOf( permission ) );

        JsonObject jsonReponse = biostoreConnector.askServer( username, ACTION_CREATE_PROJECT, parameters );
        if( !jsonReponse.get( ATTR_TYPE ).asString().equals( TYPE_OK ) )
        {
            log.severe( jsonReponse.get( ATTR_MESSAGE ).asString() );
            throw new SecurityException( jsonReponse.get( ATTR_MESSAGE ).asString() );
        }
    }

    public String getJWToken(String username, String password) throws SecurityException
    {
        Map<String, String> parameters = prepareLoginParametersMap( username, password );
        JsonObject response = biostoreConnector.askServer( username, ACTION_LOGIN, parameters );
        try
        {
            String status = response.get( ATTR_TYPE ).asString();
            if( status.equals( TYPE_OK ) )
            {
                JsonValue jsonValue = response.get( ATTR_JWTOKEN );
                if( jsonValue == null )
                    throw new SecurityException( "Specified server does not support json web tokens." );
                return jsonValue.asString();
            }
            else
            {
                if( response.get( ATTR_MESSAGE ) != null )
                {
                    log.severe( "While authorizing " + username + ":" + response.get( ATTR_MESSAGE ) );
                    throw new SecurityException( response.get( ATTR_MESSAGE ).asString() );
                }
                else
                {
                    throw new SecurityException( response.toString() );
                }
            }
        }
        catch( UnsupportedOperationException e )
        {
            log.log( Level.SEVERE, "Invalid JSON response", e );
            throw new SecurityException( "Error communicating to authentication server" );
        }
    }
}
