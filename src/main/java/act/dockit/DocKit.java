package act.dockit;

import act.Act;
import act.app.ActionContext;
import act.app.App;
import act.app.event.AppEventId;
import act.controller.Controller;
import act.route.Router;
import act.util.IdGenerator;
import act.view.ActServerError;
import com.alibaba.fastjson.JSON;
import org.osgl.$;
import org.osgl.Osgl;
import org.osgl.http.H;
import org.osgl.mvc.result.*;
import org.osgl.util.C;
import org.osgl.util.Codec;
import org.osgl.util.E;
import org.osgl.util.S;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static act.controller.Controller.Util.badRequestIfNot;
import static act.controller.Controller.Util.notFoundIf;

/**
 * Mount a {@link DocRepo} to a web application
 */
public class DocKit {

    private ImgRepo imgRepo;
    private DocRepo docRepo;
    private String urlContext = "/doc";
    private String imgPath = "/img";
    private String imgUrl;
    private Set<String> sourceIndexes = C.newSet();
    private Set<String> folderIndexes = C.newSet();
    private String portName;
    private IdGenerator idGenerator = new IdGenerator(".img.id.do-not-delete");

    private DocKit() {
        refreshImgUrl();
        Act.jobManager().on(AppEventId.POST_START, new Runnable() {
            @Override
            public void run() {
                buildIndex();
                registerToRouter();
            }
        });
    }

    private void registerToRouter() {
        Router router = App.instance().router(portName);
        router.addMapping(H.Method.GET, urlContext, new Getter());
        router.addMapping(H.Method.POST, urlContext, new Saver());
    }

    private void refreshImgUrl() {
        imgUrl = S.builder(urlContext).append(imgPath).toString();
    }

    private void buildIndex() {
        addToIndex(docRepo.root());
    }

    private void addToIndex(DocFolder folder) {
        String path = folder.path();
        folderIndexes.add(path);
        List<RepoElement> elements = docRepo.list(path);
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
        RepoElement element = docRepo.fetch(path);
        Controller.Util.notFoundIfNull(element);
        if (element.isFolder()) {
            folderIndexes.add(path);
        } else {
            sourceIndexes.add(path);
        }
    }

    private class Getter extends ResourceHandlerBase {

        @Override
        protected Result _handle(String path, ActionContext context) {
            Result result;
            if (path.startsWith(imgPath)) {
                path = path.substring(imgPath.length() + 1);
                try {
                    InputStream is = imgRepo.get(path);
                    Controller.Util.notFoundIfNull(is);
                    return new RenderBinary(is, path, true);
                } catch (IOException e) {
                    return ActServerError.of(e);
                }
            }
            if (isFolder(path)) {
                List<RepoElement> elements = docRepo.list(path);
                C.List<Map<Object, Object>> list = C.newList(elements).sorted().filter(IS_NOT_IMG).map(TO_JSON);
                if (S.notBlank(path)) {
                    list.prepend(C.map("path", S.beforeLast(path, "/"), "isFolder", true, "label", ".."));
                }
                result = new RenderJSON(JSON.toJSONString(list));
            } else if (isSource(path)) {
                String source = docRepo.read(path);
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
            return "dockit:get:" + docRepo;
        }

    }

    private class Saver extends ResourceHandlerBase {

        @Override
        protected Result _handle(String path, ActionContext context) {
            if (path.startsWith(imgPath)) {
                String data = context.paramVal("data");
                notFoundIf(S.isBlank(data));
                badRequestIfNot(data.startsWith("data:image"));
                data = data.substring(data.indexOf(",") + 1);
                InputStream is = new ByteArrayInputStream(Codec.decodeBase64(data));
                String name = idGenerator.genId() + ".png";
                try {
                    imgRepo.persist(name, is);
                } catch (IOException e) {
                    throw E.ioException(e);
                }
                return new RenderJSON(C.newMap("url", S.builder(imgUrl).append("/").append(name)));
            }
            String content = context.paramVal("content");
            docRepo.persist(path, content);
            sourceIndexes.add(path);
            return Ok.INSTANCE;
        }

    }

    private $.Predicate<RepoElement> IS_NOT_IMG = new Osgl.Predicate<RepoElement>() {
        @Override
        public boolean test(RepoElement repoElement) {
            if (repoElement.isFolder()) {
                return !(repoElement.path().startsWith(imgPath));
            }
            return true;
        }
    };

    private static $.Transformer<RepoElement, Map<Object, Object>> TO_JSON = new $.Transformer<RepoElement, Map<Object, Object>>() {
        @Override
        public Map<Object, Object> transform(RepoElement repoElement) {
            Map<Object, Object> map = C.newMap();
            map.put("path", repoElement.path());
            map.put("isFolder", repoElement.isFolder());
            return map;
        }
    };

    public static class Builder {
        private DocKit docKit = new DocKit();

        public Builder docRepo(DocRepo repo) {
            docKit.docRepo = $.notNull(repo);
            return this;
        }

        public Builder imgRepo(ImgRepo repo) {
            docKit.imgRepo = $.notNull(repo);
            return this;
        }

        public Builder urlContext(String context) {
            docKit.urlContext = context;
            docKit.refreshImgUrl();
            return this;
        }

        public Builder imgPath(String path) {
            docKit.imgPath = path;
            docKit.refreshImgUrl();
            return this;
        }

        public Builder portName(String portName) {
            docKit.portName = portName;
            return this;
        }

        public DocKit build() {
            return docKit;
        }
    }


}
