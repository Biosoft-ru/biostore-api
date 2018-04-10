package ru.biosoft.biostoreapi;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;


public class DefaultConnectionProviderTest
{
    @Test
    public void authorize()
    {
        DefaultConnectionProvider test = new DefaultConnectionProvider("biblio.biouml.org");
        UserPermissions authorize = test.authorize("", "", null);

        assertEquals(1, authorize.getDbToPermission().size());
        assertEquals(3, authorize.getDbToPermission().get("data/Collaboration/Demo").getPermissions());

        List<String> projectList = test.getProjectList("", "");

        assertEquals(1, projectList.size());
        assertEquals("Demo", projectList.get(0));
    }

    @Test(expected = SecurityException.class)
    public void errorLogin()
    {
        DefaultConnectionProvider test = new DefaultConnectionProvider("biblio.biouml.org");

        test.authorize("errorName", "", null);
    }
}