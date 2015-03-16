import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

 
public class Post_Rest {
    
    private static String session_name = "";
    private static String session_id = "";
    private static String csrf_token = "";
    private static String Rest_URL ="";
    private static String domain="";
    
     private static String httpPost_getSession(String RestURL,String user, String pass) throws Exception {

        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(RestURL + "/user/login");
        String username = user;
        String password = pass;

        JSONObject json = new JSONObject();
        //extract the username and password from UI elements and create a JSON object
        json.put("username", username.trim());
        json.put("password", password.trim());

        //add serialised JSON object into POST request
        StringEntity se = new StringEntity(json.toString());
        //set request content type
        se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        httppost.setEntity(se);

        //send the POST request
        HttpResponse response = httpclient.execute(httppost);
        //read the response from Services endpoint
        String jsonResponse = EntityUtils.toString(response.getEntity());

        if (jsonResponse.equals("[\"Wrong username or password.\"]")) {
            //System.out.println(jsonResponse);
            return jsonResponse;
        } else {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            //read the session information
            session_name = jsonObject.getString("session_name");
            session_id = jsonObject.getString("sessid");
            //System.out.println(session_name+"="+session_id);
            return (session_name+"="+session_id);
        }
    }
 
      
     private static String httpGet_csrf(String RestURL) throws IOException {

        HttpClient httpclient = new DefaultHttpClient();
        String tokenURL = RestURL.substring(0,RestURL.lastIndexOf("/")) +"/services/session/token";
        HttpGet httpget = new HttpGet(tokenURL);


        try {

            BasicHttpContext mHttpContext = new BasicHttpContext();
            CookieStore mCookieStore = new BasicCookieStore();

            //create the session cookie
            BasicClientCookie cookie = new BasicClientCookie(session_name, session_id);
            cookie.setVersion(0);
            cookie.setDomain(domain);
            cookie.setPath("/");
            mCookieStore.addCookie(cookie);
            mHttpContext.setAttribute(ClientContext.COOKIE_STORE, mCookieStore);


            HttpResponse response = httpclient.execute(httpget, mHttpContext);                
            csrf_token = EntityUtils.toString(response.getEntity());
            //System.out.println(csrf_token);
            return csrf_token;

        } catch (Exception e) {
            System.out.println("Error adding : " + e.getMessage());
        }
        
        return null;

    }


    public static String getConnection(String urlStr,String user, String pass) throws Exception{
        Rest_URL=urlStr;
        domain = getDomainName(urlStr);
       String session_response = httpPost_getSession(urlStr,user, pass);
        String csrf = httpGet_csrf(urlStr);  
        return "Session : "+session_response+"\nToken: "+csrf+"\n";
    }
    
    public static String postdata(String data) throws Exception{
        HttpClient httpclient = new DefaultHttpClient();        
        HttpPost httppost = new HttpPost(Rest_URL + "/node");
        //add raw json to be sent along with the HTTP POST request
        StringEntity se = new StringEntity(data);
        //System.out.println("Printing Content:\n" + data);
        se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded"));
        httppost.setEntity(se);
        
        String nid="";

        try {

            BasicHttpContext mHttpContext = new BasicHttpContext();
            CookieStore mCookieStore = new BasicCookieStore();

            //create the session cookie
            BasicClientCookie cookie = new BasicClientCookie(session_name, session_id);
            cookie.setVersion(0);
            cookie.setDomain(domain);
            cookie.setPath("/");
            mCookieStore.addCookie(cookie);

            httppost.addHeader("X-CSRF-TOKEN", csrf_token);
            mHttpContext.setAttribute(ClientContext.COOKIE_STORE, mCookieStore);
            HttpResponse response = httpclient.execute(httppost, mHttpContext);
            System.out.println(response);
            if(response.getStatusLine().getStatusCode()==200)
                return null;
            else
                return data;
            //System.out.println(response.getStatusLine().getStatusCode());
            //return response.toString();
            
//            String jsonResponse = EntityUtils.toString(response.getEntity());
//            JSONObject jsonObject = new JSONObject(jsonResponse);
//            nid = jsonObject.get("nid").toString();
            //System.out.println(nid);

        } catch (Exception e) {
            System.out.println("Error adding : " + e.getMessage());
        }
        return null;
    }

    void EditNode(String nid, String REST_URL, String data) throws Exception {
        HttpClient httpclient = new DefaultHttpClient(); 
        HttpPut httput = new HttpPut(Rest_URL + "/node/"+nid);

        //add raw json to be sent along with the HTTP POST request
        StringEntity se = new StringEntity(data);
        System.out.println("Modifying Content:\n" + data);
        se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded"));
        httput.setEntity(se); 
    

        try {

            BasicHttpContext mHttpContext = new BasicHttpContext();
            CookieStore mCookieStore = new BasicCookieStore();

            //create the session cookie
            BasicClientCookie cookie = new BasicClientCookie(session_name, session_id);
            cookie.setVersion(0);
            cookie.setDomain(domain);
            cookie.setPath("/");
            mCookieStore.addCookie(cookie);

            httput.addHeader("X-CSRF-TOKEN", csrf_token);
            mHttpContext.setAttribute(ClientContext.COOKIE_STORE, mCookieStore);
            HttpResponse response = httpclient.execute(httput, mHttpContext); 
            System.out.println(response);

        } catch (Exception e) {
            System.out.println("Error adding : " + e.getMessage());
        }
      
    }
   
    
    public static String getDomainName(String url) {
        try {
            URI uri = new URI(url);
            String domain = uri.getHost();
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        } catch (URISyntaxException ex) {
            Logger.getLogger(Post_Rest.class.getName()).log(Level.SEVERE, null, ex);
        }
                return null;
}
 }

