package test;

import act.Act;
import act.app.event.AppEventId;
import act.dockit.DocEditor;
import act.dockit.DocKit;
import act.dockit.FsDocRepo;
import act.job.OnAppEvent;

import java.io.File;

public class BootDocKit {

    @OnAppEvent(AppEventId.PRE_START)
    public static void loadDocKit() {
        if (Act.isDev()) {
            FsDocRepo docRepo = new FsDocRepo(new File("doc"));
            new DocKit(docRepo, "/doc");

            new DocEditor.Builder().urlContext("/editor").docUrl("/doc").build();
        }
    }

}
