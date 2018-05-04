package ru.biosoft.biostoreapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.json.JSONObject;
import org.junit.Test;

public class ProjectTest
{
    @Test
    public void createFromJson1()
    {
        checkCreating( "data/Collaboration/", "Demo", 3 );
    }

    @Test
    public void createFromJson2()
    {
        checkCreating( "data/Projects/", "Demo", 7 );
    }

    @Test
    public void createFromJsonIncorrect1()
    {
        assertNull( Project.createFromJSON( new JSONObject() ) );
    }

    @Test
    public void createFromJsonIncorrect2()
    {
        assertNull( initProject( "data/Examples/", "Demo", 0 ) );
    }

    @Test
    public void createFromJsonIncorrect3()
    {
        assertNull( initProject( "data/Collaboration/", "", 0 ) );
    }

    @Test
    public void permissionsStrEmpty()
    {
        Project p = initProject( "data/Collaboration/", "Demo", 0 );
        assertEquals( "", p.getPermissionsStr() );
    }

    @Test
    public void permissionsStrAll()
    {
        Project p = initProject( "data/Collaboration/", "Demo", 31 );
        assertEquals( "All", p.getPermissionsStr() );
    }

    @Test
    public void permissionsStrAdminInfo()
    {
        Project p = initProject( "data/Collaboration/", "Demo", 17 );
        assertEquals( "Info/Admin", p.getPermissionsStr() );
    }

    @Test
    public void permissionsStrRWD()
    {
        Project p = initProject( "data/Collaboration/", "Demo", 14 );
        assertEquals( "Read/Write/Delete", p.getPermissionsStr() );
    }

    private void checkCreating(String parentPath, String name, int permissions)
    {
        Project p = initProject( parentPath, name, permissions );
        assertNotNull( p );
        assertEquals( name, p.getProjectName() );
        assertEquals( permissions, p.getPermissions() );
    }

    private Project initProject(String parentPath, String name, int permissions)
    {
        JSONObject obj = new JSONObject();
        obj.put( "path", parentPath + name );
        obj.put( "permissions", permissions );
        return Project.createFromJSON( obj );
    }
}
