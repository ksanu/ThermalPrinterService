package pl.jakub.paragon;

import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.output.PrinterOutputStream;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.print.PrintService;
import java.io.IOException;

@SpringBootTest
class ParagonApplicationTests {

	@Test
	void contextLoads() {
	}
	@Test
	void listPrinters() {
		String[] printServicesNames = PrinterOutputStream.getListPrintServicesNames();
		for(String printServiceName: printServicesNames){
			System.out.println(printServiceName);
		}

		/*
		POS-58-Series
		OneNote for Windows 10
		OneNote (Desktop)
		Microsoft XPS Document Writer
		Microsoft Print to PDF
		Fax
		Canon MG5700 series Printer (Kopia 1)
		Canon MG5700 series Printer
		 */
	}

	@Test
	public void printByName() throws IOException {
		String printerName = "POS-58-Series";
		PrintService printService = PrinterOutputStream.getPrintServiceByName(printerName);
		PrinterOutputStream printerOutputStream = new PrinterOutputStream(printService);
		EscPos escpos = new EscPos(printerOutputStream);
		//escpos.writeLF("Hello world");
		//escpos.feed(5).cut(EscPos.CutMode.FULL);
		escpos.close();
	}
}
