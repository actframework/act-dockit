package act.dockit;

import static act.controller.Controller.Util.redirect;

import act.Act;
import act.app.ActionContext;
import act.app.App;
import act.app.event.AppEventId;
import act.controller.ParamNames;
import act.handler.builtin.controller.FastRequestHandler;
import org.osgl.http.H;
import org.osgl.mvc.result.Redirect;
import org.osgl.mvc.result.RenderJSON;
import org.osgl.util.C;
import org.osgl.util.IO;
import org.osgl.util.S;

import java.io.File;
import java.io.InputStream;

/*

!!!IMPORTANT NOTE!!!

Implementation of DocKit is NOT an example of how to
write normal actframework application.

PLEASE DO NOT FOLLOW THE CODE BELOW TO WRITE YOUR OWN APP

Go to https://github.com/actframework/act-demo-apps for reference
 */

/**
 * Request handler for editor access
 */
public class DocEditor extends FastRequestHandler {

    File editorRoot;
    String urlContext;
    String portName;
    String docUrlContext;
    String imgPath;

    DocEditor() {}

    @Override
    public void handle(ActionContext context) {
        String path = context.paramVal(ParamNames.PATH);
        if (S.blank(path)) {
            throw new Redirect(urlContext + "/index.html");
        } else {
            if (path.startsWith("/config")) {
                new RenderJSON(C.newMap("repoUrl", docUrlContext, "imgPath", imgPath)).apply(context.req(), context.resp());
                return;
            }
            if (path.endsWith(".css")) {
                context.resp().contentType("text/css");
            } else if (path.endsWith(".js")) {
                context.resp().contentType("text/javascript");
            }
            InputStream is = load(path);
            if (null == is) {
                throw redirect("%s/%s", docUrlContext, path);
            }
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
            return file.exists() && file.canRead() ? IO.is(file) : null;
        } else {
            InputStream is = DocEditor.class.getResourceAsStream("/dockit/editor" + path);
            return is;
        }
    }

    void registerToRouter() {
        Act.jobManager().on(AppEventId.PRE_START, new Runnable() {
            @Override
            public void run() {
                App.instance().router(portName).addMapping(H.Method.GET, urlContext, DocEditor.this);
            }
        });
    }

    @Override
    public String toString() {
        return S.fmt("editor@[%s]", urlContext);
    }

    public String debug() {
        String appRoot = null == editorRoot ? "embedded" : editorRoot.getAbsolutePath();

        return S.fmt("editor\n\turlContext:%s\n\tjs app root:%s", urlContext, appRoot);
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

        public Builder repoUrl(String url) {
            editor.docUrlContext = url;
            return this;
        }

        public Builder imgPath(String path) {
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            editor.imgPath = path;
            return this;
        }

        public DocEditor build() {
            editor.registerToRouter();
            return editor;
        }
    }

}
