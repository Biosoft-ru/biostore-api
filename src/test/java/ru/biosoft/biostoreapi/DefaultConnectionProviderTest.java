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

    @Before
    public void setUp() throws Exception
    {
        mock = mock(BiostoreConnector.class);
        test = new DefaultConnectionProvider(mock);
    }

    @Test
    public void authorize()
    {
        String res = "{'permissions':[{'path':'data/Collaboration/Demo','permissions':3}],'jwtoken':'123123','admin':false,'groups':[],'type':'ok','limits':[],'products':[{'name':'Server'}]}";
        when(mock.askServer(eq(""), eq(ACTION_LOGIN), any())).thenReturn(new JSONObject(doubleQuotes(res)));

        UserPermissions authorize = test.authorize("", "", null);

        assertEquals(1, authorize.getDbToPermission().size());
        assertEquals(3, authorize.getDbToPermission().get("data/Collaboration/Demo").getPermissions());
    }

    @Test
    public void projectList()
    {
        String res = "{'permissions':[{'path':'data/Collaboration/Demo','permissions':3}],'jwtoken':'123123','admin':false,'groups':[],'type':'ok','limits':[],'products':[{'name':'Server'}]}";
        when(mock.askServer(eq(""), eq(ACTION_LOGIN), any())).thenReturn(new JSONObject(doubleQuotes(res)));

        List<Project> projectList = test.getProjectList( "", "" );

        assertEquals( 1, projectList.size() );
        assertEquals( "Demo (Info/Read)", projectList.get( 0 ).toString() );
        assertEquals( "Demo", projectList.get( 0 ).getProjectName() );
        assertEquals( 3, projectList.get( 0 ).getPermissions() );
    }

    @Test
    public void projectListWithToken()
    {
        String token = "{'type':'ok','jwtoken':'123123'}";
        Map<String, String> params1 = Maps.builder().put(ATTR_USERNAME, "test").put(ATTR_PASSWORD, "test").build();
        when(mock.askServer(eq("test"), eq(ACTION_LOGIN), eq(params1))).thenReturn(new JSONObject(doubleQuotes(token)));

        JWToken jwToken = test.getJWToken("test", "test");
        assertEquals("123123", jwToken.getTokenValue());

        String res = "{'permissions':[{'path':'data/Collaboration/Demo','permissions':3}],'jwtoken':'123123','admin':false,'groups':[],'type':'ok','limits':[],'products':[{'name':'Server'}]}";
        Map<String, String> params2 = Maps.builder().put(ATTR_JWTOKEN, "123123").build();
        when(mock.askServer(eq("test"), eq(ACTION_LOGIN), eq(params2))).thenReturn(new JSONObject(doubleQuotes(res)));

        List<Project> projectList = test.getProjectList(jwToken);

        assertEquals( 1, projectList.size() );
        assertEquals( "Demo (Info/Read)", projectList.get( 0 ).toString() );
        assertEquals( "Demo", projectList.get( 0 ).getProjectName() );
        assertEquals( 3, projectList.get( 0 ).getPermissions() );
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void errorLogin()
    {
        thrown.expect( SecurityException.class );
        thrown.expectMessage( "Incorrect email or password" );

        String res = "{'type':'error','message':'Incorrect email or password'}";
        when(mock.askServer(eq("errorName"), eq(ACTION_LOGIN), any())).thenReturn(new JSONObject(doubleQuotes(res)));

        test.authorize("errorName", "", null);
    }

    private static String doubleQuotes(Object s)
    {
        return s.toString().replace("'", "\"");
    }

    public static class Maps {

        private Map<String, String> map;

        private Maps(){
            map = new HashMap<>();
        }

        public static Maps builder(){
            return new Maps();
        }

        public Maps put(String key, String value) {
            map.put(key, value);
            return this;
        }

        public Map<String, String> build() {
            return map;
        }

    }
}