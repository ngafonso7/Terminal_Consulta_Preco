/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package terminal_consulta_preco;

import java.io.FileOutputStream;

/**
 *
 * @author Natanael
 */
public class Etiqueta {
    
    private String descr;
    private String codI;
    private String codB;
    private String preco;

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public String getCodI() {
        return codI;
    }

    public void setCodI(String codI) {
        this.codI = codI;
    }

    public String getCodB() {
        return codB;
    }

    public void setCodB(String codB) {
        this.codB = codB;
    }

    public String getPreco() {
        return preco;
    }

    public void setPreco(String preco) {
        this.preco = preco;
    }
    
    public void imprime()
    {
        FileOutputStream fos = null;
        try{
            fos = new FileOutputStream("\\\\est50\\Argox");
            fos.write("\u0002L\n".getBytes());
            fos.write("D11\n".getBytes());
            fos.write(("121200000700020"+this.codI+" - "+this.descr+"\n").getBytes());
            fos.write(("1F5203500100023"+this.codB+"\n").getBytes());
            fos.write( ("124400200250220R$ "+this.preco+"\n").getBytes());

            fos.write("Q0001\n".getBytes());
            fos.write("E\n".getBytes());
            fos.write("\u0002Q\n".getBytes());
            fos.flush();
            fos.close();
        }catch(Exception e){
            System.out.println(e.toString());
        }
    }
    
}
