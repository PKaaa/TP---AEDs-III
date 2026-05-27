package dao;

import java.util.*;

import model.ReceitaAlimento;
import util.Arquivo;

public class ReceitaAlimentoDAO {
     private Arquivo <ReceitaAlimento> arq;

     public ReceitaAlimentoDAO () throws Exception {
          arq = new Arquivo<>("receitaAlimento", ReceitaAlimento.class.getConstructor());
     }

     //CREATE
     public boolean createRelacao(int idR, int idA) throws Exception {
          if (buscarRelacao(idR, idA) != null) return false; //confere se a relação já existe antes de criar
          ReceitaAlimento ra = new ReceitaAlimento(idR, idA);
          return arq.create(ra) > 0;
     }

     private ReceitaAlimento buscarRelacao (int idR, int idA) throws Exception {
          for (ReceitaAlimento ra : arq.readAll()) {
               if (ra.getIdAlimento() == idA && ra.getIdReceita() == idR) {
                    return ra;
               }
          }
          return null;
     }

     //DELETE
     public boolean deleteRelacao (int idR, int idA) throws Exception {
          ReceitaAlimento ra = buscarRelacao(idR, idA);
          if (ra == null) return false; //confere se a relação existe antes de tentar excluir
          return arq.delete(ra.getId());
     }

     public void deleteReceita (int idR) throws Exception {
          for (ReceitaAlimento ra : arq.readAll()) {
               if (ra.getIdReceita() == idR) {
                    arq.delete(ra.getId());
               }
          }
     }

     public void deleteAlimento (int idA) throws Exception {
          for (ReceitaAlimento ra : arq.readAll()) {
               if (ra.getIdAlimento() == idA) {
                    arq.delete(ra.getId());
               }
          }
     }

     //LIST
     public int[] listarAlimentosReceita (int idR) throws Exception {
          int[] ids = new int[0];
          int count = 0;

          for (ReceitaAlimento ra : arq.readAll()) {
               if (ra.getIdReceita() == idR) {
                    ids = Arrays.copyOf(ids, count + 1);
                    ids[count++] = ra.getIdAlimento();
               }
          }
          return ids;
     }

     public int[] listarReceitasAlimento (int idA) throws Exception {
          int[] ids = new int[0];
          int count = 0;

          for (ReceitaAlimento ra : arq.readAll()) {
               if (ra.getIdAlimento() == idA) {
                    ids = Arrays.copyOf(ids, count + 1);
                    ids[count++] = ra.getIdReceita();
               }
          }
          return ids;
     }
}
