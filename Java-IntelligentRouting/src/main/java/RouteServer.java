import java.util.Hashtable;
import java.util.Map;

import json.Message;

public class RouteServer {
	public static Map<Integer, MyChannel> adjacentNodeTable=new Hashtable<>();//储存当前节点连接的相邻节点
	public static Map<Integer, AccessibleNode> accessibleNodeTable=new Hashtable<>();//储存当前节点的可达节点表
	private static FloodingMessage floodingMessage=new FloodingMessage();
	private static int channel_Succ=0;//记录了已经成功连接的通道数和等待申请中的通道数，在mychannel的新建的位置加一，还有就是在申请通道处加一，减一就是在destroy和refuse处减一
	
	synchronized public static void addChannelNum() {
		channel_Succ++;
	}
	synchronized public static void deleteChannelNum() {
		channel_Succ--;
	}
	synchronized public static int getChannelNum() {
		return channel_Succ;
	}
	
	//对表的相关操作
	/*synchronized public static void addAccessibleNode(int target,AccessibleNode node) {
		accessibleNodeTable.put(target, node);
	}
	synchronized public static void deleteAccessibleNode(int target) {
		accessibleNodeTable.remove(target);
	}
	synchronized public static AccessibleNode searchAccessibleNode(int target) {
		return accessibleNodeTable.get(target);
	}
	synchronized public static void addAdjacentNode(int target,MyChannel channel) {
		adjacentNodeTable.put(target, channel);
	}
	synchronized public static void deleteAdjacentNode(int target) {
		adjacentNodeTable.remove(target);
	}
	synchronized public static MyChannel searchAdjacentNode(int target) {
		return adjacentNodeTable.get(target);
	}
	
	synchronized public static Map<Integer, MyChannel> getAdjacentNodeTable() {
		return adjacentNodeTable;
	}
	synchronized public static Map<Integer, AccessibleNode> getAccessibleNodeTable() {
		return accessibleNodeTable;
	}*/
	/**
	 * 相应各种消息的操作
	 * @param message
	 */
	public static void onRecv(Message message) {
        switch (message.callType) {
           	case Const.CALL_TYPE_PREPARE:
           		Action.onPrepare(message);
           		System.out.println("received prepare message");
           		break;
           	case Const.CALL_TYPE_SEND:
           		if(getId()==message.sysMessage.target) {//判断是否为消息终点
           			System.out.println("succ received message: " + message.sysMessage.data);
                    return ;
           		}else {
           			//判断该条消息是否被收到过
                    if(!floodingMessage.isExist(message)) {
                    	//判断消息中是否有加载消息剩余时间，0是默认初始化数字，用于初始化
                    	if(message.extMessage.residueTime==0) {
                    		//放置消息过期时间
                    		message.extMessage.residueTime=Main.curTime()+Main.config.mainConfig.timeOut;
                    	}else if(message.extMessage.residueTime>0) {
                    		//更新消息延迟时间
                    		if(message.channelType==Const.CHANNEL_TYPE_NORMAL) {
                    			message.extMessage.residueTime=message.extMessage.residueTime-Main.config.channelConfig.normalSpeed.lag;
            				}else {
            					message.extMessage.residueTime=message.extMessage.residueTime-Main.config.channelConfig.highSpeed.lag;
            				}
                    	}else {
                    		//超过消息的超时时间
                    		return ;
                    	}
                    	Action.onSend(message);
                    	floodingMessage.addMessage(message);
                    }
           		}
           		System.out.println("收到send消息");
                break;
           	case Const.CALL_TYPE_SYS:
           		Action.onSys(message);
           		System.out.println("收到系统内部消息");
                break;
           	case Const.CALL_TYPE_CHANNEL_BUILD:
               if (message.channelId != 0) {
                   Action.onSucc(message);
                   System.out.println("success build channel:"+message.channelId);
               } else {
                   switch (message.state) {
                       case Const.STATE_NOTICE:
                           Action.onBuildRequest(message);
                           System.out.println("收到一条管道请求建立的消息");
                           break;
                       case Const.STATE_REFUSE:
                           Action.onRefuse(message);
                           System.out.println("收到一条管道拒绝建立的消息");
                           break;
                   }
               }
               break;
           	case Const.CALL_TYPE_CHANNEL_DESTROY:
           		if(message.errCode!=Const.ERR_CODE_NONE)break;
           	    Action.onDestroy(message);
           	    System.out.println("destroy channel:"+message.channelId);
                break;
       }
   }
	
	
	
	
	/**
	 * 发送与建立通道相关的消息
	 * @param target
	 * @param state
	 * @param errCode
	 * @param channelType
	 */
	public static void sendChannelBuild(int target, int state, int errCode, int channelType) {
        Message message = Const.GetEmptyMessage();
        message.callType = Const.CALL_TYPE_CHANNEL_BUILD;
        message.state = state;
        message.sysMessage.target = target;
        message.errCode = errCode;
        message.channelType = channelType;
        try {
			Main.channel.send(message, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	/**
	 * 获取当前节点ID
	 * @return
	 */
	public static int getId() {
        return Main.channel.getId();
    }
}
