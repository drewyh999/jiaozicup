import com.fasterxml.jackson.databind.ObjectMapper;
import conn.Channel;
import conn.GeneralChannel;
import json.Message;
import json.config.Config;
import java.io.File;
import java.util.List;

public class Main {

    public static Config config;
    public static Channel channel;
    /**
     * 获取当前时间
     * @return
     */
    public static double curTime() {
        return System.currentTimeMillis() / 1000.0;
    }

    public static void main(String[] args) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        //config = objectMapper.readValue(new File("/home/config/client.json"), Config.class);
        config = objectMapper.readValue(new File("E:\\java-workspace\\Java-IntelligentRouting\\resources\\client.json"), Config.class);
        Channel channel = new GeneralChannel();
        channel.initConfig(config);
        Main.channel=channel;
        mainloop();
    }

    public static void mainloop(){ 
        while (true) {
            try {
                List<Message> message = channel.recv();
                for (Message msg : message) {
                    new Thread(new Runnable() {
						
						@Override
						public void run() {
							RouteServer.onRecv(msg);
						}
					}).start();
                }
                Thread.sleep(20);
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
    }
}