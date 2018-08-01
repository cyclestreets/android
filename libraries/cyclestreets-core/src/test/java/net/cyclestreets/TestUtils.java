package net.cyclestreets;

import android.content.res.Resources;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class TestUtils {

    public static String fromResourceFile(String resourceFileName) throws IOException {
        InputStream in = TestUtils.class.getClassLoader().getResourceAsStream(resourceFileName);
        if (in != null) {
            String output = IOUtils.toString(in, "UTF-8").trim();
            in.close();
            return output;
        }
        throw new Resources.NotFoundException(resourceFileName);
    }
}
