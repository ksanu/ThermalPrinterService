package pl.jakub.paragon.service;

import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.output.PrinterOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.jakub.paragon.config.ConfigProperties;

import javax.print.PrintService;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;

import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.EscPosConst;
import com.github.anastaciocintra.escpos.Style;
import com.github.anastaciocintra.output.PrinterOutputStream;
import com.github.anastaciocintra.escpos.barcode.QRCode;

import java.io.IOException;
import java.util.List;

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



    ArrayList<ArrayList<Object>> discardEmpty(ArrayList<ArrayList<Object>> data){
        ArrayList<ArrayList<Object>> result = new ArrayList<>();
        List<String> forbidden = List.of("false", "true", "Czy zrealizowane?", "Czy odebrane?", "Uwagi:", "Lista produktów:", "Cena:", "Imię i Nazwisko:");
        for(ArrayList<Object> range : data) {
            if(!(range.size() >= 1 && forbidden.contains(range.get(0).toString()) || range.size() >= 2 && forbidden.contains(range.get(1).toString()))) {
                if (range.size() >= 1 && range.get(0).toString().length() > 0) {
                    result.add(range);
                } else if (range.size() >= 2 && range.get(1).toString().length() > 0) {
                    result.add(range);
                }
            }
        }
        log.info("non empty data: {}", result);
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
        log.info("Headers: {}", result);
        return result;
    }

    ArrayList<ArrayList<Object>> extractValues(ArrayList<ArrayList<Object>> data){
        ArrayList<ArrayList<Object>> result = new ArrayList<>();
        for(ArrayList<Object> range : data){
            if(range.size() > 1) {
                if (range.get(1) instanceof Integer) {
                    ArrayList<Object> newRange = new ArrayList<>();
                    newRange.add(range.get(0));
                    newRange.add(BigDecimal.valueOf((Integer) range.get(1)));
                    result.add(newRange);
                }
                if (range.get(1) instanceof Float) {
                    ArrayList<Object> newRange = new ArrayList<>();
                    newRange.add(range.get(0));
                    newRange.add(BigDecimal.valueOf((Float) range.get(1)));
                    result.add(newRange);
                }
                if (range.get(1) instanceof Double) {
                    ArrayList<Object> newRange = new ArrayList<>();
                    newRange.add(range.get(0));
                    newRange.add(BigDecimal.valueOf((Double) range.get(1)));
                    result.add(newRange);
                }
            }
        }
        log.info("Products: {}", result);
        return result;
    }

    BigDecimal sumValues(ArrayList<ArrayList<Object>> extractedValues){
        BigDecimal sum = BigDecimal.valueOf(0);
        for(ArrayList<Object> range : extractedValues){
            BigDecimal productCost = (BigDecimal) range.get(1);
            sum = sum.add(productCost);
        }
        log.info("Products total costs: {}", sum);
        return sum;
    }

    private EscPos printHeaders(EscPos escpos, ArrayList<ArrayList<String>> headers) throws IOException {
        Style subtitle = new Style()
                .setFontSize(Style.FontSize._1, Style.FontSize._1)
                ;
        Style boldSubtitle = new Style()
                .setFontSize(Style.FontSize._2, Style.FontSize._1)
                .setBold(true);

        for(ArrayList<String> headerPair : headers){
            String left = headerPair.get(0);
            if(!(left.endsWith(":") || left.endsWith(": ")))
                left += ": ";
            String right = headerPair.get(1);
            if(left.length() + right.length() <= (properties.getMaxCharsInLine()/2)){
                escpos.writeLF(subtitle, left)
                        .writeLF(boldSubtitle, right);
            }else{
                escpos.writeLF(subtitle, left);
                boolean isFirst = true;
                for(String substring : right.split(" ")){
                    if(isFirst){
                        printOneLine(escpos, substring, "", boldSubtitle, 2);
                        isFirst=false;
                    }else {
                        printOneLine(escpos, "", substring, boldSubtitle, 2);
                    }
                }

            }

        }
        return escpos;
    }
    private EscPos printProducts(EscPos escpos, ArrayList<ArrayList<Object>> extractedValues) throws IOException {
        Style normalRight = new Style(escpos.getStyle()).setJustification(EscPosConst.Justification.Right);
        Style normalLeft = new Style(escpos.getStyle()).setJustification(EscPosConst.Justification.Left_Default);

        for(ArrayList<Object> productAndCost : extractedValues){
            printOneLineProduct(escpos, productAndCost.get(0).toString(),
                    String.format("%.2f zł",(BigDecimal)productAndCost.get(1)),
                    normalLeft);
        }

        return escpos;
    }
    private void printOneLineProduct(EscPos escpos, String left, String right, Style style) throws IOException {
        int spacesNum = properties.getMaxCharsInLine() - left.length() - right.length();
        StringBuilder spaces = new StringBuilder();
        while(spaces.length() < spacesNum){
            spaces.append(" ");
        }
        escpos.writeLF(style, left + spaces + right);
    }

    private void printOneLine(EscPos escpos, String left, String right, Style style, int charWidth) throws IOException {
        int spacesNum = (properties.getMaxCharsInLine()/charWidth) - left.length() - right.length();
        StringBuilder spaces = new StringBuilder();
        while(spaces.length() < spacesNum){
            spaces.append(" ");
        }
        escpos.write(style, left + spaces + right);
    }

    public void print(ArrayList<ArrayList<Object>> data){
        if(data == null) return;

        //this call is slow, try to use it only once and reuse the PrintService variable.
        PrintService printService = getPrintService();
        ArrayList<ArrayList<Object>> notEmptyData = discardEmpty(data);
        ArrayList<ArrayList<String>> headers = extractHeaders(notEmptyData);
        ArrayList<ArrayList<Object>> values = extractValues(notEmptyData);
        BigDecimal valuesSum = sumValues(values);


        EscPos escpos;
        try {
            escpos = new EscPos(new PrinterOutputStream(printService));

            Style boldRight = new Style(escpos.getStyle())
                    .setJustification(EscPosConst.Justification.Right)
                    .setBold(true);
            Style boldLeft = new Style(escpos.getStyle())
                    .setJustification(EscPosConst.Justification.Left_Default)
                    .setBold(true);

            Style title = new Style()
                    .setFontSize(Style.FontSize._2, Style.FontSize._2)
                    .setJustification(EscPosConst.Justification.Center);

            Style subtitle = new Style(escpos.getStyle())
                    //.setBold(true)
                    .setUnderline(Style.Underline.OneDotThick);
            Style bold = new Style(escpos.getStyle())
                    .setBold(true);

            //escpos.setPrinterCharacterTable(76); //PC3843 (Polish)
            //32 characters width

            //escpos.setCharsetName("UTF-16");
            log.info("Default charset name: {}", escpos.getDefaultCharsetName());

            escpos.setCharsetName("cp852");
            escpos.setPrinterCharacterTable(18);


            escpos.writeLF(title,properties.getTitle())
                    .feed(1);
            printHeaders(escpos,headers)
                    .feed(1);
            escpos.writeLF(subtitle, "Zamówienie:").feed(1);
            printProducts(escpos, values)
                    .writeLF("--------------------------------");
            printOneLineProduct(escpos, "SUMA: ",String.format("%.2f zł", valuesSum), boldLeft);
            escpos.writeLF("--------------------------------")
                    .feed(1);

            //qr code:
            QRCode qrcode = new QRCode();

            escpos.writeLF(properties.getQrIntro());
            escpos.feed(1);
            qrcode.setSize(7);
            qrcode.setJustification(EscPosConst.Justification.Center);
            escpos.write(qrcode, properties.getQrData());
            escpos.feed(4);


            //escpos.cut(EscPos.CutMode.FULL); doesn't work on our printer
            escpos.close();

        } catch (IOException ex) {
            log.error("Exception while printing: {}", ex.getMessage());
            ex.printStackTrace();
        }

    }
}
