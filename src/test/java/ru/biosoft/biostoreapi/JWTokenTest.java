package ru.biosoft.biostoreapi;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class JWTokenTest
{
    @Test
    public void equalsTest1()
    {
        JWToken jwt1 = new JWToken( "u1", "123123" );
        JWToken jwt2 = new JWToken( "u1", "123123" );
        JWToken jwt3 = new JWToken( "u2", "123123" );
        JWToken jwt4 = new JWToken( "u1", "321321" );

        assertTrue( jwt1.equals( jwt2 ) && jwt2.equals( jwt1 ) );
        assertTrue( jwt1.hashCode() == jwt2.hashCode() );
        assertFalse( jwt1.equals( jwt3 ) );
        assertFalse( jwt1.equals( jwt4 ) );
        assertFalse( jwt3.equals( jwt4 ) );
    }

    @Test
    public void equalsTest2()
    {
        JWToken jwt1 = new JWToken( "u1", "123123" );
        JWToken jwt2 = new JWToken( null, "123123" );
        JWToken jwt3 = new JWToken( "u1", null );
        JWToken jwt4 = new JWToken( null, null );

        assertFalse( jwt1.equals( jwt2 ) );
        assertFalse( jwt1.equals( jwt3 ) );
        assertFalse( jwt1.equals( jwt4 ) );
        assertFalse( jwt3.equals( jwt4 ) );
        assertFalse( jwt1.equals( new Object() ) );
        assertFalse( jwt1.equals( null ) );
    }
}
