/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package terminal_consulta_preco;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import sun.awt.windows.ThemeReader;

public class Client implements Runnable{
    
    Socket client;
    int id;
    Connection conBD;
    InputStream entrada;
    OutputStream saida;
            
    public Client(Socket c,int id){
        this.client = c;
        this.id = id;
    }
    
    
    
    public void run(){
        //System.out.println("Cliente conectado ID = "+String.valueOf(id));
        try{
            entrada = client.getInputStream();
            saida = client.getOutputStream();
            
            String palavra= "";
            String dado = "Conexao OK\n";

            Boolean recebendo = true;
            
            Etiqueta etq = new Etiqueta();
            
            byte[] b = dado.getBytes();

            saida.write(b);
            saida.flush();
            
            while(recebendo)
            {
                palavra = recebeCaracteres();
                if (palavra.compareTo("ETQ")==0)//Impressão de Etiqueta (Pesq Preço)
                {
                    palavra = recebeCaracteres();
                    String dados[] = palavra.split(";");
                    etq.setCodI(dados[0]);
                    etq.setCodB(dados[1]);
                    etq.setDescr(dados[2].toUpperCase());
                    etq.setPreco(dados[3]);
                    etq.imprime();
                    etq = null;
                    palavra = "";
                    this.client.close();
                    recebendo = false;
                    client = null;
                }
                else if (palavra.compareTo("CNF")==0)//Pesquisa Fornecedor (Conf NF)
                {
                    palavra = recebeCaracteres();
                    //System.out.println(palavra);
                    pesquisaFornecedor(palavra);
                    //b = "Encontrado\nTeste Conferencia NF\n".getBytes();
                    //saida.write(b);
                    //saida.flush();
                    Thread.sleep(500);
                    this.client.close();
                    recebendo = false;
                    client = null;
                 }
                else if (palavra.compareTo("CIP")==0)//Consulta Informações Produtos (Conf NF)
                {
                    palavra = recebeCaracteres();
                    //System.out.println(palavra);
                    pesquisaProduto(palavra);
                    //b = "Encontrado\nTeste Conferencia NF\n".getBytes();
                    //saida.write(b);
                    //saida.flush();
                    Thread.sleep(500);
                    this.client.close();
                    recebendo = false;
                    client = null;
                }
                else if (palavra.compareTo("CPP")==0)//Consulta Pesquisa Produto por Descricao
                {
                    palavra = recebeCaracteres();
                    //System.out.println(palavra);
                    pesquisaProdutoDescricao(palavra);
                    Thread.sleep(500);
                    this.client.close();
                    recebendo = false;
                    client = null;
                }
                else if(palavra.compareTo("CABNF")==0)//Grava cabecalho NF (Conf NF)
                {
                    boolean recebe = true;
                    int codCab = -2;
                    conBD = ConexaoAcess.getConnection();
                    palavra = recebeCaracteres();
                    String dados[] = palavra.split(";");
                    codCab = gravaCabNF(dados);
                    ArquivoLog log = new ArquivoLog();
                    
                    if(codCab != -1)
                    {
                        b = "CABNFOK\n".getBytes();
                        saida.write(b);
                        saida.flush();
                        palavra = recebeCaracteres();
                        if(palavra.compareTo("GIN")==0)//Grava informações NF (Conf NF)
                        {
                            while(recebe)
                            {
                                palavra = recebeCaracteres();
                                if(palavra.compareTo("FIM") != 0 & palavra.compareTo("") != 0)
                                {
                                    dados = palavra.split(";");
                                    if(dados.length == 2)
                                    {
                                        gravaInformacoesNF(codCab,dados);
                                        b = "Gravacao OK\n".getBytes();
                                        saida.write(b);
                                        saida.flush();
                                        log.gravaLog(String.valueOf(codCab),dados[0]+";"+dados[1]);
                                    }
                                    else
                                    {
                                        b = "Falha GIND\n".getBytes();

                                        saida.write(b);
                                        saida.flush();
                                        recebe = false;
                                        
                                        desfazerGravacao(codCab);
                                    }
                                }
                                else if (palavra.compareTo("") == 0)
                                {
                                    b = "Falha GIN\n".getBytes();

                                    saida.write(b);
                                    saida.flush();
                                    recebe = false;
                                    desfazerGravacao(codCab);
                                }
                                else    
                                {
                                    b = "GINF\n".getBytes();
                                    conBD.close();
                                    saida.write(b);
                                    saida.flush();
                                    recebe = false;
                                }
                            }
                        }
                        this.client.close();
                        recebendo = false;
                        client = null;
                    }
                    else
                    {
                        b = "Falha CABNF\n".getBytes();
                        saida.write(b);
                        saida.flush();
                        recebe = false;
                        this.client.close();
                        recebendo = false;
                        client = null;
                    }
                }
                else if(palavra.compareTo("CRP")==0) //Pesquisa Produtos para Reposicao
                {
                    conBD = ConexaoAcess.getConnection();
                    pesquisaProdutosReposicao();
                    conBD.close();
                    this.client.close();
                    recebendo = false;
                    client = null;
                }
                else if(palavra.compareTo("RRP")==0) //Registrar Produto para Reposicao
                {
                    conBD = ConexaoAcess.getConnection();
                    registraProdutosReposicao();
                    conBD.close();
                    this.client.close();
                    recebendo = false;
                    client = null;
                }
                else if(palavra.compareTo("BRP")==0) //Baixa Produto para Reposicao
                {
                    conBD = ConexaoAcess.getConnection();
                    baixaProdutosReposicao();
                    conBD.close();
                    this.client.close();
                    recebendo = false;
                    client = null;
                }
                else if(palavra.compareTo("Test Server")!=0) //Pesquisa Preço
                {
                    System.out.println(palavra);
                    pesquisaCodigo(palavra);
                    palavra = "";
                    client.close();
                }
                    //System.out.println("Cliente desconectado id = "+String.valueOf(id));
                recebendo = false;
            }
            entrada = null;
            saida = null;
            /*while(recebendo & consulta){
                int res = entrada.read();
                if((char)res == '\n')
                {
                    System.out.println(palavra);
                    pesquisaCodigo(palavra);
                    palavra = "";
                    client.close();
                    
                    //System.out.println("Cliente desconectado id = "+String.valueOf(id));
                    recebendo = false;
                }
                else    
                    palavra = palavra + (char)res;
                

            }*/
        }catch(IOException e){
            System.out.println(e.toString());
        }catch(Exception e){
            System.out.println(e.toString());
        }
        
    }
    
    public String recebeCaracteres() throws IOException
    {
        boolean recebendo = true;
        String palavra = "";
        while(recebendo){
            int res = entrada.read();
            if((char)res == '\n')
            {
                recebendo = false;
            }
            else if(res == -1){
                break;
            }
            else    
                palavra = palavra + (char)res;

        }
        return palavra;
    }
    public int gravaCabNF(String dados[])
    {
        String codF = "";
        String nNF = "";
        String modNF = "";
        String datNF = "";
        String serNF = "";
        String chNF = "";
        String sql = "";
        
        if(dados.length <= 4)
        {
            codF = dados[0];
            nNF = dados[1];
            modNF = dados[2];
            datNF = dados[3];
        }
        else if (dados.length <=5)
        {
            codF = dados[0];
            nNF = dados[1];
            modNF = dados[2];
            serNF = dados[3];
            datNF = dados[4];
        }
        if(dados.length > 5)
        {
            codF = dados[0];
            nNF = dados[1];
            modNF = dados[2];
            serNF = dados[3];
            datNF = dados[4];
            chNF = dados[5];
            
        }

        if(serNF.compareTo("")==0)
            sql = "INSERT INTO CabecalhoNF (codigoFornecedor,numeroNF,modeloNF,dataNF,chaveAcessoNF) values (" + codF + "," + nNF + "," + modNF +",'" + datNF +"','" + chNF + "' ) ";
        else
            sql = "INSERT INTO CabecalhoNF (codigoFornecedor,numeroNF,modeloNF,serieNF,dataNF,chaveAcessoNF) values (" + codF + "," + nNF + "," + modNF +"," + serNF + ",'" + datNF +"','" + chNF + "' ) ";
        
        //System.out.println(sql);
        //System.out.println("Cod: " + codF + " - N: " + nNF + " - Mod: " + modNF + " - S: " + serNF + " - Data: " + datNF + " - Chave: " + chNF);
        try{
            
            PreparedStatement stmt = conBD.prepareStatement(sql);
            stmt.execute();
            
            //sql = "SELECT CabecalhoNF.Codigo " +
            //      "FROM CabecalhoNF " +
            //     "WHERE (((CabecalhoNF.codigoFornecedor)=?) AND ((CabecalhoNF.numeroNF)=?) AND ((CabecalhoNF.modeloNF)=?) AND ((CabecalhoNF.serieNF)=?) AND ((CabecalhoNF.dataNF)=?) AND ((CabecalhoNF.chaveAcessoNF)=?));";
            if(serNF.compareTo("")!=0)
                sql = "SELECT CabecalhoNF.Codigo " +
                    "FROM CabecalhoNF " +
                    "WHERE (((CabecalhoNF.codigoFornecedor)=" + codF + ") AND ((CabecalhoNF.numeroNF)=" + nNF + ") AND ((CabecalhoNF.modeloNF)=" + modNF + ") AND ((CabecalhoNF.serieNF)=" + serNF + ") AND ((CabecalhoNF.dataNF)='" + datNF + "') AND ((CabecalhoNF.chaveAcessoNF)='" + chNF + "'));";
            else
                sql = "SELECT CabecalhoNF.Codigo " +
                    "FROM CabecalhoNF " +
                    "WHERE (((CabecalhoNF.codigoFornecedor)=" + codF + ") AND ((CabecalhoNF.numeroNF)=" + nNF + ") AND ((CabecalhoNF.modeloNF)=" + modNF + ") AND ((CabecalhoNF.dataNF)='" + datNF + "') AND ((CabecalhoNF.chaveAcessoNF)='" + chNF + "'));";    
            
            stmt = conBD.prepareStatement(sql);
            /*stmt.setString(1, codF);
            stmt.setString(2, nNF);
            stmt.setString(3, modNF);
            stmt.setString(4, serNF);
            stmt.setString(5, "'" + datNF + "'");
            stmt.setString(6, "'" + chNF + "'");*/
            
            ResultSet rs = stmt.executeQuery();
            if(rs.next())
            {
                int codCab = rs.getInt("codigo");
                String msg = codF + ";" + nNF + ";" + modNF + ";" + serNF + ";" + datNF + ";" + chNF ;
                ArquivoLog log = new ArquivoLog();
                log.gravaLog(String.valueOf(String.valueOf(codCab)), msg);
                return codCab;
            }
            else
            {
                return -1;
            }
                

        }catch(SQLException e){
            System.out.println(e.toString());
        }catch(Exception e){
            System.out.println(e.toString());
        }
        return -1;
        
    }
    
    public void desfazerGravacao(int cod)
    {
        try
        {
            String sql = "DELETE FROM CabecalhoNF WHERE CabecalhoNF.codigo = "+cod+";";
            PreparedStatement stmt = conBD.prepareStatement(sql);
            stmt.execute();
            sql  = "DELETE FROM ProdutosNF WHERE ProdutosNF.codigoCab = "+cod+";";
            stmt = conBD.prepareStatement(sql);
            stmt.execute();
            
        }
        catch(Exception e)
        {
            System.out.println(e.toString());
        }
    }
    
    public void baixaProdutosReposicao()
    {
        try{

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy");
            SimpleDateFormat dateFormat2 = new SimpleDateFormat("mm/dd/yyyy");
            
            String palavra = recebeCaracteres();
            String dados[] = palavra.split(";");
            
            Date dataR = dateFormat.parse(dados[0]);
            String data = dateFormat2.format(dataR);
            
            String sql = "DELETE FROM ProdutosReposicao WHERE (((ProdutosReposicao.Data)=#" + data + "#) AND ((ProdutosReposicao.CodigoProduto)='" + dados[1] + "') AND ((ProdutosReposicao.Quantidade)='" + dados[2] + "'))";
            
            PreparedStatement stmt = conBD.prepareStatement(sql);
            stmt.execute();
            
            
            byte b[] = ("Baixa OK\n").getBytes();
            saida.write(b);
            saida.flush();
         
        }catch(SQLException e){
            System.out.println(e.toString());
        }catch(Exception e){
            System.out.println(e.toString());
        }
    }
    public void registraProdutosReposicao()
    {
        
        try{

            String palavra = recebeCaracteres();
            String dados[] = palavra.split(";");
            
            String sql = "INSERT INTO ProdutosReposicao(Data,CodigoProduto,Descricao,Quantidade,Reposicao) VALUES ('" + dados[0] + "','" + dados[1] + "','" + dados[2] + "','" + dados[3] + "',FALSE);";
            
            PreparedStatement stmt = conBD.prepareStatement(sql);
            stmt.execute();
         
        }catch(SQLException e){
            System.out.println(e.toString());
        }catch(Exception e){
            System.out.println(e.toString());
        }
        
    }
    
    public void pesquisaProdutosReposicao()
    {
        String sql = "SELECT ProdutosReposicao.Data,ProdutosReposicao.CodigoProduto,ProdutosReposicao.Descricao,ProdutosReposicao.Quantidade,ProdutosReposicao.Reposicao FROM ProdutosReposicao WHERE ((ProdutosReposicao.Reposicao)= False) ORDER BY ProdutosReposicao.Data, ProdutosReposicao.Descricao;";
        try{
            
            PreparedStatement stmt = conBD.prepareStatement(sql);
            ResultSet rs =  stmt.executeQuery();
            
            String data;
            String cod;
            String desc;
            String quant;

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy");
            SimpleDateFormat date = new SimpleDateFormat("yyyy/mm/dd");
            byte b[];
            
            if(rs.next())
            {
                b = ("Encontrado\n").getBytes();
                saida.write(b);
                saida.flush();
                
                data = rs.getDate("Data").toString();
                data = data.replace("-", "/");
                data = dateFormat.format(date.parse(data)).toString();
                cod = rs.getString("CodigoProduto");
                desc = rs.getString("Descricao");
                quant = rs.getString("Quantidade");
                
                b = ("CRP\n"+data+";"+cod+";"+desc+";"+quant+"\n").getBytes("iso-8859-1");
                saida.write(b);
                saida.flush();
                
            }
            while(rs.next())
            {
                data = rs.getDate("Data").toString();
                data = data.replace("-", "/");
                data = dateFormat.format(date.parse(data)).toString();
                cod = rs.getString("CodigoProduto");
                desc = rs.getString("Descricao");
                quant = rs.getString("Quantidade");
                
                b = ("CRP\n"+data+";"+cod+";"+desc+";"+quant+"\n").getBytes("iso-8859-1");
                saida.write(b);
                saida.flush();
            }
            
            b = ("CRPF\n").getBytes();
            saida.write(b);
            saida.flush();
            

        }catch(SQLException e){
            System.out.println(e.toString());
        }catch(Exception e){
            System.out.println(e.toString());
        }
    }
    
    public void gravaInformacoesNF(int idCab,String dados[])
    {
        String codProd = dados[0];
        String quantProd = dados[1];
        
        quantProd = quantProd.replace(".", ",");
        
        
        String sql = "INSERT INTO ProdutosNF (codigoCab,codProduto,quantProduto) values (" + idCab + "," + codProd + ", '" + quantProd + "')";
        
        
        //System.out.println(codProd + " - " + quantProd);
        try{
            
            PreparedStatement stmt = conBD.prepareStatement(sql);
            stmt.execute();

        }catch(SQLException e){
            System.out.println(e.toString());
        }catch(Exception e){
            System.out.println(e.toString());
        }
        
    }
    
    public void pesquisaFornecedor(String forn)
    {
        String sql = "select codigo,razao from fornece where CGC='" + forn + "'";
        
        try{
            conBD = Conexao.getConnection();
            PreparedStatement stmt = conBD.prepareStatement(sql);
            ResultSet rs =  stmt.executeQuery();
            
            byte[] b;
            
            if(rs.next())
            {
                b = ("Encontrado\n"+rs.getInt("codigo")+ ";" +rs.getString("razao")+"\n").getBytes();
                
            }
            else
            {
                b = "Nao Encontrado\n".getBytes();
                
            }
            saida.write(b);
            saida.flush();
        }catch(SQLException e){
            System.out.println(e.toString());
        }catch(Exception e){
            System.out.println(e.toString());
        }
    }
    
    public void pesquisaProdutoDescricao(String desc)
    {
        //String sql = "Select C.Descricao,C.Unidade,C.codigo,CO.codbarra from cadmer as C, codbarra as CO "
                    //+ "Where C.Descricao LIKE '%"+desc+"%' and C.codigo = CO.codigo group by C.Descricao;";
        String sql = "Select C.Descricao,C.Unidade,C.codigo from cadmer as C "
                    + "Where C.Descricao LIKE '%"+desc+"%' group by C.Descricao;";
        try{
            conBD = Conexao.getConnection();
            PreparedStatement stmt = conBD.prepareStatement(sql);
            ResultSet rs =  stmt.executeQuery();
            
            String codI = "";
            String descP = "";
            
            if(rs.next())
            {
                codI = rs.getString("codigo");
                descP = rs.getString("Descricao");

                byte[] b = ("CPPI\n"+ codI + ";" + descP+"\n").getBytes("iso-8859-1");
                saida.write(b);
                saida.flush();
                Thread.sleep(100);
                while(rs.next())
                {
                    codI = rs.getString("codigo");
                    descP = rs.getString("Descricao");

                    b = ("CPPI\n"+ codI + ";" + descP+"\n").getBytes("iso-8859-1");
                    saida.write(b);
                    saida.flush();
                    Thread.sleep(100);
                }
            }
            else
            {
                
            }
            byte[] b = ("CPPF\n").getBytes();
            saida.write(b);
            saida.flush();
            Thread.sleep(100);
        }catch (Exception e)
        {
            System.out.println(e.toString());
        }
    }
    
    public void pesquisaProduto(String cod)
    {
        String sql = "Select C.Descricao,C.Unidade,C.codigo,CO.codbarra from cadmer_estoque as CE, cadmer as C, codbarra as CO "
                    + "Where CO.codbarra = '" + cod + "' and C.codigo = CO.codigo and CE.codigo=C.codigo";
        
        String sql2 = "Select C.Descricao,C.Unidade,C.codigo,CO.codbarra from cadmer_estoque as CE, cadmer as C, codbarra as CO "
                    + "Where CO.codigo = '" + cod + "' and C.codigo = CO.codigo and CE.codigo=C.codigo";
        
        String sql3 = "Select C.Descricao,C.Unidade,C.codigo from cadmer as C "
                    + "Where C.codigo = '" + cod + "'";
        
        try{
            conBD = Conexao.getConnection();
            PreparedStatement stmt = conBD.prepareStatement(sql);
            ResultSet rs =  stmt.executeQuery();
            
            String status = "";
            String desc = "";
            String codI = "";
            String unid = "";
            
            DecimalFormat codIn = new DecimalFormat("000000");
            
            if(rs.next())
            {
                status = "Encontrado\n";
                desc = rs.getString("Descricao");
                codI = codIn.format(Integer.parseInt(rs.getString("C.codigo")));
                unid = rs.getString("Unidade").toUpperCase();
            }
            else 
            {
                stmt = conBD.prepareStatement(sql2);
                rs =  stmt.executeQuery();
                if(rs.next())
                {
                    status = "Encontrado\n";
                    desc = rs.getString("Descricao");
                    codI = codIn.format(Integer.parseInt(rs.getString("C.codigo")));
                    unid = rs.getString("Unidade").toUpperCase();
                }
                else
                {
                    stmt = conBD.prepareStatement(sql3);
                    rs =  stmt.executeQuery();
                    if(rs.next())
                    {
                        status = "Encontrado\n";
                        desc = rs.getString("Descricao");
                        codI = codIn.format(Integer.parseInt(rs.getString("C.codigo")));
                        unid = rs.getString("Unidade").toUpperCase();
                    }
                    else
                        status = "Falha\n";
                }
                    
            }
            
            //String status = "Encontrado";
            //String desc = "Arroz Bonachão Tipo 1";
            //String estoque = "10";
            //String preco = "10,90";
            
            byte[] b = ("CIP\n"+ status).getBytes();
            saida.write(b);
            saida.flush();
            Thread.sleep(500);
            if(status.compareTo("Encontrado\n")==0)
            {
                String resp = codI + ";" + desc + ";"+ unid + "\n";
                b = resp.getBytes();
                saida.write(b);
                saida.flush();
            }
            conBD.close();
        }catch(SQLException e){
            System.out.println(e.toString());
        }catch(Exception e){
            System.out.println(e.toString());
        }
        
    }
    
    public void pesquisaCodigo(String cod)
    {
        
        String sql = "Select C.Descricao,C.Venda,C.Promocao,CE.Lojaest,C.Unidade,C.codigo,CO.codbarra from cadmer_estoque as CE, cadmer as C, codbarra as CO "
                    + "Where CO.codbarra = '" + cod + "' and C.codigo = CO.codigo and CE.codigo=C.codigo";
        
        String sql2 = "Select C.Descricao,C.Venda,C.Promocao,CE.Lojaest,C.Unidade,C.codigo,CO.codbarra from cadmer_estoque as CE, cadmer as C, codbarra as CO "
                    + "Where CO.codigo = '" + cod + "' and C.codigo = CO.codigo and CE.codigo=C.codigo";
        try{
            conBD = Conexao.getConnection();
            PreparedStatement stmt = conBD.prepareStatement(sql);
            ResultSet rs =  stmt.executeQuery();
            
            String status = "";
            String desc = "";
            String estoque = "";
            String preco = "";
            String codI = "";
            String codB = "";
            
            DecimalFormat peso = new DecimalFormat("0.000");
            DecimalFormat pre = new DecimalFormat("0.00");
            DecimalFormat codIn = new DecimalFormat("000000");
            
            if(rs.next())
            {
                status = "Encontrado\n";
                desc = rs.getString("Descricao");
                codI = codIn.format(Integer.parseInt(rs.getString("C.codigo")));
                codB = rs.getString("CO.codbarra");
                if(rs.getString("Unidade").toLowerCase().compareTo("un")==0)
                    estoque = String.valueOf((int)rs.getFloat("lojaest"));
                else if(rs.getFloat("lojaest") != 0)
                    estoque = peso.format(rs.getFloat("lojaest"));
                else 
                    estoque = String.valueOf(0);
                if(rs.getFloat("Promocao") != 0)
                    preco = pre.format(rs.getFloat("Promocao"));
                else
                    preco = pre.format(rs.getFloat("Venda"));
            }
            else 
            {
                stmt = conBD.prepareStatement(sql2);
                rs =  stmt.executeQuery();
                if(rs.next())
                {
                    status = "Encontrado\n";
                    desc = rs.getString("Descricao");
                    codI = codIn.format(Integer.parseInt(rs.getString("C.codigo")));
                    codB = rs.getString("CO.codbarra");
                    if(rs.getString("Unidade").toLowerCase().compareTo("un")==0)
                        estoque = String.valueOf((int)rs.getFloat("lojaest"));
                    else if(rs.getFloat("lojaest") != 0)
                        estoque = peso.format(rs.getFloat("lojaest"));
                    else 
                        estoque = String.valueOf(0);
                    if(rs.getFloat("Promocao") != 0)
                        preco = pre.format(rs.getFloat("Promocao"));
                    else
                        preco = pre.format(rs.getFloat("Venda"));
                }
                else
                    status = "Falha\n";
            }
            
            //String status = "Encontrado";
            //String desc = "Arroz Bonachão Tipo 1";
            //String estoque = "10";
            //String preco = "10,90";
            
            byte[] b = status.getBytes();
            saida.write(b);
            saida.flush();
            Thread.sleep(500);
            if(status.compareTo("Encontrado\n")==0)
            {
                String resp = codB + ";" + desc + ";" + estoque + ";" + preco + ";" + codI+ "\n";
                b = resp.getBytes();
                saida.write(b);
                saida.flush();
            }
            conBD.close();
            
        }catch(SQLException e){
            System.out.println(e.toString());
        }catch(Exception e){
            System.out.println(e.toString());
        }
        
        
    }
}
