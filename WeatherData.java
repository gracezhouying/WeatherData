import java.util.Collections; 
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Arrays;

import com.jaunt.*;
import com.jaunt.component.*;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import java.io.*;
import java.util.HashMap; 

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class trytry{
	  public static void main(String[] args) throws JauntException, Exception{
		  ArrayList<String> zipcodes = new ArrayList<String> (Arrays.asList("27701", "94040", "10025", "94108", "60605", "90028", "75201", "30303", "33131", "02133",
		            "19106", "20500", "48226", "77002", "97205", "98101", "80202", "89101", "92101", "14203",
		            "55403", "64108", "85007", "73102", "46225", "21201", "70119", "95113", "78701",  "07306"));
		  
		  // ArrayList to store all SQL insert queries
		  ArrayList<String> queries = new ArrayList<String>();
		  
		  // Get weather information of all cities from API
		  for (int i = 0; i < zipcodes.size(); i++) {
			  String url = GetUrl(zipcodes.get(i));
			  HashMap<String, Object> Weather = GetWeather(url, zipcodes.get(i));
			  queries.add(SqlQuery(Weather, "citiesweathers")); // you can change table name here	
		  }
		  	
		  // Connect MySQL DataBase, create new table and insert all information
		  try{
			  MysqlDataSource dataSource = new MysqlDataSource();
			  dataSource.setUser("root");
			  dataSource.setPassword("0912");
			  dataSource.setServerName("localhost");
			  dataSource.setPortNumber(3306);
			  dataSource.setDatabaseName("cityweather");
			  Connection conn = dataSource.getConnection();
			  Statement stmt = conn.createStatement();  
			  // Sunrise, Sunset, Refertime are INT here!!!
			  String createTable = "CREATE TABLE citiesweathers (zipcode INT(5) UNSIGNED AUTO_INCREMENT PRIMARY KEY, city VARCHAR(30) NOT NULL, description VARCHAR(30) NOT NULL, max_temp DOUBLE, min_temp DOUBLE, temp DOUBLE, lon DOUBLE, lat DOUBLE, sunrise INT, sunset INT, refertime INT)";
		      stmt.executeUpdate(createTable); //If table already created, comment this line
			  for (int i = 0; i < queries.size();i++){
				  stmt.executeUpdate(queries.get(i));
					}
			}
			catch(Exception e){
				System.out.println("error: " + e.getLocalizedMessage());
			}
	  }
	
	  /*
	   * function to get weather information from API
	   */
	  private static HashMap<String, Object> GetWeather(String url, String zipcode) throws JauntException{
		  try{
			  UserAgent userAgent = new UserAgent();
			  userAgent.sendGET(url); 
			  JNode cw = userAgent.json;
			  HashMap<String, Object> result = new HashMap<String, Object>();
			  result.put("zipcode", Integer.parseInt(zipcode));
			  result.put("name", cw.get("name").toString());
			  result.put("weather", cw.get("weather").get(0).get("description").toString());
			  result.put("temp", cw.get("main").get("temp").toDouble());
			  result.put("temp_max", cw.get("main").get("temp_max").toDouble());
			  result.put("temp_min", cw.get("main").get("temp_min").toDouble());
			  result.put("sunrise", cw.get("sys").get("sunrise").toInt());
			  result.put("sunset", cw.get("sys").get("sunset").toInt());
			  result.put("refertime", cw.get("dt").toInt());
			  result.put("longitude", cw.get("coord").get("lon").toDouble());
			  result.put("latitude", cw.get("coord").get("lat").toDouble());
			  return result;
		  }
		  catch(JauntException e){         //if an HTTP/connection error occurs, handle JauntException.
		    	System.err.println(e);
		    	return null;
		    }
	  }
	  
	  /*
	   * function to create the API url with zipcode
	   */
	  private static String GetUrl(String zipcode){
		  String s = "http://api.openweathermap.org/data/2.5/weather?zip=";
		  s = s.concat(zipcode);
		  s = s.concat(",US&units=imperial&apikey=c7bcdf9f9f288e6167d129a0e93df0ea");
		  return s;
	  }
	  
	  /*
	   * function to create the SQL insert query 
	   */
	  private static String SqlQuery(HashMap<String, Object> Weather, String TableName){
		  String result = "INSERT INTO " + TableName + " (zipcode, city, description, temp, max_temp, "
		  		+ "min_temp, sunrise, sunset, refertime, lon, lat) " +"VALUES (" + 
				  Weather.get("zipcode") + "," + "\"" + Weather.get("name") + "\"" + "," + "\"" + Weather.get("weather") + "\"" + "," +
				  Weather.get("temp") + "," + Weather.get("temp_max") + "," + Weather.get("temp_min") + "," +
				  Weather.get("sunrise") + "," + Weather.get("sunset") + "," + Weather.get("refertime") + "," +
				  Weather.get("longitude") +"," + Weather.get("latitude") + ")";
		  return result;
	  }
	  
}
