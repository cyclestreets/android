package net.cyclestreets;

import org.junit.Test;

import java.io.IOException;

public class TUTest {

    @Test
    public void testTestUtils() throws IOException {
        String journeyJson = TestUtils.fromResourceFile("journey.json");
        System.out.println(journeyJson);
    }
}
