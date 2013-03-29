package net.yoojia.updatewatcher;



/**
 * 将服务端响应解析数据为Version对象
 */
public interface ResponseParser {
	int parser(String response);
}