import json.Message;

/**
 * 保存洪泛消息，并保证更新,但缓存大小有限，消息可能会被覆盖
 * @author dq
 *
 */
public class FloodingMessage {
	private String[]messages;
	private int count;

	/**
	 * 构造函数，初始化消息队列为全空，并且count初始化为0
	 */
	public FloodingMessage() {
		messages=new String[Const.FLOODING_LENGTH];
		for(int i=0;i<Const.FLOODING_LENGTH;i++) {
			messages[i]=new String();
		}
		count=0;
	}

	/**
	 * 将传入参数message加入到消息队列当中去
	 * @param message
	 */

	public void addMessage(Message message) {
		messages[count]=message.sysMessage.target+message.sysMessage.data;
		count=(count+1)%Const.FLOODING_LENGTH;
	}
	/**
	 * @param message
	 * 通过传入参数，判断消息缓存当中是否存在这个消息
	 */
	public boolean isExist(Message message) {
		for(int i=0;i<Const.FLOODING_LENGTH;i++) {
			if(messages[i].equals(message.sysMessage.target+message.sysMessage.data)) {
				return true;
			}
		}
		return false;
	}
}
