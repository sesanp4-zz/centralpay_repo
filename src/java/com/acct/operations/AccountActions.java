/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.acct.operations;

import com.google.gson.JsonObject;
import com.util.SSLManager;
import com.util.Utilities;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.UUID;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.CDATASection;

/**
 *
 * @author centricgateway
 */
public class AccountActions {
    
      //  String acctnum="5050007512";       
      //  String acctname="OKOLI CHUKWUMA PAUL";      
      //  String trantype ="1";
      //  String bankcode="070";
    
    /*    
        The test credentials
    
        String billeridd="NIBSS0000000130";
        String billernamee="UBACENTRIC";
        String keyy="FE4B6981AF8CF962B5A4F6AF2E6FD768";
    
     */
    
        String billerid;
        String billername;
        String key; 
        String liveendpoint;
        String requeryendpoint;
    
        Utilities util = new Utilities();
    public AccountActions(){    
        
         billerid= util.getAppProperties().getProperty("billerid");
         billername= util.getAppProperties().getProperty("billername");
         key= util.getAppProperties().getProperty("key");
         liveendpoint = util.getAppProperties().getProperty("liveendpoint");
         requeryendpoint = util.getAppProperties().getProperty("requeryendpoint");
         
         System.out.println("========= info===biller id==="+billerid+"====biller name=="+billername+"====key==="+key+"===liveendpoint==="+liveendpoint+"====requery==="+requeryendpoint+"======");
         
                }
 
        SOAPElement element1;
        JsonObject obj;
    
    
   public String getBankList() throws SOAPException, MalformedURLException, IOException, JSONException{ 
        
     try{
        
        QName  q = new QName("xmlns:web");
        MessageFactory  mf = MessageFactory.newInstance();
        SOAPMessage  m = mf.createMessage();
        m.getSOAPPart().getEnvelope().addAttribute(q, "http://web.nibss.com/");
        SOAPBody  body = m.getSOAPBody();
        
      SOAPElement element=  body.addChildElement("listActiveBanksOTP", "web"); 
      element1=element.addChildElement("arg0").addTextNode(billerid);     
      SOAPConnectionFactory fac =   SOAPConnectionFactory.newInstance();
      SOAPConnection sc =  fac.createConnection();
      new SSLManager().disableSSL();
  
     URL url = new URL(liveendpoint);
     SOAPMessage response =sc.call(m, url);
     m.writeTo(System.out);
     System.out.println("++++++++Response+++++++++++");
     response.writeTo(System.out);
     sc.close();
     
     Iterator iterator1 =  response.getSOAPBody().getChildElements();
          obj = new JsonObject();
          JSONObject jobj=new JSONObject();
          while(iterator1.hasNext()){     
             SOAPBodyElement sb1 =(SOAPBodyElement)iterator1.next();
             Iterator iterator2 = sb1.getChildElements();
               while(iterator2.hasNext()){
                  SOAPBodyElement sb2 =(SOAPBodyElement)iterator2.next();
                  obj.addProperty(sb2.getNodeName(), sb2.getValue());
                   jobj.append(sb2.getNodeName(),XML.toJSONObject(sb2.getValue(),true));
                  
               }

               
          }

         return jobj.toString();
    }catch(Exception e){
       obj = new JsonObject();
       obj.addProperty("code", "S7");
       obj.addProperty("message", "operation failed");
       System.out.println(e.getMessage());
       return obj.toString();
     }
     
    }
    
    
    
  public String createMandate(String acctnum,String acctname,String bankcode) throws SOAPException, MalformedURLException, IOException, JSONException{
    try{      
      String billertransId="CG_"+UUID.randomUUID().toString();
      String payload="<CreateMandateRequest><AcctNumber>"+acctnum+"</AcctNumber><AcctName>"+acctname+"</AcctName><TransType>1</TransType><BankCode>"+bankcode+"</BankCode><BillerID>"+billerid+"</BillerID><BillerName>"+billername+"</BillerName><BillerTransId>"+billertransId+"</BillerTransId></CreateMandateRequest>"+key;
      String payloadhash =  DigestUtils.sha256Hex(payload); 
        
        QName  q = new QName("xmlns:web");
        MessageFactory  mf = MessageFactory.newInstance();
        SOAPMessage  m = mf.createMessage();
        m.getSOAPPart().getEnvelope().addAttribute(q, "http://web.nibss.com/");
        SOAPBody  body = m.getSOAPBody();
   
      SOAPElement element=  body.addChildElement("createMandateRequest", "web");
      CDATASection  cd = m.getSOAPPart().createCDATASection("<CreateMandateRequest><AcctNumber>"+acctnum+"</AcctNumber><AcctName>"+acctname+"</AcctName><TransType>1</TransType><BankCode>"+bankcode+"</BankCode><BillerID>"+billerid+"</BillerID><BillerName>"+billername+"</BillerName><BillerTransId>"+billertransId+"</BillerTransId><HashValue>"+payloadhash+"</HashValue></CreateMandateRequest>");       
      element1=element.addChildElement("arg0");
      element1.appendChild(cd);
      
      System.out.println("create mandate request");
      
      m.writeTo(System.out);
      
      SOAPConnectionFactory fac =   SOAPConnectionFactory.newInstance();
      SOAPConnection sc =  fac.createConnection();
     
     new SSLManager().disableSSL();
     
     URL url = new URL(liveendpoint);
     System.out.println("sending request to endpoint......");
     SOAPMessage response =sc.call(m, url);
     System.out.println("++++++++Response from endpoint+++++++++++");
     response.writeTo(System.out);
     sc.close();
     
     Iterator iterator1 =  response.getSOAPBody().getChildElements();
          obj = new JsonObject();
          JSONObject jobj=new JSONObject();
          while(iterator1.hasNext()){     
             SOAPBodyElement sb1 =(SOAPBodyElement)iterator1.next();
             Iterator iterator2 = sb1.getChildElements();
               while(iterator2.hasNext()){
                  SOAPBodyElement sb2 =(SOAPBodyElement)iterator2.next();
                  obj.addProperty(sb2.getNodeName(), sb2.getValue());
                  jobj.append(sb2.getNodeName(),XML.toJSONObject(sb2.getValue(),true));
                  
               }
      
          }
          
         return jobj.toString();
    } catch(Exception e){
       obj = new JsonObject();
       obj.addProperty("code", "S7");
       obj.addProperty("message", "operation failed");
       System.out.println(e.getMessage());
       return obj.toString();
     }
    
    } 
    
    
  public String validateMandateCreation(String mandatecode,String bankcode, String otp,String amount) throws SOAPException, MalformedURLException, IOException, JSONException{
    try{     
      String billertransId="CG_"+UUID.randomUUID().toString();
      String payload="<ValidateOTPRequest><MandateCode>"+mandatecode+"</MandateCode><TransType>1</TransType><BankCode>"+bankcode+"</BankCode><OTP>"+otp+"</OTP><BillerID>"+billerid+"</BillerID><BillerName>"+billername+"</BillerName><Amount>"+amount+"</Amount><BillerTransId>"+billertransId+"</BillerTransId></ValidateOTPRequest>"+key;
      String payloadhash =  DigestUtils.sha256Hex(payload); 
        
        QName  q = new QName("xmlns:web");
        MessageFactory  mf = MessageFactory.newInstance();
        SOAPMessage  m = mf.createMessage();
        m.getSOAPPart().getEnvelope().addAttribute(q, "http://web.nibss.com/");
        SOAPBody  body = m.getSOAPBody();
        SOAPElement element=  body.addChildElement("validateOTPRequest", "web");
        CDATASection cd = m.getSOAPPart().createCDATASection("<ValidateOTPRequest><MandateCode>"+mandatecode+"</MandateCode><TransType>1</TransType><BankCode>"+bankcode+"</BankCode><OTP>"+otp+"</OTP><BillerID>"+billerid+"</BillerID><BillerName>"+billername+"</BillerName><Amount>"+amount+"</Amount><BillerTransId>"+billertransId+"</BillerTransId><HashValue>"+payloadhash+"</HashValue></ValidateOTPRequest>");       
        element1=element.addChildElement("arg0");
        element1.appendChild(cd);
      
         System.out.println("Validate mandate creation request");
         m.writeTo(System.out);
         
      SOAPConnectionFactory fac =   SOAPConnectionFactory.newInstance();
      SOAPConnection sc =  fac.createConnection();
     
     new SSLManager().disableSSL();
     
     URL url = new URL(liveendpoint);
     System.out.println("Sending to endpoint");
     SOAPMessage response =sc.call(m, url);
    
     System.out.println("++++++++Response from endpoint+++++++++++");
     response.writeTo(System.out);
     sc.close();
     
     
     Iterator iterator1 =  response.getSOAPBody().getChildElements();
          obj = new JsonObject();
          JSONObject jobj=new JSONObject();
          while(iterator1.hasNext()){     
             SOAPBodyElement sb1 =(SOAPBodyElement)iterator1.next();
             Iterator iterator2 = sb1.getChildElements();
               while(iterator2.hasNext()){
                  SOAPBodyElement sb2 =(SOAPBodyElement)iterator2.next();
                  obj.addProperty(sb2.getNodeName(), sb2.getValue());
                  jobj.append(sb2.getNodeName(),XML.toJSONObject(sb2.getValue(),true));
                  
               }
      
          }
          
         return jobj.toString();
 
     } catch(Exception e){
       obj = new JsonObject();
       obj.addProperty("code", "S7");
       obj.addProperty("message", "operation failed");
       System.out.println(e.getMessage());
       return obj.toString();
     }
     
    } 
    
    
 public String cancelMandate(String mandatecode,String bankcode) throws SOAPException, MalformedURLException, IOException, JSONException{
   try{     
      String billertransId="CG_"+UUID.randomUUID().toString();
      String payload="<CancelMandateRequest><MandateCode>"+mandatecode+"</MandateCode><TransType>3</TransType><BankCode>"+bankcode+"</BankCode><BillerID>"+billerid+"</BillerID><BillerName>"+billername+"</BillerName><BillerTransId>"+billertransId+"</BillerTransId></CancelMandateRequest>"+key;
      String payloadhash =  DigestUtils.sha256Hex(payload); 
        
        QName  q = new QName("xmlns:web");
        MessageFactory  mf = MessageFactory.newInstance();
        SOAPMessage  m = mf.createMessage();
        m.getSOAPPart().getEnvelope().addAttribute(q, "http://web.nibss.com/");
        SOAPBody  body = m.getSOAPBody();
       SOAPElement element=  body.addChildElement("cancelMandate", "web");
       CDATASection  cd = m.getSOAPPart().createCDATASection("<CancelMandateRequest><MandateCode>"+mandatecode+"</MandateCode><TransType>3</TransType><BankCode>"+bankcode+"</BankCode><BillerID>"+billerid+"</BillerID><BillerName>"+billername+"</BillerName><BillerTransId>"+billertransId+"</BillerTransId><HashValue>"+payloadhash+"</HashValue></CancelMandateRequest>");
      element1=element.addChildElement("arg0");
      element1.appendChild(cd);
      
      SOAPConnectionFactory fac =   SOAPConnectionFactory.newInstance();
      SOAPConnection sc =  fac.createConnection();
     
     new SSLManager().disableSSL();
     
     URL url = new URL(liveendpoint);
     SOAPMessage response =sc.call(m, url);
     m.writeTo(System.out);
     System.out.println("+++++++++Response++++++++++");
     response.writeTo(System.out);
     sc.close();
     
     Iterator iterator1 =  response.getSOAPBody().getChildElements();
          obj = new JsonObject();
          JSONObject jobj=new JSONObject();
          while(iterator1.hasNext()){     
             SOAPBodyElement sb1 =(SOAPBodyElement)iterator1.next();
             Iterator iterator2 = sb1.getChildElements();
               while(iterator2.hasNext()){
                  SOAPBodyElement sb2 =(SOAPBodyElement)iterator2.next();
                  obj.addProperty(sb2.getNodeName(), sb2.getValue());
                  jobj.append(sb2.getNodeName(),XML.toJSONObject(sb2.getValue(),true));
                  
               }
      
          }
          
         return jobj.toString();
     } catch(Exception e){
       obj = new JsonObject();
       obj.addProperty("code", "S7");
       obj.addProperty("message", "operation failed");
       System.out.println(e.getMessage());
       return obj.toString();
     }
   
    } 
    
    
 public String validateMandateCancelation(String mandatecode,String bankcode, String otp,String amount) throws SOAPException, MalformedURLException, IOException, JSONException{
    try{      
      String billertransId="CG_"+UUID.randomUUID().toString();
      String payload="<ValidateOTPRequest><MandateCode>"+mandatecode+"</MandateCode><TransType>3</TransType><BankCode>"+bankcode+"</BankCode><OTP>"+otp+"</OTP><BillerID>"+billerid+"</BillerID><BillerName>"+billername+"</BillerName><Amount>"+amount+"</Amount><BillerTransId>"+billertransId+"</BillerTransId></ValidateOTPRequest>"+key;
      String payloadhash =  DigestUtils.sha256Hex(payload); 
        
        QName  q = new QName("xmlns:web");
        MessageFactory  mf = MessageFactory.newInstance();
        SOAPMessage  m = mf.createMessage();
        m.getSOAPPart().getEnvelope().addAttribute(q, "http://web.nibss.com/");
        SOAPBody  body = m.getSOAPBody();
        SOAPElement element=  body.addChildElement("validateOTPRequest", "web");
        CDATASection cd = m.getSOAPPart().createCDATASection("<ValidateOTPRequest><MandateCode>"+mandatecode+"</MandateCode><TransType>3</TransType><BankCode>"+bankcode+"</BankCode><OTP>"+otp+"</OTP><BillerID>"+billerid+"</BillerID><BillerName>"+billername+"</BillerName><Amount>"+amount+"</Amount><BillerTransId>"+billertransId+"</BillerTransId><HashValue>"+payloadhash+"</HashValue></ValidateOTPRequest>");       
        element1=element.addChildElement("arg0");
        element1.appendChild(cd);
      
         System.out.println("Validate mandate creation request");
         m.writeTo(System.out);
         
      SOAPConnectionFactory fac =   SOAPConnectionFactory.newInstance();
      SOAPConnection sc =  fac.createConnection();
     
     new SSLManager().disableSSL();
     
     URL url = new URL(liveendpoint);
     System.out.println("Sending to endpoint");
     SOAPMessage response =sc.call(m, url);
    
     System.out.println("++++++++Response from endpoint+++++++++++");
     response.writeTo(System.out);
     sc.close();
     
     
     Iterator iterator1 =  response.getSOAPBody().getChildElements();
          obj = new JsonObject();
          JSONObject jobj=new JSONObject();
          while(iterator1.hasNext()){     
             SOAPBodyElement sb1 =(SOAPBodyElement)iterator1.next();
             Iterator iterator2 = sb1.getChildElements();
               while(iterator2.hasNext()){
                  SOAPBodyElement sb2 =(SOAPBodyElement)iterator2.next();
                  obj.addProperty(sb2.getNodeName(), sb2.getValue());
                  jobj.append(sb2.getNodeName(),XML.toJSONObject(sb2.getValue(),true));
                  
               }
      
          }
          
         return jobj.toString();
      } catch(Exception e){
       obj = new JsonObject();
       obj.addProperty("code", "S7");
       obj.addProperty("message", "operation failed");
       System.out.println(e.getMessage());
       return obj.toString();
     }
     
     
    } 
    
    
    
 public String generateManadateTransactionOTP(String mandatecode,String bankcode,String amount) throws SOAPException, MalformedURLException, IOException, JSONException{    
   try{   
      String billertransId="CG_"+UUID.randomUUID().toString();
      String payload="<GenerateOTPRequest><MandateCode>"+mandatecode+"</MandateCode><TransType>2</TransType><BankCode>"+bankcode+"</BankCode><BillerID>"+billerid+"</BillerID><BillerName>"+billername+"</BillerName><Amount>"+amount+"</Amount><BillerTransId>"+billertransId+"</BillerTransId></GenerateOTPRequest>"+key;
      String payloadhash =  DigestUtils.sha256Hex(payload); 
        
        QName  q = new QName("xmlns:web");
        MessageFactory  mf = MessageFactory.newInstance();
        SOAPMessage  m = mf.createMessage();
        m.getSOAPPart().getEnvelope().addAttribute(q, "http://web.nibss.com/");
        SOAPBody  body = m.getSOAPBody();
        
       SOAPElement element=  body.addChildElement("generateOTPRequest", "web");
       CDATASection cd = m.getSOAPPart().createCDATASection("<GenerateOTPRequest><MandateCode>"+mandatecode+"</MandateCode><TransType>2</TransType><BankCode>"+bankcode+"</BankCode><BillerID>"+billerid+"</BillerID><BillerName>"+billername+"</BillerName><Amount>"+amount+"</Amount><BillerTransId>"+billertransId+"</BillerTransId><HashValue>"+payloadhash+"</HashValue></GenerateOTPRequest>");       
      element1=element.addChildElement("arg0");
      element1.appendChild(cd);
      
      System.out.println("generateManadateTransactionOTP creation request");
      m.writeTo(System.out);
      
      
      SOAPConnectionFactory fac =   SOAPConnectionFactory.newInstance();
      SOAPConnection sc =  fac.createConnection();
     
     new SSLManager().disableSSL();
     
     System.out.println("calling endpoint"); 
     
     URL url = new URL(liveendpoint);
     SOAPMessage response =sc.call(m, url);
     System.out.println("++++++++Rsponse from endpoint+++++++++++");
     response.writeTo(System.out);
     sc.close();
     
   

     
     Iterator iterator1 =  response.getSOAPBody().getChildElements();
          obj = new JsonObject();
          JSONObject jobj=new JSONObject();
          while(iterator1.hasNext()){     
             SOAPBodyElement sb1 =(SOAPBodyElement)iterator1.next();
             Iterator iterator2 = sb1.getChildElements();
               while(iterator2.hasNext()){
                  SOAPBodyElement sb2 =(SOAPBodyElement)iterator2.next();
                  obj.addProperty(sb2.getNodeName(), sb2.getValue());
                  jobj.append(sb2.getNodeName(),XML.toJSONObject(sb2.getValue(),true));
                  
               }
      
          }
          
         return jobj.toString();
 
     } catch(Exception e){
       obj = new JsonObject();
       obj.addProperty("code", "S7");
       obj.addProperty("message", "operation failed");
       System.out.println(e.getMessage());
       return obj.toString();
     }
     
    } 
    
    
    
    
    
  public String validateMandateTransactionOTP(String mandatecode,String bankcode, String otp,String amount) throws SOAPException, MalformedURLException, IOException, JSONException{
   try{
      String billertransId="CG_"+UUID.randomUUID().toString();
      String payload="<ValidateOTPRequest><MandateCode>"+mandatecode+"</MandateCode><TransType>2</TransType><BankCode>"+bankcode+"</BankCode><OTP>"+otp+"</OTP><BillerID>"+billerid+"</BillerID><BillerName>"+billername+"</BillerName><Amount>"+amount+"</Amount><BillerTransId>"+billertransId+"</BillerTransId></ValidateOTPRequest>"+key;
      String payloadhash =  DigestUtils.sha256Hex(payload); 
        
        QName  q = new QName("xmlns:web");
        MessageFactory  mf = MessageFactory.newInstance();
        SOAPMessage  m = mf.createMessage();
        m.getSOAPPart().getEnvelope().addAttribute(q, "http://web.nibss.com/");
        SOAPBody  body = m.getSOAPBody();
        SOAPElement element=  body.addChildElement("validateOTPRequest", "web");
        CDATASection   cd = m.getSOAPPart().createCDATASection("<ValidateOTPRequest><MandateCode>"+mandatecode+"</MandateCode><TransType>2</TransType><BankCode>"+bankcode+"</BankCode><OTP>"+otp+"</OTP><BillerID>"+billerid+"</BillerID><BillerName>"+billername+"</BillerName><Amount>"+amount+"</Amount><BillerTransId>"+billertransId+"</BillerTransId><HashValue>"+payloadhash+"</HashValue></ValidateOTPRequest>");       
      element1=element.addChildElement("arg0");
      element1.appendChild(cd);
      
      System.out.println("validateMandateTransactionOTP request"); 
     
      SOAPConnectionFactory fac =   SOAPConnectionFactory.newInstance();
      SOAPConnection sc =  fac.createConnection();
     
      new SSLManager().disableSSL();
 
      System.out.println("calling endpoint");
      
      URL url = new URL(liveendpoint);
      SOAPMessage response =sc.call(m, url);
   
     System.out.println("+++++++++Response from endpoint++++++++++");
     response.writeTo(System.out);
     sc.close();
  

     
     Iterator iterator1 =  response.getSOAPBody().getChildElements();
          obj = new JsonObject();
          JSONObject jobj=new JSONObject();
          while(iterator1.hasNext()){     
             SOAPBodyElement sb1 =(SOAPBodyElement)iterator1.next();
             Iterator iterator2 = sb1.getChildElements();
               while(iterator2.hasNext()){
                  SOAPBodyElement sb2 =(SOAPBodyElement)iterator2.next();
                  obj.addProperty(sb2.getNodeName(), sb2.getValue());
                  jobj.append(sb2.getNodeName(),XML.toJSONObject(sb2.getValue(),true));
                  
               }
      
          }
          
         return jobj.toString();
   
         } catch(Exception e){
       obj = new JsonObject();
       obj.addProperty("code", "S7");
       obj.addProperty("message", "operation failed");
       System.out.println(e.getMessage());
       return obj.toString();
     }
     
     
    } 
    
    
    
  public String generateOTPNoReg(String acctnum,String acctname,String bankcode,String amount) throws SOAPException, MalformedURLException, IOException, JSONException{
    try{      
      String billertransId="CG_"+UUID.randomUUID().toString();
      String payload="<GenerateOTPRequest><AcctNumber>"+acctnum+"</AcctNumber><AcctName>"+acctname+"</AcctName><TransType>2</TransType><BankCode>"+bankcode+"</BankCode><BillerID>"+billerid+"</BillerID><BillerName>"+billername+"</BillerName><Amount>"+amount+"</Amount><BillerTransId>"+billertransId+"</BillerTransId></GenerateOTPRequest>"+key;
      String payloadhash =  DigestUtils.sha256Hex(payload); 
        
        QName  q = new QName("xmlns:web");
        MessageFactory  mf = MessageFactory.newInstance();
        SOAPMessage  m = mf.createMessage();
        m.getSOAPPart().getEnvelope().addAttribute(q, "http://web.nibss.com/");
        SOAPBody  body = m.getSOAPBody();
        
      SOAPElement element=  body.addChildElement("generateOTPRequestNoReg", "web");
      CDATASection cd = m.getSOAPPart().createCDATASection("<GenerateOTPRequest><AcctNumber>"+acctnum+"</AcctNumber><AcctName>"+acctname+"</AcctName><TransType>2</TransType><BankCode>"+bankcode+"</BankCode><BillerID>"+billerid+"</BillerID><BillerName>"+billername+"</BillerName><Amount>"+amount+"</Amount><BillerTransId>"+billertransId+"</BillerTransId><HashValue>"+payloadhash+"</HashValue></GenerateOTPRequest>");       
      element1=element.addChildElement("arg0");
      element1.appendChild(cd);
      
      SOAPConnectionFactory fac =   SOAPConnectionFactory.newInstance();
     SOAPConnection sc =  fac.createConnection();
     
     new SSLManager().disableSSL();
     
     URL url = new URL(liveendpoint);
     SOAPMessage response =sc.call(m, url);
     m.writeTo(System.out);
     System.out.println("+++++++++Response++++++++++");
     response.writeTo(System.out);
     sc.close();
     
     Iterator iterator1 =  response.getSOAPBody().getChildElements();
          obj = new JsonObject();
          JSONObject jobj=new JSONObject();
          while(iterator1.hasNext()){     
             SOAPBodyElement sb1 =(SOAPBodyElement)iterator1.next();
             Iterator iterator2 = sb1.getChildElements();
               while(iterator2.hasNext()){
                  SOAPBodyElement sb2 =(SOAPBodyElement)iterator2.next();
                  obj.addProperty(sb2.getNodeName(), sb2.getValue());
                  jobj.append(sb2.getNodeName(),XML.toJSONObject(sb2.getValue(),true));
                  
               }
      
          }
          
         return jobj.toString();
 
     } catch(Exception e){
       obj = new JsonObject();
       obj.addProperty("code", "S7");
       obj.addProperty("message", "operation failed");
       System.out.println(e.getMessage());
       return obj.toString();
     }
     
    } 
    
    
    
  public String validateOTPNoReg(String acctnum,String acctname,String mandatecode,String bankcode,String otp,String amount) throws SOAPException, MalformedURLException, IOException, JSONException{
    try{      
      String billertransId="CG_"+UUID.randomUUID().toString();
      String payload="<ValidateOTPRequest><AcctNumber>"+acctnum+"</AcctNumber><AcctName>"+acctname+"</AcctName><MandateCode>"+mandatecode+"</MandateCode><TransType>2</TransType><BankCode>"+bankcode+"</BankCode><OTP>"+otp+"</OTP><BillerID>"+billerid+"</BillerID><BillerName>"+billername+"</BillerName><Amount>"+amount+"</Amount><BillerTransId>"+billertransId+"</BillerTransId></ValidateOTPRequest>"+key;
      String payloadhash =  DigestUtils.sha256Hex(payload); 
        
        QName  q = new QName("xmlns:web");
        MessageFactory  mf = MessageFactory.newInstance();
        SOAPMessage  m = mf.createMessage();
        m.getSOAPPart().getEnvelope().addAttribute(q, "http://web.nibss.com/");
        SOAPBody  body = m.getSOAPBody();
        
      SOAPElement element=  body.addChildElement("validateOTPRequest", "web");
     CDATASection cd = m.getSOAPPart().createCDATASection("<ValidateOTPRequest><AcctNumber>"+acctnum+"</AcctNumber><AcctName>"+acctname+"</AcctName><MandateCode>"+mandatecode+"</MandateCode><TransType>2</TransType><BankCode>"+bankcode+"</BankCode><OTP>"+otp+"</OTP><BillerID>"+billerid+"</BillerID><BillerName>"+billername+"</BillerName><Amount>"+amount+"</Amount><BillerTransId>"+billertransId+"</BillerTransId><HashValue>"+payloadhash+"</HashValue></ValidateOTPRequest>");       
      element1=element.addChildElement("arg0");
      element1.appendChild(cd);
      
      SOAPConnectionFactory fac =   SOAPConnectionFactory.newInstance();
     SOAPConnection sc =  fac.createConnection();
     
     new SSLManager().disableSSL();
     
     URL url = new URL(liveendpoint);
     SOAPMessage response =sc.call(m, url);
     m.writeTo(System.out);
     System.out.println("+++++++++Response++++++++++");
     response.writeTo(System.out);
     sc.close();
     
     Iterator iterator1 =  response.getSOAPBody().getChildElements();
          obj = new JsonObject();
          JSONObject jobj=new JSONObject();
          while(iterator1.hasNext()){     
             SOAPBodyElement sb1 =(SOAPBodyElement)iterator1.next();
             Iterator iterator2 = sb1.getChildElements();
               while(iterator2.hasNext()){
                  SOAPBodyElement sb2 =(SOAPBodyElement)iterator2.next();
                  obj.addProperty(sb2.getNodeName(), sb2.getValue());
                  jobj.append(sb2.getNodeName(),XML.toJSONObject(sb2.getValue(),true));
                  
               }
      
          }
          
         return jobj.toString();
 
     } catch(Exception e){
       obj = new JsonObject();
       obj.addProperty("code", "S7");
       obj.addProperty("message", "operation failed");
       System.out.println(e.getMessage());
       return obj.toString();
     }
     
    } 
    
    
    
    
  public String requeryMandate(String mandatecode) throws SOAPException, MalformedURLException, IOException, JSONException{
       try{   

         String payload="<RequeryMandateRequest><MandateCode>"+mandatecode+"</MandateCode><BillerID>"+billerid+"</BillerID></RequeryMandateRequest>"+key;
         String payloadhash =  DigestUtils.sha256Hex(payload); 
        
        QName  q = new QName("xmlns:web");
        MessageFactory  mf = MessageFactory.newInstance();
        SOAPMessage  m = mf.createMessage();
        m.getSOAPPart().getEnvelope().addAttribute(q, "http://web.nibss.com/");
        SOAPBody  body = m.getSOAPBody();
        SOAPElement element=  body.addChildElement("requeryMandate", "web");
        CDATASection cd = m.getSOAPPart().createCDATASection("<RequeryMandateRequest><MandateCode>"+mandatecode+"</MandateCode><BillerID>"+billerid+"</BillerID><HashValue>"+payloadhash+"</HashValue></RequeryMandateRequest>");       
        element1=element.addChildElement("arg0");
        element1.appendChild(cd);
      
         System.out.println("ReQueryMandate request");
         m.writeTo(System.out);
         
         SOAPConnectionFactory fac =   SOAPConnectionFactory.newInstance();
         SOAPConnection sc =  fac.createConnection();
     
         new SSLManager().disableSSL();
     
     URL url = new URL(liveendpoint);
     System.out.println("Sending to endpoint");
     SOAPMessage response =sc.call(m, url);
    
     System.out.println("++++++++Response from endpoint+++++++++++");
     response.writeTo(System.out);
     sc.close();
     
     
     Iterator iterator1 =  response.getSOAPBody().getChildElements();
          obj = new JsonObject();
          JSONObject jobj=new JSONObject();
          while(iterator1.hasNext()){     
             SOAPBodyElement sb1 =(SOAPBodyElement)iterator1.next();
             Iterator iterator2 = sb1.getChildElements();
               while(iterator2.hasNext()){
                  SOAPBodyElement sb2 =(SOAPBodyElement)iterator2.next();
                  obj.addProperty(sb2.getNodeName(), sb2.getValue());
                  jobj.append(sb2.getNodeName(),XML.toJSONObject(sb2.getValue(),true));
                  
               }
      
          }
          
         return jobj.toString();
 
         } catch(Exception e){
       obj = new JsonObject();
       obj.addProperty("code", "S7");
       obj.addProperty("message", "operation failed");
       System.out.println(e.getMessage());
       return obj.toString();
     }
     
     
    } 
    

   public static void main(String[] args) throws SOAPException, IOException, MalformedURLException, JSONException {
         System.out.println(new AccountActions().getBankList());
       // System.out.println(new AccountActions().createMandate("2189924346", "SESAN PAUL SAJUYIGBE", "057"));
    }
   
    
}
