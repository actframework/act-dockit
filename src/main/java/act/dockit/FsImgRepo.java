package act.dockit;

import org.osgl.util.E;
import org.osgl.util.IO;

import java.io.*;

/**
 * Implement a {@link ImgRepo} with File sytem
 */
public class FsImgRepo implements ImgRepo {

    private File root;

    public FsImgRepo(File root) {
        E.illegalArgumentIf(!root.isDirectory());
        this.root = root;
    }

    @Override
    public InputStream get(String path) throws IOException {
        File file = new File(root, path);
        return file.exists() ? new BufferedInputStream(new FileInputStream(new File(root, path))) : null;
    }

    @Override
    public void persist(String path, InputStream data) throws IOException {
        File file = new File(root, path);
        IO.copy(data, new BufferedOutputStream(new FileOutputStream(file)));
    }
}
