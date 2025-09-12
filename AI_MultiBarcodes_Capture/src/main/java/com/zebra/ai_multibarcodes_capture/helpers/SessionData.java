package com.zebra.ai_multibarcodes_capture.helpers;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SessionData
{
    public Map<Integer, String>     barcodeValuesMap;
    public Map<Integer, Integer>    barcodeQuantityMap;
    public Map<Integer, Integer>    barcodeSymbologyMap;
    public Map<Integer, Date>       barcodeDateMap;

    public SessionData()
    {
        barcodeValuesMap = new HashMap<>();
        barcodeQuantityMap = new HashMap<>();
        barcodeSymbologyMap = new HashMap<>();
        barcodeDateMap = new HashMap<>();
    }

}