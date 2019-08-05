package leticia.mrr;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import leticia.cliente.Cliente;

@Entity
public class Metrica implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	 private int id;

	private int clienteID;
	private String estado;
	private String segmento;
	private String cidade;
	private int mes;
	private Tipo tipo;
	private double valor;
	
	public Metrica() {
 	}

	public Metrica(Cliente cliente, int mes, Tipo tipo, double valor) {
		this.clienteID = cliente.getId();
		this.estado = cliente.getEstado();
		this.cidade = cliente.getCidade();
		this.segmento = cliente.getSegmento();
		this.mes = mes;
		this.tipo = tipo;
		this.valor = valor;
	}

	 public Metrica(Tipo tipo, Double valor) {
		this.tipo = tipo;
		this.valor = valor!=null?valor:0;
	}

	public int getClienteID() {
		return clienteID;
	}
	 
	 public int getMes() {
		return mes;
	}
	 
	 public double getValor() {
		return valor;
	}
	 
	 public Tipo getTipo() {
		return tipo;
	}
	 
	 public String getEstado() {
		return estado;
	}
	 
	 public String getCidade() {
		return cidade;
	}
	 
	 public String getSegmento() {
		return segmento;
	}
}
