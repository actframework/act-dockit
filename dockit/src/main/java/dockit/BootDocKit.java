package dockit;

import act.app.event.AppEventId;
import act.dockit.*;
import act.handler.builtin.Redirect;
import act.job.OnAppEvent;
import org.osgl.http.H;
import org.osgl.logging.LogManager;
import org.osgl.logging.Logger;
import org.osgl.util.S;

import java.io.File;

public class BootDocKit {

    private static Logger logger = LogManager.get(BootDocKit.class);
    private static boolean loaded = false;

    @OnAppEvent(AppEventId.PRE_START)
    public static void loadDocKit() {
        if (loaded) {
            return;
        }
        act.app.App.instance().router().addMapping(H.Method.GET, "/", new Redirect("/editor"));
        loaded = true;

        System.out.println("loading dockit...");
        String repo = getProp("repoRoot", ".");
        String imgPath = getProp("imgPath", "img");
        String urlContext = repo.startsWith("/") ? repo : "/" + repo;
        if (repo.equals(".")) {
            urlContext = "/doc";
        }
        String editorUrl = getProp("editorUrl", "/editor");
        String editorRoot = getProp("editorRoot", null);
        File editorRootDir = null;
        if (null != editorRoot) {
            editorRootDir = new File(editorRoot);
            if (!editorRootDir.isDirectory()) {
                System.err.println("editor root does not exists or is not a directory: " + editorRoot);
                editorRootDir = null;
            }
        }

        FsDocRepo docRepo = new FsDocRepo(new File(repo));
        FsImgRepo imgRepo = new FsImgRepo(new File(S.builder(repo).append("/").append(imgPath).toString()));
        DocKit docKit = new DocKit.Builder().docRepo(docRepo).imgRepo(imgRepo).urlContext(urlContext).imgPath(imgPath).suffixes(".md").build();
        DocEditor docEditor = new DocEditor.Builder().urlContext(editorUrl).repoUrl(urlContext).imgPath(imgPath).editorRoot(editorRootDir).build();

        int port = act.app.App.instance().config().httpPort();
        System.out.printf("\nDocKit is running on port: %s\n", port);
        System.out.printf("\n%s\n\n%s\n", docKit.debug(), docEditor.debug());
        System.out.println("\nDocKit is powered by");
        System.out.println("\t* [actframework](http://github.com/actframework)");
        System.out.println("\t* [mdEdit](https://github.com/jbt/mdEdit)");
        System.out.println("\t* [Prism](https://github.com/PrismJS/prism)");
        System.out.println("\t* [markdownit](https://github.com/markdown-it/markdown-it)\n");
    }

    private static String getProp(String name, String def) {
        return System.getProperty(name, def);
    }

}
