package com.ztf.send;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;

import test.Main;
 
public class Parse {
 
   private static OMFactory fac;
   private static OMNamespace omNs;
 
   static {
      fac = OMAbstractFactory.getOMFactory();
      omNs = fac.createOMNamespace("http://ofbiz.apache.org/service/", "ns1");
   }
 
   public static void main(String[] args) throws AxisFault {
 
      ServiceClient sc = new ServiceClient();
      Options opts = new Options();
      opts.setTo(new EndpointReference(
         "http://localhost:8080//webtools/control/SOAPService"));
      opts.setAction("findProductById");
      sc.setOptions(opts);
      OMElement res = sc.sendReceive(createPayLoad());
      System.out.println(res);
      
      Main.test(res);
   }
 
   private static OMElement createPayLoad() {
 
      OMElement findPartiesById = fac.createOMElement("findProductById", omNs);
      OMElement mapMap = fac.createOMElement("map-Map", omNs);
 
      findPartiesById.addChild(mapMap);
 
      mapMap.addChild(createMapEntry("idToFind", "admin"));
      mapMap.addChild(createMapEntry("login.username", "admin"));
      mapMap.addChild(createMapEntry("login.password", "ofbiz"));
 
      return findPartiesById;
   }
 
   private static OMElement createMapEntry(String key, String val) {
 
      OMElement mapEntry = fac.createOMElement("map-Entry", omNs);
 
      // create the key
      OMElement mapKey = fac.createOMElement("map-Key", omNs);
      OMElement keyElement = fac.createOMElement("std-String", omNs);
      OMAttribute keyAttribute = fac.createOMAttribute("value", null, key);
 
      mapKey.addChild(keyElement);
      keyElement.addAttribute(keyAttribute);
 
      // create the value
      OMElement mapValue = fac.createOMElement("map-Value", omNs);
      OMElement valElement = fac.createOMElement("std-String", omNs);
      OMAttribute valAttribute = fac.createOMAttribute("value", null, val);
 
      mapValue.addChild(valElement);
      valElement.addAttribute(valAttribute);
 
      // attach to map-Entry
      mapEntry.addChild(mapKey);
      mapEntry.addChild(mapValue);
 
      return mapEntry;
   }
}