package Tornado;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import javax.imageio.ImageIO;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.common.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.google.gson.Gson;


public class Main
{
	
	public static double[] Num = new double[6];
	public static String[] data = new String[5];
	
    public static void main(String[] args)
    {
        try
        {
        	//Hyperlink link = null;
        	XSSFWorkbook workbook=null;
        	XSSFSheet sheet=null;
            FileInputStream file = new FileInputStream(new File("C:\\Users\\Balu\\Desktop\\Tornado_imagery_database.xlsx"));
            
            //Create Workbook instance holding reference to .xlsx file
            try
            {
               workbook = new XSSFWorkbook(file);
 
               sheet = workbook.getSheetAt(0);
              
            //Iterate through each rows one by one
            Iterator<Row> rowIterator = sheet.iterator();
            Row row = rowIterator.next();
            while (rowIterator.hasNext())
            {
                 row = rowIterator.next();
                //For each row, iterate through all the columns
                Iterator<Cell> cellIterator = row.cellIterator();
                 int i =0 ,j = 0, count=0;
                while (cellIterator.hasNext())
                {
                    Cell cell = cellIterator.next();
                   switch (cell.getCellType())
                    {
                        case Cell.CELL_TYPE_NUMERIC:
                        	  
                               Main.Num[i] = cell.getNumericCellValue();
                               //System.out.println(Main.Num[i]);
                               i++;
                        	
                               break;
                        case Cell.CELL_TYPE_STRING:
                           	
                           Main.data[j] = cell.getStringCellValue().toString();
                           //System.out.println(Main.data[j]);
                           j++;
                           break;    
                    }
                }
                System.out.println("");
                i=0;
                j=0;
                if(count >= 444){
                	break;
                }
                else
                {
                	Main.buildData();
                	count++;
                }
              file.close();
              }
            }
            catch(Exception e)
            {
            	System.out.println(e);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    
    public static void buildData()
    {
    	 BufferedImage images;
    	 BufferedImage i1 = null;
    	 URL url = null;
		try {
			  url = new URL(Main.data[0]);
			  
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			System.out.println(url.toString());
			i1 = ImageIO.read(url.openStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         BufferedImage scaledImage = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);
         Graphics2D graphics2D = scaledImage.createGraphics();
         graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
         graphics2D.drawImage(i1, 0, 0, 600, 600, null);
         graphics2D.dispose();
         System.out.println(" Revised Height : " + scaledImage.getHeight(null));
         System.out.println(" Revised Width : " + scaledImage.getWidth(null));
         images = scaledImage;
         double[] latlong = new double[2];
         latlong[0] = Main.Num[4];
         latlong[1] = Main.Num[5];
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
      	 try {
      		 //System.out.println("Balu");
			ImageIO.write(images, "jpg", baos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         try {
			baos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         String encoded=Base64.encodeBytes(baos.toByteArray());
         try {
			baos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         Image image = new Image(encoded, "jpg");
 		 Scene sc = new Scene(Main.data[0], image, Main.data[1] + " , " + Main.data[2], latlong);
 		 Main.insertMongo(sc);
 		 
 		 System.out.println("sceneid : " + Main.data[0]);
 		 System.out.println("description : " + Main.data[1] + " , " + Main.data[2]);
 		 System.out.println("latlong : " + latlong[0] + " , " + latlong[1]);
    }
    
    public static void insertMongo(Scene obj)
    {
    	String url = "http://lasir.umkc.edu:8080/cisaserviceengine/webresources/cisa/observerdatatorn";
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