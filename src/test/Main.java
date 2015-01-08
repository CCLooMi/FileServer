package test;

import java.net.URL;

public class Main {
	public URL getpath(){
		String fname=this.getClass().getResource("../com/config/config.properties").getPath().substring(1);
		System.out.println(fname);
		return this.getClass().getResource("../com/config/config.properties");
	}
	public static void main(String[] args) {
//		String userDir=System.getProperties().getProperty("user.dir");
//		Properties properties=new Properties();
//		InputStream inStream;
//		try {
//			inStream = new FileInputStream(userDir+"/config.properties");
//			properties.load(inStream);
//			inStream.close();
//			String savePath=properties.getProperty("savePath");
//			int blobSize=Integer.valueOf(properties.getProperty("blobSize"));
//			System.out.println(savePath+blobSize);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
		new Main().getpath();
//		Properties sysP=System.getProperties();
//		System.out.println(sysP);
//		sysP.list(System.out);
	}
}
