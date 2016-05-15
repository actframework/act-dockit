package act.dockit;

/**
 * A `RepoElement` could be either a {@link DocFolder} or
 * a {@link DocSource}
 */
public interface RepoElement {
    /**
     * Returns the path to the element in a giving {@link DocRepo}
     * @return the path
     */
    String path();

    /**
     * Check if this element is a {@link DocFolder folder}
     * @return `true` if the element is a folder or `false` otherwise
     */
    boolean isFolder();
}
