# Act-Dockit

This is a semi application that could be embedded into your project to facilitate project documentation creation/viewing
in a application written by ActFramework.

Act-dockit contains two independent part connected by a well defined RESTful API

## Backend

The backend of Act-dockit is a file repository which provides the following functions

* A file system navigator, reading file directory information
* Reading file meta information
* Reading file content
* Saving new file content

## RESTful APIs

### Configuration

* 

### Read file system

```
GET {ctx}/{path}
```

When `{path}` pointing to a directory, this API will response with a JSON structure that enumerate the direct files 
in the directory. E.g.

```
[
    {"name": "README.md", "type": "file"},
    {"name": "doc", "type": "dir"},
    {"name": "abc.txt", "type": "file"},
    {"name": "abc123.png", "type": "file"}
]
```

When `{path}` point to a file, this API will return the content of the file 

### Upload a binary file

```
POST {ctx}/
```




