package act.dockit;

/**
 * A doc source in a {@link DocRepo}
 */
public class DocSource extends RepoElementBase {

    public DocSource(String path) {
        super(path);
    }

    @Override
    public boolean isFolder() {
        return false;
    }
}
