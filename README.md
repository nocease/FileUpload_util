# java-file-upload-class
java文件上传工具类封装，同时支持springMVC和fastDFS

有两种构造方法。需要文件表单名或MultipartFile对象和request对象
使用upload(String uploadpath)上传到项目类路径
使用uploadToFastDFS()方法上传到fastDFS，需要先配置好fastDFS
