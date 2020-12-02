package hzhl.net.hlcall.constant;

public class Constants {

	public static final String EVENT_UPDATE_CONTACTS = "update_contacts";
	public static final String EVENT_QUERY_CONTACTS = "query_contacts";
	public static final String EVENT_UPDATE_CALL_RECORD = "update_call_record";
	//wenyeyang
	public static final String EVENT_UPDATE_UN_READ = "update_un_read";
	public static final String EVENT_UPDATE_LOGIN_STATUS = "update_un_read";


	public static final String EVENT_FloatWindowService = "startFloatWindowService";
	public static final String BROADCASE_ADDRESS = ".broadcase";
	
	public static final String BROADCASE_INTENT = "intent";

	public static final String DB_NAME = "greendao";

	/** 广播--�?�� **/
	public static final int BROADCASE_INTENT_EXIT = 701;
	/** 广播--发起网络请求 **/
	public static final int BROADCASE_INTENT_START_REQUEST = 702;
	/** 广播--结束网络请求 **/
	public static final int BROADCASE_INTENT_END_REQUEST = 703;
	/** 广播--切换帐号 **/
	public static final int BROADCASE_CHANGE_ACCOUNT = 704;
	/** 广播--登录成功 **/
	public static final int BROADCASE_LOGIN_SUCCESS = 705;
	/** 广播--切换语言版本 **/
	public static final int BROADCASE_CHANGE_LANGUAGE = 706;
	

	

	/**
	 * 客户�?校验 Header key
	 */
	public static final String MOBILE_HEADER_KEY = "Cookie";
	
	/** 默认语言版本 **/
	public static final String LANGUAGE_DEFAULT = "chi";
	
	/**
	 * 加入堆栈
	 */
	public static final boolean STATUS_JOIN_STACK = true;

	/**
	 * 不加入堆�?
	 */
	public static final boolean NO_STATUS_JOIN_STACK = false;

	public static final String LANG_CHI = "chi";
	public static final String LANG_ENG = "eng";
	
	/** 设备类型 Android */
    public static final int DEVICE_TYPE_ANDROID = 1;
    /** 操作状�? 添加注册 */
    public static final int PROCESS_STATUS_REGISTER = 1;
    /** 操作状�? 取消注册 */
    public static final int PROCESS_STATUS_UNREGISTERED = 2;
    

    //dbConfig数据�?
    public static String SYS_VERSION = "sys_version";
    public static String ANDROID_ID = "android_id";
    public static String IGNORE_VERSION_CODE = "ignore_version_code";
    public static String IS_FIRST = "isFirst";
    public static String WIDTH = "width";
    public static String HEIGHT = "height";
    public static String DENSITY = "density";
    public static String NETWORK_MODE = "network_mode";

    
    public static final String HTTP_PARAM_NAME = "req";
    public static final String HTTP_FUNCTION_PARAM_NAME = "function";
    
    /** HTTP 请求 成功 */
    public static final int HTTP_SUCCESS = 0;
    
    /** HTTP 请求 失败 */
    public static final int FAILD = 1;
    
    /** HTTP 请求 超时 */
    public static final int FAILD_SESSION_TIMEOUT = 2;
    
    /** Http请求连接成功状态码 **/
    public static final int HTTP_CONNECTION_OK = 1;
    /** Http请求连接失败状态码 **/
    public static final int HTTP_CONNECTION_ERROR = 0;
    /** 没有网络状态码 **/
    public static final int HTTP_NETWORK_NOT_EXISTS = -1;
    /** 服务端请求失败 **/
    public static final int HTTP_CONNECTION_SERVER_FAILD = 4;
    
	public static final int GET = 0;
	public static final int HEAD = 1;
	public static final int PUT = 2;
	public static final int DELETE = 3;
	public static final int POST = 4;
	public static final int OPTIONS = 5;
	public static final int FILE_UPLOAD = 6;
	
	public static final String BUSINESS_BASE_URL = "";
	public static final String SERVER_BASE_URL = "";
	
	public static final int CONNECTION_TIMEOUT = 1000 * 10;
	public static final int CONNECTION_SO_TIMEOUT = 1000 * 60;
	/** 检查版本 **/
	public static final int HTTP_TAG_CHECK_VERSION = 5;
	/** 更新资料  **/
	public static final int HTTP_TAG_UPDATE = 2;
	
	public static final String SUCCESS = "200";



}
