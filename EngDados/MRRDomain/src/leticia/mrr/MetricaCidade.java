package leticia.mrr;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class MetricaCidade {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	private String cidade;

	private Tipo tipo;

	private int mes;

	private double valor;

	public MetricaCidade() {
	}

	public MetricaCidade(String cidade, Tipo tipo, int mes) {
		this.cidade = cidade;
		this.tipo = tipo;
		this.mes = mes;
	}

	public MetricaCidade(String cidade, Tipo tipo, int mes, double valor) {
		this.cidade = cidade;
		this.tipo = tipo;
		this.mes = mes;
		this.valor = valor;
	}

	public String getCidade() {
		return cidade;
	}

	public Tipo getTipo() {
		return tipo;
	}

	public int getMes() {
		return mes;
	}

	public double getValor() {
		return valor;
	}

	public void add(double valor) {
		this.valor += valor;

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cidade == null) ? 0 : cidade.hashCode());
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
		MetricaCidade other = (MetricaCidade) obj;
		if (cidade == null) {
			if (other.cidade != null)
				return false;
		} else if (!cidade.equals(other.cidade))
			return false;
		if (mes != other.mes)
			return false;
		if (tipo != other.tipo)
			return false;
		return true;
	}

}
