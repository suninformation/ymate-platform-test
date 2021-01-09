package net.ymate.platform.mock.web;

import net.ymate.platform.webmvc.IMultipartRequestWrapper;
import net.ymate.platform.webmvc.IUploadFileWrapper;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.util.FileUploadHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;

import javax.servlet.ServletContext;
import java.io.File;
import java.util.*;

public class MockMultipartHttpServletRequest extends MockHttpServletRequest implements IMultipartRequestWrapper {

    private final Log LOG = LogFactory.getLog(getClass());

    private final Map<String, List<IUploadFileWrapper>> multipartFiles = new LinkedHashMap<>();

    public MockMultipartHttpServletRequest() {
        this(null);
    }

    public MockMultipartHttpServletRequest(ServletContext servletContext) {
        super(servletContext);
        setMethod("POST");
        setContentType("multipart/form-data");
    }

    public void addFile(String name, final File file) {
        Assert.assertNotNull("MultipartFile name must not be null", name);
        Assert.assertNotNull("MultipartFile must not be null", file);
        if (!multipartFiles.containsKey(name)) {
            multipartFiles.put(name, new LinkedList<>());
        }
        multipartFiles.get(name).add(new FileUploadHelper.UploadFileWrapper(file) {
            @Override
            public void delete() {
                // 不真正删除测试文件
                LOG.info("Delete file \"" + file.getPath() + "\"");
            }
        });
    }

    public Type.HttpMethod getRequestMethod() {
        return Type.HttpMethod.valueOf(getMethod());
    }

    @Override
    public IUploadFileWrapper getUploadFile(String name) {
        if (multipartFiles.containsKey(name)) {
            List<IUploadFileWrapper> fileWrappers = multipartFiles.get(name);
            return fileWrappers.isEmpty() ? null : fileWrappers.get(0);
        }
        return null;
    }

    @Override
    public IUploadFileWrapper[] getUploadFiles(String name) {
        if (multipartFiles.containsKey(name)) {
            List<IUploadFileWrapper> fileWrappers = multipartFiles.get(name);
            return fileWrappers.isEmpty() ? new IUploadFileWrapper[0] : fileWrappers.toArray(new IUploadFileWrapper[0]);
        }
        return null;
    }

    @Override
    public Set<IUploadFileWrapper> getUploadFiles() {
        Set<IUploadFileWrapper> returnValues = new HashSet<>();
        for (List<IUploadFileWrapper> fileWrappers : multipartFiles.values()) {
            returnValues.addAll(fileWrappers);
        }
        return returnValues;
    }
}
