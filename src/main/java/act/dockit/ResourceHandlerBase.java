package act.dockit;

import act.app.ActionContext;
import act.controller.ParamNames;
import act.handler.builtin.controller.FastRequestHandler;
import org.osgl.mvc.result.Result;

abstract class ResourceHandlerBase extends FastRequestHandler {

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
