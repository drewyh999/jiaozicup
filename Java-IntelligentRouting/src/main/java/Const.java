import com.fasterxml.jackson.databind.ObjectMapper;
import json.Message;

/**
 * 保存相关常量信息
 * @author dq
 *
 */
public class Const {
    public final static String CALL_TYPE_PREPARE = "prepare";
    public final static String CALL_TYPE_SEND = "send";
    public final static String CALL_TYPE_SYS = "sys";
    public final static String CALL_TYPE_CHANNEL_BUILD = "channel_build";
    public final static String CALL_TYPE_CHANNEL_DESTROY = "channel_destroy";

    public final static int STATE_REQUEST = 0;
    public final static int STATE_ACCEPT = 1;
    public final static int STATE_REFUSE = 2;
    public final static int STATE_NOTICE = -1;

    public final static int CHANNEL_TYPE_ERROR = -1;
    public final static int CHANNEL_TYPE_NORMAL = 0;
    public final static int CHANNEL_TYPE_FAST = 1;

    public final static int ERR_CODE_NONE = 0x0;
    public final static int ERR_CODE_NO_SUCH_CHANNEL = 0x001;
    public final static int ERR_CODE_NO_SUCH_CALL_TYPE = 0x002;
    public final static int ERR_CODE_CHANNEL_BUILD_MASK = 0x0100;
    public final static int ERR_CODE_CHANNEL_BUILD_TARGET_REFUSE = 0x0101;
    public final static int ERR_CODE_CHANNEL_BUILD_TARGET_LIMIT = 0x0102;
    public final static int ERR_CODE_CHANNEL_BUILD_TOTAL_LIMIT = 0x0103;
    public final static int ERR_CODE_CHANNEL_BUILD_SOURCE_LIMIT = 0x0104;
    public final static int ERR_CODE_CHANNEL_BUILD_TARGET_TIMEOUT = 0x0105;
    public final static int ERR_CODE_CHANNEL_BUILD_UNKNOWN_OPERATION = 0x106;
    public final static int ERR_CODE_SEND_MASK = 0x200;
    public final static int ERR_CODE_SEND_COUNT_LIMIT = 0x201;
    public final static int ERR_CODE_SEND_SIZE_LIMIT = 0x202;

    public final static double EXP = 1e-5;

    public final static ObjectMapper MAPPER = new ObjectMapper();
    public final static String EMPTY_MESSAGE = "{\"callType\": \"\",\"channelId\": 0,\"sysMessage\": {\"target\": 0,\"data\": \"\",\"delay\": 0.0},\"extMessage\": {\"residueTime\":0,\"changeInformation\":\"[]\"},\"state\": 0,\"errCode\": 0,\"channelType\": 0}";
    public static Message GetEmptyMessage() {
        try {
            return MAPPER.readValue(EMPTY_MESSAGE, Message.class);
        } catch (Exception e) {
            return null;
        }
    }
    //管道状态
    public final static int CHANNEL_STATE_NONE = 0;
    public final static int CHANNEL_STATE_REQUEST = 1;
    public final static int CHANNEL_STATE_REFUSE = 2;
    public final static int CHANNEL_STATE_SUCCESS = 3;
    public final static int CHANNEL_STATE_WAITSUCC = 4;
    public final static int CHANNEL_STATE_BEKILLED = 5;
    //可达节点表改变的类型
    public final static int ACCESSIBLE_TABLE_ADD=0;
    public final static int ACCESSIBLE_TABLE_DESTROY=1;
    
    
    //管道存在时间
    // 单位秒
    public final static double CHANNEL_TIMEOUT = Main.config.mainConfig.timeOut*3;
    //管道被杀死的条件
    //在timeout过去七分之一后，就满足通道被杀死的条件 单位秒
    public final static double CHANNEL_TIME_BEKILLED=CHANNEL_TIMEOUT/7*6;
    //节点保存洪泛消息的个数
    public final static int FLOODING_LENGTH = 10;
    //可达节点表所能保存可达节点的最长时延,最大阈值 单位秒
    public final static double ACCESSIBLE_NODE_DELAY_MAX=(Main.config.channelConfig.normalSpeed.buildTime+Main.config.channelConfig.highSpeed.buildTime)*3;
    //可达节点的最小阈值 单位秒
    public final static double ACCESSIBLE_NODE_DELAY_MIN=Main.config.channelConfig.normalSpeed.buildTime+Main.config.channelConfig.highSpeed.buildTime;
    //循环请求建立通道，并检查条件是否允许，杀死自身的通道，最长等待时间 单位秒
    public final static double WAIT_CHANNEL_BUILD_TIMEOUT=Main.config.mainConfig.timeOut;
    //public final static double WAIT_CHANNEL_BUILD_TIMEOUT=1.5;
    //在线程循环等待通道建立中，线程每次休息的时间需要考虑，因为需要等待消息的回复，单位毫秒
    public final static long THREAD_SLEEP_TIME_INWAITCHANNEL=200 ;

    //在接受通道建立的节点是否应该循环等待通道的销毁，
}