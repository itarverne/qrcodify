package com.itarverne.qrcode;

import static org.refcodes.console.ConsoleSugar.and;
import static org.refcodes.console.ConsoleSugar.helpSwitch;
import static org.refcodes.console.ConsoleSugar.optional;
import static org.refcodes.console.ConsoleSugar.xor;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

import org.refcodes.console.AmbiguousArgsException;
import org.refcodes.console.ArgsParser;
import org.refcodes.console.ArgsParserImpl;
import org.refcodes.console.Condition;
import org.refcodes.console.Operand;
import org.refcodes.console.Option;
import org.refcodes.console.ParseArgsException;
import org.refcodes.console.StringOptionImpl;
import org.refcodes.console.SuperfluousArgsException;
import org.refcodes.console.Switch;
import org.refcodes.console.UnknownArgsException;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * Program to generate QRCode with custom logo 
 * 
 * Inspired by zxing library : https://github.com/zxing/zxing
 * 
 * @author RIGAUDIE David http://itarverne.com
 * @version 1.0.0
 *
 */
class Generator {

    private final static String APP_NAME = Generator.class.getSimpleName();
    private final static String APP_DESC = "QRCode generator allow to generate QRCode with custom image inside";
    private final static String APP_LICENCE = "Licensed under MIT";
    private final static String APP_COPYRIGHT = "Copyright (c) by ITArverne.com, Volvic, France";
    
	private static String DIR ;
    private static String EXT = "png";
    private static String LOGO = "http://itarverne.com/logo.png";    
    private static int WIDTH_LOGO = 100;
    private static int HEIGHT_LOGO = 100;
    private static String CONTENT = "";
    private static int COLOR = Colors.BLACK.getArgb();
    private final int WIDTH = 300;
    private final int HEIGHT = 300;
	    
    /**
     * The application boostrap 
     * @param args The parameter of the program
     * @throws UnknownArgsException
     * @throws AmbiguousArgsException
     * @throws SuperfluousArgsException
     * @throws ParseArgsException
     * @throws IOException
     * @throws WriterException
     */
	public static void main (String[] args) throws UnknownArgsException, AmbiguousArgsException, SuperfluousArgsException, ParseArgsException, IOException, WriterException {		
		System.out.printf("%s Generating QRCode... \n", "[START]");
		
		List<? extends Operand<?>> result = manageInput(args);
			    
		if(result != null) {
			Generator generator = new Generator();
			String path = generator.generate();
			System.out.printf("%s Generated QRCode in %s", "[END]", path);
		} else {
			System.out.printf("%s NOT generated QRCode !", "[END]");
		}
	}
	
	/**
	 * Manage the parameter of the program
	 * @param args The parameter of the program
	 * @return The parameter result after analyze  
	 * @throws UnknownArgsException
	 * @throws AmbiguousArgsException
	 * @throws SuperfluousArgsException
	 * @throws ParseArgsException
	 */
	public static List<? extends Operand<?>> manageInput(String[] args) throws UnknownArgsException, AmbiguousArgsException, SuperfluousArgsException, ParseArgsException {
		Switch theHelp = helpSwitch( "Shows this help" );
		
		Option<String> path = new StringOptionImpl("-p", "--path", "path", "The path location to create qrcode image");
		Option<String> content = new StringOptionImpl( "-c", "--content", "content", "The QRCode content" );
		Option<String> ext = new StringOptionImpl( "-e", "--ext", "extension", "The extension of the image" );
		Option<String> width = new StringOptionImpl( "-wl", "--width", "width", "The logo width added" );
		Option<String> height = new StringOptionImpl( "-hl", "--height", "height", "The logo height added" );
		Option<String> background = new StringOptionImpl( "-b", "--background", "background", "The QRCode background color");
		
		Condition root = xor( 
	       and( 
	         path, content, optional( ext ), optional( width ), optional( height ), optional( background )
	       ),
	       theHelp
	    );
		 
		ArgsParser theArgsParser = new ArgsParserImpl( root );
		theArgsParser.setName(APP_NAME);
		theArgsParser.setDescription(APP_DESC);
		theArgsParser.setLicenseNote(APP_LICENCE);
		theArgsParser.setCopyrightNote(APP_COPYRIGHT);
		 		 
		List<? extends Operand<?>> theResult = null;
		try {
			theResult = theArgsParser.evalArgs(args);
		} catch (UnknownArgsException e) {
			theArgsParser.printUsage();
			throw new UnknownArgsException(args, e.getMessage());
		}
		   
		if(theResult.size() > 4) {
			 theArgsParser.printUsage();
			 throw new UnknownArgsException(args, "Invalid arguments number");
		}
		else if(theHelp.getValue() == true)
		{
			theArgsParser.printHelp();
			 return null;
		}
		
	    if(path.getValue().charAt(path.getValue().length()-1) == File.separatorChar)
	    	DIR = path.getValue();
	    else
	    	DIR = path.getValue() + File.separatorChar;
	    System.out.printf("%s Setting directory to put image : %s \n", "[DEBUG]", DIR);

	    CONTENT = content.getValue();
	    System.out.printf("%s Setting QRCode content : %s \n", "[DEBUG]", CONTENT);
	    
	    EXT = ext.getValue();
	    System.out.printf("%s Setting extension image : %s \n", "[DEBUG]", EXT);
	    
	    if(width.getValue() != null) {
	    	WIDTH_LOGO = Integer.valueOf(width.getValue());
	    }
	    System.out.printf("%s Setting width image : %s \n", "[DEBUG]", WIDTH_LOGO);
	    
	    if(height.getValue() != null) {
		    HEIGHT_LOGO = Integer.valueOf(height.getValue());
	    }
	    System.out.printf("%s Setting height image : %s \n", "[DEBUG]", HEIGHT_LOGO);
	    
	    if(background.getValue() != null && !Arrays.asList(Colors.values()).toString().contains(background.getValue().toUpperCase())) {
			 theArgsParser.printUsage();
			 throw new UnknownArgsException(args, "Invalid arguments color, available [BLUE, RED, PURPLE, ORANGE, WHITE, BLACK]");			 
	    }
	    else if(background.getValue() != null) {
	    	COLOR = Colors.valueOf(background.getValue().toUpperCase()).getArgb();
	    }
	    System.out.printf("%s Setting QRCode color code : %s \n", "[DEBUG]", COLOR);
	    
		return theResult;
	}

	/**
	 * Generate the QRCode
	 * @return The QRCode path generated
	 * @throws IOException
	 * @throws WriterException
	 */
     private String generate() throws IOException, WriterException {
         // Create new configuration that specifies the error correction
         Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
         hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

         QRCodeWriter writer = new QRCodeWriter();
         BitMatrix bitMatrix = null;
         ByteArrayOutputStream os = new ByteArrayOutputStream();

         // init directory
         cleanDirectory(DIR);
         initDirectory(DIR);
         
         // Create QRCode with the url as content
         bitMatrix = writer.encode(CONTENT, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, hints);

         // Load QR image
         BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix, getMatrixConfig());

         // Load logo image
         BufferedImage overly = getOverly(LOGO);

         // Calculate the delta height and width between QR code and logo
         int deltaHeight = qrImage.getHeight() - overly.getHeight();
         int deltaWidth = qrImage.getWidth() - overly.getWidth();

         // Initialize combined image
         BufferedImage combined = new BufferedImage(qrImage.getHeight(), qrImage.getWidth(), BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = (Graphics2D) combined.getGraphics();

         // Write QRCode to new image at position 0/0
         g.drawImage(qrImage, 0, 0, null);
         g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

         // Write logo into combine image at position (deltaWidth / 2) and
         // (deltaHeight / 2). Background: Left/Right and Top/Bottom must be
         // the same space for the logo to be centered
         g.drawImage(overly, (int) Math.round(deltaWidth / 2), (int) Math.round(deltaHeight / 2), null);

         // Write combined image as PNG to OutputStream
         ImageIO.write(combined, EXT, os);
         
         // Store Image
         String path = DIR + generateRandoTitle(new Random(), 9) + '.' + EXT;
         Files.copy(
        		 new ByteArrayInputStream(os.toByteArray()),
        		 Paths.get(path), 
        		 StandardCopyOption.REPLACE_EXISTING
         );
         
         return path;
     }

     /**
      * Get the image from url
      * @param LOGO The url of the logo
      * @return The image with new size
      * @throws IOException
      */
     private BufferedImage getOverly(String LOGO) throws IOException {
         URL url = new URL(LOGO);
         BufferedImage image = ImageIO.read(url);
         return resize(image, HEIGHT_LOGO, WIDTH_LOGO);         
     }
     
     /**
      * Resize the logo to center on QRCode
      * @param img The image to resize 
      * @param height The new height to apply
      * @param width The new width to apply
      * @return
      */
     private static BufferedImage resize(BufferedImage img, int height, int width) {
         Image tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
         BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g2d = resized.createGraphics();
         g2d.drawImage(tmp, 0, 0, null);
         g2d.dispose();
         return resized;
     }

     /**
      * Create the folder to put the image
      * @param DIR The path of directory
      * @throws IOException
      */
     private void initDirectory(String DIR) throws IOException {
         Files.createDirectories(Paths.get(DIR));
     }

     /**
      * Delete and create the directory 
      * @param DIR The path to clean
      * @throws IOException
      */
     private void cleanDirectory(String DIR) throws IOException {
         Files.walk(Paths.get(DIR), FileVisitOption.FOLLOW_LINKS)
                 .sorted(Comparator.reverseOrder())
                 .map(Path::toFile)
                 .forEach(File::delete);
     }

     /**
      * Apply the square and background color of QRCode 
      * @return MatrixToImageConfig
      */
     private MatrixToImageConfig getMatrixConfig() {
         return new MatrixToImageConfig(Colors.WHITE.getArgb(), COLOR);
     }

     /**
      * Generate random title for image
      * 
      * @param random The random number
      * @param length The length of title
      * @return The ramdom name for title
      */
     private String generateRandoTitle(Random random, int length) {
         return random.ints(48, 122)
                 .filter(i -> (i < 57 || i > 65) && (i < 90 || i > 97))
                 .mapToObj(i -> (char) i)
                 .limit(length)
                 .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                 .toString();
     }

}
	