package CapaDAOServicios;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import javax.print.*;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.ColorSupported;
import javax.xml.bind.annotation.XmlElementDecl.GLOBAL;



//La clase debe de implementar la impresión implements Printable

//clase pública que se ejecuta donde debe de estar el main que 
// llama a laotra clase.
public class cortehoja implements Printable
{
	private String stringToPrint;
	
	//Punto tipografico
	static Double pt = 0.352778;

    public cortehoja(String stringToPrint) {
        this.stringToPrint = stringToPrint;
    }
    
    public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
        if (pageIndex >= 1) {
            return Printable.NO_SUCH_PAGE;
        }
        
        ArrayList<String> StrDivision = new ArrayList<>();
        ArrayList<String> AuxDivision = new ArrayList<>();
        g.setColor(Color.black);
        g.setFont(new Font("Calibri", Font.BOLD, 11));
        g.translate(0, 0);
        int x = 12;
        int y = 0;
        
        FontMetrics fm=g.getFontMetrics();
        
        int inferior = 0;
    	int SupCaracter = 0;
    	double SizeSupCaracter = 0;
    	String saltolinea = "\n" , AuxString = "";
    	
    	
    	String[] extraccion = stringToPrint.split(saltolinea);
    	
    	for (String string : extraccion) {
			AuxDivision.add(string);
		}
    	
    	for (int i = 0; i < AuxDivision.size(); i++) {
    		if (fm.stringWidth(AuxDivision.get(i)) < 180) {
    			StrDivision.add(AuxDivision.get(i));
			}else {
				inferior = 0;
				SupCaracter = 0;
				
				while (SupCaracter < AuxDivision.get(i).length()) {
					do {
						AuxString = AuxDivision.get(i).substring(inferior, SupCaracter);
						SizeSupCaracter = fm.stringWidth(AuxString);
						SupCaracter++;
						if (SupCaracter  == AuxDivision.get(i).length()) {
							SizeSupCaracter = 180;
						}
					} while (SizeSupCaracter < 180);
					StrDivision.add(AuxString);
					inferior = SupCaracter - 1 ;
				}
				
			}
			
		}
                
        for (String string : StrDivision) {
        	
        	for (String FinalLine1 : string.split ("\n")) {
        		g.drawString(FinalLine1, x, y += g.getFontMetrics().getHeight()*(pt*2));
			}
		}
        
        return Printable.PAGE_EXISTS;
    }
    public static void printer(String printerData, String designatedPrinter)
        throws IOException, PrinterException {
        try {
        	PrintService designatedService = null;
            PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
            AttributeSet aset = new HashAttributeSet();
            aset = new HashAttributeSet();
            aset.add(ColorSupported.NOT_SUPPORTED);
            String printers = "";
            for (int i = 0; i < printServices.length; i++) {
                printers += " service found " + printServices[i].getName() + "\n";
            }
            for (int i = 0; i < printServices.length; i++) {
                System.out.println(" service found " + printServices[i].getName());
                if (printServices[i].getName().equalsIgnoreCase(designatedPrinter)) {
                    System.out.println("I want this one: " + printServices[i].getName());
                    designatedService = printServices[i];
                    break;
                }
            }
            Writer fw = new OutputStreamWriter(new FileOutputStream("printing.txt"), StandardCharsets.UTF_8);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter writer = new PrintWriter(bw);
            writer.print(printers);
            writer.close();
            
            PrinterJob pj = PrinterJob.getPrinterJob();
            PageFormat pf = pj.defaultPage();
            
//            https://www.programcreek.com/java-api-examples/?class=java.awt.print.Paper&method=setSize
            
            Paper paper = new Paper();
//            double paperWidth = getPreferences().getDouble(PROP_PAGE_WIDTH, paper.getWidth());
//            double paperHeight = getPreferences().getDouble(PROP_PAGE_HEIGHT, paper.getHeight());
            
            paper.setSize(226.772, 2000);
            paper.setImageableArea(0, 0, 226.772, 2000);
            
            pf.setPaper(paper);
            
            pj.setPrintService(designatedService);
            Printable painter;

            // Specify the painter
            painter = new cortehoja(printerData);
            pj.setPrintable(painter,pf);
            pj.print();
            

        } catch (PrinterException e) {
            Writer fw = new OutputStreamWriter(new FileOutputStream("log.txt", true), StandardCharsets.UTF_8);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter writer = new PrintWriter(bw);
            e.printStackTrace(writer);
            writer.close();
        }
    }
}