package ru.biosoft.biostoreapi;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public class Project
{
    public static final int PERMISSION_INFO = 0b00001;
    public static final int PERMISSION_READ = 0b00010;
    public static final int PERMISSION_WRITE = 0b00100;
    public static final int PERMISSION_DELETE = 0b01000;
    public static final int PERMISSION_ADMIN = 0b10000;
    public static final int PERMISSION_ALL = 0b11111;

    static final String PROJECT_PREFIX_B = "data/Collaboration/";
    static final String PROJECT_PREFIX_G = "data/Projects/";

    private final String projectName;
    private final int permissions;

    public Project(String projectName, int permissions)
    {
        this.projectName = projectName;
        this.permissions = permissions;
    }

    private String permissionsStr;

    public String getPermissionsStr()
    {
        if( permissionsStr == null )
        {
            List<String> permStrList = new ArrayList<>();
            if( permissions == PERMISSION_ALL )
            {
                permStrList.add( "All" );
            }
            else
            {
                if( ( permissions & PERMISSION_INFO ) != 0 )
                    permStrList.add( "Info" );
                if( ( permissions & PERMISSION_READ ) != 0 )
                    permStrList.add( "Read" );
                if( ( permissions & PERMISSION_WRITE ) != 0 )
                    permStrList.add( "Write" );
                if( ( permissions & PERMISSION_DELETE ) != 0 )
                    permStrList.add( "Delete" );
                if( ( permissions & PERMISSION_ADMIN ) != 0 )
                    permStrList.add( "Admin" );
            }
            permissionsStr = permStrList.isEmpty() ? "" : String.join( "/", permStrList );
        }
        return permissionsStr;
    }

    @Override
    public String toString()
    {
        return projectName + " (" + getPermissionsStr() + ")";
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

    private static boolean isProjectPath(String path)
    {
        return isPrefixCorrect( path, PROJECT_PREFIX_B ) || isPrefixCorrect( path, PROJECT_PREFIX_G );
    }
    private static boolean isPrefixCorrect(String path, String expectedPrefix)
    {
        return path.startsWith( expectedPrefix ) && !path.equals( expectedPrefix );
    }
    private static String getProjectNameFromPath(String path)
    {
        return path.replace( PROJECT_PREFIX_B, "" ).replace( PROJECT_PREFIX_G, "" );
    }
}
