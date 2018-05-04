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
    public void authorize()
    {
        String res = "{'permissions':[{'path':'data/Collaboration/Demo','permissions':3}],'jwtoken':'123123',"
                + "'admin':false,'groups':[],'type':'ok','limits':[],'products':[{'name':'Server'}]}";
        when( mock.askServer( eq( "" ), eq( ACTION_LOGIN ), any() ) ).thenReturn( new JSONObject( doubleQuotes( res ) ) );

        UserPermissions authorize = test.authorize( "", "", null );

        assertEquals( 1, authorize.getDbToPermission().size() );
        assertEquals( 3, authorize.getDbToPermission().get( "data/Collaboration/Demo" ).getPermissions() );
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
    public void projectList()
    {
        JWToken jwToken = new JWToken( "test", "123123" );

        String res = "{'permissions':[{'path':'data/Collaboration/Demo','permissions':3}],'jwtoken':'123123',"
                + "'admin':false,'groups':[],'type':'ok','limits':[],'products':[{'name':'Server'}]}";
        Map<String, String> params = Maps.builder().put( ATTR_JWTOKEN, jwToken.getTokenValue() ).build();
        when( mock.askServer( eq( "test" ), eq( ACTION_LOGIN ), eq( params ) ) ).thenReturn( new JSONObject( doubleQuotes( res ) ) );

        List<Project> projectList = test.getProjectList( jwToken );

        assertEquals( 1, projectList.size() );
        assertEquals( "Demo (Info/Read)", projectList.get( 0 ).toString() );
        assertEquals( "Demo", projectList.get( 0 ).getProjectName() );
        assertEquals( 3, projectList.get( 0 ).getPermissions() );
    }

    @Test
    public void errorLogin()
    {
        String res = constructErrorResponse( "Incorrect email or password" );
        when( mock.askServer( eq( "errorName" ), eq( ACTION_LOGIN ), any() ) ).thenReturn( new JSONObject( doubleQuotes( res ) ) );

        test.authorize( "errorName", "", null );
    }

    @Test
    public void errorAddUserToProject()
    {
        JWToken jwToken = new JWToken( "test", "123123" );

        String res = constructErrorResponse( "Only group or server administrator can add users to project" );
        Map<String, String> params = Maps.builder().put( ATTR_JWTOKEN, jwToken.getTokenValue() ).put( ATTR_GROUP_USER, "testUser" )
                .put( ATTR_PROJECT_NAME, "Demo" ).build();
        when( mock.askServer( eq( "test" ), eq( ACTION_ADD_TO_PROJECT ), eq( params ) ) )
                .thenReturn( new JSONObject( doubleQuotes( res ) ) );

        test.addUserToProject( jwToken, "testUser", "Demo" );
    }

    private String constructErrorResponse(String errorMessage)
    {
        thrown.expect( SecurityException.class );
        thrown.expectMessage( errorMessage );

        return "{'type':'error','message':'" + errorMessage + "'}";
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