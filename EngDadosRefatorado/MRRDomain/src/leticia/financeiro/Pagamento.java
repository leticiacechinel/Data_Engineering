package leticia.financeiro;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Pagamento implements Comparable<Pagamento>{
	private static SimpleDateFormat DF = new SimpleDateFormat("dd/MM/yyyy");

	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	
	private int cliente;
 

	private Date dataPagamento;

	private String plano;

	private double valor;
	
	public Pagamento() {
	}

	public Pagamento(int cliente, Date data) {
		this.cliente = cliente;
		this.dataPagamento = data;
	}

	
	
	
	
	public Pagamento(int cliente, Date dataPagamento, String plano, double valor) {
		this.cliente = cliente;
		this.dataPagamento = dataPagamento;
		this.plano = plano;
		this.valor = valor;
	}

	public Pagamento(int cliente, String data, String plano, double valor) throws ParseException {
		this(cliente,DF.parse(data),plano,valor);
	}

	public String getPlano() {
		return plano;
	}
	
	public double getValor() {
		return valor;
	}


	public int getCliente() {
		return cliente;
	}
	
	public Date getDataPagamento() {
		return dataPagamento;
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cliente;
		result = prime * result + ((dataPagamento == null) ? 0 : dataPagamento.hashCode());
		return result;
	}





	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		
		
 		Pagamento other = (Pagamento) obj;
		if (cliente != other.cliente)
			return false;
		if (dataPagamento == null) {
			if (other.dataPagamento != null)
				return false;
		} else if (!dataPagamento.equals(other.dataPagamento))
			return false;
		return true;
	}

	@Override
	public int compareTo(Pagamento o) {
		return dataPagamento.compareTo(o.getDataPagamento());
	}


 
}
