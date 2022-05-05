package pl.jakub.paragon;

import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.output.PrinterOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.print.PrintService;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Slf4j
@SpringBootApplication
public class ParagonApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParagonApplication.class, args);
		//String printerName = "POS-58-Series";

	}

}
