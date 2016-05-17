package act.dockit;

import java.io.Reader;
import java.util.List;

/**
 * Provides methods to navigate a document repository and
 * access to specific document source
 */
public interface DocRepo {

    /**
     * Returns the root {@link DocFolder folder} of this repository
     * @return the root folder
     */
    DocFolder root();

    /**
     * Return a {@link RepoElement} by path
     * @param path the path to the element
     * @return the element
     */
    RepoElement fetch(String path);

    /**
     * List immediate {@link RepoElement elements} contained in a folder
     *
     * Note this does not return elements contained in sub folders
     *
     * @param path speicfy a folder
     * @return a list of elements contained in the folder
     */
    List<RepoElement> list(String path);

    /**
     * Read a doc source
     * @param path specify the doc source in the repo
     * @return a {@link Reader} to access the source
     */
    String read(String path);

    void persist(String path, String content);

    void delete(String path);
}
