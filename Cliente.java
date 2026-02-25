package TP;
import java.time.LocalDate;
import java.io.*;
import TP.Registro; //tirar esse import depois e implementar (implements) Registro

public class Cliente extends Registro {
     private int id;
     private String nome;
     private LocalDate dataNascimento;
     private LocalDate dataAdicao;
     private String[] email;
     private String senha;     

     //cosntrutores
     public Cliente() {
          this.id = 0;
          this.nome = "";
          this.dataNascimento = LocalDate.now();
          this.dataAdicao = LocalDate.now();
          this.email = new String[0];
          this.senha = "";
     }

     public Cliente (int id, String nome, LocalDate dataNascimento, LocalDate dataAdicao, String[] email, String senha) {
          this.id = id;
          this.nome = nome;
          this.dataNascimento = dataNascimento;
          this.dataAdicao = dataAdicao;
          this.email = email;
          this.senha = senha;
     }

     public Cliente (String nome, LocalDate dataNascimento, LocalDate dataAdicao, String[] email, String senha) {
          this.nome = nome;
          this.dataNascimento = dataNascimento;
          this.dataAdicao = dataAdicao;
          this.email = email;
          this.senha = senha;
     }

     //getters e setters
     public int getId() {return id;}
     public String getNome() {return nome;}
     public LocalDate getDataNascimento() {return dataNascimento;}
     public LocalDate getDataAdicao() {return dataAdicao;}
     public String[] getEmail() {return email;}
     public String getSenha() {return senha;}

     public void setId (int id) {this.id = id;}
     public void setNome (String nome) {this.nome = nome;}
     public void setDataNascimento (LocalDate dataNascimento) {this.dataNascimento = dataNascimento;}
     public void setDataAdicao (LocalDate dataAdicao) {this.dataAdicao = dataAdicao;}
     public void setEmail (String[] email) {this.email = email;}
     public void setSenha (String senha) {this.senha = senha;}

     @Override
     public String toString() {
          return "Cliente {id = " + id + 
          "; Nome = " + nome + 
          "; Data de Nascimento = " + dataNascimento + 
          "; Data de Adição = " + dataAdicao + 
          "; Email = " + String.join(", ", email) + 
          "; Senha = " + senha + "}";
     }

     @Override
     public int hashCode() {return id;}

     //método toByteArray
     public byte[] toByteArray() throws IOException {
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          DataOutputStream dos = new DataOutputStream(baos);

          dos.writeInt(id);
          dos.writeUTF(nome);
          dos.writeUTF (dataNascimento.toString());
          dos.writeUTF (dataAdicao.toString());
          dos.writeInt(email.length);
          for (String e : email) {
               dos.writeUTF(e);
          }
          dos.writeUTF(senha);

          return baos.toByteArray();
     }
}
