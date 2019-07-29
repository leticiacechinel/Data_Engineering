package leticia.mrr;

import java.io.Serializable;

public class NomeMesJson implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private String nome;
	private int mes;
	
	public String getNome() {
		return nome;
	}
	
	public int getMes() {
		return mes;
	}
}
