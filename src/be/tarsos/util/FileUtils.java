package be.tarsos.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {
	
	private FileUtils(){
		
	}
	
	private static String getLibraryName(){
		
		String os = System.getProperty("os.name").toLowerCase();
		String arch = System.getProperty("os.arch");
		// binary is for x86 or amd64 or x86_64 architectures (
		// see http://lopica.sourceforge.net/os.html
		if(os.contains("linux") && (arch.contains("86") || arch.contains("64"))){
			return "JVM/librubberband-jni.so";
		} else if(os.contains("mac")){
			return "JVM/librubberband-jni.dylib";
		} else if(os.contains("windows") && arch.contains("86")){
			return "JVM/rubberband-jni_win32.dll";
		} else if(os.contains("windows") && arch.contains("64")){
			return "JVM/rubberband-jni_win64.dll";
		}else{
			throw new Error("Unknown architecture! No rubberband JNI library available for your platform!");
		}
	}
	

	 /**
     * Loads library from current JAR archive
     * 
     * The file from JAR is copied into system temporary directory and then loaded. The temporary file is deleted after exiting.
     * Method uses String as filename because the pathname is "abstract", not system-dependent.
     * 
     * @param filename The filename inside JAR as absolute path (beginning with '/'), e.g. /package/File.ext
     * @throws IOException If temporary file creation or read/write operation fails
     * @throws IllegalArgumentException If source file (param path) does not exist
     * @throws IllegalArgumentException If the path is not absolute or if the filename is shorter than three characters (restriction of {@see File#createTempFile(java.lang.String, java.lang.String)}).
     */
    public static void loadLibrary() throws IOException {
 
    	String path = getLibraryName();
    	
    	if(new File("jni/" + path).exists()){
    		//running from source
    		path = "jni/" + path;
    	}else{
    		//running from jar
    		path = "/" + path;
    	}
 
        // Obtain filename from path
        String[] parts = path.split("/");
        String filename = (parts.length > 1) ? parts[parts.length - 1] : null;
 
        // Split filename to prexif and suffix (extension)
        String prefix = "";
        String suffix = null;
        if (filename != null) {
            parts = filename.split("\\.", 2);
            prefix = parts[0];
            suffix = (parts.length > 1) ? "."+parts[parts.length - 1] : null; // Thanks, davs! :-)
        }
 
        // Check if the filename is okay
        if (filename == null || prefix.length() < 3) {
            throw new IllegalArgumentException("The filename has to be at least 3 characters long.");
        }
 
        // Prepare temporary file
        File temp = File.createTempFile(prefix, suffix);
        temp.deleteOnExit();
 
        if (!temp.exists()) {
            throw new FileNotFoundException("File " + temp.getAbsolutePath() + " does not exist.");
        }
 
        // Prepare buffer for data copying
        byte[] buffer = new byte[1024];
        int readBytes;
 
        // Open and check input stream
        InputStream is;
        
        if(new File(path).exists()){
    		//running from source
        	is = new FileInputStream(new File(path));
    	}else{
    		//running from jar
    		is = FileUtils.class.getResourceAsStream(path);
    	}
        
        if (is == null) {
            throw new FileNotFoundException("File " + path + " was not found inside JAR.");
        }
 
        // Open output stream and copy data between source file in JAR and the temporary file
        OutputStream os = new FileOutputStream(temp);
        try {
            while ((readBytes = is.read(buffer)) != -1) {
                os.write(buffer, 0, readBytes);
            }
        } finally {
            // If read/write fails, close streams safely before throwing an exception
            os.close();
            is.close();
        }
 
        // Finally, load the library
        System.load(temp.getAbsolutePath());
    }
}
