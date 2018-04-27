package ru.biosoft.biostoreapi;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public class Project
{
    static final String PROJECT_PREFIX_B = "data/Collaboration";
    static final String PROJECT_PREFIX_G = "data/Projects";

    private final String projectName;
    private final int permissions;

    public Project(String projectName, int permissions)
    {
        this.projectName = projectName;
        this.permissions = permissions;
    }

    private String permissionsStr;
    @Override
    public String toString()
    {
        if( permissionsStr == null )
        {
            List<String> permStrList = new ArrayList<>();
            if( permissions == Permission.ALL )
            {
                permStrList.add( "All" );
            }
            else
            {
                if( ( permissions & Permission.INFO ) != 0 )
                    permStrList.add( "Info" );
                if( ( permissions & Permission.READ ) != 0 )
                    permStrList.add( "Read" );
                if( ( permissions & Permission.WRITE ) != 0 )
                    permStrList.add( "Write" );
                if( ( permissions & Permission.DELETE ) != 0 )
                    permStrList.add( "Delete" );
                if( ( permissions & Permission.ADMIN ) != 0 )
                    permStrList.add( "Admin" );
            }
            permissionsStr = permStrList.isEmpty() ? "" : String.join( "/", permStrList );
        }
        return projectName + " (" + permissionsStr + ")";
    }

    public String getProjectName()
    {
        return projectName;
    }

    public int getPermissions()
    {
        return permissions;
    }

    public static Project createFromJSON(JSONObject obj)
    {
        String path = obj.optString( "path", "" );
        if( path.isEmpty() || !isProjectPath( path ) )
            return null;
        int permission = obj.optInt( "permissions", 0 );
        return new Project( getProjectNameFromPath( path ), permission );
    }

    public static boolean isProjectPath(String path)
    {
        return path.startsWith( "data/Collaboration/" ) || path.startsWith( "data/Projects/" );
    }
    public static String getProjectNameFromPath(String path)
    {
        return path.replace( "data/Collaboration/", "" ).replace( "data/Projects/", "" );
    }
}
