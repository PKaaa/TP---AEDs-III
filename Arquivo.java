package TP;

import java.io.*;
import java.lang.reflect.Constructor;

public class Arquivo <T extends Registro> {
     private static final int TAM_REGISTRO = 4;
     private RandomAccessFile arquivo;
     private String nomeArq;
     private Constructor<T> construtor;

     public Arquivo (String nomeArq, Constructor<T> construtor) throws Exception {
          File diretorio = new File ("./dados");
          if (!diretorio.exists()) diretorio.mkdir();

          diretorio = new File ("./dados/" + nomeArq);
          if (!diretorio.exists()) diretorio.mkdir();

          this.nomeArq = "./dados/" + nomeArq + "/arquivo.dat";
          this.construtor = construtor;
          this.arquivo = new RandomAccessFile (this.nomeArq, "rw");

          if (arquivo.length() < TAM_REGISTRO) {
               arquivo.writeInt(0); //ultimo id
               arquivo.writeInt(-1); //lista excluida
          }
     }

     public int create (T obj) throws Exception {
          arquivo.seek(0);
          int newId = arquivo.readInt() + 1;
          arquivo.seek(0);
          arquivo.writeInt(newId);
          obj.setId(newId);
          byte[] dados = obj.toByteArray();

          long address = getDeleated (dados.length);

          if (address == -1) {
               arquivo.seek (arquivo.length());
               address = arquivo.getFilePointer();
               arquivo.writeByte (' '); //lápide
               arquivo.writeShort(dados.length);
               arquivo.write(dados);
          } else {
               arquivo.seek (address);
               arquivo.writeByte (' '); //remove lápide
               arquivo.writeShort(dados.length);
               arquivo.write(dados);
          }

          return obj.getId();
     }

     public T read (int id) throws Exception {
          arquivo.seek (TAM_REGISTRO);

          while (arquivo.getFilePointer() < arquivo.length()) {
               long address = arquivo.getFilePointer();
               byte lapide = arquivo.readByte();
               short tamanho = arquivo.readShort();
               byte[] dados = new byte[tamanho];
               arquivo.readFully(dados);

               if (lapide == ' ') {
                    T obj = construtor.newInstance();
                    obj.fromByteArray(dados);
                    if (obj.getId() == id) {
                         return obj;
                    }
               }
          }
          return null;
     }

     public boolean delete(int id) throws Exception {
          arquivo.seek(TAM_REGISTRO);
          
          while (arquivo.getFilePointer() < arquivo.length()) {
               long posicao = arquivo.getFilePointer();
               byte lapide = arquivo.readByte();
               short tamanho = arquivo.readShort();
               byte[] dados = new byte[tamanho];
               arquivo.read(dados);

               if (lapide == ' ') {
                    T obj = construtor.newInstance();
                    obj.fromByteArray(dados);

                    if (obj.getId() == id) {
                         arquivo.seek(posicao);
                         arquivo.writeByte('*');
                         addDeleted(tamanho, posicao);
                         return true;
                    }
               }
          }
          return false;
     }

     public boolean update(T novoObj) throws Exception {
          arquivo.seek(TAM_REGISTRO);
          
          while (arquivo.getFilePointer() < arquivo.length()) {
               long posicao = arquivo.getFilePointer();
               byte lapide = arquivo.readByte();
               short tamanho = arquivo.readShort();
               byte[] dados = new byte[tamanho];
               arquivo.read(dados);

               if (lapide == ' ') {
                    T obj = construtor.newInstance();
                    obj.fromByteArray(dados);

                    if (obj.getId() == novoObj.getId()) {
                         byte[] novosDados = novoObj.toByteArray();
                         short novoTam = (short) novosDados.length;
                         if (novoTam <= tamanho) {
                              arquivo.seek(posicao + 3);
                              arquivo.write(novosDados);
                         } else {
                              arquivo.seek(posicao);
                              arquivo.writeByte('*');
                              addDeleted(tamanho, posicao);
                              long novoEndereco = getDeleted(novosDados.length);

                              if (novoEndereco == -1) {
                                   arquivo.seek(arquivo.length());
                                   novoEndereco = arquivo.getFilePointer();
                                   arquivo.writeByte(' ');
                                   arquivo.writeShort(novoTam);
                                   arquivo.write(novosDados);
                              } else {
                                   arquivo.seek(novoEndereco);
                                   arquivo.writeByte(' ');
                                   arquivo.skipBytes(2);
                                   arquivo.write(novosDados);
                              }
                         }
                         return true;
                    }
               }
          }

          return false;
     }

     private void addDeleted(int tamanhoEspaco, long enderecoEspaco) throws Exception {
          long posicao = 4;
          arquivo.seek(posicao);
          long endereco = arquivo.readLong();
          long proximo;

          if (endereco == -1) {
               arquivo.seek(4);
               arquivo.writeLong(enderecoEspaco);
               arquivo.seek(enderecoEspaco + 3);
               arquivo.writeLong(-1);
          } else {
               do {
                    arquivo.seek(endereco + 1);
                    int tamanho = arquivo.readShort();
                    proximo = arquivo.readLong();
                         
                    if (tamanho > tamanhoEspaco) {
                         if (posicao == 4)
                              arquivo.seek(posicao);
                         else
                              arquivo.seek(posicao + 3);
                              arquivo.writeLong(enderecoEspaco);
                              arquivo.seek(enderecoEspaco + 3);
                              arquivo.writeLong(endereco);
                         break;
                    }

                    if (proximo == -1) {
                         arquivo.seek(endereco + 3);
                         arquivo.writeLong(enderecoEspaco);
                         arquivo.seek(enderecoEspaco + 3);
                         arquivo.writeLong(-1);
                         break;
                    }

                    posicao = endereco;
                    endereco = proximo;
               } while (endereco != -1);
          }
     }

     private long getDeleted(int tamanhoNecessario) throws Exception {
          long posicao = 4;
          arquivo.seek(posicao);
          long endereco = arquivo.readLong();
          long proximo;
          int tamanho;

          while (endereco != -1) {
               arquivo.seek(endereco + 1);
               tamanho = arquivo.readShort();
               proximo = arquivo.readLong();

               if (tamanho > tamanhoNecessario) {
                    if (posicao == 4)
                         arquivo.seek(posicao);
                    else
                         arquivo.seek(posicao + 3);
                         arquivo.writeLong(proximo);
                    return endereco;
               }

               posicao = endereco;
               endereco = proximo;
          }

          return -1;
     }

     public void clode() throws Exception {
          arquivo.close();
     }
}