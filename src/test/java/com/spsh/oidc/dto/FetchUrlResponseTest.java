package com.spsh.oidc.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FetchUrlResponseTest {

    @Test
    void testSetAndGetUserId() {
        FetchUrlResponse response = new FetchUrlResponse();
        response.setUserId("user-123");
        assertEquals("user-123", response.getUserId());
    }

    @Test
    void testSetAndGetMapToLmsRolle() {
        FetchUrlResponse response = new FetchUrlResponse();
        response.setMapToLmsRolle("Admin");
        assertEquals("Admin", response.getMapToLmsRolle());
    }

    @Test
    void testEmptyObject() {
        FetchUrlResponse response = new FetchUrlResponse();
        assertNull(response.getUserId());
        assertNull(response.getMapToLmsRolle());
    }
}