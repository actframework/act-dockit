package act.dockit;

import act.cli.Command;
import act.cli.Optional;
import act.util.PropertySpec;

import java.util.List;

public class DockitAdmin {

    @Command(name = "act.dockit.list", help = "List dockit instances")
    @PropertySpec("urlContext as ID")
    public List<DocKit> list() {
        return DocKit.instances();
    }

    @Command(name = "act.dockit.list-orphan-img", help = "List orphan images")
    @PropertySpec("this as path")
    public List<String> listOrphianImages(
            @Optional("specify dockit url context and port name if there are multiple dockits started") String id
    ) {
        DocKit docKit = DocKit.instance(id);
        return docKit.findOrphanImages();
    }

    @Command(name = "act.dockit.remove-orphan-img", help = "Remove orphan images")
    public void removeOrphianImages(
            @Optional("specify dockit url context and port name if there are multiple dockits started") String id
    ) {
        DocKit docKit = DocKit.instance(id);
        docKit.removeOrphanImages();
    }

}
