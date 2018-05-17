package ru.biosoft.biostoreapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.json.JSONObject;
import org.junit.Test;

public class ProjectTest
{
    @Test
    public void createFromJson()
    {
        checkCreating( "Demo", 7 );
    }

    @Test
    public void createFromJsonEmptyPermission()
    {
        checkCreating( "Demo", null );
    }

    @Test
    public void createFromJsonIncorrect1()
    {
        assertNull( Project.createFromJSON( new JSONObject() ) );
    }

    @Test
    public void createFromJsonIncorrect2()
    {
        assertNull( initProject( "", 0 ) );
    }

    @Test
    public void permissionsStrEmpty()
    {
        Project p = initProject( "Demo", 0 );
        assertEquals( "", p.getPermissionsStr() );
    }

    @Test
    public void permissionsStrAll()
    {
        Project p = initProject( "Demo", 31 );
        assertEquals( "All", p.getPermissionsStr() );
    }

    @Test
    public void permissionsStrAdminInfo()
    {
        Project p = initProject( "Demo", 17 );
        assertEquals( "Info/Admin", p.getPermissionsStr() );
    }

    @Test
    public void permissionsStrRWD()
    {
        Project p = initProject( "Demo", 14 );
        assertEquals( "Read/Write/Delete", p.getPermissionsStr() );
    }

    private void checkCreating(String name, Integer permissions)
    {
        Project p = initProject( name, permissions );
        assertNotNull( p );
        assertEquals( name, p.getProjectName() );
        assertEquals( permissions == null ? 0 : permissions.intValue(), p.getPermissions() );
    }

    private Project initProject(String name, Integer permissions)
    {
        JSONObject obj = new JSONObject();
        obj.put( "name", name );
        if( permissions != null )
            obj.put( "permissions", permissions.intValue() );
        return Project.createFromJSON( obj );
    }
}
