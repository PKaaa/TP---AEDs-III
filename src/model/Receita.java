package model;

import java.io.*;

public class Receita implements Registro {
     private int id;
     private String titulo;
     private String informacoes;
     private int tempoPreparo;
     private String porcao;

     public Receita() {
          this.id = 0;
          this.titulo = "";
          this.informacoes = "";
          this.tempoPreparo = 0;
          this.porcao = "";
     }

     public Receita (int id, String titulo, String informacoes, int tempoPreparo, String porcao) {
          this.id = id;
          this.titulo = titulo;
          this.informacoes = informacoes;
          this.tempoPreparo = tempoPreparo;
          this.porcao = porcao;
     }

     public Receita (String titulo, String informacoes, int tempoPreparo, String porcao) {
          this.titulo = titulo;
          this.informacoes = informacoes;
          this.tempoPreparo = tempoPreparo;
          this.porcao = porcao;
     }

     //getters e setters
     public int getId() {return id;}
     public String getTitulo() {return titulo;}
     public String getInformacoes() {return informacoes;}
     public int getTempoPreparo() {return tempoPreparo;}
     public String getPorcao() {return porcao;}

     public void setId (int id) {this.id = id;}
     public void setTitulo (String titulo) {this.titulo = titulo;}
     public void setInformacoes (String informacoes) {this.informacoes = informacoes;}
     public void setTempoPreparo (int tempoPreparo) {this.tempoPreparo = tempoPreparo;}
     public void setPorcao (String porcao) {this.porcao = porcao;}

     @Override
     public String toString() {
          return "Receita {id = " + id + 
          "; Título = " + titulo + 
          "; Informações = " + informacoes +
          "; Tempo de Preparo = " + tempoPreparo + " minutos" +
          "; Porção = " + porcao +
          "}";
     }

     @Override
     public int hashCode() {return id;}

     public byte[] toByteArray() throws IOException {
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          DataOutputStream dos = new DataOutputStream(baos);

          dos.writeInt(id);
          dos.writeUTF(titulo);
          dos.writeUTF(informacoes);
          dos.writeInt(tempoPreparo);
          dos.writeUTF(porcao);

          return baos.toByteArray();
     }

     public void fromByteArray(byte[] b) throws IOException {
          ByteArrayInputStream bais = new ByteArrayInputStream(b);
          DataInputStream dis = new DataInputStream(bais);

          id = dis.readInt();
          titulo = dis.readUTF();
          informacoes = dis.readUTF();
          tempoPreparo = dis.readInt();
          porcao = dis.readUTF();
     }
}
