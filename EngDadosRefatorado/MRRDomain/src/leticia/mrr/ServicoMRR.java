package leticia.mrr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import leticia.cliente.Cliente;
import leticia.financeiro.Pagamento;
import leticia.infra.DAO;

public class ServicoMRR {

	private static SimpleDateFormat DF = new SimpleDateFormat("dd/MM/yyyy");

	public void pagamentoEfetuadoAsynch(int cliente, String data, String plano, double valor) throws ParseException {
	}

	public void pagamentoEfetuado(int cliente, String data, String plano, double valor) throws ParseException {
		DAO dao = new DAO();
		dao.begin();

		// Registra Pagamento
		Date dataPagamento = DF.parse(data);

		Pagamento pagamento = new Pagamento(cliente, dataPagamento, plano, valor);
		registraPagamento(dao, pagamento);

		dao.commit();

	}

	private void registraPagamento(DAO dao, Pagamento pagamento) {
		List<?> l = dao.get(Pagamento.class, "cliente = ?1 and dataPagamento = ?2", pagamento.getCliente(),
				pagamento.getDataPagamento());

		if (l.size() == 0) {
			trataMudancaMes(dao,pagamento.getDataPagamento());

			dao.persist(pagamento);

			registraParcelas(dao, pagamento);

		}
	}

	private void trataMudancaMes(DAO dao, Date data) {
		Parametros parametro = getParametros(dao,data);
		int mesPagamento = getMes(data);

		int mesAposFechamento = getMesPosterior(parametro.getUltimoMesFechado());
		
		if (mesPagamento > mesAposFechamento) {
			calculaCancelled(dao, parametro, mesPagamento);
		}

	}

	private void calculaCancelled(DAO dao, Parametros parametro, int mesPagamento) {
	 
		int mesFechado = parametro.getUltimoMesFechado();
		int mesPosterior = getMesPosterior(mesFechado);

		while (mesPosterior < mesPagamento) {

			List<Parcela> parcelasMesFechamento = dao.get(Parcela.class, "mes = ?1 and clienteID not in (select clienteID from Parcela where mes = ?2)  ",
				 mesFechado,mesPosterior);

			for (Parcela parcelaMesFechado : parcelasMesFechamento) {
 					Cliente cliente = getCliente (dao,parcelaMesFechado.getClienteID());
					Metrica m = new Metrica(cliente, mesPosterior, Tipo.Cancelled_MRR, parcelaMesFechado.getValor());
					dao.persist(m);

				 
			}
			mesFechado = mesPosterior;
			mesPosterior = getMesPosterior(mesFechado);
		}
		
		parametro.setUltimoMesFechado(mesFechado);
		dao.persist(parametro);
 	}

	private Cliente getCliente(DAO dao, int clienteID) {
		Cliente cliente = dao.find(Cliente.class, clienteID);
		if (cliente == null) {
			cliente = new Cliente(clienteID);
		}
		return cliente;
	}

	private Parametros getParametros(DAO dao, Date data) {
		  List<Parametros> parametros = dao.get(Parametros.class);
		
		  if (parametros.size()  == 0) {
			  Parametros p = new Parametros();
			  int mesAnt = getMesAnterior(data);
			  p.setUltimoMesFechado(mesAnt);
			  dao.persist(p);
			  return p;
		  }
		return parametros.get(0);
	}

	private void registraParcelas(DAO dao, Pagamento pagamento) {
		Date dataPagamento = pagamento.getDataPagamento();
		int clienteID = pagamento.getCliente();

		Cliente cliente = getCliente (dao,pagamento.getCliente());
		 

		// Registra Parcelas

		// quantas parcelas tem o plano
		int qtdParcelas = Integer.valueOf(pagamento.getPlano().split("\\/")[1]);

		// valor de cada parcela
		Double valorParcela = pagamento.getValor() / qtdParcelas;

		// Trata New, Expansion, Contraction, Resurrected
		registraMudancaMRR(clienteID, dao, dataPagamento, valorParcela);

		Calendar c = Calendar.getInstance();
		c.setTime(dataPagamento);

		for (int i = 0; i < qtdParcelas; i++) {
			Parcela parcelaAtual = new Parcela(clienteID, dataPagamento, valorParcela);
			dao.persist(parcelaAtual);

			// metrica MRR

			Metrica m = new Metrica(cliente, parcelaAtual.getMes(), Tipo.MRR, parcelaAtual.getValor());
			dao.persist(m);

			// proximo mês do plano
			c.add(Calendar.MONTH, 1);
			dataPagamento = c.getTime();

		}
	}

	private void registraMudancaMRR(int clienteID, DAO dao, Date dataPagamento, Double valorParcela) {
		boolean novo = false;

		Cliente cliente = getCliente (dao,clienteID);

		Parcela parcelaAtual = new Parcela(clienteID, dataPagamento, valorParcela);

		// registra new MRR
		if (dao.get(Parcela.class, "clienteID = ?1", clienteID).size() == 0) {
			Metrica m = new Metrica(cliente, parcelaAtual.getMes(), Tipo.New_MRR, valorParcela);
			dao.persist(m);
		} else {

			int mesAnterior = getMesAnterior(dataPagamento);

			List<Parcela> parcelas = dao.get(Parcela.class, "clienteID = ?1 and mes = ?2", clienteID, mesAnterior);
			if (parcelas.size() == 1) { // comprou no mes anterior
				Parcela parcelaAnterior = parcelas.get(0);
				double delta = parcelaAtual.getValor() - parcelaAnterior.getValor();

				if (delta > 0) {
					/// Expansion MRR

					Metrica m = new Metrica(cliente, parcelaAtual.getMes(), Tipo.Expansion_MRR, delta);
					dao.persist(m);

				} else if (delta < 0) {
					// Contraction MRR
					Metrica m = new Metrica(cliente, parcelaAtual.getMes(), Tipo.Contraction_MRR, delta * -1);
					dao.persist(m);

				}
			} else { // sem compra mês Anterior
				if (!novo) {
					// Resurrected MRR
					Metrica m = new Metrica(cliente, parcelaAtual.getMes(), Tipo.Resurrected_MRR, valorParcela);
					dao.persist(m);

				}
			}
		}
	}

	private int getMesAnterior(Date data) {

		Calendar c = Calendar.getInstance();
		c.setTime(data);
		c.add(Calendar.MONTH, -1);

		int ano = c.get(Calendar.YEAR);
		int mes = c.get(Calendar.MONTH) + 1;

		return ano * 100 + mes;
	}

	private int getMes(Date data) {

		Calendar c = Calendar.getInstance();
		c.setTime(data);

		int ano = c.get(Calendar.YEAR);
		int mes = c.get(Calendar.MONTH) + 1;

		return ano * 100 + mes;
	}

	private int getMesAnterior(int mesAtual) {

		int ano = mesAtual / 100;
		int mes = mesAtual % 100 - 1;

		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.MONTH, mes);
		c.set(Calendar.YEAR, ano);

		c.add(Calendar.MONTH, -1);

		ano = c.get(Calendar.YEAR);
		mes = c.get(Calendar.MONTH) + 1;

		return ano * 100 + mes;

	}

	private int getMesPosterior(int mesAtual) {

		int ano = mesAtual / 100;
		int mes = mesAtual % 100 - 1;

		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.MONTH, mes);
		c.set(Calendar.YEAR, ano);

		c.add(Calendar.MONTH, 1);

		ano = c.get(Calendar.YEAR);
		mes = c.get(Calendar.MONTH) + 1;

		return ano * 100 + mes;

	}

	public Metrica[] getMetricas(int cliente, int mes) {
		DAO dao = new DAO();
		dao.begin();

		trataMudancaMes(dao,new Date());

		List<Metrica> metricas = dao.get(Metrica.class, "clienteID = ?1 and mes = ?2", cliente, mes);

		Metrica[] ret = new Metrica[metricas.size()];
		metricas.toArray(ret);
		dao.commit();

		return ret;

	}

	public Metrica[] getMetricasEmpresa(int mes) {

		DAO dao = new DAO();
		dao.begin();

		trataMudancaMes(dao,new Date());

		List<Metrica> ret = new ArrayList<Metrica>();

		Double valor = (Double) dao.get("select sum (valor) from Metrica where mes = ?1 and tipo = ?2", mes, Tipo.MRR)
				.get(0);

		ret.add(new Metrica(Tipo.MRR, valor));

		valor = (Double) dao
				.get("select sum (valor) from Metrica where mes = ?1 and tipo = ?2", mes, Tipo.Contraction_MRR).get(0);

		ret.add(new Metrica(Tipo.Contraction_MRR, valor));

		valor = (Double) dao
				.get("select sum (valor) from Metrica where mes = ?1 and tipo = ?2", mes, Tipo.Expansion_MRR).get(0);

		ret.add(new Metrica(Tipo.Expansion_MRR, valor));

		valor = (Double) dao
				.get("select sum (valor) from Metrica where mes = ?1 and tipo = ?2", mes, Tipo.Resurrected_MRR).get(0);

		ret.add(new Metrica(Tipo.Resurrected_MRR, valor));

		valor = (Double) dao.get("select sum (valor) from Metrica where mes = ?1 and tipo = ?2", mes, Tipo.New_MRR)
				.get(0);

		ret.add(new Metrica(Tipo.New_MRR, valor));

		valor = (Double) dao
				.get("select sum (valor) from Metrica where mes = ?1 and tipo = ?2", mes, Tipo.Cancelled_MRR).get(0);

		ret.add(new Metrica(Tipo.Cancelled_MRR, valor));

		Metrica[] r = new Metrica[ret.size()];
		ret.toArray(r);
		dao.commit();

		return r;

	}

	public Metrica[] getMetricasCidade(String cidade, int mes) {
		DAO dao = new DAO();
		dao.begin();
		
		trataMudancaMes(dao,new Date());

		List<Metrica> ret = new ArrayList<Metrica>();

		Double valor = (Double) dao.get("select sum (valor) from Metrica where mes = ?1 and tipo = ?2 and cidade = ?3",
				mes, Tipo.MRR, cidade).get(0);

		ret.add(new Metrica(Tipo.MRR, valor));

		valor = (Double) dao.get("select sum (valor) from Metrica where mes = ?1 and tipo = ?2 and cidade = ?3", mes,
				Tipo.Contraction_MRR, cidade).get(0);

		ret.add(new Metrica(Tipo.Contraction_MRR, valor));

		valor = (Double) dao.get("select sum (valor) from Metrica where mes = ?1 and tipo = ?2 and cidade = ?3", mes,
				Tipo.Expansion_MRR, cidade).get(0);

		ret.add(new Metrica(Tipo.Expansion_MRR, valor));

		valor = (Double) dao.get("select sum (valor) from Metrica where mes = ?1 and tipo = ?2 and cidade = ?3", mes,
				Tipo.Resurrected_MRR, cidade).get(0);

		ret.add(new Metrica(Tipo.Resurrected_MRR, valor));

		valor = (Double) dao.get("select sum (valor) from Metrica where mes = ?1 and tipo = ?2 and cidade = ?3", mes,
				Tipo.New_MRR, cidade).get(0);

		ret.add(new Metrica(Tipo.New_MRR, valor));

		valor = (Double) dao.get("select sum (valor) from Metrica where mes = ?1 and tipo = ?2 and cidade = ?3", mes,
				Tipo.Cancelled_MRR, cidade).get(0);

		ret.add(new Metrica(Tipo.Cancelled_MRR, valor));

		Metrica[] r = new Metrica[ret.size()];
		ret.toArray(r);
		dao.commit();

		return r;

	}

	 

	public Metrica[] getMetricasEstado(String estado, int mes) {
		DAO dao = new DAO();
		dao.begin();

		trataMudancaMes(dao,new Date());

		List<Metrica> ret = new ArrayList<Metrica>();

		Double valor = (Double) dao.get("select sum (valor) from Metrica where mes = ?1 and tipo = ?2 and estado = ?3",
				mes, Tipo.MRR, estado).get(0);

		ret.add(new Metrica(Tipo.MRR, valor));

		valor = (Double) dao.get("select sum (valor) from Metrica where mes = ?1 and tipo = ?2 and estado = ?3", mes,
				Tipo.Contraction_MRR, estado).get(0);

		ret.add(new Metrica(Tipo.Contraction_MRR, valor));

		valor = (Double) dao.get("select sum (valor) from Metrica where mes = ?1 and tipo = ?2 and estado = ?3", mes,
				Tipo.Expansion_MRR, estado).get(0);

		ret.add(new Metrica(Tipo.Expansion_MRR, valor));

		valor = (Double) dao.get("select sum (valor) from Metrica where mes = ?1 and tipo = ?2 and estado = ?3", mes,
				Tipo.Resurrected_MRR, estado).get(0);

		ret.add(new Metrica(Tipo.Resurrected_MRR, valor));

		valor = (Double) dao.get("select sum (valor) from Metrica where mes = ?1 and tipo = ?2 and estado = ?3", mes,
				Tipo.New_MRR, estado).get(0);

		ret.add(new Metrica(Tipo.New_MRR, valor));

		valor = (Double) dao.get("select sum (valor) from Metrica where mes = ?1 and tipo = ?2 and estado = ?3", mes,
				Tipo.Cancelled_MRR, estado).get(0);

		ret.add(new Metrica(Tipo.Cancelled_MRR, valor));

		Metrica[] r = new Metrica[ret.size()];
		ret.toArray(r);
		dao.commit();

		return r;

	}

	public Metrica[] getMetricasSegmento(String segmento, int mes) {
		DAO dao = new DAO();
		dao.begin();

		trataMudancaMes(dao,new Date());

		List<Metrica> ret = new ArrayList<Metrica>();

		Double valor = (Double) dao
				.get("select sum (valor) from Metrica where mes = ?1 and tipo = ?2 and segmento = ?3", mes, Tipo.MRR,
						segmento)
				.get(0);

		ret.add(new Metrica(Tipo.MRR, valor));

		valor = (Double) dao.get("select sum (valor) from Metrica where mes = ?1 and tipo = ?2 and segmento = ?3", mes,
				Tipo.Contraction_MRR, segmento).get(0);

		ret.add(new Metrica(Tipo.Contraction_MRR, valor));

		valor = (Double) dao.get("select sum (valor) from Metrica where mes = ?1 and tipo = ?2 and segmento = ?3", mes,
				Tipo.Expansion_MRR, segmento).get(0);

		ret.add(new Metrica(Tipo.Expansion_MRR, valor));

		valor = (Double) dao.get("select sum (valor) from Metrica where mes = ?1 and tipo = ?2 and segmento = ?3", mes,
				Tipo.Resurrected_MRR, segmento).get(0);

		ret.add(new Metrica(Tipo.Resurrected_MRR, valor));

		valor = (Double) dao.get("select sum (valor) from Metrica where mes = ?1 and tipo = ?2 and segmento = ?3", mes,
				Tipo.New_MRR, segmento).get(0);

		ret.add(new Metrica(Tipo.New_MRR, valor));

		valor = (Double) dao.get("select sum (valor) from Metrica where mes = ?1 and tipo = ?2 and segmento = ?3", mes,
				Tipo.Cancelled_MRR, segmento).get(0);

		ret.add(new Metrica(Tipo.Cancelled_MRR, valor));

		Metrica[] r = new Metrica[ret.size()];
		ret.toArray(r);
		dao.commit();

		return r;

	}

	public void uploadClientes(Cliente[] clientes) {
		DAO dao = new DAO();
		dao.begin();

		int i = 0;
		for (Cliente cliente : clientes) {
			dao.merge(cliente);

			if (++i % 100 == 0) {
				System.out.println(i);
				dao.commit();
				dao.begin();
			}
		}

		dao.commit();

	}

	public void uploadPagamentos(List<Pagamento> pagamentos) {
		
		Collections.sort(pagamentos)
		;
		DAO dao = new DAO();
		dao.begin();

		int i = 0;
		for (Pagamento pagamento : pagamentos) {
			registraPagamento(dao, pagamento);

			if (++i % 100 == 0) {
				System.out.println(i);
				dao.commit();
				dao.begin();
			}
		}

		dao.commit();

	}

}
