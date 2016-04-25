package basilmusa.bs3;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Charsets;

public class Main {
	
	private static final String TEMPLATE_FILE_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + 
			"<templates>";
	
	private static final String TEMPLATE_FILE_FOOTER = "</templates>";
	
	private static final String TEMPLATE_SECTION_START = "<template autoinsert=\"true\" context=\"html_all\" deleted=\"false\" description=\"{{snippet_name}}\" enabled=\"true\" name=\"{{snippet_name}}\">";
	private static final String TEMPLATE_SECTION_END = "</template>";
	
	public static void main(String[] args) {
		final String CURRENT_DIRECTORY = System.getProperty("user.dir");
		
		final Map<String, String> xmlEscapeChars = new HashMap<>();
		xmlEscapeChars.put("<", "&lt;");
		xmlEscapeChars.put(">", "&gt;");
		xmlEscapeChars.put("&", "&amp;");
	
		final StringBuilder stringBuilder = new StringBuilder(1024 * 1024 * 2);
		stringBuilder.append(TEMPLATE_FILE_HEADER);
		
		try {
		    Path startPath = Paths.get(CURRENT_DIRECTORY);
		    Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
		        @Override
		        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
		            return FileVisitResult.CONTINUE;
		        }

		        @Override
		        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) 
		        {
		        	if (!file.toString().endsWith(".html")) {
		        		return FileVisitResult.CONTINUE;
		        	}
		        	
		        	final String fullFilePath = file.toString();
		        	final String fileName = file.getFileName().toString();
		        	final String snippetName = fileName.replaceAll(".html$", "");
		        	System.out.println(snippetName);
		        	
		        	try {
						String fileContents = com.google.common.io.Files.toString(file.toFile(), Charsets.UTF_8);
						
						Map<String, String> needle2replacement = new HashMap<>();
						needle2replacement.put("{{snippet_name}}", snippetName);
						stringBuilder.append(Strtr.replaceStringUsingMap(TEMPLATE_SECTION_START, needle2replacement));
						stringBuilder.append(Strtr.replaceStringUsingMap(fileContents, xmlEscapeChars));
						stringBuilder.append(TEMPLATE_SECTION_END);
						
						System.out.println("File: " + file.toString());
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(1);
					}
		                
		            return FileVisitResult.CONTINUE;
		        }

		        @Override
		        public FileVisitResult visitFileFailed(Path file, IOException e) {
		            return FileVisitResult.CONTINUE;
		        }
		    });
		    
		    stringBuilder.append(TEMPLATE_FILE_FOOTER);
		    
		    // Finished everything
		    com.google.common.io.Files.write(stringBuilder.toString(), new File(CURRENT_DIRECTORY + File.separator + "templates-import-to-eclipse.xml"), Charsets.UTF_8);
		    System.out.println(stringBuilder.toString());
		    
		} catch (IOException e) {
		    e.printStackTrace();
		}
		
	}
}
