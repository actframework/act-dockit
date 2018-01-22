package act.dockit;

import static act.controller.Controller.Util.badRequestIfNot;
import static act.controller.Controller.Util.notFoundIf;

import act.Act;
import act.app.ActionContext;
import act.app.App;
import act.app.event.AppEventId;
import act.controller.Controller;
import act.route.Router;
import act.util.IdGenerator;
import com.alibaba.fastjson.JSON;
import org.osgl.$;
import org.osgl.Osgl;
import org.osgl.http.H;
import org.osgl.logging.LogManager;
import org.osgl.logging.Logger;
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
import java.util.concurrent.TimeUnit;

/*

!!!IMPORTANT NOTE!!!

Implementation of DocKit is NOT an example of how to
write normal actframework application.

PLEASE DO NOT FOLLOW THE CODE BELOW TO WRITE YOUR OWN APP

Go to https://github.com/actframework/act-demo-apps for reference
 */

/**
 * Mount a {@link DocRepo} to a web application
 */
public class DocKit {

    public static final int COOLING_TIME_IN_SECONDS = 60 * 60;

    private static Logger logger = LogManager.get(DocKit.class);
    private static Map<String, DocKit> instances = C.newMap();

    DocRepo docRepo;
    ImgRepo imgRepo;
    String urlContext = "/doc";
    String imgPath = "/img";
    String imgUrl;
    String portName;
    Set<String> suffixies = C.set();
    private Set<String> sourceIndexes = C.newSet();
    private Set<String> folderIndexes = C.newSet();
    private IdGenerator idGenerator = new IdGenerator(".img.id.do.not.delete");

    DocKit() {
        refreshImgUrl();
        Act.jobManager().on(AppEventId.POST_START, new Runnable() {
            @Override
            public void run() {
                buildIndex();
                registerToRouter();
                instances.put(id(), DocKit.this);
            }
        });
        Act.jobManager().every("clean-orphan-img", new Runnable() {
            @Override
            public void run() {
                removeOrphanImages();
            }
        }, COOLING_TIME_IN_SECONDS, TimeUnit.SECONDS);
    }

    private void registerToRouter() {
        Router router = App.instance().router(portName);
        router.addMapping(H.Method.GET, urlContext, new Getter());
        router.addMapping(H.Method.POST, urlContext, new Saver());
        router.addMapping(H.Method.DELETE, urlContext, new Eraser());
    }

    void refreshImgUrl() {
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

    public String id() {
        StringBuilder sb = S.builder(this.urlContext);
        if (S.notBlank(portName)) {
            sb.append(':').append(portName);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return S.fmt("dockit@[%s]", urlContext);
    }

    public String debug() {
        return S.fmt("repo\n\turlContext:%s\n\timgUrlPath:%s\n\t%s\n\t%s", urlContext, imgPath, docRepo, imgRepo);
    }

    public void removeOrphanImages() {
        logger.info("cleaning orphan images ...");
        List<String> orphanImages = findOrphanImages();
        for (String path : orphanImages) {
            imgRepo.remove(path);
        }
    }

    public List<String> findOrphanImages() {
        List<String> allImagePaths = imgRepo.list(COOLING_TIME_IN_SECONDS);
        Set<String> orphanImagePaths = C.newSet();
        for (String path : allImagePaths) {
            String imgName = S.afterLast(path, "/");
            if (!searchText(imgName)) {
                logger.info("found orphan image: %s", path);
                orphanImagePaths.add(path);
            }
        }
        return C.list(orphanImagePaths);
    }

    private boolean searchText(String string) {
        DocFolder root = docRepo.root();
        return searchText(root, string);
    }

    private boolean searchText(RepoElement doc, String search) {
        if (doc.isFolder()) {
            List<RepoElement> list = docRepo.list(doc.path());
            for (RepoElement element: list) {
                if (searchText(element, search)) {
                    return true;
                }
            }
            return false;
        } else {
            String s = docRepo.read(doc.path());
            return s.contains(search);
        }
    }

    public static DocKit instance(String key) {
        if (S.blank(key)) {
            return instance();
        }
        return instances.get(key);
    }

    public static DocKit instance() {
        E.illegalStateIf(instances.size() != 1);
        return instances.values().iterator().next();
    }

    public static List<DocKit> instances() {
        return C.list(instances.values());
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
                    throw E.ioException(e);
                }
            }
            if (isFolder(path)) {
                List<RepoElement> elements = docRepo.list(path);
                C.List<Map<Object, Object>> list = C.newList(elements).sorted().filter(new Osgl.Predicate<RepoElement>() {
                    @Override
                    public boolean test(RepoElement repoElement) {
                        if (repoElement.isFolder()) {
                            return !(repoElement.path().startsWith(imgPath));
                        } else if (!suffixies.isEmpty()) {
                            String suffix = "." + S.afterLast(repoElement.path(), ".");
                            return suffixies.contains(suffix);
                        }
                        return true;
                    }
                }).map(new $.Transformer<RepoElement, Map<Object, Object>>() {
                    @Override
                    public Map<Object, Object> transform(RepoElement repoElement) {
                        Map<Object, Object> map = C.newMap();
                        map.put("path", repoElement.path());
                        map.put("isFolder", repoElement.isFolder());
                        return map;
                    }
                });
                if (S.notBlank(path)) {
                    list.prepend(C.Map("path", S.beforeLast(path, "/"), "isFolder", true, "label", ".."));
                }
                list.prepend(C.Map("path", path, "isFolder", true, "label", "."));
                result = new RenderJSON(JSON.toJSONString(list));
            } else if (isSource(path)) {
                String source = docRepo.read(path);
                if (path.endsWith(".html")) {
                    result = new RenderHtml(source);
                } else {
                    result = new RenderText(source);
                }
            } else {
                logger.warn("Resource not found by path: %s", path);
                result = NotFound.get();
            }
            return result;
        }

        @Override
        public String toString() {
            return "dockit:get:" + docRepo;
        }

    }

    private class Eraser extends ResourceHandlerBase {
        @Override
        protected Result _handle(String path, ActionContext context) {
            docRepo.delete(path);
            return Ok.INSTANCE;
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
            if (context.endsWith("/")) {
                context = context.substring(0, context.length() - 1);
            }
            docKit.urlContext = context;
            docKit.refreshImgUrl();
            return this;
        }

        public Builder imgPath(String path) {
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            docKit.imgPath = path;
            docKit.refreshImgUrl();
            return this;
        }

        public Builder portName(String portName) {
            docKit.portName = portName;
            return this;
        }

        public Builder suffixes(String suffixes) {
            docKit.suffixies = C.setOf(suffixes.split(S.COMMON_SEP));
            return this;
        }

        public DocKit build() {
            return docKit;
        }
    }

}
