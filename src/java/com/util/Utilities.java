/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author centricgateway
 */
public class Utilities {
    
    CloseableHttpClient client = HttpClients.createDefault();
    HttpPost post;
    HttpGet get;
    CloseableHttpResponse response;
    Gson gson ;
    
    
 //   Dao dao= new Dao();
    

    
    JsonObject obj = new JsonObject();
    
    public  String generateToken(String clientId,String ClientSecret){
        
       if(clientId.equals("pk_sbtb6971c8032eed3d4643836c26f669a014fc12") && ClientSecret.equals("sk_corb6971c8032eed3d4643836c26f669a014f2c2")){
        Long exp=System.currentTimeMillis()+3600000;
        String token = Jwts.builder().claim("clientId", clientId).claim("ClientSecret", ClientSecret)
       .setExpiration(new Date(exp)).setIssuer("CG")    
       .signWith(SignatureAlgorithm.HS512, "sbt_core@_key#".getBytes()).compact();                
        obj = new JsonObject();
        obj.addProperty("access_token", token);
        obj.addProperty("issued at", LocalDateTime.now().toString().replace("T",""));
        obj.addProperty("expires_in", LocalDateTime.ofInstant(Instant.ofEpochMilli(exp), ZoneId.systemDefault()).toString());
        obj.addProperty("ResponseMessage", "Successful");
        obj.addProperty("ResponseCode", "00");
       }else{
           obj.addProperty("ResponseMessage", "Invalid API Credentails");
           obj.addProperty("ResponseCode", "S3");
                }
        return obj.toString();
    }
    
    public  JsonObject validateToken(String token){
               
        try{
        Jwts.parser().setSigningKey("sbt_core@_key#".getBytes()).parseClaimsJws(token);
        obj.addProperty("message", "Successful");
        obj.addProperty("code", "00");
        }catch(ExpiredJwtException e){
            obj.addProperty("message", "expired token");
            obj.addProperty("code", "S4");
        }catch(SignatureException e){
            obj.addProperty("message", "Cannot validate signature");
            obj.addProperty("code", "S5");
            System.out.println(e.getMessage());
        }
        
        return obj;
       
    }
    
    
      public String getThirdPartyApi() throws UnsupportedEncodingException, IOException{
        post = new HttpPost(getAppProperties().getProperty("seerbit_auth_endpoint"));
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        List<NameValuePair>list= new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("grant_type", "client_credentials"));
        list.add(new BasicNameValuePair("client_id", "test"));
        list.add(new BasicNameValuePair("client_secret", "test"));
        post.setEntity(new UrlEncodedFormEntity(list));
        response=client.execute(post);
        String msg = EntityUtils.toString(response.getEntity());
        return msg;
     }

      
     

    
    public static void generatecode(){
       DateTimeFormatter df =  DateTimeFormatter.ofPattern("yymmddhhmmss");
       String bankcode="000014";
       int unique=Instant.now().getNano();
       String code=bankcode.concat(df.format(LocalDateTime.now())).concat(Integer.toString(unique));
        System.out.println(code);
        System.out.println(unique);
    }
    
    public Properties getAppProperties(){
       Properties prop= null;
       try{
        prop  = new Properties();
        prop.load(this.getClass().getResourceAsStream("/properties.properties"));
        return prop;
       }catch(Exception e){
           System.out.println("====== caused by ===== "+e.getMessage());
          return prop;
       }
    }
    
    public void check() throws IOException{
        System.out.println(getAppProperties().getProperty("settlement_endpoint"));
    }
    
    public static void main(String[] args) throws IOException {       
           DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy:MM:dd hh:mm:ss");
           String datetime=df.format(LocalDateTime.now());
           System.out.println(datetime);
   
    }
    
    
}
