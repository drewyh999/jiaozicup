import java.io.IOException;

/**
 * 本类仅用于本地测试重复运行
 */
public class Myclass {

	public static void main(String[] args) {
		Runtime re = Runtime.getRuntime();   
		int max=40;
		try {
			for(int i=0;i<max;i++) {
				re.exec("java -jar E:\\java-workspace\\Java-IntelligentRouting\\target\\marathonSample-1.0-SNAPSHOT-jar-with-dependencies.jar");
				
				//re.exec("java -jar E:\\BaiduNetdiskDownload\\分布式赛道\\javaSample\\javaSample\\target\\marathonSample-1.0-SNAPSHOT-jar-with-dependencies.jar");
			
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
