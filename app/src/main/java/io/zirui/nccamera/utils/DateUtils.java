package io.zirui.nccamera.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    public static String getCurrentDate(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public static boolean compareDates(String setDate)
    {
        if (setDate == null){
            return false;
        }
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date1 = sdf.parse(setDate);
            Date date2 = sdf.parse(getCurrentDate());

            if(date2.after(date1)){
                return true;
            }else{
                return false;
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        return false;
    }
}
