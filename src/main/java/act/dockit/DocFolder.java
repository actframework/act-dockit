package act.dockit;

/**
 * A folder in a {@link DocRepo repository}
 */
public class DocFolder extends RepoElementBase {

    public DocFolder(String path) {
        super(path);
    }

    @Override
    public boolean isFolder() {
        return true;
    }
}
