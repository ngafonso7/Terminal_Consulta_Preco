/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package terminal_consulta_preco;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.AudioFormat;



/**
 *
 * @author Natanael
 */
public class Terminal_Consulta_Preco {

    /**
     * @param args the command line arguments
     */
    static ServerSocket server;
    static Socket client;
    static int serverPort = 12345;
    
    public static void main(String[] args) {
        // TODO code application logic here
        try{
            Thread GC = new Thread(new GarbageColector());
            GC.start();
            GC = null;
            
            Connection con = new ConexaoAcess().getConnection();
            
            if(con != null){
                System.out.println("Servidor Dados Online");
                /*String sql = "INSERT INTO Conferencia_NF (codigoFornecedor,numeroNF,codigoProduto,quantidadeProduto) VALUES(0,1,2,3);";
                PreparedStatement stmt = con.prepareStatement(sql);
                
                stmt.execute();*/
            }
            
            server = new ServerSocket(serverPort);        
            
            //System.out.println("Servidor Ouvindo porta "+String.valueOf(serverPort));
            System.out.println("Servidor Online");
            con = new Conexao().getConnection(); 
            if(con != null)
            {
                System.out.println("Servidor Mysql Online");
                System.out.println("Consulta Preços Pronto");
            }
            else
            {
                System.out.println("Servidor MySql não responde. Encerrando !");
                System.exit(0);
            }
            
            con.close();
            con = null;
            
            Client c = null;
            
            int i =0;

            while(true)
            {
                try{
                    client = server.accept();
                    c = new Client(client,i);
                    Thread tClient = new Thread(c);
                    tClient.start();
                    c = null;
                    i ++;
                }catch(Exception e)
                {
                       System.out.println(e.toString());
                }

            }
                    
                    
                    
                    
                    /*Thread tClient = new Thread(c);
                    tClient.start();
                    Runtime.getRuntime().gc();
                    c = null;
                    System.gc();*/
                    //ExecutorService executor = Executors.newSingleThreadExecutor();
                    //executor.execute(new Client(client,i));
                    
                    
                //}catch(Exception e){
                //    System.out.println(e.toString());
                //}
            
            //ObjectInputStream entrada = new ObjectInputStream(client.getInputStream());
            //byte b = entrada.readByte();
            //String res = String.valueOf(b);
            //String res = entrada.readObject().toString();
            //System.out.println("Cliente enviou dados");
            //System.out.println(res);
            
            /*ExecutorService executor = Executors.newFixedThreadPool(10);
            
            while(true){
                for (int i = 0; i < 10; i++) {
                    client = server.accept();
                    executor.execute(new Client(client,i));
			
			//Se todas as threads foram disparadas, aguardar o fim delas para disparar novas
			//Se o numero de arquivos no diretório for menor que numero de threads, o processo sera finalizado sem disparar novas threads
			if((i+1)%10 == 0) {
				executor.shutdown();
				while (!executor.isTerminated()) {}
				//Cria uma nova instancia do executor para criar novas threads
				executor = Executors.newFixedThreadPool(10);
			}
               
                }
               
            }*/
                    
            
        }catch(IOException se){
            System.out.println(se.toString());
        }catch(Exception e){
            System.out.println(e.toString());
        }
    }
}
