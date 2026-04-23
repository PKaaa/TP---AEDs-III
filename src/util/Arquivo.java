package util;

import java.io.*;
import java.lang.reflect.*;

import model.Registro;

public class Arquivo <T extends Registro> {
     private static final int TAM_CABECALHO = 12;
     private RandomAccessFile arquivo;
     private String nomeArq;
     private Constructor<T> construtor;
     private Hash hashExt;

     public Arquivo (String nomeArq, Constructor<T> construtor) throws Exception {
          File diretorio = new File ("./dados");
          if (!diretorio.exists()) diretorio.mkdir();

          diretorio = new File ("./dados/" + nomeArq);
          if (!diretorio.exists()) diretorio.mkdir();

          this.nomeArq = "./dados/" + nomeArq + "/arquivo.dat";
          this.construtor = construtor;
          this.arquivo = new RandomAccessFile (this.nomeArq, "rw");

          if (arquivo.length() < TAM_CABECALHO) {
               arquivo.writeInt(0); //ultimo id
               arquivo.writeLong(-1); //lista excluida
          }

          this.hashExt = new Hash(nomeArq);
     }

     public int create (T obj) throws Exception {
          arquivo.seek(0);
          int newId = arquivo.readInt() + 1;
          arquivo.seek(0);
          arquivo.writeInt(newId);
          obj.setId(newId);
          byte[] dados = obj.toByteArray();

          long address = getDeleted (dados.length);

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

          hashExt.create(obj.getId(), address);

          return obj.getId();
     }

     public T read (int id) throws Exception {
          /*arquivo.seek (TAM_CABECALHO);

          while (arquivo.getFilePointer() < arquivo.length()) {
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
          return null;*/

          long address = hashExt.read(id);
          if (address == -1) return null; //indice nao encontrado

          arquivo.seek(address);
          byte lapide = arquivo.readByte();
          if (lapide != ' ') return null; //registro ja excluido

          short tamanho = arquivo.readShort();
          byte[] dados = new byte[tamanho];
          arquivo.readFully(dados);

          T obj = construtor.newInstance();
          obj.fromByteArray(dados);

          return obj;
     }

     public T[] readAll() throws Exception {
     arquivo.seek(TAM_CABECALHO);
     T[] array = (T[]) java.lang.reflect.Array.newInstance(construtor.getDeclaringClass(), 0);
     int count = 0;

     while (arquivo.getFilePointer() < arquivo.length()) {

          long pos = arquivo.getFilePointer();

          // evita ler além do arquivo
          if (pos + 3 > arquivo.length()) break;

          byte lapide = arquivo.readByte();
          short tamanho = arquivo.readShort();

          if (tamanho <= 0 || pos + 3 + tamanho > arquivo.length()) {
               break; // para leitura pra evitar EOF
          }

          byte[] dados = new byte[tamanho];
          arquivo.readFully(dados);

          if (lapide == ' ') {
               T obj = construtor.newInstance();
               obj.fromByteArray(dados);

               array = java.util.Arrays.copyOf(array, count + 1);
               array[count++] = obj;
          }
     }

     return array;
     }
     
     public boolean delete(int id) throws Exception {
          /*arquivo.seek(TAM_CABECALHO);
          
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
          return false;*/

          long pos = hashExt.read(id);
          if (pos == -1) return false;

          arquivo.seek(pos);
          arquivo.writeByte('*');

          arquivo.seek(pos + 1);
          short tamanho = arquivo.readShort();

          addDeleted(tamanho, pos);

          hashExt.delete(id);

          return true;
     }

     public boolean update(T novoObj) throws Exception {
          long pos = hashExt.read(novoObj.getId());
          if (pos == -1) return false;

          arquivo.seek(pos); 
          byte lapide = arquivo.readByte();
          if (lapide != ' ') return false;

          short tamanho = arquivo.readShort();
          byte[] novosDados = novoObj.toByteArray();
          short novoTam = (short) novosDados.length;

          if (novoTam <= tamanho) {
               arquivo.seek(pos + 1);       // pos+1 = onde fica o short de tamanho
               arquivo.writeShort(novoTam); // agora sim atualiza o tamanho
               arquivo.write(novosDados);   // escreve os dados logo em seguida (pos+3)

               int restante = tamanho - novoTam;
               if (restante > 0) arquivo.write(new byte[restante]);
          } else { //senao, move o registro para outro local
               arquivo.seek(pos);
               arquivo.writeByte('*');
               addDeleted(tamanho, pos);
               long enderecNovo = getDeleted(novosDados.length);

               if (enderecNovo == -1) {
                    arquivo.seek(arquivo.length());
                    enderecNovo = arquivo.getFilePointer();
                    arquivo.writeByte(' ');
                    arquivo.writeShort(novoTam);
                    arquivo.write(novosDados);
               } else {
                    arquivo.seek(enderecNovo);
                    arquivo.writeByte(' ');
                    arquivo.writeShort(novoTam);
                    arquivo.write(novosDados);
               }

               hashExt.update(novoObj.getId(), enderecNovo);
          }

          return true;
     }

     private void addDeleted(int tamanhoEspaco, long enderecoEspaco) throws Exception {
          long posicao = 4;
          arquivo.seek(posicao);
          long endereco = arquivo.readLong();
          long proximo;

          if (endereco == -1) {
               arquivo.seek(4);
               arquivo.writeLong(enderecoEspaco);
               if (tamanhoEspaco >= 8) {
                    arquivo.seek(enderecoEspaco + 3);
                    arquivo.writeLong(-1);
               }
          } else {
               do {
                    arquivo.seek(endereco + 1);
                    int tamanho = arquivo.readShort();
                    if (tamanho >= 8) proximo = arquivo.readLong();
                    else proximo = -1;
                         
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
               
               if (tamanho >= 8) {
                    proximo = arquivo.readLong();
               } else {
                    proximo = -1;
               }

               if (tamanho >= tamanhoNecessario) {
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

     public void close() throws Exception {
          arquivo.close();
          hashExt.close();
     }
}
