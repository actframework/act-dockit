package act.dockit;

import act.app.App;
import act.conf.AppConfig;
import act.plugin.AppServicePlugin;
import org.osgl.logging.LogManager;
import org.osgl.logging.Logger;
import org.osgl.util.S;

import java.io.File;

public class DocKitConfig extends AppServicePlugin {

    private static final Logger logger = LogManager.get(DocKitConfig.class);

    public static final String CONF_ENABLE_DOCKIT = "dockit.enabled";
    public static final String CONF_REPO_ROOT = "dockit.repo.fs.root";
    public static final String CONF_IMG_PATH = "dockit.img.path";
    public static final String CONF_REPO_URL = "dockit.repo.url";
    public static final String CONF_EDITOR_URL = "dockit.editor.url";
    public static final String CONF_PORT_NAME = "dockit.port.name";

    @Override
    protected void applyTo(App app) {
        AppConfig config = app.config();
        Object o = config.get(CONF_ENABLE_DOCKIT);
        if (null != o && Boolean.parseBoolean(o.toString())) {
            loadDockit(config);
        }
    }

    private void loadDockit(AppConfig<?> config) {

        logger.info("loading dockit ...");

        String repoRoot = getConf(config, CONF_REPO_ROOT, ".");
        String imgPath = getConf(config, CONF_IMG_PATH, "img");
        String repoUrl = getConf(config, CONF_REPO_URL, "/doc");
        String editorUrl = getConf(config, CONF_EDITOR_URL, "/dockit");
        String portName = getConf(config, CONF_PORT_NAME, null);

        FsDocRepo docRepo = new FsDocRepo(new File(repoRoot));
        FsImgRepo imgRepo = new FsImgRepo(new File(S.builder(repoRoot).append("/").append(imgPath).toString()));
        DocKit docKit = new DocKit.Builder().docRepo(docRepo).imgRepo(imgRepo).urlContext(repoUrl).imgPath(imgPath).suffixes(".md").portName(portName).build();
        DocEditor docEditor = new DocEditor.Builder().urlContext(editorUrl).repoUrl(repoUrl).imgPath(imgPath).portName(portName).build();

        logger.info(docKit.debug());
        logger.info(docEditor.debug());
    }

    private String getConf(AppConfig<?> config, String key, String defVal) {
        String val = config.get(key);
        return null == val ? defVal : val;
    }
}
