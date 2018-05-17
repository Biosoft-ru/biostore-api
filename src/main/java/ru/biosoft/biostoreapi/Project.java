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
        String path = obj.optString( "name", "" );
        if( path.isEmpty() )
            return null;
        int permission = obj.optInt( "permissions", 0 );
        return new Project( path, permission );
    }
}
