package leticia.mrr;

public class PagamentoUpload {
	private int cliente;
	private String data;
	private double valor;
	private String plano;
	
	
	public int getCliente() {
		return cliente;
	}
	
	public String getData() {
		return data;
	}
	
	
	public double getValor() {
		return valor;
	}
	
	public String getPlano() {
		return plano;
	}
	
	public void setCliente(int cliente) {
		this.cliente = cliente;
	}
	
	public void setData(String data) {
		this.data = data;
	}
	
	public void setPlano(String plano) {
		this.plano = plano;
	}
	
	public void setValor(double valor) {
		this.valor = valor;
	}
	
}
