package psn.shreker.temprepo.util;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import java.util.Map.Entry;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * httpClient工具类
 */
public class HttpClientUtils {
 
	// 编码格式。发送编码格式统一用UTF-8
	private static final String ENCODING = "UTF-8";
	
	// 设置连接超时时间，单位毫秒。
	private static final int CONNECT_TIMEOUT = 6000;
	
	// 请求获取数据的超时时间(即响应时间)，单位毫秒。
	private static final int SOCKET_TIMEOUT = 6000;
 
	/**
	 * 发送get请求；不带请求头和请求参数
	 */
	public static HttpClientResult get(String url) throws Exception {
		return get(url, null, null);
	}
	
	/**
	 * 发送get请求；带请求参数
	 */
	public static HttpClientResult get(String url, Map<String, String> params) throws Exception {
		return get(url, null, params);
	}
 
	/**
	 * 发送get请求；带请求头和请求参数
	 */
	public static HttpClientResult get(String url, Map<String, String> headers, Map<String, String> params) throws Exception {
		// 创建访问的地址
		URIBuilder uriBuilder = new URIBuilder(url);
		if (params != null) {
			Set<Entry<String, String>> entrySet = params.entrySet();
			for (Entry<String, String> entry : entrySet) {
				uriBuilder.setParameter(entry.getKey(), entry.getValue());
			}
		}
 
		// 创建http对象
		HttpGet httpGet = new HttpGet(uriBuilder.build());
		/*
		 * setConnectTimeout：设置连接超时时间，单位毫秒。
		 * setConnectionRequestTimeout：设置从connect Manager(连接池)获取Connection
		 * 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的。
		 * setSocketTimeout：请求获取数据的超时时间(即响应时间)，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
		 */
		RequestConfig requestConfig =
				RequestConfig.custom().setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build();
		httpGet.setConfig(requestConfig);
		
		// 设置请求头
		handleHeader(headers, httpGet);
 
		try ( CloseableHttpClient httpClient = HttpClients.createDefault();
			CloseableHttpResponse httpResponse = httpClient.execute(httpGet) ) {
			// 执行请求并获得响应结果
			
			return getHttpClientResult(httpResponse, httpClient);
		}
	}
 
	/**
	 * 发送post请求；不带请求头和请求参数
	 */
	public static HttpClientResult post(String url) throws Exception {
		return post(url, null, null);
	}
	
	/**
	 * 发送post请求；带请求参数
	 */
	public static HttpClientResult post(String url, Map<String, String> params) throws Exception {
		return post(url, null, params);
	}
 
	/**
	 * 发送post请求；带请求头和请求参数
	 */
	public static HttpClientResult post(String url, Map<String, String> headers, Map<String, String> params) throws Exception {
		// 创建http对象
		HttpPost httpPost = new HttpPost(url);
		/*
		 * setConnectTimeout：设置连接超时时间，单位毫秒。
		 * setConnectionRequestTimeout：设置从connect Manager(连接池)获取Connection
		 * 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的。
		 * setSocketTimeout：请求获取数据的超时时间(即响应时间)，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
		 */
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build();
		httpPost.setConfig(requestConfig);
		// 设置请求头
		/*
		httpPost.setHeader("Cookie", "");
		httpPost.setHeader("Connection", "keep-alive");
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
		httpPost.setHeader("Accept-Encoding", "gzip, deflate, br");
		httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
		*/
		handleHeader(headers, httpPost);
		
		// 封装请求参数
		packageParam(params, httpPost);
 
		try(CloseableHttpClient httpClient = HttpClients.createDefault();
			CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
			// 执行请求并获得响应结果
			return getHttpClientResult(httpResponse, httpClient);
		}
	}
 
	/**
	 * 发送put请求；不带请求参数
	 */
	public static HttpClientResult put(String url) throws Exception {
		return put(url, null);
	}
 
	/**
	 * 发送put请求；带请求参数
	 */
	public static HttpClientResult put(String url, Map<String, String> params) throws Exception {
		
		HttpPut httpPut = new HttpPut(url);
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build();
		httpPut.setConfig(requestConfig);
		
		packageParam(params, httpPut);
 
		try (CloseableHttpClient httpClient = HttpClients.createDefault();
			 CloseableHttpResponse httpResponse = httpClient.execute(httpPut)) {
			return getHttpClientResult(httpResponse, httpClient);
		}
	}
 
	/**
	 * 发送delete请求；不带请求参数
	 */
	public static HttpClientResult delete(String url) throws Exception {
		
		HttpDelete httpDelete = new HttpDelete(url);
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build();
		httpDelete.setConfig(requestConfig);
 
		try (CloseableHttpClient httpClient = HttpClients.createDefault();
			 CloseableHttpResponse httpResponse = httpClient.execute(httpDelete)) {
			return getHttpClientResult(httpResponse, httpClient);
		}
	}
 
	/**
	 * 发送delete请求；带请求参数
	 */
	public static HttpClientResult delete(String url, Map<String, String> params) throws Exception {
		if (params == null) {
			params = new HashMap<>();
		}
 
		params.put("_method", "delete");
		return post(url, params);
	}
	
	/**
	 * 封装请求头
	 */
	public static void handleHeader(Map<String, String> params, HttpRequestBase httpMethod) {
		// 封装请求头
		if (params != null) {
			Set<Entry<String, String>> entrySet = params.entrySet();
			for (Entry<String, String> entry : entrySet) {
				// 设置到请求头到HttpRequestBase对象中
				httpMethod.setHeader(entry.getKey(), entry.getValue());
			}
		}
	}
 
	/**
	 * 封装请求参数
	 */
	public static void packageParam(Map<String, String> params, HttpEntityEnclosingRequestBase httpMethod)
			throws UnsupportedEncodingException {
		// 封装请求参数
		if (params != null) {
			List<NameValuePair> nvps = new ArrayList<>();
			Set<Entry<String, String>> entrySet = params.entrySet();
			for (Entry<String, String> entry : entrySet) {
				nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
 
			// 设置到请求的http对象中
			httpMethod.setEntity(new UrlEncodedFormEntity(nvps, ENCODING));
		}
	}
 
	/**
	 * 获得响应结果
	 */
	public static HttpClientResult getHttpClientResult(CloseableHttpResponse httpResponse,
			CloseableHttpClient httpClient) throws Exception {
		// 获取返回结果
		if (httpResponse != null && httpResponse.getStatusLine() != null) {
			String content = "";
			if (httpResponse.getEntity() != null) {
				content = EntityUtils.toString(httpResponse.getEntity(), ENCODING);
			}
			return new HttpClientResult(httpResponse.getStatusLine().getStatusCode(), content);
		}
		return new HttpClientResult(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}
 
}