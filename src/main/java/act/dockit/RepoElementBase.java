package act.dockit;

import org.osgl.$;

/**
 * A base implementation of a {@link RepoElement}
 */
public abstract class RepoElementBase implements RepoElement, Comparable<RepoElementBase> {

    private String path;

    public RepoElementBase(String path) {
        this.path = $.notNull(path);
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public String toString() {
        return path();
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof RepoElementBase) {
            RepoElementBase that = $.cast(obj);
            return that.path.equals(this.path);
        }
        return false;
    }

    @Override
    public int compareTo(RepoElementBase o) {
        if (isFolder() && o.isFolder()) {
            return path.compareTo(o.path());
        } else if (isFolder()) {
            return -1;
        } else if (o.isFolder()) {
            return 1;
        } else {
            return path.compareTo(o.path());
        }
    }
}
