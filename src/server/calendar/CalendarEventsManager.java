/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.calendar;
import net.channel.ChannelServer;
import java.util.concurrent.ScheduledFuture;
import server.TimerManager;

/**
 *
 * @author FateJiki
 */
public class CalendarEventsManager {
    private byte channel = 1;
    private CalendarEventsLibrary curr_event = CalendarEventsLibrary.NO_CURRENT_EVENT;
    private ScheduledFuture update_thread;
    private int[] possible_mapEffects_ids;
    
    public CalendarEventsManager(final byte ch){
        channel = ch;
     //   update_thread = TimerManager.getInstance().register(new updateCurrentEvent(), 1000 * 60 * 10);
    }
    
    public ChannelServer getCS(){
        return ChannelServer.getInstance(channel);
    }
    
    public void startTimer(){
        update_thread = TimerManager.getInstance().register(new updateCurrentEvent(), 1000 * 60 * 10);
    }
        
    public class updateCurrentEvent implements Runnable {
        @Override
        public void run(){
         //   CalendarEventsLibrary old_event = curr_event;
            curr_event = CalendarEventsLibrary.NO_CURRENT_EVENT;
            System.out.println("CURRENT EVENT IS NOW : " + curr_event.name());
            possible_mapEffects_ids = MapEffectsLibrary.generateMapEffectIDsByLib(curr_event);
        }
    }
    
    public int retrieveMapEffectID(){
        return possible_mapEffects_ids[((int)(possible_mapEffects_ids.length * Math.random()))];
    }
    
    public CalendarEventsLibrary getCurrentEvent(){
        return curr_event;
    }
    

}

 enum MapEffectsLibrary{
    SNOWY(new int[]{5120003, 5120000, 5121015}, CalendarEventsLibrary.WINTER),
    CHRISTMAS(new int[]{5120003, 5120012, 5121007, 5121015, 5120000}, CalendarEventsLibrary.CHRISTMAS),
    HALLOWEEN(new int[]{}, CalendarEventsLibrary.HALLOWEEN),
    
    ;
    
    
    final CalendarEventsLibrary cal;
    final int[] event_itemids;
    private MapEffectsLibrary(int[] id, CalendarEventsLibrary _cal){
        event_itemids = id;
        cal = _cal;
    }
    
    public int[] getIntArray(){
        return event_itemids;
    }
    
    public CalendarEventsLibrary getLib(){
        return cal;
    }
    
    public static int[] generateMapEffectIDsByLib(CalendarEventsLibrary mel){
        int[] ret = null;
        for(MapEffectsLibrary mels : MapEffectsLibrary.values()){
            if(mels.getLib() == mel){
                ret = mels.getIntArray();
                break;
            }
        }
        
        
        return ret;
    }
}
