import java.io.*;
import java.lang.Math;

public class Hash {
    private static final int capacidade = 4; 
    private RandomAccessFile arqDiretorio;
    private RandomAccessFile arqBucket;

    public Hash (String nomeArq) throws Exception {
        File diretorio = new File("./dados/" + nomeArq);
        if (!diretorio.exists()) diretorio.mkdir(); //se o arquivo do diretorio nao existir, irá criar um novo

        String diretorioPath = "./dados/" + nomeArq + "/diretorio.dat";
        String bucketPath = "./dados/" + nomeArq + "/bucket.dat";

        arqDiretorio = new RandomAccessFile (diretorioPath, "rw");
        arqBucket = new RandomAccessFile(bucketPath, "rw");

        if (arqDiretorio.length() == 0) { //se os arquivos forem novos, a estrutura já será incializada
            arqDiretorio.writeInt(0); //profundidade global
            arqDiretorio.writeLong(0); //offset nos buckets

            escreverBucket(0, 0, new int[capacidade], new long[capacidade], 0);
        }
    }

    ///////////////////////////////////////
    //Métodos

    public boolean create (int id, long endereco) throws Exception { 
        long indexBucket = buscarIndexB(id);

        Bucket bk = lerBucket(indexBucket);

        for (int i = 0; i < bk.quantidade; i++) {
            if(bk.chaves[i] == id) return false; //verifica se ja existe duplicatas
        }

        if (bk.quantidade < capacidade) {
            bk.chaves[bk.quantidade] = id;
            bk.enderecos[bk.quantidade] = endereco;
            bk.quantidade++;
            escreverBucket(indexBucket, bk.profundidadeLocal, bk.chaves, bk.enderecos, bk.quantidade);
            return true;
        }

        split(indexBucket, bk, id, endereco); //se o bucket estiver cheio, reparte;
        return true;
    }

    public long read (int id) throws Exception{
        long indexBucket = buscarIndexB(id);
        
        Bucket bk = lerBucket(indexBucket);

        for (int i = 0; i < bk.quantidade; i++) {
            if (bk.chaves[i] == id) return bk.enderecos[i];
        }

        return -1;
    } 

    public boolean update (int id, long novoEndereco) throws Exception {
        long indexBucket = buscarIndexB(id);

        Bucket bk = lerBucket (indexBucket);

        for (int i = 0; i < bk.quantidade; i++) {
            if (bk.chaves[i] == id) {
                bk.enderecos[i] = novoEndereco;
                escreverBucket(indexBucket, bk.profundidadeLocal, bk.chaves, bk.enderecos, bk.quantidade);
                return true;
            }
        }
        return false;
    }

    public boolean delete(int id) throws Exception {
        long indexBucket = buscarIndexB(id);

        Bucket bk = lerBucket(indexBucket);

        for (int i = 0; i < bk.quantidade; i++) {
            if (bk.chaves[i] == id) { //remove da array compactando
                bk.chaves[i] = bk.chaves[bk.quantidade - 1];
                bk.enderecos[i] = bk.enderecos[bk.quantidade - 1];
                bk.quantidade--;
                escreverBucket(indexBucket, bk.profundidadeLocal, bk.chaves, bk.enderecos, bk.quantidade);
                return true;
            }
        }

        return false;
    }

    public void close() throws IOException {
        arqDiretorio.close();
        arqBucket.close();
    }

    //////////////////////////////////////////
    //Lógica interna

    private int hash(int chave, int p) throws Exception {
        if (p == 0) return 0;
        return (int) (chave % Math.pow(2, p)); //hash: k % 2^p
    }

    private void escreverBucket(long indexBucket, int profLocal, int[] chaves, long[] enderecos, int qtd) throws Exception {
        arqBucket.seek(indexBucket);
        arqBucket.writeInt(profLocal);
        arqBucket.writeInt(qtd);

        for (int i = 0; i < capacidade; i++) {
            arqBucket.writeInt(i < qtd? chaves[i] : 0);
            arqBucket.writeLong(i < qtd? enderecos[i] : -1L);
        }
    }

    private long buscarIndexB(int chave) throws Exception {
        int prof = lerProfGlobal();
        int idx = hash (chave, prof);
        return lerEntradaDir(idx);
    }

    private int lerProfGlobal() throws Exception {
        arqDiretorio.seek(0);
        return arqDiretorio.readInt(); //retorna profundidade atual
    }

    private long lerEntradaDir(int index) throws Exception {
        arqDiretorio.seek(4L + (long) index * 8); 
        return arqDiretorio.readLong();
    }
        
    private Bucket lerBucket(long index) throws Exception { //preenche o bucket a partir do index
        arqBucket.seek(index);
        Bucket bk = new Bucket();
        
        bk.profundidadeLocal = arqBucket.readInt();
        bk.quantidade = arqBucket.readInt();

        for (int i = 0; i < capacidade; i++) {
            bk.chaves[i] = arqBucket.readInt();
            bk.enderecos[i] = arqBucket.readLong();
        }

        return bk;
    }
    

    /////////////////////////////////////////////////
    private void split (long indexBucket, Bucket bk, int chaveNova, long enderecNovo) throws Exception {
        int profGlobal = lerProfGlobal();

        if (bk.profundidadeLocal == profGlobal) {
            dobraDiretorio(profGlobal);
            profGlobal++;
        }

        int profLocalNovo = bk.profundidadeLocal + 1;
        long indexNovo = criaNovoBucket(profLocalNovo);
        int tam = tamDir(profGlobal);

        for (int i = 0; i < tam; i++) {
            if (lerEntradaDir(i) == indexBucket) {
                if ((i &(1 << (profLocalNovo - 1))) != 0) {
                    escreverEntradaDir(i, indexNovo);
                }
            }
        }

        int[] chaves = new int[capacidade + 1];
        long[] enderecos = new long[capacidade + 1];

        for (int i = 0; i < bk.quantidade; i++) {
            chaves[i] = bk.chaves[i];
            enderecos[i] = bk.enderecos[i];
        }

        chaves[bk.quantidade] = chaveNova;
        enderecos[bk.quantidade] = enderecNovo;
        int total = bk.quantidade + 1;

        //zera os dois buckets
        int[]  chavesOrig = new int [capacidade];
        long[] endsOrig   = new long[capacidade];
        int[]  chavesNovo = new int [capacidade];
        long[] endsNovo   = new long[capacidade];
        int qtdOrig = 0, qtdNovo = 0;

        for (int i = 0; i < total; i++) {
            int idx = hash(chaves[i], profGlobal);
            if ((lerEntradaDir(idx)) == indexNovo) {
                chavesNovo[qtdNovo]   = chaves[i];
                endsNovo  [qtdNovo++] = enderecos[i];
            } else {
                chavesOrig[qtdOrig]   = chaves[i];
                endsOrig  [qtdOrig++] = enderecos[i];
            }
        }

        escreverBucket(indexBucket, profLocalNovo, chavesOrig, endsOrig, qtdOrig);
        escreverBucket(indexNovo, profLocalNovo, chaves, enderecos, qtdNovo);

        if (qtdOrig == capacidade + 1 || qtdNovo == capacidade + 1) { //se um dos buckets ainda estiver cheio e ainda ter recebido todas as chaves, tenta recursivamente (só por segurança)
            delete(chaveNova);
            create(chaveNova, enderecNovo);
        }
    }

    private void dobraDiretorio(int profAtual) throws Exception {
        int tamAtual = tamDir(profAtual);
        int tamNovo = tamAtual * 2;
        long[] entradasAtuais = new long[tamAtual];

        for (int i = 0; i < tamAtual; i++) {
            entradasAtuais[i] = lerEntradaDir(i);
        }

        arqDiretorio.setLength(4L + (long) tamNovo * 8); //reescreve o tamanho do diretorio dobrado

        for (int i = 0; i < tamAtual; i++) {
            escreverEntradaDir(i, entradasAtuais[i]);
            escreverEntradaDir(i + tamAtual, entradasAtuais[i]);
        }

        escreverProfGlobal(profAtual + 1);
    }

    private int tamDir (int p) {
        return (int) Math.pow(2, p); //2^p
    }

    private void escreverProfGlobal (int prof) throws Exception {
        arqDiretorio.seek(0);
        arqDiretorio.writeInt(prof);
    }

    private void escreverEntradaDir(int i, long index) throws Exception {
        arqDiretorio.seek(4L + (long) i * 8);
        arqDiretorio.writeLong(index); 
    }
    
    private long criaNovoBucket(int profLocal) throws Exception {
        long index = arqBucket.length();
        escreverBucket(index, profLocal, new int[capacidade], new long[capacidade], 0);
        return index;
    }

    ///////////////////////////////////////////
    //Classe auxiliar 
    class Bucket {
        int profundidadeLocal = 0;
        int quantidade = 0;
        int[] chaves = new int[capacidade];
        long[] enderecos = new long[capacidade];
    }
}