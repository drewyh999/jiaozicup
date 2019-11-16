import java.util.ArrayList;

import json.Message;

/**
 * 储存管道信息
 * @author dq
 *
 */
public class MyChannel {
	//public boolean isActive;//判断通道的建立是主动建立还是被动建立，以解决两个节点同时发起通道申请的情况
	public int channelType;//记录管道类型
	public int channelState;//记录管道现在状态
	private ArrayList<Message> messages;//消息缓存
	private double timeout;//储存通道的timeout时间
	public int channelID;
	public int targetID;
	public boolean firstRufuse=false;//判断是否有一条通道被拒绝
	
	public MyChannel(int targetID) {
		this.channelState=Const.CHANNEL_STATE_NONE;
		this.targetID=targetID;
		timeout=Main.curTime();
	}
	
	/**
	 * 往消息缓存中添加待处理消息
	 * @param messages
	 */
	public void addMessages(Message messages) {
		this.messages.add(messages);
	}
	
	synchronized public int getChannelState() {
		return channelState;
	}
	/**
	 * 执行通道状态改变后的操作
	 * @param channelState
	 */
	synchronized public void setChannelState(int channelState) {
		if(channelState==Const.CHANNEL_STATE_SUCCESS) {//判断通道建立成功并设置通道timeout
			this.channelState = channelState;
			//设置管道的销毁时间
			timeout=Main.curTime()+Const.CHANNEL_TIMEOUT;
			destroy();
		}else if(channelState==Const.CHANNEL_STATE_REFUSE) {//判断通道建立失败
			if(firstRufuse) {
				this.channelState = channelState;
				RouteServer.deleteChannelNum();
				System.out.println("现在已连接节点数为："+RouteServer.getChannelNum());
			}else {//第一次被拒绝建立通道
				firstRufuse=true;
			}
		}else {
			this.channelState = channelState;
		}
	}

	public void setTimeout(double timeout) {
		this.timeout=timeout;
	}
	
	public double getTimeout() {
		return timeout;
	}
	
	public int getChannelType() {
		return channelType;
	}
	/**
	 * 执行线程的销毁任务
	 */
	private void destroy() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					while (Main.curTime()<timeout) {
							Thread.sleep(20);
					}
					System.out.println(timeout);
					Message message=Const.GetEmptyMessage();
					message.callType=Const.CALL_TYPE_CHANNEL_DESTROY;
					message.channelId=channelID;
					Main.channel.send(message, 0);
					RouteServer.accessibleNodeTable.remove(targetID);
					RouteServer.adjacentNodeTable.remove(targetID);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}).start();
		
	}
	
	/**
	 * 发送消息
	 * @param message
	 * @param target
	 */
    public void doSend(Message message) {
        try {
        	message.channelId=channelID;
        	message.channelType=channelType;
        	//更新管道的销毁时间
    		timeout=Main.curTime()+Const.CHANNEL_TIMEOUT;
            Main.channel.send(message, targetID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
