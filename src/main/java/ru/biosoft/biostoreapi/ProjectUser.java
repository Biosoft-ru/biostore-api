package ru.biosoft.biostoreapi;

import org.json.JSONObject;

public class ProjectUser
{
    private final String user;
    private final String role;
    public ProjectUser(String user, String role)
    {
        this.user = user;
        this.role = role;
    }

    public String getUser()
    {
        return user;
    }

    public String getRole()
    {
        return role;
    }

    @Override
    public String toString()
    {
        return user + " (" + role + ")";
    }

    public static ProjectUser createFromJSON(JSONObject obj)
    {
        String user = obj.optString( "user", "" );
        String role = obj.optString( "role", "" );
        if( user.isEmpty() || role.isEmpty() )
            return null;
        return new ProjectUser( user, role );
    }
}
