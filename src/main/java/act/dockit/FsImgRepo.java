package act.dockit;

import org.osgl.$;
import org.osgl.util.C;
import org.osgl.util.E;
import org.osgl.util.IO;
import org.osgl.util.S;

import java.io.*;
import java.util.List;

/**
 * Implement a {@link ImgRepo} with File sytem
 */
public class FsImgRepo implements ImgRepo {

    private File root;

    public FsImgRepo(File root) {
        E.illegalArgumentIf(!root.isDirectory(), "%s is not a directory", root.getAbsolutePath());
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

    @Override
    public void remove(String path) {
        File file = new File(root, path);
        if (!file.delete()) {
            file.deleteOnExit();
        }
    }

    @Override
    public List<String> list(int coolingTimeInSeconds) {
        List<File> list = C.newList();
        list(root, list, coolingTimeInSeconds);
        List<String> paths = C.newList();
        int offset = root.getAbsolutePath().length();
        for (File file : list) {
            String filePath = file.getAbsolutePath();
            paths.add(filePath.substring(offset));
        }
        return paths;
    }

    private void list(File folder, List<File> list, final int coolingTimeInSeconds) {
        List<File> files = C.listOf(folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return ($.ms() - file.lastModified()) >= coolingTimeInSeconds * 1000;
            }
        }));
        for (File file: files) {
            if (file.isDirectory()) {
                list(file, list, coolingTimeInSeconds);
            }
            list.add(file);
        }
    }


    @Override
    public String toString() {
        return S.fmt("ImgRepo[%s]", root.getAbsolutePath());
    }
}
