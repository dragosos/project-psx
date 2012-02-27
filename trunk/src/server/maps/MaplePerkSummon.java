/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.maps;
import client.MapleCharacter;

/**
 *
 * @author FateJiki
 */
public class MaplePerkSummon extends MapleSummon{
    
    public MaplePerkSummon(int skill, MapleCharacter owner){
        super(owner, skill, owner.getPosition(), SummonMovementType.CIRCLE_FOLLOW);
        skillLevel = (byte)(owner.getLevel() / 10);
        isCustom = true;
    }
    
    
    
    
}
