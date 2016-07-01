package net.ymate.platform.mock.web;

import net.ymate.platform.webmvc.IMultipartRequestWrapper;
import net.ymate.platform.webmvc.IUploadFileWrapper;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.util.FileUploadHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;

import java.io.File;
import java.util.*;

public class MockMultipartHttpServletRequest extends MockHttpServletRequest implements IMultipartRequestWrapper {

    private final Log _LOG = LogFactory.getLog(getClass());

    private final Map<String, List<IUploadFileWrapper>> __multipartFiles = new LinkedHashMap<String, List<IUploadFileWrapper>>();

    public MockMultipartHttpServletRequest() {
        setMethod("POST");
        setContentType("multipart/form-data");
    }

    public void addFile(String name, final File file) {
        Assert.assertNotNull("MultipartFile name must not be null", name);
        Assert.assertNotNull("MultipartFile must not be null", file);
        if (!__multipartFiles.containsKey(name)) {
            __multipartFiles.put(name, new LinkedList<IUploadFileWrapper>());
        }
        __multipartFiles.get(name).add(new FileUploadHelper.UploadFileWrapper(file) {
            @Override
            public void delete() {
                // 不真正删除测试文件
                _LOG.info("Delete file \"" + file.getPath() + "\"");
            }
        });
    }

    public Type.HttpMethod getRequestMethod() {
        return Type.HttpMethod.valueOf(getMethod());
    }

    public IUploadFileWrapper getUploadFile(String name) {
        if (__multipartFiles.containsKey(name)) {
            List<IUploadFileWrapper> _files = __multipartFiles.get(name);
            return _files.isEmpty() ? null : _files.get(0);
        }
        return null;
    }

    public IUploadFileWrapper[] getUploadFiles(String name) {
        if (__multipartFiles.containsKey(name)) {
            List<IUploadFileWrapper> _files = __multipartFiles.get(name);
            return _files.isEmpty() ? new IUploadFileWrapper[0] : _files.toArray(new IUploadFileWrapper[_files.size()]);
        }
        return null;
    }

    public Set<IUploadFileWrapper> getUploadFiles() {
        Set<IUploadFileWrapper> _returnValues = new HashSet<IUploadFileWrapper>();
        for (List<IUploadFileWrapper> _fileWraps : __multipartFiles.values()) {
            _returnValues.addAll(_fileWraps);
        }
        return _returnValues;
    }
}
