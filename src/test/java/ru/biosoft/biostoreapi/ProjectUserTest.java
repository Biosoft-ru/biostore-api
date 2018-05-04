package ru.biosoft.biostoreapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ProjectUserTest
{
    @Test
    public void testConstructor()
    {
        ProjectUser pu = new ProjectUser( "u", "r" );
        assertEquals( "u", pu.getUser() );
        assertEquals( "r", pu.getRole() );
        assertEquals( "u (r)", pu.toString() );
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Test
    public void testConstructorError1()
    {
        tryCreateProject( null, "r1" );
    }

    @Test
    public void testConstructorError2()
    {
        tryCreateProject( "", "r1" );
    }

    @Test
    public void testConstructorError3()
    {
        tryCreateProject( "u1", null );
    }

    @Test
    public void testConstructorError4()
    {
        tryCreateProject( "u1", "" );
    }

    private void tryCreateProject(String user, String role)
    {
        thrown.expect( IllegalArgumentException.class );
        thrown.expectMessage( "User and role must be not null and not empty" );
        new ProjectUser( user, role );
    }

    @Test
    public void testEquals()
    {
        ProjectUser pu1 = new ProjectUser( "u1", "r1" );
        ProjectUser pu2 = new ProjectUser( "u1", "r1" );
        ProjectUser pu3 = new ProjectUser( "u2", "r1" );
        ProjectUser pu4 = new ProjectUser( "u1", "r2" );

        assertTrue( pu1.equals( pu2 ) && pu2.equals( pu1 ) );
        assertTrue( pu1.hashCode() == pu2.hashCode() );
        assertFalse( pu1.equals( pu3 ) );
        assertFalse( pu1.equals( pu4 ) );
        assertFalse( pu3.equals( pu4 ) );
        assertFalse( pu1.equals( new Object() ) );
        assertFalse( pu1.equals( null ) );
    }

    @Test
    public void createFromJson()
    {
        ProjectUser pu = initProjectUser( "u", "r" );
        assertEquals( "u", pu.getUser() );
        assertEquals( "r", pu.getRole() );
        assertEquals( "u (r)", pu.toString() );
    }

    @Test
    public void createFromJsonNoUser()
    {
        assertNull( initProjectUser( "", "r" ) );
    }

    @Test
    public void createFromJsonNoRole()
    {
        assertNull( initProjectUser( "u", "" ) );
    }

    @Test
    public void createFromJsonIncorrect()
    {
        assertNull( ProjectUser.createFromJSON( new JSONObject() ) );
    }

    @Test
    public void compare()
    {
        ProjectUser pu1 = new ProjectUser( "u1", "r1" );
        ProjectUser pu2 = new ProjectUser( "u1", "r2" );
        ProjectUser pu3 = new ProjectUser( "u2", "r1" );
        ProjectUser pu4 = new ProjectUser( "u2", "r1" );

        assertEquals( -1, pu1.compareTo( pu2 ) );
        assertEquals( 1, pu2.compareTo( pu1 ) );
        assertEquals( -1, pu2.compareTo( pu3 ) );
        assertEquals( 1, pu3.compareTo( pu2 ) );
        assertEquals( 0, pu4.compareTo( pu3 ) );
        assertEquals( 0, pu3.compareTo( pu4 ) );
    }

    private ProjectUser initProjectUser(String user, String role)
    {
        JSONObject obj = new JSONObject();
        obj.put( "user", user );
        obj.put( "role", role );
        return ProjectUser.createFromJSON( obj );
    }
}
