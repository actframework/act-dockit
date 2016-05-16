package test;

import act.Act;
import act.app.event.AppEventId;
import act.dockit.*;
import act.job.OnAppEvent;

import java.io.File;

public class BootDocKit {

    @OnAppEvent(AppEventId.PRE_START)
    public static void loadDocKit() {
        if (Act.isDev()) {
            FsDocRepo docRepo = new FsDocRepo(new File("doc"));
            FsImgRepo imgRepo = new FsImgRepo(new File("doc/img"));
            new DocKit.Builder().docRepo(docRepo).imgRepo(imgRepo).urlContext("/doc").build();
            new DocEditor.Builder().urlContext("/editor").docUrl("/doc").imgUrl("/doc/img").build();
        }
    }

}
