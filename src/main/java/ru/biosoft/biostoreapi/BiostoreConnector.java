package ru.biosoft.biostoreapi;

import org.json.JSONObject;

import java.util.Map;

public interface BiostoreConnector
{
    JSONObject askServer(String username, String action, Map<String, String> parameters);
}
