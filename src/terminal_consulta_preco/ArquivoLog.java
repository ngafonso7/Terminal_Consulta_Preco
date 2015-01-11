/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package terminal_consulta_preco;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class ArquivoLog {
    
    public void gravaLog(String nome,String msg)
    {
        File dir = new File("C:\\logNF");
        File arq = new File(dir,nome+".txt");
        
        try{
            arq.createNewFile();
            FileWriter fileWriter = new FileWriter(arq,true);    
            PrintWriter printWriter = new PrintWriter(fileWriter);
            
            String dados[] = msg.split(";");
            for(int i=0;i < dados.length;i++)
            {
                printWriter.println(dados[i]);
            }
            
            
            printWriter.flush();
            printWriter.close();
        }
        catch(Exception e)
        {
            System.out.println(e.toString());
        }
            
    }
    
}
