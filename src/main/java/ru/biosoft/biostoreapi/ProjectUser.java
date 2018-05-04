package ru.biosoft.biostoreapi;

import org.json.JSONObject;

public class ProjectUser implements Comparable<ProjectUser>
{
    private final String user;
    private final String role;
    /**
     * Creates container that contains user name and role in project
     * @param user not null and not empty name of the user
     * @param role not null and not empty role of the user
     * @throws IllegalArgumentException if user or role is null or empty
     */
    public ProjectUser(String user, String role)
    {
        if( user == null || user.isEmpty() || role == null || role.isEmpty() )
            throw new IllegalArgumentException( "User and role must be nonnull and not empty" );
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

    @Override
    public boolean equals(Object o)
    {
        if( this == o )
            return true;
        if( o == null || getClass() != o.getClass() )
            return false;

        ProjectUser pu = (ProjectUser)o;
        return user.equals( pu.user ) && role.equals( pu.role );
    }

    @Override
    public int hashCode()
    {
        return 31 * user.hashCode() + role.hashCode();
    }

    @Override
    public int compareTo(ProjectUser pu)
    {
        int result = user.compareTo( pu.user );
        if( result != 0 )
            return result;
        return role.compareTo( pu.role );
    }
}
