/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.calendar;
import java.util.*;

/**
 *
 * @author FateJiki
 */
public enum CalendarEventsLibrary {
    NO_CURRENT_EVENT(-1, -1, -1, -1), // meaning no event is currently in place
    BACK_TO_SCHOOL(1, 9, 29, 9), //September
    HALLOWEEN(1, 10, 31, 10), // october
    THANSGIVING(2, 11, 21, 11), // november
    CHRISTMAS(1, 12, 31, 12), // december
    NEW_YEAR(1, 1, 21, 1), // january
    WINTER(1, 2, 21, 2), // february
    EASTER(1, 3, 21, 3), // march
    APRIL_FOOLS(1, 4, 21, 4), // april
    MAY(1, 5, 30, 5), // may
    SUMMER_BREAK(1, 7, 29, 7); // june, july, august

    final int startDate; // number
    final int startMonth; // month_number
    final int endDate; // end _number
    final int endMonth; // end number_month
    
    private CalendarEventsLibrary(int _sd, int _sm, int _ed, int _em){
        startDate = _sd;
        startMonth = _sm;
        endDate = _ed;
        endMonth = _em;
    }
    
    public static CalendarEventsLibrary getByCurrDate(){
        Calendar cale = Calendar.getInstance();
        CalendarEventsLibrary ret = CalendarEventsLibrary.NO_CURRENT_EVENT;
        int today = cale.get(cale.DAY_OF_MONTH);
        int month = cale.get(cale.MONTH) + 1;
        month = month == 13 ? 0 : month;
        for(CalendarEventsLibrary cal : CalendarEventsLibrary.values()){
           if(today >= cal.startDate || today <= cal.endDate){ // right day of month
               if(month == cal.startMonth){
                   ret = cal;
               }
           }
        }
        return ret;
    }
}


