package model;

import java.io.*;

public class Alimento implements Registro {
     private int id;
     private String nome;
     private String[] categoria;

     public Alimento() {
          this.id = 0;
          this.nome = "";
          this.categoria = new String[0];
     }

     public Alimento (int id, String nome, String[] categoria) {
          this.id = id;
          this.nome = nome;
          this.categoria = categoria;
     }

     public Alimento (String nome, String[] categoria) {
          this.nome = nome;
          this.categoria = categoria;
     }

     //getters e setters
     public int getId() {return id;}
     public String getNome() {return nome;}
     public String[] getCategoria() {return categoria;}

     public void setId (int id) {this.id = id;}
     public void setNome (String nome) {this.nome = nome;}
     public void setCategoria (String[] categoria) {this.categoria = categoria;}

     @Override
     public String toString() {
          return "Alimento {id = " + id + 
          "; Nome = " + nome + 
          "; Categoria(s) = " + String.join(", ", categoria) +
          "}";
     }

     @Override
     public int hashCode() {return id;}

     public byte[] toByteArray() throws IOException {
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          DataOutputStream dos = new DataOutputStream(baos);

          dos.writeInt(id);
          dos.writeUTF(nome);
          dos.writeInt(categoria.length);
          for (String cat : categoria) {
               dos.writeUTF(cat);
          }

          return baos.toByteArray();

     }

     public void fromByteArray(byte[] b) throws IOException {
          ByteArrayInputStream bais = new ByteArrayInputStream(b);
          DataInputStream dis = new DataInputStream(bais);

          id = dis.readInt();
          nome = dis.readUTF();
          int categoriaLength = dis.readInt();
          categoria = new String[categoriaLength];
          for (int i = 0; i < categoriaLength; i++) {
               categoria[i] = dis.readUTF();
          }
     }
}
