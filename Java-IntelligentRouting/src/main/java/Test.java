import java.io.File;
import java.io.IOException;
import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Test {

	public static void main(String[] args) throws JsonParseException {
		/*Date date=new Date();
		System.out.println( date.toString());
		System.out.println( date.getTime() / 1000.0);
		double i=System.currentTimeMillis()/1000.0;
		System.out.println(i);*/
		/*Map<Integer, MyChannel> adjacentNodeTable=new Hashtable<>();
		adjacentNodeTable.put(5, new MyChannel(5));
		adjacentNodeTable.put(4, new MyChannel(4));
		adjacentNodeTable.put(3, new MyChannel(3));
		adjacentNodeTable.put(2, new MyChannel(2));
		adjacentNodeTable.put(1, new MyChannel(1));
		Object[] setC= adjacentNodeTable.keySet().toArray();
		for(int i=0;i<setC.length;i++) {
			System.out.println((int)setC[i]);
			if(adjacentNodeTable.get((int)setC[i]).targetID==2) {
				adjacentNodeTable.remove(i);
			}
		}
		System.out.println(adjacentNodeTable.size());*/
		ObjectMapper objectMapper = new ObjectMapper();
	    JSONArray array;
		try {
			array = objectMapper.readValue(new File("E:\\java-workspace\\Java-IntelligentRouting\\test.json"), JSONArray.class);
			System.out.println(array.size());
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
