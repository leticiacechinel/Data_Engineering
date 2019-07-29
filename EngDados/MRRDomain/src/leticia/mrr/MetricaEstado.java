package leticia.mrr;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity

public class MetricaEstado {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	int id;
	
	private String estado;
	private Tipo tipo;
	private int mes;
	private double valor;
	
	public MetricaEstado() {
 	}

	public MetricaEstado(String estado, Tipo tipo, int mes, double valor) {
		this.estado = estado;
		this.tipo = tipo;
		this.mes = mes;
		this.valor = valor;
	}

	 

	public MetricaEstado(String estado, Tipo tipo, int mes) {
		this.estado = estado;
		this.tipo = tipo;
		this.mes = mes;
				
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((estado == null) ? 0 : estado.hashCode());
		result = prime * result + mes;
		result = prime * result + ((tipo == null) ? 0 : tipo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		 
		MetricaEstado other = (MetricaEstado) obj;
		if (estado == null) {
			if (other.estado != null)
				return false;
		} else if (!estado.equals(other.estado))
			return false;
		if (mes != other.mes)
			return false;
		if (tipo != other.tipo)
			return false;
		return true;
	}

	public void add(double valor) {
		this.valor+=valor;
		
	}
	
	public String getEstado() {
		return estado;
	}
	
	public int getMes() {
		return mes;
	}
	
	public Tipo getTipo() {
		return tipo;
	}
	
	public double getValor() {
		return valor;
	}

}
