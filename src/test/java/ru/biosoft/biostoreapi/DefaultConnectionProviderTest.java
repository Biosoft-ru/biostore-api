package ru.biosoft.biostoreapi;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.biosoft.biostoreapi.DefaultConnectionProvider.*;


public class DefaultConnectionProviderTest
{
    private BiostoreConnector mock;
    private DefaultConnectionProvider test;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception
    {
        mock = mock( BiostoreConnector.class );
        test = new DefaultConnectionProvider( mock );
    }

    @Test
    public void logout()
    {
        JWToken jwToken = new JWToken( "test", "123123" );
        String res = "{'type':'ok'}";
        Map<String, String> params = Maps.builder().put( ATTR_JWTOKEN, jwToken.getTokenValue() ).build();
        when( mock.askServer( eq( jwToken.getUsername() ), eq( ACTION_LOGOUT ), eq( params ) ) )
                .thenReturn( new JSONObject( doubleQuotes( res ) ) );

        test.logout( jwToken );
    }

    @Test
    public void jwToken()
    {
        String res = "{'type':'ok','jwtoken':'123123'}";
        Map<String, String> params = Maps.builder().put( ATTR_USERNAME, "test" ).put( ATTR_PASSWORD, "test" ).build();
        when( mock.askServer( eq( "test" ), eq( ACTION_LOGIN ), eq( params ) ) ).thenReturn( new JSONObject( doubleQuotes( res ) ) );

        JWToken jwToken = test.getJWToken( "test", "test" );
        assertEquals( "123123", jwToken.getTokenValue() );
        assertEquals( "test", jwToken.getUsername() );
    }

    @Test
    public void refreshJWToken()
    {
        JWToken jwToken = new JWToken( "test", "123123" );

        String res = "{'type':'ok','jwtoken':'321321'}";
        Map<String, String> params = Maps.builder().put( ATTR_JWTOKEN, jwToken.getTokenValue() ).build();
        when( mock.askServer( eq( jwToken.getUsername() ), eq( ACTION_REFRESH_J_W_TOKEN ), eq( params ) ) )
                .thenReturn( new JSONObject( doubleQuotes( res ) ) );

        JWToken newJWtoken = test.refreshJWToken( jwToken );
        assertEquals( "test", newJWtoken.getUsername() );
        assertEquals( "321321", newJWtoken.getTokenValue() );
    }

    @Test
    public void projectList()
    {
        JWToken jwToken = new JWToken( "test", "123123" );

        String res = "{'projectList':[{'path':'data/Collaboration/Demo','permissions':3}],'type':'ok'}";
        Map<String, String> params = Maps.builder().put( ATTR_JWTOKEN, jwToken.getTokenValue() ).build();
        when( mock.askServer( eq( jwToken.getUsername() ), eq( ACTION_GET_PROJECT_LIST ), eq( params ) ) )
                .thenReturn( new JSONObject( doubleQuotes( res ) ) );

        List<Project> projectList = test.getProjectList( jwToken );

        assertEquals( 1, projectList.size() );
        assertEquals( "Demo (Info/Read)", projectList.get( 0 ).toString() );
        assertEquals( "Demo", projectList.get( 0 ).getProjectName() );
        assertEquals( 3, projectList.get( 0 ).getPermissions() );
    }

    @Test
    public void createProject()
    {
        JWToken jwToken = new JWToken( "test", "123123" );

        String res = "{'type':'ok'}";
        Map<String, String> params = Maps.builder().put( ATTR_JWTOKEN, jwToken.getTokenValue() ).put( ATTR_PROJECT_NAME, "newProject" )
                .put( ATTR_PERMISSION, "7" ).build();
        when( mock.askServer( eq( jwToken.getUsername() ), eq( ACTION_CREATE_PROJECT ), eq( params ) ) )
                .thenReturn( new JSONObject( doubleQuotes( res ) ) );

        test.createProjectWithPermissions( jwToken, "newProject", 7 );
    }

    @Test
    public void addUserToProject()
    {
        JWToken jwToken = new JWToken( "test", "123123" );

        String res = "{'type':'ok'}";
        Map<String, String> params = Maps.builder().put( ATTR_JWTOKEN, jwToken.getTokenValue() ).put( ATTR_GROUP_USER, "testUser" )
                .put( ATTR_PROJECT_NAME, "Demo" ).build();
        when( mock.askServer( eq( jwToken.getUsername() ), eq( ACTION_ADD_TO_PROJECT ), eq( params ) ) )
                .thenReturn( new JSONObject( doubleQuotes( res ) ) );

        test.addUserToProject( jwToken, "testUser", "Demo" );
    }

    @Test
    public void changeUserRole()
    {
        JWToken jwToken = new JWToken( "test", "123123" );

        String res = "{'type':'ok'}";
        Map<String, String> params = Maps.builder().put( ATTR_JWTOKEN, jwToken.getTokenValue() ).put( ATTR_GROUP_USER, "testUser" )
                .put( ATTR_PROJECT_NAME, "Demo" ).put( ATTR_GROUP_ROLE, "User" ).build();
        when( mock.askServer( eq( jwToken.getUsername() ), eq( ACTION_CHANGE_ROLE_IN_PROJECT ), eq( params ) ) )
                .thenReturn( new JSONObject( doubleQuotes( res ) ) );

        test.changeUserRoleInProject( jwToken, "Demo", "testUser", "User" );
    }

    @Test
    public void projectUsers()
    {
        JWToken jwToken = new JWToken( "test", "123123" );

        String res = "{'type':'ok','projectUsers':[{'role':'User','user':'testUser'},"
                + "{'role':'Administrator','user':'projectAdmin'},{'role':'User','user':'test'}]}";
        Map<String, String> params = Maps.builder().put( ATTR_JWTOKEN, jwToken.getTokenValue() ).put( ATTR_PROJECT_NAME, "Demo" ).build();
        when( mock.askServer( eq( jwToken.getUsername() ), eq( ACTION_PROJECT_USERS ), eq( params ) ) )
                .thenReturn( new JSONObject( doubleQuotes( res ) ) );

        List<ProjectUser> users = test.getProjectUsers( jwToken, "Demo" );
        assertEquals( 3, users.size() );
        checkProjectUser( users.get( 0 ), "projectAdmin", "Administrator" );
        checkProjectUser( users.get( 1 ), "test", "User" );
        checkProjectUser( users.get( 2 ), "testUser", "User" );
    }

    private void checkProjectUser(ProjectUser pu, String expectedUser, String expectedRole)
    {
        assertEquals( expectedUser, pu.getUser() );
        assertEquals( expectedRole, pu.getRole() );
    }

    @Test
    public void errorLogout()
    {
        JWToken jwToken = new JWToken( "test", "123123" );

        String res = constructMessageResponse( "User not logged in", TYPE_NEED_LOGIN );
        Map<String, String> params = Maps.builder().put( ATTR_JWTOKEN, jwToken.getTokenValue() ).build();
        when( mock.askServer( eq( jwToken.getUsername() ), eq( ACTION_LOGOUT ), eq( params ) ) )
                .thenReturn( new JSONObject( doubleQuotes( res ) ) );

        test.logout( jwToken );
    }

    @Test
    public void errorJWToken()
    {
        String res = constructErrorResponse( "Can not get token" );
        when( mock.askServer( eq( "" ), eq( ACTION_LOGIN ), any() ) ).thenReturn( new JSONObject( doubleQuotes( res ) ) );

        test.getJWToken( "", "" );
    }

    @Test
    public void errorJWTokenNoMessage()
    {
        String res = constructErrorResponse( null );
        when( mock.askServer( eq( "" ), eq( ACTION_LOGIN ), any() ) ).thenReturn( new JSONObject( doubleQuotes( res ) ) );

        test.getJWToken( "", "" );
    }

    @Test
    public void errorRefreshJWToken()
    {
        JWToken jwToken = new JWToken( "test", "123123" );

        String res = constructErrorResponse( "Can not refresh token" );
        Map<String, String> params = Maps.builder().put( ATTR_JWTOKEN, jwToken.getTokenValue() ).build();
        when( mock.askServer( eq( jwToken.getUsername() ), eq( ACTION_REFRESH_J_W_TOKEN ), eq( params ) ) )
                .thenReturn( new JSONObject( doubleQuotes( res ) ) );

        test.refreshJWToken( jwToken );
    }

    @Test
    public void errorRefreshJWTokenNoMessage()
    {
        JWToken jwToken = new JWToken( "test", "123123" );

        String res = constructErrorResponse( null );
        Map<String, String> params = Maps.builder().put( ATTR_JWTOKEN, jwToken.getTokenValue() ).build();
        when( mock.askServer( eq( jwToken.getUsername() ), eq( ACTION_REFRESH_J_W_TOKEN ), eq( params ) ) )
                .thenReturn( new JSONObject( doubleQuotes( res ) ) );

        test.refreshJWToken( jwToken );
    }

    @Test
    public void errorProjectList()
    {
        JWToken jwToken = new JWToken( "test", "123123" );

        String res = constructErrorResponse( "Can not get project list" );
        Map<String, String> params = Maps.builder().put( ATTR_JWTOKEN, jwToken.getTokenValue() ).build();
        when( mock.askServer( eq( jwToken.getUsername() ), eq( ACTION_GET_PROJECT_LIST ), eq( params ) ) )
                .thenReturn( new JSONObject( doubleQuotes( res ) ) );

        test.getProjectList( jwToken );
    }

    @Test
    public void errorProjectListNoMessage()
    {
        JWToken jwToken = new JWToken( "test", "123123" );

        String res = constructErrorResponse( null );
        Map<String, String> params = Maps.builder().put( ATTR_JWTOKEN, jwToken.getTokenValue() ).build();
        when( mock.askServer( eq( jwToken.getUsername() ), eq( ACTION_GET_PROJECT_LIST ), eq( params ) ) )
                .thenReturn( new JSONObject( doubleQuotes( res ) ) );

        test.getProjectList( jwToken );
    }

    @Test
    public void errorCreateProject()
    {
        JWToken jwToken = new JWToken( "test", "123123" );

        String res = constructErrorResponse( "Cannot create project" );
        Map<String, String> params = Maps.builder().put( ATTR_JWTOKEN, jwToken.getTokenValue() ).put( ATTR_PROJECT_NAME, "newProject" )
                .put( ATTR_PERMISSION, "7" ).build();
        when( mock.askServer( eq( jwToken.getUsername() ), eq( ACTION_CREATE_PROJECT ), eq( params ) ) )
                .thenReturn( new JSONObject( doubleQuotes( res ) ) );

        test.createProjectWithPermissions( jwToken, "newProject", 7 );
    }

    @Test
    public void errorAddUserToProject()
    {
        JWToken jwToken = new JWToken( "test", "123123" );

        String res = constructErrorResponse( "Only group or server administrator can add users to project" );
        Map<String, String> params = Maps.builder().put( ATTR_JWTOKEN, jwToken.getTokenValue() ).put( ATTR_GROUP_USER, "testUser" )
                .put( ATTR_PROJECT_NAME, "Demo" ).build();
        when( mock.askServer( eq( jwToken.getUsername() ), eq( ACTION_ADD_TO_PROJECT ), eq( params ) ) )
                .thenReturn( new JSONObject( doubleQuotes( res ) ) );

        test.addUserToProject( jwToken, "testUser", "Demo" );
    }

    @Test
    public void errorChangeUserRole()
    {
        JWToken jwToken = new JWToken( "test", "123123" );

        String res = constructErrorResponse( "Can not change user role" );
        Map<String, String> params = Maps.builder().put( ATTR_JWTOKEN, jwToken.getTokenValue() ).put( ATTR_GROUP_USER, "testUser" )
                .put( ATTR_PROJECT_NAME, "Demo" ).put( ATTR_GROUP_ROLE, "User" ).build();
        when( mock.askServer( eq( jwToken.getUsername() ), eq( ACTION_CHANGE_ROLE_IN_PROJECT ), eq( params ) ) )
                .thenReturn( new JSONObject( doubleQuotes( res ) ) );

        test.changeUserRoleInProject( jwToken, "Demo", "testUser", "User" );
    }

    @Test
    public void errorProjectUsers()
    {
        JWToken jwToken = new JWToken( "test", "123123" );

        String res = constructErrorResponse( "Can not get project users list" );
        Map<String, String> params = Maps.builder().put( ATTR_JWTOKEN, jwToken.getTokenValue() ).put( ATTR_PROJECT_NAME, "Demo" ).build();
        when( mock.askServer( eq( jwToken.getUsername() ), eq( ACTION_PROJECT_USERS ), eq( params ) ) )
                .thenReturn( new JSONObject( doubleQuotes( res ) ) );

        test.getProjectUsers( jwToken, "Demo" );
    }

    private String constructErrorResponse(String errorMessage)
    {
        return constructMessageResponse( errorMessage, TYPE_ERROR );
    }
    private String constructMessageResponse(String message, String type)
    {
        String response = "{'type':'" + type;
        if( message != null )
            response += "','message':'" + message;
        response += "'}";

        thrown.expect( SecurityException.class );
        thrown.expectMessage( message == null ? response.replace( '\'', '"' ) : message );

        return response;
    }

    private static String doubleQuotes(Object s)
    {
        return s.toString().replace( "'", "\"" );
    }

    public static class Maps
    {
        private final Map<String, String> map;

        private Maps()
        {
            map = new HashMap<>();
        }

        public static Maps builder()
        {
            return new Maps();
        }

        public Maps put(String key, String value)
        {
            map.put( key, value );
            return this;
        }

        public Map<String, String> build()
        {
            return map;
        }
    }
}