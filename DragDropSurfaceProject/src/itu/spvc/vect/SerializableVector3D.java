package itu.spvc.vect;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.mt4j.util.math.Vector3D;

public class SerializableVector3D implements Serializable {

	private static final long serialVersionUID = 7097779705383108346L;

	float x, y, z, w;
	
	public SerializableVector3D(final Vector3D vect3D) {
		this.x = vect3D.getX();
		this.y = vect3D.getY();
		this.z = vect3D.getX();
		this.w = vect3D.getW();
	}
	
	public static ArrayList<Vector3D> fromSerializableArray(final List<SerializableVector3D> list) {
		ArrayList<Vector3D> returnValue = new ArrayList<Vector3D>();
		for(SerializableVector3D sv3D: list) {
			returnValue.add(sv3D.toVector3D());
		}
		
		return returnValue;
	}

	public Vector3D toVector3D() {
		return new Vector3D(x, y, z, w);
	}
	
	@Override
	public String toString() {
		return " X:" + this.getX() + " Y:" + this.getY() + " Z:" + this.getZ() + " W:" + this.getW();
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getZ() {
		return z;
	}

	public void setZ(float z) {
		this.z = z;
	}

	public float getW() {
		return w;
	}

	public void setW(float w) {
		this.w = w;
	}
}
