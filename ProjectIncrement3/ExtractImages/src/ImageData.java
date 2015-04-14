import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.AttributeSet;
import javax.swing.text.html.HTMLDocument;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import com.google.gson.Gson;

public class ImageData {
	public static URL url;
	public static String webUrl;
	public static int count=1740;
	public static URL urlData;
	public static String type;
	public static int en=0;
	public static String saveUrl="1";
	public static double lat;
	public static double lon;
	public static String desc;
	public static BufferedImage images;
	public static String imageUrl;
	
    public static void main(String args[])
    {
    	//ImageData.webUrl = "http://www.eqclearinghouse.org/map/gallery-detail.php?s=0&searchtext=&submit=Search&listtype=&eventid=29&cat=&name=";
    	ImageData.webUrl = "http://www.eqclearinghouse.org/map/gallery-detail.php?s=2160&searchtext=&submit=Search&listtype=&eventid=29&cat=&name=";
		try{
		ImageData.url = new URL(ImageData.webUrl);
	    while(ImageData.webUrl != null){

		   if(ImageData.saveUrl.equals(ImageData.webUrl))
	       {
			   System.out.println("Exiting2...");
			   break;
			   
	       }
	      else
	       {
	    	  
	    	    System.out.println(ImageData.webUrl);
				ImageData.url = new URL(ImageData.webUrl);
				ImageData.getImageData();
		     
	       }
	      }
	    }
		catch(Exception e)
		{
			System.out.println(e);
		}
    }
	public static void getImageData() throws Exception
	{		
		URLConnection connection = url.openConnection();
		InputStream is = connection.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		
		HTMLEditorKit htmlKit = new HTMLEditorKit();
		HTMLDocument htmlDoc = (HTMLDocument) htmlKit.createDefaultDocument();
		htmlDoc.putProperty("IgnoreCharsetDirective", new Boolean(true));
		htmlKit.read(br, htmlDoc, 0);
		try{

		for (HTMLDocument.Iterator iterator = htmlDoc.getIterator(HTML.Tag.A); iterator.isValid(); iterator.next()) {
		    AttributeSet attributes = iterator.getAttributes();
		    String latlong = (String) attributes.getAttribute(HTML.Attribute.HREF);
            if(latlong.contains("editpoint.php?id"))
            		{
		              System.out.println("Edit Urls : "  + latlong.toString());
		    		  urlData = new URL(latlong);
		              getLatLong();
            		}
		    }
		saveUrl = webUrl;
		count = count + 10;
		webUrl = "http://www.eqclearinghouse.org/map/gallery-detail.php?s=" + count + "&searchtext=&submit=Search&listtype=&eventid=29&cat=&name=";

		}
		catch(Exception e)
		{
			System.out.println(e);
		}
		
	}
	
	private static void getLatLong() throws Exception
	{		
		URLConnection connection = urlData.openConnection();
		InputStream is = connection.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		
		HTMLEditorKit htmlKit = new HTMLEditorKit();
		HTMLDocument htmlDoc = (HTMLDocument) htmlKit.createDefaultDocument();
		htmlDoc.putProperty("IgnoreCharsetDirective", new Boolean(true));
		htmlKit.read(br, htmlDoc, 0);

		for (HTMLDocument.Iterator iterator = htmlDoc.getIterator(HTML.Tag.INPUT); iterator.isValid(); iterator.next()) {
		    AttributeSet attributes = iterator.getAttributes();
		    String latlong = (String) attributes.getAttribute(HTML.Attribute.NAME);
            String value = (String) attributes.getAttribute(HTML.Attribute.VALUE);
		 
		    if (latlong != null) {
		    	
		    	String temp = latlong.toString();
		    	
		    	  if(temp.equals("lat"))	
		    	  {
		    		   System.out.println(value);
                       if(value != null && !value.isEmpty()){
		    			   
		    			   //System.out.println("Desc1 : " + value);
		    			   lat = Double.parseDouble(value);
		    		    }
		    		   else
		    		   {
		    			   lat = 0.0;     
		    		   }
		          }
		    	  else if(temp.equals("lng"))
		    	  {
		    		   System.out.println(value);
                        if(value != null && !value.isEmpty()){
		    			   
		    			   //System.out.println("Desc1 : " + value);
		    			   lon = Double.parseDouble(value);
		    		    }
		    		   else
		    		   {
		    			   lon = 0.0;     
		    		   }
		    	  }
		    	  else
		    	  {
		    		
		    	  }
		      }
	     }
		
		for (HTMLDocument.Iterator iterator1 = htmlDoc.getIterator(HTML.Tag.TEXTAREA); iterator1.isValid(); iterator1.next()) {
		    AttributeSet attributes = iterator1.getAttributes();
		    String de = (String) attributes.getAttribute(HTML.Attribute.NAME);
            String value1 = (String) attributes.getAttribute(HTML.Attribute.VALUE);
		 
		    if (de != null) {
		    	
		    	String temp = de.toString();
		    	
		    	  if(temp.equals("description"))
		    	  {
		    		   System.out.println("Desc10 : " + value1);
		    		   
		    		   if(value1 != null && !value1.isEmpty()){
		    			   
		    			   System.out.println("Desc1 : " + value1);
		    			   desc = value1;
		    		    }
		    		   else
		    		   {
		    			   System.out.println("Desc2 : " + value1);
		    			   desc = "Not Available";
		    			      
		    		   }
		    		  
		    	  }
		    	  else
		    	  {
		    		  System.out.println("BALU");
		    		 // desc.add("Not Available");
		    	  }
		      }
		    else{}
	     }
		int k=0;
		for (HTMLDocument.Iterator iterator1 = htmlDoc.getIterator(HTML.Tag.IMG); iterator1.isValid(); iterator1.next()) {
		    AttributeSet attributes = iterator1.getAttributes();
		    String src = (String) attributes.getAttribute(HTML.Attribute.SRC);
           if(k==0){
        	   download(webUrl,src);
        	   
        	   k++;
           }
           else
           {
        	   break;
           }
		       
	     }

		double[] latlong = new double[2];
		latlong[0]=lat;
    	latlong[1]=lon;
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
     	ImageIO.write(images, "jpg", baos);
         baos.flush();
         String encoded=Base64.encodeBytes(baos.toByteArray());
         baos.close();
         Image image = new Image(encoded, "jpg");
         
		Scene sc = new Scene(imageUrl, image, desc, latlong);
		ImageData.insertMongo(sc);
		System.out.println("Succesfully inserted");
		
	}
	 private static void download(String url, String imgSrc) throws IOException {
	    	BufferedImage i1;
	        try {
	            if (imgSrc.contains("thumb")) {
	            	String regex = "\\s*\\bthumbs\\b\\s*";
	            	imgSrc = imgSrc.replaceAll(regex, "");
	            	url = "http://www.eqclearinghouse.org/map/" + imgSrc;
	            	type = "jpg";
	            } else {
	                return;
	            }
	            imgSrc = imgSrc.substring(imgSrc.lastIndexOf("/") + 1);
	            URL image = new URL(url);
	            i1 = ImageIO.read(image);
	            BufferedImage scaledImage = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);
	            		Graphics2D graphics2D = scaledImage.createGraphics();
	            		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	            		graphics2D.drawImage(i1, 0, 0, 600, 600, null);
	            		graphics2D.dispose();
	            System.out.println(" Revised Height : " + scaledImage.getHeight(null));
	            System.out.println(" Revised Width : " + scaledImage.getWidth(null));
	            images = scaledImage;
	            imageUrl = url + imgSrc;
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }

	    }
	 public static void insertMongo(Scene obj)
		{
			String url = "http://lasir.umkc.edu:8080/cisaserviceengine/webresources/cisa/observerdata";
			HttpClient client = new DefaultHttpClient();
			HttpPost post;
			HttpGet get;
			HttpResponse response = null;
			try{
			Gson gson = new Gson();
			post = new HttpPost(url);
			post.setHeader("Accept", "application/json");
			post.setHeader("Content-Type", "application/json");
			System.out.println("great!!");
			StringEntity se = new StringEntity(gson.toJson(obj));
			System.out.println("End of json create");
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			post.setEntity(se);
			//System.out.println(post.getEntity());
			response = client.execute(post);
			System.out.println("Response : " + response);
			}
			catch(Exception e){
				System.out.println(e);
			}
			
		}
	
}