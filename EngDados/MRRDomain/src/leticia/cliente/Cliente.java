package leticia.cliente;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Cliente implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	@Id
	private int id;
	private String nome;
	private String cidade;
	private String estado;
	private String segmento;
	
	public Cliente() {
 	}

	public Cliente(int id, String nome, String cidade, String uf, String segmento) {
		this.id = id;
		this.nome = nome;
		this.cidade = cidade;
		this.estado = uf;
		this.segmento = segmento;
	}

	
	public String getNome() {
		return nome;
	}
	
	public String getCidade() {
		return cidade;
	}
	
	public String getEstado() {
		return estado;
	}
	
	public String getSegmento() {
		return segmento;
	}
	
	public int getId() {
		return id;
	}
	
	public void setNome(String nome) {
		this.nome = nome;
	}
	
	public void setCidade(String cidade) {
		this.cidade = cidade;
	}
	
	public void setSegmento(String segmento) {
		this.segmento = segmento;
	}
	
	public void setEstado(String estado) {
		this.estado = estado;
	}
	
	public void setId(int id) {
		this.id = id;
	}
}
