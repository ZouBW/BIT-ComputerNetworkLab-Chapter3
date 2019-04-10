package tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ReadIniFile {
	
	private static String path = null;
	
	public ReadIniFile(String path) {
		this.path = path;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	public static Map<String, String> readFile() throws IOException{
		InputStream in = new FileInputStream(new File(path));
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		Properties props = new Properties();
		props.load(br);
		Map<String,String> map = new HashMap<String, String>();
		for(Object s: props.keySet()) {
			map.put(s.toString(), props.getProperty(s.toString()));
		}
		return map;
	}
}
