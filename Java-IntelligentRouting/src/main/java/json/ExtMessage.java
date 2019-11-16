package json;

public class ExtMessage implements Cloneable{
	public double residueTime;
	public String changeInformation;
    @Override
    public ExtMessage clone() throws CloneNotSupportedException {
        return (ExtMessage) super.clone();
    }
}
