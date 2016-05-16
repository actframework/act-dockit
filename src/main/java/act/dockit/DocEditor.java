package act.dockit;

import act.Act;
import act.app.ActionContext;
import act.app.App;
import act.app.event.AppEventId;
import act.controller.ParamNames;
import act.handler.builtin.Redirect;
import act.handler.builtin.controller.FastRequestHandler;
import org.osgl.http.H;
import org.osgl.mvc.result.RenderJSON;
import org.osgl.util.C;
import org.osgl.util.IO;
import org.osgl.util.S;

import java.io.File;
import java.io.InputStream;

import static act.controller.Controller.Util.notFoundIfNot;
import static act.controller.Controller.Util.notFoundIfNull;

/**
 * Request handler for editor access
 */
public class DocEditor extends FastRequestHandler {

    private File editorRoot;
    private String urlContext;
    private String portName;
    private String docUrlContext;
    private String imgUrlContext;

    private DocEditor() {}

    @Override
    public void handle(ActionContext context) {
        String path = context.paramVal(ParamNames.PATH);
        if (S.blank(path)) {
            new Redirect(urlContext + "/index.html").apply(context);
        } else {
            if (path.startsWith("/config")) {
                new RenderJSON(C.newMap("docUrl", docUrlContext, "imgUrl", imgUrlContext)).apply(context.req(), context.resp());
                return;
            }
            if (path.endsWith(".css")) {
                context.resp().contentType("text/css");
            } else if (path.endsWith(".js")) {
                context.resp().contentType("text/javascript");
            }
            InputStream is = load(path);
            IO.copy(is, context.resp().outputStream());
        }
    }

    @Override
    public boolean supportPartialPath() {
        return true;
    }

    private InputStream load(String path) {
        if (null != editorRoot) {
            File file = new File(editorRoot, path);
            notFoundIfNot(file.exists() && file.canRead());
            return IO.is(file);
        } else {
            InputStream is = DocEditor.class.getResourceAsStream("/dockit/editor" + path);
            notFoundIfNull(is);
            return is;
        }
    }

    private void registerToRouter() {
        Act.jobManager().on(AppEventId.PRE_START, new Runnable() {
            @Override
            public void run() {
                App.instance().router(portName).addMapping(H.Method.GET, urlContext, DocEditor.this);
            }
        });
    }

    public static class Builder {
        private DocEditor editor = new DocEditor();

        public Builder urlContext(String context) {
            editor.urlContext = context;
            return this;
        }

        public Builder portName(String name) {
            editor.portName = name;
            return this;
        }

        public Builder editorRoot(File root) {
            editor.editorRoot = root;
            return this;
        }

        public Builder docUrl(String url) {
            editor.docUrlContext = url;
            return this;
        }

        public Builder imgUrl(String url) {
            editor.imgUrlContext = url;
            return this;
        }

        public DocEditor build() {
            editor.registerToRouter();
            return editor;
        }
    }
}
