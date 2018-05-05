package ru.biosoft.biostoreapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;
import ru.biosoft.biostoreapi.impl.BiostoreConnectorImpl;

public class DefaultConnectionProvider
{
    protected static final Logger log = Logger.getLogger( DefaultConnectionProvider.class.getName() );

    public static final String ACTION_LOGIN = "login";
    public static final String ACTION_LOGOUT = "logout";
    public static final String ACTION_CREATE_PROJECT = "createProject";
    public static final String ACTION_ADD_TO_PROJECT = "addToProject";
    public static final String ACTION_REFRESH_J_W_TOKEN = "refreshJWToken";
    public static final String ACTION_CHANGE_ROLE_IN_PROJECT = "changeRoleInProject";
    public static final String ACTION_PROJECT_USERS = "projectUsers";

    public static final String TYPE_OK = "ok";
    public static final String TYPE_ERROR = "error";
    public static final String TYPE_NEED_LOGIN = "unauthorized";

    public static final String ATTR_JWTOKEN = "jwtoken";
    public static final String ATTR_USERNAME = "username";
    public static final String ATTR_PASSWORD = "password";
    public static final String ATTR_IP = "ip";
    public static final String ATTR_SUDO = "sudo";

    public static final String ATTR_GROUP = "group";
    public static final String ATTR_MODULE = "module";
    public static final String ATTR_GROUP_USER = "user";
    public static final String ATTR_GROUP_ROLE = "role";
    public static final String ATTR_PROJECT_NAME = "projectName";

    public static final String ATTR_TYPE = "type";
    public static final String ATTR_MESSAGE = "message";
    public static final String ATTR_PERMISSION = "permission";

    private static final long MAX_PERMISSION_TIME = 1000L * 60 * 60 * 24 * 365; // 365 days

    protected BiostoreConnector biostoreConnector;

    private static final String BIOSTORE_DEFAULT_URL = "https://bio-store.org/biostore";

    public DefaultConnectionProvider(String serverName)
    {
        biostoreConnector = new BiostoreConnectorImpl( BIOSTORE_DEFAULT_URL + "/permission", serverName );
    }

    public DefaultConnectionProvider(BiostoreConnector biostoreConnector)
    {
        this.biostoreConnector = biostoreConnector;
    }

    public UserPermissions authorize(String username, String password, String remoteAddress)
    {
        Map<String, String> parameters = prepareLoginParametersMap( username, password );
        if( remoteAddress != null )
            parameters.put( ATTR_IP, remoteAddress );
        JSONObject response = biostoreConnector.askServer( username, ACTION_LOGIN, parameters );

        String status = response.getString( ATTR_TYPE );
        if( status.equals( TYPE_OK ) )
        {
            String[] products = getProducts( response ).toArray( String[]::new );
            UserPermissions result = new UserPermissions( username, password, products, getLimits( response ) );
            initPermissions( result, response );
            return result;
        }
        else
        {
            if( response.opt( ATTR_MESSAGE ) != null )
            {
                log.severe( "While authorizing " + username + " (" + remoteAddress + "):" + response.get( ATTR_MESSAGE ) );
                throw new SecurityException( response.getString( ATTR_MESSAGE ) );
            }
            else
            {
                throw new SecurityException( response.toString() );
            }
        }
    }

    private static Map<String, Long> getLimits(JSONObject response)
    {
        return arrayOfObjects( response.getJSONArray( "limits" ) )
                .collect( Collectors.toMap( limit -> limit.getString( "name" ), limit -> limit.getLong( "value" ) ) );
    }

    private static Stream<String> getProducts(JSONObject response)
    {
        return arrayOfObjects( response.getJSONArray( "products" ) ).map( val -> val.getString( "name" ) );
    }

    private void initPermissions(UserPermissions userPermissions, JSONObject response)
    {
        Hashtable<String, Permission> dbToPermission = userPermissions.getDbToPermission();
        long time = System.currentTimeMillis() + MAX_PERMISSION_TIME;
        if( response.optBoolean( "admin", false ) )
        {
            dbToPermission.put( "/", new Permission( Permission.ADMIN, userPermissions.getUser(), "", time ) );
        }
        else
        {
            arrayOfObjects( response.getJSONArray( "permissions" ) ).forEach( obj -> dbToPermission.put( obj.getString( "path" ),
                    new Permission( obj.getInt( "permissions" ), userPermissions.getUser(), "", time ) ) );
        }
        //TODO: rework or remove
        arrayOfObjects( response.getJSONArray( "groups" ) ).map( val -> val.getString( "name" ) )
                .forEach( name -> dbToPermission.put( "groups/" + name,
                        new Permission( Permission.READ, userPermissions.getUser(), "", time ) ) );
    }

    public static Stream<JSONObject> arrayOfObjects(JSONArray value)
    {
        List<JSONObject> arr = new ArrayList<>();
        for( int i = 0; i < value.length(); i++ )
        {
            arr.add( value.getJSONObject( i ) );
        }
        return arr.stream();
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

    public List<Project> getProjectList(JWToken jwToken)
    {
        Map<String, String> parameters = new HashMap<>();
        parameters.put( ATTR_JWTOKEN, jwToken.getTokenValue() );
        String username = jwToken.getUsername();
        JSONObject response = biostoreConnector.askServer( username, ACTION_LOGIN, parameters );

        String status = response.getString( ATTR_TYPE );
        if( status.equals( TYPE_OK ) )
        {
            return arrayOfObjects( response.getJSONArray( "permissions" ) )
                    .map( Project::createFromJSON )
                    .filter( Objects::nonNull )
                    .collect( Collectors.toList() );
        }
        else
        {
            if( response.opt( ATTR_MESSAGE ) != null )
            {
                log.severe( "While authorizing " + username + ":" + response.get( ATTR_MESSAGE ) );
                throw new SecurityException( response.getString( ATTR_MESSAGE ) );
            }
            else
            {
                throw new SecurityException( response.toString() );
            }
        }
    }

    public void createProjectWithPermissions(JWToken jwToken, String projectName, int permission)
    {
        Map<String, String> parameters = new HashMap<>();
        parameters.put( ATTR_JWTOKEN, jwToken.getTokenValue() );
        parameters.put( ATTR_PROJECT_NAME, projectName );
        parameters.put( ATTR_PERMISSION, String.valueOf( permission ) );

        JSONObject jsonResponse = biostoreConnector.askServer( jwToken.getUsername(), ACTION_CREATE_PROJECT, parameters );
        if( !TYPE_OK.equals( jsonResponse.getString( ATTR_TYPE ) ) )
        {
            log.severe( jsonResponse.getString( ATTR_MESSAGE ) );
            throw new SecurityException( jsonResponse.getString( ATTR_MESSAGE ) );
        }
    }

    public void addUserToProject(JWToken jwToken, String userToAdd, String projectName)
    {
        Map<String, String> parameters = new HashMap<>();
        parameters.put( ATTR_JWTOKEN, jwToken.getTokenValue() );
        parameters.put( ATTR_PROJECT_NAME, projectName );
        parameters.put( ATTR_GROUP_USER, userToAdd );

        JSONObject jsonResponse = biostoreConnector.askServer( jwToken.getUsername(), ACTION_ADD_TO_PROJECT, parameters );
        if( !TYPE_OK.equals( jsonResponse.getString( ATTR_TYPE ) ) )
        {
            log.severe( jsonResponse.getString( ATTR_MESSAGE ) );
            throw new SecurityException( jsonResponse.getString( ATTR_MESSAGE ) );
        }
    }

    public void changeUserRoleInProject(JWToken jwToken, String projectName, String userToChange, String newRole)
    {
        Map<String, String> params = new HashMap<>();
        params.put( ATTR_JWTOKEN, jwToken.getTokenValue() );
        params.put( ATTR_GROUP_USER, userToChange );
        params.put( ATTR_PROJECT_NAME, projectName );
        params.put( ATTR_GROUP_ROLE, newRole );

        JSONObject jsonResponse = biostoreConnector.askServer( jwToken.getUsername(), ACTION_CHANGE_ROLE_IN_PROJECT, params );
        if( !TYPE_OK.equals( jsonResponse.getString( ATTR_TYPE ) ) )
        {
            log.severe( jsonResponse.getString( ATTR_MESSAGE ) );
            throw new SecurityException( jsonResponse.getString( ATTR_MESSAGE ) );
        }
    }

    public List<ProjectUser> getProjectUsers(JWToken jwToken, String projectName)
    {
        Map<String, String> params = new HashMap<>();
        params.put( ATTR_JWTOKEN, jwToken.getTokenValue() );
        params.put( ATTR_PROJECT_NAME, projectName );

        JSONObject jsonResponse = biostoreConnector.askServer( jwToken.getUsername(), ACTION_PROJECT_USERS, params );
        if( !TYPE_OK.equals( jsonResponse.getString( ATTR_TYPE ) ) )
        {
            log.severe( jsonResponse.getString( ATTR_MESSAGE ) );
            throw new SecurityException( jsonResponse.getString( ATTR_MESSAGE ) );
        }
        return arrayOfObjects( jsonResponse.getJSONArray( "projectUsers" ) )
                .map( ProjectUser::createFromJSON )
                .filter( Objects::nonNull )
                .sorted()
                .collect( Collectors.toList() );
    }

    public JWToken getJWToken(String username, String password)
    {
        Map<String, String> parameters = prepareLoginParametersMap( username, password );
        JSONObject response = biostoreConnector.askServer( username, ACTION_LOGIN, parameters );

        String status = response.getString( ATTR_TYPE );
        if( status.equals( TYPE_OK ) )
        {
            String jwToken = response.getString( ATTR_JWTOKEN );
            if( jwToken == null )
                throw new SecurityException( "Specified server does not support json web tokens." );
            return new JWToken( username, jwToken );
        }
        else
        {
            if( response.opt( ATTR_MESSAGE ) != null )
            {
                log.severe( "While authorizing " + username + ":" + response.getString( ATTR_MESSAGE ) );
                throw new SecurityException( response.getString( ATTR_MESSAGE ) );
            }
            else
            {
                throw new SecurityException( response.toString() );
            }
        }
    }

    public JWToken refreshJWToken(JWToken jwToken)
    {
        Map<String, String> parameters = new HashMap<>();
        parameters.put( ATTR_JWTOKEN, jwToken.getTokenValue() );
        JSONObject response = biostoreConnector.askServer( jwToken.getUsername(), ACTION_REFRESH_J_W_TOKEN, parameters );

        String status = response.getString( ATTR_TYPE );
        if( status.equals( TYPE_OK ) )
        {
            String jwTokenStr = response.getString( ATTR_JWTOKEN );
            if( jwTokenStr == null )
                throw new SecurityException( "Specified server does not support json web tokens." );
            return new JWToken( jwToken.getUsername(), jwTokenStr );
        }
        else if( response.opt( ATTR_MESSAGE ) != null )
        {
            log.severe( "While refreshing token for " + jwToken.getUsername() + ":" + response.getString( ATTR_MESSAGE ) );
            throw new SecurityException( response.getString( ATTR_MESSAGE ) );
        }
        else
        {
            throw new SecurityException( response.toString() );
        }
    }

    public void logout(JWToken jwToken)
    {
        Map<String, String> parameters = new HashMap<>();
        parameters.put( ATTR_JWTOKEN, jwToken.getTokenValue() );

        JSONObject jsonResponse = biostoreConnector.askServer( jwToken.getUsername(), ACTION_LOGOUT, parameters );
        if( !TYPE_OK.equals( jsonResponse.getString( ATTR_TYPE ) ) )
        {
            log.severe( jsonResponse.getString( ATTR_MESSAGE ) );
            throw new SecurityException( jsonResponse.getString( ATTR_MESSAGE ) );
        }
    }
}
