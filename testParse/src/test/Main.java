package test;

import org.apache.axiom.om.OMElement;

public class Main {

	public static void main(String[] args) {
		try {
			// ѧϰofbiz ��ν���ofbiz�����Ľ�������
			OMElement serviceElement = null;
			Object deserialize = SoapSerializer.deserialize(serviceElement.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
