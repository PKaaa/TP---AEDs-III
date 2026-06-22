package service.busca;

public class KMP {

     //verifica se 'pattern' ocorre dentro de 'text'. 
     public boolean contains(String text, String pattern) {
          return buscar(text, pattern) != -1;
     }

     //Retorna o índice da primeira ocorrência do padrão no texto, ou -1 se não for encontrado.
     public int buscar(String text, String pattern) {
          if (text == null || pattern == null) return -1;
          if (pattern.isEmpty()) return 0;
          if (pattern.length() > text.length()) return -1;

          //normaliza para busca case-insensitive
          String t = text.toLowerCase();
          String p = pattern.toLowerCase();

          int[] falha = construirTabelaFalha(p);

          int i = 0; //ponteiro no texto
          int j = 0; //ponteiro no padrão

          while (i < t.length()) {
               if (t.charAt(i) == p.charAt(j)) {
                    i++;
                    j++;

                    if (j == p.length()) {
                         //padrão encontrado, retorna o índice de início
                         return i - j;
                    }
               } else if (j > 0) {
                    //mismatch: usa a tabela de falha para "pular" no padrão sem retroceder no texto
                    j = falha[j - 1];
               } else {
                    //j == 0 e não bateu: avança no texto
                    i++;
               }
          }

          return -1; //não encontrado
     }

     //retorna todas as posições onde o padrão ocorre no texto.
     public java.util.List<Integer> buscarTodas(String text, String pattern) {
          java.util.List<Integer> ocorrencias = new java.util.ArrayList<>();
          if (text == null || pattern == null || pattern.isEmpty()) return ocorrencias;

          String t = text.toLowerCase();
          String p = pattern.toLowerCase();

          if (p.length() > t.length()) return ocorrencias;

          int[] falha = construirTabelaFalha(p);

          int i = 0;
          int j = 0;

          while (i < t.length()) {
               if (t.charAt(i) == p.charAt(j)) {
                    i++;
                    j++;

                    if (j == p.length()) {
                         ocorrencias.add(i - j);
                         j = falha[j - 1]; //continua buscando outras ocorrências
                    }
               } else if (j > 0) {
                    j = falha[j - 1];
               } else {
                    i++;
               }
          }

          return ocorrencias;
     }

     //constrói a tabela de falha (função de prefixo) do padrão.
     private int[] construirTabelaFalha(String pattern) {
          int m = pattern.length();
          int[] falha = new int[m];
          falha[0] = 0;

          int comprimento = 0; //tamanho do prefixo/sufixo atual
          int i = 1;

          while (i < m) {
               if (pattern.charAt(i) == pattern.charAt(comprimento)) {
                    comprimento++;
                    falha[i] = comprimento;
                    i++;
               } else if (comprimento > 0) {
                    //recua usando a própria tabela de falha
                    comprimento = falha[comprimento - 1];
               } else {
                    falha[i] = 0;
                    i++;
               }
          }

          return falha;
     }
}
