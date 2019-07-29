package leticia.mrr;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;


@Entity
public class MetricaSegmento {
	
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	int id;
	
	private String segmento;
	
	private Tipo tipo;
	
	private int mes;
	
	private double valor;

	public MetricaSegmento() {
 	}
	
	public MetricaSegmento(String segmento, Tipo tipo, int mes, double valor) {
		this.segmento = segmento;
		this.tipo = tipo;
		this.mes = mes;
		this.valor = valor;
	}

	 

	public MetricaSegmento(String segmento, Tipo tipo, int mes) {
		this.segmento = segmento;
		this.tipo = tipo;
		this.mes = mes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + mes;
		result = prime * result + ((segmento == null) ? 0 : segmento.hashCode());
		result = prime * result + ((tipo == null) ? 0 : tipo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
 		MetricaSegmento other = (MetricaSegmento) obj;
		if (mes != other.mes)
			return false;
		if (segmento == null) {
			if (other.segmento != null)
				return false;
		} else if (!segmento.equals(other.segmento))
			return false;
		if (tipo != other.tipo)
			return false;
		return true;
	}

	public void add(double valor) {
		this.valor += valor;
		
	}

	
	public Tipo getTipo() {
		return tipo;
	}
	
	public int getMes() {
		return mes;
	}
	
	public String getSegmento() {
		return segmento;
	}
	
	
	public double getValor() {
		return valor;
	}
}
