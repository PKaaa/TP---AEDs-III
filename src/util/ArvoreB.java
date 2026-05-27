package util;

import java.io.*;
import java.util.*;

public class ArvoreB {
     protected static final int ordemInterna = 4; //max chaves por nó interno
     protected static final int ordemFolha = 4; //max pares por nó folha
     private static final long offSetRaiz = 8; //offset do endereço da raiz no arquivo
     protected static RandomAccessFile arq;

     public ArvoreB (String nomeArq) throws Exception {
          File diretorio = new File ("./dados/" + nomeArq);
          if (!diretorio.exists()) diretorio.mkdir(); //se o arquivo nao existir, irá criar um novo
          arq = new RandomAccessFile("./dados/" + nomeArq + "/arvoreB.dat", "rw");

          if (arq.length() == 0) { //se os arquivos forem novos, a estrutura já será inicializada
               arq.writeInt(ordemInterna); //ordem interna
               arq.writeInt(ordemFolha); //ordem folha
               arq.writeLong(-1); //endereço da raiz vazia
               arq.writeInt(0); //total de nós
          }
     }

          //Métodos
     public void create (int chave, long endereco) throws Exception {
          long posRaiz = lerRaiz();

          if (posRaiz == -1) { //se arvores estiver vazia, cria a raiz como nó folha
               long novaFolha = NoFolha.criaNoFolha();
               NoFolha nf = new NoFolha();
               nf.chaves[0] = chave;
               nf.enderecos[0] = endereco;
               nf.qtdPares = 1;
               NoFolha.escreveNoFolha(novaFolha, nf);
               escreveRaiz(novaFolha);
               return;
          }

          ResultadoSplit resp = ResultadoSplit.inserir(posRaiz, chave, endereco);
          if (resp != null) { //se a raiz foi dividida, cria um novo nó interno como raiz
               long novaRaiz = NoInterno.criaNoInterno();
               NoInterno ni = NoInterno.lerNoInterno(novaRaiz);
               ni.chaves[0] = resp.chavePromovida;
               ni.enderecos[0] = posRaiz;
               ni.enderecos[1] = resp.offSetNovoDir;
               ni.quantidade = 1;
               NoInterno.escreveNoInterno(novaRaiz, ni);
               escreveRaiz(novaRaiz);
          }
     }

     public long[] listarOrdenado() throws Exception {
          long posRaiz = lerRaiz();
          if (posRaiz == -1) return new long[0]; 

          long offsetFolha = desceFolhaEsquerda(posRaiz);

          //percorre as folhas encadeadas e coleta os endereços
          List<Long> enderecos = new ArrayList<>();
          while (offsetFolha != -1) {
               NoFolha nf = NoFolha.lerNoFolha(offsetFolha);
               for (int i = 0; i < nf.qtdPares; i++) enderecos.add(nf.enderecos[i]);
               offsetFolha = nf.folhaProx;
          }

          long[] result = new long[enderecos.size()];
          for (int i = 0; i < result.length; i++) result[i] = enderecos.get(i); //conversao para array

          return result;
     }

     public boolean update (int chave, long newEndereco) throws Exception {
          long raiz = lerRaiz();
          if (raiz == -1) return false;

          long offsetFolha = NoFolha.buscarFolha(raiz, chave);
          NoFolha nf = NoFolha.lerNoFolha(offsetFolha);

          for (int i = 0; i < nf.qtdPares; i++) {
               if (nf.chaves[i] == chave) {
                    nf.enderecos[i] = newEndereco;
                    NoFolha.escreveNoFolha(offsetFolha, nf);
                    return true;
               }
          }
          return false;
     }

     public boolean delete(int chave) throws Exception {
          long raiz = lerRaiz();
          if (raiz == -1) return false; //arvore vazia
          return ResultadoSplit.delete(raiz, chave, -1, 0);
     }

     public void close() throws IOException {
          arq.close();
     }

     //Lógica interna
     private long desceFolhaEsquerda (long offset) throws Exception {
          byte tipo = ResultadoSplit.lerTipo(offset);
          if (tipo == 'F') return offset;

          NoInterno ni = NoInterno.lerNoInterno(offset);
          return desceFolhaEsquerda(ni.enderecos[0]);
     }

     private long lerRaiz() throws Exception {
          arq.seek(offSetRaiz);
          return arq.readLong();
     }
     
     private void escreveRaiz (long endereco) throws Exception {
          arq.seek(offSetRaiz);
          arq.writeLong(endereco);
     }
}

class NoInterno {
     int quantidade = 0; //quantidade de chaves
     int[] chaves = new int[ArvoreB.ordemInterna]; //chaves do nó
     long[] enderecos = new long[ArvoreB.ordemInterna + 1]; //endereços dos filhos

     public static long criaNoInterno() throws Exception {
          long pos = ArvoreB.arq.length();     
          NoInterno ni = new NoInterno();
          escreveNoInterno(pos, ni);
          return pos;
     }

     public static NoInterno lerNoInterno(long offset) throws Exception {
          ArvoreB.arq.seek(offset);
          NoInterno ni = new NoInterno();
          ArvoreB.arq.readByte();
          ni.quantidade = ArvoreB.arq.readInt();

          for (int i = 0; i < ArvoreB.ordemInterna; i++) ni.chaves[i] = ArvoreB.arq.readInt();
          for (int i = 0; i <= ArvoreB.ordemInterna; i++) ni.enderecos[i] = ArvoreB.arq.readLong();

          return ni;
     }

     public static void escreveNoInterno(long endereco, NoInterno ni) throws Exception {
          ArvoreB.arq.seek(endereco);
          ArvoreB.arq.writeByte('I');
          ArvoreB.arq.writeInt(ni.quantidade);

          for (int i = 0; i < ArvoreB.ordemInterna; i++) ArvoreB.arq.writeInt(ni.chaves[i]);
          for (int i = 0; i <= ArvoreB.ordemInterna; i++) ArvoreB.arq.writeLong(ni.enderecos[i]);
     }
}

class NoFolha {
     int qtdPares = 0;
     int[] chaves = new int[ArvoreB.ordemFolha];
     long[] enderecos = new long[ArvoreB.ordemFolha];
     long folhaProx = -1;

     public static long criaNoFolha() throws Exception {
          long pos = ArvoreB.arq.length();     
          NoFolha nF = new NoFolha();
          escreveNoFolha(pos, nF);
          return pos;
     }

     public static long buscarFolha (long offset, int chave) throws Exception {
          byte tipo = ResultadoSplit.lerTipo(offset);
          if (tipo == 'F') return offset;

          NoInterno ni = NoInterno.lerNoInterno(offset);
          int idx = 0;
          while (idx < ni.quantidade && chave >= ni.chaves[idx]) idx++;
          return buscarFolha(ni.enderecos[idx], chave);
     }

     public static void escreveNoFolha(long endereco, NoFolha nf) throws Exception {
          ArvoreB.arq.seek(endereco);
          ArvoreB.arq.writeByte('F');
          ArvoreB.arq.writeInt(nf.qtdPares);

          for (int i = 0; i < ArvoreB.ordemFolha; i++) ArvoreB.arq.writeInt(nf.chaves[i]);
          for (int i = 0; i < ArvoreB.ordemFolha; i++) ArvoreB.arq.writeLong(nf.enderecos[i]);

          ArvoreB.arq.writeLong(nf.folhaProx);
     }

     protected static NoFolha lerNoFolha(long offset) throws Exception {
          ArvoreB.arq.seek(offset);
          NoFolha nf = new NoFolha();
          ArvoreB.arq.readByte();
          nf.qtdPares = ArvoreB.arq.readInt();

          for (int i = 0; i < ArvoreB.ordemFolha; i++) nf.chaves[i] = ArvoreB.arq.readInt();
          for (int i = 0; i < ArvoreB.ordemFolha; i++) nf.enderecos[i] = ArvoreB.arq.readLong();

          nf.folhaProx = ArvoreB.arq.readLong();
          return nf;
     }
}

class ResultadoSplit {
     int chavePromovida;
     long offSetNovoDir;

     public static ResultadoSplit inserir(long offset, int chave, long endereco) throws Exception {
          byte tipo = lerTipo (offset);
          if (tipo == 'F') {
               NoFolha nf = NoFolha.lerNoFolha(offset);
               for (int i = 0; i < nf.qtdPares; i ++) if (nf.chaves[i] == chave) return null; //verifica se há duplicatas

               int pos = 0;
               while (pos < nf.qtdPares && nf.chaves[pos] < chave) pos++;//insere ordenado

               for (int i = nf.qtdPares; i > pos; i--) {
                    nf.chaves[i] = nf.chaves[i - 1];
                    nf.enderecos[i] = nf.enderecos[i - 1];
               }
               nf.chaves[pos] = chave;
               nf.enderecos[pos] = endereco;
               nf.qtdPares++;

               if (nf.qtdPares <= ArvoreB.ordemFolha) { //se nao precisar dividir, apenas escreve o nó atualizado
                    NoFolha.escreveNoFolha(offset, nf);
                    return null;
               }

               return splitFolha(offset, nf);
          } else {
               NoInterno ni = NoInterno.lerNoInterno(offset);
               int idx = 0;
               while (idx < ni.quantidade && chave >= ni.chaves[idx]) idx++;

               ResultadoSplit resp = inserir(ni.enderecos[idx], chave, endereco);
               if (resp == null) return null; //se nao precisar dividir, apenas retorna no filho

               //se foi dividiro, precisa inserir a chave promovida no nó interno
               int pos = 0;
               while (pos < ni.quantidade && ni.chaves[pos] < resp.chavePromovida) pos++;
               for (int i = ni.quantidade; i > pos; i--) {
                    ni.chaves[i] = ni.chaves[i - 1];
                    ni.enderecos[i + 1] = ni.enderecos[i];
               }

               ni.chaves[pos] = resp.chavePromovida;
               ni.enderecos[pos + 1] = resp.offSetNovoDir;
               ni.quantidade++;

               if (ni.quantidade <= ArvoreB.ordemInterna) { //se nao precisar dividir, apenas escreve o nó atualizado
                    NoInterno.escreveNoInterno(offset, ni);
                    return null;
               }

               return splitInterno(offset, ni);
          }
     }

     public static boolean delete(long offset, int chave, long pai, int idxPai) throws Exception {
          byte tipo = lerTipo (offset);

          if (tipo == 'F') {
               NoFolha nf = NoFolha.lerNoFolha(offset);
               for (int i = 0; i < nf.qtdPares; i++) {
                    if (nf.chaves[i] == chave) {
                         for (int j = i; j < nf.qtdPares - 1; j++) {
                              nf.chaves[j] = nf.chaves[j + 1];
                              nf.enderecos[j] = nf.enderecos[j + 1];
                         }
                         nf.qtdPares--;
                         NoFolha.escreveNoFolha(offset, nf);
                         return true;
                    }
               }
               return false;
          } else {
               NoInterno ni = NoInterno.lerNoInterno(offset);
               int idx = 0;
               while (idx < ni.quantidade && chave >= ni.chaves[idx]) idx++;
               return delete(ni.enderecos[idx], chave, offset, idx);
          }
     }

     private static ResultadoSplit splitFolha (long offset, NoFolha nf) throws Exception {
          int meio = (ArvoreB.ordemFolha + 1) / 2;

          long offsetNovo = NoFolha.criaNoFolha();
          NoFolha nfNovo = NoFolha.lerNoFolha(offsetNovo);

          nfNovo.qtdPares = nf.qtdPares - meio;

          for (int i = 0; i < nfNovo.qtdPares; i++) {
               nfNovo.chaves[i] = nf.chaves[meio + i];
               nfNovo.enderecos[i] = nf.enderecos[meio + i];
          }

          nfNovo.folhaProx = nf.folhaProx;
          nf.folhaProx = offsetNovo;
          nf.qtdPares = meio;

          NoFolha.escreveNoFolha(offset, nf);
          NoFolha.escreveNoFolha(offsetNovo, nfNovo);

          ResultadoSplit resp = new ResultadoSplit();
          resp.chavePromovida = nfNovo.chaves[0];
          resp.offSetNovoDir = offsetNovo;
          return resp;
     }

     private static ResultadoSplit splitInterno (long offset, NoInterno ni) throws Exception {
          int meio = (ArvoreB.ordemInterna + 1) / 2;

          long offsetNovo = NoInterno.criaNoInterno();
          NoInterno niNovo = NoInterno.lerNoInterno(offsetNovo);

          niNovo.quantidade = ni.quantidade - meio - 1;

          for (int i = 0; i < niNovo.quantidade; i++) {
               niNovo.chaves[i] = ni.chaves[meio + 1 + i];
               niNovo.enderecos[i] = ni.enderecos[meio + 1 + i];
          }
          niNovo.enderecos[niNovo.quantidade] = ni.enderecos[ni.quantidade + 1];

          int chavePromovida = ni.chaves[meio];
          ni.quantidade = meio;

          NoInterno.escreveNoInterno(offset, ni);
          NoInterno.escreveNoInterno(offsetNovo, niNovo);

          ResultadoSplit resp = new ResultadoSplit();
          resp.chavePromovida = chavePromovida;
          resp.offSetNovoDir = offsetNovo;
          return resp;
     }

     protected static byte lerTipo (long offset) throws Exception {
          ArvoreB.arq.seek(offset);
          return ArvoreB.arq.readByte();
     }
}
