package act.dockit;

import act.Act;
import act.app.ActionContext;
import act.app.App;
import act.app.event.AppEventId;
import act.controller.Controller;
import act.controller.ParamNames;
import act.event.AppEventListenerBase;
import act.handler.builtin.controller.FastRequestHandler;
import act.route.Router;
import com.alibaba.fastjson.JSON;
import org.osgl.$;
import org.osgl.http.H;
import org.osgl.mvc.result.*;
import org.osgl.util.C;

import java.util.EventObject;
import java.util.List;
import java.util.Set;

/**
 * Mount a {@link DocRepo} to a web application
 */
public class DocKit {

    private DocRepo repo;
    private String urlContext;
    private Set<String> sourceIndexes = C.newSet();
    private Set<String> folderIndexes = C.newSet();
    private String portName;

    public DocKit(DocRepo repo, String urlContext) {
        this(repo, urlContext, null);
    }

    public DocKit(DocRepo repo, String urlContext, String portName) {
        this.repo = $.notNull(repo);
        this.urlContext = $.notNull(urlContext);
        this.portName = portName;
        Act.eventBus().bindAsync(AppEventId.POST_START, new AppEventListenerBase() {
            @Override
            public void on(EventObject event) throws Exception {
                registerToRouter();
                buildIndex();
            }
        });
    }

    private void registerToRouter() {
        Router router = App.instance().router(portName);
        router.addMapping(H.Method.GET, urlContext, new Getter());
        router.addMapping(H.Method.POST, urlContext, new Saver());
    }

    private void buildIndex() {
        addToIndex(repo.root());
    }

    private void addToIndex(DocFolder folder) {
        String path = folder.path();
        folderIndexes.add(path);
        List<RepoElement> elements = repo.list(path);
        for (RepoElement element : elements) {
            if (element.isFolder()) {
                addToIndex((DocFolder) element);
            } else {
                sourceIndexes.add(element.path());
            }
        }
    }

    private boolean isFolder(String path) {
        if (folderIndexes.contains(path)) {
            return true;
        }
        verifyPath(path);
        return false;
    }

    private boolean isSource(String path) {
        return sourceIndexes.contains(path);
    }

    private void verifyPath(String path) {
        RepoElement element = repo.fetch(path);
        Controller.Util.notFoundIfNull(element);
        if (element.isFolder()) {
            folderIndexes.add(path);
        } else {
            sourceIndexes.add(path);
        }
    }

    private abstract class _HandlerBase extends FastRequestHandler {

        @Override
        public void handle(ActionContext context) {
            String path = context.paramVal(ParamNames.PATH);
            Result result = _handle(path, context);
            result.apply(context.req(), context.resp());
        }

        protected abstract Result _handle(String path, ActionContext context);

        @Override
        public boolean supportPartialPath() {
            return true;
        }
    }

    private class Getter extends _HandlerBase {

        @Override
        protected Result _handle(String path, ActionContext context) {
            Result result;
            if (isFolder(path)) {
                List<RepoElement> elements = repo.list(path);
                List<String> list = C.list(elements).map($.F.<RepoElement>asString());
                result = new RenderJSON(JSON.toJSONString(list));
            } else if (isSource(path)) {
                String source = repo.read(path);
                if (path.endsWith(".html")) {
                    result = new RenderHtml(source);
                } else {
                    result = new RenderText(source);
                }
            } else {
                result = NotFound.INSTANCE;
            }
            return result;
        }

        @Override
        public String toString() {
            return "dockit:get:" + repo;
        }

    }

    private class Saver extends _HandlerBase {

        @Override
        protected Result _handle(String path, ActionContext context) {
            String content = context.paramVal("content");
            repo.persist(path, content);
            sourceIndexes.add(path);
            return Ok.INSTANCE;
        }

    }


}
