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
}
