package test;

import java.util.HashMap;

import org.apache.axiom.om.OMElement;


public class Main {

	public static void test(OMElement serviceElement) {
		try {
			HashMap<String, Object> deserialize = (HashMap<String, Object>)SoapSerializer.deserialize(serviceElement.toString());
			System.out.println(deserialize.get("errorMessage"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
