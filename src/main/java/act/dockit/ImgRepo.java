package act.dockit;

import java.io.IOException;
import java.io.InputStream;

/**
 * Manage image files
 */
public interface ImgRepo {
    /**
     * Returns input stream to the resource specified by path.
     *
     * Return `null` if the resource cannot be found by path
     * @param path the path to the resource
     * @return an input stream that link to the resource
     * @throws IOException if IO exception encountered
     */
    InputStream get(String path) throws IOException;


    void persist(String path, InputStream data) throws IOException;
}
