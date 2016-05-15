function Store() {
    riot.observable(this)
    var self = this
    var _showNav = true, _docUrl, _path, _list, _content
    var setList = function(list) {
        _list = list
        self.trigger('list-updated')
    }
    var target = function() {
        var load = _docUrl
        if (_path) {
            load = load + _path
        }
        return load
    }
    var curDoc = function() {
        if (!_list) return false
        for (var i = 0, j = _list.length; i < j; ++i) {
            if (_list[i].current) {
                return _list[i].url
            }
        }
        return false
    }
    var loadDoc = function() {
        $.get(target(), function(data) {
            var list = []
            if (data) {
                data.forEach(function(x) {
                    list.push({
                        'label': x,
                        'url': _docUrl + x
                    })
                })
            }
            setList(list)
        })
    }
    var saveDoc = function() {
        var target = curDoc()
        if (!target) {
            return
        }
        $.post(target, {content: _content}, function() {
            self.trigger('content-saved')
        })
    }
    self.displayNav = function() {
        return _showNav
    }
    self.getList = function() {
        return _list
    }
    self.getContent = function() {
        return _content
    }
    self.on('toggle-nav', function() {
        _showNav = !_showNav;
        self.trigger('nav-toggled')
    })
    self.on('editor-updated', function(content) {
        _content = content;
        self.trigger('content-updated', content)
    })
    self.on('remote-content-loaded', function(content, item) {
        _content = content;
        _list.forEach(function(element) {
            if (element == item) {
                element.current = true
            } else {
                delete element.current
            }
        })
        self.trigger('content-loaded', content)
    })
    self.on('remote-config-loaded', function(config) {
        _docUrl = config.docUrl
        loadDoc()
    })

    document.addEventListener('keydown', function (e) {
        if (e.keyCode == 83 && (e.ctrlKey || e.metaKey)) {
            //e.shiftKey ? showMenu() : saveAsMarkdown();
            e.preventDefault()
            saveDoc()
            return false
        } else if (e.keyCode == 86 && (e.ctrlKey || e.metaKey)) {
            console.log(e)
        }
    });
}