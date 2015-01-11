/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package terminal_consulta_preco;

/**
 *
 * @author Natanael
 */
public class GarbageColector implements Runnable{

    @Override
    public void run() {
        while(true){
            try{
                Thread.sleep(1800000);
                System.gc();
            }catch(Exception e){
                
            }
            
        }
    }
    
}
