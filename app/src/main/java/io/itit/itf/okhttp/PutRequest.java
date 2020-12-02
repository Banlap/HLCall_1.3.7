package io.itit.itf.okhttp;

import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.itit.itf.okhttp.PostRequest.FileInfo;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 
 * @author icecooly
 *
 */
public class PutRequest extends OkHttpRequest {
	//
	public static Logger logger = LoggerFactory.getLogger(PutRequest.class);
	//
	public PutRequest(String url,
			Object tag,
			Map<String, String> params, 
			Map<String, String> headers,
			List<FileInfo> fileInfos,
			String body,
			MultipartBody multipartBody,int id) {
		super(url, tag,params,headers, fileInfos,body,multipartBody,id);
	}

	@Override
	protected RequestBody buildRequestBody() {
		if(multipartBody!=null) {
			return multipartBody;
		}else if(fileInfos!=null && fileInfos.size()>0) {
			final MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
			addParams(builder);
			fileInfos.forEach(new Consumer<FileInfo>() {
				@Override
				public void accept(FileInfo fileInfo) {
					RequestBody fileBody = null;
					if (fileInfo.file != null) {
						fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), fileInfo.file);
					} else if (fileInfo.fileInputStream != null) {
						fileBody = createRequestBody(MediaType.parse("application/octet-stream"), fileInfo.fileInputStream);
					} else {
						fileBody = RequestBody.create(MediaType.parse(getMimeType(fileInfo.fileName)),
								fileInfo.fileContent);
					}
					builder.addFormDataPart(fileInfo.partName, fileInfo.fileName, fileBody);
				}
			});
			if(body!=null&&body.length()>0){
				builder.addPart(RequestBody.create(MultipartBody.FORM,body));
			}
			return builder.build();
		}else if(body!=null&&body.length()>0){
			MediaType mediaType=null;
			if(headers.containsKey("Content-Type")){
				mediaType = MediaType.parse(headers.get("Content-Type"));
			}else{
				mediaType = MediaType.parse("text/plain;charset=utf-8");
			}
			return RequestBody.create(mediaType,body);
		}else{
			FormBody.Builder builder = new FormBody.Builder();
			addParams(builder);
			FormBody formBody = builder.build();
			return formBody;
		}
	}

	@Override
	protected Request buildRequest(RequestBody requestBody) {
		return builder.put(requestBody).build();
	}

	private void addParams(final FormBody.Builder builder) {
		if (params!= null) {
			params.forEach(new BiConsumer<String, String>() {
				@Override
				public void accept(String k, String v) {
					builder.add(k, v);
				}
			});
		}
		if (encodedParams!= null) {
			encodedParams.forEach(new BiConsumer<String, String>() {
				@Override
				public void accept(String k, String v) {
					builder.addEncoded(k, v);
				}
			});
		}
	}
	//
	private void addParams(final MultipartBody.Builder builder) {
		if (params != null && !params.isEmpty()) {
			params.forEach(new BiConsumer<String, String>() {
				@Override
				public void accept(String k, String v) {
					builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + k + "\""),
							RequestBody.create(null, v));
				}
			});
		}
	}
	//
	public static String getMimeType(String path) {
		FileNameMap fileNameMap = URLConnection.getFileNameMap();
		String contentTypeFor = null;
		try {
			contentTypeFor = fileNameMap.getContentTypeFor(URLEncoder.encode(path, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(),e);
		}
		if (contentTypeFor == null) {
			contentTypeFor = "application/octet-stream";
		}
		return contentTypeFor;
	}
}