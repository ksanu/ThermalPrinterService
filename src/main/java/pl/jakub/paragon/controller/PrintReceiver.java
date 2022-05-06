package pl.jakub.paragon.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pl.jakub.paragon.service.ThermalPrinterService;

import java.util.ArrayList;
import java.util.Map;

@Slf4j
@RestController
public class PrintReceiver {

    @Autowired
    ThermalPrinterService thermalPrinterService;


    @CrossOrigin
    @RequestMapping(
            value = "/print",
            method = RequestMethod.POST)
    public String print(@RequestBody Map<String, Object> payload){
        log.info("Received payload: {}", payload);
        Object data = payload.get("data");
        System.out.println(data.getClass());
        ArrayList<ArrayList<Object>> arrayOfRanges = null;
        if(data instanceof ArrayList){
            arrayOfRanges = (ArrayList<ArrayList<Object>>) data;

        }

        thermalPrinterService.print(arrayOfRanges);


        return "ok";
    }
}
