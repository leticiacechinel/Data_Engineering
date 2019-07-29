package leticia.mrr;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Parcela {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	 private int id;
	
     private int clienteID;
     private Date data;
     private double valor;
     private int mes;
     
     public Parcela() {
		 
	}
     
     public Parcela(int cliente, Date dataPagamento, double valorParcela) {
		this.clienteID = cliente;
		this.data = dataPagamento;
		this.valor = valorParcela;
		this.mes = calculaMes(dataPagamento);
	}

	private int calculaMes(Date data) {
 		Calendar c = Calendar.getInstance();
		c.setTime(data);
		
		int ano = c.get(Calendar.YEAR);
		int mes = c.get(Calendar.MONTH)+1;
		
		return ano*100+mes;
	}

	public int getClienteID() {
		return clienteID;
	}
     
    public Date getData() {
		return data;
	}
    
    
    public double getValor() {
		return valor;
	}

	public int getMes() {
		return mes;
	}
}
