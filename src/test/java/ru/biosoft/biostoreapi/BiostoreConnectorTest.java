package ru.biosoft.biostoreapi;

import org.json.JSONObject;
import org.junit.Test;
import ru.biosoft.biostoreapi.impl.BiostoreConnectorImpl;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class BiostoreConnectorTest
{
    private static final String BIOSTORE_SERVER_NAME = "biblio.biouml.org";

    @Test
    public void askServer()
    {
        Map<String, String> params = new HashMap<>();
        params.put("password", "");
        params.put("username", "");

        BiostoreConnectorImpl defaultConnector = new BiostoreConnectorImpl(
                "https://bio-store.org/biostore/permission", BIOSTORE_SERVER_NAME);

        JSONObject jsonObject = defaultConnector.askServer("", "login", params);
        assertEquals("ok", jsonObject.getString("type"));
    }

}