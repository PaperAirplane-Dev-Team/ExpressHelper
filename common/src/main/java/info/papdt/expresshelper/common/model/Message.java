package info.papdt.expresshelper.common.model;

public class Message<OBJ> {

	private OBJ object;
	private int code = -1;

	public Message() {
	}

	public Message(OBJ obj, int code) {
		this.object = obj;
		this.code = code;
	}

	public OBJ getObject() {
		return this.object;
	}

	public void setObject(OBJ obj) {
		this.object = obj;
	}

	public int getCode() {
		return this.code;
	}

	public void setCode(int code) {
		this.code = code;
	}

}
