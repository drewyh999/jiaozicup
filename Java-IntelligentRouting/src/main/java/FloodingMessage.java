import json.Message;

/**
 * 保存洪泛消息，并保证更新,但缓存大小有限，消息可能会被覆盖
 * @author dq
 *
 */
public class FloodingMessage {
	private String[]messages;
	private int count;
	
	public FloodingMessage() {
		messages=new String[Const.FLOODING_LENGTH];
		for(int i=0;i<Const.FLOODING_LENGTH;i++) {
			messages[i]=new String();
		}
		count=0;
	}
	
	public void addMessage(Message message) {
		messages[count]=message.sysMessage.target+message.sysMessage.data;
		count=(count+1)%Const.FLOODING_LENGTH;
	}
	
	public boolean isExist(Message message) {
		for(int i=0;i<Const.FLOODING_LENGTH;i++) {
			if(messages[i].equals(message.sysMessage.target+message.sysMessage.data)) {
				return true;
			}
		}
		return false;
	}
}
