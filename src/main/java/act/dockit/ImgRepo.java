package act.dockit;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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


    /**
     * Save image data to path specified
     * @param path the path where the image data should be saved
     * @param data the intputstream to get the image data
     * @throws IOException
     */
    void persist(String path, InputStream data) throws IOException;

    /**
     * Remove image specified by path
     * @param path the path to the image
     */
    void remove(String path);

    /**
     * List all image paths that are persisted before cooling time specified
     * @param coolingTimeInSeconds the cooling time in seconds
     * @return a list of image paths
     */
    List<String> list(int coolingTimeInSeconds);
}
