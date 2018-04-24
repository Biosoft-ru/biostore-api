package ru.biosoft.biostoreapi;

import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;


public class DefaultConnectionProviderTest
{
    public static final String BIOSTORE_SERVER_NAME = "biblio.biouml.org";

    @Test
    public void authorize()
    {
        DefaultConnectionProvider test = new DefaultConnectionProvider(BIOSTORE_SERVER_NAME);
        UserPermissions authorize = test.authorize("", "", null);

        assertEquals(1, authorize.getDbToPermission().size());
        assertEquals(3, authorize.getDbToPermission().get("data/Collaboration/Demo").getPermissions());
    }

    @Test
    public void projectList()
    {
        DefaultConnectionProvider test = new DefaultConnectionProvider(BIOSTORE_SERVER_NAME);
        List<Project> projectList = test.getProjectList( "", "" );

        assertEquals( 1, projectList.size() );
        assertEquals( "Demo (Info/Read)", projectList.get( 0 ).toString() );
    }

    @Test
    @Ignore
    public void projectListWithToken()
    {
        DefaultConnectionProvider test = new DefaultConnectionProvider(BIOSTORE_SERVER_NAME);
        List<Project> projectList = test.getProjectListWithToken( "", test.getJWToken( "", "" ) );

        assertEquals( 1, projectList.size() );
        assertEquals( "Demo (Info/Read)", projectList.get( 0 ).toString() );
    }

    @Test(expected = SecurityException.class)
    public void errorLogin()
    {
        DefaultConnectionProvider test = new DefaultConnectionProvider(BIOSTORE_SERVER_NAME);

        test.authorize("errorName", "", null);
    }
}