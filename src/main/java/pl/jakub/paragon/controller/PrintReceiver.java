package pl.jakub.paragon.controller;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;

@RestController
public class PrintReceiver {

    @CrossOrigin
    @RequestMapping(
            value = "/print",
            method = RequestMethod.POST)
    public String print(@RequestBody Map<String, Object> payload){
        System.out.println(payload);
        Object data = payload.get("data");
        System.out.println(data.getClass());
        ArrayList<ArrayList<Object>> arrayOfRanges = null;
        if(data instanceof ArrayList){
            arrayOfRanges = (ArrayList<ArrayList<Object>>) data;

        }
        System.out.println(arrayOfRanges.get(0).getClass());
        System.out.println(arrayOfRanges.get(0).get(0).getClass());
        System.out.println(arrayOfRanges.get(0).get(1).getClass());
        System.out.println(arrayOfRanges.get(1).get(1).getClass());


        return "ok";
    }
}
