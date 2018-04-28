package ru.biosoft.biostoreapi;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
        assertEquals( "Demo", projectList.get( 0 ).getProjectName() );
        assertEquals( 3, projectList.get( 0 ).getPermissions() );
    }

    @Test
    public void projectListWithToken()
    {
        DefaultConnectionProvider test = new DefaultConnectionProvider(BIOSTORE_SERVER_NAME);
        List<Project> projectList = test.getProjectList( test.getJWToken( "", "" ) );

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

        DefaultConnectionProvider test = new DefaultConnectionProvider(BIOSTORE_SERVER_NAME);

        test.authorize("errorName", "", null);
    }

    @Test
    public void errorAddUserToProject()
    {
        thrown.expect( SecurityException.class );
        thrown.expectMessage( "Only group or server administrator can add users to project" );

        DefaultConnectionProvider test = new DefaultConnectionProvider( BIOSTORE_SERVER_NAME );
        test.addUserToProject( "", "", "", "Demo" );
    }
}