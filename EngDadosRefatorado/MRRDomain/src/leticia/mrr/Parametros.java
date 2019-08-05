package leticia.mrr;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Parametros {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	 private int id;

 
	private int ultimoMesFechado;
	
	public int getUltimoMesFechado() {
 		return ultimoMesFechado;
	}

	public void setUltimoMesFechado(int mesFechado) {
		ultimoMesFechado = mesFechado;
		
	}

}
