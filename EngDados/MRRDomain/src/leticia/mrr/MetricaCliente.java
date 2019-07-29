package leticia.mrr;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class MetricaCliente implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	 private int id;

	private int clienteID;
	private int mes;
	private Tipo tipo;
	private double valor;
	
	public MetricaCliente() {
 	}

	public MetricaCliente(int cliente, int mes, Tipo tipo, double valor) {
		this.clienteID = cliente;
		this.mes = mes;
		this.tipo = tipo;
		this.valor = valor;
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
}
