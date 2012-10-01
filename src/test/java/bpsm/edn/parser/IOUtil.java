package bpsm.edn.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import bpsm.edn.model.EdnException;

class IOUtil {

    static final String ENCODING = "UTF-8";
    private static final int BUFFER_SIZE = 8*1024;
    private static final int INITIAL_BUILDER_SIZE = 8*1024;

    static String stringFromResource(String resourceName) {
        URL url = IOUtil.class.getClassLoader().getResource(resourceName);
        if (url == null) {
            throw new EdnException("resource '"+ resourceName +"' not found.");
        }
        return stringFromURL(url);
    }

    static String stringFromURL(URL url) {
        try {
            InputStream urlStream = url.openStream();
            try {
                return stringFromInputStream(urlStream);
            } finally {
                urlStream.close();
            }
        } catch (IOException e) {
            throw new EdnException(e);
        }
    }

    static String stringFromInputStream(InputStream urlStream) {
        try {
            Reader reader = new InputStreamReader(urlStream, ENCODING);
            try {
                char[] buffer = new char[BUFFER_SIZE];
                StringBuilder b = new StringBuilder(INITIAL_BUILDER_SIZE);
                int n;
                while ((n = reader.read(buffer)) >= 0) {
                    b.append(buffer, 0, n);
                }
                return b.toString();
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            throw new EdnException(e);
        }
    }

}
