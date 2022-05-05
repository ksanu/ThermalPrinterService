package pl.jakub.paragon.service;

import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.output.PrinterOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.jakub.paragon.ParagonApplication;
import pl.jakub.paragon.config.ConfigProperties;

import javax.print.PrintService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Slf4j
@Service
public class ThermalPrinterService {

    @Autowired
    ConfigProperties properties;

    private PrintService printService = null;

    public PrintService getPrintService(){
        if(printService != null)
            return printService;
        printService = PrinterOutputStream.getPrintServiceByName(properties.getPrinterName());
        return printService;
    }
    public void printInfo(){
        //this call is slow, try to use it only once and reuse the PrintService variable.
        PrintService printService = getPrintService();
        log.info("Get printer by name: {}", printService.getName());
        try {
            PrinterOutputStream printerOutputStream = new PrinterOutputStream(printService);
            EscPos escpos = new EscPos(printerOutputStream);
            log.info("Printing receipt");
            escpos.writeLF("test");
            // do not forget to close...
            escpos.close();

        } catch (IOException ex) {
            Logger.getLogger(ParagonApplication.class.getName()).log(Level.SEVERE, null, ex);
        }

    }


    ArrayList<ArrayList<Object>> discardEmpty(ArrayList<ArrayList<Object>> data){
        ArrayList<ArrayList<Object>> result = new ArrayList<>();
        for(ArrayList<Object> range : data) {
            if(range.size() < 1) break;
            if(range.get(0).toString().length() > 0) result.add(range);
            if(range.size() > 1 && range.get(1).toString().length() > 0) result.add(range);
        }

        return result;
    }
    /*
    Headers are text values that should be printed first.
     */
    ArrayList<ArrayList<String>> extractHeaders(ArrayList<ArrayList<Object>> data){
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        for(ArrayList<Object> range : data){
            if(range.size() > 1 && range.get(1) instanceof String){
                ArrayList<String> newRange = new ArrayList<>();
                newRange.add(range.get(0).toString());
                newRange.add(range.get(1).toString());
                result.add(newRange);
            }
        }

        return result;
    }

    ArrayList<ArrayList<Object>> extractValues(ArrayList<ArrayList<Object>> data){
        ArrayList<ArrayList<Object>> result = new ArrayList<>();
        for(ArrayList<Object> range : data){
            if(range.size() > 1)
                if(range.get(1) instanceof Integer || range.get(1) instanceof Float || range.get(1) instanceof Double){
                    ArrayList<Object> newRange = new ArrayList<>();
                    newRange.add(range.get(0));
                    newRange.add(range.get(1));
                    result.add(newRange);
                }
        }

        return result;
    }

    Float sumValues(ArrayList<ArrayList<Object>> extractedValues){
        Float sum = 0.0F;
        for(ArrayList<Object> range : extractedValues){
            sum += (Float)range.get(1);
        }
        return sum;
    }



    public void print(ArrayList<ArrayList<Object>> data){
        //this call is slow, try to use it only once and reuse the PrintService variable.
        PrintService printService = getPrintService();
        ArrayList<ArrayList<Object>> notEmptyData = discardEmpty(data);
        ArrayList<ArrayList<String>> headers = extractHeaders(notEmptyData);
        ArrayList<ArrayList<Object>> values = extractValues(notEmptyData);
        Float valuseSum = sumValues(values);

    }
}
