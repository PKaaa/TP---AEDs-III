package model;

import java.io.*;

//TABELA INTERMEDIARIA RELACIONAMENTO N:N

public class ReceitaAlimento implements Registro {
     private int id; //ID unico para interface de Registro
     private int idReceita;
     private int idAlimento;

     public ReceitaAlimento() {
          this.id = 0;
          this.idReceita = 0;
          this.idAlimento = 0;
     }

     public ReceitaAlimento (int id, int idReceita, int idAlimento) {
          this.id = id;
          this.idReceita = idReceita;
          this.idAlimento = idAlimento;
     }

     public ReceitaAlimento (int idReceita, int idAlimento) {
          this.idReceita = idReceita;
          this.idAlimento = idAlimento;
     }

     public int getId() {return id;}
     public int getIdReceita() {return idReceita;}
     public int getIdAlimento() {return idAlimento;}

     public void setId (int id) {this.id = id;}
     public void setIdReceita (int idReceita) {this.idReceita = idReceita;}
     public void setIdAlimento (int idAlimento) {this.idAlimento = idAlimento;}

     @Override
     public String toString() {
          return "Receita_Alimento => {id = " + id + "; ID de Receita = " + idReceita + "; ID de Alimento = " + idAlimento + "}";
     }

     @Override
     public int hashCode() {return id;}

     public byte[] toByteArray() throws IOException {
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          DataOutputStream dos = new DataOutputStream(baos);

          dos.writeInt(id);
          dos.writeInt(idReceita);
          dos.writeInt(idAlimento);

          return baos.toByteArray();
     }

     public void fromByteArray(byte[] b) throws IOException {
          ByteArrayInputStream bais = new ByteArrayInputStream(b);
          DataInputStream dis = new DataInputStream(bais);

          id = dis.readInt();
          idReceita = dis.readInt();
          idAlimento = dis.readInt();
     }
}