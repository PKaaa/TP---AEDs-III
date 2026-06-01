     package util;

import java.util.*;
import java.io.*;
import java.nio.file.*;

public class LZW {
     private static final String pathDiretorio = "./dados";
     private static final String arqLzw = "./dados/backup.hl"; //hl = huffman e lzw
     private static final int tamDicionario = 65536; //16 bits. tamanho max de entradas suportadas no dicionario

     public static void main (String[] args) throws Exception {
          if (args.length < 1) {
               System.out.println("Uso: java util.LZW c   (comprime)");
               System.out.println("     java util.LZW d   (descomprime)");
               return;
          }

          switch (args[0].toLowerCase()) {
               case "c": compressao(); break;
               case "d": descompressao(); break;
               default:
                    System.out.println("Opcao invalida. Use 'c' para comprimir ou 'd' para descomprimir.");
          }
     }

     public static void compressao() throws Exception {
          List<File> arqs = salvaArqs(new File(pathDiretorio));

          if (arqs.isEmpty()) {
               System.out.println("Nenhum arquivo encontrado em " + pathDiretorio);
               return;
          }

          ByteArrayOutputStream streamOriginal = new ByteArrayOutputStream(); //concatenação dos bits

          for (File f : arqs) {
               byte[] conteudo = Files.readAllBytes(f.toPath());
               streamOriginal.write(conteudo);
          }
          byte[] dadosOriginal = streamOriginal.toByteArray();

          System.out.println("Tamanho original: " + dadosOriginal.length + " bytes.");

          //Compressao em si
          int[] comprimido = comprimeLZW(dadosOriginal);
          byte[] dadosComprimidos = intToByte(comprimido);

          System.out.println("Tamanho comprimido: " + dadosComprimidos.length + " bytes.");

          double taxa = 100.0 * (1.0 - (double) dadosComprimidos.length/dadosOriginal.length);
          System.out.printf("Taxa da compressao (LZW): %.2f%%\n", taxa);

          //gravação no arquivo de backup
          try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(arqLzw))){
               dos.writeInt(arqs.size()); //cabeçalho
               for (File f : arqs) { 
                    String nomeRelativo = new File(pathDiretorio).toURI().relativize(f.toURI()).getPath();
                    byte[] nomeBytes = nomeRelativo.getBytes("UTF-8");

                    dos.writeInt(nomeBytes.length);
                    dos.write(nomeBytes);
                    dos.writeLong(f.length());
               }

               dos.write(dadosComprimidos); //dados comprimidos
          }
     }

     public static void descompressao() throws Exception {
          File backup = new File(arqLzw);
          if (!backup.exists()) {
               System.out.println("Arquivo de backup nao encontrado/nao existe.");
               return;
          }

          try (DataInputStream dis = new DataInputStream(new FileInputStream(backup))) {
               //leitura do cabeçalho
               int qtdArqs = dis.readInt();
               String[] nomes = new String[qtdArqs];
               long[] tamanhos = new long[qtdArqs];

               for (int i = 0; i < qtdArqs; i++) {
                    int tamNome = dis.readInt();
                    byte[] nomeBytes = new byte[tamNome];
                    dis.readFully(nomeBytes);
                    nomes[i] = new String(nomeBytes, "UTF-8");
                    tamanhos[i] = dis.readLong();
               }

               byte[] dadosComprimidos = dis.readAllBytes();
               //descompressao
               int[] codigos = byteToInt(dadosComprimidos);
               byte[] dadosOriginal = descomprimeLZW(codigos);
               
               //faz a distribuição original dos bytes para o arquivo
               int offSet = 0; 
               for (int i = 0; i < qtdArqs; i++) {
                    File destino = new File(pathDiretorio + "/" + nomes[i]);
                    destino.getParentFile().mkdirs();

                    int tam = (int) tamanhos[i];
                    try(FileOutputStream fos = new FileOutputStream(destino)) {
                         fos.write(dadosOriginal, offSet, tam);
                    }

                    offSet += tam;
                    System.out.println("Restaurado: " + destino.getPath());
               }
          }

          System.out.println("Descompactacao (LZW) realizada com sucesso!");
     }

     //Lógica interna
     private static List<File> salvaArqs(File dir) {
          List<File> lista = new ArrayList<>();
          if (!dir.exists()) return lista;

          File[] conteudo = dir.listFiles();
          if (conteudo == null) return lista;

          Arrays.sort(conteudo, Comparator.comparing(File::getPath));

          for (File c : conteudo) {
               if (c.isDirectory()) lista.addAll(salvaArqs(c));
               else lista.add(c);
          }

          return lista;
     }

     private static int[] comprimeLZW(byte[] dados) {
          Map<String, Integer> dicionario = new HashMap<>();
          for (int i = 0; i < 256; i++) dicionario.put(String.valueOf((char) i), i); //inicializa com 256 simbolos
          
          List<Integer> saida = new ArrayList<>();
          String w = "";
          int Codprox = 256;
          for (byte b : dados) {
               //converte os bytes para char sem sinal 
               char c = (char) (b & 0xFF);
               String wc = w + c;

               if (dicionario.containsKey(wc)) w = wc;
               else {
                    saida.add(dicionario.get(w));
                    if (Codprox < tamDicionario) dicionario.put(wc, Codprox++);

                    w = String.valueOf(c);
               }
          }

          if (!w.isEmpty()) saida.add(dicionario.get(w));

          return saida.stream().mapToInt(Integer::intValue).toArray();
     }

     private static byte[] descomprimeLZW(int[] dados) throws Exception {
          if (dados.length == 0) return new byte[0];

          Map<Integer, String> dicionario = new HashMap<>();
          for (int i = 0; i < 256; i++) dicionario.put(i, String.valueOf((char) i));

          ByteArrayOutputStream saida = new ByteArrayOutputStream();
          int Codprox = 256;

          String w = dicionario.get(dados[0]);
          toString(saida, w);

          for (int i = 1; i < dados.length; i++) {
               int cod = dados[i];
               String entrada;

               if (dicionario.containsKey(cod)) entrada = dicionario.get(cod);
               else if (cod == Codprox) entrada = w + w.charAt(0); //caso o caracter nao esteja no dicionario. caso raro
               else throw new Exception("Codigo LZW invalido: " + cod);

               toString(saida, entrada);

               if (Codprox < tamDicionario) dicionario.put(Codprox++, w + entrada.charAt(0));

               w = entrada;
          }

          return saida.toByteArray();
     }

     private static byte[] intToByte(int[] ints) {
          byte[] result = new byte[ints.length * 2];
          for (int i = 0; i < ints.length; i++) {
               result[i * 2] = (byte) ((ints[i] >> 8) & 0xFF); //8 bits mais significativos
               result[i * 2 + 1] = (byte) (ints[i] & 0xFF); //8 bits menos significativos
          }

          return result;
     }

     private static int[] byteToInt(byte[] bytes) {
          int[] result = new int[bytes.length/2];
          for (int i = 0; i < result.length; i++) result[i] = ((bytes[i * 2] & 0xFF) << 8) | (bytes[i * 2 + 1] & 0xFF); //reconstrói o inteiro a partir dos 2 bytes
          return result;
     }

     private static void toString(ByteArrayOutputStream baos, String s) {
          for (char c : s.toCharArray()) {
               baos.write((byte) c);
          }
     }
}
