function Store() {
  riot.observable(this)
  var self = this
  var _showNav = true, _repoUrl, _imgUrl, _path, _list, _content
  var setList = function (list) {
    _list = list
    self.trigger(EVENT.LIST_UPDATED)
  }
  var target = function () {
    var load = _repoUrl
    if (_path) {
      load = load + _path
    }
    return load
  }
  var curDoc = function () {
    if (!_list) return false
    for (var i = 0, j = _list.length; i < j; ++i) {
      if (_list[i].current) {
        return _list[i]
      }
    }
    return false
  }
  var curDocUrl = function () {
    var doc = curDoc()
    return doc ? doc.url : false
  }
  var loadRepo = function () {
    $.get(target(), onRemoteListLoad)
  }
  var onRemoteListLoad = function (data) {
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
    for (var i = 0, j = list.length; i < j; ++i) {
      var x = list[i]
      if (!x.isFolder) {
        RiotControl.trigger(EVENT.LOAD_DOC, x)
        return
      }
    }
  }
  var saveDoc = function () {
    var target = curDocUrl()
    if (!target) {
      return
    }
    $.post(target, {content: _content}, function () {
      self.trigger(EVENT.CONTENT_SAVED)
    })
  }
  var deleteDoc = function () {
    var target = curDocUrl()
    if (!target) {
      return
    }
    $.ajax({
      url: target,
      method: 'DELETE',
      success: function () {
        for (var i = 0; i < _list.length; ++i) {
          var doc = _list[i]
          if (doc.url == target) {
            _list.split(i, i + 1)
            return
          }
        }
      }
    })
  }
  var renameDoc = function (newName) {
    var doc = curDoc()
    if (!doc) {
      return
    }
    _list.forEach(function (x) {
      if (x.current) {
        if (x.label == newName) return
        var oldUrl = x.url
        var url = newName
        if (!newName.startsWith('/')) {
          url = _repoUrl + '/' + newName
        }
        x.url = url
        x.label = newName
        $.post(url, {content: _content}, function () {
          $.ajax({url: oldUrl, method: 'DELETE'})
          self.trigger(EVENT.CONTENT_SAVED)
          self.trigger(EVENT.LIST_UPDATED)
        })
      }
    })
  }
  self.displayNav = function () {
    return _showNav
  }
  self.getList = function () {
    return _list
  }
  self.getContent = function () {
    return _content
  }
  self.on(EVENT.TOGGLE_NAV, function () {
    _showNav = !_showNav;
    self.trigger(EVENT.NAV_TOGGLED)
  })
  self.on(EVENT.LOAD_DOC, function (item) {
    saveDoc()
    var url = item.url
    if (item.isFolder) {
      $.get(url, onRemoteListLoad)
    } else if (url.endsWith('.md')) {
      $.get(url, function (content) {
        RiotControl.trigger(EVENT.REMOTE_CONTENT_LOADED, content, item)
      })
    }
  })
  self.on(EVENT.EDITOR_UPDATED, function (content) {
    _content = content
    self.trigger(EVENT.CONTENT_UPDATED, content)
    saveDoc()
  })
  self.on(EVENT.REMOTE_CONTENT_LOADED, function (content, item) {
    _content = content;
    _list.forEach(function (element) {
      if (element == item) {
        element.current = true
      } else {
        delete element.current
      }
    })
    self.trigger(EVENT.CONTENT_LOADED, content)
  })
  self.on(EVENT.REMOTE_CONFIG_LOADED, function (config) {
    _repoUrl = config.repoUrl
    _imgUrl = _repoUrl + config.imgPath
    loadRepo()
  })
  self.on(EVENT.IMG_PASTED, function (blob) {
    var reader = new FileReader()
    reader.onload = function (e) {
      $.post(_imgUrl, {data: e.target.result}, function (data) {
        self.trigger(EVENT.IMG_UPLOADED, data.url);
      })
    }
    reader.readAsDataURL(blob);
  })
  self.on(EVENT.BIG_INPUT_ENTERED, function (value, type) {
    if ('new-filename' == type) {
      _list.forEach(function (x) {
        delete x.current
      })
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
      RiotControl.trigger(EVENT.CONTENT_LOADED, _content)
    } else if ('rename' == type) {
      renameDoc(value)
    }
  })

  document.addEventListener('keydown', function (e) {
    if (e.ctrlKey || e.metaKey) {
      if (e.keyCode == 83) { // ctrl-s
        e.preventDefault()
        saveDoc()
        return false
      } else if (e.keyCode == 77) { // ctrl-m
        e.preventDefault()
        var filename = '/new-file.md'
        if (_path) {
          filename = _path + filename
        }
        saveDoc()
        RiotControl.trigger(EVENT.ASK_NEW_FILENAME, filename)
        return false
      } else if (e.keyCode == 113) { // ctr-f2
        var doc = curDocUrl()
        if (doc) {
          e.preventDefault()
          saveDoc()
          var filename = doc
          RiotControl.trigger(EVENT.ASK_RENAME, filename)
        }
      } else if (e.keyCode == 49) { // ctrl-alt-1
        self.trigger(EVENT.TOGGLE_NAV)
      }
    }
  });
}