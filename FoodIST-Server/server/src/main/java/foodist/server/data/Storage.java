package foodist.server.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.google.protobuf.ByteString;

import foodist.server.grpc.contract.Contract.Menu;
import foodist.server.grpc.contract.Contract.PhotoReply;

public class Storage {
	
	private static final String BASE_DIR = "photos";
	
	private static AtomicInteger atomicInteger = new AtomicInteger(0);
	private static ConcurrentHashMap<String, HashMap<String, Menu>> foodServiceMap = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, String> photoPathMap = new ConcurrentHashMap<>();
	
	public synchronized static void addMenu(String foodService, Menu menu) {
	    HashMap<String, Menu> menuMap = foodServiceMap.get(foodService);
	      
	    if(menuMap!=null) {
	    	menuMap.put(menu.getName(), menu);
	    	foodServiceMap.put(foodService, menuMap);         
	    } 
	    else {
	    	HashMap<String, Menu> newMenuMap = new HashMap<>();
	    	newMenuMap.put(menu.getName(), menu);
	    	foodServiceMap.put(foodService, newMenuMap);         
	    } 
	}
	
	public synchronized static HashMap<String, Menu> getMenuMap(String foodService) {
		foodServiceMap.putIfAbsent(foodService, new HashMap<>());
		return foodServiceMap.get(foodService);
	}
	
	public synchronized static void purge() {	
		System.out.print("Cleaning up server persistent memory... ");
		
		Iterator<Entry<String, HashMap<String, Menu>>> iterator = foodServiceMap.entrySet().iterator();
		while (iterator.hasNext()) {
	        HashMap.Entry<String, HashMap<String, Menu>> pair = iterator.next();
	        foodServiceMap.put(pair.getKey(), null);
	        iterator.remove(); 
	    }
		
		File directory = new File(BASE_DIR);		
		for(String filename : directory.list()) {
			if(filename.equals("test")) {
				continue;
			}
			else {
				try {
					FileUtils.forceDelete(new File(BASE_DIR + "/" + filename));
				} catch (IOException e) {
					System.out.println("Could not delete file: \"" + BASE_DIR + "/" + filename + "\".");
				}
			}
		}
		System.out.println("Sucess!");
	}
	
	public synchronized static void addPhotoToMenu(String photoName, String foodServiceName, String menuName, ByteString photoByteString) throws StorageException {	    	    		
		
		String foodServicePath = getFoodServiceDir(foodServiceName, menuName);
		createPhotoDir(foodServicePath);	
		String photoId = atomicInteger.incrementAndGet() + "." + photoName.split("\\.")[1];
	    String photoPath = foodServicePath + photoId;
	    try{
	        FileOutputStream out=new FileOutputStream(photoPath);	        
	        out.write(photoByteString.toByteArray());
	        out.close(); 
	        photoPathMap.put(photoId, photoPath);
	    }
	    catch (IOException ioe){
	    	throw new StorageException("Could not write file: \"" + photoPath + "\"");
	    }
	}
	
	public synchronized static Menu fetchMenuPhotos(String foodServiceName, Menu menu) {
		String foodServicePath = getFoodServiceDir(foodServiceName, menu.getName());
		
		File directory = new File(foodServicePath);
		
		Menu.Builder menuBuilder = Menu.newBuilder();
		menuBuilder.setName(menu.getName());
		menuBuilder.setPrice(menu.getPrice());
		menuBuilder.setLanguage(menu.getLanguage());
		menuBuilder.setTranslatedName(menu.getTranslatedName());

	    if (directory.exists()){
	    	    
	        for(String filename : directory.list()) {
	        	menuBuilder.addPhotoId(filename);	        	
	        }	        
	    }	
	    
	    return menuBuilder.build();
	}
	
	public synchronized static byte[] fetchPhotoBytes(String photoId) throws StorageException {
		String photoPath = photoPathMap.get(photoId);		
		
		try {
			File file = new File(photoPath);		
			
			if (file.exists()){				
				InputStream inputStream = FileUtils.openInputStream(file);
		    	byte[] bytes = IOUtils.toByteArray(inputStream);
		    	return bytes;								
		    }	
		} catch(IOException ioe) {
			throw new StorageException("Could not fetch photograph \"" + photoId + "\"");
		} catch(NullPointerException npe) {
			throw new StorageException("Photograph \"" + photoId + "\" does not exist");
		}
				
		return null;
	}	
	
	public synchronized static PhotoReply fetchPhotoIds(int quantity) {		
		PhotoReply.Builder builder = PhotoReply.newBuilder();
		
		Iterator<Entry<String, HashMap<String, Menu>>> outer = foodServiceMap.entrySet().iterator();
		
		while(outer.hasNext()) {
			Entry<String, HashMap<String, Menu>> mapping = outer.next();
			String foodService = mapping.getKey();
			Iterator<String> inner = mapping.getValue().keySet().iterator();		
			while(inner.hasNext()) {
				String menu = inner.next();
				
				String[] photoIds = new File(BASE_DIR + 
						File.separator + foodService + File.separator 
						+ menu + File.separator).list();
				
				if(photoIds!=null) {
					for(int i = 0; i<photoIds.length && i<quantity; i++) {					
						builder.addPhotoID(photoIds[i]);
					}
				}					
			}			
		}			
		
		return builder.build();
	}
	
	public synchronized static void createPhotoDir(String photoPath) {			
		File directory = new File(photoPath);
	    if (!directory.exists()){
	        directory.mkdirs();	        
	    }	
	}
	
	public synchronized static String getFileFromPath(String path) {
		String[] split_path = path.split("/");
		int position = split_path.length - 1;
		return split_path[position];
	}
	
	private synchronized static String getFoodServiceDir(String foodServiceName, String menuName) {
		StringBuilder buildPath = new StringBuilder();		
		buildPath.append(BASE_DIR).append("/").append(foodServiceName).append("/").append(menuName).append("/");
		return buildPath.toString();
	}
}
