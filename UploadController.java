package cn.nocease.util;

import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.StorageServer;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.csource.common.MyException;
import java.io.File;
import org.csource.common.NameValuePair;
import java.net.URISyntaxException;
import java.util.UUID;
import org.csource.fastdfs.ClientGlobal;

/*
*
*  文件上传工具类V4.2
*  支持了fastDFS，配置文件名为client.conf
*
* */

public class UploadController {
	// 配置信息
	private static final String fastdfs_ip="http://upload.qicp.vip/";
	private static final String conf_Path= "/client.conf";

	//传入信息
	private String formName;// 文件表单名
	private HttpServletRequest request;
	private MultipartFile file;

	// 返回信息
	private String oldName;// 选中的文件名，用于获取后缀
	private String etc;// 文件后缀名（没有.）
	private String pathName = null;// 绝对路径加文件名(保存到数据库)

	// 构造方法
	public UploadController(String formName, HttpServletRequest request) {
		super();
		this.formName = formName;
		this.request=request;
		MultipartHttpServletRequest muiRequest = (MultipartHttpServletRequest) request;
		this.file = muiRequest.getFile(this.formName);
	}

	//构造方法2
	public UploadController(MultipartFile file, HttpServletRequest request) {
		super();
		this.request=request;
		this.file = file;
	}

	//上传文件到static
	public void upload(String uploadpath) {
		try {
			File path = new File(new File(ResourceUtils.getURL("classpath:").toURI().getPath()).getAbsolutePath(), "/static");
			if (this.file != null && this.file.getSize() > 0) {
				String realpath =new StringBuffer().append(path).append("/").append(uploadpath).append("/").toString();
				File dir = new File(realpath);
				String uuid = UUID.randomUUID().toString().replaceAll("-", "");
				if (!dir.exists()) {
					dir.mkdirs();
				}
				this.oldName = this.file.getOriginalFilename();
				this.etc = this.oldName.substring(this.oldName.lastIndexOf(".") + 1);
				File destFile = new File(dir, new StringBuffer().append(uuid).append(".").append(this.etc).toString());
				this.file.transferTo(destFile);
				this.pathName = new StringBuffer().append("/").append(uploadpath).append("/").append(uuid)
						.append(".").append(this.etc).toString();
				System.out.println(new StringBuffer().append("上传文件成功：").append(this.pathName));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//上传文件到fastDFS
	public void uploadToFastDFS(){
		 if (this.file != null && this.file.getSize() > 0){
			 this.oldName = this.file.getOriginalFilename();
			 this.etc = this.oldName.substring(this.oldName.lastIndexOf(".") + 1);
			 FastDFSClient client = new FastDFSClient("classpath:"+conf_Path);
			 String path = null;
			 try {
				 path = client.uploadFile(this.file.getBytes(), this.etc);
			 } catch (IOException e) {
				 e.printStackTrace();
			 }
			 this.pathName=new StringBuffer().append(fastdfs_ip).append(path).toString();
			 System.out.println("上传文件到FastDFS成功："+this.pathName);
		 }
	}

	public String getOldName() {
		return oldName;
	}

	public String getEtc() {
		return etc;
	}

	public String getPathName() {
		return pathName;
	}

}
class FastDFSClient {
	private TrackerClient trackerClient = null;
	private TrackerServer trackerServer = null;
	private StorageClient1 storageClient = null;
	private StorageServer storageServer = null;

public FastDFSClient(String conf) {
		try {
			if (conf.contains("classpath:")) {
				conf=conf.replaceAll("classpath:", this.getClass().getResource("/").toURI().getPath());
			}
			ClientGlobal.init(conf);
			trackerClient = new TrackerClient();
			trackerServer=trackerClient.getConnection();
			storageClient=new StorageClient1(trackerServer,storageServer);
		} catch (IOException | MyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
}
	public String uploadFile(String fileName,String extName,NameValuePair[] metas) {
		try {
			return storageClient.upload_file1(fileName, extName, metas);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public String uploadFile(String fileName,String extName) {
		return this.uploadFile(fileName, extName, null);
	}
	public String uploadFile(byte[] content,String extName,NameValuePair[] metas) {
		try {
			return storageClient.upload_file1(content, extName, metas);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public String uploadFile(byte[] content,String extName) {
		return this.uploadFile(content, extName, null);
	}
}