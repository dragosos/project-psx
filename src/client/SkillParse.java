/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client;
import org.nfunk.jep.JEP;
import org.nfunk.jep.Node;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author FateJiki
 */
public class SkillParse {
    private final static JEP exp = new JEP();
    private static boolean variablesSet = false;
    final static ReentrantLock lock = new ReentrantLock();

    public static int parseSkillInfo(String equation, int skillLevel){
        int endResult = 1;
        // meaning there is no calculation to do
        if(!equation.contains("*") && !equation.contains("+") && !equation.contains("-") && !equation.contains("/") && !equation.contains("d") && !equation.contains("u") && !equation.contains("x")){
            return Integer.parseInt(equation);
        } else {
        if(equation.contains("+d") || equation.contains("*d")){
            String separator = equation.contains("+d") ? "+d" : "*d";
            String[] str = equation.split("\\" + separator);
            for(int i = 0; i < str.length; i++){
                if(separator.equals("+d")){
                endResult += parseEquation(str[i], skillLevel, true);
                } else if(separator.equals("*d")){
                    endResult *= parseEquation(str[i], skillLevel, true);
                }
            }
        } else if(equation.contains("+u") || equation.contains("*u")){
            String separator = equation.contains("+u") ? "+u" : "*u";
            String[] str = equation.split("\\" + separator);
            for(int i = 0; i < str.length; i++){
                if(separator.equals("+u")){
                endResult += parseEquation(str[i], skillLevel, true);
                } else if(separator.equals("*u")) {
                    endResult *= parseEquation(str[i], skillLevel, true);
                }
            }
        } else {
            endResult = parseEquation(equation, skillLevel, false);
        }
        }
        return endResult;
        
        }
    
    private synchronized static int parseEquation(String splitted, int level, boolean implicitMulti){
        int result = 1;
        lock.lock();
        try{
            if(!variablesSet){
                synchronized(exp){
            exp.addVariable("x", level);
            exp.addVariable("d", 1);
            exp.addVariable("u", 1);
                }
            variablesSet = true;
            } else {
                exp.setVarValue("x", level);
                // resetting the levels..
            }
            synchronized(exp){
            exp.setImplicitMul(true);
            Node n = exp.parse(splitted);
            Object o = exp.evaluate(n);
            String value = String.valueOf(o);
            double d = Double.parseDouble(value);
            result = (int)d;
            if(result < 1){
                result = 1;
            }
            }
        } catch (IllegalArgumentException f){
            f.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return result;
    }
    
    
}
