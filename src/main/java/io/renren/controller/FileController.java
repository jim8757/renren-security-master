package io.renren.controller;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.shiro.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import io.renren.utils.R;


/**
 * 资源文件处理
 * 
 * @author shangpan
 * 
 * @date Feb 21, 2017
 *
 */
@RestController
//@RequestMapping("/file")
public class FileController extends AbstractController {
	/**
	 * 上传图片
	 * @throws IOException 
	 */
	@RequestMapping("/fileupload")
	public R upload(HttpServletRequest request) throws IOException{
		String filePath = "E:/tmp/renren/";
		// 创建一个通用的多部分解析器    
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());    
        // 判断 request是否有文件上传  
        if(multipartResolver.isMultipart(request)){ 
        	// 创建目录
        	FileUtils.forceMkdir(new File(filePath));
            // 转换成多部分request      
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;  
            // 取得request中的所有文件名  
            Iterator<String> iter = multiRequest.getFileNames();  
            while (iter.hasNext()) {
                // 取得上传文件  
                List<MultipartFile> files = multiRequest.getFiles(iter.next());
                if (!CollectionUtils.isEmpty(files)) {
                	for (MultipartFile file : files) {
                		// 取得当前上传文件的文件名称  
                        String myFileName = file.getOriginalFilename(); 
                        File file2 = new File(filePath, myFileName);
                        try {
        					file.transferTo(file2);
        					System.out.println("文件上传: "+myFileName);
        				} catch (IllegalStateException | IOException e) {
        					e.printStackTrace();
        				}
                	}
                }
                
            } 
        }
        
		return R.ok().put("filePath", filePath);
	}
	
//	@Autowired
//	private SysConfigService sysConfigService;

	/*@Autowired
	private ResourcePath resPath;
	
	*//**
	 * 后台接收数组，然后循环遍历操作
	 * 
	 * @return
	 *//*
//	@SysLog("H5+Bootstrap3.x实现的文件上传")
	@RequestMapping(value = "/file", method = RequestMethod.POST)
	public Map<String, Object> uploadFile() {
		// 创建一个通用的多部分解析器
//		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
		// 文件上传配置spring-mvc.xml
		CommonsMultipartResolver multipartResolver = (CommonsMultipartResolver) SpringContextUtils.getBean("multipartResolver");
		// uploaded file url list
		List<String> urlList = new LinkedList<>();
		// 判断 request是否有文件上传
		if (!multipartResolver.isMultipart(request)) {
			return ResultMap.error("不是文件上传表单");
		} else {
			// 转换成多部分request
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
			// 取得MultipartHttpServletRequest中的所有文件名
			Iterator<String> iter = multiRequest.getFileNames();
			while (iter.hasNext()) {
				String baseurl = null;
				String orgname = null;
				String filename = null;
				// 取得上传文件
				List<MultipartFile> files = multiRequest.getFiles(iter.next());
				if (files != null) {
					for (MultipartFile file : files) {
						if (file != null) {
							orgname = file.getOriginalFilename();
							logger.info("——————>待上传的源文件名【{}】", orgname);
							baseurl = "/sys/res/" + (FileUtils.isImage(orgname) ? "img/" : ((FileUtils.isVideo(orgname)) ? "video/" : "file/"));
							String uuid = StrUtils.uuid();
							String ext = "." + PathUtils.getExt(orgname);
							filename = uuid + ext;
							// filename = orgname; // 上传的原始文件名称
							Path path = null;
							try {
								if (FileUtils.isImage(orgname)) { // 如果是图片文件，则压缩处理
									path = resPath.getPath(Constants.RES_DIR_USER_HEAD, filename);
//									int size_s = 48;
									int size_m = 72;
									int size_h = 196;
									Long userId = getUserId();
									final String userHeadTmp = userId + "_" + uuid + "_" + "head";
//									String filename_s = userHeadTmp + Constants.IMAGE_SMALL + ext;
									String filename_m = userHeadTmp + Constants.IMAGE_MIDDLE + ext;
									String filename_h = userHeadTmp + Constants.IMAGE_HIGHT + ext;
//									File sfile = resPath.getPath(Constants.RES_DIR_USER_HEAD, filename_s).toFile();
									File mfile = resPath.getPath(Constants.RES_DIR_USER_HEAD, filename_m).toFile();
									File hfile = resPath.getPath(Constants.RES_DIR_USER_HEAD, filename_h).toFile();
									// 存储原始图片
									PathUtils.save(file.getInputStream(), path, true);
									// 存储原始图片文件的缩略图
									byte[] imageData = file.getBytes();
//									Thumbnails.of(new ByteArrayInputStream(imageData)).size(size_s, size_s).toFile(sfile);
									Thumbnails.of(new ByteArrayInputStream(imageData)).size(size_m, size_m).toFile(mfile);
									Thumbnails.of(new ByteArrayInputStream(imageData)).size(size_h, size_h).toFile(hfile);
									urlList.add(baseurl + filename);
									logger.info("——————>上传图片文件【{}】到【{}】", orgname, path.toString().replaceAll(",", ""));
//									 urlList.add(baseurl + filename_s);
									 urlList.add(baseurl + filename_m);
									 urlList.add(baseurl + filename_h);
								} else { // 非图片文件处理
									path = resPath.getPath(Constants.RES_DIR_WORKSTATION, filename);
									PathUtils.save(file.getInputStream(), path, true);
									urlList.add(baseurl + filename);
									logger.info("——————>上传图片文件【{}】到【{}】", orgname, path.toString().replaceAll(",", ""));
								}
							} catch (IOException e) {
								logger.error("保存上传文件【{}】失败:{}", path.toString(), e.getMessage(), e);
								return ResultMap.error("保存上传文件出错!");
							}
						} else {
							return ResultMap.error("不存在待上传的文件!");
						}
					}
				}
			} // end while-loop
		}
		return ResultMap.ok().put("url", urlList);
	}
	
	@RequestMapping(value = "/img/{filename}.{ext}", method = RequestMethod.GET, produces = { "image/*" })
	public FileSystemResource getImage(@PathVariable String filename, @PathVariable String ext) {
		Path path = resPath.getPath(Constants.RES_DIR_USER_HEAD, filename + "." + ext);
		if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
			throw new RRException("资源未找到");
		}
		return new FileSystemResource(path.toString());
	}*/
	
	
	
	
//	@RequestMapping(value = "/video/{filename}.{ext}", method = RequestMethod.GET)
//	public void getVideo(@PathVariable String filename, @PathVariable String ext, HttpServletResponse resp) {
//		Path path = resPath.getPath(Constants.RES_DIR_ARTICLE_FILE, filename + "." + ext);
//		if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
//			throw new RRException("资源未找到");
//		}
//		try {
//			response.setContentType("video/mp4");
//			response.setContentLength((int) Files.size(path));
//			PathUtils.writeTo(path, response.getOutputStream());
//		} catch (IOException e) {
//			logger.error("write video " + path.toString(), e);
//			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
//		}
//	}
	
}
