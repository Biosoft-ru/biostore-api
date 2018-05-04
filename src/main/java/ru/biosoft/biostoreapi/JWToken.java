package ru.biosoft.biostoreapi;

public class JWToken
{
    private final String username;
    private final String jwToken;

    public JWToken(String username, String jwToken)
    {
        this.username = username;
        this.jwToken = jwToken;
    }

    public String getUsername()
    {
        return username;
    }

    public String getTokenValue()
    {
        return jwToken;
    }

    @Override
    public boolean equals(Object o)
    {
        if( this == o )
            return true;
        if( o == null || getClass() != o.getClass() )
            return false;

        JWToken jwToken1 = (JWToken)o;

        if( username != null ? !username.equals( jwToken1.username ) : jwToken1.username != null )
            return false;
        return jwToken != null ? jwToken.equals( jwToken1.jwToken ) : jwToken1.jwToken == null;
    }

    @Override
    public int hashCode()
    {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + ( jwToken != null ? jwToken.hashCode() : 0 );
        return result;
    }
}
