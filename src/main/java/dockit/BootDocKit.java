package dockit;

import act.app.*;
import act.app.event.AppEventId;
import act.app.util.NamedPort;
import act.dockit.DocEditor;
import act.dockit.DocKit;
import act.dockit.FsDocRepo;
import act.dockit.FsImgRepo;
import act.handler.builtin.Redirect;
import act.job.OnAppEvent;
import org.osgl.http.H;
import org.osgl.logging.LogManager;
import org.osgl.logging.Logger;
import org.osgl.util.S;

import java.io.File;
import java.util.List;

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
        String docHome = getProp("docDir", "doc");
        String imgHome = getProp("imgDir", "doc/img");
        if (!imgHome.startsWith(docHome)) {
            StringBuilder sb = S.builder(docHome);
            if (docHome.endsWith("/") || imgHome.startsWith("/")) {
                sb.append(imgHome);
            } else {
                sb.append("/").append(imgHome);
            }
            imgHome = sb.toString();
        }
        String docUrl = docHome.startsWith("/") ? docHome : "/" + docHome;
        String imgUrl = imgHome.startsWith("/") ? imgHome : "/" + imgHome;
        String editorUrl = getProp("editorUrl", "/editor");

        FsDocRepo docRepo = new FsDocRepo(new File(docHome));
        FsImgRepo imgRepo = new FsImgRepo(new File(imgHome));
        DocKit docKit = new DocKit.Builder().docRepo(docRepo).imgRepo(imgRepo).urlContext(docUrl).suffixes(".md").build();
        DocEditor docEditor = new DocEditor.Builder().urlContext(editorUrl).docUrl(docUrl).imgUrl(imgUrl).build();

        int port = act.app.App.instance().config().httpPort();
        System.out.printf("\nDocKit is running on port: %s\n", port);
        System.out.printf("\n%s\n\n%s\n", docKit, docEditor);
        System.out.println("\nDocKit is powered by \n\t* [actframework](http://github.com/actframework)");
        System.out.println("\t* [mdEdit](https://jbt.github.io/markdown-editor/)");
        System.out.println("\t* [markdownit](https://github.com/markdown-it/markdown-it)\n");
    }

    private static String getProp(String name, String def) {
        return System.getProperty(name, def);
    }

}
