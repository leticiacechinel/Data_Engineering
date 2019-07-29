package leticia.mrr;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class MetricaEmpresa {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;

	private int mes;
	private Tipo tipo;
	private double valor;
	
	public MetricaEmpresa() {
 	}
	
	public MetricaEmpresa(Tipo tipo, int mes, double valor) {
		this.tipo = tipo;
		this.mes = mes;
		this.valor = valor;
	}

	public MetricaEmpresa(Tipo tipo, int mes) {
		this.tipo = tipo;
		this.mes = mes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
 		MetricaEmpresa other = (MetricaEmpresa) obj;
		if (mes != other.mes)
			return false;
		if (tipo != other.tipo)
			return false;
		return true;
	}

	public Tipo getTipo() {
 		return tipo;
	}

	public double getValor() {
 
		return valor;
	}

	public void add(double valor) {
		this.valor += valor;
		
	}
	
	public int getMes() {
		return mes;
	}

}
