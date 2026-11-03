package com.kamwithk.ankiconnectandroid.routing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RouteHandlerOriginTest {
    @Test
    public void allowsAppsExtensionsAndLocalPages() {
        assertTrue(RouteHandler.isAllowedOrigin(null, ""));
        assertTrue(RouteHandler.isAllowedOrigin("moz-extension://abc", ""));
        assertTrue(RouteHandler.isAllowedOrigin("chrome-extension://abc", ""));
        assertTrue(RouteHandler.isAllowedOrigin("http://localhost:4568", ""));
        assertTrue(RouteHandler.isAllowedOrigin("http://127.0.0.1:4568", ""));
    }

    @Test
    public void refusesWebPagesUnlessConfigured() {
        assertFalse(RouteHandler.isAllowedOrigin("https://evil.example", ""));
        assertFalse(RouteHandler.isAllowedOrigin("null", ""));
        assertFalse(RouteHandler.isAllowedOrigin("http://localhost.evil.example", ""));
        assertFalse(RouteHandler.isAllowedOrigin("http://127.0.0.1.evil.example:80", ""));
        assertTrue(RouteHandler.isAllowedOrigin("https://mine.example", " https://mine.example "));
        assertTrue(RouteHandler.isAllowedOrigin("https://evil.example", "*"));
    }
}
