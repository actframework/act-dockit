function Store() {
    riot.observable(this)
    var self = this
    var _showNav = true, _repoUrl, _imgUrl, _path, _list, _content
    var setList = function(list) {
        _list = list
        self.trigger('list-updated')
    }
    var target = function() {
        var load = _repoUrl
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
    var loadRepo = function() {
        $.get(target(), onRemoteListLoad)
    }
    var onRemoteListLoad = function(data) {
        var list = []
        if (data) {
            for (var i = 0, j = data.length; i < j; ++i) {
                var x = data[i]
                if (x.label == '.') {
                    _path = x.path
                } else {
                    list.push({
                        'label': x.label ? x.label : x.path,
                        'url': _repoUrl + x.path,
                        'isFolder': x.isFolder
                    })
                }
            }
        }
        setList(list)
        for(var i = 0, j = list.length; i < j; ++i) {
            var x = list[i]
            if (!x.isFolder) {
                RiotControl.trigger('load-doc', x)
                return
            }
        }
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
    self.on('load-doc', function(item) {
        saveDoc()
        var url = item.url
        if (item.isFolder) {
            $.get(url, onRemoteListLoad)
        } else if (url.endsWith('.md')) {
            $.get(url, function(content) {
                RiotControl.trigger('remote-content-loaded', content, item)
            })
        }
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
        _repoUrl = config.repoUrl
        _imgUrl = _repoUrl + config.imgPath
        loadRepo()
    })
    self.on('img-pasted', function(blob) {
        var reader = new FileReader()
        reader.onload = function(e) {
            $.post(_imgUrl, {data: e.target.result}, function(data) {
                self.trigger('img-uploaded', data.url);
            })
        }
        reader.readAsDataURL(blob);
    })
    self.on('big-input-entered', function(value, type) {
        if ('new-filename' == type) {
            _list.forEach(function(x) {delete x.current})
            var url = _repoUrl + value
            if (!value.startsWith('/')) {
                url = _repoUrl + '/' + value
            }
            _list.push({
                url: url,
                label: value,
                isFolder: false,
                current: true
            })
            _content = ''
            RiotControl.trigger('content-loaded', _content)
        } else if ('rename' == type) {
            _list.forEach(function(x) {
                if (x.current) {
                    if (x.label == value) return
                    var url = _repoUrl + value
                    if (!value.startsWith('/')) {
                        url = _repoUrl + '/' + value
                    }
                    x.url = url
                    x.label = value
                }
            })
            RiotControl.trigger('list-updated')
        }
    })

    document.addEventListener('keydown', function (e) {
        if (e.ctrlKey || e.metaKey) {
            if (e.keyCode == 83) { // ctrl-s
                e.preventDefault()
                saveDoc()
                return false
            } else if (e.keyCode == 77) { // ctrl-b
                e.preventDefault()
                var filename = '/new-file.md'
                if (_path) {
                    filename = _path + filename
                }
                saveDoc()
                RiotControl.trigger('ask-new-filename', filename)
                return false
            } else if (e.keyCode == 113) { // ctr-f2
                var doc = curDoc()
                if (doc) {
                    e.preventDefault()
                    saveDoc()
                    var filename = doc
                    RiotControl.trigger('ask-rename', filename)
                }
            }
        }
    });
}