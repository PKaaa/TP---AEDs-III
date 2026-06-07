package util;

import java.util.*;
import java.io.*;
import java.nio.file.*;

public class Huffman {

    private static final String pathDiretorio = "./dados";
    private static final String arqHuffman = "./dados/backup.hf";

    private static class No implements Comparable<No> {

        byte simbolo;
        int frequencia;

        No esq;
        No dir;

        public No(byte simbolo, int frequencia) {
            this.simbolo = simbolo;
            this.frequencia = frequencia;
        }

        public No(int frequencia, No esq, No dir) {
            this.frequencia = frequencia;
            this.esq = esq;
            this.dir = dir;
        }

        public boolean folha() {
            return esq == null && dir == null;
        }

        @Override
        public int compareTo(No outro) {
            return Integer.compare(this.frequencia, outro.frequencia);
        }
    }


public static void compressao() throws Exception {

    long inicio = System.currentTimeMillis();
    
    List<File> arqs =
            salvaArqs(
                    new File(pathDiretorio)
            );

    if(arqs.isEmpty()) {

        System.out.println(
                "Nenhum arquivo encontrado em "
                + pathDiretorio
        );

        return;
    }

    ByteArrayOutputStream streamOriginal =
            new ByteArrayOutputStream();

    for(File f : arqs) {

        byte[] conteudo =
                Files.readAllBytes(
                        f.toPath()
                );

        streamOriginal.write(conteudo);
    }

    byte[] dadosOriginal =
            streamOriginal.toByteArray();

    System.out.println(
            "Tamanho original: "
            + dadosOriginal.length
            + " bytes."
    );

    int[] freq =
            contarFrequencias(
                    dadosOriginal
            );

    byte[] dadosCompactados =
            comprimeHuffman(
                    dadosOriginal
            );

    System.out.println(
            "Tamanho comprimido: "
            + dadosCompactados.length
            + " bytes."
    );

    double taxa =
            100.0 *
            (1.0 -
             ((double)dadosCompactados.length
              / dadosOriginal.length));

    System.out.printf(
            "Taxa da compressao (Huffman): %.2f%%\n",
            taxa
    );

    try(DataOutputStream dos =
            new DataOutputStream(
                    new FileOutputStream(
                            arqHuffman
                    )
            )) {

        dos.writeInt(arqs.size());

        for(File f : arqs) {

            String nomeRelativo =
                    new File(pathDiretorio)
                            .toURI()
                            .relativize(
                                    f.toURI()
                            )
                            .getPath();

            byte[] nomeBytes =
                    nomeRelativo.getBytes(
                            "UTF-8"
                    );

            dos.writeInt(
                    nomeBytes.length
            );

            dos.write(nomeBytes);

            dos.writeLong(
                    f.length()
            );
        }

        for(int i = 0; i < 256; i++) {
            dos.writeInt(freq[i]);
        }

        dos.writeInt(
                dadosOriginal.length
        );

        dos.write(
                dadosCompactados
        );
    }

    long fim = System.currentTimeMillis();

    System.out.println(
        "Tempo de compressao: "
        + (fim - inicio)
        + " ms"
    );

    System.out.println(
            "Backup Huffman criado com sucesso."
    );


    }

public static void descompressao() throws Exception {

    long inicio = System.currentTimeMillis();

    File backup =
            new File(arqHuffman);

    if(!backup.exists()) {

        System.out.println(
                "Arquivo de backup nao encontrado."
        );

        return;
    }

    try(DataInputStream dis =
            new DataInputStream(
                    new FileInputStream(
                            backup
                    )
            )) {

        int qtdArqs =
                dis.readInt();

        String[] nomes =
                new String[qtdArqs];

        long[] tamanhos =
                new long[qtdArqs];

        for(int i = 0;
            i < qtdArqs;
            i++) {

            int tamNome =
                    dis.readInt();

            byte[] nomeBytes =
                    new byte[tamNome];

            dis.readFully(
                    nomeBytes
            );

            nomes[i] =
                    new String(
                            nomeBytes,
                            "UTF-8"
                    );

            tamanhos[i] =
                    dis.readLong();
        }

        int[] freq =
                new int[256];

        for(int i = 0;
            i < 256;
            i++) {

            freq[i] =
                    dis.readInt();
        }

        int tamanhoOriginal =
                dis.readInt();

        byte[] dadosCompactados =
                dis.readAllBytes();

        byte[] dadosOriginais =
                descomprimeHuffman(
                        dadosCompactados,
                        freq,
                        tamanhoOriginal
                );

        int offSet = 0;

        for(int i = 0;
            i < qtdArqs;
            i++) {

            File destino =
                    new File(
                            pathDiretorio
                            + "/"
                            + nomes[i]
                    );

            destino.getParentFile()
                    .mkdirs();

            int tam =
                    (int)tamanhos[i];

            try(FileOutputStream fos =
                    new FileOutputStream(
                            destino
                    )) {

                fos.write(
                        dadosOriginais,
                        offSet,
                        tam
                );
            }

            offSet += tam;

            System.out.println(
                    "Restaurado: "
                    + destino.getPath()
            );
        }
    }

    long fim = System.currentTimeMillis();

    System.out.println(
        "Tempo de descompressao: "
        + (fim - inicio)
        + " ms"
    );

    System.out.println(
            "Descompactacao Huffman realizada com sucesso!"
    );


    }

    private static List<File> salvaArqs(File dir) {

        List<File> lista = new ArrayList<>();

        if (!dir.exists())
            return lista;

        File[] conteudo = dir.listFiles();

        if (conteudo == null)
            return lista;

        Arrays.sort(
            conteudo,
            Comparator.comparing(File::getPath)
        );

        for (File c : conteudo) {

            if (c.isDirectory())
                lista.addAll(salvaArqs(c));
            else
                lista.add(c);
        }

        return lista;
    }

    private static int[] contarFrequencias(byte[] dados) {

    int[] freq = new int[256];

    for (byte b : dados) {
        freq[b & 0xFF]++;
    }

    return freq;
    }

    private static No construirArvore(int[] freq) {

    PriorityQueue<No> fila =
            new PriorityQueue<>();

    for (int i = 0; i < 256; i++) {

        if (freq[i] > 0) {

            fila.add(
                new No(
                    (byte) i,
                    freq[i]
                )
            );
        }
    }

    if (fila.isEmpty())
        return null;

    while (fila.size() > 1) {

        No a = fila.poll();
        No b = fila.poll();

        fila.add(
            new No(
                a.frequencia +
                b.frequencia,
                a,
                b
            )
        );
    }

    return fila.poll();
    }

    private static void gerarCodigos(No no, String codigo, Map<Byte,String> tabela) {

    if (no == null)
        return;

    if (no.folha()) {

        tabela.put(
            no.simbolo,
            codigo.isEmpty()
                    ? "0"
                    : codigo
        );

        return;
    }

    gerarCodigos(
            no.esq,
            codigo + "0",
            tabela
    );

    gerarCodigos(
            no.dir,
            codigo + "1",
            tabela
    );
    }

    private static Map<Byte,String> criarTabelaCodigos(No raiz) {

    Map<Byte,String> tabela = new HashMap<>();

    gerarCodigos(
        raiz,
        "",
        tabela
    );

    return tabela;
    }

    private static String gerarBitsCompactados(byte[] dados, Map<Byte,String> tabela) {

    StringBuilder bits =
            new StringBuilder();

    for(byte b : dados) {
        bits.append(
            tabela.get(b)
        );
    }

    return bits.toString();
    }

    private static byte[] bitsParaBytes(String bits) {

    ByteArrayOutputStream baos =
            new ByteArrayOutputStream();

    int i = 0;

    while(i < bits.length()) {

        int valor = 0;

        for(int j = 0; j < 8; j++) {

            valor <<= 1;

            if(i < bits.length()
               && bits.charAt(i) == '1') {

                valor |= 1;
            }

            i++;
        }

        baos.write(valor);
    }

    return baos.toByteArray();
    }

    private static byte[] comprimeHuffman(byte[] dados) throws Exception {

    int[] freq =
            contarFrequencias(dados);

    No raiz =
            construirArvore(freq);

    Map<Byte,String> tabela =
            criarTabelaCodigos(raiz);

    String bits =
            gerarBitsCompactados(
                dados,
                tabela
            );

    return bitsParaBytes(bits);
    }

    private static String bytesParaBits(byte[] bytes) {

    StringBuilder bits =
            new StringBuilder();

    for(byte b : bytes) {

        int valor = b & 0xFF;

        String binario =
                String.format(
                        "%8s",
                        Integer.toBinaryString(valor)
                ).replace(' ', '0');

        bits.append(binario);
    }

    return bits.toString();
    }

    private static byte[] descomprimeHuffman(byte[] dadosCompactados, int[] freq, int tamanhoOriginal) throws Exception {

    No raiz =
            construirArvore(freq);

    String bits =
            bytesParaBits(
                    dadosCompactados
            );

    ByteArrayOutputStream saida =
            new ByteArrayOutputStream();

    No atual = raiz;

    int i = 0;

    while(i < bits.length()
            && saida.size()
            < tamanhoOriginal) {

        char bit =
                bits.charAt(i);

        if(bit == '0')
            atual = atual.esq;
        else
            atual = atual.dir;

        if(atual.folha()) {

            saida.write(
                    atual.simbolo
            );

            atual = raiz;
        }

        i++;
    }

    return saida.toByteArray();
    }

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            System.out.println("Uso: java util.Huffman c");
            System.out.println("     java util.Huffman d");
            return;
        }

        
        switch (args[0].toLowerCase()) {

            case "c":
                compressao();
                break;

            case "d":
                descompressao();
                break;

            default:
                System.out.println("Opcao invalida.");
        }
    }
    
}
