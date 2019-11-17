import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;

import json.Message;

/**
 * 分别实现对应不同消息的不同操作
 * @author dq
 *
 */
public class Action {
	/**
	 * 实现对应prepare的操作
	 * @param message
	 */
	public static void onPrepare(Message message) {
		//判断是否在可达节点表中,若在，则无操作，若不在，则申请一条直达管道
		//可以将所有需要建立管道的操作放在prepare中，在此未实现
		if(RouteServer.getChannelNum()<=(Main.config.maxChannelConn-1))//判断通道的建立是否已经满了,若通道已满，则不申请通道
		{
			if(RouteServer.adjacentNodeTable.get(message.sysMessage.target)==null) {//判断管道在之前是否被创建过
				if(RouteServer.accessibleNodeTable.get(message.sysMessage.target)==null) {
					applyChannel(message.sysMessage.target);
					MyChannel channel=new MyChannel(message.sysMessage.target);
					channel.setChannelState(Const.CHANNEL_STATE_REQUEST);
					//channel.isActive=true;
					RouteServer.adjacentNodeTable.put(message.sysMessage.target, channel);
					RouteServer.addChannelNum();
					System.out.println("现在已连接节点数为："+RouteServer.getChannelNum());
					return;
				}else if(RouteServer.accessibleNodeTable.get(message.sysMessage.target).delay > Const.ACCESSIBLE_NODE_DELAY_MIN){
					applyChannel(message.sysMessage.target);
					MyChannel channel=new MyChannel(message.sysMessage.target);
					channel.setChannelState(Const.CHANNEL_STATE_REQUEST);
					//channel.isActive=true;
					RouteServer.adjacentNodeTable.put(message.sysMessage.target, channel);
					RouteServer.addChannelNum();
					System.out.println("现在已连接节点数为："+RouteServer.getChannelNum());
					return;
				}
			}
		}else {
			if(RouteServer.adjacentNodeTable.get(message.sysMessage.target)==null) {
				MyChannel channel=new MyChannel(message.sysMessage.target);
				channel.setChannelState(Const.CHANNEL_STATE_REFUSE);
				RouteServer.adjacentNodeTable.put(message.sysMessage.target, channel);
				System.out.println("新建了一个拒绝管道，循环请求建立通道");
			}
		}
	}
	/**
	 * 实现对应send的操作
	 * @param message
	 */
	public static void onSend(Message message) {
		MyChannel channel=RouteServer.adjacentNodeTable.get(message.sysMessage.target);
		if(channel!=null) {//判断是否建立过管道
			if(channel.getChannelState()==Const.CHANNEL_STATE_SUCCESS) {//若管道连通
				channel.doSend(message);
			}else if (channel.getChannelState()==Const.CHANNEL_STATE_REQUEST||channel.getChannelState()==Const.CHANNEL_STATE_WAITSUCC) {//管道还在建立,等待管道建立成功
				System.out.println("消息等待"+channel.targetID+"通道建立中");
				AccessibleNode node;
				//判断节点是否在可达节点表中
				if((node=RouteServer.accessibleNodeTable.get(message.sysMessage.target))!=null) {//判断是否在可达节点表中，若在，则发送消息
					RouteServer.adjacentNodeTable.get(node.throughNode).doSend(message);
				}
				try {
					do {
						Thread.sleep(100);
					}while(channel.getChannelState()==Const.CHANNEL_STATE_REQUEST||channel.getChannelState()==Const.CHANNEL_STATE_WAITSUCC) ;
						
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				onSend(message);
			}else if (channel.getChannelState()==Const.CHANNEL_STATE_REFUSE) {//管道拒绝建立
				System.out.println("通道"+channel.targetID+"拒绝建立");
				AccessibleNode node;
				//判断节点是否在可达节点表中
				if((node=RouteServer.accessibleNodeTable.get(message.sysMessage.target))!=null) {//判断是否在可达节点表中，若在，则发送消息
					RouteServer.adjacentNodeTable.get(node.throughNode).doSend(message);
				}
					//洪泛本次消息
					Map<Integer, MyChannel> channelTable=RouteServer.adjacentNodeTable;
					Set<Integer> set=channelTable.keySet();
					for(Integer i:set) {
						if(channelTable.get(i).getChannelState()==Const.CHANNEL_STATE_SUCCESS) {
							channelTable.get(i).doSend(message);
						}
					}
					//循环等待通道建立的代码实现
					double requestTimeout=Main.curTime()+Const.WAIT_CHANNEL_BUILD_TIMEOUT;
					boolean isBuild=false;
					try {
						if(RouteServer.getChannelNum()<(Main.config.maxChannelConn-1)) {
							applyChannel(message.sysMessage.target);
							channel.firstRufuse=false;
							channel.setChannelState(Const.CHANNEL_STATE_REQUEST);
							RouteServer.addChannelNum();
							System.out.println("现在已连接节点数为："+RouteServer.getChannelNum());
						}
						//判断是否达到循环等待时间
						while (Main.curTime()<requestTimeout) {
							//线程每次休息的时间需要考虑，因为需要等待消息的回复
							Thread.sleep(Const.THREAD_SLEEP_TIME_INWAITCHANNEL);
							//判断本机管道是否连接数是否使用完毕
							if(RouteServer.getChannelNum()<(Main.config.maxChannelConn-1)) {
								//channel.isActive=true;
									switch (channel.getChannelState()) {//根据管道的状态分别执行不同的操作
									case Const.CHANNEL_STATE_REQUEST:
										//如果是REQUEST状态则直接跳过
										break;
									case Const.CHANNEL_STATE_REFUSE:
										//如果是通道被拒接了，那么我们尝试再申请一次
										applyChannel(message.sysMessage.target);
										channel.firstRufuse=false;
										channel.setChannelState(Const.CHANNEL_STATE_REQUEST);
										RouteServer.addChannelNum();
										System.out.println("现在已连接节点数为："+RouteServer.getChannelNum());
										break;
									case Const.CHANNEL_STATE_SUCCESS:
										//如果通道建立成功了，我们发送消息
										channel.doSend(message);
										isBuild=true;
										requestTimeout=0;
										break;
									default:
										
										break;
									}
							}else {//若管道使用完毕
								channelTable=RouteServer.adjacentNodeTable;
								set=channelTable.keySet();
								for(Integer i:set) {
									if(channelTable.get(i).getChannelState()==Const.CHANNEL_STATE_SUCCESS) {
										
										if((channelTable.get(i).getTimeout()-Const.CHANNEL_TIME_BEKILLED)<Main.curTime()) {
											channelTable.get(i).setTimeout(-1);
											channelTable.get(i).setChannelState(Const.CHANNEL_STATE_BEKILLED);
										}
									}
								}
							}
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					//如果未建立成功，将无用管道从表中移除
					if(!isBuild)RouteServer.adjacentNodeTable.remove(message.sysMessage.target);
				}
		}else {//当管道为空时，代表该条消息为路由来的消息或者洪泛消息
			//查询该条消息是否在可达节点表中
			AccessibleNode node;
			if((node=RouteServer.accessibleNodeTable.get(message.sysMessage.target))!=null) {//判断是否在可达节点表中，若在，则发送消息
				RouteServer.adjacentNodeTable.get(node.throughNode).doSend(message);
			}else {
				//洪泛本次消息
				Map<Integer, MyChannel> channelTable=RouteServer.adjacentNodeTable;
				Set<Integer> set=channelTable.keySet();
				for(Integer i:set) {
					if(channelTable.get(i).getChannelState()==Const.CHANNEL_STATE_SUCCESS) {
						channelTable.get(i).doSend(message);
					}
				}
			}
		}
	}
	/**
	 * 实现通道创建成功后的操作
	 * @param message
	 */
	public static void onSucc(Message message) {//管道创建成功后应该广播通道更改消息
		//判断连接节点表中是否存在节点
		if(RouteServer.adjacentNodeTable.get(message.sysMessage.target)==null) {
			MyChannel channel=new MyChannel(message.sysMessage.target);
			channel.channelID=message.channelId;
			channel.channelType=message.channelType;
			//channel.isActive=false;
			channel.setChannelState(Const.CHANNEL_STATE_SUCCESS);
			RouteServer.adjacentNodeTable.put(message.sysMessage.target, channel);
		}else {
			MyChannel channel=RouteServer.adjacentNodeTable.get(message.sysMessage.target);
			channel.channelID=message.channelId;
			channel.channelType=message.channelType;
			channel.setChannelState(Const.CHANNEL_STATE_SUCCESS);
		}
		//将节点添加进可达节点表
		AccessibleNode node=new AccessibleNode();
		node.target=message.sysMessage.target;
		node.throughNode=-1;
		//判断通道类型
		if(message.channelType==Const.CHANNEL_TYPE_FAST) {
			node.delay=Main.config.channelConfig.highSpeed.lag;
		}else {
			node.delay=Main.config.channelConfig.normalSpeed.lag;
		}
		RouteServer.accessibleNodeTable.put(node.target, node);

		sendChangeAccessibleTable();
	}
	/**
	 * 相应的管道申请操作
	 * @param message
	 */
	synchronized public static void onBuildRequest(Message message) {//管道申请响应应该考虑如何关闭自身的管道
		
		MyChannel channel;
		if((channel=RouteServer.adjacentNodeTable.get(message.sysMessage.target))==null) {//判断该条通道是否在创建中或者创建成功
			if(RouteServer.getChannelNum()>=Main.config.maxChannelConn) {
				Map<Integer, MyChannel>channelTable=RouteServer.adjacentNodeTable;
				Set<Integer>set=channelTable.keySet();
				for(Integer i:set) {
					if(channelTable.get(i).getChannelState()==Const.CHANNEL_STATE_SUCCESS) {
						
						if((channelTable.get(i).getTimeout()-Const.CHANNEL_TIME_BEKILLED)<Main.curTime()) {
							channelTable.get(i).setTimeout(-1);
							channelTable.get(i).setChannelState(Const.CHANNEL_STATE_BEKILLED);
						}
					}
				}
				RouteServer.sendChannelBuild(message.sysMessage.target, Const.STATE_REFUSE,
	                    Const.ERR_CODE_CHANNEL_BUILD_TARGET_REFUSE, message.channelType);
				return ;
			}
			
			System.out.println("新建一个管道，并发送建立成功");
			RouteServer.sendChannelBuild(message.sysMessage.target, Const.STATE_ACCEPT, Const.ERR_CODE_NONE, message.channelType);
			channel=new MyChannel(message.sysMessage.target);
			channel.channelID=message.channelId;
			channel.channelType=message.channelType;
			//channel.isActive=false;
			channel.setChannelState(Const.CHANNEL_STATE_WAITSUCC);//代表管道处于等待成功状态
			RouteServer.adjacentNodeTable.put(message.sysMessage.target, channel);
			RouteServer.addChannelNum();
			System.out.println("现在已连接节点数为："+RouteServer.getChannelNum());
		}else {
			if(channel.getChannelState()==Const.CHANNEL_STATE_WAITSUCC) {
				RouteServer.sendChannelBuild(message.sysMessage.target, Const.STATE_REFUSE,
                        Const.ERR_CODE_CHANNEL_BUILD_TARGET_REFUSE, message.channelType);
			}else if(message.sysMessage.target>Main.channel.getId()) {
				RouteServer.sendChannelBuild(message.sysMessage.target, Const.STATE_REFUSE,
                        Const.ERR_CODE_CHANNEL_BUILD_TARGET_REFUSE, message.channelType);
			}else {
				channel.setChannelState(Const.CHANNEL_STATE_WAITSUCC);
				RouteServer.sendChannelBuild(message.sysMessage.target, Const.STATE_ACCEPT, Const.ERR_CODE_NONE, message.channelType);
			}
		}
	}
	/**
	 * 实现相应的拒绝操作
	 * @param message
	 */
	public static void onRefuse(Message message) {
		//判断是否创建了临时管道，若存在，则将管道状态置为拒绝
		//当管道真正被设置为refuse时，才将通道数减一，即在mychannel中
		MyChannel channel=RouteServer.adjacentNodeTable.get(message.sysMessage.target);
		if(channel!=null&&channel.getChannelState()!=Const.CHANNEL_STATE_SUCCESS) {
			channel.setChannelState(Const.CHANNEL_STATE_REFUSE);
		}
	}
	/**
	 * 响应可达节点表的更改操作
	 * @param message
	 */
	public static void onSys(Message message) {
		boolean isChange=false;
		JSONObject object=JSONObject.parseObject(message.extMessage.changeInformation);
		Map<Integer, AccessibleNode> table=RouteServer.accessibleNodeTable;
		Set<Integer> setC=table.keySet();
		for(Integer t:setC) {
			AccessibleNode node=table.get(t);
			JSONObject infor;
			if((infor=JSONObject.parseObject((String) object.get(String.valueOf(node.target))))==null) {//不再更新信息中，即已经断开
				table.remove(t);
				isChange=true;
			}else if(infor.getIntValue("throughnode")==node.throughNode){
				node.delay=infor.getDoubleValue("delay");
				isChange=true;
			}else if(infor.getDoubleValue("delay")<node.delay){
				node.delay=infor.getDoubleValue("delay");
				isChange=true;
			}
		}
		if(isChange) {
			sendChangeAccessibleTable();
		}
		
	}
	/**
	 * 实现发送路由表更新消息
	 */
	public static void sendChangeAccessibleTable() {
		//创建消息
		Message messageNew=Const.GetEmptyMessage();
		messageNew.callType=Const.CALL_TYPE_SYS;
		messageNew.errCode=Const.ERR_CODE_NONE;
		//获取连接的通道
		Map<Integer, MyChannel> channelTable=RouteServer.adjacentNodeTable;
		Object[] set=RouteServer.adjacentNodeTable.keySet().toArray();
		for(int i=0;i<set.length;i++) {
			//判断通道是否处于等待创建
			if(channelTable.get(set[i]).getChannelState()==Const.CHANNEL_STATE_SUCCESS) {
				double channelDelay;
				if(channelTable.get(set[i]).channelType==Const.CHANNEL_TYPE_NORMAL) {
					channelDelay=Main.config.channelConfig.normalSpeed.lag;
				}else {
					channelDelay=Main.config.channelConfig.highSpeed.lag;
				}
				JSONObject object=new JSONObject();
				//Map<Integer, AccessibleNode> table=RouteServer.getAccessibleNodeTable();
				Object[] setC=RouteServer.accessibleNodeTable.keySet().toArray();
				for(int t=0;t<setC.length;t++) {
					AccessibleNode node=RouteServer.accessibleNodeTable.get(setC[t]);
					//判断是否是通向发送方的节点，若是，则不加入消息
					if(node.throughNode!=channelTable.get(set[i]).targetID) {
						//判断到节点的延迟是否超过最长延迟
						if((node.delay+channelDelay)>Const.ACCESSIBLE_NODE_DELAY_MAX) {
							JSONObject objectNode=new JSONObject();
							//objectNode.put("target", node.target);
							objectNode.put("delay", node.delay+channelDelay);
							objectNode.put("throughnode", RouteServer.getId());
							object.put(String.valueOf(node.target), objectNode);
						}
					}
				}
				if(!object.isEmpty()) {
					messageNew.extMessage.changeInformation=object.toJSONString();
					channelTable.get(set[i]).doSend(messageNew);
				}
			}
		}
	}
	/**
	 * 实现通道销毁的操作
	 * @param message
	 */
	public static void onDestroy(Message message) {
		//Map<Integer, MyChannel> channelTable=RouteServer.getAdjacentNodeTable();
		Object[] setC=RouteServer.adjacentNodeTable.keySet().toArray();
		for(int i=0;i<setC.length;i++) {
			if(RouteServer.adjacentNodeTable.get(setC[i]).channelID==message.channelId) {
				RouteServer.adjacentNodeTable.get(setC[i]).setTimeout(-1);
				
				RouteServer.accessibleNodeTable.remove(setC[i]);
				RouteServer.adjacentNodeTable.remove(setC[i]);
			}
		}
		RouteServer.deleteChannelNum();
		System.out.println("现在已连接节点数为："+RouteServer.getChannelNum());
		sendChangeAccessibleTable();
	}
	/**
	 * 实现申请通道，同时申请高速和低速通道
	 * @param target
	 */
	public static void applyChannel(int target) {
		RouteServer.sendChannelBuild(target, Const.STATE_REQUEST, Const.ERR_CODE_NONE, Const.CHANNEL_TYPE_FAST);
		RouteServer.sendChannelBuild(target, Const.STATE_REQUEST, Const.ERR_CODE_NONE, Const.CHANNEL_TYPE_NORMAL);
	}
}
