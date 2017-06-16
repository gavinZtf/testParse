package test;

import org.apache.axiom.om.OMElement;

public class Main {

	public static void main(String[] args) {
		try {
			// 学习ofbiz 如何解析ofbiz那样的解析报文
			OMElement serviceElement = null;
			Object deserialize = SoapSerializer.deserialize(serviceElement.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
