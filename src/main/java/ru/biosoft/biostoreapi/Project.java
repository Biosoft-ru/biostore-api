package ru.biosoft.biostoreapi;

import java.util.ArrayList;
import java.util.List;

import com.eclipsesource.json.JsonObject;

public class Project
{
    static final String PROJECT_PREFIX_B = "data/Collaboration";
    static final String PROJECT_PREFIX_G = "data/Projects";

    private final String projectName;
    private final int permissions;
    private final List<String> permStrList = new ArrayList<>();

    public Project(String projectName, int permission)
    {
        this.projectName = projectName;
        this.permissions = permission;

        if( permission == Permission.ALL )
        {
            permStrList.add( "All" );
            return;
        }

        if( ( permission & Permission.INFO ) != 0 )
            permStrList.add( "Info" );
        if( ( permission & Permission.READ ) != 0 )
            permStrList.add( "Read" );
        if( ( permission & Permission.WRITE ) != 0 )
            permStrList.add( "Write" );
        if( ( permission & Permission.DELETE ) != 0 )
            permStrList.add( "Delete" );
        if( ( permission & Permission.ADMIN ) != 0 )
            permStrList.add( "Admin" );
    }

    @Override
    public String toString()
    {
        return projectName + " (" + String.join( "/", permStrList ) + ")";
    }

    public String getProjectName()
    {
        return projectName;
    }

    public int getPermissions()
    {
        return permissions;
    }

    public List<String> getPermStrList()
    {
        return permStrList;
    }

    public static Project createFromJson(JsonObject obj)
    {
        String path = obj.getString( "path", "" );
        if( path.isEmpty() || !isProjectPath( path ) )
            return null;
        int permission = obj.getInt( "permissions", 0 );
        return new Project( getProjectName( path ), permission );
    }

    public static boolean isProjectPath(String path)
    {
        return path.startsWith( "data/Collaboration/" ) || path.startsWith( "data/Projects/" );
    }
    public static String getProjectName(String path)
    {
        return path.replace( "data/Collaboration/", "" ).replace( "data/Projects/", "" );
    }
}
