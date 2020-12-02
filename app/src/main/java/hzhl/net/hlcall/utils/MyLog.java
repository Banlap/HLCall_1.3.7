package hzhl.net.hlcall.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * @Title: MyLog
 * @Package com.szjn.ah.ibusinesspad.frame.tools
 * @Description: 日志记录，使用例子： 1.本地文件记录 try{...} catch(Exception e) {
 *               MyLog.log(this.getClass(), e, e.getMessage()); } 2.logout窗口记录
 *               try{...} catch(Exception e) { MyLog.d("错误输出"); }
 * @author : dana
 * @date 2012-11-26 下午1:23:23
 * @version V1.0
 */
public class MyLog {
	private static final String TAG = "TAG";
	private static final String FILE_EROOR = TAG + "_log.txt";
	private static final String FILE_PATH = "/sdcard/";
	private static boolean debug = true;
	private static boolean log = true;

	public static void d(String tag, String msg) {
		if (debug) {
			Log.d(tag, createMessage(msg));
		}
	}

	public static void e(String tag, String msg) {
		if (debug) {
			Log.e(tag, createMessage(msg));
		}
	}

	public static void w(String tag, String msg) {
		if (debug) {
			Log.w(tag, createMessage(msg));
		}
	}

	public static void i(String tag, String msg) {
		if (debug) {
			Log.i(tag, createMessage(msg));
		}
	}

	public static void d(String msg) {
		if (debug) {
			Log.d(TAG, createMessage(msg));
		}
	}

	public static void e(String msg) {
		if (debug) {
			Log.e(TAG, createMessage(msg));
		}
	}

	public static void w(String msg) {
		if (debug) {
			Log.w(TAG, createMessage(msg));
		}
	}

	public static void i(String msg) {
		if (debug) {
			Log.i(TAG, createMessage(msg));
		}
	}

	/**
	 * 获取有类名与方法名的logString
	 * @param rawMessage
	 * @return
	 */
	private static String createMessage(String rawMessage) {
		/**
		 * Throwable().getStackTrace()获取的是程序运行的堆栈信息，也就是程序运行到此处的流程，以及所有方法的信息
		 * 这里我们为什么取2呢？0是代表createMessage方法信息，1是代表上一层方法的信息，这里我们
		 * 取它是上两层方法的信息
		 */
		StackTraceElement stackTraceElement = new Throwable().getStackTrace()[2];
		String fullClassName = stackTraceElement.getClassName();
		String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
		return className + "." + stackTraceElement.getMethodName() + "(): " + rawMessage;
	}

	/**
	 * 
	 * @Title: log
	 * @Description: TODO
	 * @param errorClass  打印错误当前类名
	 * @param exception
	 * @param error    
	 * @return void
	 */
	public static void log(Class<?> errorClass, Exception exception,
                           String error) {
		if (debug) {
			d(error);
		}
		if (log) {
			StringBuilder type = new StringBuilder();
			if (errorClass != null)
				type.append(errorClass.getName()).append("\n");
			if (exception != null)
				type.append(exception.getClass().getName());
			log(type.toString(), error);
		}
	}

	/**
	 * 
	 * @Title: log
	 * @Description: TODO
	 * @param errorClass 打印错误当前类名
	 * @param throwable
	 * @param error    
	 * @return void
	 */
	public static void log(Class<?> errorClass, Throwable throwable,
                           String error) {
		if (debug) {
			d(error);
		}
		if (log) {
			StringBuilder type = new StringBuilder();
			if (errorClass != null)
				type.append(errorClass.getName()).append("\n");
			if (throwable != null)
				type.append(throwable.getClass().getName());
			log(type.toString(), error);
		}
	}

	@SuppressWarnings("unused")
	private static void log(String error) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			try {
				File f = new File(FILE_PATH + FILE_EROOR);
				if (!f.exists()) {
					f.createNewFile();
				}
				FileOutputStream fos = new FileOutputStream(f, true);
				StringBuilder sb = new StringBuilder();
				SimpleDateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
				SimpleDateFormat hms = new SimpleDateFormat("hh:mm:ss");
				Date time = new Date();
				sb.append("DATE：").append(ymd.format(time)).append("\n")
						.append("TIME：").append(hms.format(time))
						.append("\n\n").append("ERROR:").append("\n")
						.append(error).append("\n\n")
						.append("******************************")
						.append("\n\n");
				fos.write(sb.toString().getBytes());
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @Title: log
	 * @Description: 打印日志
	 * @param type
	 * @param error
	 * @return void
	 */
	private static void log(String type, String error) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			try {
				File f = new File(FILE_PATH + FILE_EROOR);
				if (!f.exists()) {
					f.createNewFile();
				}
				FileOutputStream fos = new FileOutputStream(f, true);
				StringBuilder sb = new StringBuilder();
				SimpleDateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
				SimpleDateFormat hms = new SimpleDateFormat("hh:mm:ss");
				Date time = new Date();
				sb.append("DATE：").append(ymd.format(time)).append("\n")
						.append("TIME：").append(hms.format(time)).append("\n")
						.append("TYPE:").append("\n").append(type).append("\n")
						.append("ERROR:").append("\n").append(error)
						.append("\n\n")
						.append("******************************")
						.append("\n\n");
				fos.write(sb.toString().getBytes());
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
